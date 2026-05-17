package com.his.monitor.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 监控指标 VO
 */
@Data
public class MonitorMetricsVO {

    /** 规则执行总次数 */
    private Long executionTotal;

    /** 规则执行成功次数 */
    private Long executionSuccess;

    /** 规则执行失败次数 */
    private Long executionFailed;

    /** 成功率 */
    private BigDecimal successRate;

    /** P99 执行耗时 (ms) */
    private Long p99DurationMs;

    /** P95 执行耗时 (ms) */
    private Long p95DurationMs;

    /** 平均执行耗时 (ms) */
    private Long avgDurationMs;

    /** 当前生效规则数 */
    private Integer activeRuleCount;

    /** 公式命中率 */
    private BigDecimal formulaHitRate;

    /** 最后更新时间 */
    private LocalDateTime lastUpdateTime;

    /** TOP 10 高频规则 */
    private List<RuleStatItem> topRules;

    /** 最近告警 */
    private List<AlertItem> recentAlerts;

    @Data
    public static class RuleStatItem {
        private String ruleKey;
        private String ruleName;
        private Long hitCount;
        private BigDecimal hitRate;
    }

    @Data
    public static class AlertItem {
        private String alertId;
        private String alertType;
        private String message;
        private String level;
        private LocalDateTime alertTime;
    }
}