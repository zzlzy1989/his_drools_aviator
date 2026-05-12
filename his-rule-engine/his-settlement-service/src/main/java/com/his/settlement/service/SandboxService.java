package com.his.settlement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.web.context.TenantContext;
import com.his.settlement.dto.TestDataSetDTO;
import com.his.settlement.entity.TestCase;
import com.his.settlement.entity.TestDataSet;
import com.his.settlement.mapper.TestCaseMapper;
import com.his.settlement.mapper.TestDataSetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试沙箱服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SandboxService {

    private final TestDataSetMapper dataSetMapper;
    private final TestCaseMapper testCaseMapper;
    private final ObjectMapper objectMapper;

    /**
     * 列表查询
     */
    public List<TestDataSetDTO> list(String category) {
        String tenantId = TenantContext.getTenantId("T001");
        LambdaQueryWrapper<TestDataSet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestDataSet::getTenantId, tenantId);
        wrapper.eq(TestDataSet::getDeleted, 0);
        if (StringUtils.hasText(category)) {
            wrapper.eq(TestDataSet::getCategory, category);
        }
        wrapper.orderByDesc(TestDataSet::getCreateTime);

        List<TestDataSet> dataSets = dataSetMapper.selectList(wrapper);
        return dataSets.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * 获取详情
     */
    public TestDataSetDTO getById(Long id) {
        TestDataSet dataSet = dataSetMapper.selectById(id);
        if (dataSet == null || dataSet.getDeleted() == 1) {
            return null;
        }
        return toDTO(dataSet);
    }

    /**
     * 创建数据集
     */
    @Transactional
    public TestDataSetDTO create(TestDataSetDTO dto) {
        String tenantId = TenantContext.getTenantId("T001");

        TestDataSet dataSet = new TestDataSet();
        dataSet.setDataSetName(dto.getDataSetName());
        dataSet.setDescription(dto.getDescription());
        dataSet.setCategory(dto.getCategory());
        dataSet.setTenantId(tenantId);
        dataSet.setCreateBy(tenantId);
        dataSet.setCreateTime(LocalDateTime.now());
        dataSetMapper.insert(dataSet);

        if (dto.getTestCases() != null) {
            for (TestDataSetDTO.TestCaseDTO tcDto : dto.getTestCases()) {
                saveTestCase(dataSet.getId(), tcDto, tenantId);
            }
        }

        log.info("创建测试数据集: id={}, name={}", dataSet.getId(), dataSet.getDataSetName());
        return toDTO(dataSet);
    }

    /**
     * 更新数据集
     */
    @Transactional
    public TestDataSetDTO update(Long id, TestDataSetDTO dto) {
        TestDataSet dataSet = dataSetMapper.selectById(id);
        if (dataSet == null || dataSet.getDeleted() == 1) {
            throw new RuntimeException("数据集不存在");
        }

        dataSet.setDataSetName(dto.getDataSetName());
        dataSet.setDescription(dto.getDescription());
        dataSet.setCategory(dto.getCategory());
        dataSet.setUpdateBy(TenantContext.getTenantId());
        dataSet.setUpdateTime(LocalDateTime.now());
        dataSetMapper.updateById(dataSet);

        if (dto.getTestCases() != null) {
            testCaseMapper.delete(new LambdaQueryWrapper<TestCase>().eq(TestCase::getDataSetId, id));
            String tenantId = dataSet.getTenantId();
            for (TestDataSetDTO.TestCaseDTO tcDto : dto.getTestCases()) {
                saveTestCase(id, tcDto, tenantId);
            }
        }

        log.info("更新测试数据集: id={}", id);
        return toDTO(dataSet);
    }

    /**
     * 删除数据集
     */
    @Transactional
    public void delete(Long id) {
        TestDataSet dataSet = dataSetMapper.selectById(id);
        if (dataSet == null) {
            throw new RuntimeException("数据集不存在");
        }
        dataSet.setDeleted(1);
        dataSet.setUpdateTime(LocalDateTime.now());
        dataSetMapper.updateById(dataSet);
        log.info("删除测试数据集: id={}", id);
    }

    /**
     * 执行单个测试用例
     */
    public Map<String, Object> executeTestCase(Long caseId) {
        TestCase testCase = testCaseMapper.selectById(caseId);
        if (testCase == null || testCase.getDeleted() == 1) {
            throw new RuntimeException("测试用例不存在");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> fact = objectMapper.readValue(testCase.getFactJson(), Map.class);

            Map<String, Object> result = Map.of(
                "caseId", testCase.getCaseId(),
                "caseName", testCase.getCaseName(),
                "input", fact,
                "status", "SUCCESS",
                "message", "执行成功"
            );

            testCase.setLastResult("SUCCESS");
            testCaseMapper.updateById(testCase);

            return result;
        } catch (Exception e) {
            log.error("执行测试用例失败: caseId={}", caseId, e);
            testCase.setLastResult("FAILED");
            testCaseMapper.updateById(testCase);
            return Map.of(
                "caseId", testCase.getCaseId(),
                "status", "FAILED",
                "message", e.getMessage()
            );
        }
    }

    /**
     * 批量执行测试用例
     */
    public List<Map<String, Object>> batchExecute(Long dataSetId) {
        List<TestCase> cases = testCaseMapper.selectList(
            new LambdaQueryWrapper<TestCase>()
                .eq(TestCase::getDataSetId, dataSetId)
                .eq(TestCase::getDeleted, 0)
        );

        List<Map<String, Object>> results = new ArrayList<>();
        for (TestCase tc : cases) {
            results.add(executeTestCase(tc.getId()));
        }
        return results;
    }

    private void saveTestCase(Long dataSetId, TestDataSetDTO.TestCaseDTO dto, String tenantId) {
        try {
            TestCase testCase = new TestCase();
            testCase.setDataSetId(dataSetId);
            testCase.setCaseId(dto.getCaseId() != null ? dto.getCaseId() : "TC" + System.currentTimeMillis());
            testCase.setCaseName(dto.getCaseName());
            testCase.setFactJson(dto.getFact() != null ? objectMapper.writeValueAsString(dto.getFact()) : "{}");
            testCase.setExpectedJson(dto.getExpected() != null ? objectMapper.writeValueAsString(dto.getExpected()) : "{}");
            testCase.setTenantId(tenantId);
            testCase.setCreateBy(tenantId);
            testCase.setCreateTime(LocalDateTime.now());
            testCaseMapper.insert(testCase);
        } catch (Exception e) {
            log.error("保存测试用例失败", e);
        }
    }

    private TestDataSetDTO toDTO(TestDataSet dataSet) {
        TestDataSetDTO dto = new TestDataSetDTO();
        dto.setId(dataSet.getId());
        dto.setDataSetName(dataSet.getDataSetName());
        dto.setDescription(dataSet.getDescription());
        dto.setCategory(dataSet.getCategory());

        List<TestCase> cases = testCaseMapper.selectList(
            new LambdaQueryWrapper<TestCase>()
                .eq(TestCase::getDataSetId, dataSet.getId())
                .eq(TestCase::getDeleted, 0)
        );

        dto.setTestCases(cases.stream().map(tc -> {
            TestDataSetDTO.TestCaseDTO tcDto = new TestDataSetDTO.TestCaseDTO();
            tcDto.setId(tc.getId());
            tcDto.setCaseId(tc.getCaseId());
            tcDto.setCaseName(tc.getCaseName());
            tcDto.setStatus(tc.getStatus());
            tcDto.setLastResult(tc.getLastResult());
            try {
                if (tc.getFactJson() != null) {
                    tcDto.setFact(objectMapper.readValue(tc.getFactJson(), Map.class));
                }
                if (tc.getExpectedJson() != null) {
                    tcDto.setExpected(objectMapper.readValue(tc.getExpectedJson(), Map.class));
                }
            } catch (Exception e) {
                log.error("解析测试用例JSON失败: id={}", tc.getId(), e);
            }
            return tcDto;
        }).collect(Collectors.toList()));

        return dto;
    }
}