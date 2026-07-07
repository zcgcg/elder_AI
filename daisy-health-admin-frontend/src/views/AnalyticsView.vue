<template>
  <section>
    <div class="page-heading">
      <div><h1>数据分析</h1><p>用户、交易、商品、工单、业绩与评价数据概览</p></div>
      <el-date-picker v-model="range" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" />
    </div>
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

const range = ref([])
const ageChart = ref()
const tradeChart = ref()
const serviceChart = ref()
const metrics = ref([
  { label: '活跃用户', value: 3280, delta: '+9.8%' },
  { label: '成交金额', value: '¥86.4万', delta: '+14.1%' },
  { label: '完成工单', value: 912, delta: '+7.3%' },
  { label: '好评率', value: '96.8%', delta: '+2.4%' }
])

function draw() {
  echarts.init(ageChart.value).setOption({
    series: [{ type: 'pie', radius: '70%', data: [{ name: '60-69岁', value: 42 }, { name: '70-79岁', value: 36 }, { name: '80岁以上', value: 22 }], color: ['#00D39C', '#1890FF', '#FAAD14'] }]
  })
  echarts.init(tradeChart.value).setOption({
    xAxis: { type: 'category', data: ['家政', '康复', '体检'] },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: [420, 360, 280], itemStyle: { color: '#00D39C', borderRadius: 4 } }]
  })
  echarts.init(serviceChart.value).setOption({
    color: ['#00D39C', '#1890FF'],
    tooltip: { trigger: 'axis' },
    legend: { data: ['工单量', '成交额'] },
    xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
    yAxis: { type: 'value' },
    series: [{ name: '工单量', type: 'line', smooth: true, data: [320, 410, 430, 520, 690, 760] }, { name: '成交额', type: 'line', smooth: true, data: [26, 31, 38, 44, 58, 72] }]
  })
}

onMounted(async () => {
  try {
    const data = await getAnalytics()
    if (data.metrics) metrics.value = data.metrics
  } catch (error) {}
  await nextTick()
  draw()
})
</script>
