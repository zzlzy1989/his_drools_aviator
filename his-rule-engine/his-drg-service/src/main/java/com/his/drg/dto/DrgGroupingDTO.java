package com.his.drg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DRG 分组请求 DTO
 */
@Data
public class DrgGroupingDTO {

    @NotBlank(message = "就诊ID不能为空")
    private String visitId;

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    @NotBlank(message = "主要诊断不能为空")
    private String primaryDiagnosis;

    private List<String> secondaryDiagnoses;

    private List<String> procedureCodes;

    @NotNull(message = "总费用不能为空")
    private BigDecimal totalFee;
}
