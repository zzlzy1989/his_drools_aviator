---
alwaysApply: true
---
# drug 模块概览 - HIS 合理用药审核服务

> 最后更新: 2026-05-10 | v1.0

## 职责
合理用药审核，对医生处方进行药物相互作用、极量、过敏等合理性检测。

## 核心模型
- **DrugCatalog**: 药品目录（药品编码/名称/规格/单价/目录类别）
- **DrugInteraction**: 药物相互作用（药品A/药品B/交互级别/说明）
- **PatientAllergy**: 患者过敏史（患者ID/过敏药物/严重度）

## API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/drugs/check` | 处方用药审核 |
| GET | `/api/v1/drugs/catalog` | 药品目录列表 |
| GET | `/api/v1/drugs/catalog/{id}` | 药品详情 |
| GET | `/api/v1/drugs/interactions` | 配伍禁忌列表 |

## 关键规则
- 处方审核时必须校验药品是否在目录内（BR-DR01）
- 检测到配伍禁忌时必须返回 WARN 或 BLOCK（BR-DR02）
- 特殊人群用药必须触发额外规则校验（BR-DR03）

## 审核流程
```
1. 接收处方请求（药品列表 + 诊断 + 患者信息）
2. 药品目录校验（是否在目录内/是否停产）
3. 配伍禁忌检测（Drools 规则匹配）
4. 用药极量检查（单次/日剂量是否超限）
5. 过敏史检查（患者是否有相关过敏药物）
6. 特殊人群检查（孕妇/儿童/老人用药限制）
7. 返回审核结果（PASS/WARN/BLOCK + 详细说明）
```

## 关联模块
← settlements (结算前前置审核)
← tenants (租户隔离)

## 核心服务类
- `DrugCheckService`: 用药审核核心服务
- `DrugController`: REST 控制器

## 数据结构示例
```json
{
  "prescriptionId": "RX20260426001",
  "drugs": [
    {"drugCode": "D001", "dosage": "500mg", "frequency": "tid"},
    {"drugCode": "D002", "dosage": "250mg", "frequency": "bid"}
  ],
  "result": {
    "level": "WARN",
    "messages": [
      "D001与D002存在轻度配伍禁忌，建议间隔2小时使用"
    ]
  }
}
```

## 文件位置
- 后端: `his-drug-service/src/main/java/com/his/drug/`
- 控制器: `controller/DrugController`
- 服务: `service/DrugCheckService`
- 实体: `entity/DrugCatalog`, `entity/DrugInteraction`, `entity/PatientAllergy`
