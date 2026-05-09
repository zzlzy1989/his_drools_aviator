package com.his.drug.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医保药品目录实体
 */
@Data
@TableName("drug_catalog")
public class DrugCatalog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String drugCode;

    private String drugName;

    private String specification;

    private String dosageUnit;

    private String drugType;

    private String category;

    @com.fasterxml.jackson.annotation.JsonProperty("insuranceType")
    private String reimbursementType;

    @com.fasterxml.jackson.annotation.JsonProperty("unitPrice")
    private BigDecimal limitPrice;

    private String hospitalLevel;

    private String manufacturer;

    private String isEnabled;

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
