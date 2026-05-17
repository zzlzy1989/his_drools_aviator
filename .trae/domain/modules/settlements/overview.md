---
alwaysApply: true
---
# settlements 模块概览 - HIS 医保结算

> 最后更新: 2026-05-10 | v1.1

## 职责
医保结算流程编排，协调 Drools 规则匹配、Aviator 公式计算和 Skill 管道执行。

## 核心模型
- **SettlementResult**: 结算结果（visit_id/patient_id/status/total_fee/reimburse_amount 等）
- **FormulaEntity**: 公式实体（ID/文本/分类/版本）
- **Skill 内置**: InsuranceIdentitySkill, DeductibleSkill, ReimburseRatioSkill

## API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/settlements` | 发起结算请求 |
| GET | `/api/v1/settlements/{id}` | 结算详情（含明细） |
| POST | `/api/v1/settlements/{id}/recalculate` | 重新结算 |
| GET | `/api/v1/settlements` | 结算记录列表（分页筛选） |

## 关键规则
- 只使用 active 状态的规则和公式（BR-S01）
- 结果不可修改，只可重新结算（BR-S02）
- 就诊 ID 唯一性约束（BR-S04）
- Skill Pipeline 按 getOrder() 升序执行，BLOCK 立即终止（BR-S05）

## 结算流程（Skill Pipeline 架构）
```
1. 接收结算请求（患者信息 + 费用清单）
2. 加载 Fact 对象（SettlementFact）
3. 执行 Skill Pipeline（按优先级顺序）：
   3.1 InsuranceIdentitySkill → 校验医保身份
   3.2 DeductibleSkill → 计算起付线
   3.3 ReimburseRatioSkill → 确定报销比例
   3.4 后续 Skill 按需添加
4. 各 Skill 内部调用 Drools + Aviator 计算金额
5. 汇总各 Skill 结果，生成结算记录
```

## 关联模块
← rules (规则匹配源)
← formulas (公式计算源)
← tenants (租户上下文)
← skills (Skill 实现)

## 核心服务类
- `SettlementService`: 结算入口服务，封装 SkillPipelineExecutor
- `SkillPipelineExecutor`: Skill 管道执行器（按序串联执行）
- `FormulaLoaderService`: 从数据库加载公式实体

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
  "rulesApplied": ["rule.resident.basic.2026"],
  "formulasUsed": ["formula.reimburse.resident.basic"]
}
```

## 文件位置
- 后端: `his-settlement-service/src/main/java/com/his/settlement/`
- 控制器: `controller/SettlementController`
- 服务: `service/SettlementService`, `service/FormulaLoaderService`
- 管道: `pipeline/SkillPipelineExecutor`
- Skill: `skill/InsuranceIdentitySkill`, `skill/DeductibleSkill`, `skill/ReimburseRatioSkill`
