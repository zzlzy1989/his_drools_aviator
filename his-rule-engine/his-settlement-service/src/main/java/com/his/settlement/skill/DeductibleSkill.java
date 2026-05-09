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
 * 起付线计算 Skill。
 * 
 * 根据患者类型计算起付线：
 * - 职工医保：1000 元
 * - 居民医保：500 元
 * - 救助对象：300 元
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@Slf4j
@Component
public class DeductibleSkill implements ISkill<SettlementFact> {

    @Override
    public String supportEvent() {
        return "EVENT_FEE_SETTLE";
    }

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public void execute(SkillContext<SettlementFact> context) {
        log.debug("开始执行起付线计算: tenantId={}", context.getTenantId());

        SettlementFact fact = context.getPayload();
        if (fact == null || fact.getPatientType() == null) {
            context.addResult(new SkillResult(ResultLevel.WARN, "DeductibleCheck", "患者类型缺失，起付线为零"));
            return;
        }

        BigDecimal deductible = calculateDeductible(fact.getPatientType());
        fact.setDeductible(deductible);

        log.info("起付线计算完成: patientId={}, type={}, deductible={}",
                fact.getPatientId(), fact.getPatientType(), deductible);
        
        context.addResult(new SkillResult(ResultLevel.PASS, "DeductibleCheck", 
                "起付线计算完成: " + deductible + " 元"));
    }

    private BigDecimal calculateDeductible(String patientType) {
        return switch (patientType.toLowerCase()) {
            case "employee" -> new BigDecimal("1000");
            case "resident" -> new BigDecimal("500");
            case "aid" -> new BigDecimal("300");
            default -> {
                log.warn("未知的患者类型: {}, 使用默认起付线 0", patientType);
                yield BigDecimal.ZERO;
            }
        };
    }
}
