package com.his.formula.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.web.context.TenantContext;
import com.his.common.web.exception.BusinessException;
import com.his.common.web.service.AuditLogService;
import com.his.common.aviator.engine.AviatorEngine;
import com.his.formula.dto.*;
import com.his.formula.entity.AviatorFormula;
import com.his.formula.entity.FormulaParam;
import com.his.formula.listener.FormulaPublishEvent;
import com.his.formula.mapper.AviatorFormulaMapper;
import com.his.formula.entity.FormulaHistory;
import com.his.formula.mapper.FormulaHistoryMapper;
import com.his.formula.mapper.FormulaParamMapper;
import com.his.formula.validator.FormulaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公式服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormulaService {

    private final AviatorFormulaMapper formulaMapper;
    private final FormulaParamMapper paramMapper;
    private final FormulaHistoryMapper formulaHistoryMapper;
    private final FormulaValidator formulaValidator;
    private final AuditLogService auditLogService;
    private final ApplicationEventPublisher eventPublisher;
    private final AviatorEngine aviatorEngine;

    /**
     * 分页查询公式
     */
    public IPage<FormulaVO> pageList(Integer page, Integer pageSize, FormulaQueryDTO queryDTO) {
        String tenantId = TenantContext.getTenantId("T001");

        Page<AviatorFormula> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AviatorFormula> wrapper = new LambdaQueryWrapper<AviatorFormula>()
                .eq(AviatorFormula::getTenantId, tenantId)
                .eq(AviatorFormula::getDeleted, 0);

        if (StringUtils.hasText(queryDTO.getCategory())) {
            wrapper.eq(AviatorFormula::getCategory, queryDTO.getCategory());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(AviatorFormula::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getFormulaKey())) {
            wrapper.like(AviatorFormula::getFormulaKey, queryDTO.getFormulaKey());
        }
        if (StringUtils.hasText(queryDTO.getFormulaName())) {
            wrapper.like(AviatorFormula::getFormulaName, queryDTO.getFormulaName());
        }

        wrapper.orderByDesc(AviatorFormula::getCreateTime);
        IPage<AviatorFormula> pageResult = formulaMapper.selectPage(pageParam, wrapper);

        return pageResult.convert(this::convertToVO);
    }

    /**
     * 根据ID获取公式
     */
    public FormulaVO getById(Long id) {
        AviatorFormula formula = formulaMapper.selectById(id);
        if (formula == null || formula.getDeleted() == 1) {
            throw new BusinessException("HIS-401", "公式不存在");
        }
        return convertToVO(formula);
    }

    /**
     * 根据KEY获取公式
     */
    public FormulaVO getByKey(String formulaKey) {
        String tenantId = TenantContext.getTenantId();
        AviatorFormula formula = formulaMapper.selectOne(
                new LambdaQueryWrapper<AviatorFormula>()
                        .eq(AviatorFormula::getTenantId, tenantId)
                        .eq(AviatorFormula::getFormulaKey, formulaKey)
                        .eq(AviatorFormula::getDeleted, 0)
        );
        if (formula == null) {
            throw new BusinessException("HIS-401", "公式不存在: " + formulaKey);
        }
        return convertToVO(formula);
    }

    /**
     * 创建公式
     */
    @Transactional
    public FormulaVO create(FormulaCreateDTO dto) {
        String tenantId = TenantContext.getTenantId();

        AviatorFormula existing = formulaMapper.selectOne(
                new LambdaQueryWrapper<AviatorFormula>()
                        .eq(AviatorFormula::getTenantId, tenantId)
                        .eq(AviatorFormula::getFormulaKey, dto.getFormulaKey())
                        .eq(AviatorFormula::getDeleted, 0)
        );
        if (existing != null) {
            throw new BusinessException("公式Key已存在: " + dto.getFormulaKey());
        }

        FormulaValidator.ValidationResult validation = formulaValidator.validate(dto.getFormulaText());

        AviatorFormula formula = new AviatorFormula();
        formula.setFormulaKey(dto.getFormulaKey());
        formula.setFormulaName(dto.getFormulaName());
        formula.setFormulaText(dto.getFormulaText());
        formula.setCategory(dto.getCategory());
        formula.setDescription(dto.getDescription());
        formula.setVersion(1);
        formula.setStatus(validation.isValid() ? "validated" : "draft");
        formula.setIsValidated(validation.isValid() ? 1 : 0);
        formula.setValidatedMsg(validation.getMessage());
        formula.setTenantId(tenantId);
        formula.setCreateBy(tenantId);

        formulaMapper.insert(formula);

        if (dto.getParams() != null && !dto.getParams().isEmpty()) {
            saveParams(formula.getId(), dto.getParams(), tenantId);
        }

        log.info("创建公式: key={}, tenantId={}", dto.getFormulaKey(), tenantId);
        auditLogService.logCreate("FORMULA", String.valueOf(formula.getId()), formula.getFormulaKey(), formula);

        return convertToVO(formula);
    }

    /**
     * 更新公式
     */
    @Transactional
    public FormulaVO update(Long id, FormulaUpdateDTO dto) {
        AviatorFormula formula = formulaMapper.selectById(id);
        if (formula == null || formula.getDeleted() == 1) {
            throw new BusinessException("HIS-401", "公式不存在");
        }

        if (dto.getFormulaText() != null) {
            FormulaValidator.ValidationResult validation = formulaValidator.validate(dto.getFormulaText());
            if (!validation.isValid()) {
                throw new BusinessException("HIS-402", "公式语法错误: " + validation.getMessage());
            }

            // 保存旧版本到历史
            saveToHistory(formula, "更新公式");

            formula.setFormulaText(dto.getFormulaText());
            formula.setIsValidated(1);
            formula.setValidatedMsg(null);
            formula.setVersion(formula.getVersion() + 1);
        }

        if (dto.getFormulaName() != null) {
            formula.setFormulaName(dto.getFormulaName());
        }
        if (dto.getDescription() != null) {
            formula.setDescription(dto.getDescription());
        }

        formula.setUpdateBy(TenantContext.getTenantId());
        formulaMapper.updateById(formula);

        if (dto.getParams() != null) {
            paramMapper.delete(new LambdaQueryWrapper<FormulaParam>()
                    .eq(FormulaParam::getFormulaId, id));
            saveParams(id, dto.getParams(), formula.getTenantId());
        }

        log.info("更新公式: id={}", id);
        auditLogService.logUpdate("FORMULA", String.valueOf(id), formula.getFormulaKey(), formula);

        return convertToVO(formula);
    }

    /**
     * 删除公式
     */
    @Transactional
    public void delete(Long id) {
        AviatorFormula formula = formulaMapper.selectById(id);
        if (formula == null || formula.getDeleted() == 1) {
            throw new BusinessException("HIS-401", "公式不存在");
        }

        formula.setDeleted(1);
        formula.setUpdateBy(TenantContext.getTenantId());
        formulaMapper.updateById(formula);

        log.info("删除公式: id={}", id);
        auditLogService.logDelete("FORMULA", String.valueOf(id), formula.getFormulaKey());
    }

    /**
     * 发布公式
     */
    @Transactional
    public FormulaVO publish(Long id) {
        AviatorFormula formula = formulaMapper.selectById(id);
        if (formula == null || formula.getDeleted() == 1) {
            throw new BusinessException("HIS-401", "公式不存在");
        }

        FormulaValidator.ValidationResult validation = formulaValidator.validate(formula.getFormulaText());
        if (!validation.isValid()) {
            throw new BusinessException("HIS-402", "公式语法错误: " + validation.getMessage());
        }

        formula.setStatus("active");
        formula.setUpdateBy(TenantContext.getTenantId());
        formulaMapper.updateById(formula);

        log.info("发布公式: id={}, key={}", id, formula.getFormulaKey());
        auditLogService.logPublish("FORMULA", String.valueOf(id), formula.getFormulaKey());

        eventPublisher.publishEvent(FormulaPublishEvent.published(this, formula));

        return convertToVO(formula);
    }

    /**
     * 校验公式
     */
    public String validate(Long id) {
        AviatorFormula formula = formulaMapper.selectById(id);
        if (formula == null || formula.getDeleted() == 1) {
            throw new BusinessException("HIS-401", "公式不存在");
        }

        FormulaValidator.ValidationResult validation = formulaValidator.validate(formula.getFormulaText());
        if (validation.isValid()) {
            formula.setIsValidated(1);
            formula.setValidatedMsg(null);
        } else {
            formula.setIsValidated(2);
            formula.setValidatedMsg(validation.getMessage());
        }
        formula.setUpdateBy(TenantContext.getTenantId());
        formulaMapper.updateById(formula);

        auditLogService.logValidate("FORMULA", String.valueOf(id), formula.getFormulaKey(), validation.getMessage());

        return validation.getMessage();
    }

    /**
     * 保存参数
     */
    private void saveParams(Long formulaId, List<FormulaCreateDTO.ParamDTO> params, String tenantId) {
        int order = 0;
        for (FormulaCreateDTO.ParamDTO paramDTO : params) {
            FormulaParam param = new FormulaParam();
            param.setFormulaId(formulaId);
            param.setParamName(paramDTO.getParamName());
            param.setParamType(paramDTO.getParamType());
            param.setDefaultValue(paramDTO.getDefaultValue());
            param.setDescription(paramDTO.getDescription());
            param.setParamOrder(paramDTO.getParamOrder() != null ? paramDTO.getParamOrder() : order++);
            param.setTenantId(tenantId);
            param.setCreateBy(tenantId);
            paramMapper.insert(param);
        }
    }

    /**
     * 保存到历史记录
     */
    private void saveToHistory(AviatorFormula formula, String changeReason) {
        FormulaHistory history = new FormulaHistory();
        history.setFormulaId(formula.getId());
        history.setFormulaKey(formula.getFormulaKey());
        history.setFormulaText(formula.getFormulaText());
        history.setVersion(formula.getVersion());
        history.setStatus(formula.getStatus());
        history.setChangeReason(changeReason);
        history.setChangeBy(TenantContext.getTenantId());
        history.setTenantId(formula.getTenantId());
        formulaHistoryMapper.insert(history);
        log.info("保存公式历史: formulaKey={}, version={}", formula.getFormulaKey(), formula.getVersion());
    }

    /**
     * 保存快照（手动触发）
     */
    @Transactional
    public void saveSnapshot(Long id) {
        AviatorFormula formula = formulaMapper.selectById(id);
        if (formula == null || formula.getDeleted() == 1) {
            throw new BusinessException("HIS-401", "公式不存在");
        }
        saveToHistory(formula, "手动保存快照");
    }

    /**
     * 回滚到指定版本
     */
    @Transactional
    public FormulaVO rollback(Long id, Integer targetVersion) {
        AviatorFormula formula = formulaMapper.selectById(id);
        if (formula == null || formula.getDeleted() == 1) {
            throw new BusinessException("HIS-401", "公式不存在");
        }

        FormulaHistory history = formulaHistoryMapper.selectOne(
                new LambdaQueryWrapper<FormulaHistory>()
                        .eq(FormulaHistory::getFormulaId, id)
                        .eq(FormulaHistory::getVersion, targetVersion)
        );

        if (history == null) {
            throw new BusinessException("HIS-401", "历史版本不存在: version=" + targetVersion);
        }

        // 保存当前版本到历史
        saveToHistory(formula, "回滚到版本 " + targetVersion);

        // 恢复到目标版本
        formula.setFormulaText(history.getFormulaText());
        formula.setVersion(targetVersion + 1);
        formula.setUpdateBy(TenantContext.getTenantId());
        formulaMapper.updateById(formula);

        log.info("公式回滚: id={}, targetVersion={}, newVersion={}", id, targetVersion, formula.getVersion());
        return convertToVO(formula);
    }

    /**
     * 获取版本历史
     */
    public List<FormulaHistoryVO> getVersionHistory(Long formulaId) {
        List<FormulaHistory> histories = formulaHistoryMapper.selectList(
                new LambdaQueryWrapper<FormulaHistory>()
                        .eq(FormulaHistory::getFormulaId, formulaId)
                        .orderByDesc(FormulaHistory::getVersion)
        );

        return histories.stream().map(h -> {
            FormulaHistoryVO vo = new FormulaHistoryVO();
            vo.setId(h.getId());
            vo.setFormulaId(h.getFormulaId());
            vo.setFormulaKey(h.getFormulaKey());
            vo.setFormulaText(h.getFormulaText());
            vo.setVersion(h.getVersion());
            vo.setStatus(h.getStatus());
            vo.setChangeReason(h.getChangeReason());
            vo.setChangeBy(h.getChangeBy());
            vo.setChangeTime(h.getChangeTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 测试公式执行
     */
    public Object test(Long id, Map<String, Object> params) {
        AviatorFormula formula = formulaMapper.selectById(id);
        if (formula == null || formula.getDeleted() == 1) {
            throw new BusinessException("HIS-401", "公式不存在");
        }
        long start = System.currentTimeMillis();
        Object result = aviatorEngine.execute(formula.getFormulaText(), params);
        long elapsed = System.currentTimeMillis() - start;
        log.info("公式测试: id={}, formulaKey={}, elapsed={}ms", id, formula.getFormulaKey(), elapsed);
        return result;
    }

    /**
     * 转换为 VO
     */
    private FormulaVO convertToVO(AviatorFormula formula) {
        FormulaVO vo = new FormulaVO();
        vo.setId(formula.getId());
        vo.setFormulaKey(formula.getFormulaKey());
        vo.setFormulaName(formula.getFormulaName());
        vo.setFormulaText(formula.getFormulaText());
        vo.setCategory(formula.getCategory());
        vo.setVersion(formula.getVersion());
        vo.setStatus(formula.getStatus());
        vo.setDescription(formula.getDescription());
        vo.setIsValidated(formula.getIsValidated());
        vo.setValidatedMsg(formula.getValidatedMsg());
        vo.setTenantId(formula.getTenantId());
        vo.setCreateBy(formula.getCreateBy());
        vo.setCreateTime(formula.getCreateTime());
        vo.setUpdateBy(formula.getUpdateBy());
        vo.setUpdateTime(formula.getUpdateTime());

        List<FormulaParam> params = paramMapper.selectList(
                new LambdaQueryWrapper<FormulaParam>()
                        .eq(FormulaParam::getFormulaId, formula.getId())
                        .eq(FormulaParam::getDeleted, 0)
                        .orderByAsc(FormulaParam::getParamOrder)
        );
        vo.setParams(params.stream().map(this::convertToParamVO).collect(Collectors.toList()));

        return vo;
    }

    private FormulaVO.ParamVO convertToParamVO(FormulaParam param) {
        FormulaVO.ParamVO vo = new FormulaVO.ParamVO();
        vo.setId(param.getId());
        vo.setParamName(param.getParamName());
        vo.setParamType(param.getParamType());
        vo.setDefaultValue(param.getDefaultValue());
        vo.setDescription(param.getDescription());
        vo.setParamOrder(param.getParamOrder());
        return vo;
    }
}
