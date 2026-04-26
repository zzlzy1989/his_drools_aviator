# 性能优化规范 - HIS 动态规则中台

## 触发条件
- 发现性能瓶颈
- 新功能开发（预防性优化）
- 代码审查中的性能问题
- 生产环境性能调优

---

## 分层优化策略

```
┌─────────────────────────────────────┐
│ Layer 1: 架构层 (影响最大)          │ ← 优先处理
│   缓存策略 / 异步处理 / 规则预编译   │
├─────────────────────────────────────┤
│ Layer 2: 引擎层 (核心影响)          │
│   Drools ReteOO优化 / Aviator缓存   │
├─────────────────────────────────────┤
│ Layer 3: 查询层 (高频影响)          │
│   索引优化 / 分页 / MyBatis批处理   │
├─────────────────────────────────────┤
│ Layer 4: 代码层 (局部影响)          │
│   算法优化 / BigDecimal池化 / 并发  │
└─────────────────────────────────────┘
```

---

## 一、Drools 规则引擎性能

### 1.1 KIE Base 编译优化

| 场景 | 问题 | 优化方案 |
|------|------|---------|
| 首次启动慢 | DRL 编译耗时 | 预编译 + 序列化缓存（KieBaseModel） |
| 规则多时内存高 | ReteOO 网络大 | 按 RuleUnit 拆分，按需加载 |
| 热更新卡顿 | 重建 KIE Base | 双缓冲切换（新旧 Session 并存） |

### 1.2 规则编写性能要点

```java
// ✅ 正确：使用 Phreak 算法（Drools 8 默认，更高效）
KieServices ks = KieServices.Factory.get();
KieContainer kc = ks.newKieClasspathContainer();

// ✅ 正确：Rule Unit 按模块隔离（减少单次匹配规模）
public enum RuleGroup {
    REIMBURSEMENT("reimbursement.drl"),
    DRUG_CHECK("drug_check.drl"),
    INFECTION_CONTROL("infection_control.drl");
    
    private final String drlFile;
}

// ❌ 错误：在 RHS 中做重量级操作
then
    // 不要在这里查数据库或调外部API
    List<Drug> list = drugRepo.findAll();  // 严重性能问题！
end

// ✅ 正确：RHS 只设置 Fact 字段，复杂逻辑走 Service
then
    $f.setNeedDrugCheck(true);
    update($f);  // 触发后续规则匹配
end
```

### 1.3 Session 复用 vs 每次新建

| 方案 | 适用场景 | 性能影响 |
|------|---------|---------|
| StatelessKieSession | 无状态的纯函数式规则（推荐） | ✅ 最高，无状态维护开销 |
| StatefulKieSession | 需要多步推理/累积事实 | ⚠️ 中等，需要 dispose |
| Session Pool | 高并发短请求 | ✅ 好，避免反复创建 |

```java
// 推荐: StatelessKieSession（无状态，线程安全，高性能）
@Component
public class RuleExecutionService {

    private final StatelessKieSession session;

    @PostConstruct
    public void init() {
        this.session = kieContainer.newStatelessKieSession();
    }

    public List<Object> execute(Fact fact) {
        List<Object> results = new ArrayList<>();
        session.setGlobal("results", results);
        session.execute(fact);  // 线程安全，无状态
        return results;
    }
}
```

---

## 二、Aviator 表达式引擎性能

### 2.1 编译缓存（最关键优化）

```java
// ✅ 正确: Caffeine 缓存编译后的 Expression 对象
@Component
public class AviatorExpressionCache {

    private final Cache<String, Expression> cache = Caffeine.newBuilder()
        .maximumSize(5000)                    // 最多缓存 5000 个表达式
        .expireAfterAccess(30, TimeUnit.MINUTES)  // 30 分钟未访问淘汰
        .build();

    public Expression getOrCompile(String expression) {
        return cache.get(expression, key -> {
            long start = System.nanoTime();
            Expression expr = AviatorCompiler.compile(key);
            long cost = System.nanoTime() - start;
            if (cost > 1_000_000L) {  // > 1ms
                log.warn("Aviator编译耗时: {}ms, expr={}", cost / 1_000_000L, key.substring(0, Math.min(50, key.length())));
            }
            return expr;
        });
    }
}
```

### 2.2 性能对比

| 操作 | 无缓存 | 有 Caffeine 缓存 | 提升 |
|------|--------|-----------------|------|
| 编译简单表达式 | ~2ms | ~0.01ms (命中) | **200x** |
| 编译复杂表达式 | ~10ms | ~0.01ms (命中) | **1000x** |
| 执行简单表达式 | ~0.01ms | ~0.01ms | 持平 |
| 执行复杂表达式 | ~0.1ms | ~0.1ms | 持平 |

### 2.3 BigDecimal 性能注意点

```java
// ❌ 错误: 每次计算都 new BigDecimal
BigDecimal result = new BigDecimal(a).multiply(new BigDecimal(b));

// ✅ 正确: 使用 Aviator 的 BigDecimal 模式（自动复用）
AviatorEvaluator.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_BIGDECIMAL);

// ✅ 正确: 高频计算的中间值用变量缓存
String formula = "let a = total - deductible; let b = a * ratio; round(b, 2)";
```

### 2.4 表达式设计性能建议

| 建议 | 原因 |
|------|------|
| 避免深层嵌套三元表达式 | 降低编译和执行复杂度 |
| 使用 `let` 定义中间变量 | 减少重复计算 |
| 避免在循环中频繁编译 | 预编译 + 缓存 |
| 控制表达式长度 < 500 字符 | 过长表达式难以优化 |

---

## 三、Caffeine 缓存策略

### 3.1 缓存层级（从快到慢）

| 层级 | 技术 | 适用场景 | TTL 建议 |
|------|------|---------|---------|
| L1 进程内 | Caffeine (Expression 编译) | 同一 JVM 内复用 | 30min |
| L1 进程内 | Caffeine (规则数据) | 规则定义/租户配置 | 5min |
| L2 分布式 | Redis (可选) | 跨实例共享缓存 | 10min ~ 1h |
| L3 数据库 | MySQL | 持久化源数据 | — |

### 3.2 缓存 Key 命名规范

```
{领域}:{实体}:{标识}:{变体}:{租户}

示例:
aviator:expr:hash123456:               # Aviator 表达式编译缓存
rule:def:rule.reimburse.resident:T001   # 规则定义缓存(含租户)
formula:text:formula.reimburse.basic:T001 # 公式文本缓存
config:settlement:param:T001             # 结算参数缓存
tenant:quota:T001                        # 租户配额缓存
```

### 3.3 缓存失效策略

```java
// ✅ 正确: 配置变更时主动刷新相关缓存
@EventListener
public void onConfigChange(ConfigChangeEvent event) {
    String changedKey = event.getKey();
    
    if (changedKey.startsWith("rules.drl.")) {
        // 规则变更 → 刷新规则缓存 + 重建 KIE Base
        ruleCache.invalidateAll();
        kieContainerManager.rebuildKieBase();
    } else if (changedKey.startsWith("formulas.aviator.")) {
        // 公式变更 → 刷新表达式编译缓存
        expressionCache.invalidateAll();
    }
    
    log.info("配置变更触发缓存刷新: key={}", changedKey);
}

// ✅ 正确: 写时失效（Cache-Aside Pattern）
public void updateFormula(FormulaUpdateDTO dto) {
    formulaRepository.update(dto);
    expressionCache.invalidate(dto.getFormulaKey());  // 主动失效
}
```

### 3.4 缓存禁忌

```java
// ❌ 缓存大量数据（单个 value > 1MB）
cache.put("huge_rule_set", largeDrlText);

// ❌ 缓存敏感信息
cache.put("patient:" + id, patientWithSensitiveData);

// ❌ 无过期时间的永久缓存（配置变更后不生效）
cache.put("formula:fixed", expr, /* no expiry */);

// ✅ 正确: 合理 TTL + 主动失效 + 监控命中率
cache.put(key, expr, expireAfterWrite(30, MINUTES));
```

---

## 四、数据库查询性能

### 4.1 慢查询阈值

| 操作类型 | 警告阈值 | 阻塞阈值 |
|---------|---------|---------|
| 单次 SELECT | > 50ms | > 500ms |
| 规则批量加载 | > 200ms | > 2s |
| 结算写入（含明细） | > 300ms | > 3s |
| COUNT(*) | > 100ms | > 1s |
| JOIN 查询 | > 100ms | > 1s |

### 4.2 强制索引规范

```sql
-- ✅ 正确: 所有 WHERE 条件字段必须有索引
CREATE INDEX idx_rule_tenant_status ON rule_definition(tenant_id, status);
CREATE INDEX idx_formula_key_tenant ON formula(formula_key, tenant_id);
CREATE INDEX idx_settlement_visit ON settlement_record(visit_id);
CREATE INDEX idx_skill_exec_status ON skill_execution(status, created_at);

-- ✅ 正则: 分页必须走索引
SELECT * FROM rule_definition 
WHERE tenant_id = 'T001' AND status = 'ACTIVE'
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;  -- created_at 上必须有索引
```

### 4.3 MyBatis 批量操作

```java
// ✅ 正确: 批量插入使用 batch mode
SqlSession batchSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
try {
    RuleDefinitionMapper mapper = batchSession.getMapper(RuleDefinitionMapper.class);
    for (RuleDefinition rule : rules) {
        mapper.insert(rule);
    }
    batchSession.commit();  // 一次性提交
} finally {
    batchSession.close();
}
```

---

## 五、并发控制

### 5.1 结算执行并发模型

```
客户端请求
    ↓
Semaphore(permits=50)  ← 并发上限控制
    ↓
ReentrantLock(visitId)  ← 同一就诊互斥（防止重复结算）
    ↓
规则匹配(Aviator公式计算)
    ↓
释放锁 → 返回结果
```

### 5.2 关键并发参数

| 参数 | 默认值 | 说明 | 调优方向 |
|------|--------|------|---------|
| `settlement.concurrent.max` | 50 | 最大并发结算数 | ↑ 服务器好可提高 |
| `skill.executor.core-pool` | 10 | Skill 执行核心线程数 | 按 CPU 核心数 × 2 |
| `skill.executor.max-pool` | 50 | 最大线程数 | 峰值支撑 |
| `kie.session.pool-size` | 10 | KIE Session 池大小 | 与并发匹配 |
| `aviator.compile.parallelism` | 4 | 并行编译线程数 | CPU 密集型不宜过大 |

### 5.3 幂等性保障

```java
// ✅ 正理: 同一就诊防重复结算
public SettlementResult settle(String visitId) {
    // 分布式锁（Redisson 或 DB 唯一索引）
    RLock lock = redissonClient.getLock("settle:" + visitId);
    if (!lock.tryLock(5, 30, TimeUnit.SECONDS)) {
        throw new HisException("HIS-203", "该就诊正在结算中，请勿重复提交");
    }
    try {
        // 幂等检查
        SettlementRecord existing = settlementRepo.findActiveByVisitId(visitId);
        if (existing != null) {
            return SettlementResult.from(existing);  // 直接返回已有结果
        }
        // 执行结算...
    } finally {
        lock.unlock();
    }
}
```

---

## 六、性能监控指标

| 指标 | 目标值 | 告警阈值 | 监控方式 |
|------|-------|---------|---------|
| 规则匹配 P95 延迟 | < 50ms | > 200ms | Micrometer + Prometheus |
| 公式计算 P95 延迟 | < 10ms | > 50ms | Micrometer |
| 结算全流程 P99 | < 2s | > 5s | 自定义埋点 |
| Aviator 编译缓存命中率 | > 95% | < 90% | Caffeine stats() |
| KIE Base 切换时间 | < 3s | > 10s | 自定义计时 |
| 规则热更新频率 | < 1次/min | > 5次/min | Nacos 监控 |
| JVM GC Pause (G1) | < 100ms | > 500ms | JMX / Grafana |
| 数据库连接池活跃率 | < 70% | > 90% | HikariCP metrics |
| 线程池排队率 | < 10% | > 50% | ThreadPool metrics |

---

## 七、JVM 调优参考

### 7.1 G1GC 推荐参数（规则引擎场景）

```bash
-Xms1g -Xmx2g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200       # 低停顿（结算请求不能长时间 STW）
-XX:G1HeapRegionSize=16m
-XX:InitiatingHeapOccupancyPercent=45  # 提前开始 Mixed GC
-XX:+UnlockExperimentalVMOptions
-XX:G1ReservePercent=15
```

### 7.2 内存分配估算

| 区域 | 估算大小 | 说明 |
|------|---------|------|
| Heap (规则对象) | 512M ~ 1G | Fact + Session + 结果 |
| Metaspace (DRL编译) | 128M ~ 256M | KIE Base 类定义 |
| Caffeine Cache | 100M ~ 200M | 编译后 Expression 对象 |
| Thread Stack | N × 1MB | 并发线程栈 |
| Direct Buffer | 32M | NIO/网络缓冲 |

---

最后更新: 2026-04-26 | v1.0 (HIS Drools+Aviator 规则引擎专用)
