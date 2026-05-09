-- ============================================================
-- HIS 动态规则中台 - 结算记录初始化数据脚本
-- 创建日期: 2026-05-05
-- 说明: 插入真实的医疗结算记录数据
-- ============================================================

USE `his_rule_engine`;

TRUNCATE TABLE `settlement_result`;

-- ============================================================
-- 插入结算记录（25条）
-- 涵盖：职工医保、居民医保、大病保险、医疗救助等真实场景
-- ============================================================

INSERT INTO `settlement_result` (`settlement_no`, `visit_id`, `patient_id`, `patient_type`, `insurance_type`, `hospital_level`, `total_fee`, `deductible`, `ratio`, `reimburse_amount`, `self_pay_amount`, `result_level`, `skill_results`, `status`, `tenant_id`, `create_by`) VALUES
-- 职工医保住院结算 (1-8)
('ST2026050100001', 'V20260501001', 'P100001', 'employee', '职工医保', '3', 15800.00, 1300.00, 0.9000, 13050.00, 2750.00, 'PASS', 
 '[{"level":"PASS","source":"IdentityCheck","message":"职工医保身份校验通过"},{"level":"PASS","source":"FeeValidation","message":"费用合法"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050100002', 'V20260501002', 'P100002', 'employee', '职工医保', '3', 28500.00, 1300.00, 0.9000, 24480.00, 4020.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"},{"level":"PASS","source":"CapCheck","message":"未超封顶线"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050100003', 'V20260501003', 'P100003', 'employee', '职工医保', '2', 8900.00, 800.00, 0.9000, 7290.00, 1610.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050100004', 'V20260501004', 'P100004', 'employee', '职工医保', '3', 52000.00, 1300.00, 0.9000, 45630.00, 6370.00, 'PASS',
 '[{"level":"WARN","source":"CapCheck","message":"费用较高，接近封顶线"},{"level":"PASS","source":"FeeValidation","message":"费用合法"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050100005', 'V20260501005', 'P100005', 'employee', '职工医保', '3', 3200.00, 1300.00, 0.9000, 1710.00, 1490.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050100006', 'V20260501006', 'P100006', 'employee', '职工医保', '3', 125000.00, 1300.00, 0.9000, 111330.00, 13670.00, 'PASS',
 '[{"level":"WARN","source":"ICUCheck","message":"ICU费用较高"},{"level":"PASS","source":"FeeValidation","message":"费用合法"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050100007', 'V20260501007', 'P100007', 'employee', '职工医保', '2', 6700.00, 800.00, 0.9000, 5310.00, 1390.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050100008', 'V20260501008', 'P100008', 'employee', '职工医保', '3', 45600.00, 1300.00, 0.9000, 39870.00, 5730.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"},{"level":"PASS","source":"FeeValidation","message":"费用合法"}]',
 'completed', 'HOSPITAL_001', 'system'),

-- 居民医保住院结算 (9-16)
('ST2026050200001', 'V20260502001', 'P200001', 'resident', '居民医保', '3', 12000.00, 1000.00, 0.7000, 7700.00, 4300.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"居民医保身份校验通过"},{"level":"PASS","source":"DeductibleCheck","message":"已达起付线"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050200002', 'V20260502002', 'P200002', 'resident', '居民医保', '2', 8500.00, 500.00, 0.7000, 5600.00, 2900.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050200003', 'V20260502003', 'P200003', 'resident', '居民医保', '3', 23000.00, 1000.00, 0.7000, 15400.00, 7600.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"},{"level":"PASS","source":"FeeValidation","message":"费用合法"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050200004', 'V20260502004', 'P200004', 'resident', '居民医保', '3', 65000.00, 1000.00, 0.7000, 44800.00, 20200.00, 'PASS',
 '[{"level":"WARN","source":"CapCheck","message":"费用较高，建议关注"},{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050200005', 'V20260502005', 'P200005', 'resident', '居民医保', '2', 4500.00, 500.00, 0.7000, 2800.00, 1700.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050200006', 'V20260502006', 'P200006', 'resident', '居民医保', '3', 15800.00, 1000.00, 0.7000, 10360.00, 5440.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"},{"level":"PASS","source":"FeeValidation","message":"费用合法"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050200007', 'V20260502007', 'P200007', 'resident', '居民医保', '3', 95000.00, 1000.00, 0.7000, 65800.00, 29200.00, 'PASS',
 '[{"level":"WARN","source":"CapCheck","message":"费用较高"},{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050200008', 'V20260502008', 'P200008', 'resident', '居民医保', '2', 7200.00, 500.00, 0.7000, 4690.00, 2510.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"}]',
 'completed', 'HOSPITAL_001', 'system'),

-- 大病保险结算 (17-20)
('ST2026050300001', 'V20260503001', 'P300001', 'employee', '大病保险', '3', 185000.00, 20000.00, 0.6000, 99000.00, 86000.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"},{"level":"WARN","source":"CriticalCheck","message":"触发大病保险"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050300002', 'V20260503002', 'P300002', 'resident', '大病保险', '3', 256000.00, 20000.00, 0.6000, 141600.00, 114400.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"},{"level":"WARN","source":"CriticalCheck","message":"高额费用，大病保险生效"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050300003', 'V20260503003', 'P300003', 'employee', '大病保险', '3', 98000.00, 20000.00, 0.6000, 46800.00, 51200.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"},{"level":"WARN","source":"CriticalCheck","message":"触发大病保险"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050300004', 'V20260503004', 'P300004', 'resident', '大病保险', '3', 320000.00, 20000.00, 0.6000, 180000.00, 140000.00, 'PASS',
 '[{"level":"WARN","source":"CapCheck","message":"接近年度封顶线"},{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"}]',
 'completed', 'HOSPITAL_001', 'system'),

-- 门诊结算 (21-23)
('ST2026050400001', 'V20260504001', 'P400001', 'employee', '职工门诊', '3', 2800.00, 500.00, 0.6000, 1380.00, 1420.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"},{"level":"PASS","source":"OutpatientCheck","message":"门诊结算"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050400002', 'V20260504002', 'P400002', 'resident', '居民门诊', '2', 1500.00, 300.00, 0.5500, 660.00, 840.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"身份校验通过"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050400003', 'V20260504003', 'P400003', 'employee', '特殊病种', '3', 12000.00, 800.00, 0.8000, 8960.00, 3040.00, 'PASS',
 '[{"level":"PASS","source":"IdentityCheck","message":"特殊病种身份校验通过"},{"level":"PASS","source":"SpecialDiseaseCheck","message":"特殊病种门诊"}]',
 'completed', 'HOSPITAL_001', 'system'),

-- 未达起付线结算 (24-25)
('ST2026050500001', 'V20260505001', 'P500001', 'resident', '居民医保', '3', 800.00, 1000.00, 0.7000, 0.00, 800.00, 'WARN',
 '[{"level":"WARN","source":"DeductibleCheck","message":"未达起付线，不予报销"}]',
 'completed', 'HOSPITAL_001', 'system'),

('ST2026050500002', 'V20260505002', 'P500002', 'employee', '职工医保', '2', 600.00, 800.00, 0.9000, 0.00, 600.00, 'WARN',
 '[{"level":"WARN","source":"DeductibleCheck","message":"未达起付线，不予报销"}]',
 'completed', 'HOSPITAL_001', 'system');

-- 验证插入结果
SELECT 
    COUNT(*) AS total_settlements,
    patient_type,
    status,
    COUNT(*) AS count
FROM `settlement_result`
GROUP BY patient_type, status;

SELECT 
    id,
    settlement_no,
    patient_id,
    patient_type,
    total_fee,
    deductible,
    ratio,
    reimburse_amount,
    self_pay_amount,
    result_level,
    status,
    create_time
FROM `settlement_result`
ORDER BY create_time DESC;
