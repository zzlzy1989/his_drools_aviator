package com.his.rule.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.web.result.PageResult;
import com.his.common.web.result.Result;
import com.his.rule.dto.*;
import com.his.rule.service.RuleDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 规则定义 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
@Tag(name = "规则管理", description = "规则定义 CRUD 和发布")
public class RuleDefinitionController {

    private final RuleDefinitionService ruleDefinitionService;

    @GetMapping
    @Operation(summary = "分页查询规则")
    public Result<PageResult<RuleVO>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            RuleQueryDTO queryDTO) {
        IPage<RuleVO> pageResult = ruleDefinitionService.pageList(page, pageSize, queryDTO);
        return Result.success(PageResult.of(pageResult));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取规则详情")
    public Result<RuleVO> getById(@PathVariable Long id) {
        return Result.success(ruleDefinitionService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建规则")
    public Result<RuleVO> create(@RequestBody @Valid RuleCreateDTO dto) {
        return Result.success(ruleDefinitionService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新规则")
    public Result<RuleVO> update(@PathVariable Long id,
                                  @RequestBody @Valid RuleUpdateDTO dto) {
        return Result.success(ruleDefinitionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除规则")
    public Result<Void> delete(@PathVariable Long id) {
        ruleDefinitionService.delete(id);
        return Result.success(null);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布规则")
    public Result<RuleVO> publish(@PathVariable Long id) {
        return Result.success(ruleDefinitionService.publish(id));
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "校验规则")
    public Result<String> validate(@PathVariable Long id) {
        String message = ruleDefinitionService.validate(id);
        return Result.success(message);
    }
}
