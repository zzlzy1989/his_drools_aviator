---
alwaysApply: true
---
# 业务规则 - HIS 动态规则中台

> 记录业务约束和隐式规则，每条带编号和来源

---

## 规则引擎核心规则

**BR-R01**: Drools 规则包（KIE Package）内规则名称不可重复
- 来源: `DrlRuleService.validateRuleName()` (rules/code-style.md)
- 影响: 部署/热更新规则时触发唯一性校验
- 例外: 不同 Rule Unit 下的规则可同名

**BR-R02**: 删除规则时级联停用所有关联的规则实例
- 来源: `RuleDefinition` 与 `RuleInstance` 关联关系 (rules/database.md)
- 影响: 规则删除需确认影响范围，建议先停用再归档
- 缓解: 软删除机制，保留历史版本

**BR-R03**: 只有租户成员才能查看/编辑该租户下的规则
- 来源: `TenantContextFilter` + RBAC 权限控制 (rules/security.md)
- 影响: 所有 Service 层查询必须过滤 tenant_id

**BR-R04**: 规则创建者自动成为 Owner，可授权其他角色编辑
- 来源: 多租户架构设计约定
- 影响: Owner 有完全控制权，普通成员权限由角色决定

**BR-R05**: 规则状态流转: 草稿(draft) → 已校验(validated) → 生效(active) → 停用(inactive)
- 来源: `RuleDefinition.Status` 枚举 (domain/state-machines.md)
- 影响: 只有 validated 状态才能发布生效

---

## 规则编排流程（RuleFlow）

**BR-FW01**: 流程定义必须包含唯一的 start 节点和至少一个 end 节点
- 来源: `RuleFlowValidator.validateStructure()`
- 影响: 保存前校验流程拓扑完整性

**BR-FW02**: 流程节点间的流转条件必须为有效的 Aviator 布尔表达式
- 来源: `FlowEdge.condition` 校验
- 影响: 条件表达式编译失败则拒绝保存流程

**BR-FW03**: 流程发布时自动校验所有引用的规则和公式是否已生效
- 来源: `RuleFlowService.publishFlow()`
- 影响: 防止流程引用未就绪的依赖

**BR-FW04**: 同一流程同租户只能有一个版本为 active 状态
- 来源: `rule_flow` 表唯一约束 (rules/database.md)
- 影响: 发布新版本时自动停用旧版本

**BR-FW05**: 流程版本支持回滚至任意历史版本
- 来源: `RuleFlowHistory` 历史记录表
- 影响: 回滚后原 active 版本自动 inactive

---

## 公式管理规则

**BR-F01**: Aviator 公式必须通过语法校验后才能保存
- 来源: `FormulaService.validateSyntax()` (rules/workflow.md Step 4)
- 影响: 语法错误的公式无法保存到数据库

**BR-F02**: 公式中引用的变量必须在参数定义表中注册
- 来源: `FormulaParam` 表外键约束 (rules/database.md)
- 影响: 未注册变量会导致运行时异常

**BR-F03**: 涉及金额计算的公式必须使用 BigDecimal 类型
- 来源: 精度要求 (rules/code-style.md)
- 影响: 禁止使用 double/float 进行医保结算计算

**BR-F04**: 公式版本号递增规则: 草稿修改不升级，每次发布自动 +1
- 来源: `Formula.version` 字段逻辑
- 影响: 版本号反映正式发布次数

**BR-F05**: 相同 formula_key 在同一租户下只能有一个生效版本
- 来源: UNIQUE KEY `key_tenant` (rules/database.md)
- 影响: 发布新版本时自动停用旧版本

---

## 结算执行规则

**BR-S01**: 医保结算只能使用"生效"状态的规则和公式
- 来源: `SettlementExecutor` 执行逻辑
- 影响: 草稿/停用的规则不出现在结算选择列表中

**BR-S02**: 结算结果一旦生成不可修改（只可重新结算）
- 来源: 医保数据完整性要求
- 影响: 如需修正需创建新的结算记录并注明原因

**BR-S03**: 结算流程状态机: 待处理→规则匹配→公式计算→结果审核→已完成
- 来源: `SettlementRecord` 状态字段 (domain/state-machines.md)
- 不同状态下允许的操作不同

**BR-S04**: 同一次就诊只产生一条有效结算记录
- 来源: 就诊 ID 唯一性约束
- 影响: 重复结算需作废原记录后再新建

**BR-S05**: 结算 Pipeline 中的 Skill 按 getOrder() 升序执行，BLOCK 级别立即终止
- 来源: `SkillPipelineExecutor` 执行逻辑
- 影响: BLOCK 结果阻断后续 Skill

---

## DRG 分组规则

**BR-D01**: DRG 入组必须同时校验主诊断和手术编码
- 来源: `DrgGroupService.grouping()` 入组算法
- 影响: 任一条件不满足则无法入组

**BR-D02**: DRG 权重值不可为负数
- 来源: `DrgDefinition.weight` 校验
- 影响: 负权重视为数据异常

**BR-D03**: 未入组的病例使用默认费用结算规则
- 来源: DRG 降级策略
- 影响: 未匹配到 DRG 组走常规结算流程

---

## 合理用药审核规则

**BR-DR01**: 处方审核时必须校验药品是否在目录内
- 来源: `DrugCatalog` 目录表关联
- 影响: 目录外药品标记为自费提示

**BR-DR02**: 检测到配伍禁忌时必须返回 WARN 或 BLOCK 级别结果
- 来源: `DrugInteraction` 交互表 + Drools 规则
- 影响: 配伍禁忌严重度决定结果级别

**BR-DR03**: 特殊人群用药必须触发额外规则校验
- 来源: 患者基本信息（年龄/性别/妊娠状态）→ 特殊人群规则组
- 影响: 孕妇/儿童用药限制更严格

---

## 质控检测规则

**BR-Q01**: 抗菌药物使用率超过阈值时触发质控预警
- 来源: `QualityDefinition` 质控指标定义
- 影响: 超标科室纳入质控考核

**BR-Q02**: 质控检测周期支持按日/周/月统计
- 来源: `QualityCheckService` 统计逻辑
- 影响: 不同粒度适用于不同质控场景

---

## Skill/Agent 规则

**BR-A01**: Skill 必须定义输入参数 schema 和输出结果 schema
- 来源: `SkillDefinition.model` 字段约束
- 影响: 无 schema 的 Skill 无法被 Agent 调度

**BR-A02**: Agent 的决策链路必须可追溯（日志记录每步输入输出）
- 来源: 可审计性要求 (rules/database.md skill_execution_log)
- 影响: 所有 Agent 执行必须有完整链路日志

**BR-A03**: Skill 超时时间默认 30s，可配置但不得超过 300s
- 来源: 性能保护机制
- 影响: 超时 Skill 自动终止并记录错误

**BR-A04**: Agent 循环调用深度限制 ≤ 5 层，防止无限递归
- 来源: 安全防护 (rules/security.md)
- 影响: 超过深度限制直接中断并返回错误

---

## 配置中心规则

**BR-C01**: Nacos/Apollo 配置变更必须经过灰度验证后再全量发布
- 来源: 生产稳定性要求
- 影响: 直接全量发布需要管理员权限

**BR-C02**: 规则热更新时必须保留上一版本快照（回滚用）
- 来源: `rule_snapshot` 表 (rules/database.md)
- 影响: 支持一键回滚到历史版本

**BR-C03**: 敏感配置（数据库密码/API Key）禁止明文存储
- 来源: 加密存储要求 (rules/security.md)
- 影响: 使用加密算法存储，运行时解密

---

## 缓存规则

**BR-H01**: Caffeine 缓存规则数据 TTL 默认 5 分钟
- 来源: `CacheConfig.ruleCacheTTL`
- 影响: 规则变更后最多 5 分钟生效（或手动刷新）

**BR-H02**: 公式编译结果缓存 key = formula_text 的 hash
- 来源: `AviatorExpressionCache`
- 影响: 相同表达式文本复用编译后的 Expression 对象

**BR-H03**: 租户间缓存隔离，禁止跨租户读取缓存
- 来源: 多租户安全要求
- 影响: 缓存 key 必须包含 tenant_id

---

## 安全规则

**BR-S01**: Aviator 表达式必须经过白名单校验，禁止调用危险函数
- 来源: `AviatorSecurityInterceptor` (rules/security.md)
- 影响: 禁止调用 System.exit()/Runtime.exec() 等

**BR-S02**: DRL 规则禁止包含 Java 代码块（不能使用 eval/mvel）
- 来源: Drools 安全最佳实践
- 影响: 只允许纯 rule 语法，复杂逻辑走 Service 层

**BR-S03**: 敏感接口（规则发布/结算执行/配置变更）必须限流
- 来源: rate limiting 要求 (rules/security.md)
- 影响: 同一 IP/用户单位时间内请求次数受限

**BR-S04**: 规则/公式导入必须校验格式和内容（防恶意规则注入）
- 来源: 规则安全要求 (rules/security.md)
- 影响: 仅允许白名单操作和函数

---

## 网关安全规则

**BR-G01**: 所有 API 请求必须经过 JWT 认证
- 来源: `JwtAuthenticationFilter`
- 影响: 无 Token 或 Token 过期直接拒绝

**BR-G02**: 网关层提取租户 ID 并注入请求上下文
- 来源: `TenantContextFilter`
- 影响: 所有下游服务自动获取租户信息

---

最后更新: 2026-05-10 | 共 40+ 条业务规则
