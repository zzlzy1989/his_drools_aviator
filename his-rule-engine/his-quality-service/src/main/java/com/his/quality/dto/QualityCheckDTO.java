package com.his.quality.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 质控检查请求 DTO
 */
@Data
public class QualityCheckDTO {

    @NotBlank(message = "就诊ID不能为空")
    private String visitId;

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    private List<String> diagnosisCodes;

    private List<String> procedureCodes;

    private String deptCode;

    private String doctorId;
}
