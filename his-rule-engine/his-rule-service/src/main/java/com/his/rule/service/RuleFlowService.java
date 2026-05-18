package com.his.rule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.aviator.helper.AviatorHelper;
import com.his.common.web.context.TenantContext;
import com.his.common.web.context.UserContext;
import com.his.common.web.exception.BusinessException;
import com.his.common.web.result.ErrorCode;
import com.his.rule.dto.*;
import com.his.rule.entity.RuleFlow;
import com.his.rule.entity.RuleFlowHistory;
import com.his.rule.engine.RuleFlowEngine;
import com.his.rule.engine.DroolsRuleExecutor;
import com.his.rule.feign.FormulaFeignClient;
import com.his.rule.mapper.RuleFlowMapper;
import com.his.rule.mapper.RuleFlowHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final FormulaFeignClient formulaFeignClient;
    private final DroolsRuleExecutor droolsRuleExecutor;

    /**
     * 分页查询规则流
     */
    public IPage<RuleFlowVO> pageList(Integer page, Integer pageSize, FlowQueryDTO queryDTO) {
        String tenantId = TenantContext.getTenantId("T001");
        Page<RuleFlow> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<RuleFlow> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(RuleFlow::getTenantId, tenantId);
        wrapper.like(StringUtils.hasText(queryDTO.getFlowName()), RuleFlow::getFlowName, queryDTO.getFlowName());
        wrapper.eq(StringUtils.hasText(queryDTO.getCategory()), RuleFlow::getCategory, queryDTO.getCategory());
        wrapper.eq(StringUtils.hasText(queryDTO.getStatus()), RuleFlow::getStatus, queryDTO.getStatus());
        wrapper.eq(RuleFlow::getDeleted, 0);
        wrapper.orderByDesc(RuleFlow::getUpdateTime);

        // 只查询必要字段，避免加载flowDefinition
        wrapper.select(RuleFlow.class, v -> !v.getProperty().equals("flowDefinition"));

        IPage<RuleFlow> resultPage = ruleFlowMapper.selectPage(p, wrapper);

        return resultPage.convert(flow -> toVO(flow, false));
    }

    /**
     * 根据ID获取规则流
     */
    public RuleFlowVO getById(Long id) {
        RuleFlow flow = ruleFlowMapper.selectById(id);
        if (flow == null) {
            return null;
        }
        return toVO(flow, true);
    }

    /**
     * 规则流下拉列表（轻量级，不含flowDefinition）
     */
    public List<RuleFlowVO> listOptions(String category, String status) {
        String tenantId = TenantContext.getTenantId("T001");
        LambdaQueryWrapper<RuleFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleFlow::getTenantId, tenantId);
        wrapper.eq(RuleFlow::getDeleted, 0);
        wrapper.eq(StringUtils.hasText(category), RuleFlow::getCategory, category);
        wrapper.eq(StringUtils.hasText(status), RuleFlow::getStatus, status);
        wrapper.orderByDesc(RuleFlow::getUpdateTime);
        wrapper.select(RuleFlow.class, v -> !v.getProperty().equals("flowDefinition"));

        List<RuleFlow> flows = ruleFlowMapper.selectList(wrapper);
        return flows.stream().map(f -> toVO(f, false)).collect(Collectors.toList());
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
        flow.setCreateBy(UserContext.getUserId("admin"));
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
            throw new BusinessException(ErrorCode.RULE_FLOW_NOT_FOUND, id);
        }

        // 保存历史版本
        saveHistory(flow, "更新规则流");

        // 更新
        flow.setFlowName(dto.getFlowName());
        flow.setCategory(dto.getCategory());
        flow.setDescription(dto.getDescription());
        flow.setFlowDefinition(toFlowDefinitionJson(dto));
        flow.setUpdateBy(UserContext.getUserId("admin"));
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
            throw new BusinessException(ErrorCode.RULE_FLOW_NOT_FOUND, id);
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
            throw new BusinessException(ErrorCode.RULE_FLOW_NOT_FOUND, id);
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
            throw new BusinessException(ErrorCode.RULE_FLOW_VERSION_NOT_FOUND, id);
        }

        RuleFlow flow = ruleFlowMapper.selectById(id);
        if (flow == null) {
            throw new BusinessException(ErrorCode.RULE_FLOW_NOT_FOUND, id);
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
     * 对比两个版本的规则流差异
     */
    public FlowCompareVO compareVersions(Long flowId, Integer fromVersion, Integer toVersion) {
        RuleFlowHistory fromHistory = ruleFlowHistoryMapper.selectOne(
            new LambdaQueryWrapper<RuleFlowHistory>()
                .eq(RuleFlowHistory::getFlowId, flowId)
                .eq(RuleFlowHistory::getVersion, fromVersion)
        );

        RuleFlowHistory toHistory = ruleFlowHistoryMapper.selectOne(
            new LambdaQueryWrapper<RuleFlowHistory>()
                .eq(RuleFlowHistory::getFlowId, flowId)
                .eq(RuleFlowHistory::getVersion, toVersion)
        );

        if (fromHistory == null) {
            throw new BusinessException(ErrorCode.RULE_FLOW_VERSION_NOT_FOUND, flowId);
        }
        if (toHistory == null) {
            throw new BusinessException(ErrorCode.RULE_FLOW_VERSION_NOT_FOUND, flowId);
        }

        FlowCompareVO result = new FlowCompareVO();
        result.setFlowId(flowId);
        result.setFromVersion(fromVersion);
        result.setToVersion(toVersion);

        try {
            RuleFlowVO.FlowDefinitionDTO fromDef =
                objectMapper.readValue(fromHistory.getFlowDefinition(), RuleFlowVO.FlowDefinitionDTO.class);
            RuleFlowVO.FlowDefinitionDTO toDef =
                objectMapper.readValue(toHistory.getFlowDefinition(), RuleFlowVO.FlowDefinitionDTO.class);

            // 对比节点
            List<FlowCompareVO.NodeDiff> nodeDiffs = compareNodes(fromDef, toDef);
            List<FlowCompareVO.EdgeDiff> edgeDiffs = compareEdges(fromDef, toDef);

            result.setNodeDiffs(nodeDiffs);
            result.setEdgeDiffs(edgeDiffs);
            result.setHasDiff(!nodeDiffs.isEmpty() || !edgeDiffs.isEmpty());

        } catch (JsonProcessingException e) {
            throw new RuntimeException("规则流定义解析失败: " + e.getMessage());
        }

        return result;
    }

    private List<FlowCompareVO.NodeDiff> compareNodes(RuleFlowVO.FlowDefinitionDTO from, RuleFlowVO.FlowDefinitionDTO to) {
        List<FlowCompareVO.NodeDiff> diffs = new java.util.ArrayList<>();

        // 构建节点映射
        Map<String, RuleFlowVO.NodeDTO> fromNodeMap = from.getNodes().stream()
            .collect(java.util.stream.Collectors.toMap(RuleFlowVO.NodeDTO::getNodeId, n -> n));
        Map<String, RuleFlowVO.NodeDTO> toNodeMap = to.getNodes().stream()
            .collect(java.util.stream.Collectors.toMap(RuleFlowVO.NodeDTO::getNodeId, n -> n));

        // 检查删除和修改的节点
        for (Map.Entry<String, RuleFlowVO.NodeDTO> entry : fromNodeMap.entrySet()) {
            RuleFlowVO.NodeDTO fromNode = entry.getValue();
            RuleFlowVO.NodeDTO toNode = toNodeMap.get(entry.getKey());

            if (toNode == null) {
                FlowCompareVO.NodeDiff diff = new FlowCompareVO.NodeDiff();
                diff.setNodeId(fromNode.getNodeId());
                diff.setNodeLabel(fromNode.getLabel());
                diff.setChangeType("REMOVED");
                diff.setFromContent(toJson(fromNode));
                diffs.add(diff);
            } else if (!toJson(fromNode).equals(toJson(toNode))) {
                FlowCompareVO.NodeDiff diff = new FlowCompareVO.NodeDiff();
                diff.setNodeId(fromNode.getNodeId());
                diff.setNodeLabel(fromNode.getLabel());
                diff.setChangeType("MODIFIED");
                diff.setFromContent(toJson(fromNode));
                diff.setToContent(toJson(toNode));
                diffs.add(diff);
            }
        }

        // 检查新增的节点
        for (Map.Entry<String, RuleFlowVO.NodeDTO> entry : toNodeMap.entrySet()) {
            if (!fromNodeMap.containsKey(entry.getKey())) {
                FlowCompareVO.NodeDiff diff = new FlowCompareVO.NodeDiff();
                diff.setNodeId(entry.getValue().getNodeId());
                diff.setNodeLabel(entry.getValue().getLabel());
                diff.setChangeType("ADDED");
                diff.setToContent(toJson(entry.getValue()));
                diffs.add(diff);
            }
        }

        return diffs;
    }

    private List<FlowCompareVO.EdgeDiff> compareEdges(RuleFlowVO.FlowDefinitionDTO from, RuleFlowVO.FlowDefinitionDTO to) {
        List<FlowCompareVO.EdgeDiff> diffs = new java.util.ArrayList<>();

        // 构建边映射 (source_target 唯一标识)
        Map<String, RuleFlowVO.EdgeDTO> fromEdgeMap = from.getEdges().stream()
            .collect(java.util.stream.Collectors.toMap(
                e -> e.getSource() + "_" + e.getTarget(), e -> e));
        Map<String, RuleFlowVO.EdgeDTO> toEdgeMap = to.getEdges().stream()
            .collect(java.util.stream.Collectors.toMap(
                e -> e.getSource() + "_" + e.getTarget(), e -> e));

        // 检查删除和修改的边
        for (Map.Entry<String, RuleFlowVO.EdgeDTO> entry : fromEdgeMap.entrySet()) {
            RuleFlowVO.EdgeDTO fromEdge = entry.getValue();
            RuleFlowVO.EdgeDTO toEdge = toEdgeMap.get(entry.getKey());

            if (toEdge == null) {
                FlowCompareVO.EdgeDiff diff = new FlowCompareVO.EdgeDiff();
                diff.setEdgeId(entry.getKey());
                diff.setChangeType("REMOVED");
                diff.setFromContent(toJson(fromEdge));
                diffs.add(diff);
            } else if (!equals(fromEdge, toEdge)) {
                FlowCompareVO.EdgeDiff diff = new FlowCompareVO.EdgeDiff();
                diff.setEdgeId(entry.getKey());
                diff.setChangeType("MODIFIED");
                diff.setFromContent(toJson(fromEdge));
                diff.setToContent(toJson(toEdge));
                diffs.add(diff);
            }
        }

        // 检查新增的边
        for (Map.Entry<String, RuleFlowVO.EdgeDTO> entry : toEdgeMap.entrySet()) {
            if (!fromEdgeMap.containsKey(entry.getKey())) {
                FlowCompareVO.EdgeDiff diff = new FlowCompareVO.EdgeDiff();
                diff.setEdgeId(entry.getKey());
                diff.setChangeType("ADDED");
                diff.setToContent(toJson(entry.getValue()));
                diffs.add(diff);
            }
        }

        return diffs;
    }

    private boolean equals(RuleFlowVO.EdgeDTO a, RuleFlowVO.EdgeDTO b) {
        if (a == null || b == null) return false;
        boolean sourceEq = a.getSource() == null ? b.getSource() == null : a.getSource().equals(b.getSource());
        boolean targetEq = a.getTarget() == null ? b.getTarget() == null : a.getTarget().equals(b.getTarget());
        boolean labelEq = a.getLabel() == null ? b.getLabel() == null : a.getLabel().equals(b.getLabel());
        return sourceEq && targetEq && labelEq;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }

    /**
     * 导出规则流JSON
     */
    public String exportFlowJson(Long id) {
        RuleFlow flow = ruleFlowMapper.selectById(id);
        if (flow == null) {
            throw new BusinessException(ErrorCode.RULE_FLOW_NOT_FOUND, id);
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
    public RuleFlowEngine.ExecutionResult executeFlow(Long id, String factJson) {
        RuleFlow flow = ruleFlowMapper.selectById(id);
        if (flow == null) {
            throw new RuntimeException("规则流不存在: id=" + id);
        }

        if (!"active".equals(flow.getStatus())) {
            throw new RuntimeException("规则流未发布，无法执行: status=" + flow.getStatus());
        }

        // 构建执行器
        RuleFlowEngine.RuleExecutor ruleExecutor = (ruleKey, fact) -> {
            // 调用 Drools 执行 DRL 规则
            log.info("执行规则: ruleKey={}", ruleKey);
            return droolsRuleExecutor.execute(ruleKey, fact);
        };

        RuleFlowEngine.FormulaExecutor formulaExecutor = (formulaKey, fact) -> {
            // 调用 his-formula-service 获取公式并执行
            log.info("执行公式: formulaKey={}", formulaKey);
            try {
                // 1. 获取公式详情
                var formulaResult = formulaFeignClient.getByKey(formulaKey);
                if (formulaResult == null || !"0".equals(formulaResult.getCode()) || formulaResult.getData() == null) {
                    log.error("公式不存在或获取失败: formulaKey={}", formulaKey);
                    return fact;
                }

                // 2. 获取公式表达式
                Map<String, Object> formulaData = formulaResult.getData();
                String expression = (String) formulaData.get("formulaText");
                if (expression == null || expression.isBlank()) {
                    log.error("公式表达式为空: formulaKey={}", formulaKey);
                    return fact;
                }

                // 3. 克隆 fact 以避免修改原对象
                Map<String, Object> env = new java.util.LinkedHashMap<>();
                if (fact instanceof Map) {
                    env.putAll((Map<String, Object>) fact);
                }

                // 4. 使用 Aviator 执行公式
                Object result = AviatorHelper.execute(expression, env);
                log.info("公式执行完成: formulaKey={}, result={}", formulaKey, result);

                // 5. 返回计算结果（不是整个fact）
                return result;

            } catch (Exception e) {
                log.error("公式执行异常: formulaKey={}, error={}", formulaKey, e.getMessage(), e);
                return fact;
            }
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
        return ruleFlowEngine.execute(
            flow.getFlowDefinition(), fact, ruleExecutor, formulaExecutor
        );
    }

    // ==================== 私有方法 ====================

    private void saveHistory(RuleFlow flow, String changeDesc) {
        RuleFlowHistory history = new RuleFlowHistory();
        history.setFlowId(flow.getId());
        history.setVersion(flow.getVersion());
        history.setFlowDefinition(flow.getFlowDefinition());
        history.setChangeDesc(changeDesc);
        history.setChangeBy(UserContext.getUserId("admin"));
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
        return toVO(flow, true);
    }

    /**
     * 转换为VO
     * @param flow 规则流实体
     * @param parseDefinition 是否解析flowDefinition（列表查询时可设为false提升性能）
     */
    private RuleFlowVO toVO(RuleFlow flow, boolean parseDefinition) {
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

        // 仅在详情查询时解析flowDefinition
        if (parseDefinition && flow.getFlowDefinition() != null) {
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