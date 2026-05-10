#!/bin/bash
# post-execute-shell.sh
# shell 命令执行后的日志记录 (HIS 规则引擎)

COMMAND="$1"
EXIT_CODE="$2"
DURATION="$3"

# 记录到日志
LOG_FILE="$(dirname "$0")/../logs/command_log.md"

# 确保日志目录存在
mkdir -p "$(dirname "$LOG_FILE")"

echo "## $(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"
echo "- 命令: \`$COMMAND\`" >> "$LOG_FILE"
echo "- 退出码: $EXIT_CODE" >> "$LOG_FILE"
echo "- 执行时间: ${DURATION}s" >> "$LOG_FILE"
echo "" >> "$LOG_FILE"

# 如果执行失败，输出警告
if [ "$EXIT_CODE" -ne 0 ]; then
    echo "⚠️ 命令执行失败 (退出码: $EXIT_CODE)"
    echo "命令: $COMMAND"
fi

exit 0
