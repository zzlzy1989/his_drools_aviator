package com.his.rule.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则流版本历史VO
 */
@Data
public class FlowVersionVO {
    private Long id;
    private Long flowId;
    private Integer version;
    private String flowDefinition;
    private String changeDesc;
    private String changeBy;
    private LocalDateTime changeTime;
}