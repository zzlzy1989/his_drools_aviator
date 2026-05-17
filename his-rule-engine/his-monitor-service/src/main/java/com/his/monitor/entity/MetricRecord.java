package com.his.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 指标历史记录实体
 */
@Data
@TableName("metric_record")
public class MetricRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String metricName;

    private BigDecimal metricValue;

    private String tag;

    private String tenantId;

    private LocalDateTime recordTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}