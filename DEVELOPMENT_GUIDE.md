# HIS 动态规则中台 — V1.0 开发指导文档

> 文档版本: v1.0  
> 创建日期: 2026-04-26  
> 文档状态: 可执行  
> 依据文档: [PRD.md](PRD.md) v1.0  
> 适用项目: his_drools_aviator

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

### 2.1 整体架构图

```
                    ┌─────────────────────────────────────────────────────┐
                    │                    前端管理后台                        │
                    │              (Vue3 + Element Plus)                   │
                    └────────────────────────┬────────────────────────────┘
                                             │ HTTP/HTTPS
                                             ▼
                    ┌─────────────────────────────────────────────────────┐
                    │              API Gateway (9000)                      │
                    │  JWT 鉴权 │ 路由转发 │ 限流熔断 │ 日志记录 │ CORS    │
                    └──────┬──────┬──────┬──────┬──────┬──────┬───────────┘
                           │      │      │      │      │      │
              ┌────────────┘      │      │      │      │      └────────────┐
              ▼                   ▼      ▼      ▼      ▼                   ▼
     ┌─────────────┐    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
     │Rule Service │    │Formula Svc  │ │Settlement   │ │  Drug Svc   │
     │   (9001)    │    │   (9002)    │ │   (9003)    │ │   (9004)    │
     └──────┬──────┘    └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
            │                  │                │                │
            ▼                  ▼                ▼                ▼
     ┌─────────────┐    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
     │Quality Svc  │    │  DRG Svc    │ │  MySQL 8.0  │ │   Nacos     │
     │   (9005)    │    │   (9006)    │ │  (规则/公式  │ │  配置中心   │
     └─────────────┘    └─────────────┘ │  /结算数据)  │ │  注册中心   │
                                         └─────────────┘ └─────────────┘
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
his-gateway
    └── (无内部依赖，仅依赖 Spring Cloud Gateway)

his-rule-service
    ├── his-common-core
    ├── his-common-web
    └── his-common-drools

his-formula-service
    ├── his-common-core
    ├── his-common-web
    └── his-common-aviator

his-settlement-service
    ├── his-common-core
    ├── his-common-web
    ├── his-common-drools
    └── his-common-aviator

his-drug-service
    ├── his-common-core
    ├── his-common-web
    ├── his-common-drools
    └── his-common-aviator

his-quality-service
    ├── his-common-core
    ├── his-common-web
    ├── his-common-drools
    └── his-common-aviator

his-drg-service
    ├── his-common-core
    ├── his-common-web
    └── his-common-aviator

his-common (公共模块)
    ├── his-common-core      ← 所有服务依赖
    ├── his-common-web       ← 所有服务依赖
    ├── his-common-drools    ← 需要 Drools 的服务依赖
    └── his-common-aviator   ← 需要 Aviator 的服务依赖
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

### 5.2 编码规范

#### 5.2.1 类命名

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

#### 5.2.2 强制规则

1. 金额计算必须使用 `BigDecimal`，禁止使用 `double`/`float`
2. 所有 Controller 方法返回 `Result<T>`
3. 所有 Service 方法必须记录日志（`@Slf4j`）
4. DRL 规则中禁止使用 `System.out.println`，使用 SLF4J
5. Aviator 表达式必须缓存编译结果
6. 所有查询必须过滤 `tenant_id`
7. 所有写操作必须记录审计日志
8. 单个 Java 文件不超过 500 行

### 5.3 Git 提交规范

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

| 阶段 | 名称 | 周期 | 核心交付 |
|------|------|------|---------|
| Phase 0 | 基础设施 | W1 | 数据库、Nacos、公共模块 |
| Phase 1 | 规则与公式 | W2-W3 | 规则管理、公式管理 |
| Phase 2 | 结算与用药 | W4-W5 | 医保结算、合理用药 |
| Phase 3 | 质控与 DRG | W6 | 质控、DRG 分组 |
| Phase 4 | 网关与集成 | W7 | API 网关、联调测试 |
| Phase 5 | 测试与优化 | W8 | 性能测试、安全测试、文档 |

### 6.2 Phase 0: 基础设施（W1）

**目标**: 搭建开发环境，完成公共模块和数据库初始化

| 步骤 | 任务 | 交付物 | 验收标准 |
|------|------|--------|---------|
| 0.1 | 安装 Nacos Server | Nacos 运行在 8848 端口 | 控制台可访问 |
| 0.2 | 创建 MySQL 数据库 | 执行建表脚本 | 10 张表创建成功 |
| 0.3 | his-common-core 开发 | 枚举、异常、Fact 对象 | 编译通过，单元测试通过 |
| 0.4 | his-common-web 开发 | 统一响应、异常处理、Swagger | 编译通过 |
| 0.5 | his-common-drools 开发 | Drools 会话管理、规则加载 | 编译通过，可加载 DRL |
| 0.6 | his-common-aviator 开发 | Aviator 执行器、Caffeine 缓存 | 编译通过，表达式执行正常 |

**详细任务分解**:

#### 0.3 his-common-core 开发

```
文件清单:
├── ErrorCode.java              # 错误码枚举
├── ResultLevel.java            # PASS/WARN/BLOCK
├── HisEventType.java           # 事件类型常量
├── SettlementFact.java         # 结算事实对象
├── ISkill.java                 # Skill 接口
├── SkillContext.java           # 执行上下文
├── SkillResult.java            # 执行结果
├── exception/
│   ├── HisBaseException.java   # 基础异常
│   ├── BusinessException.java  # 业务异常
│   ├── FormulaException.java   # 公式异常
│   └── SettlementException.java # 结算异常
└── constant/
    └── RuleConstants.java      # 规则常量
```

#### 0.4 his-common-web 开发

```
文件清单:
├── config/
│   ├── WebMvcConfig.java       # Web MVC 配置
│   └── SwaggerConfig.java      # SpringDoc 配置
├── interceptor/
│   └── TenantContextFilter.java # 租户上下文过滤器
├── handler/
│   └── GlobalExceptionHandler.java # 全局异常处理
├── result/
│   ├── Result.java             # 统一响应
│   └── PageResult.java         # 分页响应
└── annotation/
    └── RequireTenant.java      # 租户校验注解
```

#### 0.5 his-common-drools 开发

```
文件清单:
├── config/
│   └── DroolsConfig.java       # KieContainer 配置
├── engine/
│   ├── DroolsEngine.java       # Drools 执行封装
│   └── KieSessionManager.java  # KieSession 管理
└── helper/
    └── DrlValidator.java       # DRL 语法校验
```

#### 0.6 his-common-aviator 开发

```
文件清单:
├── config/
│   └── AviatorConfig.java      # Aviator 初始化
├── engine/
│   ├── AviatorEngine.java      # Aviator 执行封装
│   └── ExpressionCache.java    # Caffeine 缓存
├── helper/
│   ├── AviatorHelper.java      # 公式执行工具
│   └── FormulaValidator.java   # 公式语法校验
└── function/
    └── HisAviatorFunctions.java # 自定义函数注册
```

### 6.3 Phase 1: 规则与公式管理（W2-W3）

**目标**: 实现规则管理和公式管理的完整 CRUD + 发布流程

| 步骤 | 任务 | 交付物 | 验收标准 |
|------|------|--------|---------|
| 1.1 | 规则 Entity/Mapper | RuleDefinition Entity + Mapper XML | 数据库 CRUD 正常 |
| 1.2 | 规则 Service | RuleDefinitionService | 状态流转正确 |
| 1.3 | 规则 Controller | RuleDefinitionController | API 可调用，Swagger 可见 |
| 1.4 | 规则校验逻辑 | DRL 语法校验、命名冲突检测 | 错误规则被拦截 |
| 1.5 | 规则发布逻辑 | 状态变更 + 版本递增 + 审计日志 | 发布后规则生效 |
| 1.6 | 公式 Entity/Mapper | AviatorFormula Entity + Mapper | 数据库 CRUD 正常 |
| 1.7 | 公式 Service | FormulaService | 状态流转正确 |
| 1.8 | 公式 Controller | FormulaController | API 可调用 |
| 1.9 | 公式语法校验 | Aviator 编译验证 | 错误公式被拦截 |
| 1.10 | 公式发布 + Nacos 同步 | 发布后写入 Nacos | Nacos 配置可查询 |
| 1.11 | 公式参数管理 | FormulaParam CRUD | 参数关联正确 |
| 1.12 | 审计日志 | AuditLog 记录所有写操作 | 日志表有记录 |

**详细任务分解**:

#### 1.1-1.5 规则管理模块

```
his-rule-service/
├── entity/
│   └── RuleDefinition.java
├── mapper/
│   └── RuleDefinitionMapper.java
├── service/
│   ├── RuleDefinitionService.java
│   └── impl/
│       └── RuleDefinitionServiceImpl.java
├── controller/
│   └── RuleDefinitionController.java
├── dto/
│   ├── RuleCreateDTO.java
│   ├── RuleUpdateDTO.java
│   ├── RuleQueryDTO.java
│   ├── RuleVO.java
│   └── RuleValidateResult.java
└── config/
    └── RuleEngineConfig.java
```

#### 1.6-1.11 公式管理模块

```
his-formula-service/
├── entity/
│   ├── AviatorFormula.java
│   └── FormulaParam.java
├── mapper/
│   ├── AviatorFormulaMapper.java
│   └── FormulaParamMapper.java
├── service/
│   ├── FormulaService.java
│   └── impl/
│       └── FormulaServiceImpl.java
├── controller/
│   └── FormulaController.java
├── dto/
│   ├── FormulaCreateDTO.java
│   ├── FormulaUpdateDTO.java
│   ├── FormulaQueryDTO.java
│   ├── FormulaVO.java
│   ├── FormulaParamDTO.java
│   ├── FormulaParamVO.java
│   ├── FormulaTestDTO.java
│   ├── FormulaTestResult.java
│   └── FormulaValidateResult.java
├── listener/
│   └── NacosFormulaSyncListener.java
└── config/
    └── NacosConfig.java
```

### 6.4 Phase 2: 结算与用药（W4-W5）

**目标**: 实现医保结算核心流程和合理用药审核

| 步骤 | 任务 | 交付物 | 验收标准 |
|------|------|--------|---------|
| 2.1 | 结算 Entity/Mapper | SettlementResult Entity + Mapper | 数据库 CRUD 正常 |
| 2.2 | 结算 Service | SettlementService | 结算流程可执行 |
| 2.3 | 结算 Controller | SettlementController | API 可调用 |
| 2.4 | 身份校验 Skill | InsuranceIdentitySkill | 身份缺失返回 BLOCK |
| 2.5 | 起付线 Skill | DeductibleSkill | 正确设置起付线 |
| 2.6 | 报销比例 Skill | ReimburseRatioSkill | 正确设置报销比例 |
| 2.7 | 报销金额计算 | 调用 Aviator 公式 | 计算结果正确 |
| 2.8 | 目录限制校验 | CatalogLimitSkill | 不在目录返回 WARN |
| 2.9 | 重复结算拦截 | DuplicateSettlementValidator | 重复结算被拦截 |
| 2.10 | 用药 Entity/Mapper | PrescriptionRecord Entity + Mapper | 数据库 CRUD 正常 |
| 2.11 | 用药 Service | DrugCheckService | 处方审核可执行 |
| 2.12 | 用药 Controller | DrugController | API 可调用 |
| 2.13 | 配伍禁忌 Skill | DrugCompatibilitySkill | 禁忌药品返回 BLOCK |
| 2.14 | 极量检查 Skill | DrugDosageLimitSkill | 超量返回 WARN |
| 2.15 | 过敏史检查 Skill | DrugAllergySkill | 过敏返回 BLOCK |

**详细任务分解**:

#### 2.1-2.9 医保结算模块

```
his-settlement-service/
├── entity/
│   └── SettlementResult.java
├── mapper/
│   └── SettlementResultMapper.java
├── service/
│   ├── SettlementService.java
│   └── impl/
│       └── SettlementServiceImpl.java
├── controller/
│   └── SettlementController.java
├── dto/
│   ├── SettlementRequestDTO.java
│   ├── SettlementDetailDTO.java
│   ├── SettlementResultVO.java
│   └── SettlementAmountsVO.java
├── skill/
│   ├── InsuranceIdentitySkill.java
│   ├── DeductibleSkill.java
│   ├── ReimburseRatioSkill.java
│   ├── ReimburseAmountSkill.java
│   ├── CatalogLimitSkill.java
│   └── DuplicateSettlementValidator.java
├── config/
│   └── SettlementEngineConfig.java
└── validator/
    └── SettlementValidator.java
```

#### 2.10-2.15 合理用药模块

```
his-drug-service/
├── entity/
│   └── PrescriptionRecord.java
├── mapper/
│   └── PrescriptionRecordMapper.java
├── service/
│   ├── DrugCheckService.java
│   └── impl/
│       └── DrugCheckServiceImpl.java
├── controller/
│   └── DrugController.java
├── dto/
│   ├── PrescriptionCheckDTO.java
│   ├── PrescriptionItemDTO.java
│   ├── DrugCheckResultVO.java
│   └── DrugCheckDetailVO.java
├── skill/
│   ├── DrugCompatibilitySkill.java
│   ├── DrugDosageLimitSkill.java
│   └── DrugAllergySkill.java
├── config/
│   └── DrugEngineConfig.java
└── constant/
    └── DrugConstants.java
```

### 6.5 Phase 3: 质控与 DRG（W6）

**目标**: 实现质控检查和 DRG 分组

| 步骤 | 任务 | 交付物 | 验收标准 |
|------|------|--------|---------|
| 3.1 | 质控 Entity/Mapper | QualityRecord Entity + Mapper | 数据库 CRUD 正常 |
| 3.2 | 质控 Service | QualityCheckService | 质控检查可执行 |
| 3.3 | 质控 Controller | QualityController | API 可调用 |
| 3.4 | 院感规则 Skill | InfectionControlSkill | 院感违规返回 BLOCK |
| 3.5 | 质控规则 Skill | QualityRuleSkill | 质控规则可执行 |
| 3.6 | DRG Entity/Mapper | DrgGroupRecord Entity + Mapper | 数据库 CRUD 正常 |
| 3.7 | DRG Service | DrgGroupService | DRG 分组可执行 |
| 3.8 | DRG Controller | DrgController | API 可调用 |
| 3.9 | DRG 分组 Skill | DrgGroupingSkill | 分组结果正确 |
| 3.10 | 权重计算 | 调用 Aviator 公式 | 权重计算正确 |

### 6.6 Phase 4: 网关与集成（W7）

**目标**: 完成 API 网关配置和全链路联调

| 步骤 | 任务 | 交付物 | 验收标准 |
|------|------|--------|---------|
| 4.1 | 网关路由配置 | Gateway 路由到所有服务 | 请求正确转发 |
| 4.2 | JWT 鉴权 | Token 校验拦截器 | 无 Token 返回 401 |
| 4.3 | CORS 配置 | 跨域支持 | 前端可跨域访问 |
| 4.4 | 日志记录 | 请求日志过滤器 | 日志表有记录 |
| 4.5 | 全链路联调 | 端到端测试 | 结算流程可完整执行 |
| 4.6 | 限流配置 | Sentinel 限流规则 | 超限请求被拦截 |

### 6.7 Phase 5: 测试与优化（W8）

**目标**: 完成测试、性能优化和文档

| 步骤 | 任务 | 交付物 | 验收标准 |
|------|------|--------|---------|
| 5.1 | 单元测试 | 各模块单元测试 | 覆盖率达标 |
| 5.2 | 集成测试 | Controller 层集成测试 | API 测试通过 |
| 5.3 | 性能测试 | JMeter 压测报告 | 满足 NFR-P01~P08 |
| 5.4 | 安全测试 | 安全扫描报告 | 无高危漏洞 |
| 5.5 | API 文档 | SpringDoc 自动生成的文档 | 所有接口有文档 |
| 5.6 | 部署文档 | Docker 部署指南 | 可一键部署 |

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
