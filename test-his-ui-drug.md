为了将“规则剥离/按能力加载（Skill/Agent）”的理念真正落地，支持全院级 HIS 系统的所有业务场景（医保、用药、质控等），下面按照你之前提供的结构，为你生成一份**全院级 HIS 动态规则中台（Skill/Agent 架构）**的完整技术落地示例。

结合 `his-drug`（用药）等项目背景，这里的代码示例将以 **全院事件驱动 + 合理用药 Agent** 为切入点。

---

## 一、整体架构图（文字版）

```text
[ 医生站 / 护士站 / 计费系统 ] (退化为纯前端+数据底座)
       ↓ 1. 触发业务动作 (如：保存处方、点击结算)
[ HIS 核心网关 / 基础服务 ] 
       ↓ 2. 抛出标准化事件 (如：EVENT_DRUG_PRESCRIBE)
=======================================================================
[ Skill Gateway 智能调度网关 ] ← 核心中枢
       │
       ├─ 动态加载配置 (Nacos / Apollo)
       │      └─ 维护当前激活的 Skill 列表及版本
       │
       └─ 根据 EventType 匹配并穿透执行 Skill Pipeline
              │
              ├─ [Skill 1: 医保限制 Agent] → (通过)
              ├─ [Skill 2: 合理用药 Agent] → 触发 Drools + Aviator 计算 → (警告/阻断)
              └─ [Skill 3: 院感拦截 Agent] → (通过)
                     ↓
[ Skill 调度网关 ] 汇总执行结果 (Result 集合)
=======================================================================
       ↓ 3. 返回标准化干预指令 (PASS / WARN / BLOCK)
[ HIS 核心服务 ] ← 根据指令：放行提交 / 弹窗警告 / 强行回滚事务
```

---

## 二、核心设计要点

| 组件 | 职责 | 数据载体 / 核心类 |
| :--- | :--- | :--- |
| **标准事件定义** | 定义全院统一的拦截点常量 | `HisEventType.java` |
| **统一上下文** | 封装各类业务事实（Fact），屏蔽底层差异 | `SkillContext<T>.java` |
| **能力标准接口** | 所有业务 Agent 必须实现的契约 | `ISkill.java` |
| **调度网关** | 负责事件路由、Pipeline 组装与结果汇总 | `SkillGateway.java` |
| **动态规则引擎** | 封装 Drools (逻辑卡控) + Aviator (数值计算) | `RuleEngineTemplate.java` |

---

## 三、实战代码示例

### 1. 统一上下文与结果定义（SkillContext & SkillResult）

不再为每个业务写死 Fact，而是使用泛型上下文。

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 统一干预级别
public enum ResultLevel {
    PASS,   // 放行
    WARN,   // 警告（弹窗提示，可忽略）
    BLOCK;  // 阻断（强拦截，事务回滚）
}

// 规则执行结果
public class SkillResult {
    private ResultLevel level;
    private String sourceSkill; // 哪个Agent产生的
    private String message;     // 提示内容
    // getters/setters...
}

// 统一上下文
public class SkillContext<T> {
    private String eventType;      // 事件类型 (如 DRUG_PRESCRIBE)
    private String tenantId;       // 医院/租户ID
    private T payload;             // 业务原始数据（处方、结算单等）
    private Map<String, Object> ext;// 扩展参数
    
    private List<SkillResult> results = new ArrayList<>();

    public SkillContext(String eventType, T payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public void addResult(SkillResult result) {
        this.results.add(result);
    }
    
    public boolean hasBlock() {
        return results.stream().anyMatch(r -> r.getLevel() == ResultLevel.BLOCK);
    }
}
```

### 2. 标准能力接口（ISkill）

```java
public interface ISkill<T> {
    /** 声明关注的事件类型 */
    String supportEvent();
    
    /** 执行优先级（越小越先执行） */
    int getOrder();
    
    /** 核心执行逻辑 */
    void execute(SkillContext<T> context);
}
```

### 3. 核心网关（SkillGateway）

负责拦截 HIS 系统的请求，并分发给对应的 Agent。

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SkillGateway {

    // Spring 自动注入所有实现了 ISkill 的 Bean (可结合热加载机制动态刷新)
    @Autowired
    private List<ISkill> allSkills; 

    public <T> SkillContext<T> fireEvent(String eventType, T payload) {
        SkillContext<T> context = new SkillContext<>(eventType, payload);

        // 1. 过滤出关注该事件的 Skill，并按优先级排序
        List<ISkill> targetSkills = allSkills.stream()
                .filter(skill -> skill.supportEvent().equals(eventType))
                .sorted(Comparator.comparingInt(ISkill::getOrder))
                .collect(Collectors.toList());

        // 2. Pipeline 穿透执行
        for (ISkill skill : targetSkills) {
            skill.execute(context);
            
            // 3. 如果出现强阻断，快速失败，不再执行后续规则
            if (context.hasBlock()) {
                break;
            }
        }

        return context; // 返回最终的干预结果集合
    }
}
```

### 4. 具体 Agent 实现（以合理用药为例）

结合 Drools 和 Aviator 实现的一个具体 Skill。

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RationalDrugUseSkill implements ISkill<PrescriptionDTO> {

    @Autowired
    private RuleEngineTemplate ruleEngine; // 封装了 Drools 和 Aviator 的统一模板

    @Override
    public String supportEvent() {
        return "EVENT_DRUG_PRESCRIBE"; // 拦截开处方事件
    }

    @Override
    public int getOrder() {
        return 10; // 优先级排在医保校验之后
    }

    @Override
    public void execute(SkillContext<PrescriptionDTO> context) {
        // 1. 获取动态 DRL 规则 (可从 Nacos 或 DB 动态获取)
        String ruleGroup = "DRUG_RULES_" + context.getTenantId();
        
        // 2. 将上下文直接丢入规则引擎
        // 在 DRL 中可以直接调用 context.addResult(new SkillResult(BLOCK, "配伍禁忌..."))
        // 在 DRL 中也可调用 Aviator 进行极量公式计算
        ruleEngine.fireRules(ruleGroup, context);
    }
}
```

---

## 四、动态 Agent 的来源（生产环境演进路径）

随着架构深化，Agent（Skill）的加载方式需要越来越灵活：

| 阶段 | 加载方式 | 适用场景 | 运维人员 |
| :--- | :--- | :--- | :--- |
| **V1: 静态 Bean 注入** | Spring `@Service` + DRL 从数据库动态读取 | 逻辑主干稳定，仅阈值、判断条件、公式变化（占 80% 场景） | 实施工程师修改 Nacos/DB 中的规则文本 |
| **V2: 脚本化 Agent** | 使用 Groovy 脚本直接实现 `ISkill` 接口，数据库存储脚本 | 新增简单的自定义卡控逻辑（如：某个科室周末禁开某药） | HIS 二次开发工程师编写 Groovy |
| **V3: Jar 包热加载** | 通过自定义 `ClassLoader` 动态加载编译好的独立 Jar 包 | 极其复杂的全新业务逻辑接入（如全新的 DRG/DIP 分组结算引擎） | 平台研发团队下发新版本 Agent |

---

## 五、这样剥离的好处（对 HIS 架构的颠覆）

| 收益维度 | 传统 HIS 架构 | Skill/Agent 中台架构 |
| :--- | :--- | :--- |
| **系统稳定性** | 改一个医保规则，可能导致处方保存接口崩溃。 | 规则沙箱隔离。某个 Skill 抛异常，网关可降级为 PASS，**HIS 永不宕机**。 |
| **扩展性** | 每接入一家第三方监管平台，HIS 核心代码要写一堆 if-else。 | 新增一个实现 `ISkill` 的 Agent 即可，**HIS 底座零代码修改**。 |
| **复用能力** | 门诊收费、住院结算的医保校验逻辑散落在两处。 | 只需抛出 `EVENT_FEE_CHECK` 事件，**底层共享同一个计费 Agent**。 |
| **商业模式** | 靠堆人头驻场改 Bug、改硬编码赚取人天费。 | **规则即服务 (RaaS)**：卖引擎底座，各医院自主沉淀知识库，形成护城河。 |

---

## 六、注意事项

1. **事务一致性边界**
   * **同步拦截（BLOCK）**：必须在 HIS 数据库事务 `Commit` 之前调用网关。一旦返回 BLOCK，HIS 直接触发 `Rollback` 并透传 Message 给前端弹窗。
   * **异步分析（WARN）**：对于“病历质控”等耗时超过 500ms 的长规则，建议 HIS 通过 MQ 抛出事件，不阻塞当前流程，Agent 算完后通过 WebSocket 推送警告。
2. **性能压舱石**
   * 网关拦截是全院级别的，调用频率极高。
   * 务必对 `Drools KieBase` 和 `Aviator Expression` 进行 **Caffeine 本地缓存**。
   * 复杂上下文组装（如查阅历史 90 天用药记录）应做到**按需懒加载 (Lazy Loading)**，只有当 Drools 规则确实用到该条件时，再去 DB 查询。
3. **闭环反馈机制**
   * 当 Agent 给出 `WARN` 级别的拦截时，医生如果选择“强制双签名忽略”，该行为及原因应当回调并记录到 Rule 库中，用于后期**反哺优化规则引擎**，防止频繁误报导致的“警告疲劳”。