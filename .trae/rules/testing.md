# 测试规范 - HIS 动态规则中台

## 触发条件
- 编写测试用例
- 执行测试
- 测试覆盖率检查

---

## 一、测试框架

| 层级 | 框架 | 用途 |
|------|------|------|
| 单元测试 | JUnit 5 + Mockito | Service/Helper/Cache 层测试 |
| 规则测试 | Drools 规则单元测试 | DRL 规则逻辑验证 |
| 表达式测试 | Aviator 公式测试 | 公式计算正确性验证 |
| 集成测试 | Spring Boot Test (@SpringBootTest) | 全链路集成验证 |
| 性能测试 | JMH (Java Microbenchmark Harness) | Aviator 编译/执行性能基准 |

### Maven 依赖

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.drools</groupId>
    <artifactId>drools-compiler</artifactId>
    <type>test-jar</type>
    <scope>test</scope>
</dependency>
```

---

## 二、单元测试规范

### 2.1 测试命名

```
{类名}Test.java
├── test_{方法名}_{场景}_{预期结果}()
│   示例: test_executeFormula_validFormula_returnsCorrectAmount()
│   示例: test_executeFormula_nullFormula_returnsZero()
│   示例: test_fireRules_patientTypeEmployee_usesEmployeeDeductible()
```

### 2.2 Aviator 公式测试（本项目核心）

```java
@DisplayName("Aviator 公式计算测试")
class AviatorFormulaTest {

    @BeforeEach
    void setUp() {
        AviatorEvaluator.setOption(
            Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_BIGDECIMAL, true
        );
    }

    @Test
    @DisplayName("职工医保报销公式 - 正常场景")
    void test_employeeReimbursement_normal() {
        String formula = "round((totalFee - deductible) * ratio, 2)";
        Map<String, Object> env = Map.of(
            "totalFee", new BigDecimal("10000"),
            "deductible", new BigDecimal("1000"),
            "ratio", new BigDecimal("0.85")
        );

        Object result = AviatorEvaluator.execute(formula, env);

        assertThat(result).isEqualTo(new BigDecimal("7650.00"));
    }

    @Test
    @DisplayName("居民医保报销公式 - 起付线以下")
    void test_residentReimbursement_belowDeductible() {
        String formula = "totalFee > deductible ? round((totalFee - deductible) * ratio, 2) : BigDecimal.ZERO";
        Map<String, Object> env = Map.of(
            "totalFee", new BigDecimal("300"),
            "deductible", new BigDecimal("500"),
            "ratio", new BigDecimal("0.65")
        );

        Object result = AviatorEvaluator.execute(formula, env);

        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("DRG 权重调整公式")
    void test_drgWeightAdjustment() {
        String formula = "(baseWeight + extraPoints) * severityFactor";
        Map<String, Object> env = Map.of(
            "baseWeight", new BigDecimal("1.2"),
            "extraPoints", new BigDecimal("0.3"),
            "severityFactor", new BigDecimal("1.1")
        );

        Object result = AviatorEvaluator.execute(formula, env);

        assertThat(result).isEqualTo(new BigDecimal("1.650")); // (1.2+0.3)*1.1
    }

    @ParameterizedTest
    @CsvSource({
        "10000, 1000, 0.85, 7650.00",
        "5000, 500, 0.65, 2925.00",
        "200000, 1000, 0.85, 168150.00"
    })
    @DisplayName("报销公式参数化测试")
    void test_reimbursement_parameterized(
        BigDecimal totalFee, BigDecimal deductible,
        BigDecimal ratio, BigDecimal expected) {
        String formula = "round((totalFee - deductible) * ratio, 2)";
        Map<String, Object> env = Map.of(
            "totalFee", totalFee, "deductible", deductible, "ratio", ratio
        );

        Object result = AviatorEvaluator.execute(formula, env);

        assertThat(result).isEqualTo(expected);
    }
}
```

### 2.3 Drools 规则测试

```java
@DisplayName("医保结算 DRL 规则测试")
class ReimbursementRuleTest {

    private KieSession kieSession;

    @BeforeEach
    void setUp() {
        KieServices ks = KieServices.Factory.get();
        KieContainer kc = ks.getKieClasspathContainer();
        kieSession = kc.newKieSession("reimbursementKS");
    }

    @AfterEach
    void tearDown() {
        kieSession.dispose();
    }

    @Test
    @DisplayName("职工起付线规则 - 应设置1000元起付线")
    void test_employeeDeductibleRule() {
        SettlementFact fact = new SettlementFact();
        fact.setPatientType("employee");
        fact.setTotalFee(new BigDecimal("5000"));

        kieSession.insert(fact);
        int rulesFired = kieSession.fireAllRules();

        assertThat(fact.getDeductible()).isEqualTo(new BigDecimal("1000"));
        assertThat(rulesFired).isGreaterThan(0);
    }

    @Test
    @DisplayName("身份缺失规则 - 应拒绝结算并设置金额为0")
    void test_missingIdentityRule() {
        SettlementFact fact = new SettlementFact();
        fact.setPatientType(null);  // 身份缺失

        kieSession.insert(fact);
        kieSession.fireAllRules();

        assertThat(fact.getFinalAmount()).isEqualTo(BigDecimal.ZERO);
    }
}
```

### 2.4 Skill/Agent 测试

```java
@DisplayName("合理用药 Skill 测试")
class RationalDrugUseSkillTest {

    @Mock
    private RuleEngineTemplate ruleEngine;

    @InjectMocks
    private RationalDrugUseSkill skill;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("执行成功 - 无阻断结果")
    void testExecute_success_noBlock() {
        SkillContext<PrescriptionDTO> context = new SkillContext<>(
            "EVENT_DRUG_PRESCRIBE", new PrescriptionDTO()
        );
        context.setTenantId("hospital_001");

        skill.execute(context);

        verify(ruleEngine).fireRules(eq("DRUG_RULES_hospital_001"), eq(context));
        assertThat(context.hasBlock()).isFalse();
    }

    @Test
    @DisplayName("执行异常 - 应降级为 WARN 而非抛出异常")
    void testExecute_exception_degradedToWarn() {
        when(ruleEngine.fireRules(any(), any()))
            .thenThrow(new RuntimeException("规则引擎异常"));

        SkillContext<PrescriptionDTO> context = new SkillContext<>(
            "EVENT_DRUG_PRESCRIBE", new PrescriptionDTO()
        );

        assertDoesNotThrow(() -> skill.execute(context));
        assertThat(context.getResults()).hasSize(1);
        assertThat(context.getResults().get(0).getLevel()).isEqualTo(ResultLevel.WARN);
    }
}
```

### 2.5 缓存测试

```java
@DisplayName("Aviator 表达式缓存测试")
class AviatorExpressionCacheTest {

    private AviatorExpressionCache cache;

    @BeforeEach
    void setUp() {
        cache = new AviatorExpressionCache();
        cache.init();
    }

    @Test
    @DisplayName("同一公式编译后应缓存命中")
    void test_cacheHit_sameFormula() {
        String formula = "round(x * y, 2)";

        Expression exp1 = cache.getCompiledExpression(formula);
        Expression exp2 = cache.getCompiledExpression(formula);

        assertThat(exp1).isSameAs(exp2);  // 缓存命中，同一实例
    }

    @Test
    @DisplayName("刷新后应重新编译")
    void test_refresh_recompiles() {
        String formula = "x + y";

        Expression exp1 = cache.getCompiledExpression(formula);
        cache.refreshFormula(formula);
        Expression exp2 = cache.getCompiledExpression(formula);

        assertThat(exp1).isNotSameAs(exp2);  // 刷新后是新实例
    }
}
```

---

## 三、测试覆盖率要求

| 目标 | 要求 |
|------|------|
| 行覆盖率 | ≥ 80% |
| 分支覆盖率 | ≥ 70% |
| 核心 Service 层 | ≥ 90% |
| AviatorHelper / AviatorExpressionCache | ≥ 95% |
| Drools 规则 | 每条规则至少一个正向 + 一个边界测试 |

---

## 四、测试数据管理

| 规则 | 要求 |
|------|------|
| 测试数据 | 使用 `@MethodSource` / `@CsvSource` 参数化，不硬编码在代码里 |
| Fact 对象构建 | 使用 Builder 模式或 Test Data Factory |
| 数据库测试 | 使用 H2 内存数据库或 Testcontainers |
| 敏感数据 | 禁止使用真实患者信息，使用脱敏的模拟数据 |

---

最后更新: 2026-04-26 | v1.0 (HIS 规则引擎测试规范)
