-- ============================================================
-- HIS 动态规则中台 - Aviator公式初始化数据脚本
-- 创建日期: 2026-05-05
-- 说明: 插入医疗机构常用的Aviator计算公式
-- ============================================================

USE `his_rule_engine`;

TRUNCATE TABLE `aviator_formula`;

-- ============================================================
-- 插入公式数据（25条）
-- 涵盖：医保报销、药品计算、DRG权重、质控指标等
-- ============================================================

INSERT INTO `aviator_formula` (`formula_key`, `formula_name`, `formula_text`, `category`, `version`, `status`, `description`, `is_validated`, `tenant_id`, `create_by`) VALUES
-- 医保报销类公式 (1-10)
('formula.reimburse.basic', '基本报销金额计算', 
'round(max(totalFee - deductible, 0) * ratio, 2)', 
'REIMBURSE', 1, 'active', '基本医保报销金额 = (总费用 - 起付线) × 报销比例', 1, 'HOSPITAL_001', 'admin'),

('formula.reimburse.staged', '阶梯式报销计算',
'let base = max(totalFee - deductible, 0);
base <= 10000 ? round(base * 0.85, 2) :
base <= 50000 ? round(10000 * 0.85 + (base - 10000) * 0.75, 2) :
round(10000 * 0.85 + 40000 * 0.75 + (base - 50000) * 0.65, 2)',
'REIMBURSE', 1, 'active', '分段报销：1万以下85%，1-5万75%，5万以上65%', 1, 'HOSPITAL_001', 'admin'),

('formula.reimburse.cap', '封顶线报销计算',
'let amount = round(max(totalFee - deductible, 0) * ratio, 2);
min(amount, capAmount)',
'REIMBURSE', 1, 'active', '考虑封顶线的报销计算', 1, 'HOSPITAL_001', 'admin'),

('formula.reimburse.employee', '职工医保报销计算',
'let base = max(totalFee - deductible, 0);
round(base * 0.90, 2)',
'REIMBURSE', 1, 'active', '职工医保报销比例90%', 1, 'HOSPITAL_001', 'admin'),

('formula.reimburse.resident', '居民医保报销计算',
'let base = max(totalFee - deductible, 0);
round(base * 0.70, 2)',
'REIMBURSE', 1, 'active', '居民医保报销比例70%', 1, 'HOSPITAL_001', 'admin'),

('formula.reimburse.aid', '医疗救助报销计算',
'let base = max(totalFee - deductible, 0);
let reimburse = round(base * 0.95, 2);
min(reimburse, 50000)',
'REIMBURSE', 1, 'active', '医疗救助报销95%，封顶5万元', 1, 'HOSPITAL_001', 'admin'),

('formula.reimburse.critical', '大病保险报销计算',
'let base = max(totalFee - 20000, 0);
base <= 50000 ? round(base * 0.60, 2) :
round(50000 * 0.60 + (base - 50000) * 0.70, 2)',
'REIMBURSE', 1, 'active', '大病保险：起付线2万，5万以下60%，5万以上70%', 1, 'HOSPITAL_001', 'admin'),

('formula.reimburse.outpatient', '门诊报销计算',
'let base = max(totalFee - 500, 0);
round(min(base * 0.60, 3000), 2)',
'REIMBURSE', 1, 'active', '门诊报销：起付线500，比例60%，封顶3000', 1, 'HOSPITAL_001', 'admin'),

('formula.reimburse.special_disease', '特殊病种门诊报销',
'let base = max(totalFee - 800, 0);
round(min(base * 0.80, 10000), 2)',
'REIMBURSE', 1, 'active', '特殊病种门诊：起付线800，比例80%，封顶1万', 1, 'HOSPITAL_001', 'admin'),

('formula.reimburse.self_pay', '自付金额计算',
'round(totalFee - reimburseAmount, 2)',
'REIMBURSE', 1, 'active', '自付金额 = 总费用 - 报销金额', 1, 'HOSPITAL_001', 'admin'),

-- 药品计算类公式 (11-15)
('formula.drug.daily_dose', '日用药剂量计算',
'round(totalDose / days, 2)',
'DRUG', 1, 'active', '日用药剂量 = 总剂量 / 用药天数', 1, 'HOSPITAL_001', 'admin'),

('formula.drug.cost_ratio', '药品费用占比计算',
'round(drugCost / totalFee * 100, 2)',
'DRUG', 1, 'active', '药品费用占比 = 药品费用 / 总费用 × 100%', 1, 'HOSPITAL_001', 'admin'),

('formula.drug.dosage_by_weight', '按体重计算剂量',
'round(weight * dosePerKg, 2)',
'DRUG', 1, 'active', '儿童用药剂量 = 体重 × 每公斤剂量', 1, 'HOSPITAL_001', 'admin'),

('formula.drug.dosage_by_bsa', '按体表面积计算剂量',
'round(sqrt(weight * height / 3600) * dosePerM2, 2)',
'DRUG', 1, 'active', '按体表面积计算剂量(化疗药物常用)', 1, 'HOSPITAL_001', 'admin'),

('formula.drug.infusion_rate', '输液速度计算',
'round(totalVolume * dropFactor / (hours * 60), 0)',
'DRUG', 1, 'active', '输液滴速 = 总量 × 滴系数 / (小时 × 60)', 1, 'HOSPITAL_001', 'admin'),

-- DRG相关公式 (16-20)
('formula.drg.weight', 'DRG权重计算',
'round(baseWeight * adjustFactor, 4)',
'DRG', 1, 'active', 'DRG权重 = 基础权重 × 调整因子', 1, 'HOSPITAL_001', 'admin'),

('formula.drg.payment', 'DRG支付金额计算',
'round(weight * baseRate, 2)',
'DRG', 1, 'active', 'DRG支付金额 = 权重 × 基础费率', 1, 'HOSPITAL_001', 'admin'),

('formula.drg.cmi', '病例组合指数CMI计算',
'round(sum(weight) / count, 4)',
'DRG', 1, 'active', 'CMI = 总权重 / 病例数', 1, 'HOSPITAL_001', 'admin'),

('formula.drg.cost_efficiency', 'DRG成本效率指数',
'round(actualCost / standardCost * 100, 2)',
'DRG', 1, 'active', '成本效率指数 = 实际成本 / 标准成本 × 100%', 1, 'HOSPITAL_001', 'admin'),

('formula.drg.outlier_threshold', 'DRG异常值阈值计算',
'round(avgCost + 2 * stdDev, 2)',
'DRG', 1, 'active', '异常值阈值 = 平均值 + 2 × 标准差', 1, 'HOSPITAL_001', 'admin'),

-- 质控指标公式 (21-25)
('formula.quality.avg_fee', '次均费用计算',
'round(totalFee / visitCount, 2)',
'GENERAL', 1, 'active', '次均费用 = 总费用 / 就诊人次', 1, 'HOSPITAL_001', 'admin'),

('formula.quality.bed_turnover', '床位周转率计算',
'round(dischargeCount / avgBeds * 100, 2)',
'GENERAL', 1, 'active', '床位周转率 = 出院人数 / 平均床位 × 100%', 1, 'HOSPITAL_001', 'admin'),

('formula.quality.bed_utilization', '床位使用率计算',
'round(occupiedBedDays / totalBedDays * 100, 2)',
'GENERAL', 1, 'active', '床位使用率 = 占用床日 / 总床日 × 100%', 1, 'HOSPITAL_001', 'admin'),

('formula.quality.avg_stay', '平均住院日计算',
'round(totalStayDays / dischargeCount, 1)',
'GENERAL', 1, 'active', '平均住院日 = 总住院日 / 出院人数', 1, 'HOSPITAL_001', 'admin'),

('formula.quality.cure_rate', '治愈率计算',
'round(cureCount / totalCount * 100, 2)',
'GENERAL', 1, 'active', '治愈率 = 治愈人数 / 总人数 × 100%', 1, 'HOSPITAL_001', 'admin');

-- 验证插入结果
SELECT 
    COUNT(*) AS total_formulas,
    category,
    status,
    COUNT(*) AS count
FROM `aviator_formula`
GROUP BY category, status;

SELECT 
    id,
    formula_key,
    formula_name,
    category,
    status,
    create_time
FROM `aviator_formula`
ORDER BY category, formula_key;
