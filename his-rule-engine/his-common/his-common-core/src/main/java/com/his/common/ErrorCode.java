package com.his.common;

public enum ErrorCode {

    SUCCESS("0", "操作成功"),

    BUSINESS_PATIENT_INFO_MISSING("HIS-001", "患者身份信息缺失"),
    BUSINESS_RULE_NOT_FOUND("HIS-002", "规则不存在"),
    BUSINESS_SETTLEMENT_IN_PROGRESS("HIS-003", "结算正在进行中"),

    FORMULA_SYNTAX_ERROR("HIS-101", "公式语法错误"),
    FORMULA_PARAM_MISMATCH("HIS-102", "公式参数不匹配"),
    FORMULA_CALC_OVERFLOW("HIS-103", "公式计算溢出"),

    SETTLEMENT_DUPLICATE("HIS-201", "重复结算"),
    SETTLEMENT_REJECTED("HIS-202", "审核拒绝"),

    PERMISSION_DENIED("HIS-301", "无租户权限"),

    SECURITY_INJECTION_DETECTED("HIS-401", "Aviator注入检测"),

    SYSTEM_CONFIG_UNAVAILABLE("HIS-901", "配置中心不可用"),
    SYSTEM_KIE_COMPILE_FAILED("HIS-902", "KIE编译失败"),
    SYSTEM_INTERNAL_ERROR("HIS-999", "系统内部异常");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
