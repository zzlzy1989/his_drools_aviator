package com.his.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 规则模板实体
 */
@Data
@TableName("rule_template")
public class RuleTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String templateKey;

    private String name;

    private String category;

    private String tags;

    private String description;

    private String version;

    /** JSON: {rules: [], formulas: [], flows: []} */
    private String content;

    private String providerId;

    private String providerName;

    private String publishedBy;

    /** 状态: draft/published/inactive */
    private String status;

    private Integer installCount;

    private String tenantId;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}