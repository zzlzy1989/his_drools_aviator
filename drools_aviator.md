下面给出一个 **Drools + Aviator 混合架构**的具体示例，场景为 **医保住院费用结算**：Drools 负责多条规则的编排与触发（如患者身份校验、目录限制、起付线判断等），当命中某条“按公式计算报销金额”的规则时，调用 Aviator 动态执行计费表达式。

---

## 一、整体架构图（微服务版）

```text
[ HIS 结算请求 ]
       ↓
[ API Gateway (his-gateway) ]  ← Spring Cloud Gateway + Sentinel 限流 + JWT 鉴权
       ↓ 路由分发
┌──────────────────────────────────────────────────────────────┐
│  微服务集群 (Spring Cloud 2025.0.1 + Nacos 注册/配置中心)      │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                  │
│  │ his-rule-service │  │his-formula-service│                  │
│  │ 规则 CRUD/DRL发布 │  │ 公式 CRUD/语法校验 │                  │
│  │ + Drools 8.44    │  │ + Nacos 同步      │                  │
│  └──────────────────┘  └──────────────────┘                  │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                  │
│  │his-settlement-svc│  │ his-drug-service │                  │
│  │ 医保结算/报销计算  │  │ 合理用药/处方审核  │                  │
│  │ + Drools+Aviator │  │ + Drools 规则     │                  │
│  └──────────────────┘  └──────────────────┘                  │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                  │
│  │his-quality-service│ │  his-drg-service │                  │
│  │ 质控/院感/拦截    │  │ DRG/DIP 分组/权重 │                  │
│  │ + Drools+Aviator │  │ + Aviator 计算    │                  │
│  └──────────────────┘  └──────────────────┘                  │
└──────────────────────────────────────────────────────────────┘
       ↓
[ Nacos Config ] ← 公式/规则配置热更新
[ Redis ]        ← 二级缓存（公式缓存）
[ MySQL ]        ← 规则/公式/结算数据持久化
```

### 技术栈版本

| 组件 | 版本 | 说明 |
| :--- | :--- | :--- |
| **Java** | 21 | LTS 版本 |
| **Spring Boot** | 3.5.0 | 基于 Jakarta EE |
| **Spring Cloud** | 2025.0.1 | 微服务框架 |
| **Spring Cloud Alibaba** | 2025.1.0.0 | Nacos + Sentinel |
| **Drools** | 8.44.0.Final | 规则引擎 |
| **Aviator** | 5.4.3 | 表达式引擎 |
| **Caffeine** | 3.1.8 | 本地缓存 |
| **MyBatis-Plus** | 3.5.6 | ORM 框架 |
| **MySQL Connector** | 8.2.0 | 数据库驱动 |
| **Hutool** | 5.8.26 | 工具类库 |
| **MapStruct** | 1.5.5.Final | 对象映射 |
| **SpringDoc** | 2.7.0 | OpenAPI/Swagger |
| **Nacos** | 内置于 SCA 2025.1.0.0 | 注册中心 + 配置中心 |
| **Sentinel** | 1.8.8 | 流量控制 |
| **Redis** | spring-boot-starter-data-redis | 二级缓存 |
| **OpenFeign** | Spring Cloud 内置 | 服务间调用 |
| **JWT (jjwt)** | 0.12.5 | 认证令牌 |

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
package com.his.common;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SettlementFact {
    private String patientId;
    private String patientName;
    private String patientType;     // 职工/居民
    private String tenantId;        // 租户ID
    private String settlementId;

    private BigDecimal totalFee;    // 总费用
    private BigDecimal deductible;  // 起付线
    private BigDecimal ratio;       // 报销比例
    private BigDecimal finalAmount; // 最终报销金额（待计算）

    private String insuranceType;
    private String hospitalLevel;
    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;

    private String diagnosisCode;
    private String drugCode;
    private Integer drugQuantity;
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

## 四、动态公式的来源（项目实际方案）

公式存储在 **MySQL 数据库**中，通过 `his-formula-service` 管理，发布后自动同步到 **Nacos 配置中心**和 **Redis 二级缓存**，各微服务实时感知。

| 存储方式 | 说明 | 更新机制 |
| :--- | :--- | :--- |
| MySQL（主存储） | `aviator_formula` 表（formula_key/formula_text/category/status/tenant_id 等） | 公式 CRUD + 语法校验（FormulaValidator） |
| Nacos（配置中心） | Data ID: `his-formula-{tenantId}-{formulaKey}`，JSON 格式 | `NacosFormulaSyncListener` 监听 FormulaPublishEvent 自动同步 |
| Redis（二级缓存） | Key: `formula:{tenantId}:{formulaKey}`，Hash 结构 | 与 Nacos 同步时一并写入，TTL 24 小时 |

**关键接口**：  
Drools 规则只需引用一个抽象的公式Key，值注入由 Aviator 动态解析。项目通过 `FormulaService` 提供公式查询，各微服务通过 OpenFeign 调用获取公式。

```java
// FormulaService 提供的方法（his-formula-service）
public FormulaVO getByKey(String formulaKey) {
    // 从 MySQL 查询公式，返回 formulaText
}

// 消费方通过 OpenFeign 调用
@FeignClient(name = "his-formula-service")
public interface FormulaClient {
    @GetMapping("/api/v1/formulas/byKey")
    Result<FormulaVO> getByKey(@RequestParam String formulaKey);
}
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

如果需要，我可以进一步给出 **Drools + Aviator 的表达式缓存实现** 或 **基于 Nacos 的动态公式刷新示例**。

下面分别给出 **Aviator 表达式缓存** 和 **基于 Nacos 的动态公式刷新** 的完整实现示例，两者可以组合使用，以实现高性能且支持热更新的公式管理。

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

import jakarta.annotation.PostConstruct;
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

## 二、基于 Nacos 的动态公式刷新实现（项目实际方案）

Nacos 是阿里巴巴开源的配置中心和服务发现平台，本项目使用 Spring Cloud Alibaba 2025.1.0.0 集成 Nacos 作为唯一的配置中心，支持配置的动态监听和热更新。

### 1. 依赖（Spring Cloud Alibaba 已管理版本）

```xml
<!-- Nacos 配置中心（版本由 spring-cloud-alibaba-dependencies BOM 管理） -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
<!-- Nacos 服务发现 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
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
        group: HIS_RULE_ENGINE
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: his_prod
```

### 3. 在 Nacos 配置中心添加公式配置

- **Data ID**: `his-formula-{tenantId}-{formulaKey}`（如 `his-formula-tenant001-reimburse_resident`）
- **Group**: `HIS_RULE_ENGINE`
- **配置格式**: JSON
- **配置内容**:

```json
{
  "formulaKey": "reimburse.resident",
  "formulaText": "round((totalFee - 500) * 0.65, 2)",
  "category": "reimburse",
  "version": 1,
  "status": "active"
}
```

### 4. 动态公式管理器（基于 Nacos + Redis 二级缓存）

项目实际实现使用 `NacosFormulaSyncListener` 监听公式发布事件，将公式同步到 Nacos 和 Redis：

```java
package com.his.formula.listener;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.formula.entity.AviatorFormula;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Nacos 公式同步监听器
 * 监听公式发布事件，将公式同步到 Nacos 配置中心 + Redis 二级缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NacosFormulaSyncListener {

    private final NacosConfigManager nacosConfigManager;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String FORMULA_DATA_ID_PREFIX = "his-formula-";
    private static final String FORMULA_GROUP = "HIS_RULE_ENGINE";

    /**
     * 处理公式发布事件
     */
    @EventListener
    public void onFormulaPublish(FormulaPublishEvent event) {
        AviatorFormula formula = event.getFormula();
        String action = event.getAction();

        log.info("收到公式发布事件: action={}, formulaKey={}, tenantId={}",
                action, formula.getFormulaKey(), formula.getTenantId());

        try {
            switch (action) {
                case "PUBLISH", "ACTIVATE" -> publishToNacos(formula);
                case "DEACTIVATE" -> removeFromNacos(formula);
            }
        } catch (Exception e) {
            log.error("公式同步Nacos失败: formulaKey={}", formula.getFormulaKey(), e);
        }
    }

    /**
     * 发布公式到 Nacos
     */
    private void publishToNacos(AviatorFormula formula) {
        try {
            String dataId = buildDataId(formula.getFormulaKey(), formula.getTenantId());

            Map<String, Object> formulaConfig = new HashMap<>();
            formulaConfig.put("formulaKey", formula.getFormulaKey());
            formulaConfig.put("formulaText", formula.getFormulaText());
            formulaConfig.put("category", formula.getCategory());
            formulaConfig.put("version", formula.getVersion());
            formulaConfig.put("status", formula.getStatus());

            String content = objectMapper.writeValueAsString(formulaConfig);

            nacosConfigManager.getConfigService()
                    .publishConfig(dataId, FORMULA_GROUP, content, "JSON");

            log.info("公式已同步到Nacos: dataId={}, group={}", dataId, FORMULA_GROUP);

            publishToRedis(formula);

        } catch (Exception e) {
            log.error("发布公式到Nacos失败: formulaKey={}", formula.getFormulaKey(), e);
            throw new RuntimeException("Nacos同步失败", e);
        }
    }

    /**
     * 从 Nacos 移除公式
     */
    private void removeFromNacos(AviatorFormula formula) {
        try {
            String dataId = buildDataId(formula.getFormulaKey(), formula.getTenantId());
            nacosConfigManager.getConfigService()
                    .removeConfig(dataId, FORMULA_GROUP);
            log.info("公式已从Nacos删除: dataId={}", dataId);
            removeFromRedis(formula);
        } catch (Exception e) {
            log.error("从Nacos删除公式失败: formulaKey={}", formula.getFormulaKey(), e);
        }
    }

    /**
     * 发布到 Redis（作为二级缓存）
     */
    private void publishToRedis(AviatorFormula formula) {
        try {
            String key = buildRedisKey(formula.getFormulaKey(), formula.getTenantId());
            Map<String, Object> formulaConfig = new HashMap<>();
            formulaConfig.put("formulaKey", formula.getFormulaKey());
            formulaConfig.put("formulaText", formula.getFormulaText());
            formulaConfig.put("category", formula.getCategory());
            formulaConfig.put("version", formula.getVersion());

            redisTemplate.opsForHash().putAll(key, formulaConfig);
            redisTemplate.expire(key, java.time.Duration.ofHours(24));

            log.debug("公式已同步到Redis: key={}", key);
        } catch (Exception e) {
            log.warn("Redis同步失败（不影响主流程）: formulaKey={}", formula.getFormulaKey(), e);
        }
    }

    /**
     * 从 Redis 移除
     */
    private void removeFromRedis(AviatorFormula formula) {
        try {
            String key = buildRedisKey(formula.getFormulaKey(), formula.getTenantId());
            redisTemplate.delete(key);
            log.debug("公式已从Redis删除: key={}", key);
        } catch (Exception e) {
            log.warn("Redis删除失败: formulaKey={}", formula.getFormulaKey(), e);
        }
    }

    private String buildDataId(String formulaKey, String tenantId) {
        return FORMULA_DATA_ID_PREFIX + tenantId + "-" + formulaKey.replace(".", "_");
    }

    private String buildRedisKey(String formulaKey, String tenantId) {
        return "formula:" + tenantId + ":" + formulaKey;
    }
}
```

### 5. 公式发布事件

```java
package com.his.formula.listener;

import com.his.formula.entity.AviatorFormula;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 公式发布事件
 */
@Getter
public class FormulaPublishEvent extends ApplicationEvent {

    private final AviatorFormula formula;
    private final String action; // PUBLISH / ACTIVATE / DEACTIVATE

    private FormulaPublishEvent(Object source, AviatorFormula formula, String action) {
        super(source);
        this.formula = formula;
        this.action = action;
    }

    public static FormulaPublishEvent published(Object source, AviatorFormula formula) {
        return new FormulaPublishEvent(source, formula, "PUBLISH");
    }

    public static FormulaPublishEvent activated(Object source, AviatorFormula formula) {
        return new FormulaPublishEvent(source, formula, "ACTIVATE");
    }

    public static FormulaPublishEvent deactivated(Object source, AviatorFormula formula) {
        return new FormulaPublishEvent(source, formula, "DEACTIVATE");
    }
}
```

### 6. 公式实体（实际实现）

```java
package com.his.formula.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公式定义实体
 */
@Data
@TableName("aviator_formula")
public class AviatorFormula {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String formulaKey;
    private String formulaName;
    private String formulaText;
    private String category;
    private Integer version;
    private String status;
    private String description;
    private Integer isValidated;
    private String validatedMsg;
    private String tenantId;
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
```

### 7. 公式语法校验器

```java
package com.his.formula.validator;

import com.his.common.aviator.helper.AviatorHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 公式语法校验器
 */
@Slf4j
@Component
public class FormulaValidator {

    public ValidationResult validate(String formulaText) {
        if (formulaText == null || formulaText.isBlank()) {
            return ValidationResult.fail("公式内容不能为空");
        }
        if (formulaText.length() > 1000) {
            return ValidationResult.fail("公式内容不超过1000字符");
        }
        if (AviatorHelper.containsDangerousFunctions(formulaText)) {
            return ValidationResult.fail("公式包含不允许的函数");
        }
        String error = AviatorHelper.getValidationError(formulaText);
        if (error != null) {
            log.warn("公式语法校验失败: {}", error);
            return ValidationResult.fail(error);
        }
        return ValidationResult.ok();
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}
```

---

## 三、组合效果总结

| 能力 | 实现方式 |
| :--- | :--- |
| **表达式编译性能** | Caffeine 缓存 Expression 对象，避免重复编译 |
| **公式热更新** | Spring Event 发布公式事件 → NacosFormulaSyncListener 监听 → 同步到 Nacos + Redis 二级缓存 → 消费方拉取新公式重新编译 |
| **规则与公式隔离** | Drools 只负责条件判断和流向，具体数值计算委托给 Aviator + 动态公式 |
| **业务友好** | 公式存储在 MySQL，通过 his-formula-service 管理，发布后自动同步到 Nacos，各微服务实时感知 |
| **多租户隔离** | 公式按 tenantId 隔离，Nacos Data ID 和 Redis Key 均包含租户标识 |

---

## 四、生产环境注意事项

1. **表达式缓存大小**：HIS 系统中公式数量通常不超过几百个，设置 `maximumSize(5000)` 足够（见 `.trae/rules/performance.md`）。
2. **Nacos 推送延迟**：配置变更后，客户端默认 10 秒轮询拉取。如需秒级生效，可调整 `spring.cloud.nacos.config.refreshInterval`。
3. **降级策略**：当 Nacos 不可用或公式语法错误时，应记录日志并返回默认值（如 0），避免结算中断。
4. **灰度发布**：通过 Nacos namespace 实现不同环境（dev/test/prod）或不同院区的公式隔离。
5. **Redis 二级缓存**：Redis 作为 Nacos 的补充缓存，TTL 设为 24 小时，Redis 不可用时不影响主流程（仅 warn 日志）。
6. **微服务间调用**：his-settlement-service 等通过 OpenFeign 调用 his-formula-service 获取公式，需配置超时和重试策略。
7. **Sentinel 限流**：his-gateway 集成 Sentinel 对规则发布、结算执行等敏感接口进行流量控制。

如果需要，我可以继续给出 **Drools KieBase 分组策略** 或 **微服务间 OpenFeign 调用示例**。

---

## 七、微服务模块说明

项目采用微服务架构，各模块职责如下：

| 模块 | 职责 | 依赖引擎 |
| :--- | :--- | :--- |
| **his-gateway** | API 网关：路由、限流（Sentinel）、JWT 鉴权 | Spring Cloud Gateway |
| **his-rule-service** | 规则管理：规则 CRUD、版本管理、DRL 发布 | Drools 8.44 |
| **his-formula-service** | 公式管理：公式 CRUD、语法校验、Nacos 同步 | Aviator 5.4.3 + Nacos + Redis |
| **his-settlement-service** | 医保结算：报销计算、规则执行 | Drools + Aviator |
| **his-drug-service** | 合理用药：处方审核、药物相互作用、极量检查 | Drools |
| **his-quality-service** | 质控管理：院感预防、质控规则、拦截控制 | Drools + Aviator |
| **his-drg-service** | DRG/DIP：分组、权重计算、标准分 | Aviator |

### 公共模块（his-common）

| 模块 | 职责 |
| :--- | :--- |
| **his-common-core** | 核心实体：SettlementFact、ErrorCode、ISkill、SkillResult 等 |
| **his-common-web** | Web 公共组件：统一响应、全局异常、租户上下文 |
| **his-common-drools** | Drools 引擎封装：KieSession 管理、规则加载 |
| **his-common-aviator** | Aviator 引擎封装：表达式缓存（Caffeine）、AviatorHelper |

---

## 八、核心 Fact 对象（实际实现）

```java
package com.his.common;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SettlementFact {

    private String patientId;
    private String patientName;
    private String patientType;
    private String tenantId;
    private String settlementId;

    private BigDecimal totalFee;
    private BigDecimal deductible;
    private BigDecimal ratio;
    private BigDecimal finalAmount;

    private String insuranceType;
    private String hospitalLevel;
    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;

    private String diagnosisCode;
    private String drugCode;
    private Integer drugQuantity;
}
```

---

## 九、注意事项

1. **Aviator 不是完整规则引擎**：它只负责计算，不做条件分支（分支条件仍在 Drools 中）。
2. **表达式缓存**：生产中应对 `AviatorEvaluator.compile(formula, true)` 的结果进行缓存（以 formula 为 key），避免重复编译。项目使用 Caffeine 缓存，最大 5000 条，30 分钟过期。
3. **类型一致性**：Drools 和 Aviator 之间传递的数值统一使用 `BigDecimal`，避免浮点精度问题。
4. **异常处理**：公式语法错误时，Aviator 会抛出异常，需要捕获并回退到默认逻辑。
5. **Jakarta EE 迁移**：Spring Boot 3.5.0 使用 Jakarta EE（`jakarta.*` 包），不再使用 `javax.*`。
6. **租户隔离**：所有公式和规则查询强制过滤 `tenant_id`，通过 `TenantContext` 传递。