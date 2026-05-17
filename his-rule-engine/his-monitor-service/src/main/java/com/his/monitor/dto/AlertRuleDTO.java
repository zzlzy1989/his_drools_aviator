package com.his.monitor.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 告警规则 DTO
 */
@Data
public class AlertRuleDTO {

    private Long id;

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "指标名称不能为空")
    private String metricName;

    @NotBlank(message = "条件类型不能为空")
    private String conditionType;

    @NotNull(message = "阈值不能为空")
    private Double threshold;

    @NotBlank(message = "告警级别不能为空")
    private String level;

    private Boolean enabled = true;

    private String notifyChannels;

    private String notifyTarget;

    private String messageTemplate;

    private Integer consecutiveTriggers = 1;

    private Integer cooldownSeconds = 300;
}