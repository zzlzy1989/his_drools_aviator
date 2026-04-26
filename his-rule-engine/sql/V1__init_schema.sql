-- ============================================================
-- HIS 动态规则中台 V1.0 数据库初始化脚本
-- 版本: V1
-- 创建日期: 2026-04-26
-- 说明: 初始化 his_rule_engine 数据库核心表结构
-- ============================================================

CREATE DATABASE IF NOT EXISTS `his_rule_engine`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `his_rule_engine`;

-- ------------------------------------------------------------
-- 1. 规则分组表 (rule_group)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `rule_group` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `group_code`        VARCHAR(64)    NOT NULL                    COMMENT '规则组编码: REIMBURSEMENT/DRUG_CHECK/INFECTION_CONTROL/DRG',
    `group_name`        VARCHAR(128)   NOT NULL                    COMMENT '规则组名称',
    `description`       VARCHAR(512)   DEFAULT NULL                COMMENT '描述',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID(医院标识)',
    `is_enabled`        TINYINT       NOT NULL    DEFAULT 1       COMMENT '是否启用: 0=禁用 1=启用',
    `priority`          INT           NOT NULL    DEFAULT 0       COMMENT '组优先级(越小越先)',
    `create_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '创建人',
    `create_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '更新人',
    `update_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT       NOT NULL    DEFAULT 0       COMMENT '逻辑删除: 0=正常 1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_tenant` (`group_code`, `tenant_id`),
    KEY `idx_tenant_enabled` (`tenant_id`, `is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则分组表';

-- ------------------------------------------------------------
-- 2. 规则定义表 (rule_definition)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `rule_definition` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `rule_group_id`     BIGINT         DEFAULT NULL                COMMENT '所属规则组ID',
    `rule_key`          VARCHAR(128)   NOT NULL                    COMMENT '规则唯一标识: rule.{category}.{name}',
    `rule_name`         VARCHAR(128)   NOT NULL                    COMMENT '规则名称',
    `rule_text`         TEXT           NOT NULL                    COMMENT 'DRL规则内容',
    `category`          VARCHAR(32)    NOT NULL                    COMMENT '分类: reimbursement/drug/quality/drg',
    `version`           INT            NOT NULL    DEFAULT 1       COMMENT '版本号',
    `status`            VARCHAR(16)    NOT NULL    DEFAULT 'draft' COMMENT '状态: draft/validated/active/inactive',
    `description`       VARCHAR(500)   DEFAULT NULL                COMMENT '规则描述',
    `salience`          INT            NOT NULL    DEFAULT 0       COMMENT '优先级(越大越先执行)',
    `activation_group`  VARCHAR(64)    DEFAULT NULL                COMMENT '激活组',
    `effective_start`    DATETIME       DEFAULT NULL                COMMENT '生效开始时间',
    `effective_end`     DATETIME       DEFAULT NULL                COMMENT '生效结束时间',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    `create_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '创建人',
    `create_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '更新人',
    `update_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT        NOT NULL    DEFAULT 0       COMMENT '逻辑删除: 0=正常 1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_rule_key` (`tenant_id`, `rule_key`),
    KEY `idx_group` (`rule_group_id`),
    KEY `idx_tenant_category` (`tenant_id`, `category`),
    KEY `idx_tenant_status` (`tenant_id`, `status`),
    CONSTRAINT `fk_rule_group` FOREIGN KEY (`rule_group_id`) REFERENCES `rule_group` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则定义表';

-- ------------------------------------------------------------
-- 3. 规则历史表 (rule_history)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `rule_history` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `rule_id`           BIGINT         NOT NULL                    COMMENT '规则ID',
    `rule_key`          VARCHAR(128)   NOT NULL                    COMMENT '规则标识(快照)',
    `rule_text`         TEXT           NOT NULL                    COMMENT 'DRL规则内容(快照)',
    `version`           INT            NOT NULL                    COMMENT '版本号',
    `status`            VARCHAR(16)    NOT NULL                    COMMENT '状态(快照)',
    `change_reason`     VARCHAR(512)   DEFAULT NULL                COMMENT '变更原因',
    `change_by`         VARCHAR(64)    NOT NULL                    COMMENT '变更人',
    `change_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_rule_id` (`rule_id`),
    KEY `idx_tenant_rule` (`tenant_id`, `rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则历史表';

-- ------------------------------------------------------------
-- 4. 公式定义表 (aviator_formula)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `aviator_formula` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `formula_key`       VARCHAR(128)   NOT NULL                    COMMENT '公式唯一标识: formula.{category}.{name}',
    `formula_name`      VARCHAR(128)   NOT NULL                    COMMENT '公式名称',
    `formula_text`     VARCHAR(1000)  NOT NULL                    COMMENT 'Aviator表达式文本',
    `category`          VARCHAR(32)    NOT NULL                    COMMENT '分类: REIMBURSE/DRUG/DRG/GENERAL',
    `version`           INT            NOT NULL    DEFAULT 1       COMMENT '版本号',
    `status`            VARCHAR(16)    NOT NULL    DEFAULT 'draft' COMMENT '状态: draft/validated/active/inactive',
    `description`       VARCHAR(500)   DEFAULT NULL                COMMENT '公式描述',
    `is_validated`      TINYINT        NOT NULL    DEFAULT 0       COMMENT '语法校验: 0=未校验 1=通过 2=失败',
    `validated_msg`     VARCHAR(500)   DEFAULT NULL                COMMENT '校验信息',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    `create_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '创建人',
    `create_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '更新人',
    `update_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT        NOT NULL    DEFAULT 0       COMMENT '逻辑删除: 0=正常 1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_formula_key` (`tenant_id`, `formula_key`),
    KEY `idx_tenant_category` (`tenant_id`, `category`),
    KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公式定义表';

-- ------------------------------------------------------------
-- 5. 公式参数表 (formula_param)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `formula_param` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `formula_id`        BIGINT         NOT NULL                    COMMENT '所属公式ID',
    `param_name`        VARCHAR(64)    NOT NULL                    COMMENT '参数名',
    `param_type`        VARCHAR(32)    NOT NULL                    COMMENT '参数类型: BigDecimal/String/Integer/Boolean',
    `default_value`     VARCHAR(256)   DEFAULT NULL                COMMENT '默认值',
    `description`       VARCHAR(256)   DEFAULT NULL                COMMENT '参数描述',
    `param_order`       INT            NOT NULL    DEFAULT 0       COMMENT '参数顺序',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    `create_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '创建人',
    `create_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '更新人',
    `update_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT        NOT NULL    DEFAULT 0       COMMENT '逻辑删除: 0=正常 1=删除',
    PRIMARY KEY (`id`),
    KEY `idx_formula_id` (`formula_id`),
    KEY `idx_tenant_formula` (`tenant_id`, `formula_id`),
    CONSTRAINT `fk_formula_param` FOREIGN KEY (`formula_id`) REFERENCES `aviator_formula` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公式参数表';

-- ------------------------------------------------------------
-- 6. 公式历史表 (formula_history)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `formula_history` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `formula_id`        BIGINT         NOT NULL                    COMMENT '公式ID',
    `formula_key`       VARCHAR(128)   NOT NULL                    COMMENT '公式标识(快照)',
    `formula_text`     VARCHAR(1000)  NOT NULL                    COMMENT '表达式文本(快照)',
    `version`           INT            NOT NULL                    COMMENT '版本号',
    `status`            VARCHAR(16)    NOT NULL                    COMMENT '状态(快照)',
    `change_reason`     VARCHAR(512)   DEFAULT NULL                COMMENT '变更原因',
    `change_by`         VARCHAR(64)    NOT NULL                    COMMENT '变更人',
    `change_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_formula_id` (`formula_id`),
    KEY `idx_tenant_formula` (`tenant_id`, `formula_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公式历史表';

-- ------------------------------------------------------------
-- 7. 结算结果表 (settlement_result)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `settlement_result` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `settlement_no`     VARCHAR(64)    NOT NULL                    COMMENT '结算单号',
    `visit_id`          VARCHAR(64)    NOT NULL                    COMMENT '就诊ID',
    `patient_id`        VARCHAR(64)    NOT NULL                    COMMENT '患者ID',
    `patient_type`      VARCHAR(32)    NOT NULL                    COMMENT '患者类型: employee/resident/aid',
    `insurance_type`    VARCHAR(32)    DEFAULT NULL                COMMENT '医保类型',
    `hospital_level`    VARCHAR(16)    DEFAULT NULL                COMMENT '医院等级: 1/2/3',
    `total_fee`         DECIMAL(14,2)  NOT NULL                    COMMENT '总费用',
    `deductible`        DECIMAL(14,2)  DEFAULT NULL                COMMENT '起付线',
    `ratio`             DECIMAL(5,4)   DEFAULT NULL                COMMENT '报销比例',
    `reimburse_amount`  DECIMAL(14,2)  DEFAULT NULL                COMMENT '报销金额',
    `self_pay_amount`   DECIMAL(14,2)  DEFAULT NULL                COMMENT '自付金额',
    `result_level`       VARCHAR(16)    NOT NULL    DEFAULT 'PASS' COMMENT '结果级别: PASS/WARN/BLOCK',
    `skill_results`     JSON           DEFAULT NULL                COMMENT '各Skill执行结果(JSON数组)',
    `status`            VARCHAR(16)    NOT NULL    DEFAULT 'pending' COMMENT '结算状态: pending/completed/failed',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    `create_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '创建人',
    `create_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '更新人',
    `update_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_settlement_no` (`settlement_no`),
    UNIQUE KEY `uk_tenant_visit` (`tenant_id`, `visit_id`),
    KEY `idx_tenant_patient` (`tenant_id`, `patient_id`),
    KEY `idx_tenant_status` (`tenant_id`, `status`),
    KEY `idx_tenant_created` (`tenant_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算结果表';

-- ------------------------------------------------------------
-- 8. 审计日志表 (audit_log)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `audit_log` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    `action`            VARCHAR(32)    NOT NULL                    COMMENT '操作类型: CREATE/UPDATE/PUBLISH/DELETE/DEACTIVATE/EXECUTE',
    `target_type`       VARCHAR(32)    NOT NULL                    COMMENT '目标类型: RULE/FORMULA/SETTLEMENT/DRUG/QUALITY/DRG',
    `target_id`         VARCHAR(64)    NOT NULL                    COMMENT '目标ID',
    `target_key`        VARCHAR(128)   DEFAULT NULL                COMMENT '目标标识(规则KEY/公式KEY等)',
    `operator`          VARCHAR(64)    NOT NULL                    COMMENT '操作人',
    `detail`            TEXT           DEFAULT NULL                COMMENT '操作详情(JSON)',
    `ip_address`        VARCHAR(64)    DEFAULT NULL                COMMENT 'IP地址',
    `user_agent`        VARCHAR(256)   DEFAULT NULL                COMMENT '用户代理',
    `create_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_target` (`tenant_id`, `target_type`, `target_id`),
    KEY `idx_tenant_action` (`tenant_id`, `action`),
    KEY `idx_tenant_created` (`tenant_id`, `create_time`),
    KEY `idx_operator` (`operator`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- ------------------------------------------------------------
-- 9. 药品配伍禁忌表 (drug_interaction)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `drug_interaction` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `drug_code_a`       VARCHAR(64)    NOT NULL                    COMMENT '药品代码A',
    `drug_name_a`       VARCHAR(128)   NOT NULL                    COMMENT '药品名称A',
    `drug_code_b`       VARCHAR(64)    NOT NULL                    COMMENT '药品代码B',
    `drug_name_b`       VARCHAR(128)   NOT NULL                    COMMENT '药品名称B',
    `interaction_type`  VARCHAR(32)    NOT NULL                    COMMENT '相互作用类型: contraindication/warning/synergy',
    `severity_level`    VARCHAR(16)    NOT NULL                    COMMENT '严重程度: mild/moderate/severe',
    `description`       VARCHAR(500)   DEFAULT NULL                COMMENT '配伍说明',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    `is_enabled`        TINYINT        NOT NULL    DEFAULT 1       COMMENT '是否启用: 0=禁用 1=启用',
    `create_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '创建人',
    `create_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '更新人',
    `update_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT        NOT NULL    DEFAULT 0       COMMENT '逻辑删除: 0=正常 1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_drug_pair` (`tenant_id`, `drug_code_a`, `drug_code_b`),
    KEY `idx_drug_code_a` (`drug_code_a`),
    KEY `idx_drug_code_b` (`drug_code_b`),
    KEY `idx_interaction_type` (`interaction_type`),
    KEY `idx_tenant_enabled` (`tenant_id`, `is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品配伍禁忌表';

-- ------------------------------------------------------------
-- 10. DRG 分组定义表 (drg_definition)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `drg_definition` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `drg_code`          VARCHAR(32)    NOT NULL                    COMMENT 'DRG分组代码',
    `drg_name`          VARCHAR(128)   NOT NULL                    COMMENT 'DRG分组名称',
    `mdc_code`          VARCHAR(32)    DEFAULT NULL                COMMENT 'MDC主诊分类代码',
    `mdc_name`          VARCHAR(128)   DEFAULT NULL                COMMENT 'MDC主诊分类名称',
    `base_weight`       DECIMAL(10,4)  DEFAULT NULL                COMMENT '基础权重',
    `base_fee`          DECIMAL(14,2)  DEFAULT NULL                COMMENT '基础费用',
    `standard_score`   DECIMAL(10,2)  DEFAULT NULL                COMMENT '标准分值',
    `adjust_factor`     DECIMAL(5,4)   DEFAULT '1.0000'            COMMENT '调整因子',
    `description`       VARCHAR(500)   DEFAULT NULL                COMMENT '分组说明',
    `rules_text`        TEXT           DEFAULT NULL                COMMENT '分组规则(DRL或JSON)',
    `version`           INT            NOT NULL    DEFAULT 1       COMMENT '版本号',
    `status`            VARCHAR(16)    NOT NULL    DEFAULT 'active' COMMENT '状态: active/inactive',
    `effective_start`   DATETIME       DEFAULT NULL                COMMENT '生效开始时间',
    `effective_end`     DATETIME       DEFAULT NULL                COMMENT '生效结束时间',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    `create_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '创建人',
    `create_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '更新人',
    `update_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT        NOT NULL    DEFAULT 0       COMMENT '逻辑删除: 0=正常 1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_drg_code` (`tenant_id`, `drg_code`),
    KEY `idx_mdc_code` (`mdc_code`),
    KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DRG分组定义表';

-- ------------------------------------------------------------
-- 11. 就诊记录表 (visit_record) - 辅助表，用于重复结算拦截
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `visit_record` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `visit_id`          VARCHAR(64)    NOT NULL                    COMMENT '就诊ID',
    `patient_id`        VARCHAR(64)    NOT NULL                    COMMENT '患者ID',
    `visit_type`        VARCHAR(32)    NOT NULL                    COMMENT '就诊类型: outpatient/emergency/inpatient',
    `visit_date`        DATE           NOT NULL                    COMMENT '就诊日期',
    `department`        VARCHAR(64)    DEFAULT NULL                COMMENT '就诊科室',
    `doctor_id`         VARCHAR(64)    DEFAULT NULL                COMMENT '医生ID',
    `diagnosis_codes`   VARCHAR(500)   DEFAULT NULL                COMMENT '诊断编码(JSON数组)',
    `status`            VARCHAR(16)    NOT NULL    DEFAULT 'active' COMMENT '状态: active/closed',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    `create_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '创建人',
    `create_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '更新人',
    `update_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_visit` (`tenant_id`, `visit_id`),
    KEY `idx_tenant_patient` (`tenant_id`, `patient_id`),
    KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊记录表';

-- ------------------------------------------------------------
-- 12. 租户配置表 (tenant_config) - 多租户隔离配置
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tenant_config` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `tenant_id`         VARCHAR(64)    NOT NULL                    COMMENT '租户ID',
    `tenant_name`       VARCHAR(128)   NOT NULL                    COMMENT '租户名称',
    `contact_person`    VARCHAR(64)    DEFAULT NULL                COMMENT '联系人',
    `contact_phone`     VARCHAR(32)    DEFAULT NULL                COMMENT '联系电话',
    `status`            VARCHAR(16)    NOT NULL    DEFAULT 'active' COMMENT '状态: active/inactive',
    `config_json`       JSON           DEFAULT NULL                COMMENT '租户配置(JSON)',
    `create_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '创建人',
    `create_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '更新人',
    `update_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT        NOT NULL    DEFAULT 0       COMMENT '逻辑删除: 0=正常 1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_id` (`tenant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户配置表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 插入默认租户 (用于系统级配置)
INSERT INTO `tenant_config` (`tenant_id`, `tenant_name`, `contact_person`, `status`, `create_by`)
VALUES ('SYSTEM', '系统租户', 'system', 'active', 'system')
ON DUPLICATE KEY UPDATE `tenant_name` = VALUES(`tenant_name`);

-- 插入规则分组默认数据
INSERT INTO `rule_group` (`group_code`, `group_name`, `description`, `tenant_id`, `is_enabled`, `priority`, `create_by`)
VALUES
    ('REIMBURSEMENT', '医保结算规则组', '医保报销相关规则', 'SYSTEM', 1, 10, 'system'),
    ('DRUG_CHECK', '合理用药规则组', '处方审核、配伍禁忌等', 'SYSTEM', 1, 20, 'system'),
    ('QUALITY_CONTROL', '质控规则组', '院感防控、质控指标等', 'SYSTEM', 1, 30, 'system'),
    ('DRG_GROUPING', 'DRG分组规则组', 'DRG/DIP分组相关规则', 'SYSTEM', 1, 40, 'system')
ON DUPLICATE KEY UPDATE `group_name` = VALUES(`group_name`);

-- ============================================================
-- 创建索引优化 (针对高频查询)
-- ============================================================

-- 结算查询高频索引
CREATE INDEX IF NOT EXISTS `idx_settlement_patient_date` ON `settlement_result`(`tenant_id`, `patient_id`, `create_time`);
CREATE INDEX IF NOT EXISTS `idx_audit_log_date_range` ON `audit_log`(`tenant_id`, `create_time`, `action`);

-- ============================================================
-- 文档结束
-- ============================================================
