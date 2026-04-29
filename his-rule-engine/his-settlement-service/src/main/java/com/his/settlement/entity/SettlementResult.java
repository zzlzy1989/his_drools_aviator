package com.his.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算结果实体
 */
@Data
@TableName("settlement_result")
public class SettlementResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String settlementNo;

    private String visitId;

    private String patientId;

    private String patientType;

    private String insuranceType;

    private String hospitalLevel;

    private BigDecimal totalFee;

    private BigDecimal deductible;

    private BigDecimal ratio;

    private BigDecimal reimburseAmount;

    private BigDecimal selfPayAmount;

    private String resultLevel;

    private String skillResults;

    private String status;

    private String tenantId;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
