package com.his.rule.validator;

import com.his.common.drools.helper.DroolsHelper;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.builder.Results;
import org.springframework.stereotype.Component;

/**
 * DRL 规则校验器
 */
@Slf4j
@Component
public class DrlValidator {

    /**
     * 校验 DRL 语法
     *
     * @param drlContent DRL 内容
     * @return 校验结果
     */
    public ValidationResult validate(String drlContent) {
        if (drlContent == null || drlContent.isBlank()) {
            return ValidationResult.fail("DRL内容不能为空");
        }

        if (drlContent.length() > 50000) {
            return ValidationResult.fail("DRL内容不超过50000字符");
        }

        try {
            Results results = DroolsHelper.validateDrl(drlContent);

            if (results.hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                StringBuilder errors = new StringBuilder();
                results.getMessages(org.kie.api.builder.Message.Level.ERROR)
                        .forEach(msg -> errors.append(msg.toString()).append("; "));
                log.warn("DRL校验失败: {}", errors);
                return ValidationResult.fail(errors.toString());
            }

            if (results.hasMessages(org.kie.api.builder.Message.Level.WARNING)) {
                StringBuilder warnings = new StringBuilder();
                results.getMessages(org.kie.api.builder.Message.Level.WARNING)
                        .forEach(msg -> warnings.append(msg.toString()).append("; "));
                log.info("DRL校验警告: {}", warnings);
                return ValidationResult.ok(warnings.toString());
            }

            return ValidationResult.ok();
        } catch (Exception e) {
            log.error("DRL校验异常", e);
            return ValidationResult.fail("DRL校验异常: " + e.getMessage());
        }
    }

    /**
     * 校验结果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult ok(String message) {
            return new ValidationResult(true, message);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
