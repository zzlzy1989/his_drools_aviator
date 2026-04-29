package com.his.formula.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.web.context.TenantContext;
import com.his.common.web.exception.BusinessException;
import com.his.common.web.service.AuditLogService;
import com.his.formula.dto.*;
import com.his.formula.entity.AviatorFormula;
import com.his.formula.entity.FormulaParam;
import com.his.formula.listener.FormulaPublishEvent;
import com.his.formula.mapper.AviatorFormulaMapper;
import com.his.formula.mapper.FormulaParamMapper;
import com.his.formula.validator.FormulaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final FormulaValidator formulaValidator;
    private final AuditLogService auditLogService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 分页查询公式
     */
    public IPage<FormulaVO> pageList(Integer page, Integer pageSize, FormulaQueryDTO queryDTO) {
        String tenantId = TenantContext.getTenantId();

        Page<AviatorFormula> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AviatorFormula> wrapper = new LambdaQueryWrapper<AviatorFormula>()
                .eq(AviatorFormula::getTenantId, tenantId)
                .eq(AviatorFormula::getDeleted, 0);

        if (queryDTO.getCategory() != null) {
            wrapper.eq(AviatorFormula::getCategory, queryDTO.getCategory());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(AviatorFormula::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getFormulaKey() != null) {
            wrapper.like(AviatorFormula::getFormulaKey, queryDTO.getFormulaKey());
        }
        if (queryDTO.getFormulaName() != null) {
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
            formula.setFormulaText(dto.getFormulaText());
            formula.setIsValidated(1);
            formula.setValidatedMsg(null);
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
