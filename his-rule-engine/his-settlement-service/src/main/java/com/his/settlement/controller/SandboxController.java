package com.his.settlement.controller;

import com.his.common.web.result.Result;
import com.his.settlement.dto.TestDataSetDTO;
import com.his.settlement.service.SandboxService;
import com.his.settlement.service.TestReportService;
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
public class SandboxController {

    private final SandboxService sandboxService;
    private final TestReportService testReportService;

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
}