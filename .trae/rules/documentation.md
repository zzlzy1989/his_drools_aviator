---
alwaysApply: true
description: 文档编写规范，所有代码和规则文件必须包含规范注释
---
# 文档编写规则 - HIS 动态规则中台

## 触发条件
- 编写 README
- 创建技术文档
- 编写 DRL 规则注释
- 编写 Aviator 公式说明
- 微服务文档编写
- API 接口文档
- 网关配置文档

---

## 一、文档结构模板

```markdown
# 标题

## 简介
一句话描述这是什么（规则/公式/Skill/模块）

## 适用场景
哪些业务场景会用到

## 输入/输出
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|

## 核心逻辑
关键步骤和判断流程

## 公式/规则内容
实际表达式或 DRL 代码

## 测试用例
输入 → 预期输出 的对照表

## 注意事项
边界情况、已知限制

## 关联规则/公式
引用的其他规则或公式 Key
```

---

## 二、DRL 规则注释规范

### 2.1 文件头注释

```drools
/**
 * ============================================================
 * 规则包: com.his.rules.reimbursement.resident
 * 功能: 居民医保住院费用结算规则集
 * 作者: AI Assistant
 * 版本: v1.2
 * 最后修改: 2026-04-26
 * 
 * 触发事件: EVENT_FEE_SETTLE
 * 前置条件: 患者身份=居民医保, 就诊类型=住院
 * 依赖公式: formula.reimburse.resident.basic
 *           formula.reimburse.resident.supplement
 * ============================================================
 */
package com.his.rules.reimbursement.resident;
```

### 2.2 单条规则注释

```drools
/**
 * R01: 身份基础校验
 * 
 * 业务含义: 校验患者是否具备有效的居民医保身份
 * 优先级: 100 (最高优先级，最先执行)
 * 
 * 条件:
 *   - patientType 为空或不是 RESIDENT
 * 
 * 动作:
 *   - 设置结果为 BLOCK
 *   - 记录拒绝原因
 */
rule "1. 居民身份校验"
    salience 100
    when
        $f: SettlementFact(patientType == null || patientType != "RESIDENT")
    then
        $f.addResult(ResultLevel.BLOCK, "IdentityCheck",
            "患者非居民医保身份，无法按居民医保规则结算");
        log.warn("身份校验失败: patientId={}, type={}",
            $f.getPatientId(), $f.getPatientType());
end
```

### 2.3 规则间依赖注释

```drools
/**
 * R05: 报销金额计算
 * 
 * 依赖关系:
 *   ← R02: 起付线判定 (必须先确定 deductible)
 *   ← R03: 报销比例匹配 (必须先确定 ratio)
 *   ← R04: 封顶线检查 (必须先确定 capAmount)
 *   
 * 计算链路:
 *   totalFee → 扣除起付线 → × 报销比例 → min(封顶线) → finalAmount
 *   
 * 公式调用: formula.reimburse.resident.basic
 *   参数: totalFee, deductible, ratio
 *   返回: BigDecimal (保留2位小数)
 */
rule "5. 报销金额计算"
    salience 10
    when
        $f: SettlementFact(
            totalFee != null, deductible != null,
            ratio != null, resultLevel != ResultLevel.BLOCK
        )
    then
        // ... Aviator 公式调用
end
```

---

## 三、Aviator 公式注释规范

### 3.1 公式头注释

```javascript
/*
 * ============================================================
 * Formula Key: formula.reimburse.resident.basic
 * 名称: 居民医保基本报销金额计算
 * 版本: 3
 * 分类: REIMBURSE (报销)
 * 
 * 输入参数:
 *   - totalFee: BigDecimal  总费用 (必填)
 *   - deductible: BigDecimal 起付线 (必填)
 *   - ratio: BigDecimal    报销比例 (必填, 如 0.65 表示65%)
 * 
 * 输出: BigDecimal (保留2位小数, HALF_UP舍入)
 * 
 * 公式: round((totalFee - deductible) * ratio, 2)
 * 
 * 业务规则来源: 医保政策文件[2026]第15号
 * 最后校验: 2026-04-26 by AI
 * ============================================================
 */
round((totalFee - deductible) * ratio, 2)
```

### 3.2 复杂公式分段注释

```javascript
/*
 * Formula Key: formula.reimburse.resident.staged
 * 名称: 居民医保阶梯式报销（三段式）
 * 
 * 阶段说明:
 *   第一段: 0~1000元  报销80%
 *   第二段: 1000~5000元 报销60%
 *   第三段: 5000元以上 报销40%
 * 
 * 边界情况:
 *   - totalFee <= deductible 时返回 0 (不会为负)
 *   - 起付线扣除后剩余部分参与阶梯计算
 */

let base = max(totalFee - deductible, 0);
base <= 1000 ? round(base * 0.8, 2) :
base <= 5000 ? round(1000 * 0.8 + (base - 1000) * 0.6, 2) :
round(1000 * 0.8 + 4000 * 0.6 + (base - 5000) * 0.4, 2)
```

---

## 四、Skill/Agent 文档规范

### 4.1 Skill 定义文档模板

```markdown
# Skill: RationalDrugUseSkill (合理用药审核)

## 基本信息
| 属性 | 值 |
|------|-----|
| 支持事件 | EVENT_DRUG_PRESCRIBE |
| 执行优先级 | 20 |
| 超时时间 | 20s |
| 重试次数 | 1 |

## 能力描述
对医生开具的处方进行合理性审核，包括：
- 配伍禁忌检测
- 用药极量检查
- 特殊人群（孕妇/儿童/老人）用药限制
- 适应症匹配度评估

## 输入参数 (PrescriptionContext)
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| patientId | String | ✅ | 患者ID |
| drugs | List<DrugItem> | ✅ | 药品列表 |
| diagnosis | List<String | ✅ | 诊断列表 |
| patientInfo | PatientBasicInfo | ✅ | 患者基本信息(年龄/性别/体重等) |

## 输出结果 (List<SkillResult>)
| level | 含义 | 处理方式 |
|-------|------|---------|
| PASS | 无问题 | 放行 |
| WARN | 有警告 | 提示但允许 |
| BLOCK | 严重问题 | 拦截处方 |

## 依赖的规则组
- `DRUG_INTERACTION_RULES` — 配伍禁忌规则
- `DOSAGE_LIMIT_RULES` — 用药极量规则
- `SPECIAL_POPULATION_RULES` — 特殊人群规则

## 降级策略
- 规则引擎不可用 → 返回 PASS（不阻塞业务）
- 超过超时时间 → 返回 WARN + "审核超时"提示
```

---

## 六、微服务文档规范

### 6.1 服务 README 模板

```markdown
# HIS {服务名称} Service

## 服务职责
简要描述该微服务的核心职责和业务范围

## 端口与路径
- 端口: 900X
- API 路径: /api/v1/{module}/**

## 依赖服务
| 服务名 | 调用方式 | 说明 |
|--------|---------|------|
| his-rule-service | Feign | 获取规则定义和公式 |
| Nacos | 配置中心 | 动态配置拉取 |

## 核心功能
1. 功能点 1
2. 功能点 2
3. 功能点 3

## 数据库
- 数据库名: `his_{module}`
- 核心表: table_1, table_2

## 配置项
| 配置 Key | 默认值 | 说明 |
|---------|-------|------|
| {module}.param1 | 100 | 参数 1 说明 |
| {module}.param2 | 50 | 参数 2 说明 |

## 健康检查
- 端点: `/actuator/health`
- 检查项: 数据库连接、依赖服务连通性

## 部署说明
- Docker 镜像: `his-{module}-service`
- JVM 参数: `-Xms512m -Xmx1024m`
- 资源限制: Memory 1G, CPU 1.0
```

### 6.2 API 接口文档模板

```markdown
# {资源名称} API 接口文档

## 接口列表

### 1. 获取{资源}列表
- **URL**: `GET /api/v1/{resource}`
- **权限**: `{module}:{resource}:list`
- **请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 20 |
| keyword | String | 否 | 搜索关键词 |

- **响应示例**:

```json
{
  "code": "0",
  "data": {
    "list": [...],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

### 2. 创建{资源}
- **URL**: `POST /api/v1/{resource}`
- **权限**: `{module}:{resource}:create`
- **请求体**:

```json
{
  "name": "示例名称",
  "description": "描述信息"
}
```
```

### 6.3 网关路由文档

```markdown
# 网关路由配置文档

## 路由列表

| 路由 ID | 目标服务 | 匹配路径 | 限流配置 | 鉴权要求 |
|--------|---------|---------|---------|---------|
| rule-service | his-rule-service:9001 | /api/v1/rules/** | 100 QPS | JWT |
| settlement-service | his-settlement-service:9002 | /api/v1/settlements/** | 50 QPS | JWT |

## 全局过滤器
1. **AuthFilter**: JWT 鉴权，排除路径列表
2. **RateLimiterFilter**: Redis 限流
3. **TraceFilter**: 链路追踪 traceId 注入
4. **CorsFilter**: 跨域配置

## 白名单路径
- `/api/v1/health` - 健康检查
- `/api/v1/rules/public/**` - 公开规则查询
```

---

## 七、代码 Javadoc 规范

### 7.1 类级注释

```java
/**
 * Drools + Aviator 统一规则执行模板。
 * 
 * <p>封装了完整的规则执行生命周期：</p>
 * <ol>
 *   <li>加载 Fact 对象并设置全局变量</li>
 *   <li>Drools 规则匹配（条件分支）</li>
 *   <li>Aviator 公式计算（数值运算）</li>
 *   <li>结果收集与汇总</li>
 * </ol>
 * 
 * <p>线程安全：每个调用创建独立的 KieSession</p>
 * 
 * @see AviatorHelper
 * @see DynamicFormulaManager
 * @author AI Assistant
 * @since 1.0.0
 */
@Service
public class RuleEngineTemplate { ... }
```

### 7.2 方法级注释

```java
/**
 * 执行规则并返回结算结果。
 *
 * @param ruleGroup 规则组名称（对应 KIE Package），如 "REIMBURSEMENT_RESIDENT"
 * @param fact 业务 Fact 对象（不可为 null）
 * @return 包含所有 Skill 执行结果的上下文
 * @throws RuleEngineException 规则编译或执行异常
 * @throws IllegalArgumentException fact 为 null 或 ruleGroup 为空
 */
public SkillContext<?> fireRules(String ruleGroup, Fact fact) { ... }
```

---

## 八、检查清单

- [ ] DRL 文件有完整文件头注释（功能/版本/依赖/作者）
- [ ] 每条规则有序号和中文名称注释
- [ ] Aviator 公式有参数说明和业务来源
- [ ] Skill 有输入/输出/降级策略说明
- [ ] Java 类和方法有标准 Javadoc
- [ ] 公式测试用例覆盖正常和边界场景
- [ ] 规则间依赖关系通过注释明确标注
- [ ] 微服务有 README 文档（职责/依赖/配置/部署）
- [ ] API 接口有完整文档（参数/响应/权限）
- [ ] 网关路由配置有文档记录
- [ ] Feign Client 有降级策略说明

---

最后更新: 2026-05-12 | v1.1 (HIS Drools+Aviator 规则引擎专用 - 微服务架构版)
