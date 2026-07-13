export function toQueryParams(filters = {}) {
  const params = {}
  for (const key of ['keyword', 'status', 'tag', 'personnelId', 'customerId']) {
    const value = String(filters[key] ?? '').trim()
    if (value) params[key] = value
  }
  const range = Array.isArray(filters.dateRange) ? filters.dateRange : []
  if (range[0]) params.startDate = formatDate(range[0])
  if (range[1]) params.endDate = formatDate(range[1])
  return params
}

export function sevenDayWindow(base = new Date()) {
  const start = localDate(base)
  const end = new Date(start)
  end.setDate(end.getDate() + 6)
  return { startDate: formatDate(start), endDate: formatDate(end) }
}

export function isOutsideWindow(value, window) {
  const date = formatDate(value)
  return date < window.startDate || date > window.endDate
}

export function formatDate(value) {
  if (typeof value === 'string') return value.slice(0, 10)
  return [value.getFullYear(), String(value.getMonth() + 1).padStart(2, '0'), String(value.getDate()).padStart(2, '0')].join('-')
}

function localDate(value) {
  return new Date(value.getFullYear(), value.getMonth(), value.getDate())
}
