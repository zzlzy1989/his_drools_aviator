package com.his.settlement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.web.context.TenantContext;
import com.his.settlement.dto.TestDataSetDTO;
import com.his.settlement.dto.TestSuiteDTO;
import com.his.settlement.entity.TestCase;
import com.his.settlement.entity.TestDataSet;
import com.his.settlement.entity.TestSuite;
import com.his.settlement.mapper.TestCaseMapper;
import com.his.settlement.mapper.TestDataSetMapper;
import com.his.settlement.mapper.TestSuiteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试套件服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestSuiteService {

    private final TestSuiteMapper suiteMapper;
    private final TestDataSetMapper dataSetMapper;
    private final TestCaseMapper testCaseMapper;
    private final SandboxService sandboxService;

    /**
     * 列表查询
     */
    public List<TestSuiteDTO> list(String category) {
        String tenantId = TenantContext.getTenantId("T001");
        LambdaQueryWrapper<TestSuite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestSuite::getTenantId, tenantId);
        wrapper.eq(TestSuite::getDeleted, 0);
        if (StringUtils.hasText(category)) {
            wrapper.eq(TestSuite::getCategory, category);
        }
        wrapper.orderByDesc(TestSuite::getCreateTime);

        List<TestSuite> suites = suiteMapper.selectList(wrapper);
        return suites.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * 获取详情
     */
    public TestSuiteDTO getById(Long id) {
        TestSuite suite = suiteMapper.selectById(id);
        if (suite == null || suite.getDeleted() == 1) {
            return null;
        }
        return toDTO(suite);
    }

    /**
     * 创建套件
     */
    @Transactional
    public TestSuiteDTO create(TestSuiteDTO dto) {
        String tenantId = TenantContext.getTenantId("T001");

        TestSuite suite = new TestSuite();
        suite.setSuiteName(dto.getSuiteName());
        suite.setDescription(dto.getDescription());
        suite.setCategory(dto.getCategory());
        suite.setExecutionMode(dto.getExecutionMode() != null ? dto.getExecutionMode() : "SEQUENTIAL");
        if (dto.getDataSetIds() != null && !dto.getDataSetIds().isEmpty()) {
            suite.setDataSetIds(String.join(",", dto.getDataSetIds().stream().map(String::valueOf).collect(Collectors.toList())));
        }
        suite.setTenantId(tenantId);
        suite.setCreateBy(tenantId);
        suite.setCreateTime(LocalDateTime.now());
        suiteMapper.insert(suite);

        log.info("创建测试套件: id={}, name={}", suite.getId(), suite.getSuiteName());
        return toDTO(suite);
    }

    /**
     * 更新套件
     */
    @Transactional
    public TestSuiteDTO update(Long id, TestSuiteDTO dto) {
        TestSuite suite = suiteMapper.selectById(id);
        if (suite == null || suite.getDeleted() == 1) {
            throw new RuntimeException("套件不存在");
        }

        suite.setSuiteName(dto.getSuiteName());
        suite.setDescription(dto.getDescription());
        suite.setCategory(dto.getCategory());
        suite.setExecutionMode(dto.getExecutionMode());
        if (dto.getDataSetIds() != null) {
            suite.setDataSetIds(String.join(",", dto.getDataSetIds().stream().map(String::valueOf).collect(Collectors.toList())));
        }
        suite.setUpdateBy(TenantContext.getTenantId());
        suite.setUpdateTime(LocalDateTime.now());
        suiteMapper.updateById(suite);

        log.info("更新测试套件: id={}", id);
        return toDTO(suite);
    }

    /**
     * 删除套件
     */
    @Transactional
    public void delete(Long id) {
        TestSuite suite = suiteMapper.selectById(id);
        if (suite == null) {
            throw new RuntimeException("套件不存在");
        }
        suite.setDeleted(1);
        suite.setUpdateTime(LocalDateTime.now());
        suiteMapper.updateById(suite);
        log.info("删除测试套件: id={}", id);
    }

    /**
     * 批量执行套件中的所有测试
     */
    public List<Map<String, Object>> executeSuite(Long suiteId) {
        TestSuite suite = suiteMapper.selectById(suiteId);
        if (suite == null || suite.getDeleted() == 1) {
            throw new RuntimeException("套件不存在");
        }

        List<Map<String, Object>> allResults = new ArrayList<>();

        if (!StringUtils.hasText(suite.getDataSetIds())) {
            return allResults;
        }

        List<Long> dataSetIds = Arrays.stream(suite.getDataSetIds().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());

        if ("CONCURRENT".equalsIgnoreCase(suite.getExecutionMode())) {
            // 并发执行
            List<Thread> threads = new ArrayList<>();
            for (Long dataSetId : dataSetIds) {
                Thread t = new Thread(() -> {
                    try {
                        List<Map<String, Object>> results = sandboxService.batchExecute(dataSetId);
                        synchronized (allResults) {
                            allResults.addAll(results);
                        }
                    } catch (Exception e) {
                        log.error("执行数据集失败: dataSetId={}", dataSetId, e);
                    }
                });
                threads.add(t);
                t.start();
            }
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            // 顺序执行
            for (Long dataSetId : dataSetIds) {
                try {
                    List<Map<String, Object>> results = sandboxService.batchExecute(dataSetId);
                    allResults.addAll(results);
                } catch (Exception e) {
                    log.error("执行数据集失败: dataSetId={}", dataSetId, e);
                }
            }
        }

        log.info("执行测试套件完成: suiteId={}, totalResults={}", suiteId, allResults.size());
        return allResults;
    }

    private TestSuiteDTO toDTO(TestSuite suite) {
        TestSuiteDTO dto = new TestSuiteDTO();
        dto.setId(suite.getId());
        dto.setSuiteName(suite.getSuiteName());
        dto.setDescription(suite.getDescription());
        dto.setCategory(suite.getCategory());
        dto.setExecutionMode(suite.getExecutionMode());

        // 解析数据集IDs
        if (StringUtils.hasText(suite.getDataSetIds())) {
            List<Long> dataSetIds = Arrays.stream(suite.getDataSetIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            dto.setDataSetIds(dataSetIds);

            // 获取数据集信息
            List<TestDataSetDTO> dataSets = new ArrayList<>();
            int totalCases = 0, passCount = 0, failCount = 0;
            for (Long dsId : dataSetIds) {
                TestDataSet ds = dataSetMapper.selectById(dsId);
                if (ds != null && ds.getDeleted() == 0) {
                    TestDataSetDTO dsDto = new TestDataSetDTO();
                    dsDto.setId(ds.getId());
                    dsDto.setDataSetName(ds.getDataSetName());
                    dsDto.setCategory(ds.getCategory());
                    dataSets.add(dsDto);

                    // 统计用例数
                    List<TestCase> cases = testCaseMapper.selectList(
                            new LambdaQueryWrapper<TestCase>()
                                    .eq(TestCase::getDataSetId, dsId)
                                    .eq(TestCase::getDeleted, 0)
                    );
                    totalCases += cases.size();
                    passCount += cases.stream().filter(c -> "PASS".equals(c.getLastResult())).count();
                    failCount += cases.stream().filter(c -> "FAILED".equals(c.getLastResult())).count();
                }
            }
            dto.setDataSets(dataSets);
            dto.setTotalCases(totalCases);
            dto.setPassCount(passCount);
            dto.setFailCount(failCount);
        }

        return dto;
    }
}