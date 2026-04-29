package com.his.settlement.service;

import com.googlecode.aviator.Expression;
import com.his.common.aviator.helper.AviatorHelper;
import com.his.settlement.entity.FormulaEntity;
import com.his.settlement.mapper.FormulaEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 公式加载器服务
 * 从数据库加载公式并缓存，使用 Aviator 执行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormulaLoaderService {

    private final FormulaEntityMapper formulaMapper;

    /**
     * 公式缓存：key = tenantId + formulaKey
     */
    private final ConcurrentHashMap<String, FormulaCache> formulaCache = new ConcurrentHashMap<>();

    /**
     * 加载并执行报销公式
     */
    public BigDecimal executeReimburseFormula(String tenantId, String patientType, BigDecimal totalFee,
                                              BigDecimal deductible, BigDecimal ratio) {
        String formulaKey = "formula.reimburse." + patientType.toLowerCase();
        String cacheKey = tenantId + ":" + formulaKey;

        String formulaText = getFormulaText(cacheKey, formulaKey, tenantId);

        if (formulaText == null) {
            log.warn("公式未找到，使用硬编码计算: key={}", formulaKey);
            return calculateHardcoded(patientType, totalFee, deductible, ratio);
        }

        // 公式执行前语法校验
        if (!AviatorHelper.validate(formulaText)) {
            log.error("公式语法校验失败: key={}, formula={}", formulaKey, formulaText);
            return calculateHardcoded(patientType, totalFee, deductible, ratio);
        }

        try {
            Map<String, Object> env = Map.of(
                "totalFee", totalFee,
                "deductible", deductible,
                "ratio", ratio
            );

            Object result = AviatorHelper.execute(formulaText, env);

            if (result instanceof BigDecimal bd) {
                return bd.setScale(2, java.math.RoundingMode.HALF_UP);
            }
            return BigDecimal.valueOf(((Number) result).doubleValue()).setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("公式执行失败: key={}, formula={}, error={}", formulaKey, formulaText, e.getMessage());
            return calculateHardcoded(patientType, totalFee, deductible, ratio);
        }
    }

    /**
     * 获取公式文本（带缓存）
     */
    private String getFormulaText(String cacheKey, String formulaKey, String tenantId) {
        FormulaCache cache = formulaCache.get(cacheKey);

        if (cache != null && !cache.isExpired()) {
            return cache.formulaText;
        }

        // 从数据库加载
        FormulaEntity entity = formulaMapper.selectByKey(formulaKey, tenantId);
        if (entity == null || entity.getFormulaText() == null) {
            return null;
        }

        FormulaCache newCache = new FormulaCache(entity.getFormulaText(), System.currentTimeMillis());
        formulaCache.put(cacheKey, newCache);

        log.info("公式加载成功: key={}, text={}", formulaKey, entity.getFormulaText());
        return entity.getFormulaText();
    }

    /**
     * 硬编码计算（降级方案）
     */
    private BigDecimal calculateHardcoded(String patientType, BigDecimal totalFee,
                                          BigDecimal deductible, BigDecimal ratio) {
        if (totalFee.compareTo(deductible) <= 0) {
            return BigDecimal.ZERO;
        }
        return totalFee.subtract(deductible).multiply(ratio).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 刷新公式缓存
     */
    public void refreshCache(String tenantId, String formulaKey) {
        String cacheKey = tenantId + ":" + formulaKey;
        formulaCache.remove(cacheKey);
        log.info("公式缓存已刷新: key={}", cacheKey);
    }

    /**
     * 清空所有缓存
     */
    public void clearCache() {
        formulaCache.clear();
        log.info("公式缓存已清空");
    }

    /**
     * 公式缓存项
     */
    private static class FormulaCache {
        final String formulaText;
        final long createTime;
        static final long EXPIRE_MS = 5 * 60 * 1000; // 5分钟过期

        FormulaCache(String formulaText, long createTime) {
            this.formulaText = formulaText;
            this.createTime = createTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createTime > EXPIRE_MS;
        }
    }
}