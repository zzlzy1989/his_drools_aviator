package com.his.market.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TemplateSecurityScanner 单元测试
 */
@DisplayName("模板安全扫描器测试")
class TemplateSecurityScannerTest {

    private final TemplateSecurityScanner scanner = new TemplateSecurityScanner();

    @Test
    @DisplayName("DRL扫描 - 安全内容通过")
    void testScanDrl_SafeContent() {
        String safeDrl = """
            package com.his.rules.reimburse;
            rule "1. 身份校验"
                when
                    $f: SettlementFact(patientType != null)
                then
                    log.info("校验通过");
                end
            """;

        TemplateSecurityScanner.ScanResult result = scanner.scanDrl(safeDrl);

        assertThat(result.isPass()).isTrue();
        assertThat(result.getMessages()).isEmpty();
    }

    @Test
    @DisplayName("DRL扫描 - 危险模式检测")
    void testScanDrl_DangerousPattern() {
        String dangerousDrl = """
            package com.his.rules;
            rule "test"
                when
                then
                    java.lang.System.exit(0);
                end
            """;

        TemplateSecurityScanner.ScanResult result = scanner.scanDrl(dangerousDrl);

        assertThat(result.isPass()).isFalse();
        assertThat(result.getMessages()).anyMatch(m -> m.contains("java.lang.System"));
    }

    @Test
    @DisplayName("DRL扫描 - 长度超限")
    void testScanDrl_LengthExceeded() {
        String longDrl = "a".repeat(60000);

        TemplateSecurityScanner.ScanResult result = scanner.scanDrl(longDrl);

        assertThat(result.isPass()).isFalse();
        assertThat(result.getMessages()).anyMatch(m -> m.contains("DRL内容超限"));
    }

    @Test
    @DisplayName("Aviator扫描 - 安全公式通过")
    void testScanAviator_SafeFormula() {
        String safeFormula = "round((totalFee - deductible) * ratio, 2)";

        TemplateSecurityScanner.ScanResult result = scanner.scanAviator(safeFormula);

        assertThat(result.isPass()).isTrue();
        assertThat(result.getMessages()).isEmpty();
    }

    @Test
    @DisplayName("Aviator扫描 - 危险函数检测")
    void testScanAviator_DangerousFunction() {
        String dangerousFormula = "sys.exec('ls')";

        TemplateSecurityScanner.ScanResult result = scanner.scanAviator(dangerousFormula);

        assertThat(result.isPass()).isFalse();
        assertThat(result.getMessages()).anyMatch(m -> m.contains("sys."));
    }

    @Test
    @DisplayName("Aviator扫描 - 长度超限")
    void testScanAviator_LengthExceeded() {
        String longFormula = "a".repeat(600);

        TemplateSecurityScanner.ScanResult result = scanner.scanAviator(longFormula);

        assertThat(result.isPass()).isFalse();
        assertThat(result.getMessages()).anyMatch(m -> m.contains("公式长度超限"));
    }

    @Test
    @DisplayName("综合扫描 - DRL和Aviator都安全")
    void testScan_AllSafe() {
        String safeDrl = "rule 'test' when $f: Object() then end";
        String safeFormula = "totalFee + deductible";

        TemplateSecurityScanner.SecurityScanResult result = scanner.scan(safeDrl, safeFormula);

        assertThat(result.isPass()).isTrue();
    }

    @Test
    @DisplayName("综合扫描 - DRL危险")
    void testScan_DrlDangerous() {
        String dangerousDrl = "java.lang.Runtime.getRuntime().exec('rm -rf /')";
        String safeFormula = "totalFee * ratio";

        TemplateSecurityScanner.SecurityScanResult result = scanner.scan(dangerousDrl, safeFormula);

        assertThat(result.isPass()).isFalse();
        assertThat(result.getMessages()).anyMatch(m -> m.contains("Runtime"));
    }

    @Test
    @DisplayName("综合扫描 - Aviator危险")
    void testScan_AviatorDangerous() {
        String safeDrl = "rule 'test' when then end";
        String dangerousFormula = "fn.eval('malicious code')";

        TemplateSecurityScanner.SecurityScanResult result = scanner.scan(safeDrl, dangerousFormula);

        assertThat(result.isPass()).isFalse();
        assertThat(result.getMessages()).anyMatch(m -> m.contains("fn."));
    }

    @Test
    @DisplayName("空内容扫描")
    void testScan_NullContent() {
        TemplateSecurityScanner.SecurityScanResult result = scanner.scan(null, null);

        assertThat(result.isPass()).isTrue();
    }
}