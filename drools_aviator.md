# Drools + Aviator 混合架构文档

> HIS 医疗业务规则引擎：Drools 负责规则编排，Aviator 负责动态公式计算

**版本**: v2.0  
**最后更新**: 2026-05-17  
**项目路径**: `/home/gaoxu/Documents/trae_projects/gx_project/his_drools_aviator`

---

## 一、整体架构图（微服务版）

```text
[ HIS 结算请求 / 处方审核 / 质控检查 ]
       ↓
[ API Gateway (his-gateway :9000) ]  ← Spring Cloud Gateway + CORS + JWT(可配置)
       ↓ 路由分发
┌──────────────────────────────────────────────────────────────┐
│  微服务集群 (Spring Cloud 2025.0.0 + Nacos 注册/配置中心)      │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                  │
│  │ his-rule-service │  │his-formula-service│                  │
│  │ 规则 CRUD/DRL发布 │  │ 公式 CRUD/语法校验 │                  │
│  │ + Drools 8.44    │  │ + Nacos 同步      │                  │
│  │ (:9001)          │  │ (:9002)          │                  │
│  └──────────────────┘  └──────────────────┘                  │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                  │
│  │his-settlement-svc│  │ his-drug-service │                  │
│  │ 医保结算/报销计算  │  │ 合理用药/处方审核  │                  │
│  │ + Drools+Aviator │  │ + Drools 规则     │                  │
│  │ (:9003)          │  │ (:9004)          │                  │
│  └──────────────────┘  └──────────────────┘                  │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                  │
│  │his-quality-service│ │  his-drg-service │                  │
│  │ 质控/院感/拦截    │  │ DRG/DIP 分组/权重 │                  │
│  │ + Drools+Aviator │  │ + Aviator 计算    │                  │
│  │ (:9005)          │  │ (:9006)          │                  │
│  └──────────────────┘  └──────────────────┘                  │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                  │
│  │his-monitor-svc   │  │his-market-service│                  │
│  │ 监控服务          │  │ 规则市场          │                  │
│  │ (:9007)          │  │ (:9008)          │                  │
│  └──────────────────┘  └──────────────────┘                  │
└──────────────────────────────────────────────────────────────┘
       ↓
[ Nacos Config ] ← 公式/规则配置热更新
[ Redis ]        ← 二级缓存（公式缓存）
[ MySQL ]        ← 规则/公式/结算数据持久化
```

### 技术栈版本（项目实际）

| 组件 | 版本 | 说明 |
| :--- | :--- | :--- |
| **Java** | 21 | LTS 版本 |
| **Spring Boot** | 3.5.0 | 基于 Jakarta EE |
| **Spring Cloud** | 2025.0.0 | 微服务框架 |
| **Spring Cloud Alibaba** | 2025.0.0.0 | Nacos 注册+配置 |
| **Drools** | 8.44.0.Final | 规则引擎 |
| **Aviator** | 5.4.3 | 表达式引擎 |
| **Caffeine** | 3.1.8 | 本地缓存 |
| **MyBatis-Plus** | 3.5.6 | ORM 框架 |
| **MySQL Connector** | 8.2.0 | 数据库驱动 |
| **Hutool** | 5.8.26 | 工具类库 |
| **MapStruct** | 1.5.5.Final | 对象映射 |
| **SpringDoc** | 2.7.0 | OpenAPI/Swagger |

---

## 二、核心设计要点

| 组件 | 职责 | 实际文件路径 |
| :--- | :--- | :--- |
| **Fact 对象** | 存放患者、费用明细、结算上下文 | `his-common-core/.../common/SettlementFact.java` |
| **Drools 规则文件** | 编排规则流，定义何时触发哪种计算 | `rule_definition` 表（数据库存储） |
| **Aviator 表达式** | 以字符串形式存储在数据库 | `aviator_formula` 表 |
| **Aviator 执行器** | 封装编译、缓存、执行 | `his-common-aviator/.../engine/AviatorEngine.java` |
| **表达式缓存** | Caffeine 缓存编译后的 Expression | `his-common-aviator/.../cache/AviatorExpressionCache.java` |
| **配置类** | Aviator 引擎配置参数 | `his-common-aviator/.../config/AviatorConfig.java` |
| **工具类** | 基础静态方法 | `his-common-aviator/.../helper/AviatorHelper.java` |

---

## 三、核心代码（项目实际实现）

### 3.1 Fact 对象（传递给 Drools 和 Aviator）

**SettlementFact.java** — 结算 Fact（项目实际实现）

```java
package com.his.common;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private List<SkillResult> results = new ArrayList<>();

    public void addResult(ResultLevel level, String source, String message) {
        this.results.add(new SkillResult(level, source, message));
    }

    public void addResult(SkillResult result) {
        this.results.add(result);
    }

    public boolean hasBlock() {
        return results.stream().anyMatch(r -> r.getLevel() == ResultLevel.BLOCK);
    }

    public ResultLevel getResultLevel() {
        if (hasBlock()) return ResultLevel.BLOCK;
        if (results.stream().anyMatch(r -> r.getLevel() == ResultLevel.WARN)) return ResultLevel.WARN;
        return ResultLevel.PASS;
    }
}
```

### 3.2 Aviator 引擎架构（项目实际实现）

项目采用四层架构设计：

```
┌─────────────────────────────────────────────────┐
│  AviatorEngine (Spring Component)               │
│  - execute() / executeDecimal() / executeBoolean() │
│  - validate() / compile() / refresh()           │
│  - 异常封装 (FormulaException)                   │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│  AviatorExpressionCache (Caffeine)              │
│  - maximumSize: 5000                            │
│  - expireAfterAccess: 30 minutes                │
│  - recordStats() 命中率监控                      │
│  - 编译耗时告警 (>1ms warn)                      │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│  AviatorConfig (@ConfigurationProperties)       │
│  - his.aviator.cacheMaxSize=5000                │
│  - his.aviator.cacheExpireMinutes=30            │
│  - his.aviator.maxExpressionLength=512          │
│  - his.aviator.compileTimeoutMs=1000            │
│  - his.aviator.executeTimeoutMs=100             │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│  AviatorHelper (静态工具类)                      │
│  - compile() / execute() / executeDecimal()     │
│  - validate() / getValidationError()            │
│  - containsDangerousFunctions()                 │
└─────────────────────────────────────────────────┘
```

**AviatorConfig.java** — 配置类

```java
package com.his.common.aviator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "his.aviator")
public class AviatorConfig {

    private boolean enabled = true;
    private int cacheMaxSize = 5000;
    private long cacheExpireMinutes = 30;
    private int maxExpressionLength = 512;
    private boolean alwaysParseFloatingPointAsBigDecimal = true;
    private boolean scientificNotationEnabled = false;
    private long compileTimeoutMs = 1000;
    private long executeTimeoutMs = 100;
}
```

**AviatorExpressionCache.java** — Caffeine 缓存

```java
package com.his.common.aviator.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.his.common.aviator.config.AviatorConfig;
import com.his.common.aviator.helper.AviatorHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AviatorExpressionCache {

    private final Cache<String, com.googlecode.aviator.Expression> cache;
    private final AviatorConfig config;

    public AviatorExpressionCache(AviatorConfig config) {
        this.config = config;
        this.cache = Caffeine.newBuilder()
                .maximumSize(config.getCacheMaxSize())
                .expireAfterAccess(config.getCacheExpireMinutes(), TimeUnit.MINUTES)
                .recordStats()
                .build();

        log.info("AviatorExpressionCache initialized: maxSize={}, expireMinutes={}",
                config.getCacheMaxSize(), config.getCacheExpireMinutes());
    }

    public com.googlecode.aviator.Expression getCompiledExpression(String expression) {
        return cache.get(expression, key -> {
            long start = System.nanoTime();
            try {
                com.googlecode.aviator.Expression compiled = AviatorHelper.compile(expression);
                long costMs = (System.nanoTime() - start) / 1_000_000;

                if (costMs > 1) {
                    log.warn("Aviator编译耗时较长: {}ms, expr={}",
                            costMs, key.substring(0, Math.min(50, key.length())));
                }

                return compiled;
            } catch (Exception e) {
                log.error("Aviator表达式编译失败: {}", key, e);
                throw e;
            }
        });
    }

    public com.googlecode.aviator.Expression getCompiledExpression(String expression, boolean cacheOnFailure) {
        if (cacheOnFailure) {
            return getCompiledExpression(expression);
        }
        return AviatorHelper.compile(expression);
    }

    public boolean isCached(String expression) {
        return cache.getIfPresent(expression) != null;
    }

    public void invalidate(String expression) {
        cache.invalidate(expression);
        log.debug("Aviator expression cache invalidated: {}", expression);
    }

    public void invalidateAll() {
        cache.invalidateAll();
        log.info("All Aviator expression caches invalidated");
    }
}
```

**AviatorEngine.java** — 引擎封装

```java
package com.his.common.aviator.engine;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.his.common.aviator.cache.AviatorExpressionCache;
import com.his.common.aviator.config.AviatorConfig;
import com.his.common.exception.FormulaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AviatorEngine {

    private final AviatorExpressionCache expressionCache;
    private final AviatorConfig config;

    public Object execute(String expression, Map<String, Object> env) {
        if (expression == null || expression.isBlank()) {
            throw new FormulaException("表达式不能为空");
        }

        if (expression.length() > config.getMaxExpressionLength()) {
            throw new FormulaException("表达式长度超限: " + expression.length() + " > " + config.getMaxExpressionLength());
        }

        try {
            Expression compiled = expressionCache.getCompiledExpression(expression);
            return compiled.execute(env);
        } catch (Exception e) {
            log.error("Aviator表达式执行失败: expression={}", expression, e);
            throw new FormulaException("表达式执行错误: " + e.getMessage(), e);
        }
    }

    public BigDecimal executeDecimal(String expression, Map<String, Object> env) {
        Object result = execute(expression, env);
        if (result instanceof BigDecimal bd) {
            return bd;
        }
        if (result instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        throw new FormulaException("表达式结果无法转换为 BigDecimal: " + result.getClass());
    }

    public Boolean executeBoolean(String expression, Map<String, Object> env) {
        Object result = execute(expression, env);
        if (result instanceof Boolean b) {
            return b;
        }
        throw new FormulaException("表达式结果无法转换为 Boolean: " + result.getClass());
    }

    public boolean validate(String expression) {
        try {
            AviatorEvaluator.compile(expression, true);
            return true;
        } catch (Exception e) {
            log.debug("Aviator表达式语法验证失败: {}", expression, e);
            return false;
        }
    }

    public Expression compile(String expression) {
        return expressionCache.getCompiledExpression(expression);
    }

    public void refresh(String expression) {
        expressionCache.invalidate(expression);
        log.info("Aviator expression cache refreshed: {}", expression);
    }

    public void refreshAll() {
        expressionCache.invalidateAll();
        log.info("All Aviator expression caches refreshed");
    }
}
```

**AviatorHelper.java** — 基础工具类

```java
package com.his.common.aviator.helper;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
public class AviatorHelper {

    public static Expression compile(String expression) {
        return AviatorEvaluator.compile(expression, true);
    }

    public static Expression compile(String expression, boolean cached) {
        return AviatorEvaluator.compile(expression, cached);
    }

    public static Object execute(String expression, Map<String, Object> env) {
        Expression compiled = compile(expression);
        return compiled.execute(env);
    }

    public static BigDecimal executeDecimal(String expression, Map<String, Object> env) {
        Object result = execute(expression, env);
        if (result instanceof BigDecimal bd) {
            return bd.setScale(2, RoundingMode.HALF_UP);
        }
        if (result instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }
        throw new IllegalArgumentException("无法转换为 BigDecimal: " + result.getClass());
    }

    public static boolean validate(String expression) {
        try {
            AviatorEvaluator.compile(expression, true);
            return true;
        } catch (Exception e) {
            log.debug("表达式语法验证失败: {}", e.getMessage());
            return false;
        }
    }

    public static String getValidationError(String expression) {
        try {
            AviatorEvaluator.compile(expression, true);
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static boolean containsDangerousFunctions(String expression) {
        String lower = expression.toLowerCase();
        String[] dangerous = {"system", "runtime", "exec", "process", "class", "reflect"};
        for (String fn : dangerous) {
            if (lower.contains(fn + "(")) {
                log.warn("检测到潜在危险函数: {}", fn);
                return true;
            }
        }
        return false;
    }
}
```

### 3.3 Drools 规则文件示例

```drools
import com.his.common.SettlementFact

rule "1. 身份校验"
    when
        $f: SettlementFact(patientType == null || patientType == "")
    then
        $f.addResult(ResultLevel.BLOCK, "IdentityCheck", "患者身份缺失，拒绝结算");
        $f.setFinalAmount(BigDecimal.ZERO);
        update($f);
end

rule "2. 起付线确定"
    when
        $f: SettlementFact(patientType == "职工", totalFee != null)
    then
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
        String formula = "round((totalFee - deductible) * ratio, 2)";
        
        Map<String, Object> env = new java.util.HashMap<>();
        env.put("totalFee", $f.getTotalFee());
        env.put("deductible", $f.getDeductible());
        env.put("ratio", $f.getRatio());
        
        BigDecimal amount = AviatorHelper.executeDecimal(formula, env);
        $f.setFinalAmount(amount);
        
        update($f);
end
```

### 3.4 调用入口（Skill/Agent 模式）

项目已采用 Skill/Agent 插件化架构，替代传统的 `KieSession` 直接调用：

```java
package com.his.settlement.skill;

import com.his.common.*;
import com.his.common.aviator.engine.AviatorEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementCalculateSkill implements ISkill<SettlementFact> {

    private final AviatorEngine aviatorEngine;

    @Override
    public String supportEvent() {
        return HisEventType.SETTLEMENT_EXECUTE;
    }

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public void execute(SkillContext<SettlementFact> context) {
        if (context.hasBlock()) {
            return;
        }

        SettlementFact fact = context.getPayload();

        try {
            String formula = "round((totalFee - deductible) * ratio, 2)";
            Map<String, Object> env = Map.of(
                "totalFee", fact.getTotalFee(),
                "deductible", fact.getDeductible(),
                "ratio", fact.getRatio()
            );

            BigDecimal amount = aviatorEngine.executeDecimal(formula, env);
            fact.setFinalAmount(amount);

            context.addResult(new SkillResult(
                ResultLevel.PASS, "SettlementCalculateSkill", "结算计算完成"));
        } catch (Exception e) {
            log.error("结算计算失败", e);
            context.addResult(new SkillResult(
                ResultLevel.WARN, "SettlementCalculateSkill", "计算异常: " + e.getMessage()));
        }
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

### Nacos 公式同步监听器（项目实际实现）

```java
package com.his.formula.listener;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.formula.entity.AviatorFormula;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NacosFormulaSyncListener {

    private final NacosConfigManager nacosConfigManager;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String FORMULA_DATA_ID_PREFIX = "his-formula-";
    private static final String FORMULA_GROUP = "HIS_RULE_ENGINE";

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

    private void removeFromRedis(AviatorFormula formula) {
        try {
            String key = buildRedisKey(formula.getFormulaKey(), formula.getTenantId());
            redisTemplate.delete(key);
            log.debug("公式已从Redis删除: key={}", key);
        } catch (Exception e) {
            log.warn("从Redis删除公式失败: formulaKey={}", formula.getFormulaKey(), e);
        }
    }

    private String buildDataId(String formulaKey, String tenantId) {
        return FORMULA_DATA_ID_PREFIX + tenantId + "-" + formulaKey;
    }

    private String buildRedisKey(String formulaKey, String tenantId) {
        return "formula:" + tenantId + ":" + formulaKey;
    }
}
```

### 公式发布事件

```java
package com.his.formula.listener;

import com.his.formula.entity.AviatorFormula;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FormulaPublishEvent extends ApplicationEvent {

    private final AviatorFormula formula;
    private final String action;

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

### 公式语法校验器（项目实际实现）

```java
package com.his.formula.validator;

import com.his.common.aviator.helper.AviatorHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

## 五、这样混合的好处

| 好处 | 说明 |
| :--- | :--- |
| **规则编排清晰** | Drools 管理规则流（条件、优先级、分组），维护方便 |
| **计算性能极高** | Aviator 处理数学/逻辑表达式，比 Java 反射快 10~100 倍 |
| **公式热更新** | 修改数据库中的 formula_text，无需重启 Drools 会话，实时生效 |
| **降低 Drools 复杂度** | 避免在 DRL 中编写复杂的算术公式，代码可读性更高 |
| **便于业务人员维护** | 公式字符串可放在配置后台，供运营或医保专员直接修改 |

---

## 六、微服务模块说明

项目采用微服务架构，各模块职责如下：

| 微服务 | 端口 | 职责 | 依赖引擎 |
| :--- | :--- | :--- | :--- |
| **his-gateway** | 9000 | API 网关：路由、CORS、JWT(可配置) | Spring Cloud Gateway |
| **his-rule-service** | 9001 | 规则管理：规则 CRUD、版本管理、DRL 发布、规则流 | Drools 8.44 |
| **his-formula-service** | 9002 | 公式管理：公式 CRUD、语法校验、Nacos 同步 | Aviator 5.4.3 + Nacos + Redis |
| **his-settlement-service** | 9003 | 医保结算：报销计算、规则执行、沙箱测试 | Drools + Aviator |
| **his-drug-service** | 9004 | 合理用药：处方审核、药物相互作用、极量检查 | Drools |
| **his-quality-service** | 9005 | 质控管理：院感预防、质控规则、拦截控制 | Drools + Aviator |
| **his-drg-service** | 9006 | DRG/DIP：分组、权重计算、标准分 | Aviator |
| **his-monitor-service** | 9007 | 监控服务：健康检查、指标采集 | - |
| **his-market-service** | 9008 | 规则市场：规则共享、模板管理 | - |

### 公共模块（his-common）

| 模块 | 职责 |
| :--- | :--- |
| **his-common-core** | 核心实体：SettlementFact、ErrorCode、ISkill、SkillResult、ResultLevel、HisEventType 等 |
| **his-common-web** | Web 公共组件：统一响应、全局异常、租户上下文 |
| **his-common-drools** | Drools 引擎封装：KieSession 管理、规则加载、KieBase 分组 |
| **his-common-aviator** | Aviator 引擎封装：AviatorEngine、AviatorExpressionCache（Caffeine）、AviatorConfig、AviatorHelper |

---

## 七、组合效果总结

| 能力 | 实现方式 |
| :--- | :--- |
| **表达式编译性能** | Caffeine 缓存 Expression 对象，maximumSize=5000，30分钟过期，编译耗时>1ms告警 |
| **公式热更新** | Spring Event 发布公式事件 → NacosFormulaSyncListener 监听 → 同步到 Nacos + Redis 二级缓存 → 消费方拉取新公式重新编译 |
| **规则与公式隔离** | Drools 只负责条件判断和流向，具体数值计算委托给 Aviator + 动态公式 |
| **业务友好** | 公式存储在 MySQL，通过 his-formula-service 管理，发布后自动同步到 Nacos，各微服务实时感知 |
| **多租户隔离** | 公式按 tenantId 隔离，Nacos Data ID 和 Redis Key 均包含租户标识 |
| **安全校验** | FormulaValidator 校验公式语法 + 危险函数检测（system/runtime/exec/process/class/reflect） |
| **异常降级** | AviatorEngine 封装 FormulaException，Skill 层捕获异常降级为 WARN |

---

## 八、生产环境注意事项

1. **表达式缓存大小**：HIS 系统中公式数量通常不超过几百个，设置 `maximumSize(5000)` 足够（见 `.trae/rules/performance.md`）。
2. **Nacos 推送延迟**：配置变更后，客户端默认 10 秒轮询拉取。如需秒级生效，可调整 `spring.cloud.nacos.config.refreshInterval`。
3. **降级策略**：当 Nacos 不可用或公式语法错误时，应记录日志并返回默认值（如 0），避免结算中断。
4. **灰度发布**：通过 Nacos namespace 实现不同环境（dev/test/prod）或不同院区的公式隔离。
5. **Redis 二级缓存**：Redis 作为 Nacos 的补充缓存，TTL 设为 24 小时，Redis 不可用时不影响主流程（仅 warn 日志）。
6. **微服务间调用**：his-settlement-service 等通过 OpenFeign 调用 his-formula-service 获取公式，需配置超时和重试策略。
7. **Aviator 不是完整规则引擎**：它只负责计算，不做条件分支（分支条件仍在 Drools 中）。
8. **类型一致性**：Drools 和 Aviator 之间传递的数值统一使用 `BigDecimal`，避免浮点精度问题。
9. **Jakarta EE 迁移**：Spring Boot 3.5.0 使用 Jakarta EE（`jakarta.*` 包），不再使用 `javax.*`。
10. **租户隔离**：所有公式和规则查询强制过滤 `tenant_id`，通过 `TenantContext` 传递。

---

最后更新: 2026-05-17 | 基于项目实际代码更新 v2.0
