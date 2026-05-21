<template>
  <div :class="['his-stat-card', `his-stat-card--${variant}`]">
    <div class="his-stat-card__label">{{ label }}</div>
    <div class="his-stat-card__value">{{ formattedValue }}</div>
    <div v-if="trend || description" class="his-stat-card__footer">
      <span v-if="trend" :class="['his-stat-card__trend', `his-stat-card__trend--${trend}`]">
        <span v-if="trend === 'up'">↑</span>
        <span v-else-if="trend === 'down'">↓</span>
        {{ trendText }}
      </span>
      <span v-if="description" class="his-stat-card__desc">{{ description }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type StatVariant = 'default' | 'primary' | 'success' | 'warning' | 'danger'
type TrendType = 'up' | 'down' | 'flat'

const props = withDefaults(defineProps<{
  label: string
  value: number | string
  variant?: StatVariant
  trend?: TrendType
  trendText?: string
  description?: string
  formatter?: (val: number | string) => string
}>(), {
  variant: 'default',
})

const formattedValue = computed(() => {
  if (props.formatter) return props.formatter(props.value)
  if (typeof props.value === 'number') return props.value.toLocaleString()
  return props.value
})
</script>

<style scoped lang="scss">

.his-stat-card {
  background-color: $color-canvas;
  border: 1px solid $color-hairline;
  padding: $spacing-lg;
  transition: box-shadow 0.15s ease;

  &:hover {
    box-shadow: $shadow-card;
  }

  &__label {
    font-size: $font-size-caption;
    color: $color-ink-muted;
    letter-spacing: $letter-spacing-caption;
    text-transform: uppercase;
    margin-bottom: $spacing-xs;
  }

  &__value {
    font-size: $font-size-display-lg;
    font-weight: $font-weight-light;
    line-height: 1.25;
    color: $color-ink;
    margin-bottom: $spacing-xxs;
  }

  &__footer {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__trend {
    font-size: $font-size-caption;
    font-weight: $font-weight-semibold;

    &--up {
      color: $color-semantic-pass;
    }

    &--down {
      color: $color-semantic-block;
    }

    &--flat {
      color: $color-ink-muted;
    }
  }

  &__desc {
    font-size: $font-size-caption;
    color: $color-ink-muted;
  }

  &--primary {
    border-left: 3px solid $color-primary;

    .his-stat-card__value {
      color: $color-primary;
    }
  }

  &--success {
    border-left: 3px solid $color-semantic-pass;

    .his-stat-card__value {
      color: $color-semantic-pass;
    }
  }

  &--warning {
    border-left: 3px solid $color-semantic-warn;

    .his-stat-card__value {
      color: $color-semantic-warn;
    }
  }

  &--danger {
    border-left: 3px solid $color-semantic-block;

    .his-stat-card__value {
      color: $color-semantic-block;
    }
  }
}
</style>
