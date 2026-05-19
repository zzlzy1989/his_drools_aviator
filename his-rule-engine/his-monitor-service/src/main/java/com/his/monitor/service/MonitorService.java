package com.his.monitor.service;

import com.his.monitor.dto.MonitorMetricsVO;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
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
 * <p>支持 Redis 持久化和内存降级</p>
 */
@Slf4j
@Service
public class MonitorService {

    private final ApplicationEventPublisher eventPublisher;
    private final AlertRuleService alertRuleService;
    private final MeterRegistry meterRegistry;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis 持久化存储（可选）
    private RedisMetricsStore redisMetricsStore;

    // Micrometer counters
    private final Counter totalCounter;
    private final Counter successCounter;
    private final Counter failedCounter;
    private final Counter formulaHitCounter;

    // 内存中的指标存储（降级使用）
    private final AtomicLong executionTotal = new AtomicLong(0);
    private final AtomicLong executionSuccess = new AtomicLong(0);
    private final AtomicLong executionFailed = new AtomicLong(0);
    private final AtomicLong formulaHits = new AtomicLong(0);
    private final Map<String, AtomicLong> ruleHitCounts = new ConcurrentHashMap<>();
    private final List<MonitorMetricsVO.AlertItem> recentAlerts = new Vector<>();
    private volatile long p99DurationMs = 0;
    private volatile long p95DurationMs = 0;

    // Redis 可用性标志
    private volatile boolean redisAvailable = false;

    public MonitorService(ApplicationEventPublisher eventPublisher,
                         AlertRuleService alertRuleService,
                         MeterRegistry meterRegistry,
                         RedisTemplate<String, Object> redisTemplate) {
        this.eventPublisher = eventPublisher;
        this.alertRuleService = alertRuleService;
        this.meterRegistry = meterRegistry;
        this.redisTemplate = redisTemplate;

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
        // 尝试初始化 Redis 存储
        try {
            redisMetricsStore = new RedisMetricsStore(redisTemplate);
            // 测试 Redis 连接
            redisTemplate.opsForValue().get("his:monitor:test");
            redisAvailable = true;
            log.info("MonitorService initialized with Redis persistence");
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("Redis不可用，使用内存存储: {}", e.getMessage());
            log.info("MonitorService initialized with in-memory storage");
        }
    }

    /**
     * 记录规则执行
     */
    public void recordExecution(boolean success, long durationMs) {
        totalCounter.increment();

        if (redisAvailable) {
            try {
                redisMetricsStore.recordExecution(success, durationMs);
            } catch (Exception e) {
                log.warn("Redis记录失败，降级到内存: {}", e.getMessage());
                redisAvailable = false;
                recordExecutionMemory(success, durationMs);
            }
        } else {
            recordExecutionMemory(success, durationMs);
        }
    }

    private void recordExecutionMemory(boolean success, long durationMs) {
        executionTotal.incrementAndGet();
        if (success) {
            successCounter.increment();
            executionSuccess.incrementAndGet();
        } else {
            failedCounter.increment();
            executionFailed.incrementAndGet();
        }
        updateDurationStats(durationMs);

        // 检查告警
        checkAlerts(executionTotal.get(), executionFailed.get(), durationMs);
    }

    private void checkAlerts(long total, long failed, long durationMs) {
        if (total > 0) {
            double failRate = failed * 100.0 / total;
            if (failRate > 5.0) {
                alertRuleService.checkAlerts("execution_fail_rate", BigDecimal.valueOf(failRate));
            }
        }
        if (durationMs > 100) {
            alertRuleService.checkAlerts("execution_duration_ms", BigDecimal.valueOf(durationMs));
        }
    }

    /**
     * 记录规则命中
     */
    public void recordRuleHit(String ruleKey) {
        ruleHitCounts.computeIfAbsent(ruleKey, k -> new AtomicLong(0)).incrementAndGet();

        if (redisAvailable) {
            try {
                redisMetricsStore.recordRuleHit(ruleKey);
            } catch (Exception e) {
                log.warn("Redis记录规则命中失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 记录公式执行
     */
    public void recordFormulaHit() {
        formulaHitCounter.increment();
        formulaHits.incrementAndGet();

        if (redisAvailable) {
            try {
                redisMetricsStore.recordFormulaHit();
            } catch (Exception e) {
                log.warn("Redis记录公式命中失败: {}", e.getMessage());
            }
        }
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
        if (recentAlerts.size() > 100) {
            recentAlerts.remove(recentAlerts.size() - 1);
        }

        if (redisAvailable) {
            try {
                redisMetricsStore.recordAlert(alertType, message, level);
            } catch (Exception e) {
                log.warn("Redis记录告警失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 获取当前指标
     */
    public MonitorMetricsVO getMetrics() {
        MonitorMetricsVO vo = new MonitorMetricsVO();

        long total;
        long success;
        long failed;

        // 从数据源获取指标（使用数组避免lambda捕获问题）
        long[] metrics = new long[3]; // [0]=total, [1]=success, [2]=failed

        if (redisAvailable) {
            try {
                metrics[0] = redisMetricsStore.getExecutionTotal();
                metrics[1] = redisMetricsStore.getExecutionSuccess();
                metrics[2] = redisMetricsStore.getExecutionFailed();
                p99DurationMs = redisMetricsStore.getP99Duration();
                p95DurationMs = redisMetricsStore.getP95Duration();

                // 从 Redis 获取规则命中
                Map<String, Long> redisRuleHits = redisMetricsStore.getRuleHits();
                for (Map.Entry<String, Long> entry : redisRuleHits.entrySet()) {
                    ruleHitCounts.computeIfAbsent(entry.getKey(), k -> new AtomicLong(0))
                            .set(entry.getValue());
                }

                // 从 Redis 获取告警
                List<Map<String, Object>> redisAlerts = redisMetricsStore.getRecentAlerts(10);
                recentAlerts.clear();
                for (Map<String, Object> alertMap : redisAlerts) {
                    MonitorMetricsVO.AlertItem item = new MonitorMetricsVO.AlertItem();
                    item.setAlertId((String) alertMap.get("alertId"));
                    item.setAlertType((String) alertMap.get("alertType"));
                    item.setMessage((String) alertMap.get("message"));
                    item.setLevel((String) alertMap.get("level"));
                    Object alertTime = alertMap.get("alertTime");
                    if (alertTime instanceof String) {
                        item.setAlertTime(LocalDateTime.parse((String) alertTime));
                    }
                    recentAlerts.add(item);
                }
            } catch (Exception e) {
                log.warn("Redis读取指标失败，降级到内存: {}", e.getMessage());
                redisAvailable = false;
                metrics[0] = executionTotal.get();
                metrics[1] = executionSuccess.get();
                metrics[2] = executionFailed.get();
            }
        } else {
            metrics[0] = executionTotal.get();
            metrics[1] = executionSuccess.get();
            metrics[2] = executionFailed.get();
        }

        vo.setExecutionTotal(metrics[0]);
        vo.setExecutionSuccess(metrics[1]);
        vo.setExecutionFailed(metrics[2]);
        vo.setSuccessRate(metrics[0] > 0
                ? BigDecimal.valueOf(metrics[1] * 100.0 / metrics[0]).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        vo.setP99DurationMs(p99DurationMs);
        vo.setP95DurationMs(p95DurationMs);
        vo.setAvgDurationMs(p95DurationMs / 2);
        vo.setActiveRuleCount(ruleHitCounts.size());

        // 公式命中率
        long formulaTotal = formulaHits.get();
        if (formulaTotal > 0 && metrics[0] > 0) {
            double hitRate = (double) formulaTotal * 100.0 / metrics[0];
            vo.setFormulaHitRate(BigDecimal.valueOf(hitRate).setScale(2, RoundingMode.HALF_UP));
        } else {
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
                    item.setHitRate(metrics[0] > 0
                            ? BigDecimal.valueOf(e.getValue().get() * 100.0 / metrics[0]).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO);
                    return item;
                })
                .toList();
        vo.setTopRules(topRules);

        // 最近告警
        vo.setRecentAlerts(new ArrayList<>(recentAlerts.stream().limit(10).toList()));

        return vo;
    }

    /**
     * 发布指标更新事件（供 WebSocket 推送使用）
     */
    public void publishMetricsUpdate() {
        eventPublisher.publishEvent(new MetricsUpdateEvent(this, getMetrics()));
    }

    /**
     * 每5秒通过事件发布推送指标
     */
    @Scheduled(fixedRate = 5000)
    public void checkAndPushMetrics() {
        publishMetricsUpdate();
    }

    private void updateDurationStats(long durationMs) {
        if (durationMs > p99DurationMs) {
            p99DurationMs = durationMs;
        }
        if (durationMs > p95DurationMs && durationMs <= p99DurationMs) {
            p95DurationMs = durationMs;
        }
    }

    /**
     * 检查 Redis 可用性
     */
    public boolean isRedisAvailable() {
        return redisAvailable;
    }

    /**
     * 强制切换到 Redis 存储
     */
    public void switchToRedis() {
        if (!redisAvailable) {
            try {
                redisTemplate.opsForValue().get("his:monitor:test");
                redisMetricsStore = new RedisMetricsStore(redisTemplate);
                redisAvailable = true;
                log.info("已切换到Redis存储");
            } catch (Exception e) {
                log.warn("切换到Redis失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 强制切换到内存存储
     */
    public void switchToMemory() {
        redisAvailable = false;
        log.info("已切换到内存存储");
    }

    /**
     * 指标更新事件
     */
    public record MetricsUpdateEvent(Object source, MonitorMetricsVO metrics) {}
}