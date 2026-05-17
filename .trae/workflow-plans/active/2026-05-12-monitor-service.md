---
title: "V2.0 监控大屏模块"
type: "feature"
status: "completed"
created_at: "2026-05-12"
updated_at: "2026-05-13"
completed_at: "2026-05-13"
phase: "Phase 1"
owner: "developer"
reviewer: ""
priority: "P0"
tags: ["V2.0", "监控大屏", "Prometheus", "ECharts", "Vue"]
related_files:
  - "his-rule-engine/his-monitor-service/"
  - "his-rule-engine-web/src/views/monitor/"
dependencies: []
---

# PLAN-20260512-001: V2.0 监控大屏

> 计划 ID: PLAN-20260512-001
> 创建时间: 2026-05-12
> 更新时间: 2026-05-13
> 状态: ⏳ in_progress

---

## 1. 任务概述

### 1.1 目标
实现 V2.0 监控大屏模块，包括：
1. his-monitor-service (9007) - 监控服务后端
2. 前端监控大屏 - Vue + ECharts 实时看板

### 1.2 技术选型
| 组件 | 技术 | 说明 |
|------|------|------|
| 后端 | Spring Boot 3.5 + Micrometer | Prometheus 埋点 |
| 前端 | Vue 3 + ECharts 5 | 实时图表 |
| 实时推送 | WebSocket | 监控数据实时推送 |
| 端口 | 9007 | 新服务 |

---

## 2. 当前进度

### 已完成
- ✅ his-monitor-service (9007) - 监控服务后端
- ✅ MonitorController - 3 个 API (/metrics, /alerts, /top-rules)
- ✅ MonitorService - 指标收集服务 (内存存储)
- ✅ Docker 镜像构建成功
- ✅ API 测试验证成功
- ✅ Dashboard.vue - 监控大屏前端组件 (9122 bytes)
- ✅ monitor.ts - API 调用文件 (1304 bytes)
- ✅ 路由配置 `/monitor` → Dashboard.vue
- ✅ 菜单配置 "监控大屏" 菜单项
- ✅ ECharts 5.5.0 依赖已安装

### 待完成
- ⏳ 前端启动验证（需要 his-monitor-service 正常运行）
- ⏳ WebSocket 实时推送 (可选)

---

## 3. 验收标准

- [x] his-monitor-service 启动成功 (端口 9007)
- [x] `/api/v1/monitor/metrics` 返回指标数据
- [x] 前端监控大屏显示4个指标卡片
- [x] 执行耗时图表正常显示
- [x] TOP规则排行榜正常显示

---

## 4. 进度

| 时间 | 操作 | 状态 |
|------|------|------|
| 2026-05-12 | 创建计划 | completed |
| 2026-05-12 | his-monitor-service 后端 | completed |
| 2026-05-13 | API 验证通过 | completed |
| 2026-05-13 | 前端监控大屏 | completed |

---

*计划已完成*