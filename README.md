# HIS 动态规则中台 - 基于 Spring Cloud Alibaba + Nacos

## 📖 项目简介

本项目是一个**医疗业务规则剥离引擎**，旨在将 HIS 系统中高频变化的业务规则（医保报销、质控、合理用药、DRG 分组等）从核心系统中抽离，封装为可独立热加载的规则单元。通过 **Drools + Aviator** 混合架构，实现规则的灵活编排和高性能计算，并通过 **Nacos** 配置中心实现公式的实时动态刷新。

**核心价值**：  
- 医院可自主维护业务规则，不再依赖 HIS 厂商的代码修改周期  
- 规则变更从"2~4周"缩短至"分钟级"  
- 底层 HIS 退化为稳定的事务底座，上层业务逻辑灵活可控  

---

## 🛠️ 技术栈

| 组件 | 版本 | 状态 | 用途 |
|:---|:---|:---:|:---|
| Spring Boot | 3.2.5 | ✅ | 基础框架 |
| Spring Cloud | 2023.0.1 | ✅ | 微服务生态 |
| Spring Cloud Alibaba | 2023.0.1.0 | ✅ | Nacos 注册中心 + 配置中心 |
| Drools | 8.44.0.Final | ✅ | 规则引擎（规则流编排） |
| Aviator | 5.4.3 | ✅ | 高性能表达式求值 |
| Caffeine | 3.1.8 | ✅ | 本地缓存（编译后表达式） |
| MyBatis-Plus | 3.5.6 | ✅ | ORM 框架 |
| MySQL | 8.0+ | 📋 | 存储规则定义、事实数据 |
| Sentinel | - | 📋 | 流量控制、熔断降级 |
| OpenFeign | - | ✅ | 声明式服务调用 |
| Maven | 3.8+ | ✅ | 构建工具 |
| JDK | 21 | ✅ | 运行环境 |

> ✅ 已集成 &nbsp; 📋 待实现 &nbsp; ⏳ 规划中

---

## 📦 环境要求

- JDK 21 或更高版本  
- Maven 3.8+    
- Nacos Server 2.x（[下载地址](https://github.com/alibaba/nacos/releases)）  
- MySQL 8.0（仅当需要持久化规则库时）  
- Git  

---

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/zzlzy1989/his_drools_aviator.git
cd his_drools_aviator
```

### 2. 启动 Nacos Server <span style="color:orange">📋 待实现</span>

```bash
# 解压并启动
unzip nacos-server-2.x.x.zip
cd nacos/bin
# Linux/Mac
sh startup.sh -m standalone
# Windows
startup.cmd -m standalone
```

访问 Nacos 控制台：http://localhost:8848/nacos （默认账号密码：nacos/nacos）

### 3. 创建 Nacos 配置 <span style="color:orange">📋 待实现</span>

在 Nacos 中创建 Data ID 为 `his-rule-engine.yaml`，Group 为 `HIS_RULE_GROUP` 的配置，内容如下：

```yaml
formulas:
  reimburse:
    resident: "round((totalFee - 500) * 0.65, 2)"
    employee: "round((totalFee - 1000) * 0.85, 2)"
  drg:
    weight: "(baseWeight + extraPoints) * severityFactor"
```

### 4. 配置数据库 <span style="color:orange">📋 待实现</span>

数据库配置：本地127.0.0.1/92.168.1.105 端口 3306,用户名root,密码testhub123
如需要持久化规则操作日志或规则元数据，请创建 MySQL 数据库并执行以下脚本：

```sql
CREATE DATABASE `his_rule_engine` CHARACTER SET utf8mb4;

USE `his_rule_engine`;

CREATE TABLE `rule_formula` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rule_key` varchar(128) NOT NULL COMMENT '规则标识',
  `formula_text` text NOT NULL COMMENT '公式表达式',
  `version` int DEFAULT '1',
  `status` tinyint DEFAULT '1' COMMENT '1-生效 0-失效',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_key` (`rule_key`)
);
```

修改各服务 `application.yml` 中的数据库连接参数。

### 5. 编译项目

```bash
cd his-rule-engine
mvn clean compile
```

### 6. 启动服务 <span style="color:orange">📋 待实现</span>

```bash
# 启动网关（端口 9000）
cd his-gateway
mvn spring-boot:run

# 启动规则管理服务（端口 9001）
cd his-rule-service
mvn spring-boot:run

# 启动医保结算服务（端口 9003）
cd his-settlement-service
mvn spring-boot:run

# ... 其他服务按需启动
```

### 7. 测试规则调用 <span style="color:orange">📋 待实现</span>

```bash
# 结算测试（示例）
curl -X POST http://localhost:9000/api/v1/settlements \
  -H "Content-Type: application/json" \
  -d '{"patientType":"resident","totalFee":2000}'
```

预期返回：`{"finalAmount":975.00}` （因公式 `round((2000-500)*0.65,2)` = 975）

---

## 📁 项目模块结构

```
his_drools_aviator/
├── .trae/                          # Trae IDE Harness 工程配置
│   ├── agent.md                    # AI 行为规范（身份/约束/流程/计划检查）
│   ├── MEMORY.md                   # 项目事实知识（技术栈/目录/API/环境）
│   ├── settings.local.json         # 权限配置 + 计划管理设置
│   │
│   ├── rules/                      # 编码规范（按领域模块化）
│   │   ├── README.md               # 规则索引 + 触发场景速查
│   │   ├── workflow.md             # 7 步强制工作流程 + 3 阻塞节点
│   │   ├── code-style.md           # Java/Spring/Drools/Aviator 编码规范
│   │   ├── testing.md              # JUnit5/MockMvc/DRL 规则测试规范
│   │   ├── security.md             # SQL 注入/XSS/Aviator 表达式注入防护
│   │   ├── api-design.md           # REST API 设计规范
│   │   ├── database.md             # MySQL 表结构/查询优化/索引规范
│   │   ├── error-handling.md       # ErrorCode 体系/异常层级/日志规范
│   │   ├── docker-deploy.md        # Docker 多阶段构建/JVM 优化/服务编排
│   │   ├── performance.md          # Caffeine 缓存/KieBase 分组/并发控制
│   │   ├── documentation.md        # DRL 规则/Aviator 公式注释规范
│   │   └── git-commit-message.md   # Conventional Commits 提交规范
│   │
│   ├── commands/                   # 自定义快捷命令
│   │   ├── README.md               # 命令清单 + 使用说明
│   │   ├── health-check.md         # ((command:health)) 环境检查
│   │   ├── show-context.md         # ((command:context)) 任务上下文
│   │   ├── project-status.md       # ((command:status)) 项目状态
│   │   ├── analyze-task.md         # ((command:analyze-task)) 任务分析
│   │   ├── check-memory.md         # ((command:check-memory)) 记忆检查
│   │   ├── summarize.md            # ((command:summarize)) 会话总结
│   │   ├── review.md               # ((command:review)) 代码审查
│   │   ├── knowledge-writeback.md  # ((command:knowledge-writeback)) 知识回写
│   │   └── plan.md                 # ((command:plan)) 计划管理
│   │
│   ├── hooks/                      # 事件驱动拦截脚本
│   │   ├── README.md               # Hook 配置说明 + 与 agent.md 关系
│   │   ├── pre-execute-shell.sh    # Shell 执行前 - 危险命令拦截
│   │   ├── post-execute-shell.sh   # Shell 执行后 - 日志记录
│   │   ├── pre-write-file.sh       # 文件写入前 - 受保护文件确认
│   │   ├── post-read-file.sh       # 文件读取后 - 敏感信息提醒
│   │   ├── pre-browser.sh          # 浏览器启动前 - CDP 提示
│   │   ├── pre-search.sh           # 搜索前 - 敏感词过滤
│   │   ├── pre-task-start.sh       # 任务开始前 - 计划检查/创建提示
│   │   ├── post-task-complete.sh   # 任务完成后 - 知识回写 + 计划状态检查
│   │   └── post-plan-update.sh     # 计划变更后 - 索引更新 + 变更日志
│   │
│   ├── domain/                     # 业务知识库（持续积累）
│   │   ├── README.md               # 知识库索引
│   │   ├── glossary.md             # HIS/Drools/Aviator 术语表
│   │   ├── rules.md                # 医保结算/费用校验业务规则
│   │   ├── state-machines.md       # 规则生命周期/结算流程状态机
│   │   ├── edge-cases.md           # BigDecimal 精度/规则冲突边界
│   │   ├── decisions.md            # 架构/数据/集成决策记录
│   │   └── modules/                # 模块概述（6 个微服务）
│   │
│   ├── workflow-plans/             # 计划生成与跟踪系统
│   │   ├── README.md               # 使用指南
│   │   ├── TEMPLATE.md             # 计划模板
│   │   ├── INDEX.md                # 计划跟踪索引（自动更新）
│   │   ├── plan.sh                 # 计划管理 CLI 工具
│   │   ├── active/                 # 活跃计划（pending/in_progress）
│   │   ├── completed/              # 已完成计划
│   │   └── archived/               # 已归档计划（7 天后自动归档）
│   │
│   ├── memory/                     # 日记系统
│   │   ├── README.md               # 日记使用说明
│   │   └── TEMPLATE.md             # 日记模板
│   │
│   ├── CHANGELOG.md                # Harness 配置变更历史
│   ├── SUMMARY.md                  # 实施总结
│   └── MIGRATION_GUIDE.md          # Harness 工程跨项目迁移指南
│
├── .gitignore                      # Git 忽略规则
├── README.md                       # 项目说明
├── PRD.md                          # 产品需求文档（v1.0）
├── DEVELOPMENT_GUIDE.md            # v1.0 开发指导文档
├── drools_aviator.md               # 架构设计文档
├── test-his-ui-drug.md             # 测试用例文档
│
└── his-rule-engine/                # 后端代码（微服务架构）
    ├── pom.xml                     # 父 POM（版本管理、依赖统一）
    │
    ├── his-common/                 # 公共模块
    │   ├── pom.xml
    │   ├── his-common-core/        #   核心：枚举、异常、Fact 对象
    │   ├── his-common-web/         #   Web：统一响应、异常处理、Swagger
    │   ├── his-common-drools/      #   Drools 引擎封装
    │   └── his-common-aviator/     #   Aviator 引擎封装
    │
    ├── his-gateway/                # API 网关 (9000)
    │   └── Spring Cloud Gateway + Nacos + Sentinel
    │
    ├── his-rule-service/           # 规则管理服务 (9001)
    │   └── 规则 CRUD、版本管理、DRL 发布
    │
    ├── his-formula-service/        # 公式管理服务 (9002)
    │   └── 公式 CRUD、语法校验、Nacos 同步
    │
    ├── his-settlement-service/     # 医保结算服务 (9003)
    │   └── 费用结算、报销计算、规则执行
    │
    ├── his-drug-service/           # 合理用药服务 (9004)
    │   └── 处方审核、配伍禁忌、极量检查
    │
    ├── his-quality-service/        # 质控服务 (9005)
    │   └── 院感防控、质控规则、拦截卡控
    │
    └── his-drg-service/            # DRG分组服务 (9006)
        └── DRG/DIP 分组、权重计算、标准分值
```

---

## 📊 服务端口规划

| 服务 | 端口 | 说明 | 状态 |
|:---|:---:|:---|:---:|
| API Gateway | 9000 | 统一入口，路由转发 | ✅ 骨架 |
| Rule Service | 9001 | 规则管理 | ✅ 骨架 |
| Formula Service | 9002 | 公式管理 | ✅ 骨架 |
| Settlement Service | 9003 | 医保结算 | ✅ 骨架 |
| Drug Service | 9004 | 合理用药 | ✅ 骨架 |
| Quality Service | 9005 | 质控 | ✅ 骨架 |
| DRG Service | 9006 | DRG/DIP 分组 | ✅ 骨架 |

> ✅ 骨架 = 模块结构、启动类、配置文件已就绪，业务逻辑待开发

---

## ⚙️ 核心配置说明

### Nacos 配置刷新 <span style="color:orange">📋 待实现</span>

- 所有公式均通过 `@RefreshScope` + `NacosConfigManager` 实现动态更新  
- 修改 Nacos 中的 `formulas.*` 配置后，应用会自动重新加载公式并刷新 Aviator 缓存，**无需重启**

### Drools 规则热加载 <span style="color:orange">📋 待实现</span>

- 规则文件放在 `src/main/resources/rules/` 下  
- 生产环境建议将 `.drl` 文件也存放在 Nacos 或数据库中，通过 `KieScanner` 实现热部署

### Aviator 表达式缓存 <span style="color:orange">📋 待实现</span>

- 使用 Caffeine 缓存编译后的 `Expression` 对象  
- 缓存大小：5000 条，过期时间：30 分钟  
- 公式变更时自动失效对应的缓存条目

---

## 🧪 示例：医保结算规则流 <span style="color:orange">📋 待实现</span>

### 事实对象（Fact）

```java
public class SettlementFact {
    private String patientType;      // resident / employee
    private BigDecimal totalFee;
    private BigDecimal deductible;
    private BigDecimal ratio;
    private BigDecimal finalAmount;
    // getters/setters
}
```

### Drools 规则文件（reimbursement.drl）

```drools
import com.his.rule.fact.SettlementFact;
import com.his.rule.engine.aviator.AviatorHelper;

global NacosFormulaManager formulaManager;
global AviatorHelper aviatorHelper;

rule "Determine Deductible"
    when
        $f: SettlementFact(patientType == "resident")
    then
        $f.setDeductible(new BigDecimal("500"));
        update($f);
end

rule "Calculate Reimbursement"
    when
        $f: SettlementFact(deductible != null, totalFee != null)
    then
        String formula = formulaManager.getFormula("reimburse.resident");
        BigDecimal amount = aviatorHelper.executeFormula(formula, $f);
        $f.setFinalAmount(amount);
        update($f);
end
```

---

## 📊 性能指标（目标） <span style="color:orange">📋 待实现</span>

| 场景 | 目标耗时 |
| :--- | :--- |
| 单条规则匹配（Drools） | < 5ms |
| Aviator 表达式执行（已缓存） | < 0.5ms |
| Nacos 配置变更到生效（含缓存刷新） | < 2s |
| 规则引擎冷启动（加载 100 条规则） | ~800ms |

---

## 🧰 常见问题

**Q：Nacos 配置不生效？**  
A：检查 `application.yml` 中的 `spring.cloud.nacos.config` 是否正确，并确保应用已添加 `@RefreshScope`。

**Q：Aviator 表达式报错 `Unknown variable`？**  
A：请在 `FormulaValidator` 中定义允许的变量白名单，或在执行前将所需变量全部放入 `env` 中。

**Q：如何发布新的规则文件？**  
A：本示例从 classpath 加载 `.drl` 文件，重新打包即可。生产环境建议将规则文件放入 Nacos 或数据库，通过 `KieScanner` 动态加载。

**Q：支持多租户（多个医院）吗？**  
A：可在 Nacos 中使用不同的 `namespace` 或 `group` 来隔离医院的配置；Drools 会话也可按租户独立构建。

---

## 🔧 扩展开发指南

1. **新增一个业务规则集**  
   - 定义新的事实对象（Fact）  
   - 编写对应的 `.drl` 文件  
   - 在 Nacos 中添加公式配置  
   - 实现业务服务调用规则引擎

2. **接入自己的数据库作为规则源**  
   - 实现 `RuleProvider` 接口，从数据库读取规则内容  
   - 移除 `application.yml` 中的 Nacos 依赖（可选）

3. **集成监控**  
   - 暴露 `/actuator/health` 端点  
   - 记录每次规则调用的耗时和命中率（Aviator 缓存统计）

---

## 📄 许可证

Apache License 2.0

---

## 👥 贡献者

欢迎提交 Issue 或 Pull Request。

**项目维护者**：HIS 创新实验室

---

## 🔗 相关文档

- [Spring Cloud Alibaba 官方文档](https://sca.aliyun.com/docs/)
- [Nacos 配置管理](https://nacos.io/zh-cn/docs/configuration-management.html)
- [Drools 用户手册](https://docs.drools.org/)
- [Aviator 表达式引擎指南](https://github.com/killme2008/aviator)
