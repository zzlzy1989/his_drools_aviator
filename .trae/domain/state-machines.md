# 状态机 - HIS 动态规则中台

> 核心实体的状态流转定义，使用 Mermaid 图表示

---

## SM-01: 规则定义状态机

```mermaid
stateDiagram-v2
    [*] --> Draft: 创建规则
    
    Draft --> Validated: 语法校验通过
    Draft --> Draft: 编辑草稿
    Draft --> [*]: 删除草稿
    
    Validated --> Active: 发布生效
    Validated --> Draft: 校验失败/修改
    
    Active --> Inactive: 停用
    Active --> Active: 编辑(新版本)
    Active --> Archived: 归档
    
    Inactive --> Active: 重新启用
    Inactive --> Archived: 归档
    
    Archived --> [*]: 物理删除(仅Admin)
    
    note right of Draft
        草稿状态: 仅创建者可见
        可自由编辑和删除
        未通过语法校验
    end note
    
    note right of Validated
        已校验: DRL语法正确
        引用的函数/对象存在
        等待发布审批
    end note
    
    note right of Active
        生效状态: 可被结算引擎加载
        支持热更新（无需重启）
        编辑会创建新版本
    end note
    
    note right of Inactive
        停用状态: 不参与规则匹配
        保留数据和配置
        可随时重新启用
    end note
```

**状态枚举**: `RuleDefinition.Status` = `draft` | `validated` | `active` | `inactive` | `archived`

**关键约束**:
- Draft → Validated 需要 DRL 语法检查 + 依赖校验
- Validated → Active 需要审批流程（如项目开启了规则审核）
- Active → 编辑会创建新版本，旧版本自动归档
- Active 规则可通过 Nacos/Apollo 热更新

---

## SM-02: 公式定义状态机

```mermaid
stateDiagram-v2
    [*] --> Draft: 创建公式
    
    Draft --> SyntaxChecked: Aviator语法校验通过
    Draft --> Draft: 编辑
    
    SyntaxChecked --> ParamValidated: 参数校验通过
    SyntaxChecked --> Draft: 语法错误
    
    ParamValidated --> Active: 发布生效
    ParamValidated --> Draft: 参数不匹配
    
    Active --> Inactive: 停用
    Active --> Active: 新版本发布
    Active --> Archived: 归档
    
    Inactive --> Active: 重新启用
    Inactive --> Archived: 归档
    
    Archived --> [*]: 物理删除
    
    note right of Draft
        草稿: 编写Aviator表达式
        定义输入/输出参数
    end note
    
    note right of SyntaxChecked
        语法已检: Aviator.compile()成功
        函数引用合法
    end note
    
    note right of ParamValidated
        参数已检: 变量类型匹配
        必填参数完整
    end note
    
    note right of Active
        生效: 可被结算公式调用
        编译结果缓存
        版本唯一性保证
    end note
```

**状态字段**: `Formula.Status` = `draft` | `syntax_checked` | `param_validated` | `active` | `inactive` | `archived`

**关键约束**:
- Draft → SyntaxChecked 通过 AviatorEvaluator.compile() 验证
- SyntaxChecked → ParamValidated 校验所有引用变量是否在 param_defs 中注册
- Active → 新版本发布时，旧版本自动设为 inactive（同一 key 只有一个 active）

---

## SM-03: 结算记录状态机

```mermaid
stateDiagram-v2
    [*] --> Pending: 发起结算请求
    
    Pending --> Matching: 开始规则匹配
    Pending --> Cancelled: 取消结算
    
    Matching --> Calculating: 规则匹配完成
    Matching --> Failed: 规则匹配无结果/异常
    Matching --> Cancelled: 取消
    
    Calculating --> Reviewing: 公式计算完成
    Calculating --> Failed: 计算异常(除零/溢出)
    
    Reviewing --> Completed: 审核通过
    Reviewing --> Returned: 审核退回(需修正)
    Reviewing --> Cancelled: 取消
    
    Returned --> Matching: 重新匹配
    Returned --> Calculating: 仅重算(规则不变)
    
    Completed --> [*]: 结束
    Failed --> [*]: 结束(可重试)
    Cancelled --> [*]: 结束
    
    note right of Pending
        待处理: 接收结算请求
        加载患者/费用信息
        选择结算规则集
    end note
    
    note right of Matching
        规则匹配: Drools引擎执行
        输出适用的规则列表
        确定报销比例/起付线等
    end note
    
    note right of Calculating
        公式计算: Aviator引擎执行
        逐步计算各项金额
        BigDecimal精度保证
    end note
    
    note right of Reviewing
        结果审核: 人工复核关键金额
        异常值预警标记
        审核意见记录
    end note
```

**状态字段**: `SettlementRecord.status` = `pending` | `matching` | `calculating` | `reviewing` | `completed` | `returned` | `failed` | `cancelled`

**关键约束**:
- Pending → Matching 时锁定就诊信息（防止并发修改）
- Matching → Calculating 要求至少匹配到一条有效规则
- Calculating → Reviewing 时进行合理性校验（金额范围/比例检查）
- Reviewing → Completed 需要审核人确认签名
- Failed 状态支持重试（有限次，默认 3 次）

---

## SM-04: Skill 执行状态机

```mermaid
stateDiagram-v2
    [*] --> Queued: Agent调度Skill
    
    Queued --> Running: 开始执行
    Queued --> Rejected: 资源不足/限流拒绝
    Queued --> Cancelled: 取消执行
    
    Running --> Success: 执行完成
    Running --> Failed: 执行异常
    Running --> Timeout: 超时终止
    Running --> Running: 重试(瞬态错误)
    
    Success --> [*]: 返回结果
    Failed --> [*]: 返回错误
    Timeout --> [*]: 返回超时错误
    Rejected --> [*]: 返回拒绝原因
    Cancelled --> [*]: 终结
    
    note right of Queued
        排队中: 等待执行资源
        按优先级排序
        支持插队(高优先级)
    end note
    
    note right of Running
        执行中: 加载Skill定义
        参数绑定与校验
        执行业务逻辑
        记录执行日志
    end note
```

**状态字段**: `SkillExecution.status` = `queued` | `running` | `success` | `failed` | `timeout` | `rejected` | `cancelled`

**关键约束**:
- Queued → Running 受限于并发数限制（默认 10）
- Running → Timeout 默认 30s，可在 skill 定义中自定义
- Failed 支持自动重试（可配置重试策略：固定间隔/指数退避）
- 所有终态都会写入 skill_execution_log 表

---

## SM-05: 规则热更新状态机

```mermaid
stateDiagram-v2
    [*] --> Detecting: 监听配置变更
    
    Detecting --> Downloading: 检测到规则版本变化
    Detecting --> Detecting: 无变化(心跳)
    
    Downloading --> Validating: 下载规则包完成
    Downloading --> Failed: 下载失败
    
    Validating --> Compiling: DRL语法+依赖校验通过
    Validating --> Rollback: 校验失败(回滚到上一版)
    
    Compiling --> Switching: KIE Base编译完成
    Compiling --> Failed: 编译异常
    
    Switching --> Applied: 切换完成(新规则生效)
    Switching --> Rollback: 切换失败
    
    Applied --> Detecting: 继续监听
    Rollback --> Detecting: 回滚完成,继续监听
    Failed --> Detecting: 错误告警,保持当前版本
    
    note right of Detecting
        监听中: Nacos长轮询/Apollo通知
        版本号比对
        变更事件触发
    end note
    
    note right of Validating
        校验中: DRL语法检查
        引用函数存在性
        白名单安全扫描
    end note
    
    note right of Applied
        已生效: 新规则在线
        旧Session优雅关闭
        快照已保存(用于回滚)
    end note
```

**状态字段**: `RuleHotUpdate.status` = `detecting` | `downloading` | `validating` | `compiling` | `switching` | `applied` | `rollback` | `failed`

**关键约束**:
- Validating 失败自动触发 Rollback，不会导致服务不可用
- Switching 采用双缓冲策略：新 Session 就绪后才切换，实现零停机
- Rollback 时从 rule_snapshot 表恢复上一版本的 DRL 文本
- Applied 后发送 webhook 通知（可选）给监控系统

---

## SM-06: 租户状态机

```mermaid
stateDiagram-v2
    [*] --> Pending: 创建租户申请
    
    Pending --> Active: 审批通过
    Pending --> Rejected: 审批拒绝
    Pending --> Pending: 补充资料
    
    Active --> Suspended: 欠费/违规暂停
    Active --> Expired: 到期失效
    Active --> Active: 续费/升级
    
    Suspended --> Active: 恢复(补缴/整改完成)
    Suspended --> Expired: 超期未恢复
    
    Expired --> Active: 重新开通(数据保留期限內)
    Expired --> [*]: 数据清理(超过保留期)
    
    Rejected --> Pending: 重新申请
    
    note right of Pending
        待审核: 提交租户资料
        管理员审核
        初始化资源
    end note
    
    note right of Active
        正常: 完整功能可用
        规则/公式独立隔离
        资源配额管控
    end note
    
    note right of Suspended
        已暂停: 只读访问
        规则停止执行
        数据保留
    end note
```

**状态字段**: `Tenant.Status` = `pending` | `active` | `suspended` | `expired` | `rejected`

**关键约束**:
- Pending → Active 时自动初始化租户资源（数据库 schema / 缓存命名空间）
- Suspended 状态下已有结算任务执行完毕后停止接受新任务
- Expired 超过 90 天（可配置）后触发数据清理

---

## SM-07: 规则编排流程（RuleFlow）状态机

```mermaid
stateDiagram-v2
    [*] --> Draft: 创建流程
    
    Draft --> Editing: 开始编辑
    Draft --> Draft: 保存草稿
    
    Editing --> Draft: 保存草稿
    Editing --> Validating: 提交校验
    
    Validating --> Validated: 拓扑/条件校验通过
    Validating --> Editing: 校验失败（需修复）
    
    Validated --> Active: 发布生效
    Validated --> Editing: 修改重校
    
    Active --> Inactive: 停用
    Active --> Active: 发布新版本
    Active --> Archived: 归档
    
    Inactive --> Active: 重新启用
    Inactive --> Archived: 归档
    
    Archived --> [*]: 物理删除
    
    note right of Draft
        草稿: 流程基本信息
        节点和边定义中
    end note
    
    note right of Validating
        校验中: start/end节点存在
        无孤立节点/环
        条件表达式合法
    end note
    
    note right of Active
        生效: 可被结算调用
        旧版本自动停用
        历史记录已保存
    end note
```

**状态字段**: `RuleFlow.status` = `draft` | `editing` | `validating` | `validated` | `active` | `inactive` | `archived`

**关键约束**:
- Draft → Validating 必须包含至少 1 个 start 节点和 1 个 end 节点
- Validating → Validated 校验拓扑完整性（无孤岛/无死循环）+ Aviator 条件表达式编译成功
- Active → 新版本发布时，旧版本自动 inactive（同一流程同租户只一个 active）
- Archived 流程不可恢复，只能作为新流程模板复制

---

最后更新: 2026-05-10 | 共 7 个核心状态机
