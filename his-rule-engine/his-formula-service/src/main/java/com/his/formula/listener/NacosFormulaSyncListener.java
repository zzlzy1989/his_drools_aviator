package com.his.formula.listener;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.formula.entity.AviatorFormula;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Nacos 公式同步监听器
 *
 * <p>监听公式发布事件，将公式同步到 Nacos 配置中心</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NacosFormulaSyncListener {

    private final NacosConfigManager nacosConfigManager;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String FORMULA_DATA_ID_PREFIX = "his-formula-";
    private static final String FORMULA_GROUP = "HIS_RULE_ENGINE";

    /**
     * 处理公式发布事件
     */
    @EventListener
    public void onFormulaPublish(FormulaPublishEvent event) {
        AviatorFormula formula = event.getFormula();
        String action = event.getAction();

        log.info("收到公式发布事件: action={}, formulaKey={}, tenantId={}",
                action, formula.getFormulaKey(), formula.getTenantId());

        try {
            switch (action) {
                case "PUBLISH", "ACTIVATE" -> publishToNacos(formula);
                case "DEACTIVATE" -> removeFromNacos(formula);
            }
        } catch (Exception e) {
            log.error("公式同步Nacos失败: formulaKey={}", formula.getFormulaKey(), e);
        }
    }

    /**
     * 发布公式到 Nacos
     */
    private void publishToNacos(AviatorFormula formula) {
        try {
            String dataId = buildDataId(formula.getFormulaKey(), formula.getTenantId());

            Map<String, Object> formulaConfig = new HashMap<>();
            formulaConfig.put("formulaKey", formula.getFormulaKey());
            formulaConfig.put("formulaText", formula.getFormulaText());
            formulaConfig.put("category", formula.getCategory());
            formulaConfig.put("version", formula.getVersion());
            formulaConfig.put("status", formula.getStatus());

            String content = objectMapper.writeValueAsString(formulaConfig);

            nacosConfigManager.getConfigService()
                    .publishConfig(dataId, FORMULA_GROUP, content, "JSON");

            log.info("公式已同步到Nacos: dataId={}, group={}", dataId, FORMULA_GROUP);

            publishToRedis(formula);

        } catch (Exception e) {
            log.error("发布公式到Nacos失败: formulaKey={}", formula.getFormulaKey(), e);
            throw new RuntimeException("Nacos同步失败", e);
        }
    }

    /**
     * 从 Nacos 移除公式
     */
    private void removeFromNacos(AviatorFormula formula) {
        try {
            String dataId = buildDataId(formula.getFormulaKey(), formula.getTenantId());

            nacosConfigManager.getConfigService()
                    .deleteConfig(dataId, FORMULA_GROUP);

            log.info("公式已从Nacos删除: dataId={}", dataId);

            removeFromRedis(formula);

        } catch (Exception e) {
            log.error("从Nacos删除公式失败: formulaKey={}", formula.getFormulaKey(), e);
        }
    }

    /**
     * 发布到 Redis（作为二级缓存）
     */
    private void publishToRedis(AviatorFormula formula) {
        try {
            String key = buildRedisKey(formula.getFormulaKey(), formula.getTenantId());
            Map<String, Object> formulaConfig = new HashMap<>();
            formulaConfig.put("formulaKey", formula.getFormulaKey());
            formulaConfig.put("formulaText", formula.getFormulaText());
            formulaConfig.put("category", formula.getCategory());
            formulaConfig.put("version", formula.getVersion());

            redisTemplate.opsForHash().putAll(key, formulaConfig);
            redisTemplate.expire(key, java.time.Duration.ofHours(24));

            log.debug("公式已同步到Redis: key={}", key);
        } catch (Exception e) {
            log.warn("Redis同步失败（不影响主流程）: formulaKey={}", formula.getFormulaKey(), e);
        }
    }

    /**
     * 从 Redis 移除
     */
    private void removeFromRedis(AviatorFormula formula) {
        try {
            String key = buildRedisKey(formula.getFormulaKey(), formula.getTenantId());
            redisTemplate.delete(key);
            log.debug("公式已从Redis删除: key={}", key);
        } catch (Exception e) {
            log.warn("Redis删除失败: formulaKey={}", formula.getFormulaKey(), e);
        }
    }

    /**
     * 构建 DataId
     */
    private String buildDataId(String formulaKey, String tenantId) {
        return FORMULA_DATA_ID_PREFIX + tenantId + "-" + formulaKey.replace(".", "_");
    }

    /**
     * 构建 Redis Key
     */
    private String buildRedisKey(String formulaKey, String tenantId) {
        return "formula:" + tenantId + ":" + formulaKey;
    }
}
