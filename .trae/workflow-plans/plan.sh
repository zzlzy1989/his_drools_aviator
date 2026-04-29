#!/bin/bash
# ============================================================================
# 计划管理脚本 - HIS Drools+Aviator 规则引擎
# 功能: 创建、更新、完成、归档、查看计划
# 用法: ./plan.sh <command> [options]
# ============================================================================

set -e

# 配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PLANS_DIR="$SCRIPT_DIR"
ACTIVE_DIR="$PLANS_DIR/active"
COMPLETED_DIR="$PLANS_DIR/completed"
ARCHIVED_DIR="$PLANS_DIR/archived"
TEMPLATE_FILE="$PLANS_DIR/TEMPLATE.md"
INDEX_FILE="$PLANS_DIR/INDEX.md"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step() { echo -e "${BLUE}[STEP]${NC} $1"; }

# 生成计划 ID
generate_plan_id() {
    local date_str=$(date +%Y%m%d)
    local count=$(ls -1 "$ACTIVE_DIR" 2>/dev/null | wc -l)
    count=$((count + 1))
    printf "PLAN-%s-%03d" "$date_str" "$count"
}

# 生成文件名 slug
generate_slug() {
    echo "$1" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9]/-/g' | sed 's/--*/-/g' | sed 's/^-//;s/-$//'
}

# 创建新计划
cmd_create() {
    local title="$1"
    local type="${2:-feature}"
    local phase="${3:-Phase 1}"
    local owner="${4:-developer}"
    local priority="${5:-P1}"

    if [ -z "$title" ]; then
        log_error "用法: $0 create <title> [type] [phase] [owner] [priority]"
        log_info "  type: feature|bugfix|refactor|config|docs|test|deploy|research"
        exit 1
    fi

    local plan_id=$(generate_plan_id)
    local date_str=$(date +%Y-%m-%d)
    local slug=$(generate_slug "$title")
    local filename="${date_str}-${type}-${slug}.md"
    local filepath="$ACTIVE_DIR/$filename"

    if [ -f "$filepath" ]; then
        log_error "文件已存在: $filepath"
        exit 1
    fi

    # 复制模板并替换元数据
    cp "$TEMPLATE_FILE" "$filepath"

    local now=$(date '+%Y-%m-%d %H:%M:%S')

    # 替换 YAML frontmatter
    sed -i "s/title: \"任务标题\"/title: \"$title\"/" "$filepath"
    sed -i "s/type: \"feature\"/type: \"$type\"/" "$filepath"
    sed -i "s/status: \"pending\"/status: \"pending\"/" "$filepath"
    sed -i "s/created_at: \"YYYY-MM-DD HH:mm:ss\"/created_at: \"$now\"/" "$filepath"
    sed -i "s/updated_at: \"YYYY-MM-DD HH:mm:ss\"/updated_at: \"$now\"/" "$filepath"
    sed -i "s/phase: \"Phase X\"/phase: \"$phase\"/" "$filepath"
    sed -i "s/owner: \"开发者名称\"/owner: \"$owner\"/" "$filepath"
    sed -i "s/priority: \"P0\"/priority: \"$priority\"/" "$filepath"

    # 替换正文中的占位符
    sed -i "s/# 任务标题/# $plan_id: $title/" "$filepath"
    sed -i "s/计划 ID: PLAN-YYYYMMDD-XXX/计划 ID: $plan_id/" "$filepath"
    sed -i "s/创建时间: YYYY-MM-DD HH:mm:ss/创建时间: $now/" "$filepath"
    sed -i "s/YYYY-MM-DD HH:mm | 创建计划 | pending | 初始创建/$now | 创建计划 | pending | 初始创建/" "$filepath"

    log_info "计划创建成功!"
    log_info "  ID: $plan_id"
    log_info "  文件: $filepath"
    log_info "  类型: $type"
    log_info "  阶段: $phase"
}

# 更新计划状态
cmd_update() {
    local filename="$1"
    local new_status="$2"
    local note="${3:-}"

    if [ -z "$filename" ] || [ -z "$new_status" ]; then
        log_error "用法: $0 update <filename> <status> [note]"
        log_info "  status: pending|in_progress|completed|cancelled"
        exit 1
    fi

    # 查找文件
    local filepath=""
    for dir in "$ACTIVE_DIR" "$COMPLETED_DIR" "$ARCHIVED_DIR"; do
        if [ -f "$dir/$filename" ]; then
            filepath="$dir/$filename"
            break
        fi
    done

    if [ -z "$filepath" ]; then
        log_error "找不到计划文件: $filename"
        exit 1
    fi

    local now=$(date '+%Y-%m-%d %H:%M:%S')
    local old_status=$(grep "^status:" "$filepath" | head -1 | sed 's/status: "//;s/"//')

    # 更新状态
    sed -i "0,/^status: \"$old_status\"/{s/^status: \"$old_status\"/status: \"$new_status\"/}" "$filepath"
    sed -i "s/updated_at: \".*\"/updated_at: \"$now\"/" "$filepath"

    # 如果状态是 completed，添加完成时间
    if [ "$new_status" = "completed" ]; then
        sed -i "s/completed_at: null/completed_at: \"$now\"/" "$filepath"
    fi

    # 添加进度记录
    sed -i "s/| | | | |/| $now | 状态更新 | $old_status → $new_status | $note|/" "$filepath"

    # 移动文件
    if [ "$new_status" = "completed" ]; then
        mv "$filepath" "$COMPLETED_DIR/"
        log_info "文件已移动到: $COMPLETED_DIR/$filename"
    elif [ "$new_status" = "cancelled" ]; then
        mv "$filepath" "$ARCHIVED_DIR/"
        log_info "文件已移动到: $ARCHIVED_DIR/$filename"
    fi

    log_info "计划状态已更新: $old_status → $new_status"
}

# 查看活跃计划
cmd_list() {
    local status_filter="${1:-all}"

    echo ""
    echo "=== 活跃计划 ==="
    echo ""

    if [ ! -d "$ACTIVE_DIR" ] || [ -z "$(ls -A "$ACTIVE_DIR" 2>/dev/null)" ]; then
        log_info "没有活跃计划"
        return
    fi

    printf "%-40s %-12s %-10s %-10s %-15s\n" "文件名" "类型" "状态" "阶段" "负责人"
    printf "%-40s %-12s %-10s %-10s %-15s\n" "------" "----" "----" "----" "------"

    for file in "$ACTIVE_DIR"/*.md; do
        [ -f "$file" ] || continue
        local fname=$(basename "$file")
        local type=$(grep "^type:" "$file" | head -1 | sed 's/type: "//;s/"//')
        local status=$(grep "^status:" "$file" | head -1 | sed 's/status: "//;s/"//')
        local phase=$(grep "^phase:" "$file" | head -1 | sed 's/phase: "//;s/"//')
        local owner=$(grep "^owner:" "$file" | head -1 | sed 's/owner: "//;s/"//')

        if [ "$status_filter" != "all" ] && [ "$status" != "$status_filter" ]; then
            continue
        fi

        printf "%-40s %-12s %-10s %-10s %-15s\n" "$fname" "$type" "$status" "$phase" "$owner"
    done

    echo ""
}

# 查看计划详情
cmd_show() {
    local filename="$1"

    if [ -z "$filename" ]; then
        log_error "用法: $0 show <filename>"
        exit 1
    fi

    local filepath=""
    for dir in "$ACTIVE_DIR" "$COMPLETED_DIR" "$ARCHIVED_DIR"; do
        if [ -f "$dir/$filename" ]; then
            filepath="$dir/$filename"
            break
        fi
    done

    if [ -z "$filepath" ]; then
        log_error "找不到计划文件: $filename"
        exit 1
    fi

    cat "$filepath"
}

# 更新索引
cmd_reindex() {
    log_step "正在更新索引..."

    local now=$(date '+%Y-%m-%d')
    local active_count=0
    local completed_count=0
    local archived_count=0
    local in_progress_count=0

    # 统计数量
    for file in "$ACTIVE_DIR"/*.md; do
        [ -f "$file" ] || continue
        active_count=$((active_count + 1))
        local status=$(grep "^status:" "$file" | head -1 | sed 's/status: "//;s/"//')
        if [ "$status" = "in_progress" ]; then
            in_progress_count=$((in_progress_count + 1))
        fi
    done

    completed_count=$(ls -1 "$COMPLETED_DIR"/*.md 2>/dev/null | wc -l)
    archived_count=$(ls -1 "$ARCHIVED_DIR"/*.md 2>/dev/null | wc -l)

    # 重建索引头部
    cat > "$INDEX_FILE" << EOF
# 计划跟踪索引

> 自动生成的计划跟踪索引
> 最后更新: $now

---

## 活跃计划 (active/)

| 计划 ID | 标题 | 类型 | 状态 | 阶段 | 负责人 | 创建时间 | 文件 |
|---------|------|------|------|------|--------|---------|------|
EOF

    # 添加活跃计划
    for file in "$ACTIVE_DIR"/*.md; do
        [ -f "$file" ] || continue
        local fname=$(basename "$file")
        local title=$(grep "^title:" "$file" | head -1 | sed 's/title: "//;s/"//')
        local type=$(grep "^type:" "$file" | head -1 | sed 's/type: "//;s/"//')
        local status=$(grep "^status:" "$file" | head -1 | sed 's/status: "//;s/"//')
        local phase=$(grep "^phase:" "$file" | head -1 | sed 's/phase: "//;s/"//')
        local owner=$(grep "^owner:" "$file" | head -1 | sed 's/owner: "//;s/"//')
        local created=$(grep "^created_at:" "$file" | head -1 | sed 's/created_at: "//;s/"//')
        local plan_id=$(grep "计划 ID:" "$file" | head -1 | sed 's/.*计划 ID: //')

        echo "| $plan_id | $title | $type | $status | $phase | $owner | $created | [链接](active/$fname) |" >> "$INDEX_FILE"
    done

    # 添加已完成计划
    echo "" >> "$INDEX_FILE"
    echo "## 已完成计划 (completed/)" >> "$INDEX_FILE"
    echo "" >> "$INDEX_FILE"
    echo "| 计划 ID | 标题 | 类型 | 完成时间 | 文件 |" >> "$INDEX_FILE"
    echo "|---------|------|------|---------|------|" >> "$INDEX_FILE"

    for file in "$COMPLETED_DIR"/*.md; do
        [ -f "$file" ] || continue
        local fname=$(basename "$file")
        local title=$(grep "^title:" "$file" | head -1 | sed 's/title: "//;s/"//')
        local type=$(grep "^type:" "$file" | head -1 | sed 's/type: "//;s/"//')
        local completed=$(grep "^completed_at:" "$file" | head -1 | sed 's/completed_at: "//;s/"//')
        local plan_id=$(grep "计划 ID:" "$file" | head -1 | sed 's/.*计划 ID: //')

        if [ "$completed" = "null" ]; then
            completed=$(grep "^updated_at:" "$file" | head -1 | sed 's/updated_at: "//;s/"//')
        fi

        echo "| $plan_id | $title | $type | $completed | [链接](completed/$fname) |" >> "$INDEX_FILE"
    done

    # 添加统计
    cat >> "$INDEX_FILE" << EOF

---

## 统计

| 状态 | 数量 |
|------|------|
| 活跃 (pending) | $((active_count - in_progress_count)) |
| 进行中 (in_progress) | $in_progress_count |
| 已完成 (completed) | $completed_count |
| 已归档 (archived) | $archived_count |
| **总计** | **$((active_count + completed_count + archived_count))** |

---

*索引结束*
EOF

    log_info "索引已更新: $INDEX_FILE"
    log_info "  活跃: $active_count, 进行中: $in_progress_count, 已完成: $completed_count, 已归档: $archived_count"
}

# 显示帮助
cmd_help() {
    echo "计划管理工具 - HIS Drools+Aviator 规则引擎"
    echo ""
    echo "用法: $0 <command> [options]"
    echo ""
    echo "命令:"
    echo "  create <title> [type] [phase] [owner] [priority]"
    echo "      创建新计划"
    echo "      type: feature|bugfix|refactor|config|docs|test|deploy|research (默认: feature)"
    echo "      phase: Phase 1|Phase 2|... (默认: Phase 1)"
    echo "      owner: 负责人名称 (默认: developer)"
    echo "      priority: P0|P1|P2|P3 (默认: P1)"
    echo ""
    echo "  update <filename> <status> [note]"
    echo "      更新计划状态"
    echo "      status: pending|in_progress|completed|cancelled"
    echo ""
    echo "  list [status]"
    echo "      列出活跃计划"
    echo "      status: all|pending|in_progress (默认: all)"
    echo ""
    echo "  show <filename>"
    echo "      查看计划详情"
    echo ""
    echo "  reindex"
    echo "      更新索引文件"
    echo ""
    echo "  help"
    echo "      显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 create \"实现医保报销规则\" feature \"Phase 2\" \"张三\" P0"
    echo "  $0 update 2026-04-26-feature-xxx.md in_progress"
    echo "  $0 list in_progress"
    echo "  $0 show 2026-04-26-feature-xxx.md"
    echo "  $0 reindex"
}

# 主函数
main() {
    local command="${1:-help}"
    shift || true

    case "$command" in
        create)
            cmd_create "$@"
            ;;
        update)
            cmd_update "$@"
            ;;
        list)
            cmd_list "$@"
            ;;
        show)
            cmd_show "$@"
            ;;
        reindex)
            cmd_reindex
            ;;
        help|--help|-h)
            cmd_help
            ;;
        *)
            log_error "未知命令: $command"
            cmd_help
            exit 1
            ;;
    esac
}

main "$@"
