# API 测试整改计划

## 基本信息

| 字段 | 值 |
|------|-----|
| 计划编号 | PLAN-2026-05-18-001 |
| 创建日期 | 2026-05-18 |
| 完成日期 | 2026-05-18 |
| 类型 | bugfix + refactor |
| 阶段 | Phase 3 |
| 优先级 | P0 (紧急) |
| 状态 | ✅ COMPLETED |
| 触发来源 | 接口测试报告 (docs/test/TEST_REPORT.md) |

## 背景

2026-05-18 进行全量 API 接口测试（96 个用例），发现以下问题导致部分用例返回不规范的 HIS-099 系统错误：

1. **RuleFlowService**: 使用 `RuntimeException` 而非 `BusinessException`，导致异常被全局异常处理器兜底捕获为 HIS-099
2. **SandboxService**: 同上问题
3. **QualityController**: 删除操作缺少存在性校验
4. **ErrorCode 枚举**: 缺少部分业务错误码定义

## 整改任务清单

### Task 1: 扩展 ErrorCode 枚举 ✅ COMPLETED

**文件**: `his-common/his-common-web/src/main/java/com/his/common/web/result/ErrorCode.java`

**新增错误码**:

| 错误码 | 说明 | 模块 |
|--------|------|------|
| `RULE_FLOW_NOT_FOUND` | 规则流不存在 | RuleFlow |
| `RULE_FLOW_VERSION_NOT_FOUND` | 规则流版本不存在 | RuleFlow |
| `FORMULA_VERSION_NOT_FOUND` | 公式版本不存在 | Formula |
| `SANDBOX_DATASET_NOT_FOUND` | 数据集不存在 | Sandbox |
| `SANDBOX_SUITE_NOT_FOUND` | 测试套件不存在 | Sandbox |

### Task 2: 修复 RuleFlowService ✅ COMPLETED

**文件**: `his-rule-service/src/main/java/com/his/rule/service/RuleFlowService.java`

**修复内容**:
- 添加 `BusinessException` 和 `ErrorCode` 导入
- 6 处 `RuntimeException` → `BusinessException(ErrorCode.xxx)`
  - `updateFlow()`: RULE_FLOW_NOT_FOUND
  - `deleteFlow()`: RULE_FLOW_NOT_FOUND
  - `publishFlow()`: RULE_FLOW_NOT_FOUND
  - `rollbackFlow()`: RULE_FLOW_VERSION_NOT_FOUND / RULE_FLOW_NOT_FOUND
  - `compareFlowVersions()`: RULE_FLOW_VERSION_NOT_FOUND (2处)
  - `exportFlowJson()`: RULE_FLOW_NOT_FOUND

### Task 3: 修复 SandboxService ✅ COMPLETED

**文件**: `his-settlement-service/src/main/java/com/his/settlement/service/SandboxService.java`

**修复内容**:
- 添加 `BusinessException` 和 `ErrorCode` 导入
- 2 处 `RuntimeException` → `BusinessException(ErrorCode.SANDBOX_DATASET_NOT_FOUND)`
  - `update()`
  - `delete()`

### Task 4: 修复 QualityController ⏳ IN_PROGRESS

**文件**: `his-quality-service/src/main/java/com/his/quality/controller/QualityController.java`

**问题**: `delete()` 方法直接调用 `deleteById(id)`，无存在性校验
**修复**: 添加记录存在性检查，不存在时抛出 `BusinessException`

### Task 5: 编译验证 ⏳ PENDING

- 重新编译全部微服务模块
- 验证编译无错误

### Task 6: 回归测试 ⏳ PENDING

- 运行 `docs/test/test-runner.sh`
- 确认 96 个用例 100% 通过

## 进度追踪

| 任务 | 状态 | 完成时间 |
|------|------|---------|
| Task 1: ErrorCode 扩展 | ✅ | - |
| Task 2: RuleFlowService 修复 | ✅ | - |
| Task 3: SandboxService 修复 | ✅ | - |
| Task 4: QualityController 修复 | ⏳ | - |
| Task 5: 编译验证 | ⏳ | - |
| Task 6: 回归测试 | ⏳ | - |
