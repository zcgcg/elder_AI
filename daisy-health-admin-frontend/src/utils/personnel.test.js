import test from 'node:test'
import assert from 'node:assert/strict'
import { eligiblePersonnel } from './personnel.js'

test('appointment and work-order selectors only show enabled approved personnel', () => {
  const rows = [
    { id: 1, name: '张敏', status: '启用', auditStatus: '已通过' },
    { id: 2, name: '李华', status: '禁用', auditStatus: '已通过' },
    { id: 3, name: '周丽', status: '启用', auditStatus: '待审核' }
  ]

  assert.deepEqual(eligiblePersonnel(rows), [rows[0]])
})

test('service personnel selector only keeps people matching the selected product category', () => {
  const rows = [
    { id: 1, name: '张敏', serviceType: '家政护理', status: '启用', auditStatus: '已通过' },
    { id: 2, name: '李华', serviceType: '康复理疗', status: '启用', auditStatus: '已通过' },
    { id: 3, name: '王芳', serviceType: '家政护理', status: '禁用', auditStatus: '已通过' }
  ]

  assert.deepEqual(eligiblePersonnel(rows, '家政护理'), [rows[0]])
  assert.deepEqual(eligiblePersonnel(rows, '康复理疗'), [rows[1]])
  assert.deepEqual(eligiblePersonnel(rows, ''), [rows[0], rows[1]])
})
