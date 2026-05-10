package com.his.settlement.skill;

import com.his.common.ISkill;
import com.his.common.ResultLevel;
import com.his.common.SkillContext;
import com.his.common.SkillResult;
import com.his.common.SettlementFact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 起付线检查 Skill。
 * 
 * 检查总费用是否达到起付线：
 * - 未达到起付线：返回 WARN，阻断后续报销计算
 * - 达到起付线：返回 PASS，允许继续执行
 * 
 * @author HIS Rule Engine
 * @since 1.0.0
 */
@Slf4j
@Component
public class DeductibleCheckSkill implements ISkill<SettlementFact> {

    @Override
    public String supportEvent() {
        return "EVENT_FEE_SETTLE";
    }

    @Override
    public int getOrder() {
        return 25;
    }

    @Override
    public void execute(SkillContext<SettlementFact> context) {
        log.debug("开始执行起付线检查: tenantId={}", context.getTenantId());

        SettlementFact fact = context.getPayload();
        if (fact == null || fact.getTotalFee() == null || fact.getDeductible() == null) {
            log.warn("起付线检查跳过: 数据不完整");
            return;
        }

        BigDecimal totalFee = fact.getTotalFee();
        BigDecimal deductible = fact.getDeductible();

        if (totalFee.compareTo(deductible) <= 0) {
            context.addResult(new SkillResult(
                ResultLevel.WARN,
                "DeductibleCheck",
                String.format("未达到起付线: 总费用=%.2f, 起付线=%.2f", totalFee, deductible)
            ));
            log.warn("起付线检查: patientId={}, totalFee={}, deductible={}, 未达起付线",
                    fact.getPatientId(), totalFee, deductible);
        } else {
            log.debug("起付线检查通过: patientId={}, totalFee={}, deductible={}",
                    fact.getPatientId(), totalFee, deductible);
        }
    }
}
