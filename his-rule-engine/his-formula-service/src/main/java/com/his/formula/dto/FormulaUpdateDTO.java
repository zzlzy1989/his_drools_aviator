package com.his.formula.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 公式更新 DTO
 */
@Data
public class FormulaUpdateDTO {

    @Size(max = 128, message = "公式名称不超过128字符")
    private String formulaName;

    @Size(max = 1000, message = "公式内容不超过1000字符")
    private String formulaText;

    @Size(max = 500, message = "描述不超过500字符")
    private String description;

    private List<FormulaCreateDTO.ParamDTO> params;
}
