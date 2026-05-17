package com.his.monitor.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.monitor.dto.MonitorMetricsVO;
import com.his.monitor.service.MonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 监控 WebSocket 处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorWebSocketHandler extends TextWebSocketHandler {

    private final MonitorService monitorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 已连接的会话
    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    // 订阅主题 -> 对应会话
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> subscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket 连接建立: sessionId={}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        // 清理会话的所有订阅
        subscriptions.forEach((topic, topicSessions) -> topicSessions.remove(session));
        log.info("WebSocket 连接关闭: sessionId={}, status={}", session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            var json = objectMapper.readTree(payload);

            String action = json.has("action") ? json.get("action").asText() : "subscribe";
            String topic = json.has("topic") ? json.get("topic").asText() : "metrics";

            switch (action) {
                case "subscribe" -> handleSubscribe(session, topic);
                case "unsubscribe" -> handleUnsubscribe(session, topic);
                case "ping" -> handlePing(session);
                default -> log.warn("未知 WebSocket 动作: {}", action);
            }
        } catch (Exception e) {
            log.error("处理 WebSocket 消息失败", e);
        }
    }

    private void handleSubscribe(WebSocketSession session, String topic) {
        subscriptions.computeIfAbsent(topic, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("WebSocket 订阅: sessionId={}, topic={}", session.getId(), topic);

        // 立即发送当前数据
        if ("metrics".equals(topic)) {
            sendMetrics(session);
        }
    }

    private void handleUnsubscribe(WebSocketSession session, String topic) {
        var topicSessions = subscriptions.get(topic);
        if (topicSessions != null) {
            topicSessions.remove(session);
        }
        log.info("WebSocket 取消订阅: sessionId={}, topic={}", session.getId(), topic);
    }

    private void handlePing(WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
        } catch (IOException e) {
            log.error("发送 pong 失败", e);
        }
    }

    /**
     * 推送指标数据到所有订阅的客户端
     */
    public void broadcastMetrics(MonitorMetricsVO metrics) {
        String json;
        try {
            json = objectMapper.writeValueAsString(new MetricsMessage("metrics", metrics));
        } catch (Exception e) {
            log.error("序列化指标数据失败", e);
            return;
        }

        TextMessage message = new TextMessage(json);
        var topicSessions = subscriptions.get("metrics");

        if (topicSessions != null) {
            for (WebSocketSession session : topicSessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.error("推送 WebSocket 消息失败: sessionId={}", session.getId(), e);
                    }
                }
            }
        }
    }

    private void sendMetrics(WebSocketSession session) {
        if (!session.isOpen()) return;

        try {
            MonitorMetricsVO metrics = monitorService.getMetrics();
            String json = objectMapper.writeValueAsString(new MetricsMessage("metrics", metrics));
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error("发送初始指标失败: sessionId={}", session.getId(), e);
        }
    }

    /**
     * 获取当前连接数
     */
    public int getConnectionCount() {
        return sessions.size();
    }

    /**
     * 指标消息
     */
    public record MetricsMessage(String type, MonitorMetricsVO data) {}
}