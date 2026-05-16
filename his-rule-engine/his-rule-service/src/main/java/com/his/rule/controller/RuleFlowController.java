package com.his.rule.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.web.result.PageResult;
import com.his.common.web.result.Result;
import com.his.rule.dto.*;
import com.his.rule.engine.RuleFlowEngine;
import com.his.rule.entity.RuleFlow;
import com.his.rule.service.RuleFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 规则流管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/flows")
@RequiredArgsConstructor
@Tag(name = "规则流管理", description = "规则流定义 CRUD、发布、回滚")
public class RuleFlowController {

    private final RuleFlowService ruleFlowService;

    /**
     * 分页查询规则流
     */
    @GetMapping
    @Operation(summary = "分页查询规则流")
    public Result<PageResult<RuleFlowVO>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String flowName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tenantId) {

        FlowQueryDTO queryDTO = new FlowQueryDTO();
        queryDTO.setFlowName(flowName);
        queryDTO.setCategory(category);
        queryDTO.setStatus(status);
        queryDTO.setTenantId(tenantId);

        IPage<RuleFlowVO> iPage = ruleFlowService.pageList(page, pageSize, queryDTO);
        return Result.success(PageResult.of(iPage));
    }

    /**
     * 获取规则流详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取规则流详情")
    public Result<RuleFlowVO> getById(@PathVariable Long id) {
        return Result.success(ruleFlowService.getById(id));
    }

    /**
     * 创建规则流
     */
    @PostMapping
    @Operation(summary = "创建规则流")
    public Result<Long> create(@RequestBody @Valid CreateFlowDTO dto) {
        RuleFlow flow = ruleFlowService.createFlow(dto);
        return Result.success(flow.getId());
    }

    /**
     * 更新规则流
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新规则流")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid UpdateFlowDTO dto) {
        ruleFlowService.updateFlow(id, dto);
        return Result.success();
    }

    /**
     * 删除规则流
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除规则流")
    public Result<Void> delete(@PathVariable Long id) {
        ruleFlowService.deleteFlow(id);
        return Result.success();
    }

    /**
     * 发布规则流
     */
    @PostMapping("/{id}/publish")
    @Operation(summary = "发布规则流")
    public Result<Void> publish(@PathVariable Long id) {
        ruleFlowService.publishFlow(id);
        return Result.success();
    }

    /**
     * 回滚到历史版本
     */
    @PostMapping("/{id}/rollback")
    @Operation(summary = "回滚到历史版本")
    public Result<Void> rollback(@PathVariable Long id, @RequestParam Integer targetVersion) {
        ruleFlowService.rollback(id, targetVersion);
        return Result.success();
    }

    /**
     * 获取版本历史
     */
    @GetMapping("/{id}/versions")
    @Operation(summary = "获取版本历史")
    public Result<List<FlowVersionVO>> getVersions(@PathVariable Long id) {
        return Result.success(ruleFlowService.getVersionHistory(id));
    }

    /**
     * 导出规则流JSON
     */
    @GetMapping("/{id}/export")
    @Operation(summary = "导出规则流JSON")
    public Result<String> exportFlow(@PathVariable Long id) {
        return Result.success(ruleFlowService.exportFlowJson(id));
    }

    /**
     * 导入规则流JSON
     */
    @PostMapping("/import")
    @Operation(summary = "导入规则流JSON")
    public Result<Long> importFlow(@RequestBody String flowJson,
            @RequestParam(required = false, defaultValue = "default") String tenantId) {
        RuleFlow flow = ruleFlowService.importFlowJson(flowJson, tenantId);
        return Result.success(flow.getId());
    }

    /**
     * 执行规则流
     */
    @PostMapping("/{id}/execute")
    @Operation(summary = "执行规则流")
    public Result<RuleFlowEngine.ExecutionResult> execute(@PathVariable Long id, @RequestBody(required = false) String factJson) {
        RuleFlowEngine.ExecutionResult result = ruleFlowService.executeFlow(id, factJson);
        return Result.success(result);
    }
}