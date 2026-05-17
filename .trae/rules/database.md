---
alwaysApply: true
description: 数据库规范
---
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

-- 结算明细表
CREATE TABLE `settlement_detail` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `settlement_id`   BIGINT        NOT NULL COMMENT '结算记录ID',
    `fee_item_name`   VARCHAR(128)  NOT NULL COMMENT '费用项目名称',
    `fee_amount`      DECIMAL(14,2) NOT NULL COMMENT '费用金额',
    `reimburse_ratio` DECIMAL(5,4)  NOT NULL DEFAULT 0 COMMENT '报销比例',
    `reimburse_amount`DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '报销金额',
    `self_pay_amount` DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '自付金额',
    `tenant_id`       VARCHAR(64)   NOT NULL DEFAULT '',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_settlement` (`settlement_id`)
) ENGINE=InnoDB COMMENT='结算明细';
```

#### DRG 相关表

```sql
-- DRG 分组定义表
CREATE TABLE `drg_definition` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `drg_code`        VARCHAR(32)  NOT NULL COMMENT 'DRG编码',
    `drg_name`        VARCHAR(128) NOT NULL COMMENT 'DRG名称',
    `mdc_code`        VARCHAR(32)  NOT NULL COMMENT 'MDC编码(主要诊断大类)',
    `base_weight`     DECIMAL(8,4) NOT NULL COMMENT '基础权重',
    `relative_weight` DECIMAL(8,4) NOT NULL COMMENT '相对权重',
    `avg_los`         INT          DEFAULT NULL COMMENT '平均住院日',
    `avg_cost`        DECIMAL(14,2)DEFAULT NULL COMMENT '平均费用',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=停用 1=启用',
    `version`         VARCHAR(32)  NOT NULL DEFAULT 'CHS-DRG-1.0' COMMENT 'DRG版本',
    `tenant_id`       VARCHAR(64)  NOT NULL DEFAULT '',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_drg_code_version` (`drg_code`, `version`, `tenant_id`),
    KEY `idx_mdc` (`mdc_code`)
) ENGINE=InnoDB COMMENT='DRG分组定义';

-- DRG 分组记录表
CREATE TABLE `drg_group_record` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `medical_record_no` VARCHAR(64) NOT NULL COMMENT '病案号',
    `patient_id`      VARCHAR(64)   NOT NULL COMMENT '患者ID',
    `main_diagnosis`  VARCHAR(128)  NOT NULL COMMENT '主要诊断ICD编码',
    `secondary_diagnosis` TEXT      DEFAULT NULL COMMENT '次要诊断ICD编码(JSON数组)',
    `surgery_code`    TEXT          DEFAULT NULL COMMENT '手术操作编码(JSON数组)',
    `drg_code`        VARCHAR(32)   DEFAULT NULL COMMENT '分组结果DRG编码',
    `drg_name`        VARCHAR(128)  DEFAULT NULL COMMENT '分组结果DRG名称',
    `weight`          DECIMAL(8,4)  DEFAULT NULL COMMENT '权重',
    `payment_amount`  DECIMAL(14,2) DEFAULT NULL COMMENT '支付金额',
    `group_status`    TINYINT       NOT NULL DEFAULT 0 COMMENT '分组状态: 0=成功 1=未入组 2=歧义',
    `tenant_id`       VARCHAR(64)   NOT NULL DEFAULT '',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_medical_record` (`medical_record_no`, `tenant_id`),
    KEY `idx_drg_code` (`drg_code`),
    KEY `idx_tenant_time` (`tenant_id`, `create_time`)
) ENGINE=InnoDB COMMENT='DRG分组记录';
```

#### 质控相关表

```sql
-- 质控指标定义表
CREATE TABLE `quality_indicator` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `indicator_code`  VARCHAR(64)  NOT NULL COMMENT '指标编码',
    `indicator_name`  VARCHAR(128) NOT NULL COMMENT '指标名称',
    `category`        VARCHAR(64)  NOT NULL COMMENT '分类: 过程/结果/结构',
    `formula`         TEXT         DEFAULT NULL COMMENT '计算公式',
    `target_value`    VARCHAR(64)  DEFAULT NULL COMMENT '目标值',
    `unit`            VARCHAR(32)  DEFAULT NULL COMMENT '单位',
    `threshold_low`   DECIMAL(10,4)DEFAULT NULL COMMENT '下限阈值',
    `threshold_high`  DECIMAL(10,4)DEFAULT NULL COMMENT '上限阈值',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=停用 1=启用',
    `tenant_id`       VARCHAR(64)  NOT NULL DEFAULT '',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_indicator_code` (`indicator_code`, `tenant_id`)
) ENGINE=InnoDB COMMENT='质控指标定义';

-- 质控结果表
CREATE TABLE `quality_result` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `indicator_id`    BIGINT        NOT NULL COMMENT '指标ID',
    `indicator_code`  VARCHAR(64)   NOT NULL COMMENT '指标编码',
    `indicator_value` DECIMAL(14,4) DEFAULT NULL COMMENT '指标实际值',
    `is_qualified`    TINYINT       NOT NULL COMMENT '是否达标: 0=否 1=是',
    `deviation`       DECIMAL(10,4) DEFAULT NULL COMMENT '偏差值',
    `stat_date`       DATE          NOT NULL COMMENT '统计日期',
    `detail_json`     JSON          DEFAULT NULL COMMENT '明细数据',
    `tenant_id`       VARCHAR(64)   NOT NULL DEFAULT '',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_indicator_date` (`indicator_id`, `stat_date`),
    KEY `idx_tenant_date` (`tenant_id`, `stat_date`)
) ENGINE=InnoDB COMMENT='质控结果';
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

## 六、微服务数据库隔离

### 6.1 数据库分配

| 微服务 | 数据库/Schema | 核心表 |
|--------|--------------|--------|
| his-rule-service | `his_rule` | rule_definition, rule_group, formula, formula_history |
| his-settlement-service | `his_settlement` | settlement_record, settlement_detail, skill_execution_log |
| his-drug-service | `his_drug` | drug_interaction, drug_limit, prescription_record |
| his-quality-service | `his_quality` | quality_indicator, quality_result |
| his-drg-service | `his_drg` | drg_definition, drg_group_record |

### 6.2 跨服务数据访问规则

| 规则 | 要求 |
|------|------|
| 禁止跨库 JOIN | 每个服务只能访问自己的数据库/表 |
| 数据共享 | 通过 Feign 客户端调用其他服务 API 获取数据 |
| 数据冗余 | 允许适度冗余（如结算服务缓存规则快照） |
| 最终一致性 | 使用事件驱动（Spring Cloud Stream）保证数据最终一致 |

---

## 七、SQL 编写规范

| 规则 | 要求 |
|------|------|
| 参数绑定 | 使用 `#{}` (MyBatis) 或 PreparedStatment，禁止拼接 |
| SELECT | 明确列出列名，禁止 `SELECT *` |
| 分页 | 使用 PageHelper / MyBatis-Plus 分页插件 |
| 大表操作 | 必须带 `WHERE` 条件和 `LIMIT` |
| 事务 | 只在 Service 层使用 `@Transactional` |
| 金额存储 | `DECIMAL(14,2)` 或更大精度 |

---

## 八、数据迁移

| 规则 | 要求 |
|------|------|
| 迁移工具 | Flyway 或 Liquibase（与 Spring Boot 集成） |
| 脚本命名 | `V{版本}__{描述}.sql` 如 `V1__init_schema.sql` |
| 回滚支持 | 每个迁移脚本配套回滚脚本 |
| 禁止操作 | 迁移脚本中禁止 `DROP DATABASE/TABLE/TRUNCATE` |
| 服务隔离 | 每个微服务独立管理自己的迁移脚本 |

---

最后更新: 2026-05-12 | v1.1 (HIS 规则引擎数据库规范 - 微服务架构版)
