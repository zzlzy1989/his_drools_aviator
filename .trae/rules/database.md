# 数据库规范 - HIS 动态规则中台

## 触发条件
- 设计数据库表结构
- 编写 SQL / MyBatis Mapper
- 数据迁移
- 查询优化

---

## 一、数据库基础信息

| 属性 | 值 |
|------|-----|
| 类型 | MySQL 8.0+ |
| 字符集 | `utf8mb4` |
| 排序规则 | `utf8mb4_general_ci` |
| 存储引擎 | InnoDB |
| ORM 框架 | MyBatis / MyBatis-Plus（可选 JPA） |

---

## 二、表设计规范

### 2.1 命名约定

| 对象 | 命名规则 | 示例 |
|------|---------|------|
| 表名 | 小写 + 下划线 | `rule_group`, `settlement_record`, `formula_history` |
| 主键 | `id` (BIGINT AUTO_INCREMENT) | — |
| 外键 | `{关联表}_id` | `rule_group_id`, `tenant_id` |
| 时间字段 | `{动作}_time` (LocalDateTime) | `create_time`, `update_time`, `effect_time` |
| 状态字段 | `{实体}_status` | `rule_status`, `formula_status` |
| 布尔字段 | `is_{形容词}` 或 `has_{名词}` | `is_enabled`, `has_audit` |
| 金额字段 | `{含义}_amount` 或 `{含义}_fee` | `total_fee`, `reimburse_amount`, `deductible_amount` |

### 2.2 通用字段（每张表必须包含）

```sql
`id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
`tenant_id`   VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '租户ID(医院标识)',
`create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
`create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
`update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常 1=删除',
PRIMARY KEY (`id`),
KEY `idx_tenant` (`tenant_id`)
```

### 2.3 核心表设计

#### 规则相关表

```sql
-- 规则组表
CREATE TABLE `rule_group` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `group_code`      VARCHAR(64)  NOT NULL COMMENT '规则组编码: REIMBURSEMENT/DRUG_CHECK/INFECTION_CONTROL',
    `group_name`      VARCHAR(128) NOT NULL COMMENT '规则组名称',
    `description`     VARCHAR(512) DEFAULT NULL COMMENT '描述',
    `tenant_id`       VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '租户ID',
    `is_enabled`      TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用',
    `priority`        INT          NOT NULL DEFAULT 0 COMMENT '组优先级',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_tenant` (`group_code`, `tenant_id`)
) ENGINE=InnoDB COMMENT='规则组';

-- 规则定义表
CREATE TABLE `rule_definition` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `rule_group_id`   BIGINT       NOT NULL COMMENT '所属规则组',
    `rule_name`       VARCHAR(128) NOT NULL COMMENT '规则名称',
    `rule_content`    TEXT         NOT NULL COMMENT 'DRL规则内容',
    `salience`        INT          NOT NULL DEFAULT 0 COMMENT '优先级(越大越先)',
    `activation_group`VARCHAR(64)  DEFAULT NULL COMMENT '激活组',
    `effective_start` DATETIME     DEFAULT NULL COMMENT '生效开始时间',
    `effective_end`   DATETIME     DEFAULT NULL COMMENT '生效结束时间',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=草稿 1=已启用 2=已禁用',
    `version`         INT          NOT NULL DEFAULT 1 COMMENT '版本号',
    `tenant_id`       VARCHAR(64)  NOT NULL DEFAULT '',
    `create_by`       VARCHAR(64)  DEFAULT NULL,
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`       VARCHAR(64)  DEFAULT NULL,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_group` (`rule_group_id`),
    KEY `idx_status_tenant` (`status`, `tenant_id`)
) ENGINE=InnoDB COMMENT='规则定义';
```

#### 公式相关表

```sql
-- 公式定义表
CREATE TABLE `formula` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `formula_key`     VARCHAR(128) NOT NULL COMMENT '公式Key: formula.reimburse.resident',
    `formula_text`    TEXT         NOT NULL COMMENT 'Aviator表达式文本',
    `category`        VARCHAR(64)  NOT NULL DEFAULT 'GENERAL' COMMENT '分类: REIMBURSE/DRUG/DRG/GENERAL',
    `description`     VARCHAR(512) DEFAULT NULL COMMENT '公式说明',
    `version`         INT          NOT NULL DEFAULT 1 COMMENT '当前版本号',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=草稿 1=生效 2=停用',
    `is_validated`    TINYINT      NOT NULL DEFAULT 0 COMMENT '语法校验: 0=未校验 1=通过 2=失败',
    `tenant_id`       VARCHAR(64)  NOT NULL DEFAULT '',
    `create_by`       VARCHAR(64)  DEFAULT NULL,
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`       VARCHAR(64)  DEFAULT NULL,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `key_tenant` (`formula_key`, `tenant_id`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB COMMENT='公式定义';

-- 公式变更历史表
CREATE TABLE `formula_history` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `formula_id`      BIGINT       NOT NULL COMMENT '公式ID',
    `formula_text`    TEXT         NOT NULL COMMENT '变更时快照',
    `version`         INT          NOT NULL COMMENT '版本号',
    `change_reason`   VARCHAR(512) DEFAULT NULL COMMENT '变更原因',
    `change_by`       VARCHAR(64)  NOT NULL COMMENT '变更人',
    `change_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_formula` (`formula_id`)
) ENGINE=InnoDB COMMENT='公式变更历史';
```

#### 结算相关表

```sql
-- 结算记录表
CREATE TABLE `settlement_record` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `settlement_no`   VARCHAR(64)   NOT NULL COMMENT '结算单号',
    `patient_id`      VARCHAR(64)   NOT NULL COMMENT '患者ID',
    `patient_type`    VARCHAR(32)   NOT NULL COMMENT '患者类型: employee/resident/aid',
    `total_fee`       DECIMAL(14,2) NOT NULL COMMENT '总费用',
    `deductible`      DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '起付线',
    `ratio`           DECIMAL(5,4)  NOT NULL COMMENT '报销比例',
    `reimburse_amount`DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '报销金额',
    `self_pay_amount` DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '自付金额',
    `result_level`    VARCHAR(16)   NOT NULL DEFAULT 'PASS' COMMENT 'PASS/WARN/BLOCK',
    `skill_results`   JSON          DEFAULT NULL COMMENT '各Skill执行结果(JSON数组)',
    `tenant_id`       VARCHAR(64)   NOT NULL DEFAULT '',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_settlement_no` (`settlement_no`),
    KEY `idx_patient` (`patient_id`),
    KEY `idx_tenant_time` (`tenant_id`, `create_time`)
) ENGINE=InnoDB COMMENT='结算记录';
```

#### Skill 执行日志表

```sql
-- Skill 执行日志
CREATE TABLE `skill_execution_log` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `event_type`      VARCHAR(64)  NOT NULL COMMENT '事件类型',
    `skill_name`      VARCHAR(128) NOT NULL COMMENT '执行的Skill名称',
    `result_level`    VARCHAR(16)  NOT NULL COMMENT 'PASS/WARN/BLOCK',
    `message`         VARCHAR(1024)DEFAULT NULL COMMENT '结果消息',
    `execute_ms`      INT          DEFAULT NULL COMMENT '执行耗时(ms)',
    `payload_snapshot`TEXT         DEFAULT NULL COMMENT '输入数据快照',
    `tenant_id`       VARCHAR(64)  NOT NULL DEFAULT '',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_event_skill` (`event_type`, `skill_name`),
    KEY `idx_tenant_time` (`tenant_id`, `create_time`)
) ENGINE=InnoDB COMMENT='Skill执行日志';
```

---

## 三、索引策略

| 场景 | 必须建索引 |
|------|-----------|
| 租户查询 | `tenant_id` （每张表必须有） |
| 状态筛选 | `(status, tenant_id)` 联合索引 |
| 时间范围查询 | `(tenant_id, create_time)` 联合索引 |
| 外键关联 | 关联字段单独索引 |
| 唯一约束 | 业务唯一键（如 settlement_no）|
| 公式查找 | `(formula_key, tenant_id)` 唯一索引 |

---

## 四、SQL 编写规范

| 规则 | 要求 |
|------|------|
| 参数绑定 | 使用 `#{}` (MyBatis) 或 PreparedStatment，禁止拼接 |
| SELECT | 明确列出列名，禁止 `SELECT *` |
| 分页 | 使用 PageHelper / MyBatis-Plus 分页插件 |
| 大表操作 | 必须带 `WHERE` 条件和 `LIMIT` |
| 事务 | 只在 Service 层使用 `@Transactional` |
| 金额存储 | `DECIMAL(14,2)` 或更大精度 |

---

## 五、数据迁移

| 规则 | 要求 |
|------|------|
| 迁移工具 | Flyway 或 Liquibase（与 Spring Boot 集成） |
| 脚本命名 | `V{版本}__{描述}.sql` 如 `V1__init_schema.sql` |
| 回滚支持 | 每个迁移脚本配套回滚脚本 |
| 禁止操作 | 迁移脚本中禁止 `DROP DATABASE/TABLE/TRUNCATE` |

---

最后更新: 2026-04-26 | v1.0 (HIS 规则引擎数据库规范)
