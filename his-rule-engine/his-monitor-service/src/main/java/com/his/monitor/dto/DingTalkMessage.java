package com.his.monitor.dto;

import lombok.Data;

/**
 * 钉钉消息格式 DTO
 */
@Data
public class DingTalkMessage {
    private String msgtype;
    private MarkdownContent markdown;

    public DingTalkMessage() {
        this.markdown = new MarkdownContent();
    }

    @Data
    public static class MarkdownContent {
        private String title;
        private String text;
    }
}