import test from 'node:test'
import assert from 'node:assert/strict'
import { SERVICE_CATEGORIES, catalogItemsByCategory, personnelByProduct, productCategory } from './serviceCategory.js'

test('catalog always exposes the four service categories in business order', () => {
  assert.deepEqual(SERVICE_CATEGORIES, ['家政护理', '康复理疗', '上门体检', '其他'])
})

test('catalog and personnel matching use the selected product category', () => {
  const products = [
    { id: 1, name: '助浴护理', category: '家政护理' },
    { id: 2, name: '肩颈理疗', category: '康复理疗' },
    { id: 3, name: '代办服务', category: '其他' }
  ]

  assert.deepEqual(catalogItemsByCategory(products, '其他'), [products[2]])
  assert.equal(productCategory(products, '2'), '康复理疗')
  assert.equal(productCategory(products, ''), '')
})

test('personnel selection follows the selected product category', () => {
  const products = [{ id: 1, category: '家政护理' }, { id: 2, category: '上门体检' }]
  const personnel = [
    { id: 10, serviceType: '家政护理' },
    { id: 11, serviceType: '上门体检' },
    { id: 12, serviceType: '其他' }
  ]

  assert.deepEqual(personnelByProduct(personnel, products, 2).map((item) => item.id), [11])
  assert.deepEqual(personnelByProduct(personnel, products, '').map((item) => item.id), [])
})
