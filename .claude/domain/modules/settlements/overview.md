# settlements 模块概览

## 职责
医保结算流程编排，协调 Drools 规则匹配和 Aviator 公式计算。

## 核心模型
- **SettlementRecord**: 结算记录主表（visit_id/patient_id/status/total_amount/pay_amount等）
- **SettlementDetail**: 结算明细（费用项目/规则匹配结果/各段金额）
- **SettlementAuditLog**: 审核日志（审核人/审核时间/审核意见/操作类型）

## API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/settlements` | 发起结算请求 |
| GET | `/api/v1/settlements/{id}` | 结算详情（含明细） |
| POST | `/api/v1/settlements/{id}/review` | 提交审核 |
| POST | `/api/v1/settlements/{id}/approve` | 审核通过 |
| POST | `/api/v1/settlements/{id}/reject` | 审核退回 |
| POST | `/api/v1/settlements/{id}/recalculate` | 重新结算 |
| GET | `/api/v1/settlements` | 结算记录列表（分页筛选） |
| GET | `/api/v1/settlements/{id}/timeline` | 操作时间线 |

## 关键规则
- 只使用 active 状态的规则和公式（BR-S01）
- 结果不可修改，只可重新结算（BR-S02）
- 状态机: pending → matching → calculating → reviewing → completed（SM-03）
- 就诊 ID 唯一性约束（BR-S04）
- 失败支持有限次重试（默认 3 次）

## 结算流程
```
1. 接收结算请求（就诊ID + 费用清单）
2. 加载患者信息 + 医保身份
3. Drools 规则匹配 → 输出适用规则集（报销比例/起付线/封顶线）
4. Aviator 公式计算 → 逐步计算各项金额
5. 结果合理性校验（范围检查/比例检查）
6. 进入审核队列（或自动审核模式）
7. 审核通过 → 生成结算单
```

## 关联模块
← rules (规则匹配源)
← formulas (公式计算源)
← tenants (租户上下文)
→ skills (可触发 Agent 辅助审核)

## 核心服务类
- `SettlementService`: 结算流程编排
- `RuleMatchingService`: Drools 规则匹配执行
- `FormulaCalculationService`: Aviator 公式批量计算
- `SettlementValidator`: 结果合理性校验器

## 数据结构示例
```json
{
  "settlementId": "ST202604260001",
  "patientId": "P10086",
  "visitId": "V20260426001",
  "status": "completed",
  "amounts": {
    "totalFee": 10000.00,
    "deductible": 1000.00,
    "selfPay": 1500.00,
    "reimburseAmount": 7500.00,
    "personalPay": 2500.00
  },
  "rulesApplied": ["rule.resident.basic.2026", "rule.age.adjust"],
  "formulasUsed": ["reimburse.resident.basic"]
}
```
