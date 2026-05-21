<template>
  <div :class="['his-monitor-stat', { 'his-monitor-stat--pulse': pulse }]">
    <div class="his-monitor-stat__label">{{ label }}</div>
    <div class="his-monitor-stat__value">{{ formattedValue }}</div>
    <div v-if="unit" class="his-monitor-stat__unit">{{ unit }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  label: string
  value: number | string
  unit?: string
  pulse?: boolean
  formatter?: (val: number | string) => string
}>(), {
  pulse: false,
})

const formattedValue = computed(() => {
  if (props.formatter) return props.formatter(props.value)
  if (typeof props.value === 'number') return props.value.toLocaleString()
  return props.value
})
</script>

<style scoped lang="scss">

.his-monitor-stat {
  display: flex;
  flex-direction: column;

  &__label {
    font-size: $font-size-caption;
    color: $color-monitor-on-dark-muted;
    letter-spacing: $letter-spacing-caption;
    text-transform: uppercase;
    margin-bottom: $spacing-xxs;
  }

  &__value {
    font-size: $font-size-display-lg;
    font-weight: $font-weight-light;
    color: $color-monitor-accent-lime;
    line-height: 1.25;
  }

  &__unit {
    font-size: $font-size-caption;
    color: $color-monitor-on-dark-muted;
    margin-top: 2px;
  }

  &--pulse {
    .his-monitor-stat__value {
      animation: monitor-pulse 2s ease-in-out infinite;
    }
  }
}

@keyframes monitor-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}
</style>
