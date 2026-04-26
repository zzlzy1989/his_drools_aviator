package com.his.rule.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则定义实体
 */
@Data
@TableName("rule_definition")
public class RuleDefinition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ruleGroupId;

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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
