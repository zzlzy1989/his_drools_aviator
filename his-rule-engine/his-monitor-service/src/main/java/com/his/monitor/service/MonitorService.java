package com.his.monitor.service;

import com.his.monitor.dto.MonitorMetricsVO;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
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

    private final MeterRegistry meterRegistry;

    // 内存中的指标存储（生产环境建议用 Redis）
    private final AtomicLong executionTotal = new AtomicLong(0);
    private final AtomicLong executionSuccess = new AtomicLong(0);
    private final AtomicLong executionFailed = new AtomicLong(0);
    private final Map<String, AtomicLong> ruleHitCounts = new ConcurrentHashMap<>();
    private final List<MonitorMetricsVO.AlertItem> recentAlerts = new Vector<>();
    private volatile long p99DurationMs = 0;
    private volatile long p95DurationMs = 0;

    public MonitorService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // 初始化计数器
        Counter.builder("rule_execution_total")
                .description("规则执行总次数")
                .register(meterRegistry);

        Counter.builder("rule_execution_success")
                .description("规则执行成功次数")
                .register(meterRegistry);

        Counter.builder("rule_execution_failed")
                .description("规则执行失败次数")
                .register(meterRegistry);

        log.info("MonitorService initialized");
    }

    /**
     * 记录规则执行
     */
    public void recordExecution(boolean success, long durationMs) {
        executionTotal.incrementAndGet();
        if (success) {
            executionSuccess.incrementAndGet();
        } else {
            executionFailed.incrementAndGet();
        }

        // 更新 P99/P95 (简化版，实际可用滑动窗口)
        updateDurationStats(durationMs);
    }

    /**
     * 记录规则命中
     */
    public void recordRuleHit(String ruleKey) {
        ruleHitCounts.computeIfAbsent(ruleKey, k -> new AtomicLong(0)).incrementAndGet();
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
        vo.setFormulaHitRate(BigDecimal.valueOf(99.2).setScale(2, RoundingMode.HALF_UP)); // TODO: 真实计算
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

    private void updateDurationStats(long durationMs) {
        // 简化实现，实际可用 Histogram 的滑动窗口
        if (durationMs > p99DurationMs) {
            p99DurationMs = durationMs;
        }
        if (durationMs > p95DurationMs && durationMs <= p99DurationMs) {
            p95DurationMs = durationMs;
        }
    }
}