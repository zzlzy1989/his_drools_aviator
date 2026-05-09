package com.his.settlement.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 结算更新 DTO
 */
@Data
public class SettlementUpdateDTO {

    private String visitId;

    private String patientId;

    private String patientType;

    private String insuranceType;

    private String hospitalLevel;

    private BigDecimal totalFee;
}
