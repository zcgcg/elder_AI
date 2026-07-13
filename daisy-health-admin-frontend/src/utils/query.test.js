import test from 'node:test'
import assert from 'node:assert/strict'
import { isOutsideWindow, sevenDayWindow, toQueryParams } from './query.js'

test('list filters become stable backend query parameters', () => {
  assert.deepEqual(toQueryParams({
    keyword: ' 王秀兰 ',
    status: '已完成',
    tag: '重点关怀',
    dateRange: ['2026-07-10', '2026-07-13'],
    personnelId: '2'
  }), {
    keyword: '王秀兰',
    status: '已完成',
    tag: '重点关怀',
    personnelId: '2',
    startDate: '2026-07-10',
    endDate: '2026-07-13'
  })
})

test('appointment board keeps an inclusive seven day window', () => {
  const window = sevenDayWindow(new Date(2026, 6, 13, 12, 0, 0))
  assert.deepEqual(window, { startDate: '2026-07-13', endDate: '2026-07-19' })
  assert.equal(isOutsideWindow(new Date(2026, 6, 19), window), false)
  assert.equal(isOutsideWindow(new Date(2026, 6, 20), window), true)
})
