package com.his.common;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SettlementFact {

    private String patientId;
    private String patientName;
    private String patientType;
    private String tenantId;
    private String settlementId;

    private BigDecimal totalFee;
    private BigDecimal deductible;
    private BigDecimal ratio;
    private BigDecimal finalAmount;

    private String insuranceType;
    private String hospitalLevel;
    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;

    private String diagnosisCode;
    private String drugCode;
    private Integer drugQuantity;
}
