package com.his.rule.dto;

import lombok.Data;

import java.util.List;

/**
 * 规则流版本对比结果VO
 */
@Data
public class FlowCompareVO {
    private Long flowId;
    private Integer fromVersion;
    private Integer toVersion;
    private boolean hasDiff;
    private List<NodeDiff> nodeDiffs;
    private List<EdgeDiff> edgeDiffs;

    @Data
    public static class NodeDiff {
        private String nodeId;
        private String nodeLabel;
        private String changeType; // ADDED / REMOVED / MODIFIED
        private String fromContent;
        private String toContent;
    }

    @Data
    public static class EdgeDiff {
        private String edgeId;
        private String changeType;
        private String fromContent;
        private String toContent;
    }
}