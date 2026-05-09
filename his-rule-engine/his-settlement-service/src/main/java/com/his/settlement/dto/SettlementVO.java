package com.his.settlement.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算视图对象
 */
@Data
public class SettlementVO {

    private Long id;

    private String settlementNo;

    private String visitId;

    private String patientId;

    private String patientName;

    private String patientType;

    private String insuranceType;

    private String hospitalLevel;

    private BigDecimal totalFee;

    private BigDecimal deductible;

    private BigDecimal ratio;

    private BigDecimal reimburseAmount;

    private BigDecimal selfPayAmount;

    private String resultLevel;

    private String status;

    private String tenantId;

    private String createBy;

    private LocalDateTime createTime;
}
