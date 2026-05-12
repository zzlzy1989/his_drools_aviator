package com.his.formula.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公式历史记录 VO
 */
@Data
public class FormulaHistoryVO {

    private Long id;

    private Long formulaId;

    private String formulaKey;

    private String formulaText;

    private Integer version;

    private String status;

    private String changeReason;

    private String changeBy;

    private LocalDateTime changeTime;
}