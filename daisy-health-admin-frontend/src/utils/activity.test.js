import test from 'node:test'
import assert from 'node:assert/strict'
import { splitUserActivities } from './activity.js'

test('joined activities appear in my activities and not in available activities', () => {
  const result = splitUserActivities([
    { id: 1, title: '社区义诊', joined: true, canJoin: false },
    { id: 2, title: '健康讲座', joined: false, canJoin: true },
    { id: 3, title: '往期活动', joined: false, canJoin: false }
  ])

  assert.deepEqual(result.mine.map((item) => item.id), [1])
  assert.deepEqual(result.available.map((item) => item.id), [2])
  assert.deepEqual(result.unavailable.map((item) => item.id), [3])
})

test('attended activities stay in mine while cancelled activities are unavailable', () => {
  const result = splitUserActivities([
    { id: 4, joined: true, enrollmentStatus: '已参加', canJoin: false },
    { id: 5, joined: false, enrollmentStatus: '已取消', canJoin: false }
  ])

  assert.deepEqual(result.mine.map((item) => item.id), [4])
  assert.deepEqual(result.unavailable.map((item) => item.id), [5])
})
