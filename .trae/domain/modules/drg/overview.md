# drg 模块概览 - HIS DRG 分组服务

> 最后更新: 2026-05-10 | v1.0

## 职责
DRG（按疾病诊断相关分组）分组计算，根据诊断+手术编码将病例分入对应 DRG 组。

## 核心模型
- **DrgDefinition**: DRG 定义（组编码/权重/入组条件/MDC 分类）
- **DrgGroupingDTO**: 分组请求（诊断编码/手术编码/住院信息）
- **DrgGroupingVO**: 分组结果（DRG 组/权重/基础费用）

## API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/drg/grouping` | 执行 DRG 分组 |
| GET | `/api/v1/drg/definitions` | DRG 定义列表 |
| GET | `/api/v1/drg/definitions/{id}` | DRG 定义详情 |
| POST | `/api/v1/drg/definitions` | 创建 DRG 定义 |
| PUT | `/api/v1/drg/definitions/{id}` | 更新 DRG 定义 |

## 关键规则
- 入组必须同时校验主诊断和手术编码（BR-D01）
- DRG 权重值不可为负数（BR-D02）
- 未入组的病例使用默认费用结算规则（BR-D03）

## 分组流程
```
1. 接收分组请求（诊断编码 + 手术编码 + 住院天数等）
2. 匹配 MDC（主要诊断大类）
3. 在 MDC 下匹配 ADRG（相邻 DRG 组）
4. 根据手术编码细化到具体 DRG
5. 返回 DRG 组编码、权重、建议费用
6. 如未入组，返回未分组状态
```

## 关联模块
← settlements (结算服务调用分组结果)
← tenants (租户隔离)

## 核心服务类
- `DrgGroupService`: DRG 分组计算核心
- `DrgController`: REST 控制器

## 数据结构示例
```json
{
  "diagnosisCode": "I10",
  "procedureCode": "38.93",
  "hospitalDays": 7,
  "age": 65,
  "drgCode": "A11",
  "drgName": "高血压伴合并症",
  "weight": 1.25,
  "baseRate": 8000.00
}
```

## 文件位置
- 后端: `his-drg-service/src/main/java/com/his/drg/`
- 控制器: `controller/DrgController`
- 服务: `service/DrgGroupService`
- 实体: `entity/DrgDefinition`
