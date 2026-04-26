package com.his.formula.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 公式创建 DTO
 */
@Data
public class FormulaCreateDTO {

    @NotBlank(message = "公式Key不能为空")
    @Pattern(regexp = "^formula\\.[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$",
            message = "公式Key格式: formula.{category}.{name}")
    private String formulaKey;

    @NotBlank(message = "公式名称不能为空")
    @Size(max = 128, message = "公式名称不超过128字符")
    private String formulaName;

    @NotBlank(message = "公式内容不能为空")
    @Size(max = 1000, message = "公式内容不超过1000字符")
    private String formulaText;

    @NotBlank(message = "分类不能为空")
    private String category;

    @Size(max = 500, message = "描述不超过500字符")
    private String description;

    private List<ParamDTO> params;

    /**
     * 参数定义
     */
    @Data
    public static class ParamDTO {
        @NotBlank(message = "参数名不能为空")
        private String paramName;

        @NotBlank(message = "参数类型不能为空")
        private String paramType;

        private String defaultValue;

        private String description;

        private Integer paramOrder;
    }
}
