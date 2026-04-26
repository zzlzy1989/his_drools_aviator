下面给出一个 **Drools + Aviator 混合架构**的具体示例，场景为 **医保住院费用结算**：Drools 负责多条规则的编排与触发（如患者身份校验、目录限制、起付线判断等），当命中某条“按公式计算报销金额”的规则时，调用 Aviator 动态执行计费表达式。

---

## 一、整体架构图（文字版）

```text
[ HIS 结算请求 ] 
       ↓
[ Drools 规则引擎 ]   ← 加载规则文件 (DRL)
       │
       ├─ 规则1: 身份校验 → 通过/拒绝
       ├─ 规则2: 限制用药校验 → 通过/拒绝
       ├─ 规则3: 起付线判断 → 计算基数
       └─ 规则4: 报销金额计算 → 触发 Aviator 表达式
              ↓
       [ Aviator 表达式引擎 ]  ← 动态执行公式字符串
              ↓
       返回计算结果 → 继续后续规则或结束
```

---

## 二、核心设计要点

| 组件 | 职责 | 数据载体 |
| :--- | :--- | :--- |
| **Fact 对象** | 存放患者、费用明细、结算上下文 | `SettlementFact.java` |
| **Drools 规则文件** | 编排规则流，定义何时触发哪种计算 | `reimbursement.drl` |
| **Aviator 表达式** | 以字符串形式存储在数据库或配置中心 | `formula = "round((totalFee - deductible) * ratio, 2)"` |
| **Aviator 执行器** | 一个 Java 工具类，接收公式和参数，返回计算结果 | `AviatorEvaluator.execute(formula, env)` |

---

## 三、实战代码示例

### 1. Fact 对象（传递给 Drools 和 Aviator）

```java
public class SettlementFact {
    private String patientType;     // 职工/居民
    private BigDecimal totalFee;    // 总费用
    private BigDecimal deductible;  // 起付线
    private BigDecimal ratio;       // 报销比例
    private BigDecimal finalAmount; // 最终报销金额（待计算）
    // getters/setters
}
```

### 2. Drools 规则文件（reimbursement.drl）

```drools
import com.example.SettlementFact
import com.example.AviatorHelper

rule "1. 身份校验"
    when
        $f: SettlementFact(patientType == null || patientType == "")
    then
        System.out.println("患者身份缺失，拒绝结算");
        $f.setFinalAmount(BigDecimal.ZERO);
        update($f);
end

rule "2. 起付线确定"
    when
        $f: SettlementFact(patientType == "职工", totalFee != null)
    then
        // 职工起付线 1000
        $f.setDeductible(new BigDecimal("1000"));
        update($f);
end

rule "3. 报销比例确定"
    when
        $f: SettlementFact(patientType == "居民")
    then
        $f.setRatio(new BigDecimal("0.65"));
        update($f);
end

rule "4. 按公式计算报销金额（核心混合点）"
    when
        $f: SettlementFact(totalFee != null, deductible != null, ratio != null)
    then
        // 公式模板（实际可从配置中心获取）
        String formula = "round((totalFee - deductible) * ratio, 2)";
        
        // 调用 Aviator 执行公式
        BigDecimal amount = AviatorHelper.executeFormula(formula, $f);
        $f.setFinalAmount(amount);
        
        System.out.println("计算结果：" + amount);
        update($f);
end
```

### 3. Aviator 执行器（AviatorHelper.java）

```java
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class AviatorHelper {

    static {
        // 注册 BigDecimal 支持（可选，让表达式直接使用 BigDecimal 运算）
        AviatorEvaluator.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_BIGDECIMAL);
    }

    public static BigDecimal executeFormula(String formula, SettlementFact fact) {
        // 构建参数环境
        Map<String, Object> env = new HashMap<>();
        env.put("totalFee", fact.getTotalFee());
        env.put("deductible", fact.getDeductible());
        env.put("ratio", fact.getRatio());
        
        // 编译+执行（生产环境建议缓存表达式实例）
        Expression exp = AviatorEvaluator.compile(formula, true);
        Object result = exp.execute(env);
        
        // 返回 BigDecimal
        return (BigDecimal) result;
    }
}
```

### 4. 调用入口（结算服务）

```java
public class SettlementService {
    private KieSession kieSession;  // Drools 会话

    public void doSettlement(SettlementFact fact) {
        kieSession.insert(fact);
        kieSession.fireAllRules();   // 执行规则流
        System.out.println("最终报销金额：" + fact.getFinalAmount());
    }
}
```

---

## 四、动态公式的来源（生产环境最佳实践）

公式不会硬编码在 DRL 文件中，而是存储在**数据库或配置中心**，便于业务人员实时调整。

| 存储方式 | 示例表结构 | 更新机制 |
| :--- | :--- | :--- |
| 数据库 | `reimbursement_formula` (rule_id, formula_text, version) | 修改后通过 Drools 的动态规则加载 + Aviator 表达式缓存刷新 |
| Apollo/Nacos | 配置项 `formula.reimbursement.resident = "round((totalFee - deductible) * 0.65, 2)"` | 监听配置变更，更新 Aviator 编译后的表达式 |

**关键接口**：  
Drools 规则只需引用一个抽象的公式ID，值注入由 Aviator 动态解析。

```drools
rule "4. 动态公式计算"
    when
        $f: SettlementFact()
        $formula: FormulaEntity(ruleId == "reimburse_resident")
    then
        BigDecimal amount = AviatorHelper.executeFormula($formula.getText(), $f);
        $f.setFinalAmount(amount);
end
```

---

## 五、这样混合的好处

| 好处 | 说明 |
| :--- | :--- |
| **规则编排清晰** | Drools 管理规则流（条件、优先级、分组），维护方便 |
| **计算性能极高** | Aviator 处理数学/逻辑表达式，比 Java 反射快 10~100 倍 |
| **公式热更新** | 修改数据库中的 formula_text，无需重启 Drools 会话，实时生效 |
| **降低 Drools 复杂度** | 避免在 DRL 中编写复杂的算术公式，代码可读性更高 |
| **便于业务人员维护** | 公式字符串可放在配置后台，供运营或医保专员直接修改 |

---

## 六、注意事项

1. **Aviator 不是完整规则引擎**：它只负责计算，不做条件分支（分支条件仍在 Drools 中）。
2. **表达式缓存**：生产中应对 `AviatorEvaluator.compile(formula, true)` 的结果进行缓存（以 formula 为 key），避免重复编译。
3. **类型一致性**：Drools 和 Aviator 之间传递的数值统一使用 `BigDecimal`，避免浮点精度问题。
4. **异常处理**：公式语法错误时，Aviator 会抛出异常，需要捕获并回退到默认逻辑。

---

如果需要，我可以进一步给出 **Drools + Aviator 的表达式缓存实现** 或 **基于 Apollo 的动态公式刷新示例**。

下面分别给出 **Aviator 表达式缓存** 和 **基于 Apollo 的动态公式刷新** 的完整实现示例，两者可以组合使用，以实现高性能且支持热更新的公式管理。

---

## 一、Aviator 表达式缓存实现（基于 Caffeine）

在混合架构中，同一个公式可能被多次调用（例如每天数千次结算），每次都重新编译表达式会损耗性能。需要引入缓存。

### 1. 依赖（Maven）

```xml
<!-- Aviator -->
<dependency>
    <groupId>com.googlecode.aviator</groupId>
    <artifactId>aviator</artifactId>
    <version>5.4.3</version>
</dependency>
<!-- Caffeine 缓存 -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
    <version>3.1.8</version>
</dependency>
```

### 2. 公式缓存服务（支持动态刷新）

```java
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
public class AviatorExpressionCache {

    // 缓存：公式文本 -> 编译后的 Expression
    private Cache<String, Expression> expressionCache;

    @PostConstruct
    public void init() {
        expressionCache = Caffeine.newBuilder()
                .maximumSize(1000)                 // 最多缓存1000个表达式
                .expireAfterWrite(30, TimeUnit.MINUTES)  // 30分钟未使用则过期
                .recordStats()                     // 可选：记录命中率
                .build();
    }

    /**
     * 获取编译后的表达式（优先从缓存获取）
     */
    public Expression getCompiledExpression(String formula) {
        return expressionCache.get(formula, key -> {
            // 如果缓存中没有，则编译并存入
            return AviatorEvaluator.compile(key, true);
        });
    }

    /**
     * 手动刷新某个公式（通常在配置变更时调用）
     */
    public void refreshFormula(String formula) {
        expressionCache.invalidate(formula);
        // 重新编译预热（可选）
        getCompiledExpression(formula);
    }

    /**
     * 清除所有缓存
     */
    public void clearAll() {
        expressionCache.invalidateAll();
    }

    /**
     * 执行公式
     */
    public Object execute(String formula, Map<String, Object> env) {
        Expression exp = getCompiledExpression(formula);
        return exp.execute(env);
    }
}
```

### 3. 在 Drools 规则中使用（通过 Spring 注入）

```java
// AviatorHelper 改造为使用缓存
@Component
public class AviatorHelper {
    @Autowired
    private AviatorExpressionCache cache;

    public BigDecimal executeFormula(String formula, SettlementFact fact) {
        Map<String, Object> env = new HashMap<>();
        env.put("totalFee", fact.getTotalFee());
        env.put("deductible", fact.getDeductible());
        env.put("ratio", fact.getRatio());
        
        Object result = cache.execute(formula, env);
        return (BigDecimal) result;
    }
}
```

---

## 二、基于 Apollo 的动态公式刷新示例

Apollo（携程开源的配置中心）可以动态修改配置，应用实时感知。我们让公式文本存储在 Apollo 的一个命名空间中，当配置变化时，自动刷新缓存并通知 Drools（如果需要）。

### 1. 依赖（Apollo 客户端）

```xml
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
    <version>2.2.0</version>
</dependency>
```

### 2. Apollo 配置示例

在 Apollo 中创建一个 Application 命名空间，添加配置项：

| Key | Value |
| --- | --- |
| `formula.reimburse.resident` | `round((totalFee - 500) * 0.65, 2)` |
| `formula.reimburse.employee` | `round((totalFee - 1000) * 0.85, 2)` |
| `formula.drg.adjWeight` | `(baseWeight + extraPoints) * severityFactor` |

### 3. 动态公式管理器（监听 Apollo 变更）

```java
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.googlecode.aviator.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DynamicFormulaManager {

    private static final Logger log = LoggerFactory.getLogger(DynamicFormulaManager.class);
    
    // 本地缓存：formulaKey -> 表达式文本
    private final ConcurrentHashMap<String, String> formulaTextCache = new ConcurrentHashMap<>();
    
    @Autowired
    private AviatorExpressionCache aviatorCache;

    @PostConstruct
    public void init() {
        Config config = ConfigService.getAppConfig(); // 获取 Apollo 配置
        // 1. 初始加载所有公式配置
        refreshAllFormulas(config);
        
        // 2. 添加变更监听器
        config.addChangeListener(new ConfigChangeListener() {
            @Override
            public void onChange(ConfigChangeEvent changeEvent) {
                for (String key : changeEvent.changedKeys()) {
                    if (key.startsWith("formula.")) {
                        String newValue = changeEvent.getChange(key).getNewValue();
                        log.info("Formula changed: {} = {}", key, newValue);
                        // 更新本地文本
                        formulaTextCache.put(key, newValue);
                        // 关键：让 Aviator 缓存中的表达式失效
                        aviatorCache.refreshFormula(newValue);
                        // 可选：如果 Drools 规则中引用了公式Key，也可以通知 Drools 重新加载某条规则
                    }
                }
            }
        });
    }

    private void refreshAllFormulas(Config config) {
        // 获取所有以 "formula." 开头的配置项（Apollo 没有直接获取全部，这里演示具体写法）
        // 实际项目中可预先定义需要监听的公式Key列表
        List<String> formulaKeys = Arrays.asList(
            "formula.reimburse.resident",
            "formula.reimburse.employee",
            "formula.drg.adjWeight"
        );
        for (String key : formulaKeys) {
            String value = config.getProperty(key, null);
            if (value != null) {
                formulaTextCache.put(key, value);
                // 预热缓存（可选）
                aviatorCache.getCompiledExpression(value);
            }
        }
    }

    /**
     * 根据公式Key获取当前表达式文本
     */
    public String getFormulaByKey(String key) {
        return formulaTextCache.get(key);
    }
}
```

### 4. 在 Drools 规则中结合 Apollo

修改之前的 DRL 规则，不再硬编码公式，而是通过 Key 动态获取：

```drools
import com.example.SettlementFact
import com.example.DynamicFormulaManager
import com.example.AviatorHelper

global DynamicFormulaManager formulaManager;
global AviatorHelper aviatorHelper;

rule "动态报销金额计算 (Apollo)"
    when
        $f: SettlementFact(patientType == "resident")
    then
        // 根据患者类型获取公式Key
        String formulaKey = "formula.reimburse.resident";
        String formulaText = formulaManager.getFormulaByKey(formulaKey);
        
        if (formulaText != null) {
            BigDecimal amount = aviatorHelper.executeFormula(formulaText, $f);
            $f.setFinalAmount(amount);
        } else {
            // 降级逻辑
            $f.setFinalAmount(BigDecimal.ZERO);
        }
        update($f);
end
```

### 5. 调用方需设置全局变量

```java
KieSession kieSession = ...;
kieSession.setGlobal("formulaManager", dynamicFormulaManager);
kieSession.setGlobal("aviatorHelper", aviatorHelper);
kieSession.insert(fact);
kieSession.fireAllRules();
```

---

## 三、组合效果总结

| 能力 | 实现方式 |
| :--- | :--- |
| **表达式编译性能** | Caffeine 缓存 Expression 对象，避免重复编译 |
| **公式热更新** | Apollo 监听配置变更 → 刷新本地文本缓存 → 使 Aviator 缓存失效 → 下次调用重新编译 |
| **规则与公式隔离** | Drools 只负责条件判断和流向，具体数值计算委托给 Aviator + 动态公式 |
| **业务友好** | 公式字符串可放在 Apollo 配置中心，运维或医保专员可直接在 Web 界面修改，无需重启应用 |

---

## 四、生产环境注意事项

1. **表达式缓存大小**：HIS 系统中公式数量通常不超过几百个，设置 `maximumSize(2000)` 足够。
2. **Apollo 推送延迟**：配置变更后，客户端最长 1 分钟（默认）会拉取新值。如需秒级生效，可增加 `apollo.refreshInterval` 或主动调用 `Config.refresh()`。
3. **降级策略**：当 Apollo 不可用或公式语法错误时，应记录日志并返回默认值（如 0），避免结算中断。
4. **灰度发布**：可为不同医院或不同环境配置不同的 Apollo 集群，实现公式按院区隔离。

如果需要，我可以继续给出 **基于 Nacos 的实现** 或 **公式语法校验工具类**。

下面分别给出 **基于 Nacos 的动态公式刷新实现** 和 **Aviator 公式语法校验工具类**，两者均可与之前的 Drools + Aviator 架构无缝集成。

---

## 一、基于 Nacos 的动态公式刷新实现

Nacos 是阿里巴巴开源的配置中心和服务发现平台，与 Apollo 类似，也支持配置的动态监听和热更新。

### 1. 依赖（Spring Boot + Nacos）

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    <version>2021.0.5.0</version>
</dependency>
```

### 2. bootstrap.yml 配置 Nacos

```yaml
spring:
  application:
    name: his-rule-engine
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yaml
        namespace: his_prod
        group: HIS_RULE_GROUP
```

### 3. 在 Nacos 配置中心添加公式配置

- **Data ID**: `his-rule-engine.yaml`
- **Group**: `HIS_RULE_GROUP`
- **配置内容**:

```yaml
formulas:
  reimburse:
    resident: "round((totalFee - 500) * 0.65, 2)"
    employee: "round((totalFee - 1000) * 0.85, 2)"
    medical_aid: "round((totalFee - 200) * 0.95, 2)"
  drg:
    weight_adjust: "(baseWeight + extraPoints) * severityFactor"
  critical_value: "totalFee > 20000 ? totalFee * 0.5 : totalFee * 0.3"
```

### 4. 动态公式管理器（使用 `@RefreshScope` + 监听）

```java
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Component
@RefreshScope  // 支持配置自动刷新
public class NacosFormulaManager implements InitializingBean {

    @Value("${formulas:}")
    private Map<String, Map<String, String>> formulasConfig; // 自动注入

    private final Map<String, String> formulaCache = new ConcurrentHashMap<>();

    @Autowired
    private NacosConfigManager nacosConfigManager;

    @Autowired
    private AviatorExpressionCache aviatorCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始加载
        refreshFormulas();
        // 注册 Nacos 监听器（可选，用于更精细的控制）
        registerNacosListener();
    }

    private void refreshFormulas() {
        if (formulasConfig == null) return;
        formulaCache.clear();
        for (Map.Entry<String, Map<String, String>> category : formulasConfig.entrySet()) {
            for (Map.Entry<String, String> entry : category.getValue().entrySet()) {
                String key = category.getKey() + "." + entry.getKey(); // 如 "reimburse.resident"
                String formula = entry.getValue();
                formulaCache.put(key, formula);
                // 预热 Aviator 缓存（可选）
                aviatorCache.getCompiledExpression(formula);
            }
        }
        System.out.println("Formulas refreshed: " + formulaCache.size());
    }

    /**
     * 获取公式文本
     */
    public String getFormula(String key) {
        return formulaCache.get(key);
    }

    /**
     * 手动刷新（通常由 @RefreshScope 机制自动触发，此处作为备份）
     */
    public void manualRefresh() {
        refreshFormulas();
    }

    /**
     * 注册 Nacos 监听器，在配置变更时主动清除缓存并重新编译（与 @RefreshScope 互补）
     */
    private void registerNacosListener() throws NacosException {
        String dataId = "his-rule-engine.yaml";
        String group = "HIS_RULE_GROUP";
        nacosConfigManager.getConfigService().addListener(dataId, group, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                // 当 Nacos 配置被修改时，该回调会执行
                System.out.println("Nacos config changed, refreshing formulas...");
                // 重新解析配置并更新缓存
                // 这里因为使用了 @RefreshScope，实际上配置会自动注入到 formulasConfig
                // 但为了立即刷新内部缓存，可以调用 manualRefresh
                manualRefresh();
            }
        });
    }
}
```

### 5. 在 Drools 规则中使用

```drools
global NacosFormulaManager formulaManager;
global AviatorHelper aviatorHelper;

rule "按居民医保公式计算"
    when
        $f: SettlementFact(patientType == "resident")
    then
        String formula = formulaManager.getFormula("reimburse.resident");
        if (formula != null) {
            BigDecimal amount = aviatorHelper.executeFormula(formula, $f);
            $f.setFinalAmount(amount);
        }
end
```

---

## 二、Aviator 公式语法校验工具类

在动态公式场景中，业务人员可能在配置中心输入错误的公式（如括号不匹配、变量名拼错、函数不存在等）。我们需要一个**校验工具**，在保存配置时或系统启动时提前验证，避免运行时崩溃。

### 1. 校验核心类

```java
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.exception.ExpressionSyntaxErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FormulaValidator {

    private static final Logger logger = LoggerFactory.getLogger(FormulaValidator.class);

    /**
     * 校验公式语法是否正确
     * @param formula 公式字符串，如 "round((totalFee - 500) * 0.65, 2)"
     * @return true 表示语法合法
     */
    public static boolean validateSyntax(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            return false;
        }
        try {
            // 仅编译，不执行
            Expression expr = AviatorEvaluator.compile(formula, true);
            return true;
        } catch (ExpressionSyntaxErrorException e) {
            logger.error("Formula syntax error: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during formula compilation: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 校验公式中使用的变量是否都在提供的变量集合中（可选）
     * @param formula 公式字符串
     * @param allowedVariables 允许的变量名集合（如 ["totalFee", "deductible", "ratio"]）
     * @return 是否所有变量都被声明
     */
    public static boolean validateVariables(String formula, Set<String> allowedVariables) {
        try {
            Expression expr = AviatorEvaluator.compile(formula, true);
            // 获取表达式中使用的变量名（Aviator 5.x 提供 getVariableNames 方法）
            Set<String> usedVars = expr.getVariableNames();
            for (String var : usedVars) {
                if (!allowedVariables.contains(var)) {
                    logger.warn("Unknown variable '{}' in formula: {}", var, formula);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 综合校验 + 返回详细的错误信息
     * @param formula 公式
     * @param allowedVariables 允许的变量集合（可为null）
     * @return 校验结果对象
     */
    public static ValidationResult validate(String formula, Set<String> allowedVariables) {
        if (formula == null || formula.trim().isEmpty()) {
            return ValidationResult.fail("公式不能为空");
        }
        try {
            Expression expr = AviatorEvaluator.compile(formula, true);
            if (allowedVariables != null && !allowedVariables.isEmpty()) {
                Set<String> usedVars = expr.getVariableNames();
                for (String var : usedVars) {
                    if (!allowedVariables.contains(var)) {
                        return ValidationResult.fail("使用了未定义的变量: " + var);
                    }
                }
            }
            return ValidationResult.success();
        } catch (ExpressionSyntaxErrorException e) {
            return ValidationResult.fail("语法错误: " + e.getMessage());
        } catch (Exception e) {
            return ValidationResult.fail("未知错误: " + e.getMessage());
        }
    }

    public static class ValidationResult {
        private boolean valid;
        private String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }
}
```

### 2. 在配置中心保存前调用校验（以 Nacos 为例，可扩展为 Apollo 同理）

```java
@RestController
public class FormulaController {

    @Autowired
    private NacosConfigManager nacosConfigManager;

    @PostMapping("/formula/update")
    public String updateFormula(@RequestParam String key, @RequestParam String formula) {
        // 校验公式
        Set<String> allowedVars = Set.of("totalFee", "deductible", "ratio", "baseWeight", "extraPoints", "severityFactor");
        ValidationResult result = FormulaValidator.validate(formula, allowedVars);
        if (!result.isValid()) {
            return "校验失败: " + result.getErrorMessage();
        }
        // 通过校验，更新 Nacos 配置（实际需通过 Nacos OpenAPI 或 SDK）
        // ...
        return "success";
    }
}
```

### 3. 在系统启动时批量校验所有公式（结合 NacosFormulaManager）

```java
@Component
public class FormulaStartupValidator implements ApplicationRunner {

    @Autowired
    private NacosFormulaManager formulaManager;

    @Override
    public void run(ApplicationArguments args) {
        // 假设所有公式Key已知，遍历校验
        List<String> allKeys = Arrays.asList("reimburse.resident", "reimburse.employee", "drg.weight_adjust");
        for (String key : allKeys) {
            String formula = formulaManager.getFormula(key);
            if (!FormulaValidator.validateSyntax(formula)) {
                throw new IllegalStateException("Formula invalid at startup: " + key + " -> " + formula);
            }
        }
        System.out.println("All formulas validated.");
    }
}
```

---

## 三、结合使用建议

| 场景 | 推荐方案 |
| :--- | :--- |
| **配置中心选型** | 如果已使用 Spring Cloud Alibaba，选 Nacos；如果已有 Apollo，用之前提供的 Apollo 实现。两者效果等价。 |
| **公式校验** | 在配置保存接口、系统启动、配置变更监听中均调用校验工具，确保恶意或错误公式不会进入生产环境。 |
| **性能** | 校验时只需要编译一次，不执行，开销很小。可放心在每次配置更新时调用。 |
| **变量白名单** | 在 HIS 系统中，应明确定义每个公式可用的变量集合（基于 Fact 对象的字段），避免注入攻击或意外变量。 |

如果需要，我可以继续给出 **基于 Nacos 的配置加解密** 或 **公式性能压测对比** 的示例。