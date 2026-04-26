# ((command:context)) 显示任务上下文 - HIS 规则引擎

## 执行步骤

1. 读取 .claude/agent.md 中的项目概述
2. 读取 .claude/MEMORY.md 中的最近记录
3. 汇总当前任务状态

## 预期输出

```
## 当前任务上下文

### 项目信息
- 项目: HIS Drools+Aviator 规则引擎（医疗业务规则剥离引擎）
- 技术栈: Java 17+ / Spring Boot 3.x / Drools 8.x / Aviator 5.x
- 配置中心: Nacos 2.5.0 / 3.2.0

### 进行中的任务
- 任务1: ...
- 任务2: ...

### 最近记忆
- 2026-04-26: 完成 Harness 工程迁移
- ...

### 下一步
- [ ] 待办1
- [ ] 待办2

### 快速参考
- 命令: mvn clean package / mvn test
- API 路径: /api/v1/rules/*, /api/v1/settlement/*
- DRL 路径: src/main/resources/rules/
```

## 使用

```
((command:context))
```
