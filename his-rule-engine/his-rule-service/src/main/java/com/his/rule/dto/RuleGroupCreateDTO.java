package com.his.rule.dto;

import lombok.Data;

/**
 * 规则分组创建 DTO
 */
@Data
public class RuleGroupCreateDTO {

    private String groupCode;

    private String groupName;

    private String description;

    private Integer priority;
}
