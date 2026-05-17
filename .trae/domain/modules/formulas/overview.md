# formulas 模块概览 - HIS Aviator 公式管理

> 最后更新: 2026-05-10 | v1.1

## 职责
Aviator 表达式定义、参数管理和编译缓存，负责所有金额计算逻辑。

## 核心模型
- **AviatorFormula**: 公式定义主表（formula_key/formula_text/category/description/version/status）
- **FormulaParam**: 公式参数定义（param_name/param_type/required/default_value/description）
- **FormulaVO**: 公式视图对象（含参数列表）

## API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST | `/api/v1/formulas` | 列表/创建公式 |
| GET/PUT/DELETE | `/api/v1/formulas/{id}` | CRUD |
| POST | `/api/v1/formulas/validate` | Aviator 语法校验 |
| GET | `/api/v1/formulas/test` | 测试执行（传入参数看结果） |

## 关键规则
- 必须通过语法校验才能保存（BR-F01）
- 引用变量必须在 param_defs 注册（BR-F02）
- 金额计算必须用 BigDecimal（BR-F03）
- 同 key 同租户只有一个 active 版本（BR-F05）

## 关联模块
← tenants (tenant_id FK)
← rules (规则可调用公式)
→ settlements (结算计算引擎)

## 核心服务类
- `FormulaService`: 公式 CRUD + 语法校验 + 发布管理
- `FormulaValidator`: Aviator 语法校验器

## 常用公式示例
| formula_key | 说明 | 示例表达式 |
|-------------|------|-----------|
| `formula.reimburse.resident.basic` | 居民医保基本报销 | `round((totalFee - deductible) * ratio, 2)` |
| `formula.reimburse.employee.supplement` | 职工大额补充 | `max(0, totalFee * supplement_ratio - cap)` |
| `formula.drug.limit.check` | 合理用药限额 | `total > daily_limit * days ? "EXCEED" : "PASS"` |

## 安全约束
- 白名单函数：仅允许 math/string/logic/date 类函数
- 禁止调用：Runtime/System/反射/IO 相关类
- 执行超时：单次 ≤ 5s

## 文件位置
- 后端: `his-formula-service/src/main/java/com/his/formula/`
- 控制器: `controller/FormulaController`
- 服务: `service/FormulaService`
- 校验器: `validator/FormulaValidator`
