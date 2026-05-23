<template>
  <div :class="['his-page-container', { 'his-page-container--no-padding': noPadding }]">
    <div v-if="title || $slots.header" :class="['his-page-header', { 'his-page-header--bordered': bordered }]">
      <div class="his-page-header__left">
        <h3 v-if="title" class="his-page-header__title">{{ title }}</h3>
        <slot name="header" />
      </div>
      <div class="his-page-header__right">
        <slot name="actions" />
      </div>
    </div>
    <div :class="['his-page-body', { 'his-page-body--card': card }]">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title?: string
  bordered?: boolean
  card?: boolean
  noPadding?: boolean
}>(), {
  bordered: true,
  card: true,
  noPadding: false,
})
</script>

<style scoped lang="scss">

.his-page-container {
  padding: $spacing-md;
  min-height: 100%;
  background-color: $color-surface-1;

  &--no-padding {
    padding: 0;
  }
}

.his-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-lg;
  background-color: $color-canvas;
  border: 1px solid $color-hairline;
  border-radius: $radius-lg;
  margin-bottom: $spacing-md;

  &--bordered {
    border-bottom: 1px solid $color-hairline;
  }

  &__left {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__title {
    font-size: $font-size-tagline;
    font-weight: $font-weight-semibold;
    color: $color-ink;
    margin: 0;
    letter-spacing: $letter-spacing-headline;
  }

  &__right {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
  }
}

.his-page-body {
  &--card {
    background-color: $color-canvas;
    border: 1px solid $color-hairline;
    border-radius: $radius-lg;
    padding: $spacing-lg;
  }
}
</style>
