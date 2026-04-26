#!/bin/sh
# HIS Rule Engine 健康检查脚本
# 用法: ./healthcheck.sh <service_port>

PORT=${1:-8080}
HOST=${2:-localhost}

if command -v curl > /dev/null 2>&1; then
    curl -f -s "http://${HOST}:${PORT}/actuator/health" > /dev/null 2>&1
    exit $?
elif command -v wget > /dev/null 2>&1; then
    wget -q -O - "http://${HOST}:${PORT}/actuator/health" > /dev/null 2>&1
    exit $?
else
    echo "ERROR: curl or wget is required for healthcheck"
    exit 1
fi
