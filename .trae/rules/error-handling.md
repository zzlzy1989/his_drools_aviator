---
alwaysApply: true
description: 错误处理规范，所有异常必须遵循此规范
---
# 错误处理规范 - HIS 动态规则中台

## 触发条件
- 定义异常体系
- 编写错误码
- 异常处理逻辑
- 错误响应格式

---

## 一、错误码体系

### 1.1 错误码格式

```
HIS-{模块编号}{序列号}
```

| 模块 | 前缀 | 范围 | 所属服务 |
|------|------|------|---------|
| 通用错误 | HIS-0 | 001~099 | 公共模块 |
| 规则管理 | HIS-1 | 100~199 | his-rule-service |
| 结算模块 | HIS-2 | 200~299 | his-settlement-service |
| 用药审核 | HIS-3 | 300~399 | his-drug-service |
| 质控模块 | HIS-4 | 400~499 | his-quality-service |
| DRG 模块 | HIS-5 | 500~599 | his-drg-service |
| 公式引擎 | HIS-6 | 600~699 | his-rule-service |
| 网关错误 | HIS-7 | 700~799 | his-gateway |
| 配置中心 | HIS-8 | 800~899 | 公共模块 |
| 缓存服务 | HIS-9 | 900~999 | 公共模块 |

### 1.2 核心错误码清单

```java
public enum ErrorCode {
    // ===== 通用 (0xx) =====
    SUCCESS("0", "操作成功"),
    PARAM_MISSING("HIS-001", "必填参数缺失: {}"),
    PARAM_INVALID("HIS-002", "参数无效: {}"),
    UNAUTHORIZED("HIS-003", "未授权访问"),
    FORBIDDEN("HIS-004", "无权限操作"),
    RESOURCE_NOT_FOUND("HIS-005", "资源不存在: {}"),
    TENANT_NOT_FOUND("HIS-006", "租户不存在: {}"),
    SYSTEM_ERROR("HIS-099", "系统内部错误"),

    // ===== 规则管理 (1xx) =====
    RULE_NOT_FOUND("HIS-101", "规则未找到: {}"),
    RULE_PARSE_ERROR("HIS-102", "规则解析错误: {}"),
    RULE_PUBLISH_FAILED("HIS-103", "规则发布失败: {}"),
    RULE_GROUP_NOT_FOUND("HIS-104", "规则组未找到: {}"),
    RULE_VERSION_CONFLICT("HIS-105", "规则版本冲突"),

    // ===== 结算模块 (2xx) =====
    PATIENT_ID_MISSING("HIS-201", "患者ID缺失"),
    PATIENT_TYPE_INVALID("HIS-202", "无效的患者类型: {}"),
    SETTLEMENT_IN_PROGRESS("HIS-203", "该患者存在进行中的结算"),
    FEE_NEGATIVE("HIS-204", "费用不能为负数"),
    DEDUCTIBLE_NOT_FOUND("HIS-205", "未找到对应患者类型的起付线配置"),
    SETTLEMENT_BLOCKED("HIS-206", "结算被阻断: {}"),
    SETTLEMENT_DUPLICATE("HIS-207", "重复结算请求"),

    // ===== 用药审核 (3xx) =====
    PRESCRIPTION_EMPTY("HIS-301", "处方为空"),
    DRUG_NOT_FOUND("HIS-302", "药品未找到: {}"),
    INCOMPATIBILITY_DETECTED("HIS-303", "检测到配伍禁忌: {}"),
    DOSAGE_EXCEEDED("HIS-304", "用药剂量超限: 当前{}, 上限{}"),
    CONTRAINDICATION_DETECTED("HIS-305", "检测到禁忌: {}"),

    // ===== 质控模块 (4xx) =====
    INDICATOR_NOT_FOUND("HIS-401", "质控指标未找到: {}"),
    INDICATOR_CALC_FAILED("HIS-402", "指标计算失败: {}"),
    QUALITY_CHECK_FAILED("HIS-403", "质控校验未通过: {}"),

    // ===== DRG 模块 (5xx) =====
    DRG_NOT_FOUND("HIS-501", "DRG分组未找到: {}"),
    DRG_GROUP_FAILED("HIS-502", "DRG分组失败: {}"),
    DRG_AMBIGUOUS("HIS-503", "DRG分组歧义: {}"),
    ICD_CODE_INVALID("HIS-504", "ICD编码无效: {}"),

    // ===== 公式引擎 (6xx) =====
    FORMULA_NOT_FOUND("HIS-601", "公式未找到: {}"),
    FORMULA_SYNTAX_ERROR("HIS-602", "公式语法错误: {}"),
    FORMULA_EXECUTION_ERROR("HIS-603", "公式执行错误: {}"),
    FORMULA_LENGTH_EXCEEDED("HIS-604", "公式长度超限(最大512字符)"),
    FORMULA_INJECTION_DETECTED("HIS-605", "检测到公式注入风险"),

    // ===== 网关错误 (7xx) =====
    GATEWAY_ROUTE_NOT_FOUND("HIS-701", "网关路由未找到: {}"),
    GATEWAY_RATE_LIMIT("HIS-702", "请求过于频繁，请稍后重试"),
    GATEWAY_TIMEOUT("HIS-703", "网关超时: {}服务响应超时"),
    GATEWAY_CIRCUIT_BREAK("HIS-704", "服务熔断: {}"),

    // ===== 配置中心 (8xx) =====
    NACOS_CONNECT_FAILED("HIS-801", "Nacos连接失败: {}"),
    CONFIG_NOT_FOUND("HIS-802", "配置项未找到: {}"),
    CONFIG_REFRESH_FAILED("HIS-803", "配置刷新失败: {}"),

    // ===== 缓存服务 (9xx) =====
    CACHE_OPERATION_FAILED("HIS-901", "缓存操作失败"),
    CACHE_EXPIRED("HIS-902", "缓存已过期");

    private final String code;
    private final String message;
}
```

---

## 二、异常体系

### 2.1 异常层次

```
Exception (JDK)
 └── HisException (项目基类, RuntimeException)
      ├── BusinessException    (业务异常, 给用户看的)
      │    ├── RuleEngineException   (规则引擎异常)
      │    ├── FormulaException      (公式引擎异常)
      │    ├── SettlementException   (结算业务异常)
      │    └── DrugCheckException    (用药审核异常)
      ├── ConfigurationException (配置异常)
      └── SystemException        (系统异常, 不暴露给用户)
```

### 2.2 异常基类

```java
@Getter
public class HisException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object[] args;

    public HisException(ErrorCode errorCode, Object... args) {
        super(MessageFormat.format(errorCode.getMessage(), args));
        this.errorCode = errorCode;
        this.args = args;
    }

    public HisException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(MessageFormat.format(errorCode.getMessage(), args), cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    public String getCode() { return errorCode.getCode(); }
}
```

### 2.3 全局异常处理器

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(HisException.class)
    public ResponseEntity<Result<Void>> handleHisException(HisException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.ok(Result.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(FormulaParseException.class)
    public ResponseEntity<Result<Void>> handleFormulaParse(FormulaParseException e) {
        log.error("公式解析异常", e);
        return ResponseEntity.ok(
            Result.fail(ErrorCode.FORMULA_SYNTAX_ERROR.getCode(), e.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return ResponseEntity.ok(Result.fail(ErrorCode.PARAM_INVALID.getCode(), msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpected(Exception e) {
        log.error("未预期异常", e);
        return ResponseEntity.status(500).body(
            Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), "系统繁忙，请稍后重试")
        );
    }
}
```

---

## 三、错误响应格式

### 统一响应包装

```java
@Data
@AllArgsConstructor
public class Result<T> {
    private String code;       // 错误码, "0" 表示成功
    private T data;            // 业务数据
    private String message;    // 提示信息

    public static <T> Result<T> ok(T data) {
        return new Result<>("0", data, "操作成功");
    }

    public static <T> Result<T> fail(String code, String message) {
        return new Result<>(code, null, message);
    }
}
```

### 典型错误响应示例

```json
// 成功
{"code": "0", "data": {"finalAmount": 7650.00}, "message": "操作成功"}

// 业务异常
{"code": "HIS-101", "data": null, "message": "患者ID缺失"}

// 公式语法错误
{"code": "HIS-402", "data": null, "message": "公式语法错误: unexpected token at position 15"}

// Pipeline 阻断
{
  "code": "HIS-502",
  "data": {"blockSource": "RationalDrugUseSkill", "blockMessage": "配伍禁忌: A药+B药"},
  "message": "Pipeline被阻断"
}

// 系统内部错误（不暴露细节）
{"code": "HIS-099", "data": null, "message": "系统繁忙，请稍后重试"}
```

---

## 五、微服务错误传播

### 5.1 Feign 错误解码器

```java
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            Result<?> result = JSON.parseObject(body, Result.class);
            
            if (result != null && !"0".equals(result.getCode())) {
                // 将远程错误转换为本地异常
                return new HisException(
                    ErrorCode.valueOf(result.getCode()),
                    result.getMessage()
                );
            }
        } catch (IOException e) {
            log.error("Feign 响应解析失败", e);
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
```

### 5.2 网关错误响应统一格式

```java
@Component
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        Result<Void> errorResult;
        if (ex instanceof ResponseStatusException rse) {
            response.setStatusCode(rse.getStatusCode());
            errorResult = Result.fail(
                mapHttpStatusToCode(rse.getStatusCode()),
                rse.getReason()
            );
        } else if (ex instanceof HisException he) {
            errorResult = Result.fail(he.getCode(), he.getMessage());
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            errorResult = Result.fail(
                ErrorCode.SYSTEM_ERROR.getCode(),
                "系统繁忙，请稍后重试"
            );
        }
        
        // 写入统一格式错误响应
        byte[] bytes = JSON.toJSONString(errorResult).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
```

### 5.3 链路追踪 traceId 传递

| 层级 | 实现方式 |
|------|---------|
| 网关层 | 生成 traceId 放入请求头 `X-Trace-Id` |
| Feign 调用 | 通过 `RequestInterceptor` 自动传递 traceId |
| 日志输出 | 使用 MDC 存储 traceId，日志格式包含 `%X{traceId}` |
| 错误响应 | 错误响应中包含 traceId 便于问题排查 |

```java
// MDC 过滤器
@Component
public class TraceIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String traceId = req.getHeader("X-Trace-Id");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put("traceId", traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
        }
    }
}
```

---

## 六、异常处理原则

| 原则 | 说明 |
|------|------|
| **规则引擎异常不外泄** | Drools/Aviator 异常捕获后转为 WARN 结果，不中断整体流程 |
| **公式错误降级** | 公式解析/执行失败 → 返回默认值(如 ZERO)，记录详细日志 |
| **敏感信息隐藏** | 生产环境不返回堆栈、SQL、完整异常信息给前端 |
| **审计日志** | 所有 ERROR 级别异常必须写入审计日志 |
| **多语言** | message 支持国际化（通过错误码映射多语言文本） |
| **微服务错误透传** | Feign 调用错误解码为本地异常，保持错误码一致性 |
| **网关统一错误格式** | 所有错误通过网关返回统一 Result\<T\> 格式 |

---

最后更新: 2026-05-12 | v1.1 (HIS 规则引擎错误处理规范 - 微服务架构版)
