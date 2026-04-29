# HIS 动态规则中台 — V1.0 开发指导文档

> 文档版本: v1.0  
> 创建日期: 2026-04-26  
> 文档状态: 可执行  
> 依据文档: [PRD.md](PRD.md) v1.0  
> 适用项目: his_drools_aviator

---

## 0. 项目上下文

### 0.1 文档目的
根据 PRD.md 制定详细的 V1.0 版本开发指导，明确开发步骤、交付物标准和时间节点，确保开发过程可追踪、可验证。

### 0.2 项目背景
传统 HIS 系统业务规则硬编码导致规则变更周期长达 2~4 周，本项目旨在通过 Drools + Aviator 混合架构实现规则的灵活编排和热更新，将变更周期缩短至分钟级。

### 0.3 当前状态
- 项目处于**早期开发阶段**，仅完成基础骨架
- `his-common-core` 已实现 7 个核心类（ISkill, SkillContext, SkillResult, SettlementFact, ResultLevel, ErrorCode, HisEventType）
- 所有 7 个微服务仅有 Application 启动类
- `his-common-drools` 和 `his-common-aviator` 为空骨架

---

## 1. 版本范围

### 1.1 V1.0 包含功能（P0 + P1）

| 模块 | P0 功能数 | P1 功能数 | 合计 |
|------|:---------:|:---------:|:----:|
| 规则管理 | 6 | 4 | 10 |
| 公式管理 | 6 | 2 | 8 |
| 医保结算 | 7 | 3 | 10 |
| 合理用药 | 5 | 3 | 8 |
| 质控 | 3 | 2 | 5 |
| DRG 分组 | 3 | 2 | 5 |
| API 网关 | 5 | 1 | 6 |
| 公共功能 | 1 | 5 | 6 |
| **合计** | **36** | **22** | **58** |

### 1.2 V1.0 不包含功能（V2.0 规划）

- 规则可视化编排（拖拽式规则流设计）
- 规则测试沙箱
- 规则执行监控大屏
- 规则市场

---

## 2. 系统架构设计

### 2.1 高层架构图

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

### 2.2 技术选型（已确定）

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 基础框架 | Spring Boot | 3.2.5 | 微服务基础 |
| 微服务生态 | Spring Cloud | 2023.0.1 | 服务治理 |
| 服务注册/配置 | Spring Cloud Alibaba | 2023.0.1.0 | Nacos 集成 |
| 规则引擎 | Drools | 8.44.0.Final | 规则编排 |
| 表达式引擎 | Aviator | 5.4.3 | 公式计算 |
| 本地缓存 | Caffeine | 3.1.8 | 表达式编译缓存 |
| ORM | MyBatis-Plus | 3.5.6 | 数据库操作 |
| 数据库 | MySQL | 8.0+ | 数据持久化 |
| API 文档 | SpringDoc | 2.5.0 | OpenAPI 3.0 |
| 工具类 | Hutool | 5.8.26 | 通用工具 |
| 对象映射 | MapStruct | 1.5.5 | DTO/Entity 转换 |
| 构建工具 | Maven | 3.8+ | 项目构建 |
| 运行环境 | JDK | 21 | Java 运行 |

### 2.3 模块依赖关系

```
his-gateway (无内部依赖)
his-rule-service → his-common-core + his-common-web + his-common-drools
his-formula-service → his-common-core + his-common-web + his-common-aviator
his-settlement-service → his-common-core + his-common-web + his-common-drools + his-common-aviator
his-drug-service → his-common-core + his-common-web + his-common-drools + his-common-aviator
his-quality-service → his-common-core + his-common-web + his-common-drools + his-common-aviator
his-drg-service → his-common-core + his-common-web + his-common-drools + his-common-aviator
```

### 2.4 Skill Pipeline 架构

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

### 2.5 规则状态机

```
draft → validated → active → inactive → deleted
         (校验)     (发布)   (停用)
```

---

## 3. 数据库设计

### 3.1 建表脚本执行顺序

```
Phase 1: 基础表
  1. rule_definition.sql          -- 规则定义表
  2. aviator_formula.sql          -- 公式定义表
  3. formula_param.sql            -- 公式参数表

Phase 2: 业务表
  4. settlement_result.sql        -- 结算结果表
  5. settlement_detail.sql        -- 结算明细表
  6. prescription_record.sql      -- 处方记录表
  7. quality_record.sql           -- 质控记录表
  8. drg_group.sql                -- DRG 分组表

Phase 3: 日志表
  9. audit_log.sql                -- 审计日志表
  10. operation_log.sql           -- 操作日志表
```

### 3.2 核心表 DDL

#### 3.2.1 rule_definition

```sql
CREATE TABLE `rule_definition` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
  `rule_key` VARCHAR(128) NOT NULL COMMENT '规则唯一标识',
  `rule_text` TEXT NOT NULL COMMENT 'DRL规则内容',
  `category` VARCHAR(32) NOT NULL COMMENT '分类: reimbursement/drug/quality/drg',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/validated/active/inactive',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '规则描述',
  `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_rule_key` (`tenant_id`, `rule_key`),
  KEY `idx_tenant_category` (`tenant_id`, `category`),
  KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则定义表';
```

#### 3.2.2 aviator_formula

```sql
CREATE TABLE `aviator_formula` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
  `formula_key` VARCHAR(128) NOT NULL COMMENT '公式唯一标识',
  `formula_text` VARCHAR(1000) NOT NULL COMMENT '表达式文本',
  `category` VARCHAR(32) NOT NULL COMMENT '分类',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft' COMMENT '状态',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '公式描述',
  `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_formula_key` (`tenant_id`, `formula_key`),
  KEY `idx_tenant_category` (`tenant_id`, `category`),
  KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公式定义表';
```

#### 3.2.3 formula_param

```sql
CREATE TABLE `formula_param` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `formula_id` BIGINT NOT NULL COMMENT '所属公式ID',
  `param_name` VARCHAR(64) NOT NULL COMMENT '参数名',
  `param_type` VARCHAR(32) NOT NULL COMMENT '参数类型: BigDecimal/String/Integer',
  `default_value` VARCHAR(256) DEFAULT NULL COMMENT '默认值',
  `description` VARCHAR(256) DEFAULT NULL COMMENT '参数描述',
  PRIMARY KEY (`id`),
  KEY `idx_formula_id` (`formula_id`),
  CONSTRAINT `fk_param_formula` FOREIGN KEY (`formula_id`) REFERENCES `aviator_formula` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公式参数表';
```

#### 3.2.4 settlement_result

```sql
CREATE TABLE `settlement_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
  `settlement_id` VARCHAR(64) NOT NULL COMMENT '结算流水号',
  `visit_id` VARCHAR(64) NOT NULL COMMENT '就诊ID',
  `patient_id` VARCHAR(64) NOT NULL COMMENT '患者ID',
  `patient_type` VARCHAR(32) NOT NULL COMMENT '患者类型: resident/employee/...',
  `total_fee` DECIMAL(12,2) NOT NULL COMMENT '总费用',
  `deductible` DECIMAL(12,2) DEFAULT NULL COMMENT '起付线',
  `reimburse_amount` DECIMAL(12,2) DEFAULT NULL COMMENT '报销金额',
  `self_pay_amount` DECIMAL(12,2) DEFAULT NULL COMMENT '自付金额',
  `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/success/failed',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_id` (`settlement_id`),
  UNIQUE KEY `uk_tenant_visit` (`tenant_id`, `visit_id`),
  KEY `idx_tenant_patient` (`tenant_id`, `patient_id`),
  KEY `idx_tenant_created` (`tenant_id`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算结果表';
```

#### 3.2.5 audit_log

```sql
CREATE TABLE `audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
  `action` VARCHAR(32) NOT NULL COMMENT '操作类型: CREATE/UPDATE/PUBLISH/DELETE/EXECUTE',
  `target_type` VARCHAR(32) NOT NULL COMMENT '目标类型: RULE/FORMULA/SETTLEMENT',
  `target_id` VARCHAR(64) NOT NULL COMMENT '目标ID',
  `operator` VARCHAR(64) NOT NULL COMMENT '操作人',
  `detail` TEXT DEFAULT NULL COMMENT '操作详情(JSON)',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_target` (`tenant_id`, `target_type`, `target_id`),
  KEY `idx_tenant_created` (`tenant_id`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
```

---

## 4. 接口定义

### 4.1 统一响应格式

```java
@Data
@Builder
public class Result<T> {
    private String code;
    private T data;
    private String message;
    private Long timestamp;

    public static <T> Result<T> success(T data) { ... }
    public static <T> Result<T> fail(String code, String message) { ... }
}

@Data
public class PageResult<T> {
    private List<T> list;
    private Long total;
    private Integer page;
    private Integer pageSize;
}
```

### 4.2 规则管理接口（his-rule-service）

| 方法 | 路径 | 说明 | 请求体 | 响应体 |
|------|------|------|--------|--------|
| GET | `/api/v1/rules` | 分页查询规则 | Query: page, pageSize, category, status, keyword | `Result<PageResult<RuleVO>>` |
| GET | `/api/v1/rules/{id}` | 规则详情 | — | `Result<RuleVO>` |
| POST | `/api/v1/rules` | 创建规则 | `RuleCreateDTO` | `Result<Void>` |
| PUT | `/api/v1/rules/{id}` | 更新规则 | `RuleUpdateDTO` | `Result<Void>` |
| DELETE | `/api/v1/rules/{id}` | 删除规则 | — | `Result<Void>` |
| POST | `/api/v1/rules/{id}/validate` | 校验规则 | — | `Result<RuleValidateResult>` |
| POST | `/api/v1/rules/{id}/publish` | 发布规则 | — | `Result<Void>` |
| POST | `/api/v1/rules/{id}/deactivate` | 停用规则 | — | `Result<Void>` |
| GET | `/api/v1/rules/{id}/versions` | 历史版本 | Query: page, pageSize | `Result<PageResult<RuleVersionVO>>` |
| POST | `/api/v1/rules/{id}/rollback/{version}` | 版本回滚 | — | `Result<Void>` |

### 4.3 公式管理接口（his-formula-service）

| 方法 | 路径 | 说明 | 请求体 | 响应体 |
|------|------|------|--------|--------|
| GET | `/api/v1/formulas` | 分页查询公式 | Query: page, pageSize, category, status | `Result<PageResult<FormulaVO>>` |
| GET | `/api/v1/formulas/{id}` | 公式详情 | — | `Result<FormulaVO>` |
| POST | `/api/v1/formulas` | 创建公式 | `FormulaCreateDTO` | `Result<Void>` |
| PUT | `/api/v1/formulas/{id}` | 更新公式 | `FormulaUpdateDTO` | `Result<Void>` |
| DELETE | `/api/v1/formulas/{id}` | 删除公式 | — | `Result<Void>` |
| POST | `/api/v1/formulas/{id}/validate` | 语法校验 | — | `Result<FormulaValidateResult>` |
| POST | `/api/v1/formulas/{id}/publish` | 发布公式 | — | `Result<Void>` |
| POST | `/api/v1/formulas/{id}/test` | 公式测试 | `FormulaTestDTO` | `Result<FormulaTestResult>` |
| GET | `/api/v1/formulas/{id}/params` | 参数列表 | — | `Result<List<FormulaParamVO>>` |
| POST | `/api/v1/formulas/{id}/params` | 添加参数 | `FormulaParamDTO` | `Result<Void>` |

### 4.4 医保结算接口（his-settlement-service）

| 方法 | 路径 | 说明 | 请求体 | 响应体 |
|------|------|------|--------|--------|
| POST | `/api/v1/settlements` | 执行结算 | `SettlementRequestDTO` | `Result<SettlementResultVO>` |
| GET | `/api/v1/settlements/{settlementId}` | 结算详情 | — | `Result<SettlementResultVO>` |
| GET | `/api/v1/settlements` | 结算历史 | Query: page, pageSize, patientId, startDate, endDate | `Result<PageResult<SettlementResultVO>>` |

### 4.5 合理用药接口（his-drug-service）

| 方法 | 路径 | 说明 | 请求体 | 响应体 |
|------|------|------|--------|--------|
| POST | `/api/v1/drugs/check` | 处方审核 | `PrescriptionCheckDTO` | `Result<DrugCheckResultVO>` |

### 4.6 质控接口（his-quality-service）

| 方法 | 路径 | 说明 | 请求体 | 响应体 |
|------|------|------|--------|--------|
| POST | `/api/v1/quality/check` | 质控检查 | `QualityCheckDTO` | `Result<QualityCheckResultVO>` |

### 4.7 DRG 分组接口（his-drg-service）

| 方法 | 路径 | 说明 | 请求体 | 响应体 |
|------|------|------|--------|--------|
| POST | `/api/v1/drg/group` | DRG 分组 | `DrgGroupDTO` | `Result<DrgGroupResultVO>` |
| GET | `/api/v1/drg/{groupCode}` | 分组详情 | — | `Result<DrgGroupResultVO>` |

### 4.8 DTO 定义

#### RuleCreateDTO

```java
@Data
public class RuleCreateDTO {
    @NotBlank(message = "规则Key不能为空")
    @Pattern(regexp = "^rule\\.[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$", message = "格式: rule.{module}.{name}")
    private String ruleKey;

    @NotBlank(message = "DRL内容不能为空")
    @Size(max = 50000, message = "DRL内容不超过50000字符")
    private String ruleText;

    @NotNull(message = "分类不能为空")
    private RuleCategory category;

    private String description;
}
```

#### FormulaCreateDTO

```java
@Data
public class FormulaCreateDTO {
    @NotBlank(message = "公式Key不能为空")
    @Pattern(regexp = "^formula\\.[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$", message = "格式: formula.{category}.{name}")
    private String formulaKey;

    @NotBlank(message = "公式表达式不能为空")
    @Size(max = 1000, message = "公式表达式不超过1000字符")
    private String formulaText;

    @NotNull(message = "分类不能为空")
    private FormulaCategory category;

    private String description;
}
```

#### SettlementRequestDTO

```java
@Data
public class SettlementRequestDTO {
    @NotBlank(message = "就诊ID不能为空")
    private String visitId;

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    @NotBlank(message = "患者类型不能为空")
    private String patientType;

    @NotNull(message = "总费用不能为空")
    private BigDecimal totalFee;

    private String insuranceType;
    private String hospitalLevel;
    private List<SettlementDetailDTO> details;
}
```

---

## 5. 开发规范

### 5.1 包结构规范

```
com.his.{module}
├── controller/          # REST 控制器
├── service/             # 业务服务
│   ├── impl/            # 服务实现
│   └── {Module}Service.java
├── mapper/              # MyBatis Mapper 接口
├── entity/              # 数据库实体类
├── dto/                 # 数据传输对象
│   ├── CreateDTO.java
│   ├── UpdateDTO.java
│   ├── QueryDTO.java
│   └── VO.java
├── config/              # 配置类
├── constant/            # 常量类
├── exception/           # 自定义异常
├── listener/            # 监听器（Nacos 配置变更等）
└── skill/               # Skill 实现（用药/质控服务）
```

### 5.2 强制约束

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

### 5.3 编码规范

#### 5.3.1 类命名

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| Controller | `{Resource}Controller` | `RuleDefinitionController` |
| Service 接口 | `{Resource}Service` | `RuleDefinitionService` |
| Service 实现 | `{Resource}ServiceImpl` | `RuleDefinitionServiceImpl` |
| Mapper | `{Resource}Mapper` | `RuleDefinitionMapper` |
| Entity | `{Resource}` | `RuleDefinition` |
| DTO | `{Resource}CreateDTO` | `RuleCreateDTO` |
| VO | `{Resource}VO` | `RuleVO` |
| Config | `{Topic}Config` | `DroolsConfig` |
| Exception | `{Type}Exception` | `FormulaException` |
| Skill | `{Business}Skill` | `RationalDrugUseSkill` |

#### 5.3.2 强制规则

1. 金额计算必须使用 `BigDecimal`，禁止使用 `double`/`float`
2. 所有 Controller 方法返回 `Result<T>`
3. 所有 Service 方法必须记录日志（`@Slf4j`）
4. DRL 规则中禁止使用 `System.out.println`，使用 SLF4J
5. Aviator 表达式必须缓存编译结果
6. 所有查询必须过滤 `tenant_id`
7. 所有写操作必须记录审计日志
8. 单个 Java 文件不超过 500 行

### 5.4 Git 提交规范

遵循 Conventional Commits 规范：

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

| type | 说明 | 示例 |
|------|------|------|
| feat | 新功能 | `feat(settlement): 添加医保结算接口` |
| fix | Bug 修复 | `fix(rule): 修复规则状态流转异常` |
| docs | 文档 | `docs: 更新 API 接口文档` |
| style | 代码格式 | `style: 格式化 RuleService 代码` |
| refactor | 重构 | `refactor(formula): 重构公式校验逻辑` |
| test | 测试 | `test(settlement): 添加结算单元测试` |
| chore | 构建/工具 | `chore: 升级 Drools 到 8.44.0` |

---

## 6. 开发阶段规划

### 6.1 阶段总览

| 阶段 | 名称 | 周期 | 核心交付 | 关键技术挑战 |
|------|------|------|---------|--------------|
| Phase 0 | 基础设施 | W1 | 数据库、Nacos、公共模块 | Nacos + MySQL 环境搭建 |
| Phase 1 | 规则与公式 | W2-W3 | 规则管理、公式管理 | Drools 规则编译、Aviator 语法校验 |
| Phase 2 | 结算与用药 | W4-W5 | 医保结算、合理用药 | Skill 管道编排、Drools+Aviator 混合调用 |
| Phase 3 | 质控与 DRG | W6 | 质控、DRG 分组 | DRG 分组算法、权重公式计算 |
| Phase 4 | 网关与集成 | W7 | API 网关、联调测试 | JWT 鉴权、全链路测试 |
| Phase 5 | 测试与优化 | W8 | 性能测试、安全测试、文档 | 性能压测、安全扫描 |

### 6.2 Phase 0: 基础设施（W1）

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

**DrlValidator 安全规则**:
- 禁止 `System.out` / `System.err`
- 禁止 `java.lang.Runtime` / `java.lang.ProcessBuilder`
- 禁止 `new java.` 实例化
- 仅允许 `import` 预定义包（`com.his.common.*`）

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

**Aviator 关键配置**:
```java
AviatorEvaluatorInstance instance = AviatorEvaluator.getInstance();
instance.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_BIGDECIMAL, true);
instance.setOption(Options.ALWAYS_PARSE_INTEGRAL_NUMBER_INTO_BIGDECIMAL, true);
```

| 项目 | 内容 |
|------|------|
| 负责人 | 后端开发 |
| 交付物 | 可执行 Aviator 表达式的封装模块 |
| 验收标准 | `executeFormula("round((totalFee - deductible) * ratio, 2)", env)` 返回正确 BigDecimal 结果 |

**Phase 0 里程碑检查点**: M0 - 基础设施就绪

---

### 6.3 Phase 1: 规则管理 + 公式管理（W2-W3）

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

### 6.4 Phase 2: 医保结算 + 合理用药（W4-W5）

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

### 6.5 Phase 3: 质控 + DRG 分组（W6）

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

### 6.6 Phase 4: API 网关 + 联调（W7）

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

### 6.7 Phase 5: 测试 + 优化（W8）

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

## 7. 测试策略

### 7.1 测试层级

| 层级 | 工具 | 覆盖率要求 | 执行时机 |
|------|------|-----------|---------|
| 单元测试 | JUnit 5 + Mockito | 核心规则 ≥ 90%，工具类 ≥ 80% | 每次提交 |
| 集成测试 | MockMvc + TestContainers | Controller ≥ 70% | 每次提交 |
| DRL 规则测试 | KieSession 注入 | 所有规则 100% 覆盖 | 规则发布前 |
| Aviator 公式测试 | 表达式编译缓存测试 | 所有公式 100% 覆盖 | 公式发布前 |
| 端到端测试 | Postman / JMeter | 核心流程 100% 覆盖 | 版本发布前 |

### 7.2 测试模板

#### 单元测试模板

```java
@ExtendWith(MockitoExtension.class)
class RuleDefinitionServiceTest {

    @Mock
    private RuleDefinitionMapper ruleMapper;

    @InjectMocks
    private RuleDefinitionServiceImpl ruleService;

    @Test
    @DisplayName("创建规则 - 成功")
    void createRule_success() {
        // Given
        RuleCreateDTO dto = new RuleCreateDTO();
        dto.setRuleKey("rule.test.demo");
        dto.setRuleText("package com.his.rules;\nrule \"test\" when then end");
        dto.setCategory(RuleCategory.REIMBURSEMENT);

        when(ruleMapper.selectByRuleKey(any(), any())).thenReturn(null);

        // When
        ruleService.create(dto);

        // Then
        verify(ruleMapper).insert(any());
    }
}
```

#### 集成测试模板

```java
@SpringBootTest
@AutoConfigureMockMvc
class RuleDefinitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("分页查询规则 - 成功")
    void pageList_success() throws Exception {
        mockMvc.perform(get("/api/v1/rules")
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data.list").isArray());
    }
}
```

#### DRL 规则测试模板

```java
@SpringBootTest
class ReimbursementRuleTest {

    @Autowired
    private KieSession kieSession;

    @Test
    @DisplayName("居民起付线规则 - 正确设置500")
    void residentDeductible_success() {
        // Given
        SettlementFact fact = new SettlementFact();
        fact.setPatientType("resident");
        fact.setTotalFee(new BigDecimal("2000"));

        // When
        kieSession.insert(fact);
        kieSession.fireAllRules();

        // Then
        assertThat(fact.getDeductible()).isEqualTo(new BigDecimal("500"));
    }
}
```

### 7.3 性能测试场景

| 场景 | 并发数 | 持续时间 | 通过标准 |
|------|:------:|---------|---------|
| 单接口响应时间 | 1 | 100 次 | P99 < 100ms |
| 结算流程压测 | 50 | 5 分钟 | P99 < 50ms, 错误率 < 0.1% |
| 并发支持测试 | 500 | 10 分钟 | TPS ≥ 500, 错误率 < 0.1% |
| 稳定性测试 | 100 | 24 小时 | 无内存泄漏，无服务崩溃 |

---

## 8. 部署方案

### 8.1 开发环境

```bash
# 1. 启动 Nacos
cd nacos/bin && sh startup.sh -m standalone

# 2. 启动 MySQL
docker run -d --name mysql8 -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=his_rule_engine \
  mysql:8.0

# 3. 执行建表脚本
mysql -h 127.0.0.1 -u root -proot123 his_rule_engine < sql/schema.sql

# 4. 编译项目
cd his-rule-engine && mvn clean compile

# 5. 启动服务（按依赖顺序）
mvn spring-boot:run -pl his-gateway
mvn spring-boot:run -pl his-rule-service
mvn spring-boot:run -pl his-formula-service
mvn spring-boot:run -pl his-settlement-service
mvn spring-boot:run -pl his-drug-service
mvn spring-boot:run -pl his-quality-service
mvn spring-boot:run -pl his-drg-service
```

### 8.2 生产环境（Docker Compose）

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: his_rule_engine
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    ports:
      - "3306:3306"

  nacos:
    image: nacos/nacos-server:v2.2.3
    environment:
      MODE: standalone
    ports:
      - "8848:8848"
      - "9848:9848"

  his-gateway:
    build: ./his-rule-engine/his-gateway
    ports:
      - "9000:9000"
    environment:
      NACOS_ADDR: nacos:8848
      MYSQL_HOST: mysql
    depends_on:
      - nacos
      - mysql

  # ... 其他服务

volumes:
  mysql_data:
```

---

## 9. 风险清单

| 编号 | 风险 | 影响 | 概率 | 应对措施 | 责任人 |
|------|------|------|:----:|---------|--------|
| R01 | Drools 规则编译失败 | 高 | 中 | 发布前强制校验，失败拒绝发布 | 后端开发 |
| R02 | Aviator 公式语法错误 | 高 | 中 | 保存前编译验证，执行时捕获异常降级 | 后端开发 |
| R03 | Nacos 不可用 | 高 | 低 | 本地缓存兜底，不影响已加载规则 | 运维 |
| R04 | 多租户数据泄露 | 高 | 低 | 所有查询强制过滤 tenant_id，单元测试覆盖 | 后端开发 |
| R05 | 金额计算精度丢失 | 高 | 低 | 强制 BigDecimal，Code Review 检查 | 后端开发 |
| R06 | 微服务调用超时 | 中 | 中 | 设置合理超时，同机房部署 | 运维 |
| R07 | 缓存内存溢出 | 中 | 低 | 设置 maximumSize，监控内存 | 后端开发 |

---

## 10. 里程碑

| 里程碑 | 日期 | 交付物 | 验收人 |
|--------|------|--------|--------|
| M0: 基础设施就绪 | W1 结束 | 公共模块编译通过，数据库初始化完成 | 架构师 |
| M1: 规则公式就绪 | W3 结束 | 规则/公式 CRUD + 发布流程可执行 | 产品经理 |
| M2: 结算用药就绪 | W5 结束 | 结算流程可执行，用药审核可拦截 | 产品经理 |
| M3: 全功能就绪 | W6 结束 | 所有模块功能开发完成 | 产品经理 |
| M4: 集成测试通过 | W7 结束 | 全链路联调通过，网关路由正常 | QA |
| M5: 发布就绪 | W8 结束 | 测试报告、性能报告、部署文档 | 项目经理 |

---

## 11. 附录

### 11.1 错误码清单

| 错误码 | 说明 | 模块 |
|--------|------|------|
| HIS-001 | 患者身份信息缺失 | 结算 |
| HIS-002 | 规则不存在 | 规则管理 |
| HIS-003 | 结算正在进行中 | 结算 |
| HIS-101 | 公式语法错误 | 公式管理 |
| HIS-102 | 公式参数不匹配 | 公式管理 |
| HIS-103 | 公式计算溢出 | 公式管理 |
| HIS-201 | 重复结算 | 结算 |
| HIS-202 | 审核拒绝 | 结算 |
| HIS-301 | 无租户权限 | 权限 |
| HIS-401 | Aviator 注入检测 | 安全 |
| HIS-901 | 配置中心不可用 | 系统 |
| HIS-902 | KIE 编译失败 | 系统 |
| HIS-999 | 系统内部异常 | 系统 |

### 11.2 事件类型清单

| 事件类型 | 说明 | 关注服务 |
|---------|------|---------|
| EVENT_SETTLEMENT_EXECUTE | 结算执行 | 结算服务 |
| EVENT_DRUG_PRESCRIBE | 处方开具 | 用药服务 |
| EVENT_QUALITY_CHECK | 质控检查 | 质控服务 |
| EVENT_DRG_GROUP | DRG 分组 | DRG 服务 |

### 11.3 参考文档

- [PRD.md](PRD.md) — 产品需求文档
- [README.md](README.md) — 项目说明
- [drools_aviator.md](drools_aviator.md) — 架构设计文档
- [.trae/rules/](.trae/rules/) — 编码规范
- [CLAUDE.md](CLAUDE.md) — 项目规则

---

*文档结束*
