import assert from 'node:assert/strict'
import test from 'node:test'

import { buildHealthTrend } from './healthChart.js'

test('raw user health records become the same weight and heart-rate trend used by admin', () => {
  const trend = buildHealthTrend([
    { dataType: 'heart_rate', value: '76', recordDate: '2026-07-11' },
    { dataType: 'weight', value: '58.4', recordDate: '2026-07-11' },
    { dataType: 'weight', value: '58.1', recordDate: '2026-07-12' }
  ])

  assert.deepEqual(trend.dates, ['2026-07-11', '2026-07-12'])
  assert.deepEqual(trend.weights, [58.4, 58.1])
  assert.deepEqual(trend.heartRates, [76, null])
})

test('admin aggregated health rows remain compatible with the shared trend', () => {
  const trend = buildHealthTrend([
    { day: '07-11', recordDate: '2026-07-11', weight: 58.4, heartRate: 76 }
  ])

  assert.deepEqual(trend.dates, ['2026-07-11'])
  assert.deepEqual(trend.weights, [58.4])
  assert.deepEqual(trend.heartRates, [76])
})
