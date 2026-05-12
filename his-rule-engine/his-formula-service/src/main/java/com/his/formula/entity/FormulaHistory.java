package com.his.formula.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公式历史记录实体
 */
@Data
@TableName("formula_history")
public class FormulaHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long formulaId;

    private String formulaKey;

    private String formulaText;

    private Integer version;

    private String status;

    private String changeReason;

    private String changeBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime changeTime;

    private String tenantId;
}