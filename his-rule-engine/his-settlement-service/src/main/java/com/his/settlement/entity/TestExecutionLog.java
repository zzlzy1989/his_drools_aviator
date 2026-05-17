package com.his.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试执行历史记录
 */
@Data
@TableName("test_execution_log")
public class TestExecutionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long testCaseId;

    private Long dataSetId;

    private String caseName;

    private String inputJson;

    private String expectedJson;

    private String actualJson;

    private String diffJson;

    private String status;

    private Integer elapsedMs;

    private String errorMessage;

    private String executedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime executeTime;

    private String tenantId;

    @TableLogic
    private Integer deleted;
}