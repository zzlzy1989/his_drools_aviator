# rules 模块概览 - HIS Drools 规则引擎核心

> 最后更新: 2026-05-10 | v1.1

## 职责
Drools 规则定义、版本管理、热更新和可视化流程编排（RuleFlow），是规则引擎的核心模块。

## 核心模型
- **RuleDefinition**: 规则定义主表（rule_key/rule_text/category/version/status/tenant_id）
- **RuleGroup**: 规则分组（按业务域组织：医保结算/合理用药/质控）
- **RuleFlow**: 规则可视化编排流程（nodes/edges JSON 存储）
- **RuleFlowHistory**: 流程版本历史（支持回滚）

## API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST | `/api/v1/rules` | 列表(分页)/创建规则 |
| GET/PUT/DELETE | `/api/v1/rules/{id}` | CRUD |
| POST | `/api/v1/rules/{id}/validate` | DRL 语法校验 |
| POST | `/api/v1/rules/{id}/publish` | 发布生效 |
| POST | `/api/v1/rules/{id}/disable` | 停用 |
| GET/POST | `/api/v1/rule-groups` | 分组管理 |
| GET/POST | `/api/v1/rule-flows` | 流程列表/创建 |
| GET/PUT/DELETE | `/api/v1/rule-flows/{id}` | 流程 CRUD |
| POST | `/api/v1/rule-flows/{id}/publish` | 流程发布 |
| GET | `/api/v1/rule-flows/{id}/versions` | 流程版本历史 |
| POST | `/api/v1/rule-flows/{id}/rollback` | 流程回滚 |

## 关键规则
- 规则名称在 Package 内不可重复（BR-R01）
- 租户隔离：查询必须过滤 tenant_id（BR-R03）
- 状态机: draft → validated → active → inactive → archived（SM-01）
- DRL 禁止 Java 代码块（BR-S02）
- RuleFlow 必须包含 start 和 end 节点（BR-FW01）
- RuleFlow 流转条件必须为有效 Aviator 布尔表达式（BR-FW02）

## 关联模块
← tenants (tenant_id FK)
↔ formulas (规则可引用公式)
→ settlements (结算执行时加载)
→ config (热更新来源)

## 核心服务类
- `RuleDefinitionService`: 规则 CRUD + DRL 语法校验 + 发布管理
- `RuleGroupService`: 规则分组管理
- `RuleFlowService`: 流程编排 + 版本管理 + 回滚
- `RuleFlowEngine`: 后端解析 RuleFlow 并驱动执行
- `DrlValidator`: DRL 规则文本校验器

## 关键配置
```yaml
drools:
  kie-base: "his-rule-base"
  hot-update:
    enabled: true
    provider: nacos
    data-id: "his-rules-drl"
    group: "RULE_ENGINE"
```

## 文件位置
- 后端: `his-rule-service/src/main/java/com/his/rule/`
- 实体: `entity/RuleDefinition`, `entity/RuleGroup`, `entity/RuleFlow`, `entity/RuleFlowHistory`
- 控制器: `controller/RuleDefinitionController`, `controller/RuleGroupController`, `controller/RuleFlowController`
