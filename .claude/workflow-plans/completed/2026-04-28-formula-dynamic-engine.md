---
title: "公式动态引擎集成与文档"
type: "feature"
status: "completed"
created_at: "2026-04-28 23:25:00"
updated_at: "2026-04-28 23:45:00"
completed_at: "2026-04-28 23:45:00"
phase: "Phase 1"
owner: "developer"
reviewer: ""
priority: "P1"
tags: ["公式引擎", "Aviator", "热更新", "文档"]
related_files:
  - "his-settlement-service/src/main/java/com/his/settlement/service/FormulaLoaderService.java"
  - "his-settlement-service/src/main/java/com/his/settlement/entity/FormulaEntity.java"
  - "his-settlement-service/src/main/java/com/his/settlement/mapper/FormulaEntityMapper.java"
  - "his-settlement-service/src/main/java/com/his/settlement/service/SettlementService.java"
  - "his-settlement-service/src/main/java/com/his/settlement/controller/SettlementController.java"
dependencies: []
---

# PLAN-20260428-001: 公式动态引擎集成与文档

> 计划 ID: PLAN-20260428-001
> 创建时间: 2026-04-28 23:25:00
> 状态: ✅ completed

---

## 1. 任务概述

### 1.1 背景
公式动态引擎是 HIS 规则中台的核心能力之一，支持业务公式热更新，无需重启服务即可调整报销计算逻辑。

### 1.2 完成项
- [x] FormulaEntity 实体类创建
- [x] FormulaEntityMapper 数据库访问层
- [x] FormulaLoaderService 公式加载器服务
- [x] SettlementService 集成动态公式
- [x] 热更新 API 接口
- [x] 数据库公式初始化
- [x] 热更新验证测试

---

## 2. 技术架构

### 2.1 核心组件

```
SettlementController
    ├── /api/v1/settlements (POST) - 发起结算
    ├── /api/v1/settlements/cache/refresh (POST) - 刷新公式缓存
    └── /api/v1/settlements/cache/clear (POST) - 清空公式缓存

SettlementService
    └── executeReimburseFormula() → FormulaLoaderService

FormulaLoaderService
    ├── executeReimburseFormula() - 执行公式计算
    ├── getFormulaText() - 带缓存的公式加载
    ├── refreshCache() - 刷新单个公式缓存
    └── clearCache() - 清空所有缓存

FormulaEntityMapper
    └── selectByKey() - 从数据库查询公式
```

### 2.2 公式缓存机制

| 特性 | 说明 |
|------|------|
| 缓存介质 | ConcurrentHashMap |
| 缓存 Key | `tenantId:formulaKey` |
| 过期时间 | 5 分钟 |
| 刷新方式 | 调用 refreshCache API |

---

## 3. API 接口文档

### 3.1 发起结算

**请求**
```http
POST /api/v1/settlements
Content-Type: application/json
X-Tenant-Id: hospital_001

{
  "patientId": "P001",
  "patientType": "employee",
  "visitId": "V001",
  "totalFee": 10000
}
```

**响应**
```json
{
  "code": "0",
  "data": {
    "id": 6,
    "settlementNo": "ST202604282320190EBC6DC2",
    "patientType": "employee",
    "totalFee": 10000,
    "deductible": 1000,
    "ratio": 0.85,
    "reimburseAmount": 7650.00,
    "selfPayAmount": 2350.00,
    "resultLevel": "PASS",
    "status": "completed"
  },
  "message": "操作成功",
  "success": true
}
```

### 3.2 刷新公式缓存

**请求**
```http
POST /api/v1/settlements/cache/refresh?tenantId=hospital_001&formulaKey=formula.reimburse.employee
X-Tenant-Id: hospital_001
```

**响应**
```json
{
  "code": "0",
  "data": null,
  "message": "操作成功",
  "success": true
}
```

### 3.3 清空公式缓存

**请求**
```http
POST /api/v1/settlements/cache/clear
X-Tenant-Id: hospital_001
```

**响应**
```json
{
  "code": "0",
  "data": null,
  "message": "操作成功",
  "success": true
}
```

---

## 4. 数据库公式配置

### 4.1 公式表 (aviator_formula)

```sql
INSERT INTO aviator_formula (formula_key, formula_name, formula_text, category, version, status, tenant_id, create_by) VALUES
('formula.reimburse.employee', '职工医保报销公式', '(totalFee - deductible) * ratio', 'REIMBURSE', 1, 'active', 'hospital_001', 'system'),
('formula.reimburse.resident', '居民医保报销公式', '(totalFee - deductible) * ratio', 'REIMBURSE', 1, 'active', 'hospital_001', 'system'),
('formula.reimburse.aid', '救助对象报销公式', '(totalFee - deductible) * ratio', 'REIMBURSE', 1, 'active', 'hospital_001', 'system');
```

### 4.2 公式格式

Aviator 表达式语法：
- 数学运算符: `+`, `-`, `*`, `/`
- 比较运算符: `>`, `<`, `>=`, `<=`, `==`, `!=`
- 条件表达式: `condition ? value1 : value2`
- 函数: `round(value, 2)` - 四舍五入保留2位

**示例公式**
```
(totalFee - deductible) * ratio
(totalFee - deductible) * ratio * 1.1
totalFee > 10000 ? totalFee * 0.9 : totalFee * 0.85
```

---

## 5. 热更新操作流程

### 5.1 场景一：修改报销比例

1. **更新数据库**
```sql
UPDATE aviator_formula 
SET formula_text = '(totalFee - deductible) * ratio * 1.1' 
WHERE formula_key = 'formula.reimburse.employee';
```

2. **刷新缓存**
```bash
curl -X POST "http://localhost:9003/api/v1/settlements/cache/refresh?tenantId=hospital_001&formulaKey=formula.reimburse.employee"
```

3. **验证结果**
```bash
curl -X POST "http://localhost:9003/api/v1/settlements" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: hospital_001" \
  -d '{"patientId":"P999","patientType":"employee","visitId":"V999","totalFee":10000}'
# 期望: reimburseAmount = 8415.00 (原 7650.00)
```

### 5.2 场景二：添加新公式

1. **插入数据库**
```sql
INSERT INTO aviator_formula (formula_key, formula_name, formula_text, category, status, tenant_id) VALUES
('formula.reimburse.vip', 'VIP客户报销公式', '(totalFee - deductible) * ratio * 1.2', 'REIMBURSE', 'active', 'hospital_001');
```

2. **无需重启服务**，下次调用 `formula.reimburse.vip` 时自动加载

---

## 6. 降级策略

| 异常场景 | 处理方式 |
|---------|---------|
| 公式不存在 | 使用硬编码计算 `calculateHardcoded()` |
| Aviator 执行失败 | 使用硬编码计算 + 记录 ERROR 日志 |
| 数据库连接失败 | 使用缓存中的公式（如果有） |

**硬编码计算逻辑**
```java
if (totalFee <= deductible) {
    return ZERO;
}
return (totalFee - deductible) * ratio;
```

---

## 7. 测试验证

| 测试项 | 输入 | 预期输出 | 结果 |
|--------|------|---------|------|
| 职工医保正常 | totalFee=10000, deductible=1000, ratio=0.85 | 7650.00 | ✅ |
| 居民医保正常 | totalFee=10000, deductible=500, ratio=0.65 | 6175.00 | ✅ |
| 起付线以下 | totalFee=500, deductible=1000 | 0.00 | ✅ |
| 热更新后刷新 | 修改公式后调用 refreshCache | 新公式生效 | ✅ |

---

## 8. 相关文件清单

| 文件 | 路径 | 说明 |
|------|------|------|
| FormulaLoaderService | `.../service/FormulaLoaderService.java` | 公式加载器，核心逻辑 |
| FormulaEntity | `.../entity/FormulaEntity.java` | 公式实体类 |
| FormulaEntityMapper | `.../mapper/FormulaEntityMapper.java` | 数据库访问层 |
| SettlementService | `.../service/SettlementService.java` | 结算服务，集成公式引擎 |
| SettlementController | `.../controller/SettlementController.java` | REST API 控制器 |
| AviatorHelper | `his-common-aviator/.../AviatorHelper.java` | Aviator 执行帮助类 |
| aviator_formula | MySQL 表 | 公式存储表 |

---

## 9. Swagger/OpenAPI 文档

### 9.1 访问地址

| 环境 | 地址 |
|------|------|
| 本地 | `http://localhost:9003/swagger-ui.html` |
| API Docs | `http://localhost:9003/v3/api-docs` |

### 9.2 接口清单

| 方法 | 路径 | 说明 | 标签 |
|------|------|------|------|
| POST | `/api/v1/settlements` | 发起结算 | 结算管理 |
| POST | `/api/v1/settlements/cache/refresh` | 刷新公式缓存 | 结算管理 |
| POST | `/api/v1/settlements/cache/clear` | 清空公式缓存 | 结算管理 |
| GET | `/api/v1/settlements/page` | 分页查询结算记录 | 结算管理 |
| GET | `/api/v1/settlements/{settlementNo}` | 根据结算单号查询 | 结算管理 |

### 9.3 结算请求 DTO

```json
{
  "patientId": "string (必填)",
  "patientType": "string (必填, employee/resident/aid)",
  "visitId": "string (必填)",
  "totalFee": "number (必填)",
  "insuranceType": "string (可选)",
  "hospitalLevel": "string (可选)"
}
```

### 9.4 结算响应 VO

```json
{
  "id": "number",
  "settlementNo": "string",
  "patientId": "string",
  "patientType": "string",
  "totalFee": "number",
  "deductible": "number",
  "ratio": "number",
  "reimburseAmount": "number",
  "selfPayAmount": "number",
  "resultLevel": "string (PASS/WARN/BLOCK)",
  "status": "string (pending/completed/failed)",
  "tenantId": "string",
  "createTime": "string (ISO datetime)"
}
```

---

## 10. 进度跟踪

| 时间 | 操作 | 状态变更 |
|------|------|---------|
| 2026-04-28 23:25 | 创建计划 | in_progress |
| 2026-04-28 23:30 | FormulaEntity + FormulaEntityMapper | ✅ |
| 2026-04-28 23:35 | FormulaLoaderService 实现 | ✅ |
| 2026-04-28 23:40 | SettlementService 集成 | ✅ |
| 2026-04-28 23:42 | 数据库公式初始化 | ✅ |
| 2026-04-28 23:45 | 热更新验证通过 | ✅ |
| 2026-04-29 14:47 | 代码审查修复 (FQCN, 语法校验) | ✅ |
| 2026-04-29 14:50 | Swagger/OpenAPI 文档完成 | ✅ |

---

## 11. 完成检查清单

- [x] FormulaEntity 实体类创建
- [x] FormulaEntityMapper 数据库访问
- [x] FormulaLoaderService 公式加载器
- [x] SettlementService 集成动态公式
- [x] 热更新 API 接口添加
- [x] 数据库公式初始化
- [x] 热更新验证测试通过
- [x] API 接口文档编写
- [x] 操作文档编写
- [x] 代码审查问题修复
  - [x] FQCN 导入改为标准 import
  - [x] 添加公式执行前语法校验
- [x] Swagger/OpenAPI 集成验证

---

*计划已完成*
