import assert from 'node:assert/strict'
import test from 'node:test'
import { nativeBackAction } from './app.js'

test('Android back closes an overlay before navigating', () => {
  assert.equal(nativeBackAction({ hasOverlay: true, path: '/users', historyLength: 3 }), 'close-overlay')
})

test('Android back navigates from a business page when history exists', () => {
  assert.equal(nativeBackAction({ hasOverlay: false, path: '/users', historyLength: 3 }), 'go-back')
})

test('Android back exits from login and when no history exists', () => {
  assert.equal(nativeBackAction({ hasOverlay: false, path: '/login', historyLength: 3 }), 'exit-app')
  assert.equal(nativeBackAction({ hasOverlay: false, path: '/dashboard', historyLength: 1 }), 'exit-app')
})
