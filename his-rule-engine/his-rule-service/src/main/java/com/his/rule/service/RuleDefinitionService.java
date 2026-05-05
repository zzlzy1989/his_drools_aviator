package com.his.rule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.web.context.TenantContext;
import com.his.common.web.exception.BusinessException;
import com.his.common.web.service.AuditLogService;
import com.his.rule.dto.*;
import com.his.rule.entity.RuleDefinition;
import com.his.rule.entity.RuleGroup;
import com.his.rule.mapper.RuleDefinitionMapper;
import com.his.rule.mapper.RuleGroupMapper;
import com.his.rule.validator.DrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 规则定义服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleDefinitionService {

    private final RuleDefinitionMapper ruleDefinitionMapper;
    private final RuleGroupMapper ruleGroupMapper;
    private final DrlValidator drlValidator;
    private final AuditLogService auditLogService;

    /**
     * 分页查询规则
     */
    public IPage<RuleVO> pageList(Integer page, Integer pageSize, RuleQueryDTO queryDTO) {
        String tenantId = TenantContext.getTenantId("T001");

        Page<RuleDefinition> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<RuleDefinition> wrapper = new LambdaQueryWrapper<RuleDefinition>()
                .eq(RuleDefinition::getTenantId, tenantId)
                .eq(RuleDefinition::getDeleted, 0);

        if (StringUtils.hasText(queryDTO.getCategory())) {
            wrapper.eq(RuleDefinition::getCategory, queryDTO.getCategory());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(RuleDefinition::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getRuleKey())) {
            wrapper.like(RuleDefinition::getRuleKey, queryDTO.getRuleKey());
        }
        if (StringUtils.hasText(queryDTO.getRuleName())) {
            wrapper.like(RuleDefinition::getRuleName, queryDTO.getRuleName());
        }
        if (queryDTO.getRuleGroupId() != null) {
            wrapper.eq(RuleDefinition::getRuleGroupId, queryDTO.getRuleGroupId());
        }

        wrapper.orderByDesc(RuleDefinition::getCreateTime);
        IPage<RuleDefinition> pageResult = ruleDefinitionMapper.selectPage(pageParam, wrapper);

        return pageResult.convert(this::convertToVO);
    }

    /**
     * 根据ID获取规则
     */
    public RuleVO getById(Long id) {
        RuleDefinition rule = ruleDefinitionMapper.selectById(id);
        if (rule == null || rule.getDeleted() == 1) {
            throw new BusinessException("HIS-002", "规则不存在");
        }
        return convertToVO(rule);
    }

    /**
     * 创建规则
     */
    @Transactional
    public RuleVO create(RuleCreateDTO dto) {
        String tenantId = TenantContext.getTenantId();

        RuleDefinition existing = ruleDefinitionMapper.selectOne(
                new LambdaQueryWrapper<RuleDefinition>()
                        .eq(RuleDefinition::getTenantId, tenantId)
                        .eq(RuleDefinition::getRuleKey, dto.getRuleKey())
                        .eq(RuleDefinition::getDeleted, 0)
        );
        if (existing != null) {
            throw new BusinessException("规则Key已存在: " + dto.getRuleKey());
        }

        DrlValidator.ValidationResult validation = drlValidator.validate(dto.getRuleText());
        String status = validation.isValid() ? "validated" : "draft";

        RuleDefinition rule = new RuleDefinition();
        rule.setRuleKey(dto.getRuleKey());
        rule.setRuleName(dto.getRuleName());
        rule.setRuleText(dto.getRuleText());
        rule.setCategory(dto.getCategory());
        rule.setRuleGroupId(dto.getRuleGroupId());
        rule.setDescription(dto.getDescription());
        rule.setSalience(dto.getSalience() != null ? dto.getSalience() : 0);
        rule.setActivationGroup(dto.getActivationGroup());
        rule.setEffectiveStart(dto.getEffectiveStart());
        rule.setEffectiveEnd(dto.getEffectiveEnd());
        rule.setVersion(1);
        rule.setStatus(status);
        rule.setTenantId(tenantId);
        rule.setCreateBy(TenantContext.getTenantId());

        ruleDefinitionMapper.insert(rule);
        log.info("创建规则: key={}, tenantId={}", dto.getRuleKey(), tenantId);

        auditLogService.logCreate("RULE", String.valueOf(rule.getId()), rule.getRuleKey(), rule);

        return convertToVO(rule);
    }

    /**
     * 更新规则
     */
    @Transactional
    public RuleVO update(Long id, RuleUpdateDTO dto) {
        RuleDefinition rule = ruleDefinitionMapper.selectById(id);
        if (rule == null || rule.getDeleted() == 1) {
            throw new BusinessException("HIS-002", "规则不存在");
        }

        if (dto.getRuleText() != null) {
            DrlValidator.ValidationResult validation = drlValidator.validate(dto.getRuleText());
            if (!validation.isValid()) {
                throw new BusinessException("DRL校验失败: " + validation.getMessage());
            }
            rule.setRuleText(dto.getRuleText());
        }

        if (dto.getRuleName() != null) {
            rule.setRuleName(dto.getRuleName());
        }
        if (dto.getRuleGroupId() != null) {
            rule.setRuleGroupId(dto.getRuleGroupId());
        }
        if (dto.getDescription() != null) {
            rule.setDescription(dto.getDescription());
        }
        if (dto.getSalience() != null) {
            rule.setSalience(dto.getSalience());
        }
        if (dto.getActivationGroup() != null) {
            rule.setActivationGroup(dto.getActivationGroup());
        }
        if (dto.getEffectiveStart() != null) {
            rule.setEffectiveStart(dto.getEffectiveStart());
        }
        if (dto.getEffectiveEnd() != null) {
            rule.setEffectiveEnd(dto.getEffectiveEnd());
        }

        rule.setUpdateBy(TenantContext.getTenantId());
        ruleDefinitionMapper.updateById(rule);

        log.info("更新规则: id={}", id);
        auditLogService.logUpdate("RULE", String.valueOf(id), rule.getRuleKey(), rule);

        return convertToVO(rule);
    }

    /**
     * 删除规则
     */
    @Transactional
    public void delete(Long id) {
        RuleDefinition rule = ruleDefinitionMapper.selectById(id);
        if (rule == null || rule.getDeleted() == 1) {
            throw new BusinessException("HIS-002", "规则不存在");
        }

        rule.setDeleted(1);
        rule.setUpdateBy(TenantContext.getTenantId());
        ruleDefinitionMapper.updateById(rule);

        log.info("删除规则: id={}", id);
        auditLogService.logDelete("RULE", String.valueOf(id), rule.getRuleKey());
    }

    /**
     * 发布规则
     */
    @Transactional
    public RuleVO publish(Long id) {
        RuleDefinition rule = ruleDefinitionMapper.selectById(id);
        if (rule == null || rule.getDeleted() == 1) {
            throw new BusinessException("HIS-002", "规则不存在");
        }

        DrlValidator.ValidationResult validation = drlValidator.validate(rule.getRuleText());
        if (!validation.isValid()) {
            throw new BusinessException("DRL校验失败: " + validation.getMessage());
        }

        rule.setStatus("active");
        rule.setUpdateBy(TenantContext.getTenantId());
        ruleDefinitionMapper.updateById(rule);

        log.info("发布规则: id={}, key={}", id, rule.getRuleKey());
        auditLogService.logPublish("RULE", String.valueOf(id), rule.getRuleKey());

        return convertToVO(rule);
    }

    /**
     * 校验规则
     */
    public String validate(Long id) {
        RuleDefinition rule = ruleDefinitionMapper.selectById(id);
        if (rule == null || rule.getDeleted() == 1) {
            throw new BusinessException("HIS-002", "规则不存在");
        }

        DrlValidator.ValidationResult validation = drlValidator.validate(rule.getRuleText());
        if (validation.isValid()) {
            rule.setStatus("validated");
            rule.setUpdateBy(TenantContext.getTenantId());
            ruleDefinitionMapper.updateById(rule);
        }

        auditLogService.logValidate("RULE", String.valueOf(id), rule.getRuleKey(), validation.getMessage());

        return validation.getMessage();
    }

    /**
     * 转换为 VO
     */
    private RuleVO convertToVO(RuleDefinition rule) {
        RuleVO vo = new RuleVO();
        vo.setId(rule.getId());
        vo.setRuleGroupId(rule.getRuleGroupId());
        vo.setRuleKey(rule.getRuleKey());
        vo.setRuleName(rule.getRuleName());
        vo.setRuleText(rule.getRuleText());
        vo.setCategory(rule.getCategory());
        vo.setVersion(rule.getVersion());
        vo.setStatus(rule.getStatus());
        vo.setDescription(rule.getDescription());
        vo.setSalience(rule.getSalience());
        vo.setActivationGroup(rule.getActivationGroup());
        vo.setEffectiveStart(rule.getEffectiveStart());
        vo.setEffectiveEnd(rule.getEffectiveEnd());
        vo.setTenantId(rule.getTenantId());
        vo.setCreateBy(rule.getCreateBy());
        vo.setCreateTime(rule.getCreateTime());
        vo.setUpdateBy(rule.getUpdateBy());
        vo.setUpdateTime(rule.getUpdateTime());

        if (rule.getRuleGroupId() != null) {
            RuleGroup group = ruleGroupMapper.selectById(rule.getRuleGroupId());
            if (group != null) {
                vo.setGroupName(group.getGroupName());
            }
        }

        return vo;
    }
}
