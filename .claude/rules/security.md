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

## 四、依赖安全扫描

| 规则 | 要求 |
|------|------|
| 定期扫描 | 使用 OWASP Dependency-Check 或 Snyk |
| 已知漏洞 | 升级到修复版本，无法升级则排除该依赖功能 |
| 许可证合规 | 检查依赖的开源协议兼容性 |
| Maven 命令 | `mvn org.owasp:dependency-check-maven:check` |

### 本项目关键依赖安全关注点
| 依赖 | 关注点 |
|------|--------|
| Drools | 规则执行沙箱逃逸风险 → 限制规则来源 + 资源配额 |
| Aviator | 表达式注入风险 → 白名单函数 + 语法校验 + 长度限制 |
| Nacos Client | 默认空密码风险 → 生产环境必须配置认证 |
| Jackson | 反序列化风险 → 配置 `DefaultTyping.NON_FINAL` 禁用 |
| Caffeine | 无安全风险（纯内存缓存） |

---

## 五、Shell 危险命令黑名单

详见 `hooks/pre-execute-shell.sh`（通用拦截脚本，可直接复用）。

本项目额外关注：
| 命令 | 风险 | 说明 |
|------|------|------|
| `DROP DATABASE/TABLE` | 数据破坏 | 规则库/公式库不可删除 |
| `TRUNCATE` | 数据清空 | 审计日志不可清除 |
| `mysql -u root -p` | 明文密码 | 禁止命令行传密码 |
| `curl` + 内网地址 | SSRF | 规则引擎不应主动发起外部请求 |

---

最后更新: 2026-04-26 | v1.0 (HIS 规则引擎安全规范，重点: 表达式注入防护)
