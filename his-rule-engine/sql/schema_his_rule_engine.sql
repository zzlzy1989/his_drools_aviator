-- MySQL dump 10.13  Distrib 8.0.45, for Linux (x86_64)
--
-- Host: 192.168.1.105    Database: his_rule_engine
-- ------------------------------------------------------
-- Server version	8.0.45-0ubuntu0.24.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `audit_log`
--

DROP TABLE IF EXISTS `audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  `action` varchar(32) NOT NULL COMMENT '操作类型: CREATE/UPDATE/PUBLISH/DELETE/DEACTIVATE/EXECUTE',
  `target_type` varchar(32) NOT NULL COMMENT '目标类型: RULE/FORMULA/SETTLEMENT/DRUG/QUALITY/DRG',
  `target_id` varchar(64) NOT NULL COMMENT '目标ID',
  `target_key` varchar(128) DEFAULT NULL COMMENT '目标标识(规则KEY/公式KEY等)',
  `operator` varchar(64) NOT NULL COMMENT '操作人',
  `detail` text COMMENT '操作详情(JSON)',
  `ip_address` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(256) DEFAULT NULL COMMENT '用户代理',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_target` (`tenant_id`,`target_type`,`target_id`),
  KEY `idx_tenant_action` (`tenant_id`,`action`),
  KEY `idx_tenant_created` (`tenant_id`,`create_time`),
  KEY `idx_operator` (`operator`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审计日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `aviator_formula`
--

DROP TABLE IF EXISTS `aviator_formula`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `aviator_formula` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `formula_key` varchar(128) NOT NULL COMMENT '公式唯一标识: formula.{category}.{name}',
  `formula_name` varchar(128) NOT NULL COMMENT '公式名称',
  `formula_text` varchar(1000) NOT NULL COMMENT 'Aviator表达式文本',
  `category` varchar(32) NOT NULL COMMENT '分类: REIMBURSE/DRUG/DRG/GENERAL',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `status` varchar(16) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/validated/active/inactive',
  `description` varchar(500) DEFAULT NULL COMMENT '公式描述',
  `is_validated` tinyint NOT NULL DEFAULT '0' COMMENT '语法校验: 0=未校验 1=通过 2=失败',
  `validated_msg` varchar(500) DEFAULT NULL COMMENT '校验信息',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0=正常 1=删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_formula_key` (`tenant_id`,`formula_key`),
  KEY `idx_tenant_category` (`tenant_id`,`category`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公式定义表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `drg_definition`
--

DROP TABLE IF EXISTS `drg_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drg_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `drg_code` varchar(32) NOT NULL COMMENT 'DRG分组代码',
  `drg_name` varchar(128) NOT NULL COMMENT 'DRG分组名称',
  `mdc_code` varchar(32) DEFAULT NULL COMMENT 'MDC主诊分类代码',
  `mdc_name` varchar(128) DEFAULT NULL COMMENT 'MDC主诊分类名称',
  `base_weight` decimal(10,4) DEFAULT NULL COMMENT '基础权重',
  `base_fee` decimal(14,2) DEFAULT NULL COMMENT '基础费用',
  `standard_score` decimal(10,2) DEFAULT NULL COMMENT '标准分值',
  `adjust_factor` decimal(5,4) DEFAULT '1.0000' COMMENT '调整因子',
  `description` varchar(500) DEFAULT NULL COMMENT '分组说明',
  `rules_text` text COMMENT '分组规则(DRL或JSON)',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `status` varchar(16) NOT NULL DEFAULT 'active' COMMENT '状态: active/inactive',
  `effective_start` datetime DEFAULT NULL COMMENT '生效开始时间',
  `effective_end` datetime DEFAULT NULL COMMENT '生效结束时间',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0=正常 1=删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_drg_code` (`tenant_id`,`drg_code`),
  KEY `idx_mdc_code` (`mdc_code`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='DRG分组定义表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `drug_catalog`
--

DROP TABLE IF EXISTS `drug_catalog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drug_catalog` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `drug_code` varchar(64) NOT NULL COMMENT '药品编码',
  `drug_name` varchar(128) NOT NULL COMMENT '药品名称',
  `specification` varchar(128) DEFAULT NULL COMMENT '规格',
  `dosage_unit` varchar(32) DEFAULT NULL COMMENT '剂量单位',
  `drug_type` varchar(32) DEFAULT NULL COMMENT '药品类型',
  `category` varchar(32) DEFAULT NULL COMMENT '分类',
  `reimbursement_type` varchar(32) DEFAULT NULL COMMENT '报销类型',
  `limit_price` decimal(14,2) DEFAULT NULL COMMENT '限价',
  `hospital_level` varchar(32) DEFAULT NULL COMMENT '医院级别',
  `manufacturer` varchar(128) DEFAULT NULL COMMENT '生产厂家',
  `is_enabled` varchar(8) DEFAULT '1' COMMENT '是否启用',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0=正常 1=删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant` (`tenant_id`),
  KEY `idx_drug_code` (`drug_code`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb3 COMMENT='药品目录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `drug_interaction`
--

DROP TABLE IF EXISTS `drug_interaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drug_interaction` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `drug_code_a` varchar(64) NOT NULL COMMENT '药品代码A',
  `drug_name_a` varchar(128) NOT NULL COMMENT '药品名称A',
  `drug_code_b` varchar(64) NOT NULL COMMENT '药品代码B',
  `drug_name_b` varchar(128) NOT NULL COMMENT '药品名称B',
  `interaction_type` varchar(32) NOT NULL COMMENT '相互作用类型: contraindication/warning/synergy',
  `severity_level` varchar(16) NOT NULL COMMENT '严重程度: mild/moderate/severe',
  `description` varchar(500) DEFAULT NULL COMMENT '配伍说明',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  `is_enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用: 0=禁用 1=启用',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0=正常 1=删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_drug_pair` (`tenant_id`,`drug_code_a`,`drug_code_b`),
  KEY `idx_drug_code_a` (`drug_code_a`),
  KEY `idx_drug_code_b` (`drug_code_b`),
  KEY `idx_interaction_type` (`interaction_type`),
  KEY `idx_tenant_enabled` (`tenant_id`,`is_enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='药品配伍禁忌表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `formula_history`
--

DROP TABLE IF EXISTS `formula_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `formula_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `formula_id` bigint NOT NULL COMMENT '公式ID',
  `formula_key` varchar(128) NOT NULL COMMENT '公式标识(快照)',
  `formula_text` varchar(1000) NOT NULL COMMENT '表达式文本(快照)',
  `version` int NOT NULL COMMENT '版本号',
  `status` varchar(16) NOT NULL COMMENT '状态(快照)',
  `change_reason` varchar(512) DEFAULT NULL COMMENT '变更原因',
  `change_by` varchar(64) NOT NULL COMMENT '变更人',
  `change_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_formula_id` (`formula_id`),
  KEY `idx_tenant_formula` (`tenant_id`,`formula_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公式历史表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `formula_param`
--

DROP TABLE IF EXISTS `formula_param`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `formula_param` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `formula_id` bigint NOT NULL COMMENT '所属公式ID',
  `param_name` varchar(64) NOT NULL COMMENT '参数名',
  `param_type` varchar(32) NOT NULL COMMENT '参数类型: BigDecimal/String/Integer/Boolean',
  `default_value` varchar(256) DEFAULT NULL COMMENT '默认值',
  `description` varchar(256) DEFAULT NULL COMMENT '参数描述',
  `param_order` int NOT NULL DEFAULT '0' COMMENT '参数顺序',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0=正常 1=删除',
  PRIMARY KEY (`id`),
  KEY `idx_formula_id` (`formula_id`),
  KEY `idx_tenant_formula` (`tenant_id`,`formula_id`),
  CONSTRAINT `fk_formula_param` FOREIGN KEY (`formula_id`) REFERENCES `aviator_formula` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公式参数表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient_allergy`
--

DROP TABLE IF EXISTS `patient_allergy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_allergy` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `patient_id` varchar(64) NOT NULL COMMENT '患者ID',
  `drug_code` varchar(64) NOT NULL COMMENT '药品编码',
  `drug_name` varchar(128) DEFAULT NULL COMMENT '药品名称',
  `allergy_type` varchar(32) DEFAULT NULL COMMENT '过敏类型',
  `severity_level` varchar(16) DEFAULT NULL COMMENT '严重程度',
  `reaction` varchar(256) DEFAULT NULL COMMENT '过敏反应',
  `memo` varchar(512) DEFAULT NULL COMMENT '备注',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0=正常 1=删除',
  PRIMARY KEY (`id`),
  KEY `idx_patient` (`patient_id`),
  KEY `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3 COMMENT='患者过敏史表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `quality_definition`
--

DROP TABLE IF EXISTS `quality_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quality_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_key` varchar(100) NOT NULL COMMENT '项目Key',
  `item_name` varchar(200) NOT NULL COMMENT '项目名称',
  `rule_text` text COMMENT 'DRL规则内容',
  `category` varchar(50) NOT NULL COMMENT '分类',
  `level` varchar(20) NOT NULL COMMENT '等级',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `salience` int NOT NULL DEFAULT '0' COMMENT '规则优先级',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态',
  `tenant_id` varchar(64) NOT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_key` (`item_key`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质控规则定义表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rule_definition`
--

DROP TABLE IF EXISTS `rule_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rule_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_group_id` bigint DEFAULT NULL COMMENT '所属规则组ID',
  `rule_key` varchar(128) NOT NULL COMMENT '规则唯一标识: rule.{category}.{name}',
  `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `rule_text` text NOT NULL COMMENT 'DRL规则内容',
  `category` varchar(32) NOT NULL COMMENT '分类: reimbursement/drug/quality/drg',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `status` varchar(16) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/validated/active/inactive',
  `description` varchar(500) DEFAULT NULL COMMENT '规则描述',
  `salience` int NOT NULL DEFAULT '0' COMMENT '优先级(越大越先执行)',
  `activation_group` varchar(64) DEFAULT NULL COMMENT '激活组',
  `effective_start` datetime DEFAULT NULL COMMENT '生效开始时间',
  `effective_end` datetime DEFAULT NULL COMMENT '生效结束时间',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0=正常 1=删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_rule_key` (`tenant_id`,`rule_key`),
  KEY `idx_group` (`rule_group_id`),
  KEY `idx_tenant_category` (`tenant_id`,`category`),
  KEY `idx_tenant_status` (`tenant_id`,`status`),
  CONSTRAINT `fk_rule_group` FOREIGN KEY (`rule_group_id`) REFERENCES `rule_group` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='规则定义表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rule_flow`
--

DROP TABLE IF EXISTS `rule_flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rule_flow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `flow_key` varchar(128) NOT NULL COMMENT '规则流唯一标识',
  `flow_name` varchar(128) NOT NULL COMMENT '规则流名称',
  `flow_definition` json NOT NULL COMMENT '规则流图定义',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `status` varchar(16) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/active/inactive',
  `category` varchar(32) DEFAULT NULL COMMENT '分类: SETTLEMENT/DRUG/QUALITY/DRG',
  `description` varchar(500) DEFAULT NULL COMMENT '规则流描述',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0=正常 1=删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_key_tenant` (`flow_key`,`tenant_id`),
  KEY `idx_status_tenant` (`status`,`tenant_id`),
  KEY `idx_category_tenant` (`category`,`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='规则流定义表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rule_flow_history`
--

DROP TABLE IF EXISTS `rule_flow_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rule_flow_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `flow_id` bigint NOT NULL COMMENT '规则流ID',
  `version` int NOT NULL COMMENT '版本号',
  `flow_definition` json NOT NULL COMMENT '规则流定义快照',
  `change_desc` varchar(512) DEFAULT NULL COMMENT '变更说明',
  `change_by` varchar(64) NOT NULL COMMENT '变更人',
  `change_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  PRIMARY KEY (`id`),
  KEY `idx_flow_version` (`flow_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='规则流历史版本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rule_group`
--

DROP TABLE IF EXISTS `rule_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rule_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `group_code` varchar(64) NOT NULL COMMENT '规则组编码: REIMBURSEMENT/DRUG_CHECK/INFECTION_CONTROL/DRG',
  `group_name` varchar(128) NOT NULL COMMENT '规则组名称',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID(医院标识)',
  `is_enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用: 0=禁用 1=启用',
  `priority` int NOT NULL DEFAULT '0' COMMENT '组优先级(越小越先)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0=正常 1=删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_tenant` (`group_code`,`tenant_id`),
  KEY `idx_tenant_enabled` (`tenant_id`,`is_enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='规则分组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rule_history`
--

DROP TABLE IF EXISTS `rule_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rule_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `rule_key` varchar(128) NOT NULL COMMENT '规则标识(快照)',
  `rule_text` text NOT NULL COMMENT 'DRL规则内容(快照)',
  `version` int NOT NULL COMMENT '版本号',
  `status` varchar(16) NOT NULL COMMENT '状态(快照)',
  `change_reason` varchar(512) DEFAULT NULL COMMENT '变更原因',
  `change_by` varchar(64) NOT NULL COMMENT '变更人',
  `change_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_tenant_rule` (`tenant_id`,`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='规则历史表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sandbox_execution_log`
--

DROP TABLE IF EXISTS `sandbox_execution_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sandbox_execution_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `data_set_id` bigint NOT NULL COMMENT '数据集ID',
  `case_id` varchar(64) NOT NULL COMMENT '测试用例ID',
  `status` varchar(16) NOT NULL COMMENT 'RUNNING/SUCCESS/FAILED/ERROR',
  `actual_result` json DEFAULT NULL COMMENT '实际结果',
  `expected_result` json DEFAULT NULL COMMENT '预期结果',
  `diff_result` json DEFAULT NULL COMMENT '差异详情',
  `execute_ms` int DEFAULT NULL COMMENT '执行耗时(ms)',
  `error_message` text COMMENT '错误信息',
  `execute_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_dataset_case` (`data_set_id`,`case_id`),
  KEY `idx_tenant_time` (`tenant_id`,`execute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='沙箱执行日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `settlement_result`
--

DROP TABLE IF EXISTS `settlement_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `settlement_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `settlement_no` varchar(64) NOT NULL COMMENT '结算单号',
  `visit_id` varchar(64) NOT NULL COMMENT '就诊ID',
  `patient_id` varchar(64) NOT NULL COMMENT '患者ID',
  `patient_name` varchar(64) DEFAULT NULL COMMENT '患者姓名',
  `patient_type` varchar(32) NOT NULL COMMENT '患者类型: employee/resident/aid',
  `insurance_type` varchar(32) DEFAULT NULL COMMENT '医保类型',
  `hospital_level` varchar(16) DEFAULT NULL COMMENT '医院等级: 1/2/3',
  `total_fee` decimal(14,2) NOT NULL COMMENT '总费用',
  `deductible` decimal(14,2) DEFAULT NULL COMMENT '起付线',
  `ratio` decimal(5,4) DEFAULT NULL COMMENT '报销比例',
  `reimburse_amount` decimal(14,2) DEFAULT NULL COMMENT '报销金额',
  `self_pay_amount` decimal(14,2) DEFAULT NULL COMMENT '自付金额',
  `result_level` varchar(16) NOT NULL DEFAULT 'PASS' COMMENT '结果级别: PASS/WARN/BLOCK',
  `skill_results` json DEFAULT NULL COMMENT '各Skill执行结果(JSON数组)',
  `status` varchar(16) NOT NULL DEFAULT 'pending' COMMENT '结算状态: pending/completed/failed',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`),
  UNIQUE KEY `uk_tenant_visit` (`tenant_id`,`visit_id`),
  KEY `idx_tenant_patient` (`tenant_id`,`patient_id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`),
  KEY `idx_tenant_created` (`tenant_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='结算结果表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenant_config`
--

DROP TABLE IF EXISTS `tenant_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(64) NOT NULL COMMENT '租户ID',
  `tenant_name` varchar(128) NOT NULL COMMENT '租户名称',
  `contact_person` varchar(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `status` varchar(16) NOT NULL DEFAULT 'active' COMMENT '状态: active/inactive',
  `config_json` json DEFAULT NULL COMMENT '租户配置(JSON)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0=正常 1=删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `test_data_set`
--

DROP TABLE IF EXISTS `test_data_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `test_data_set` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `set_name` varchar(128) NOT NULL COMMENT '数据集名称',
  `set_description` varchar(512) DEFAULT NULL COMMENT '描述',
  `category` varchar(32) NOT NULL COMMENT '分类: SETTLEMENT/DRUG/QUALITY/DRG',
  `test_cases` json NOT NULL COMMENT '测试用例数组',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_tenant` (`category`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测试数据集表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `visit_record`
--

DROP TABLE IF EXISTS `visit_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visit_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `visit_id` varchar(64) NOT NULL COMMENT '就诊ID',
  `patient_id` varchar(64) NOT NULL COMMENT '患者ID',
  `visit_type` varchar(32) NOT NULL COMMENT '就诊类型: outpatient/emergency/inpatient',
  `visit_date` date NOT NULL COMMENT '就诊日期',
  `department` varchar(64) DEFAULT NULL COMMENT '就诊科室',
  `doctor_id` varchar(64) DEFAULT NULL COMMENT '医生ID',
  `diagnosis_codes` varchar(500) DEFAULT NULL COMMENT '诊断编码(JSON数组)',
  `status` varchar(16) NOT NULL DEFAULT 'active' COMMENT '状态: active/closed',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_visit` (`tenant_id`,`visit_id`),
  KEY `idx_tenant_patient` (`tenant_id`,`patient_id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='就诊记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-09 23:33:00
