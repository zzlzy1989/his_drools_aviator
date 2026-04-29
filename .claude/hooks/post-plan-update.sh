#!/bin/bash
# post-plan-update.sh - 计划状态变更后自动触发
# 触发条件: 计划状态从 in_progress → completed
# 用途: 1. 自动更新索引
#       2. 触发知识回写
#       3. 记录变更日志

set -euo pipefail

# 配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
PLANS_DIR="$PROJECT_ROOT/.trae/workflow-plans"
CHANGELOG="$PROJECT_ROOT/.trae/CHANGELOG.md"

# 参数
PLAN_FILE="${1:-}"
OLD_STATUS="${2:-}"
NEW_STATUS="${3:-}"

if [ -z "$PLAN_FILE" ] || [ -z "$NEW_STATUS" ]; then
    echo "用法: $0 <plan_file> <old_status> <new_status>"
    exit 1
fi

echo ""
echo "============================================"
echo "📊 计划状态变更: $OLD_STATUS → $NEW_STATUS"
echo "============================================"
echo ""
echo "计划文件: $PLAN_FILE"
echo "变更时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# 状态为 completed 时触发后续动作
if [ "$NEW_STATUS" = "completed" ]; then
    echo "✅ 计划已完成，执行后续动作..."
    echo ""

    # 1. 更新索引
    echo "  1️⃣ 更新索引..."
    if [ -f "$PLANS_DIR/plan.sh" ]; then
        bash "$PLANS_DIR/plan.sh" reindex
    fi

    # 2. 记录变更日志
    echo ""
    echo "  2️⃣ 记录变更日志..."
    local_date=$(date '+%Y-%m-%d')
    local_time=$(date '+%H:%M:%S')
    local_title=$(grep "^title:" "$PLAN_FILE" 2>/dev/null | head -1 | sed 's/title: "//;s/"//' || echo "未知")
    local_type=$(grep "^type:" "$PLAN_FILE" 2>/dev/null | head -1 | sed 's/type: "//;s/"//' || echo "未知")

    if [ -f "$CHANGELOG" ]; then
        echo "- [$local_date $local_time] 完成计划: [$local_title] ($local_type)" >> "$CHANGELOG"
        echo "     已追加到: .trae/CHANGELOG.md"
    fi

    # 3. 提示知识回写
    echo ""
    echo "  3️⃣ 建议执行知识回写 (Step 7)"
    echo "     使用 /knowledge-writeback 或手动更新 domain/ 知识库"
    echo ""
fi

echo "============================================"

exit 0
