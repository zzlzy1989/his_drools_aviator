package com.his.common.web.exception;

import com.his.common.web.result.ErrorCode;
import lombok.Getter;

/**
 * 基础异常类
 *
 * <p>所有业务异常的基类</p>
 */
@Getter
public class HisException extends RuntimeException {

    /**
     * 错误码
     */
    private final ErrorCode errorCode;

    /**
     * 错误码字符串（当直接使用字符串构造时使用）
     */
    private final String codeString;

    /**
     * 格式化参数
     */
    private final Object[] args;

    /**
     * 构造器
     *
     * @param errorCode 错误码
     * @param args      格式化参数
     */
    public HisException(ErrorCode errorCode, Object... args) {
        super(String.format(errorCode.getMessage(), args));
        this.errorCode = errorCode;
        this.codeString = errorCode.getCode();
        this.args = args;
    }

    /**
     * 构造器（带原因）
     *
     * @param errorCode 错误码
     * @param cause     原因
     * @param args      格式化参数
     */
    public HisException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(String.format(errorCode.getMessage(), args), cause);
        this.errorCode = errorCode;
        this.codeString = errorCode.getCode();
        this.args = args;
    }

    /**
     * 构造器（直接使用字符串错误码）
     *
     * @param code    错误码字符串
     * @param message 错误信息
     */
    public HisException(String code, String message) {
        super(message);
        this.errorCode = null;
        this.codeString = code;
        this.args = new Object[0];
    }

    /**
     * 获取错误码字符串
     *
     * @return 错误码
     */
    public String getCode() {
        return codeString;
    }
}
