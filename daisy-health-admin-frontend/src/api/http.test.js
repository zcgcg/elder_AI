import assert from 'node:assert/strict'
import test from 'node:test'

import { resourcePath } from './http.js'

test('health data uses the backend kebab-case resource path', () => {
  assert.equal(resourcePath('healthData'), '/health-data')
})
