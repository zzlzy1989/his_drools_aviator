# rules 模块概览

## 职责
Drools 规则定义、版本管理和热更新，是规则引擎的核心模块。

## 核心模型
- **RuleDefinition**: 规则定义主表（rule_key/rule_text/category/version/status/tenant_id）
- **RuleInstance**: 规则实例（绑定业务场景的规则运行时配置）
- **RuleSnapshot**: 规则快照（每次发布的完整备份，用于回滚）
- **RuleGroup**: 规则分组（按业务域组织：医保结算/合理用药/质控）

## API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST | `/api/v1/rules` | 列表(分页)/创建规则 |
| GET/PUT/DELETE | `/api/v1/rules/{id}` | CRUD |
| POST | `/api/v1/rules/{id}/validate` | DRL 语法校验 |
| POST | `/api/v1/rules/{id}/publish` | 发布生效 |
| POST | `/api/v1/rules/{id}/disable` | 停用 |
| POST | `/api/v1/rules/{id}/rollback/{version}` | 回滚到指定版本 |
| GET | `/api/v1/rules/{id}/versions` | 版本历史列表 |
| GET/POST | `/api/v1/rule-groups` | 分组管理 |

## 关键规则
- 规则名称在 Package 内不可重复（BR-R01）
- 删除级联停用关联实例（BR-R02）
- 租户隔离：查询必须过滤 tenant_id（BR-R03）
- 状态机: draft → validated → active → inactive → archived（SM-01）
- DRL 禁止 Java 代码块（BR-S02）

## 关联模块
← tenants (tenant_id FK)
↔ formulas (规则可引用公式)
→ settlements (结算执行时加载)
→ config (热更新来源)

## 核心服务类
- `DrlRuleService`: 规则 CRUD + 语法校验 + 发布管理
- `KieContainerManager`: KIE Base 编译与 Session 管理
- `RuleHotUpdateListener`: Nacos/Apollo 变更监听与热更新

## 关键配置
```yaml
drools:
  kie-base: "his-rule-base"
  hot-update:
    enabled: true
    provider: nacos  # 或 apollo
    data-id: "his-rules-drl"
    group: "RULE_ENGINE"
```
