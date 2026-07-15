import test from 'node:test'
import assert from 'node:assert/strict'
import { appointmentRangeMinutes, clipAppointmentRange, serviceDurationMinutes } from './serviceDuration.js'

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

test('appointment range is clipped to the visible 06:00-22:00 board window', () => {
  assert.deepEqual(clipAppointmentRange({ startTime: '05:30', durationMinutes: 60 }, 6, 22), {
    startMinutes: 360,
    endMinutes: 390
  })
  assert.deepEqual(clipAppointmentRange({ startTime: '21:30', durationMinutes: 120 }, 6, 22), {
    startMinutes: 1290,
    endMinutes: 1320
  })
  assert.equal(clipAppointmentRange({ startTime: '05:00', durationMinutes: 30 }, 6, 22), null)
  assert.equal(clipAppointmentRange({ startTime: '22:00', durationMinutes: 60 }, 6, 22), null)
})
