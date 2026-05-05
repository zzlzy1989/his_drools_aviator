-- ============================================================
-- HIS 动态规则中台 - 规则定义初始化数据脚本
-- 创建日期: 2026-05-05
-- 说明: 插入医疗机构常用的DRL规则定义数据
-- ============================================================

USE `his_rule_engine`;

-- 清空现有数据
TRUNCATE TABLE `rule_definition`;

-- 关闭外键检查以便清空表
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 插入规则定义数据（25条）
-- 涵盖：医保报销、合理用药、质控管理、DRG等常用规则
-- ============================================================

INSERT INTO `rule_definition` (`rule_group_id`, `rule_key`, `rule_name`, `rule_text`, `category`, `version`, `status`, `description`, `salience`, `activation_group`, `tenant_id`, `create_by`) VALUES
-- 规则组1：医保结算规则组
(1, 'rule.reimbursement.identity_check', '患者身份校验规则', 
'package com.his.rules.reimbursement;
import com.his.fact.SettlementFact;
import com.his.common.ResultLevel;

rule "1. 患者身份校验"
    salience 100
    when
        $f: SettlementFact(patientId == null || patientId.isEmpty())
    then
        $f.addResult(ResultLevel.BLOCK, "IdentityCheck", "患者ID不能为空");
        log.warn("身份校验失败: patientId为空");
end', 
'reimbursement', 1, 'active', '校验患者身份信息是否完整，防止无效结算', 100, 'identity_check', 'HOSPITAL_001', 'admin'),

(1, 'rule.reimbursement.insurance_type_check', '医保类型校验规则',
'package com.his.rules.reimbursement;
import com.his.fact.SettlementFact;
import com.his.common.ResultLevel;

rule "2. 医保类型校验"
    salience 90
    when
        $f: SettlementFact(insuranceType == null || insuranceType.isEmpty())
    then
        $f.addResult(ResultLevel.BLOCK, "InsuranceCheck", "医保类型不能为空");
        log.warn("医保类型校验失败");
end',
'reimbursement', 1, 'active', '校验患者医保类型，确保按正确规则结算', 90, 'insurance_check', 'HOSPITAL_001', 'admin'),

(1, 'rule.reimbursement.fee_validation', '费用合法性校验规则',
'package com.his.rules.reimbursement;
import com.his.fact.SettlementFact;
import com.his.common.ResultLevel;
import java.math.BigDecimal;

rule "3. 费用合法性校验"
    salience 85
    when
        $f: SettlementFact(totalFee != null, totalFee.compareTo(BigDecimal.ZERO) <= 0)
    then
        $f.addResult(ResultLevel.BLOCK, "FeeValidation", "总费用必须大于0");
        log.warn("费用校验失败: totalFee={}", $f.getTotalFee());
end',
'reimbursement', 1, 'active', '校验费用金额是否合法，防止负数或零费用结算', 85, 'fee_validation', 'HOSPITAL_001', 'admin'),

(1, 'rule.reimbursement.deductible_check', '起付线校验规则',
'package com.his.rules.reimbursement;
import com.his.fact.SettlementFact;
import com.his.common.ResultLevel;
import java.math.BigDecimal;

rule "4. 起付线校验"
    salience 80
    when
        $f: SettlementFact(deductible != null, totalFee != null, 
            totalFee.compareTo($f.getDeductible()) < 0)
    then
        $f.addResult(ResultLevel.WARN, "DeductibleCheck", "总费用未达起付线，不予报销");
        log.info("未达起付线: totalFee={}, deductible={}", $f.getTotalFee(), $f.getDeductible());
end',
'reimbursement', 1, 'active', '校验费用是否达到起付线标准', 80, 'deductible_check', 'HOSPITAL_001', 'admin'),

(1, 'rule.reimbursement.cap_check', '封顶线校验规则',
'package com.his.rules.reimbursement;
import com.his.fact.SettlementFact;
import com.his.common.ResultLevel;
import java.math.BigDecimal;

rule "5. 封顶线校验"
    salience 75
    when
        $f: SettlementFact(capAmount != null, totalFee != null,
            totalFee.compareTo($f.getCapAmount()) > 0)
    then
        BigDecimal cap = $f.getCapAmount();
        $f.setTotalFee(cap);
        $f.addResult(ResultLevel.WARN, "CapCheck", "费用超过封顶线，按封顶线计算");
        log.info("超过封顶线: totalFee={}, capAmount={}", $f.getTotalFee(), cap);
end',
'reimbursement', 1, 'active', '校验费用是否超过年度封顶线', 75, 'cap_check', 'HOSPITAL_001', 'admin'),

(1, 'rule.reimbursement.settlement_calculation', '报销金额计算规则',
'package com.his.rules.reimbursement;
import com.his.fact.SettlementFact;
import com.his.helper.AviatorHelper;
import java.math.BigDecimal;

rule "6. 报销金额计算"
    salience 10
    when
        $f: SettlementFact(totalFee != null, deductible != null, 
            ratio != null, resultLevel != ResultLevel.BLOCK)
    then
        String formulaKey = "formula.reimburse.basic";
        String formulaText = formulaManager.getFormulaByKey(formulaKey);
        if (formulaText != null) {
            BigDecimal amount = AviatorHelper.executeFormula(formulaText, $f);
            $f.setReimburseAmount(amount);
            log.info("报销计算完成: amount={}", amount);
        }
end',
'reimbursement', 1, 'active', '根据公式计算报销金额', 10, 'calculation', 'HOSPITAL_001', 'admin'),

-- 规则组2：住院报销规则组
(2, 'rule.inpatient.admission_check', '住院资格校验规则',
'package com.his.rules.inpatient;
import com.his.fact.SettlementFact;
import com.his.common.ResultLevel;

rule "1. 住院资格校验"
    salience 100
    when
        $f: SettlementFact(visitType == null || !"inpatient".equals($f.getVisitType()))
    then
        $f.addResult(ResultLevel.BLOCK, "AdmissionCheck", "非住院类型不能按住院规则结算");
end',
'reimbursement', 1, 'active', '校验是否为住院类型', 100, 'admission_check', 'HOSPITAL_001', 'admin'),

(2, 'rule.inpatient.length_of_stay_check', '住院天数校验规则',
'package com.his.rules.inpatient;
import com.his.fact.SettlementFact;
import com.his.common.ResultLevel;

rule "2. 住院天数校验"
    salience 90
    when
        $f: SettlementFact(lengthOfStay != null, lengthOfStay > 180)
    then
        $f.addResult(ResultLevel.WARN, "StayCheck", "住院天数超过180天，需特殊审批");
end',
'reimbursement', 1, 'active', '校验住院天数是否合理', 90, 'stay_check', 'HOSPITAL_001', 'admin'),

(2, 'rule.inpatient.icu_fee_check', 'ICU费用校验规则',
'package com.his.rules.inpatient;
import com.his.fact.SettlementFact;
import com.his.common.ResultLevel;
import java.math.BigDecimal;

rule "3. ICU费用校验"
    salience 85
    when
        $f: SettlementFact(icuFee != null, icuFee.compareTo(new BigDecimal("50000")) > 0)
    then
        $f.addResult(ResultLevel.WARN, "ICUCheck", "ICU费用超过5万元，需专项审核");
end',
'reimbursement', 1, 'active', '校验ICU费用是否合理', 85, 'icu_check', 'HOSPITAL_001', 'admin'),

-- 规则组7：合理用药规则组
(7, 'rule.drug.prescription_check', '处方完整性校验规则',
'package com.his.rules.drug;
import com.his.fact.PrescriptionFact;
import com.his.common.ResultLevel;

rule "1. 处方完整性校验"
    salience 100
    when
        $f: PrescriptionFact(drugs == null || drugs.isEmpty())
    then
        $f.addResult(ResultLevel.BLOCK, "PrescriptionCheck", "处方药品不能为空");
end',
'drug', 1, 'active', '校验处方药品是否为空', 100, 'prescription_check', 'HOSPITAL_001', 'admin'),

(7, 'rule.drug.duplicate_drug_check', '重复用药检测规则',
'package com.his.rules.drug;
import com.his.fact.PrescriptionFact;
import com.his.common.ResultLevel;

rule "2. 重复用药检测"
    salience 90
    when
        $f: PrescriptionFact($drugList: drugs, $drugList.size() >= 2)
        exists DrugItem($code: drugCode) from $drugList
        exists DrugItem(drugCode == $code, id != $id) from $drugList
    then
        $f.addResult(ResultLevel.WARN, "DuplicateCheck", "检测到重复用药: " + $code);
end',
'drug', 1, 'active', '检测处方中是否存在重复药品', 90, 'duplicate_check', 'HOSPITAL_001', 'admin'),

(7, 'rule.drug.generic_substitution', '通用名替换建议规则',
'package com.his.rules.drug;
import com.his.fact.PrescriptionFact;
import com.his.common.ResultLevel;

rule "3. 通用名替换建议"
    salience 80
    when
        $f: PrescriptionFact(brandDrug == true, genericAvailable == true)
    then
        $f.addResult(ResultLevel.WARN, "GenericCheck", "建议使用通用名药品以降低成本");
end',
'drug', 1, 'active', '建议优先使用通用名药品', 80, 'generic_check', 'HOSPITAL_001', 'admin'),

-- 规则组8：药品配伍禁忌规则组
(8, 'rule.interaction.severe_check', '严重配伍禁忌检测规则',
'package com.his.rules.interaction;
import com.his.fact.PrescriptionFact;
import com.his.common.ResultLevel;

rule "1. 严重配伍禁忌检测"
    salience 100
    when
        $f: PrescriptionFact($drugs: drugs)
        exists DrugInteraction(severityLevel == "severe", drugCodeA == $codeA, drugCodeB == $codeB)
            from $drugs
    then
        $f.addResult(ResultLevel.BLOCK, "SevereInteraction", "检测到严重配伍禁忌: " + $codeA + "+" + $codeB);
end',
'drug', 1, 'active', '检测严重配伍禁忌，阻断处方', 100, 'severe_check', 'HOSPITAL_001', 'admin'),

(8, 'rule.interaction.moderate_warn', '中等配伍警告规则',
'package com.his.rules.interaction;
import com.his.fact.PrescriptionFact;
import com.his.common.ResultLevel;

rule "2. 中等配伍警告"
    salience 90
    when
        $f: PrescriptionFact($drugs: drugs)
        exists DrugInteraction(severityLevel == "moderate", drugCodeA == $codeA, drugCodeB == $codeB)
            from $drugs
    then
        $f.addResult(ResultLevel.WARN, "ModerateInteraction", "检测到中等配伍风险: " + $codeA + "+" + $codeB);
end',
'drug', 1, 'active', '检测中等配伍风险，提示医生', 90, 'moderate_warn', 'HOSPITAL_001', 'admin'),

-- 规则组9：用药剂量规则组
(9, 'rule.dosage.adult_max_check', '成人最大剂量校验规则',
'package com.his.rules.dosage;
import com.his.fact.PrescriptionFact;
import com.his.common.ResultLevel;

rule "1. 成人最大剂量校验"
    salience 100
    when
        $f: PrescriptionFact(isAdult == true, dailyDosage != null, maxDosage != null,
            dailyDosage.compareTo(maxDosage) > 0)
    then
        $f.addResult(ResultLevel.WARN, "DosageCheck", "用药剂量超过成人最大剂量限制");
end',
'drug', 1, 'active', '校验成人用药剂量是否超限', 100, 'adult_dosage', 'HOSPITAL_001', 'admin'),

(9, 'rule.dosage.pediatric_check', '儿童剂量校验规则',
'package com.his.rules.dosage;
import com.his.fact.PrescriptionFact;
import com.his.common.ResultLevel;

rule "2. 儿童剂量校验"
    salience 95
    when
        $f: PrescriptionFact(isAdult == false, weight != null, dailyDosage != null)
        Double weightKg = Double.parseDouble($f.getWeight());
        Double maxDose = weightKg * 10;
        eval($f.getDailyDosage().doubleValue() > maxDose)
    then
        $f.addResult(ResultLevel.WARN, "PediatricCheck", "儿童用药剂量可能超限，请确认");
end',
'drug', 1, 'active', '校验儿童用药剂量是否按体重计算合理', 95, 'pediatric_dosage', 'HOSPITAL_001', 'admin'),

-- 规则组10：药物过敏规则组
(10, 'rule.allergy.penicillin_check', '青霉素过敏检测规则',
'package com.his.rules.allergy;
import com.his.fact.PrescriptionFact;
import com.his.common.ResultLevel;

rule "1. 青霉素过敏检测"
    salience 100
    when
        $f: PrescriptionFact(allergyHistory contains "penicillin", 
            $drugs: drugs)
        exists DrugItem(drugType == "penicillin") from $drugs
    then
        $f.addResult(ResultLevel.BLOCK, "AllergyCheck", "患者对青霉素过敏，禁止使用青霉素类药物");
end',
'drug', 1, 'active', '检测青霉素过敏史，阻断处方', 100, 'penicillin_allergy', 'HOSPITAL_001', 'admin'),

(10, 'rule.allergy.sulfa_check', '磺胺类过敏检测规则',
'package com.his.rules.allergy;
import com.his.fact.PrescriptionFact;
import com.his.common.ResultLevel;

rule "2. 磺胺类过敏检测"
    salience 95
    when
        $f: PrescriptionFact(allergyHistory contains "sulfa", 
            $drugs: drugs)
        exists DrugItem(drugType == "sulfa") from $drugs
    then
        $f.addResult(ResultLevel.BLOCK, "AllergyCheck", "患者对磺胺类药物过敏");
end',
'drug', 1, 'active', '检测磺胺类过敏史', 95, 'sulfa_allergy', 'HOSPITAL_001', 'admin'),

-- 规则组11：特殊人群用药规则组
(11, 'rule.special.pregnancy_check', '孕妇禁用药检测规则',
'package com.his.rules.special;
import com.his.fact.PrescriptionFact;
import com.his.common.ResultLevel;

rule "1. 孕妇禁用药检测"
    salience 100
    when
        $f: PrescriptionFact(isPregnant == true, $drugs: drugs)
        exists DrugItem(pregnancyCategory == "X") from $drugs
    then
        $f.addResult(ResultLevel.BLOCK, "PregnancyCheck", "检测到孕妇禁用药物(FDA X级)");
end',
'drug', 1, 'active', '检测孕妇禁用药物', 100, 'pregnancy_check', 'HOSPITAL_001', 'admin'),

-- 规则组12：抗菌药物使用规则组
(12, 'rule.antibiotic.grade_check', '抗菌药物分级管理规则',
'package com.his.rules.antibiotic;
import com.his.fact.PrescriptionFact;
import com.his.common.ResultLevel;

rule "1. 抗菌药物分级管理"
    salience 100
    when
        $f: PrescriptionFact($drugs: drugs)
        exists DrugItem(antibioticGrade == "special", doctorLevel != "senior") from $drugs
    then
        $f.addResult(ResultLevel.BLOCK, "AntibioticCheck", "特殊级抗菌药物需高级职称医师处方");
end',
'drug', 1, 'active', '抗菌药物分级管理，特殊级需高级职称', 100, 'antibiotic_grade', 'HOSPITAL_001', 'admin'),

(12, 'rule.antibiotic.duration_check', '抗菌药物使用时长校验规则',
'package com.his.rules.antibiotic;
import com.his.fact.PrescriptionFact;
import com.his.common.ResultLevel;

rule "2. 抗菌药物使用时长校验"
    salience 90
    when
        $f: PrescriptionFact($drugs: drugs)
        exists DrugItem(antibioticGrade != null, durationDays > 14) from $drugs
    then
        $f.addResult(ResultLevel.WARN, "AntibioticDuration", "抗菌药物使用超过14天，需重新评估");
end',
'drug', 1, 'active', '监控抗菌药物使用时长', 90, 'antibiotic_duration', 'HOSPITAL_001', 'admin'),

-- 规则组14：院感防控规则组
(14, 'rule.infection.hand_hygiene', '手卫生依从性监控规则',
'package com.his.rules.infection;
import com.his.fact.InfectionFact;
import com.his.common.ResultLevel;

rule "1. 手卫生依从性监控"
    salience 100
    when
        $f: InfectionFact(handHygieneRate != null, handHygieneRate < 0.85)
    then
        $f.addResult(ResultLevel.WARN, "HandHygiene", "手卫生依从性低于85%，需加强培训");
end',
'quality', 1, 'active', '监控手卫生依从性指标', 100, 'hand_hygiene', 'HOSPITAL_001', 'admin'),

-- 规则组17：DRG分组规则组
(17, 'rule.drg.mdc_check', 'MDC主诊分类校验规则',
'package com.his.rules.drg;
import com.his.fact.DrgFact;
import com.his.common.ResultLevel;

rule "1. MDC主诊分类校验"
    salience 100
    when
        $f: DrgFact(mainDiagnosis == null || mainDiagnosis.isEmpty())
    then
        $f.addResult(ResultLevel.BLOCK, "MDCCheck", "主要诊断不能为空");
end',
'drg', 1, 'active', '校验DRG分组所需的主要诊断', 100, 'mdc_check', 'HOSPITAL_001', 'admin'),

(17, 'rule.drg.surgery_check', '手术操作校验规则',
'package com.his.rules.drg;
import com.his.fact.DrgFact;
import com.his.common.ResultLevel;

rule "2. 手术操作校验"
    salience 90
    when
        $f: DrgFact(surgeryCode == null, expectedSurgery == true)
    then
        $f.addResult(ResultLevel.WARN, "SurgeryCheck", "预期应有手术操作但未记录");
end',
'drg', 1, 'active', '校验手术操作是否完整记录', 90, 'surgery_check', 'HOSPITAL_001', 'admin'),

-- 规则组22：费用控制规则组
(22, 'rule.cost.drug_ratio', '药占比监控规则',
'package com.his.rules.cost;
import com.his.fact.CostFact;
import com.his.common.ResultLevel;
import java.math.BigDecimal;

rule "1. 药占比监控"
    salience 100
    when
        $f: CostFact(drugRatio != null, drugRatio.compareTo(new BigDecimal("0.30")) > 0)
    then
        $f.addResult(ResultLevel.WARN, "DrugRatio", "药占比超过30%，需重点关注");
end',
'quality', 1, 'active', '监控药品费用占比', 100, 'drug_ratio', 'HOSPITAL_001', 'admin'),

(22, 'rule.cost.consumable_ratio', '耗材占比监控规则',
'package com.his.rules.cost;
import com.his.fact.CostFact;
import com.his.common.ResultLevel;
import java.math.BigDecimal;

rule "2. 耗材占比监控"
    salience 95
    when
        $f: CostFact(consumableRatio != null, consumableRatio.compareTo(new BigDecimal("0.20")) > 0)
    then
        $f.addResult(ResultLevel.WARN, "ConsumableRatio", "耗材占比超过20%，需审核");
end',
'quality', 1, 'active', '监控耗材费用占比', 95, 'consumable_ratio', 'HOSPITAL_001', 'admin');

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 验证插入结果
SELECT 
    COUNT(*) AS total_rules,
    category,
    status,
    COUNT(*) AS count
FROM `rule_definition`
GROUP BY category, status;

SELECT 
    id,
    rule_key,
    rule_name,
    category,
    status,
    salience,
    create_time
FROM `rule_definition`
ORDER BY salience DESC;
