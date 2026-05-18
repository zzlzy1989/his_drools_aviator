# Phase 11: 统一整改计划 — 未完成项汇总

## 基本信息

| 字段 | 值 |
|------|-----|
| 计划编号 | PLAN-2026-05-18-003 |
| 创建日期 | 2026-05-18 |
| 类型 | bugfix + feature + quality |
| 阶段 | Phase 11 |
| 优先级 | P0 |
| 状态 | pending |
| 触发来源 | V2_REMAINING_PLAN.md + Phase10计划 + API测试整改 合并 |

## 背景

2026-05-18 对 `his-rule-engine/docs/` 下的计划文件进行迁移归档，同时逐项代码审查验证所有标记为"已完成"的项目。发现以下项目实际未完成或部分完成，需统一纳入新计划整改。

---

## 未完成项清单

### 任务 1: MonitorService 接入 Redis 持久化 🔴

**来源**: V2_REMAINING_PLAN G-03 + Phase10 任务1  
**当前状态**: ❌ 未完成  
**优先级**: P1  

**代码现状**:
- [MonitorService.java](../../his-rule-engine/his-monitor-service/src/main/java/com/his/monitor/service/MonitorService.java) 仍使用 `ConcurrentHashMap` + `AtomicLong` + `Vector` 内存存储
- [MonitorServiceApplication.java](../../his-rule-engine/his-monitor-service/src/main/java/com/his/monitor/MonitorServiceApplication.java) 显式排除 Redis: `@SpringBootApplication(exclude = {RedisAutoConfiguration.class})`
- 代码注释: `// 内存中的指标存储（生产环境建议用 Redis）`

**修复方案**:
1. 移除 `RedisAutoConfiguration.class` 排除
2. 在 `his-monitor-service/pom.xml` 添加 `spring-boot-starter-data-redis` 依赖
3. 创建 `RedisMetricsRepository` 类，使用 `StringRedisTemplate` 持久化指标
4. 实现双层架构: 内存缓存(读) + Redis持久化(写)
5. 添加 `@ConditionalOnProperty` 配置开关，支持 Redis 不可用时降级到纯内存模式
6. 启动时从 Redis 恢复指标数据

**涉及文件**:
- `his-monitor-service/pom.xml`
- `his-monitor-service/src/main/java/com/his/monitor/MonitorServiceApplication.java`
- `his-monitor-service/src/main/java/com/his/monitor/service/MonitorService.java` (新增)
- `his-monitor-service/src/main/java/com/his/monitor/repository/RedisMetricsRepository.java` (新增)
- `his-monitor-service/src/main/resources/application.yml`

---

### 任务 2: 子流程节点递归执行实现 🔴

**来源**: V2_REMAINING_PLAN M-04 + Phase10 任务2  
**当前状态**: ❌ 未完成（框架存在但实际跳过）  
**优先级**: P0  

**代码现状**:
- [RuleFlowEngine.java#L303-304](../../his-rule-engine/his-rule-service/src/main/java/com/his/rule/engine/RuleFlowEngine.java):
  ```java
  log.warn("子流程执行需要 RuleFlowService 注入，当前简化为跳过");
  return fact;
  ```
- 递归深度保护已实现（5层限制），但子流程实际不执行

**修复方案**:
1. 在 `RuleFlowEngine` 中注入 `RuleFlowService`（使用 `@Lazy` 解决循环依赖）
2. 从数据库加载子流程定义（通过 `subFlowId` 查询 `RuleFlow`）
3. 解析 `FlowDefinitionDTO` 并递归调用 `execute()` 方法
4. 子流程执行结果合并到主流程 fact 中
5. 保持递归深度保护（5层限制）

**涉及文件**:
- `his-rule-service/src/main/java/com/his/rule/engine/RuleFlowEngine.java`

---

### 任务 3: formulaHitRate 硬编码修复 🟡

**来源**: V2_REMAINING_PLAN Q-01 + Phase10 任务3  
**当前状态**: ⚠️ 部分完成（有真实计算但无数据时硬编码 99.2）  
**优先级**: P1  

**代码现状**:
- [MonitorService.java#L175](../../his-rule-engine/his-monitor-service/src/main/java/com/his/monitor/service/MonitorService.java):
  ```java
  vo.setFormulaHitRate(BigDecimal.valueOf(99.2).setScale(2, RoundingMode.HALF_UP)); // 默认值
  ```
- 当 `formulaTotal > 0` 时有真实计算逻辑，但 `formulaTotal == 0` 时返回硬编码 99.2

**修复方案**:
1. 当 `formulaTotal == 0` 时返回 `BigDecimal.ZERO` 而非 99.2
2. 前端对 0 值显示"暂无数据"

**涉及文件**:
- `his-monitor-service/src/main/java/com/his/monitor/service/MonitorService.java`

---

### 任务 4: 后端单元测试补充 🟡

**来源**: V2_REMAINING_PLAN Q-03 + Phase10 任务4  
**当前状态**: ⚠️ 不足（仅6个测试文件）  
**优先级**: P2  

**当前测试文件**:
| # | 文件路径 | 模块 |
|---|---------|------|
| 1 | RuleFlowServiceTest.java | his-rule-service |
| 2 | MonitorServiceTest.java | his-monitor-service |
| 3 | DroolsHelperTest.java | his-common-drools |
| 4 | AviatorHelperTest.java | his-common-aviator |
| 5 | SettlementServiceTest.java | his-settlement-service |
| 6 | AviatorFormulaTest.java | his-formula-service |

**缺失测试模块**:
- RuleDefinitionControllerTest
- RuleGroupControllerTest
- AuditLogControllerTest
- FormulaControllerTest
- DrugControllerTest
- QualityControllerTest
- DrgControllerTest
- TemplateSecurityScannerTest
- NotificationServiceTest
- AlertRuleServiceTest

**修复方案**: 逐步补充，优先 Controller 层集成测试

---

### 任务 5: 前端组件测试 🟡

**来源**: V2_REMAINING_PLAN Q-04 + Phase10 任务5  
**当前状态**: ⚠️ 部分完成（V2_REMAINING_PLAN 标记已完成，但 Phase10 标记未完成）  
**优先级**: P2  

**需验证**: V2_REMAINING_PLAN 声称有 21 个 Vitest 测试通过，需确认实际文件是否存在

---

### 任务 6: API 测试整改收尾 🟡

**来源**: API测试整改计划 (PLAN-2026-05-18-001)  
**当前状态**: ⚠️ 部分完成  
**优先级**: P2  

**未完成项**:
- Task 4: QualityController 存在性校验（标记 IN_PROGRESS，需确认是否已修复）
- Task 5: 编译验证
- Task 6: 回归测试（96 用例 100% 通过）

---

## 优先级排序

| 优先级 | 任务 | 理由 |
|:------:|------|------|
| **P0** | 任务2: 子流程递归执行 | 核心功能缺失，规则流子流程节点不执行 |
| **P1** | 任务1: MonitorService Redis持久化 | 数据丢失风险，重启后指标清零 |
| **P1** | 任务3: formulaHitRate硬编码 | 数据不准确，误导用户 |
| **P2** | 任务4: 后端单元测试 | 质量保障，可逐步补充 |
| **P2** | 任务5: 前端组件测试 | 质量保障，需先验证现状 |
| **P2** | 任务6: API测试整改收尾 | 验证已有修复的完整性 |

## 验收标准

- [ ] 子流程节点可正常递归执行（最大5层深度保护）
- [ ] MonitorService 支持 Redis 持久化，重启后数据不丢失
- [ ] Redis 不可用时自动降级到内存模式
- [ ] formulaHitRate 无数据时返回 0，不硬编码假数据
- [ ] 后端单元测试新增 ≥10 个测试类
- [ ] 前端组件测试文件确认存在并通过
- [ ] API 回归测试 96 用例 100% 通过

---

*文档创建日期: 2026-05-18*
*合并来源: V2_REMAINING_PLAN.md + Phase10计划 + API测试整改计划*
