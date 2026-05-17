package com.his.rule.engine;

import com.his.rule.entity.RuleDefinition;
import com.his.rule.mapper.RuleDefinitionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drools 规则执行器
 *
 * <p>从数据库加载规则并使用 Drools 执行</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DroolsRuleExecutor {

    private final RuleDefinitionMapper ruleDefinitionMapper;
    private final KieServices kieServices = KieServices.Factory.get();
    private final Map<String, KieContainer> containerCache = new ConcurrentHashMap<>();

    /**
     * 执行规则
     *
     * @param ruleKey 规则Key
     * @param fact    事实对象
     * @return 更新后的 fact
     */
    public Object execute(String ruleKey, Object fact) {
        log.info("Drools执行规则: ruleKey={}", ruleKey);

        try {
            // 1. 获取规则定义
            RuleDefinition rule = getActiveRule(ruleKey);
            if (rule == null || rule.getRuleText() == null) {
                log.warn("规则不存在或未激活: ruleKey={}", ruleKey);
                return fact;
            }

            // 2. 获取或创建 KieContainer
            KieContainer kieContainer = getKieContainer(ruleKey, rule.getRuleText());

            // 3. 创建 KieSession 并执行
            KieSession kieSession = kieContainer.newKieSession();
            try {
                kieSession.insert(fact);
                int fired = kieSession.fireAllRules();
                log.info("Drools规则执行完成: ruleKey={}, firedCount={}", ruleKey, fired);
            } finally {
                kieSession.dispose();
            }

            return fact;

        } catch (Exception e) {
            log.error("Drools规则执行异常: ruleKey={}, error={}", ruleKey, e.getMessage(), e);
            return fact;
        }
    }

    /**
     * 获取激活的规则
     */
    private RuleDefinition getActiveRule(String ruleKey) {
        return ruleDefinitionMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RuleDefinition>()
                .eq(RuleDefinition::getRuleKey, ruleKey)
                .eq(RuleDefinition::getStatus, "active")
                .eq(RuleDefinition::getDeleted, 0)
        );
    }

    /**
     * 获取或创建 KieContainer（带缓存）
     */
    private KieContainer getKieContainer(String ruleKey, String drlContent) {
        return containerCache.computeIfAbsent(ruleKey, key -> {
            log.info("编译DRL并创建KieContainer: ruleKey={}", ruleKey);

            KieFileSystem kfs = kieServices.newKieFileSystem();
            kfs.write("src/main/resources/rules/" + ruleKey.replace(".", "/") + ".drl", drlContent);

            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
            kieBuilder.buildAll();

            Results results = kieBuilder.getResults();
            if (results.hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                log.error("DRL编译错误: ruleKey={}, messages={}", ruleKey, results.getMessages());
                throw new RuntimeException("DRL编译失败: " + results.getMessages());
            }

            KieModule kieModule = kieBuilder.getKieModule();
            // 使用 newKieContainer 从 KieModule 获取容器
            return kieServices.newKieContainer(kieModule.getReleaseId());
        });
    }

    /**
     * 刷新规则缓存（热更新）
     */
    public void refreshRule(String ruleKey) {
        containerCache.remove(ruleKey);
        log.info("规则缓存已刷新: ruleKey={}", ruleKey);
    }

    /**
     * 清空所有缓存
     */
    public void clearCache() {
        containerCache.clear();
        log.info("KieContainer缓存已清空");
    }
}
