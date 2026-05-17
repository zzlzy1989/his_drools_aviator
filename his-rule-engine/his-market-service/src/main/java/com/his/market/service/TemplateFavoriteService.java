package com.his.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.market.entity.RuleTemplate;
import com.his.market.entity.TemplateFavorite;
import com.his.market.mapper.RuleTemplateMapper;
import com.his.market.mapper.TemplateFavoriteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模板收藏服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateFavoriteService {

    private final TemplateFavoriteMapper favoriteMapper;
    private final RuleTemplateMapper templateMapper;

    /**
     * 获取用户收藏的模板列表
     */
    public List<Map<String, Object>> getFavorites(String userId) {
        LambdaQueryWrapper<TemplateFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateFavorite::getUserId, userId);
        wrapper.eq(TemplateFavorite::getDeleted, 0);
        wrapper.orderByDesc(TemplateFavorite::getCreateTime);

        List<TemplateFavorite> favorites = favoriteMapper.selectList(wrapper);
        if (favorites.isEmpty()) {
            return List.of();
        }

        List<Long> templateIds = favorites.stream().map(TemplateFavorite::getTemplateId).collect(Collectors.toList());
        LambdaQueryWrapper<RuleTemplate> templateWrapper = new LambdaQueryWrapper<>();
        templateWrapper.in(RuleTemplate::getId, templateIds);
        templateWrapper.eq(RuleTemplate::getDeleted, 0);
        List<RuleTemplate> templates = templateMapper.selectList(templateWrapper);

        return templates.stream().map(t -> {
            TemplateFavorite fav = favorites.stream()
                    .filter(f -> f.getTemplateId().equals(t.getId()))
                    .findFirst().orElse(null);
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", t.getId());
            map.put("name", t.getName());
            map.put("category", t.getCategory());
            map.put("tags", t.getTags());
            map.put("version", t.getVersion());
            map.put("installCount", t.getInstallCount());
            map.put("favoriteTime", fav != null ? fav.getCreateTime().toString() : null);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 收藏模板
     */
    @Transactional
    public void favorite(Long templateId, String userId) {
        if (templateId == null || !StringUtils.hasText(userId)) {
            throw new RuntimeException("参数错误");
        }

        // 检查模板是否存在
        RuleTemplate template = templateMapper.selectById(templateId);
        if (template == null || template.getDeleted() == 1) {
            throw new RuntimeException("模板不存在");
        }

        // 检查是否已收藏
        LambdaQueryWrapper<TemplateFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateFavorite::getTemplateId, templateId);
        wrapper.eq(TemplateFavorite::getUserId, userId);
        wrapper.eq(TemplateFavorite::getDeleted, 0);
        TemplateFavorite existing = favoriteMapper.selectOne(wrapper);

        if (existing != null) {
            throw new RuntimeException("已收藏，请勿重复收藏");
        }

        TemplateFavorite favorite = new TemplateFavorite();
        favorite.setTemplateId(templateId);
        favorite.setUserId(userId);
        favorite.setTenantId(userId);
        favorite.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(favorite);

        log.info("收藏模板: templateId={}, userId={}", templateId, userId);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void unfavorite(Long templateId, String userId) {
        LambdaQueryWrapper<TemplateFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateFavorite::getTemplateId, templateId);
        wrapper.eq(TemplateFavorite::getUserId, userId);
        wrapper.eq(TemplateFavorite::getDeleted, 0);

        TemplateFavorite favorite = favoriteMapper.selectOne(wrapper);
        if (favorite != null) {
            favorite.setDeleted(1);
            favoriteMapper.updateById(favorite);
            log.info("取消收藏: templateId={}, userId={}", templateId, userId);
        }
    }

    /**
     * 检查是否已收藏
     */
    public boolean isFavorited(Long templateId, String userId) {
        LambdaQueryWrapper<TemplateFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateFavorite::getTemplateId, templateId);
        wrapper.eq(TemplateFavorite::getUserId, userId);
        wrapper.eq(TemplateFavorite::getDeleted, 0);
        return favoriteMapper.selectCount(wrapper) > 0;
    }
}