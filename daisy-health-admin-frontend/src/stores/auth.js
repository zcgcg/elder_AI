import { defineStore } from 'pinia'
import { login, logout as logoutRequest, profile, updateProfile } from '../api/http.js'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('daisy_token') || '',
    user: readJson('daisy_user'),
    permissions: readJson('daisy_permissions')
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    homePath: (state) => {
      if (state.user?.roleType === 'elderly') return '/portal/user'
      if (state.user?.roleType === 'service') return '/portal/service'
      return '/dashboard'
    },
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
      const res = await login(payload)
      this.token = res.token
      this.user = res.user
      this.permissions = normalizePermissions(res.user?.permissions)
      persist(this.token, this.user, this.permissions)
      return res
    },
    async loadProfile() {
      if (!this.token) return null
      const res = await profile()
      this.user = res
      this.permissions = normalizePermissions(res.permissions)
      persist(this.token, this.user, this.permissions)
      return res
    },
    async saveProfile(payload) {
      const res = await updateProfile(payload)
      this.user = res
      this.permissions = normalizePermissions(res.permissions)
      persist(this.token, this.user, this.permissions)
      return res
    },
    updateCachedAvatar(avatarUrl) {
      if (!this.user) return
      this.user = { ...this.user, avatarUrl }
      persist(this.token, this.user, this.permissions)
    },
    async signOut() {
      let remoteAccepted = false
      try {
        if (this.token) {
          await logoutRequest()
          remoteAccepted = true
        }
      } catch (error) {
        remoteAccepted = false
      } finally {
        this.clearSession()
      }
      return remoteAccepted
    },
    clearSession() {
      this.token = ''
      this.user = null
      this.permissions = null
      localStorage.removeItem('daisy_token')
      localStorage.removeItem('daisy_user')
      localStorage.removeItem('daisy_permissions')
    }
  }
})

function persist(token, user, permissions) {
  localStorage.setItem('daisy_token', token)
  localStorage.setItem('daisy_user', JSON.stringify(user))
  localStorage.setItem('daisy_permissions', JSON.stringify(permissions))
}

function readJson(key) {
  const value = localStorage.getItem(key)
  if (!value || value === 'undefined') {
    localStorage.removeItem(key)
    return null
  }
  try {
    return JSON.parse(value)
  } catch (error) {
    localStorage.removeItem(key)
    return null
  }
}

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
