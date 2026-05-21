<template>
  <div :class="['his-settlement-amount', `his-settlement-amount--${size}`]">
    <div v-if="label" class="his-settlement-amount__label">{{ label }}</div>
    <div :class="['his-settlement-amount__value', `his-settlement-amount__value--${variant}`]">
      <span v-if="showSymbol" class="his-settlement-amount__symbol">¥</span>
      <span class="his-settlement-amount__number">{{ formattedAmount }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type AmountSize = 'small' | 'default' | 'large'
type AmountVariant = 'default' | 'positive' | 'negative' | 'muted'

const props = withDefaults(defineProps<{
  amount: number
  label?: string
  size?: AmountSize
  variant?: AmountVariant
  showSymbol?: boolean
  precision?: number
}>(), {
  size: 'default',
  variant: 'default',
  showSymbol: true,
  precision: 2,
})

const formattedAmount = computed(() => {
  return props.amount.toFixed(props.precision).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
})
</script>

<style scoped lang="scss">

.his-settlement-amount {
  display: flex;
  flex-direction: column;

  &__label {
    font-size: $font-size-caption;
    color: $color-settlement-ink-secondary;
    letter-spacing: $letter-spacing-caption;
    text-transform: uppercase;
    margin-bottom: $spacing-xxs;
  }

  &__value {
    font-family: $font-family-mono;
    font-weight: $font-weight-regular;
    letter-spacing: -0.42px;
    font-feature-settings: "tnum";

    &--default {
      color: $color-settlement-ink;
    }

    &--positive {
      color: $color-semantic-pass;
    }

    &--negative {
      color: $color-semantic-block;
    }

    &--muted {
      color: $color-ink-muted;
    }
  }

  &__symbol {
    margin-right: 2px;
  }

  &--small {
    .his-settlement-amount__value {
      font-size: $font-size-body;
    }
  }

  &--default {
    .his-settlement-amount__value {
      font-size: $font-size-subhead;
    }
  }

  &--large {
    .his-settlement-amount__value {
      font-size: 28px;
    }
  }
}
</style>
