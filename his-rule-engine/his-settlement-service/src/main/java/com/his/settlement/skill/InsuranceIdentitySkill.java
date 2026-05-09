package com.his.settlement.skill;

import com.his.common.ISkill;
import com.his.common.ResultLevel;
import com.his.common.SkillContext;
import com.his.common.SkillResult;
import com.his.common.SettlementFact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 医保身份校验 Skill。
 * 
 * 校验患者是否具备有效的医保身份，确保 patientType 不为空且合法。
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@Slf4j
@Component
public class InsuranceIdentitySkill implements ISkill<SettlementFact> {

    @Override
    public String supportEvent() {
        return "EVENT_FEE_SETTLE";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void execute(SkillContext<SettlementFact> context) {
        log.debug("开始执行医保身份校验: tenantId={}", context.getTenantId());

        SettlementFact fact = context.getPayload();
        if (fact == null) {
            context.addResult(new SkillResult(ResultLevel.BLOCK, "IdentityCheck", "结算数据为空"));
            log.warn("医保身份校验失败: 结算数据为空");
            return;
        }

        String patientType = fact.getPatientType();
        if (patientType == null || patientType.isBlank()) {
            context.addResult(new SkillResult(ResultLevel.BLOCK, "IdentityCheck", "患者类型缺失"));
            log.warn("医保身份校验失败: patientId={}, 患者类型缺失", fact.getPatientId());
            return;
        }

        if (!isValidPatientType(patientType)) {
            context.addResult(new SkillResult(ResultLevel.BLOCK, "IdentityCheck", 
                    "无效的患者类型: " + patientType));
            log.warn("医保身份校验失败: patientId={}, 无效类型={}", fact.getPatientId(), patientType);
            return;
        }

        context.addResult(new SkillResult(ResultLevel.PASS, "IdentityCheck", "医保身份校验通过"));
        log.info("医保身份校验通过: patientId={}, type={}", fact.getPatientId(), patientType);
    }

    private boolean isValidPatientType(String patientType) {
        return "employee".equalsIgnoreCase(patientType) 
                || "resident".equalsIgnoreCase(patientType)
                || "aid".equalsIgnoreCase(patientType);
    }
}
