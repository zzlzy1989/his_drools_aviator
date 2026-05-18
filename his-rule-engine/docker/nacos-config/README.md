# Nacos 共享配置

## 目录说明

| 文件 | 说明 |
|------|------|
| `shared-common.yml` | 所有服务共享的基础配置 |
| `gateway.yml` | 网关服务专用配置 |

## 使用方式

### 1. 导入共享配置

在各服务的 `application.yml` 中添加：

```yaml
spring:
  config:
    import: optional:nacos:shared-common.yml
```

### 2. Nacos 控制台导入

登录 Nacos 控制台 (http://192.168.1.105:8848/nacos)：

1. 进入 **配置管理** → **配置列表**
2. 选择命名空间（如 `his_rule_engine`）
3. 点击 **导入配置**，选择 yaml 文件
4. 配置格式选择 **YAML**

### 3. 共享配置内容

#### shared-common.yml

- JWT 认证配置（secret、expiration）
- CORS 跨域配置
- Redis 连接配置
- HikariCP 连接池配置
- 日志级别配置

#### gateway.yml

- 网关端口 9000
- Sentinel 限流配置
- Actuator 监控端点

## 注意事项

1. 各服务自己的配置会覆盖共享配置
2.敏感配置（如 JWT secret）建议在 Nacos 中加密存储
3. 环境特定配置通过 `spring.profiles.active` 区分

---

最后更新: 2026-05-18