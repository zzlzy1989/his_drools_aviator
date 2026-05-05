# 前端模块全量开发计划

**创建日期**: 2026-05-04
**状态**: ✅ completed
**完成日期**: 2026-05-05
**优先级**: P0

---

## 背景

当前前端所有业务模块（除首页、规则流设计器外）均显示"功能开发中..."，需要完整实现 CRUD 功能页面。

## 待开发模块清单

| # | 模块 | 路由 | 后端 API | 优先级 | 状态 |
|---|------|------|---------|--------|------|
| 1 | 规则组管理 | `/rule-group` | `/api/v1/rule-groups` | P1 | ✅ 已完成 |
| 2 | 规则定义 | `/rule` | `/api/v1/rules` | P1 | ✅ 已完成 |
| 3 | 公式管理 | `/formula` | `/api/v1/formulas` | P1 | ✅ 已完成 |
| 4 | 规则流列表 | `/flow` | `/api/v2/flows` | ✅ 已完成 | ✅ 已完成 |
| 5 | 规则流设计器 | `/flow/editor/:id` | `/api/v2/flows/*` | ✅ 已完成 | ✅ 已完成 |
| 6 | 结算管理 | `/settlement` | `/api/v1/settlements` | P2 | ✅ 已完成 |
| 7 | 用药审核 | `/drug` | `/api/v1/drugs` | P2 | ✅ 已完成 |
| 8 | 质量控制 | `/quality` | `/api/v1/quality` | P3 | ✅ 已完成 |
| 9 | DRG管理 | `/drg` | `/api/v1/drg` | P3 | ✅ 已完成 |

## 技术栈

- **前端**: Vue 3 + TypeScript + Element Plus + Vite + Pinia
- **HTTP**: Axios 封装 `@/api/request.ts`（baseURL 自动加 `/api` 前缀）
- **路由**: Vue Router（Layout 包裹模式）
- **样式**: SCSS，统一 page-container 样式

## 开发规范

每个模块需包含以下功能：
1. **列表页**: 搜索条件 + 分页表格 + 操作列（编辑/删除/发布等）
2. **新增/编辑**: 表单弹窗（el-dialog + el-form + 校验规则）
3. **后端 API 封装**: 在 `@/api/` 下创建对应模块的 API 文件
4. **类型定义**: 统一 TypeScript 接口（DTO/VO）

## 开发步骤

### Step 1: API 层
为每个模块创建 `src/api/{module}.ts`，定义接口函数和类型。

### Step 2: 页面组件
实现 `{Module}List.vue`，包含：
- 搜索区域（el-form inline）
- 操作栏（新增按钮 + 批量操作）
- 数据表格（el-table + 分页 el-pagination）
- 编辑弹窗（el-dialog + el-form）

### Step 3: 构建测试
`npm run build` → `docker cp` 部署 → 浏览器验证。

---

## 进度跟踪

- [x] 分析项目结构
- [x] 规则组管理 API + 页面 (25条规则组数据已入库)
- [x] 规则定义 API + 页面 (25条规则定义已入库)
- [x] 公式管理 API + 页面 (25条Aviator公式已入库)
- [x] 结算管理 API + 页面 (25条结算记录已入库)
- [x] 用药审核 API + 页面 (45条药品目录+25条配伍禁忌已入库)
- [x] 质量控制 API + 页面 (25条质控规则已入库)
- [x] DRG管理 API + 页面 (25条DRG定义已入库)
- [x] 构建部署并验证
- [x] 数据库初始化 - 所有页面25条真实医疗数据

---

## 完成情况汇总 (2026-05-05 最终版)

### 前端模块完成情况

| 模块 | 页面组件 | API封装 | 数据就绪 | 测试状态 |
|------|----------|---------|----------|----------|
| 规则组管理 | RuleGroup.vue | rule-group.ts | 25条 | ✅ 通过 |
| 规则定义 | RuleList.vue | rule-definition.ts | 25条 | ✅ 通过 |
| 公式管理 | FormulaList.vue | formula.ts | 25条 | ✅ 通过 |
| 规则流 | FlowList.vue + FlowEditor.vue | rule-flow.ts | 10条 | ✅ 通过 |
| 结算管理 | SettlementList.vue | settlement.ts | 25条 | ✅ 通过 |
| 用药审核 | DrugList.vue | drug.ts | 45条药品+25条配伍 | ✅ 通过 |
| 质量控制 | QualityList.vue | quality.ts | 25条 | ✅ 通过 |
| DRG管理 | DrgList.vue | drg.ts | 50条 | ✅ 通过 |

### 数据修复记录 (2026-05-05)

| 页面 | 问题 | 修复方式 | 状态 |
|------|------|---------|------|
| 所有页面 | 加载失败"系统繁忙" | 1. TenantContextFilter 默认租户 default→T001<br>2. 所有 Service `!=null` → `StringUtils.hasText()`<br>3. 添加 MyBatis-Plus 分页插件 | ✅ 已修复 |
| 所有页面 | 字段全为空 | 前端 API 响应解析 `res.data.list` → `res.list`（interceptor已解包） | ✅ 已修复 |
| 规则流 | API 404 | 1. 前端 API `/api/v2` → `/api/v1`<br>2. Gateway 路由添加 `/api/v1/flows/**`<br>3. Controller `IPage` → `PageResult.of()` | ✅ 已修复 |
| 规则流 | 无数据 | 初始化 10 条规则流数据（结算/用药/质控/DRG 场景） | ✅ 已修复 |
| 公式管理 | 公式名称/表达式为空 | 数据库补充 `formula_name` 数据；FormulaVO `@JsonProperty("expression")` 映射 | ✅ 已修复 |
| 结算管理 | 患者姓名为空 | 数据库添加 `patient_name` 列并填充；Entity/VO 添加字段 | ✅ 已修复 |
| 用药审核 | 通用名/单价为空 | DrugCatalog Entity `@JsonProperty` 映射：`category→genericName`、`limitPrice→unitPrice`、`reimbursementType→insuranceType` | ✅ 已修复 |
| DRG管理 | 分类/权重为空 | DrgDefinition Entity `@JsonProperty` 映射：`mdcCode→category`、`baseWeight→weight` | ✅ 已修复 |

### 数据库数据就绪

| 表名 | 记录数 | 说明 |
|------|--------|------|
| rule_group | 25条 | 医保/用药/质控/DRG/费用规则组 |
| rule_definition | 25条 | DRL规则定义 |
| aviator_formula | 25条 | 报销/剂量/DRG/质控计算公式 |
| rule_flow | 10条 | 结算/用药/质控/DRG 规则流定义 |
| settlement_result | 25条 | 职工医保/居民医保/大病保险结算记录（含 patient_name） |
| quality_definition | 25条 | 用药安全/诊断/费用/病历/院控质控规则 |
| drg_definition | 50条 | 神经系统/呼吸/循环/消化等DRG分组 |
| drug_catalog | 45条 | 抗生素(15)/心血管(10)/内分泌(7)/消化(5)/呼吸(4)/中成药(4) |
| drug_interaction | 25条 | 严重禁忌(10)/中等警告(10)/协同作用(5) |

### 修改文件清单

| 文件 | 修改内容 |
|------|---------|
| TenantContextFilter.java | 默认租户 `default` → `T001` |
| request.ts | 默认租户 `default` → `T001` |
| MybatisPlusConfig.java | 添加 `PaginationInnerInterceptor` 分页插件 |
| DrgController.java | `!=null` → `StringUtils.hasText()` |
| DrugController.java | `!=null` → `StringUtils.hasText()` |
| QualityController.java | `!=null` → `StringUtils.hasText()` |
| SettlementController.java | 添加 `settlementNo` 参数 |
| SettlementService.java | `!=null` → `StringUtils.hasText()` |
| RuleDefinitionService.java | `!=null` → `StringUtils.hasText()` |
| RuleFlowService.java | `!=null` → `StringUtils.hasText()`，使用 TenantContext |
| RuleGroupService.java | 使用 TenantContext 默认值 |
| FormulaService.java | `!=null` → `StringUtils.hasText()` |
| RuleFlowController.java | `/api/v2` → `/api/v1`，返回 `PageResult.of()` |
| RuleFlowController.java | 添加 `/api/v1/flows/**` Gateway 路由 |
| rule-flow.ts | API 路径 `/api/v2` → `/api/v1` |
| FormulaVO.java | `@JsonProperty("expression")` 映射 |
| SettlementResult.java | 添加 `patientName` 字段 |
| SettlementVO.java | 添加 `patientName` 字段 |
| DrugCatalog.java | `@JsonProperty` 映射 genericName/unitPrice/insuranceType |
| DrgDefinition.java | `@JsonProperty` 映射 category/weight |
| DrgList.vue | 字段 `category`→`mdcCode`，`weight`→`baseWeight` |
| DrugList.vue | 字段 `genericName`→`category`，`unitPrice`→`limitPrice`，`insuranceType`→`reimbursementType` |
| rule-group.ts | API 路径 `/api/v1/rule-groups` → `/api/v1/rule-groups/page` |

---

**备注**: 所有模块复用已有的 Layout 布局、request 封装、统一 Result 响应格式。