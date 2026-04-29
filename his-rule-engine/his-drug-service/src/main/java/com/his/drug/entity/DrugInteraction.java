package com.his.drug.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药品配伍禁忌实体
 */
@Data
@TableName("drug_interaction")
public class DrugInteraction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String drugCodeA;

    private String drugNameA;

    private String drugCodeB;

    private String drugNameB;

    private String interactionType;

    private String severityLevel;

    private String description;

    private String tenantId;

    private Integer isEnabled;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
