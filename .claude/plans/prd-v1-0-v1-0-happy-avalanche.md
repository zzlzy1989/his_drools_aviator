# HIS 动态规则中台 V1.0 开发指导文档

> 文档版本: v1.0
> 创建日期: 2026-04-26
> 状态: 待确认

---

## 一、上下文说明

### 1.1 文档目的
根据 PRD.md 制定详细的 V1.0 版本开发指导，明确开发步骤、交付物标准和时间节点，确保开发过程可追踪、可验证。

### 1.2 项目背景
传统 HIS 系统业务规则硬编码导致规则变更周期长达 2~4 周，本项目旨在通过 Drools + Aviator 混合架构实现规则的灵活编排和热更新，将变更周期缩短至分钟级。

### 1.3 当前状态
- 项目处于**早期开发阶段**，仅完成基础骨架
- `his-common-core` 已实现 7 个核心类（ISkill, SkillContext, SkillResult, SettlementFact, ResultLevel, ErrorCode, HisEventType）
- 所有 7 个微服务仅有 Application 启动类
- `his-common-drools` 和 `his-common-aviator` 为空骨架

---

## 二、系统架构

### 2.1 高层架构

```
[前端管理后台]
    │ HTTP/HTTPS
    ▼
[API Gateway :9000] ─ JWT 鉴权 ─ 路由转发 ─ 限流熔断
    │
    ├── /api/v1/rules/*    ──→ [Rule Service :9001]      ──→ MySQL + Drools
    ├── /api/v1/formulas/* ──→ [Formula Service :9002]  ──→ MySQL + Nacos + Aviator
    ├── /api/v1/settlements/* → [Settlement Service :9003] → Drools 编排 + Aviator 计算
    ├── /api/v1/drugs/*    ──→ [Drug Service :9004]     ──→ Drools 规则 + Aviator 公式
    ├── /api/v1/quality/*  ──→ [Quality Service :9005]   ──→ Drools 规则引擎
    └── /api/v1/drg/*     ──→ [DRG Service :9006]       ──→ Drools 分组 + Aviator 权重

[Nacos :8848] ─ 配置中心 + 服务注册
[MySQL :3306]  ─ 规则/公式/结算数据持久化
```

### 2.2 模块依赖关系

```
his-gateway (无内部依赖)
his-rule-service → his-common-core + his-common-web + his-common-drools
his-formula-service → his-common-core + his-common-web + his-common-aviator
his-settlement-service → his-common-core + his-common-web + his-common-drools + his-common-aviator
his-drug-service → his-common-core + his-common-web + his-common-drools + his-common-aviator
his-quality-service → his-common-core + his-common-web + his-common-drools + his-common-aviator
his-drg-service → his-common-core + his-common-web + his-common-drools + his-common-aviator
```

### 2.3 Skill Pipeline 架构

核心设计：每个业务能力实现 `ISkill<T>` 接口，通过 `SkillPipelineExecutor` 统一调度。

```
[SettlementRequest] → [SkillContext] → [Skill Pipeline]
                                          │
                    ┌─────────────────────┼─────────────────────┐
                    ▼                     ▼                     ▼
            [InsuranceIdentitySkill] [DeductibleSkill] [ReimburseRatioSkill]
                    │                     │                     │
                    └─────────────────────┼─────────────────────┘
                                          ▼
                                [hasBlock()?] ─ NO → 继续
                                          │
                                         YES → 管道终止
                                          │
                    ┌─────────────────────┼─────────────────────┐
                    ▼                     ▼                     ▼
            [CatalogLimitSkill] [ReimburseAmountSkill] [DuplicateValidator]
```

---

## 三、开发阶段规划

### 3.1 总体时间线（8 周）

| 阶段 | 周期 | 核心交付 | 关键技术挑战 |
|------|------|----------|--------------|
| Phase 0 | W1 | 基础设施、公共模块、数据库 | Nacos + MySQL 环境搭建 |
| Phase 1 | W2-W3 | 规则管理 + 公式管理 | Drools 规则编译、Aviator 语法校验 |
| Phase 2 | W4-W5 | 医保结算 + 合理用药 | Skill 管道编排、Drools+Aviator 混合调用 |
| Phase 3 | W6 | 质控 + DRG 分组 | DRG 分组算法、权重公式计算 |
| Phase 4 | W7 | API 网关 + 联调 | JWT 鉴权、全链路测试 |
| Phase 5 | W8 | 测试 + 优化 | 性能压测、安全扫描 |

---

## 四、详细开发任务

### Phase 0: 基础设施搭建（W1）

**目标**: 公共模块可用，数据库初始化完成

#### Step 0.1: 环境准备
| 项目 | 内容 |
|------|------|
| 负责人 | 运维/架构师 |
| 交付物 | Nacos 2.x 运行中、MySQL 8.0 运行中 |
| 验收标准 | Nacos 控制台可访问 (http://localhost:8848)、MySQL 连接成功 |

#### Step 0.2: 数据库初始化
```sql
-- 核心表（按优先级）
CREATE TABLE rule_definition (...);    -- 规则定义表
CREATE TABLE aviator_formula (...);    -- 公式定义表
CREATE TABLE formula_param (...);      -- 公式参数表
CREATE TABLE settlement_result (...);  -- 结算结果表
CREATE TABLE audit_log (...);          -- 审计日志表
CREATE TABLE rule_group (...);         -- 规则分组表
CREATE TABLE formula_history (...);     -- 公式历史表
CREATE TABLE rule_history (...);       -- 规则历史表
CREATE TABLE drug_interaction (...);    -- 药品配伍表
CREATE TABLE drg_definition (...);     -- DRG 分组定义表
```
| 项目 | 内容 |
|------|------|
| 负责人 | DBA |
| 交付物 | 10 张表创建成功，DDL 脚本存档 |
| 验收标准 | 所有表创建成功，外键约束正确，索引已建立 |

#### Step 0.3: his-common-web 完善
```
src/main/java/com/his/common/web/
├── result/
│   ├── Result.java              # 统一响应封装
│   └── PageResult.java          # 分页响应
├── exception/
│   ├── HisException.java        # 基础异常
│   ├── BusinessException.java   # 业务异常
│   └── GlobalExceptionHandler.java
├── context/
│   └── TenantContext.java       # 租户上下文获取
└── util/
    └── DesensitizeUtil.java     # 敏感数据脱敏
```
| 项目 | 内容 |
|------|------|
| 负责人 | 后端开发 |
| 交付物 | 编译通过的 common-web 模块 |
| 验收标准 | `Result.success()` 和 `Result.fail()` 可正常调用，异常处理器可正确捕获 HisException |

#### Step 0.4: his-common-drools 开发
```
src/main/java/com/his/common/drools/
├── config/
│   └── DroolsConfig.java          # KieContainer 配置，规则分组加载
├── engine/
│   ├── DroolsEngine.java         # execute(fact), setGlobal(), fireAllRules()
│   └── KieSessionManager.java    # 按租户缓存会话，规则热更新
├── helper/
│   └── DrlValidator.java         # DRL 语法校验 + 安全检查
│                                # 禁止: System./, java., new java.
└── cache/
    └── RuleCache.java            # 规则编译结果缓存
```
| 项目 | 内容 |
|------|------|
| 负责人 | 后端开发 |
| 交付物 | 可加载 DRL 文件的 Drools 封装 |
| 验收标准 | 加载示例 DRL 文件并正确执行 fireAllRules() |

#### Step 0.5: his-common-aviator 开发
```
src/main/java/com/his/common/aviator/
├── config/
│   └── AviatorConfig.java         # AviatorEvaluator 初始化
│                                    # ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_BIGDECIMAL = true
├── engine/
│   ├── AviatorEngine.java         # executeFormula(formula, env)
│   └── ExpressionCache.java       # Caffeine 缓存, maxSize=5000, expire=30min
├── helper/
│   ├── AviatorHelper.java         # 公式执行工具方法
│   └── FormulaValidator.java      # 语法校验 + 变量白名单
└── function/
    └── HisAviatorFunctions.java   # 自定义函数
```
| 项目 | 内容 |
|------|------|
| 负责人 | 后端开发 |
| 交付物 | 可执行 Aviator 表达式的封装模块 |
| 验收标准 | `executeFormula("round((totalFee - deductible) * ratio, 2)", env)` 返回正确 BigDecimal 结果 |

**Phase 0 里程碑检查点**: M0 - 基础设施就绪

---

### Phase 1: 规则管理 + 公式管理（W2-W3）

**目标**: 规则和公式的完整 CRUD 和发布流程

#### Step 1.1: his-rule-service 完整实现

**模块结构**:
```
src/main/java/com/his/rule/
├── controller/
│   └── RuleDefinitionController.java    # REST API
├── service/
│   ├── RuleDefinitionService.java
│   └── impl/RuleDefinitionServiceImpl.java
├── mapper/
│   └── RuleDefinitionMapper.java
├── entity/
│   └── RuleDefinition.java
├── dto/
│   ├── RuleCreateDTO.java
│   ├── RuleUpdateDTO.java
│   ├── RuleQueryDTO.java
│   └── RuleVO.java
└── engine/
    └── RulePublisher.java               # 规则发布逻辑
```

**核心功能**:
| 功能 | API 端点 | 说明 |
|------|----------|------|
| 规则列表 | GET `/api/v1/rules` | 分页查询，按 category/status/keyword 筛选 |
| 规则详情 | GET `/api/v1/rules/{id}` | 获取单条规则 |
| 创建规则 | POST `/api/v1/rules` | 创建草稿状态规则 |
| 更新规则 | PUT `/api/v1/rules/{id}` | 仅允许草稿状态更新 |
| 删除规则 | DELETE `/api/v1/rules/{id}` | 软删除 |
| 校验规则 | POST `/api/v1/rules/{id}/validate` | DRL 语法校验 |
| 发布规则 | POST `/api/v1/rules/{id}/publish` | 发布生效，版本递增 |
| 停用规则 | POST `/api/v1/rules/{id}/deactivate` | 状态变更为 inactive |
| 版本历史 | GET `/api/v1/rules/{id}/versions` | 查看历史版本 |
| 回滚规则 | POST `/api/v1/rules/{id}/rollback/{version}` | 回滚到指定版本 |

**规则状态机**:
```
draft → validated → active → inactive → deleted
         (校验)     (发布)   (停用)
```

| 项目 | 内容 |
|------|------|
| 负责人 | 后端开发 |
| 交付物 | 完整的规则管理微服务 |
| 验收标准 | 规则 CRUD 功能正常，发布流程完整，DRL 校验通过 |

#### Step 1.2: his-formula-service 完整实现

**模块结构**:
```
src/main/java/com/his/formula/
├── controller/
│   └── FormulaController.java
├── service/
│   ├── FormulaService.java
│   └── impl/FormulaServiceImpl.java
├── mapper/
│   ├── AviatorFormulaMapper.java
│   └── FormulaParamMapper.java
├── entity/
│   ├── AviatorFormula.java
│   └── FormulaParam.java
├── dto/
│   ├── FormulaCreateDTO.java
│   ├── FormulaUpdateDTO.java
│   ├── FormulaQueryDTO.java
│   ├── FormulaVO.java
│   ├── FormulaTestDTO.java
│   └── FormulaTestResult.java
├── config/
│   └── NacosConfig.java
└── listener/
    └── NacosFormulaSyncListener.java
```

**核心功能**:
| 功能 | API 端点 | 说明 |
|------|----------|------|
| 公式列表 | GET `/api/v1/formulas` | 分页查询 |
| 创建公式 | POST `/api/v1/formulas` | 创建草稿 |
| 更新公式 | PUT `/api/v1/formulas/{id}` | 更新草稿 |
| 语法校验 | POST `/api/v1/formulas/{id}/validate` | Aviator 编译校验 |
| 发布公式 | POST `/api/v1/formulas/{id}/publish` | 发布并同步 Nacos |
| 测试公式 | POST `/api/v1/formulas/test` | 执行测试 |
| 参数管理 | GET/POST/PUT/DELETE `/api/v1/formulas/{id}/params` | 参数 CRUD |

**Nacos 同步机制**:
- 发布时写入 Nacos 配置中心
- 通过 `@RefreshScope` 实现自动刷新
- 变更时调用 `ExpressionCache.invalidate()` 刷新缓存

| 项目 | 内容 |
|------|------|
| 负责人 | 后端开发 |
| 交付物 | 完整的公式管理微服务 |
| 验收标准 | 公式 CRUD 正常，语法校验正确，Nacos 同步生效 |

**Phase 1 里程碑检查点**: M1 - 规则公式就绪

---

### Phase 2: 医保结算 + 合理用药（W4-W5）

**目标**: 核心业务链路打通

#### Step 2.1: his-settlement-service 实现

**Skill 实现**（核心混合架构）:
```java
// InsuranceIdentitySkill.java - 身份校验
@Skill(eventType = "EVENT_SETTLEMENT_EXECUTE", order = 1)
public class InsuranceIdentitySkill implements ISkill<SettlementFact> {
    public void execute(SkillContext<SettlementFact> context) {
        if (context.hasBlock()) return;
        SettlementFact fact = context.getPayload();
        if (fact.getPatientType() == null) {
            context.addResult(new SkillResult(ResultLevel.BLOCK,
                "InsuranceIdentitySkill", "患者身份信息缺失"));
        }
    }
}

// DeductibleSkill.java - 起付线计算
@Skill(eventType = "EVENT_SETTLEMENT_EXECUTE", order = 2)
public class DeductibleSkill implements ISkill<SettlementFact> {
    public void execute(SkillContext<SettlementFact> context) {
        if (context.hasBlock()) return;
        // 根据 patientType + hospitalLevel 确定起付线
    }
}

// ReimburseRatioSkill.java - 报销比例
@Skill(eventType = "EVENT_SETTLEMENT_EXECUTE", order = 3)
public class ReimburseRatioSkill implements ISkill<SettlementFact> {
    public void execute(SkillContext<SettlementFact> context) {
        if (context.hasBlock()) return;
        // 根据 patientType 确定报销比例
    }
}

// ReimburseAmountSkill.java - Aviator 计算报销金额（关键混合点）
@Skill(eventType = "EVENT_SETTLEMENT_EXECUTE", order = 4)
public class ReimburseAmountSkill implements ISkill<SettlementFact> {
    @Autowired private AviatorHelper aviatorHelper;

    public void execute(SkillContext<SettlementFact> context) {
        if (context.hasBlock()) return;
        SettlementFact fact = context.getPayload();

        String formula = "round((totalFee - deductible) * ratio, 2)";
        Map<String, Object> env = Map.of(
            "totalFee", fact.getTotalFee(),
            "deductible", fact.getDeductible(),
            "ratio", fact.getRatio()
        );

        BigDecimal amount = aviatorHelper.executeFormula(formula, env);
        fact.setFinalAmount(amount);
    }
}
```

**Skill Pipeline 执行器**:
```java
@Service
public class SkillPipelineExecutor {
    @Autowired private List<ISkill> skills;

    public SkillContext execute(String eventType, Object payload, String tenantId) {
        SkillContext context = new SkillContext();
        context.setTenantId(tenantId);
        context.setEventType(eventType);
        context.setPayload(payload);

        List<ISkill> matchedSkills = skills.stream()
            .filter(s -> s.supportEvent().equals(eventType))
            .sorted(Comparator.comparingInt(ISkill::getOrder))
            .toList();

        for (ISkill skill : matchedSkills) {
            try {
                skill.execute(context);
                if (context.hasBlock()) break;
            } catch (Exception e) {
                log.error("Skill execution error: {}", e.getMessage());
                context.addResult(new SkillResult(ResultLevel.WARN,
                    skill.getClass().getSimpleName(), e.getMessage()));
            }
        }
        return context;
    }
}
```

**API 端点**:
| 功能 | API 端点 | 说明 |
|------|----------|------|
| 执行结算 | POST `/api/v1/settlements` | 构建 SettlementFact，执行 Skill Pipeline |
| 结算详情 | GET `/api/v1/settlements/{settlementId}` | 获取结算结果 |
| 结算历史 | GET `/api/v1/settlements` | 按 patientId/date 查询 |

| 项目 | 内容 |
|------|------|
| 负责人 | 后端开发 |
| 交付物 | 可执行的医保结算服务 |
| 验收标准 | 结算请求返回正确报销金额，Skill Pipeline 正确执行 |

#### Step 2.2: his-drug-service 实现

**Skill 实现**:
```java
// DrugCompatibilitySkill.java - 配伍禁忌
@Skill(eventType = "EVENT_DRUG_PRESCRIBE", order = 1)
public class DrugCompatibilitySkill implements ISkill<PrescriptionFact> {
    public void execute(SkillContext<PrescriptionFact> context) {
        if (context.hasBlock()) return;
        // 检查配伍禁忌
    }
}

// DrugDosageLimitSkill.java - 极量检查（调用 Aviator）
@Skill(eventType = "EVENT_DRUG_PRESCRIBE", order = 2)
public class DrugDosageLimitSkill implements ISkill<PrescriptionFact> {
    @Autowired private AviatorHelper aviatorHelper;

    public void execute(SkillContext<PrescriptionFact> context) {
        if (context.hasBlock()) return;
        // 调用 Aviator 检查极量
    }
}

// DrugAllergySkill.java - 过敏史检查
@Skill(eventType = "EVENT_DRUG_PRESCRIBE", order = 3)
public class DrugAllergySkill implements ISkill<PrescriptionFact> {
    public void execute(SkillContext<PrescriptionFact> context) {
        if (context.hasBlock()) return;
        // 检查过敏史
    }
}
```

**API 端点**:
| 功能 | API 端点 | 说明 |
|------|----------|------|
| 处方审核 | POST `/api/v1/drugs/check` | 返回 PASS/WARN/BLOCK 结果 |

| 项目 | 内容 |
|------|------|
| 负责人 | 后端开发 |
| 交付物 | 可执行的合理用药审核服务 |
| 验收标准 | 含禁忌处方返回 BLOCK，配伍警告返回 WARN |

**Phase 2 里程碑检查点**: M2 - 结算用药就绪

---

### Phase 3: 质控 + DRG 分组（W6）

#### Step 3.1: his-quality-service 实现
- InfectionControlSkill - 院感规则检查
- QualityRuleSkill - 抗菌药物使用率等

#### Step 3.2: his-drg-service 实现
- DrgGroupingSkill - DRG 分组（核心算法）
- DrgWeightCalcSkill - 权重计算（调用 Aviator）
- DrgStandardScoreSkill - 标准分值计算

| 项目 | 内容 |
|------|------|
| 负责人 | 后端开发 |
| 交付物 | 质控和 DRG 分组服务 |
| 验收标准 | DRG 分组正确，权重计算准确 |

**Phase 3 里程碑检查点**: M3 - 全功能就绪

---

### Phase 4: API 网关 + 联调（W7）

#### Step 4.1: his-gateway 完善
- JWT Token 校验
- 权限拦截（@PreAuthorize）
- 限流熔断（Sentinel）
- 请求日志记录

#### Step 4.2: 全链路联调
- 服务间调用（OpenFeign）
- 分布式事务
- 链路追踪（traceId）

| 项目 | 内容 |
|------|------|
| 负责人 | 后端开发 |
| 交付物 | 可运行的完整系统 |
| 验收标准 | 所有 API 可调用，网关路由正常 |

**Phase 4 里程碑检查点**: M4 - 集成测试通过

---

### Phase 5: 测试 + 优化（W8）

#### Step 5.1: 单元测试
| 层级 | 覆盖率目标 |
|------|------------|
| 核心规则（Drools） | 100% |
| 公式（Aviator） | 100% |
| Service 层 | ≥ 90% |
| Controller 层 | ≥ 70% |

#### Step 5.2: 集成测试
- 全链路流程测试
- Skill Pipeline 测试
- Nacos 配置刷新测试

#### Step 5.3: 性能压测
| 指标 | 目标值 |
|------|--------|
| 结算流程 P99 | < 50ms |
| 并发支持 | ≥ 500 TPS |
| 缓存命中率 | ≥ 95% |

**Phase 5 里程碑检查点**: M5 - 发布就绪

---

## 五、核心接口定义

### 5.1 统一响应格式
```json
{
  "code": "0",
  "data": {...},
  "message": "操作成功",
  "timestamp": 1714108800000
}
```

### 5.2 Key API 规格

| 服务 | 方法 | 路径 | 说明 |
|------|------|------|------|
| Rule | GET | `/api/v1/rules` | 分页查询规则 |
| Rule | POST | `/api/v1/rules` | 创建规则 |
| Rule | POST | `/api/v1/rules/{id}/publish` | 发布规则 |
| Formula | GET | `/api/v1/formulas` | 分页查询公式 |
| Formula | POST | `/api/v1/formulas/{id}/validate` | 语法校验 |
| Formula | POST | `/api/v1/formulas/{id}/publish` | 发布 + Nacos 同步 |
| Settlement | POST | `/api/v1/settlements` | 执行结算 |
| Drug | POST | `/api/v1/drugs/check` | 处方审核 |
| DRG | POST | `/api/v1/drg/group` | DRG 分组 |

---

## 六、开发规范

### 6.1 强制约束

| 约束 | 说明 |
|------|------|
| **BigDecimal** | 所有金额/比例字段必须使用 `BigDecimal`，禁止 `double`/`float` |
| **统一响应** | 所有 Controller 返回 `Result<T>` |
| **日志记录** | 使用 `@Slf4j`，使用 `{}` 占位符，禁止 `System.out.println` |
| **租户隔离** | 所有查询必须过滤 `tenant_id` |
| **审计日志** | 所有写操作记录 `audit_log` |
| **表达式缓存** | Aviator 表达式必须通过 `ExpressionCache` 缓存 |
| **DRL 安全** | 规则发布前必须通过 `DrlValidator` 校验 |
| **异常处理** | Skill 执行时捕获异常，添加 WARN 结果，不向上抛出 |

### 6.2 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 规则 Key | `rule.{category}.{name}` | `rule.reimburse.resident_deductible` |
| 公式 Key | `formula.{category}.{name}` | `formula.reimburse.calculate` |
| Skill 类 | `{Business}Skill` | `DeductibleSkill` |
| 状态 | 小写 | `draft`, `validated`, `active`, `inactive` |

---

## 七、关键风险与应对

| 编号 | 风险 | 影响 | 概率 | 应对措施 |
|------|------|------|:----:|---------|
| TR-01 | Drools 规则编译失败 | 高 | 中 | 发布前校验，失败拒绝发布 |
| TR-02 | Aviator 公式语法错误 | 高 | 中 | 保存前强制校验，执行时捕获异常降级 |
| TR-03 | Nacos 配置中心不可用 | 高 | 低 | 本地缓存兜底 |
| TR-04 | 多租户数据泄露 | 高 | 低 | 所有查询强制过滤 tenant_id |
| TR-05 | 金额计算精度丢失 | 高 | 低 | 强制 BigDecimal |

---

## 八、里程碑总览

| 里程碑 | 日期 | 交付物 | 验收人 |
|--------|------|--------|--------|
| M0: 基础设施就绪 | W1 结束 | 公共模块编译通过，数据库初始化 | 架构师 |
| M1: 规则公式就绪 | W3 结束 | 规则/公式 CRUD + 发布流程可执行 | 产品经理 |
| M2: 结算用药就绪 | W5 结束 | 结算流程可执行，用药审核可拦截 | 产品经理 |
| M3: 全功能就绪 | W6 结束 | 所有模块功能开发完成 | 产品经理 |
| M4: 集成测试通过 | W7 结束 | 全链路联调通过，网关路由正常 | QA |
| M5: 发布就绪 | W8 结束 | 测试报告 + 性能报告 + 部署文档 | 项目经理 |

---

## 九、关键文件路径

| 文件 | 路径 | 说明 |
|------|------|------|
| ISkill 接口 | `his-common/his-common-core/.../ISkill.java` | Skill 契约接口 |
| SkillContext | `his-common/his-common-core/.../SkillContext.java` | 执行上下文 |
| SkillResult | `his-common/his-common-core/.../SkillResult.java` | 结果对象 |
| SettlementFact | `his-common/his-common-core/.../SettlementFact.java` | 结算事实对象 |
| PRD 文档 | `PRD.md` | 完整需求定义 |
| 架构文档 | `drools_aviator.md` | Drools+Aviator 混合架构设计 |
| 规则文件 | `his-rule-service/src/main/resources/rules/` | DRL 规则文件目录 |

---

## 十、验收标准摘要

### 功能验收
| 验收项 | 通过标准 |
|--------|---------|
| 规则 CRUD | 增删改查功能正常，状态流转正确 |
| 规则发布 | 发布后规则立即生效，无需重启 |
| 公式语法校验 | 正确识别语法错误并提示 |
| 公式计算 | 计算结果与预期一致，精度正确 |
| 医保结算 | 报销金额计算正确，结果保存 |
| 合理用药审核 | 正确返回 WARN/BLOCK 干预结果 |
| 多租户隔离 | 只能看到本租户数据 |

### 性能验收
| 验收项 | 通过标准 |
|--------|---------|
| 结算耗时 | P99 < 50ms |
| 并发支持 | 500 用户并发，错误率 < 0.1% |
| 缓存命中率 | ≥ 95% |
| 配置刷新 | 2 秒内生效 |

---

**文档状态**: 待确认
**下一步**: 请确认开发计划是否符合预期
