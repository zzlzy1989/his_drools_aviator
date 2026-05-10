#!/bin/bash
# pre-execute-shell.sh - Shell 命令执行前安全检查（强化版）
# 触发条件: PreToolUse(bash)
# 用途: 危险命令拦截，防止误操作导致数据丢失或系统损坏

set -euo pipefail

COMMAND="${1:-}"
WORKSPACE="${CLAUDE_WORKSPACE:-$(pwd)}"

# ===== 危险命令黑名单（直接拦截，exit 1）=====
DANGEROUS_COMMANDS=(
    # 数据破坏性操作
    "rm -rf /"
    "rm -rf ~"
    "rm -rf \."
    "dd if="
    ":(){ :|:& };:"        # fork bomb
    "mkfs"                  # 格式化磁盘
    "> /dev/sda"            # 覆盖硬盘
    "chmod -R 777 /"
    "chown -R"
    
    # Git 危险操作
    "git push --force"
    "git reset --hard HEAD~"
    "git clean -fd"
    "git branch -D "
    
    # Docker 危险操作
    "docker system prune -a"
    "docker rmi --force"
    "docker volume rm"
    
    # 生产环境危险操作
    "DROP DATABASE"
    "DROP TABLE"
    "TRUNCATE TABLE"
    "DELETE FROM.*WHERE 1=1"
)

for pattern in "${DANGEROUS_COMMANDS[@]}"; do
    if echo "$COMMAND" | grep -qiE "$pattern"; then
        echo "⛔ 危险命令被拦截: $COMMAND"
        echo ""
        echo "该命令属于高风险操作，可能导致不可逆的数据丢失。"
        echo ""
        echo "如确认需要执行，请在用户消息中明确说明原因并获得授权。"
        exit 1
    fi
done

# ===== 需要警告的命令（不拦截，但显示提醒）=====
WARNING_COMMANDS=(
    "rm -rf"
    "DROP\|TRUNCATE"
    "git push --force-with-lease"
    "docker-compose down -v"
)

for pattern in "${WARNING_COMMANDS[@]}"; do
    if echo "$COMMAND" | grep -qiE "$pattern"; then
        echo "⚠️ 注意: $COMMAND 可能造成数据丢失，请确认操作正确"
        break
    fi
done

# ===== 受保护路径检查 =====
PROTECTED_PATHS=(
    ".env"
    ".env.local"
    ".env.production"
    "config.yaml"
    "secrets.yaml"
    "credentials.json"
    "*.key"
    "*.pem"
)

for path_pattern in "${PROTECTED_PATHS[@]}"; do
    if echo "$COMMAND" | grep -qE "(cat|less|more|view|edit|write).*${path_pattern}"; then
        echo "⚠️ 操作涉及敏感文件: ${path_pattern}"
        echo "请确保不会泄露敏感信息到输出中"
        break
    fi
done

exit 0
