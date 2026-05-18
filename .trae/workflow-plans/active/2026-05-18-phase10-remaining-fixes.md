# Phase 10: 遗留问题修复计划

## 基本信息

| 字段 | 值 |
|------|-----|
| 计划编号 | PLAN-2026-05-18-002 |
| 创建日期 | 2026-05-18 |
| 类型 | bugfix + feature |
| 阶段 | Phase 10 |
| 优先级 | P1 |
| 状态 | pending |
| 触发来源 | V2_REMAINING_PLAN.md 代码审查验证 |

## 背景

2026-05-18 对 `V2_REMAINING_PLAN.md` 中标记为"已完成"的项目进行逐项代码审查，发现以下项目实际未完成或部分完成：

## 遗留问题清单

### 任务 1: MonitorService 接入 Redis 持久化 ⏳

**问题编号**: G-03  
**当前状态**: ⚠️ 部分完成（仍使用 ConcurrentHashMap，未接入 Redis）  
**影响**: MonitorService 重启后所有指标数据丢失  

**当前实现**:
- `ConcurrentHashMap` 存储 `ruleHitCounts`
- `AtomicLong` 存储各类计数器
- `Vector` 存储 `recentAlerts`
- 代码注释：`// 内存中的指标存储（生产环境建议用 Redis）`

**修复方案**:
1. 引入 `spring-boot-starter-data-redis` 依赖
2. 将指标存储改为 `RedisTemplate<String, String>` 或 `StringRedisTemplate`
3. 保持内存缓存 + Redis 持久化双层架构
4. 支持 Redis 不可用时降级到内存模式

**涉及文件**:
- `his-monitor-service/pom.xml`
- `his-monitor-service/src/main/java/com/his/monitor/service/MonitorService.java`
- `his-monitor-service/src/main/resources/application.yml`

**预估工作量**: 2-3天

---

### 任务 2: 子流程节点递归执行实现 ⏳

**问题编号**: M-04  
**当前状态**: ⚠️ 部分完成（有框架但实际"跳过"占位）  
**影响**: 规则流中子流程节点不执行任何操作  

**当前实现** (`RuleFlowEngine.java#L303-304`):
```java
log.warn("子流程执行需要 RuleFlowService 注入，当前简化为跳过");
return fact;
```

**修复方案**:
1. 在 `RuleFlowEngine` 中注入 `RuleFlowService`（需解决循环依赖，可使用 `@Lazy`）
2. 从数据库加载子流程定义（通过 `subflowId` 查询 `RuleFlow`）
3. 解析 `FlowDefinitionDTO` 并递归调用 `execute()` 方法
4. 已有递归深度保护（5层限制），保持不变
5. 子流程执行结果合并到主流程 fact 中

**涉及文件**:
- `his-rule-service/src/main/java/com/his/rule/engine/RuleFlowEngine.java`
- `his-rule-service/src/main/java/com/his/rule/service/RuleFlowService.java`

**预估工作量**: 1-2天

---

### 任务 3: formulaHitRate 硬编码修复 ⏳

**问题编号**: Q-01  
**当前状态**: ⚠️ 部分完成（有真实计算但无数据时硬编码 99.2）  
**影响**: 监控看板公式命中率始终显示 99.2%，误导用户  

**当前实现** (`MonitorService.java#L175`):
```java
vo.setFormulaHitRate(BigDecimal.valueOf(99.2).setScale(2, RoundingMode.HALF_UP)); // 默认值
```

**修复方案**:
1. 当 `formulaHits == 0` 时返回 `BigDecimal.ZERO` 或 `null`
2. 前端显示"暂无数据"而非硬编码数值
3. 或者返回真实计算的命中率（公式命中次数 / 总执行次数）

**涉及文件**:
- `his-monitor-service/src/main/java/com/his/monitor/service/MonitorService.java`

**预估工作量**: 0.5天

---

### 任务 4: 后端单元测试补充 ⏳

**问题编号**: Q-03  
**当前状态**: ⚠️ 测试文件数量不足（仅6个测试文件）  
**影响**: 测试覆盖率远低于规范要求  

**规范要求**:
- 核心规则 ≥90%
- 工具类 ≥80%
- Controller ≥70%
- Skill ≥85%

**当前测试文件**:
| # | 文件路径 | 模块 |
|---|---------|------|
| 1 | `RuleFlowServiceTest.java` | his-rule-service |
| 2 | `MonitorServiceTest.java` | his-monitor-service |
| 3 | `DroolsHelperTest.java` | his-common-drools |
| 4 | `AviatorHelperTest.java` | his-common-aviator |
| 5 | `SettlementServiceTest.java` | his-settlement-service |
| 6 | `AviatorFormulaTest.java` | his-formula-service |

**缺失测试模块**:
- `RuleDefinitionControllerTest`
- `RuleGroupControllerTest`
- `AuditLogControllerTest`
- `FormulaControllerTest`
- `DrugControllerTest`
- `QualityControllerTest`
- `DrgControllerTest`
- `TemplateSecurityScannerTest`
- `NotificationServiceTest`
- `AlertRuleServiceTest`

**修复方案**:
1. 优先为所有 Controller 编写集成测试（MockMvc）
2. 为核心 Service 编写单元测试
3. 目标：新增至少 15 个测试类

**涉及文件**: 各服务 `src/test/` 目录

**预估工作量**: 5-7天

---

### 任务 5: 前端组件测试 ⏳

**问题编号**: Q-04  
**当前状态**: ❌ 未完成（无任何 .spec.ts 文件）  
**影响**: 前端代码变更无自动化测试保障  

**当前状态**: 0 个前端测试文件

**修复方案**:
1. 配置 Vitest 或 Jest 测试框架
2. 为核心组件编写单元测试：
   - `Layout.vue` (侧边栏导航)
   - `FlowEditor.vue` (规则流编辑器)
   - `Dashboard.vue` (监控大屏)
   - `SandboxList.vue` (测试沙箱列表)
3. 目标：新增至少 5 个前端测试文件

**涉及文件**: `his-rule-engine-web/src/` 目录

**预估工作量**: 3-5天

---

## 优先级排序

| 优先级 | 任务 | 理由 |
|:------:|------|------|
| P0 | 任务2: 子流程递归执行 | 核心功能缺失，影响规则流执行 |
| P1 | 任务1: MonitorService Redis持久化 | 数据丢失风险 |
| P1 | 任务3: formulaHitRate硬编码 | 数据不准确，误导用户 |
| P2 | 任务4: 后端单元测试 | 质量保障，可逐步补充 |
| P2 | 任务5: 前端组件测试 | 质量保障，可逐步补充 |

## 总体工作量评估

| 任务 | 预估工作量 | 优先级 |
|------|-----------|:------:|
| 子流程递归执行 | 1-2天 | P0 |
| MonitorService Redis持久化 | 2-3天 | P1 |
| formulaHitRate修复 | 0.5天 | P1 |
| 后端单元测试 | 5-7天 | P2 |
| 前端组件测试 | 3-5天 | P2 |
| **合计** | **11.5-17.5天** | |

## 验收标准

- [ ] 子流程节点可正常递归执行（最大5层深度保护）
- [ ] MonitorService 支持 Redis 持久化，重启后数据不丢失
- [ ] formulaHitRate 无数据时返回 0 或 null，不硬编码假数据
- [ ] 后端单元测试覆盖所有 Controller（≥70%）
- [ ] 前端至少有 5 个核心组件测试文件

---

*文档创建日期: 2026-05-18*
*基于代码审查结果: V2_REMAINING_PLAN.md 逐项验证*
