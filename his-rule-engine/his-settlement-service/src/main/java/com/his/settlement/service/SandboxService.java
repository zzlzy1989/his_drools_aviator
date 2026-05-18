package com.his.settlement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.SettlementFact;
import com.his.common.SkillContext;
import com.his.common.SkillResult;
import com.his.common.ResultLevel;
import com.his.common.web.context.TenantContext;
import com.his.common.web.exception.BusinessException;
import com.his.common.web.result.ErrorCode;
import com.his.settlement.dto.TestDataSetDTO;
import com.his.settlement.entity.TestCase;
import com.his.settlement.entity.TestDataSet;
import com.his.settlement.entity.TestExecutionLog;
import com.his.settlement.mapper.TestCaseMapper;
import com.his.settlement.mapper.TestDataSetMapper;
import com.his.settlement.mapper.TestExecutionLogMapper;
import com.his.settlement.pipeline.SkillPipelineExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final TestExecutionLogMapper executionLogMapper;
    private final ObjectMapper objectMapper;
    private final SkillPipelineExecutor skillPipelineExecutor;
    private final FormulaLoaderService formulaLoaderService;

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
            throw new BusinessException(ErrorCode.SANDBOX_DATASET_NOT_FOUND, id);
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
            throw new BusinessException(ErrorCode.SANDBOX_DATASET_NOT_FOUND, id);
        }
        dataSet.setDeleted(1);
        dataSet.setUpdateTime(LocalDateTime.now());
        dataSetMapper.updateById(dataSet);
        log.info("删除测试数据集: id={}", id);
    }

    /**
     * 执行单个测试用例（真实规则执行）
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> executeTestCase(Long caseId) {
        TestCase testCase = testCaseMapper.selectById(caseId);
        if (testCase == null || testCase.getDeleted() == 1) {
            throw new RuntimeException("测试用例不存在");
        }

        long startTime = System.currentTimeMillis();
        Map<String, Object> execResult = new LinkedHashMap<>();
        execResult.put("caseId", testCase.getCaseId());
        execResult.put("caseName", testCase.getCaseName());
        Map<String, Object> factMap = null;
        long elapsed = 0;

        try {
            // 1. 解析输入 Fact
            factMap = objectMapper.readValue(testCase.getFactJson(), Map.class);
            execResult.put("input", factMap);

            // 2. 构建 SettlementFact 对象
            SettlementFact fact = buildSettlementFact(factMap, testCase.getTenantId());

            // 3. 构建 SkillContext 并执行 Skill 管道
            SkillContext<SettlementFact> context = new SkillContext<>();
            context.setTenantId(testCase.getTenantId());
            context.setEventType("EVENT_FEE_SETTLE");
            context.setPayload(fact);

            skillPipelineExecutor.execute(context);

            // 4. 执行公式计算（报销金额）
            BigDecimal reimburseAmount = BigDecimal.ZERO;
            if (fact.getTotalFee() != null && fact.getDeductible() != null && fact.getRatio() != null) {
                if (fact.getTotalFee().compareTo(fact.getDeductible()) > 0) {
                    reimburseAmount = formulaLoaderService.executeReimburseFormula(
                            testCase.getTenantId(),
                            fact.getPatientType(),
                            fact.getTotalFee(),
                            fact.getDeductible(),
                            fact.getRatio()
                    );
                }
            }
            fact.setFinalAmount(reimburseAmount);

            // 5. 收集执行结果
            Map<String, Object> actualOutput = buildOutputMap(fact, context);

            // 6. 解析期望输出
            Map<String, Object> expected = new LinkedHashMap<>();
            if (StringUtils.hasText(testCase.getExpectedJson())) {
                expected = objectMapper.readValue(testCase.getExpectedJson(), Map.class);
            }

            // 7. 对比实际与期望
            List<Map<String, String>> diffs = compareResults(expected, actualOutput);

            elapsed = System.currentTimeMillis() - startTime;
            execResult.put("actual", actualOutput);
            execResult.put("expected", expected);
            execResult.put("diff", diffs);
            execResult.put("elapsed", elapsed + "ms");

            if (diffs.isEmpty()) {
                execResult.put("status", "PASS");
                execResult.put("message", "测试通过");
                testCase.setLastResult("PASS");
            } else {
                execResult.put("status", "FAILED");
                execResult.put("message", "存在 " + diffs.size() + " 项不匹配");
                testCase.setLastResult("FAILED");
            }
            testCase.setLastExecutionTime(LocalDateTime.now());
            testCaseMapper.updateById(testCase);

            // 记录执行历史
            saveExecutionLog(testCase, factMap, expected, actualOutput, diffs, execResult.get("status").toString(), (int) elapsed);

            log.info("测试用例执行完成: caseId={}, status={}, elapsed={}ms, diffs={}",
                    testCase.getCaseId(), execResult.get("status"), elapsed, diffs.size());

            return execResult;

        } catch (Exception e) {
            log.error("执行测试用例失败: caseId={}", caseId, e);
            testCase.setLastResult("ERROR");
            testCase.setLastExecutionTime(LocalDateTime.now());
            testCaseMapper.updateById(testCase);

            execResult.put("status", "ERROR");
            execResult.put("message", e.getMessage());

            // 记录异常执行历史
            saveExecutionLog(testCase, factMap, null, null, null, "ERROR", (int) elapsed);

            return execResult;
        }
    }

    /**
     * 保存执行历史
     */
    private void saveExecutionLog(TestCase testCase, Map<String, Object> input,
                                   Map<String, Object> expected, Map<String, Object> actual,
                                   List<Map<String, String>> diffs, String status, int elapsedMs) {
        try {
            TestExecutionLog log = new TestExecutionLog();
            log.setTestCaseId(testCase.getId());
            log.setDataSetId(testCase.getDataSetId());
            log.setCaseName(testCase.getCaseName());
            log.setInputJson(objectMapper.writeValueAsString(input));
            log.setExpectedJson(expected != null ? objectMapper.writeValueAsString(expected) : null);
            log.setActualJson(actual != null ? objectMapper.writeValueAsString(actual) : null);
            log.setDiffJson(diffs != null ? objectMapper.writeValueAsString(diffs) : null);
            log.setStatus(status);
            log.setElapsedMs(elapsedMs);
            log.setExecutedBy(testCase.getTenantId());
            log.setExecuteTime(LocalDateTime.now());
            log.setTenantId(testCase.getTenantId());
            executionLogMapper.insert(log);
        } catch (Exception e) {
            log.error("保存执行历史失败", e);
        }
    }

    /**
     * 分页查询执行历史
     */
    public IPage<TestExecutionLog> listExecutionLogs(Integer page, Integer pageSize,
                                                      String status, String startDate, String endDate) {
        String tenantId = TenantContext.getTenantId("T001");
        Page<TestExecutionLog> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<TestExecutionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestExecutionLog::getTenantId, tenantId);
        wrapper.eq(TestExecutionLog::getDeleted, 0);

        if (StringUtils.hasText(status)) {
            wrapper.eq(TestExecutionLog::getStatus, status);
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(TestExecutionLog::getExecuteTime, LocalDateTime.parse(startDate + " 00:00:00", df));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(TestExecutionLog::getExecuteTime, LocalDateTime.parse(endDate + " 23:59:59", df));
        }

        wrapper.orderByDesc(TestExecutionLog::getExecuteTime);
        return executionLogMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 获取执行历史详情
     */
    public TestExecutionLog getExecutionLog(Long id) {
        TestExecutionLog log = executionLogMapper.selectById(id);
        if (log == null || log.getDeleted() == 1) {
            throw new RuntimeException("执行记录不存在");
        }
        return log;
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

    /**
     * 构建 SettlementFact 对象
     */
    @SuppressWarnings("unchecked")
    private SettlementFact buildSettlementFact(Map<String, Object> factMap, String tenantId) {
        SettlementFact fact = new SettlementFact();
        fact.setTenantId(tenantId);
        fact.setPatientId((String) factMap.get("patientId"));
        fact.setPatientName((String) factMap.get("patientName"));
        fact.setPatientType((String) factMap.get("patientType"));
        fact.setInsuranceType((String) factMap.get("insuranceType"));
        fact.setHospitalLevel((String) factMap.get("hospitalLevel"));
        fact.setSettlementId((String) factMap.get("settlementId"));
        fact.setDiagnosisCode((String) factMap.get("diagnosisCode"));

        // BigDecimal 字段处理
        Object totalFee = factMap.get("totalFee");
        if (totalFee instanceof Number) {
            fact.setTotalFee(BigDecimal.valueOf(((Number) totalFee).doubleValue()));
        }

        Object deductible = factMap.get("deductible");
        if (deductible instanceof Number) {
            fact.setDeductible(BigDecimal.valueOf(((Number) deductible).doubleValue()));
        }

        Object ratio = factMap.get("ratio");
        if (ratio instanceof Number) {
            fact.setRatio(BigDecimal.valueOf(((Number) ratio).doubleValue()));
        }

        Object finalAmount = factMap.get("finalAmount");
        if (finalAmount instanceof Number) {
            fact.setFinalAmount(BigDecimal.valueOf(((Number) finalAmount).doubleValue()));
        }

        return fact;
    }

    /**
     * 构建输出结果 Map
     */
    private Map<String, Object> buildOutputMap(SettlementFact fact, SkillContext<SettlementFact> context) {
        Map<String, Object> output = new LinkedHashMap<>();

        // 基础字段
        if (fact.getDeductible() != null) {
            output.put("deductible", fact.getDeductible());
        }
        if (fact.getRatio() != null) {
            output.put("ratio", fact.getRatio());
        }
        if (fact.getFinalAmount() != null) {
            output.put("finalAmount", fact.getFinalAmount());
        }

        // 报销金额（从 fact.results 收集）
        BigDecimal reimburseAmount = fact.getFinalAmount();
        if (reimburseAmount == null && fact.getTotalFee() != null && fact.getDeductible() != null
                && fact.getTotalFee().compareTo(fact.getDeductible()) > 0) {
            // 兜底：基于比例计算
            if (fact.getRatio() != null) {
                reimburseAmount = fact.getTotalFee().subtract(fact.getDeductible())
                        .multiply(fact.getRatio()).setScale(2, java.math.RoundingMode.HALF_UP);
            }
        }
        output.put("reimburseAmount", reimburseAmount != null ? reimburseAmount : BigDecimal.ZERO);

        // 自付金额
        if (fact.getTotalFee() != null && reimburseAmount != null) {
            output.put("selfPayAmount", fact.getTotalFee().subtract(reimburseAmount));
        }

        // Skill 结果汇总
        List<Map<String, Object>> skillResults = new ArrayList<>();
        for (SkillResult sr : context.getResults()) {
            Map<String, Object> srMap = new LinkedHashMap<>();
            srMap.put("level", sr.getLevel().name());
            srMap.put("source", sr.getSource());
            srMap.put("message", sr.getMessage());
            skillResults.add(srMap);
        }
        output.put("skillResults", skillResults);

        // 整体结果等级
        output.put("resultLevel", context.hasBlock() ? "BLOCK" :
                context.getResults().stream().anyMatch(r -> r.getLevel() == ResultLevel.WARN) ? "WARN" : "PASS");

        return output;
    }

    /**
     * 对比实际结果与期望结果（智能数值比较）
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, String>> compareResults(Map<String, Object> expected, Map<String, Object> actual) {
        List<Map<String, String>> diffs = new ArrayList<>();

        for (String key : expected.keySet()) {
            Object expVal = expected.get(key);
            Object actVal = actual.get(key);

            if (!isEqual(expVal, actVal)) {
                diffs.add(Map.of(
                        "field", key,
                        "expected", formatValue(expVal),
                        "actual", formatValue(actVal)
                ));
            }
        }

        return diffs;
    }

    /**
     * 判断两个值是否相等（支持数值类型自动转换）
     */
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        // 数值类型比较：BigDecimal / Number / String 数字
        if (isNumeric(a) && isNumeric(b)) {
            BigDecimal ba = toBigDecimal(a);
            BigDecimal bb = toBigDecimal(b);
            // 允许 0.001 的误差（处理浮点精度问题）
            return ba.subtract(bb).abs().compareTo(new BigDecimal("0.001")) < 0;
        }

        return Objects.equals(a, b);
    }

    private boolean isNumeric(Object v) {
        return v instanceof Number || v instanceof String;
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number num) return BigDecimal.valueOf(num.doubleValue());
        if (v instanceof String s) {
            try { return new BigDecimal(s); } catch (Exception e) { return BigDecimal.ZERO; }
        }
        return BigDecimal.ZERO;
    }

    private String formatValue(Object v) {
        if (v == null) return "null";
        if (v instanceof BigDecimal bd) return bd.stripTrailingZeros().toPlainString();
        if (v instanceof Number num) {
            if (num.doubleValue() == num.longValue()) return String.valueOf(num.longValue());
            return String.valueOf(num.doubleValue());
        }
        return String.valueOf(v);
    }
}