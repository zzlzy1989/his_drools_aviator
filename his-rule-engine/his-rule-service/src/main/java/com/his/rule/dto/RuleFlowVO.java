package com.his.rule.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 规则流VO
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleFlowVO {

    private Long id;
    private String flowKey;
    private String flowName;
    private String category;
    private String description;
    private String status;
    private Integer version;
    private FlowDefinitionDTO flowDefinition;
    private String tenantId;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    @Data
    public static class FlowDefinitionDTO {
        private List<NodeDTO> nodes;
        private List<EdgeDTO> edges;
    }

    @Data
    public static class NodeDTO {
        private String nodeId;
        private String type;
        private String label;
        private String expression;
        private Map<String, String> branches;
        private String ruleKey;
        private String formulaKey;
        private String subFlowId;
        private Integer timeout;
        private Map<String, Object> position;
        private Map<String, Object> params;
    }

    @Data
    public static class EdgeDTO {
        private String source;
        private String target;
        private String label;
    }
}