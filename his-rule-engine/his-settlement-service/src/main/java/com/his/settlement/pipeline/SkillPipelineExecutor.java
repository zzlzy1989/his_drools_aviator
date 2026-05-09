package com.his.settlement.pipeline;

import com.his.common.ISkill;
import com.his.common.ResultLevel;
import com.his.common.SettlementFact;
import com.his.common.SkillContext;
import com.his.common.SkillResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Skill 管道执行器。
 * 
 * 负责按 order 顺序执行所有注册的 Skill，支持：
 * - 按优先级排序执行
 * - BLOCK 结果时立即阻断管道
 * - 超时控制（默认 30 秒）
 * - 执行结果收集
 * 
 * 使用方式：
 * <pre>
 * SkillContext<SettlementFact> context = new SkillContext<>();
 * context.setPayload(fact);
 * pipelineExecutor.execute(context);
 * </pre>
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@Slf4j
@Component
public class SkillPipelineExecutor {

    private static final long DEFAULT_TIMEOUT_MS = 30000;

    private final List<ISkill<SettlementFact>> skills;

    public SkillPipelineExecutor(List<ISkill<SettlementFact>> skills) {
        this.skills = skills.stream()
                .sorted(Comparator.comparingInt(ISkill::getOrder))
                .toList();
        log.info("SkillPipelineExecutor 初始化完成，已注册 {} 个 Skill", skills.size());
        skills.forEach(s -> log.info("  - Skill: {}, order={}, event={}", 
                s.getClass().getSimpleName(), s.getOrder(), s.supportEvent()));
    }

    /**
     * 执行所有 Skill。
     * 
     * @param context 执行上下文，包含 Fact 数据和结果收集
     */
    public void execute(SkillContext<SettlementFact> context) {
        long startTime = System.currentTimeMillis();
        log.info("SkillPipelineExecutor 开始执行: eventType={}, skillCount={}", 
                context.getEventType(), skills.size());

        context.addResult(new SkillResult(ResultLevel.PASS, "Pipeline", "结算流程启动"));

        for (ISkill<SettlementFact> skill : skills) {
            // 检查管道是否已被 BLOCK
            if (context.hasBlock()) {
                log.warn("SkillPipelineExecutor 管道被阻断，剩余 {} 个 Skill 未执行", 
                        skills.indexOf(skill));
                break;
            }

            // 检查超时
            if (System.currentTimeMillis() - startTime > DEFAULT_TIMEOUT_MS) {
                log.error("SkillPipelineExecutor 执行超时: {}ms", DEFAULT_TIMEOUT_MS);
                context.addResult(new SkillResult(ResultLevel.WARN, "Pipeline", 
                        "执行超时(" + DEFAULT_TIMEOUT_MS + "ms)"));
                break;
            }

            // 执行单个 Skill
            executeSingleSkill(skill, context);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("SkillPipelineExecutor 执行完成: elapsed={}ms, results={}, hasBlock={}",
                elapsed, context.getResults().size(), context.hasBlock());
    }

    /**
     * 获取所有注册的 Skill 信息。
     */
    public List<SkillInfo> getRegisteredSkills() {
        return skills.stream()
                .map(s -> new SkillInfo(s.getClass().getSimpleName(), s.getOrder(), s.supportEvent()))
                .toList();
    }

    private void executeSingleSkill(ISkill<SettlementFact> skill, SkillContext<SettlementFact> context) {
        String skillName = skill.getClass().getSimpleName();
        long skillStart = System.currentTimeMillis();
        
        try {
            log.debug("开始执行 Skill: {}", skillName);
            skill.execute(context);
            
            long skillElapsed = System.currentTimeMillis() - skillStart;
            log.debug("Skill 执行完成: {}, 耗时={}ms", skillName, skillElapsed);
        } catch (Exception e) {
            log.error("Skill 执行异常: {}", skillName, e);
            context.addResult(new SkillResult(ResultLevel.WARN, skillName, 
                    "执行异常: " + e.getMessage()));
        }
    }

    /**
     * Skill 信息内部类。
     */
    public record SkillInfo(String name, int order, String event) {
    }
}
