package com.his.monitor.controller;

import com.his.monitor.dto.AlertRuleDTO;
import com.his.monitor.dto.MetricHistoryQueryDTO;
import com.his.monitor.dto.MonitorMetricsVO;
import com.his.monitor.entity.AlertRule;
import com.his.monitor.service.AlertRuleService;
import com.his.monitor.service.MetricHistoryService;
import com.his.monitor.service.MonitorService;
import com.his.monitor.websocket.MonitorWebSocketHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 监控 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;
    private final MetricHistoryService metricHistoryService;
    private final AlertRuleService alertRuleService;
    private final MonitorWebSocketHandler webSocketHandler;

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        return toResult(monitorService.getMetrics());
    }

    @GetMapping("/alerts")
    public Map<String, Object> getAlerts() {
        return toResult(monitorService.getMetrics().getRecentAlerts());
    }

    @GetMapping("/top-rules")
    public Map<String, Object> getTopRules() {
        return toResult(monitorService.getMetrics().getTopRules());
    }

    @PostMapping("/record")
    public Map<String, Object> recordExecution(
            @RequestParam boolean success,
            @RequestParam long durationMs,
            @RequestParam(required = false) String ruleKey) {
        monitorService.recordExecution(success, durationMs);
        if (ruleKey != null) {
            monitorService.recordRuleHit(ruleKey);
        }
        return toResult(null);
    }

    @PostMapping("/alert")
    public Map<String, Object> recordAlert(
            @RequestParam String alertType,
            @RequestParam String message,
            @RequestParam(defaultValue = "WARN") String level) {
        monitorService.recordAlert(alertType, message, level);
        return toResult(null);
    }

    @GetMapping("/history")
    public Map<String, Object> queryHistory(MetricHistoryQueryDTO query) {
        return toResult(metricHistoryService.queryHistory(query));
    }

    @GetMapping("/trend")
    public Map<String, Object> getTrend(
            @RequestParam String metricName,
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(required = false) String tag) {
        return toResult(metricHistoryService.getTrend(metricName, hours, tag));
    }

    @GetMapping("/heatmap")
    public Map<String, Object> getRuleHeatmap(
            @RequestParam(defaultValue = "7") int days) {
        return toResult(metricHistoryService.getRuleHeatmapData(days));
    }

    // ===== 告警规则管理 =====
    @GetMapping("/alert-rules")
    public Map<String, Object> listAlertRules() {
        return toResult(alertRuleService.listRules());
    }

    @GetMapping("/alert-rules/{id}")
    public Map<String, Object> getAlertRule(@PathVariable Long id) {
        return toResult(alertRuleService.getRule(id));
    }

    @PostMapping("/alert-rules")
    public Map<String, Object> createAlertRule(@RequestBody @Valid AlertRuleDTO dto) {
        alertRuleService.createRule(dto);
        return toResult(null);
    }

    @PutMapping("/alert-rules/{id}")
    public Map<String, Object> updateAlertRule(@PathVariable Long id, @RequestBody @Valid AlertRuleDTO dto) {
        alertRuleService.updateRule(id, dto);
        return toResult(null);
    }

    @DeleteMapping("/alert-rules/{id}")
    public Map<String, Object> deleteAlertRule(@PathVariable Long id) {
        alertRuleService.deleteRule(id);
        return toResult(null);
    }

    // ===== WebSocket 状态 =====
    @GetMapping("/ws/status")
    public Map<String, Object> getWebSocketStatus() {
        return toResult(Map.of(
            "connectionCount", webSocketHandler.getConnectionCount(),
            "wsPath", "/ws/monitor"
        ));
    }

    private Map<String, Object> toResult(Object data) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("code", "0");
        result.put("data", data);
        result.put("message", "操作成功");
        result.put("timestamp", System.currentTimeMillis());
        result.put("success", true);
        return result;
    }
}