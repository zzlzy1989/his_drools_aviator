package com.his.rule.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则更新 DTO
 */
@Data
public class RuleUpdateDTO {

    @Size(max = 128, message = "规则名称不超过128字符")
    private String ruleName;

    private String ruleText;

    private Long ruleGroupId;

    @Size(max = 500, message = "描述不超过500字符")
    private String description;

    private Integer salience;

    private String activationGroup;

    private LocalDateTime effectiveStart;

    private LocalDateTime effectiveEnd;
}
