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

| 模块 | 前缀 | 范围 |
|------|------|------|
| 通用错误 | HIS-0 | 001~099 |
| 结算模块 | HIS-1 | 100~199 |
| 用药审核 | HIS-2 | 200~299 |
| 规则引擎 | HIS-3 | 300~399 |
| 公式引擎 | HIS-4 | 400~499 |
| Agent/Skill | HIS-5 | 500~599 |
| 配置中心 | HIS-6 | 600~699 |
| 缓存服务 | HIS-7 | 700~799 |

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
    SYSTEM_ERROR("HIS-099", "系统内部错误"),

    // ===== 结算模块 (1xx) =====
    PATIENT_ID_MISSING("HIS-101", "患者ID缺失"),
    PATIENT_TYPE_INVALID("HIS-102", "无效的患者类型: {}"),
    SETTLEMENT_IN_PROGRESS("HIS-103", "该患者存在进行中的结算"),
    FEE_NEGATIVE("HIS-104", "费用不能为负数"),
    DEDUCTIBLE_NOT_FOUND("HIS-105", "未找到对应患者类型的起付线配置"),

    // ===== 用药审核 (2xx) =====
    PRESCRIPTION_EMPTY("HIS-201", "处方为空"),
    DRUG_NOT_FOUND("HIS-202", "药品未找到: {}"),
    INCOMPATIBILITY_DETECTED("HIS-203", "检测到配伍禁忌: {}"),
    DOSAGE_EXCEEDED("HIS-204", "用药剂量超限: 当前{}, 上限{}"),

    // ===== 规则引擎 (3xx) =====
    RULE_NOT_FOUND("HIS-301", "规则未找到: {}"),
    RULE_PARSE_ERROR("HIS-302", "规则解析错误: {}"),
    RULE_EXECUTION_TIMEOUT("HIS-303", "规则执行超时({}ms)"),
    FACT_INVALID("HIS-304", "Fact 对象无效: 缺少必要字段 {}"),

    // ===== 公式引擎 (4xx) =====
    FORMULA_NOT_FOUND("HIS-401", "公式未找到: {}"),
    FORMULA_SYNTAX_ERROR("HIS-402", "公式语法错误: {}"),
    FORMULA_EXECUTION_ERROR("HIS-403", "公式执行错误: {}"),
    FORMULA_LENGTH_EXCEEDED("HIS-404", "公式长度超限(最大512字符)"),

    // ===== Agent/Skill (5xx) =====
    SKILL_NOT_REGISTERED("HIS-501", "未注册的Skill: {}"),
    PIPELINE_BLOCKED("HIS-502", "Pipeline被阻断: 由{}触发"),
    SKILL_EXECUTION_FAILED("HIS-503", "Skill执行失败: {}"),

    // ===== 配置中心 (6xx) =====
    NACOS_CONNECT_FAILED("HIS-601", "Nacos连接失败: {}"),
    CONFIG_NOT_FOUND("HIS-602", "配置项未找到: {}"),
    CONFIG_REFRESH_FAILED("HIS-603", "配置刷新失败: {}"),

    // ===== 缓存服务 (7xx) =====
    CACHE_OPERATION_FAILED("HIS-701", "缓存操作失败");

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

## 四、异常处理原则

| 原则 | 说明 |
|------|------|
| **规则引擎异常不外泄** | Drools/Aviator 异常捕获后转为 WARN 结果，不中断整体流程 |
| **公式错误降级** | 公式解析/执行失败 → 返回默认值(如 ZERO)，记录详细日志 |
| **敏感信息隐藏** | 生产环境不返回堆栈、SQL、完整异常信息给前端 |
| **审计日志** | 所有 ERROR 级别异常必须写入审计日志 |
| **多语言** | message 支持国际化（通过错误码映射多语言文本） |

---

最后更新: 2026-04-26 | v1.0 (HIS 规则引擎错误处理规范)
