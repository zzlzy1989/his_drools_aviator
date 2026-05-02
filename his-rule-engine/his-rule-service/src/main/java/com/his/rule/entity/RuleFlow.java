package com.his.rule.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则流定义实体
 */
@Data
@TableName("rule_flow")
public class RuleFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则流唯一标识 */
    private String flowKey;

    /** 规则流名称 */
    private String flowName;

    /** 规则流图定义 (JSON) */
    private String flowDefinition;

    /** 版本号 */
    private Integer version;

    /** 状态: draft/active/inactive */
    private String status;

    /** 分类: SETTLEMENT/DRUG/QUALITY/DRG */
    private String category;

    /** 规则流描述 */
    private String description;

    /** 租户ID */
    private String tenantId;

    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}