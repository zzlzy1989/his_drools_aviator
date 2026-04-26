package com.his.common.aviator.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.his.common.aviator.config.AviatorConfig;
import com.his.common.aviator.helper.AviatorHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Aviator 表达式缓存
 *
 * <p>使用 Caffeine 缓存编译后的 Expression 对象</p>
 */
@Slf4j
@Component
public class AviatorExpressionCache {

    private final Cache<String, com.googlecode.aviator.Expression> cache;
    private final AviatorConfig config;

    public AviatorExpressionCache(AviatorConfig config) {
        this.config = config;
        this.cache = Caffeine.newBuilder()
                .maximumSize(config.getCacheMaxSize())
                .expireAfterAccess(config.getCacheExpireMinutes(), TimeUnit.MINUTES)
                .recordStats()
                .build();

        log.info("AviatorExpressionCache initialized: maxSize={}, expireMinutes={}",
                config.getCacheMaxSize(), config.getCacheExpireMinutes());
    }

    /**
     * 获取编译后的表达式
     *
     * @param expression 表达式文本
     * @return 编译后的 Expression 对象
     */
    public com.googlecode.aviator.Expression getCompiledExpression(String expression) {
        return cache.get(expression, key -> {
            long start = System.nanoTime();
            try {
                com.googlecode.aviator.Expression compiled = AviatorHelper.compile(expression);
                long costMs = (System.nanoTime() - start) / 1_000_000;

                if (costMs > 1) {
                    log.warn("Aviator编译耗时较长: {}ms, expr={}",
                            costMs, key.substring(0, Math.min(50, key.length())));
                }

                return compiled;
            } catch (Exception e) {
                log.error("Aviator表达式编译失败: {}", key, e);
                throw e;
            }
        });
    }

    /**
     * 获取编译后的表达式（不缓存失败结果）
     *
     * @param expression 表达式文本
     * @param cacheOnFailure 编译失败时是否缓存异常
     * @return 编译后的 Expression 对象
     */
    public com.googlecode.aviator.Expression getCompiledExpression(String expression, boolean cacheOnFailure) {
        if (cacheOnFailure) {
            return getCompiledExpression(expression);
        }
        return AviatorHelper.compile(expression);
    }

    /**
     * 判断表达式是否已缓存
     *
     * @param expression 表达式文本
     * @return true 表示已缓存
     */
    public boolean isCached(String expression) {
        return cache.getIfPresent(expression) != null;
    }

    /**
     * 使表达式缓存失效
     *
     * @param expression 表达式文本
     */
    public void invalidate(String expression) {
        cache.invalidate(expression);
        log.debug("Aviator expression cache invalidated: {}", expression);
    }

    /**
     * 清空所有缓存
     */
    public void invalidateAll() {
        cache.invalidateAll();
        log.info("All Aviator expression caches invalidated");
    }

    /**
     * 获取缓存统计信息
     *
     * @return 统计信息字符串
     */
    public String getStats() {
        var stats = cache.stats();
        return String.format("hitCount=%d, missCount=%d, hitRate=%.2f%%, evictionCount=%d",
                stats.hitCount(), stats.missCount(), stats.hitRate() * 100, stats.evictionCount());
    }

    /**
     * 获取当前缓存大小
     *
     * @return 缓存条目数
     */
    public long getSize() {
        return cache.estimatedSize();
    }
}
