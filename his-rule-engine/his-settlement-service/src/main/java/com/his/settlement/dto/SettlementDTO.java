package com.his.settlement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 结算请求 DTO
 */
@Data
public class SettlementDTO {

    @NotBlank(message = "就诊ID不能为空")
    private String visitId;

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    @NotBlank(message = "患者类型不能为空")
    private String patientType;

    private String insuranceType;

    private String hospitalLevel;

    @NotNull(message = "总费用不能为空")
    private BigDecimal totalFee;
}
