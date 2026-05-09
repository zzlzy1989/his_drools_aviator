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
            executeNode(startNode.getNodeId(), nodeMap, fact, ruleExecutor, formulaExecutor, result);

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
            nodeOutput = switch (node.getType()) {
                case "start" -> executeStartNode(node, fact);
                case "end" -> executeEndNode(node, fact);
                case "condition" -> executeConditionNode(node, nodeMap, fact, ruleExecutor, formulaExecutor, result);
                case "action" -> executeActionNode(node, fact, ruleExecutor);
                case "formula" -> executeFormulaNode(node, fact, formulaExecutor);
                case "subflow" -> executeSubflowNode(node, fact, ruleExecutor, formulaExecutor);
                default -> throw new IllegalArgumentException("不支持的节点类型: " + node.getType());
            };

            nodeResult.setSuccess(true);
            nodeResult.setOutput(nodeOutput);
            result.getNodeResults().add(nodeResult);

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
                                        Object fact, RuleExecutor ruleExecutor,
                                        FormulaExecutor formulaExecutor, ExecutionResult result) {
        log.info("执行条件节点: {}, expression={}", node.getNodeId(), node.getExpression());

        // 解析条件表达式
        boolean conditionResult = evaluateCondition(node.getExpression(), fact);

        // 获取分支
        Map<String, String> branches = node.getBranches();
        String nextNodeId = conditionResult ?
            (branches != null ? branches.get("true") : null) :
            (branches != null ? branches.get("false") : null);

        NodeResult nodeResult = result.getNodeResults().stream()
            .filter(r -> r.getNodeId().equals(node.getNodeId()))
            .findFirst()
            .orElseThrow();
        nodeResult.setBranches(branches);
        nodeResult.setConditionResult(conditionResult);

        if (nextNodeId == null) {
            log.warn("条件节点[{}]没有后续节点", node.getNodeId());
            return fact;
        }

        // 添加到执行路径
        result.getExecutionPath().add(nextNodeId);

        // 执行下一个节点
        return executeNode(nextNodeId, nodeMap, fact, ruleExecutor, formulaExecutor, result);
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
    private Object executeFormulaNode(FlowNode node, Object fact, FormulaExecutor formulaExecutor) {
        String formulaKey = node.getFormulaKey();
        log.info("执行公式节点: {}, formulaKey={}", node.getNodeId(), formulaKey);

        if (formulaExecutor == null) {
            log.warn("公式执行器为空，跳过公式执行");
            return fact;
        }

        return formulaExecutor.execute(formulaKey, fact);
    }

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