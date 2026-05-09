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
 * 报销比例计算 Skill。
 * 
 * 根据患者类型和医院等级计算报销比例：
 * - 职工医保基础比例：85%
 * - 居民医保基础比例：65%
 * - 救助对象基础比例：50%
 * - 三级医院：基础比例 × 0.9
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@Slf4j
@Component
public class ReimburseRatioSkill implements ISkill<SettlementFact> {

    @Override
    public String supportEvent() {
        return "EVENT_FEE_SETTLE";
    }

    @Override
    public int getOrder() {
        return 30;
    }

    @Override
    public void execute(SkillContext<SettlementFact> context) {
        log.debug("开始执行报销比例计算: tenantId={}", context.getTenantId());

        SettlementFact fact = context.getPayload();
        if (fact == null || fact.getPatientType() == null) {
            context.addResult(new SkillResult(ResultLevel.WARN, "RatioCheck", "患者类型缺失，报销比例为零"));
            fact.setRatio(BigDecimal.ZERO);
            return;
        }

        BigDecimal baseRatio = calculateBaseRatio(fact.getPatientType());
        BigDecimal finalRatio = adjustByHospitalLevel(baseRatio, fact.getHospitalLevel());
        fact.setRatio(finalRatio);

        log.info("报销比例计算完成: patientId={}, type={}, hospitalLevel={}, baseRatio={}, finalRatio={}",
                fact.getPatientId(), fact.getPatientType(), fact.getHospitalLevel(), baseRatio, finalRatio);
        
        context.addResult(new SkillResult(ResultLevel.PASS, "RatioCheck", 
                "报销比例计算完成: " + finalRatio.multiply(new BigDecimal("100")).setScale(1) + "%"));
    }

    private BigDecimal calculateBaseRatio(String patientType) {
        return switch (patientType.toLowerCase()) {
            case "employee" -> new BigDecimal("0.85");
            case "resident" -> new BigDecimal("0.65");
            case "aid" -> new BigDecimal("0.50");
            default -> {
                log.warn("未知的患者类型: {}, 使用默认报销比例 0", patientType);
                yield BigDecimal.ZERO;
            }
        };
    }

    private BigDecimal adjustByHospitalLevel(BigDecimal baseRatio, String hospitalLevel) {
        if ("三级".equals(hospitalLevel) || "3".equals(hospitalLevel)) {
            return baseRatio.multiply(new BigDecimal("0.9")).setScale(4, java.math.RoundingMode.HALF_UP);
        }
        return baseRatio;
    }
}
