# skills 模块概览 - HIS Skill/Agent 插件架构

> 最后更新: 2026-05-10 | v1.1

## 职责
Skill 定义管理和 Pipeline 调度执行，实现插件化的能力扩展。

## 核心接口
- **ISkill\<T\>**: Skill 统一接口（supportEvent/getOrder/execute）
- **SkillContext\<T\>**: 统一执行上下文（泛型 T 承载业务数据）
- **SkillResult**: 技能执行结果（level/source/message）
- **ResultLevel**: 干预级别枚举（PASS/WARN/BLOCK）

## 已实现 Skill 列表
| Skill 类名 | 所属服务 | 用途 | getOrder |
|-----------|---------|------|----------|
| `InsuranceIdentitySkill` | his-settlement-service | 校验患者医保身份 | 10 |
| `DeductibleSkill` | his-settlement-service | 计算起付线扣除 | 20 |
| `ReimburseRatioSkill` | his-settlement-service | 确定报销比例 | 30 |

## Skill 执行流程
```
1. SettlementService 创建 SkillContext
2. SkillPipelineExecutor 获取所有已注册 Skill（按 getOrder 升序）
3. 依次执行每个 Skill.execute(context)
4. 遇到 BLOCK 级别结果立即终止管道
5. 返回最终 context（包含所有结果）
```

## 关键规则
- Skill 必须实现 ISkill 接口（BR-A01）
- 执行链路必须可追溯（BR-A02）
- 默认超时 30s，最大 300s（BR-A03）
- 调用深度限制 ≤ 5 层（BR-A04）
- BLOCK 级别立即终止管道（BR-S05）

## 新增 Skill 步骤
1. 创建类实现 `ISkill<SettlementFact>`
2. 标注 `@Service` 和 `@Slf4j`
3. 实现 `supportEvent()` 返回事件类型
4. 实现 `getOrder()` 返回优先级
5. 实现 `execute()` 执行业务逻辑
6. Spring 自动装配，无需手动注册

## 配置示例
```yaml
skill:
  executor:
    core-pool-size: 10
    max-pool-size: 50
    queue-capacity: 200
  timeout:
    default-seconds: 30
    max-seconds: 300
```

## 文件位置
- 通用接口: `his-common/his-common-core/src/main/java/com/his/common/ISkill.java`
- 上下文: `his-common/his-common-core/src/main/java/com/his/common/SkillContext.java`
- 结算 Skill: `his-settlement-service/src/main/java/com/his/settlement/skill/`
