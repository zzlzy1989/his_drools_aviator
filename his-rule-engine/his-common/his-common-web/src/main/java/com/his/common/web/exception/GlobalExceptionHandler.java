package com.his.common.web.exception;

import com.his.common.web.result.ErrorCode;
import com.his.common.web.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.MethodArgumentNotValidException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * <p>统一处理各类型异常，返回标准化错误响应</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e      业务异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(HisException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleHisException(HisException e, HttpServletRequest request) {
        log.warn("业务异常: path={}, code={}, message={}",
                request.getRequestURI(), e.getCode(), e.getMessage());

        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     *
     * @param e      参数校验异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e,
                                                    HttpServletRequest request) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("参数校验失败: path={}, errors={}", request.getRequestURI(), errors);

        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), errors);
    }

    /**
     * 处理绑定异常
     *
     * @param e      绑定异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String errors = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("参数绑定失败: path={}, errors={}", request.getRequestURI(), errors);

        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), errors);
    }

    /**
     * 处理缺少请求参数异常
     *
     * @param e      缺少请求参数异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParameter(MissingServletRequestParameterException e,
                                                HttpServletRequest request) {
        String message = String.format("缺少必填参数: %s", e.getParameterName());
        log.warn("缺少请求参数: path={}, param={}", request.getRequestURI(), e.getParameterName());

        return Result.fail(ErrorCode.PARAM_MISSING.getCode(), message);
    }

    /**
     * 处理参数类型不匹配异常
     *
     * @param e      参数类型不匹配异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                            HttpServletRequest request) {
        String message = String.format("参数 %s 类型错误", e.getName());
        log.warn("参数类型不匹配: path={}, param={}, type={}",
                request.getRequestURI(), e.getName(), e.getRequiredType());

        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), message);
    }

    /**
     * 处理IllegalArgumentException
     *
     * @param e      IllegalArgumentException
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e,
                                                HttpServletRequest request) {
        log.warn("非法参数: path={}, message={}", request.getRequestURI(), e.getMessage());

        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), e.getMessage());
    }

    /**
     * 处理未预期异常
     *
     * @param e      异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleUnexpectedException(Exception e, HttpServletRequest request) {
        log.error("未预期异常: path={}", request.getRequestURI(), e);

        return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), "系统繁忙，请稍后重试");
    }
}
