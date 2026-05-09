package com.his.fact;

import com.his.common.ResultLevel;
import com.his.common.SkillResult;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 质控 Fact 对象，用于医疗质量控制规则。
 */
@Data
public class QualityFact {

    private String tenantId;
    private String deptId;
    private String deptName;

    // 用药安全指标
    private BigDecimal antibioticUsageRate;
    private BigDecimal injectionRate;
    private BigDecimal ivRate;
    private BigDecimal genericRate;
    private Double avgDrugsPerPrescription;
    private BigDecimal ppiUsageRate;
    private BigDecimal auxiliaryDrugRate;
    private BigDecimal expensiveDrugRate;

    // 诊断质量指标
    private BigDecimal icdAccuracy;
    private BigDecimal missingDiagnosisRate;
    private BigDecimal complicationRate;
    private BigDecimal readmissionRate;
    private BigDecimal mortalityRate;

    // 费用监控指标
    private BigDecimal avgInpatientFee;
    private BigDecimal avgOutpatientFee;
    private BigDecimal drugRatio;
    private BigDecimal consumableRatio;
    private BigDecimal examRatio;

    // 病历质量指标
    private BigDecimal recordCompletionRate;
    private BigDecimal recordTimelinessRate;
    private BigDecimal gradeARate;
    private BigDecimal signatureRate;
    private BigDecimal modificationRate;

    // 院感防控指标
    private BigDecimal haiRate;
    private BigDecimal handHygieneRate;

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
