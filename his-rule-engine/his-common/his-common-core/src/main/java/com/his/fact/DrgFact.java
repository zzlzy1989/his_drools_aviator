package com.his.fact;

import com.his.common.ResultLevel;
import com.his.common.SkillResult;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DRG 分组 Fact 对象，用于 DRG 分组规则。
 */
@Data
public class DrgFact {

    private String tenantId;
    private String visitId;
    private String patientId;

    private String mainDiagnosis;
    private List<String> secondaryDiagnoses;
    private String surgeryCode;
    private Boolean expectedSurgery;

    private BigDecimal totalFee;
    private Integer lengthOfStay;
    private String dischargeStatus;

    // DRG 计算结果
    private String drgCode;
    private String drgName;
    private BigDecimal baseWeight;
    private BigDecimal expectedFee;

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
