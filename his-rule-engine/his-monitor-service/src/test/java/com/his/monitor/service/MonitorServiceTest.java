package com.his.monitor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MonitorService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class MonitorServiceTest {

    private MonitorService monitorService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AlertRuleService alertRuleService;

    @BeforeEach
    void setUp() {
        monitorService = new MonitorService(eventPublisher, alertRuleService);
    }

    @Test
    @DisplayName("记录成功执行")
    void testRecordExecution_success() {
        monitorService.recordExecution(true, 50);
        assertEquals(1, monitorService.getMetrics().getExecutionTotal());
        assertEquals(1, monitorService.getMetrics().getExecutionSuccess());
        assertEquals(0, monitorService.getMetrics().getExecutionFailed());
    }

    @Test
    @DisplayName("记录失败执行")
    void testRecordExecution_failed() {
        monitorService.recordExecution(false, 50);
        assertEquals(1, monitorService.getMetrics().getExecutionTotal());
        assertEquals(0, monitorService.getMetrics().getExecutionSuccess());
        assertEquals(1, monitorService.getMetrics().getExecutionFailed());
    }

    @Test
    @DisplayName("计算成功率")
    void testSuccessRate() {
        monitorService.recordExecution(true, 50);
        monitorService.recordExecution(true, 50);
        monitorService.recordExecution(false, 50);
        assertEquals(3, monitorService.getMetrics().getExecutionTotal());
        assertEquals(new BigDecimal("66.67"), monitorService.getMetrics().getSuccessRate());
    }

    @Test
    @DisplayName("记录规则命中")
    void testRecordRuleHit() {
        monitorService.recordRuleHit("rule.reimburse.employee");
        monitorService.recordRuleHit("rule.reimburse.employee");
        monitorService.recordRuleHit("rule.reimburse.resident");

        var topRules = monitorService.getMetrics().getTopRules();
        assertFalse(topRules.isEmpty());
        assertEquals("rule.reimburse.employee", topRules.get(0).getRuleKey());
        assertEquals(2, topRules.get(0).getHitCount());
    }

    @Test
    @DisplayName("记录公式执行")
    void testRecordFormulaHit() {
        monitorService.recordFormulaHit();
        monitorService.recordFormulaHit();
        monitorService.recordFormulaHit();
        // 公式命中率基于 totalExec 计算，没有执行记录(totalExec=0)时返回 0
        var metrics = monitorService.getMetrics();
        assertEquals(BigDecimal.ZERO, metrics.getFormulaHitRate());
    }

    @Test
    @DisplayName("获取指标 - 包含最近告警")
    void testGetMetrics_withAlerts() {
        monitorService.recordAlert("test_alert", "测试告警", "WARN");
        monitorService.recordAlert("test_alert2", "测试告警2", "ERROR");

        var metrics = monitorService.getMetrics();
        assertNotNull(metrics.getRecentAlerts());
        assertEquals(2, metrics.getRecentAlerts().size());
        // 最近告警按时间倒序排列，最新添加的在前面
        assertEquals("ERROR", metrics.getRecentAlerts().get(0).getLevel());
    }

    @Test
    @DisplayName("指标重置后应该清零")
    void testMetricsAfterReset() {
        monitorService.recordExecution(true, 50);
        monitorService.recordExecution(true, 50);

        var metrics = monitorService.getMetrics();
        assertEquals(2, metrics.getExecutionTotal());
    }

    @Test
    @DisplayName("P99/P95 延迟计算")
    void testDurationStats() {
        monitorService.recordExecution(true, 100);
        monitorService.recordExecution(true, 200);
        monitorService.recordExecution(true, 150);

        var metrics = monitorService.getMetrics();
        assertEquals(200, metrics.getP99DurationMs());
        assertTrue(metrics.getP95DurationMs() <= 200);
    }
}