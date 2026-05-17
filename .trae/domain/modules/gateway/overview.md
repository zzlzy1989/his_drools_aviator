---
alwaysApply: true
---
# gateway 模块概览 - HIS 微服务网关

> 最后更新: 2026-05-10 | v1.0

## 职责
Spring Cloud Gateway API 网关，统一入口、认证、限流、路由转发。

## 核心功能
- JWT Token 认证校验
- 租户上下文提取与注入
- 路由转发到各微服务
- Sentinel 流量控制与熔断
- 跨域 (CORS) 配置
- 请求日志记录

## 路由配置
| 路径前缀 | 目标服务 | 说明 |
|---------|---------|------|
| `/api/v1/rules/*` | his-rule-service | 规则管理 |
| `/api/v1/rule-flows/*` | his-rule-service | 流程编排 |
| `/api/v1/rule-groups/*` | his-rule-service | 规则分组 |
| `/api/v1/formulas/*` | his-formula-service | 公式管理 |
| `/api/v1/settlements/*` | his-settlement-service | 结算服务 |
| `/api/v1/drugs/*` | his-drug-service | 用药审核 |
| `/api/v1/drg/*` | his-drg-service | DRG 分组 |
| `/api/v1/quality/*` | his-quality-service | 质控管理 |

## 关键过滤器
| 过滤器 | 顺序 | 职责 |
|--------|------|------|
| `JwtAuthenticationFilter` | 1 | JWT 校验，验证 Token 合法性 |
| `TenantContextFilter` | 2 | 从 Header 提取 tenant_id 注入上下文 |
| `RequestLoggingFilter` | 3 | 请求日志记录 |

## 关键规则
- 所有 API 请求必须经过 JWT 认证（BR-G01）
- 网关层提取租户 ID 并注入请求上下文（BR-G02）
- 敏感接口必须限流（Sentinel）

## 关联模块
→ his-rule-service (路由转发)
→ his-formula-service (路由转发)
→ his-settlement-service (路由转发)
→ his-drug-service (路由转发)
→ his-drg-service (路由转发)
→ his-quality-service (路由转发)

## 核心配置类
- `GatewayRoutesConfig`: 路由规则定义
- `JwtAuthenticationFilter`: JWT 认证过滤器
- `TenantContextFilter`: 租户上下文过滤器
- `SentinelConfig`: Sentinel 限流配置
- `CorsConfig`: 跨域配置
- `GlobalErrorAttributes`: 全局错误处理

## 文件位置
- 后端: `his-gateway/src/main/java/com/his/gateway/`
- 启动类: `GatewayApplication.java`
