package com.his.rule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.web.context.TenantContext;
import com.his.common.web.exception.BusinessException;
import com.his.rule.entity.RuleGroup;
import com.his.rule.mapper.RuleGroupMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 规则分组服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleGroupService {

    private final RuleGroupMapper ruleGroupMapper;

    /**
     * 查询所有启用的规则分组
     */
    public List<RuleGroup> listEnabled() {
        String tenantId = TenantContext.getTenantId("T001");
        return ruleGroupMapper.selectList(
                new LambdaQueryWrapper<RuleGroup>()
                        .eq(RuleGroup::getTenantId, tenantId)
                        .eq(RuleGroup::getIsEnabled, 1)
                        .eq(RuleGroup::getDeleted, 0)
                        .orderByAsc(RuleGroup::getPriority)
        );
    }

    /**
     * 分页查询规则分组
     */
    public IPage<RuleGroup> pageList(Integer page, Integer pageSize) {
        String tenantId = TenantContext.getTenantId("T001");
        Page<RuleGroup> pageParam = new Page<>(page, pageSize);
        return ruleGroupMapper.selectPage(pageParam,
                new LambdaQueryWrapper<RuleGroup>()
                        .eq(RuleGroup::getTenantId, tenantId)
                        .eq(RuleGroup::getDeleted, 0)
                        .orderByAsc(RuleGroup::getPriority)
        );
    }

    /**
     * 根据ID获取分组
     */
    public RuleGroup getById(Long id) {
        RuleGroup group = ruleGroupMapper.selectById(id);
        if (group == null || group.getDeleted() == 1) {
            throw new BusinessException("规则分组不存在");
        }
        return group;
    }

    /**
     * 创建规则分组
     */
    @Transactional
    public RuleGroup create(String groupCode, String groupName, String description) {
        String tenantId = TenantContext.getTenantId();

        RuleGroup existing = ruleGroupMapper.selectOne(
                new LambdaQueryWrapper<RuleGroup>()
                        .eq(RuleGroup::getTenantId, tenantId)
                        .eq(RuleGroup::getGroupCode, groupCode)
                        .eq(RuleGroup::getDeleted, 0)
        );
        if (existing != null) {
            throw new BusinessException("分组编码已存在: " + groupCode);
        }

        RuleGroup group = new RuleGroup();
        group.setGroupCode(groupCode);
        group.setGroupName(groupName);
        group.setDescription(description);
        group.setTenantId(tenantId);
        group.setIsEnabled(1);
        group.setPriority(0);
        group.setCreateBy(tenantId);

        ruleGroupMapper.insert(group);
        log.info("创建规则分组: code={}, tenantId={}", groupCode, tenantId);

        return group;
    }

    /**
     * 更新规则分组
     */
    @Transactional
    public RuleGroup update(Long id, String groupName, String description, Integer priority) {
        RuleGroup group = ruleGroupMapper.selectById(id);
        if (group == null || group.getDeleted() == 1) {
            throw new BusinessException("规则分组不存在");
        }

        if (groupName != null) {
            group.setGroupName(groupName);
        }
        if (description != null) {
            group.setDescription(description);
        }
        if (priority != null) {
            group.setPriority(priority);
        }
        group.setUpdateBy(TenantContext.getTenantId());

        ruleGroupMapper.updateById(group);
        log.info("更新规则分组: id={}", id);

        return group;
    }

    /**
     * 启用/禁用分组
     */
    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        RuleGroup group = ruleGroupMapper.selectById(id);
        if (group == null || group.getDeleted() == 1) {
            throw new BusinessException("规则分组不存在");
        }

        group.setIsEnabled(enabled ? 1 : 0);
        group.setUpdateBy(TenantContext.getTenantId());
        ruleGroupMapper.updateById(group);

        log.info("设置分组启用状态: id={}, enabled={}", id, enabled);
    }

    /**
     * 删除规则分组
     */
    @Transactional
    public void delete(Long id) {
        RuleGroup group = ruleGroupMapper.selectById(id);
        if (group == null || group.getDeleted() == 1) {
            throw new BusinessException("规则分组不存在");
        }

        group.setDeleted(1);
        group.setUpdateBy(TenantContext.getTenantId());
        ruleGroupMapper.updateById(group);

        log.info("删除规则分组: id={}", id);
    }
}
