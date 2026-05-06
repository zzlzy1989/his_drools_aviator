package com.his.rule.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.web.result.PageResult;
import com.his.common.web.result.Result;
import com.his.rule.dto.RuleGroupCreateDTO;
import com.his.rule.entity.RuleGroup;
import com.his.rule.service.RuleGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 规则分组 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rule-groups")
@RequiredArgsConstructor
@Tag(name = "规则分组管理", description = "规则分组 CRUD")
public class RuleGroupController {

    private final RuleGroupService ruleGroupService;

    @GetMapping
    @Operation(summary = "查询所有规则分组")
    public Result<List<RuleGroup>> list() {
        return Result.success(ruleGroupService.listEnabled());
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询规则分组")
    public Result<PageResult<RuleGroup>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String status) {
        IPage<RuleGroup> pageResult = ruleGroupService.pageList(page, pageSize, groupName, status);
        return Result.success(PageResult.of(pageResult));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取规则分组详情")
    public Result<RuleGroup> getById(@PathVariable Long id) {
        return Result.success(ruleGroupService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建规则分组")
    public Result<RuleGroup> create(@RequestBody @Valid RuleGroupCreateDTO dto) {
        return Result.success(ruleGroupService.create(dto.getGroupCode(), dto.getGroupName(), dto.getDescription()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新规则分组")
    public Result<RuleGroup> update(@PathVariable Long id,
                                     @RequestBody RuleGroupUpdateDTO dto) {
        return Result.success(ruleGroupService.update(id, dto.getGroupName(), dto.getDescription(), dto.getPriority()));
    }

    @PutMapping("/{id}/enabled")
    @Operation(summary = "启用/禁用规则分组")
    public Result<Void> setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        ruleGroupService.setEnabled(id, enabled);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除规则分组")
    public Result<Void> delete(@PathVariable Long id) {
        ruleGroupService.delete(id);
        return Result.success(null);
    }

    @Data
    public static class RuleGroupUpdateDTO {
        private String groupName;
        private String description;
        private Integer priority;
    }
}
