<template>
  <span :class="['his-status-tag', `his-status-tag--${type}`, `his-status-tag--${size}`]">
    <span v-if="showDot" :class="['his-status-tag__dot', `his-status-tag__dot--${type}`]" />
    <slot>{{ label }}</slot>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type StatusType = 'pass' | 'warn' | 'block' | 'info' | 'draft' | 'published' | 'disabled' | 'validating' | 'pending' | 'processing' | 'completed' | 'rejected'
type TagSize = 'small' | 'default'

const props = withDefaults(defineProps<{
  type: StatusType
  label?: string
  size?: TagSize
  showDot?: boolean
}>(), {
  size: 'default',
  showDot: false,
})

const labelMap: Record<StatusType, string> = {
  pass: '通过',
  warn: '警告',
  block: '阻断',
  info: '信息',
  draft: '草稿',
  published: '已发布',
  disabled: '已停用',
  validating: '验证中',
  pending: '待结算',
  processing: '结算中',
  completed: '已完成',
  rejected: '已拒绝',
}

const label = computed(() => props.label ?? labelMap[props.type])
</script>

<style scoped lang="scss">

.his-status-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border-radius: $radius-pill;
  font-weight: $font-weight-semibold;
  line-height: 1.29;
  letter-spacing: $letter-spacing-caption;
  white-space: nowrap;

  &--small {
    padding: 2px 8px;
    font-size: $font-size-fine-print;
  }

  &--default {
    padding: 3px 10px;
    font-size: $font-size-caption;
  }

  &--pass {
    color: $color-semantic-pass;
    background-color: $color-semantic-pass-bg;
  }

  &--published,
  &--completed {
    color: $color-semantic-pass;
    background-color: $color-semantic-pass-bg;
  }

  &--warn {
    color: $color-semantic-warn;
    background-color: $color-semantic-warn-bg;
  }

  &--processing {
    color: $color-semantic-warn;
    background-color: $color-semantic-warn-bg;
  }

  &--block {
    color: $color-semantic-block;
    background-color: $color-semantic-block-bg;
  }

  &--rejected {
    color: $color-semantic-block;
    background-color: $color-semantic-block-bg;
  }

  &--info {
    color: $color-semantic-info;
    background-color: $color-semantic-info-bg;
  }

  &--validating {
    color: $color-semantic-info;
    background-color: $color-semantic-info-bg;
  }

  &--draft {
    color: $color-ink-muted;
    background-color: $color-surface-1;
  }

  &--disabled {
    color: $color-ink-secondary;
    background-color: $color-surface-1;
  }

  &--pending {
    color: $color-ink-secondary;
    background-color: $color-surface-1;
  }
}

.his-status-tag__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;

  &--pass,
  &--published,
  &--completed {
    background-color: $color-semantic-pass;
  }

  &--warn,
  &--processing {
    background-color: $color-semantic-warn;
  }

  &--block,
  &--rejected {
    background-color: $color-semantic-block;
  }

  &--info,
  &--validating {
    background-color: $color-semantic-info;
  }

  &--draft,
  &--pending {
    background-color: $color-ink-muted;
  }

  &--disabled {
    background-color: $color-ink-secondary;
  }
}
</style>
