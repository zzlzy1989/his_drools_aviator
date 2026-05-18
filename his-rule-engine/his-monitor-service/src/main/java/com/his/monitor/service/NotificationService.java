package com.his.monitor.service;

import com.his.monitor.dto.DingTalkMessage;
import com.his.monitor.entity.AlertRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 通知服务 - 钉钉Webhook发送
 */
@Slf4j
@Service
public class NotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送钉钉告警通知
     */
    public void sendDingTalkNotification(AlertRule rule, String message) {
        String webhook = rule.getNotifyTarget();
        if (webhook == null || webhook.isBlank()) {
            log.warn("[DINGTALK] webhook未配置，跳过通知: ruleName={}", rule.getRuleName());
            return;
        }

        DingTalkMessage dto = new DingTalkMessage();
        dto.setMsgtype("markdown");

        DingTalkMessage.MarkdownContent markdown = new DingTalkMessage.MarkdownContent();
        markdown.setTitle(rule.getRuleName());
        markdown.setText(buildAlertText(rule, message));
        dto.setMarkdown(markdown);

        try {
            restTemplate.postForEntity(webhook, dto, String.class);
            log.info("[DINGTALK] 告警通知发送成功: ruleName={}", rule.getRuleName());
        } catch (Exception e) {
            log.error("[DINGTALK] 告警通知发送失败: ruleName={}, error={}", rule.getRuleName(), e.getMessage());
        }
    }

    private String buildAlertText(AlertRule rule, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 告警通知\n\n");
        sb.append("**规则名称**: ").append(rule.getRuleName()).append("\n\n");
        sb.append("**告警级别**: ").append(rule.getLevel()).append("\n\n");
        sb.append("**消息内容**: ").append(message).append("\n\n");
        sb.append("**指标名称**: ").append(rule.getMetricName()).append("\n\n");
        sb.append("**阈值**: ").append(rule.getThreshold()).append("\n\n");
        return sb.toString();
    }
}