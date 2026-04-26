package com.his.common.web.exception;

import com.his.common.web.result.ErrorCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * <p>用于业务逻辑校验失败、状态不正确等业务场景</p>
 */
@Getter
public class BusinessException extends HisException {

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public BusinessException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
