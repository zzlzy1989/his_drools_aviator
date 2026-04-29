package com.his.formula;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Aviator 公式计算测试
 */
@DisplayName("Aviator 公式计算测试")
class AviatorFormulaTest {

    private BigDecimal toDecimal(Object result) {
        if (result instanceof BigDecimal bd) {
            return bd;
        }
        if (result instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        return new BigDecimal(result.toString());
    }

    @Test
    @DisplayName("基本加减乘除运算")
    void test_basicArithmetic() {
        String formula = "a + b - c * d";
        Map<String, Object> env = Map.of(
            "a", new BigDecimal("100"),
            "b", new BigDecimal("50"),
            "c", new BigDecimal("3"),
            "d", new BigDecimal("10")
        );

        Object result = AviatorEvaluator.execute(formula, env);

        // a + b - c * d = 100 + 50 - 3 * 10 = 100 + 50 - 30 = 150 - 30 = 120
        assertThat(toDecimal(result).setScale(2, RoundingMode.HALF_UP))
            .isEqualTo(new BigDecimal("120.00"));
    }

    @Test
    @DisplayName("职工医保报销公式 - 正常场景")
    void test_employeeReimbursement_normal() {
        String formula = "(totalFee - deductible) * ratio";
        Map<String, Object> env = Map.of(
            "totalFee", new BigDecimal("10000"),
            "deductible", new BigDecimal("1000"),
            "ratio", new BigDecimal("0.85")
        );

        Object result = AviatorEvaluator.execute(formula, env);
        BigDecimal expected = new BigDecimal("7650.00").setScale(2, RoundingMode.HALF_UP);

        assertThat(toDecimal(result).setScale(2, RoundingMode.HALF_UP)).isEqualTo(expected);
    }

    @Test
    @DisplayName("居民医保报销公式 - 起付线以下返回零")
    void test_residentReimbursement_belowDeductible() {
        String formula = "totalFee > deductible ? (totalFee - deductible) * ratio : 0";
        Map<String, Object> env = Map.of(
            "totalFee", new BigDecimal("300"),
            "deductible", new BigDecimal("500"),
            "ratio", new BigDecimal("0.65")
        );

        Object result = AviatorEvaluator.execute(formula, env);

        assertThat(toDecimal(result).setScale(2, RoundingMode.HALF_UP))
            .isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("条件表达式 - 三元运算")
    void test_conditionalExpression() {
        String formula = "amount > 1000 ? amount * 0.9 : amount";
        Map<String, Object> env1 = Map.of("amount", new BigDecimal("2000"));
        Map<String, Object> env2 = Map.of("amount", new BigDecimal("500"));

        assertThat(toDecimal(AviatorEvaluator.execute(formula, env1)).setScale(2, RoundingMode.HALF_UP))
            .isEqualTo(new BigDecimal("1800.00"));
        assertThat(toDecimal(AviatorEvaluator.execute(formula, env2)).setScale(2, RoundingMode.HALF_UP))
            .isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("DRG 权重调整公式")
    void test_drgWeightAdjustment() {
        String formula = "(baseWeight + extraPoints) * severityFactor";
        Map<String, Object> env = Map.of(
            "baseWeight", new BigDecimal("1.2"),
            "extraPoints", new BigDecimal("0.3"),
            "severityFactor", new BigDecimal("1.1")
        );

        Object result = AviatorEvaluator.execute(formula, env);

        assertThat(toDecimal(result).setScale(2, RoundingMode.HALF_UP))
            .isEqualTo(new BigDecimal("1.65"));
    }

    @Test
    @DisplayName("BigDecimal 精度测试 - 浮点数运算")
    void test_bigDecimalPrecision() {
        String formula = "x * y";
        Map<String, Object> env = Map.of(
            "x", new BigDecimal("0.1"),
            "y", new BigDecimal("0.2")
        );

        Object result = AviatorEvaluator.execute(formula, env);

        assertThat(toDecimal(result).setScale(2, RoundingMode.HALF_UP))
            .isEqualTo(new BigDecimal("0.02"));
    }

    @Test
    @DisplayName("表达式编译 - 同一表达式多次编译结果一致")
    void test_expressionCompilation() {
        String formula = "a + b";
        Map<String, Object> env = Map.of("a", new BigDecimal("1"), "b", new BigDecimal("2"));

        Object result1 = AviatorEvaluator.execute(formula, env);
        Object result2 = AviatorEvaluator.execute(formula, env);

        assertThat(toDecimal(result1)).isEqualTo(toDecimal(result2));
        assertThat(toDecimal(result1)).isEqualTo(new BigDecimal("3"));
    }

    @Test
    @DisplayName("除法精度测试")
    void test_divisionPrecision() {
        String formula = "x / y";
        Map<String, Object> env = Map.of(
            "x", new BigDecimal("10"),
            "y", new BigDecimal("3")
        );

        Object result = AviatorEvaluator.execute(formula, env);

        assertThat(toDecimal(result).setScale(4, RoundingMode.HALF_UP))
            .isEqualTo(new BigDecimal("3.3333"));
    }

    @Test
    @DisplayName("比较运算")
    void test_comparison() {
        String formula = "a > b";
        Map<String, Object> env1 = Map.of("a", new BigDecimal("10"), "b", new BigDecimal("5"));
        Map<String, Object> env2 = Map.of("a", new BigDecimal("3"), "b", new BigDecimal("7"));

        assertThat(AviatorEvaluator.execute(formula, env1)).isEqualTo(true);
        assertThat(AviatorEvaluator.execute(formula, env2)).isEqualTo(false);
    }

    @Test
    @DisplayName("字符串比较")
    void test_stringComparison() {
        String formula = "a == b";
        Map<String, Object> env1 = Map.of("a", "employee", "b", "employee");
        Map<String, Object> env2 = Map.of("a", "employee", "b", "resident");

        assertThat(AviatorEvaluator.execute(formula, env1)).isEqualTo(true);
        assertThat(AviatorEvaluator.execute(formula, env2)).isEqualTo(false);
    }
}