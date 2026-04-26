#!/bin/bash
# pre-write-file.sh - 文件写入前保护检查（强化版）
# 触发条件: PreToolUse(Write)
# 用途: 受保护文件强制确认，防止核心配置被意外覆盖

set -euo pipefail

FILE_PATH="${1:-}"
FILE_CONTENT="${2:-}"

# 受保护的文件列表（写入前必须获得用户显式确认）
PROTECTED_FILES=(
    # Harness 核心文件
    ".claude/agent.md"
    ".claude/MEMORY.md"
    ".claude/settings.local.json"
    ".claude/settings.schema.json"
    ".claude/rules/workflow.md"
    
    # 环境与密钥
    ".env"
    ".env.local"
    ".env.production"
    ".env.development"
    "config.yaml"
    
    # 版本控制关键文件
    ".gitignore"
    ".git/config"
    
    # 构建配置
    "package.json"
    "requirements.txt"
    "pyproject.toml"
    "Cargo.toml"
    
    # 入口文件
    "backend/manage.py"
    "frontend/vite.config.js"
    "frontend/tsconfig.json"
)

# 归一化文件路径
normalize_path() {
    local path="$1"
    # 移除开头的 ./
    path="${path#./}"
    # 移除重复的 /
    while [[ "$path" == *//* ]]; do
        path="${path//\/\///}"
    done
    echo "$path"
}

NORMALIZED_PATH=$(normalize_path "$FILE_PATH")

# 检查是否为受保护文件
for protected in "${PROTECTED_FILES[@]}"; do
    if [[ "$NORMALIZED_PATH" == "$protected" ]] || [[ "$NORMALIZED_PATH" == */"$protected" ]]; then
        # 检查是否有明确的覆盖意图标记
        if ! echo "$FILE_CONTENT" | head -5 | grep -qi "overwrite.*intent\|explicit.*update\|authorized.*modify"; then
            echo "⛔ 受保护文件写入拦截: $FILE_PATH"
            echo ""
            echo "该文件属于受保护列表，写入需要满足以下条件之一："
            echo ""
            echo "  1️⃣ 在用户消息中明确说明要修改此文件及原因"
            echo "  2️⃣ 文件内容包含 'OVERWRITE_INTENT: {reason}' 标记"
            echo ""
            echo "当前尝试的写入已被阻止。请先获得用户授权。"
            exit 1
        else
            echo "✅ 受保护文件写入已获授权: $FILE_PATH"
        fi
        break
    fi
done

# 二进制文件检测（禁止通过 Write 工具写入二进制内容）
if echo "$FILE_CONTENT" | head -c 100 | grep -qP '[\x00-\x08\x0e-\x1f]'; then
    echo "⛔ 检测到二进制内容，请使用其他方式处理二进制文件"
    exit 1
fi

# 大文件警告（>100KB）
CONTENT_LENGTH=${#FILE_CONTENT}
if (( CONTENT_LENGTH > 102400 )); then
    echo "⚠️ 写入内容较大 (${CONTENT_LENGTH} bytes)，确认这是预期行为"
fi

exit 0
