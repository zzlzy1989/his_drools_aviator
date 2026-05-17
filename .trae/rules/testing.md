---
alwaysApply: true
description: 
---
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

### 2.5 Skill Pipeline 测试

```java
@DisplayName("结算 Pipeline 串联测试")
class SettlementPipelineTest {

    @MockBean
    private DeductibleCheckSkill deductibleCheckSkill;

    @MockBean
    private InsuranceLimitSkill insuranceLimitSkill;

    @MockBean
    private RationalDrugUseSkill rationalDrugUseSkill;

    @Autowired
    private SettlementPipeline pipeline;

    @Test
    @DisplayName("正常流程 - 所有 Skill 通过")
    void testExecute_allPass() {
        SettlementFact fact = buildTestFact();

        SettlementResult result = pipeline.execute(fact);

        assertThat(result.hasBlock()).isFalse();
        assertThat(result.getResults()).allMatch(r -> r.getLevel() == ResultLevel.PASS);
    }

    @Test
    @DisplayName("BLOCK 阻断 - 后续 Skill 跳过")
    void testExecute_block_skipsSubsequentSkills() {
        SettlementFact fact = buildTestFact();
        fact.setTotalFee(BigDecimal.ZERO); // 触发阻断条件

        SettlementResult result = pipeline.execute(fact);

        assertThat(result.hasBlock()).isTrue();
        verify(deductibleCheckSkill, times(1)).execute(any());
        verify(insuranceLimitSkill, never()).execute(any());
    }

    @Test
    @DisplayName("WARN 警告 - Pipeline 继续执行")
    void testExecute_warn_continuesPipeline() {
        SettlementFact fact = buildTestFact();

        SettlementResult result = pipeline.execute(fact);

        assertThat(result.hasBlock()).isFalse();
        assertThat(result.hasWarn()).isTrue();
    }
}
```

### 2.6 缓存测试

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

## 三、微服务集成测试

### 3.1 Feign 客户端测试

```java
@DataJpaTest
@AutoConfigureMockMvc
@DisplayName("规则服务 Feign 客户端测试")
class RuleServiceClientTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("通过 Feign 获取规则详情")
    void testGetById_success() throws Exception {
        mockMvc.perform(get("/api/v1/rules/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.ruleKey").value("rule.reimburse.employee"));
    }

    @Test
    @DisplayName("规则不存在 - 返回错误码")
    void testGetById_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/rules/99999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("HIS-001"))
            .andExpect(jsonPath("$.message").value("规则不存在"));
    }
}
```

### 3.2 端到端结算流程测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.yml")
@DisplayName("端到端结算流程测试")
class EndToEndSettlementTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SettlementPipeline pipeline;

    @Test
    @DisplayName("完整结算流程 - 职工医保")
    void testFullSettlement_employee() {
        // 1. 构建结算 Fact
        SettlementFact fact = SettlementFact.builder()
            .tenantId("tenant_001")
            .patientId("P001")
            .patientType("employee")
            .totalFee(new BigDecimal("15000"))
            .deductible(new BigDecimal("1000"))
            .ratio(new BigDecimal("0.85"))
            .build();

        // 2. 执行 Pipeline
        SettlementResult result = pipeline.execute(fact);

        // 3. 验证结果
        assertThat(result.hasBlock()).isFalse();
        assertThat(result.getReimburseAmount())
            .isEqualByComparingTo(new BigDecimal("11900.00"));

        // 4. 验证结算记录已保存
        mockMvc.perform(get("/api/v1/settlements/latest?patientId=P001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.patientId").value("P001"))
            .andExpect(jsonPath("$.data.reimburseAmount").value("11900.00"));
    }

    @Test
    @DisplayName("结算阻断 - 身份缺失")
    void testSettlementBlock_missingIdentity() {
        SettlementFact fact = SettlementFact.builder()
            .tenantId("tenant_001")
            .patientId("P002")
            .patientType(null) // 身份缺失
            .totalFee(new BigDecimal("5000"))
            .build();

        SettlementResult result = pipeline.execute(fact);

        assertThat(result.hasBlock()).isTrue();
        assertThat(result.getBlockReason()).contains("身份");
    }
}
```

### 3.3 网关路由测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("网关路由测试")
class GatewayRouteTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("网关转发 - 规则服务")
    void testGatewayForward_ruleService() {
        ResponseEntity<Result> response = restTemplate.getForEntity(
            "/api/v1/rules/1",
            Result.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCode()).isEqualTo("0");
    }

    @Test
    @DisplayName("网关转发 - 结算服务")
    void testGatewayForward_settlementService() {
        ResponseEntity<Result> response = restTemplate.getForEntity(
            "/api/v1/settlements/latest?patientId=P001",
            Result.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("限流测试 - 超过 QPS 限制")
    void testRateLimit_exceedQps() throws InterruptedException {
        // 快速发送多个请求
        for (int i = 0; i < 100; i++) {
            restTemplate.getForEntity("/api/v1/rules", Result.class);
        }

        // 应该触发限流
        ResponseEntity<Result> response = restTemplate.getForEntity(
            "/api/v1/rules",
            Result.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
```

### 3.4 Testcontainers 集成测试

```java
@Testcontainers
@SpringBootTest
@DisplayName("使用 Testcontainers 集成测试")
class DatabaseIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("his_test")
        .withUsername("root")
        .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Test
    @DisplayName("规则持久化测试")
    void testRulePersistence() {
        RuleDefinition rule = RuleDefinition.builder()
            .ruleKey("rule.test.employee")
            .ruleText("package com.his.rules.test;")
            .category(RuleCategory.REIMBURSE)
            .build();

        ruleMapper.insert(rule);

        RuleDefinition saved = ruleMapper.selectById(rule.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getRuleKey()).isEqualTo("rule.test.employee");
    }
}
```

---

## 四、测试覆盖率要求

| 模块 | 行覆盖率 | 分支覆盖率 | 说明 |
|------|---------|-----------|------|
| 核心规则引擎 | ≥ 90% | ≥ 85% | Drools 规则、Aviator 公式 |
| Skill Pipeline | ≥ 85% | ≥ 80% | 每个 Skill 独立测试 + Pipeline 串联 |
| 工具类 | ≥ 80% | ≥ 75% | AviatorHelper、BigDecimalUtils |
| Controller 层 | ≥ 70% | ≥ 65% | MockMvc 集成测试 |
| Feign 客户端 | ≥ 80% | ≥ 70% | 服务间调用测试 |
| 网关路由 | ≥ 75% | ≥ 70% | 路由转发、限流、鉴权 |

### 规则测试特殊要求

| 规则类型 | 测试要求 |
|---------|---------|
| 身份校验规则 | 至少 3 个用例：职工/居民/身份缺失 |
| 起付线规则 | 至少 3 个用例：高于/低于/等于起付线 |
| 报销比例规则 | 至少 2 个用例：不同患者类型 |
| 封顶线规则 | 至少 3 个用例：低于/等于/超过封顶线 |
| 计算公式规则 | 参数化测试覆盖边界值 |

---

## 五、测试数据管理

| 规则 | 要求 |
|------|------|
| 测试数据 | 使用 `@MethodSource` / `@CsvSource` 参数化，不硬编码在代码里 |
| Fact 对象构建 | 使用 Builder 模式或 Test Data Factory |
| 数据库测试 | 使用 H2 内存数据库或 Testcontainers |
| 敏感数据 | 禁止使用真实患者信息，使用脱敏的模拟数据 |
| 测试环境 | 使用 `application-test.yml` 隔离测试配置 |
| Mock 数据 | 使用 `@MockBean` 替代外部服务依赖 |

---

最后更新: 2026-05-12 | v1.1 (HIS 规则引擎测试规范 - 微服务架构版)
