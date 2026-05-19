# HIS 规则引擎配置指南

> 本文档说明如何配置和使用 HIS 动态规则中台的各个模块

---

## 一、租户配置

### 1.1 添加新租户

```sql
-- 在 tenant_config 表中添加租户
INSERT INTO tenant_config (tenant_id, tenant_name, status, create_by, create_time)
VALUES ('T002', '测试医院2', 'active', 'system', NOW());
```

### 1.2 租户配置参数

| 参数 | 说明 | 示例 |
|------|------|------|
| tenant_id | 租户唯一标识 | T001 |
| tenant_name | 租户名称 | 测试医院1 |
| status | 状态: active/inactive | active |

---

## 二、Drools 规则引擎配置

### 2.1 Drools 配置参数

在 `application.yml` 或 Nacos 配置中心配置：

```yaml
his:
  drools:
    enabled: true                    # 是否启用Drools
    session-pool-size: 10           # KIE Session池大小
    execution-timeout-ms: 10000     # 规则执行超时时间(毫秒)
    kie-base-name: defaultKieBase   # KIE Base名称
    kie-session-name: defaultKieSession  # KIE Session名称
    rule-file-path: rules/           # 规则文件扫描路径
    rule-trace-enabled: false        # 是否开启规则追踪
```

### 2.2 核心组件说明

| 组件 | 类 | 说明 |
|------|---|------|
| DroolsConfig | `com.his.common.drools.config.DroolsConfig` | Drools配置属性类 |
| KieSessionManager | `com.his.common.drools.engine.KieSessionManager` | KIE Session管理器 |
| RuleEngineTemplate | `com.his.common.drools.engine.RuleEngineTemplate` | 规则执行模板 |
| RuleDefinitionCache | `com.his.common.drools.cache.RuleDefinitionCache` | 规则定义缓存 |
| DroolsHelper | `com.his.common.drools.helper.DroolsHelper` | DRL工具类 |

### 2.3 创建Drools规则（DRL语法）

**DRL文件结构**
```drools
package com.his.rules.{module};

import com.his.fact.SettlementFact;
import com.his.common.ResultLevel;

// 全局变量
global java.util.List results;

// 规则列表
rule "1. 规则名称"
    salience 100    // 优先级，数字越大越先执行
    when
        // 条件 (LHS)
        $f: SettlementFact(patientType == "employee")
    then
        // 动作 (RHS)
        $f.setDeductible(new java.math.BigDecimal("1000"));
        update($f);
        results.add(new SkillResult(ResultLevel.PASS, "RuleAgent", "规则执行成功"));
end
```

### 2.4 常用DRL模板

**设置起付线规则**
```drools
rule "1. 职工医保起付线"
 salience 100
 when
    $f: com.his.fact.SettlementFact(patientType == "employee")
 then
    $f.setDeductible(new java.math.BigDecimal("1000"));
    update($f);
end
```

**设置报销比例规则**
```drools
rule "2. 职工医保报销比例"
 salience 90
 when
    $f: com.his.fact.SettlementFact(patientType == "employee", totalFee != null)
 then
    $f.setRatio(new java.math.BigDecimal("0.85"));
    update($f);
end
```

**封顶线检查规则**
```drools
rule "3. 封顶线检查"
 salience 80
 when
    $f: com.his.fact.SettlementFact(totalFee > 50000)
 then
    $f.setCapAmount(new java.math.BigDecimal("50000"));
    update($f);
end
```

**调用Aviator公式规则**
```drools
rule "4. 报销金额计算"
 salience 10
 when
    $f: com.his.fact.SettlementFact(deductible != null, ratio != null)
 then
    // 直接调用AviatorHelper执行公式
    Map<String, Object> env = new java.util.HashMap<>();
    env.put("totalFee", $f.getTotalFee());
    env.put("deductible", $f.getDeductible());
    env.put("ratio", $f.getRatio());
    Object result = com.his.common.aviator.helper.AviatorHelper.execute(
        "round((totalFee - deductible) * ratio, 2)", env);
    $f.setFinalAmount(new java.math.BigDecimal(result.toString()));
    update($f);
end
```

**过敏史检查规则**
```drools
rule "1. 过敏史检查"
 salience 100
 when
    $f: com.his.fact.PrescriptionFact(hasAllergy == true)
 then
    $f.setBlockResult(true);
    $f.addWarnMessage("患者存在药物过敏史");
end
```

**DRG分组规则**
```drools
rule "1. J18.x 分组"
 salience 100
 when
    $f: com.his.fact.DrgFact(diagnosisCode startsWith "J18")
 then
    $f.setDrgCode("BJ11");
    $f.setDrgName("呼吸系统感染/炎症");
end
```

### 2.5 DRL语法要点

| 语法 | 说明 | 示例 |
|------|------|------|
| `rule "名称"` | 定义规则 | `rule "1. 起付线"` |
| `salience N` | 优先级 | `salience 100` |
| `when` | 条件开始 | LHS (Left Hand Side) |
| `then` | 动作开始 | RHS (Right Hand Side) |
| `$f: Class()` | 绑定Fact对象 | `$f: SettlementFact()` |
| `==` | 等于比较 | `patientType == "employee"` |
| `!=` | 不等于 | `status != "BLOCKED"` |
| `>` `<` `>=` `<=` | 比较运算 | `totalFee > 1000` |
| `&&` `\|\|` | 逻辑运算 | `totalFee != null && ratio != null` |
| `startsWith` | 字符串前缀 | `diagnosisCode startsWith "J18"` |
| `update($f)` | 更新Fact | 触发规则重新匹配 |
| `insert(new Fact())` | 插入新Fact | 添加工作内存 |
| `retract($f)` | 删除Fact | 从工作内存移除 |

### 2.6 Drools API使用

**获取KieSession**
```java
@Autowired
private KieSessionManager kieSessionManager;

// 获取默认规则组的Session
KieSession kieSession = kieSessionManager.getKieSession("REIMBURSEMENT");
```

**执行规则**
```java
@Autowired
private RuleEngineTemplate ruleEngine;

// 创建上下文和Fact
SkillContext<?> context = new SkillContext<>();
SettlementFact fact = new SettlementFact();
fact.setPatientType("employee");
fact.setTotalFee(new BigDecimal("15000"));

// 执行规则
ruleEngine.fireRules(context, fact);

// 获取执行结果
List<SkillResult> results = context.getResults();
```

**DRL语法验证**
```java
// 验证DRL内容是否正确
Results results = DroolsHelper.validateDrl(drlContent);
if (results.hasMessages(Message.Level.ERROR)) {
    // DRL语法错误
    results.getMessages().forEach(msg -> log.error(msg.toString()));
    return false;
}
return true;
```

**编译DRL文件**
```java
// 从classpath编译DRL文件
boolean success = DroolsHelper.compileDrlFromClasspath("/rules/reimbursement.drl");
if (!success) {
    log.error("DRL编译失败");
}
```

### 2.7 KieBase分组策略

| 分组 | 规则组ID | 用途 |
|------|-----------|------|
| REIMBURSEMENT | 1 | 医保报销规则 |
| DRUG_CHECK | 2 | 用药审核规则 |
| QUALITY_CONTROL | 3 | 质控规则 |
| DRG_GROUPING | 4 | DRG分组规则 |

**按租户隔离的KieBase**
```java
// 获取租户特定的规则组
String ruleGroup = "REIMBURSEMENT_" + tenantId;  // 如 "REIMBURSEMENT_T001"
KieSession session = kieSessionManager.getKieSession(ruleGroup);
```

### 2.8 Session池配置

```yaml
his:
  drools:
    session-pool-size: 10  # 根据并发量调整
```

**Session复用策略**
- `KieSessionManager` 使用 `ConcurrentHashMap` 缓存Session
- 相同ruleGroup获取同一Session实例
- 使用完毕后调用 `dispose()` 归还池中

### 2.9 规则执行超时配置

```yaml
his:
  drools:
    execution-timeout-ms: 10000  # 10秒超时
```

**超时处理**
```java
// RuleEngineTemplate 中的超时处理
long startTime = System.nanoTime();
int rulesFired = kieSession.fireAllRules();
long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

if (elapsedMs > timeoutMs) {
    log.warn("规则执行超时: elapsedMs={}, timeoutMs={}", elapsedMs, timeoutMs);
}
```

### 2.10 规则追踪调试

```yaml
his:
  drools:
    rule-trace-enabled: true  # 开启规则追踪
```

**追踪输出示例**
```
[RuleEngine] rule "1. 职工医保起付线" fired, elapsed=2ms
[RuleEngine] rule "2. 职工医保报销比例" fired, elapsed=1ms
[RuleEngine] rulesFired=2, totalElapsed=5ms
```

### 2.11 Fact对象说明

| Fact类 | 用途 | 关键字段 |
|--------|------|---------|
| SettlementFact | 结算信息 | patientType, totalFee, deductible, ratio, finalAmount |
| PrescriptionFact | 处方信息 | drugs, hasAllergy, hasInteraction, blockResult |
| DrgFact | DRG分组 | diagnosisCode, surgeryCode, drgCode, drgName |
| QualityFact | 质控信息 | checkType, checkPassed, messages |

### 2.12 操作步骤：添加新规则

**步骤1：确定规则组**
根据规则用途选择对应的rule_group_id：
- 医保相关 → 1 (REIMBURSEMENT)
- 用药相关 → 2 (DRUG_CHECK)
- 质控相关 → 3 (QUALITY_CONTROL)
- DRG相关 → 4 (DRG_GROUPING)

**步骤2：编写DRL**
参考2.4节的模板编写规则内容

**步骤3：插入数据库**
```sql
INSERT INTO rule_definition (rule_group_id, rule_key, rule_name, rule_text, category, salience, status, version, tenant_id, create_by, create_time)
VALUES (1, 'rule.reimburse.employee.cap', '职工医保封顶线规则',
'rule \"3. 封顶线检查\"\n salience 80\n when\n    $f: com.his.fact.SettlementFact(patientType == \"employee\", totalFee > 50000)\n then\n    $f.setCapAmount(new java.math.BigDecimal(\"50000\"));\n    update($f);\nend',
'REIMBURSEMENT', 80, 'active', 1, 'T001', 'system', NOW());
```

**步骤4：验证规则**
```bash
curl -X POST http://localhost:9001/api/v1/rules/1/validate \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: T001"
```

**步骤5：发布规则**
```bash
curl -X POST http://localhost:9001/api/v1/rules/1/publish \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: T001"
```

---

## 三、规则管理 (his-rule-service :9001)

### 3.1 规则组配置

规则组是规则的分类容器，预定义组如下：

| group_code | group_name | 说明 |
|------------|------------|------|
| REIMBURSEMENT | 医保结算规则组 | 医保报销相关规则 |
| DRUG_CHECK | 合理用药规则组 | 处方审核、配伍禁忌 |
| QUALITY_CONTROL | 质控规则组 | 院感防控、质控指标 |
| DRG_GROUPING | DRG分组规则组 | DRG/DIP分组 |

### 3.2 添加新规则

**方式一：API创建**

```bash
curl -X POST http://localhost:9001/api/v1/rules \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: T001" \
  -d '{
    "ruleKey": "rule.reimburse.employee.cap",
    "ruleName": "职工医保封顶线规则",
    "ruleText": "rule \"3. 封顶线检查\"\n salience 80\n when\n    $f: com.his.fact.SettlementFact(patientType == \"employee\", totalFee > 50000)\n then\n    $f.setCapAmount(new java.math.BigDecimal(\"50000\"));\n    update($f);\nend",
    "category": "REIMBURSEMENT",
    "description": "职工医保超过5万按封顶线计算"
  }'
```

**方式二：数据库直接插入**

```sql
INSERT INTO rule_definition (rule_group_id, rule_key, rule_name, rule_text, category, salience, status, version, tenant_id, create_by, create_time)
VALUES (
  1,  -- REIMBURSEMENT组
  'rule.reimburse.employee.cap',
  '职工医保封顶线规则',
  'rule \"3. 封顶线检查\"\n salience 80\n when\n    $f: com.his.fact.SettlementFact(patientType == \"employee\", totalFee > 50000)\n then\n    $f.setCapAmount(new java.math.BigDecimal(\"50000\"));\n    update($f);\nend',
  'REIMBURSEMENT',
  80,
  'active',
  1,
  'T001',
  'system',
  NOW()
);
```

### 3.3 规则参数说明

| 参数 | 说明 | 可选值 |
|------|------|--------|
| rule_group_id | 所属规则组ID | 1=REIMBURSEMENT, 2=DRUG_CHECK, 3=QUALITY_CONTROL, 4=DRG_GROUPING |
| salience | 优先级，数字越大越先执行 | 1-1000 |
| status | 规则状态 | draft/active/inactive |
| category | 规则分类 | REIMBURSE/DRUG/QUALITY/DRG |

---

## 四、公式管理 (his-formula-service :9002)

### 4.1 公式分类

| category | 说明 | 示例公式 |
|-----------|------|---------|
| REIMBURSE | 报销计算公式 | round((totalFee - deductible) * ratio, 2) |
| DRUG | 用药检查公式 | dailyDosage > maxAllowedDosage ? maxAllowedDosage : dailyDosage |
| DRG | DRG计算公式 | (baseWeight + extraPoints) * severityFactor |
| QUALITY | 质控评分公式 | (passedCount * 100.0 / totalCount) * qualityWeight |
| SETTLEMENT | 结算相关公式 | round(totalFee - reimburseAmount, 2) |
| GENERAL | 通用公式 | a + b |

### 4.2 添加新公式

**API方式**
```bash
curl -X POST http://localhost:9002/api/v1/formulas \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: T001" \
  -d '{
    "formulaKey": "formula.reimburse.resident.staged",
    "formulaName": "居民医保阶梯式报销",
    "formulaText": "totalFee <= 500 ? round(totalFee * 0.8, 2) : totalFee <= 3000 ? round(500 * 0.8 + (totalFee - 500) * 0.65, 2) : round(500 * 0.8 + 2500 * 0.65 + (totalFee - 3000) * 0.5, 2)",
    "category": "REIMBURSE",
    "description": "居民医保三段式阶梯报销"
  }'
```

**数据库方式**
```sql
INSERT INTO aviator_formula (formula_key, formula_name, formula_text, category, status, is_validated, tenant_id, create_by, create_time)
VALUES (
  'formula.reimburse.resident.staged',
  '居民医保阶梯式报销',
  'totalFee <= 500 ? round(totalFee * 0.8, 2) : totalFee <= 3000 ? round(500 * 0.8 + (totalFee - 500) * 0.65, 2) : round(500 * 0.8 + 2500 * 0.65 + (totalFee - 3000) * 0.5, 2)',
  'REIMBURSE',
  'active',
  1,
  'T001',
  'system',
  NOW()
);
```

### 4.3 常用公式模板

**基本报销公式**
```
round((totalFee - deductible) * ratio, 2)
```

**阶梯式报销公式（两段）**
```
totalFee <= 1000 ? round(totalFee * 0.9, 2) : round(1000 * 0.9 + (totalFee - 1000) * 0.8, 2)
```

**阶梯式报销公式（三段）**
```
totalFee <= 1000 ? round(totalFee * 0.8, 2) : totalFee <= 5000 ? round(1000 * 0.8 + (totalFee - 1000) * 0.6, 2) : round(1000 * 0.8 + 4000 * 0.6 + (totalFee - 5000) * 0.4, 2)
```

**封顶线检查**
```
reimburseAmount > capAmount ? capAmount : reimburseAmount
```

**DRG权重计算**
```
(baseWeight + extraPoints) * severityFactor
```

**剂量检查**
```
dailyDosage > maxDailyDosage ? maxDailyDosage : dailyDosage
```

### 4.4 公式测试

```bash
curl -X POST http://localhost:9002/api/v1/formulas/1/test \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: T001" \
  -d '{"params":{"totalFee":15000,"deductible":1000,"ratio":0.85}}'
```

---

## 五、医保结算 (his-settlement-service :9003)

### 5.1 结算参数

| 患者类型 | patientType | 起付线 | 报销比例 |
|---------|-------------|--------|---------|
| 职工医保 | employee | 1000元 | 85% |
| 居民医保 | resident | 500元 | 65% |
| 救助对象 | aid | 0元 | 90% |

### 5.2 执行结算

```bash
curl -X POST http://localhost:9003/api/v1/settlements \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: T001" \
  -d '{
    "visitId": "V001",
    "patientId": "P001",
    "patientType": "employee",
    "totalFee": 15000
  }'
```

### 5.3 响应示例

```json
{
  "code": "0",
  "data": {
    "settlementId": "ST202605010001",
    "patientId": "P001",
    "patientType": "employee",
    "totalFee": 15000,
    "deductible": 1000,
    "ratio": 0.85,
    "reimburseAmount": 11900.00,
    "selfPayAmount": 3100.00,
    "resultLevel": "PASS"
  },
  "message": "操作成功"
}
```

### 5.4 计算公式

```
reimburseAmount = (totalFee - deductible) * ratio
selfPayAmount = totalFee - reimburseAmount
```

---

## 六、合理用药 (his-drug-service :9004)

### 6.1 药品目录配置

```sql
INSERT INTO drug_catalog (drug_code, drug_name, specification, dosage_unit, drug_type, category, reimbursement_type, limit_price, tenant_id, create_by)
VALUES
('D001', '阿莫西林胶囊', '0.25g*24粒', '粒', '抗生素', '西药', '甲类', 8.50, 'T001', 'system'),
('D002', '头孢克肟分散片', '0.1g*6片', '片', '抗生素', '西药', '乙类', 22.80, 'T001', 'system'),
('D003', '阿司匹林肠溶片', '100mg*30片', '片', '心脑血管', '西药', '甲类', 15.60, 'T001', 'system');
```

### 6.2 药物相互作用配置

```sql
INSERT INTO drug_interaction (drug_code_a, drug_name_a, drug_code_b, drug_name_b, interaction_type, severity_level, description, tenant_id, create_by)
VALUES
('D001', '阿莫西林', 'D003', '阿司匹林', '吸收影响', 'moderate', '阿莫西林与阿司匹林合用可能影响吸收', 'T001', 'system'),
('D003', '阿司匹林', 'D006', '银杏叶片', '出血风险', 'severe', '阿司匹林与银杏叶合用增加出血风险', 'T001', 'system');
```

### 6.3 处方审核

```bash
curl -X POST http://localhost:9004/api/v1/drugs/check \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: T001" \
  -d '{
    "visitId": "V001",
    "patientId": "P001",
    "drugs": [
      {"name": "阿莫西林", "dosage": 100, "unit": "mg"},
      {"name": "阿司匹林", "dosage": 50, "unit": "mg"}
    ]
  }'
```

---

## 七、DRG分组 (his-drg-service :9006)

### 7.1 DRG定义配置

```sql
INSERT INTO drg_definition (drg_code, drg_name, mdc_code, mdc_name, base_weight, base_fee, rules_text, status, version, tenant_id, create_by)
VALUES
('BJ11', '呼吸系统感染/炎症', 'MDCA', '呼吸系统疾病及功能障碍', 1.20, 8500.00, '规则组:呼吸系统感染', 'active', 1, 'T001', 'system'),
('CV21', '冠脉支架手术', 'MDC', '循环系统疾病及功能障碍', 4.20, 35000.00, '规则组:心血管手术', 'active', 1, 'T001', 'system'),
('GC19', '糖尿病', 'MDCG', '内分泌、营养及代谢疾病', 0.80, 6000.00, '规则组:内分泌', 'active', 1, 'T001', 'system');
```

### 7.2 DRG分组规则配置

```sql
-- 添加DRG分组规则到rule_definition
INSERT INTO rule_definition (rule_group_id, rule_key, rule_name, rule_text, category, salience, status, version, tenant_id, create_by, create_time)
VALUES
(4, 'rule.drg.group.j18', '呼吸系统疾病DRG规则',
 'rule "1. J18.x 分组"\n salience 100\n when\n    $f: com.his.fact.DrgFact(diagnosisCode startsWith "J18")\n then\n    $f.setDrgCode("BJ11");\n    $f.setDrgName("呼吸系统感染/炎症");\nend',
 'DRG', 100, 'active', 1, 'T001', 'system', NOW()),

(4, 'rule.drg.group.i21', '心血管疾病DRG规则',
 'rule "2. I21.x 分组"\n salience 100\n when\n    $f: com.his.fact.DrgFact(diagnosisCode startsWith "I21")\n then\n    $f.setDrgCode("CV21");\n    $f.setDrgName("冠脉支架手术");\nend',
 'DRG', 100, 'active', 1, 'T001', 'system', NOW());
```

### 7.3 DRG分组请求

```bash
curl -X POST http://localhost:9006/api/v1/drg/group \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: T001" \
  -d '{
    "primaryDiagnosis": "J18.9",
    "secondaryDiagnoses": [],
    "surgeryCode": ""
  }'
```

### 7.4 响应示例

```json
{
  "code": "0",
  "data": {
    "drgCode": "BJ11",
    "drgName": "呼吸系统感染/炎症",
    "weight": 1.20,
    "baseFee": 8500.00,
    "adjustedFee": 10200.00
  }
}
```

---

## 八、质控 (his-quality-service :9005)

### 8.1 质控规则配置

```sql
INSERT INTO rule_definition (rule_group_id, rule_key, rule_name, rule_text, category, salience, status, version, tenant_id, create_by, create_time)
VALUES
(3, 'rule.quality.infection.monitor', '院感监控规则',
 'rule "1. 院感监控"\n salience 100\n when\n    $f: com.his.fact.QualityFact(checkType == "infection_control")\n then\n    $f.setCheckPassed(true);\nend',
 'QUALITY', 100, 'active', 1, 'T001', 'system', NOW());
```

### 8.2 质控检查请求

```bash
curl -X POST http://localhost:9005/api/v1/quality/check \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: T001" \
  -d '{
    "visitId": "V001",
    "checkType": "infection_control"
  }'
```

---

## 九、就诊记录与患者

### 9.1 就诊记录配置

```sql
INSERT INTO visit_record (visit_id, patient_id, visit_type, visit_date, department, diagnosis_codes, status, tenant_id, create_by, create_time)
VALUES
('V001', 'P001', 'inpatient', '2026-05-01', '内科', 'J18.9', 'active', 'T001', 'system', NOW()),
('V002', 'P002', 'inpatient', '2026-05-01', '心内科', 'I21.9', 'active', 'T001', 'system', NOW());
```

| visit_type | 说明 |
|------------|------|
| inpatient | 住院 |
| outpatient | 门诊 |
| emergency | 急诊 |

### 9.2 过敏史配置

```sql
INSERT INTO patient_allergy (patient_id, drug_code, drug_name, allergy_type, severity_level, reaction, tenant_id, create_by)
VALUES
('P001', 'D001', '阿莫西林', 'drug', 'severe', '青霉素过敏', 'T001', 'system'),
('P002', 'D003', '阿司匹林', 'drug', 'moderate', '服用后胃部不适', 'T001', 'system');
```

---

## 十、安全配置

### 10.1 Aviator表达式安全

系统自动检测危险函数：

| 危险函数 | 示例 | 处理 |
|---------|------|------|
| system() | system(exit) | 拒绝保存 |
| runtime() | runtime(exec) | 拒绝保存 |
| exec() | exec('ls') | 拒绝保存 |
| process() | process(1) | 拒绝保存 |
| class() | class(123) | 拒绝保存 |

### 10.2 SQL注入防护

- 所有查询使用 MyBatis `#{}` 参数化
- 禁止 `${}` 字符串拼接

### 10.3 租户隔离

所有数据查询必须带 `tenant_id` 条件，通过 `X-Tenant-Id` Header传递。

---

## 十一、API快速参考

| 服务 | 端口 | 基础路径 | 主要功能 |
|------|------|---------|---------|
| his-gateway | 9000 | /api/v1 | API网关 |
| his-rule-service | 9001 | /api/v1/rules | 规则CRUD |
| his-formula-service | 9002 | /api/v1/formulas | 公式CRUD |
| his-settlement-service | 9003 | /api/v1/settlements | 结算执行 |
| his-drug-service | 9004 | /api/v1/drugs | 用药审核 |
| his-quality-service | 9005 | /api/v1/quality | 质控检查 |
| his-drg-service | 9006 | /api/v1/drg | DRG分组 |

### 通用Header

| Header | 说明 | 示例 |
|--------|------|------|
| X-Tenant-Id | 租户ID | T001 |
| Content-Type | 内容类型 | application/json |

---

## 十二、常见问题

### Q1: 规则不生效？
1. 检查规则状态是否为 `active`
2. 检查 `tenant_id` 是否匹配
3. 检查 `salience` 优先级是否正确
4. 查看服务日志确认规则是否加载

### Q2: 公式计算错误？
1. 检查公式语法是否正确
2. 检查参数名称是否与Fact字段一致
3. 使用 `/api/v1/formulas/{id}/test` 测试公式

### Q3: 结算金额不对？
1. 检查起付线和报销比例规则
2. 确认公式是否正确配置
3. 检查是否有封顶线规则

### Q4: 如何热更新规则/公式？
1. 通过API更新数据库记录
2. 重启对应服务使缓存刷新
3. 或等待缓存过期（默认30分钟）

### Q5: DRL编译失败？
1. 检查语法是否正确（大小写、分号、end关键字）
2. 确认import的类路径是否正确
3. 使用 `DroolsHelper.validateDrl()` 验证语法

### Q6: KieSession获取失败？
1. 检查KieBase是否存在
2. 确认规则是否已编译
3. 查看 `KieSessionManager` 日志

---

*最后更新: 2026-05-01 | v1.0*