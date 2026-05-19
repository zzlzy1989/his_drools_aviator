# HIS 动态规则中台 V2.0 详细设计说明书

> 文档版本: v2.0
> 创建日期: 2026-05-01
> 文档状态: ✅ 已完成 (2026-05-19)
> 适用项目: his_drools_aviator V2.0

---

## 1. 技术架构总览

### 1.1 V2.0 新增组件

```
┌─────────────────────────────────────────────────────────────────────┐
│                         V2.0 架构                                   │
├─────────────────────────────────────────────────────────────────────┤
│  新增服务 (2个)                                                     │
│  ├── his-monitor-service (9007)  - 监控服务                         │
│  └── his-market-service (9008)   - 规则市场服务                     │
│                                                                     │
│  新增前端 (4个)                                                     │
│  ├── 规则可视化编排器 - React + AntV X6                             │
│  ├── 测试沙箱界面    - React                                        │
│  ├── 监控大屏        - Vue + ECharts                                │
│  └── 规则市场        - React                                        │
│                                                                     │
│  依赖组件                                                           │
│  ├── Prometheus      - 指标采集存储                                │
│  ├── Grafana         - 可视化看板 (可选)                            │
│  └── Redis           - 监控实时数据缓存                             │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.2 技术选型

| 组件 | 技术方案 | 版本 | 说明 |
|------|---------|------|------|
| 规则流前端 | React + AntV X6 | 2.x | 拖拽式流程图 |
| 监控前端 | Vue3 + ECharts | 5.x | 实时大屏 |
| 监控后端 | Spring Boot + Micrometer | 3.5 | Prometheus埋点 |
| 规则市场后端 | Spring Boot | 3.5 | REST API |
| 指标存储 | Prometheus | 2.x | 时序数据 |
| 实时推送 | WebSocket | - | 监控数据实时推送 |
| 图数据库 | 无 | - | 规则流使用JSON存储 |

---

## 2. 规则可视化编排模块

### 2.1 模块架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                    规则可视化编排模块                                 │
├─────────────────────────────────────────────────────────────────────┤
│  前端层                                                              │
│  ├── FlowEditor  - 画布组件 (AntV X6)                               │
│  ├── NodePanel   - 节点面板                                         │
│  ├── PropertyPanel - 属性配置面板                                   │
│  └── RuleLibrary - 规则库选择                                       │
│                                                                     │
│  后端层                                                              │
│  ├── RuleFlowController - REST API                                  │
│  ├── RuleFlowService - 规则流管理                                   │
│  ├── RuleFlowEngine - 规则流执行引擎                                │
│  └── RuleFlowRepository - 数据访问                                   │
│                                                                     │
│  执行层                                                              │
│  ├── DroolsKieBase - 规则执行                                       │
│  └── AviatorEvaluator - 公式执行                                    │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 规则流数据模型

#### 2.2.1 规则流图定义 (JSON)

```json
{
  "flowId": "reimburse-flow-v1",
  "flowName": "医保结算流程",
  "version": 1,
  "nodes": [
    {
      "nodeId": "node-001",
      "type": "start",
      "label": "开始",
      "position": {"x": 100, "y": 200}
    },
    {
      "nodeId": "node-002",
      "type": "condition",
      "label": "患者类型判断",
      "expression": "fact.patientType == 'RESIDENT'",
      "branches": {
        "true": "node-003",
        "false": "node-004"
      },
      "position": {"x": 300, "y": 200}
    },
    {
      "nodeId": "node-003",
      "type": "action",
      "label": "居民医保结算",
      "ruleKey": "rule.reimburse.resident",
      "next": "node-005",
      "position": {"x": 500, "y": 100}
    },
    {
      "nodeId": "node-004",
      "type": "action",
      "label": "职工医保结算",
      "ruleKey": "rule.reimburse.employee",
      "next": "node-005",
      "position": {"x": 500, "y": 300}
    },
    {
      "nodeId": "node-005",
      "type": "formula",
      "label": "计算报销金额",
      "formulaKey": "formula.reimburse.calculate",
      "next": "node-006",
      "position": {"x": 700, "y": 200}
    },
    {
      "nodeId": "node-006",
      "type": "end",
      "label": "结束",
      "position": {"x": 900, "y": 200}
    }
  ],
  "edges": [
    {"source": "node-001", "target": "node-002", "label": ""},
    {"source": "node-003", "target": "node-005", "label": ""},
    {"source": "node-004", "target": "node-005", "label": ""},
    {"source": "node-005", "target": "node-006", "label": ""}
  ]
}
```

#### 2.2.2 节点类型枚举

```java
public enum FlowNodeType {
    START("start", "开始节点"),
    END("end", "结束节点"),
    CONDITION("condition", "条件节点"),
    ACTION("action", "动作节点"),
    FORMULA("formula", "公式节点"),
    SUBFLOW("subflow", "子流程节点");

    private final String code;
    private final String desc;
}
```

### 2.3 核心类设计

#### 2.3.1 RuleFlowEntity

```java
@Data
@TableName("rule_flow")
public class RuleFlowEntity {
    private Long id;
    private String flowKey;          // 规则流唯一标识
    private String flowName;        // 规则流名称
    private String flowDefinition;  // JSON定义
    private Integer version;        // 版本号
    private String status;          // draft/active/inactive
    private String tenantId;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
```

#### 2.3.2 RuleFlowService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleFlowService {

    private final RuleFlowRepository repository;
    private final RuleFlowEngine engine;
    private final AviatorExpressionCache expressionCache;

    /**
     * 创建规则流
     */
    public RuleFlowEntity createFlow(CreateFlowDTO dto) {
        // 1. 校验节点定义完整性
        validateFlowDefinition(dto.getFlowDefinition());
        // 2. 生成flowKey
        String flowKey = generateFlowKey(dto.getCategory());
        // 3. 保存
        RuleFlowEntity entity = new RuleFlowEntity();
        entity.setFlowKey(flowKey);
        entity.setFlowName(dto.getFlowName());
        entity.setFlowDefinition(JSON.toJSONString(dto.getFlowDefinition()));
        entity.setVersion(1);
        entity.setStatus("draft");
        entity.setTenantId(dto.getTenantId());
        return repository.save(entity);
    }

    /**
     * 发布规则流
     */
    public RuleFlowEntity publishFlow(Long flowId) {
        RuleFlowEntity entity = repository.findById(flowId);
        // 1. 语法校验
        validateFlowDefinition(entity.getFlowDefinition());
        // 2. 检查依赖的规则/公式是否存在
        checkDependencies(entity.getFlowDefinition());
        // 3. 预编译表达式缓存
        precompileExpressions(entity.getFlowDefinition());
        // 4. 更新状态
        entity.setStatus("active");
        entity.setVersion(entity.getVersion() + 1);
        return repository.update(entity);
    }

    /**
     * 回滚到指定版本
     */
    public RuleFlowEntity rollback(Long flowId, Integer targetVersion) {
        RuleFlowEntity entity = repository.findById(flowId);
        RuleFlowHistoryEntity history = historyRepository.findByFlowIdAndVersion(flowId, targetVersion);
        if (history == null) {
            throw new BusinessException("HIS-301", "历史版本不存在");
        }
        // 保存当前版本到历史
        saveHistory(entity);
        // 恢复到目标版本
        entity.setFlowDefinition(history.getFlowDefinition());
        entity.setVersion(targetVersion);
        return repository.update(entity);
    }
}
```

#### 2.3.3 RuleFlowEngine

```java
@Service
@Slf4j
public class RuleFlowEngine {

    private final DroolsRuleExecutor droolsRuleExecutor;
    private final AviatorHelper aviatorHelper;

    /**
     * 执行规则流
     * @param flowDefinition 规则流定义
     * @param context 业务上下文
     * @return 执行结果
     */
    public FlowExecutionResult execute(String flowDefinition, BusinessContext context) {
        FlowDefinition flow = JSON.parseObject(flowDefinition, FlowDefinition.class);
        FlowExecutionResult result = new FlowExecutionResult();

        // 按拓扑顺序执行节点
        List<String> executionOrder = topologicalSort(flow);
        for (String nodeId : executionOrder) {
            FlowNode node = flow.getNodeMap().get(nodeId);
            NodeExecutionResult nodeResult = executeNode(node, context);
            result.addNodeResult(nodeResult);

            // 条件节点根据结果确定下一步
            if (node.getType() == FlowNodeType.CONDITION) {
                Boolean branchResult = (Boolean) nodeResult.getOutput();
                String nextNodeId = branchResult
                    ? node.getBranches().get("true")
                    : node.getBranches().get("false");
                if (nextNodeId != null) {
                    context.setCurrentNodeId(nextNodeId);
                }
            } else if (node.getNext() != null) {
                context.setCurrentNodeId(node.getNext());
            }
        }

        return result;
    }

    /**
     * 执行单个节点
     */
    private NodeExecutionResult executeNode(FlowNode node, BusinessContext context) {
        long startTime = System.currentTimeMillis();
        try {
            switch (node.getType()) {
                case START:
                    return NodeExecutionResult.success(node.getNodeId());
                case END:
                    return NodeExecutionResult.success(node.getNodeId(), context.getResult());
                case CONDITION:
                    Object condResult = aviatorHelper.executeExpression(
                        node.getExpression(), context.getVariables());
                    return NodeExecutionResult.success(node.getNodeId(), condResult);
                case ACTION:
                    Object actionResult = droolsRuleExecutor.fireRules(
                        node.getRuleKey(), context.getFact());
                    context.setResult(actionResult);
                    return NodeExecutionResult.success(node.getNodeId(), actionResult);
                case FORMULA:
                    Object formulaResult = aviatorHelper.executeFormula(
                        node.getFormulaKey(), context.getVariables());
                    context.setResult(formulaResult);
                    return NodeExecutionResult.success(node.getNodeId(), formulaResult);
                case SUBFLOW:
                    RuleFlowEntity subFlow = ruleFlowRepository.findActiveByKey(node.getSubFlowId());
                    FlowExecutionResult subResult = execute(subFlow.getFlowDefinition(), context);
                    return NodeExecutionResult.success(node.getNodeId(), subResult);
                default:
                    throw new UnsupportedOperationException("不支持的节点类型: " + node.getType());
            }
        } catch (Exception e) {
            log.error("节点执行失败: nodeId={}", node.getNodeId(), e);
            return NodeExecutionResult.failed(node.getNodeId(), e.getMessage());
        }
    }
}
```

### 2.4 API 接口设计

#### 2.4.1 规则流 Controller

```java
@RestController
@RequestMapping("/api/v2/flows")
@RequiredArgsConstructor
@Slf4j
public class RuleFlowController {

    private final RuleFlowService ruleFlowService;

    @GetMapping
    public Result<PageResult<RuleFlowVO>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            RuleFlowQueryDTO queryDTO) {
        return Result.success(ruleFlowService.pageList(page, pageSize, queryDTO));
    }

    @GetMapping("/{id}")
    public Result<RuleFlowVO> getById(@PathVariable Long id) {
        return Result.success(ruleFlowService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody @Valid CreateFlowDTO dto) {
        return Result.success(ruleFlowService.createFlow(dto).getId());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid UpdateFlowDTO dto) {
        ruleFlowService.updateFlow(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ruleFlowService.deleteFlow(id);
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        ruleFlowService.publishFlow(id);
        return Result.success();
    }

    @PostMapping("/{id}/rollback")
    public Result<Void> rollback(@PathVariable Long id, @RequestParam Integer targetVersion) {
        ruleFlowService.rollback(id, targetVersion);
        return Result.success();
    }

    @GetMapping("/{id}/versions")
    public Result<List<FlowVersionVO>> getVersions(@PathVariable Long id) {
        return Result.success(ruleFlowService.getVersionHistory(id));
    }

    @GetMapping("/{id}/compare")
    public Result<FlowCompareVO> compare(@PathVariable Long id,
            @RequestParam Integer versionA, @RequestParam Integer versionB) {
        return Result.success(ruleFlowService.compareVersions(id, versionA, versionB));
    }

    @GetMapping("/{id}/export")
    public Result<String> exportFlow(@PathVariable Long id) {
        return Result.success(ruleFlowService.exportFlowJson(id));
    }

    @PostMapping("/import")
    public Result<Long> importFlow(@RequestBody String flowJson) {
        return Result.success(ruleFlowService.importFlowJson(flowJson).getId());
    }
}
```

---

## 3. 规则测试沙箱模块

### 3.1 模块架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                     规则测试沙箱模块                                 │
├─────────────────────────────────────────────────────────────────────┤
│  隔离机制                                                            │
│  ├── 独立数据库 (sandbox_his) - 测试数据与生产隔离                    │
│  ├── 独立Redis命名空间 - 测试缓存隔离                                │
│  └── 测试会话管理 - 独立执行环境                                     │
│                                                                     │
│  核心组件                                                            │
│  ├── TestDataManager - 测试数据管理                                 │
│  ├── SandboxExecutor - 沙箱执行器                                    │
│  ├── ResultComparator - 结果对比                                    │
│  └── TestReportGenerator - 报告生成                                 │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.2 测试数据模型

#### 3.2.1 测试数据集

```java
@Data
@TableName("test_data_set")
public class TestDataSetEntity {
    private Long id;
    private String setName;
    private String setDescription;
    private String category;      // SETTLEMENT/DRUG/QUALITY/DRG
    private String testCases;     // JSON数组
    private String tenantId;
    private LocalDateTime createTime;
}

@Data
public class TestCase {
    private String caseId;
    private String caseName;
    private String description;
    private Map<String, Object> fact;     // 输入Fact
    private Map<String, Object> expected; // 预期结果
    private Map<String, Object> variables; // Aviator变量
}
```

#### 3.2.2 执行记录

```java
@Data
@TableName("sandbox_execution_log")
public class SandboxExecutionLogEntity {
    private Long id;
    private Long dataSetId;
    private String caseId;
    private String status;           // RUNNING/SUCCESS/FAILED
    private String actualResult;    // JSON
    private String expectedResult;  // JSON
    private String diffResult;      // 差异详情
    private Long executeMs;
    private String errorMessage;
    private LocalDateTime executeTime;
}
```

### 3.3 核心类设计

#### 3.3.1 SandboxExecutor

```java
@Service
@Slf4j
public class SandboxExecutor {

    private final RuleEngineTemplate ruleEngine;
    private final AviatorHelper aviatorHelper;
    private final ObjectMapper objectMapper;

    /**
     * 执行单条测试用例
     */
    public TestCaseResult executeTestCase(TestCase testCase, SandboxContext context) {
        long startTime = System.currentTimeMillis();
        TestCaseResult result = new TestCaseResult();
        result.setCaseId(testCase.getCaseId());

        try {
            // 1. 构建Fact对象
            Object fact = buildFact(testCase.getFact(), context.getFactType());

            // 2. 准备变量上下文
            Map<String, Object> variables = new HashMap<>(testCase.getVariables());

            // 3. 执行规则
            if (context.getRuleKey() != null) {
                Object ruleResult = ruleEngine.fireRules(context.getRuleKey(), fact);
                variables.put("_ruleResult", ruleResult);
            }

            // 4. 执行公式（如有）
            if (context.getFormulaKey() != null) {
                Object formulaResult = aviatorHelper.executeFormula(
                    context.getFormulaKey(), variables);
                variables.put("_formulaResult", formulaResult);
            }

            // 5. 构建实际结果
            Map<String, Object> actual = extractResults(fact, variables);

            // 6. 比对预期
            result.setActual(actual);
            result.setExpected(testCase.getExpected());
            result.setDiff(compare(expected, actual));
            result.setStatus(result.getDiff().isEmpty() ? "SUCCESS" : "FAILED");

            log.info("测试用例执行完成: caseId={}, status={}, diffCount={}",
                testCase.getCaseId(), result.getStatus(), result.getDiff().size());

        } catch (Exception e) {
            log.error("测试用例执行异常: caseId={}", testCase.getCaseId(), e);
            result.setStatus("ERROR");
            result.setErrorMessage(e.getMessage());
        }

        result.setExecuteMs(System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 批量执行测试用例
     */
    public BatchTestResult executeBatch(List<TestCase> testCases, SandboxContext context) {
        BatchTestResult batchResult = new BatchTestResult();
        batchResult.setTotal(testCases.size());

        for (TestCase tc : testCases) {
            TestCaseResult caseResult = executeTestCase(tc, context);
            batchResult.addResult(caseResult);

            if ("SUCCESS".equals(caseResult.getStatus())) {
                batchResult.incrementSuccess();
            } else {
                batchResult.incrementFailed();
            }
        }

        batchResult.setSuccessRate(
            batchResult.getTotal() > 0
                ? (double) batchResult.getSuccess() / batchResult.getTotal() * 100
                : 0);

        return batchResult;
    }

    private Map<String, Object> compare(Map<String, Object> expected, Map<String, Object> actual) {
        Map<String, Object> diff = new HashMap<>();
        for (String key : expected.keySet()) {
            Object expValue = expected.get(key);
            Object actValue = actual.get(key);
            if (!Objects.equals(expValue, actValue)) {
                diff.put(key, DiffItem.builder()
                    .expected(expValue)
                    .actual(actValue)
                    .build());
            }
        }
        return diff;
    }
}
```

#### 3.3.2 TestReportGenerator

```java
@Service
@Slf4j
public class TestReportGenerator {

    private final TemplateEngine templateEngine;  // 如 Thymeleaf

    /**
     * 生成HTML测试报告
     */
    public String generateHtmlReport(BatchTestResult batchResult) {
        Map<String, Object> data = new HashMap<>();
        data.put("summary", batchResult);
        data.put("results", batchResult.getResults());
        data.put("generatedAt", LocalDateTime.now());

        // 使用Thymeleaf渲染HTML模板
        return templateEngine.process("test-report-template", data);
    }

    /**
     * 生成PDF测试报告
     */
    public byte[] generatePdfReport(BatchTestResult batchResult) {
        String html = generateHtmlReport(batchResult);
        // 使用 iText 或 openhtmltopdf 转换为 PDF
        return pdfConverter.convert(html);
    }
}
```

### 3.4 API 接口设计

```java
@RestController
@RequestMapping("/api/v2/sandbox")
@RequiredArgsConstructor
@Slf4j
public class SandboxController {

    private final TestDataSetService testDataSetService;
    private final SandboxExecutor sandboxExecutor;
    private final TestReportGenerator reportGenerator;

    // ========== 测试数据管理 ==========

    @GetMapping("/datasets")
    public Result<PageResult<TestDataSetVO>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String category) {
        return Result.success(testDataSetService.pageList(page, pageSize, category));
    }

    @PostMapping("/datasets")
    public Result<Long> createDataSet(@RequestBody @Valid CreateDataSetDTO dto) {
        return Result.success(testDataSetService.createDataSet(dto).getId());
    }

    @PutMapping("/datasets/{id}")
    public Result<Void> updateDataSet(@PathVariable Long id, @RequestBody @Valid UpdateDataSetDTO dto) {
        testDataSetService.updateDataSet(id, dto);
        return Result.success();
    }

    @DeleteMapping("/datasets/{id}")
    public Result<Void> deleteDataSet(@PathVariable Long id) {
        testDataSetService.deleteDataSet(id);
        return Result.success();
    }

    // ========== 测试执行 ==========

    @PostMapping("/execute")
    public Result<TestCaseResult> execute(@RequestBody @Valid ExecuteTestDTO dto) {
        SandboxContext context = buildContext(dto);
        TestCase testCase = buildTestCase(dto);
        return Result.success(sandboxExecutor.executeTestCase(testCase, context));
    }

    @PostMapping("/execute/batch")
    public Result<BatchTestResult> executeBatch(@RequestBody @Valid BatchExecuteDTO dto) {
        SandboxContext context = buildContext(dto);
        List<TestCase> testCases = buildTestCases(dto.getDataSetId());
        return Result.success(sandboxExecutor.executeBatch(testCases, context));
    }

    @GetMapping("/reports/{executionId}")
    public Result<TestReportVO> getReport(@PathVariable String executionId) {
        return Result.success(reportGenerator.getReport(executionId));
    }

    @GetMapping("/reports/{executionId}/export")
    public Result<String> exportReport(@PathVariable String executionId,
            @RequestParam(defaultValue = "HTML") String format) {
        if ("PDF".equals(format)) {
            return Result.success(Base64.getEncoder().encodeToString(
                reportGenerator.generatePdfReport(executionId)));
        }
        return Result.success(reportGenerator.generateHtmlReport(executionId));
    }
}
```

---

## 4. 规则执行监控大屏模块

### 4.1 模块架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                     规则执行监控模块                                 │
├─────────────────────────────────────────────────────────────────────┤
│  指标采集层                                                          │
│  ├── Micrometer + Prometheus - Spring Boot 指标埋点                  │
│  ├── 自定义埋点 - SkillPipelineMonitor / RuleExecutionMonitor        │
│  └── 中间件埋点 - Druid/HikariCP / Redis / Spring MVC                │
│                                                                     │
│  实时处理层                                                          │
│  ├── Prometheus Remote Write - 指标存储                              │
│  ├── Redis Pub/Sub - 实时数据缓存                                    │
│  └── WebSocket - 前端实时推送                                       │
│                                                                     │
│  可视化层                                                            │
│  ├── Grafana (可选) - 专业监控看板                                   │
│  └── 自建Vue大屏 - 业务监控专用                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 4.2 监控指标定义

#### 4.2.1 Micrometer 指标注册

```java
@Configuration
public class MonitorMetricsConfig {

    @Bean
    public MeterRegistry meterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    @Bean
    public RuleExecutionMonitor ruleExecutionMonitor(MeterRegistry registry) {
        return new RuleExecutionMonitor(registry, "his.rule");
    }

    @Bean
    public FormulaExecutionMonitor formulaExecutionMonitor(MeterRegistry registry) {
        return new FormulaExecutionMonitor(registry, "his.formula");
    }

    @Bean
    public SkillPipelineMonitor skillPipelineMonitor(MeterRegistry registry) {
        return new SkillPipelineMonitor(registry, "his.skill");
    }
}
```

#### 4.2.2 规则执行监控器

```java
@Component
@Slf4j
public class RuleExecutionMonitor {

    private final Counter executionTotal;
    private final Counter executionSuccess;
    private final Counter executionFailed;
    private final Timer executionDuration;

    public RuleExecutionMonitor(MeterRegistry registry, String prefix) {
        this.executionTotal = registry.counter(prefix, "type", "execution_total");
        this.executionSuccess = registry.counter(prefix, "type", "execution_success");
        this.executionFailed = registry.counter(prefix, "type", "execution_failed");
        this.executionDuration = registry.timer(prefix, "type", "duration");
    }

    public void recordExecution(String ruleKey, String tenantId, long durationMs, boolean success) {
        executionTotal.increment();
        Tag[] tags = {
            Tag.of("rule_key", ruleKey),
            Tag.of("tenant_id", tenantId),
            Tag.of("success", String.valueOf(success))
        };

        if (success) {
            executionSuccess.increment(tags);
        } else {
            executionFailed.increment(tags);
        }

        executionDuration.record(durationMs, TimeUnit.MILLISECONDS);

        // 记录慢规则告警
        if (durationMs > 100) {
            log.warn("规则执行慢: ruleKey={}, duration={}ms", ruleKey, durationMs);
        }
    }
}
```

#### 4.2.3 AOP 埋点

```java
@Aspect
@Component
@Slf4j
public class RuleExecutionAspect {

    @Autowired
    private RuleExecutionMonitor monitor;

    @Around("execution(* com.his.engine.RuleEngine+.fireRules(..))")
    public Object aroundRuleExecution(ProceedingJoinPoint pjp) throws Throwable {
        String ruleKey = extractRuleKey(pjp.getArgs());
        long startTime = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - startTime;
            monitor.recordExecution(ruleKey, getTenantId(), duration, true);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            monitor.recordExecution(ruleKey, getTenantId(), duration, false);
            throw e;
        }
    }
}
```

### 4.3 核心类设计

#### 4.3.1 MonitorService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorService {

    private final MeterRegistry meterRegistry;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RuleExecutionMonitor ruleMonitor;
    private final FormulaExecutionMonitor formulaMonitor;
    private final SkillPipelineMonitor skillMonitor;

    /**
     * 获取监控看板数据
     */
    public DashboardVO getDashboardData() {
        DashboardVO dashboard = new DashboardVO();

        // 总执行次数
        dashboard.setTotalExecutions(getCounterValue("his.rule", "execution_total"));

        // 命中率
        Double hitRate = calculateHitRate();
        dashboard.setHitRate(hitRate);

        // P99耗时
        dashboard.setP99Duration(getP99Duration("his.rule", "duration"));

        // TOP10规则
        dashboard.setTopRules(getTopRules(10));

        // 异常规则
        dashboard.setAlertRules(getAlertRules());

        return dashboard;
    }

    /**
     * 获取TOP N慢规则
     */
    public List<RuleStatVO> getTopSlowRules(int topN) {
        // 从Prometheus查询
        String query = "topk(" + topN + ", his_rule_duration_seconds_sum / his_rule_execution_total)";
        return prometheusClient.queryRange(query, Instant.now().minus(1, ChronoUnit.HOURS), Instant.now());
    }

    /**
     * 获取规则触发热力图
     */
    public HeatmapVO getRuleHeatmap(String tenantId) {
        // 查询过去24小时每小时的规则触发次数
        String query = "sum by (le) (rate(his_rule_execution_total[1h]))";
        return null;
    }
}
```

#### 4.3.2 AlertRuleService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertRuleService {

    private final AlertRuleRepository repository;
    private final NotificationService notificationService;
    private final MonitorService monitorService;

    @Scheduled(fixedRate = 60000)  // 每分钟检查一次
    public void checkAlerts() {
        List<AlertRuleEntity> rules = repository.findAllEnabled();
        for (AlertRuleEntity rule : rules) {
            boolean triggered = evaluateRule(rule);
            if (triggered && !rule.isAlreadyTriggered()) {
                // 发送通知
                sendNotification(rule);
                rule.setAlreadyTriggered(true);
                repository.update(rule);
            } else if (!triggered) {
                rule.setAlreadyTriggered(false);
                repository.update(rule);
            }
        }
    }

    private boolean evaluateRule(AlertRuleEntity rule) {
        Double currentValue = getMetricValue(rule.getMetricName(), rule.getTenantId());

        switch (rule.getCondition()) {
            case ">":
                return currentValue > rule.getThreshold();
            case "<":
                return currentValue < rule.getThreshold();
            case ">=":
                return currentValue >= rule.getThreshold();
            case "<=":
                return currentValue <= rule.getThreshold();
            case "=":
                return currentValue.equals(rule.getThreshold());
            default:
                return false;
        }
    }
}
```

### 4.4 API 接口设计

```java
@RestController
@RequestMapping("/api/v2/monitor")
@RequiredArgsConstructor
@Slf4j
public class MonitorController {

    private final MonitorService monitorService;
    private final AlertRuleService alertRuleService;

    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        return Result.success(monitorService.getDashboardData());
    }

    @GetMapping("/rules/top")
    public Result<List<RuleStatVO>> getTopRules(
            @RequestParam(defaultValue = "10") Integer topN,
            @RequestParam(defaultValue = "duration") String sortBy) {
        return Result.success(monitorService.getTopRules(topN, sortBy));
    }

    @GetMapping("/heatmap")
    public Result<HeatmapVO> getHeatmap(@RequestParam String tenantId) {
        return Result.success(monitorService.getRuleHeatmap(tenantId));
    }

    @GetMapping("/trends")
    public Result<TrendVO> getTrends(
            @RequestParam String metric,
            @RequestParam String tenantId,
            @RequestParam(defaultValue = "1h") String duration) {
        return Result.success(monitorService.getTrends(metric, tenantId, duration));
    }

    // ========== 告警规则管理 ==========

    @GetMapping("/alerts")
    public Result<PageResult<AlertRuleVO>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(alertRuleService.pageList(page, pageSize));
    }

    @PostMapping("/alerts")
    public Result<Long> createAlert(@RequestBody @Valid CreateAlertRuleDTO dto) {
        return Result.success(alertRuleService.createAlertRule(dto).getId());
    }

    @PutMapping("/alerts/{id}")
    public Result<Void> updateAlert(@PathVariable Long id, @RequestBody @Valid UpdateAlertRuleDTO dto) {
        alertRuleService.updateAlertRule(id, dto);
        return Result.success();
    }

    @DeleteMapping("/alerts/{id}")
    public Result<Void> deleteAlert(@PathVariable Long id) {
        alertRuleService.deleteAlertRule(id);
        return Result.success();
    }
}
```

### 4.5 WebSocket 实时推送

```java
@Configuration
@EnableWebSocket
public class MonitorWebSocketConfig {

    @Bean
    public WebSocketHandler monitorWebSocketHandler() {
        return new MonitorWebSocketHandler();
    }
}

@Component
@Slf4j
public class MonitorWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Scheduled(fixedRate = 1000)  // 每秒推送
    public void pushMetrics() {
        DashboardVO dashboard = monitorService.getDashboardData();
        String message = JSON.toJSONString(dashboard);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("监控WebSocket连接建立: sessionId={}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("监控WebSocket连接关闭: sessionId={}", session.getId());
    }
}
```

---

## 5. 规则市场模块

### 5.1 模块架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                      规则市场模块                                    │
├─────────────────────────────────────────────────────────────────────┤
│  核心服务                                                            │
│  ├── TemplatePublishService - 模板发布管理                           │
│  ├── TemplateSearchService - 模板搜索检索                           │
│  ├── TemplateSubscribeService - 模板订阅安装                         │
│  └── TemplateRatingService - 模板评分评论                           │
│                                                                     │
│  安全机制                                                            │
│  ├── TemplateSecurityScanner - 模板内容安全扫描                       │
│  └── TenantAccessController - 租户权限控制                           │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.2 数据模型

#### 5.2.1 规则模板实体

```java
@Data
@TableName("rule_template")
public class RuleTemplateEntity {
    private Long id;
    private String templateKey;
    private String templateName;
    private String description;
    private String category;        // REIMBURSE/DRUG/QUALITY/DRG/GENERAL
    private String tags;            // JSON数组
    private String content;          // JSON: {rules: [], formulas: [], flows: []}
    private String providerTenantId;
    private String providerTenantName;
    private String publishedBy;
    private LocalDateTime publishedAt;
    private String version;
    private Integer installCount;
    private BigDecimal avgRating;
    private Integer commentCount;
    private String status;           // PUBLISHED/ARCHIVED
    private String tenantId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

#### 5.2.2 模板订阅实体

```java
@Data
@TableName("template_subscription")
public class TemplateSubscriptionEntity {
    private Long id;
    private Long templateId;
    private String subscriberTenantId;
    private String installedVersion;
    private LocalDateTime installTime;
    private String status;           // ACTIVE/UPDATED/UNSUBSCRIBED
    private LocalDateTime createTime;
}
```

#### 5.2.3 模板评论实体

```java
@Data
@TableName("template_comment")
public class TemplateCommentEntity {
    private Long id;
    private Long templateId;
    private String tenantId;
    private String tenantName;
    private Integer rating;          // 1-5星
    private String comment;
    private String replyTo;          // 回复某条评论
    private LocalDateTime createTime;
}
```

### 5.3 核心类设计

#### 5.3.1 TemplatePublishService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplatePublishService {

    private final RuleTemplateRepository repository;
    private final TemplateSecurityScanner securityScanner;
    private final RuleDefinitionService ruleService;
    private final FormulaService formulaService;
    private final RuleFlowService ruleFlowService;

    /**
     * 发布模板
     */
    public RuleTemplateEntity publishTemplate(PublishTemplateDTO dto) {
        // 1. 安全扫描
        SecurityScanResult scanResult = securityScanner.scan(dto.getContent());
        if (!scanResult.isPass()) {
            throw new BusinessException("HIS-401", "模板内容安全扫描未通过: " + scanResult.getMessage());
        }

        // 2. 校验依赖完整性
        validateDependencies(dto.getContent());

        // 3. 生成templateKey
        String templateKey = "tpl." + dto.getCategory().toLowerCase()
            + "." + UUID.randomUUID().toString().substring(0, 8);

        // 4. 保存模板
        RuleTemplateEntity entity = new RuleTemplateEntity();
        entity.setTemplateKey(templateKey);
        entity.setTemplateName(dto.getTemplateName());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setTags(JSON.toJSONString(dto.getTags()));
        entity.setContent(JSON.toJSONString(dto.getContent()));
        entity.setProviderTenantId(dto.getTenantId());
        entity.setPublishedBy(dto.getOperator());
        entity.setPublishedAt(LocalDateTime.now());
        entity.setVersion("1.0.0");
        entity.setInstallCount(0);
        entity.setAvgRating(BigDecimal.ZERO);
        entity.setStatus("PUBLISHED");

        log.info("模板发布成功: templateKey={}, name={}", templateKey, dto.getTemplateName());
        return repository.save(entity);
    }

    /**
     * 模板内容安全扫描
     */
    public SecurityScanResult scanTemplateContent(TemplateContent content) {
        SecurityScanResult result = new SecurityScanResult();

        // 扫描DRL规则
        for (RuleDefinition rule : content.getRules()) {
            SecurityCheckResult ruleCheck = securityScanner.scanDrl(rule.getRuleText());
            if (!ruleCheck.isPass()) {
                result.setPass(false);
                result.addMessage("规则 " + rule.getRuleKey() + ": " + ruleCheck.getMessage());
            }
        }

        // 扫描Aviator公式
        for (Formula formula : content.getFormulas()) {
            SecurityCheckResult formulaCheck = securityScanner.scanAviator(formula.getFormulaText());
            if (!formulaCheck.isPass()) {
                result.setPass(false);
                result.addMessage("公式 " + formula.getFormulaKey() + ": " + formulaCheck.getMessage());
            }
        }

        return result;
    }
}
```

#### 5.3.2 TemplateSecurityScanner

```java
@Component
@Slf4j
public class TemplateSecurityScanner {

    private static final Set<String> DANGEROUS_DRL_PATTERNS = Arrays.asList(
        "java.lang.System",
        "Runtime.getRuntime",
        "ProcessBuilder",
        "java.io.File"
    );

    private static final Set<String> DANGEROUS_AVIATOR_FUNCTIONS = Arrays.asList(
        "sys.",
        "fn.",
        "exec",
        "eval"
    );

    /**
     * 扫描DRL规则
     */
    public SecurityCheckResult scanDrl(String drlContent) {
        for (String pattern : DANGEROUS_DRL_PATTERNS) {
            if (drlContent.contains(pattern)) {
                return SecurityCheckResult.fail("DRL包含危险模式: " + pattern);
            }
        }

        // 检查是否有Java代码块
        if (drlContent.contains("when") && drlContent.contains("eval(")) {
            // 允许简单的eval但禁止复杂调用
            if (drlContent.contains("System")) {
                return SecurityCheckResult.fail("DRL禁止调用System");
            }
        }

        return SecurityCheckResult.pass();
    }

    /**
     * 扫描Aviator公式
     */
    public SecurityCheckResult scanAviator(String formulaText) {
        for (String dangerous : DANGEROUS_AVIATOR_FUNCTIONS) {
            if (formulaText.contains(dangerous)) {
                return SecurityCheckResult.fail("公式包含禁止函数: " + dangerous);
            }
        }

        // 长度限制
        if (formulaText.length() > 500) {
            return SecurityCheckResult.fail("公式长度超限(最大500字符)");
        }

        return SecurityCheckResult.pass();
    }
}
```

#### 5.3.3 TemplateSubscribeService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateSubscribeService {

    private final TemplateSubscriptionRepository subscriptionRepository;
    private final RuleTemplateRepository templateRepository;
    private final RuleDefinitionService ruleService;
    private final FormulaService formulaService;
    private final RuleFlowService ruleFlowService;

    /**
     * 订阅并安装模板
     */
    @Transactional
    public TemplateSubscriptionEntity subscribeTemplate(Long templateId, String tenantId) {
        RuleTemplateEntity template = templateRepository.findById(templateId);
        if (template == null) {
            throw new BusinessException("HIS-501", "模板不存在");
        }

        // 检查是否已订阅
        TemplateSubscriptionEntity existing = subscriptionRepository
            .findByTemplateIdAndSubscriberTenantId(templateId, tenantId);
        if (existing != null && "ACTIVE".equals(existing.getStatus())) {
            throw new BusinessException("HIS-502", "已订阅该模板，请先取消订阅");
        }

        // 解析模板内容
        TemplateContent content = JSON.parseObject(template.getContent(), TemplateContent.class);

        // 安装规则
        for (RuleDefinition rule : content.getRules()) {
            ruleService.createFromTemplate(rule, tenantId, template.getTemplateKey());
        }

        // 安装公式
        for (Formula formula : content.getFormulas()) {
            formulaService.createFromTemplate(formula, tenantId, template.getTemplateKey());
        }

        // 安装规则流
        for (RuleFlowDefinition flow : content.getFlows()) {
            ruleFlowService.createFromTemplate(flow, tenantId, template.getTemplateKey());
        }

        // 创建订阅记录
        TemplateSubscriptionEntity subscription = new TemplateSubscriptionEntity();
        subscription.setTemplateId(templateId);
        subscription.setSubscriberTenantId(tenantId);
        subscription.setInstalledVersion(template.getVersion());
        subscription.setInstallTime(LocalDateTime.now());
        subscription.setStatus("ACTIVE");

        // 更新模板安装数
        template.setInstallCount(template.getInstallCount() + 1);
        templateRepository.update(template);

        log.info("模板订阅安装成功: templateId={}, tenantId={}", templateId, tenantId);
        return subscriptionRepository.save(subscription);
    }

    /**
     * 取消订阅
     */
    @Transactional
    public void unsubscribeTemplate(Long templateId, String tenantId) {
        TemplateSubscriptionEntity subscription = subscriptionRepository
            .findByTemplateIdAndSubscriberTenantId(templateId, tenantId);

        if (subscription == null) {
            throw new BusinessException("HIS-503", "未找到订阅记录");
        }

        subscription.setStatus("UNSUBSCRIBED");
        subscriptionRepository.update(subscription);

        log.info("模板取消订阅: templateId={}, tenantId={}", templateId, tenantId);
    }
}
```

### 5.4 API 接口设计

```java
@RestController
@RequestMapping("/api/v2/market")
@RequiredArgsConstructor
@Slf4j
public class MarketController {

    private final TemplateSearchService searchService;
    private final TemplatePublishService publishService;
    private final TemplateSubscribeService subscribeService;
    private final TemplateRatingService ratingService;

    // ========== 模板搜索 ==========

    @GetMapping("/templates")
    public Result<PageResult<TemplateVO>> searchTemplates(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy) {
        return Result.success(searchService.search(page, pageSize, category, keyword, sortBy));
    }

    @GetMapping("/templates/{id}")
    public Result<TemplateDetailVO> getTemplateDetail(@PathVariable Long id) {
        return Result.success(searchService.getDetail(id));
    }

    @GetMapping("/categories")
    public Result<List<CategoryVO>> getCategories() {
        return Result.success(searchService.getCategories());
    }

    // ========== 模板发布 ==========

    @PostMapping("/templates")
    public Result<Long> publishTemplate(@RequestBody @Valid PublishTemplateDTO dto) {
        return Result.success(publishService.publishTemplate(dto).getId());
    }

    @PutMapping("/templates/{id}")
    public Result<Void> updateTemplate(@PathVariable Long id, @RequestBody @Valid UpdateTemplateDTO dto) {
        publishService.updateTemplate(id, dto);
        return Result.success();
    }

    @DeleteMapping("/templates/{id}")
    public Result<Void> archiveTemplate(@PathVariable Long id) {
        publishService.archiveTemplate(id);
        return Result.success();
    }

    // ========== 模板订阅 ==========

    @PostMapping("/templates/{id}/subscribe")
    public Result<Void> subscribeTemplate(@PathVariable Long id, @RequestHeader("X-Tenant-Id") String tenantId) {
        subscribeService.subscribeTemplate(id, tenantId);
        return Result.success();
    }

    @PostMapping("/templates/{id}/unsubscribe")
    public Result<Void> unsubscribeTemplate(@PathVariable Long id, @RequestHeader("X-Tenant-Id") String tenantId) {
        subscribeService.unsubscribeTemplate(id, tenantId);
        return Result.success();
    }

    // ========== 我的发布/订阅 ==========

    @GetMapping("/my")
    public Result<PageResult<TemplateVO>> getMyTemplates(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return Result.success(publishService.getMyTemplates(page, pageSize, tenantId));
    }

    @GetMapping("/subscribed")
    public Result<PageResult<SubscribedTemplateVO>> getSubscribed(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return Result.success(subscribeService.getSubscribedTemplates(page, pageSize, tenantId));
    }

    // ========== 评分评论 ==========

    @PostMapping("/templates/{id}/rate")
    public Result<Void> rateTemplate(@PathVariable Long id,
            @RequestBody @Valid RateTemplateDTO dto,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        ratingService.rateTemplate(id, tenantId, dto);
        return Result.success();
    }

    @GetMapping("/templates/{id}/comments")
    public Result<PageResult<CommentVO>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(ratingService.getComments(id, page, pageSize));
    }
}
```

---

## 6. 新增数据库表

### 6.1 表清单

| 表名 | 说明 |
|------|------|
| rule_flow | 规则流定义 |
| rule_flow_history | 规则流历史版本 |
| test_data_set | 测试数据集 |
| test_case | 测试用例 |
| sandbox_execution_log | 沙箱执行日志 |
| rule_template | 规则模板 |
| template_subscription | 模板订阅 |
| template_comment | 模板评论 |
| monitor_alert_rule | 监控告警规则 |
| monitor_alert_record | 告警触发记录 |

### 6.2 表结构 DDL

```sql
-- =============================================
-- 规则流定义表
-- =============================================
CREATE TABLE rule_flow (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    flow_key        VARCHAR(128) NOT NULL COMMENT '规则流唯一标识',
    flow_name       VARCHAR(128) NOT NULL COMMENT '规则流名称',
    flow_definition JSON         NOT NULL COMMENT '规则流图定义',
    version         INT          NOT NULL DEFAULT 1 COMMENT '版本号',
    status          VARCHAR(16)  NOT NULL DEFAULT 'draft' COMMENT 'draft/active/inactive',
    category        VARCHAR(32)  DEFAULT NULL COMMENT '分类',
    tenant_id       VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '租户ID',
    create_by       VARCHAR(64)  DEFAULT NULL,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       VARCHAR(64)  DEFAULT NULL,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_flow_key_tenant (flow_key, tenant_id),
    KEY idx_status_tenant (status, tenant_id)
) ENGINE=InnoDB COMMENT='规则流定义';

-- =============================================
-- 规则流历史版本表
-- =============================================
CREATE TABLE rule_flow_history (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    flow_id         BIGINT       NOT NULL COMMENT '规则流ID',
    version         INT          NOT NULL COMMENT '版本号',
    flow_definition JSON         NOT NULL COMMENT '规则流定义快照',
    change_desc     VARCHAR(512) DEFAULT NULL COMMENT '变更说明',
    change_by       VARCHAR(64)  NOT NULL COMMENT '变更人',
    change_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_flow_version (flow_id, version)
) ENGINE=InnoDB COMMENT='规则流历史版本';

-- =============================================
-- 测试数据集表
-- =============================================
CREATE TABLE test_data_set (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    set_name        VARCHAR(128) NOT NULL COMMENT '数据集名称',
    set_description VARCHAR(512) DEFAULT NULL COMMENT '描述',
    category        VARCHAR(32)  NOT NULL COMMENT '分类: SETTLEMENT/DRUG/QUALITY/DRG',
    test_cases      JSON         NOT NULL COMMENT '测试用例数组',
    tenant_id       VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '租户ID',
    create_by       VARCHAR(64)  DEFAULT NULL,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       VARCHAR(64)  DEFAULT NULL,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category_tenant (category, tenant_id)
) ENGINE=InnoDB COMMENT='测试数据集';

-- =============================================
-- 沙箱执行日志表
-- =============================================
CREATE TABLE sandbox_execution_log (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    data_set_id     BIGINT       NOT NULL COMMENT '数据集ID',
    case_id         VARCHAR(64)  NOT NULL COMMENT '测试用例ID',
    status          VARCHAR(16)  NOT NULL COMMENT 'RUNNING/SUCCESS/FAILED/ERROR',
    actual_result   JSON         DEFAULT NULL COMMENT '实际结果',
    expected_result JSON         DEFAULT NULL COMMENT '预期结果',
    diff_result     JSON         DEFAULT NULL COMMENT '差异详情',
    execute_ms      INT          DEFAULT NULL COMMENT '执行耗时(ms)',
    error_message   TEXT         DEFAULT NULL COMMENT '错误信息',
    execute_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id       VARCHAR(64)  NOT NULL DEFAULT '',
    PRIMARY KEY (id),
    KEY idx_dataset_case (data_set_id, case_id),
    KEY idx_tenant_time (tenant_id, execute_time)
) ENGINE=InnoDB COMMENT='沙箱执行日志';

-- =============================================
-- 规则模板表
-- =============================================
CREATE TABLE rule_template (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    template_key         VARCHAR(128)  NOT NULL COMMENT '模板唯一标识',
    template_name        VARCHAR(128)  NOT NULL COMMENT '模板名称',
    description          VARCHAR(512)  DEFAULT NULL COMMENT '模板描述',
    category             VARCHAR(32)   NOT NULL COMMENT '分类',
    tags                 JSON          DEFAULT NULL COMMENT '标签数组',
    content              JSON          NOT NULL COMMENT '规则内容',
    provider_tenant_id    VARCHAR(64)  NOT NULL COMMENT '提供者租户ID',
    provider_tenant_name VARCHAR(128) DEFAULT NULL COMMENT '提供者租户名称',
    published_by         VARCHAR(64)   DEFAULT NULL COMMENT '发布人',
    published_at         DATETIME      DEFAULT NULL COMMENT '发布时间',
    version              VARCHAR(16)   NOT NULL DEFAULT '1.0.0' COMMENT '版本号',
    install_count        INT           NOT NULL DEFAULT 0 COMMENT '安装次数',
    avg_rating           DECIMAL(3,2)  DEFAULT NULL COMMENT '平均评分',
    comment_count        INT           NOT NULL DEFAULT 0 COMMENT '评论数',
    status               VARCHAR(16)   NOT NULL DEFAULT 'PUBLISHED' COMMENT 'PUBLISHED/ARCHIVED',
    tenant_id            VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '租户ID',
    create_time          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_template_key (template_key),
    KEY idx_category_status (category, status),
    KEY idx_provider_tenant (provider_tenant_id)
) ENGINE=InnoDB COMMENT='规则模板';

-- =============================================
-- 模板订阅表
-- =============================================
CREATE TABLE template_subscription (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    template_id      BIGINT       NOT NULL COMMENT '模板ID',
    subscriber_tenant_id VARCHAR(64) NOT NULL COMMENT '订阅者租户ID',
    installed_version VARCHAR(16)  NOT NULL COMMENT '安装版本',
    install_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '安装时间',
    status           VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/UPDATED/UNSUBSCRIBED',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_template_subscriber (template_id, subscriber_tenant_id),
    KEY idx_subscriber_tenant (subscriber_tenant_id)
) ENGINE=InnoDB COMMENT='模板订阅';

-- =============================================
-- 模板评论表
-- =============================================
CREATE TABLE template_comment (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    template_id     BIGINT       NOT NULL COMMENT '模板ID',
    tenant_id       VARCHAR(64)  NOT NULL COMMENT '评论者租户ID',
    tenant_name     VARCHAR(128) DEFAULT NULL COMMENT '评论者租户名称',
    rating          TINYINT      NOT NULL COMMENT '评分1-5',
    comment         VARCHAR(1024) DEFAULT NULL COMMENT '评论内容',
    reply_to        BIGINT       DEFAULT NULL COMMENT '回复的评论ID',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_template_id (template_id)
) ENGINE=InnoDB COMMENT='模板评论';

-- =============================================
-- 监控告警规则表
-- =============================================
CREATE TABLE monitor_alert_rule (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    rule_name        VARCHAR(128)  NOT NULL COMMENT '规则名称',
    metric_name      VARCHAR(64)   NOT NULL COMMENT '指标名称',
    condition        VARCHAR(32)   NOT NULL COMMENT '条件: >, <, >=, <=, =',
    threshold        DECIMAL(10,2) NOT NULL COMMENT '阈值',
    duration         INT           NOT NULL DEFAULT 60 COMMENT '持续时间(秒)',
    notification_type VARCHAR(32)  NOT NULL COMMENT '通知方式: email/dingtalk',
    notification_addr VARCHAR(256) DEFAULT NULL COMMENT '通知地址',
    enabled          TINYINT       NOT NULL DEFAULT 1 COMMENT '是否启用',
    already_triggered TINYINT      NOT NULL DEFAULT 0 COMMENT '当前是否已触发',
    tenant_id        VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '租户ID',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_enabled_tenant (enabled, tenant_id)
) ENGINE=InnoDB COMMENT='监控告警规则';

-- =============================================
-- 告警触发记录表
-- =============================================
CREATE TABLE monitor_alert_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    rule_id         BIGINT       NOT NULL COMMENT '告警规则ID',
    trigger_value   DECIMAL(10,2) NOT NULL COMMENT '触发时的指标值',
    threshold       DECIMAL(10,2) NOT NULL COMMENT '告警阈值',
    notified_at     DATETIME      NOT NULL COMMENT '通知发送时间',
    notification_result VARCHAR(256) DEFAULT NULL COMMENT '通知结果',
    tenant_id       VARCHAR(64)  NOT NULL DEFAULT '',
    PRIMARY KEY (id),
    KEY idx_rule_trigger_time (rule_id, trigger_value, notified_at)
) ENGINE=InnoDB COMMENT='告警触发记录';
```

---

## 7. 前端技术方案

### 7.1 技术栈

| 模块 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 规则可视化编排器 | React | 18.x | 主流前端框架 |
| 规则可视化编排器 | AntV X6 | 2.x | 流程图编辑器 |
| 监控大屏 | Vue3 | 3.x | 监控场景Vue更合适 |
| 监控大屏 | ECharts | 5.x | 图表库 |
| 监控大屏 | Pinia | - | 状态管理 |
| 规则市场/测试沙箱 | React | 18.x | 复用技术栈 |
| UI组件库 | Ant Design | 5.x | 配套React |
| UI组件库 | Element Plus | - | 配套Vue |

### 7.2 规则流编辑器组件设计

```tsx
// 核心组件
import { Canvas } from '@antv/x6-react-components';

// 节点组件
const ConditionNode = ({ data }) => (
  <div className="flow-node condition-node">
    <div className="node-header">{data.label}</div>
    <div className="node-body">
      <code>{data.expression}</code>
    </div>
  </div>
);

const ActionNode = ({ data }) => (
  <div className="flow-node action-node">
    <div className="node-header">{data.label}</div>
    <div className="node-body">
      <span>规则: {data.ruleKey}</span>
    </div>
  </div>
);

// 注册节点
Graph.registerComponent('condition-node', ConditionNode);
Graph.registerComponent('action-node', ActionNode);

// 画布组件
export const FlowEditor = () => {
  const [graph, setGraph] = useState(null);

  const initGraph = (data) => {
    const g = new Graph({
      container: containerRef.current,
      data: data.nodes,
      edges: data.edges,
    });
    setGraph(g);
  };

  return (
    <div className="flow-editor">
      <div className="node-palette">
        {/* 节点拖拽面板 */}
      </div>
      <div className="canvas-container">
        <Canvas />
      </div>
      <div className="property-panel">
        {/* 选中节点属性配置 */}
      </div>
    </div>
  );
};
```

---

## 8. 部署架构

### 8.1 新增服务配置

```yaml
# docker-compose.yml 新增服务

services:
  # ... 现有服务 ...

  # ===== 监控服务 (9007) =====
  his-monitor-service:
    build:
      context: ..
      dockerfile: docker/Dockerfile
      args:
        SERVICE_NAME: his-monitor-service
    container_name: his-monitor-service
    ports:
      - "9007:9007"
    environment:
      <<: *common-env
      SERVER_PORT: 9007
    volumes:
      - ./logs/monitor:/app/logs
    <<: *common-config
    depends_on:
      - redis

  # ===== 规则市场服务 (9008) =====
  his-market-service:
    build:
      context: ..
      dockerfile: docker/Dockerfile
      args:
        SERVICE_NAME: his-market-service
    container_name: his-market-service
    ports:
      - "9008:9008"
    environment:
      <<: *common-env
      SERVER_PORT: 9008
    volumes:
      - ./logs/market:/app/logs
    <<: *common-config

  # ===== Prometheus =====
  prometheus:
    image: prom/prometheus:v2.47.0
    container_name: his-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    networks:
      - his-net

volumes:
  prometheus-data:

networks:
  his-net:
    driver: bridge
```

### 8.2 Prometheus 配置

```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'his-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['his-gateway:9000']

  - job_name: 'his-monitor-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['his-monitor-service:9007']

  - job_name: 'his-rule-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['his-rule-service:9001']
```

---

## 9. 里程碑与工期

| 阶段 | 开始时间 | 完成时间 | 主要交付 |
|------|---------|---------|---------|
| Phase 1 | +1周 | +3周 | 规则可视化编排器 (前端+后端+执行引擎) |
| Phase 2 | +3周 | +5周 | 测试沙箱 (环境隔离+执行器+报告生成) |
| Phase 3 | +5周 | +6周 | 监控大屏 (Prometheus埋点+实时推送+看板) |
| Phase 4 | +6周 | +7周 | 规则市场 (发布/订阅/评分) |
| Phase 5 | +7周 | +8周 | 增强功能 (回滚/导入导出/历史查询) |

**预计总工期: 8周**

---

## 10. 实现状态 (2026-05-19)

### 10.1 核心组件实现

| 组件 | 状态 | 说明 |
|------|------|------|
| RuleFlowEngine | ✅ 完成 | 拓扑排序执行、子流程递归 |
| SandboxExecutor | ✅ 完成 | 测试用例执行、批量测试 |
| MonitorService | ✅ 完成 | Prometheus指标采集 |
| AlertRuleService | ✅ 完成 | 告警规则引擎、钉钉通知 |
| TemplatePublishService | ✅ 完成 | 模板发布、安全扫描 |
| TemplateSubscribeService | ✅ 完成 | 订阅安装 |
| TemplateSecurityScanner | ✅ 完成 | DRL/公式安全扫描 |

### 10.2 前端组件实现

| 组件 | 状态 | 技术栈 |
|------|------|--------|
| FlowEditor | ✅ 完成 | Vue3 + AntV X6 |
| FlowList | ✅ 完成 | Vue3 |
| FlowHistory | ✅ 完成 | Vue3 |
| SandboxPage | ✅ 完成 | Vue3 |
| DashboardPage | ✅ 完成 | Vue3 + ECharts |
| MarketPage | ✅ 完成 | Vue3 |

### 10.3 数据库表

| 表名 | 状态 |
|------|------|
| rule_flow | ✅ 已创建 |
| rule_flow_history | ✅ 已创建 |
| rule_flow_node | ✅ 已创建 |
| rule_flow_edge | ✅ 已创建 |
| test_data_set | ✅ 已创建 |
| rule_template | ✅ 已创建 |
| template_subscription | ✅ 已创建 |
| monitor_alert_rule | ✅ 已创建 |

---

*文档结束*