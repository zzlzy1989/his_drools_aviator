package com.his.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模板收藏实体
 */
@Data
@TableName("template_favorite")
public class TemplateFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;

    /** 收藏用户ID */
    private String userId;

    /** 租户ID */
    private String tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}