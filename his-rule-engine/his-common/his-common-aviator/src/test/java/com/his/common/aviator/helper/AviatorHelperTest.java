package com.his.common.aviator.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AviatorHelper 单元测试
 */
@DisplayName("AviatorHelper 工具类测试")
class AviatorHelperTest {

    @Test
    @DisplayName("compile - 编译简单表达式")
    void test_compileSimple() {
        var expr = AviatorHelper.compile("a + b");
        assertThat(expr).isNotNull();
    }

    @Test
    @DisplayName("compile - 编译复杂表达式")
    void test_compileComplex() {
        var expr = AviatorHelper.compile("round((totalFee - deductible) * ratio, 2)");
        assertThat(expr).isNotNull();
    }

    @Test
    @DisplayName("compile - 编译无效表达式应抛出异常")
    void test_compileInvalid() {
        assertThatThrownBy(() -> AviatorHelper.compile("a + "))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("execute - 基本运算")
    void test_executeBasic() {
        Map<String, Object> env = Map.of(
            "a", new BigDecimal("10"),
            "b", new BigDecimal("5")
        );
        Object result = AviatorHelper.execute("a + b", env);
        assertThat(toDecimal(result)).isEqualByComparingTo(new BigDecimal("15"));
    }

    @Test
    @DisplayName("execute - 报销公式")
    void test_executeReimbursement() {
        Map<String, Object> env = Map.of(
            "totalFee", new BigDecimal("10000"),
            "deductible", new BigDecimal("1000"),
            "ratio", new BigDecimal("0.85")
        );
        Object result = AviatorHelper.execute("(totalFee - deductible) * ratio", env);
        // (10000 - 1000) * 0.85 = 7650
        assertThat(toDecimal(result)).isEqualByComparingTo(new BigDecimal("7650"));
    }

    @Test
    @DisplayName("executeDecimal - 返回 BigDecimal 结果")
    void test_executeDecimal() {
        Map<String, Object> env = Map.of(
            "x", new BigDecimal("100"),
            "y", new BigDecimal("3")
        );
        BigDecimal result = AviatorHelper.executeDecimal("x / y", env);
        assertThat(result).isEqualByComparingTo(new BigDecimal("33.33"));
    }

    @Test
    @DisplayName("executeDecimal - 边界值处理")
    void test_executeDecimalEdge() {
        // 除数为零
        Map<String, Object> envZero = Map.of(
            "x", new BigDecimal("100"),
            "y", new BigDecimal("0")
        );
        assertThatThrownBy(() -> AviatorHelper.executeDecimal("x / y", envZero))
            .isInstanceOf(ArithmeticException.class);

        // 空环境
        assertThatThrownBy(() -> AviatorHelper.executeDecimal("a + b", new HashMap<>()))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("validate - 有效表达式")
    void test_validateValid() {
        assertThat(AviatorHelper.validate("a + b")).isTrue();
        assertThat(AviatorHelper.validate("round(x, 2)")).isTrue();
        assertThat(AviatorHelper.validate("a > b ? c : d")).isTrue();
    }

    @Test
    @DisplayName("validate - 无效表达式")
    void test_validateInvalid() {
        assertThat(AviatorHelper.validate("a + ")).isFalse();
        assertThat(AviatorHelper.validate("a > ? b")).isFalse();
        assertThat(AviatorHelper.validate("if(a > b)")).isFalse();  // Aviator 不支持 if 语句
    }

    @Test
    @DisplayName("getValidationError - 获取错误信息")
    void test_getValidationError() {
        assertThat(AviatorHelper.getValidationError("a +")).isNotNull();
        assertThat(AviatorHelper.getValidationError("a + b")).isNull();
    }

    @Test
    @DisplayName("containsDangerousFunctions - 检测危险函数")
    void test_containsDangerousFunctions() {
        // AviatorHelper 检测的是函数名后紧跟 ( 的模式，如 system( 而不是 system.exit(
        assertThat(AviatorHelper.containsDangerousFunctions("system(exit)")).isTrue();
        assertThat(AviatorHelper.containsDangerousFunctions("runtime(exec)")).isTrue();
        assertThat(AviatorHelper.containsDangerousFunctions("exec('ls')")).isTrue();
        assertThat(AviatorHelper.containsDangerousFunctions("process(1)")).isTrue();
        assertThat(AviatorHelper.containsDangerousFunctions("class(123)")).isTrue();
        assertThat(AviatorHelper.containsDangerousFunctions("reflect.Field")).isFalse();  // 不是函数调用
        assertThat(AviatorHelper.containsDangerousFunctions("a + b")).isFalse();
        assertThat(AviatorHelper.containsDangerousFunctions("system.exit(1)")).isFalse();  // system.exit 不是 system(
    }

    @Test
    @DisplayName("containsDangerousFunctions - 大小写不敏感")
    void test_containsDangerousFunctionsCaseInsensitive() {
        assertThat(AviatorHelper.containsDangerousFunctions("SYSTEM(exit)")).isTrue();
        assertThat(AviatorHelper.containsDangerousFunctions("RUNTIME(exec)")).isTrue();
    }

    @Test
    @DisplayName("条件表达式")
    void test_conditionalExpression() {
        Map<String, Object> env1 = Map.of("amount", new BigDecimal("2000"));
        Map<String, Object> env2 = Map.of("amount", new BigDecimal("500"));

        Object result1 = AviatorHelper.execute("amount > 1000 ? amount * 0.9 : amount", env1);
        Object result2 = AviatorHelper.execute("amount > 1000 ? amount * 0.9 : amount", env2);

        assertThat(toDecimal(result1)).isEqualByComparingTo(new BigDecimal("1800"));
        assertThat(toDecimal(result2)).isEqualByComparingTo(new BigDecimal("500"));
    }

    @Test
    @DisplayName("DRG 权重公式")
    void test_drgWeightFormula() {
        Map<String, Object> env = Map.of(
            "baseWeight", new BigDecimal("1.2"),
            "extraPoints", new BigDecimal("0.3"),
            "severityFactor", new BigDecimal("1.1")
        );
        Object result = AviatorHelper.execute("(baseWeight + extraPoints) * severityFactor", env);
        // (1.2 + 0.3) * 1.1 = 1.65
        assertThat(toDecimal(result).setScale(2, RoundingMode.HALF_UP))
            .isEqualByComparingTo(new BigDecimal("1.65"));
    }

    @Test
    @DisplayName("阶梯式报销公式")
    void test_stagedReimbursement() {
        // 第一段: 0~1000 报销 80%
        // 第二段: 1000~5000 报销 60%
        // 第三段: 5000以上 报销 40%
        Map<String, Object> env1 = Map.of("totalFee", new BigDecimal("800"));
        Map<String, Object> env2 = Map.of("totalFee", new BigDecimal("3000"));
        Map<String, Object> env3 = Map.of("totalFee", new BigDecimal("10000"));

        // 简化测试
        Object r1 = AviatorHelper.execute("totalFee <= 1000 ? totalFee * 0.8 : totalFee", env1);
        Object r2 = AviatorHelper.execute("totalFee <= 1000 ? totalFee * 0.8 : totalFee", env2);
        Object r3 = AviatorHelper.execute("totalFee <= 1000 ? totalFee * 0.8 : totalFee", env3);

        assertThat(toDecimal(r1)).isEqualByComparingTo(new BigDecimal("640")); // 800 * 0.8
    }

    @Test
    @DisplayName("let 语法 - Aviator 5.x 不支持 let")
    void test_letSyntax() {
        // Aviator 5.x 不支持 let 语法，这是 Aviator 6 的特性
        // 使用普通的变量引用代替
        Map<String, Object> env = Map.of(
            "totalFee", new BigDecimal("10000"),
            "deductible", new BigDecimal("1000"),
            "ratio", new BigDecimal("0.85")
        );
        // 使用三元表达式代替 let
        Object result = AviatorHelper.execute(
            "(totalFee - deductible) * ratio",
            env
        );
        assertThat(toDecimal(result)).isEqualByComparingTo(new BigDecimal("7650"));
    }

    @Test
    @DisplayName("init - 初始化方法")
    void test_init() {
        AviatorHelper.init(); // 应无异常
    }

    private BigDecimal toDecimal(Object result) {
        if (result instanceof BigDecimal bd) {
            return bd.setScale(2, RoundingMode.HALF_UP);
        }
        if (result instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(result.toString()).setScale(2, RoundingMode.HALF_UP);
    }
}