package com.his.settlement.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 测试数据集 DTO
 */
@Data
public class TestDataSetDTO {

    private Long id;

    private String dataSetName;

    private String description;

    private String category;

    private List<TestCaseDTO> testCases;

    @Data
    public static class TestCaseDTO {
        private Long id;
        private String caseId;
        private String caseName;
        private Map<String, Object> fact;
        private Map<String, Object> expected;
        private Integer status;
        private String lastResult;
    }
}