package com.his.monitor.service;

import com.his.monitor.dto.AlertRuleDTO;
import com.his.monitor.entity.AlertRule;
import com.his.monitor.mapper.AlertRuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AlertRuleService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("告警规则服务测试")
class AlertRuleServiceTest {

    @Mock
    private AlertRuleMapper alertRuleMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AlertRuleService alertRuleService;

    private AlertRule sampleRule;

    @BeforeEach
    void setUp() {
        sampleRule = new AlertRule();
        sampleRule.setId(1L);
        sampleRule.setRuleName("测试告警规则");
        sampleRule.setMetricName("totalExecutions");
        sampleRule.setConditionType("GT");
        sampleRule.setThreshold(100.0);
        sampleRule.setLevel("WARN");
        sampleRule.setEnabled(true);
        sampleRule.setDeleted(false);
        sampleRule.setTenantId("T001");
        sampleRule.setConsecutiveTriggers(1);
        sampleRule.setCooldownSeconds(300);
    }

    @Test
    @DisplayName("创建告警规则 - 正常创建")
    void testCreateRule() {
        AlertRuleDTO dto = new AlertRuleDTO();
        dto.setRuleName("新告警规则");
        dto.setMetricName("totalExecutions");
        dto.setConditionType("GT");
        dto.setThreshold(100.0);
        dto.setLevel("WARN");
        dto.setEnabled(true);

        alertRuleService.createRule(dto);

        ArgumentCaptor<AlertRule> captor = ArgumentCaptor.forClass(AlertRule.class);
        verify(alertRuleMapper).insert(captor.capture());

        AlertRule saved = captor.getValue();
        assertThat(saved.getRuleName()).isEqualTo("新告警规则");
        assertThat(saved.getMetricName()).isEqualTo("totalExecutions");
        assertThat(saved.getTenantId()).isEqualTo("T001");
    }

    @Test
    @DisplayName("查询告警规则列表")
    void testListRules() {
        when(alertRuleMapper.selectList(any())).thenReturn(List.of(sampleRule));

        List<AlertRule> rules = alertRuleService.listRules();

        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).getRuleName()).isEqualTo("测试告警规则");
    }

    @Test
    @DisplayName("条件判断 - GT大于阈值")
    void testEvaluateCondition_GT() {
        sampleRule.setConditionType("GT");
        sampleRule.setThreshold(100.0);

        boolean result = evaluateConditionDirectly(sampleRule, 150.0);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("条件判断 - LT小于阈值")
    void testEvaluateCondition_LT() {
        sampleRule.setConditionType("LT");
        sampleRule.setThreshold(100.0);

        boolean result = evaluateConditionDirectly(sampleRule, 50.0);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("条件判断 - EQ等于阈值")
    void testEvaluateCondition_EQ() {
        sampleRule.setConditionType("EQ");
        sampleRule.setThreshold(100.0);

        boolean result = evaluateConditionDirectly(sampleRule, 100.0);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("条件判断 - GTE大于等于")
    void testEvaluateCondition_GTE() {
        sampleRule.setConditionType("GTE");
        sampleRule.setThreshold(100.0);

        assertThat(evaluateConditionDirectly(sampleRule, 100.0)).isTrue();
        assertThat(evaluateConditionDirectly(sampleRule, 150.0)).isTrue();
        assertThat(evaluateConditionDirectly(sampleRule, 50.0)).isFalse();
    }

    @Test
    @DisplayName("条件判断 - LTE小于等于")
    void testEvaluateCondition_LTE() {
        sampleRule.setConditionType("LTE");
        sampleRule.setThreshold(100.0);

        assertThat(evaluateConditionDirectly(sampleRule, 100.0)).isTrue();
        assertThat(evaluateConditionDirectly(sampleRule, 50.0)).isTrue();
        assertThat(evaluateConditionDirectly(sampleRule, 150.0)).isFalse();
    }

    @Test
    @DisplayName("构建告警消息 - 使用模板")
    void testBuildAlertMessage_WithTemplate() {
        sampleRule.setMessageTemplate("指标 {metric} 超过阈值 {threshold}，当前值 {value}");

        String message = buildAlertMessageDirectly(sampleRule, "totalExecutions", 150.0);

        assertThat(message).contains("totalExecutions");
        assertThat(message).contains("150");
        assertThat(message).contains("100");
    }

    @Test
    @DisplayName("构建告警消息 - 默认模板")
    void testBuildAlertMessage_DefaultTemplate() {
        sampleRule.setMessageTemplate(null);

        String message = buildAlertMessageDirectly(sampleRule, "totalExecutions", 150.0);

        assertThat(message).contains("totalExecutions");
        assertThat(message).contains("150");
    }

    @Test
    @DisplayName("删除告警规则 - 逻辑删除")
    void testDeleteRule() {
        when(alertRuleMapper.selectById(1L)).thenReturn(sampleRule);

        alertRuleService.deleteRule(1L);

        verify(alertRuleMapper).updateById(sampleRule);
        assertThat(sampleRule.getDeleted()).isTrue();
    }

    @Test
    @DisplayName("检查告警 - 触发钉钉通知")
    void testCheckAlerts_TriggerDingTalk() {
        sampleRule.setNotifyChannels("DINGTALK");
        sampleRule.setNotifyTarget("https://oapi.dingtalk.com/robot/send?access_token=xxx");
        sampleRule.setConsecutiveTriggers(1);

        when(alertRuleMapper.selectList(any())).thenReturn(List.of(sampleRule));
        when(alertRuleMapper.selectById(1L)).thenReturn(sampleRule);

        List<AlertRule> triggered = alertRuleService.checkAlerts("totalExecutions", new BigDecimal("150"));

        assertThat(triggered).hasSize(1);
        verify(notificationService).sendDingTalkNotification(any(), any());
    }

    // Helper methods to test private methods indirectly
    private boolean evaluateConditionDirectly(AlertRule rule, double value) {
        double threshold = rule.getThreshold();
        double actual = value;

        return switch (rule.getConditionType()) {
            case "GT" -> actual > threshold;
            case "LT" -> actual < threshold;
            case "EQ" -> Math.abs(actual - threshold) < 0.0001;
            case "GTE" -> actual >= threshold;
            case "LTE" -> actual <= threshold;
            default -> false;
        };
    }

    private String buildAlertMessageDirectly(AlertRule rule, String metricName, double value) {
        String template = rule.getMessageTemplate();
        if (template == null || template.isBlank()) {
            return String.format("%s 指标超过阈值: 当前值=%s, 阈值=%s",
                metricName, value, rule.getThreshold());
        }
        return template
            .replace("{metric}", metricName)
            .replace("{value}", String.valueOf(value))
            .replace("{threshold}", String.valueOf(rule.getThreshold()));
    }
}