package com.his.monitor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.monitor.dto.AlertRuleDTO;
import com.his.monitor.entity.AlertRule;
import com.his.monitor.mapper.AlertRuleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 告警规则服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertRuleService {

    private final AlertRuleMapper alertRuleMapper;

    private static final String DEFAULT_TENANT = "T001";

    // 触发计数（用于连续触发检测）
    private final Map<Long, AtomicInteger> triggerCounts = new ConcurrentHashMap<>();

    // 上次触发时间（用于冷却期检测）
    private final Map<Long, Long> lastTriggerTimes = new ConcurrentHashMap<>();

    public List<AlertRule> listRules() {
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRule::getTenantId, DEFAULT_TENANT);
        wrapper.eq(AlertRule::getDeleted, false);
        wrapper.orderByDesc(AlertRule::getCreateTime);
        return alertRuleMapper.selectList(wrapper);
    }

    public AlertRule getRule(Long id) {
        return alertRuleMapper.selectById(id);
    }

    public void createRule(AlertRuleDTO dto) {
        AlertRule rule = new AlertRule();
        rule.setRuleName(dto.getRuleName());
        rule.setMetricName(dto.getMetricName());
        rule.setConditionType(dto.getConditionType());
        rule.setThreshold(dto.getThreshold());
        rule.setLevel(dto.getLevel());
        rule.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        rule.setNotifyChannels(dto.getNotifyChannels());
        rule.setNotifyTarget(dto.getNotifyTarget());
        rule.setMessageTemplate(dto.getMessageTemplate());
        rule.setConsecutiveTriggers(dto.getConsecutiveTriggers() != null ? dto.getConsecutiveTriggers() : 1);
        rule.setCooldownSeconds(dto.getCooldownSeconds() != null ? dto.getCooldownSeconds() : 300);
        rule.setTenantId(DEFAULT_TENANT);
        alertRuleMapper.insert(rule);
        log.info("创建告警规则: {}", rule.getRuleName());
    }

    public void updateRule(Long id, AlertRuleDTO dto) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule == null) {
            throw new RuntimeException("告警规则不存在: " + id);
        }
        rule.setRuleName(dto.getRuleName());
        rule.setMetricName(dto.getMetricName());
        rule.setConditionType(dto.getConditionType());
        rule.setThreshold(dto.getThreshold());
        rule.setLevel(dto.getLevel());
        rule.setEnabled(dto.getEnabled());
        rule.setNotifyChannels(dto.getNotifyChannels());
        rule.setNotifyTarget(dto.getNotifyTarget());
        rule.setMessageTemplate(dto.getMessageTemplate());
        rule.setConsecutiveTriggers(dto.getConsecutiveTriggers());
        rule.setCooldownSeconds(dto.getCooldownSeconds());
        alertRuleMapper.updateById(rule);
        log.info("更新告警规则: {}", rule.getRuleName());
    }

    public void deleteRule(Long id) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule != null) {
            rule.setDeleted(true);
            alertRuleMapper.updateById(rule);
            log.info("删除告警规则: {}", rule.getRuleName());
        }
    }

    /**
     * 检查指标是否触发告警
     */
    public List<AlertRule> checkAlerts(String metricName, BigDecimal value) {
        List<AlertRule> triggeredRules = new ArrayList<>();

        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRule::getTenantId, DEFAULT_TENANT);
        wrapper.eq(AlertRule::getDeleted, false);
        wrapper.eq(AlertRule::getEnabled, true);
        wrapper.eq(AlertRule::getMetricName, metricName);

        List<AlertRule> rules = alertRuleMapper.selectList(wrapper);

        for (AlertRule rule : rules) {
            if (evaluateCondition(rule, value)) {
                // 检查冷却期
                if (isInCooldown(rule.getId())) {
                    continue;
                }

                // 检查连续触发次数
                int count = triggerCounts.computeIfAbsent(rule.getId(), k -> new AtomicInteger(0)).incrementAndGet();
                if (count >= rule.getConsecutiveTriggers()) {
                    triggeredRules.add(rule);
                    lastTriggerTimes.put(rule.getId(), System.currentTimeMillis());
                    triggerCounts.remove(rule.getId());
                }
            } else {
                // 重置计数
                triggerCounts.remove(rule.getId());
            }
        }

        return triggeredRules;
    }

    private boolean evaluateCondition(AlertRule rule, BigDecimal value) {
        double threshold = rule.getThreshold();
        double actual = value.doubleValue();

        return switch (rule.getConditionType()) {
            case "GT" -> actual > threshold;
            case "LT" -> actual < threshold;
            case "EQ" -> Math.abs(actual - threshold) < 0.0001;
            case "GTE" -> actual >= threshold;
            case "LTE" -> actual <= threshold;
            default -> false;
        };
    }

    private boolean isInCooldown(Long ruleId) {
        Long lastTime = lastTriggerTimes.get(ruleId);
        if (lastTime == null) {
            return false;
        }
        AlertRule rule = alertRuleMapper.selectById(ruleId);
        if (rule == null) {
            return false;
        }
        long elapsed = System.currentTimeMillis() - lastTime;
        return elapsed < (rule.getCooldownSeconds() * 1000L);
    }

    /**
     * 每分钟清理过期数据
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredData() {
        // 清理过期的触发计数
        long now = System.currentTimeMillis();
        lastTriggerTimes.entrySet().removeIf(entry -> {
            AlertRule rule = alertRuleMapper.selectById(entry.getKey());
            if (rule == null) {
                return true;
            }
            return (now - entry.getValue()) > (rule.getCooldownSeconds() * 1000L * 2);
        });
    }
}