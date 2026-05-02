<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  // Initialize user store
  userStore.init()
})
</script>

<style>
#app {
  width: 100%;
  height: 100%;
}
</style>