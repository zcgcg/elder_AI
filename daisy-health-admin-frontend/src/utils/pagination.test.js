import test from 'node:test'
import assert from 'node:assert/strict'
import { PAGE_SIZE, normalizePage, paginate } from './pagination.js'

test('每页最多展示二十条并可进入第三页', () => {
  const rows = Array.from({ length: 45 }, (_, index) => index + 1)

  assert.equal(PAGE_SIZE, 20)
  assert.deepEqual(paginate(rows, 1), rows.slice(0, 20))
  assert.deepEqual(paginate(rows, 2), rows.slice(20, 40))
  assert.deepEqual(paginate(rows, 3), rows.slice(40, 45))
})

test('数据减少后页码回到最后一个有效页', () => {
  assert.equal(normalizePage(3, 45), 3)
  assert.equal(normalizePage(3, 12), 1)
  assert.equal(normalizePage(0, 61), 1)
})
