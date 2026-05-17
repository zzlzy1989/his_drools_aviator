package com.his.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.market.dto.TemplateRatingDTO;
import com.his.market.entity.TemplateRating;
import com.his.market.mapper.TemplateRatingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模板评分服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateRatingService {

    private final TemplateRatingMapper ratingMapper;

    /**
     * 获取模板评分列表
     */
    public List<Map<String, Object>> getRatings(Long templateId) {
        LambdaQueryWrapper<TemplateRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateRating::getTemplateId, templateId);
        wrapper.eq(TemplateRating::getDeleted, 0);
        wrapper.orderByDesc(TemplateRating::getCreateTime);

        List<TemplateRating> ratings = ratingMapper.selectList(wrapper);
        return ratings.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("userId", r.getUserId());
            map.put("rating", r.getRating());
            map.put("comment", r.getComment());
            map.put("createTime", r.getCreateTime() != null ? r.getCreateTime().toString() : null);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 获取模板评分汇总
     */
    public Map<String, Object> getRatingSummary(Long templateId) {
        LambdaQueryWrapper<TemplateRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateRating::getTemplateId, templateId);
        wrapper.eq(TemplateRating::getDeleted, 0);

        List<TemplateRating> ratings = ratingMapper.selectList(wrapper);
        int count = ratings.size();
        double avgRating = 0.0;

        if (count > 0) {
            int total = ratings.stream().mapToInt(TemplateRating::getRating).sum();
            avgRating = BigDecimal.valueOf((double) total / count)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue();
        }

        // 评分分布统计
        long[] distribution = new long[5]; // 1-5星各多少个
        for (TemplateRating r : ratings) {
            int star = Math.min(Math.max(r.getRating(), 1), 5);
            distribution[star - 1]++;
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("count", count);
        summary.put("avgRating", avgRating);
        summary.put("distribution", Map.of(
                "5", distribution[4],
                "4", distribution[3],
                "3", distribution[2],
                "2", distribution[1],
                "1", distribution[0]
        ));

        return summary;
    }

    /**
     * 评分/评论模板
     */
    @Transactional
    public void rate(Long templateId, String tenantId, TemplateRatingDTO dto) {
        if (templateId == null) {
            throw new RuntimeException("模板ID不能为空");
        }

        // 检查是否已评分（每人每模板只能评一次）
        LambdaQueryWrapper<TemplateRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateRating::getTemplateId, templateId);
        wrapper.eq(TemplateRating::getUserId, tenantId);
        wrapper.eq(TemplateRating::getDeleted, 0);

        TemplateRating existing = ratingMapper.selectOne(wrapper);
        if (existing != null) {
            // 更新评分
            existing.setRating(dto.getRating());
            existing.setComment(dto.getComment());
            ratingMapper.updateById(existing);
            log.info("更新模板评分: templateId={}, userId={}, rating={}",
                    templateId, tenantId, dto.getRating());
        } else {
            // 新增评分
            TemplateRating rating = new TemplateRating();
            rating.setTemplateId(templateId);
            rating.setUserId(tenantId);
            rating.setTenantId(tenantId);
            rating.setRating(dto.getRating());
            rating.setComment(dto.getComment());
            rating.setCreateTime(LocalDateTime.now());
            ratingMapper.insert(rating);
            log.info("新增模板评分: templateId={}, userId={}, rating={}",
                    templateId, tenantId, dto.getRating());
        }
    }
}