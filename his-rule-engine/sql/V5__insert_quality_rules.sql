-- ============================================================
-- HIS 动态规则中台 - 质控规则定义初始化数据脚本
-- 创建日期: 2026-05-05
-- 说明: 插入医疗机构常用的质控规则数据
-- ============================================================

USE `his_rule_engine`;

TRUNCATE TABLE `quality_definition`;

-- ============================================================
-- 插入质控规则数据（25条）
-- 涵盖：用药安全、诊断质控、费用监控、病历质量等
-- ============================================================

INSERT INTO `quality_definition` (`item_key`, `item_name`, `rule_text`, `category`, `level`, `status`, `description`, `salience`, `tenant_id`) VALUES
-- 用药安全类质控 (1-8)
('quality.drug.antibiotic_usage_rate', '抗菌药物使用率监控',
'package com.his.quality.drug;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "1. 抗菌药物使用率监控"
    salience 100
    when
        $f: QualityFact(antibioticUsageRate != null, antibioticUsageRate > 0.60)
    then
        $f.addResult(ResultLevel.WARN, "AntibioticUsage", "抗菌药物使用率超过60%");
        log.warn("科室抗菌药物使用率过高: rate={}", $f.getAntibioticUsageRate());
end',
'medication_safety', 'WARN', 'active', '监控科室抗菌药物使用率是否超标', 100, 'HOSPITAL_001'),

('quality.drug.injection_rate', '注射剂使用率监控',
'package com.his.quality.drug;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "2. 注射剂使用率监控"
    salience 95
    when
        $f: QualityFact(injectionRate != null, injectionRate > 0.50)
    then
        $f.addResult(ResultLevel.WARN, "InjectionRate", "注射剂使用率超过50%");
end',
'medication_safety', 'WARN', 'active', '监控注射剂使用比例', 95, 'HOSPITAL_001'),

('quality.drug.iv_rate', '静脉输液率监控',
'package com.his.quality.drug;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "3. 静脉输液率监控"
    salience 90
    when
        $f: QualityFact(ivRate != null, ivRate > 0.70)
    then
        $f.addResult(ResultLevel.WARN, "IVRate", "静脉输液率超过70%");
end',
'medication_safety', 'WARN', 'active', '监控静脉输液使用率', 90, 'HOSPITAL_001'),

('quality.drug.generic_rate', '通用名使用率监控',
'package com.his.quality.drug;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "4. 通用名使用率监控"
    salience 85
    when
        $f: QualityFact(genericRate != null, genericRate < 0.90)
    then
        $f.addResult(ResultLevel.WARN, "GenericRate", "通用名使用率低于90%");
end',
'medication_safety', 'WARN', 'active', '监控药品通用名使用率', 85, 'HOSPITAL_001'),

('quality.drug.prescription_avg_drugs', '处方平均药品数监控',
'package com.his.quality.drug;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "5. 处方平均药品数监控"
    salience 80
    when
        $f: QualityFact(avgDrugsPerPrescription != null, avgDrugsPerPrescription > 3.0)
    then
        $f.addResult(ResultLevel.WARN, "AvgDrugs", "处方平均药品数超过3种");
end',
'medication_safety', 'WARN', 'active', '监控处方药品数量是否合理', 80, 'HOSPITAL_001'),

('quality.drug.ppi_usage', '质子泵抑制剂使用监控',
'package com.his.quality.drug;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "6. 质子泵抑制剂使用监控"
    salience 75
    when
        $f: QualityFact(ppiUsageRate != null, ppiUsageRate > 0.30)
    then
        $f.addResult(ResultLevel.WARN, "PPIUsage", "质子泵抑制剂使用率超过30%");
end',
'medication_safety', 'WARN', 'active', '监控PPI类药物使用情况', 75, 'HOSPITAL_001'),

('quality.drug.auxiliary_drug', '辅助用药使用监控',
'package com.his.quality.drug;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "7. 辅助用药使用监控"
    salience 70
    when
        $f: QualityFact(auxiliaryDrugRate != null, auxiliaryDrugRate > 0.15)
    then
        $f.addResult(ResultLevel.WARN, "AuxiliaryDrug", "辅助用药占比超过15%");
end',
'medication_safety', 'WARN', 'active', '监控辅助用药使用比例', 70, 'HOSPITAL_001'),

('quality.drug.expensive_drug', '高价药品使用监控',
'package com.his.quality.drug;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "8. 高价药品使用监控"
    salience 65
    when
        $f: QualityFact(expensiveDrugRate != null, expensiveDrugRate > 0.20)
    then
        $f.addResult(ResultLevel.WARN, "ExpensiveDrug", "高价药品占比超过20%");
end',
'medication_safety', 'WARN', 'active', '监控高价药品使用情况', 65, 'HOSPITAL_001'),

-- 诊断质控类 (9-13)
('quality.diagnosis.icd_accuracy', 'ICD编码准确率监控',
'package com.his.quality.diagnosis;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "1. ICD编码准确率监控"
    salience 100
    when
        $f: QualityFact(icdAccuracy != null, icdAccuracy < 0.95)
    then
        $f.addResult(ResultLevel.WARN, "ICDAccuracy", "ICD编码准确率低于95%");
end',
'diagnosis_quality', 'WARN', 'active', '监控疾病编码准确率', 100, 'HOSPITAL_001'),

('quality.diagnosis.missing_diagnosis', '漏诊率监控',
'package com.his.quality.diagnosis;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "2. 漏诊率监控"
    salience 95
    when
        $f: QualityFact(missingDiagnosisRate != null, missingDiagnosisRate > 0.02)
    then
        $f.addResult(ResultLevel.WARN, "MissingDiagnosis", "漏诊率超过2%");
end',
'diagnosis_quality', 'WARN', 'active', '监控临床漏诊情况', 95, 'HOSPITAL_001'),

('quality.diagnosis.complication_rate', '并发症发生率监控',
'package com.his.quality.diagnosis;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "3. 并发症发生率监控"
    salience 90
    when
        $f: QualityFact(complicationRate != null, complicationRate > 0.05)
    then
        $f.addResult(ResultLevel.WARN, "ComplicationRate", "并发症发生率超过5%");
end',
'diagnosis_quality', 'WARN', 'active', '监控手术并发症发生率', 90, 'HOSPITAL_001'),

('quality.diagnosis.readmission_rate', '30天再入院率监控',
'package com.his.quality.diagnosis;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "4. 30天再入院率监控"
    salience 85
    when
        $f: QualityFact(readmissionRate != null, readmissionRate > 0.05)
    then
        $f.addResult(ResultLevel.WARN, "ReadmissionRate", "30天再入院率超过5%");
end',
'diagnosis_quality', 'WARN', 'active', '监控30天内非计划再入院率', 85, 'HOSPITAL_001'),

('quality.diagnosis.mortality_rate', '住院死亡率监控',
'package com.his.quality.diagnosis;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "5. 住院死亡率监控"
    salience 80
    when
        $f: QualityFact(mortalityRate != null, mortalityRate > 0.03)
    then
        $f.addResult(ResultLevel.WARN, "MortalityRate", "住院死亡率超过3%");
end',
'diagnosis_quality', 'WARN', 'active', '监控住院患者死亡率', 80, 'HOSPITAL_001'),

-- 费用监控类 (14-18)
('quality.cost.avg_inpatient_fee', '次均住院费用监控',
'package com.his.quality.cost;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;
import java.math.BigDecimal;

rule "1. 次均住院费用监控"
    salience 100
    when
        $f: QualityFact(avgInpatientFee != null, avgInpatientFee.compareTo(new BigDecimal("15000")) > 0)
    then
        $f.addResult(ResultLevel.WARN, "AvgFee", "次均住院费用超过15000元");
end',
'cost_monitoring', 'WARN', 'active', '监控次均住院费用', 100, 'HOSPITAL_001'),

('quality.cost.avg_outpatient_fee', '次均门诊费用监控',
'package com.his.quality.cost;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;
import java.math.BigDecimal;

rule "2. 次均门诊费用监控"
    salience 95
    when
        $f: QualityFact(avgOutpatientFee != null, avgOutpatientFee.compareTo(new BigDecimal("500")) > 0)
    then
        $f.addResult(ResultLevel.WARN, "AvgOutpatientFee", "次均门诊费用超过500元");
end',
'cost_monitoring', 'WARN', 'active', '监控次均门诊费用', 95, 'HOSPITAL_001'),

('quality.cost.drug_ratio', '药占比监控',
'package com.his.quality.cost;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;
import java.math.BigDecimal;

rule "3. 药占比监控"
    salience 90
    when
        $f: QualityFact(drugRatio != null, drugRatio.compareTo(new BigDecimal("0.30")) > 0)
    then
        $f.addResult(ResultLevel.WARN, "DrugRatio", "药占比超过30%");
end',
'cost_monitoring', 'WARN', 'active', '监控药品费用占比', 90, 'HOSPITAL_001'),

('quality.cost.consumable_ratio', '耗材占比监控',
'package com.his.quality.cost;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;
import java.math.BigDecimal;

rule "4. 耗材占比监控"
    salience 85
    when
        $f: QualityFact(consumableRatio != null, consumableRatio.compareTo(new BigDecimal("0.20")) > 0)
    then
        $f.addResult(ResultLevel.WARN, "ConsumableRatio", "耗材占比超过20%");
end',
'cost_monitoring', 'WARN', 'active', '监控医用耗材占比', 85, 'HOSPITAL_001'),

('quality.cost.exam_ratio', '检查检验占比监控',
'package com.his.quality.cost;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;
import java.math.BigDecimal;

rule "5. 检查检验占比监控"
    salience 80
    when
        $f: QualityFact(examRatio != null, examRatio.compareTo(new BigDecimal("0.25")) > 0)
    then
        $f.addResult(ResultLevel.WARN, "ExamRatio", "检查检验占比超过25%");
end',
'cost_monitoring', 'WARN', 'active', '监控检查检验费用占比', 80, 'HOSPITAL_001'),

-- 病历质量类 (19-23)
('quality.record.completion_rate', '病历完成率监控',
'package com.his.quality.record;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "1. 病历完成率监控"
    salience 100
    when
        $f: QualityFact(recordCompletionRate != null, recordCompletionRate < 0.98)
    then
        $f.addResult(ResultLevel.WARN, "CompletionRate", "病历完成率低于98%");
end',
'medical_record', 'WARN', 'active', '监控病历书写完成率', 100, 'HOSPITAL_001'),

('quality.record.timeliness', '病历书写及时性监控',
'package com.his.quality.record;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "2. 病历书写及时性监控"
    salience 95
    when
        $f: QualityFact(recordTimelinessRate != null, recordTimelinessRate < 0.95)
    then
        $f.addResult(ResultLevel.WARN, "Timeliness", "病历书写及时率低于95%");
end',
'medical_record', 'WARN', 'active', '监控病历书写及时性', 95, 'HOSPITAL_001'),

('quality.record.grade_a_rate', '甲级病历率监控',
'package com.his.quality.record;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "3. 甲级病历率监控"
    salience 90
    when
        $f: QualityFact(gradeARate != null, gradeARate < 0.90)
    then
        $f.addResult(ResultLevel.WARN, "GradeARate", "甲级病历率低于90%");
end',
'medical_record', 'WARN', 'active', '监控甲级病历比例', 90, 'HOSPITAL_001'),

('quality.record.signature_rate', '签名完整率监控',
'package com.his.quality.record;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "4. 签名完整率监控"
    salience 85
    when
        $f: QualityFact(signatureRate != null, signatureRate < 0.98)
    then
        $f.addResult(ResultLevel.WARN, "SignatureRate", "签名完整率低于98%");
end',
'medical_record', 'WARN', 'active', '监控病历签名完整性', 85, 'HOSPITAL_001'),

('quality.record.modification_rate', '病历修改率监控',
'package com.his.quality.record;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "5. 病历修改率监控"
    salience 80
    when
        $f: QualityFact(modificationRate != null, modificationRate > 0.10)
    then
        $f.addResult(ResultLevel.WARN, "ModificationRate", "病历修改率超过10%");
end',
'medical_record', 'WARN', 'active', '监控病历修改频率', 80, 'HOSPITAL_001'),

-- 院感防控类 (24-25)
('quality.infection.hai_rate', '医院感染发生率监控',
'package com.his.quality.infection;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "1. 医院感染发生率监控"
    salience 100
    when
        $f: QualityFact(haiRate != null, haiRate > 0.03)
    then
        $f.addResult(ResultLevel.WARN, "HAIRate", "院感发生率超过3%");
end',
'infection_control', 'WARN', 'active', '监控医院感染发生率', 100, 'HOSPITAL_001'),

('quality.infection.hand_hygiene', '手卫生依从性监控',
'package com.his.quality.infection;
import com.his.fact.QualityFact;
import com.his.common.ResultLevel;

rule "2. 手卫生依从性监控"
    salience 95
    when
        $f: QualityFact(handHygieneRate != null, handHygieneRate < 0.85)
    then
        $f.addResult(ResultLevel.WARN, "HandHygiene", "手卫生依从性低于85%");
end',
'infection_control', 'WARN', 'active', '监控手卫生依从性指标', 95, 'HOSPITAL_001');

-- 验证插入结果
SELECT 
    COUNT(*) AS total_quality_rules,
    category,
    status,
    COUNT(*) AS count
FROM `quality_definition`
GROUP BY category, status;

SELECT 
    id,
    item_key,
    item_name,
    category,
    status,
    salience,
    create_time
FROM `quality_definition`
ORDER BY category, salience DESC;
