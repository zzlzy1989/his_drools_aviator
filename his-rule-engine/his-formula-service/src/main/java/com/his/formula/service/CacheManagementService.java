package com.his.formula.service;

import com.his.common.aviator.cache.AviatorExpressionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 缓存管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheManagementService {

    private final AviatorExpressionCache aviatorExpressionCache;

    /**
     * 获取缓存统计
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // Aviator 表达式缓存统计
        var aviatorStats = aviatorExpressionCache.getStats();
        stats.put("aviatorCache", Map.of(
                "size", aviatorExpressionCache.getSize(),
                "stats", aviatorStats
        ));

        stats.put("timestamp", System.currentTimeMillis());
        return stats;
    }

    /**
     * 获取缓存key列表（简化版，只返回数量）
     */
    public Map<String, Object> getCacheKeys(String cacheName) {
        Map<String, Object> result = new LinkedHashMap<>();

        if ("aviator".equals(cacheName)) {
            result.put("cacheName", "aviator");
            result.put("size", aviatorExpressionCache.getSize());
            result.put("message", "缓存key过多，不列出具体key");
        } else {
            result.put("error", "未知缓存: " + cacheName);
        }

        return result;
    }

    /**
     * 刷新指定缓存
     */
    public Map<String, Object> invalidateCache(String cacheName, String key) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cacheName", cacheName);
        result.put("key", key);

        if ("aviator".equals(cacheName)) {
            if (key == null || key.isBlank()) {
                aviatorExpressionCache.invalidateAll();
                result.put("action", "invalidateAll");
                result.put("success", true);
            } else {
                aviatorExpressionCache.invalidate(key);
                result.put("action", "invalidate");
                result.put("success", true);
            }
        } else {
            result.put("success", false);
            result.put("error", "未知缓存: " + cacheName);
        }

        log.info("缓存刷新: cacheName={}, key={}, success={}", cacheName, key, result.get("success"));
        return result;
    }
}