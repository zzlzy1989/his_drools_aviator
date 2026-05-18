-- ============================================================
-- HIS 动态规则中台 V2.0 Phase 3: 监控大屏 + 规则市场
-- 版本: V3__monitor_market
-- 创建日期: 2026-05-17
-- 说明: 监控告警规则、指标记录、规则模板、模板订阅/评分/收藏、测试套件
-- ============================================================

USE `his_rule_engine`;

-- ------------------------------------------------------------
-- 1. 监控告警规则表 (monitor_alert_rule)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `monitor_alert_rule` (
    `id`                  BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `rule_name`          VARCHAR(128)   NOT NULL                    COMMENT '规则名称',
    `metric_name`        VARCHAR(64)   NOT NULL                    COMMENT '指标名称',
    `condition_type`     VARCHAR(16)   NOT NULL                    COMMENT '条件类型: GT/LT/EQ/GTE/LTE',
    `threshold`          DECIMAL(10,2) NOT NULL                    COMMENT '阈值',
    `level`              VARCHAR(16)   NOT NULL    DEFAULT 'WARN'  COMMENT '告警级别: WARN/ERROR',
    `enabled`            TINYINT        NOT NULL    DEFAULT 1       COMMENT '是否启用: 0=禁用 1=启用',
    `notify_channels`    VARCHAR(64)   DEFAULT NULL                COMMENT '通知渠道: DINGTALK/EMAIL',
    `notify_target`      VARCHAR(256)  DEFAULT NULL                COMMENT '通知目标 (webhook URL / 邮箱)',
    `message_template`   VARCHAR(512)  DEFAULT NULL                COMMENT '消息模板',
    `consecutive_triggers` INT          NOT NULL    DEFAULT 1       COMMENT '连续触发次数阈值',
    `cooldown_seconds`   INT            NOT NULL    DEFAULT 300     COMMENT '冷却时间(秒)',
    `tenant_id`          VARCHAR(64)    NOT NULL    DEFAULT 'T001'  COMMENT '租户ID',
    `create_by`          VARCHAR(64)    DEFAULT NULL                COMMENT '创建人',
    `create_time`        DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`          VARCHAR(64)    DEFAULT NULL                COMMENT '更新人',
    `update_time`        DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`            TINYINT        NOT NULL    DEFAULT 0       COMMENT '逻辑删除: 0=正常 1=删除',
    PRIMARY KEY (`id`),
    KEY `idx_enabled_tenant` (`enabled`, `tenant_id`),
    KEY `idx_metric_name` (`metric_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控告警规则表';

-- ------------------------------------------------------------
-- 2. 告警触发记录表 (monitor_alert_record)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `monitor_alert_record` (
    `id`                  BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `rule_id`            BIGINT         NOT NULL                    COMMENT '告警规则ID',
    `trigger_value`      DECIMAL(14,4) NOT NULL                    COMMENT '触发时的指标值',
    `threshold`          DECIMAL(14,4) NOT NULL                    COMMENT '告警阈值',
    `message`            VARCHAR(512)  DEFAULT NULL                COMMENT '告警消息',
    `notified_at`        DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '通知发送时间',
    `notification_result` VARCHAR(256)  DEFAULT NULL                COMMENT '通知结果 (成功/失败原因)',
    `tenant_id`          VARCHAR(64)    NOT NULL    DEFAULT 'T001'  COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_rule_trigger_time` (`rule_id`, `notified_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警触发记录表';

-- ------------------------------------------------------------
-- 3. 指标历史记录表 (metric_record)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `metric_record` (
    `id`                  BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `metric_name`        VARCHAR(64)   NOT NULL                    COMMENT '指标名称',
    `metric_value`       DECIMAL(14,4) NOT NULL                    COMMENT '指标值',
    `tags`               VARCHAR(256)  DEFAULT NULL                COMMENT '标签 (JSON格式)',
    `recorded_at`        DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    `tenant_id`          VARCHAR(64)    NOT NULL    DEFAULT 'T001'  COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_metric_recorded` (`metric_name`, `recorded_at`),
    KEY `idx_tenant_time` (`tenant_id`, `recorded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标历史记录表';

-- ------------------------------------------------------------
-- 4. 规则模板表 (rule_template)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `rule_template` (
    `id`                  BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `template_key`       VARCHAR(128)  NOT NULL                    COMMENT '模板唯一标识',
    `template_name`      VARCHAR(128)  NOT NULL                    COMMENT '模板名称',
    `description`        VARCHAR(512)  DEFAULT NULL                COMMENT '模板描述',
    `category`           VARCHAR(32)   NOT NULL                    COMMENT '分类: REIMBURSE/DRUG/QUALITY/DRG/GENERAL',
    `tags`               JSON          DEFAULT NULL                COMMENT '标签数组',
    `content`            JSON          NOT NULL                    COMMENT '规则内容 (rules/formulas/flows)',
    `provider_tenant_id`  VARCHAR(64)   NOT NULL                    COMMENT '提供者租户ID',
    `provider_tenant_name` VARCHAR(128) DEFAULT NULL                COMMENT '提供者租户名称',
    `published_by`       VARCHAR(64)   DEFAULT NULL                COMMENT '发布人',
    `published_at`       DATETIME      DEFAULT NULL                COMMENT '发布时间',
    `version`            VARCHAR(16)   NOT NULL    DEFAULT '1.0.0' COMMENT '版本号',
    `install_count`      INT            NOT NULL    DEFAULT 0       COMMENT '安装次数',
    `avg_rating`         DECIMAL(3,2)  DEFAULT NULL                COMMENT '平均评分',
    `comment_count`      INT            NOT NULL    DEFAULT 0       COMMENT '评论数',
    `status`             VARCHAR(16)   NOT NULL    DEFAULT 'PUBLISHED' COMMENT '状态: PUBLISHED/ARCHIVED',
    `tenant_id`          VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    `create_by`          VARCHAR(64)   DEFAULT NULL                COMMENT '创建人',
    `create_time`        DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`          VARCHAR(64)   DEFAULT NULL                COMMENT '更新人',
    `update_time`        DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_key` (`template_key`),
    KEY `idx_category_status` (`category`, `status`),
    KEY `idx_provider_tenant` (`provider_tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则模板表';

-- ------------------------------------------------------------
-- 5. 模板订阅表 (template_subscription)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `template_subscription` (
    `id`                  BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `template_id`        BIGINT         NOT NULL                    COMMENT '模板ID',
    `subscriber_tenant_id` VARCHAR(64)   NOT NULL                    COMMENT '订阅者租户ID',
    `installed_version`  VARCHAR(16)   NOT NULL                    COMMENT '安装版本',
    `install_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '安装时间',
    `status`             VARCHAR(16)   NOT NULL    DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/UPDATED/UNSUBSCRIBED',
    `create_time`        DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_subscriber` (`template_id`, `subscriber_tenant_id`),
    KEY `idx_subscriber_tenant` (`subscriber_tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板订阅表';

-- ------------------------------------------------------------
-- 6. 模板评论表 (template_comment)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `template_comment` (
    `id`                  BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `template_id`        BIGINT         NOT NULL                    COMMENT '模板ID',
    `tenant_id`          VARCHAR(64)   NOT NULL                    COMMENT '评论者租户ID',
    `tenant_name`       VARCHAR(128)  DEFAULT NULL                COMMENT '评论者租户名称',
    `rating`             TINYINT       NOT NULL                    COMMENT '评分1-5',
    `comment`           VARCHAR(1024) DEFAULT NULL                COMMENT '评论内容',
    `reply_to`          BIGINT        DEFAULT NULL                COMMENT '回复的评论ID',
    `create_time`        DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板评论表';

-- ------------------------------------------------------------
-- 7. 模板收藏表 (template_favorite)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `template_favorite` (
    `id`                  BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `template_id`        BIGINT         NOT NULL                    COMMENT '模板ID',
    `tenant_id`          VARCHAR(64)   NOT NULL                    COMMENT '收藏者租户ID',
    `create_time`        DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_tenant` (`template_id`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板收藏表';

-- ------------------------------------------------------------
-- 8. 测试套件表 (test_suite)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `test_suite` (
    `id`                  BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `suite_name`         VARCHAR(128)   NOT NULL                    COMMENT '套件名称',
    `description`        VARCHAR(512)  DEFAULT NULL                COMMENT '套件描述',
    `category`           VARCHAR(32)   NOT NULL                    COMMENT '分类: SETTLEMENT/DRUG/QUALITY/DRG',
    `suite_config`       JSON          DEFAULT NULL                COMMENT '套件配置 (包含的数据集ID列表)',
    `tenant_id`          VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    `create_by`          VARCHAR(64)   DEFAULT NULL                COMMENT '创建人',
    `create_time`        DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`          VARCHAR(64)   DEFAULT NULL                COMMENT '更新人',
    `update_time`        DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`            TINYINT        NOT NULL    DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_tenant` (`category`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试套件表';

-- ------------------------------------------------------------
-- 9. 测试用例扩展表 (test_case) - 独立于数据集的用例管理
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `test_case` (
    `id`                  BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `case_name`          VARCHAR(128)   NOT NULL                    COMMENT '用例名称',
    `case_description`  VARCHAR(512)   DEFAULT NULL                COMMENT '用例描述',
    `rule_key`           VARCHAR(128)  DEFAULT NULL                COMMENT '关联的规则Key',
    `formula_key`        VARCHAR(128)  DEFAULT NULL                COMMENT '关联的公式Key',
    `fact_template`     JSON          NOT NULL                    COMMENT '输入Fact模板',
    `expected_template`  JSON          DEFAULT NULL                COMMENT '预期结果模板',
    `variables`          JSON          DEFAULT NULL                COMMENT 'Aviator变量',
    `category`           VARCHAR(32)   NOT NULL                    COMMENT '分类: SETTLEMENT/DRUG/QUALITY/DRG',
    `tenant_id`          VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    `create_by`          VARCHAR(64)   DEFAULT NULL                COMMENT '创建人',
    `create_time`        DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`          VARCHAR(64)   DEFAULT NULL                COMMENT '更新人',
    `update_time`        DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`            TINYINT        NOT NULL    DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_rule_key` (`rule_key`),
    KEY `idx_category_tenant` (`category`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试用例表';

-- ------------------------------------------------------------
-- 10. 测试执行记录表 (test_execution_log) - 独立执行历史
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `test_execution_log` (
    `id`                  BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `case_id`            VARCHAR(64)    NOT NULL                    COMMENT '测试用例ID',
    `case_name`         VARCHAR(128)   DEFAULT NULL                COMMENT '用例名称 (冗余存储)',
    `suite_id`          BIGINT         DEFAULT NULL                COMMENT '所属套件ID',
    `data_set_id`       BIGINT         DEFAULT NULL                COMMENT '所属数据集ID',
    `status`            VARCHAR(16)   NOT NULL                    COMMENT '状态: RUNNING/SUCCESS/FAILED/ERROR',
    `actual_result`     JSON           DEFAULT NULL                COMMENT '实际结果',
    `expected_result`   JSON           DEFAULT NULL                COMMENT '预期结果',
    `diff_result`        JSON           DEFAULT NULL                COMMENT '差异详情',
    `execute_ms`        INT            DEFAULT NULL                COMMENT '执行耗时(ms)',
    `error_message`     TEXT           DEFAULT NULL                COMMENT '错误信息',
    `execute_time`      DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_case_status` (`case_id`, `status`),
    KEY `idx_tenant_time` (`tenant_id`, `execute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试执行记录表';

-- ============================================================
-- 索引优化建议 (针对高频查询)
-- ============================================================

-- monitor_alert_rule: 按租户+启用状态查询
CREATE INDEX idx_alert_tenant_enabled ON monitor_alert_rule(tenant_id, enabled);

-- metric_record: 按时间范围查询指标趋势
CREATE INDEX idx_metric_time_range ON metric_record(metric_name, recorded_at);

-- rule_template: 按分类+评分排序
CREATE INDEX idx_template_category_rating ON rule_template(category, avg_rating DESC);

-- test_execution_log: 按执行时间查询最近记录
CREATE INDEX idx_execution_recent ON test_execution_log(tenant_id, execute_time DESC);