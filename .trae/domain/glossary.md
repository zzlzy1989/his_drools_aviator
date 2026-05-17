# 术语表 - HIS 动态规则中台

> 最稳定的知识层，跨模块共享。按三层分类：技术术语 / 平台领域术语 / 业务领域术语

---

## Layer 1: 技术术语（通用）

### Java / Spring
| 术语 | 英文 | 说明 |
|------|------|------|
| Java 17 | — | 开发语言，LTS 版本 |
| Spring Boot | — | 应用框架，3.x 版本 |
| Spring Cloud Alibaba | — | 微服务全家桶（Nacos/Sentinel/Gateway） |
| Spring IoC | Inversion of Control | 控制反转，依赖注入 |
| AOP | Aspect-Oriented Programming | 面向切面编程（日志/事务/缓存） |
| Bean | — | Spring 管理的组件实例 |
| @Service | — | 服务层注解 |
| @Component | — | 通用组件注解 |
| @ConfigurationProperties | — | 配置属性绑定 |
| @RefreshScope | — | Nacos/Apollo 配置动态刷新 |
| @RequiredArgsConstructor | — | Lombok 生成全参构造注解 |
| @Slf4j | — | Lombok 生成日志注解 |

### Drools 规则引擎
| 术语 | 英文 | 说明 |
|------|------|------|
| Drools | — | JBoss 规则引擎（PHREAK 算法） |
| DRL | Drools Rule Language | Drools 规则文件格式 |
| KIE | Knowledge Is Everything | Drools 知识工程基础 |
| KieBase | — | 知识库，包含所有规则定义 |
| KieSession | — | 规则会话，规则执行上下文 |
| StatelessKieSession | — | 无状态会话（线程安全，推荐） |
| Fact | — | 传入规则引擎的事实对象（Java POJO） |
| Rule | — | 单条规则（when-then 结构） |
| Agenda | — | 规则激活队列（按 salience 排序） |
| salience | — | 规则优先级（数值越大越先执行） |
| activation-group | — | 激活组（同组只执行第一条命中的规则） |
| ruleflow-group | — | 规则流程组 |
| update() | — | 修改 Fact 后重新匹配规则 |
| insert/retract | — | 插入/撤回 Fact 对象 |
| global | — | 全局变量（在 KieSession 中设置） |
| Rule Unit | — | Drools 8 新特性，规则单元化组织 |
| Pattern Matcher | — | 模式匹配器（PHREAK/RETEOO） |
| LHS/RHS | Left/Right Hand Side | 规则的 when 条件部分 / then 动作部分 |

### Aviator 表达式引擎
| 术语 | 英文 | 说明 |
|------|------|------|
| Aviator | — | 高性能 Java 表达式求值引擎 |
| Expression | — | 编译后的表达式对象（可缓存复用） |
| AviatorEvaluator | — | Aviator 核心入口类 |
| compile() | — | 编译表达式字符串为 Expression |
| execute() | — | 执行表达式（传入环境变量 Map） |
| Options | — | Aviator 全局配置选项 |
| BigDecimal mode | — | BigDecimal 运算模式（避免浮点精度问题） |
| 自定义函数 | Custom Function | 通过 `AviatorEvaluator.addFunction()` 注册 |
| SandboxAviatorEvaluator | — | Aviator 沙箱模式（白名单函数限制） |

### 缓存与配置中心
| 术语 | 英文 | 说明 |
|------|------|------|
| Caffeine | — | 高性能本地缓存库 |
| Cache | — | Caffeine 缓存实例 |
| invalidate() | — | 使缓存条目失效 |
| Nacos | — | 阿里巴巴配置中心 + 服务发现（v3.x） |
| Apollo | — | 携程开源配置中心 |
| ConfigChangeListener | — | 配置变更监听器 |
| namespace | — | 命名空间（隔离不同环境/租户） |
| group | — | 配置分组 |
| Data ID | — | 配置数据标识 |
| Long Polling | — | Nacos 长轮询配置变更机制 |

### 架构模式
| 术语 | 英文 | 说明 |
|------|------|------|
| Skill | — | 能力单元，实现 ISkill 接口的业务组件 |
| Agent | — | 智能 Agent（Skill 的具体实现） |
| SkillGateway | — | 技能调度网关（事件路由 + Pipeline 执行） |
| SkillContext | — | 统一执行上下文（泛型 T 承载业务数据） |
| SkillResult | — | 技能执行结果（PASS/WARN/BLOCK + message） |
| ResultLevel | — | 干预级别枚举: PASS(放行) / WARN(警告) / BLOCK(阻断) |
| Pipeline | — | 按优先级顺序穿透执行的 Skill 链 |
| EventType | — | 事件类型（如 EVENT_DRUG_PRESCRIBE, EVENT_FEE_SETTLE） |
| Hot Loading | — | 热加载（运行时动态加载/卸载 Skill） |
| RaaS | Rules as a Service | 规则即服务商业模式 |
| SPI | Service Provider Interface | Java 服务发现与加载机制 |
| SCA | Spring Cloud Alibaba | 微服务生态全家桶 |

### 网关与安全
| 术语 | 英文 | 说明 |
|------|------|------|
| Gateway | — | Spring Cloud Gateway API 网关 |
| JWT | JSON Web Token | 用户身份认证令牌 |
| Sentinel | — | 流量控制与熔断降级组件 |
| CORS | Cross-Origin Resource Sharing | 跨域资源共享 |
| Rate Limiting | — | 接口限流 |
| RBAC | Role-Based Access Control | 基于角色的访问控制 |

---

## Layer 2: 平台领域术语（HIS 规则引擎特有概念）

### 核心实体
| 术语 | 英文 | 说明 |
|------|------|------|
| Fact 对象 | Fact Object | 传入 Drools 的业务数据载体（如 SettlementFact/CostFact） |
| 规则组 | Rule Group | 按业务分类的规则集合（如 REIMBURSEMENT, DRUG_CHECK） |
| 公式 | Formula | Aviator 可执行的表达式字符串（如报销计算公式） |
| 公式实体 | FormulaEntity | 数据库中的公式记录（ID/文本/分类/版本） |
| 公式参数 | FormulaParam | 公式的输入参数定义（名称/类型/是否必填） |
| 规则版本 | RuleVersion | 规则变更历史记录（支持回滚） |
| 事件类型 | EventType | HIS 系统抛出的标准化事件常量 |
| 租户 | Tenant | 医院/院区隔离单位（多租户规则集） |

### 规则编排与流程
| 术语 | 英文 | 说明 |
|------|------|------|
| RuleFlow | — | 规则可视化编排流程，定义节点流转和条件分支 |
| Flow Node | — | 流程节点，包含规则节点/公式节点/决策节点/网关节点 |
| Flow Edge | — | 流程边，定义节点间的流转条件 |
| Flow Version | — | 流程版本号，支持多版本并存 |
| 流程引擎 | FlowEngine | 后端解析 RuleFlow 并驱动执行的核心服务 |
| CreateFlowDTO | — | 创建流程的请求体（含节点和边定义） |

### 结算相关
| 术语 | 英文 | 说明 |
|------|------|------|
| 结算事实 | SettlementFact | 医保结算的核心 Fact 对象 |
| 起付线 | Deductible | 医保报销起付门槛金额 |
| 报销比例 | Ratio | 医保报销百分比（职工/居民/救助等不同） |
| 总费用 | TotalFee | 患者本次医疗总费用 |
| 最终报销额 | FinalAmount | 经规则计算后的最终报销金额 |
| 患者类型 | PatientType | 职工/居民/低保/其他 |
| 结算记录 | SettlementRecord | 一次结算操作的完整记录 |
| SkillPipelineExecutor | — | 结算管道执行器，按序调度各 Skill |

### Fact 类型
| 术语 | 英文 | 说明 |
|------|------|------|
| SettlementFact | — | 结算事实对象（费用/患者类型/起付线/比例） |
| CostFact | — | 费用事实对象（总费用/药品费用/检查费用） |
| PrescriptionFact | — | 处方事实对象（处方明细/诊断/患者信息） |
| DrgFact | — | DRG 分组事实对象（诊断/手术/住院信息） |
| InfectionFact | — | 院感事实对象（感染指标/抗菌药物使用情况） |
| QualityFact | — | 质控事实对象（质控指标/统计周期） |

### DRG 相关
| 术语 | 英文 | 说明 |
|------|------|------|
| DRG 分组 | DRG Grouping | 按诊断+手术将病例分入 DRG 组 |
| 权重 | Weight | DRG 组相对权重值，反映资源消耗 |
| 基础费率 | Base Rate | 每个权重点对应金额 |
| 入组 | Grouping | 病例匹配进入某个 DRG 组的过程 |
| MDC | Major Diagnostic Category | 主要诊断大类 |
| ADRG | Adjacent DRG | 相邻 DRG 组 |
| DrgDefinition | — | DRG 定义实体（组编码/权重/入组条件） |
| DrgGroupService | — | DRG 分组计算服务 |

### 合理用药相关
| 术语 | 英文 | 说明 |
|------|------|------|
| 处方事实 | PrescriptionFact | 合理用药审核的 Fact 对象 |
| 处方事件 | EVENT_DRUG_PRESCRIBE | 开处方时触发的事件 |
| 配伍禁忌 | Drug Incompatibility | 药物间的不良相互作用 |
| 极量检查 | Dosage Limit Check | 用药剂量是否超过安全上限 |
| 适应症匹配 | Indication Matching | 药物是否适用于当前诊断 |
| 特殊人群 | Special Population | 孕妇/儿童/老年人/肝肾功能不全者 |
| DrugCatalog | — | 药品目录实体 |
| DrugInteraction | — | 药物相互作用实体 |
| DrugCheckService | — | 用药审核服务 |

### 质控相关
| 术语 | 英文 | 说明 |
|------|------|------|
| 质控 | Quality Control | 医疗质量控制指标检测 |
| 质控规则 | Quality Rule | 质控指标的检测规则 |
| 抗菌药物使用率 | Antimicrobial Usage Rate | 门诊/住院抗菌药物使用占比 |
| 处方合格率 | Prescription Qualification Rate | 合格处方数/总处方数 |
| QualityDefinition | — | 质控定义实体（指标编码/阈值/检测规则） |
| QualityCheckService | — | 质控检测服务 |

---

## Layer 3: 业务领域术语（HIS 医疗行业通用）

### 医保结算
| 术语 | 英文 | 说明 |
|------|------|------|
| 医保 | Medical Insurance | 基本医疗保险（城镇职工/城乡居民） |
| 职工医保 | Employee Insurance | 城镇职工基本医疗保险 |
| 居民医保 | Resident Insurance | 城乡居民基本医疗保险 |
| 医疗救助 | Medical Aid | 对困难群众的医疗费用补助 |
| DRG | Diagnosis Related Groups | 按疾病诊断相关分组付费 |
| DIP | Big Diagnosis Intervention Packet | 按病种分值付费 |
| 起付线 | Deductible Line | 医保报销的起付标准 |
| 封顶线 | Cap | 医保年度最高支付限额 |
| 共付 | Co-payment | 个人自付部分 |
| 自费 | Self-pay | 医保不报销的费用 |
| 目录内 | In-Catalog | 医保药品/诊疗项目目录范围内 |
| 目录外 | Out-of-Catalog | 医保目录范围外，需自费 |
| 甲类/乙类 | Class A/B | 甲类全额纳入报销，乙类按比例报销 |

### 合理用药
| 术语 | 英文 | 说明 |
|------|------|------|
| 合理用药 | Rational Drug Use | 安全、有效、经济、适当地使用药物 |
| 处方审核 | Prescription Review | 对医生处方的合理性进行审核 |
| 抗菌药物 | Antimicrobial | 抗生素类药物（分级管理） |
| 精麻药品 | Controlled Substances | 精神药品和麻醉药品（特殊管理） |
| 基药目录 | Essential Drug List | 国家基本药物目录 |
| 集采 | Centralized Procurement | 带量集中采购药品 |
| 药品说明书 | Drug Package Insert | 药品批准证明文件中的说明 |

### 医院信息管理
| 术语 | 英文 | 说明 |
|------|------|------|
| HIS | Hospital Information System | 医院信息系统 |
| EMR | Electronic Medical Record | 电子病历系统 |
| LIS | Laboratory Information System | 检验信息系统 |
| PACS | Picture Archiving System | 影像归档与通信系统 |
| 院感 | Hospital Infection | 医院感染防控 |
| 质控 | Quality Control | 医疗质量控制 |
| CDR | Clinical Data Repository | 临床数据中心 |

---

## 缩写速查表

| 缩写 | 全称 | 上下文 |
|------|------|--------|
| HIS | Hospital Information System | 医院信息系统 |
| DRL | Drools Rule Language | Drools 规则语言 |
| API | Application Programming Interface | 接口 |
| CRUD | Create/Read/Update/Delete | 增删改查 |
| RaaS | Rules as a Service | 规则即服务 |
| DRG | Diagnosis Related Groups | 按疾病诊断分组 |
| DIP | Big Diagnosis Intervention Packet | 按病种分值付费 |
| EMR | Electronic Medical Record | 电子病历 |
| MQ | Message Queue | 消息队列 |
| DTO | Data Transfer Object | 数据传输对象 |
| VO | View Object | 视图对象（返回给前端） |
| POJO | Plain Old Java Object | 普通 Java 对象 |
| SLA | Service Level Agreement | 服务等级协议 |
| JWT | JSON Web Token | 认证令牌 |
| CORS | Cross-Origin Resource Sharing | 跨域资源共享 |
| RBAC | Role-Based Access Control | 基于角色的访问控制 |
| SCA | Spring Cloud Alibaba | 微服务全家桶 |
| SPI | Service Provider Interface | 服务发现接口 |
| LHS | Left Hand Side | Drools 规则的条件部分（when） |
| RHS | Right Hand Side | Drools 规则的动作部分（then） |

---

最后更新: 2026-05-10 | v1.1 (HIS Drools+Aviator 专用, 120+ 条术语)
