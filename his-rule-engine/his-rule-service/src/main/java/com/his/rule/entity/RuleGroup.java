package com.his.rule.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则分组实体
 */
@Data
@TableName("rule_group")
public class RuleGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonProperty("groupKey")
    private String groupCode;

    private String groupName;

    private String description;

    private String tenantId;

    private Integer isEnabled;

    private Integer priority;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
