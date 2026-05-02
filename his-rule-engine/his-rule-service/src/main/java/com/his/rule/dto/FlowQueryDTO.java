package com.his.rule.dto;

import lombok.Data;

/**
 * 规则流查询DTO
 */
@Data
public class FlowQueryDTO {
    private String flowName;
    private String category;
    private String status;
    private String tenantId;
}