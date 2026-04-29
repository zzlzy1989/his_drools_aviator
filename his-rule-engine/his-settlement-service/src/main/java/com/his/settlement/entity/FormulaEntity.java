package com.his.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公式实体
 */
@Data
@TableName("aviator_formula")
public class FormulaEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String formulaKey;

    private String formulaName;

    private String formulaText;

    private String category;

    private Integer version;

    private String status;

    private String description;

    private Integer isValidated;

    private String validatedMsg;

    private String tenantId;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
