package com.his.common.web.result;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 统一响应封装
 *
 * @param <T> data数据类型
 */
@Data
@Builder
public class Result<T> {

    /**
     * 错误码: "0" 表示成功，非0表示业务异常
     */
    private String code;

    /**
     * 业务数据
     */
    private T data;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 创建成功响应
     *
     * @param data 业务数据
     * @param <T>  data类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code("0")
                .data(data)
                .message("操作成功")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建成功响应（无数据）
     *
     * @return 成功响应
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 创建失败响应
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     data类型
     * @return 失败响应
     */
    public static <T> Result<T> fail(String code, String message) {
        return Result.<T>builder()
                .code(code)
                .data(null)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建失败响应（使用错误码枚举）
     *
     * @param errorCode 错误码
     * @param <T>       data类型
     * @return 失败响应
     */
    public static <T> Result<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 创建失败响应（带参数格式化）
     *
     * @param errorCode 错误码
     * @param args      格式化参数
     * @param <T>       data类型
     * @return 失败响应
     */
    public static <T> Result<T> fail(ErrorCode errorCode, Object... args) {
        String message = String.format(errorCode.getMessage(), args);
        return fail(errorCode.getCode(), message);
    }

    /**
     * 判断是否成功
     *
     * @return true表示成功
     */
    public boolean isSuccess() {
        return "0".equals(this.code);
    }
}
