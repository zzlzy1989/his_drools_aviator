package com.his.rule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.rule.dto.*;
import com.his.rule.entity.RuleFlow;
import com.his.rule.entity.RuleFlowHistory;
import com.his.rule.engine.RuleFlowEngine;
import com.his.rule.mapper.RuleFlowMapper;
import com.his.rule.mapper.RuleFlowHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 规则流Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleFlowService {

    private final RuleFlowMapper ruleFlowMapper;
    private final RuleFlowHistoryMapper ruleFlowHistoryMapper;
    private final RuleFlowEngine ruleFlowEngine;
    private final ObjectMapper objectMapper;

    /**
     * 分页查询规则流
     */
    public IPage<RuleFlowVO> pageList(Integer page, Integer pageSize, FlowQueryDTO queryDTO) {
        Page<RuleFlow> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<RuleFlow> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getTenantId() != null) {
            wrapper.eq(RuleFlow::getTenantId, queryDTO.getTenantId());
        }
        if (queryDTO.getFlowName() != null) {
            wrapper.like(RuleFlow::getFlowName, queryDTO.getFlowName());
        }
        if (queryDTO.getCategory() != null) {
            wrapper.eq(RuleFlow::getCategory, queryDTO.getCategory());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(RuleFlow::getStatus, queryDTO.getStatus());
        }
        wrapper.eq(RuleFlow::getDeleted, 0);
        wrapper.orderByDesc(RuleFlow::getUpdateTime);

        IPage<RuleFlow> resultPage = ruleFlowMapper.selectPage(p, wrapper);

        return resultPage.convert(this::toVO);
    }

    /**
     * 根据ID获取规则流
     */
    public RuleFlowVO getById(Long id) {
        RuleFlow flow = ruleFlowMapper.selectById(id);
        if (flow == null) {
            return null;
        }
        return toVO(flow);
    }

    /**
     * 创建规则流
     */
    @Transactional
    public RuleFlow createFlow(CreateFlowDTO dto) {
        String category = dto.getCategory() != null ? dto.getCategory().toLowerCase() : "default";
        String flowKey = "flow." + category + "." + UUID.randomUUID().toString().substring(0, 8);

        RuleFlow flow = new RuleFlow();
        flow.setFlowKey(flowKey);
        flow.setFlowName(dto.getFlowName());
        flow.setCategory(category);
        flow.setDescription(dto.getDescription());
        flow.setStatus("draft");
        flow.setVersion(1);
        flow.setTenantId(dto.getTenantId());
        flow.setFlowDefinition(toFlowDefinitionJson(dto));
        flow.setCreateBy("admin"); // TODO: 从上下文获取
        flow.setCreateTime(LocalDateTime.now());

        ruleFlowMapper.insert(flow);
        log.info("规则流创建成功: flowKey={}", flowKey);
        return flow;
    }

    /**
     * 更新规则流
     */
    @Transactional
    public RuleFlow updateFlow(Long id, UpdateFlowDTO dto) {
        RuleFlow flow = ruleFlowMapper.selectById(id);
        if (flow == null) {
            throw new RuntimeException("规则流不存在: id=" + id);
        }

        // 保存历史版本
        saveHistory(flow, "更新规则流");

        // 更新
        flow.setFlowName(dto.getFlowName());
        flow.setCategory(dto.getCategory());
        flow.setDescription(dto.getDescription());
        flow.setFlowDefinition(toFlowDefinitionJson(dto));
        flow.setUpdateBy("admin");
        flow.setUpdateTime(LocalDateTime.now());

        ruleFlowMapper.updateById(flow);
        log.info("规则流更新成功: id={}", id);
        return flow;
    }

    /**
     * 删除规则流
     */
    @Transactional
    public void deleteFlow(Long id) {
        RuleFlow flow = ruleFlowMapper.selectById(id);
        if (flow == null) {
            throw new RuntimeException("规则流不存在: id=" + id);
        }

        // 逻辑删除
        flow.setDeleted(1);
        flow.setUpdateTime(LocalDateTime.now());
        ruleFlowMapper.updateById(flow);

        log.info("规则流删除成功: id={}", id);
    }

    /**
     * 发布规则流
     */
    @Transactional
    public RuleFlow publishFlow(Long id) {
        RuleFlow flow = ruleFlowMapper.selectById(id);
        if (flow == null) {
            throw new RuntimeException("规则流不存在: id=" + id);
        }

        // 校验规则流定义
        validateFlowDefinition(flow.getFlowDefinition());

        // 保存历史版本
        saveHistory(flow, "发布规则流");

        // 更新状态和版本
        flow.setStatus("active");
        flow.setVersion(flow.getVersion() + 1);
        flow.setUpdateTime(LocalDateTime.now());
        ruleFlowMapper.updateById(flow);

        log.info("规则流发布成功: id={}, version={}", id, flow.getVersion());
        return flow;
    }

    /**
     * 回滚到指定版本
     */
    @Transactional
    public RuleFlow rollback(Long id, Integer targetVersion) {
        RuleFlowHistory history = ruleFlowHistoryMapper.selectOne(
            new LambdaQueryWrapper<RuleFlowHistory>()
                .eq(RuleFlowHistory::getFlowId, id)
                .eq(RuleFlowHistory::getVersion, targetVersion)
        );

        if (history == null) {
            throw new RuntimeException("历史版本不存在: flowId=" + id + ", version=" + targetVersion);
        }

        RuleFlow flow = ruleFlowMapper.selectById(id);
        if (flow == null) {
            throw new RuntimeException("规则流不存在: id=" + id);
        }

        // 保存当前版本到历史
        saveHistory(flow, "回滚到版本 " + targetVersion);

        // 恢复到目标版本
        flow.setFlowDefinition(history.getFlowDefinition());
        flow.setVersion(targetVersion);
        flow.setStatus("active");
        flow.setUpdateTime(LocalDateTime.now());
        ruleFlowMapper.updateById(flow);

        log.info("规则流回滚成功: id={}, targetVersion={}", id, targetVersion);
        return flow;
    }

    /**
     * 获取版本历史
     */
    public List<FlowVersionVO> getVersionHistory(Long flowId) {
        List<RuleFlowHistory> histories = ruleFlowHistoryMapper.selectList(
            new LambdaQueryWrapper<RuleFlowHistory>()
                .eq(RuleFlowHistory::getFlowId, flowId)
                .orderByDesc(RuleFlowHistory::getVersion)
        );

        return histories.stream().map(h -> {
            FlowVersionVO vo = new FlowVersionVO();
            vo.setId(h.getId());
            vo.setFlowId(h.getFlowId());
            vo.setVersion(h.getVersion());
            vo.setFlowDefinition(h.getFlowDefinition());
            vo.setChangeDesc(h.getChangeDesc());
            vo.setChangeBy(h.getChangeBy());
            vo.setChangeTime(h.getChangeTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 导出规则流JSON
     */
    public String exportFlowJson(Long id) {
        RuleFlow flow = ruleFlowMapper.selectById(id);
        if (flow == null) {
            throw new RuntimeException("规则流不存在: id=" + id);
        }
        return flow.getFlowDefinition();
    }

    /**
     * 导入规则流JSON
     */
    @Transactional
    public RuleFlow importFlowJson(String flowJson, String tenantId) {
        try {
            CreateFlowDTO dto = objectMapper.readValue(flowJson, CreateFlowDTO.class);
            dto.setTenantId(tenantId);
            return createFlow(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("规则流JSON格式错误: " + e.getMessage());
        }
    }

    /**
     * 执行规则流
     */
    public String executeFlow(Long id, String factJson) {
        RuleFlow flow = ruleFlowMapper.selectById(id);
        if (flow == null) {
            throw new RuntimeException("规则流不存在: id=" + id);
        }

        if (!"active".equals(flow.getStatus())) {
            throw new RuntimeException("规则流未发布，无法执行: status=" + flow.getStatus());
        }

        // 构建执行器
        RuleFlowEngine.RuleExecutor ruleExecutor = (ruleKey, fact) -> {
            // TODO: 调用实际的规则引擎执行DRL规则
            log.info("执行规则: ruleKey={}", ruleKey);
            return fact;
        };

        RuleFlowEngine.FormulaExecutor formulaExecutor = (formulaKey, fact) -> {
            // TODO: 调用Aviator执行公式
            log.info("执行公式: formulaKey={}", formulaKey);
            return fact;
        };

        // 解析fact
        Object fact = null;
        if (factJson != null && !factJson.isBlank()) {
            try {
                fact = objectMapper.readValue(factJson, Object.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Fact JSON格式错误: " + e.getMessage());
            }
        }

        // 执行规则流
        RuleFlowEngine.ExecutionResult result = ruleFlowEngine.execute(
            flow.getFlowDefinition(), fact, ruleExecutor, formulaExecutor
        );

        // 返回执行结果JSON
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("执行结果序列化失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    private void saveHistory(RuleFlow flow, String changeDesc) {
        RuleFlowHistory history = new RuleFlowHistory();
        history.setFlowId(flow.getId());
        history.setVersion(flow.getVersion());
        history.setFlowDefinition(flow.getFlowDefinition());
        history.setChangeDesc(changeDesc);
        history.setChangeBy("admin"); // TODO: 从上下文获取
        history.setChangeTime(LocalDateTime.now());
        ruleFlowHistoryMapper.insert(history);
    }

    private void validateFlowDefinition(String flowDefinitionJson) {
        try {
            RuleFlowVO.FlowDefinitionDTO def =
                objectMapper.readValue(flowDefinitionJson, RuleFlowVO.FlowDefinitionDTO.class);

            if (def.getNodes() == null || def.getNodes().isEmpty()) {
                throw new RuntimeException("规则流至少需要一个节点");
            }

            // 检查开始和结束节点
            boolean hasStart = def.getNodes().stream().anyMatch(n -> "start".equals(n.getType()));
            boolean hasEnd = def.getNodes().stream().anyMatch(n -> "end".equals(n.getType()));

            if (!hasStart) {
                throw new RuntimeException("规则流必须有开始节点");
            }
            if (!hasEnd) {
                throw new RuntimeException("规则流必须有结束节点");
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("规则流定义格式错误: " + e.getMessage());
        }
    }

    private String toFlowDefinitionJson(Object dto) {
        try {
            Object flowDef = dto.getClass().getMethod("getFlowDefinition").invoke(dto);
            if (flowDef == null) {
                return "{\"nodes\":[],\"edges\":[]}";
            }
            return objectMapper.writeValueAsString(flowDef);
        } catch (Exception e) {
            throw new RuntimeException("规则流定义序列化失败: " + e.getMessage());
        }
    }

    private RuleFlowVO toVO(RuleFlow flow) {
        RuleFlowVO vo = new RuleFlowVO();
        vo.setId(flow.getId());
        vo.setFlowKey(flow.getFlowKey());
        vo.setFlowName(flow.getFlowName());
        vo.setCategory(flow.getCategory());
        vo.setDescription(flow.getDescription());
        vo.setStatus(flow.getStatus());
        vo.setVersion(flow.getVersion());
        vo.setTenantId(flow.getTenantId());
        vo.setCreateBy(flow.getCreateBy());
        vo.setCreateTime(flow.getCreateTime());
        vo.setUpdateBy(flow.getUpdateBy());
        vo.setUpdateTime(flow.getUpdateTime());

        // 解析flowDefinition
        if (flow.getFlowDefinition() != null) {
            try {
                vo.setFlowDefinition(
                    objectMapper.readValue(flow.getFlowDefinition(), RuleFlowVO.FlowDefinitionDTO.class)
                );
            } catch (JsonProcessingException e) {
                log.error("解析flowDefinition失败: id={}", flow.getId(), e);
            }
        }

        return vo;
    }
}