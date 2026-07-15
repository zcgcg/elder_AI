export const DEFAULT_SERVICE_DURATION_MINUTES = 60

export function serviceDurationMinutes(product) {
  const duration = Number(product?.duration)
  return Number.isFinite(duration) && duration > 0 ? duration : DEFAULT_SERVICE_DURATION_MINUTES
}

export function appointmentRangeMinutes(appointment) {
  const [hour, minute] = String(appointment?.startTime || '').split(':').map(Number)
  if (![hour, minute].every(Number.isFinite)) return null
  const startMinutes = hour * 60 + minute
  const duration = Number(appointment?.durationMinutes)
  const durationMinutes = Number.isFinite(duration) && duration > 0 ? duration : DEFAULT_SERVICE_DURATION_MINUTES
  return { startMinutes, endMinutes: startMinutes + durationMinutes }
}
