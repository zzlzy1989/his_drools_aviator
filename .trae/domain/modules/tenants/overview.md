---
alwaysApply: true
---
# tenants 模块概览

## 职责
多租户管理，实现数据隔离、资源配额和独立配置。

## 核心模型
- **Tenant**: 租户主表（tenant_code/name/status/package_type/expire_date/quota_config）
- **TenantMember**: 租户成员（user_id/tenant_id/role/joined_at）
- **TenantQuota**: 租户资源配额（max_rules/max_formulas/max_settlements/day）
- **TenantConfig**: 租户个性化配置（KV 存储，覆盖默认配置）

## API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST (Admin) | `/api/admin/tenants` | 租户列表/创建 |
| GET/PUT (Admin) | `/api/admin/tenants/{id}` | 租户详情/更新 |
| POST (Admin) | `/api/admin/tenants/{id}/suspend` | 暂停租户 |
| POST (Admin) | `/api/admin/tenants/{id}/activate` | 激活租户 |
| GET/POST | `/api/v1/tenants/members` | 成员列表/邀请 |
| DELETE | `/api/v1/tenants/members/{uid}` | 移除成员 |
| GET | `/api/v1/tenants/config` | 当前租户配置 |
| PUT | `/api/v1/tenants/config` | 更新租户配置 |

## 关键规则
- 所有数据查询必须过滤 tenant_id（BR-R03）
- 缓存 key 必须包含 tenant_id（BR-H03）
- Pending → Active 时初始化资源（SM-06）
- Suspended 下只读访问（SM-06）
- 超级管理员可跨租户查看（只读）（EC-08）

## 隔离策略
| 维度 | 隔离方式 |
|------|---------|
| 数据库 | tenant_id 字段 + 强制过滤 |
| 缓存 | key 前缀 `{tenantId}:` |
| KIE Session | 按 RuleUnit 隔离 |
| 文件存储 | 目录隔离 `/files/{tenantId}/` |
| 配置 | Nacos namespace 或配置前缀 |

## 套餐类型
| 套餐 | 规则数上限 | 公式数上限 | 日结算量 | 价格 |
|------|----------|----------|---------|------|
| BASIC | 100 | 50 | 1,000 | ¥0 |
| PRO | 1,000 | 500 | 10,000 | ¥XXX |
| ENTERPRISE | 10,000 | 5,000 | 100,000 | ¥XXX |

## 关联模块
→ rules (规则归属)
→ formulas (公式归属)
→ settlements (结算归属)
→ skills (Skill 可按租户定制)

## 核心服务类
- `TenantContext`: 租户上下文（ThreadLocal 存储）
- `TenantFilter`: HTTP 请求过滤器（提取并设置 tenant_id）
- `QuotaService`: 配额校验与用量统计
- `TenantInitService`: 租户初始化（创建 schema/命名空间）
