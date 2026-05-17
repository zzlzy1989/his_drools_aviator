package com.his.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.web.context.TenantContext;
import com.his.market.dto.RuleTemplateDTO;
import com.his.market.entity.RuleTemplate;
import com.his.market.entity.TemplateInstall;
import com.his.market.mapper.RuleTemplateMapper;
import com.his.market.mapper.TemplateInstallMapper;
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
 * 规则模板服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleTemplateService {

    private final RuleTemplateMapper templateMapper;
    private final TemplateInstallMapper installMapper;
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
        return templates.stream().map(this::toDTO).collect(Collectors.toList());
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

        return dto;
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
}