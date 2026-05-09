package com.his.common;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SettlementFact {

    private String patientId;
    private String patientName;
    private String patientType;
    private String tenantId;
    private String settlementId;

    private BigDecimal totalFee;
    private BigDecimal deductible;
    private BigDecimal ratio;
    private BigDecimal finalAmount;

    private String insuranceType;
    private String hospitalLevel;
    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;

    private String diagnosisCode;
    private String drugCode;
    private Integer drugQuantity;

    private List<SkillResult> results = new ArrayList<>();

    public void addResult(ResultLevel level, String source, String message) {
        this.results.add(new SkillResult(level, source, message));
    }

    public void addResult(SkillResult result) {
        this.results.add(result);
    }

    public boolean hasBlock() {
        return results.stream().anyMatch(r -> r.getLevel() == ResultLevel.BLOCK);
    }

    public ResultLevel getResultLevel() {
        if (hasBlock()) return ResultLevel.BLOCK;
        if (results.stream().anyMatch(r -> r.getLevel() == ResultLevel.WARN)) return ResultLevel.WARN;
        return ResultLevel.PASS;
    }
}
