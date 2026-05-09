package com.his.fact;

import com.his.common.ResultLevel;
import com.his.common.SkillResult;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 院感 Fact 对象，用于院感防控规则。
 */
@Data
public class InfectionFact {

    private String tenantId;
    private String deptId;
    private String deptName;

    private BigDecimal handHygieneRate;
    private BigDecimal infectionRate;
    private Integer totalCases;
    private Integer infectionCases;

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
