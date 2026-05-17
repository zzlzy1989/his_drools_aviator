package com.his.settlement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.web.result.PageResult;
import com.his.common.web.result.Result;
import com.his.settlement.dto.TestDataSetDTO;
import com.his.settlement.entity.TestExecutionLog;
import com.his.settlement.entity.TestSuite;
import com.his.settlement.service.SandboxService;
import com.his.settlement.service.TestReportService;
import com.his.settlement.service.TestSuiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 测试沙箱控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sandbox")
@RequiredArgsConstructor
@Tag(name = "测试沙箱", description = "测试沙箱与执行历史")
public class SandboxController {

    private final SandboxService sandboxService;
    private final TestReportService testReportService;
    private final TestSuiteService suiteService;

    /**
     * 数据集列表
     */
    @GetMapping("/datasets")
    public Result<List<TestDataSetDTO>> list(@RequestParam(required = false) String category) {
        return Result.success(sandboxService.list(category));
    }

    /**
     * 获取数据集详情
     */
    @GetMapping("/datasets/{id}")
    public Result<TestDataSetDTO> getById(@PathVariable Long id) {
        return Result.success(sandboxService.getById(id));
    }

    /**
     * 创建数据集
     */
    @PostMapping("/datasets")
    public Result<TestDataSetDTO> create(@RequestBody TestDataSetDTO dto) {
        return Result.success(sandboxService.create(dto));
    }

    /**
     * 更新数据集
     */
    @PutMapping("/datasets/{id}")
    public Result<TestDataSetDTO> update(@PathVariable Long id, @RequestBody TestDataSetDTO dto) {
        return Result.success(sandboxService.update(id, dto));
    }

    /**
     * 删除数据集
     */
    @DeleteMapping("/datasets/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sandboxService.delete(id);
        return Result.success(null);
    }

    /**
     * 执行单个测试用例
     */
    @PostMapping("/execute/{caseId}")
    public Result<Map<String, Object>> executeTestCase(@PathVariable Long caseId) {
        return Result.success(sandboxService.executeTestCase(caseId));
    }

    /**
     * 批量执行测试用例
     */
    @PostMapping("/batch-execute/{dataSetId}")
    public Result<List<Map<String, Object>>> batchExecute(@PathVariable Long dataSetId) {
        return Result.success(sandboxService.batchExecute(dataSetId));
    }

    /**
     * 生成测试报告
     */
    @GetMapping("/report/{dataSetId}")
    public Result<String> generateReport(@PathVariable Long dataSetId) {
        List<Map<String, Object>> results = sandboxService.batchExecute(dataSetId);
        String html = testReportService.generateHtmlReport(dataSetId, results);
        return Result.success(html);
    }

    /**
     * 分页查询测试执行历史
     */
    @GetMapping("/execution-logs")
    @Operation(summary = "分页查询测试执行历史")
    public Result<PageResult<TestExecutionLog>> listExecutionLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<TestExecutionLog> pageResult = sandboxService.listExecutionLogs(page, pageSize, status, startDate, endDate);
        return Result.success(PageResult.of(pageResult));
    }

    /**
     * 获取执行历史详情
     */
    @GetMapping("/execution-logs/{id}")
    @Operation(summary = "获取执行历史详情")
    public Result<TestExecutionLog> getExecutionLog(@PathVariable Long id) {
        return Result.success(sandboxService.getExecutionLog(id));
    }

    // ===== 测试套件 =====

    /**
     * 套件列表
     */
    @GetMapping("/suites")
    @Operation(summary = "测试套件列表")
    public Result<List<com.his.settlement.dto.TestSuiteDTO>> listSuites(@RequestParam(required = false) String category) {
        return Result.success(suiteService.list(category));
    }

    /**
     * 获取套件详情
     */
    @GetMapping("/suites/{id}")
    @Operation(summary = "获取套件详情")
    public Result<com.his.settlement.dto.TestSuiteDTO> getSuite(@PathVariable Long id) {
        return Result.success(suiteService.getById(id));
    }

    /**
     * 创建套件
     */
    @PostMapping("/suites")
    @Operation(summary = "创建测试套件")
    public Result<com.his.settlement.dto.TestSuiteDTO> createSuite(@RequestBody com.his.settlement.dto.TestSuiteDTO dto) {
        return Result.success(suiteService.create(dto));
    }

    /**
     * 更新套件
     */
    @PutMapping("/suites/{id}")
    @Operation(summary = "更新测试套件")
    public Result<com.his.settlement.dto.TestSuiteDTO> updateSuite(@PathVariable Long id, @RequestBody com.his.settlement.dto.TestSuiteDTO dto) {
        return Result.success(suiteService.update(id, dto));
    }

    /**
     * 删除套件
     */
    @DeleteMapping("/suites/{id}")
    @Operation(summary = "删除测试套件")
    public Result<Void> deleteSuite(@PathVariable Long id) {
        suiteService.delete(id);
        return Result.success(null);
    }

    /**
     * 执行套件
     */
    @PostMapping("/suites/{id}/execute")
    @Operation(summary = "批量执行套件中的所有测试")
    public Result<List<Map<String, Object>>> executeSuite(@PathVariable Long id) {
        return Result.success(suiteService.executeSuite(id));
    }
}