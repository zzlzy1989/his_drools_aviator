package com.his.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.web.context.TenantContext;
import com.his.market.dto.RuleTemplateDTO;
import com.his.market.entity.RuleTemplate;
import com.his.market.entity.TemplateInstall;
import com.his.market.entity.TemplateRating;
import com.his.market.mapper.RuleTemplateMapper;
import com.his.market.mapper.TemplateInstallMapper;
import com.his.market.mapper.TemplateRatingMapper;
import com.his.market.security.TemplateSecurityScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 规则模板服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleTemplateService {

    private final RuleTemplateMapper templateMapper;
    private final TemplateInstallMapper installMapper;
    private final TemplateRatingMapper ratingMapper;
    private final TemplateSecurityScanner securityScanner;
    private final ObjectMapper objectMapper;

    /**
     * 模板列表（分页+筛选）
     */
    public IPage<RuleTemplateDTO> pageList(int page, int pageSize, String category, String keyword, String tenantId) {
        LambdaQueryWrapper<RuleTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleTemplate::getDeleted, 0);
        wrapper.eq(RuleTemplate::getStatus, "published");
        if (StringUtils.hasText(category)) {
            wrapper.eq(RuleTemplate::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(RuleTemplate::getName, keyword).or().like(RuleTemplate::getTags, keyword));
        }
        wrapper.orderByDesc(RuleTemplate::getInstallCount, RuleTemplate::getCreateTime);

        Page<RuleTemplate> p = new Page<>(page, pageSize);
        Page<RuleTemplate> result = templateMapper.selectPage(p, wrapper);

        Page<RuleTemplateDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::toDTO).collect(Collectors.toList()));
        return dtoPage;
    }

    /**
     * 获取模板详情
     */
    public RuleTemplateDTO getById(Long id) {
        RuleTemplate template = templateMapper.selectById(id);
        if (template == null || template.getDeleted() == 1) {
            return null;
        }
        return toDTO(template);
    }

    /**
     * 发布模板
     */
    @Transactional
    public RuleTemplateDTO publish(RuleTemplateDTO dto) {
        String tenantId = TenantContext.getTenantId("T001");

        // 安全扫描
        String drlContent = extractDrlContent(dto);
        String aviatorContent = extractAviatorContent(dto);
        TemplateSecurityScanner.SecurityScanResult scanResult = securityScanner.scan(drlContent, aviatorContent);
        if (!scanResult.isPass()) {
            throw new SecurityException("模板内容安全扫描未通过: " + String.join(", ", scanResult.getMessages()));
        }

        RuleTemplate template = new RuleTemplate();
        template.setTemplateKey(dto.getTemplateKey() != null ? dto.getTemplateKey() : "tpl." + System.currentTimeMillis());
        template.setName(dto.getName());
        template.setCategory(dto.getCategory());
        template.setTags(dto.getTags());
        template.setDescription(dto.getDescription());
        template.setVersion(dto.getVersion() != null ? dto.getVersion() : "1.0.0");
        template.setContent(toJsonContent(dto));
        template.setProviderId(tenantId);
        template.setProviderName(dto.getProviderName());
        template.setPublishedBy(tenantId);
        template.setStatus("published");
        template.setInstallCount(0);
        template.setTenantId(tenantId);
        template.setCreateBy(tenantId);
        template.setCreateTime(LocalDateTime.now());
        templateMapper.insert(template);

        log.info("发布规则模板: id={}, name={}", template.getId(), template.getName());
        return toDTO(template);
    }

    /**
     * 更新模板
     */
    @Transactional
    public RuleTemplateDTO update(Long id, RuleTemplateDTO dto) {
        RuleTemplate template = templateMapper.selectById(id);
        if (template == null || template.getDeleted() == 1) {
            throw new RuntimeException("模板不存在");
        }

        template.setName(dto.getName());
        template.setCategory(dto.getCategory());
        template.setTags(dto.getTags());
        template.setDescription(dto.getDescription());
        if (dto.getVersion() != null) {
            template.setVersion(dto.getVersion());
        }
        if (dto.getContent() != null) {
            template.setContent(toJsonContent(dto));
        }
        template.setUpdateBy(TenantContext.getTenantId());
        template.setUpdateTime(LocalDateTime.now());
        templateMapper.updateById(template);

        log.info("更新规则模板: id={}", id);
        return toDTO(template);
    }

    /**
     * 删除模板
     */
    @Transactional
    public void delete(Long id) {
        RuleTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        template.setDeleted(1);
        template.setUpdateTime(LocalDateTime.now());
        templateMapper.updateById(template);
        log.info("删除规则模板: id={}", id);
    }

    /**
     * 安装模板
     */
    @Transactional
    public void install(Long templateId, String tenantId) {
        RuleTemplate template = templateMapper.selectById(templateId);
        if (template == null || template.getDeleted() == 1) {
            throw new RuntimeException("模板不存在");
        }

        LambdaQueryWrapper<TemplateInstall> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateInstall::getTemplateId, templateId);
        wrapper.eq(TemplateInstall::getTenantId, tenantId);
        wrapper.eq(TemplateInstall::getDeleted, 0);
        TemplateInstall existing = installMapper.selectOne(wrapper);

        if (existing != null) {
            throw new RuntimeException("该模板已安装，请先卸载再安装");
        }

        TemplateInstall install = new TemplateInstall();
        install.setTemplateId(templateId);
        install.setTenantId(tenantId);
        install.setInstalledBy(tenantId);
        install.setInstalledVersion(template.getVersion());
        install.setInstallTime(LocalDateTime.now());
        install.setLastSyncVersion(template.getVersion());
        install.setLastSyncTime(LocalDateTime.now());
        install.setStatus("active");
        installMapper.insert(install);

        template.setInstallCount(template.getInstallCount() + 1);
        templateMapper.updateById(template);

        log.info("安装模板: templateId={}, tenantId={}", templateId, tenantId);
    }

    /**
     * 卸载模板
     */
    @Transactional
    public void uninstall(Long templateId, String tenantId) {
        LambdaQueryWrapper<TemplateInstall> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateInstall::getTemplateId, templateId);
        wrapper.eq(TemplateInstall::getTenantId, tenantId);
        wrapper.eq(TemplateInstall::getDeleted, 0);
        TemplateInstall install = installMapper.selectOne(wrapper);

        if (install != null) {
            install.setDeleted(1);
            install.setStatus("unsubscribed");
            installMapper.updateById(install);
        }

        RuleTemplate template = templateMapper.selectById(templateId);
        if (template != null && template.getInstallCount() > 0) {
            template.setInstallCount(template.getInstallCount() - 1);
            templateMapper.updateById(template);
        }

        log.info("卸载模板: templateId={}, tenantId={}", templateId, tenantId);
    }

    /**
     * 我的发布模板
     */
    public List<RuleTemplateDTO> myTemplates(String tenantId) {
        LambdaQueryWrapper<RuleTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleTemplate::getTenantId, tenantId);
        wrapper.eq(RuleTemplate::getDeleted, 0);
        wrapper.orderByDesc(RuleTemplate::getCreateTime);
        List<RuleTemplate> templates = templateMapper.selectList(wrapper);
        return templates.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * 已订阅模板
     */
    public List<RuleTemplateDTO> subscribedTemplates(String tenantId) {
        LambdaQueryWrapper<TemplateInstall> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateInstall::getTenantId, tenantId);
        wrapper.eq(TemplateInstall::getDeleted, 0);
        wrapper.eq(TemplateInstall::getStatus, "active");
        List<TemplateInstall> installs = installMapper.selectList(wrapper);

        List<Long> templateIds = installs.stream().map(TemplateInstall::getTemplateId).collect(Collectors.toList());
        if (templateIds.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<RuleTemplate> templateWrapper = new LambdaQueryWrapper<>();
        templateWrapper.in(RuleTemplate::getId, templateIds);
        templateWrapper.eq(RuleTemplate::getDeleted, 0);
        List<RuleTemplate> templates = templateMapper.selectList(templateWrapper);

        return templates.stream().map(t -> {
            RuleTemplateDTO dto = toDTO(t);
            // 找出安装记录，填充版本信息
            TemplateInstall install = installs.stream()
                    .filter(i -> i.getTemplateId().equals(t.getId()))
                    .findFirst().orElse(null);
            if (install != null) {
                Map<String, Object> installInfo = new java.util.LinkedHashMap<>();
                installInfo.put("installedVersion", install.getInstalledVersion());
                installInfo.put("availableVersion", install.getAvailableVersion());
                installInfo.put("lastSyncTime", install.getLastSyncTime() != null ? install.getLastSyncTime().toString() : null);
                installInfo.put("lastCheckTime", install.getLastCheckTime() != null ? install.getLastCheckTime().toString() : null);
                dto.setInstallInfo(installInfo);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 检查模板更新
     */
    @Transactional
    public Map<String, Object> checkForUpdate(Long templateId, String tenantId) {
        TemplateInstall install = getInstallRecord(templateId, tenantId);
        RuleTemplate template = templateMapper.selectById(templateId);

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("templateId", templateId);
        result.put("currentVersion", install != null ? install.getInstalledVersion() : null);
        result.put("latestVersion", template != null ? template.getVersion() : null);
        result.put("hasUpdate", install != null && template != null &&
                !install.getInstalledVersion().equals(template.getVersion()));

        // 更新检查时间
        if (install != null) {
            install.setAvailableVersion(template.getVersion());
            install.setLastCheckTime(LocalDateTime.now());
            installMapper.updateById(install);
        }

        return result;
    }

    /**
     * 升级模板版本
     */
    @Transactional
    public void upgradeTemplate(Long templateId, String tenantId) {
        TemplateInstall install = getInstallRecord(templateId, tenantId);
        RuleTemplate template = templateMapper.selectById(templateId);

        if (install == null || template == null) {
            throw new RuntimeException("未找到安装记录或模板");
        }

        // 检查是否有新版本
        if (!install.getInstalledVersion().equals(template.getVersion())) {
            install.setInstalledVersion(template.getVersion());
            install.setLastSyncVersion(template.getVersion());
            install.setLastSyncTime(LocalDateTime.now());
            installMapper.updateById(install);

            log.info("升级模板版本: templateId={}, from={}, to={}",
                    templateId, install.getInstalledVersion(), template.getVersion());
        }
    }

    private TemplateInstall getInstallRecord(Long templateId, String tenantId) {
        return installMapper.selectOne(new LambdaQueryWrapper<TemplateInstall>()
                .eq(TemplateInstall::getTemplateId, templateId)
                .eq(TemplateInstall::getTenantId, tenantId)
                .eq(TemplateInstall::getDeleted, 0));
    }

    private RuleTemplateDTO toDTO(RuleTemplate template) {
        RuleTemplateDTO dto = new RuleTemplateDTO();
        dto.setId(template.getId());
        dto.setTemplateKey(template.getTemplateKey());
        dto.setName(template.getName());
        dto.setCategory(template.getCategory());
        dto.setTags(template.getTags());
        dto.setDescription(template.getDescription());
        dto.setVersion(template.getVersion());
        dto.setProviderId(template.getProviderId());
        dto.setProviderName(template.getProviderName());
        dto.setPublishedBy(template.getPublishedBy());
        dto.setStatus(template.getStatus());
        dto.setInstallCount(template.getInstallCount());
        dto.setTenantId(template.getTenantId());
        dto.setCreateBy(template.getCreateBy());
        dto.setCreateTime(template.getCreateTime() != null ? template.getCreateTime().toString() : null);

        try {
            if (template.getContent() != null) {
                Map<String, Object> contentMap = objectMapper.readValue(template.getContent(), Map.class);
                dto.setRules((List<Map<String, Object>>) contentMap.get("rules"));
                dto.setFormulas((List<Map<String, Object>>) contentMap.get("formulas"));
                dto.setFlows((List<Map<String, Object>>) contentMap.get("flows"));
            }
        } catch (Exception e) {
            log.error("解析模板内容失败: id={}", template.getId(), e);
        }

        // 填充评分汇总
        try {
            dto.setRatingSummary(getRatingSummary(template.getId()));
        } catch (Exception e) {
            log.error("获取评分汇总失败: templateId={}", template.getId(), e);
        }

        return dto;
    }

    /**
     * 获取评分汇总（供内部使用）
     */
    private Map<String, Object> getRatingSummary(Long templateId) {
        List<TemplateRating> ratings = ratingMapper.selectList(
            new LambdaQueryWrapper<TemplateRating>()
                .eq(TemplateRating::getTemplateId, templateId)
                .eq(TemplateRating::getDeleted, 0)
        );
        int count = ratings.size();
        double avgRating = 0.0;
        if (count > 0) {
            int total = ratings.stream().mapToInt(TemplateRating::getRating).sum();
            avgRating = BigDecimal.valueOf((double) total / count)
                    .setScale(1, java.math.RoundingMode.HALF_UP).doubleValue();
        }
        long[] distribution = new long[5];
        for (TemplateRating r : ratings) {
            int star = Math.min(Math.max(r.getRating(), 1), 5);
            distribution[star - 1]++;
        }
        return Map.of(
            "count", count,
            "avgRating", avgRating,
            "distribution", Map.of("5", distribution[4], "4", distribution[3], "3", distribution[2], "2", distribution[1], "1", distribution[0])
        );
    }

    private String toJsonContent(RuleTemplateDTO dto) {
        try {
            Map<String, Object> content = Map.of(
                "rules", dto.getRules() != null ? dto.getRules() : List.of(),
                "formulas", dto.getFormulas() != null ? dto.getFormulas() : List.of(),
                "flows", dto.getFlows() != null ? dto.getFlows() : List.of()
            );
            return objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            log.error("序列化模板内容失败", e);
            return "{}";
        }
    }

    /**
     * 提取 DRL 内容用于安全扫描
     */
    private String extractDrlContent(RuleTemplateDTO dto) {
        StringBuilder sb = new StringBuilder();
        if (dto.getRules() != null) {
            for (Object rule : dto.getRules()) {
                if (rule instanceof Map r) {
                    Object content = r.get("ruleContent");
                    if (content != null) {
                        sb.append(content).append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * 提取 Aviator 公式内容用于安全扫描
     */
    private String extractAviatorContent(RuleTemplateDTO dto) {
        StringBuilder sb = new StringBuilder();
        if (dto.getFormulas() != null) {
            for (Object formula : dto.getFormulas()) {
                if (formula instanceof Map f) {
                    Object content = f.get("formulaText");
                    if (content != null) {
                        sb.append(content).append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * 安全异常（用于模板内容扫描未通过）
     */
    public static class SecurityException extends RuntimeException {
        public SecurityException(String message) {
            super(message);
        }
    }
}