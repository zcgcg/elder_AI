import assert from 'node:assert/strict'
import test from 'node:test'

import { assetUrl, resourcePath } from './http.js'
import * as httpApi from './http.js'

test('health data uses the backend kebab-case resource path', () => {
  assert.equal(resourcePath('healthData'), '/health-data')
})

test('uploaded assets stay on the current origin for the production proxy', () => {
  assert.equal(assetUrl('/uploads/avatar/example.png'), '/uploads/avatar/example.png')
})

test('mobile clients expose the backend logout endpoint', () => {
  assert.equal(typeof httpApi.logout, 'function')
})
