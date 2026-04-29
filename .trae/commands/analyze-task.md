# ((command:analyze-task)) 分析任务 - HIS 规则引擎

## 执行步骤

当用户提供一个新任务时：

1. **识别类型** — 问答/配置/规则开发/公式编写/测试/文档
2. **检查规范** — 是否有对应 rules/
3. **检查上下文** — MEMORY.md 中是否有相关信息
4. **规划步骤** — 按 workflow.md 执行（注意 3 个阻塞节点）
5. **输出分析** — 告诉用户准备怎么做

## 模板

```
## 任务分析

### 任务类型
[问答/配置/规则开发/公式编写/测试/文档]

### 涉及范围
- 文件 A (Java/DRL/Aviator)
- 配置 B (Nacos/application.yml)
- 操作 C

### HIS 规则引擎特定检查
- [ ] 是否涉及金额计算（必须使用 BigDecimal）
- [ ] 是否涉及 Aviator 公式（必须经过语法校验）
- [ ] 是否涉及 DRL 规则（需要 KieBase 分组）
- [ ] 是否涉及数据库操作（MyBatis #{} 参数绑定）

### 规范检查
- [x] rules/code-style.md (Java/Drools/Aviator 规范)
- [ ] rules/security.md (SQL注入/表达式注入防护)
- [ ] rules/workflow.md (7步流程+阻塞节点)

### 执行计划
1. 步骤1
2. 步骤2
3. ...

### 需要确认
- [ ] 确认点1（★ 阻塞节点）
- [ ] 确认点2
```

## 使用

```
((command:analyze-task))
```
