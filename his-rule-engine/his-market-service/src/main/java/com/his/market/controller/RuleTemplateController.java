package com.his.market.controller;

import com.his.common.web.context.TenantContext;
import com.his.common.web.result.Result;
import com.his.common.web.result.PageResult;
import com.his.market.dto.RuleTemplateDTO;
import com.his.market.service.RuleTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 规则模板控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/market/templates")
@RequiredArgsConstructor
public class RuleTemplateController {

    private final RuleTemplateService templateService;

    /**
     * 模板列表（分页+筛选）
     */
    @GetMapping
    public Result<PageResult<RuleTemplateDTO>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        String tenantId = TenantContext.getTenantId("T001");
        var pageResult = templateService.pageList(page, pageSize, category, keyword, tenantId);
        return Result.success(PageResult.of(pageResult));
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    public Result<RuleTemplateDTO> getById(@PathVariable Long id) {
        return Result.success(templateService.getById(id));
    }

    /**
     * 发布模板
     */
    @PostMapping
    public Result<RuleTemplateDTO> publish(@RequestBody RuleTemplateDTO dto) {
        return Result.success(templateService.publish(dto));
    }

    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public Result<RuleTemplateDTO> update(@PathVariable Long id, @RequestBody RuleTemplateDTO dto) {
        return Result.success(templateService.update(id, dto));
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return Result.success(null);
    }

    /**
     * 安装模板
     */
    @PostMapping("/{id}/install")
    public Result<Void> install(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId("T001");
        templateService.install(id, tenantId);
        return Result.success(null);
    }

    /**
     * 卸载模板
     */
    @DeleteMapping("/{id}/install")
    public Result<Void> uninstall(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId("T001");
        templateService.uninstall(id, tenantId);
        return Result.success(null);
    }

    /**
     * 我的发布模板
     */
    @GetMapping("/my")
    public Result<List<RuleTemplateDTO>> myTemplates() {
        String tenantId = TenantContext.getTenantId("T001");
        return Result.success(templateService.myTemplates(tenantId));
    }

    /**
     * 已订阅模板
     */
    @GetMapping("/subscribed")
    public Result<List<RuleTemplateDTO>> subscribed() {
        String tenantId = TenantContext.getTenantId("T001");
        return Result.success(templateService.subscribedTemplates(tenantId));
    }
}