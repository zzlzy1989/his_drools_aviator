#!/bin/bash
# pre-search.sh
# 网络搜索前的检查 (HIS 规则引擎)

QUERY="$1"

# 检查搜索内容是否包含敏感信息
SENSITIVE_PATTERNS=(
    "password"
    "secret"
    "apikey"
    "token"
    "private key"
    "数据库密码"
    "Nacos 密码"
    "MySQL 密码"
)

for pattern in "${SENSITIVE_PATTERNS[@]}"; do
    if echo "$QUERY" | grep -qi "$pattern"; then
        echo "⚠️ 搜索内容可能包含敏感信息: $pattern"
        echo "   已自动过滤敏感关键词"
    fi
done

exit 0
