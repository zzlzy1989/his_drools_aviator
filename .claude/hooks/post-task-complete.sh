#!/bin/bash
# post-task-complete.sh - 任务完成后自动触发知识回写 + 计划状态更新
# 触发条件: TaskStop 事件（任务完成）
# 用途: 1. 提醒 AI 执行 Step 7 知识回写
#       2. 自动更新关联计划的状态

set -euo pipefail

# 配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
PLANS_DIR="$PROJECT_ROOT/.trae/workflow-plans"
ACTIVE_DIR="$PLANS_DIR/active"

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo ""
echo "============================================"
echo "📝 任务已完成 — 建议执行知识回写 (Step 7)"
echo "============================================"
echo ""
echo "本次任务是否产生了值得持久化的知识？"
echo ""
echo "请回答以下三问："
echo "  1️⃣ 暴露了哪些未明确的业务假设？"
echo "  2️⃣ 代码做了什么但没说清楚为什么？"
echo "  3️⃣ 有没有和现有知识冲突？"
echo ""
echo "使用 /knowledge-writeback 执行完整回写流程"
echo "或直接回复 '无' 跳过（仅适用于纯配置/简单修改）"
echo ""

# 自动检查活跃计划
if [ -d "$ACTIVE_DIR" ] && [ -n "$(ls -A "$ACTIVE_DIR" 2>/dev/null)" ]; then
    echo "============================================"
    echo "📋 关联计划状态检查"
    echo "============================================"
    echo ""
    echo "当前活跃计划："
    echo ""

    for file in "$ACTIVE_DIR"/*.md; do
        [ -f "$file" ] || continue
        local_title=$(grep "^title:" "$file" | head -1 | sed 's/title: "//;s/"//')
        local_status=$(grep "^status:" "$file" | head -1 | sed 's/status: "//;s/"//')
        local_type=$(grep "^type:" "$file" | head -1 | sed 's/type: "//;s/"//')

        echo "  [$local_status] $local_title ($local_type)"
    done

    echo ""
    echo "💡 提示: 如任务已完成，请更新对应计划状态："
    echo "   bash .trae/workflow-plans/plan.sh update <文件名> completed"
    echo ""
fi

echo "============================================"

exit 0
