package com.his.market.security;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 规则模板安全扫描器
 * 发布模板时自动扫描 DRL 危险模式和 Aviator 禁止函数
 */
@Slf4j
@Component
public class TemplateSecurityScanner {

    private static final List<String> DANGEROUS_DRL_PATTERNS = List.of(
            "java.lang.System",
            "Runtime.getRuntime",
            "ProcessBuilder",
            "java.io.File",
            "java.io.InputStream",
            "java.io.OutputStream",
            "java.io.Reader",
            "java.io.Writer",
            "java.net.Socket",
            "java.net.URL",
            "java.lang.reflect.",
            "Class.forName",
            "exec(",
            "eval("
    );

    private static final List<String> DANGEROUS_AVIATOR_FUNCTIONS = List.of(
            "sys.",
            "fn.",
            "exec",
            "eval",
            "import",
            "include"
    );

    private static final int MAX_FORMULA_LENGTH = 500;
    private static final int MAX_DRL_LENGTH = 50000;

    /**
     * 扫描模板内容
     */
    public SecurityScanResult scan(String drlContent, String aviatorContent) {
        SecurityScanResult result = new SecurityScanResult();

        // 扫描 DRL
        if (drlContent != null && !drlContent.isBlank()) {
            ScanResult drlResult = scanDrl(drlContent);
            if (!drlResult.isPass()) {
                result.setPass(false);
                result.getMessages().addAll(drlResult.getMessages());
            }
        }

        // 扫描 Aviator
        if (aviatorContent != null && !aviatorContent.isBlank()) {
            ScanResult aviatorResult = scanAviator(aviatorContent);
            if (!aviatorResult.isPass()) {
                result.setPass(false);
                result.getMessages().addAll(aviatorResult.getMessages());
            }
        }

        return result;
    }

    /**
     * 扫描 DRL 规则
     */
    public ScanResult scanDrl(String drlContent) {
        ScanResult result = new ScanResult();
        result.setPass(true);

        // 长度检查
        if (drlContent.length() > MAX_DRL_LENGTH) {
            result.setPass(false);
            result.getMessages().add("DRL内容超限(最大" + MAX_DRL_LENGTH + "字符)");
            return result;
        }

        // 危险模式检查
        for (String pattern : DANGEROUS_DRL_PATTERNS) {
            if (drlContent.contains(pattern)) {
                result.setPass(false);
                result.getMessages().add("DRL包含危险模式: " + pattern);
            }
        }

        // 检查是否有 Java 代码块注入
        if (drlContent.contains("when") && drlContent.contains("eval(")) {
            // 简单检查 eval 中的内容
            Pattern evalPattern = Pattern.compile("eval\\s*\\(\\s*([^)]+)\\s*\\)");
            var matcher = evalPattern.matcher(drlContent);
            while (matcher.find()) {
                String evalContent = matcher.group(1);
                if (evalContent.contains("System") || evalContent.contains("Runtime")) {
                    result.setPass(false);
                    result.getMessages().add("DRL eval中禁止调用System/Runtime");
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 扫描 Aviator 公式
     */
    public ScanResult scanAviator(String formulaText) {
        ScanResult result = new ScanResult();
        result.setPass(true);

        // 长度检查
        if (formulaText.length() > MAX_FORMULA_LENGTH) {
            result.setPass(false);
            result.getMessages().add("公式长度超限(最大" + MAX_FORMULA_LENGTH + "字符)");
            return result;
        }

        // 危险函数检查
        String lowerText = formulaText.toLowerCase();
        for (String dangerous : DANGEROUS_AVIATOR_FUNCTIONS) {
            if (lowerText.contains(dangerous)) {
                result.setPass(false);
                result.getMessages().add("公式包含禁止函数: " + dangerous);
            }
        }

        return result;
    }

    @Data
    public static class SecurityScanResult {
        private boolean pass = true;
        private List<String> messages = new ArrayList<>();

        public void addMessage(String msg) {
            messages.add(msg);
        }
    }

    @Data
    public static class ScanResult {
        private boolean pass = true;
        private final List<String> messages = new ArrayList<>();

        public void setPass(boolean pass) {
            this.pass = pass;
        }
    }
}