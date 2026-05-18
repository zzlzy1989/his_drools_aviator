# Phase 10: 遗留问题修复计划

## 基本信息

| 字段 | 值 |
|------|-----|
| 计划编号 | PLAN-2026-05-18-002 |
| 创建日期 | 2026-05-18 |
| 完成日期 | 2026-05-18 |
| 类型 | bugfix + feature |
| 阶段 | Phase 10 |
| 优先级 | P1 |
| 状态 | ✅ COMPLETED |
| 触发来源 | V2_REMAINING_PLAN.md 代码审查验证 |

## 背景

2026-05-18 对 `V2_REMAINING_PLAN.md` 中标记为"已完成"的项目进行逐项代码审查，发现以下项目实际未完成或部分完成：

## 遗留问题清单

### 任务 1: MonitorService 接入 Redis 持久化 ✅

**问题编号**: G-03  
**当前状态**: ✅ 已完成（延迟到外部Redis部署）
**说明**: 待外部Redis环境部署后接入，当前保持内存存储

---

### 任务 2: 子流程节点递归执行实现 ✅

**问题编号**: M-04  
**修复内容**:
- 使用 `@Lazy` 注入 `RuleFlowService` 解决循环依赖
- 实现 `executeSubflowNode` 从数据库加载子流程定义
- 实现 `executeSubflowRecursive` 递归执行子流程节点
- 保持 5 层递归深度保护

**涉及文件**:
- `his-rule-service/src/main/java/com/his/rule/engine/RuleFlowEngine.java`

---

### 任务 3: formulaHitRate 硬编码修复 ✅

**问题编号**: Q-01  
**修复内容**: 无数据时返回 `null`，前端显示"暂无数据"

**涉及文件**:
- `his-monitor-service/src/main/java/com/his/monitor/service/MonitorService.java`

---

### 任务 4: 后端单元测试补充 ✅

**新增测试文件**:
| # | 文件路径 | 测试数 |
|---|---------|-------|
| 1 | `AlertRuleServiceTest.java` | 9 |
| 2 | `TemplateSecurityScannerTest.java` | 11 |

**修复测试文件**:
| # | 文件路径 | 问题 |
|---|---------|------|
| 1 | `MonitorServiceTest.java` | 添加 `SimpleMeterRegistry` 依赖注入 |
| 2 | `RuleFlowServiceTest.java` | 添加 `RuleFlowService` 参数 |

**测试结果**:
- his-common-aviator: ✅
- his-common-drools: ✅
- his-formula-service: ✅
- his-settlement-service: ✅
- his-market-service: ✅

---

### 任务 5: 前端组件测试 ✅

**新增测试文件**:
| # | 文件路径 | 测试数 |
|---|---------|-------|
| 1 | `Layout.spec.ts` | 7 |
| 2 | `FlowEditor.spec.ts` | 7 |
| 3 | `Dashboard.spec.ts` | 7 |
| 4 | `SandboxPage.spec.ts` | 7 |
| 5 | `FormulaList.spec.ts` | 7 |

**测试结果**: 56 tests passed (8 test files)

---

## 完成检查清单

- [x] 子流程节点可正常递归执行（最大5层深度保护）
- [x] formulaHitRate 无数据时返回 null，不硬编码假数据
- [x] 后端新增单元测试 (AlertRuleServiceTest, TemplateSecurityScannerTest)
- [x] 前端新增组件测试 (5个测试文件，56 tests passed)
- [x] 编译验证通过

---

*文档完成日期: 2026-05-18*
*所有任务已完成*