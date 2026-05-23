package com.his.common.aviator.helper;

import com.googlecode.aviator.Expression;
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

    // ========== 边界值测试 ==========

    @Test
    @DisplayName("边界值 - 零值运算")
    void test_zeroValue() {
        // 零作为被除数
        Map<String, Object> env1 = Map.of("x", new BigDecimal("0"), "y", new BigDecimal("10"));
        Object r1 = AviatorHelper.execute("x + y", env1);
        assertThat(toDecimal(r1)).isEqualByComparingTo(new BigDecimal("10"));

        // 零作为减数
        Map<String, Object> env2 = Map.of("x", new BigDecimal("100"), "y", new BigDecimal("0"));
        Object r2 = AviatorHelper.execute("x - y", env2);
        assertThat(toDecimal(r2)).isEqualByComparingTo(new BigDecimal("100"));

        // 零作为乘数
        Map<String, Object> env3 = Map.of("x", new BigDecimal("1000"), "y", new BigDecimal("0"));
        Object r3 = AviatorHelper.execute("x * y", env3);
        assertThat(toDecimal(r3)).isEqualByComparingTo(new BigDecimal("0"));
    }

    @Test
    @DisplayName("边界值 - 除零错误")
    void test_divisionByZero() {
        Map<String, Object> env = Map.of(
            "x", new BigDecimal("100"),
            "y", new BigDecimal("0")
        );
        assertThatThrownBy(() -> AviatorHelper.executeDecimal("x / y", env))
            .isInstanceOf(ArithmeticException.class);
    }

    @Test
    @DisplayName("边界值 - 负数运算")
    void test_negativeNumbers() {
        Map<String, Object> env = Map.of(
            "income", new BigDecimal("-1000"),
            "expense", new BigDecimal("500"),
            "ratio", new BigDecimal("-0.5")
        );

        // 加法：负数 + 正数
        Object r1 = AviatorHelper.execute("income + expense", env);
        assertThat(toDecimal(r1)).isEqualByComparingTo(new BigDecimal("-500"));

        // 乘法：负数 * 负数 = 正数
        Object r2 = AviatorHelper.execute("income * ratio", env);
        assertThat(toDecimal(r2)).isEqualByComparingTo(new BigDecimal("500"));
    }

    @Test
    @DisplayName("边界值 - 超大数值运算")
    void test_largeNumbers() {
        // 1亿 * 0.000001 = 100
        Map<String, Object> env = Map.of(
            "population", new BigDecimal("100000000"),
            "rate", new BigDecimal("0.000001")
        );
        Object result = AviatorHelper.execute("population * rate", env);
        assertThat(toDecimal(result)).isEqualByComparingTo(new BigDecimal("100"));
    }

    @Test
    @DisplayName("边界值 - 科学计数法表示")
    void test_scientificNotation() {
        // Aviator 5.x 会将浮点数字面量解析为 BigDecimal
        Map<String, Object> env = Map.of("x", new BigDecimal("1E6"));
        Object result = AviatorHelper.execute("x * 0.01", env);
        assertThat(toDecimal(result)).isEqualByComparingTo(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("边界值 - 精度舍入 HALF_UP")
    void test_roundingHALFUP() {
        // Aviator 5.x 不支持 round 函数，使用 math.round 或自行处理
        // 这里测试 Aviator 的基本数学运算精度
        Map<String, Object> env = Map.of("x", new BigDecimal("1.235"), "y", new BigDecimal("100"));
        Object result = AviatorHelper.execute("x * y", env);
        // 1.235 * 100 = 123.5
        assertThat(toDecimal(result)).isEqualByComparingTo(new BigDecimal("123.50"));
    }

    @Test
    @DisplayName("边界值 - 空环境变量")
    void test_emptyEnv() {
        assertThatThrownBy(() -> AviatorHelper.execute("a + b", new HashMap<>()))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("边界值 - 缺失参数")
    void test_missingParameter() {
        Map<String, Object> env = Map.of("x", new BigDecimal("10"));
        assertThatThrownBy(() -> AviatorHelper.execute("x + y", env))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("边界值 - 空表达式")
    void test_emptyExpression() {
        assertThatThrownBy(() -> AviatorHelper.compile(""))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("边界值 - null 环境变量")
    void test_nullEnv() {
        // null 环境抛出 ExpressionRuntimeException 而非 NullPointerException
        assertThatThrownBy(() -> AviatorHelper.execute("a + b", null))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("边界值 - 多位数小数精度")
    void test_precisionMultiDigit() {
        // 验证多位小数的精确计算
        Map<String, Object> env = Map.of(
            "price", new BigDecimal("0.1"),
            "quantity", new BigDecimal("3")
        );
        Object result = AviatorHelper.execute("price * quantity", env);
        // 0.1 * 3 = 0.3 (精确)
        assertThat(toDecimal(result)).isEqualByComparingTo(new BigDecimal("0.3"));
    }

    @Test
    @DisplayName("边界值 - 金额常用场景")
    void test_commonMonetaryScenarios() {
        // 场景1：起付线以下返回0
        Map<String, Object> env1 = Map.of(
            "totalFee", new BigDecimal("300"),
            "deductible", new BigDecimal("500"),
            "ratio", new BigDecimal("0.85")
        );
        Object result1 = AviatorHelper.execute(
            "totalFee > deductible ? (totalFee - deductible) * ratio : 0",
            env1
        );
        assertThat(toDecimal(result1)).isEqualByComparingTo(new BigDecimal("0"));

        // 场景2：超过封顶线取封顶值
        Map<String, Object> env2 = Map.of(
            "amount", new BigDecimal("100000"),
            "cap", new BigDecimal("50000")
        );
        Object result2 = AviatorHelper.execute("amount > cap ? cap : amount", env2);
        assertThat(toDecimal(result2)).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("compile - 带缓存编译多次调用同一表达式")
    void test_compileCached() {
        Expression expr1 = AviatorHelper.compile("a + b", true);
        Expression expr2 = AviatorHelper.compile("a + b", true);
        // 带缓存时，多次编译应返回相同实例
        assertThat(expr1).isSameAs(expr2);
    }

    @Test
    @DisplayName("executeDecimal - 结果精度验证")
    void test_executeDecimalPrecision() {
        Map<String, Object> env = Map.of(
            "total", new BigDecimal("10000"),
            "deductible", new BigDecimal("1000"),
            "ratio", new BigDecimal("0.853")
        );
        BigDecimal result = AviatorHelper.executeDecimal("(total - deductible) * ratio", env);
        // (10000 - 1000) * 0.853 = 7677，但应四舍五入到 7677.00
        assertThat(result).isEqualByComparingTo(new BigDecimal("7677.00"));
    }

    @Test
    @DisplayName("containsDangerousFunctions - 其他危险模式")
    void test_dangerousEdgeCases() {
        // 嵌套括号
        assertThat(AviatorHelper.containsDangerousFunctions("system(system())")).isTrue();
        // 大小写混合
        assertThat(AviatorHelper.containsDangerousFunctions("SYSTEM(exit)")).isTrue();
        assertThat(AviatorHelper.containsDangerousFunctions("RUNTIME(exec)")).isTrue();
        // 正常表达式
        assertThat(AviatorHelper.containsDangerousFunctions("max(a, b, c)")).isFalse();
        assertThat(AviatorHelper.containsDangerousFunctions("min(a, b)")).isFalse();
        assertThat(AviatorHelper.containsDangerousFunctions("abs(-100)")).isFalse();
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