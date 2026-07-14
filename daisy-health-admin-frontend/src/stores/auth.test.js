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
let loginRole = 'admin'
axios.defaults.adapter = async (config) => {
  requests.push(config)
  if (rejectLogout) throw new Error('backend unavailable')
  const data = config.url === '/auth/login'
    ? { token: `${loginRole}-jwt`, user: { id: 1, roleType: loginRole } }
    : { accepted: true }
  return {
    data: { code: 0, message: 'success', data },
    status: 200,
    statusText: 'OK',
    headers: {},
    config
  }
}

const { useAuthStore } = await import('./auth.js')

function emptyStore() {
  storage.clear()
  requests.length = 0
  rejectLogout = false
  setActivePinia(createPinia())
  return useAuthStore()
}

test('admin, elderly and service logins enter their matching mobile homes', async () => {
  const auth = emptyStore()
  const cases = [
    ['admin', '/dashboard'],
    ['elderly', '/portal/user'],
    ['service', '/portal/service']
  ]

  for (const [role, expectedPath] of cases) {
    loginRole = role
    await auth.signIn({ phone: role, password: 'test-password' })
    assert.equal(auth.user.roleType, role)
    assert.equal(auth.homePath, expectedPath)
    assert.equal(localStorage.getItem('daisy_token'), `${role}-jwt`)
  }
})

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
