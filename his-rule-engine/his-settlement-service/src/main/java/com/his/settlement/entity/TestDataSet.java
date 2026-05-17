package com.his.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测试数据集实体
 */
@Data
@TableName("test_data_set")
public class TestDataSet {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dataSetName;

    private String description;

    private String category;

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