package com.his.common.aviator.helper;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.Functions;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Aviator 辅助工具类
 *
 * <p>提供表达式编译、执行、验证的快捷方法</p>
 */
@Slf4j
public class AviatorHelper {

    static {
        AviatorEvaluator.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_BIGDECIMAL, true);
        AviatorEvaluator.setOption(Options.ENABLE_SCIENTIFIC_NOTATION, false);
        AviatorEvaluator.setOption(Options.MAX_LOOKUP_CACHE_SIZE, 1024);
    }

    /**
     * 编译表达式
     *
     * @param expression 表达式文本
     * @return 编译后的 Expression 对象
     */
    public static Expression compile(String expression) {
        return AviatorEvaluator.compile(expression, true);
    }

    /**
     * 编译表达式（带缓存）
     *
     * @param expression 表达式文本
     * @param cached 是否使用缓存
     * @return 编译后的 Expression 对象
     */
    public static Expression compile(String expression, boolean cached) {
        return AviatorEvaluator.compile(expression, cached);
    }

    /**
     * 执行表达式
     *
     * @param expression 表达式文本
     * @param env 环境变量
     * @return 执行结果
     */
    public static Object execute(String expression, Map<String, Object> env) {
        Expression compiled = compile(expression);
        return compiled.execute(env);
    }

    /**
     * 执行表达式并返回 BigDecimal
     *
     * @param expression 表达式文本
     * @param env 环境变量
     * @return BigDecimal 结果
     */
    public static BigDecimal executeDecimal(String expression, Map<String, Object> env) {
        Object result = execute(expression, env);
        if (result instanceof BigDecimal bd) {
            return bd.setScale(2, RoundingMode.HALF_UP);
        }
        if (result instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }
        throw new IllegalArgumentException("无法转换为 BigDecimal: " + result.getClass());
    }

    /**
     * 验证表达式语法
     *
     * @param expression 表达式文本
     * @return true 表示语法正确
     */
    public static boolean validate(String expression) {
        try {
            AviatorEvaluator.compile(expression, true);
            return true;
        } catch (Exception e) {
            log.debug("表达式语法验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取验证错误信息
     *
     * @param expression 表达式文本
     * @return 错误信息，无错误返回 null
     */
    public static String getValidationError(String expression) {
        try {
            AviatorEvaluator.compile(expression, true);
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * 判断表达式是否包含危险函数
     *
     * @param expression 表达式文本
     * @return true 表示包含危险函数
     */
    public static boolean containsDangerousFunctions(String expression) {
        String lower = expression.toLowerCase();
        String[] dangerous = {"system", "runtime", "exec", "process", "class", "reflect"};
        for (String fn : dangerous) {
            if (lower.contains(fn + "(")) {
                log.warn("检测到潜在危险函数: {}", fn);
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化 Aviator 配置
     */
    public static void init() {
        log.info("AviatorHelper initialized, version: {}",
                com.googlecode.aviator.utils.Version.getVersion());
    }
}
