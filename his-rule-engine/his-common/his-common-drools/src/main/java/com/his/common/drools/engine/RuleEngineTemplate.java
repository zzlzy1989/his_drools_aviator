package com.his.common.drools.engine;

import com.his.common.SkillContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;

import java.util.concurrent.TimeUnit;

/**
 * 规则执行模板
 *
 * <p>封装规则执行的完整生命周期</p>
 */
@Slf4j
@RequiredArgsConstructor
public class RuleEngineTemplate {

    private final KieSession kieSession;
    private final long timeoutMs;

    /**
     * 执行规则
     *
     * @param context Skill上下文
     * @param fact 业务 Fact 对象
     */
    public void fireRules(SkillContext<?> context, Object fact) {
        try {
            kieSession.setGlobal("context", context);
            kieSession.insert(fact);

            long startTime = System.nanoTime();
            int rulesFired = kieSession.fireAllRules();
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

            log.info("规则执行完成: rulesFired={}, elapsedMs={}", rulesFired, elapsedMs);

            if (elapsedMs > timeoutMs) {
                log.warn("规则执行超时: elapsedMs={}, timeoutMs={}", elapsedMs, timeoutMs);
            }
        } finally {
            kieSession.dispose();
        }
    }

    /**
     * 执行规则（静态方法版本）
     *
     * @param kieSession KIE Session
     * @param fact 业务 Fact 对象
     * @param timeoutMs 超时时间
     */
    public static void execute(KieSession kieSession, Object fact, long timeoutMs) {
        RuleEngineTemplate template = new RuleEngineTemplate(kieSession, timeoutMs);
        SkillContext<?> context = new SkillContext<>();
        template.fireRules(context, fact);
    }
}
