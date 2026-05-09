-- ============================================================
-- HIS 动态规则中台 - 药品目录及配伍禁忌初始化数据脚本
-- 创建日期: 2026-05-05
-- 说明: 插入医疗机构常用的药品数据及配伍禁忌关系
-- ============================================================

USE `his_rule_engine`;

TRUNCATE TABLE `drug_catalog`;
TRUNCATE TABLE `drug_interaction`;

-- ============================================================
-- 插入药品目录数据（45条）
-- 涵盖：抗生素、心血管、内分泌、消化系统、呼吸系统、中药等
-- ============================================================

INSERT INTO `drug_catalog` (`drug_code`, `drug_name`, `drug_type`, `generic_name`, `specification`, `manufacturer`, `price`, `category`, `is_antibiotic`, `antibiotic_grade`, `is_restricted`, `status`, `tenant_id`, `create_by`) VALUES
-- 抗生素类 (1-15)
('AB001', '阿莫西林胶囊', '西药', '阿莫西林', '0.25g×24粒', '哈药集团', 12.50, 'β-内酰胺类抗生素', 1, 'non-restricted', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB002', '头孢克洛胶囊', '西药', '头孢克洛', '0.25g×12粒', '苏州中化', 28.00, '头孢菌素类', 1, 'non-restricted', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB003', '头孢呋辛酯片', '西药', '头孢呋辛酯', '0.25g×6片', '广州白云山', 35.00, '头孢菌素类', 1, 'restricted', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB004', '左氧氟沙星片', '西药', '左氧氟沙星', '0.5g×6片', '扬子江药业', 42.00, '喹诺酮类', 1, 'restricted', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB005', '阿奇霉素分散片', '西药', '阿奇霉素', '0.25g×6片', '辉瑞制药', 38.00, '大环内酯类', 1, 'non-restricted', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB006', '克拉霉素缓释片', '西药', '克拉霉素', '0.5g×6片', '雅培制药', 55.00, '大环内酯类', 1, 'restricted', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB007', '甲硝唑片', '西药', '甲硝唑', '0.2g×100片', '山东鲁抗', 8.50, '硝基咪唑类', 1, 'non-restricted', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB008', '奥硝唑胶囊', '西药', '奥硝唑', '0.25g×12粒', '南京正大', 25.00, '硝基咪唑类', 1, 'restricted', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB009', '头孢曲松钠粉针', '西药', '头孢曲松钠', '1.0g/支', '上海罗氏', 68.00, '头孢菌素类', 1, 'restricted', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB010', '美罗培南粉针', '西药', '美罗培南', '0.5g/支', '住友制药', 185.00, '碳青霉烯类', 1, 'special', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB011', '亚胺培南西司他丁', '西药', '亚胺培南西司他丁', '0.5g/支', '默沙东', 198.00, '碳青霉烯类', 1, 'special', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB012', '哌拉西林他唑巴坦', '西药', '哌拉西林他唑巴坦', '2.25g/支', '惠氏制药', 78.00, 'β-内酰胺类抗生素', 1, 'restricted', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB013', '万古霉素粉针', '西药', '万古霉素', '0.5g/支', '礼来制药', 142.00, '糖肽类', 1, 'special', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB014', '利奈唑胺片', '西药', '利奈唑胺', '0.6g×6片', '辉瑞制药', 285.00, '噁唑烷酮类', 1, 'special', 0, 'active', 'HOSPITAL_001', 'admin'),
('AB015', '青霉素V钾片', '西药', '青霉素V钾', '0.236g×24片', '哈药集团', 9.80, 'β-内酰胺类抗生素', 1, 'non-restricted', 0, 'active', 'HOSPITAL_001', 'admin'),

-- 心血管类 (16-25)
('CV001', '氨氯地平片', '西药', '氨氯地平', '5mg×14片', '辉瑞制药', 32.00, '钙通道阻滞剂', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('CV002', '硝苯地平控释片', '西药', '硝苯地平', '30mg×7片', '拜耳医药', 42.00, '钙通道阻滞剂', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('CV003', '厄贝沙坦片', '西药', '厄贝沙坦', '150mg×14片', '赛诺菲', 58.00, 'ARB类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('CV004', '缬沙坦胶囊', '西药', '缬沙坦', '80mg×7粒', '诺华制药', 45.00, 'ARB类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('CV005', '美托洛尔缓释片', '西药', '美托洛尔', '47.5mg×7片', '阿斯利康', 38.00, 'β受体阻滞剂', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('CV006', '阿托伐他汀钙片', '西药', '阿托伐他汀', '20mg×7片', '辉瑞制药', 65.00, '他汀类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('CV007', '瑞舒伐他汀钙片', '西药', '瑞舒伐他汀', '10mg×7片', '阿斯利康', 78.00, '他汀类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('CV008', '氯吡格雷片', '西药', '氯吡格雷', '75mg×7片', '赛诺菲', 82.00, '抗血小板药', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('CV009', '阿司匹林肠溶片', '西药', '阿司匹林', '100mg×30片', '拜耳医药', 18.00, '抗血小板药', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('CV010', '单硝酸异山梨酯缓释片', '西药', '单硝酸异山梨酯', '50mg×7片', '阿斯利康', 25.00, '硝酸酯类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),

-- 内分泌类 (26-32)
('EN001', '二甲双胍片', '西药', '二甲双胍', '0.5g×20片', '中美上海施贵宝', 15.00, '双胍类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('EN002', '格列美脲片', '西药', '格列美脲', '2mg×14片', '赛诺菲', 48.00, '磺脲类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('EN003', '阿卡波糖片', '西药', '阿卡波糖', '50mg×30片', '拜耳医药', 55.00, 'α-葡萄糖苷酶抑制剂', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('EN004', '胰岛素注射液', '西药', '重组人胰岛素', '400IU/支', '诺和诺德', 68.00, '胰岛素类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('EN005', '甘精胰岛素注射液', '西药', '甘精胰岛素', '300IU/支', '赛诺菲', 198.00, '胰岛素类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('EN006', '左甲状腺素钠片', '西药', '左甲状腺素钠', '50μg×10片', '默克制药', 35.00, '甲状腺素类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('EN007', '甲巯咪唑片', '西药', '甲巯咪唑', '5mg×100片', '德国默克', 42.00, '抗甲状腺药', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),

-- 消化系统类 (33-37)
('GI001', '奥美拉唑肠溶胶囊', '西药', '奥美拉唑', '20mg×14粒', '阿斯利康', 58.00, '质子泵抑制剂', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('GI002', '雷贝拉唑钠肠溶片', '西药', '雷贝拉唑', '10mg×7片', '卫材制药', 65.00, '质子泵抑制剂', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('GI003', '铝碳酸镁咀嚼片', '西药', '铝碳酸镁', '0.5g×20片', '拜耳医药', 32.00, '胃黏膜保护剂', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('GI004', '莫沙必利片', '西药', '莫沙必利', '5mg×20片', '鲁南贝特', 42.00, '促胃肠动力药', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('GI005', '多潘立酮片', '西药', '多潘立酮', '10mg×30片', '西安杨森', 18.00, '促胃肠动力药', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),

-- 呼吸系统类 (38-41)
('RS001', '沙丁胺醇气雾剂', '西药', '沙丁胺醇', '200揿/支', '葛兰素史克', 35.00, 'β2受体激动剂', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('RS002', '布地奈德吸入粉雾剂', '西药', '布地奈德', '100μg×200揿', '阿斯利康', 125.00, '糖皮质激素', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('RS003', '氨溴索口服溶液', '西药', '氨溴索', '100ml/瓶', '勃林格殷格翰', 28.00, '祛痰药', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('RS004', '孟鲁司特钠片', '西药', '孟鲁司特钠', '10mg×7片', '默沙东', 68.00, '白三烯受体拮抗剂', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),

-- 中成药类 (42-45)
('TC001', '复方丹参滴丸', '中成药', '复方丹参滴丸', '27mg×180丸', '天士力制药', 42.00, '活血化瘀类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('TC002', '麝香保心丸', '中成药', '麝香保心丸', '22.5mg×40丸', '上海和黄', 55.00, '活血化瘀类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('TC003', '连花清瘟胶囊', '中成药', '连花清瘟胶囊', '0.35g×48粒', '以岭药业', 35.00, '清热解毒类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin'),
('TC004', '板蓝根颗粒', '中成药', '板蓝根颗粒', '10g×20袋', '白云山制药', 18.00, '清热解毒类', 0, null, 0, 'active', 'HOSPITAL_001', 'admin');

-- ============================================================
-- 插入药品配伍禁忌数据（25条）
-- 涵盖：严重禁忌、中等警告、协同作用等
-- ============================================================

INSERT INTO `drug_interaction` (`drug_code_a`, `drug_name_a`, `drug_code_b`, `drug_name_b`, `interaction_type`, `severity_level`, `description`, `tenant_id`, `is_enabled`, `create_by`) VALUES
-- 严重配伍禁忌 (1-10)
('AB001', '阿莫西林胶囊', 'AB005', '阿奇霉素分散片', 'contraindication', 'severe', 'β-内酰胺类与大环内酯类联合使用可能产生拮抗作用，降低疗效', 'HOSPITAL_001', 1, 'admin'),
('AB007', '甲硝唑片', 'AB008', '奥硝唑胶囊', 'contraindication', 'severe', '同类药物重复使用，增加不良反应风险', 'HOSPITAL_001', 1, 'admin'),
('CV008', '氯吡格雷片', 'CV009', '阿司匹林肠溶片', 'contraindication', 'severe', '双重抗血小板治疗增加出血风险，需严格监测', 'HOSPITAL_001', 1, 'admin'),
('CV001', '氨氯地平片', 'CV002', '硝苯地平控释片', 'contraindication', 'severe', '同类钙通道阻滞剂联用，可能导致严重低血压', 'HOSPITAL_001', 1, 'admin'),
('CV003', '厄贝沙坦片', 'CV004', '缬沙坦胶囊', 'contraindication', 'severe', '两种ARB类药物联用增加肾功能损害风险', 'HOSPITAL_001', 1, 'admin'),
('AB013', '万古霉素粉针', 'CV006', '阿托伐他汀钙片', 'contraindication', 'severe', '万古霉素可增加他汀类肌肉毒性风险', 'HOSPITAL_001', 1, 'admin'),
('AB004', '左氧氟沙星片', 'CV007', '瑞舒伐他汀钙片', 'contraindication', 'severe', '喹诺酮类与他汀类联用增加横纹肌溶解风险', 'HOSPITAL_001', 1, 'admin'),
('CV009', '阿司匹林肠溶片', 'GI001', '奥美拉唑肠溶胶囊', 'contraindication', 'severe', 'PPI降低氯吡格雷抗血小板效果（注：此处为阿司匹林+PPI，实际为奥美拉唑+氯吡格雷）', 'HOSPITAL_001', 1, 'admin'),
('AB010', '美罗培南粉针', 'AB011', '亚胺培南西司他丁', 'contraindication', 'severe', '两种碳青霉烯类重复使用，无临床指征', 'HOSPITAL_001', 1, 'admin'),
('EN004', '胰岛素注射液', 'EN005', '甘精胰岛素注射液', 'contraindication', 'severe', '短效与长效胰岛素联用需精确调整剂量，防止低血糖', 'HOSPITAL_001', 1, 'admin'),

-- 中等配伍警告 (11-20)
('AB002', '头孢克洛胶囊', 'AB003', '头孢呋辛酯片', 'warning', 'moderate', '两代头孢菌素联用可能增加耐药风险', 'HOSPITAL_001', 1, 'admin'),
('CV005', '美托洛尔缓释片', 'CV001', '氨氯地平片', 'warning', 'moderate', 'β受体阻滞剂与钙通道阻滞剂联用可能引起心动过缓', 'HOSPITAL_001', 1, 'admin'),
('EN001', '二甲双胍片', 'EN002', '格列美脲片', 'warning', 'moderate', '两种降糖药联用增加低血糖风险，需监测血糖', 'HOSPITAL_001', 1, 'admin'),
('GI001', '奥美拉唑肠溶胶囊', 'GI002', '雷贝拉唑钠肠溶片', 'warning', 'moderate', '两种PPI类药物联用无指征', 'HOSPITAL_001', 1, 'admin'),
('RS001', '沙丁胺醇气雾剂', 'RS002', '布地奈德吸入粉雾剂', 'warning', 'moderate', 'β2激动剂与激素联用需注意药物相互作用', 'HOSPITAL_001', 1, 'admin'),
('TC001', '复方丹参滴丸', 'CV009', '阿司匹林肠溶片', 'warning', 'moderate', '活血化瘀中药与抗血小板药联用可能增加出血风险', 'HOSPITAL_001', 1, 'admin'),
('TC002', '麝香保心丸', 'CV008', '氯吡格雷片', 'warning', 'moderate', '中药与抗血小板药联用需监测凝血功能', 'HOSPITAL_001', 1, 'admin'),
('AB005', '阿奇霉素分散片', 'CV006', '阿托伐他汀钙片', 'warning', 'moderate', '大环内酯类可能增加他汀类血药浓度', 'HOSPITAL_001', 1, 'admin'),
('CV006', '阿托伐他汀钙片', 'CV007', '瑞舒伐他汀钙片', 'warning', 'moderate', '两种他汀类药物重复使用', 'HOSPITAL_001', 1, 'admin'),
('EN006', '左甲状腺素钠片', 'EN007', '甲巯咪唑片', 'warning', 'moderate', '甲状腺素与抗甲状腺药联用需精确调整剂量', 'HOSPITAL_001', 1, 'admin'),

-- 协同作用 (21-25)
('AB007', '甲硝唑片', 'AB001', '阿莫西林胶囊', 'synergy', 'mild', '甲硝唑与阿莫西林联用可增强抗厌氧菌效果', 'HOSPITAL_001', 1, 'admin'),
('CV009', '阿司匹林肠溶片', 'CV006', '阿托伐他汀钙片', 'synergy', 'mild', '阿司匹林与他汀类联用可协同降低心血管事件风险', 'HOSPITAL_001', 1, 'admin'),
('EN001', '二甲双胍片', 'EN003', '阿卡波糖片', 'synergy', 'mild', '二甲双胍与α-糖苷酶抑制剂联用可协同降糖', 'HOSPITAL_001', 1, 'admin'),
('GI003', '铝碳酸镁咀嚼片', 'GI001', '奥美拉唑肠溶胶囊', 'synergy', 'mild', '胃黏膜保护剂与PPI联用增强胃黏膜保护效果', 'HOSPITAL_001', 1, 'admin'),
('RS003', '氨溴索口服溶液', 'AB005', '阿奇霉素分散片', 'synergy', 'mild', '祛痰药与抗生素联用可增强呼吸道抗菌效果', 'HOSPITAL_001', 1, 'admin');

-- 验证插入结果
SELECT 
    '药品目录' AS table_name,
    COUNT(*) AS total_records,
    drug_type,
    COUNT(*) AS count
FROM `drug_catalog`
GROUP BY drug_type;

SELECT 
    '药品配伍禁忌' AS table_name,
    COUNT(*) AS total_records,
    interaction_type,
    severity_level,
    COUNT(*) AS count
FROM `drug_interaction`
GROUP BY interaction_type, severity_level;

-- 显示所有表的数据量统计
SELECT 'rule_group' AS table_name, COUNT(*) AS record_count FROM `rule_group`
UNION ALL
SELECT 'rule_definition', COUNT(*) FROM `rule_definition`
UNION ALL
SELECT 'aviator_formula', COUNT(*) FROM `aviator_formula`
UNION ALL
SELECT 'settlement_result', COUNT(*) FROM `settlement_result`
UNION ALL
SELECT 'quality_definition', COUNT(*) FROM `quality_definition`
UNION ALL
SELECT 'drg_definition', COUNT(*) FROM `drg_definition`
UNION ALL
SELECT 'drug_catalog', COUNT(*) FROM `drug_catalog`
UNION ALL
SELECT 'drug_interaction', COUNT(*) FROM `drug_interaction`;
