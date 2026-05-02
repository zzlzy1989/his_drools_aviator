package com.his.rule.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则流历史版本实体
 */
@Data
@TableName("rule_flow_history")
public class RuleFlowHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则流ID */
    private Long flowId;

    /** 版本号 */
    private Integer version;

    /** 规则流定义快照 (JSON) */
    private String flowDefinition;

    /** 变更说明 */
    private String changeDesc;

    /** 变更人 */
    private String changeBy;

    /** 变更时间 */
    private LocalDateTime changeTime;
}