package com.his.settlement.dto;

import lombok.Data;
import java.util.List;

/**
 * 测试套件 DTO
 */
@Data
public class TestSuiteDTO {

    private Long id;

    private String suiteName;

    private String description;

    private String category;

    /** 数据集ID列表 */
    private List<Long> dataSetIds;

    /** 执行模式 */
    private String executionMode;

    /** 关联的数据集信息 */
    private List<TestDataSetDTO> dataSets;

    /** 统计信息 */
    private Integer totalCases;

    private Integer passCount;

    private Integer failCount;
}