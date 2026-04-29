package com.his.formula.listener;

import com.his.formula.entity.AviatorFormula;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 公式发布事件
 */
@Getter
public class FormulaPublishEvent extends ApplicationEvent {

    private final AviatorFormula formula;
    private final String action;

    public FormulaPublishEvent(Object source, AviatorFormula formula, String action) {
        super(source);
        this.formula = formula;
        this.action = action;
    }

    public static FormulaPublishEvent published(Object source, AviatorFormula formula) {
        return new FormulaPublishEvent(source, formula, "PUBLISH");
    }

    public static FormulaPublishEvent activated(Object source, AviatorFormula formula) {
        return new FormulaPublishEvent(source, formula, "ACTIVATE");
    }

    public static FormulaPublishEvent deactivated(Object source, AviatorFormula formula) {
        return new FormulaPublishEvent(source, formula, "DEACTIVATE");
    }
}
