---
alwaysApply: true
description: 生成/修改java文件时，检查编码风格
---
# 编码风格规则 - HIS 动态规则中台

## 触发条件
- 生成/修改 .java 文件
- 编写/修改 .drl 规则文件
- 设计 Aviator 表达式
- 代码审查请求

---

## 一、Java 编码规范 (Spring Boot)

### 1.1 项目包结构

```
com.his.
├── config/          # 配置类 (@Configuration)
├── fact/            # Fact 对象 (POJO, 传给 Drools)
├── skill/           # Agent/Skill 实现 (ISkill 接口)
├── engine/          # 引擎封装 (Drools + Aviator)
├── cache/           # 缓存层 (Caffeine)
├── formula/         # 公式管理 (动态加载/刷新)
├── event/           # 事件定义 (常量/枚举)
├── service/         # 业务服务层 (@Service)
├── controller/      # REST 控制器 (@RestController)
├── common/          # 公共工具 (异常/常量/枚举)
├── dto/             # 数据传输对象
└── mapper/          # MyBatis/JPA 数据访问层
```

### 1.1.1 微服务包结构

```
his-rule-engine/
├── his-common/              # 公共模块
│   ├── his-common-core/     # 核心工具类、枚举、常量
│   ├── his-common-web/      # Web 相关（拦截器、异常处理）
│   └── his-common-mybatis/  # MyBatis 配置
├── his-gateway/             # API 网关
│   └── com.his.gateway/
│       ├── config/          # 网关路由配置
│       ├── filter/          # 全局过滤器
│       └── handler/         # 异常处理
├── his-rule-service/        # 规则管理服务
│   └── com.his.rule/
│       ├── controller/      # 规则 CRUD API
│       ├── service/         # 规则业务逻辑
│       ├── entity/          # 规则实体
│       ├── mapper/          # MyBatis Mapper
│       └── dto/             # 数据传输对象
├── his-settlement-service/  # 结算服务
│   └── com.his.settlement/
│       ├── controller/      # 结算 API
│       ├── service/         # 结算业务逻辑
│       ├── pipeline/        # Skill Pipeline
│       ├── skill/           # Skill 实现
│       ├── fact/            # Fact 对象
│       └── drl/             # DRL 规则文件
├── his-drug-service/        # 合理用药服务
├── his-quality-service/     # 质控服务
└── his-drg-service/         # DRG 分组服务
```

### 1.2 类命名规范

| 场景 | 命名规则 | 示例 |
|------|---------|------|
| Fact 对象 | PascalCase + `Fact` 后缀 | `SettlementFact`, `PrescriptionFact` |
| Agent/Skill 实现 | PascalCase + `Skill` 后缀 | `RationalDrugUseSkill` |
| Service 层 | PascalCase + `Service` 后缀 | `SettlementService`, `FormulaManager` |
| 配置类 | PascalCase + `Config` | `DroolsConfig`, `AviatorProperties` |
| Controller | PascalCase + `Controller` | `SettlementController` |
| 异常类 | PascalCase + `Exception` | `RuleEngineException`, `FormulaParseException` |
| 枚举 | PascalCase | `ResultLevel`, `HisEventType`, `ErrorCode` |
| 常量接口/类 | UPPER_SNAKE_CASE 或 PascalCase | `HisConstants`, `ErrorCode` |
| 工具类 | PascalCase + `Util`/`Helper` | `AviatorHelper`, `BigDecimalUtil` |

### 1.3 方法命名规范

| 场景 | 命名规则 | 示例 |
|------|---------|------|
| 获取数据 | `get`/`find`/`query`/`load` | `getFormula()`, `findByTenantId()` |
| 执行操作 | `execute`/`fire`/`run`/`process` | `fireRules()`, `executeFormula()` |
| 计算操作 | `calculate`/`compute` | `calculateReimbursement()` |
| 校验操作 | `validate`/`check` | `validatePrescription()` |
| 刷新操作 | `refresh`/`reload`/`invalidate` | `refreshFormula()`, `invalidateCache()` |
| 布尔判断 | `is`/`has`/`can`/`should` | `hasBlock()`, `isValidFormula()` |
| 回调/监听 | `on` + 事件 | `onConfigChange()`, `onFormulaUpdated()` |

### 1.4 字段类型强制规范

| 数据类型 | 使用场景 | 说明 |
|---------|---------|------|
| **BigDecimal** | 所有金额、比例、费率 | **禁止使用 double/float** |
| **String** | 公式文本、规则内容、描述性字段 | — |
| **LocalDateTime** | 时间戳 | 禁止使用 Date/Calendar |
| **Integer/Long** | 数量、ID、计数 | — |
| **Boolean** | 标志位 | 优先用包装类避免 NPE |
| **Enum** | 固定选项集合 | 如 PatientType, ResultLevel |

```java
// ✅ 正确：金额必须用 BigDecimal
public class SettlementFact {
    private BigDecimal totalFee;       // 总费用
    private BigDecimal deductible;     // 起付线
    private BigDecimal ratio;          // 报销比例
    private BigDecimal finalAmount;    // 最终报销金额
}

// ❌ 错误：禁止 double 做金额
private double ratio;  // 浮点精度丢失！
```

### 1.5 日志规范

| 规则 | 要求 |
|------|------|
| 日志框架 | **SLF4J + Logback**，禁止 System.out.println |
| Logger 声明 | `private static final Logger log = LoggerFactory.getLogger(Xxx.class);` |
| 日志级别 | ERROR(系统故障) / WARN(业务异常) / INFO(关键流程) / DEBUG(调试详情) |
| 参数化日志 | 使用 `{}` 占位符，禁止字符串拼接 |
| 敏感信息 | 禁止打印患者姓名/身份证号/医保卡号 |

```java
// ✅ 正确：SLF4J 参数化日志
log.info("结算完成: patientId={}, amount={}", fact.getPatientId(), fact.getFinalAmount());
log.warn("公式语法错误: formula={}", formula);

// ❌ 错误：System.out.println
System.out.println("计算结果：" + amount);

// ❌ 错误：字符串拼接（每次都拼接，即使日志级别不够）
log.info("结算完成: " + fact.getPatientId());
```

### 1.6 文件大小限制

| 级别 | 行数 | 处理方式 |
|------|------|---------|
| ✅ 理想 | < 300 行 | 保持现状 |
| ⚠️ 警告 | 300~500 行 | 考虑拆分 |
| ❌ 必须 | > 500 行 | **必须**按职责拆分 |

### 1.7 注释规范

| 规则 | 要求 |
|------|------|
| 类注释 | Javadoc，说明职责和使用方式 |
| 公共方法 | Javadoc，含 @param/@return/@throws |
| 关键业务逻辑 | 行内注释解释"为什么"而非"做什么" |
| Fact 字段 | 每个字段注释其业务含义 |
| TODO/FIXME | 必须标注作者或 issue 编号 |

---

## 二、DRL 规则文件编写规范

### 2.1 文件结构模板

```drools
package com.his.rules.{module};

// 1. Fact 导入
import com.his.fact.{Xxx}Fact;
import com.his.helper.AviatorHelper;
import com.his.common.ResultLevel;

// 2. 全局变量声明（可选）
global java.util.List results;
global com.his.formula.DynamicFormulaManager formulaManager;

// 3. 规则列表（按业务流程排序，salience 从大到小）
rule "序号. 规则名称"
    salience {优先级}
    when
        // 条件（LHS）
        $f: {FactType}({条件表达式})
    then
        // 动作（RHS）— 使用 log 不用 println
        log.{level}("提示信息");
        // 修改 Fact 或调用外部服务
end
```

### 2.2 规则编写约束

| 规则 | 要求 | 示例 |
|------|------|------|
| 规则命名 | `"序号. 中文名称"` | `"1. 身份校验"`, `"4. 报销金额计算"` |
| salience | 数字越大越先执行 | 身份校验=100, 计算公式=10 |
| 条件判断 | 只做条件分支，不做复杂计算 | 计算委托给 Aviator |
| 修改 Fact | 必须 `update($f)` 触发重新匹配 | — |
| 日志输出 | 使用 `log` 全局变量(SLF4J) | `log.warn("...")` |
| 公式调用 | 通过 AviatorHelper 执行 | 见下方示例 |
| 禁止硬编码 | 政策参数从配置获取 | — |

### 2.3 Drools 中调用 Aviator（核心混合点）

```drools
rule "按公式计算报销金额"
    salience 10
    when
        $f: SettlementFact(totalFee != null, deductible != null, ratio != null)
    then
        // 从配置中心获取公式（通过全局变量）
        String formulaKey = "formula.reimburse." + $f.getPatientType().toLowerCase();
        String formulaText = formulaManager.getFormulaByKey(formulaKey);
        
        if (formulaText != null) {
            // 调用 Aviator 执行公式
            BigDecimal amount = AviatorHelper.executeFormula(formulaText, $f);
            $f.setFinalAmount(amount);
            log.info("公式计算完成: key={}, result={}", formulaKey, amount);
        } else {
            // 降级处理
            $f.setFinalAmount(BigDecimal.ZERO);
            log.warn("公式未找到，降级为零: key={}", formulaKey);
        }
        
        update($f);
end
```

### 2.4 禁止事项

```drools
// ❌ 禁止：在 DRL 中硬编码计算公式
$f.setFinalAmount(
    ($f.getTotalFee().subtract($f.getDeductible()))
     .multiply($f.getRatio()).setScale(2, RoundingMode.HALF_UP)
);

// ❌ 禁止：System.out.println
System.out.println("结果: " + amount);

// ❌ 禁止：在 RHS 中直接查询数据库
List<Drug> drugs = drugRepository.findAll();  // 绝对禁止！

// ✅ 正确：公式交给 Aviator，日志用 SLF4J
BigDecimal amount = AviatorHelper.executeFormula(formula, $f);
log.debug("公式计算: {}", amount);
```

---

## 三、Aviator 表达式设计规范

### 3.1 表达式编写约定

| 规则 | 要求 |
|------|------|
| 数值类型 | 统一使用 BigDecimal（配置 `ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_BIGDECIMAL`） |
| 四舍五入 | 使用 `round(value, scale)` 函数 |
| 变量命名 | 与 Fact 字段名一致（camelCase） |
| 公式可读性 | 适当加括号明确运算优先级 |
| 边界保护 | 除法前检查除数不为零（在 Drools 层面保证） |

### 3.2 常用公式模板

```javascript
// 报销金额计算
round((totalFee - deductible) * ratio, 2)

// DRG 权重调整
(baseWeight + extraPoints) * severityFactor

// 临界值判断
totalFee > 20000 ? totalFee * 0.5 : totalFee * 0.3

// 阶梯式报销
totalFee <= 1000 ? totalFee * 0.8 :
 totalFee <= 5000 ? 1000 * 0.8 + (totalFee - 1000) * 0.6 :
 1000 * 0.8 + 4000 * 0.6 + (totalFee - 5000) * 0.4

// 用药极量检查
dailyDosage > maxDailyDosage ? maxDailyDosage : dailyDosage
```

### 3.3 公式存储规范

| 维度 | 规范 |
|------|------|
| Key 命名 | `formula.{分类}.{子类}` 如 `formula.reimburse.resident` |
| 版本管理 | 每次修改记录到 FormulaHistory 表 |
| 语法校验 | 保存前调用 `AviatorValidator.validate()` |
| 测试验证 | 保存后用预设测试数据跑一遍确认结果正确 |

---

## 五、微服务开发规范

### 5.1 服务拆分原则

| 原则 | 说明 |
|------|------|
| 单一职责 | 每个服务只负责一个业务域（规则/结算/用药/质控/DRG） |
| 数据独立 | 每个服务拥有独立的数据库表，禁止跨服务直接访问数据库 |
| 接口契约 | 服务间通过 REST API + Feign 客户端通信 |
| 独立部署 | 每个服务可独立构建、部署、扩缩容 |

### 5.2 Feign 客户端规范

```java
// ✅ 正确：定义 Feign 客户端接口
@FeignClient(
    name = "his-rule-service",
    path = "/api/v1/rules",
    configuration = FeignConfig.class
)
public interface RuleServiceClient {

    @GetMapping("/{id}")
    Result<RuleDefinitionVO> getById(@PathVariable("id") Long id);

    @PostMapping("/batch")
    Result<List<RuleDefinitionVO>> batchGet(@RequestBody List<Long> ids);
}

// ✅ 正确：使用 Feign 客户端
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final RuleServiceClient ruleClient;

    public void executeSettlement(Long ruleId) {
        Result<RuleDefinitionVO> result = ruleClient.getById(ruleId);
        if (!"0".equals(result.getCode())) {
            throw new BusinessException("规则不存在: " + ruleId);
        }
        // 业务逻辑...
    }
}
```

### 5.3 服务间调用约束

| 规则 | 要求 |
|------|------|
| 禁止直接 HTTP | 必须使用 Feign 客户端，禁止 RestTemplate/HttpClient 直接调用 |
| 超时控制 | Feign 调用必须配置超时（connectTimeout=3s, readTimeout=5s） |
| 重试策略 | 幂等接口可重试，非幂等接口禁止重试 |
| 降级处理 | 使用 Sentinel 配置降级策略，避免级联故障 |
| 链路追踪 | 必须传递 traceId，便于问题排查 |

### 5.4 统一配置管理

```yaml
# 所有服务共享的配置（his-common-core）
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        file-extension: yaml
        shared-configs:
          - data-id: his-common.yaml
            refresh: true

# 服务特有配置（各自 application.yml）
server:
  port: ${SERVER_PORT:9001}

his:
  rule:
    # 规则引擎特有配置
    kie-base-group: reimbursement
    aviator-cache-size: 5000
```

---

## 六、Skill Pipeline 规范

### 6.1 ISkill 接口实现

```java
/**
 * 医保起付线检查 Skill
 * 
 * 职责：检查患者是否达到起付线标准
 * 触发事件：EVENT_SETTLEMENT_EXECUTE
 * 执行顺序：10（优先级高，先执行）
 */
@Service
@Slf4j
public class DeductibleCheckSkill implements ISkill<SettlementFact> {

    private final RuleEngineTemplate ruleEngine;

    @Override
    public String supportEvent() {
        return "EVENT_SETTLEMENT_EXECUTE";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void execute(SkillContext<SettlementFact> context) {
        long startTime = System.currentTimeMillis();
        try {
            SettlementFact fact = context.getFact();
            
            // 执行起付线检查规则
            String ruleGroup = "DEDUCTIBLE_CHECK_" + context.getTenantId();
            ruleEngine.fireRules(ruleGroup, fact);
            
            // 检查结果
            if (fact.getTotalFee().compareTo(fact.getDeductible()) < 0) {
                context.addResult(new SkillResult(
                    ResultLevel.BLOCK,
                    "DeductibleCheck",
                    "未达到起付线标准"
                ));
            }
            
            long cost = System.currentTimeMillis() - startTime;
            log.debug("起付线检查完成: cost={}ms", cost);
            
        } catch (Exception e) {
            log.error("起付线检查异常", e);
            // 异常降级：返回 WARN 而非抛出异常
            context.addResult(new SkillResult(
                ResultLevel.WARN,
                "DeductibleCheck",
                "起付线检查异常: " + e.getMessage()
            ));
        }
    }
}
```

### 6.2 ResultLevel 处理规范

| 级别 | 含义 | Pipeline 处理 | 业务场景 |
|------|------|--------------|---------|
| PASS | 通过 | 继续执行下一个 Skill | 正常场景 |
| WARN | 警告 | 继续执行，最终结果标记为 WARN | 配伍禁忌、剂量超限 |
| BLOCK | 阻断 | 中断 Pipeline，返回 BLOCK 结果 | 身份缺失、未达起付线 |

### 6.3 Pipeline 编排规范

```java
@Service
@RequiredArgsConstructor
public class SettlementPipeline {

    private final List<ISkill<SettlementFact>> skills;

    public SettlementResult execute(SettlementFact fact) {
        SkillContext<SettlementFact> context = new SkillContext<>(fact);
        
        // 按优先级排序执行
        skills.stream()
            .sorted(Comparator.comparingInt(ISkill::getOrder))
            .forEach(skill -> {
                // 如果已有 BLOCK，跳过后续 Skill
                if (context.hasBlock()) {
                    log.warn("Pipeline 已阻断，跳过 Skill: {}", skill.getClass().getSimpleName());
                    return;
                }
                skill.execute(context);
            });
        
        return buildResult(context);
    }
}
```

### 6.4 异常降级规范

| 异常类型 | 降级策略 | 日志级别 |
|---------|---------|---------|
| 规则语法错误 | WARN 级别返回，不影响其他 Skill | ERROR |
| 公式解析失败 | 降级为默认值（如 ZERO） | ERROR |
| 配置中心不可用 | 使用本地缓存的上一次值 | WARN |
| 超时（>10s） | 取消当前 Skill，返回 WARN | WARN |
| 致命异常 | BLOCK 级别返回，中断 Pipeline | ERROR |

---

最后更新: 2026-05-12 | v1.1 (HIS Drools+Aviator 规则引擎专用 - 微服务架构版)
