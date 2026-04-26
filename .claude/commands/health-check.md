# ((command:health)) 健康检查 - HIS 规则引擎

## 执行步骤

1. 检查 MEMORY.md 是否存在
2. 检查 .claude/ 目录结构
3. 检查 pom.xml 和依赖
4. 检查 JDK 版本
5. 验证必要文件完整性

## 预期输出

```
✅ MEMORY.md: 存在，最后更新 2026-04-26
✅ .claude/rules/: 10 个规则文件
✅ .claude/domain/: 6 个知识文件
✅ pom.xml: 存在
⚠️  JDK 版本: 需确认 ≥17
⚠️  Maven: 需确认 ≥3.9
```

## HIS 规则引擎特定检查

1. 检查 src/main/resources/rules/ 目录是否存在（DRL 规则文件）
2. 检查 pom.xml 中 drools-core 和 aviator 依赖版本是否正确
3. 检查 bootstrap.yml 中 Nacos 配置是否正确
4. 检查 application.yml 中数据库连接配置

## 使用

```
((command:health))
```
