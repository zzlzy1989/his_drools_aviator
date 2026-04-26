package com.his.formula.validator;

import com.his.common.aviator.helper.AviatorHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 公式语法校验器
 */
@Slf4j
@Component
public class FormulaValidator {

    /**
     * 校验公式语法
     *
     * @param formulaText 公式内容
     * @return 校验结果
     */
    public ValidationResult validate(String formulaText) {
        if (formulaText == null || formulaText.isBlank()) {
            return ValidationResult.fail("公式内容不能为空");
        }

        if (formulaText.length() > 1000) {
            return ValidationResult.fail("公式内容不超过1000字符");
        }

        if (AviatorHelper.containsDangerousFunctions(formulaText)) {
            return ValidationResult.fail("公式包含不允许的函数");
        }

        String error = AviatorHelper.getValidationError(formulaText);
        if (error != null) {
            log.warn("公式语法校验失败: {}", error);
            return ValidationResult.fail(error);
        }

        return ValidationResult.ok();
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
