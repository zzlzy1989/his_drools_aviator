# skills 模块概览

## 职责
Skill 定义管理和 Agent 调度执行，实现插件化的能力扩展。

## 核心模型
- **SkillDefinition**: Skill 定义（skill_name/input_schema/output_schema/timeout/handler_class）
- **AgentDefinition**: Agent 定义（agent_name/skill_chain/max_steps/retry_policy）
- **SkillExecution**: Skill 执行记录（skill_id/status/input/output/error/duration_ms）
- **AgentExecutionLog**: Agent 决策日志（step_no/skill_called/input/output/decision）

## API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST | `/api/v1/skills` | Skill 列表/注册 |
| GET/PUT/DELETE | `/api/v1/skills/{id}` | Skill CRUD |
| POST | `/api/v1/skills/{id}/execute` | 手动执行 Skill |
| GET/POST | `/api/v1/agents` | Agent 列表/定义 |
| POST | `/api/v1/agents/{id}/run` | 触发 Agent 执行 |
| GET | `/api/v1/agents/{id}/executions/{execId}` | Agent 执行详情 |
| GET | `/api/v1/skill-executions` | 执行记录列表 |

## 关键规则
- 必须定义 input/output schema（BR-A01）
- 决策链路必须可追溯（BR-A02）
- 默认超时 30s，最大 300s（BR-A03）
- 循环调用深度 ≤ 5 层（BR-A04）
- 状态机: queued → running → success/failed/timeout（SM-04）

## 内置 Skill 列表
| Skill 名称 | 用途 | 超时 |
|-----------|------|------|
| `settlement.calculate` | 执行结算计算 | 30s |
| `rule.match` | Drools 规则匹配 | 10s |
| `formula.batch-calc` | 批量公式计算 | 15s |
| `drug.interaction-check` | 药物相互作用检测 | 20s |
| `audit.anomaly-detect` | 异常值检测 | 10s |
| `report.generate` | 结算报告生成 | 15s |

## 关联模块
← tenants (租户隔离)
→ settlements (结算 Skill 调用)
→ rules (规则匹配 Skill)
→ formulas (公式计算 Skill)

## 核心服务类
- `SkillRegistry`: Skill 注册中心（启动时扫描 @Skill 注解）
- `AgentOrchestrator`: Agent 编排器（决策链调度）
- `SkillExecutor`: Skill 执行器（线程池 + 超时控制）
- `ExecutionTraceLogger`: 链路日志记录器

## 配置示例
```yaml
skill:
  executor:
    core-pool-size: 10
    max-pool-size: 50
    queue-capacity: 200
  agent:
    max-steps: 100
    max-depth: 5
    retry:
      max-attempts: 3
      strategy: exponential_backoff
      initial-interval-ms: 1000
```
