package com.his.formula.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公式参数实体
 */
@Data
@TableName("formula_param")
public class FormulaParam {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long formulaId;

    private String paramName;

    private String paramType;

    private String defaultValue;

    private String description;

    private Integer paramOrder;

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
