package com.his.common.aviator.engine;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import com.his.common.aviator.cache.AviatorExpressionCache;
import com.his.common.aviator.config.AviatorConfig;
import com.his.common.exception.FormulaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Aviator 表达式引擎
 *
 * <p>封装 Aviator 表达式的编译和执行</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AviatorEngine {

    private final AviatorExpressionCache expressionCache;
    private final AviatorConfig config;

    /**
     * 执行表达式
     *
     * @param expression 表达式文本
     * @param env 环境变量
     * @return 执行结果
     */
    public Object execute(String expression, Map<String, Object> env) {
        if (expression == null || expression.isBlank()) {
            throw new FormulaException("表达式不能为空");
        }

        if (expression.length() > config.getMaxExpressionLength()) {
            throw new FormulaException("表达式长度超限: " + expression.length() + " > " + config.getMaxExpressionLength());
        }

        try {
            Expression compiled = expressionCache.getCompiledExpression(expression);
            return compiled.execute(env);
        } catch (Exception e) {
            log.error("Aviator表达式执行失败: expression={}", expression, e);
            throw new FormulaException("表达式执行错误: " + e.getMessage(), e);
        }
    }

    /**
     * 执行表达式并返回 BigDecimal 结果
     *
     * @param expression 表达式文本
     * @param env 环境变量
     * @return BigDecimal 结果
     */
    public BigDecimal executeDecimal(String expression, Map<String, Object> env) {
        Object result = execute(expression, env);
        if (result instanceof BigDecimal bd) {
            return bd;
        }
        if (result instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        throw new FormulaException("表达式结果无法转换为 BigDecimal: " + result.getClass());
    }

    /**
     * 执行表达式并返回 Boolean 结果
     *
     * @param expression 表达式文本
     * @param env 环境变量
     * @return Boolean 结果
     */
    public Boolean executeBoolean(String expression, Map<String, Object> env) {
        Object result = execute(expression, env);
        if (result instanceof Boolean b) {
            return b;
        }
        throw new FormulaException("表达式结果无法转换为 Boolean: " + result.getClass());
    }

    /**
     * 验证表达式语法
     *
     * @param expression 表达式文本
     * @return true 表示语法正确
     */
    public boolean validate(String expression) {
        try {
            AviatorEvaluator.compile(expression, true);
            return true;
        } catch (Exception e) {
            log.debug("Aviator表达式语法验证失败: {}", expression, e);
            return false;
        }
    }

    /**
     * 编译表达式
     *
     * @param expression 表达式文本
     * @return 编译后的 Expression 对象
     */
    public Expression compile(String expression) {
        return expressionCache.getCompiledExpression(expression);
    }

    /**
     * 刷新表达式缓存
     *
     * @param expression 表达式文本
     */
    public void refresh(String expression) {
        expressionCache.invalidate(expression);
        log.info("Aviator expression cache refreshed: {}", expression);
    }

    /**
     * 刷新所有表达式缓存
     */
    public void refreshAll() {
        expressionCache.invalidateAll();
        log.info("All Aviator expression caches refreshed");
    }
}
