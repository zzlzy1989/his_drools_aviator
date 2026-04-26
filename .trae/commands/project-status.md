# ((command:status)) 项目状态总览 - HIS 规则引擎

## 执行步骤

1. 检查 .trae/ 目录完整性
2. 读取 agent.md 和 MEMORY.md
3. 检查 MEMORY.md 最后更新时间
4. 检查 pom.xml 依赖状态
5. 输出汇总报告

## 预期输出

```
## HIS 规则引擎项目状态

### 基本信息
- 项目: HIS Drools+Aviator 规则引擎 v1.0
- 技术栈: Java 17+ / Spring Boot 3.5.x / Drools 8.44 / Aviator 5.2.6
- 构建工具: Maven 3.9+

### Harness 配置
- agent.md: ✅ (v2.0 HIS版)
- MEMORY.md: ✅ (v2.1 HIS版)
- rules/: ✅ 10 个规则文件
- domain/: ✅ 6 个知识文件
- hooks/: ✅ 钩子脚本

### 开发环境
- JDK: 17+
- Maven: 3.9+
- Nacos: 2.2.0+ (配置中心)
- MySQL: 8.0+ (可选)

### 系统状态
- ✅ 正常运行
```

## 使用

```
((command:status))
```
