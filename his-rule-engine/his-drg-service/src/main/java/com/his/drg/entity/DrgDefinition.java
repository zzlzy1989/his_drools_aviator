package com.his.drg.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DRG 分组定义实体
 */
@Data
@TableName("drg_definition")
public class DrgDefinition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String drgCode;

    private String drgName;

    private String mdcCode;

    private String mdcName;

    private BigDecimal baseWeight;

    private BigDecimal baseFee;

    private BigDecimal standardScore;

    private BigDecimal adjustFactor;

    private String description;

    private String rulesText;

    private Integer version;

    private String status;

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
