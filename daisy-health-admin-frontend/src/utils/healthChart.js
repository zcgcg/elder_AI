function numeric(value) {
  if (value === null || value === undefined || value === '') return null
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

export function buildHealthTrend(rows = []) {
  const byDate = new Map()
  rows.forEach((row) => {
    const date = row.recordDate || row.day
    if (!date) return
    const point = byDate.get(date) || { weight: null, heartRate: null }
    if (row.weight !== undefined && row.weight !== null) point.weight = numeric(row.weight)
    if (row.heartRate !== undefined && row.heartRate !== null) point.heartRate = numeric(row.heartRate)
    if (row.dataType === 'weight') point.weight = numeric(row.value)
    if (row.dataType === 'heart_rate') point.heartRate = numeric(row.value)
    byDate.set(date, point)
  })
  const dates = [...byDate.keys()].sort()
  return {
    dates,
    weights: dates.map((date) => byDate.get(date).weight),
    heartRates: dates.map((date) => byDate.get(date).heartRate)
  }
}

export function createHealthChartOption(rows = []) {
  const trend = buildHealthTrend(rows)
  return {
    color: ['#159a84', '#e3a21a'],
    tooltip: { trigger: 'axis' },
    legend: { data: ['体重', '心率'] },
    grid: { left: 48, right: 48, top: 48, bottom: 36 },
    xAxis: { type: 'category', data: trend.dates },
    yAxis: [
      { type: 'value', name: 'kg', scale: true },
      { type: 'value', name: 'bpm', scale: true }
    ],
    series: [
      { name: '体重', type: 'line', smooth: true, connectNulls: true, data: trend.weights },
      { name: '心率', type: 'line', smooth: true, connectNulls: true, yAxisIndex: 1, data: trend.heartRates }
    ]
  }
}
