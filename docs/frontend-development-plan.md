# HIS 动态规则中台 — 前端开发计划（V1.0 + V2.0 全量）

> 文档版本: v2.0
> 创建日期: 2026-05-01
> 更新日期: 2026-05-01
> 适用项目: his_drools_aviator
> 前端仓库: his-rule-engine-web
> 覆盖范围: V1.0 基础功能 + V2.0 增强功能

---

## 一、技术栈选型

### 1.1 主技术栈

| 分类 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **框架** | Vue 3 | 3.4+ | Composition API + `<script setup>` |
| **构建工具** | Vite | 5.x | 极速开发体验 |
| **UI 框架** | Element Plus | 2.7+ | 企业级组件库（管理后台） |
| **状态管理** | Pinia | 2.x | Vue3 官方推荐 |
| **路由** | Vue Router | 4.x | 路由守卫 + 动态路由 |
| **HTTP 客户端** | Axios | 1.x | 请求/响应拦截、统一错误处理 |
| **代码编辑器** | Monaco Editor | 0.45+ | DRL/Aviator 代码编辑（VSCode 同款引擎） |
| **图表** | ECharts | 5.x | 结算统计、监控大屏 |
| **CSS 预处理** | SCSS | - | 主题变量、布局样式 |
| **代码规范** | ESLint + Prettier | - | 统一代码风格 |
| **类型支持** | TypeScript | 5.x | 类型安全 |
| **权限** | 自定义指令 + 路由守卫 | - | 按钮级权限控制 |
| **国际化** | vue-i18n | 9.x | 预留多语言支持 |

### 1.2 V2.0 新增技术栈

| 分类 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **流程图引擎** | AntV X6 | 2.x | 规则可视化编排（Vue 封装） |
| **实时通信** | WebSocket | - | 监控大屏数据实时推送 |
| **JSON Diff** | deep-diff | - | 测试沙箱结果对比 |
| **PDF 生成** | html2canvas + jsPDF | - | 测试报告导出 |
| **拖拽排序** | vuedraggable | 4.x | 测试用例排序、参数排序 |
| **代码差异** | Monaco Diff Editor | - | 规则/公式版本对比 |
| **热力图** | ECharts Heatmap | - | 规则触发热力图 |

---

## 二、项目目录结构

```
his-rule-engine-web/
├── public/
│   └── favicon.ico
├── src/
│   ├── api/                              # API 请求层
│   │   ├── request.ts                    # Axios 实例 + 拦截器
│   │   ├── rule.ts                       # 规则管理 API (V1)
│   │   ├── rule-group.ts                 # 规则分组 API (V1)
│   │   ├── formula.ts                    # 公式管理 API (V1)
│   │   ├── settlement.ts                 # 结算管理 API (V1)
│   │   ├── drug.ts                       # 用药审核 API (V1)
│   │   ├── quality.ts                    # 质控管理 API (V1)
│   │   ├── drg.ts                        # DRG 分组 API (V1)
│   │   ├── audit.ts                      # 审计日志 API (V1)
│   │   ├── tenant.ts                     # 租户管理 API (V1)
│   │   ├── rule-flow.ts                  # 规则流 API (V2)
│   │   ├── sandbox.ts                    # 测试沙箱 API (V2)
│   │   ├── monitor.ts                    # 监控 API (V2)
│   │   └── market.ts                     # 规则市场 API (V2)
│   │
│   ├── assets/                           # 静态资源
│   │   ├── styles/
│   │   │   ├── variables.scss            # 全局 SCSS 变量
│   │   │   ├── mixins.scss               # SCSS 混入
│   │   │   ├── reset.scss                # 样式重置
│   │   │   ├── index.scss                # 入口样式
│   │   │   └── flow-editor.scss          # 规则流编辑器专用样式
│   │   └── images/
│   │
│   ├── components/                       # 全局公共组件
│   │   ├── Layout/                       # 布局组件
│   │   │   ├── AppLayout.vue             # 主布局
│   │   │   ├── Sidebar.vue               # 侧边栏
│   │   │   ├── Navbar.vue                # 顶部导航
│   │   │   └── TagsView.vue              # 标签页导航
│   │   ├── CodeEditor/                   # 代码编辑器封装
│   │   │   ├── MonacoEditor.vue          # Monaco Editor 组件
│   │   │   └── MonacoDiffEditor.vue      # Monaco Diff 对比组件 (V2)
│   │   ├── StatusTag/                    # 状态标签
│   │   │   └── StatusTag.vue             # 统一状态展示
│   │   ├── SearchForm/                   # 搜索表单
│   │   │   └── SearchForm.vue            # 通用搜索表单
│   │   ├── ResultLevel/                  # 结果级别
│   │   │   └── ResultLevel.vue           # PASS/WARN/BLOCK 展示
│   │   ├── ConfirmDialog/                # 确认弹窗
│   │   │   └── ConfirmDialog.vue
│   │   ├── JsonDiffView/                 # JSON Diff 视图 (V2)
│   │   │   └── JsonDiffView.vue          # 测试结果 Diff 对比
│   │   └── VersionTimeline/              # 版本时间线 (V2)
│   │       └── VersionTimeline.vue       # 版本历史时间线
│   │
│   ├── composables/                      # 组合式函数
│   │   ├── useTable.ts                   # 表格分页逻辑
│   │   ├── usePermission.ts              # 权限判断
│   │   ├── useLoading.ts                 # 加载状态
│   │   ├── useDict.ts                    # 字典数据
│   │   ├── useWebSocket.ts               # WebSocket 连接 (V2)
│   │   └── useFlowEditor.ts              # 规则流编辑器逻辑 (V2)
│   │
│   ├── directives/                       # 自定义指令
│   │   └── permission.ts                 # v-permission 权限指令
│   │
│   ├── router/                           # 路由
│   │   ├── index.ts                      # 路由实例
│   │   ├── guards.ts                     # 路由守卫
│   │   └── modules/                      # 路由模块
│   │       ├── rule.ts
│   │       ├── formula.ts
│   │       ├── settlement.ts
│   │       ├── drug.ts
│   │       ├── quality.ts
│   │       ├── drg.ts
│   │       ├── flow.ts                   # V2 规则流
│   │       ├── sandbox.ts                # V2 测试沙箱
│   │       ├── monitor.ts                # V2 监控
│   │       ├── market.ts                 # V2 规则市场
│   │       └── system.ts
│   │
│   ├── stores/                           # Pinia 状态
│   │   ├── user.ts                       # 用户信息
│   │   ├── permission.ts                 # 权限/菜单
│   │   ├── app.ts                        # 应用全局状态
│   │   ├── dict.ts                       # 字典缓存
│   │   ├── monitor.ts                    # 监控实时数据 (V2)
│   │   └── flow-editor.ts               # 规则流编辑器状态 (V2)
│   │
│   ├── types/                            # TypeScript 类型
│   │   ├── api.ts                        # 通用 API 类型
│   │   ├── rule.ts                       # 规则相关类型
│   │   ├── formula.ts                    # 公式相关类型
│   │   ├── settlement.ts                 # 结算相关类型
│   │   ├── drug.ts                       # 用药相关类型
│   │   ├── quality.ts                    # 质控相关类型
│   │   ├── drg.ts                        # DRG 相关类型
│   │   ├── flow.ts                       # 规则流类型 (V2)
│   │   ├── sandbox.ts                    # 测试沙箱类型 (V2)
│   │   ├── monitor.ts                    # 监控类型 (V2)
│   │   ├── market.ts                     # 规则市场类型 (V2)
│   │   └── system.ts                     # 系统相关类型
│   │
│   ├── utils/                            # 工具函数
│   │   ├── auth.ts                       # Token 管理
│   │   ├── validate.ts                   # 表单校验规则
│   │   ├── format.ts                     # 格式化工具
│   │   ├── constants.ts                  # 常量定义
│   │   ├── websocket.ts                  # WebSocket 封装 (V2)
│   │   └── diff.ts                       # JSON Diff 工具 (V2)
│   │
│   ├── views/                            # 页面视图
│   │   ├── login/                        # 登录
│   │   │   └── LoginView.vue
│   │   │
│   │   ├── dashboard/                    # 仪表盘
│   │   │   └── DashboardView.vue
│   │   │
│   │   ├── rule/                         # 规则管理 (V1)
│   │   │   ├── RuleList.vue              # 规则列表
│   │   │   ├── RuleDetail.vue            # 规则详情
│   │   │   ├── RuleForm.vue              # 规则创建/编辑
│   │   │   └── RuleHistory.vue           # 版本历史
│   │   │
│   │   ├── rule-group/                   # 规则分组 (V1)
│   │   │   ├── RuleGroupList.vue
│   │   │   └── RuleGroupForm.vue
│   │   │
│   │   ├── formula/                      # 公式管理 (V1)
│   │   │   ├── FormulaList.vue           # 公式列表
│   │   │   ├── FormulaDetail.vue         # 公式详情
│   │   │   ├── FormulaForm.vue           # 公式创建/编辑
│   │   │   └── FormulaHistory.vue        # 版本历史
│   │   │
│   │   ├── settlement/                   # 结算管理 (V1)
│   │   │   ├── SettlementList.vue        # 结算列表
│   │   │   ├── SettlementDetail.vue      # 结算详情
│   │   │   └── SettlementExecute.vue     # 发起结算
│   │   │
│   │   ├── drug/                         # 用药审核 (V1)
│   │   │   ├── PrescriptionReview.vue    # 处方审核
│   │   │   └── DrugInteraction.vue       # 配伍禁忌管理
│   │   │
│   │   ├── quality/                      # 质控管理 (V1)
│   │   │   └── QualityCheck.vue          # 质控检查
│   │   │
│   │   ├── drg/                          # DRG 分组 (V1)
│   │   │   └── DrgGrouping.vue           # DRG 分组
│   │   │
│   │   ├── flow/                         # 规则流编排 (V2)
│   │   │   ├── FlowList.vue              # 规则流列表
│   │   │   ├── FlowEditor.vue            # 规则流可视化编辑器
│   │   │   ├── FlowDetail.vue            # 规则流详情
│   │   │   ├── FlowHistory.vue           # 规则流版本历史
│   │   │   └── FlowCompare.vue           # 版本对比
│   │   │
│   │   ├── sandbox/                      # 测试沙箱 (V2)
│   │   │   ├── SandboxDashboard.vue      # 沙箱首页
│   │   │   ├── DataSetList.vue           # 测试数据集管理
│   │   │   ├── DataSetForm.vue           # 数据集创建/编辑
│   │   │   ├── TestCaseBuilder.vue       # 测试用例构建器
│   │   │   ├── SandboxExecute.vue        # 沙箱执行界面
│   │   │   └── TestReport.vue            # 测试报告
│   │   │
│   │   ├── monitor/                      # 监控大屏 (V2)
│   │   │   ├── MonitorDashboard.vue      # 监控看板（全屏大屏）
│   │   │   ├── MonitorAlert.vue          # 告警规则管理
│   │   │   ├── MonitorHistory.vue        # 历史数据查询
│   │   │   └── MonitorAlertForm.vue      # 告警规则表单
│   │   │
│   │   ├── market/                       # 规则市场 (V2)
│   │   │   ├── MarketList.vue            # 市场模板列表
│   │   │   ├── MarketDetail.vue          # 模板详情
│   │   │   ├── MarketPublish.vue         # 发布模板
│   │   │   ├── MyTemplates.vue           # 我的发布
│   │   │   ├── MySubscriptions.vue       # 已订阅模板
│   │   │   └── TemplateComments.vue      # 评论列表
│   │   │
│   │   ├── audit/                        # 审计日志 (V1)
│   │   │   └── AuditLogList.vue
│   │   │
│   │   └── system/                       # 系统管理 (V1)
│   │       ├── TenantList.vue            # 租户管理
│   │       └── UserManage.vue            # 用户管理
│   │
│   ├── App.vue
│   └── main.ts
│
├── .env.development                      # 开发环境变量
├── .env.production                       # 生产环境变量
├── .eslintrc.cjs
├── .prettierrc
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

---

## 三、页面功能详细设计

### 3.1 登录页 (`/login`)

| 元素 | 说明 |
|------|------|
| 用户名/密码 | 表单校验，支持回车提交 |
| 租户选择 | 下拉选择所属医院/租户 |
| 记住密码 | localStorage 存储 |
| 登录逻辑 | 调用后端认证接口，存储 Token + 用户信息到 Pinia |

### 3.2 仪表盘 (`/dashboard`)

| 区域 | 内容 | 版本 |
|------|------|------|
| 统计卡片 | 规则总数/活跃规则数、公式总数/活跃公式数、今日结算笔数、今日审核拦截数 | V1 |
| 结算趋势图 | ECharts 折线图，近 7/30 天结算金额趋势 | V1 |
| 规则执行统计 | 饼图，各分类规则命中分布 | V1 |
| 最近操作 | 审计日志最近 10 条 | V1 |
| 待办提醒 | 待发布规则、校验失败公式 | V1 |
| 实时监控摘要 | 规则命中率、P99耗时、异常告警数（从监控服务获取） | V2 |
| 规则流状态 | 活跃规则流数量、最近执行状态 | V2 |
| 市场动态 | 新上架模板、热门模板 TOP3 | V2 |

---

### 3.3 规则管理模块 (V1)

#### 规则列表 (`/rule/list`)

| 功能 | 说明 |
|------|------|
| 搜索栏 | 分类（下拉）、状态（下拉）、规则Key（输入）、规则名称（输入）、规则分组（下拉） |
| 表格列 | ID、规则Key、规则名称、分类、分组、版本、状态（StatusTag）、优先级、生效时间、操作 |
| 操作按钮 | 查看详情、编辑（草稿态）、发布、校验、删除 |
| 批量操作 | 批量发布、批量停用 |
| 分页 | Element Plus Pagination |

#### 规则创建/编辑 (`/rule/create`, `/rule/edit/:id`)

| 字段 | 组件 | 校验 |
|------|------|------|
| 规则Key | ElInput | 正则 `rule.{module}.{name}`，创建后不可修改 |
| 规则名称 | ElInput | 必填，≤128字符 |
| DRL内容 | **Monaco Editor** | 必填，DRL 语法高亮 |
| 分类 | ElSelect | 必填，reimbursement/drug/quality/drg |
| 规则分组 | ElSelect | 关联 rule_group 表 |
| 描述 | ElInput textarea | ≤500字符 |
| 优先级(salience) | ElInputNumber | 默认0 |
| 激活组 | ElInput | 可选 |
| 生效时间 | ElDatePicker range | 可选 |

#### 规则详情 (`/rule/detail/:id`)

| 区域 | 说明 |
|------|------|
| 基本信息卡片 | Key、名称、分类、状态、版本、分组、优先级 |
| DRL 代码区 | Monaco Editor 只读模式，带行号 |
| 操作按钮 | 编辑、发布、校验、停用（根据状态动态显示） |
| 版本历史 | 时间线组件，展示历史版本列表 |

#### 版本历史 (`/rule/history/:id`)

| 列 | 说明 |
|------|------|
| 版本号、状态快照、变更人、变更时间、变更原因 | ElTimeline 或 ElTable |
| 版本对比 | 选择两个版本，Monaco Diff Editor 展示差异 (V2) |

### 3.4 规则分组管理 (`/rule-group`) (V1)

| 功能 | 说明 |
|------|------|
| 列表 | 分组编码、名称、描述、优先级、启用状态、操作 |
| 创建 | 分组编码 + 名称 + 描述 |
| 编辑 | 名称、描述、优先级 |
| 启用/禁用 | Switch 切换 |

### 3.5 公式管理模块 (V1)

#### 公式列表 (`/formula/list`)

| 功能 | 说明 |
|------|------|
| 搜索栏 | 分类、状态、公式Key、公式名称 |
| 表格列 | ID、公式Key、公式名称、公式内容(截断)、分类、版本、状态、校验状态、操作 |
| 操作 | 查看、编辑、发布、校验、删除 |

#### 公式创建/编辑 (`/formula/create`, `/formula/edit/:id`)

| 字段 | 组件 | 校验 |
|------|------|------|
| 公式Key | ElInput | 正则 `formula.{category}.{name}` |
| 公式名称 | ElInput | 必填，≤128字符 |
| 公式内容 | **Monaco Editor** | 必填，≤1000字符，Aviator 语法高亮 |
| 分类 | ElSelect | REIMBURSE/DRUG/DRG/GENERAL |
| 描述 | ElInput textarea | ≤500字符 |
| 参数定义 | **动态表单** | 可增删参数行：参数名、类型(BigDecimal/String/Integer/Boolean)、默认值、描述、排序 |

#### 公式详情 (`/formula/detail/:id`)

| 区域 | 说明 |
|------|------|
| 基本信息 | Key、名称、分类、状态、版本 |
| 公式代码 | Monaco Editor 只读 |
| 参数列表 | ElTable 展示参数定义 |
| 校验结果 | 校验状态 + 校验信息 |
| 操作按钮 | 编辑、发布、校验 |

### 3.6 结算管理模块 (V1)

#### 结算列表 (`/settlement/list`)

| 功能 | 说明 |
|------|------|
| 搜索栏 | 患者ID、结算状态（pending/completed/failed） |
| 表格列 | 结算单号、就诊ID、患者ID、患者类型、总费用、报销金额、自付金额、结果级别、状态、时间 |
| 结果级别 | PASS(绿)/WARN(橙)/BLOCK(红) 标签 |

#### 发起结算 (`/settlement/execute`)

| 字段 | 组件 | 校验 |
|------|------|------|
| 就诊ID | ElInput | 必填 |
| 患者ID | ElInput | 必填 |
| 患者类型 | ElSelect (employee/resident/aid) | 必填 |
| 医保类型 | ElSelect | 可选 |
| 医院等级 | ElSelect (1/2/3) | 可选 |
| 总费用 | ElInputNumber | 必填，BigDecimal 精度 |

#### 结算详情 (`/settlement/detail/:settlementNo`)

| 区域 | 说明 |
|------|------|
| 基本信息 | 结算单号、就诊ID、患者信息 |
| 费用明细 | 总费用、起付线、报销比例、报销金额、自付金额 |
| Skill 执行结果 | 各 Skill 执行结果列表（PASS/WARN/BLOCK） |
| 缓存管理 | 刷新/清空公式缓存按钮 |

### 3.7 用药审核模块 (V1)

#### 处方审核 (`/drug/review`)

| 字段 | 组件 | 说明 |
|------|------|------|
| 就诊ID | ElInput | 必填 |
| 患者ID | ElInput | 必填 |
| 患者信息 | ElInput | 可选 |
| 药品列表 | **动态表格** | 可增删行：药品代码、名称、规格、剂量、单位、数量、用法 |
| 诊断编码 | ElSelect multiple | 可选 |
| 医生ID | ElInput | 必填 |

**审核结果展示**

| 区域 | 说明 |
|------|------|
| 审核状态 | PASS/WARN/BLOCK 大标签 |
| 审核项列表 | 每项显示：级别、来源Skill、消息、关联药品 |

#### 配伍禁忌管理 (`/drug/interaction`)

| 功能 | 说明 |
|------|------|
| 列表 | 药品A、药品B、相互作用类型、严重程度、说明、启用状态 |
| CRUD | 新增/编辑/启用/禁用配伍禁忌记录 |

### 3.8 质控管理 (`/quality/check`) (V1)

| 字段 | 组件 | 说明 |
|------|------|------|
| 就诊ID | ElInput | 必填 |
| 患者ID | ElInput | 必填 |
| 诊断编码 | ElSelect multiple | 可选 |
| 手术编码 | ElSelect multiple | 可选 |
| 科室代码 | ElInput | 可选 |
| 医生ID | ElInput | 可选 |

**检查结果展示**：同处方审核，展示 PASS/WARN/BLOCK 及检查项明细

### 3.9 DRG 分组 (`/drg/grouping`) (V1)

| 字段 | 组件 | 说明 |
|------|------|------|
| 就诊ID | ElInput | 必填 |
| 患者ID | ElInput | 必填 |
| 主要诊断 | ElInput | 必填 |
| 次要诊断 | ElSelect multiple | 可选 |
| 手术编码 | ElSelect multiple | 可选 |
| 总费用 | ElInputNumber | 必填 |

**分组结果展示**

| 区域 | 说明 |
|------|------|
| DRG 信息 | DRG编码、名称、MDC分类 |
| 权重/费用 | 基础权重、调整权重、标准分值、总费用、支付金额 |
| 分组状态 | 成功/失败 |

---

### 3.10 规则流编排模块 (V2)

#### 规则流列表 (`/flow/list`)

| 功能 | 说明 |
|------|------|
| 搜索栏 | 分类、状态、规则流Key、名称 |
| 表格列 | ID、flowKey、名称、分类、版本、状态、节点数、更新时间、操作 |
| 操作 | 编辑（进入画布）、查看详情、发布、回滚、导入/导出、删除 |

#### 规则流可视化编辑器 (`/flow/editor/:id?`)

**三栏布局**：节点面板 | 画布区 | 属性配置面板

| 区域 | 组件 | 说明 |
|------|------|------|
| 顶部工具栏 | ElButton 组 | 保存、发布、撤销、重做、导入、导出、缩放、适应画布 |
| 节点面板（左侧） | AntV X6 Stencil | 条件节点、动作节点、公式节点、子流程节点、开始/结束节点 |
| 规则库面板（左侧折叠） | ElTree | 按分类展示已有规则/公式，拖入画布自动创建节点 |
| 画布区（中央） | AntV X6 Graph | 拖拽放置节点、连线、缩放、平移、框选、对齐 |
| 属性配置面板（右侧） | ElForm | 选中节点后展示属性编辑表单 |
| 迷你地图 | AntV X6 Minimap | 画布缩略图 |

**节点类型与属性配置**

| 节点类型 | 图标颜色 | 属性字段 |
|---------|---------|---------|
| 开始节点 (start) | 绿色 | label |
| 结束节点 (end) | 红色 | label |
| 条件节点 (condition) | 橙色菱形 | label、expression（Aviator表达式）、true分支目标、false分支目标 |
| 动作节点 (action) | 蓝色矩形 | label、ruleKey（从规则库选择）、timeout(ms)、失败策略(抛异常/跳过/降级) |
| 公式节点 (formula) | 紫色矩形 | label、formulaKey（从公式库选择）、参数映射表 |
| 子流程节点 (subflow) | 青色矩形 | label、flowId（从规则流列表选择）、async(是否异步) |

**画布交互**

| 操作 | 说明 |
|------|------|
| 拖拽创建 | 从节点面板拖入画布创建节点 |
| 连线 | 从节点端口拖出连线到目标节点 |
| 删除 | 选中节点/连线后按 Delete 或右键删除 |
| 属性编辑 | 单击节点，右侧面板展示属性表单 |
| 批量操作 | 框选多节点，批量移动/删除 |
| 撤销/重做 | Ctrl+Z / Ctrl+Y |
| 自动布局 | 一键自动排列节点 |
| 校验 | 检查流程完整性（是否有开始/结束、是否有断连、表达式语法） |

#### 规则流详情 (`/flow/detail/:id`)

| 区域 | 说明 |
|------|------|
| 基本信息卡片 | flowKey、名称、分类、版本、状态 |
| 流程图预览 | AntV X6 只读模式渲染 |
| 节点列表 | ElTable 展示所有节点及其属性 |
| 操作按钮 | 编辑（进入画布）、发布、校验、停用 |

#### 规则流版本历史 (`/flow/history/:id`)

| 列 | 说明 |
|------|------|
| 版本号、变更说明、变更人、变更时间 | ElTimeline |
| 版本对比 | 选择两个版本，Monaco Diff Editor 展示 JSON 差异 |
| 回滚 | 一键回滚到指定版本 |

#### 规则流版本对比 (`/flow/compare/:id`)

| 区域 | 说明 |
|------|------|
| 版本选择 | 两个 ElSelect 选择对比版本 |
| 差异展示 | Monaco Diff Editor 展示 flowDefinition JSON 差异 |
| 图形对比 | 左右两个 AntV X6 只读画布并排展示 |

### 3.11 测试沙箱模块 (V2)

#### 沙箱首页 (`/sandbox/dashboard`)

| 区域 | 说明 |
|------|------|
| 统计卡片 | 数据集总数、测试用例总数、最近执行通过率、最近执行时间 |
| 快捷入口 | 新建数据集、执行测试、查看报告 |
| 最近执行 | 最近 10 条执行记录列表 |

#### 测试数据集列表 (`/sandbox/datasets`)

| 功能 | 说明 |
|------|------|
| 搜索栏 | 分类（SETTLEMENT/DRUG/QUALITY/DRG）、名称 |
| 表格列 | ID、名称、描述、分类、用例数、创建时间、操作 |
| 操作 | 编辑、执行全部、删除、导入/导出 |

#### 测试数据集创建/编辑 (`/sandbox/dataset-form/:id?`)

| 字段 | 组件 | 说明 |
|------|------|------|
| 数据集名称 | ElInput | 必填 |
| 描述 | ElInput textarea | 可选 |
| 分类 | ElSelect | SETTLEMENT/DRUG/QUALITY/DRG |
| 测试用例列表 | **动态表格（可拖拽排序）** | 可增删行 |

**测试用例行**

| 字段 | 组件 | 说明 |
|------|------|------|
| 用例ID | ElInput | 自动生成，可编辑 |
| 用例名称 | ElInput | 必填 |
| 输入Fact | ElButton → 弹窗 JSON 编辑器 | 必填 |
| Aviator变量 | ElButton → 弹窗 Key-Value 编辑器 | 可选 |
| 预期结果 | ElButton → 弹窗 JSON 编辑器 | 必填 |

#### 测试用例构建器 (`/sandbox/test-builder`)

| 区域 | 组件 | 说明 |
|------|------|------|
| 选择目标 | ElSelect | 选择要测试的规则Key或公式Key |
| 输入参数 | **动态表单** | 根据规则/公式的参数定义自动生成表单字段 |
| 预期结果 | JSON 编辑器 | 手动填写预期输出 |
| 快速填充 | ElButton | 从历史数据/模板加载 |

#### 沙箱执行界面 (`/sandbox/execute/:dataSetId`)

**左右分栏布局**

| 左侧 | 说明 |
|------|------|
| 测试数据集树 | ElTree，按分类展开，勾选要执行的用例 |
| 操作按钮 | 执行选中、执行全部、生成报告 |

| 右侧 | 说明 |
|------|------|
| 执行结果概览 | 总数、通过数、失败数、通过率进度条 |
| 用例结果列表 | 每行：用例ID、名称、状态(✅/❌)、耗时、操作(查看详情) |
| 详情弹窗 | 输入参数、预期结果、实际结果、Diff对比(JsonDiffView) |
| 执行日志 | 实时滚动日志，显示每步执行过程 |

#### 测试报告 (`/sandbox/report/:executionId`)

| 区域 | 说明 |
|------|------|
| 报告头 | 数据集名称、执行时间、总耗时 |
| 统计摘要 | 总数/通过/失败/错误、通过率饼图 |
| 用例明细表 | 每条用例的输入、预期、实际、Diff、耗时 |
| 导出按钮 | 导出 HTML / PDF |

### 3.12 监控大屏模块 (V2)

#### 监控看板 (`/monitor/dashboard`)

**全屏大屏模式，深色主题，WebSocket 实时推送**

| 区域 | 组件 | 说明 |
|------|------|------|
| 顶部标题栏 | 标题 + 刷新频率切换(实时/1min/5min) + 全屏按钮 | |
| 核心指标卡片 | 数字翻牌器 | 规则执行总次数、规则命中率(%)、P99执行耗时(ms)、异常规则数 |
| 执行耗时分布 | ECharts Histogram | P50/P75/P90/P99 耗时柱状图 |
| TOP10 高频规则 | ECharts 横向柱状图 | 按执行次数排序 |
| TOP10 慢规则 | ECharts 横向柱状图 | 按 P99 耗时排序 |
| 规则触发热力图 | ECharts Heatmap | 按分类+时间维度的触发频次 |
| 异常规则 TOP5 | 列表 | 超时/解析失败/执行异常的规则 |
| 最近告警 | 滚动列表 | 最近触发的告警记录 |
| 结算趋势 | ECharts 折线图 | 近 24h 结算量/金额趋势 |
| 公式执行统计 | ECharts 饼图 | 各公式执行占比 |

#### 告警规则管理 (`/monitor/alerts`)

| 功能 | 说明 |
|------|------|
| 列表 | 规则名称、指标名称、条件、阈值、持续时间、通知方式、启用状态、操作 |
| 创建/编辑 | 表单：指标名(下拉)、条件(>/</>=/<=/=)、阈值、持续时间(秒)、通知方式(email/dingtalk)、通知地址 |
| 启用/禁用 | Switch 切换 |

#### 历史数据查询 (`/monitor/history`)

| 功能 | 说明 |
|------|------|
| 时间范围选择 | ElDatePicker range |
| 指标选择 | ElSelect 多选 |
| 趋势图 | ECharts 折线图，支持多指标叠加 |
| 数据表格 | 原始指标数据列表 |

### 3.13 规则市场模块 (V2)

#### 市场模板列表 (`/market/list`)

| 功能 | 说明 |
|------|------|
| 分类标签 | 全部 / 医保报销 / 合理用药 / 质控 / DRG分组 |
| 搜索栏 | 关键词搜索 |
| 排序 | 最新发布 / 最多安装 / 最高评分 |
| 模板卡片 | 名称、版本、评分(星级)、分类标签、提供者、安装数、简介 |
| 操作 | 查看详情、订阅安装 |

#### 模板详情 (`/market/detail/:id`)

| 区域 | 说明 |
|------|------|
| 基本信息 | 名称、版本、分类、标签、描述、提供者、发布时间 |
| 统计 | 安装数、平均评分、评论数 |
| 内容预览 | 包含的规则列表、公式列表、规则流列表（只读） |
| 操作按钮 | 订阅安装、收藏 |
| 评论区 | 评分 + 评论列表 + 发表评论 |

#### 发布模板 (`/market/publish`)

| 字段 | 组件 | 说明 |
|------|------|------|
| 模板名称 | ElInput | 必填 |
| 分类 | ElSelect | 必填 |
| 标签 | ElTag + ElInput | 可增删 |
| 描述 | ElInput textarea | 必填 |
| 选择规则 | ElTransfer / ElTree 多选 | 从本租户已有规则中选择 |
| 选择公式 | ElTransfer / ElTree 多选 | 从本租户已有公式中选择 |
| 选择规则流 | ElTransfer / ElTree 多选 | 从本租户已有规则流中选择 |
| 安全扫描 | ElButton | 点击扫描，展示扫描结果 |

#### 我的发布 (`/market/my`)

| 功能 | 说明 |
|------|------|
| 列表 | 模板名称、版本、状态、安装数、评分、操作 |
| 操作 | 编辑、下架、更新版本 |

#### 已订阅模板 (`/market/subscribed`)

| 功能 | 说明 |
|------|------|
| 列表 | 模板名称、安装版本、最新版本、安装时间、状态、操作 |
| 操作 | 更新到最新版本、取消订阅 |
| 版本提示 | 安装版本 < 最新版本时显示"有新版本"标签 |

---

### 3.14 审计日志 (`/audit/log`) (V1)

| 功能 | 说明 |
|------|------|
| 搜索 | 操作类型(CREATE/UPDATE/PUBLISH/DELETE/EXECUTE/SUBSCRIBE)、目标类型(RULE/FORMULA/FLOW/SETTLEMENT/TEMPLATE)、操作人、时间范围 |
| 表格 | 操作时间、操作人、操作类型、目标类型、目标ID、目标Key、IP地址 |
| 详情 | 点击展开操作详情 JSON |

### 3.15 系统管理 (V1)

#### 租户管理 (`/system/tenant`)

| 功能 | 说明 |
|------|------|
| 列表 | 租户ID、名称、联系人、联系电话、状态 |
| CRUD | 新增/编辑/启用/禁用租户 |

---

## 四、核心公共组件设计

### 4.1 Monaco Editor 封装

```
功能要点:
- 支持 DRL 语法高亮（自定义 Monarch 语法定义）
- 支持 Aviator 语法高亮（自定义 Monarch 语法定义）
- 支持 JSON 语法高亮（内置）
- 只读/编辑模式切换
- 代码折叠、行号、搜索
- 主题适配（亮/暗色）
- Props: { language, modelValue, readOnly, height }
- Emits: { update:modelValue, change }
```

### 4.2 Monaco Diff Editor 封装 (V2)

```
功能要点:
- 左右对比模式 / 内联对比模式
- 用于规则/公式/规则流版本对比
- Props: { original, modified, language, mode }
```

### 4.3 StatusTag 状态标签

| 状态值 | 颜色 | 文案 |
|--------|------|------|
| draft | info | 草稿 |
| validated | warning | 已校验 |
| active | success | 已发布 |
| inactive | danger | 已停用 |
| pending | warning | 结算中 |
| completed | success | 已完成 |
| failed | danger | 失败 |
| PUBLISHED | success | 已上架 (V2) |
| ARCHIVED | info | 已下架 (V2) |

### 4.4 ResultLevel 结果级别

| 级别 | 颜色 | 图标 |
|------|------|------|
| PASS | 绿色 success | ✓ |
| WARN | 橙色 warning | ⚠ |
| BLOCK | 红色 danger | ✕ |

### 4.5 JsonDiffView (V2)

```
功能要点:
- 递归对比两个 JSON 对象
- 差异字段高亮（新增=绿色、删除=红色、修改=黄色）
- 折叠/展开
- Props: { expected, actual }
```

### 4.6 VersionTimeline (V2)

```
功能要点:
- ElTimeline 封装
- 展示版本号、变更说明、变更人、变更时间
- 支持选择两个版本进行对比
- 支持回滚操作
- Props: { versions, currentVersion }
- Emits: { compare, rollback }
```

### 4.7 useTable 组合式函数

```typescript
function useTable<T>(apiFn: (params) => Promise<Result<PageResult<T>>>) {
  // 响应式: tableData, loading, pagination, queryParams
  // 方法: fetchList(), handleSearch(), handleReset(), handlePageChange()
  return { tableData, loading, pagination, fetchList, ... }
}
```

### 4.8 useWebSocket 组合式函数 (V2)

```typescript
function useWebSocket(url: string) {
  // 响应式: data, status, error
  // 方法: connect(), disconnect(), send()
  // 自动重连机制
  // 心跳检测
  return { data, status, connect, disconnect, send }
}
```

### 4.9 useFlowEditor 组合式函数 (V2)

```typescript
function useFlowEditor(containerRef: Ref<HTMLElement>) {
  // AntV X6 Graph 实例管理
  // 节点注册、事件绑定
  // 撤销/重做栈
  // 序列化/反序列化 flowDefinition
  // 校验流程完整性
  return { graph, addNode, removeNode, serialize, validate, undo, redo }
}
```

---

## 五、API 对接设计

### 5.1 Axios 实例配置

```typescript
// 统一请求/响应拦截
- 请求拦截: 自动添加 Authorization Token + X-Tenant-Id Header
- 响应拦截:
  - code === "0" → 正常返回 data
  - code !== "0" → ElMessage.error(message) 统一错误提示
  - 401 → 跳转登录页
  - 403 → 无权限提示
  - 500 → 网络异常提示
```

### 5.2 V1.0 API 接口清单

| 模块 | 方法 | 接口 | 说明 |
|------|------|------|------|
| 规则管理 | GET | `/api/v1/rules` | 分页查询 |
| | GET | `/api/v1/rules/{id}` | 详情 |
| | POST | `/api/v1/rules` | 创建 |
| | PUT | `/api/v1/rules/{id}` | 更新 |
| | DELETE | `/api/v1/rules/{id}` | 删除 |
| | POST | `/api/v1/rules/{id}/publish` | 发布 |
| | POST | `/api/v1/rules/{id}/validate` | 校验 |
| 规则分组 | GET | `/api/v1/rule-groups` | 列表 |
| | GET | `/api/v1/rule-groups/page` | 分页 |
| | GET | `/api/v1/rule-groups/{id}` | 详情 |
| | POST | `/api/v1/rule-groups` | 创建 |
| | PUT | `/api/v1/rule-groups/{id}` | 更新 |
| | PUT | `/api/v1/rule-groups/{id}/enabled` | 启用禁用 |
| | DELETE | `/api/v1/rule-groups/{id}` | 删除 |
| 公式管理 | GET | `/api/v1/formulas` | 分页查询 |
| | GET | `/api/v1/formulas/{id}` | 详情 |
| | GET | `/api/v1/formulas/key/{key}` | 按Key查询 |
| | POST | `/api/v1/formulas` | 创建 |
| | PUT | `/api/v1/formulas/{id}` | 更新 |
| | DELETE | `/api/v1/formulas/{id}` | 删除 |
| | POST | `/api/v1/formulas/{id}/publish` | 发布 |
| | POST | `/api/v1/formulas/{id}/validate` | 校验 |
| 结算管理 | POST | `/api/v1/settlements` | 发起结算 |
| | GET | `/api/v1/settlements/page` | 分页查询 |
| | GET | `/api/v1/settlements/{settlementNo}` | 详情 |
| | POST | `/api/v1/settlements/cache/refresh` | 刷新缓存 |
| | POST | `/api/v1/settlements/cache/clear` | 清空缓存 |
| 用药审核 | POST | `/api/v1/drugs/review` | 处方审核 |
| 质控管理 | POST | `/api/v1/quality/check` | 质控检查 |
| DRG 分组 | POST | `/api/v1/drg/grouping` | DRG分组 |

### 5.3 V2.0 API 接口清单

| 模块 | 方法 | 接口 | 说明 |
|------|------|------|------|
| 规则流 | GET | `/api/v2/flows` | 分页查询规则流 |
| | GET | `/api/v2/flows/{id}` | 获取规则流详情 |
| | POST | `/api/v2/flows` | 创建规则流 |
| | PUT | `/api/v2/flows/{id}` | 更新规则流 |
| | DELETE | `/api/v2/flows/{id}` | 删除规则流 |
| | POST | `/api/v2/flows/{id}/publish` | 发布规则流 |
| | POST | `/api/v2/flows/{id}/rollback` | 回滚到历史版本 |
| | GET | `/api/v2/flows/{id}/versions` | 获取版本历史 |
| | GET | `/api/v2/flows/{id}/compare` | 版本对比 |
| | GET | `/api/v2/flows/{id}/export` | 导出规则流JSON |
| | POST | `/api/v2/flows/import` | 导入规则流JSON |
| 测试沙箱 | GET | `/api/v2/sandbox/datasets` | 查询测试数据集 |
| | POST | `/api/v2/sandbox/datasets` | 创建测试数据集 |
| | PUT | `/api/v2/sandbox/datasets/{id}` | 更新数据集 |
| | DELETE | `/api/v2/sandbox/datasets/{id}` | 删除数据集 |
| | POST | `/api/v2/sandbox/execute` | 执行单条测试 |
| | POST | `/api/v2/sandbox/execute/batch` | 批量执行 |
| | GET | `/api/v2/sandbox/reports/{executionId}` | 获取测试报告 |
| | GET | `/api/v2/sandbox/reports/{executionId}/export` | 导出报告(HTML/PDF) |
| 监控 | GET | `/api/v2/monitor/dashboard` | 获取监控看板数据 |
| | GET | `/api/v2/monitor/rules/top` | TOP N 规则统计 |
| | GET | `/api/v2/monitor/heatmap` | 热力图数据 |
| | GET | `/api/v2/monitor/trends` | 趋势数据 |
| | GET | `/api/v2/monitor/alerts` | 查询告警规则 |
| | POST | `/api/v2/monitor/alerts` | 创建告警规则 |
| | PUT | `/api/v2/monitor/alerts/{id}` | 更新告警规则 |
| | DELETE | `/api/v2/monitor/alerts/{id}` | 删除告警规则 |
| | WS | `/ws/monitor` | 监控数据实时推送 |
| 规则市场 | GET | `/api/v2/market/templates` | 查询模板列表 |
| | GET | `/api/v2/market/templates/{id}` | 模板详情 |
| | POST | `/api/v2/market/templates` | 发布模板 |
| | PUT | `/api/v2/market/templates/{id}` | 更新模板 |
| | DELETE | `/api/v2/market/templates/{id}` | 下架模板 |
| | POST | `/api/v2/market/templates/{id}/subscribe` | 订阅模板 |
| | POST | `/api/v2/market/templates/{id}/unsubscribe` | 取消订阅 |
| | GET | `/api/v2/market/my` | 我的发布 |
| | GET | `/api/v2/market/subscribed` | 已订阅模板 |
| | POST | `/api/v2/market/templates/{id}/rate` | 评分 |
| | GET | `/api/v2/market/templates/{id}/comments` | 评论列表 |
| | GET | `/api/v2/market/categories` | 分类列表 |

---

## 六、权限控制设计

### 6.1 路由级权限

```typescript
{
  path: '/flow',
  meta: {
    title: '规则流编排',
    roles: ['ADMIN', 'RULE_ADMIN'],
    icon: 'Share'
  }
}
```

### 6.2 按钮级权限

```html
<el-button v-permission="['RULE_ADMIN']" @click="handlePublish">发布规则</el-button>
<el-button v-permission="['ADMIN', 'RULE_ADMIN']" @click="handleDelete">删除</el-button>
```

### 6.3 角色菜单映射（V1 + V2 全量）

| 角色 | 可见菜单 |
|------|---------|
| ADMIN | 全部菜单 |
| RULE_ADMIN | 仪表盘、规则管理、规则分组、公式管理、规则流编排、测试沙箱、审计日志 |
| INSURANCE_SPEC | 仪表盘、规则管理(受限)、公式管理、结算管理、测试沙箱(受限) |
| PHARMACIST | 仪表盘、用药审核、测试沙箱(受限) |
| QUALITY_SPEC | 仪表盘、质控管理、测试沙箱(受限) |
| DOCTOR | 仪表盘、结算查询(只读) |
| AUDITOR | 仪表盘、审计日志(只读)、监控大屏(只读) |

### 6.4 V2.0 新增权限点

| 功能 | 允许角色 |
|------|---------|
| 规则流创建/编辑 | ADMIN, RULE_ADMIN |
| 规则流发布 | ADMIN, RULE_ADMIN |
| 测试沙箱执行 | ADMIN, RULE_ADMIN, INSURANCE_SPEC, PHARMACIST, QUALITY_SPEC |
| 监控大屏查看 | ADMIN, RULE_ADMIN, AUDITOR |
| 告警规则配置 | ADMIN |
| 规则市场发布 | ADMIN, RULE_ADMIN |
| 规则市场订阅 | ADMIN, RULE_ADMIN, INSURANCE_SPEC, PHARMACIST, QUALITY_SPEC |
| 规则市场评分 | 所有已认证用户 |

---

## 七、开发阶段规划

### 第一阶段：项目初始化与基础框架（3天）

| 任务 | 说明 |
|------|------|
| 项目脚手架搭建 | Vite + Vue3 + TS + Element Plus + Pinia + Router |
| 布局框架开发 | AppLayout + Sidebar + Navbar + TagsView |
| Axios 封装 | 请求/响应拦截、Token 管理、统一错误处理 |
| 登录页开发 | 登录表单 + 租户选择 + Token 存储 |
| 路由守卫 | 登录拦截 + 权限校验 + 动态路由 |
| 公共组件开发 | StatusTag、SearchForm、ConfirmDialog |
| useTable 封装 | 分页查询通用逻辑 |

### 第二阶段：V1 核心业务页面（5天）

| 任务 | 说明 |
|------|------|
| Monaco Editor 封装 | DRL/Aviator 语法高亮、只读/编辑模式 |
| 规则管理 | 列表 + 创建/编辑 + 详情 + 发布/校验/停用 |
| 规则分组管理 | 列表 + CRUD + 启用/禁用 |
| 公式管理 | 列表 + 创建/编辑(含动态参数表单) + 详情 + 发布/校验 |
| 版本历史 | 规则/公式版本历史时间线 |

### 第三阶段：V1 业务执行页面（4天）

| 任务 | 说明 |
|------|------|
| 结算管理 | 列表 + 发起结算 + 结算详情 + Skill结果展示 |
| 用药审核 | 处方审核表单(动态药品列表) + 审核结果展示 |
| 配伍禁忌管理 | 列表 + CRUD |
| 质控管理 | 质控检查表单 + 结果展示 |
| DRG 分组 | 分组表单 + 分组结果展示 |

### 第四阶段：V2 规则流编排（5天）

| 任务 | 说明 |
|------|------|
| AntV X6 集成 | Vue3 封装、Graph 初始化、节点注册 |
| 节点面板开发 | 条件/动作/公式/子流程/开始/结束节点拖拽面板 |
| 画布交互开发 | 拖拽创建、连线、删除、缩放、框选、自动布局 |
| 属性面板开发 | 各节点类型的属性编辑表单 |
| 规则库/公式库面板 | 拖入画布自动创建节点 |
| 规则流列表/详情 | CRUD + 发布 + 校验 |
| 撤销/重做 | 操作栈管理 |
| 导入/导出 | JSON 格式导入导出 |

### 第五阶段：V2 测试沙箱（4天）

| 任务 | 说明 |
|------|------|
| 测试数据集管理 | 列表 + 创建/编辑(含动态用例表) + 导入/导出 |
| 测试用例构建器 | 参数化表单 + JSON 编辑器 + 快速填充 |
| 沙箱执行界面 | 左右分栏 + 用例勾选 + 执行 + 实时日志 |
| 结果对比 | JsonDiffView 组件 + Diff 高亮 |
| 测试报告 | 统计摘要 + 用例明细 + HTML/PDF 导出 |

### 第六阶段：V2 监控大屏（4天）

| 任务 | 说明 |
|------|------|
| WebSocket 封装 | useWebSocket + 自动重连 + 心跳 |
| 大屏布局 | 深色主题 + 全屏 + 响应式网格 |
| 核心指标卡片 | 数字翻牌器 + 实时更新 |
| ECharts 图表 | 耗时分布/高频TOP10/慢规则TOP10/热力图/趋势图 |
| 告警规则管理 | 列表 + CRUD + 启用/禁用 |
| 历史数据查询 | 时间范围 + 指标选择 + 趋势图 |

### 第七阶段：V2 规则市场 + 系统功能（4天）

| 任务 | 说明 |
|------|------|
| 市场模板列表 | 分类标签 + 搜索 + 排序 + 卡片布局 |
| 模板详情 | 基本信息 + 内容预览 + 评分 + 评论 |
| 发布模板 | 表单 + 规则/公式/规则流选择 + 安全扫描 |
| 我的发布/订阅 | 列表 + 版本更新提示 |
| 仪表盘完善 | V2 监控摘要 + 规则流状态 + 市场动态 |
| 审计日志 | 列表 + 搜索 + 详情展开(V2增加FLOW/MARKET目标类型) |
| 租户管理 | 列表 + CRUD |
| 权限完善 | 按钮级权限 + 角色菜单映射 |

### 第八阶段：联调测试与部署（3天）

| 任务 | 说明 |
|------|------|
| 后端联调 | 全部 V1 + V2 API 对接测试 |
| 边界处理 | 空状态、加载态、错误态、网络异常 |
| 性能优化 | 路由懒加载、组件按需引入、Monaco Editor 异步加载、AntV X6 按需加载 |
| 构建部署 | Nginx 配置、环境变量、Docker 部署 |
| 代码审查 | ESLint + 类型检查 |

**总计: 32 个工作日**

---

## 八、环境配置

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:9007/ws/monitor
VITE_APP_TITLE=HIS 动态规则中台

# .env.production
VITE_API_BASE_URL=https://api.his-hospital.com
VITE_WS_URL=wss://api.his-hospital.com/ws/monitor
VITE_APP_TITLE=HIS 动态规则中台
```

---

## 九、关键依赖 package.json

```json
{
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.0",
    "element-plus": "^2.7.0",
    "axios": "^1.7.0",
    "monaco-editor": "^0.45.0",
    "echarts": "^5.5.0",
    "vue-i18n": "^9.13.0",
    "@element-plus/icons-vue": "^2.3.0",
    "@antv/x6": "^2.18.0",
    "@antv/x6-plugin-selection": "^2.2.0",
    "@antv/x6-plugin-snapline": "^2.1.0",
    "@antv/x6-plugin-clipboard": "^2.1.0",
    "@antv/x6-plugin-history": "^2.2.0",
    "@antv/x6-plugin-minimap": "^2.0.0",
    "@antv/x6-plugin-stencil": "^2.1.0",
    "@antv/x6-vue-shape": "^2.1.0",
    "vuedraggable": "^4.1.0",
    "deep-diff": "^1.0.2",
    "html2canvas": "^1.4.1",
    "jspdf": "^2.5.1"
  },
  "devDependencies": {
    "vite": "^5.4.0",
    "@vitejs/plugin-vue": "^5.0.0",
    "typescript": "^5.4.0",
    "sass": "^1.77.0",
    "eslint": "^8.57.0",
    "prettier": "^3.2.0",
    "unplugin-auto-import": "^0.17.0",
    "unplugin-vue-components": "^0.27.0"
  }
}
```

---

## 十、与后端对接注意事项

| 事项 | 说明 |
|------|------|
| 统一响应格式 | 后端返回 `Result<T>` → 前端解构 `{ code, data, message, timestamp }` |
| 金额字段 | 后端 BigDecimal → 前端使用字符串展示，避免浮点精度丢失 |
| 租户隔离 | 所有请求 Header 携带 `X-Tenant-Id` |
| 分页参数 | `page` 从 1 开始，`pageSize` 默认 20 |
| 状态枚举 | 前端维护与后端一致的状态枚举字典 |
| 日期格式 | 统一 ISO 8601 格式 `yyyy-MM-dd'T'HH:mm:ss` |
| DRL/Aviator 内容 | 大文本字段，使用 Monaco Editor 编辑，POST 请求传输 |
| 规则流定义 | flowDefinition 为 JSON 字符串，前端序列化/反序列化 |
| 监控数据 | WebSocket 推送 JSON，前端解析后更新 Pinia Store |
| V2 API 版本 | V2 接口统一前缀 `/api/v2/`，V1 接口保持 `/api/v1/` |
| 安全扫描 | 市场发布模板前调用安全扫描接口，展示扫描结果 |
| WebSocket 重连 | 监控大屏断线自动重连，指数退避策略 |
| 规则流导入导出 | JSON 格式，导入时校验格式完整性 |

---

## 十一、TypeScript 类型定义参考

### 通用类型

```typescript
// api.ts
interface Result<T> {
  code: string
  data: T
  message: string
  timestamp: number
}

interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

interface PageParams {
  page: number
  pageSize: number
}
```

### V1 类型定义

```typescript
// rule.ts
interface RuleVO {
  id: number
  ruleGroupId: number | null
  groupName: string
  ruleKey: string
  ruleName: string
  ruleText: string
  category: string
  version: number
  status: 'draft' | 'validated' | 'active' | 'inactive'
  description: string
  salience: number
  activationGroup: string
  effectiveStart: string | null
  effectiveEnd: string | null
  tenantId: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
}

interface RuleCreateDTO {
  ruleKey: string
  ruleName: string
  ruleText: string
  category: string
  ruleGroupId?: number
  description?: string
  salience?: number
  activationGroup?: string
  effectiveStart?: string
  effectiveEnd?: string
}

interface RuleUpdateDTO {
  ruleName?: string
  ruleText?: string
  ruleGroupId?: number
  description?: string
  salience?: number
  activationGroup?: string
  effectiveStart?: string
  effectiveEnd?: string
}

interface RuleQueryDTO {
  category?: string
  status?: string
  ruleKey?: string
  ruleName?: string
  ruleGroupId?: number
}

// formula.ts
interface FormulaVO {
  id: number
  formulaKey: string
  formulaName: string
  formulaText: string
  category: string
  version: number
  status: 'draft' | 'validated' | 'active' | 'inactive'
  description: string
  isValidated: number
  validatedMsg: string
  tenantId: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
  params: FormulaParamVO[]
}

interface FormulaParamVO {
  id: number
  paramName: string
  paramType: string
  defaultValue: string
  description: string
  paramOrder: number
}

interface FormulaCreateDTO {
  formulaKey: string
  formulaName: string
  formulaText: string
  category: string
  description?: string
  params?: FormulaParamDTO[]
}

interface FormulaParamDTO {
  paramName: string
  paramType: string
  defaultValue?: string
  description?: string
  paramOrder?: number
}

interface FormulaUpdateDTO {
  formulaName?: string
  formulaText?: string
  description?: string
  params?: FormulaParamDTO[]
}

interface FormulaQueryDTO {
  category?: string
  status?: string
  formulaKey?: string
  formulaName?: string
}

// settlement.ts
interface SettlementVO {
  id: number
  settlementNo: string
  visitId: string
  patientId: string
  patientType: string
  insuranceType: string
  hospitalLevel: string
  totalFee: string
  deductible: string
  ratio: string
  reimburseAmount: string
  selfPayAmount: string
  resultLevel: 'PASS' | 'WARN' | 'BLOCK'
  status: 'pending' | 'completed' | 'failed'
  tenantId: string
  createBy: string
  createTime: string
}

interface SettlementDTO {
  visitId: string
  patientId: string
  patientType: string
  insuranceType?: string
  hospitalLevel?: string
  totalFee: number
}

// drug.ts
interface PrescriptionDTO {
  visitId: string
  patientId: string
  patientInfo?: string
  drugs: DrugItem[]
  diagnosisCodes?: string[]
  doctorId: string
}

interface DrugItem {
  drugCode: string
  drugName: string
  specification?: string
  dosage?: string
  unit?: string
  quantity?: number
  administration?: string
}

interface PrescriptionReviewVO {
  visitId: string
  reviewStatus: string
  pass: boolean
  reviewItems: ReviewItem[]
}

interface ReviewItem {
  level: string
  source: string
  message: string
  drugCode: string
  drugName: string
}

// quality.ts
interface QualityCheckDTO {
  visitId: string
  patientId: string
  diagnosisCodes?: string[]
  procedureCodes?: string[]
  deptCode?: string
  doctorId?: string
}

interface QualityCheckVO {
  visitId: string
  checkStatus: string
  pass: boolean
  checkItems: CheckItem[]
}

interface CheckItem {
  level: string
  source: string
  message: string
  code: string
}

// drg.ts
interface DrgGroupingDTO {
  visitId: string
  patientId: string
  primaryDiagnosis: string
  secondaryDiagnoses?: string[]
  procedureCodes?: string[]
  totalFee: number
}

interface DrgGroupingVO {
  visitId: string
  drgCode: string
  drgName: string
  mdcCode: string
  mdcName: string
  baseWeight: string
  adjustWeight: string
  standardScore: string
  totalFee: string
  paymentAmount: string
  groupingStatus: string
  success: boolean
}
```

### V2 类型定义

```typescript
// flow.ts
interface FlowNode {
  nodeId: string
  type: 'start' | 'end' | 'condition' | 'action' | 'formula' | 'subflow'
  label: string
  position: { x: number; y: number }
  expression?: string
  branches?: { true: string; false: string }
  ruleKey?: string
  formulaKey?: string
  flowId?: string
  next?: string
  timeout?: number
  params?: Record<string, string>
  async?: boolean
}

interface FlowEdge {
  source: string
  target: string
  label?: string
}

interface FlowDefinition {
  flowId: string
  flowName: string
  version: number
  nodes: FlowNode[]
  edges: FlowEdge[]
}

interface RuleFlowVO {
  id: number
  flowKey: string
  flowName: string
  flowDefinition: string
  version: number
  status: 'draft' | 'active' | 'inactive'
  category: string
  tenantId: string
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
}

interface CreateFlowDTO {
  flowName: string
  category: string
  flowDefinition: FlowDefinition
  description?: string
}

interface UpdateFlowDTO {
  flowName?: string
  flowDefinition?: FlowDefinition
  description?: string
}

interface FlowVersionVO {
  version: number
  flowDefinition: string
  changeDesc: string
  changeBy: string
  changeTime: string
}

interface FlowCompareVO {
  versionA: FlowVersionVO
  versionB: FlowVersionVO
}

// sandbox.ts
interface TestDataSetVO {
  id: number
  setName: string
  setDescription: string
  category: string
  testCases: TestCase[]
  tenantId: string
  createTime: string
}

interface TestCase {
  caseId: string
  caseName: string
  description?: string
  fact: Record<string, any>
  expected: Record<string, any>
  variables?: Record<string, any>
}

interface TestCaseResult {
  caseId: string
  status: 'SUCCESS' | 'FAILED' | 'ERROR'
  actual: Record<string, any>
  expected: Record<string, any>
  diff: Record<string, DiffItem>
  executeMs: number
  errorMessage?: string
}

interface DiffItem {
  expected: any
  actual: any
}

interface BatchTestResult {
  total: number
  success: number
  failed: number
  successRate: number
  results: TestCaseResult[]
}

interface CreateDataSetDTO {
  setName: string
  setDescription?: string
  category: string
  testCases: TestCase[]
}

// monitor.ts
interface DashboardVO {
  totalExecutions: number
  hitRate: number
  p99Duration: number
  alertCount: number
  topRules: RuleStatVO[]
  alertRules: AlertRuleVO[]
}

interface RuleStatVO {
  ruleKey: string
  executionCount: number
  avgDuration: number
  p99Duration: number
  hitRate: number
}

interface HeatmapVO {
  categories: string[]
  timeSlots: string[]
  values: number[][]
}

interface TrendVO {
  timestamps: string[]
  values: number[]
}

interface AlertRuleVO {
  id: number
  ruleName: string
  metricName: string
  condition: string
  threshold: number
  duration: number
  notificationType: string
  notificationAddr: string
  enabled: boolean
  alreadyTriggered: boolean
}

interface CreateAlertRuleDTO {
  ruleName: string
  metricName: string
  condition: string
  threshold: number
  duration: number
  notificationType: string
  notificationAddr: string
}

// market.ts
interface TemplateVO {
  id: number
  templateKey: string
  templateName: string
  description: string
  category: string
  tags: string[]
  version: string
  providerTenantName: string
  publishedAt: string
  installCount: number
  avgRating: number
  commentCount: number
  status: 'PUBLISHED' | 'ARCHIVED'
}

interface TemplateDetailVO extends TemplateVO {
  content: TemplateContent
}

interface TemplateContent {
  rules: RuleVO[]
  formulas: FormulaVO[]
  flows: RuleFlowVO[]
}

interface PublishTemplateDTO {
  templateName: string
  category: string
  tags: string[]
  description: string
  content: TemplateContent
}

interface SubscribedTemplateVO {
  templateId: number
  templateName: string
  installedVersion: string
  latestVersion: string
  installTime: string
  status: 'ACTIVE' | 'UPDATED' | 'UNSUBSCRIBED'
}

interface CommentVO {
  id: number
  tenantName: string
  rating: number
  comment: string
  createTime: string
}

interface RateTemplateDTO {
  rating: number
  comment?: string
}
```

---

## 十二、字典常量定义参考

```typescript
// constants.ts

// ===== V1 常量 =====

export const RULE_CATEGORY_OPTIONS = [
  { label: '医保报销', value: 'reimbursement' },
  { label: '合理用药', value: 'drug' },
  { label: '质控管理', value: 'quality' },
  { label: 'DRG分组', value: 'drg' }
]

export const RULE_STATUS_OPTIONS = [
  { label: '草稿', value: 'draft', type: 'info' },
  { label: '已校验', value: 'validated', type: 'warning' },
  { label: '已发布', value: 'active', type: 'success' },
  { label: '已停用', value: 'inactive', type: 'danger' }
]

export const FORMULA_CATEGORY_OPTIONS = [
  { label: '医保报销', value: 'REIMBURSE' },
  { label: '合理用药', value: 'DRUG' },
  { label: 'DRG分组', value: 'DRG' },
  { label: '通用', value: 'GENERAL' }
]

export const PATIENT_TYPE_OPTIONS = [
  { label: '职工医保', value: 'employee' },
  { label: '居民医保', value: 'resident' },
  { label: '医疗救助', value: 'aid' }
]

export const HOSPITAL_LEVEL_OPTIONS = [
  { label: '一级', value: '1' },
  { label: '二级', value: '2' },
  { label: '三级', value: '3' }
]

export const SETTLEMENT_STATUS_OPTIONS = [
  { label: '结算中', value: 'pending', type: 'warning' },
  { label: '已完成', value: 'completed', type: 'success' },
  { label: '失败', value: 'failed', type: 'danger' }
]

export const RESULT_LEVEL_OPTIONS = [
  { label: '通过', value: 'PASS', type: 'success' },
  { label: '警告', value: 'WARN', type: 'warning' },
  { label: '阻断', value: 'BLOCK', type: 'danger' }
]

export const AUDIT_ACTION_OPTIONS = [
  { label: '创建', value: 'CREATE' },
  { label: '更新', value: 'UPDATE' },
  { label: '发布', value: 'PUBLISH' },
  { label: '删除', value: 'DELETE' },
  { label: '执行', value: 'EXECUTE' },
  { label: '订阅', value: 'SUBSCRIBE' }
]

export const AUDIT_TARGET_OPTIONS = [
  { label: '规则', value: 'RULE' },
  { label: '公式', value: 'FORMULA' },
  { label: '规则流', value: 'FLOW' },
  { label: '结算', value: 'SETTLEMENT' },
  { label: '模板', value: 'TEMPLATE' }
]

// ===== V2 常量 =====

export const FLOW_STATUS_OPTIONS = [
  { label: '草稿', value: 'draft', type: 'info' },
  { label: '已发布', value: 'active', type: 'success' },
  { label: '已停用', value: 'inactive', type: 'danger' }
]

export const FLOW_NODE_TYPE_OPTIONS = [
  { label: '开始', value: 'start', color: '#67C23A' },
  { label: '结束', value: 'end', color: '#F56C6C' },
  { label: '条件', value: 'condition', color: '#E6A23C' },
  { label: '动作', value: 'action', color: '#409EFF' },
  { label: '公式', value: 'formula', color: '#9B59B6' },
  { label: '子流程', value: 'subflow', color: '#1ABC9C' }
]

export const SANDBOX_CATEGORY_OPTIONS = [
  { label: '结算', value: 'SETTLEMENT' },
  { label: '用药', value: 'DRUG' },
  { label: '质控', value: 'QUALITY' },
  { label: 'DRG', value: 'DRG' }
]

export const TEST_CASE_STATUS_OPTIONS = [
  { label: '成功', value: 'SUCCESS', type: 'success' },
  { label: '失败', value: 'FAILED', type: 'danger' },
  { label: '错误', value: 'ERROR', type: 'danger' }
]

export const ALERT_CONDITION_OPTIONS = [
  { label: '大于', value: '>' },
  { label: '小于', value: '<' },
  { label: '大于等于', value: '>=' },
  { label: '小于等于', value: '<=' },
  { label: '等于', value: '=' }
]

export const NOTIFICATION_TYPE_OPTIONS = [
  { label: '邮件', value: 'email' },
  { label: '钉钉', value: 'dingtalk' }
]

export const MONITOR_METRIC_OPTIONS = [
  { label: '规则执行总数', value: 'rule_execution_total' },
  { label: '规则执行耗时', value: 'rule_execution_duration_seconds' },
  { label: '公式执行总数', value: 'formula_execution_total' },
  { label: '公式执行耗时', value: 'formula_execution_duration_seconds' },
  { label: 'Skill执行耗时', value: 'skill_pipeline_duration_seconds' },
  { label: '缓存命中数', value: 'cache_hit_total' },
  { label: '缓存未命中数', value: 'cache_miss_total' }
]

export const MARKET_CATEGORY_OPTIONS = [
  { label: '医保报销', value: 'REIMBURSE' },
  { label: '合理用药', value: 'DRUG' },
  { label: '质控', value: 'QUALITY' },
  { label: 'DRG分组', value: 'DRG' },
  { label: '通用', value: 'GENERAL' }
]

export const MARKET_STATUS_OPTIONS = [
  { label: '已上架', value: 'PUBLISHED', type: 'success' },
  { label: '已下架', value: 'ARCHIVED', type: 'info' }
]

export const SUBSCRIPTION_STATUS_OPTIONS = [
  { label: '已激活', value: 'ACTIVE', type: 'success' },
  { label: '有更新', value: 'UPDATED', type: 'warning' },
  { label: '已取消', value: 'UNSUBSCRIBED', type: 'info' }
]

export const MARKET_SORT_OPTIONS = [
  { label: '最新发布', value: 'publishedAt' },
  { label: '最多安装', value: 'installCount' },
  { label: '最高评分', value: 'avgRating' }
]

export const REFRESH_INTERVAL_OPTIONS = [
  { label: '实时', value: 0 },
  { label: '1分钟', value: 60000 },
  { label: '5分钟', value: 300000 }
]
```