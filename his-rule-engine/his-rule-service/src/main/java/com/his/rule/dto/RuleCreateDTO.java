package com.his.rule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则创建 DTO
 */
@Data
public class RuleCreateDTO {

    @NotBlank(message = "规则Key不能为空")
    @Pattern(regexp = "^rule\\.[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$",
            message = "规则Key格式: rule.{module}.{name}")
    private String ruleKey;

    @NotBlank(message = "规则名称不能为空")
    @Size(max = 128, message = "规则名称不超过128字符")
    private String ruleName;

    @NotBlank(message = "DRL内容不能为空")
    private String ruleText;

    @NotBlank(message = "分类不能为空")
    private String category;

    private Long ruleGroupId;

    @Size(max = 500, message = "描述不超过500字符")
    private String description;

    private Integer salience;

    private String activationGroup;

    private LocalDateTime effectiveStart;

    private LocalDateTime effectiveEnd;
}
