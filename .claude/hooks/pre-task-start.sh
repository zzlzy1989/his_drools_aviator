#!/bin/bash
# pre-task-start.sh - 任务开始前自动检查/创建计划
# 触发条件: 新任务开始时
# 用途: 1. 检查是否有关联计划
#       2. 如无计划，提示创建

set -euo pipefail

# 配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
PLANS_DIR="$PROJECT_ROOT/.trae/workflow-plans"
ACTIVE_DIR="$PLANS_DIR/active"

echo ""
echo "============================================"
echo "📋 任务启动 — 计划检查"
echo "============================================"
echo ""

# 检查活跃计划数量
if [ -d "$ACTIVE_DIR" ]; then
    active_count=$(ls -1 "$ACTIVE_DIR"/*.md 2>/dev/null | wc -l)
    echo "当前活跃计划数: $active_count"
    echo ""

    if [ "$active_count" -gt 0 ]; then
        echo "活跃计划列表："
        echo ""
        for file in "$ACTIVE_DIR"/*.md; do
            [ -f "$file" ] || continue
            fname=$(basename "$file")
            ftitle=$(grep "^title:" "$file" | head -1 | sed 's/title: "//;s/"//')
            fstatus=$(grep "^status:" "$file" | head -1 | sed 's/status: "//;s/"//')
            ftype=$(grep "^type:" "$file" | head -1 | sed 's/type: "//;s/"//')

            echo "  📄 $fname"
            echo "     标题: $ftitle"
            echo "     状态: $fstatus | 类型: $ftype"
            echo ""
        done
    else
        echo "💡 当前无活跃计划"
        echo ""
        echo "如需创建计划，请使用："
        echo "  bash .trae/workflow-plans/plan.sh create <标题> <类型> <阶段> <负责人>"
        echo ""
        echo "示例："
        echo "  bash .trae/workflow-plans/plan.sh create \"实现医保报销规则\" feature \"Phase 2\" \"张三\" P0"
        echo ""
    fi
fi

echo "============================================"

exit 0
