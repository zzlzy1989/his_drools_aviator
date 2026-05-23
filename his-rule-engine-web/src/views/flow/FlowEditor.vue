<template>
  <div class="flow-editor-view">
    <FlowEditor />
  </div>
</template>

<script setup lang="ts">
import FlowEditor from '@/components/FlowEditor/FlowEditor.vue'
</script>

<style lang="scss" scoped>
.flow-editor-view {
  width: 100%;
  height: 100%;
}
</style>