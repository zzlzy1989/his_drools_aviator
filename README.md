# HIS 规则剥离引擎 - 基于 Spring Cloud Alibaba + Nacos

## 📖 项目简介

本项目是一个**医疗业务规则剥离引擎**，旨在将 HIS 系统中高频变化的业务规则（医保报销、质控、合理用药、DRG 分组等）从核心系统中抽离，封装为可独立热加载的规则单元。通过 **Drools + Aviator** 混合架构，实现规则的灵活编排和高性能计算，并通过 **Nacos** 配置中心实现公式的实时动态刷新。

**核心价值**：  
- 医院可自主维护业务规则，不再依赖 HIS 厂商的代码修改周期  
- 规则变更从“2~4周”缩短至“分钟级”  
- 底层 HIS 退化为稳定的事务底座，上层业务逻辑灵活可控  

---

## 🛠️ 技术栈

| 组件 | 版本 | 用途 |
|:---|:---|:---|
| Spring Boot | 3.5.x | 基础框架 |
| Spring Cloud Alibaba | 2025.1.0.0 | 微服务生态（Nacos 配置中心） |
| Nacos Client (配置中心) | 2.5.0 | 配置管理（存储公式/规则参数），与服务器版本独立演进 |
| Nacos | 3.2.0 | 配置中心（存储公式/规则参数） |
| Drools | 8.44.0.Final | 规则引擎（规则流编排） |
| Aviator | 5.2.6 | 高性能表达式求值（负责公式计算） |
| Caffeine | 3.2.3 | 本地缓存（缓存编译后的表达式） |
| MySQL | 8.0+ | 存储规则定义、事实数据（可选） |
| Maven | 3.9+ | 构建工具 |

---

## 📦 环境要求

- JDK 11 或更高版本  
- Maven 3.9+    
- Nacos Server 2.2.0（[下载地址](https://github.com/alibaba/nacos/releases)）  
- MySQL 8.0（仅当需要持久化规则库时）  
- Git  

---

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/zzlzy1989/his_drools_aviator.git
cd his_drools_aviator
```

### 2. 启动 Nacos Server

```bash
# 解压并启动
unzip nacos-server-3.2.0.zip
cd nacos/bin
# Linux/Mac
sh startup.sh -m standalone
# Windows
startup.cmd -m standalone
```

访问 Nacos 控制台：http://localhost:8848/nacos （默认账号密码：nacos/nacos）

### 3. 创建 Nacos 配置

在 Nacos 中创建 Data ID 为 `his-rule-engine.yaml`，Group 为 `HIS_RULE_GROUP` 的配置，内容如下：

```yaml
formulas:
  reimburse:
    resident: "round((totalFee - 500) * 0.65, 2)"
    employee: "round((totalFee - 1000) * 0.85, 2)"
  drg:
    weight: "(baseWeight + extraPoints) * severityFactor"
```

### 4. 配置数据库（可选）

如需要持久化规则操作日志或规则元数据，请创建 MySQL 数据库并执行以下脚本：

```sql
CREATE DATABASE `his_rule_engine` CHARACTER SET utf8mb4;

USE `his_rule_engine`;

CREATE TABLE `rule_formula` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rule_key` varchar(128) NOT NULL COMMENT '规则标识',
  `formula_text` text NOT NULL COMMENT '公式表达式',
  `version` int DEFAULT '1',
  `status` tinyint DEFAULT '1' COMMENT '1-生效 0-失效',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_key` (`rule_key`)
);
```

修改 `application.yml` 中的数据库连接参数。

### 5. 修改应用配置

编辑 `src/main/resources/bootstrap.yml`：

```yaml
spring:
  application:
    name: his-rule-engine
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yaml
        group: HIS_RULE_GROUP
        namespace: public   # 如使用命名空间则填写
  profiles:
    active: dev
```

### 6. 编译与运行

```bash
mvn clean package
java -jar target/his-rule-engine-1.0.0.jar
```

### 7. 测试规则调用

```bash
# 结算测试（示例）
curl -X POST http://localhost:8080/api/settlement \
  -H "Content-Type: application/json" \
  -d '{"patientType":"resident","totalFee":2000}'
```

预期返回：`{"finalAmount":975.00}` （因公式 `round((2000-500)*0.65,2)` = 975）

---

## 📁 项目模块结构

```
his-rule-engine/
├── src/main/java/com/his/rule
│   ├── config/                 # Spring 配置（Drools、缓存、Aviator）
│   ├── engine/                 # 规则引擎核心
│   │   ├── drools/             # Drools 会话管理、规则加载
│   │   ├── aviator/            # Aviator 表达式缓存与执行器
│   │   └── formula/            # 公式管理器（Nacos + 本地缓存）
│   ├── fact/                   # 事实对象定义（SettlementFact、OrderFact等）
│   ├── listener/               # Nacos 配置变更监听器
│   ├── service/                # 业务服务（结算、质控等）
│   ├── controller/             # REST API
│   └── validator/              # 公式校验工具
├── src/main/resources/
│   ├── rules/                  # Drools 规则文件（.drl）
│   │   └── reimbursement.drl   # 医保报销规则流
│   ├── bootstrap.yml           # Nacos 配置
│   ├── application.yml         # 通用配置（端口、数据库等）
│   └── logback-spring.xml      # 日志配置
└── pom.xml
```

---

## ⚙️ 核心配置说明

### Nacos 配置刷新

- 所有公式均通过 `@RefreshScope` + `NacosConfigManager` 实现动态更新  
- 修改 Nacos 中的 `formulas.*` 配置后，应用会自动重新加载公式并刷新 Aviator 缓存，**无需重启**

### Drools 规则热加载

- 规则文件放在 `src/main/resources/rules/` 下  
- 生产环境建议将 `.drl` 文件也存放在 Nacos 或数据库中，通过 `KieScanner` 实现热部署（本示例默认从 classpath 加载）

### Aviator 表达式缓存

- 使用 Caffeine 缓存编译后的 `Expression` 对象  
- 缓存大小：1000 条，过期时间：30 分钟  
- 公式变更时自动失效对应的缓存条目

---

## 🧪 示例：医保结算规则流

### 事实对象（Fact）

```java
public class SettlementFact {
    private String patientType;      // resident / employee
    private BigDecimal totalFee;
    private BigDecimal deductible;
    private BigDecimal ratio;
    private BigDecimal finalAmount;
    // getters/setters
}
```

### Drools 规则文件（reimbursement.drl）

```drools
import com.his.rule.fact.SettlementFact;
import com.his.rule.engine.aviator.AviatorHelper;

global NacosFormulaManager formulaManager;
global AviatorHelper aviatorHelper;

rule "Determine Deductible"
    when
        $f: SettlementFact(patientType == "resident")
    then
        $f.setDeductible(new BigDecimal("500"));
        update($f);
end

rule "Calculate Reimbursement"
    when
        $f: SettlementFact(deductible != null, totalFee != null)
    then
        String formula = formulaManager.getFormula("reimburse.resident");
        BigDecimal amount = aviatorHelper.executeFormula(formula, $f);
        $f.setFinalAmount(amount);
        update($f);
end
```

---

## 📊 性能指标（参考）

| 场景 | 耗时 |
| :--- | :--- |
| 单条规则匹配（Drools） | < 5ms |
| Aviator 表达式执行（已缓存） | < 0.5ms |
| Nacos 配置变更到生效（含缓存刷新） | < 2s |
| 规则引擎冷启动（加载 100 条规则） | ~800ms |

---

## 🧰 常见问题

**Q：Nacos 配置不生效？**  
A：检查 `bootstrap.yml` 中的 `spring.cloud.nacos.config` 是否正确，并确保应用已添加 `@RefreshScope`。

**Q：Aviator 表达式报错 `Unknown variable`？**  
A：请在 `FormulaValidator` 中定义允许的变量白名单，或在执行前将所需变量全部放入 `env` 中。

**Q：如何发布新的规则文件？**  
A：本示例从 classpath 加载 `.drl` 文件，重新打包即可。生产环境建议将规则文件放入 Nacos 或数据库，通过 `KieScanner` 动态加载。

**Q：支持多租户（多个医院）吗？**  
A：可在 Nacos 中使用不同的 `namespace` 或 `group` 来隔离医院的配置；Drools 会话也可按租户独立构建。

---

## 🔧 扩展开发指南

1. **新增一个业务规则集**  
   - 定义新的事实对象（Fact）  
   - 编写对应的 `.drl` 文件  
   - 在 Nacos 中添加公式配置  
   - 实现业务服务调用规则引擎

2. **接入自己的数据库作为规则源**  
   - 实现 `RuleProvider` 接口，从数据库读取规则内容  
   - 移除 `bootstrap.yml` 中的 Nacos 依赖（可选）

3. **集成监控**  
   - 暴露 `/actuator/health` 端点  
   - 记录每次规则调用的耗时和命中率（Aviator 缓存统计）

---

## 📄 许可证

Apache License 2.0

---

## 👥 贡献者

欢迎提交 Issue 或 Pull Request。

**项目维护者**：HIS 创新实验室

---

## 🔗 相关文档

- [Spring Cloud Alibaba 官方文档](https://sca.aliyun.com/docs/)
- [Nacos 配置管理](https://nacos.io/zh-cn/docs/configuration-management.html)
- [Drools 用户手册](https://docs.drools.org/)
- [Aviator 表达式引擎指南](https://github.com/killme2008/aviator)