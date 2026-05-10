#!/bin/bash
# pre-browser.sh
# 浏览器操作前的检查 (HIS 规则引擎)

BROWSER_ARGS="$*"

# 检查是否携带 CDP 端口（暴露浏览器）
if echo "$BROWSER_ARGS" | grep -q "cdp_port"; then
    echo "⚠️ CDP 模式会暴露浏览器历史、Cookies 等敏感信息"
    echo "   请确认是否继续"
fi

# 检查是否是无头模式
if echo "$BROWSER_ARGS" | grep -q "headless"; then
    echo "ℹ️ 将以无头模式启动浏览器"
fi

# HIS 规则引擎：检查是否访问本地开发服务器或 Nacos 控制台
if echo "$BROWSER_ARGS" | grep -qE "localhost:8080|localhost:8848"; then
    echo "ℹ️ 访问本地 Spring Boot 服务或 Nacos 控制台"
fi

exit 0
