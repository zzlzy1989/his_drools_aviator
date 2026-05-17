package com.his.monitor.websocket;

import com.his.monitor.service.MonitorService.MetricsUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * WebSocket 指标推送监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsUpdateListener {

    private final MonitorWebSocketHandler webSocketHandler;

    @Async
    @EventListener
    public void onMetricsUpdate(MetricsUpdateEvent event) {
        if (webSocketHandler.getConnectionCount() > 0) {
            try {
                webSocketHandler.broadcastMetrics(event.metrics());
                log.debug("WebSocket 推送指标: 连接数={}", webSocketHandler.getConnectionCount());
            } catch (Exception e) {
                log.error("WebSocket 推送指标失败", e);
            }
        }
    }
}