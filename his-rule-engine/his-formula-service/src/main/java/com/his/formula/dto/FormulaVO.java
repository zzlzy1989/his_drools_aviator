package com.his.formula.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公式视图对象
 */
@Data
public class FormulaVO {

    private Long id;

    private String formulaKey;

    private String formulaName;

    @com.fasterxml.jackson.annotation.JsonProperty("expression")
    private String formulaText;

    private String returnType;

    public String getReturnType() {
        return returnType != null ? returnType : 
            (formulaText != null && formulaText.contains("round") ? "BigDecimal" : "String");
    }

    private String category;

    private Integer version;

    private String status;

    private String description;

    private Integer isValidated;

    private String validatedMsg;

    private String tenantId;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    private List<ParamVO> params;

    /**
     * 参数视图对象
     */
    @Data
    public static class ParamVO {
        private Long id;
        private String paramName;
        private String paramType;
        private String defaultValue;
        private String description;
        private Integer paramOrder;
    }
}
