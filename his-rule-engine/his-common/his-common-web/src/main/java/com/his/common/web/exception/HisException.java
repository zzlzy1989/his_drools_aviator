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
        this.args = args;
    }

    /**
     * 获取错误码字符串
     *
     * @return 错误码
     */
    public String getCode() {
        return errorCode.getCode();
    }
}
