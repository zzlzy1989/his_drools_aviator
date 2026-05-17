package com.his.market.controller;

import com.his.common.web.context.TenantContext;
import com.his.common.web.result.Result;
import com.his.common.web.result.PageResult;
import com.his.market.dto.RuleTemplateDTO;
import com.his.market.dto.TemplateRatingDTO;
import com.his.market.service.RuleTemplateService;
import com.his.market.service.TemplateRatingService;
import com.his.market.service.TemplateFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 规则模板控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/market/templates")
@RequiredArgsConstructor
public class RuleTemplateController {

    private final RuleTemplateService templateService;
    private final TemplateRatingService ratingService;
    private final TemplateFavoriteService favoriteService;

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

    /**
     * 获取模板评分列表
     */
    @GetMapping("/{id}/ratings")
    @Operation(summary = "获取模板评分列表")
    public Result<List<Map<String, Object>>> getRatings(@PathVariable Long id) {
        return Result.success(ratingService.getRatings(id));
    }

    /**
     * 获取模板平均评分
     */
    @GetMapping("/{id}/rating-summary")
    @Operation(summary = "获取模板评分汇总")
    public Result<Map<String, Object>> getRatingSummary(@PathVariable Long id) {
        return Result.success(ratingService.getRatingSummary(id));
    }

    /**
     * 评分/评论模板
     */
    @PostMapping("/{id}/ratings")
    @Operation(summary = "评分/评论模板")
    public Result<Void> rateTemplate(@PathVariable Long id, @RequestBody @Valid TemplateRatingDTO dto) {
        String tenantId = TenantContext.getTenantId("T001");
        ratingService.rate(id, tenantId, dto);
        return Result.success(null);
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("/favorites")
    @Operation(summary = "获取收藏的模板列表")
    public Result<List<Map<String, Object>>> getFavorites() {
        String userId = TenantContext.getTenantId("T001");
        return Result.success(favoriteService.getFavorites(userId));
    }

    /**
     * 收藏模板
     */
    @PostMapping("/{id}/favorite")
    @Operation(summary = "收藏模板")
    public Result<Void> favorite(@PathVariable Long id) {
        String userId = TenantContext.getTenantId("T001");
        favoriteService.favorite(id, userId);
        return Result.success(null);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{id}/favorite")
    @Operation(summary = "取消收藏")
    public Result<Void> unfavorite(@PathVariable Long id) {
        String userId = TenantContext.getTenantId("T001");
        favoriteService.unfavorite(id, userId);
        return Result.success(null);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/{id}/favorite-status")
    @Operation(summary = "检查是否已收藏")
    public Result<Map<String, Boolean>> getFavoriteStatus(@PathVariable Long id) {
        String userId = TenantContext.getTenantId("T001");
        return Result.success(Map.of("favorited", favoriteService.isFavorited(id, userId)));
    }

    /**
     * 检查模板更新
     */
    @GetMapping("/{id}/check-update")
    @Operation(summary = "检查模板更新")
    public Result<Map<String, Object>> checkUpdate(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId("T001");
        return Result.success(templateService.checkForUpdate(id, tenantId));
    }

    /**
     * 升级模板
     */
    @PostMapping("/{id}/upgrade")
    @Operation(summary = "升级模板到最新版本")
    public Result<Void> upgrade(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId("T001");
        templateService.upgradeTemplate(id, tenantId);
        return Result.success(null);
    }
}