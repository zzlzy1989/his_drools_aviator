#!/bin/bash
# post-task-complete.sh - 任务完成后自动触发知识回写提示
# 触发条件: TaskStop 事件（任务完成）
# 用途: 提醒 AI 执行 Step 7 知识回写

set -euo pipefail

# 只在任务成功完成时触发
if [[ "${CLAUDE_TASK_STATUS:-}" != "completed" ]]; then
    exit 0
fi

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
echo "============================================"

exit 0
