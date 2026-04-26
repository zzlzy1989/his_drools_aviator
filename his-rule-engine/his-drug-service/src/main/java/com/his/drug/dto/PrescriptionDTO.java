package com.his.drug.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 处方审核请求 DTO
 */
@Data
public class PrescriptionDTO {

    @NotBlank(message = "就诊ID不能为空")
    private String visitId;

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    private String patientInfo;

    @NotEmpty(message = "药品列表不能为空")
    private List<DrugItem> drugs;

    private List<String> diagnosisCodes;

    @NotBlank(message = "医生ID不能为空")
    private String doctorId;

    /**
     * 药品项
     */
    @Data
    public static class DrugItem {
        @NotBlank(message = "药品代码不能为空")
        private String drugCode;

        @NotBlank(message = "药品名称不能为空")
        private String drugName;

        private String specification;

        private String dosage;

        private String unit;

        private Integer quantity;

        private String administration;
    }
}
