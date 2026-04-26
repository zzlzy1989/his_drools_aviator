# CHANGELOG - HIS Drools+Aviator 规则引擎 Harness 配置变更日志

> 记录 `.claude/` 目录所有配置和内容的变更历史

---

## [2.0.0] - 2026-04-26 (HIS 规则引擎迁移)

### Phase 1: 核心配置迁移

#### 重构文件
- **重构** `agent.md` — 从 TestHub 行为规范 → HIS 规则引擎行为规范 (v2.0)
  - 身份定位：TestHub 开发助手 → HIS 规则引擎专家
  - 技术栈：Python/Django/Vue → Java/Spring/Drools/Aviator
  - 新增：BigDecimal 强制使用规则、Aviator 公式安全约束
  - 新增：Drools KieBase 分组策略、Caffeine 缓存规范

- **重构** `MEMORY.md` — 从 TestHub 项目事实 → HIS 技术栈知识库 (v2.1)
  - 项目元数据：TestHub Platform → HIS 规则剥离引擎 v1.0
  - API 端点：测试管理 API → 规则引擎 API (`/api/v1/rules/*`, `/api/v1/settlement/*`)
  - 数据库模型：测试用例/执行结果 → 规则定义/公式/结算结果
  - 环境配置：Django/Nginx → Spring Boot/Nacos/MySQL

---

### Phase 2: 规范层适配（10 个文件）

#### 完全重写
- **重写** `rules/code-style.md` — Python/Django → Java/Spring/Drools 编码规范
- **重写** `rules/testing.md` — 前端测试 → JUnit5/MockMvc/DRL规则测试
- **重写** `rules/security.md` — Web安全 → SQL注入/Aviator表达式注入防护
- **重写** `rules/api-design.md` — DRF序列化器 → REST Controller/DTO设计
- **重写** `rules/database.md` — Django ORM → MySQL 表结构设计
- **重写** `rules/error-handling.md` — Django异常 → ErrorCode枚举+全局异常处理器
- **重写** `rules/workflow.md` — 通用流程 → 7步流程+3阻塞节点(含公式验证)
- **重写** `rules/docker-deploy.md` — Django部署 → Maven多阶段构建+JVM优化
- **重写** `rules/documentation.md` — 通用文档 → DRL规则/Aviator公式注释规范
- **重写** `rules/performance.md` — Redis缓存 → Caffeine本地缓存/KieBase分组

---

### Phase 3: 知识层重建（6 个文件）

#### 完全重写
- **重写** `domain/glossary.md` — 测试术语(103条) → HIS/规则引擎术语(60+条)
- **重写** `domain/rules.md` — 测试业务规则 → 医保结算/费用校验规则
- **重写** `domain/state-machines.md` — 测试状态机 → 规则生命周期/结算流程
- **重写** `domain/edge-cases.md` — 测试边界 → BigDecimal精度/规则冲突边界
- **重写** `domain/decisions.md` — TestHub决策(22条) → HIS决策(20条)
- **重写** `domain/README.md` — TestHub知识索引 → HIS规则引擎知识索引

---

### Phase 4: 根目录文件更新

#### 更新文件
- **更新** `.claude/SUMMARY.md` — TestHub实施总结 → HIS迁移实施总结
- **更新** `.claude/README.md` — TestHub Harness说明 → HIS规则引擎Harness说明
- **更新** `.claude/CHANGELOG.md` — TestHub变更日志 → HIS迁移变更日志

---

## [1.0.0] - 2026-04-24 (初始创建 - TestHub 版本)

> ⚠️ 此版本为原始 TestHub 项目配置，已完全替换为 HIS 规则引擎版本

### 新建内容
- 创建 `.claude/` 完整目录结构
- 创建 `agent.md` (TestHub版)
- 创建 `MEMORY.md` (TestHub版)
- 创建 `rules/` (6 个规则文件 + README)
- 创建 `commands/` (6 个命令 + README)
- 创建 `hooks/` (6 个钩子脚本 + README)
- 创建 `domain/` (glossary.md + decisions.md)
- 创建 `settings.local.json`
- 创建 `settings.schema.json`

---

## 变更类型说明

| 类型 | 含义 |
|------|------|
| **新增** | 新建文件或功能 |
| **删除** | 移除文件或功能 |
| **重构** | 大幅修改文件结构或职责 |
| **重写** | 完全替换内容（从其他项目迁移） |
| **增强** | 在现有基础上补充内容 |
| **修复** | 修正错误或不一致 |

---

*遵循 [Keep a Changelog](https://keepachangelog.com/) 格式*
*最后更新: 2026-04-26 | HIS Drools+Aviator 规则引擎 v2.0.0*
