# ((command:summarize)) 总结会话 - HIS 规则引擎

## 执行步骤

1. 读取当前会话记录
2. 提取关键决策、配置、路径
3. 更新 MEMORY.md

## 模板

```
## 会话总结 — YYYY-MM-DD HH:mm

### 完成事项
- [x] 任务1
- [x] 任务2

### 关键决策
- 决策1 → 结果
- 决策2 → 结果

### 新学到的路径
- 路径1: src/main/java/com/his/rule/engine/...
- 路径2: src/main/resources/rules/...

### 待跟进
- [ ] 待办1
- [ ] 待办2

### 沉淀到 MEMORY.md / domain/
- [x] 已更新 glossary.md (新术语)
- [x] 已更新 decisions.md (新决策)
```

## HIS 规则引擎特定记录

- 新增 DRL 规则文件路径
- 新增 Aviator 公式定义
- Nacos 配置变更
- Drools KieBase 分组调整
- 结算逻辑变更

## 使用场景

- 会话结束前
- 切换任务前
- 每日结束时

## 使用

```
((command:summarize))
```
