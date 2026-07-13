<template>
  <section>
    <div class="page-heading">
      <div><h1>数据分析</h1><p>用户、交易、商品、工单、业绩与评价数据概览</p></div>
      <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" @change="load" />
    </div>
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <div class="metric-grid">
      <article v-for="item in metrics" :key="item.label" class="metric-card"><span>{{ item.label }}</span><strong>{{ item.value }}</strong><el-tag type="success">{{ item.delta }}</el-tag></article>
    </div>
    <div class="dashboard-grid">
      <section class="panel"><h2>年龄结构</h2><div ref="ageChart" class="chart"></div></section>
      <section class="panel"><h2>交易概况</h2><div ref="tradeChart" class="chart"></div></section>
      <section class="panel wide"><h2>服务业绩趋势</h2><div ref="serviceChart" class="chart"></div></section>
    </div>
  </section>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getAnalytics } from '../api/http'
import { formatDate, toQueryParams } from '../utils/query'

const end = new Date()
const start = new Date(end)
start.setDate(start.getDate() - 29)
const range = ref([formatDate(start), formatDate(end)])
const error = ref('')
const ageChart = ref()
const tradeChart = ref()
const serviceChart = ref()
const ageDistribution = ref([])
const tradeDistribution = ref([])
const serviceTrend = ref([])
const metrics = ref([
  { label: '活跃用户', value: 3280, delta: '+9.8%' },
  { label: '成交金额', value: '¥86.4万', delta: '+14.1%' },
  { label: '完成工单', value: 912, delta: '+7.3%' },
  { label: '好评率', value: '96.8%', delta: '+2.4%' }
])

function draw() {
  echarts.getInstanceByDom(ageChart.value)?.dispose()
  echarts.getInstanceByDom(tradeChart.value)?.dispose()
  echarts.getInstanceByDom(serviceChart.value)?.dispose()
  echarts.init(ageChart.value).setOption({
    series: [{ type: 'pie', radius: '70%', data: ageDistribution.value, color: ['#00D39C', '#1890FF', '#FAAD14'] }]
  })
  echarts.init(tradeChart.value).setOption({
    xAxis: { type: 'category', data: tradeDistribution.value.map((item) => item.name) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: tradeDistribution.value.map((item) => item.value), itemStyle: { color: '#00D39C', borderRadius: 4 } }]
  })
  echarts.init(serviceChart.value).setOption({
    color: ['#00D39C', '#1890FF'],
    tooltip: { trigger: 'axis' },
    legend: { data: ['工单量', '成交额'] },
    xAxis: { type: 'category', data: serviceTrend.value.map((item) => item.day) },
    yAxis: { type: 'value' },
    series: [{ name: '工单量', type: 'line', smooth: true, data: serviceTrend.value.map((item) => item.orders) }, { name: '成交额', type: 'line', smooth: true, data: serviceTrend.value.map((item) => item.amount) }]
  })
}

async function load() {
  error.value = ''
  try {
    const data = await getAnalytics(toQueryParams({ dateRange: range.value }))
    if (data.metrics) metrics.value = data.metrics
    ageDistribution.value = data.ageDistribution || []
    tradeDistribution.value = data.tradeDistribution || []
    serviceTrend.value = data.serviceTrend || []
  } catch (exception) {
    metrics.value = []
    ageDistribution.value = []
    tradeDistribution.value = []
    serviceTrend.value = []
    error.value = exception.message || '分析数据加载失败'
  }
  await nextTick()
  if (ageChart.value && tradeChart.value && serviceChart.value) draw()
}

onMounted(async () => {
  await load()
})
</script>
