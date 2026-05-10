---
title: "服务集成测试验证"
type: "test"
status: "pending"
created_at: "2026-05-09"
updated_at: "2026-05-09"
completed_at: null
phase: "Phase 1"
owner: "developer"
reviewer: ""
priority: "P1"
tags: ["集成测试", "Skill管道", "DRL规则", "Aviator公式", "结算流程"]
related_files:
  - "his-settlement-service/"
  - "his-rule-engine/sql/"
  - "postman/HIS_Rule_Engine_API_Collection.json"
dependencies:
  - "PLAN-20260508-002"
  - "PLAN-20260508-001"
---

# PLAN-20260509-001: 服务集成测试验证

> 计划 ID: PLAN-20260509-001  
> 创建时间: 2026-05-09  
> 状态: ⏳ pending  

---

## 1. 任务概述

### 1.1 背景
所有核心功能已开发完成并入库，需要启动服务进行端到端集成测试，验证完整结算流程。

### 1.2 目标
- 验证 Skill 管道 + DRL 规则 + Aviator 公式的完整结算流程
- 验证数据库中的 34 条 DRL 规则和 52 条公式能正确加载和执行
- 输出集成测试报告

### 1.3 测试范围
| 模块 | 服务 | 端口 | 测试内容 |
|------|------|------|---------|
| 结算服务 | his-settlement-service | 9003 | 完整结算流程 |
| 规则管理 | his-rule-service | 9001 | DRL 规则加载 |
| 公式管理 | his-formula-service | 9002 | Aviator 公式编译 |
| API 网关 | his-gateway | 9000 | 路由转发 |

---

## 2. 执行计划

### Phase 1: 服务启动验证

| 步骤 | 操作 | 验收标准 |
|------|------|---------|
| 1.1 | 启动 MySQL 并验证数据库连接 | 连接成功，表结构正确 |
| 1.2 | 启动 Nacos 配置中心 | 服务注册成功 |
| 1.3 | 启动 his-rule-service (:9001) | 健康检查通过 |
| 1.4 | 启动 his-formula-service (:9002) | 健康检查通过 |
| 1.5 | 启动 his-settlement-service (:9003) | 健康检查通过 |
| 1.6 | 启动 his-gateway (:9000) | 路由转发正常 |

### Phase 2: 规则加载验证

| 步骤 | 操作 | 验收标准 |
|------|------|---------|
| 2.1 | 验证 rule_definition 表规则加载 | 34 条规则全部加载到 KieBase |
| 2.2 | 验证 aviator_formula 表公式编译 | 52 条公式全部编译通过 |
| 2.3 | 验证规则组分组加载 | 51 个规则组正确分组 |

### Phase 3: 结算流程测试

| 用例 | 输入 | 预期输出 | 优先级 |
|------|------|---------|--------|
| S01 职工医保结算 | patientType=employee, totalFee=15000 | 起付线1000, 比例85%, 报销11900 | P0 |
| S02 居民医保结算 | patientType=resident, totalFee=10000 | 起付线500, 比例65%, 报销6175 | P0 |
| S03 救助对象结算 | patientType=aid, totalFee=8000 | 起付线300, 比例50%, 报销3850 | P0 |
| S04 三级医院结算 | hospitalLevel=三级, totalFee=20000 | 比例降低10% | P0 |
| S05 未达起付线 | totalFee=300, deductible=500 | BLOCK/WARN, 报销0 | P0 |
| S06 患者类型缺失 | patientType=null | BLOCK: 患者类型缺失 | P1 |
| S07 费用为负数 | totalFee=-100 | BLOCK: 费用不能为负 | P1 |

### Phase 4: Skill 管道验证

| 步骤 | 操作 | 验收标准 |
|------|------|---------|
| 4.1 | 验证 InsuranceIdentitySkill 执行 | 身份校验通过/阻断 |
| 4.2 | 验证 DeductibleSkill 执行 | 起付线正确计算 |
| 4.3 | 验证 ReimburseRatioSkill 执行 | 报销比例正确计算 |
| 4.4 | 验证 SkillPipelineExecutor 超时控制 | 30秒超时返回 WARN |
| 4.5 | 验证 BLOCK 阻断机制 | 阻断后后续 Skill 不执行 |

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

| 输出物 | 格式 | 说明 |
|--------|------|------|
| 集成测试报告 | Markdown | 测试结果汇总 |
| 日志文件 | log | 服务启动和规则加载日志 |
| 失败用例记录 | Markdown | 失败原因和修复建议 |

---

## 5. 验收标准

- [ ] 所有服务启动成功，健康检查通过
- [ ] 34 条 DRL 规则全部加载到 KieBase
- [ ] 52 条 Aviator 公式全部编译通过
- [ ] 7 个结算测试用例全部通过
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

---

## 7. 进度跟踪

| 时间 | 操作 | 状态变更 | 备注 |
|------|------|---------|------|
| 2026-05-09 | 创建计划 | pending | 初始创建 |
| | | | |

---

*计划待执行*
