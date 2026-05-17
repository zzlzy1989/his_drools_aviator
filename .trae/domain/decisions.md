---
alwaysApply: true
---
# 决策记录 - HIS Drools+Aviator 规则引擎

> 记录为什么这么做，传承知识。按类别分组：架构决策 / 数据模型 / 集成方案 / 业务决策

---

## 决策目录（v1.1 — 共 30 条）

### 架构决策 (D001-D010)
| 编号 | 决策 | 日期 | 状态 |
|------|------|------|------|
| D001 | 双引擎架构：Drools 规则匹配 + Aviator 表达式计算 | 2026-04-26 | ✅ 已执行 |
| D002 | Skill/Agent 插件化架构 | 2026-04-26 | ✅ 已执行 |
| D003 | Caffeine 本地缓存 + Redis 分布式缓存双层架构 | 2026-04-26 | ✅ 已执行 |
| D004 | Nacos 配置中心统一管理规则参数 | 2026-04-26 | ✅ 已执行 |
| D005 | API 前缀统一为 /api/v1/ | 2026-04-26 | ✅ 已执行 |
| D006 | 统一响应格式 Result\<T\> + 分页 PageResult\<T\> | 2026-04-26 | ✅ 已执行 |
| D007 | 全局异常处理器 + 错误码体系 | 2026-04-26 | ✅ 已执行 |
| D008 | BigDecimal 强制用于金额计算 | 2026-04-26 | ✅ 已执行 |
| D009 | 微服务架构：按业务域拆分为独立服务 | 2026-05-01 | ✅ 已执行 |
| D010 | Spring Cloud Gateway 统一网关 + JWT 认证 | 2026-05-01 | ✅ 已执行 |

### 数据模型 (D011-D018)
| 编号 | 决策 | 日期 | 状态 |
|------|------|------|------|
| D011 | 规则定义与规则实例分离存储 | 2026-04-26 | ✅ 已执行 |
| D012 | Aviator 公式独立表存储（支持版本管理） | 2026-04-26 | ✅ 已执行 |
| D013 | 结算结果与费用明细一对多关系 | 2026-04-26 | ✅ 已执行 |
| D014 | MySQL utf8mb4 字符集强制要求 | 2026-04-26 | ✅ 已执行 |
| D015 | 审计日志独立表（非 JSONField） | 2026-04-26 | ✅ 已执行 |
| D016 | 时间字段统一使用 LocalDateTime + Jackson 序列化 | 2026-04-26 | ✅ 已执行 |
| D017 | RuleFlow 节点和边使用 JSON 存储 | 2026-05-09 | ✅ 已执行 |
| D018 | Fact 对象拆分为多个类型（SettlementFact/CostFact/DrgFact 等） | 2026-05-08 | ✅ 已执行 |

### 集成方案 (D019-D024)
| 编号 | 决策 | 日期 | 状态 |
|------|------|------|------|
| D019 | Drools KieBase 按业务域分组加载 | 2026-04-26 | ✅ 已执行 |
| D020 | Aviator Expression 对象池化 + 编译缓存 | 2026-04-26 | ✅ 已执行 |
| D021 | Docker 部署禁止容器内启动 MySQL | 2026-04-26 | ✅ 已执行 |
| D022 | 规则热更新支持（无需重启服务） | 2026-04-26 | ✅ 已执行 |
| D023 | Spring Cloud Alibaba 统一依赖（Nacos/Sentinel/Gateway） | 2026-05-01 | ✅ 已执行 |
| D024 | Skill 独立类实现 + Pipeline 重构 | 2026-05-08 | ✅ 已执行 |

### 业务决策 (D025-D030)
| 编号 | 决策 | 日期 | 状态 |
|------|------|------|------|
| D025 | 医保结算采用分步计算策略（先自费→再医保→最后补充） | 2026-04-26 | ✅ 已执行 |
| D026 | 7 步工作流程含 3 个阻塞节点（规划/审查/公式验证） | 2026-04-26 | ✅ 已执行 |
| D027 | 前端 Vue 3 + Vite + Element Plus 技术栈 | 2026-04-26 | ✅ 已执行 |
| D028 | RuleFlow 可视化编排使用 X6/LogicFlow | 2026-05-09 | ⬜ 规划中 |
| D029 | DRG 分组采用独立微服务 | 2026-04-27 | ✅ 已执行 |
| D030 | 合理用药审核采用独立微服务 | 2026-04-27 | ✅ 已执行 |

---

## 架构决策详情

### D001: 双引擎架构
**问题**: 为什么同时使用 Drools 和 Aviator？
**选择**: Drools 负责规则匹配和流程控制，Aviator 负责表达式计算
**理由**:
- Drools 擅长复杂业务规则推理（if-then-else、规则优先级、冲突解决）
- Aviator 擅长数学表达式解析和动态公式计算（支持自定义函数）
- 职责分离：规则逻辑与计算逻辑解耦，便于维护
- 性能优化：各自在擅长领域达到最优性能
**替代方案**: A — 仅用 Drools（❌ 表达式编写复杂）；B — 仅用 Aviator（❌ 缺乏规则推理能力）

---

### D002: Skill/Agent 插件化架构
**问题**: 如何支持不同类型的规则处理？
**选择**: SPI 机制 + Spring 自动装配实现插件化
**理由**:
- 新增规则类型只需实现接口，无需修改核心代码
- 支持运行时动态加载/卸载 Skill
- 便于单元测试和集成测试
- 符合开闭原则（对扩展开放，对修改关闭）
**核心接口**:
```java
public interface ISkill<T> {
    String supportEvent();
    int getOrder();
    void execute(SkillContext<T> context);
}
```

---

### D003: 双层缓存架构
**问题**: 如何平衡性能和数据一致性？
**选择**: Caffeine L1（本地高速）+ Redis L2（分布式共享）
**理由**:
- Caffeine：纳秒级访问，适合热点数据（编译后的 Expression、KieSession）
- Redis：跨实例共享，适合规则配置、字典数据
- L1 → L2 → DB 三级降级，保证可用性
- Cache Aside 模式：先更新 DB，再删除缓存

---

### D004: Nacos 配置中心
**问题**: 动态规则参数如何管理？
**选择**: Nacos 统一配置 + @RefreshScope 动态刷新
**理由**:
- 支持配置变更实时生效（无需重启）
- 多环境隔离（dev/test/prod 命名空间）
- 配置版本管理和回滚能力
- 与 Spring Cloud 原生集成

---

### D009: 微服务拆分架构
**问题**: 单一服务还是多个微服务？
**选择**: 按业务域拆分为独立服务（his-rule-service/his-drug-service/his-drg-service/his-quality-service/his-settlement-service）+ his-gateway 网关
**理由**:
- 各业务域职责独立，可独立部署和扩缩容
- 规则引擎核心独立，不受其他服务升级影响
- 用药审核/DRG/质控可并行迭代
- 网关统一路由、认证、限流

---

### D010: 网关统一认证
**问题**: 微服务间如何统一认证？
**选择**: Spring Cloud Gateway + JWT Token 在网关层统一认证
**理由**:
- 网关作为唯一入口，统一鉴权
- JWT 无状态，无需共享 Session
- 配合 Sentinel 实现接口级限流
- TenantContextFilter 自动提取租户 ID 注入下游

---

## 数据模型决策详情

### D017: RuleFlow JSON 存储
**问题**: 流程节点和边如何存储？
**选择**: `nodes` 和 `edges` 字段使用 JSON 类型存储
**理由**:
- 流程拓扑结构复杂，关系型表设计冗余
- JSON 灵活支持新增节点类型
- 后端解析为对象树执行
- 便于前端 FlowEditor 直接对接

---

### D018: Fact 对象分类
**问题**: 单一 Fact 还是多种 Fact？
**选择**: 按业务场景拆分为 SettlementFact/CostFact/PrescriptionFact/DrgFact/InfectionFact/QualityFact
**理由**:
- 不同场景所需字段差异大，单一 Fact 字段过多
- Fact 类型对应不同服务（结算/用药/DRG/质控）
- Drools 规则可按 Fact 类型隔离
- 降低各模块间耦合

---

## 集成方案决策详情

### D023: Spring Cloud Alibaba 统一
**问题**: 微服务治理选型？
**选择**: Spring Cloud Alibaba 2025.1.0.0 全家桶（Nacos 3.x + Sentinel + Gateway）
**理由**:
- Nacos 3.x 支持 MCP 协议，扩展性强
- Sentinel 提供完善限流/熔断/降级
- Gateway 原生 Spring Cloud 集成
- 国内生态完善，社区活跃

---

### D024: Skill 独立类 + Pipeline 重构
**问题**: Skill 如何组织和执行？
**选择**: 每个 Skill 为独立的 @Service 实现 ISkill 接口，通过 SkillPipelineExecutor 按 getOrder() 升序串联执行
**理由**:
- 每个 Skill 可独立测试和部署
- Pipeline 执行顺序由 getOrder() 控制，清晰可控
- BLOCK 级别立即终止，快速失败
- 新增 Skill 只需实现接口并注册为 Spring Bean
**核心实现**:
```java
public class SkillPipelineExecutor {
    private final List<ISkill<?>> skills;
    public void execute(SkillContext<?> context) {
        for (ISkill<?> skill : skills) {
            skill.execute(context);
            if (context.isBlocked()) break;
        }
    }
}
```

---

## 业务决策详情

### D025: 分步结算策略
**问题**: 医保结算计算顺序如何设计？
**选择**: 先自费 → 再医保报销 → 最后补充保险
**理由**:
- 符合医保政策规定的计算顺序
- 每步可独立验证和调整
- 便于向患者解释费用构成
- 支持多险种叠加场景

---

### D026: 7 步流程 + 3 个阻塞节点
**问题**: 工作流程如何平衡效率和质量？
**选择**: Step 2(规划)、Step 4(审查)、Step 5(公式验证) 为阻塞节点
**理由**:
- 阻塞节点确保方向正确后再投入大量编码工作
- 公式验证额外阻塞：金额计算错误后果严重
- 非阻塞步骤允许 AI 自主推进（提升效率）
- Bug 修复只保留 Step 4 一个阻塞点

---

### D029: DRG 独立微服务
**问题**: DRG 分组功能放在哪个服务？
**选择**: his-drg-service 独立服务，提供 DRG 分组计算 API
**理由**:
- DRG 分组算法复杂，独立部署可单独扩缩容
- 与结算服务解耦，分组结果供结算服务调用
- DRG 政策调整频繁，独立版本管理

---

### D030: 合理用药独立微服务
**问题**: 用药审核功能放在哪个服务？
**选择**: his-drug-service 独立服务，提供处方审核 API
**理由**:
- 药品目录/配伍禁忌数据量大，独立存储
- 处方审核与结算解耦，可前置审核
- 用药审核规则独立于报销规则

---

最后更新: 2026-05-10 | v1.1 (共 30 条决策, 分 4 类)
