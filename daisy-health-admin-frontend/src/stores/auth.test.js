import assert from 'node:assert/strict'
import test from 'node:test'
import axios from 'axios'
import { createPinia, setActivePinia } from 'pinia'

const storage = new Map()
globalThis.localStorage = {
  getItem: (key) => storage.get(key) ?? null,
  setItem: (key, value) => storage.set(key, String(value)),
  removeItem: (key) => storage.delete(key)
}

const requests = []
let rejectLogout = false
axios.defaults.adapter = async (config) => {
  requests.push(config)
  if (rejectLogout) throw new Error('backend unavailable')
  return {
    data: { code: 0, message: 'success', data: { accepted: true } },
    status: 200,
    statusText: 'OK',
    headers: {},
    config
  }
}

const { useAuthStore } = await import('./auth.js')

function authenticatedStore() {
  storage.clear()
  requests.length = 0
  rejectLogout = false
  localStorage.setItem('daisy_token', 'mobile-jwt')
  localStorage.setItem('daisy_user', JSON.stringify({ id: 1, roleType: 'elderly' }))
  localStorage.setItem('daisy_permissions', JSON.stringify({ users: ['view'] }))
  setActivePinia(createPinia())
  return useAuthStore()
}

test('signOut calls the backend before clearing the mobile session', async () => {
  const auth = authenticatedStore()

  const remoteAccepted = await auth.signOut()

  assert.equal(remoteAccepted, true)
  assert.equal(requests.length, 1)
  assert.equal(requests[0].method, 'post')
  assert.equal(requests[0].url, '/auth/logout')
  assert.equal(requests[0].headers.Authorization, 'Bearer mobile-jwt')
  assert.equal(auth.token, '')
  assert.equal(auth.user, null)
  assert.equal(localStorage.getItem('daisy_token'), null)
})

test('signOut still clears local credentials when the backend is unavailable', async () => {
  const auth = authenticatedStore()
  rejectLogout = true

  const remoteAccepted = await auth.signOut()

  assert.equal(remoteAccepted, false)
  assert.equal(requests.length, 1)
  assert.equal(auth.token, '')
  assert.equal(auth.permissions, null)
  assert.equal(localStorage.getItem('daisy_user'), null)
  assert.equal(localStorage.getItem('daisy_permissions'), null)
})
