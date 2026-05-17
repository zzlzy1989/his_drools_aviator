---
alwaysApply: true
---
# config 模块概览

## 职责
配置中心对接（Nacos/Apollo），实现规则/公式的动态热更新。

## 核心功能
- Nacos/Apollo 配置监听与同步
- 规则包下载与版本管理
- 灰度发布与全量推送
- 变更通知与 Webhook
- 配置加密存储

## 配置项说明
| 配置 Key | 类型 | 说明 | 示例 |
|----------|------|------|------|
| `rules.drl.{ruleKey}` | Text | DRL 规则文本 | 完整 rule 语法 |
| `formulas.aviator.{formulaKey}` | Text | Aviator 表达式 | 数学表达式 |
| `system.settlement.*` | Map | 结算系统参数 | 起付线/比例/封顶 |
| `cache.ttl.*` | Integer | 各缓存 TTL | 300(秒) |
| `security.whitelist.*` | List | 函数白名单 | 允许的 Aviator 函数 |

## API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/config/list` | 当前配置列表 |
| PUT | `/api/v1/config/{key}` | 更新配置（需权限） |
| POST | `/api/v1/config/push` | 手动触发热更新 |
| POST | `/api/v1/config/rollback` | 一键回滚配置 |
| GET | `/api/v1/config/history` | 配置变更历史 |
| POST | `/api/v1/config/canary` | 灰度发布 |

## 关键规则
- 配置变更需灰度验证后全量（BR-C01）
- 必须保留上一版本快照（BR-C02）
- 敏感配置禁止明文（BR-C03）
- 状态机: detecting → downloading → validating → compiling → switching → applied（SM-05）

## 热更新流程
```
1. Nacos Long Polling / Apollo Push Notification
2. 检测到 version 变更
3. 下载最新规则包/配置
4. DRL 语法校验 + 安全扫描
5. KIE Base 异步编译（新 Session）
6. 双缓冲切换（零停机）
7. 保存快照 + 发送 webhook
8. 如失败 → 自动回滚到上一版
```

## 关联模块
← rules (规则热更新目标)
← formulas (公式热更新目标)
→ tenants (按租户隔离配置)

## 核心服务类
- `ConfigSyncService`: 配置同步服务（Nacos/Apollo 适配）
- `RuleHotUpdater`: 规则热更新编排器
- `ConfigEncryptionService`: 配置加解密（AES-256）
- `CanaryReleaseManager`: 灰度发布管理器

## Nacos 配置示例
```yaml
# Data ID: his-rules-drl
# Group: RULE_ENGINE

rules:
  resident-basic:
    version: 23
    drl: |
      package com.his.rules.reimburse
      rule "ResidentBasicReimburese"
        when
          $p : Patient(insuranceType == "RESIDENT")
        then
          // rule logic...
      end
    enabled: true
    
  employee-supplement:
    version: 15
    drl: |
      // ...
    enabled: true
```
