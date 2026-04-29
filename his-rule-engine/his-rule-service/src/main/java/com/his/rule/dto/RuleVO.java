package com.his.rule.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则视图对象
 */
@Data
public class RuleVO {

    private Long id;

    private Long ruleGroupId;

    private String groupName;

    private String ruleKey;

    private String ruleName;

    private String ruleText;

    private String category;

    private Integer version;

    private String status;

    private String description;

    private Integer salience;

    private String activationGroup;

    private LocalDateTime effectiveStart;

    private LocalDateTime effectiveEnd;

    private String tenantId;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
