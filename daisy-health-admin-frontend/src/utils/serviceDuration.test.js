import test from 'node:test'
import assert from 'node:assert/strict'
import { appointmentRangeMinutes, serviceDurationMinutes } from './serviceDuration.js'

test('service duration defaults to one hour when product duration is absent or invalid', () => {
  assert.equal(serviceDurationMinutes(), 60)
  assert.equal(serviceDurationMinutes({ duration: null }), 60)
  assert.equal(serviceDurationMinutes({ duration: 0 }), 60)
  assert.equal(serviceDurationMinutes({ duration: -10 }), 60)
})

test('service duration keeps a positive number of minutes', () => {
  assert.equal(serviceDurationMinutes({ duration: 45 }), 45)
  assert.equal(serviceDurationMinutes({ duration: '90' }), 90)
})

test('appointment range keeps a cross-midnight service visible', () => {
  assert.deepEqual(appointmentRangeMinutes({ startTime: '23:30', durationMinutes: 120 }), {
    startMinutes: 1410,
    endMinutes: 1530
  })
})
