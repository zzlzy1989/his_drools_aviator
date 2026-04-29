package com.his.formula.dto;

import lombok.Data;

/**
 * 公式查询 DTO
 */
@Data
public class FormulaQueryDTO {

    private String category;

    private String status;

    private String formulaKey;

    private String formulaName;
}
