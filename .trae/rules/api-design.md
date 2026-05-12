---
alwaysApply: false
description: 
---
# API 设计规则 - HIS 动态规则中台

## 触发条件
- 设计 REST API
- 创建 Controller 文件
- 定义接口规范
- 配置网关路由
- 微服务间 Feign 接口设计

---

## 一、API 命名与结构规范

### 1.1 URL 命名约定

```
基础路径: /api/v1/{module}

示例:
/api/v1/rules              规则管理
/api/v1/formulas           公式管理
/api/v1/settlements        结算管理
/api/v1/skills             Skill 管理
/api/v1/tenants            租户管理
/api/v1/config             配置管理
/api/v1/drugs              合理用药
/api/v1/quality            质控指标
/api/v1/drg                DRG 分组
```

### 1.2 微服务 API 分配

| 微服务 | 端口 | API 路径 | 说明 |
|--------|------|---------|------|
| his-gateway | 8080 | `/api/v1/**` | 统一入口，路由到各服务 |
| his-rule-service | 9001 | `/api/v1/rules/**`<br>`/api/v1/formulas/**` | 规则管理、公式管理 |
| his-settlement-service | 9002 | `/api/v1/settlements/**`<br>`/api/v1/skills/**` | 结算管理、Skill 执行 |
| his-drug-service | 9003 | `/api/v1/drugs/**` | 合理用药检查 |
| his-quality-service | 9004 | `/api/v1/quality/**` | 质控指标管理 |
| his-drg-service | 9005 | `/api/v1/drg/**` | DRG 分组管理 |

### 1.3 Controller 编写模板

```java
@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
@Slf4j
public class RuleDefinitionController {

    private final RuleDefinitionService ruleService;

    @GetMapping
    public Result<PageResult<RuleDefinitionVO>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            RuleQueryDTO queryDTO) {
        return Result.success(ruleService.pageList(page, pageSize, queryDTO));
    }

    @GetMapping("/{id}")
    public Result<RuleDefinitionVO> getById(@PathVariable Long id) {
        return Result.success(ruleService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid RuleCreateDTO dto) {
        ruleService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid RuleUpdateDTO dto) {
        ruleService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ruleService.delete(id);
        return Result.success();
    }
}
```

---

## 二、RESTful 风格

| 方法 | 路径 | 说明 | 幂等性 |
|------|------|------|--------|
| GET | /api/v1/rules | 分页列表 | ✅ |
| GET | /api/v1/rules/{id} | 详情 | ✅ |
| POST | /api/v1/rules | 创建 | ❌ |
| PUT | /api/v1/rules/{id} | 全量更新 | ✅ |
| PATCH | /api/v1/rules/{id} | 部分更新 | ❌ |
| DELETE | /api/v1/rules/{id} | 删除 | ✅ |
| POST | /api/v1/rules/{id}/publish | 发布（状态变更） | ❌ |
| POST | /api/v1/rules/{id}/validate | 校验（操作） | ❌ |

---

## 三、网关路由配置

### 3.1 Spring Cloud Gateway 路由规则

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 规则管理路由
        - id: rule-service
          uri: lb://his-rule-service
          predicates:
            - Path=/api/v1/rules/**,/api/v1/formulas/**
          filters:
            - StripPrefix=0
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
            - name: AuthFilter
              args:
                excludePaths: /api/v1/rules/public/**

        # 结算管理路由
        - id: settlement-service
          uri: lb://his-settlement-service
          predicates:
            - Path=/api/v1/settlements/**,/api/v1/skills/**
          filters:
            - StripPrefix=0
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 50
                redis-rate-limiter.burstCapacity: 100

        # 合理用药路由
        - id: drug-service
          uri: lb://his-drug-service
          predicates:
            - Path=/api/v1/drugs/**
          filters:
            - StripPrefix=0

        # 质控路由
        - id: quality-service
          uri: lb://his-quality-service
          predicates:
            - Path=/api/v1/quality/**
          filters:
            - StripPrefix=0

        # DRG 路由
        - id: drg-service
          uri: lb://his-drg-service
          predicates:
            - Path=/api/v1/drg/**
          filters:
            - StripPrefix=0
```

### 3.2 网关全局过滤器

```java
@Component
public class GlobalLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        ServerHttpRequest request = exchange.getRequest().mutate()
            .header("X-Trace-Id", traceId)
            .header("X-Request-Time", String.valueOf(System.currentTimeMillis()))
            .build();
        
        log.info("网关请求: traceId={}, method={}, path={}", 
            traceId, request.getMethod(), request.getPath());
        
        return chain.filter(exchange.mutate().request(request).build())
            .doFinally(signalType -> {
                ServerHttpResponse response = exchange.getResponse();
                log.info("网关响应: traceId={}, status={}", 
                    traceId, response.getStatusCode());
            });
    }

    @Override
    public int getOrder() {
        return -1;  // 最高优先级
    }
}
```

### 3.3 跨域配置

```java
@Configuration
public class GatewayCorsConfig {

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("https://his.example.com"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/**", config);
        return new CorsWebFilter(source);
    }
}
```

---

## 四、统一响应格式

```java
// 成功响应
{
  "code": "0",
  "data": { ... },
  "message": "操作成功",
  "timestamp": 1714108800000
}

// 分页响应
{
  "code": "0",
  "data": {
    "list": [ ... ],
    "total": 100,
    "page": 1,
    "pageSize": 20
  },
  "message": "查询成功"
}

// 错误响应
{
  "code": "HIS-001",
  "data": null,
  "message": "患者身份信息缺失",
  "timestamp": 1714108800000
}

// Skill 执行结果
{
  "code": "0",
  "data": {
    "settlementId": "ST202604260001",
    "results": [
      { "level": "PASS", "source": "InsuranceLimitSkill", "message": null },
      { "level": "WARN", "source": "RationalDrugUseSkill", "message": "配伍禁忌警告" }
    ],
    "hasBlock": false,
    "amounts": { "totalFee": 10000, "reimburseAmount": 7500 }
  }
}
```

### 统一响应封装类

```java
@Data
@Builder
public class Result<T> {
    private String code;
    private T data;
    private String message;
    private Long timestamp;

    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
            .code("0")
            .data(data)
            .message("操作成功")
            .timestamp(System.currentTimeMillis())
            .build();
    }

    public static <T> Result<T> fail(String code, String message) {
        return Result.<T>builder()
            .code(code)
            .data(null)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
```

---

## 五、业务码定义

| 码范围 | 说明 | 示例 |
|--------|------|------|
| `"0"` | 成功 | — |
| `"HIS-001"` ~ `"HIS-099"` | 业务异常 | 患者信息缺失/规则不存在 |
| `"HIS-101"` ~ `"HIS-199"` | 公式异常 | 语法错误/参数不匹配/计算溢出 |
| `"HIS-201"` ~ `"HIS-299"` | 结算异常 | 结算中/审核拒绝/重复结算 |
| `"HIS-301"` ~ `"HIS-399"` | 权限异常 | 无租户权限/角色不足 |
| `"HIS-401"` ~ `"HIS-499"` | 安全异常 | Aviator 注入检测/DRL 安全拦截 |
| `"HIS-901"` ~ `"HIS-999"` | 系统异常 | 配置中心不可用/KIE 编译失败 |

详见 `rules/error-handling.md`

---

## 六、DTO 命名与设计规范

### 6.1 DTO 分类命名

| 类型 | 后缀 | 说明 |
|------|------|------|
| 创建请求 | `XxxCreateDTO` | 新建时必填字段 |
| 更新请求 | `XxxUpdateDTO` | 更新时可选字段 |
| 查询条件 | `XxxQueryDTO` | 列表筛选条件 |
| 响应视图 | `XxxVO` | 返回给前端的视图对象 |
| 内部传输 | `XxxDTO` | 服务间传递 |

### 6.2 校验注解使用

```java
@Data
public class RuleCreateDTO {

    @NotBlank(message = "规则Key不能为空")
    @Pattern(regexp = "^rule\\.[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$",
             message = "格式: rule.{module}.{name}")
    private String ruleKey;

    @NotBlank(message = "DRL内容不能为空")
    @Size(max = 50000, message = "DRL内容不超过50000字符")
    private String ruleText;

    @NotNull(message = "分类不能为空")
    private RuleCategory category;

    private String description;
}
```

---

## 七、微服务间 Feign 接口设计

### 7.1 Feign Client 定义

```java
@FeignClient(
    name = "his-rule-service",
    url = "${feign.client.rule-service.url:}",
    configuration = FeignConfig.class,
    fallbackFactory = RuleServiceFallbackFactory.class
)
public interface RuleServiceClient {

    @GetMapping("/api/v1/rules/{id}")
    Result<RuleVO> getRuleById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/rules/{id}/snapshot")
    Result<RuleSnapshot> getRuleSnapshot(@PathVariable("id") Long id);

    @PostMapping("/api/v1/rules/batch")
    Result<List<RuleVO>> batchGetRules(@RequestBody List<Long> ruleIds);
}
```

### 7.2 Feign 降级处理

```java
@Component
public class RuleServiceFallbackFactory implements FallbackFactory<RuleServiceClient> {

    @Override
    public RuleServiceClient create(Throwable cause) {
        log.error("Rule Service 调用失败，触发降级", cause);
        
        return new RuleServiceClient() {
            @Override
            public Result<RuleVO> getRuleById(Long id) {
                return Result.fail("HIS-901", "规则服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<RuleSnapshot> getRuleSnapshot(Long id) {
                return Result.fail("HIS-901", "规则服务暂时不可用");
            }

            @Override
            public Result<List<RuleVO>> batchGetRules(List<Long> ruleIds) {
                return Result.fail("HIS-901", "规则服务暂时不可用");
            }
        };
    }
}
```

### 7.3 Feign 配置

```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 3000
        readTimeout: 5000
        loggerLevel: BASIC
      his-rule-service:
        connectTimeout: 2000
        readTimeout: 3000
  httpclient:
    enabled: true
    max-connections: 200
    max-connections-per-route: 50
  compression:
    request:
      enabled: true
      mime-types: application/json
      min-request-size: 2048
    response:
      enabled: true
```

---

## 八、安全检查清单

- [ ] 所有接口有权限校验（`@PreAuthorize` 或自定义拦截器）
- [ ] 敏感参数不记录日志（身份证号/医保卡号/金额明细）
- [ ] 租户隔离：所有查询强制过滤 `tenant_id`
- [ ] 防止 SQL 注入（MyBatis 参数化 `#{}`）
- [ ] 防止 Aviator 表达式注入（白名单校验，见 `rules/security.md`）
- [ ] 接口限流（规则发布/结算执行等敏感接口）
- [ ] 金额字段返回使用字符串（前端避免精度丢失）
- [ ] 网关路由配置限流过滤器
- [ ] 跨域配置正确（仅允许前端域名）
- [ ] Feign 调用有降级处理
- [ ] 链路追踪 traceId 传递

---

最后更新: 2026-05-12 | v1.1 (HIS Drools+Aviator 规则引擎专用 - 微服务架构版)
