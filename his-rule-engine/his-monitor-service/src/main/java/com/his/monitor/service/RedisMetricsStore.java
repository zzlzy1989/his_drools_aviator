package com.his.monitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis 指标存储服务
 * <p>使用 Redis 存储监控指标，支持持久化和跨服务共享</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMetricsStore {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "his:monitor:";
    private static final String KEY_EXECUTION_TOTAL = KEY_PREFIX + "execution:total";
    private static final String KEY_EXECUTION_SUCCESS = KEY_PREFIX + "execution:success";
    private static final String KEY_EXECUTION_FAILED = KEY_PREFIX + "execution:failed";
    private static final String KEY_FORMULA_HITS = KEY_PREFIX + "formula:hits";
    private static final String KEY_RULE_HITS = KEY_PREFIX + "rule:hits:";
    private static final String KEY_ALERTS = KEY_PREFIX + "alerts";
    private static final String KEY_DURATIONS = KEY_PREFIX + "durations";
    private static final long DURATION_EXPIRE_HOURS = 24;

    // Lua 脚本用于原子递增
    private static final String INCR_SCRIPT =
            "redis.call('incrby', KEYS[1], ARGV[1]) " +
            "redis.call('expire', KEYS[1], ARGV[2]) " +
            "return redis.call('get', KEYS[1])";

    /**
     * 记录执行次数
     */
    public void recordExecution(boolean success, long durationMs) {
        try {
            // 递增总执行次数
            increment(KEY_EXECUTION_TOTAL);

            if (success) {
                increment(KEY_EXECUTION_SUCCESS);
            } else {
                increment(KEY_EXECUTION_FAILED);
            }

            // 记录耗时用于P99/P95计算
            recordDuration(durationMs);

        } catch (Exception e) {
            log.error("Redis记录执行指标失败", e);
        }
    }

    /**
     * 记录规则命中
     */
    public void recordRuleHit(String ruleKey) {
        try {
            increment(KEY_RULE_HITS + ruleKey);
        } catch (Exception e) {
            log.error("Redis记录规则命中失败: ruleKey={}", ruleKey, e);
        }
    }

    /**
     * 记录公式命中
     */
    public void recordFormulaHit() {
        try {
            increment(KEY_FORMULA_HITS);
        } catch (Exception e) {
            log.error("Redis记录公式命中失败", e);
        }
    }

    /**
     * 记录告警
     */
    public void recordAlert(String alertType, String message, String level) {
        try {
            Map<String, Object> alert = new LinkedHashMap<>();
            alert.put("alertId", UUID.randomUUID().toString());
            alert.put("alertType", alertType);
            alert.put("message", message);
            alert.put("level", level);
            alert.put("alertTime", LocalDateTime.now().toString());

            // 添加到列表头部
            redisTemplate.opsForList().leftPush(KEY_ALERTS, alert);
            // 只保留最近100条
            redisTemplate.opsForList().trim(KEY_ALERTS, 0, 99);
            // 24小时过期
            redisTemplate.expire(KEY_ALERTS, 24, TimeUnit.HOURS);

        } catch (Exception e) {
            log.error("Redis记录告警失败", e);
        }
    }

    /**
     * 获取总执行次数
     */
    public long getExecutionTotal() {
        return getLongValue(KEY_EXECUTION_TOTAL);
    }

    /**
     * 获取成功执行次数
     */
    public long getExecutionSuccess() {
        return getLongValue(KEY_EXECUTION_SUCCESS);
    }

    /**
     * 获取失败执行次数
     */
    public long getExecutionFailed() {
        return getLongValue(KEY_EXECUTION_FAILED);
    }

    /**
     * 获取公式命中次数
     */
    public long getFormulaHits() {
        return getLongValue(KEY_FORMULA_HITS);
    }

    /**
     * 获取规则命中次数
     */
    public Map<String, Long> getRuleHits() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        try {
            Set<String> keys = redisTemplate.keys(KEY_RULE_HITS + "*");
            if (keys != null) {
                for (String key : keys) {
                    String ruleKey = key.substring(KEY_RULE_HITS.length());
                    Long count = getLongValue(key);
                    if (count > 0) {
                        result.put(ruleKey, count);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Redis获取规则命中失败", e);
        }
        return result;
    }

    /**
     * 获取最近告警
     */
    public List<Map<String, Object>> getRecentAlerts(int limit) {
        List<Map<String, Object>> alerts = new ArrayList<>();
        try {
            List<Object> list = redisTemplate.opsForList().range(KEY_ALERTS, 0, limit - 1);
            if (list != null) {
                for (Object item : list) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> alert = (Map<String, Object>) item;
                        alerts.add(alert);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Redis获取最近告警失败", e);
        }
        return alerts;
    }

    /**
     * 获取P99耗时
     */
    public long getP99Duration() {
        return getPercentileDuration(99);
    }

    /**
     * 获取P95耗时
     */
    public long getP95Duration() {
        return getPercentileDuration(95);
    }

    /**
     * 获取平均耗时
     */
    public long getAvgDuration() {
        return getPercentileDuration(50);
    }

    /**
     * 重置所有指标
     */
    public void resetAll() {
        try {
            Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            log.info("Redis指标已重置");
        } catch (Exception e) {
            log.error("Redis重置指标失败", e);
        }
    }

    // ==================== 私有辅助方法 ====================

    private void increment(String key) {
        try {
            redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("Redis increment失败: key={}", key, e);
        }
    }

    private void recordDuration(long durationMs) {
        try {
            String key = KEY_DURATIONS + (durationMs / 10); // 按10ms分组
            redisTemplate.opsForZSet().incrementScore(key, UUID.randomUUID().toString(), durationMs);
            redisTemplate.expire(key, DURATION_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Redis记录耗时失败: durationMs={}", durationMs, e);
        }
    }

    private long getLongValue(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) return 0L;
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            log.error("Redis获取值失败: key={}", key, e);
            return 0L;
        }
    }

    private long getPercentileDuration(int percentile) {
        try {
            // 获取所有耗时记录
            Set<String> durationKeys = redisTemplate.keys(KEY_DURATIONS + "*");
            if (durationKeys == null || durationKeys.isEmpty()) {
                return 0L;
            }

            List<Long> allDurations = new ArrayList<>();
            for (String key : durationKeys) {
                Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> tuples =
                        redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
                if (tuples != null) {
                    for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object> tuple : tuples) {
                        if (tuple.getValue() != null && tuple.getScore() != null) {
                            allDurations.add(tuple.getScore().longValue());
                        }
                    }
                }
            }

            if (allDurations.isEmpty()) {
                return 0L;
            }

            Collections.sort(allDurations);
            int index = (int) Math.ceil(percentile / 100.0 * allDurations.size()) - 1;
            index = Math.max(0, Math.min(index, allDurations.size() - 1));
            return allDurations.get(index);

        } catch (Exception e) {
            log.error("Redis获取百分位耗时失败: percentile={}", percentile, e);
            return 0L;
        }
    }
}