---
alwaysApply: true
trigger: command:plan
---
# ((command:plan)) - 计划管理命令

## 触发方式
在对话中输入 `((command:plan))` 或 `((command:plan <参数>))`

## 功能
管理 workflow-plans 目录下的计划文件，支持创建、更新、查看、列表操作。

## 使用方法

### 1. 查看所有活跃计划
```
((command:plan list))
```

### 2. 创建新计划
```
((command:plan create <标题> <类型> <阶段> <负责人> <优先级>))
```

**参数说明**:
- 标题: 任务标题（必填）
- 类型: feature|bugfix|refactor|config|docs|test|deploy|research（默认: feature）
- 阶段: Phase 1~5（默认: Phase 1）
- 负责人: 开发者名称（默认: developer）
- 优先级: P0|P1|P2|P3（默认: P1）

**示例**:
```
((command:plan create 实现医保报销规则引擎 feature Phase 2 张三 P0))
```

### 3. 更新计划状态
```
((command:plan update <文件名> <新状态> <备注>))
```

**状态值**: pending|in_progress|completed|cancelled

**示例**:
```
((command:plan update 2026-04-26-feature-reimburse-rule-engine.md in_progress 开始开发))
```

### 4. 查看计划详情
```
((command:plan show <文件名>))
```

**示例**:
```
((command:plan show 2026-04-26-feature-reimburse-rule-engine.md))
```

### 5. 更新索引
```
((command:plan reindex))
```

## 执行逻辑

AI 收到此命令后：
1. 解析命令参数
2. 调用 `.trae/workflow-plans/plan.sh` 脚本（如可用）
3. 或直接通过文件操作实现对应功能
4. 返回执行结果

## 注意事项

- 由于 Trae 系统限制，`.trae` 目录下文件不能通过 Bash 操作
- AI 应使用 Write 工具直接创建/修改计划文件
- 创建计划后自动更新 INDEX.md 索引
