-- ============================================================
-- HIS 动态规则中台 - 用户认证表
-- 版本: V10
-- 创建日期: 2026-05-20
-- 说明: 创建用户认证表 his_auth_user，插入默认管理员账户
-- ============================================================

USE `his_rule_engine`;

-- ------------------------------------------------------------
-- 用户认证表 (his_auth_user)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `his_auth_user` (
    `id`                BIGINT         NOT NULL    AUTO_INCREMENT  COMMENT '主键',
    `username`          VARCHAR(64)    NOT NULL                    COMMENT '用户名',
    `password`          VARCHAR(128)   NOT NULL                    COMMENT 'BCrypt加密密码',
    `real_name`         VARCHAR(64)    DEFAULT NULL                COMMENT '真实姓名',
    `email`             VARCHAR(128)   DEFAULT NULL                COMMENT '邮箱',
    `phone`             VARCHAR(32)    DEFAULT NULL                COMMENT '手机号',
    `avatar`            VARCHAR(256)   DEFAULT NULL                COMMENT '头像URL',
    `role`              VARCHAR(32)    NOT NULL    DEFAULT 'operator' COMMENT '角色: super_admin/admin/operator/viewer',
    `tenant_id`         VARCHAR(64)    NOT NULL    DEFAULT ''      COMMENT '所属租户ID',
    `status`            VARCHAR(16)    NOT NULL    DEFAULT 'active' COMMENT '状态: active/disabled/locked',
    `login_fail_count`  INT            NOT NULL    DEFAULT 0       COMMENT '连续登录失败次数',
    `last_login_time`   DATETIME       DEFAULT NULL                COMMENT '最后登录时间',
    `last_login_ip`     VARCHAR(64)    DEFAULT NULL                COMMENT '最后登录IP',
    `locked_until`      DATETIME       DEFAULT NULL                COMMENT '锁定截止时间',
    `create_by`         VARCHAR(64)    DEFAULT 'system'            COMMENT '创建人',
    `create_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(64)    DEFAULT NULL                COMMENT '更新人',
    `update_time`       DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT        NOT NULL    DEFAULT 0       COMMENT '逻辑删除: 0=正常 1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_tenant_status` (`tenant_id`, `status`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证表';

-- ------------------------------------------------------------
-- 初始管理员账户
-- 密码: admin (BCrypt hash, rounds=10)
-- ------------------------------------------------------------
INSERT INTO `his_auth_user` (`username`, `password`, `real_name`, `role`, `tenant_id`, `status`, `create_by`)
VALUES ('admin', '$2b$10$fkgw/tQq0oVRPqDqorstPutBhHWAamt/5RgMqPvEPO6xz3EFIp/im', '系统管理员', 'super_admin', 'T001', 'active', 'system');

-- ------------------------------------------------------------
-- 演示用户
-- 密码: admin123 (BCrypt hash, rounds=10)
-- ------------------------------------------------------------
INSERT INTO `his_auth_user` (`username`, `password`, `real_name`, `role`, `tenant_id`, `status`, `create_by`)
VALUES ('operator01', '$2b$10$fkgw/tQq0oVRPqDqorstPutBhHWAamt/5RgMqPvEPO6xz3EFIp/im', '操作员01', 'operator', 'T001', 'active', 'system');
