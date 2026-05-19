package com.his.common.drools.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.builder.KieModule;

import java.util.concurrent.TimeUnit;

/**
 * 规则定义缓存
 *
 * <p>使用 Caffeine 缓存已编译的规则模块</p>
 */
@Slf4j
public class RuleDefinitionCache {

    private final Cache<String, KieModule> cache;

    public RuleDefinitionCache(int maxSize, long expireMinutes) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(expireMinutes, TimeUnit.MINUTES)
                .recordStats()
                .build();

        log.info("RuleDefinitionCache initialized: maxSize={}, expireMinutes={}", maxSize, expireMinutes);
    }

    /**
     * 获取缓存的规则模块
     *
     * @param key 缓存键（通常是规则组名称）
     * @return 规则模块，不存在返回 null
     */
    public KieModule get(String key) {
        return cache.getIfPresent(key);
    }

    /**
     * 缓存规则模块
     *
     * @param key 缓存键
     * @param module 规则模块
     */
    public void put(String key, KieModule module) {
        cache.put(key, module);
        log.debug("Rule module cached: key={}", key);
    }

    /**
     * 使缓存失效
     *
     * @param key 缓存键
     */
    public void invalidate(String key) {
        cache.invalidate(key);
        log.info("Rule cache invalidated: key={}", key);
    }

    /**
     * 清空所有缓存
     */
    public void invalidateAll() {
        cache.invalidateAll();
        log.info("All rule caches invalidated");
    }

    /**
     * 获取缓存命中率统计
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
