import assert from 'node:assert/strict'
import test from 'node:test'
import { createServerConfig, normalizeServerOrigin, saveServerForSession } from './server.js'

function memoryPreferences(initialValue = null) {
  let value = initialValue
  return {
    async get() { return { value } },
    async set(entry) { value = entry.value }
  }
}

test('browser keeps same-origin API and uploaded asset paths', async () => {
  const config = createServerConfig({ native: false, preferences: memoryPreferences('http://192.168.1.20:8080') })

  await config.initialize()

  assert.equal(config.apiBaseUrl(), '/api/v1')
  assert.equal(config.assetUrl('/uploads/avatar.png'), '/uploads/avatar.png')
  assert.equal(config.isConfigured(), true)
})

test('Android app builds API and asset URLs from the remembered server', async () => {
  const config = createServerConfig({ native: true, preferences: memoryPreferences('http://192.168.1.20:8080/') })

  await config.initialize()

  assert.equal(config.currentOrigin(), 'http://192.168.1.20:8080')
  assert.equal(config.apiBaseUrl(), 'http://192.168.1.20:8080/api/v1')
  assert.equal(config.assetUrl('/uploads/report.pdf'), 'http://192.168.1.20:8080/uploads/report.pdf')
})

test('Android app rejects addresses that point back to the phone', () => {
  assert.throws(() => normalizeServerOrigin('http://localhost:8080', { native: true }), /不能使用 localhost/)
  assert.throws(() => normalizeServerOrigin('http://127.0.0.1:8080', { native: true }), /不能使用 localhost/)
})

test('Android app only permits cleartext HTTP for private IPv4 servers', () => {
  assert.equal(normalizeServerOrigin('http://10.200.244.66:8080', { native: true }), 'http://10.200.244.66:8080')
  assert.equal(normalizeServerOrigin('http://172.20.0.5:8080', { native: true }), 'http://172.20.0.5:8080')
  assert.throws(() => normalizeServerOrigin('http://api.example.com', { native: true }), /公网服务器必须使用 HTTPS/)
})

test('saving a different server reports the change', async () => {
  const config = createServerConfig({ native: true, preferences: memoryPreferences('http://192.168.1.20:8080') })
  await config.initialize()

  const result = await config.save('https://api.example.com/')

  assert.deepEqual(result, { origin: 'https://api.example.com', changed: true })
  assert.equal(config.apiBaseUrl(), 'https://api.example.com/api/v1')
})

test('switching servers clears the old login session', async () => {
  const config = createServerConfig({ native: true, preferences: memoryPreferences('http://192.168.1.20:8080') })
  await config.initialize()
  let cleared = 0

  await saveServerForSession(config, { clearSession: () => { cleared += 1 } }, 'https://api.example.com')

  assert.equal(cleared, 1)
})

test('failed persistence keeps the old server and old session together', async () => {
  const preferences = memoryPreferences('http://192.168.1.20:8080')
  preferences.set = async () => { throw new Error('disk unavailable') }
  const config = createServerConfig({ native: true, preferences })
  await config.initialize()
  let cleared = 0

  await assert.rejects(
    saveServerForSession(config, { clearSession: () => { cleared += 1 } }, 'https://api.example.com'),
    /disk unavailable/
  )

  assert.equal(config.currentOrigin(), 'http://192.168.1.20:8080')
  assert.equal(cleared, 0)
})
