package com.his.rule.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.web.result.PageResult;
import com.his.common.web.result.Result;
import com.his.rule.entity.RuleGroup;
import com.his.rule.service.RuleGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Operation(summary = "查询所有启用的规则分组")
    public Result<List<RuleGroup>> listEnabled() {
        return Result.success(ruleGroupService.listEnabled());
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询规则分组")
    public Result<PageResult<RuleGroup>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<RuleGroup> pageResult = ruleGroupService.pageList(page, pageSize);
        return Result.success(PageResult.of(pageResult));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取规则分组详情")
    public Result<RuleGroup> getById(@PathVariable Long id) {
        return Result.success(ruleGroupService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建规则分组")
    public Result<RuleGroup> create(@RequestParam String groupCode,
                                    @RequestParam String groupName,
                                    @RequestParam(required = false) String description) {
        return Result.success(ruleGroupService.create(groupCode, groupName, description));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新规则分组")
    public Result<RuleGroup> update(@PathVariable Long id,
                                     @RequestParam(required = false) String groupName,
                                     @RequestParam(required = false) String description,
                                     @RequestParam(required = false) Integer priority) {
        return Result.success(ruleGroupService.update(id, groupName, description, priority));
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
}
