package com.his.rule.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新规则流DTO
 */
@Data
public class UpdateFlowDTO {

    @NotBlank(message = "规则流名称不能为空")
    @Size(max = 128, message = "规则流名称不超过128字符")
    private String flowName;

    @Size(max = 32, message = "分类不超过32字符")
    private String category;

    @Size(max = 500, message = "描述不超过500字符")
    private String description;

    /** 规则流定义 (节点和边) */
    private FlowDefinitionDTO flowDefinition;
}