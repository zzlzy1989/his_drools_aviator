package com.his.market.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 规则模板 DTO
 */
@Data
public class RuleTemplateDTO {

    private Long id;

    private String templateKey;

    private String name;

    private String category;

    private String tags;

    private String description;

    private String version;

    /** 模板内容: rules/formulas/flows */
    private TemplateContentDTO content;

    private String providerId;

    private String providerName;

    private String publishedBy;

    private String status;

    private Integer installCount;

    private String tenantId;

    private String createBy;

    private String createTime;

    private List<Map<String, Object>> rules;

    private List<Map<String, Object>> formulas;

    private List<Map<String, Object>> flows;

    /** 评分汇总信息 */
    private Map<String, Object> ratingSummary;

    /** 已订阅模板的安装信息 */
    private Map<String, Object> installInfo;

    @Data
    public static class TemplateContentDTO {
        private List<Map<String, Object>> rules;
        private List<Map<String, Object>> formulas;
        private List<Map<String, Object>> flows;
    }
}