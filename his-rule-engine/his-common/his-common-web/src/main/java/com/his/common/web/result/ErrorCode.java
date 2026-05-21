package com.his.common.web.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 *
 * <p>遵循 HIS-XXX-XXX 格式</p>
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ===== 通用 (0xx) =====
    SUCCESS("0", "操作成功"),
    SYSTEM_ERROR("HIS-099", "系统内部错误"),

    // ===== 规则管理 (Rxx) =====
    RULE_NOT_FOUND("HIS-R01", "规则不存在: %s"),
    RULE_KEY_DUPLICATED("HIS-R02", "规则Key已存在: %s"),
    RULE_STATUS_INVALID("HIS-R03", "规则状态不允许此操作: %s"),
    RULE_VALIDATION_FAILED("HIS-R04", "规则校验失败: %s"),
    RULE_GROUP_NOT_FOUND("HIS-R05", "规则分组不存在: %s"),
    RULE_FLOW_NOT_FOUND("HIS-R06", "规则流不存在: %s"),
    RULE_FLOW_VERSION_NOT_FOUND("HIS-R07", "规则流版本不存在: %s"),

    // ===== 公式管理 (Fxx) =====
    FORMULA_NOT_FOUND("HIS-F01", "公式不存在: %s"),
    FORMULA_KEY_DUPLICATED("HIS-F02", "公式Key已存在: %s"),
    FORMULA_SYNTAX_ERROR("HIS-F03", "公式语法错误: %s"),
    FORMULA_EXECUTION_ERROR("HIS-F04", "公式执行错误: %s"),
    FORMULA_PARAM_MISSING("HIS-F05", "公式参数缺失: %s"),
    FORMULA_VERSION_NOT_FOUND("HIS-F06", "公式版本不存在: %s"),

    // ===== 结算模块 (Sxx) =====
    SETTLEMENT_NOT_FOUND("HIS-S01", "结算记录不存在: %s"),
    PATIENT_TYPE_INVALID("HIS-S02", "无效的患者类型: %s"),
    SETTLEMENT_IN_PROGRESS("HIS-S03", "该患者存在进行中的结算"),
    DUPLICATE_SETTLEMENT("HIS-S04", "重复结算: 就诊ID %s 已存在结算记录"),

    // ===== 用药审核 (Dxx) =====
    PRESCRIPTION_EMPTY("HIS-D01", "处方为空"),
    DRUG_NOT_FOUND("HIS-D02", "药品未找到: %s"),
    INCOMPATIBILITY_DETECTED("HIS-D03", "检测到配伍禁忌: %s"),
    DOSAGE_EXCEEDED("HIS-D04", "用药剂量超限: 当前 %s, 上限 %s"),
    ALLERGY_DETECTED("HIS-D05", "患者对药品 %s 过敏"),

    // ===== 质控 (Qxx) =====
    QUALITY_VIOLATION("HIS-Q01", "质控违规: %s"),
    INFECTION_RISK("HIS-Q02", "院感风险: %s"),
    QUALITY_RULE_NOT_FOUND("HIS-Q03", "质控规则不存在: %s"),

    // ===== DRG (Gxx) =====
    DRG_NOT_FOUND("HIS-G01", "DRG分组不存在: %s"),
    DRG_GROUPING_FAILED("HIS-G02", "DRG分组失败: %s"),
    DRG_RECORD_NOT_FOUND("HIS-G03", "DRG记录不存在: %s"),

    // ===== 权限/认证 (Axx) =====
    UNAUTHORIZED("HIS-A01", "未授权访问"),
    FORBIDDEN("HIS-A02", "无权限操作"),
    TOKEN_EXPIRED("HIS-A03", "Token已过期"),
    TENANT_MISMATCH("HIS-A04", "租户不匹配"),
    LOGIN_FAILED("HIS-A05", "用户名或密码错误"),
    ACCOUNT_LOCKED("HIS-A06", "账户已被锁定，请稍后重试"),
    ACCOUNT_DISABLED("HIS-A07", "账户已被禁用"),

    // ===== 参数校验 (Pxx) =====
    PARAM_MISSING("HIS-P01", "必填参数缺失: %s"),
    PARAM_INVALID("HIS-P02", "参数无效: %s"),

    // ===== 沙箱测试 (Bxx) =====
    SANDBOX_DATASET_NOT_FOUND("HIS-B01", "数据集不存在: %s"),
    SANDBOX_SUITE_NOT_FOUND("HIS-B02", "测试套件不存在: %s"),

    // ===== 规则模板 (Mxx) =====
    TEMPLATE_NOT_FOUND("HIS-M01", "规则模板不存在: %s"),

    // ===== 第三方服务 (Exx) =====
    NACOS_CONNECT_FAILED("HIS-E01", "Nacos连接失败: %s"),
    DB_CONNECT_FAILED("HIS-E02", "数据库连接失败: %s"),
    EXTERNAL_SERVICE_ERROR("HIS-E03", "外部服务调用失败: %s");

    private final String code;
    private final String message;
}
