package com.his.fact;

import com.his.common.ResultLevel;
import com.his.common.SkillResult;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 费用 Fact 对象，用于费用控制规则。
 */
@Data
public class CostFact {

    private String tenantId;
    private String deptId;
    private String deptName;

    private BigDecimal totalFee;
    private BigDecimal drugFee;
    private BigDecimal consumableFee;
    private BigDecimal examFee;

    private BigDecimal drugRatio;
    private BigDecimal consumableRatio;

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
