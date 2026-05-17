-- 测试沙箱表初始化脚本
-- 创建日期: 2026-05-13

CREATE TABLE IF NOT EXISTS `test_data_set` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `data_set_name`   VARCHAR(128) NOT NULL COMMENT '数据集名称',
    `description`     VARCHAR(512) DEFAULT NULL COMMENT '描述',
    `category`        VARCHAR(64)  DEFAULT NULL COMMENT '分类: settlement/drg/drug/quality',
    `tenant_id`       VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '租户ID',
    `create_by`       VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常 1=删除',
    PRIMARY KEY (`id`),
    KEY `idx_tenant` (`tenant_id`),
    KEY `idx_tenant_deleted` (`tenant_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试数据集';

CREATE TABLE IF NOT EXISTS `test_case` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `data_set_id`     BIGINT       NOT NULL COMMENT '所属数据集ID',
    `case_id`         VARCHAR(64)  DEFAULT NULL COMMENT '用例编号',
    `case_name`       VARCHAR(128) NOT NULL COMMENT '用例名称',
    `fact_json`       TEXT         DEFAULT NULL COMMENT '输入数据JSON',
    `expected_json`   TEXT         DEFAULT NULL COMMENT '期望结果JSON',
    `status`          INT          DEFAULT 0 COMMENT '状态: 0=未执行 1=通过 2=失败',
    `last_result`     VARCHAR(32)  DEFAULT NULL COMMENT '最后执行结果: SUCCESS/FAILED',
    `tenant_id`       VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '租户ID',
    `create_by`       VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常 1=删除',
    PRIMARY KEY (`id`),
    KEY `idx_data_set` (`data_set_id`),
    KEY `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试用例';