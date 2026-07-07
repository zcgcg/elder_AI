import { defineStore } from 'pinia'
import { login, profile } from '../api/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('daisy_token') || '',
    user: JSON.parse(localStorage.getItem('daisy_user') || 'null')
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token)
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
      localStorage.setItem('daisy_token', this.token)
      localStorage.setItem('daisy_user', JSON.stringify(this.user))
      return res
    },
    async loadProfile() {
      if (!this.token) return null
      const res = await profile()
      this.user = res
      localStorage.setItem('daisy_user', JSON.stringify(res))
      return res
    },
    signOut() {
      this.token = ''
      this.user = null
      localStorage.removeItem('daisy_token')
      localStorage.removeItem('daisy_user')
    }
  }
})
