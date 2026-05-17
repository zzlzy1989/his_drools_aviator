package com.his.rule.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * 创建规则流DTO
 */
@Data
public class CreateFlowDTO {

    @NotBlank(message = "规则流名称不能为空")
    @Size(max = 128, message = "规则流名称不超过128字符")
    private String flowName;

    @Size(max = 32, message = "分类不超过32字符")
    private String category;

    @Size(max = 500, message = "描述不超过500字符")
    private String description;

    /** 规则流定义 (节点和边) */
    private FlowDefinitionDTO flowDefinition;

    /** 租户ID */
    private String tenantId;

    @Data
    public static class FlowDefinitionDTO {
        private List<NodeDTO> nodes;
        private List<EdgeDTO> edges;
    }

    @Data
    public static class NodeDTO {
        private String nodeId;
        private String type;        // start/end/condition/action/formula/subflow
        private String label;
        private String expression;  // 条件节点表达式
        private Map<String, String> branches; // 条件节点分支
        private String ruleKey;     // 动作节点引用的规则
        private String formulaKey;  // 公式节点引用的公式
        private String subFlowId;   // 子流程节点引用的子流程
        private Integer timeout;   // 超时时间(ms)
        private String resultField; // 公式/规则结果写入的字段名
        private Double x;          // x坐标
        private Double y;          // y坐标
        private Map<String, Object> params;  // 其他参数
    }

    @Data
    public static class EdgeDTO {
        private String source;
        private String target;
        private String label;
    }
}
