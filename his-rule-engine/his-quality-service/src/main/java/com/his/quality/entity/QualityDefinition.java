package com.his.quality.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 质控规则定义实体
 */
@Data
@TableName("quality_definition")
public class QualityDefinition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String itemKey;

    private String itemName;

    private String category;

    private String level;

    private String description;

    private String status;

    private String tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
