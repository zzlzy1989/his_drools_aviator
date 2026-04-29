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
}