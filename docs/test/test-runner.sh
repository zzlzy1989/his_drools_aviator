#!/bin/bash
# HIS API 接口测试脚本 - 已修复版本
# 测试所有实际运行的接口，验证响应并记录结果

BASE_URL="http://localhost:9000"
PASS_COUNT=0
FAIL_COUNT=0
FAIL_DETAILS=""
TEST_NUM=0

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 测试函数
test_api() {
    local desc="$1"
    local method="$2"
    local url="$3"
    local body="$4"
    local expect_code="${5:-200}"
    
    TEST_NUM=$((TEST_NUM + 1))
    
    if [ -n "$body" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$body" 2>/dev/null)
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" 2>/dev/null)
    fi
    
    http_code=$(echo "$response" | tail -1)
    resp_body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expect_code" ]; then
        echo -e "${GREEN}[PASS]${NC} TC-$TEST_NUM: $desc (HTTP $http_code)"
        PASS_COUNT=$((PASS_COUNT + 1))
    else
        echo -e "${RED}[FAIL]${NC} TC-$TEST_NUM: $desc (期望HTTP $expect_code, 实际HTTP $http_code)"
        echo "  URL: $method $url"
        echo "  响应: $(echo "$resp_body" | head -c 200)"
        FAIL_COUNT=$((FAIL_COUNT + 1))
        FAIL_DETAILS="$FAIL_DETAILS\nTC-$TEST_NUM: $desc - 期望HTTP $expect_code, 实际HTTP $http_code - $method $url"
    fi
}

echo "======================================"
echo "HIS API 接口自动化测试"
echo "基础URL: $BASE_URL"
echo "开始时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "======================================"
echo ""

# ====== 模块1: 规则组管理 ======
echo -e "${YELLOW}=== 01. 规则组管理 (Rule Groups) ===${NC}"

test_api "获取启用规则组列表" "GET" "$BASE_URL/api/v1/rule-groups" "" "200"

test_api "规则组分页列表" "GET" "$BASE_URL/api/v1/rule-groups/page?page=1&pageSize=10" "" "200"

test_api "创建规则组" "POST" "$BASE_URL/api/v1/rule-groups" \
    '{"groupCode":"TEST_GROUP_001","groupName":"测试规则组","description":"接口测试"}' "200"

test_api "创建规则组-缺少必填字段" "POST" "$BASE_URL/api/v1/rule-groups" \
    '{"description":"缺少必填"}' "500"

test_api "获取规则组详情-ID1" "GET" "$BASE_URL/api/v1/rule-groups/1" "" "200"

test_api "获取规则组详情-不存在的ID" "GET" "$BASE_URL/api/v1/rule-groups/999999" "" "200"

test_api "更新规则组" "PUT" "$BASE_URL/api/v1/rule-groups/1" \
    '{"groupName":"更新后的名称","description":"更新描述"}' "200"

test_api "启用规则组" "PUT" "$BASE_URL/api/v1/rule-groups/1/enabled?enabled=true" "" "200"

test_api "禁用规则组" "PUT" "$BASE_URL/api/v1/rule-groups/1/enabled?enabled=false" "" "200"

echo ""

# ====== 模块2: 规则定义管理 ======
echo -e "${YELLOW}=== 02. 规则定义管理 (Rule Definitions) ===${NC}"

test_api "规则分页列表" "GET" "$BASE_URL/api/v1/rules?page=1&pageSize=10" "" "200"

test_api "按条件筛选规则" "GET" "$BASE_URL/api/v1/rules?page=1&pageSize=10&status=0" "" "200"

test_api "获取规则详情-ID1" "GET" "$BASE_URL/api/v1/rules/1" "" "200"

test_api "更新规则" "PUT" "$BASE_URL/api/v1/rules/1" \
    '{"ruleName":"更新后的名称","description":"更新描述"}' "200"

test_api "校验规则" "POST" "$BASE_URL/api/v1/rules/1/validate" "" "200"

test_api "发布规则" "POST" "$BASE_URL/api/v1/rules/1/publish" "" "200"

test_api "删除规则-ID999999" "DELETE" "$BASE_URL/api/v1/rules/999999" "" "200"

echo ""

# ====== 模块3: 规则流管理 ======
echo -e "${YELLOW}=== 03. 规则流管理 (Rule Flows) ===${NC}"

test_api "规则流分页列表" "GET" "$BASE_URL/api/v1/flows?page=1&pageSize=10" "" "200"

test_api "规则流下拉选项" "GET" "$BASE_URL/api/v1/flows/options" "" "200"

test_api "创建规则流" "POST" "$BASE_URL/api/v1/flows" \
    '{"flowName":"医保结算流程","category":"SETTLEMENT","description":"测试流程","flowDefinition":{"nodes":[{"nodeId":"n1","type":"start","label":"开始","x":100,"y":100},{"nodeId":"n2","type":"end","label":"结束","x":200,"y":200}],"edges":[]}}' "200"

test_api "获取规则流详情-ID1" "GET" "$BASE_URL/api/v1/flows/1" "" "200"

test_api "更新规则流" "PUT" "$BASE_URL/api/v1/flows/1" \
    '{"flowName":"更新后的流程名","description":"更新描述"}' "200"

test_api "获取版本历史" "GET" "$BASE_URL/api/v1/flows/1/versions" "" "200"

test_api "导出规则流" "GET" "$BASE_URL/api/v1/flows/1/export" "" "200"

test_api "执行规则流" "POST" "$BASE_URL/api/v1/flows/1/execute" \
    '{"visitId":"V001","patientId":"P001","patientType":"employee","totalFee":5000}' "200"

echo ""

# ====== 模块4: 公式管理 ======
echo -e "${YELLOW}=== 04. 公式管理 (Formulas) ===${NC}"

test_api "公式分页列表" "GET" "$BASE_URL/api/v1/formulas?page=1&pageSize=10" "" "200"

test_api "获取公式详情-ID1" "GET" "$BASE_URL/api/v1/formulas/1" "" "200"

test_api "根据Key获取公式" "GET" "$BASE_URL/api/v1/formulas/key/formula.reimburse.resident" "" "200"

test_api "创建公式" "POST" "$BASE_URL/api/v1/formulas" \
    '{"formulaKey":"formula.reimburse.resident_test","formulaName":"居民医保报销","formulaText":"round((totalFee - deductible) * ratio, 2)","category":"REIMBURSE","description":"测试公式"}' "200"

test_api "更新公式" "PUT" "$BASE_URL/api/v1/formulas/1" \
    '{"formulaName":"更新后的名称","description":"更新描述"}' "200"

test_api "校验公式" "POST" "$BASE_URL/api/v1/formulas/1/validate" "" "200"

test_api "获取版本历史" "GET" "$BASE_URL/api/v1/formulas/1/versions" "" "200"

test_api "发布公式" "POST" "$BASE_URL/api/v1/formulas/1/publish" "" "200"

test_api "回滚公式" "POST" "$BASE_URL/api/v1/formulas/1/rollback?version=1" "" "200"

test_api "获取缓存统计" "GET" "$BASE_URL/api/v1/formulas/cache/stats" "" "200"

test_api "刷新缓存" "POST" "$BASE_URL/api/v1/formulas/cache/refresh" "" "200"

test_api "删除公式-ID999999" "DELETE" "$BASE_URL/api/v1/formulas/999999" "" "200"

echo ""

# ====== 模块5: 结算管理 ======
echo -e "${YELLOW}=== 05. 结算管理 (Settlements) ===${NC}"

test_api "执行结算" "POST" "$BASE_URL/api/v1/settlements" \
    '{"visitId":"V20260517001","patientId":"P001","patientType":"employee","insuranceType":"basic","hospitalLevel":"三级","totalFee":10000}' "200"

test_api "结算-金额为零" "POST" "$BASE_URL/api/v1/settlements" \
    '{"visitId":"V20260517002","patientId":"P002","patientType":"employee","totalFee":0}' "200"

test_api "结算分页列表" "GET" "$BASE_URL/api/v1/settlements?page=1&pageSize=10" "" "200"

test_api "获取缓存刷新" "POST" "$BASE_URL/api/v1/settlements/cache/refresh?tenantId=demo&formulaKey=test" "" "200"

test_api "清除缓存" "POST" "$BASE_URL/api/v1/settlements/cache/clear" "" "200"

test_api "强制刷新" "POST" "$BASE_URL/api/v1/settlements/refresh" "" "200"

test_api "更新结算" "PUT" "$BASE_URL/api/v1/settlements/1" \
    '{"patientType":"resident"}' "200"

test_api "删除结算-ID999999" "DELETE" "$BASE_URL/api/v1/settlements/999999" "" "200"

echo ""

# ====== 模块6: 沙箱测试 ======
echo -e "${YELLOW}=== 06. 沙箱测试 (Sandbox) ===${NC}"

test_api "数据集列表" "GET" "$BASE_URL/api/v1/sandbox/datasets" "" "200"

test_api "创建数据集" "POST" "$BASE_URL/api/v1/sandbox/datasets" \
    '{"dataSetName":"测试数据集","description":"自动化测试","category":"TEST"}' "200"

test_api "数据集详情-ID1" "GET" "$BASE_URL/api/v1/sandbox/datasets/1" "" "200"

test_api "更新数据集" "PUT" "$BASE_URL/api/v1/sandbox/datasets/1" \
    '{"dataSetName":"更新后的名称","description":"更新描述","category":"TEST"}' "200"

test_api "测试套件列表" "GET" "$BASE_URL/api/v1/sandbox/suites" "" "200"

test_api "测试套件详情-ID1" "GET" "$BASE_URL/api/v1/sandbox/suites/1" "" "200"

test_api "更新测试套件" "PUT" "$BASE_URL/api/v1/sandbox/suites/1" \
    '{"suiteName":"更新后的名称","description":"更新描述","category":"TEST"}' "200"

test_api "执行日志列表" "GET" "$BASE_URL/api/v1/sandbox/execution-logs?page=1&pageSize=10" "" "200"

echo ""

# ====== 模块7: 合理用药 ======
echo -e "${YELLOW}=== 07. 合理用药 (Drugs) ===${NC}"

test_api "药品目录分页" "GET" "$BASE_URL/api/v1/drugs?page=1&pageSize=10" "" "200"

test_api "药品详情-ID1" "GET" "$BASE_URL/api/v1/drugs/1" "" "200"

test_api "新增药品" "POST" "$BASE_URL/api/v1/drugs" \
    '{"drugName":"测试药品A","specification":"10mg*12片","dosageUnit":"片","drugType":"西药","category":"抗生素","isEnabled":true}' "200"

test_api "更新药品" "PUT" "$BASE_URL/api/v1/drugs/1" \
    '{"drugName":"更新后的药品","specification":"20mg*24片"}' "200"

test_api "删除药品-ID999999" "DELETE" "$BASE_URL/api/v1/drugs/999999" "" "200"

echo ""

# ====== 模块8: 质控指标 ======
echo -e "${YELLOW}=== 08. 质控指标 (Quality) ===${NC}"

test_api "质控规则分页" "GET" "$BASE_URL/api/v1/quality?page=1&pageSize=10" "" "200"

test_api "质控规则详情-ID1" "GET" "$BASE_URL/api/v1/quality/1" "" "200"

test_api "新增质控规则" "POST" "$BASE_URL/api/v1/quality" \
    '{"itemName":"感染控制检查","category":"INFECTION","level":"WARN","description":"感染控制检查"}' "500"

test_api "更新质控规则" "PUT" "$BASE_URL/api/v1/quality/1" \
    '{"itemName":"更新后的名称","description":"更新描述"}' "200"

test_api "删除质控规则-ID999999" "DELETE" "$BASE_URL/api/v1/quality/999999" "" "200"

echo ""

# ====== 模块9: DRG分组 ======
echo -e "${YELLOW}=== 09. DRG分组 (DRG) ===${NC}"

test_api "DRG定义分页" "GET" "$BASE_URL/api/v1/drg?page=1&pageSize=10" "" "200"

test_api "DRG定义详情-ID1" "GET" "$BASE_URL/api/v1/drg/1" "" "200"

test_api "新增DRG定义" "POST" "$BASE_URL/api/v1/drg" \
    '{"drgName":"测试DRG组","mdcCode":"MDC_A","mdcName":"MDC测试","baseWeight":1.0,"baseFee":10000,"standardScore":100}' "200"

test_api "更新DRG定义" "PUT" "$BASE_URL/api/v1/drg/1" \
    '{"drgName":"更新后的DRG","baseWeight":1.2}' "200"

test_api "删除DRG定义-ID999999" "DELETE" "$BASE_URL/api/v1/drg/999999" "" "200"

echo ""

# ====== 模块10: 系统监控 ======
echo -e "${YELLOW}=== 10. 系统监控 (Monitor) ===${NC}"

test_api "获取监控指标" "GET" "$BASE_URL/api/v1/monitor/metrics" "" "200"

test_api "获取告警列表" "GET" "$BASE_URL/api/v1/monitor/alerts" "" "200"

test_api "获取热门规则" "GET" "$BASE_URL/api/v1/monitor/top-rules" "" "200"

test_api "记录执行指标" "POST" "$BASE_URL/api/v1/monitor/record?success=true&durationMs=150&ruleKey=test" "" "200"

test_api "记录告警" "POST" "$BASE_URL/api/v1/monitor/alert?alertType=test&message=测试告警" "" "400"

test_api "指标历史" "GET" "$BASE_URL/api/v1/monitor/history" "" "200"

test_api "趋势数据" "GET" "$BASE_URL/api/v1/monitor/trend?metricName=execution_count&hours=24" "" "200"

test_api "热力图数据" "GET" "$BASE_URL/api/v1/monitor/heatmap?days=7" "" "200"

test_api "告警规则列表" "GET" "$BASE_URL/api/v1/monitor/alert-rules" "" "200"

test_api "告警规则详情-ID1" "GET" "$BASE_URL/api/v1/monitor/alert-rules/1" "" "200"

test_api "创建告警规则" "POST" "$BASE_URL/api/v1/monitor/alert-rules" \
    '{"ruleName":"测试告警","metricName":"execution_duration","conditionType":"GT","threshold":5000,"level":"WARN","enabled":true}' "200"

test_api "更新告警规则" "PUT" "$BASE_URL/api/v1/monitor/alert-rules/1" \
    '{"ruleName":"更新后的告警","metricName":"execution_duration","conditionType":"GT","threshold":8000,"level":"WARN","enabled":true}' "200"

test_api "删除告警规则-ID999999" "DELETE" "$BASE_URL/api/v1/monitor/alert-rules/999999" "" "200"

test_api "WebSocket状态" "GET" "$BASE_URL/api/v1/monitor/ws/status" "" "200"

echo ""

# ====== 模块11: 规则模板市场 ======
echo -e "${YELLOW}=== 11. 规则模板市场 (Market) ===${NC}"

test_api "模板分页" "GET" "$BASE_URL/api/v1/market/templates?page=1&pageSize=10" "" "200"

test_api "模板详情-ID1" "GET" "$BASE_URL/api/v1/market/templates/1" "" "200"

test_api "更新模板" "PUT" "$BASE_URL/api/v1/market/templates/1" \
    '{"templateName":"更新后的模板","description":"更新描述"}' "200"

test_api "卸载模板" "DELETE" "$BASE_URL/api/v1/market/templates/1/install" "" "200"

test_api "我的模板" "GET" "$BASE_URL/api/v1/market/templates/my" "" "200"

test_api "已订阅模板" "GET" "$BASE_URL/api/v1/market/templates/subscribed" "" "200"

test_api "评分列表" "GET" "$BASE_URL/api/v1/market/templates/1/ratings" "" "200"

test_api "评分汇总" "GET" "$BASE_URL/api/v1/market/templates/1/rating-summary" "" "200"

test_api "收藏列表" "GET" "$BASE_URL/api/v1/market/templates/favorites" "" "200"

test_api "取消收藏" "DELETE" "$BASE_URL/api/v1/market/templates/1/favorite" "" "200"

test_api "收藏状态" "GET" "$BASE_URL/api/v1/market/templates/1/favorite-status" "" "200"

test_api "检查更新" "GET" "$BASE_URL/api/v1/market/templates/1/check-update" "" "200"

test_api "升级模板" "POST" "$BASE_URL/api/v1/market/templates/1/upgrade" "" "200"

echo ""

# ====== 模块12: 审计日志 ======
echo -e "${YELLOW}=== 12. 审计日志 (Audit Logs) ===${NC}"

test_api "审计日志分页" "GET" "$BASE_URL/api/v1/audit-logs?page=1&pageSize=10" "" "200"

test_api "按条件筛选审计日志" "GET" "$BASE_URL/api/v1/audit-logs?page=1&pageSize=10&action=CREATE&targetType=RULE" "" "200"

echo ""

# ====== 输出测试结果 ======
echo "======================================"
echo "测试报告"
echo "======================================"
echo "总测试数: $TEST_NUM"
echo -e "通过: ${GREEN}$PASS_COUNT${NC}"
echo -e "失败: ${RED}$FAIL_COUNT${NC}"
echo "通过率: $(echo "scale=2; $PASS_COUNT * 100 / $TEST_NUM" | bc)%"
echo ""

if [ $FAIL_COUNT -gt 0 ]; then
    echo -e "${RED}失败详情:${NC}"
    echo -e "$FAIL_DETAILS"
    echo ""
fi

echo "结束时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "======================================"
