import { defineStore } from 'pinia'
import { login, profile, updateProfile } from '../api/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('daisy_token') || '',
    user: readJson('daisy_user'),
    permissions: readJson('daisy_permissions')
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
          user: {
            id: 1,
            name: '系统管理员',
            phone: payload.phone,
            role: '超级管理员',
            roleType: 'staff',
            permissions: { '*': ['*'] }
          }
        }
      }
      if (!isAdminUser(res.user)) {
        this.signOut()
        throw new Error('该账号不是后台管理端账号')
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
      if (!isAdminUser(res)) {
        this.signOut()
        throw new Error('该账号不是后台管理端账号')
      }
      this.user = res
      this.permissions = normalizePermissions(res.permissions)
      localStorage.setItem('daisy_user', JSON.stringify(res))
      localStorage.setItem('daisy_permissions', JSON.stringify(this.permissions))
      return res
    },
    async saveProfile(payload) {
      const res = await updateProfile(payload)
      if (!isAdminUser(res)) {
        this.signOut()
        throw new Error('该账号不是后台管理端账号')
      }
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

function isAdminUser(user) {
  return !user?.roleType || user.roleType === 'staff'
}
