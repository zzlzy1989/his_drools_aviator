package com.his.common.exception;

/**
 * 公式执行异常
 *
 * <p>用于公式解析、执行失败等场景</p>
 */
public class FormulaException extends RuntimeException {

    public FormulaException(String message) {
        super(message);
    }

    public FormulaException(String message, Throwable cause) {
        super(message, cause);
    }
}
