#!/bin/bash
# post-read-file.sh
# 文件读取后的处理 (HIS 规则引擎)

FILE_PATH="$1"
READ_SIZE="$2"

# 检查是否读取敏感文件
PROTECTED_PATTERNS=(
    "\.env$"
    "\.env\."
    "password"
    "secret"
    "token"
    "credential"
    ".git/config"
    "application-prod"
    "bootstrap.*\.yml"
    "nacos.*password"
    "mysql.*password"
)

for pattern in "${PROTECTED_PATTERNS[@]}"; do
    if [[ "$FILE_PATH" =~ $pattern ]]; then
        echo "📄 已读取敏感文件: $FILE_PATH"
        echo "   注意：不要在回答中泄露内容"
        break
    fi
done

exit 0
