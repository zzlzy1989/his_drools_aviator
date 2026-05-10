---
title: "服务集成测试验证"
type: "test"
status: "in_progress"
created_at: "2026-05-09"
updated_at: "2026-05-10"
completed_at: null
phase: "Phase 1"
owner: "developer"
reviewer: ""
priority: "P0"
tags: ["集成测试", "Skill管道", "DRL规则", "Aviator公式", "结算流程", "Postman"]
related_files:
  - "his-rule-engine/his-settlement-service/"
  - "his-rule-engine/his-rule-service/"
  - "his-rule-engine/his-formula-service/"
  - "his-rule-engine/his-drug-service/"
  - "his-rule-engine/his-quality-service/"
  - "his-rule-engine/his-drg-service/"
  - "his-rule-engine/postman/HIS_API_Collection.json"
dependencies:
  - "PLAN-20260508-002"
  - "PLAN-20260508-001"
---

# PLAN-20260509-001: 服务集成测试验证

> 计划 ID: PLAN-20260509-001  
> 创建时间: 2026-05-09  
> 更新时间: 2026-05-10  
> 状态: 🔄 in_progress  

---

## 1. 任务概述

### 1.1 背景
所有核心功能已开发完成并入库，8 个微服务全部实现，55 个 REST API 已导出 Postman 集合。需要启动服务进行端到端集成测试，验证完整结算流程。

### 1.2 目标
- 验证 Skill 管道 + DRL 规则 + Aviator 公式的完整结算流程
- 验证数据库中的 DRL 规则和公式能正确加载和执行
- 验证 8 个微服务的 55 个 API 接口正常工作
- 输出集成测试报告

### 1.3 测试范围
| 模块 | 服务 | 端口 | 测试内容 | 状态 |
|------|------|------|---------|------|
| 规则管理 | his-rule-service | 9001 | DRL 规则加载、规则流、规则分组 | ✅ 已实现 |
| 公式管理 | his-formula-service | 9002 | Aviator 公式编译、缓存 | ✅ 已实现 |
| 结算服务 | his-settlement-service | 9003 | 完整结算流程、缓存管理 | ✅ 已实现 |
| 用药审核 | his-drug-service | 9004 | 药品目录、配伍禁忌检查 | ✅ 已实现 |
| 质控管理 | his-quality-service | 9005 | 质控规则、指标监控 | ✅ 已实现 |
| DRG管理 | his-drg-service | 9006 | DRG分组、权重计算 | ✅ 已实现 |
| API 网关 | his-gateway | 9000 | 路由转发 | ⏳ 待验证 |
| 前端界面 | his-rule-engine-web | 5173 | 可视化规则管理 | ✅ 已实现 |

### 1.4 已完成工作
- [x] 8 个微服务代码实现完成
- [x] 55 个 REST API 接口实现
- [x] Postman 集合导出 (HIS_API_Collection.json)
- [x] 前端 Vue 3 管理界面实现
- [x] 规则流可视化编辑器后端 API

---

## 2. 执行计划

### Phase 1: 服务启动验证

| 步骤 | 操作 | 验收标准 | 状态 |
|------|------|---------|------|
| 1.1 | 启动 MySQL 并验证数据库连接 | 连接成功，表结构正确 | ⏳ |
| 1.2 | 启动 Nacos 配置中心 | 服务注册成功 | ⏳ |
| 1.3 | 启动 his-rule-service (:9001) | 健康检查通过 | ⏳ |
| 1.4 | 启动 his-formula-service (:9002) | 健康检查通过 | ⏳ |
| 1.5 | 启动 his-settlement-service (:9003) | 健康检查通过 | ⏳ |
| 1.6 | 启动 his-drug-service (:9004) | 健康检查通过 | ⏳ |
| 1.7 | 启动 his-quality-service (:9005) | 健康检查通过 | ⏳ |
| 1.8 | 启动 his-drg-service (:9006) | 健康检查通过 | ⏳ |
| 1.9 | 启动 his-gateway (:9000) | 路由转发正常 | ⏳ |
| 1.10 | 启动前端 his-rule-engine-web (:5173) | 页面可访问 | ⏳ |

### Phase 2: 规则加载验证

| 步骤 | 操作 | 验收标准 | 状态 |
|------|------|---------|------|
| 2.1 | 验证 rule_definition 表规则加载 | 规则全部加载到 KieBase | ⏳ |
| 2.2 | 验证 aviator_formula 表公式编译 | 公式全部编译通过 | ⏳ |
| 2.3 | 验证规则组分组加载 | 规则组正确分组 | ⏳ |
| 2.4 | 验证规则流定义加载 | 流程定义可正确解析 | ⏳ |

### Phase 3: API 接口测试 (使用 Postman 集合)

| 用例 | API | 预期输出 | 优先级 | 状态 |
|------|-----|---------|--------|------|
| R01 规则分页查询 | GET /api/v1/rules | 返回规则列表 | P0 | ⏳ |
| R02 规则详情查询 | GET /api/v1/rules/{id} | 返回规则详情 | P0 | ⏳ |
| R03 创建规则 | POST /api/v1/rules | 创建成功返回ID | P0 | ⏳ |
| R04 更新规则 | PUT /api/v1/rules/{id} | 更新成功 | P1 | ⏳ |
| R05 删除规则 | DELETE /api/v1/rules/{id} | 删除成功 | P1 | ⏳ |
| R06 校验规则 | POST /api/v1/rules/{id}/validate | 校验通过/失败 | P0 | ⏳ |
| R07 发布规则 | POST /api/v1/rules/{id}/publish | 发布成功 | P0 | ⏳ |
| F01 规则流分页查询 | GET /api/v1/flows | 返回流程列表 | P0 | ⏳ |
| F02 规则流详情 | GET /api/v1/flows/{id} | 返回流程定义JSON | P0 | ⏳ |
| F03 创建规则流 | POST /api/v1/flows | 创建成功返回ID | P0 | ⏳ |
| F04 更新规则流 | PUT /api/v1/flows/{id} | 更新成功 | P1 | ⏳ |
| F05 删除规则流 | DELETE /api/v1/flows/{id} | 删除成功 | P1 | ⏳ |
| F06 发布规则流 | POST /api/v1/flows/{id}/publish | 发布成功 | P0 | ⏳ |
| F07 回滚规则流 | POST /api/v1/flows/{id}/rollback | 回滚成功 | P1 | ⏳ |
| F08 版本历史 | GET /api/v1/flows/{id}/versions | 返回版本列表 | P1 | ⏳ |
| F09 导出流程 | GET /api/v1/flows/{id}/export | 返回JSON定义 | P1 | ⏳ |
| F10 导入流程 | POST /api/v1/flows/import | 导入成功 | P1 | ⏳ |
| F11 执行规则流 | POST /api/v1/flows/{id}/execute | 执行成功返回结果 | P0 | ⏳ |
| G01 规则分组查询 | GET /api/v1/rule-groups | 返回分组列表 | P0 | ⏳ |
| G02 规则分组分页 | GET /api/v1/rule-groups/page | 返回分页列表 | P1 | ⏳ |
| G03 规则分组详情 | GET /api/v1/rule-groups/{id} | 返回分组详情 | P1 | ⏳ |
| G04 创建规则分组 | POST /api/v1/rule-groups | 创建成功 | P1 | ⏳ |
| G05 更新规则分组 | PUT /api/v1/rule-groups/{id} | 更新成功 | P1 | ⏳ |
| G06 启用/禁用分组 | PUT /api/v1/rule-groups/{id}/enabled | 操作成功 | P1 | ⏳ |
| G07 删除规则分组 | DELETE /api/v1/rule-groups/{id} | 删除成功 | P1 | ⏳ |
| FM01 公式分页查询 | GET /api/v1/formulas | 返回公式列表 | P0 | ⏳ |
| FM02 公式详情 | GET /api/v1/formulas/{id} | 返回公式详情 | P0 | ⏳ |
| FM03 按KEY获取公式 | GET /api/v1/formulas/key/{key} | 返回公式 | P0 | ⏳ |
| FM04 创建公式 | POST /api/v1/formulas | 创建成功 | P1 | ⏳ |
| FM05 更新公式 | PUT /api/v1/formulas/{id} | 更新成功 | P1 | ⏳ |
| FM06 删除公式 | DELETE /api/v1/formulas/{id} | 删除成功 | P1 | ⏳ |
| FM07 校验公式 | POST /api/v1/formulas/{id}/validate | 校验通过/失败 | P0 | ⏳ |
| FM08 发布公式 | POST /api/v1/formulas/{id}/publish | 发布成功 | P0 | ⏳ |
| S01 发起结算 | POST /api/v1/settlements | 结算成功返回结果 | P0 | ⏳ |
| S02 结算记录查询 | GET /api/v1/settlements | 返回结算列表 | P0 | ⏳ |
| S03 按单号查询 | GET /api/v1/settlements/{no} | 返回结算详情 | P0 | ⏳ |
| S04 更新结算记录 | PUT /api/v1/settlements/{id} | 更新成功 | P1 | ⏳ |
| S05 删除结算记录 | DELETE /api/v1/settlements/{id} | 删除成功 | P1 | ⏳ |
| S06 刷新公式缓存 | POST /api/v1/settlements/cache/refresh | 刷新成功 | P1 | ⏳ |
| S07 清空公式缓存 | POST /api/v1/settlements/cache/clear | 清空成功 | P1 | ⏳ |
| D01 药品目录查询 | GET /api/v1/drugs | 返回药品列表 | P0 | ⏳ |
| D02 药品详情 | GET /api/v1/drugs/{id} | 返回药品详情 | P1 | ⏳ |
| D03 新增药品 | POST /api/v1/drugs | 新增成功 | P1 | ⏳ |
| D04 更新药品 | PUT /api/v1/drugs/{id} | 更新成功 | P1 | ⏳ |
| D05 删除药品 | DELETE /api/v1/drugs/{id} | 删除成功 | P1 | ⏳ |
| Q01 质控规则查询 | GET /api/v1/quality | 返回质控列表 | P0 | ⏳ |
| Q02 质控规则详情 | GET /api/v1/quality/{id} | 返回质控详情 | P1 | ⏳ |
| Q03 新增质控规则 | POST /api/v1/quality | 新增成功 | P1 | ⏳ |
| Q04 更新质控规则 | PUT /api/v1/quality/{id} | 更新成功 | P1 | ⏳ |
| Q05 删除质控规则 | DELETE /api/v1/quality/{id} | 删除成功 | P1 | ⏳ |
| DR01 DRG定义查询 | GET /api/v1/drg | 返回DRG列表 | P0 | ⏳ |
| DR02 DRG定义详情 | GET /api/v1/drg/{id} | 返回DRG详情 | P1 | ⏳ |
| DR03 新增DRG定义 | POST /api/v1/drg | 新增成功 | P1 | ⏳ |
| DR04 更新DRG定义 | PUT /api/v1/drg/{id} | 更新成功 | P1 | ⏳ |
| DR05 删除DRG定义 | DELETE /api/v1/drg/{id} | 删除成功 | P1 | ⏳ |

### Phase 4: 结算流程测试

| 用例 | 输入 | 预期输出 | 优先级 | 状态 |
|------|------|---------|--------|------|
| S01 职工医保结算 | patientType=employee, totalFee=15000 | 起付线1000, 比例85%, 报销11900 | P0 | ⏳ |
| S02 居民医保结算 | patientType=resident, totalFee=10000 | 起付线500, 比例65%, 报销6175 | P0 | ⏳ |
| S03 救助对象结算 | patientType=aid, totalFee=8000 | 起付线300, 比例50%, 报销3850 | P0 | ⏳ |
| S04 三级医院结算 | hospitalLevel=三级, totalFee=20000 | 比例降低10% | P0 | ⏳ |
| S05 未达起付线 | totalFee=300, deductible=500 | BLOCK/WARN, 报销0 | P0 | ⏳ |
| S06 患者类型缺失 | patientType=null | BLOCK: 患者类型缺失 | P1 | ⏳ |
| S07 费用为负数 | totalFee=-100 | BLOCK: 费用不能为负 | P1 | ⏳ |

### Phase 5: Skill 管道验证

| 步骤 | 操作 | 验收标准 | 状态 |
|------|------|---------|------|
| 5.1 | 验证 InsuranceIdentitySkill 执行 | 身份校验通过/阻断 | ⏳ |
| 5.2 | 验证 DeductibleSkill 执行 | 起付线正确计算 | ⏳ |
| 5.3 | 验证 ReimburseRatioSkill 执行 | 报销比例正确计算 | ⏳ |
| 5.4 | 验证 SkillPipelineExecutor 超时控制 | 30秒超时返回 WARN | ⏳ |
| 5.5 | 验证 BLOCK 阻断机制 | 阻断后后续 Skill 不执行 | ⏳ |

---

## 3. 测试数据

### 3.1 测试用例输入

```json
{
  "visitId": "V001",
  "patientId": "P001",
  "patientType": "employee",
  "insuranceType": "职工医保",
  "hospitalLevel": "二级",
  "totalFee": 15000.00
}
```

### 3.2 预期结算结果

```json
{
  "settlementNo": "ST202605090001",
  "totalFee": 15000.00,
  "deductible": 1000.00,
  "ratio": 0.85,
  "reimburseAmount": 11900.00,
  "selfPayAmount": 3100.00,
  "resultLevel": "PASS",
  "skillResults": [
    {"level": "PASS", "source": "InsuranceIdentitySkill", "message": "医保身份校验通过"},
    {"level": "PASS", "source": "DeductibleSkill", "message": "起付线计算完成"},
    {"level": "PASS", "source": "ReimburseRatioSkill", "message": "报销比例计算完成"}
  ]
}
```

---

## 4. 输出物

| 输出物 | 格式 | 说明 | 状态 |
|--------|------|------|------|
| Postman 集合 | JSON | 55 个 API 接口测试集 | ✅ 已完成 |
| 集成测试报告 | Markdown | 测试结果汇总 | ⏳ 待生成 |
| 日志文件 | log | 服务启动和规则加载日志 | ⏳ 待收集 |
| 失败用例记录 | Markdown | 失败原因和修复建议 | ⏳ 待记录 |

---

## 5. 验收标准

- [ ] 所有服务启动成功，健康检查通过
- [ ] DRL 规则全部加载到 KieBase
- [ ] Aviator 公式全部编译通过
- [ ] 55 个 API 接口测试通过
- [ ] 结算测试用例全部通过
- [ ] Skill 管道执行顺序正确
- [ ] BLOCK 阻断机制正常工作
- [ ] 超时控制正常工作

---

## 6. 风险评估

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|:----:|---------|
| KieBase 编译失败 | 高 | 中 | 检查 DRL 语法，逐条加载验证 |
| Aviator 公式编译失败 | 高 | 低 | 预编译验证，跳过错误公式 |
| 服务端口冲突 | 中 | 低 | 修改 application.yml 端口配置 |
| 数据库连接失败 | 高 | 低 | 检查 MySQL 连接配置 |
| Nacos 配置中心未启动 | 中 | 低 | 检查 Nacos 服务状态 |

---

## 7. 进度跟踪

| 时间 | 操作 | 状态变更 | 备注 |
|------|------|---------|------|
| 2026-05-09 | 创建计划 | pending | 初始创建 |
| 2026-05-10 | 更新计划 | in_progress | 补充 8 个微服务、55 个 API、Postman 集合 |
| | | | |

---

*计划执行中*
