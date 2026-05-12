package com.his.monitor.controller;

import com.his.monitor.dto.MonitorMetricsVO;
import com.his.monitor.service.MonitorService;
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