# 计划跟踪索引

> 自动生成的计划跟踪索引
> 最后更新: 2026-05-09

---

## 活跃计划 (active/)

| 计划 ID | 标题 | 类型 | 状态 | 阶段 | 优先级 | 负责人 | 创建时间 | 文件 |
|---------|------|------|------|------|--------|--------|---------|------|
| PLAN-20260509-001 | 服务集成测试验证 | test | ⏳ pending | Phase 1 | P1 | developer | 2026-05-09 | [链接](active/2026-05-09-integration-test.md) |
| PLAN-20260509-002 | V2.0 规则可视化编排 | feature | ⏳ pending | Phase 2 | P0 | developer | 2026-05-09 | [链接](active/2026-05-09-v2-rule-flow-editor.md) |
| PLAN-20260509-003 | Nacos 配置中心热更新机制 | feature | ⏳ pending | Phase 2 | P1 | developer | 2026-05-09 | [链接](active/2026-05-09-nacos-hot-reload.md) |

## 已完成计划 (completed/)

| 计划 ID | 标题 | 类型 | 完成时间 | 文件 |
|---------|------|------|---------|------|
| PLAN-20260508-002 | Skill 独立类实现 + Pipeline 重构 | feature | 2026-05-08 | [链接](completed/2026-05-08-skill-pipeline-refactor.md) |
| PLAN-20260501-002 | V1.0 部署配置：Nacos配置+Docker Compose部署 | deploy | 2026-05-01 | [链接](completed/2026-05-01-deploy-docker-nacos.md) |
| PLAN-20260430-001 | 架构升级：SCA 2025.1.0.0 + Nacos 3.2.0 | feature | 2026-05-01 | [链接](completed/2026-05-01-sca-upgrade.md) |
| PLAN-20260427-001 | P1 测试验证与联调 | feature | 2026-04-28 | [链接](completed/2026-04-27-p1-test-verification.md) |
| PLAN-20260426-001 | 实现医保报销规则引擎 | feature | 2026-04-27 | [链接](completed/2026-04-27-feature-reimburse-rule-engine.md) |

## 已归档计划 (archived/)

| 计划 ID | 标题 | 类型 | 归档时间 | 文件 |
|---------|------|------|---------|------|
| PLAN-20260508-001 | DRL 规则文件补充与验证 | feature | 2026-05-09 | [链接](archived/2026-05-08-drl-rules-supplement.md) |
| PLAN-20260501-001 | V1.0 功能测试：单元测试+接口测试+安全测试+数据准备 | test | 2026-05-09 | [链接](archived/2026-05-01-test-v1-full-coverage.md) |
| PLAN-20260426-000 | V1.0 全功能开发 | feature | 2026-04-26 | [链接](archived/2026-04-26-feature-v1-full-development.md) |

---

## 统计

| 状态 | 数量 |
|------|------|
| 活跃 (pending) | 3 |
| 进行中 (in_progress) | 0 |
| 已完成 (completed) | 5 |
| 已归档 (archived) | 3 |
| **总计** | **11** |

---

## 活跃计划摘要

### PLAN-20260509-001: 服务集成测试验证 (P1, 1-2天)
- **目标**: 验证 Skill 管道 + DRL 规则 + Aviator 公式的完整结算流程
- **关键步骤**: 服务启动 → 规则加载 → 7个结算用例 → Skill 管道验证
- **依赖**: PLAN-20260508-002, PLAN-20260508-001

### PLAN-20260509-002: V2.0 规则可视化编排 (P0, 5-7天)
- **目标**: 前端流程编辑器对接后端规则引擎
- **关键步骤**: 后端 API (2天) → 前端编辑器 (3天) → 联调测试 (2天)
- **技术栈**: Vue 3 + X6/LogicFlow + Spring Boot
- **依赖**: PLAN-20260509-001

### PLAN-20260509-003: Nacos 配置中心热更新机制 (P1, 2-3天)
- **目标**: 规则/公式动态刷新机制，无需重启服务
- **关键步骤**: Nacos 集成 (1天) → 规则刷新 (1天) → 公式刷新 (1天) → 灰度回滚 (0.5天)
- **依赖**: PLAN-20260509-001

---

## 2026-05-09 工作摘要

### 创建新计划
- ✅ PLAN-20260509-001: 服务集成测试验证
- ✅ PLAN-20260509-002: V2.0 规则可视化编排
- ✅ PLAN-20260509-003: Nacos 配置中心热更新机制

### 归档旧计划
- ✅ 3 个活跃计划已归档

---

*索引结束*
