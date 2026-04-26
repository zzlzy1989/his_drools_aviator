# formulas 模块概览

## 职责
Aviator 表达式定义、参数管理和编译缓存，负责所有金额计算逻辑。

## 核心模型
- **Formula**: 公式定义主表（formula_key/formula_text/category/description/version/status）
- **FormulaParam**: 公式参数定义（param_name/param_type/required/default_value/description）
- **FormulaCategory**: 公式分类（REIMBURSE/DRUG/DRG/GENERAL）

## API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST | `/api/v1/formulas` | 列表/创建公式 |
| GET/PUT/DELETE | `/api/v1/formulas/{id}` | CRUD |
| POST | `/api/v1/formulas/{id}/syntax-check` | Aviator 语法校验 |
| POST | `/api/v1/formulas/{id}/validate-params` | 参数完整性校验 |
| POST | `/api/v1/formulas/{id}/publish` | 发布生效 |
| POST | `/api/v1/formulas/{id}/test-run` | 测试执行（传入参数看结果） |
| GET/POST | `/api/v1/formulas/{id}/params` | 参数定义管理 |

## 关键规则
- 必须通过语法校验才能保存（BR-F01）
- 引用变量必须在 param_defs 注册（BR-F02）
- 金额计算必须用 BigDecimal（BR-F03）
- 同 key 同租户只有一个 active 版本（BR-F05）
- 状态机: draft → syntax_checked → param_validated → active（SM-02）

## 关联模块
← tenants (tenant_id FK)
← rules (规则可调用公式)
→ settlements (结算计算引擎)

## 核心服务类
- `FormulaService`: 公式 CRUD + 校验链 + 发布管理
- `AviatorExpressionCache`: 编译结果 Caffeine 缓存
- `BigDecimalEnv`: BigDecimal 类型安全的环境变量封装

## 常用公式示例
| formula_key | 说明 | 示例表达式 |
|-------------|------|-----------|
| `reimburse.resident.basic` | 居民医保基本报销 | `round((total - deductible) * ratio, 2)` |
| `reimburse.employee.supplement` | 职工大额补充 | `max(0, total * supplement_ratio - cap)` |
| `drug.limit.check` | 合理用药限额 | `total > daily_limit * days ? 'EXCEED' : 'PASS'` |
| `drg.weight.calc` | DRG 权重计算 | `base_weight * age_factor * cc_factor` |

## 安全约束
- 白名单函数：仅允许 math/string/logic/date 类函数
- 禁止调用：Runtime/System/反射/IO 相关类
- 执行超时：单次 ≤ 5s
