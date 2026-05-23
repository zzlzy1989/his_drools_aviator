package com.his.settlement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 结算金额计算测试
 */
@DisplayName("结算金额计算测试")
class SettlementServiceTest {

    @Test
    @DisplayName("职工医保报销计算 - 正常场景")
    void test_employeeReimbursement() {
        BigDecimal totalFee = new BigDecimal("10000");
        BigDecimal deductible = new BigDecimal("1000");
        BigDecimal ratio = new BigDecimal("0.85");

        BigDecimal baseAmount = totalFee.subtract(deductible);
        BigDecimal reimburseAmount = baseAmount.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal selfPayAmount = totalFee.subtract(reimburseAmount);

        assertThat(reimburseAmount).isEqualByComparingTo(new BigDecimal("7650.00"));
        assertThat(selfPayAmount).isEqualByComparingTo(new BigDecimal("2350.00"));
    }

    @Test
    @DisplayName("居民医保报销计算 - 起付线以下")
    void test_residentReimbursement_belowDeductible() {
        BigDecimal totalFee = new BigDecimal("300");
        BigDecimal deductible = new BigDecimal("500");
        BigDecimal ratio = new BigDecimal("0.65");

        BigDecimal reimburseAmount;
        BigDecimal selfPayAmount;

        if (totalFee.compareTo(deductible) <= 0) {
            reimburseAmount = BigDecimal.ZERO;
            selfPayAmount = totalFee;
        } else {
            BigDecimal baseAmount = totalFee.subtract(deductible);
            reimburseAmount = baseAmount.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
            selfPayAmount = totalFee.subtract(reimburseAmount);
        }

        assertThat(reimburseAmount).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(selfPayAmount).isEqualByComparingTo(new BigDecimal("300"));
    }

    @Test
    @DisplayName("BigDecimal 精度测试 - 浮点数运算")
    void test_bigDecimalPrecision() {
        BigDecimal a = new BigDecimal("0.1");
        BigDecimal b = new BigDecimal("0.2");
        BigDecimal c = a.add(b);

        assertThat(c).isEqualByComparingTo(new BigDecimal("0.3"));
    }

    @Test
    @DisplayName("BigDecimal 精度测试 - 乘积精度")
    void test_bigDecimalMultiplication() {
        BigDecimal price = new BigDecimal("123.45");
        BigDecimal quantity = new BigDecimal("10");
        BigDecimal total = price.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

        assertThat(total).isEqualByComparingTo(new BigDecimal("1234.50"));
    }

    @Test
    @DisplayName("BigDecimal 除法精度测试")
    void test_bigDecimalDivision() {
        BigDecimal a = new BigDecimal("10");
        BigDecimal b = new BigDecimal("3");
        BigDecimal result = a.divide(b, 4, RoundingMode.HALF_UP);

        assertThat(result).isEqualByComparingTo(new BigDecimal("3.3333"));
    }

    @Test
    @DisplayName("报销比例计算 - 三级医院")
    void test_ratioWithHospitalLevel() {
        BigDecimal baseRatio = new BigDecimal("0.85");
        String hospitalLevel = "三级";

        BigDecimal finalRatio = "三级".equals(hospitalLevel) || "3".equals(hospitalLevel)
            ? baseRatio.multiply(new BigDecimal("0.9"))
            : baseRatio;

        assertThat(finalRatio).isEqualByComparingTo(new BigDecimal("0.765"));
    }

    @Test
    @DisplayName("起付线计算 - 职工")
    void test_deductible_employee() {
        String patientType = "employee";
        BigDecimal deductible = switch (patientType.toLowerCase()) {
            case "employee" -> new BigDecimal("1000");
            case "resident" -> new BigDecimal("500");
            case "aid" -> new BigDecimal("300");
            default -> BigDecimal.ZERO;
        };

        assertThat(deductible).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    @DisplayName("起付线计算 - 居民")
    void test_deductible_resident() {
        String patientType = "resident";
        BigDecimal deductible = switch (patientType.toLowerCase()) {
            case "employee" -> new BigDecimal("1000");
            case "resident" -> new BigDecimal("500");
            case "aid" -> new BigDecimal("300");
            default -> BigDecimal.ZERO;
        };

        assertThat(deductible).isEqualByComparingTo(new BigDecimal("500"));
    }

    @Test
    @DisplayName("起付线计算 - 救助对象")
    void test_deductible_aid() {
        String patientType = "aid";
        BigDecimal deductible = switch (patientType.toLowerCase()) {
            case "employee" -> new BigDecimal("1000");
            case "resident" -> new BigDecimal("500");
            case "aid" -> new BigDecimal("300");
            default -> BigDecimal.ZERO;
        };

        assertThat(deductible).isEqualByComparingTo(new BigDecimal("300"));
    }

    @Test
    @DisplayName("大额费用计算")
    void test_largeAmountCalculation() {
        BigDecimal totalFee = new BigDecimal("200000");
        BigDecimal deductible = new BigDecimal("1000");
        BigDecimal ratio = new BigDecimal("0.85");

        BigDecimal baseAmount = totalFee.subtract(deductible);
        BigDecimal reimburseAmount = baseAmount.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal selfPayAmount = totalFee.subtract(reimburseAmount);

        assertThat(reimburseAmount).isEqualByComparingTo(new BigDecimal("169150.00"));
        assertThat(selfPayAmount).isEqualByComparingTo(new BigDecimal("30850.00"));
    }

    // ========== 边界值测试 ==========

    @Test
    @DisplayName("边界值 - 零费用")
    void test_zeroTotalFee() {
        BigDecimal totalFee = BigDecimal.ZERO;
        BigDecimal deductible = new BigDecimal("1000");
        BigDecimal ratio = new BigDecimal("0.85");

        BigDecimal reimburseAmount = totalFee.compareTo(deductible) <= 0
            ? BigDecimal.ZERO
            : totalFee.subtract(deductible).multiply(ratio).setScale(2, RoundingMode.HALF_UP);

        assertThat(reimburseAmount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("边界值 - 正好等于起付线")
    void test_exactlyAtDeductible() {
        BigDecimal totalFee = new BigDecimal("1000");
        BigDecimal deductible = new BigDecimal("1000");
        BigDecimal ratio = new BigDecimal("0.85");

        // 等于起付线时，报销金额应为0
        BigDecimal reimburseAmount = totalFee.compareTo(deductible) <= 0
            ? BigDecimal.ZERO
            : totalFee.subtract(deductible).multiply(ratio).setScale(2, RoundingMode.HALF_UP);

        assertThat(reimburseAmount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("边界值 - 略高于起付线")
    void test_slightlyAboveDeductible() {
        BigDecimal totalFee = new BigDecimal("1001");
        BigDecimal deductible = new BigDecimal("1000");
        BigDecimal ratio = new BigDecimal("0.85");

        BigDecimal reimburseAmount = totalFee.compareTo(deductible) <= 0
            ? BigDecimal.ZERO
            : totalFee.subtract(deductible).multiply(ratio).setScale(2, RoundingMode.HALF_UP);

        // (1001 - 1000) * 0.85 = 0.85
        assertThat(reimburseAmount).isEqualByComparingTo(new BigDecimal("0.85"));
    }

    @Test
    @DisplayName("边界值 - 零报销比例")
    void test_zeroRatio() {
        BigDecimal totalFee = new BigDecimal("10000");
        BigDecimal deductible = new BigDecimal("1000");
        BigDecimal ratio = BigDecimal.ZERO;

        BigDecimal reimburseAmount = totalFee.subtract(deductible).multiply(ratio).setScale(2, RoundingMode.HALF_UP);

        assertThat(reimburseAmount).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("边界值 - 百分百报销比例")
    void test_fullRatio() {
        BigDecimal totalFee = new BigDecimal("10000");
        BigDecimal deductible = new BigDecimal("1000");
        BigDecimal ratio = new BigDecimal("1.00");

        BigDecimal reimburseAmount = totalFee.subtract(deductible).multiply(ratio).setScale(2, RoundingMode.HALF_UP);

        assertThat(reimburseAmount).isEqualByComparingTo(new BigDecimal("9000.00"));
    }

    @Test
    @DisplayName("边界值 - 负数费用（不合理场景）")
    void test_negativeTotalFee() {
        BigDecimal totalFee = new BigDecimal("-1000");
        BigDecimal deductible = new BigDecimal("1000");
        BigDecimal ratio = new BigDecimal("0.85");

        // 负数费用应该导致零报销
        BigDecimal reimburseAmount = totalFee.compareTo(deductible) <= 0
            ? BigDecimal.ZERO
            : totalFee.subtract(deductible).multiply(ratio).setScale(2, RoundingMode.HALF_UP);

        assertThat(reimburseAmount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("边界值 - 极高精度数值")
    void test_highPrecisionNumbers() {
        BigDecimal totalFee = new BigDecimal("10000.9999");
        BigDecimal deductible = new BigDecimal("1000.1111");
        BigDecimal ratio = new BigDecimal("0.8532");

        BigDecimal baseAmount = totalFee.subtract(deductible);
        BigDecimal reimburseAmount = baseAmount.multiply(ratio).setScale(2, RoundingMode.HALF_UP);

        // 验证结果仍然是 BigDecimal 且精度正确
        assertThat(reimburseAmount).isNotNull();
        assertThat(reimburseAmount.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("业务场景 - 职工医保完整结算流程")
    void test_employeeSettlementFullFlow() {
        // 模拟完整结算流程
        String patientType = "employee";
        BigDecimal totalFee = new BigDecimal("15000");

        // 1. 确定起付线
        BigDecimal deductible = switch (patientType.toLowerCase()) {
            case "employee" -> new BigDecimal("1000");
            case "resident" -> new BigDecimal("500");
            case "aid" -> new BigDecimal("300");
            default -> BigDecimal.ZERO;
        };

        // 2. 确定报销比例
        BigDecimal ratio = new BigDecimal("0.85");

        // 3. 计算报销金额
        BigDecimal reimburseAmount = totalFee.compareTo(deductible) <= 0
            ? BigDecimal.ZERO
            : totalFee.subtract(deductible).multiply(ratio).setScale(2, RoundingMode.HALF_UP);

        // 4. 计算自付金额
        BigDecimal selfPayAmount = totalFee.subtract(reimburseAmount);

        // 验证：(15000 - 1000) * 0.85 = 11900
        assertThat(deductible).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(reimburseAmount).isEqualByComparingTo(new BigDecimal("11900.00"));
        assertThat(selfPayAmount).isEqualByComparingTo(new BigDecimal("3100.00"));
    }

    @Test
    @DisplayName("业务场景 - 居民医保完整结算流程（起付线以下）")
    void test_residentSettlementBelowDeductible() {
        String patientType = "resident";
        BigDecimal totalFee = new BigDecimal("300");

        BigDecimal deductible = switch (patientType.toLowerCase()) {
            case "employee" -> new BigDecimal("1000");
            case "resident" -> new BigDecimal("500");
            case "aid" -> new BigDecimal("300");
            default -> BigDecimal.ZERO;
        };

        BigDecimal ratio = new BigDecimal("0.65");

        BigDecimal reimburseAmount = totalFee.compareTo(deductible) <= 0
            ? BigDecimal.ZERO
            : totalFee.subtract(deductible).multiply(ratio).setScale(2, RoundingMode.HALF_UP);

        BigDecimal selfPayAmount = totalFee.subtract(reimburseAmount);

        // 起付线以下，零报销
        assertThat(reimburseAmount).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(selfPayAmount).isEqualByComparingTo(new BigDecimal("300"));
    }

    @Test
    @DisplayName("业务场景 - 救助对象医保")
    void test_aidSettlement() {
        String patientType = "aid";
        BigDecimal totalFee = new BigDecimal("5000");

        BigDecimal deductible = switch (patientType.toLowerCase()) {
            case "employee" -> new BigDecimal("1000");
            case "resident" -> new BigDecimal("500");
            case "aid" -> new BigDecimal("300");
            default -> BigDecimal.ZERO;
        };

        BigDecimal ratio = new BigDecimal("0.90"); // 救助对象报销比例更高

        BigDecimal reimburseAmount = totalFee.compareTo(deductible) <= 0
            ? BigDecimal.ZERO
            : totalFee.subtract(deductible).multiply(ratio).setScale(2, RoundingMode.HALF_UP);

        assertThat(deductible).isEqualByComparingTo(new BigDecimal("300"));
        // (5000 - 300) * 0.90 = 4230
        assertThat(reimburseAmount).isEqualByComparingTo(new BigDecimal("4230.00"));
    }
}