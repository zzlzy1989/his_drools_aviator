-- ============================================================
-- HIS 动态规则中台 - 规则组初始化数据脚本
-- 创建日期: 2026-05-05
-- 说明: 插入医疗机构常用的规则组数据
-- ============================================================

USE `his_rule_engine`;

-- 清空现有数据（保留自增起始值）
TRUNCATE TABLE `rule_group`;

-- ============================================================
-- 插入规则组数据（25条）
-- 涵盖：医保报销、合理用药、质控管理、DRG/DIP、院感防控等
-- ============================================================

INSERT INTO `rule_group` (`group_code`, `group_name`, `description`, `tenant_id`, `is_enabled`, `priority`, `create_by`) VALUES
-- 医保报销类 (1-6)
('REIMBURSEMENT', '医保结算规则组', '医保报销相关规则，包含职工医保、居民医保、大病保险等', 'HOSPITAL_001', 1, 10, 'admin'),
('REIMBURSE_INPATIENT', '住院报销规则组', '住院费用医保报销规则，包含起付线、报销比例、封顶线等', 'HOSPITAL_001', 1, 11, 'admin'),
('REIMBURSE_OUTPATIENT', '门诊报销规则组', '门诊费用医保报销规则，包含普通门诊、特殊病种门诊等', 'HOSPITAL_001', 1, 12, 'admin'),
('REIMBURSE_CRITICAL', '大病保险规则组', '大病保险报销规则，包含大病起付线、分段报销比例等', 'HOSPITAL_001', 1, 13, 'admin'),
('REIMBURSE_MEDICAID', '医疗救助规则组', '医疗救助报销规则，包含救助对象识别、救助比例等', 'HOSPITAL_001', 1, 14, 'admin'),
('REIMBURSE_SUPPLEMENT', '补充医疗保险规则组', '补充医疗保险报销规则，包含公务员补助、企业补充险等', 'HOSPITAL_001', 1, 15, 'admin'),

-- 合理用药类 (7-12)
('DRUG_CHECK', '合理用药规则组', '处方审核、配伍禁忌、用药极量等规则', 'HOSPITAL_001', 1, 20, 'admin'),
('DRUG_INTERACTION', '药品配伍禁忌规则组', '药品配伍禁忌检测规则，包含西药、中药配伍禁忌', 'HOSPITAL_001', 1, 21, 'admin'),
('DRUG_DOSAGE', '用药剂量规则组', '用药剂量检查规则，包含成人剂量、儿童剂量、老人剂量等', 'HOSPITAL_001', 1, 22, 'admin'),
('DRUG_ALLERGY', '药物过敏规则组', '药物过敏史检查规则，包含青霉素类、头孢类等过敏检测', 'HOSPITAL_001', 1, 23, 'admin'),
('DRUG_SPECIAL_POPULATION', '特殊人群用药规则组', '孕妇、哺乳期、儿童、老人等特殊人群用药限制规则', 'HOSPITAL_001', 1, 24, 'admin'),
('DRUG_ANTIBIOTIC', '抗菌药物使用规则组', '抗菌药物分级管理规则，包含非限制级、限制级、特殊级', 'HOSPITAL_001', 1, 25, 'admin'),

-- 质控管理类 (13-17)
('QUALITY_CONTROL', '质控规则组', '院感防控、质控指标等规则', 'HOSPITAL_001', 1, 30, 'admin'),
('QUALITY_INFECTION', '院感防控规则组', '医院感染防控规则，包含手卫生、消毒隔离、抗菌药物使用等', 'HOSPITAL_001', 1, 31, 'admin'),
('QUALITY_BLOOD', '输血质控规则组', '输血质量管理规则，包含输血指征、血型核对、不良反应监测等', 'HOSPITAL_001', 1, 32, 'admin'),
('QUALITY_SURGERY', '手术质控规则组', '手术质量安全管理规则，包含手术分级、术前讨论、术后并发症等', 'HOSPITAL_001', 1, 33, 'admin'),
('QUALITY_MEDICAL_RECORD', '病历质控规则组', '病历质量管理规则，包含病历书写及时性、完整性、甲级病历率等', 'HOSPITAL_001', 1, 34, 'admin'),

-- DRG/DIP类 (18-21)
('DRG_GROUPING', 'DRG分组规则组', 'DRG/DIP分组相关规则', 'HOSPITAL_001', 1, 40, 'admin'),
('DRG_WEIGHT', 'DRG权重规则组', 'DRG权重计算和调整规则，包含CMI计算、权重调整等', 'HOSPITAL_001', 1, 41, 'admin'),
('DRG_OUTLIER', 'DRG异常值规则组', 'DRG异常病例检测规则，包含高倍率、低倍率、极值病例等', 'HOSPITAL_001', 1, 42, 'admin'),
('DRG_PAYMENT', 'DRG支付规则组', 'DRG付费结算规则，包含基础支付、调整支付、结算清算等', 'HOSPITAL_001', 1, 43, 'admin'),

-- 费用管理类 (22-25)
('COST_CONTROL', '费用控制规则组', '医疗费用控制规则，包含次均费用、药占比、耗材占比等', 'HOSPITAL_001', 1, 50, 'admin'),
('COST_DRUG_RATIO', '药占比控制规则组', '药品费用占比控制规则，包含科室药占比、医生药占比等', 'HOSPITAL_001', 1, 51, 'admin'),
('COST_CONSUMABLE', '耗材使用规则组', '医用耗材使用管理规则，包含高值耗材、普通耗材使用监控', 'HOSPITAL_001', 1, 52, 'admin'),
('COST_EXAMINATION', '检查检验规则组', '检查检验合理性规则，包含重复检查、大型设备检查阳性率等', 'HOSPITAL_001', 1, 53, 'admin');

-- 验证插入结果
SELECT 
    COUNT(*) AS total_rules,
    SUM(CASE WHEN is_enabled = 1 THEN 1 ELSE 0 END) AS enabled_count,
    SUM(CASE WHEN is_enabled = 0 THEN 1 ELSE 0 END) AS disabled_count
FROM `rule_group`;

SELECT 
    id,
    group_code,
    group_name,
    is_enabled,
    priority,
    create_time
FROM `rule_group`
ORDER BY priority;
