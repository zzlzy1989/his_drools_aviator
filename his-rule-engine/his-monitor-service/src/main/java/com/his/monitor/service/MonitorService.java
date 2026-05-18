package com.his.monitor.service;

import com.his.monitor.dto.MonitorMetricsVO;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 监控服务
 */
@Slf4j
@Service
public class MonitorService {

    private final ApplicationEventPublisher eventPublisher;
    private final AlertRuleService alertRuleService;
    private final MeterRegistry meterRegistry;

    // Micrometer counters
    private final Counter totalCounter;
    private final Counter successCounter;
    private final Counter failedCounter;
    private final Counter formulaHitCounter;

    // 内存中的指标存储（生产环境建议用 Redis）
    private final AtomicLong executionTotal = new AtomicLong(0);
    private final AtomicLong executionSuccess = new AtomicLong(0);
    private final AtomicLong executionFailed = new AtomicLong(0);
    private final AtomicLong formulaHits = new AtomicLong(0);
    private final Map<String, AtomicLong> ruleHitCounts = new ConcurrentHashMap<>();
    private final List<MonitorMetricsVO.AlertItem> recentAlerts = new Vector<>();
    private volatile long p99DurationMs = 0;
    private volatile long p95DurationMs = 0;

    public MonitorService(ApplicationEventPublisher eventPublisher, AlertRuleService alertRuleService, MeterRegistry meterRegistry) {
        this.eventPublisher = eventPublisher;
        this.alertRuleService = alertRuleService;
        this.meterRegistry = meterRegistry;

        // 初始化 Micrometer counters
        this.totalCounter = Counter.builder("his_rule_execution_total")
                .description("总执行次数")
                .register(meterRegistry);
        this.successCounter = Counter.builder("his_rule_execution_success")
                .description("成功执行次数")
                .register(meterRegistry);
        this.failedCounter = Counter.builder("his_rule_execution_failed")
                .description("失败执行次数")
                .register(meterRegistry);
        this.formulaHitCounter = Counter.builder("his_formula_hit_total")
                .description("公式命中次数")
                .register(meterRegistry);

        // 注册 gauges
        Gauge.builder("his_rule_p99_duration_ms", () -> p99DurationMs)
                .description("P99 执行延迟")
                .register(meterRegistry);
        Gauge.builder("his_rule_p95_duration_ms", () -> p95DurationMs)
                .description("P95 执行延迟")
                .register(meterRegistry);
        Gauge.builder("his_active_rule_count", () -> ruleHitCounts.size())
                .description("活跃规则数")
                .register(meterRegistry);
    }

    @PostConstruct
    public void init() {
        log.info("MonitorService initialized");
    }

    /**
     * 记录规则执行
     */
    public void recordExecution(boolean success, long durationMs) {
        totalCounter.increment();
        if (success) {
            successCounter.increment();
        } else {
            failedCounter.increment();
        }

        // 更新 P99/P95 (简化版，实际可用滑动窗口)
        updateDurationStats(durationMs);

        // 检查是否触发告警（失败率 > 5% 或 执行时间 > 100ms）
        long total = executionTotal.get();
        long failed = executionFailed.get();
        if (total > 0) {
            double failRate = failed * 100.0 / total;
            // 失败率超过 5%
            if (failRate > 5.0) {
                alertRuleService.checkAlerts("execution_fail_rate", BigDecimal.valueOf(failRate));
            }
        }
        // 执行时间超过 100ms
        if (durationMs > 100) {
            alertRuleService.checkAlerts("execution_duration_ms", BigDecimal.valueOf(durationMs));
        }
    }

    /**
     * 记录规则命中
     */
    public void recordRuleHit(String ruleKey) {
        ruleHitCounts.computeIfAbsent(ruleKey, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 记录公式执行
     */
    public void recordFormulaHit() {
        formulaHitCounter.increment();
    }

    /**
     * 记录告警
     */
    public void recordAlert(String alertType, String message, String level) {
        MonitorMetricsVO.AlertItem alert = new MonitorMetricsVO.AlertItem();
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setAlertType(alertType);
        alert.setMessage(message);
        alert.setLevel(level);
        alert.setAlertTime(LocalDateTime.now());
        recentAlerts.add(0, alert);
        // 保持最近 100 条
        if (recentAlerts.size() > 100) {
            recentAlerts.remove(recentAlerts.size() - 1);
        }
    }

    /**
     * 获取当前指标
     */
    public MonitorMetricsVO getMetrics() {
        MonitorMetricsVO vo = new MonitorMetricsVO();

        long total = executionTotal.get();
        long success = executionSuccess.get();

        vo.setExecutionTotal(total);
        vo.setExecutionSuccess(success);
        vo.setExecutionFailed(executionFailed.get());
        vo.setSuccessRate(total > 0
                ? BigDecimal.valueOf(success * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        vo.setP99DurationMs(p99DurationMs);
        vo.setP95DurationMs(p95DurationMs);
        vo.setAvgDurationMs(p95DurationMs / 2); // 简化估算
        vo.setActiveRuleCount(ruleHitCounts.size());
        // 基于实际公式执行记录计算命中率
        long formulaTotal = formulaHits.get();
        long ruleTotal = ruleHitCounts.values().stream().mapToLong(AtomicLong::get).sum();
        if (formulaTotal > 0) {
            long totalExec = executionTotal.get();
            if (totalExec > 0) {
                double hitRate = (double) formulaHits.get() * 100.0 / totalExec;
                vo.setFormulaHitRate(BigDecimal.valueOf(hitRate).setScale(2, RoundingMode.HALF_UP));
            } else {
                vo.setFormulaHitRate(BigDecimal.ZERO);
            }
        } else {
            // 无数据时返回 null，前端显示"暂无数据"而非硬编码假数据
            vo.setFormulaHitRate(null);
        }
        vo.setLastUpdateTime(LocalDateTime.now());

        // TOP 10 规则
        List<MonitorMetricsVO.RuleStatItem> topRules = ruleHitCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(10)
                .map(e -> {
                    MonitorMetricsVO.RuleStatItem item = new MonitorMetricsVO.RuleStatItem();
                    item.setRuleKey(e.getKey());
                    item.setRuleName(e.getKey());
                    item.setHitCount(e.getValue().get());
                    item.setHitRate(total > 0
                            ? BigDecimal.valueOf(e.getValue().get() * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO);
                    return item;
                })
                .toList();
        vo.setTopRules(topRules);

        // 最近告警
        vo.setRecentAlerts(recentAlerts.stream().limit(10).toList());

        return vo;
    }

    /**
     * 发布指标更新事件（供 WebSocket 推送使用）
     */
    public void publishMetricsUpdate() {
        eventPublisher.publishEvent(new MetricsUpdateEvent(this, getMetrics()));
    }

    /**
     * 每5秒通过事件发布推送指标（仅当有活跃连接时）
     * 注意：实际推送由 MetricsUpdateListener 处理
     */
    @Scheduled(fixedRate = 5000)
    public void checkAndPushMetrics() {
        // 发布事件，让监听器决定是否推送
        publishMetricsUpdate();
    }

    private void updateDurationStats(long durationMs) {
        // 简化实现，实际可用 Histogram 的滑动窗口
        if (durationMs > p99DurationMs) {
            p99DurationMs = durationMs;
        }
        if (durationMs > p95DurationMs && durationMs <= p99DurationMs) {
            p95DurationMs = durationMs;
        }
    }

    /**
     * 指标更新事件
     */
    public record MetricsUpdateEvent(Object source, MonitorMetricsVO metrics) {}
}