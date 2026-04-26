# Commands - 自定义快捷命令

## 说明
自定义可复用的命令，格式：`((command:名称))`

## 命令列表

| 命令 | 文件 | 说明 |
|------|------|------|
| `((command:health))` | health-check.md | 检查开发环境健康状态（JDK/Maven/Nacos） |
| `((command:context))` | show-context.md | 显示当前任务上下文（HIS 规则引擎） |
| `((command:status))` | project-status.md | 项目整体状态总览 |
| `((command:analyze-task))` | analyze-task.md | 分析任务并规划实现（含阻塞节点检查） |
| `((command:check-memory))` | check-memory.md | 检查长期记忆状态 |
| `((command:summarize))` | summarize.md | 总结当前会话并回写知识 |

## HIS 规则引擎特定命令

| 命令 | 说明 |
|------|------|
| `((command:rules))` | 列出 DRL 规则文件清单 |
| `((command:formulas))` | 列出 Aviator 公式定义 |
| `((command:apis))` | 列出 REST API 端点清单 |

## 使用方式

在对话中直接输入：
```
((command:health))
((command:context))
((command:status))
```

## 命名规范

- 文件名使用 kebab-case
- 描述使用中文
- 包含执行步骤和预期输出
- 必须适配 HIS 规则引擎技术栈

---

最后更新: 2026-04-26 | HIS Drools+Aviator 规则引擎版
