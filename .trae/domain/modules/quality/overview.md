# quality 模块概览 - HIS 医疗质控服务

> 最后更新: 2026-05-10 | v1.0

## 职责
医疗质量控制指标检测，包括抗菌药物使用率、处方合格率等质控指标统计与预警。

## 核心模型
- **QualityDefinition**: 质控定义（指标编码/名称/阈值/检测周期/检测规则）
- **QualityCheckDTO**: 质控检测请求（指标编码/统计周期/科室范围）
- **QualityCheckVO**: 质控检测结果（当前值/阈值/是否超标）

## API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/quality/check` | 执行质控检测 |
| GET | `/api/v1/quality/definitions` | 质控指标列表 |
| GET | `/api/v1/quality/definitions/{id}` | 质控指标详情 |
| POST | `/api/v1/quality/definitions` | 创建质控指标 |
| PUT | `/api/v1/quality/definitions/{id}` | 更新质控指标 |
| GET | `/api/v1/quality/history` | 历史检测记录 |

## 关键规则
- 抗菌药物使用率超过阈值时触发质控预警（BR-Q01）
- 质控检测周期支持按日/周/月统计（BR-Q02）

## 检测流程
```
1. 接收检测请求（指标编码 + 统计周期 + 范围）
2. 查询该周期内的相关数据
3. 按质控定义中的计算公式统计当前值
4. 与阈值比较判定是否超标
5. 返回检测结果（达标/预警/超标 + 详细数据）
```

## 关联模块
← tenants (租户隔离)
→ rules (部分质控指标通过 Drools 规则检测)

## 核心服务类
- `QualityCheckService`: 质控检测核心服务
- `QualityController`: REST 控制器

## 常用质控指标
| 指标编码 | 指标名称 | 计算方式 | 阈值 |
|---------|---------|---------|------|
| QC-AMR-001 | 门诊抗菌药物使用率 | 抗菌处方数/总处方数×100% | ≤20% |
| QC-AMR-002 | 住院抗菌药物使用率 | 抗菌医嘱数/总医嘱数×100% | ≤60% |
| QC-RX-001 | 处方合格率 | 合格处方数/总处方数×100% | ≥95% |
| QC-INJ-001 | 注射剂使用率 | 注射处方数/总处方数×100% | ≤15% |

## 文件位置
- 后端: `his-quality-service/src/main/java/com/his/quality/`
- 控制器: `controller/QualityController`
- 服务: `service/QualityCheckService`
- 实体: `entity/QualityDefinition`
