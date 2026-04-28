# MEMORY.md - HIS 动态规则中台

> 跨会话持久化的事实性知识（不含 AI 行为规则，行为规范见 agent.md）

---

## 1. 项目元信息

| 属性 | 值 |
|------|-----|
| 名称 | HIS 动态规则中台（Drools + Aviator） |
| 定位 | 面向医院信息系统的规则引擎平台，支持医保结算、合理用药、质控等业务场景的动态规则编排与公式计算 |
| 许可 | 内部项目 |

### 核心能力
- **Drools 规则引擎**: 业务规则编排（条件判断/优先级/分组/触发流程）
- **Aviator 表达式引擎**: 高性能动态公式计算（报销金额/用药剂量/DRG权重等）
- **Skill/Agent 插件化架构**: 按事件类型动态加载和执行业务能力
- **配置中心集成**: Nacos/Apollo 动态规则热更新
- **表达式缓存**: Caffeine 本地缓存编译后的 Aviator Expression
- **多租户支持**: 按医院/租户隔离规则集和公式配置

### 典型应用场景
| 场景 | 说明 |
|------|------|
| 医保住院费用结算 | 身份校验 → 起付线判断 → 报销比例 → 公式计算报销金额 |
| 合理用药审核 | 配伍禁忌/极量检查/适应症匹配/特殊人群用药限制 |
| DRG/DIP 分组结算 | 诊断分组 → 权重调整 → 标准分值计算 |
| 院感防控拦截 | 手卫生/无菌操作/隔离措施等规则卡控 |

---

## 2. 技术栈详情

### 后端核心
| 组件 | 版本 | 用途 |
|------|------|------|
| Java | 17+ | 开发语言 |
| Spring Boot | 3.x | 应用框架 |
| Drools | 8.x | 规则引擎（KIE / rule units） |
| Aviator | 5.4.x | 表达式引擎（高性能数学/逻辑计算） |
| Caffeine | 3.1.x | 本地缓存（Expression 编译缓存） |
| Nacos / Apollo | — | 配置中心（动态公式/规则热更新） |
| MySQL | 8.0+ | 数据库（utf8mb4 字符集） |
| Maven | — | 构建工具 |
| SLF4J + Logback | — | 日志框架 |
| Jackson | — | JSON 序列化 |

### 架构分层
```
┌─────────────────────────────────────────────┐
│           HIS 业务系统 (医生站/护士站/计费)     │  ← 事件来源
├─────────────────────────────────────────────┤
│         Skill Gateway (智能调度网关)            │  ← 事件路由 + Pipeline
├──────┬──────┬──────┬──────┬──────────────────┤
│医保   │合理  │院感  │质控  │ ... (可扩展 Skill) │
│Limit  │用药  │拦截  │规则  │                     │
│Skill  │Skill │Skill │Skill │                     │
├──────┴──────┴──────┴──────┴──────────────────┤
│    RuleEngineTemplate (Drools + Aviator)      │  ← 统一规则执行模板
├────────────────────┬─────────────────────────┤
│  AviatorExpressionCache │ DynamicFormulaManager│  ← 缓存 + 动态刷新
├────────────────────┴─────────────────────────┤
│        Nacos / Apollo (配置中心)               │  ← 规则/公式存储
└─────────────────────────────────────────────┘
```

---

## 3. 目录结构

```
his_drools_aviator/
├── .trae/                    # Harness 工程配置（AI 辅助开发）
├── drools_aviator.md           # Drools+Aviator 混合架构技术文档
├── test-his-ui-drug.md       # 全院级 Skill/Agent 架构设计文档
├── README.md                   # 项目说明
│
├── src/main/java/com/his/
│   ├── config/                 # 配置类 (DroolsConfig, AviatorProperties, NacosConfig)
│   ├── fact/                   # Fact 对象 (SettlementFact, PrescriptionFact, ...)
│   ├── rule/                   # DRL 规则文件 (reimbursement.drl, drug_check.drl, ...)
│   ├── skill/                  # Agent/Skill 实现 (ISkill 接口 + 各业务 Skill)
│   │   ├── ISkill.java         #   能力标准接口
│   │   ├── SkillGateway.java   #   调度网关
│   │   ├── SkillContext.java   #   统一上下文
│   │   ├── SkillResult.java    #   执行结果
│   │   ├── InsuranceLimitSkill.java    # 医保限制 Agent
│   │   ├── RationalDrugUseSkill.java   # 合理用药 Agent
│   │   └── InfectionControlSkill.java  # 院感拦截 Agent
│   ├── engine/                 # 引擎封装
│   │   ├── RuleEngineTemplate.java     # Drools + Aviator 统一模板
│   │   └── AviatorHelper.java          # Aviator 执行器
│   ├── cache/                  # 缓存层
│   │   └── AviatorExpressionCache.java # Caffeine 表达式缓存
│   ├── formula/                # 公式管理
│   │   ├── DynamicFormulaManager.java  # 动态公式管理器 (Nacos/Apollo)
│   │   └── FormulaEntity.java          # 公式实体
│   ├── event/                  # 事件定义
│   │   └── HisEventType.java           # 标准事件类型常量
│   ├── service/                # 服务层 (SettlementService, ...)
│   ├── controller/             # 控制器层
│   ├── common/                 # 公共工具 (异常/常量/枚举)
│   │   ├── ResultLevel.java            # PASS/WARN/BLOCK
│   │   ├── HisException.java           # 自定义异常基类
│   │   └── ErrorCode.java              # 错误码枚举
│   └── HisRuleEngineApplication.java   # 启动类
│
├── src/main/resources/
│   ├── rules/                  # DRL 规则文件 (或从 DB/Nacos 加载)
│   ├── application.yml         # 主配置
│   ├── application-dev.yml     # 开发环境
│   ├── application-prod.yml    # 生产环境
│   └── bootstrap.yml           # Nacos 配置 (如使用 Nacos)
│
├── src/test/java/              # 测试代码
└── pom.xml                     # Maven 依赖
```

---

## 4. API 端点全集

### 规则引擎核心接口
| 模块 | 路径 | 说明 |
|------|------|------|
| 结算 | `/api/settlement/execute` | 执行医保费用结算 |
| 处方审核 | `/api/drug/prescribe/check` | 合理用药审核 |
| 规则管理 | `/api/rules/` | 规则 CRUD + 版本管理 |
| 公式管理 | `/api/formulas/` | 公式 CRUD + 语法校验 |
| Agent 管理 | `/api/skills/` | Skill 注册/启用/禁用 |
| 事件触发 | `/api/events/fire` | 手动触发事件执行 |
| 健康检查 | `/api/health` | 引擎状态 + 缓存命中率 |

### 响应格式约定
```java
// 成功响应
{
  "code": "0",
  "data": { ... },
  "message": "操作成功"
}

// Skill 执行结果
{
  "code": "0",
  "data": {
    "results": [
      { "level": "PASS", "source": "InsuranceLimitSkill", "message": null },
      { "level": "WARN", "source": "RationalDrugUseSkill", "message": "配伍禁忌警告: A药+B药" }
    ],
    "hasBlock": false
  }
}

// 错误响应
{ "code": "HIS-001", "data": null, "message": "患者身份信息缺失" }
```

---

## 5. 数据库模型概要

### 核心表关系
```
RuleGroup (规则组 - 按业务分类)
  ├── Rule (规则定义 - DRL内容/优先级/生效时间)
  │   └── RuleVersion (规则版本 - 支持回滚)
  Formula (公式定义 - Aviator表达式文本)
  ├── FormulaHistory (公式变更历史)
  └── FormulaCategory (公式分类: 报销/用药/DRG等)

SkillConfig (Agent/Skill 配置)
  ├── SkillEventBinding (事件-Skill绑定关系)
  └── SkillExecutionLog (执行日志)

SettlementRecord (结算记录)
  └── SettlementDetail (结算明细)

PrescriptionAuditLog (处方审核日志)
```

### 字符集与排序
- 字符集: `utf8mb4`
- 排序: `utf8mb4_general_ci`

### 配置方式
- 主配置: `application.yml` / `application-{profile}.yml`
- 配置中心: Nacos (`bootstrap.yml`) 或 Apollo
- 环境变量覆盖: 优先级最高

---

## 6. 环境与部署

### Maven 核心依赖
```xml
<!-- Drools -->
<dependency>
    <groupId>org.drools</groupId>
    <artifactId>drools-core</artifactId>
    <version>8.x.x</version>
</dependency>
<dependency>
    <groupId>org.drools</groupId>
    <artifactId>drools-compiler</artifactId>
    <version>8.x.x</version>
</dependency>

<!-- Aviator -->
<dependency>
    <groupId>com.googlecode.aviator</groupId>
    <artifactId>aviator</artifactId>
    <version>5.4.3</version>
</dependency>

<!-- Caffeine 缓存 -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
    <version>3.1.8</version>
</dependency>

<!-- Nacos (可选) -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

### 关键启动参数
| 参数 | 默认值 | 说明 |
|------|--------|------|
| `aviator.cache.max-size` | 1000 | 表达式缓存最大条数 |
| `aviator.cache.expire-minutes` | 30 | 缓存过期时间(分钟) |
| `drools.rule.scan-interval` | 60 | 规则文件扫描间隔(秒) |
| `nacos.config.namespace` | his_prod | Nacos 命名空间 |
| `skill.pipeline.timeout-ms` | 5000 | Skill Pipeline 超时时间 |

---

## 7. 常用命令

```bash
# 构建
mvn clean package -DskipTests

# 运行
mvn spring-boot:run

# 测试
mvn test

# 指定 Profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 8. 历史经验与常见问题

### 已知注意事项
1. **Aviator 类型一致性**: Drools 和 Aviator 之间传递数值统一使用 `BigDecimal`
2. **表达式缓存失效**: 配置变更时需调用 `aviatorCache.refreshFormula()` 刷新缓存
3. **降级策略**: Apollo/Nacos 不可用时返回默认值，避免业务中断
4. **事务边界**: 同步拦截(BLOCK)必须在 HIS 事务 Commit 之前调用网关
5. **异步分析(WARN)**: 耗时 >500ms 的长规则建议通过 MQ 异步处理

---

最后更新: 2026-04-26 | 版本: v1.0 (HIS Drools+Aviator 规则引擎)
