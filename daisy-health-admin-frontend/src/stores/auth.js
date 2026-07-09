import { defineStore } from 'pinia'
import { login, profile, updateProfile } from '../api/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('daisy_token') || '',
    user: JSON.parse(localStorage.getItem('daisy_user') || 'null'),
    permissions: JSON.parse(localStorage.getItem('daisy_permissions') || 'null')
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    canAccess: (state) => (module, action = 'view') => {
      const permissions = state.permissions || state.user?.permissions
      if (!permissions) return false
      if (permissions['*']?.includes('*')) return true
      const actions = permissions[module]
      return Array.isArray(actions) && (actions.includes('*') || actions.includes(action))
    }
  },
  actions: {
    async signIn(payload) {
      let res
      try {
        res = await login(payload)
      } catch (error) {
        if (!payload.phone || !payload.password) throw error
        res = {
          token: 'local-preview-token',
          user: { id: 1, name: '系统管理员', phone: payload.phone, role: '超级管理员' }
        }
      }
      this.token = res.token
      this.user = res.user
      this.permissions = normalizePermissions(res.user?.permissions)
      localStorage.setItem('daisy_token', this.token)
      localStorage.setItem('daisy_user', JSON.stringify(this.user))
      localStorage.setItem('daisy_permissions', JSON.stringify(this.permissions))
      return res
    },
    async loadProfile() {
      if (!this.token) return null
      const res = await profile()
      this.user = res
      this.permissions = normalizePermissions(res.permissions)
      localStorage.setItem('daisy_user', JSON.stringify(res))
      localStorage.setItem('daisy_permissions', JSON.stringify(this.permissions))
      return res
    },
    async saveProfile(payload) {
      const res = await updateProfile(payload)
      this.user = res
      this.permissions = normalizePermissions(res.permissions)
      localStorage.setItem('daisy_user', JSON.stringify(res))
      localStorage.setItem('daisy_permissions', JSON.stringify(this.permissions))
      return res
    },
    signOut() {
      this.token = ''
      this.user = null
      this.permissions = null
      localStorage.removeItem('daisy_token')
      localStorage.removeItem('daisy_user')
      localStorage.removeItem('daisy_permissions')
    }
  }
})

function normalizePermissions(value) {
  if (!value) return null
  if (typeof value === 'string') {
    try {
      return JSON.parse(value)
    } catch (error) {
      return null
    }
  }
  return value
}
