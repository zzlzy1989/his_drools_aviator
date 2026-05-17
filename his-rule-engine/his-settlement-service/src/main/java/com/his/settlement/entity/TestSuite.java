package com.his.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试套件实体
 */
@Data
@TableName("test_suite")
public class TestSuite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String suiteName;

    private String description;

    private String category;

    /** 包含的数据集IDs，逗号分隔 */
    private String dataSetIds;

    /** 执行模式: SEQUENTIAL/CONCURRENT */
    private String executionMode;

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