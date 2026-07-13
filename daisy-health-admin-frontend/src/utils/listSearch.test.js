import test from 'node:test'
import assert from 'node:assert/strict'
import { isListSearchEnabled, listSearchStatuses } from './listSearch.js'

test('only work orders, products and orders keep list search', () => {
  assert.equal(isListSearchEnabled('workOrders'), true)
  assert.equal(isListSearchEnabled('products'), true)
  assert.equal(isListSearchEnabled('orders'), true)
  assert.equal(isListSearchEnabled('users'), false)
  assert.equal(isListSearchEnabled('afterSales'), false)
  assert.equal(isListSearchEnabled('activities'), false)
})

test('product and order search expose only the requested states', () => {
  assert.deepEqual(listSearchStatuses('products'), ['上架', '下架'])
  assert.deepEqual(listSearchStatuses('orders'), ['待服务', '已完成', '售后中'])
})
