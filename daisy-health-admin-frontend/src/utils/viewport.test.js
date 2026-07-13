import assert from 'node:assert/strict'
import test from 'node:test'

import { columnsForWidth } from './viewport.js'

test('phone layouts use one description column', () => {
  assert.equal(columnsForWidth(390, 3), 1)
  assert.equal(columnsForWidth(720, 2), 1)
})

test('tablet and desktop layouts preserve useful description columns', () => {
  assert.equal(columnsForWidth(768, 3), 2)
  assert.equal(columnsForWidth(1280, 3), 3)
})
