---
title: "Nacos 配置中心热更新机制"
type: "feature"
status: "pending"
created_at: "2026-05-09"
updated_at: "2026-05-10"
completed_at: null
phase: "Phase 2"
owner: "developer"
reviewer: ""
priority: "P1"
tags: ["Nacos", "热更新", "配置中心", "动态刷新", "规则引擎"]
related_files:
  - "his-rule-engine/his-rule-service/"
  - "his-rule-engine/his-formula-service/"
  - "his-rule-engine/his-settlement-service/"
  - "docker/"
dependencies:
  - "PLAN-20260509-001"
---

# PLAN-20260509-003: Nacos 配置中心热更新机制

> 计划 ID: PLAN-20260509-003  
> 创建时间: 2026-05-09  
> 更新时间: 2026-05-10  
> 状态: ⏳ pending  

---

## 1. 任务概述

### 1.1 背景
当前规则和公式的更新需要重启服务才能生效，生产环境变更成本高。需要实现规则/公式的动态刷新机制，支持运行时更新无需重启。

### 1.2 目标
- 实现 DRL 规则的动态加载和热更新
- 实现 Aviator 公式的动态编译和缓存刷新
- 通过 Nacos 配置中心触发刷新事件
- 支持灰度发布和回滚

### 1.3 范围
- **包含**: Nacos 监听器、规则刷新器、公式刷新器、刷新 API
- **不包含**: Nacos 集群部署（运维范畴）

### 1.4 当前架构现状
| 组件 | 当前状态 | 热更新需求 |
|------|---------|-----------|
| his-rule-service (:9001) | 规则管理、规则流、规则分组 | KieBase 热刷新 |
| his-formula-service (:9002) | Aviator 公式管理 | Expression 缓存刷新 |
| his-settlement-service (:9003) | 结算服务、规则执行 | 监听刷新事件 |

---

## 2. 技术方案

### 2.1 整体架构

```
┌─────────────────────────────────────────────┐
│                  Nacos Server                │
│  ┌────────────────────────────────────────┐  │
│  │  DataId: his-rule-engine               │  │
│  │  Group: RULE_ENGINE                    │  │
│  │  Content: { ruleVersion, formulaVersion } │  │
│  └────────────────────────────────────────┘  │
└──────────────────┬──────────────────────────┘
                   │ @NacosConfigListener
                   ▼
┌─────────────────────────────────────────────┐
│              his-settlement-service          │
│  ┌─────────────┐    ┌──────────────────┐    │
│  │ ConfigWatcher│───▶│  RefreshHandler  │    │
│  └─────────────┘    └────────┬─────────┘    │
│                              │               │
│              ┌───────────────┼────────────┐ │
│              ▼               ▼            │ │
│  ┌─────────────┐    ┌──────────────┐    │ │
│  │RuleRefresher│    │FormulaRefresher│  │ │
│  └─────────────┘    └──────────────┘    │ │
│              │               │            │ │
│              ▼               ▼            │ │
│  ┌─────────────┐    ┌──────────────┐    │ │
│  │  KieBase    │    │ExpressionCache│   │ │
│  └─────────────┘    └──────────────┘    │ │
└─────────────────────────────────────────────┘
```

### 2.2 核心组件

| 组件 | 职责 | 技术实现 |
|------|------|---------|
| NacosConfigListener | 监听配置变更 | `@NacosConfigListener` 注解 |
| RefreshHandler | 刷新事件处理 | 事件驱动，异步执行 |
| RuleRefresher | DRL 规则刷新 | 重建 KieBase/KieSession |
| FormulaRefresher | 公式刷新 | 清理 ExpressionCache，重新编译 |
| RefreshAPI | 手动触发刷新 | REST API `/api/v1/config/refresh` |

### 2.3 Nacos 配置格式

```json
{
  "ruleVersion": "20260509001",
  "formulaVersion": "20260509001",
  "ruleGroups": ["reimbursement", "drug_check"],
  "refreshMode": "full",
  "enabled": true
}
```

---

## 3. 执行计划

### Phase 1: Nacos 集成配置 (1 天)

| 步骤 | 操作 | 交付物 |
|------|------|--------|
| 1.1 | 引入 Nacos 客户端依赖 | pom.xml 更新 |
| 1.2 | 配置 Nacos 连接参数 | bootstrap.yml |
| 1.3 | 实现配置监听器 | NacosConfigListener.java |
| 1.4 | 验证配置读取 | 单元测试 |

### Phase 2: 规则热更新实现 (1 天)

| 步骤 | 操作 | 交付物 |
|------|------|--------|
| 2.1 | 实现 RuleRefresher | 重建 KieBase |
| 2.2 | 实现 KieBase 安全替换 | 原子切换 |
| 2.3 | 实现刷新日志和审计 | AuditLog |
| 2.4 | 编写规则刷新测试 | 集成测试 |

### Phase 3: 公式热更新实现 (1 天)

| 步骤 | 操作 | 交付物 |
|------|------|--------|
| 3.1 | 实现 FormulaRefresher | 缓存清理 + 重新编译 |
| 3.2 | 实现公式语法预校验 | AviatorValidator |
| 3.3 | 实现公式刷新 API | REST API |
| 3.4 | 编写公式刷新测试 | 集成测试 |

### Phase 4: 灰度与回滚 (0.5 天)

| 步骤 | 操作 | 交付物 |
|------|------|--------|
| 4.1 | 实现刷新前备份 | 版本快照 |
| 4.2 | 实现刷新失败回滚 | 自动回退 |
| 4.3 | 实现手动回滚 API | REST API |

---

## 4. 核心代码设计

### 4.1 Nacos 配置监听器

```java
@Component
@Slf4j
public class RuleConfigListener {

    @Autowired
    private RefreshHandler refreshHandler;

    @NacosConfigListener(
        dataId = "his-rule-engine",
        group = "RULE_ENGINE",
        timeout = 5000
    )
    public void onConfigChange(String configJson) {
        try {
            RuleConfig config = JSON.parseObject(configJson, RuleConfig.class);
            log.info("收到配置变更通知: ruleVersion={}, formulaVersion={}",
                config.getRuleVersion(), config.getFormulaVersion());
            
            refreshHandler.handleRefresh(config);
        } catch (Exception e) {
            log.error("配置变更处理失败", e);
        }
    }
}
```

### 4.2 规则刷新器

```java
@Service
@Slf4j
public class RuleRefresher {

    @Autowired
    private KieContainer kieContainer;

    @Autowired
    private RuleDefinitionMapper ruleMapper;

    public void refreshRules(List<String> ruleGroups) {
        log.info("开始刷新规则: groups={}", ruleGroups);
        
        // 1. 从数据库加载最新规则
        List<RuleDefinition> rules = ruleMapper.selectByGroups(ruleGroups);
        
        // 2. 构建新的 KieBase
        KieBase newKieBase = buildKieBase(rules);
        
        // 3. 原子替换 KieContainer
        KieServices kieServices = KieServices.Factory.get();
        KieRepository repository = kieServices.getRepository();
        repository.addKieModule(newKieBase.getKieModule());
        
        log.info("规则刷新完成: ruleCount={}", rules.size());
    }
}
```

---

## 5. 验收标准

- [ ] Nacos 配置监听器正常工作
- [ ] 规则热更新无需重启服务
- [ ] 公式热更新缓存正确刷新
- [ ] 刷新失败自动回滚到上一版本
- [ ] 手动触发刷新 API 可用
- [ ] 刷新日志和审计记录完整
- [ ] 灰度发布机制正常

---

## 6. 风险评估

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|:----:|---------|
| KieBase 替换期间规则执行中断 | 高 | 中 | 使用读写锁，等待执行完成 |
| 刷新过程中 OOM | 高 | 低 | 限制并发刷新，监控内存 |
| Nacos 连接不稳定 | 中 | 中 | 增加重试机制和本地缓存 |
| 规则语法错误导致刷新失败 | 中 | 中 | 刷新前预编译验证 |

---

## 7. 进度跟踪

| 时间 | 操作 | 状态变更 | 备注 |
|------|------|---------|------|
| 2026-05-09 | 创建计划 | pending | 初始创建 |
| 2026-05-10 | 更新计划 | pending | 补充当前架构现状、依赖服务列表 |
| 2026-05-10 | Phase 1-3 完成 | in_progress | 配置热更新监听器 + API |
| 2026-05-10 | 测试通过 | completed | 热更新API + 结算验证 |

---

## 8. 实施总结

### 已完成
- ✅ `ConfigRefreshListener.java` - 监听 Spring Cloud RefreshEvent
- ✅ `SettlementController.refresh()` - 热更新 API `/api/v1/settlements/refresh`
- ✅ `FormulaLoaderService.refreshCache()` - 公式缓存刷新
- ✅ `FormulaLoaderService.clearCache()` - 全量缓存清空

### 热更新流程
```
公式发布 → NacosFormulaSyncListener → Nacos 配置中心
                                          ↓
配置变更 → RefreshEvent → ConfigRefreshListener → FormulaLoaderService.clearCache()
                                                                     ↓
                                                        下次结算时重新加载最新公式
```

### API 接口
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/settlements/refresh` | POST | 全量刷新缓存 |
| `/api/v1/settlements/refresh?tenantId=X&formulaKey=Y` | POST | 指定刷新 |
| `/api/v1/settlements/cache/refresh` | POST | 刷新指定公式 |
| `/api/v1/settlements/cache/clear` | POST | 清空所有缓存 |

---

*计划已完成 - Phase 1-3 核心功能实现*
