---
alwaysApply: true
description: 安全检查规范，所有操作必须遵循
---
# 安全检查规则 - HIS 动态规则中台

## 触发条件
- 编写涉及用户输入处理的代码
- 配置认证/授权
- 执行 shell 命令
- 访问敏感文件
- Docker 部署配置
- 依赖管理
- 规则/公式注入防护

---

## 一、Web 安全防护

### SQL 注入防护

| 规则 | 要求 | 示例 |
|------|------|------|
| ORM/MyBatis | **必须**使用参数化查询 | `@Param` 绑定 / `#{}` 占位符 |
| 禁止字符串拼接 | **禁止**字符串拼 SQL | `WHERE id = " + id` ❌ |
| LIKE 查询 | 使用参数化 + 转义特殊字符 | 见下方 |

```java
// ✅ 安全: MyBatis 参数化
@Select("SELECT * FROM settlement_record WHERE patient_id = #{patientId}")
List<SettlementRecord> findByPatientId(@Param("patientId") String patientId);

// ✅ 安全: LIKE 参数化
@Select("SELECT * FROM formula WHERE formula_text LIKE CONCAT('%', #{keyword}, '%')")
List<Formula> searchByKeyword(@Param("keyword") String keyword);

// ❌ 危险: 字符串拼接
String sql = "SELECT * FROM formula WHERE formula_text LIKE '%" + keyword + "%'";
```

### XSS 防护

| 层面 | 防护措施 |
|------|---------|
| REST API | JSON 响应天然安全（非 HTML 上下文） |
| 公式存储 | Aviator 表达式白名单校验（只允许数学运算符和预定义函数） |
| 日志输出 | 敏感信息脱敏（患者姓名/身份证号/医保卡号） |

### Aviator 表达式注入防护（本项目特有）

> **最高风险点**: 用户可通过配置中心写入恶意 Aviator 表达式

| 防护层级 | 措施 |
|---------|------|
| 白名单函数 | 只注册安全函数（math/string/collection），禁用 `sys.*` / `fn.*` 危险函数 |
| 表达式长度限制 | 单个公式 ≤ 512 字符 |
| 语法校验 | 保存前 `AviatorEvaluator.compile()` 预编译校验 |
| 沙箱执行 | 设置 `SecurityManager` 或自定义 `AviatorFunctionLoader` |
| 操作审计 | 所有公式变更记录操作人 + 时间 + 变更前后对比 |

```java
// ✅ 安全: 公式保存前的校验
public void saveFormula(FormulaEntity formula) {
    // 1. 长度校验
    if (formula.getText().length() > 512) {
        throw new BusinessException("公式长度超限");
    }
    
    // 2. 语法校验（预编译）
    try {
        AviatorEvaluator.compile(formula.getText(), true);
    } catch (Exception e) {
        throw new BusinessException("公式语法错误: " + e.getMessage());
    }
    
    // 3. 危险函数检测
    if (containsDangerousFunctions(formula.getText())) {
        throw new SecurityException("公式包含不允许的函数");
    }
    
    // 4. 保存 + 记录审计日志
    formulaRepository.save(formula);
    auditLogService.recordFormulaChange(formula);
}
```

### Drools 规则注入防护

| 防护层级 | 措施 |
|---------|------|
| 规则来源控制 | 只允许从数据库/Nacos 加载，禁止前端直接提交 DRL |
| 规则审核 | 规则变更需审批流程后才生效 |
| 资源限制 | 设置最大规则数 / 最大 Fact 数 / 最大执行时间 |
| 执行沙箱 | Drools 8 Rule Unit 隔离执行环境 |

---

## 二、认证与授权

| 检查项 | 要求 |
|--------|------|
| 密码存储 | **必须**使用 BCrypt/Argon2，禁止明文/MD5 |
| JWT Token | 使用 RS256/HS256 签名，合理过期时间 |
| 接口权限 | 每个 Controller 方法必须有权限注解 (`@PreAuthorize`) |
| 多租户隔离 | 每个查询必须带 tenantId 过滤 |
| 操作审计 | 关键操作（规则变更/公式修改/结算执行）必须记录审计日志 |

```java
// ✅ 正确: 接口权限控制
@RestController
@RequestMapping("/api/rules")
public class RuleController {

    @PreAuthorize("hasRole('RULE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> createRule(@RequestBody RuleDTO dto) {
        // 创建规则...
    }

    @PreAuthorize("hasPermission(#tenantId, 'RULE_READ')")
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<?> listRules(@PathVariable String tenantId) {
        // 按租户查询...
    }
}
```

---

## 三、敏感数据保护

| 数据类型 | 保护措施 |
|---------|---------|
| 患者姓名 | 日志中脱敏为 `张*` / `***` |
| 身份证号 | 存储 AES 加密，日志显示 `**************1234` |
| 医保卡号 | 日志脱敏，传输 HTTPS |
| 结算金额 | 前端展示脱敏规则按角色区分 |
| 配置中心密码 | Nacos/Apollo 加密存储（如使用 KMS） |

```java
// ✅ 正确: 日志脱敏工具
public class DesensitizeUtil {
    public static String maskName(String name) {
        if (name == null || name.length() <= 1) return "***";
        return name.charAt(0 + "**");
    }
    
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 14) return "************";
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }
}
```

---

## 五、网关安全

### 5.1 JWT 鉴权

```yaml
# 网关统一鉴权配置
spring:
  cloud:
    gateway:
      default-filters:
        - name: AuthGlobalFilter
          args:
            excludePaths:
              - /api/v1/health
              - /api/v1/rules/public/**
              - /actuator/health
            tokenHeader: Authorization
            tokenPrefix: Bearer
```

| 规则 | 要求 |
|------|------|
| 统一鉴权 | 网关层统一处理，后端服务不重复校验 |
| 白名单 | 健康检查、公开规则查询等接口免鉴权 |
| Token 传递 | 网关解析后传递 userId/tenantId/roles 到下游服务 |
| 过期处理 | Token 过期返回 401，由前端刷新 |

### 5.2 限流熔断（Sentinel）

```java
// 网关限流配置
@Configuration
public class SentinelGatewayConfig {

    @PostConstruct
    public void initGatewayRules() {
        // QPS 限流
        GatewayFlowRule rule = new GatewayFlowRule("rule-service")
            .setCount(100)
            .setIntervalSec(1);
        
        // 并发线程数限制
        GatewayFlowRule concurrentRule = new GatewayFlowRule("settlement-service")
            .setCount(50)
            .setIntervalSec(1)
            .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        
        GatewayRuleManager.loadRules(Set.of(rule, concurrentRule));
    }
}
```

| 场景 | 限流策略 | 降级处理 |
|------|---------|---------|
| 规则查询 | 100 QPS | 返回缓存数据 |
| 结算执行 | 50 QPS | 排队等待 |
| 公式发布 | 10 QPS | 拒绝并提示稍后重试 |
| 健康检查 | 不限流 | — |

### 5.3 CORS 配置

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://his.example.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsWebFilter(source);
    }
}
```

---

## 六、微服务间安全

### 6.1 Feign 调用鉴权

```java
@Configuration
public class FeignConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("Authorization");
            if (token != null) {
                template.header("Authorization", token);
            }
        }
        // 添加服务间调用标识
        template.header("X-Service-Name", "his-settlement-service");
    }
}
```

### 6.2 内部接口保护

| 规则 | 要求 |
|------|------|
| 内部接口标识 | 使用 `@InternalApi` 注解标记仅服务间调用的接口 |
| 网关不可达 | 内部接口不配置网关路由，仅内网可访问 |
| 服务间 Token | 使用独立的服务间调用 Token，与用户 Token 分离 |
| IP 白名单 | 生产环境限制仅允许内网 IP 访问内部接口 |

```java
// 内部接口示例：仅服务间调用
@RestController
@RequestMapping("/internal")
@InternalApi
public class InternalRuleController {

    @GetMapping("/batch")
    public Result<List<RuleVO>> batchGet(@RequestBody List<Long> ids) {
        // 批量获取规则（供结算服务调用）
        return Result.success(ruleService.batchGet(ids));
    }
}
```

### 6.3 服务发现安全

| 规则 | 要求 |
|------|------|
| Nacos 认证 | 生产环境必须配置 Nacos 用户名密码 |
| 命名空间隔离 | 不同环境使用不同命名空间（dev/test/prod） |
| 服务注册鉴权 | 仅允许白名单服务注册 |
| 配置加密 | 敏感配置使用 Jasypt 加密 |

---

## 七、Shell 危险命令黑名单

详见 `hooks/pre-execute-shell.sh`（通用拦截脚本，可直接复用）。

本项目额外关注：
| 命令 | 风险 | 说明 |
|------|------|------|
| `DROP DATABASE/TABLE` | 数据破坏 | 规则库/公式库不可删除 |
| `TRUNCATE` | 数据清空 | 审计日志不可清除 |
| `mysql -u root -p` | 明文密码 | 禁止命令行传密码 |
| `curl` + 内网地址 | SSRF | 规则引擎不应主动发起外部请求 |

---

最后更新: 2026-05-12 | v1.1 (HIS 规则引擎安全规范 - 微服务架构版)
