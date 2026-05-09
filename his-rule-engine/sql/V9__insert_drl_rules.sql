-- ============================================================
-- HIS 动态规则中台 - DRL 规则定义初始化脚本 V9
-- 创建日期: 2026-05-08
-- 说明: 插入职工医保/居民医保结算 DRL 规则，与 Skill 管道配合
-- 依赖: V1__init_schema.sql, V2__insert_rule_groups.sql
-- ============================================================

USE `his_rule_engine`;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 职工医保结算规则组 (rule_group_id = 1)
-- ============================================================

INSERT INTO `rule_definition` (`rule_group_id`, `rule_key`, `rule_name`, `rule_text`, `category`, `version`, `status`, `description`, `salience`, `activation_group`, `tenant_id`, `create_by`) VALUES
(1, 'rule.reimbursement.employee_deductible', '职工起付线规则',
'package com.his.rules.reimbursement;

import com.his.common.SettlementFact;
import java.math.BigDecimal;

/**
 * R01: 职工起付线设置
 * 业务含义: 职工医保起付线固定为 1000 元
 * 优先级: 90 (身份校验后执行)
 * 依赖: InsuranceIdentitySkill 已通过身份校验
 */
rule "1. 职工起付线规则"
    salience 90
    when
        $f: SettlementFact(
            patientType == "employee",
            deductible == null
        )
    then
        $f.setDeductible(new BigDecimal("1000"));
        log.info("起付线已设置: patientId={}, type=employee, deductible=1000", $f.getPatientId());
end',
'reimbursement', 1, 'active', '职工医保起付线设置规则（1000元）', 90, 'employee_deductible', 'T001', 'system'),

(1, 'rule.reimbursement.employee_ratio', '职工报销比例规则',
'package com.his.rules.reimbursement;

import com.his.common.SettlementFact;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * R02: 职工报销比例计算
 * 业务含义: 职工医保基础报销比例 85%，三级医院降低至 76.5%
 * 优先级: 80 (起付线设置后执行)
 */
rule "2. 职工报销比例规则"
    salience 80
    when
        $f: SettlementFact(
            patientType == "employee",
            deductible != null,
            ratio == null
        )
    then
        BigDecimal baseRatio = new BigDecimal("0.85");
        
        // 三级医院报销比例降低 10%
        if ("三级".equals($f.getHospitalLevel()) || "3".equals($f.getHospitalLevel())) {
            baseRatio = baseRatio.multiply(new BigDecimal("0.9")).setScale(4, RoundingMode.HALF_UP);
        }
        
        $f.setRatio(baseRatio);
        log.info("职工报销比例: patientId={}, hospitalLevel={}, ratio={}", 
            $f.getPatientId(), $f.getHospitalLevel(), baseRatio);
end',
'reimbursement', 1, 'active', '职工医保报销比例计算规则', 80, 'employee_ratio', 'T001', 'system'),

(1, 'rule.reimbursement.deductible_check', '起付线检查规则',
'package com.his.rules.reimbursement;

import com.his.common.SettlementFact;
import com.his.common.ResultLevel;
import com.his.common.SkillResult;
import java.util.List;

/**
 * R03: 起付线检查
 * 业务含义: 总费用未达起付线时，拦截报销
 * 优先级: 70 (比例计算后执行)
 */
rule "3. 起付线检查规则"
    salience 70
    when
        $f: SettlementFact(
            totalFee != null,
            deductible != null,
            ratio != null,
            totalFee <= deductible
        )
        $results: List()
    then
        $f.addResult(new SkillResult(ResultLevel.WARN, "DeductibleCheck", "未达到起付线"));
        log.warn("起付线检查: patientId={}, totalFee={}, deductible={}", 
            $f.getPatientId(), $f.getTotalFee(), $f.getDeductible());
end',
'reimbursement', 1, 'active', '起付线检查规则（未达起付线拦截）', 70, 'deductible_check', 'T001', 'system');

-- ============================================================
-- 居民医保结算规则组 (rule_group_id = 2)
-- ============================================================

INSERT INTO `rule_definition` (`rule_group_id`, `rule_key`, `rule_name`, `rule_text`, `category`, `version`, `status`, `description`, `salience`, `activation_group`, `tenant_id`, `create_by`) VALUES
(2, 'rule.reimbursement.resident_deductible', '居民起付线规则',
'package com.his.rules.reimbursement;

import com.his.common.SettlementFact;
import java.math.BigDecimal;

/**
 * R01: 居民起付线设置
 * 业务含义: 居民医保起付线固定为 500 元
 * 优先级: 90
 */
rule "1. 居民起付线规则"
    salience 90
    when
        $f: SettlementFact(
            patientType == "resident",
            deductible == null
        )
    then
        $f.setDeductible(new BigDecimal("500"));
        log.info("起付线已设置: patientId={}, type=resident, deductible=500", $f.getPatientId());
end',
'reimbursement', 1, 'active', '居民医保起付线设置规则（500元）', 90, 'resident_deductible', 'T001', 'system'),

(2, 'rule.reimbursement.resident_ratio', '居民报销比例规则',
'package com.his.rules.reimbursement;

import com.his.common.SettlementFact;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * R02: 居民报销比例计算
 * 业务含义: 居民医保基础报销比例 65%，三级医院降低至 58.5%
 * 优先级: 80
 */
rule "2. 居民报销比例规则"
    salience 80
    when
        $f: SettlementFact(
            patientType == "resident",
            deductible != null,
            ratio == null
        )
    then
        BigDecimal baseRatio = new BigDecimal("0.65");
        
        // 三级医院报销比例降低 10%
        if ("三级".equals($f.getHospitalLevel()) || "3".equals($f.getHospitalLevel())) {
            baseRatio = baseRatio.multiply(new BigDecimal("0.9")).setScale(4, RoundingMode.HALF_UP);
        }
        
        $f.setRatio(baseRatio);
        log.info("居民报销比例: patientId={}, hospitalLevel={}, ratio={}", 
            $f.getPatientId(), $f.getHospitalLevel(), baseRatio);
end',
'reimbursement', 1, 'active', '居民医保报销比例计算规则', 80, 'resident_ratio', 'T001', 'system'),

(2, 'rule.reimbursement.resident_amount_calc', '居民报销金额计算',
'package com.his.rules.reimbursement;

import com.his.common.SettlementFact;
import com.his.common.ResultLevel;
import com.his.common.SkillResult;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * R03: 居民报销金额计算
 * 业务含义: 报销金额 = (总费用 - 起付线) × 报销比例
 * 优先级: 10 (最后执行)
 * 依赖公式: formula.reimburse.resident.basic = round((totalFee - deductible) * ratio, 2)
 */
rule "3. 居民报销金额计算"
    salience 10
    when
        $f: SettlementFact(
            totalFee != null,
            deductible != null,
            ratio != null,
            totalFee > deductible
        )
    then
        BigDecimal baseAmount = $f.getTotalFee().subtract($f.getDeductible());
        BigDecimal amount = baseAmount.multiply($f.getRatio()).setScale(2, RoundingMode.HALF_UP);
        
        $f.setFinalAmount(amount);
        $f.addResult(new SkillResult(ResultLevel.PASS, "AmountCalc", "报销金额计算完成"));
        log.info("报销金额计算: patientId={}, baseAmount={}, ratio={}, finalAmount={}", 
            $f.getPatientId(), baseAmount, $f.getRatio(), amount);
end',
'reimbursement', 1, 'active', '居民医保报销金额计算规则', 10, 'resident_amount', 'T001', 'system');

-- ============================================================
-- 救助对象结算规则组 (rule_group_id = 3)
-- ============================================================

INSERT INTO `rule_definition` (`rule_group_id`, `rule_key`, `rule_name`, `rule_text`, `category`, `version`, `status`, `description`, `salience`, `activation_group`, `tenant_id`, `create_by`) VALUES
(3, 'rule.reimbursement.aid_deductible', '救助对象起付线规则',
'package com.his.rules.reimbursement;

import com.his.common.SettlementFact;
import java.math.BigDecimal;

/**
 * R01: 救助对象起付线设置
 * 业务含义: 救助对象起付线固定为 300 元
 * 优先级: 90
 */
rule "1. 救助对象起付线规则"
    salience 90
    when
        $f: SettlementFact(
            patientType == "aid",
            deductible == null
        )
    then
        $f.setDeductible(new BigDecimal("300"));
        log.info("起付线已设置: patientId={}, type=aid, deductible=300", $f.getPatientId());
end',
'reimbursement', 1, 'active', '救助对象起付线设置规则（300元）', 90, 'aid_deductible', 'T001', 'system'),

(3, 'rule.reimbursement.aid_ratio', '救助对象报销比例规则',
'package com.his.rules.reimbursement;

import com.his.common.SettlementFact;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * R02: 救助对象报销比例计算
 * 业务含义: 救助对象基础报销比例 50%
 * 优先级: 80
 */
rule "2. 救助对象报销比例规则"
    salience 80
    when
        $f: SettlementFact(
            patientType == "aid",
            deductible != null,
            ratio == null
        )
    then
        BigDecimal baseRatio = new BigDecimal("0.50");
        $f.setRatio(baseRatio);
        log.info("救助对象报销比例: patientId={}, ratio={}", $f.getPatientId(), baseRatio);
end',
'reimbursement', 1, 'active', '救助对象报销比例计算规则', 80, 'aid_ratio', 'T001', 'system');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 验证查询
-- ============================================================
SELECT rd.id, rd.rule_key, rd.rule_name, rd.salience, rg.group_name
FROM rule_definition rd
JOIN rule_group rg ON rd.rule_group_id = rg.id
WHERE rd.category = 'reimbursement'
ORDER BY rg.group_name, rd.salience DESC;
