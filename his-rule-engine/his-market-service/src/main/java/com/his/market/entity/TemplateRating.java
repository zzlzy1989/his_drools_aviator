package com.his.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模板评分与评论实体
 */
@Data
@TableName("template_rating")
public class TemplateRating {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;

    /** 评分用户ID */
    private String userId;

    /** 租户ID */
    private String tenantId;

    /** 评分 1-5星 */
    private Integer rating;

    /** 评论内容 */
    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}