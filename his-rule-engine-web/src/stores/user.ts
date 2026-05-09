import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const tenantId = ref(localStorage.getItem('tenantId') || 'default')
  const username = ref(localStorage.getItem('username') || '')

  function init() {
    // Initialize from localStorage if needed
  }

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setTenantId(id: string) {
    tenantId.value = id
    localStorage.setItem('tenantId', id)
  }

  function logout() {
    token.value = ''
    username.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
  }

  return {
    token,
    tenantId,
    username,
    init,
    setToken,
    setTenantId,
    logout,
  }
})