package com.his.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模板安装记录实体
 */
@Data
@TableName("template_install")
public class TemplateInstall {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;

    private String tenantId;

    private String installedBy;

    private String installedVersion;

    private LocalDateTime installTime;

    private String lastSyncVersion;

    private LocalDateTime lastSyncTime;

    /** 可升级到的最新版本 */
    private String availableVersion;

    /** 最后检查版本更新时间 */
    private LocalDateTime lastCheckTime;

    /** 状态: active/unsubscribed */
    private String status;

    @TableLogic
    private Integer deleted;
}