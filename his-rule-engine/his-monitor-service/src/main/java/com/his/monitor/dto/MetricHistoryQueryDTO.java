package com.his.monitor.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 监控指标历史查询 DTO
 */
@Data
public class MetricHistoryQueryDTO {

    private String metricName;

    private String tag;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 时间粒度: MINUTE/HOUR/DAY */
    private String granularity;

    /** 聚合方式: AVG/MAX/MIN/SUM/COUNT */
    private String aggregation;

    /** 最新的N个点 */
    private Integer latest;

    public enum Granularity {
        MINUTE, HOUR, DAY
    }

    public enum Aggregation {
        AVG, MAX, MIN, SUM, COUNT
    }
}