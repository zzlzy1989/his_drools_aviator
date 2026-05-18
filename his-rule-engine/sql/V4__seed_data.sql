-- ============================================================
-- HIS 动态规则中台 V2.0 种子数据
-- 版本: V4__seed_data
-- 创建日期: 2026-05-17
-- 说明: 监控告警规则、规则模板、测试套件等示例数据
-- ============================================================

USE `his_rule_engine`;

-- ------------------------------------------------------------
-- 1. 监控告警规则示例数据
-- ------------------------------------------------------------
INSERT INTO `monitor_alert_rule` (`rule_name`, `metric_name`, `condition_type`, `threshold`, `level`, `enabled`, `notify_channels`, `notify_target`, `message_template`, `consecutive_triggers`, `cooldown_seconds`, `tenant_id`, `create_by`, `create_time`) VALUES
('执行失败率告警', 'execution_fail_rate', 'GT', 5.00, 'WARN', 1, 'DINGTALK', 'https://oapi.dingtalk.com/robot/send?access_token=xxx', '规则执行失败率超过 {value}%', 3, 300, 'T001', 'admin', NOW()),
('执行超时告警', 'execution_duration_ms', 'GT', 100.00, 'ERROR', 1, 'DINGTALK', 'https://oapi.dingtalk.com/robot/send?access_token=xxx', '规则执行时间超过 {value}ms', 1, 60, 'T001', 'admin', NOW()),
('规则命中数为零告警', 'rule_hit_count', 'LT', 1.00, 'WARN', 1, NULL, NULL, '规则 {metric} 连续无命中', 5, 600, 'T001', 'admin', NOW()),
('公式命中率低于95%', 'formula_hit_rate', 'LT', 95.00, 'WARN', 1, NULL, NULL, '公式命中率低于 {value}%', 2, 300, 'T001', 'admin', NOW());

-- ------------------------------------------------------------
-- 2. 规则模板示例数据
-- ------------------------------------------------------------
INSERT INTO `rule_template` (`template_key`, `template_name`, `description`, `category`, `tags`, `content`, `provider_tenant_id`, `provider_tenant_name`, `published_by`, `published_at`, `version`, `install_count`, `avg_rating`, `comment_count`, `status`, `tenant_id`, `create_by`, `create_time`) VALUES
('tpl.reimburse.employee.basic', '职工医保基础报销模板', '适用于职工医保的住院费用报销规则集，包含起付线、报销比例、封顶线计算', 'REIMBURSE', '["职工医保", "住院", "基础报销"]', '{"rules": [{"ruleName": "1. 身份校验", "ruleContent": "rule \\"1. 身份校验\\"\\n    salience 100\\n    when\\n        $f: SettlementFact(patientType == null || patientType != \\"EMPLOYEE\\")\\n    then\\n        $f.addResult(ResultLevel.BLOCK, \\"IdentityCheck\\", \\"患者非职工医保身份\\");\\nend"}], "formulas": [{"formulaName": "基本报销公式", "formulaText": "round((totalFee - deductible) * ratio, 2)"}], "flows": []}', 'T001', '测试医院', 'admin', NOW(), '1.0.0', 15, 4.50, 3, 'PUBLISHED', 'T001', 'admin', NOW()),

('tpl.reimburse.resident.basic', '居民医保基础报销模板', '适用于居民医保的门诊费用报销规则集', 'REIMBURSE', '["居民医保", "门诊", "基础报销"]', '{"rules": [{"ruleName": "1. 身份校验", "ruleContent": "rule \\"1. 身份校验\\"\\n    salience 100\\n    when\\n        $f: SettlementFact(patientType == null || patientType != \\"RESIDENT\\")\\n    then\\n        $f.addResult(ResultLevel.BLOCK, \\"IdentityCheck\\", \\"患者非居民医保身份\\");\\nend"}], "formulas": [{"formulaName": "基本报销公式", "formulaText": "round((totalFee - deductible) * ratio, 2)"}], "flows": []}', 'T001', '测试医院', 'admin', NOW(), '1.0.0', 8, 4.20, 2, 'PUBLISHED', 'T001', 'admin', NOW()),

('tpl.drug.interaction.check', '用药配伍禁忌检查模板', '检测处方中药品是否存在配伍禁忌', 'DRUG', '["用药审核", "配伍禁忌", "处方"]', '{"rules": [{"ruleName": "1. 配伍禁忌检测", "ruleContent": "rule \\"1. 配伍禁忌检测\\"\\n    salience 100\\n    when\\n        $f: PrescriptionFact(hasInteraction == true)\\n    then\\n        $f.addResult(ResultLevel.BLOCK, \\"DrugInteraction\\", \\"检测到配伍禁忌\\");\\nend"}], "formulas": [], "flows": []}', 'T002', '示范医院', 'admin', NOW(), '1.0.0', 23, 4.80, 5, 'PUBLISHED', 'T002', 'admin', NOW()),

('tpl.quality.infection.control', '院内感染控制规则模板', '用于监测和控制院内感染指标', 'QUALITY', '["院感", "感染控制", "监测"]', '{"rules": [{"ruleName": "1. 感染指标监测", "ruleContent": "rule \\"1. 感染指标监测\\"\\n    salience 50\\n    when\\n        $f: QualityFact(infectionRate > 0.05)\\n    then\\n        $f.addResult(ResultLevel.WARN, \\"InfectionControl\\", \\"感染率超过阈值\\");\\nend"}], "formulas": [{"formulaName": "感染率计算", "formulaText": "round(infectionCases / totalCases * 100, 2)"}], "flows": []}', 'T001', '测试医院', 'admin', NOW(), '1.0.0', 5, 4.00, 1, 'PUBLISHED', 'T001', 'admin', NOW()),

('tpl.drg.grouping.standard', 'DRG标准分组模板', '基于诊断和手术操作的DRG标准分组规则', 'DRG', '["DRG", "分组", "权重"]', '{"rules": [{"ruleName": "1. DRG主分组", "ruleContent": "rule \\"1. DRG主分组\\"\\n    salience 100\\n    when\\n        $f: DrgFact(diagnosisCode != null)\\n    then\\n        $f.setDrgCode($f.calculateDrgCode());\\nend"}], "formulas": [{"formulaName": "DRG权重计算", "formulaText": "baseWeight * complexityFactor"}], "flows": []}', 'T003', '示范医院', 'admin', NOW(), '1.0.0', 12, 4.60, 4, 'PUBLISHED', 'T003', 'admin', NOW());

-- ------------------------------------------------------------
-- 3. 模板评分示例数据
-- ------------------------------------------------------------
INSERT INTO `template_rating` (`template_id`, `rating`, `comment`, `tenant_id`, `create_by`, `create_time`) VALUES
(1, 5, '非常好用，覆盖了职工医保所有场景', 'T002', 'user1', NOW()),
(1, 4, '基本满足需求，但希望能增加封顶线计算', 'T003', 'user2', NOW()),
(1, 5, '配置简单，效果很好', 'T004', 'user3', NOW()),
(2, 4, '还不错，但居民医保门诊和住院应该分开', 'T001', 'user1', NOW()),
(2, 4, '希望能增加学生医保支持', 'T005', 'user4', NOW()),
(3, 5, '配伍禁忌检测非常准确', 'T001', 'admin', NOW()),
(3, 5, '大大减少了用药风险', 'T002', 'user2', NOW()),
(4, 4, '院感监测效果好', 'T001', 'user1', NOW()),
(5, 5, 'DRG分组逻辑准确', 'T001', 'admin', NOW());

-- ------------------------------------------------------------
-- 4. 测试套件示例数据
-- ------------------------------------------------------------
INSERT INTO `test_suite` (`suite_name`, `description`, `category`, `test_type`, `tenant_id`, `create_by`, `create_time`) VALUES
('职工医保结算测试套件', '验证职工医保各项结算规则的正确性', 'SETTLEMENT', 'UNIT', 'T001', 'admin', NOW()),
('居民医保结算测试套件', '验证居民医保各项结算规则的正确性', 'SETTLEMENT', 'UNIT', 'T001', 'admin', NOW()),
('用药审核测试套件', '验证处方审核规则的有效性', 'DRUG', 'UNIT', 'T001', 'admin', NOW()),
('DRG分组测试套件', '验证DRG分组规则的准确性', 'DRG', 'UNIT', 'T001', 'admin', NOW()),
('公式执行测试套件', '验证各公式计算结果的正确性', 'FORMULA', 'UNIT', 'T001', 'admin', NOW());

-- ------------------------------------------------------------
-- 5. 测试用例示例数据
-- ------------------------------------------------------------
INSERT INTO `test_case` (`suite_id`, `case_name`, `description`, `input_data`, `expected_result`, `assertion_type`, `enabled`, `tenant_id`, `create_by`, `create_time`) VALUES
-- 职工医保测试用例
(1, '起付线计算_职工', '验证职工医保起付线为1000元', '{"patientType": "EMPLOYEE", "totalFee": 5000, "deductible": 1000}', '5000', 'EQUALS', 1, 'T001', 'admin', NOW()),
(1, '报销比例计算_职工', '验证职工医保报销85%', '{"patientType": "EMPLOYEE", "totalFee": 10000, "deductible": 1000, "ratio": 0.85}', '7650', 'EQUALS', 1, 'T001', 'admin', NOW()),
(1, '封顶线校验_职工', '验证职工医保封顶线为30万', '{"patientType": "EMPLOYEE", "totalFee": 500000, "deductible": 1000, "ratio": 0.85}', '300000', 'LESS_THAN_OR_EQUALS', 1, 'T001', 'admin', NOW()),
(1, '起付线以下返回零', '验证费用低于起付线时返回零', '{"patientType": "EMPLOYEE", "totalFee": 500, "deductible": 1000, "ratio": 0.85}', '0', 'EQUALS', 1, 'T001', 'admin', NOW()),

-- 居民医保测试用例
(2, '起付线计算_居民', '验证居民医保起付线为500元', '{"patientType": "RESIDENT", "totalFee": 3000, "deductible": 500}', '3000', 'EQUALS', 1, 'T001', 'admin', NOW()),
(2, '报销比例计算_居民', '验证居民医保报销65%', '{"patientType": "RESIDENT", "totalFee": 5000, "deductible": 500, "ratio": 0.65}', '2925', 'EQUALS', 1, 'T001', 'admin', NOW()),
(2, '分段报销计算', '验证阶梯式报销', '{"patientType": "RESIDENT", "totalFee": 8000, "deductible": 500, "ratio": 0.65}', '4875', 'EQUALS', 1, 'T001', 'admin', NOW()),

-- 用药审核测试用例
(3, '配伍禁忌检测', '验证A药+B药配伍禁忌检测', '{"drugs": [{"code": "A001", "name": "维生素C"}, {"code": "B002", "name": "维生素K"}]}', 'BLOCK', 'EQUALS', 1, 'T001', 'admin', NOW()),
(3, '剂量超限检测', '验证超出最大剂量的检测', '{"dailyDosage": 2000, "maxDailyDosage": 1000}', 'WARN', 'EQUALS', 1, 'T001', 'admin', NOW()),
(3, '正常处方通过', '验证正常处方通过审核', '{"drugs": [{"code": "C001", "name": "阿莫西林"}]}', 'PASS', 'EQUALS', 1, 'T001', 'admin', NOW()),

-- DRG测试用例
(4, 'DRG主分组', '验证基于诊断的DRG分组', '{"diagnosisCode": "I21.0", "surgeryCode": null}', 'MDC', 'NOT_NULL', 1, 'T001', 'admin', NOW()),
(4, 'DRG权重计算', '验证DRG权重计算', '{"baseWeight": 1.5, "complexityFactor": 1.2}', '1.8', 'EQUALS', 1, 'T001', 'admin', NOW()),

-- 公式测试用例
(5, '基本报销公式', '验证 round((totalFee - deductible) * ratio, 2)', '{"totalFee": 10000, "deductible": 1000, "ratio": 0.85}', '7650', 'EQUALS', 1, 'T001', 'admin', NOW()),
(5, 'DRG权重调整公式', '验证 (baseWeight + extraPoints) * severityFactor', '{"baseWeight": 1.2, "extraPoints": 0.3, "severityFactor": 1.1}', '1.65', 'EQUALS', 1, 'T001', 'admin', NOW()),
(5, '阶梯式报销公式', '验证分段报销计算', '{"totalFee": 8000, "deductible": 500, "ratio": 0.65}', '4875', 'EQUALS', 1, 'T001', 'admin', NOW());

-- ------------------------------------------------------------
-- 6. 测试执行日志示例
-- ------------------------------------------------------------
INSERT INTO `test_execution_log` (`case_id`, `suite_id`, `execution_time`, `status`, `actual_result`, `error_message`, `duration_ms`, `tenant_id`, `create_time`) VALUES
(1, 1, NOW(), 'PASS', '5000', NULL, 15, 'T001', NOW()),
(2, 1, NOW(), 'PASS', '7650', NULL, 12, 'T001', NOW()),
(3, 1, NOW(), 'PASS', '300000', NULL, 18, 'T001', NOW()),
(4, 1, NOW(), 'PASS', '0', NULL, 10, 'T001', NOW()),
(5, 2, NOW(), 'PASS', '3000', NULL, 14, 'T001', NOW()),
(6, 2, NOW(), 'PASS', '2925', NULL, 11, 'T001', NOW()),
(7, 2, NOW(), 'PASS', '4875', NULL, 16, 'T001', NOW()),
(8, 3, NOW(), 'PASS', 'BLOCK', NULL, 20, 'T001', NOW()),
(9, 3, NOW(), 'PASS', 'WARN', NULL, 13, 'T001', NOW()),
(10, 3, NOW(), 'PASS', 'PASS', NULL, 9, 'T001', NOW()),
(11, 4, NOW(), 'PASS', 'MDC', NULL, 25, 'T001', NOW()),
(12, 4, NOW(), 'PASS', '1.8', NULL, 17, 'T001', NOW()),
(13, 5, NOW(), 'PASS', '7650', NULL, 8, 'T001', NOW()),
(14, 5, NOW(), 'PASS', '1.65', NULL, 7, 'T001', NOW()),
(15, 5, NOW(), 'PASS', '4875', NULL, 9, 'T001', NOW());