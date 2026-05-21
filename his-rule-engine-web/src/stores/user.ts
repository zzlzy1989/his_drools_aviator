import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const tenantId = ref(localStorage.getItem('tenantId') || 'default')
  const username = ref(localStorage.getItem('username') || '')
  const realName = ref(localStorage.getItem('realName') || '')
  const role = ref(localStorage.getItem('role') || '')

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setTenantId(id: string) {
    tenantId.value = id
    localStorage.setItem('tenantId', id)
  }

  function setUsername(name: string) {
    username.value = name
    localStorage.setItem('username', name)
  }

  function setRealName(name: string) {
    realName.value = name
    localStorage.setItem('realName', name)
  }

  function setRole(r: string) {
    role.value = r
    localStorage.setItem('role', r)
  }

  function setLoginInfo(data: {
    token: string
    userId: string
    username: string
    realName: string
    tenantId: string
    role: string
  }) {
    setToken(data.token)
    setTenantId(data.tenantId)
    setUsername(data.username)
    setRealName(data.realName || '')
    setRole(data.role || '')
  }

  function logout() {
    token.value = ''
    username.value = ''
    realName.value = ''
    role.value = ''
    tenantId.value = 'default'
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('realName')
    localStorage.removeItem('role')
    localStorage.removeItem('tenantId')
    localStorage.removeItem('userId')
  }

  function isAdmin(): boolean {
    return role.value === 'super_admin' || role.value === 'admin'
  }

  return {
    token,
    tenantId,
    username,
    realName,
    role,
    setToken,
    setTenantId,
    setUsername,
    setRealName,
    setRole,
    setLoginInfo,
    logout,
    isAdmin,
  }
})
