为了将“规则剥离/按能力加载（Skill/Agent）”的理念真正落地，支持全院级 HIS 系统的所有业务场景（医保、用药、质控等），下面按照你之前提供的结构，为你生成一份**全院级 HIS 动态规则中台（Skill/Agent 架构）**的完整技术落地示例。

结合 `his-drug-service`（用药）等项目背景，这里的代码示例将以 **全院事件驱动 + 合理用药 Skill** 为切入点。

---

## 一、整体架构图（微服务版）

```text
[ 医生站 / 护士站 / 计费系统 ] (前端 UI)
       ↓ 1. 触发业务动作 (如：保存处方、点击结算)
[ his-gateway (:9000) ]  ← Spring Cloud Gateway + Sentinel 限流 + JWT 鉴权
       ↓ 2. 路由到对应微服务
=======================================================================
[ 业务微服务 ] ← his-drug-service / his-settlement-service / his-quality-service
       │
       │ 3. 构建 SkillContext<T>，触发事件
       │
       ▼
[ Skill Pipeline 执行 ] ← 微服务内部 Spring 注入所有 ISkill Bean
       │
       ├─ [Skill 1: 医保限制] → (通过)
       ├─ [Skill 2: 合理用药] → 触发 Drools + Aviator 计算 → (警告/阻断)
       └─ [Skill 3: 院感拦截] → (通过)
              ↓
       汇总执行结果 (List<SkillResult>)
=======================================================================
       ↓ 4. 返回标准化干预指令 (PASS / WARN / BLOCK)
[ 前端 UI ] ← 根据指令：放行提交 / 弹窗警告 / 强行阻断
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
| **Nacos** | 3.2.0 | 注册中心 + 配置中心 |

---

## 二、核心设计要点

| 组件 | 职责 | 实际文件 |
| :--- | :--- | :--- |
| **事件类型常量** | 定义全院统一的拦截点常量 | `HisEventType.java` |
| **统一上下文** | 封装各类业务事实（Fact），屏蔽底层差异 | `SkillContext<T>.java` |
| **能力标准接口** | 所有业务 Skill 必须实现的契约 | `ISkill.java` |
| **执行结果** | 标准化干预指令（PASS/WARN/BLOCK） | `SkillResult.java` |
| **结果级别** | 枚举：放行/警告/阻断 | `ResultLevel.java` |
| **错误码** | 统一错误编码体系 | `ErrorCode.java` |
| **核心 Fact** | 结算/用药等业务数据载体 | `SettlementFact.java` |

---

## 三、实战代码示例

### 1. 统一上下文与结果定义（项目实际实现）

所有 Skill 相关核心类位于 `com.his.common` 包（his-common-core 模块）。

**ResultLevel.java** — 干预级别枚举

```java
package com.his.common;

public enum ResultLevel {
    PASS,   // 放行
    WARN,   // 警告（弹窗提示，可忽略）
    BLOCK;  // 阻断（强拦截，事务回滚）
}
```

**SkillResult.java** — 规则执行结果

```java
package com.his.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillResult {

    private ResultLevel level;
    private String source;      // 产生该结果的 Skill 名称
    private String message;     // 提示/错误内容
}
```

**SkillContext<T>** — 统一上下文（泛型）

```java
package com.his.common;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class SkillContext<T> {

    private String tenantId;
    private String eventType;
    private T payload;
    private List<SkillResult> results = new ArrayList<>();

    public void addResult(SkillResult result) {
        results.add(result);
    }

    public boolean hasBlock() {
        return results.stream().anyMatch(r -> r.getLevel() == ResultLevel.BLOCK);
    }
}
```

**HisEventType.java** — 事件类型常量

```java
package com.his.common;

public final class HisEventType {

    private HisEventType() {}

    public static final String SETTLEMENT_EXECUTE = "SETTLEMENT_EXECUTE";
    public static final String PRESCRIPTION_CHECK = "PRESCRIPTION_CHECK";
    public static final String DRUG_AUDIT = "DRUG_AUDIT";
    public static final String INFECTION_CONTROL = "INFECTION_CONTROL";
    public static final String QUALITY_CONTROL = "QUALITY_CONTROL";
    public static final String DRG_GROUP = "DRG_GROUP";
}
```

**ErrorCode.java** — 统一错误码

```java
package com.his.common;

public enum ErrorCode {

    SUCCESS("0", "操作成功"),

    BUSINESS_PATIENT_INFO_MISSING("HIS-001", "患者身份信息缺失"),
    BUSINESS_RULE_NOT_FOUND("HIS-002", "规则不存在"),
    BUSINESS_SETTLEMENT_IN_PROGRESS("HIS-003", "结算正在进行中"),

    FORMULA_SYNTAX_ERROR("HIS-101", "公式语法错误"),
    FORMULA_PARAM_MISMATCH("HIS-102", "公式参数不匹配"),
    FORMULA_CALC_OVERFLOW("HIS-103", "公式计算溢出"),

    SETTLEMENT_DUPLICATE("HIS-201", "重复结算"),
    SETTLEMENT_REJECTED("HIS-202", "审核拒绝"),

    PERMISSION_DENIED("HIS-301", "无租户权限"),

    SECURITY_INJECTION_DETECTED("HIS-401", "Aviator注入检测"),

    SYSTEM_CONFIG_UNAVAILABLE("HIS-901", "配置中心不可用"),
    SYSTEM_KIE_COMPILE_FAILED("HIS-902", "KIE编译失败"),
    SYSTEM_INTERNAL_ERROR("HIS-999", "系统内部异常");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
```

### 2. 标准能力接口（ISkill — 项目实际实现）

```java
package com.his.common;

public interface ISkill<T> {

    String supportEvent();

    int getOrder();

    void execute(SkillContext<T> context);
}
```

**使用示例**：

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class RationalDrugUseSkill implements ISkill<PrescriptionDTO> {

    private final RuleEngineTemplate ruleEngine;

    @Override
    public String supportEvent() {
        return HisEventType.PRESCRIPTION_CHECK;
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void execute(SkillContext<PrescriptionDTO> context) {
        if (context.hasBlock()) {
            return;
        }
        try {
            String ruleGroup = "DRUG_RULES_" + context.getTenantId();
            ruleEngine.fireRules(ruleGroup, context);
        } catch (Exception e) {
            log.error("合理用药规则执行失败", e);
            context.addResult(new SkillResult(
                ResultLevel.WARN, "RationalDrugUseSkill", "规则执行异常: " + e.getMessage()));
        }
    }
}
```

### 3. 核心网关（SkillGateway — 微服务内实现）

Skill 执行不是独立网关，而是嵌入各业务微服务内部。Spring 自动注入所有 `ISkill` Bean。

```java
package com.his.settlement.gateway;

import com.his.common.ISkill;
import com.his.common.SkillContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SkillGateway {

    private final List<ISkill> allSkills;

    public <T> SkillContext<T> fireEvent(String eventType, T payload) {
        SkillContext<T> context = new SkillContext<>();
        context.setEventType(eventType);
        context.setPayload(payload);

        List<ISkill> targetSkills = allSkills.stream()
                .filter(skill -> skill.supportEvent().equals(eventType))
                .sorted(Comparator.comparingInt(ISkill::getOrder))
                .collect(Collectors.toList());

        log.info("触发事件: {}, 匹配到 {} 个 Skill", eventType, targetSkills.size());

        for (ISkill skill : targetSkills) {
            skill.execute(context);

            if (context.hasBlock()) {
                log.warn("事件 {} 被阻断，来源: {}", eventType, skill.getClass().getSimpleName());
                break;
            }
        }

        return context;
    }
}
```

### 4. 具体 Skill 实现（以合理用药为例 — 项目实际方案）

结合 Drools 和 Aviator 实现的一个具体 Skill。

```java
package com.his.drug.skill;

import com.his.common.*;
import com.his.drug.dto.PrescriptionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RationalDrugUseSkill implements ISkill<PrescriptionDTO> {

    private final RuleEngineTemplate ruleEngine;

    @Override
    public String supportEvent() {
        return HisEventType.PRESCRIPTION_CHECK;
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void execute(SkillContext<PrescriptionDTO> context) {
        if (context.hasBlock()) {
            return;
        }
        try {
            String ruleGroup = "DRUG_RULES_" + context.getTenantId();
            ruleEngine.fireRules(ruleGroup, context);
        } catch (Exception e) {
            log.error("合理用药规则执行失败, tenantId={}", context.getTenantId(), e);
            context.addResult(new SkillResult(
                ResultLevel.WARN, "RationalDrugUseSkill", "规则执行异常: " + e.getMessage()));
        }
    }
}
```

---

## 四、动态 Skill 的来源（项目实际方案）

项目当前采用 **V1 阶段**：Spring `@Service` 静态注入 + DRL/公式从数据库动态读取。

| 阶段 | 加载方式 | 适用场景 | 状态 |
| :--- | :--- | :--- | :--- |
| **V1: 静态 Bean 注入** | Spring `@Service` + DRL 从数据库动态读取 | 逻辑主干稳定，仅阈值、判断条件、公式变化（占 80% 场景） | ✅ 已实现 |
| **V2: 脚本化 Skill** | 使用 Groovy 脚本直接实现 `ISkill` 接口，数据库存储脚本 | 新增简单的自定义卡控逻辑（如：某个科室周末禁开某药） | 规划中 |
| **V3: Jar 包热加载** | 通过自定义 `ClassLoader` 动态加载编译好的独立 Jar 包 | 极其复杂的全新业务逻辑接入（如全新的 DRG/DIP 分组结算引擎） | 规划中 |

**V1 阶段核心流程**：

```
[ 业务请求 ] → [ SkillGateway.fireEvent() ]
                      ↓
           Spring 注入所有 ISkill Bean
                      ↓
           按 supportEvent() 过滤 + getOrder() 排序
                      ↓
           Pipeline 顺序执行 → 异常时降级为 WARN
                      ↓
           返回 SkillContext（含 results 列表）
```

---

## 五、这样剥离的好处（对 HIS 架构的颠覆）

| 收益维度 | 传统 HIS 架构 | Skill 中台架构 |
| :--- | :--- | :--- |
| **系统稳定性** | 改一个医保规则，可能导致处方保存接口崩溃。 | 规则沙箱隔离。某个 Skill 抛异常，降级为 WARN，**HIS 永不宕机**。 |
| **扩展性** | 每接入一家第三方监管平台，HIS 核心代码要写一堆 if-else。 | 新增一个实现 `ISkill` 的 Skill 即可，**HIS 底座零代码修改**。 |
| **复用能力** | 门诊收费、住院结算的医保校验逻辑散落在两处。 | 只需抛出 `SETTLEMENT_EXECUTE` 事件，**底层共享同一个结算 Skill**。 |
| **商业模式** | 靠堆人头驻场改 Bug、改硬编码赚取人天费。 | **规则即服务 (RaaS)**：卖引擎底座，各医院自主沉淀知识库，形成护城河。 |

---

## 六、注意事项

1. **事务一致性边界**
   * **同步拦截（BLOCK）**：必须在 HIS 数据库事务 `Commit` 之前调用 SkillGateway。一旦返回 BLOCK，HIS 直接触发 `Rollback` 并透传 Message 给前端弹窗。
   * **异步分析（WARN）**：对于“病历质控”等耗时超过 500ms 的长规则，建议 HIS 通过 MQ 抛出事件，不阻塞当前流程，Skill 算完后通过 WebSocket 推送警告。
2. **性能压舱石**
   * 网关拦截是全院级别的，调用频率极高。
   * 务必对 `Drools KieBase` 和 `Aviator Expression` 进行 **Caffeine 本地缓存**。
   * 复杂上下文组装（如查阅历史 90 天用药记录）应做到**按需懒加载 (Lazy Loading)**，只有当 Drools 规则确实用到该条件时，再去 DB 查询。
3. **闭环反馈机制**
   * 当 Skill 给出 `WARN` 级别的拦截时，医生如果选择“强制双签名忽略”，该行为及原因应当回调并记录到 Rule 库中，用于后期**反哺优化规则引擎**，防止频繁误报导致的“警告疲劳”。
4. **微服务部署**
   * 项目采用微服务架构，7 个服务独立部署：his-gateway(:9000)、his-rule-service(:9001)、his-formula-service(:9002)、his-settlement-service(:9003)、his-drug-service(:9004)、his-quality-service(:9005)、his-drg-service(:9006)。
   * Skill 执行在各自业务微服务内部完成，不依赖独立网关服务。
5. **技术栈要求**
   * Java 21 + Spring Boot 3.5.0（Jakarta EE），所有注解使用 `jakarta.*` 包。
   * Drools 8.44.0.Final + Aviator 5.4.3 混合架构。
   * Nacos 3.2.0 作为注册中心和配置中心。