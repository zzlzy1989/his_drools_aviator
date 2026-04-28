# ((command:check-memory)) 检查记忆状态 - HIS 规则引擎

## 执行步骤

1. 读取 .trae/MEMORY.md
2. 列出 .trae/memory/ 目录下的所有日记
3. 检查最后更新时间

## 预期输出

```
## 记忆状态

### MEMORY.md
- 最后更新: 2026-04-26
- 记录数: X 条
- 关键路径: src/main/java/com/his/rule/, src/main/resources/rules/

### HIS 规则引擎项目上下文
- 技术栈: Java 17+ / Spring Boot 3.5.x / Drools 8.44 / Aviator 5.2.6
- 核心约束: BigDecimal 强制用于金额、Aviator 公式必须校验、Drools KieBase 分组
- API 规范: /api/v1/* 版本化管理

### 每日日记
- memory/YYYY-MM-DD.md: ...

### 建议
- [ ] 检查是否有未同步的规则变更
- [ ] 考虑更新 domain/ 知识库
- [ ] 更新最新的项目配置
```

## 使用

```
((command:check-memory))
```
