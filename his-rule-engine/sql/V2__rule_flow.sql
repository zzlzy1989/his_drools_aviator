-- ============================================================
-- HIS 动态规则中台 V2.0 Phase 1: 规则可视化编排
-- 版本: V2__rule_flow
-- 创建日期: 2026-05-01
-- 说明: 规则流定义表和历史版本表
-- ============================================================

USE `his_rule_engine`;

-- ------------------------------------------------------------
-- 1. 规则流定义表 (rule_flow)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `rule_flow` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `flow_key`          VARCHAR(128)   NOT NULL                    COMMENT '规则流唯一标识',
    `flow_name`         VARCHAR(128)   NOT NULL                    COMMENT '规则流名称',
    `flow_definition`   JSON           NOT NULL                    COMMENT '规则流图定义',
    `version`           INT            NOT NULL    DEFAULT 1       COMMENT '版本号',
    `status`            VARCHAR(16)    NOT NULL    DEFAULT 'draft' COMMENT '状态: draft/active/inactive',
    `category`          VARCHAR(32)    DEFAULT NULL                COMMENT '分类: SETTLEMENT/DRUG/QUALITY/DRG',
    `description`       VARCHAR(500)   DEFAULT NULL                COMMENT '规则流描述',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    `create_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '创建人',
    `create_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '更新人',
    `update_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT        NOT NULL    DEFAULT 0       COMMENT '逻辑删除: 0=正常 1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_flow_key_tenant` (`flow_key`, `tenant_id`),
    KEY `idx_status_tenant` (`status`, `tenant_id`),
    KEY `idx_category_tenant` (`category`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则流定义表';

-- ------------------------------------------------------------
-- 2. 规则流历史版本表 (rule_flow_history)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `rule_flow_history` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `flow_id`           BIGINT         NOT NULL                    COMMENT '规则流ID',
    `version`           INT            NOT NULL                    COMMENT '版本号',
    `flow_definition`   JSON           NOT NULL                    COMMENT '规则流定义快照',
    `change_desc`       VARCHAR(512)   DEFAULT NULL                COMMENT '变更说明',
    `change_by`         VARCHAR(64)    NOT NULL                    COMMENT '变更人',
    `change_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    PRIMARY KEY (`id`),
    KEY `idx_flow_version` (`flow_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则流历史版本表';

-- ------------------------------------------------------------
-- 3. 测试数据集表 (test_data_set) - Phase 2
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `test_data_set` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `set_name`          VARCHAR(128)   NOT NULL                    COMMENT '数据集名称',
    `set_description`   VARCHAR(512)   DEFAULT NULL                COMMENT '描述',
    `category`          VARCHAR(32)    NOT NULL                    COMMENT '分类: SETTLEMENT/DRUG/QUALITY/DRG',
    `test_cases`        JSON           NOT NULL                    COMMENT '测试用例数组',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    `create_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '创建人',
    `create_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '更新人',
    `update_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT        NOT NULL    DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_tenant` (`category`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试数据集表';

-- ------------------------------------------------------------
-- 4. 沙箱执行日志表 (sandbox_execution_log) - Phase 2
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sandbox_execution_log` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `data_set_id`       BIGINT         NOT NULL                    COMMENT '数据集ID',
    `case_id`           VARCHAR(64)   NOT NULL                    COMMENT '测试用例ID',
    `status`            VARCHAR(16)   NOT NULL                    COMMENT 'RUNNING/SUCCESS/FAILED/ERROR',
    `actual_result`     JSON           DEFAULT NULL                COMMENT '实际结果',
    `expected_result`   JSON           DEFAULT NULL                COMMENT '预期结果',
    `diff_result`       JSON           DEFAULT NULL                COMMENT '差异详情',
    `execute_ms`        INT            DEFAULT NULL                COMMENT '执行耗时(ms)',
    `error_message`     TEXT           DEFAULT NULL                COMMENT '错误信息',
    `execute_time`      DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_dataset_case` (`data_set_id`, `case_id`),
    KEY `idx_tenant_time` (`tenant_id`, `execute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='沙箱执行日志表';