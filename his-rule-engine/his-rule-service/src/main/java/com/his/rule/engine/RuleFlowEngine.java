package com.his.rule.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.SettlementFact;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 规则流执行引擎
 * 支持拓扑排序执行、条件分支、规则/公式调用
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RuleFlowEngine {

    private final ObjectMapper objectMapper;

    /**
     * 执行规则流
     *
     * @param flowDefinitionJson 规则流定义JSON
     * @param fact 输入的Fact对象
     * @param ruleExecutor 规则执行器（可用于调用DRL规则）
     * @param formulaExecutor 公式执行器（可用于调用Aviator公式）
     * @return 执行结果（包含执行路径和节点结果）
     */
    public ExecutionResult execute(String flowDefinitionJson, Object fact,
                                    RuleExecutor ruleExecutor, FormulaExecutor formulaExecutor) {
        ExecutionResult result = new ExecutionResult();
        result.setSuccess(true);
        result.setNodeResults(new ArrayList<>());

        try {
            // 解析规则流定义
            FlowDefinition flow = objectMapper.readValue(flowDefinitionJson, FlowDefinition.class);

            // 构建节点映射
            Map<String, FlowNode> nodeMap = flow.getNodes().stream()
                .collect(Collectors.toMap(FlowNode::getNodeId, n -> n));

            // 查找开始节点
            FlowNode startNode = flow.getNodes().stream()
                .filter(n -> "start".equals(n.getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("规则流缺少开始节点"));

            // 记录执行路径
            result.setExecutionPath(new ArrayList<>());
            result.getExecutionPath().add(startNode.getNodeId());

            // 检测并处理循环依赖
            List<String> sortedNodes = topologicalSort(flow);
            log.info("规则流拓扑排序结果: {}", sortedNodes);

            // 从开始节点执行
            executeNode(startNode.getNodeId(), nodeMap, flow.getEdges(), fact, ruleExecutor, formulaExecutor, result);

        } catch (Exception e) {
            log.error("规则流执行失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    /**
     * 执行单个节点
     */
    private Object executeNode(String nodeId, Map<String, FlowNode> nodeMap,
                               List<FlowEdge> edges,
                               Object fact, RuleExecutor ruleExecutor,
                               FormulaExecutor formulaExecutor, ExecutionResult result) {
        FlowNode node = nodeMap.get(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("节点不存在: " + nodeId);
        }

        NodeResult nodeResult = new NodeResult();
        nodeResult.setNodeId(nodeId);
        nodeResult.setNodeType(node.getType());
        nodeResult.setNodeLabel(node.getLabel());
        long startTime = System.currentTimeMillis();
        Object nodeOutput = null;

        try {
            nodeResult.setSuccess(true);
            nodeResult.setOutput(nodeOutput);
            // 先添加到列表，供 condition 等节点在执行过程中查找
            result.getNodeResults().add(nodeResult);

            nodeOutput = switch (node.getType()) {
                case "start" -> executeStartNode(node, fact);
                case "end" -> executeEndNode(node, fact);
                case "condition" -> executeConditionNode(node, nodeMap, edges, fact, ruleExecutor, formulaExecutor, result);
                case "action" -> executeActionNode(node, fact, ruleExecutor);
                case "formula" -> {
                    // 获取结果字段名
                    String resultField = node.getResultField();
                    FormulaResult fr = executeFormulaNode(node, fact, formulaExecutor);
                    // 如果指定了结果字段，将结果写入 updatedFact
                    if (resultField != null && fr.result() != null && fr.updatedFact() instanceof Map) {
                        ((Map<String, Object>) fr.updatedFact()).put(resultField, fr.result());
                        log.info("公式结果写入字段: field={}, value={}", resultField, fr.result());
                    }
                    yield fr.updatedFact();
                }
                case "subflow" -> executeSubflowNode(node, fact, ruleExecutor, formulaExecutor);
                default -> throw new IllegalArgumentException("不支持的节点类型: " + node.getType());
            };

            nodeResult.setSuccess(true);
            nodeResult.setOutput(nodeOutput);

            // 非条件节点执行完成后，自动查找下一个节点
            if (!"condition".equals(node.getType()) && !"end".equals(node.getType())) {
                String nextNodeId = findNextNode(nodeId, edges);
                if (nextNodeId != null && !result.getExecutionPath().contains(nextNodeId)) {
                    result.getExecutionPath().add(nextNodeId);
                    executeNode(nextNodeId, nodeMap, edges, nodeOutput, ruleExecutor, formulaExecutor, result);
                }
            }

        } catch (Exception e) {
            log.error("节点执行失败: nodeId={}", nodeId, e);
            nodeResult.setSuccess(false);
            nodeResult.setErrorMessage(e.getMessage());
            result.getNodeResults().add(nodeResult);
            result.setSuccess(false);
            result.setErrorMessage("节点[" + node.getLabel() + "]执行失败: " + e.getMessage());
        }

        nodeResult.setDurationMs(System.currentTimeMillis() - startTime);
        return nodeResult.isSuccess() ? nodeOutput : fact;
    }

    /**
     * 查找下一个节点（无条件）
     */
    private String findNextNode(String currentNodeId, List<FlowEdge> edges) {
        if (edges == null || edges.isEmpty()) {
            return null;
        }
        for (FlowEdge edge : edges) {
            if (currentNodeId.equals(edge.getSource())) {
                return edge.getTarget();
            }
        }
        return null;
    }

    /**
     * 根据条件结果查找下一个节点
     * 边的 label 为 "true" 或 "false"，对应条件表达式结果
     */
    private String findNextNodeByCondition(String nodeId, List<FlowEdge> edges, boolean conditionResult) {
        if (edges == null || edges.isEmpty()) {
            return null;
        }
        String targetLabel = conditionResult ? "true" : "false";
        for (FlowEdge edge : edges) {
            if (nodeId.equals(edge.getSource()) && targetLabel.equals(edge.getLabel())) {
                return edge.getTarget();
            }
        }
        // 如果没有匹配的 label，返回第一条边（默认路径）
        for (FlowEdge edge : edges) {
            if (nodeId.equals(edge.getSource())) {
                return edge.getTarget();
            }
        }
        return null;
    }

    /**
     * 执行开始节点
     */
    private Object executeStartNode(FlowNode node, Object fact) {
        log.info("执行开始节点: {}", node.getNodeId());
        return fact;
    }

    /**
     * 执行结束节点
     */
    private Object executeEndNode(FlowNode node, Object fact) {
        log.info("执行结束节点: {}", node.getNodeId());
        return fact;
    }

    /**
     * 执行条件节点
     */
    private Object executeConditionNode(FlowNode node, Map<String, FlowNode> nodeMap,
                                        List<FlowEdge> edges,
                                        Object fact, RuleExecutor ruleExecutor,
                                        FormulaExecutor formulaExecutor, ExecutionResult result) {
        log.info("执行条件节点: {}, expression={}", node.getNodeId(), node.getExpression());

        // 解析条件表达式
        boolean conditionResult = evaluateCondition(node.getExpression(), fact);

        // 获取分支 - 优先使用边的 label 来判断
        String nextNodeId = findNextNodeByCondition(node.getNodeId(), edges, conditionResult);

        NodeResult nodeResult = result.getNodeResults().stream()
            .filter(r -> r.getNodeId().equals(node.getNodeId()))
            .findFirst()
            .orElseThrow();
        nodeResult.setConditionResult(conditionResult);

        if (nextNodeId == null) {
            log.warn("条件节点[{}]没有后续节点", node.getNodeId());
            return fact;
        }

        // 添加到执行路径
        result.getExecutionPath().add(nextNodeId);

        // 执行下一个节点
        return executeNode(nextNodeId, nodeMap, edges, fact, ruleExecutor, formulaExecutor, result);
    }

    /**
     * 执行动作节点（调用DRL规则）
     */
    private Object executeActionNode(FlowNode node, Object fact, RuleExecutor ruleExecutor) {
        String ruleKey = node.getRuleKey();
        log.info("执行动作节点: {}, ruleKey={}", node.getNodeId(), ruleKey);

        if (ruleExecutor == null) {
            log.warn("规则执行器为空，跳过规则执行");
            return fact;
        }

        return ruleExecutor.execute(ruleKey, fact);
    }

    /**
     * 执行公式节点（调用Aviator公式）
     */
    private FormulaResult executeFormulaNode(FlowNode node, Object fact, FormulaExecutor formulaExecutor) {
        String formulaKey = node.getFormulaKey();
        log.info("执行公式节点: {}, formulaKey={}", node.getNodeId(), formulaKey);

        if (formulaExecutor == null) {
            log.warn("公式执行器为空，跳过公式执行");
            return new FormulaResult(null, fact);
        }

        Object result = formulaExecutor.execute(formulaKey, fact);

        // 如果指定了 resultField，创建 shallow copy 写入结果，避免序列化时自引用
        String resultField = node.getResultField();
        Object updatedFact = fact;
        if (resultField != null && result != null && fact instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> originalMap = (Map<String, Object>) fact;
            Map<String, Object> clonedMap = new java.util.LinkedHashMap<>(originalMap);
            clonedMap.put(resultField, result);
            updatedFact = clonedMap;
            log.info("公式结果写入字段: field={}, value={}", resultField, result);
        }

        return new FormulaResult(result, updatedFact);
    }

    /**
     * 公式执行结果
     */
    public record FormulaResult(Object result, Object updatedFact) {}

    /**
     * 执行子流程节点
     */
    private Object executeSubflowNode(FlowNode node, Object fact,
                                      RuleExecutor ruleExecutor, FormulaExecutor formulaExecutor) {
        String subFlowId = node.getSubFlowId();
        log.info("执行子流程节点: {}, subFlowId={}", node.getNodeId(), subFlowId);
        // TODO: 递归执行子流程
        return fact;
    }

    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(String expression, Object fact) {
        if (expression == null || expression.isBlank()) {
            return true;
        }

        try {
            // 使用SpEL或者简单的表达式评估
            // 这里简化为直接解析 expression 格式: fact.field == value
            if (fact instanceof SettlementFact sf) {
                // 支持简单的表达式如 "patientType == 'RESIDENT'"
                return evaluateSettlementCondition(expression, sf);
            }
            return true;
        } catch (Exception e) {
            log.error("条件表达式评估失败: expression={}", expression, e);
            return false;
        }
    }

    private boolean evaluateSettlementCondition(String expression, SettlementFact fact) {
        // 简单的表达式解析
        // 格式: field op value, 例如: patientType == 'RESIDENT' 或 totalFee > 1000
        try {
            String[] parts = expression.split("\\s+");
            if (parts.length < 3) return true;

            String field = parts[0];
            String op = parts[1];
            String value = String.join("", Arrays.copyOfRange(parts, 2, parts.length)).replace("'", "");

            Object fieldValue = switch (field) {
                case "patientType" -> fact.getPatientType();
                case "totalFee" -> fact.getTotalFee();
                case "deductible" -> fact.getDeductible();
                default -> null;
            };

            if (fieldValue == null) return false;

            if (fieldValue instanceof String sfValue) {
                return op.equals("==") && sfValue.equals(value);
            } else if (fieldValue instanceof BigDecimal bdValue) {
                BigDecimal numValue = new BigDecimal(value);
                int cmp = bdValue.compareTo(numValue);
                return switch (op) {
                    case ">" -> cmp > 0;
                    case "<" -> cmp < 0;
                    case ">=" -> cmp >= 0;
                    case "<=" -> cmp <= 0;
                    case "==" -> cmp == 0;
                    default -> false;
                };
            }

            return false;
        } catch (Exception e) {
            log.error("条件解析失败: {}", expression, e);
            return false;
        }
    }

    /**
     * 拓扑排序（用于检测循环依赖）
     */
    private List<String> topologicalSort(FlowDefinition flow) {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adjacency = new HashMap<>();

        // 初始化
        for (FlowNode node : flow.getNodes()) {
            inDegree.put(node.getNodeId(), 0);
            adjacency.put(node.getNodeId(), new ArrayList<>());
        }

        // 构建图和入度
        for (FlowEdge edge : flow.getEdges()) {
            adjacency.get(edge.getSource()).add(edge.getTarget());
            inDegree.merge(edge.getTarget(), 1, Integer::sum);
        }

        // Kahn算法
        Queue<String> queue = new LinkedList<>();
        for (String nodeId : inDegree.keySet()) {
            if (inDegree.get(nodeId) == 0) {
                queue.offer(nodeId);
            }
        }

        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            result.add(nodeId);
            for (String neighbor : adjacency.get(nodeId)) {
                inDegree.merge(neighbor, -1, Integer::sum);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // 检测循环
        if (result.size() != flow.getNodes().size()) {
            throw new IllegalStateException("规则流存在循环依赖");
        }

        return result;
    }

    // ==================== 内部类 ====================

    @Data
    public static class FlowDefinition {
        private List<FlowNode> nodes;
        private List<FlowEdge> edges;
    }

    @Data
    public static class FlowNode {
        private String nodeId;
        private String type;
        private String label;
        private String expression;
        private Map<String, String> branches;
        private String ruleKey;
        private String formulaKey;
        private String subFlowId;
        private Integer timeout;
        private String resultField;  // 公式/规则结果写入的字段名
        private Map<String, Object> params;  // 节点参数
    }

    @Data
    public static class FlowEdge {
        private String source;
        private String target;
        private String label;
    }

    @Data
    public static class ExecutionResult {
        private boolean success;
        private String errorMessage;
        private List<String> executionPath;
        private List<NodeResult> nodeResults;
    }

    @Data
    public static class NodeResult {
        private String nodeId;
        private String nodeType;
        private String nodeLabel;
        private boolean success;
        private Object output;
        private String errorMessage;
        private Boolean conditionResult;
        private Map<String, String> branches;
        private long durationMs;
    }

    // ==================== 接口 ====================

    @FunctionalInterface
    public interface RuleExecutor {
        Object execute(String ruleKey, Object fact);
    }

    @FunctionalInterface
    public interface FormulaExecutor {
        Object execute(String formulaKey, Object fact);
    }
}