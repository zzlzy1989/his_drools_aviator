package com.his.rule.dto;

import lombok.Data;

/**
 * 规则查询 DTO
 */
@Data
public class RuleQueryDTO {

    private String category;

    private String status;

    private String ruleKey;

    private String ruleName;

    private Long ruleGroupId;
}
