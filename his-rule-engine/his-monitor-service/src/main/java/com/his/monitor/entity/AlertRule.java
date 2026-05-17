package com.his.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 告警规则实体
 */
@Data
@TableName("alert_rule")
public class AlertRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则名称 */
    private String ruleName;

    /** 指标名称 */
    private String metricName;

    /** 条件类型: GT(大于), LT(小于), EQ(等于), GTE(大于等于), LTE(小于等于) */
    private String conditionType;

    /** 阈值 */
    private Double threshold;

    /** 告警级别: WARN, ERROR */
    private String level;

    /** 是否启用 */
    private Boolean enabled;

    /** 通知方式: EMAIL, DINGTALK, SMS */
    private String notifyChannels;

    /** 通知目标 */
    private String notifyTarget;

    /** 告警消息模板 */
    private String messageTemplate;

    /** 连续触发次数阈值(用于抑制频繁告警) */
    private Integer consecutiveTriggers;

    /** 冷却时间(秒) */
    private Integer cooldownSeconds;

    private String tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}