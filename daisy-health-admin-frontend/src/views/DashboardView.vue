<template>
  <section>
    <div class="page-heading">
      <div>
        <h1>工作台</h1>
        <p>本周用户、订单、工单与内容运营概览</p>
      </div>
      <el-date-picker v-model="range" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" />
    </div>

    <div class="metric-grid">
      <article v-for="item in dashboard.metrics" :key="item.label" class="metric-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <el-tag :type="item.tone">{{ item.delta }} 较上周</el-tag>
      </article>
    </div>

    <div class="quick-grid">
      <el-button v-for="item in quickLinks" :key="item.path" @click="$router.push(item.path)">
        <el-icon><component :is="item.icon" /></el-icon>{{ item.label }}
      </el-button>
    </div>

    <div class="dashboard-grid">
      <section class="panel"><h2>用户标签分布</h2><div ref="tagChart" class="chart"></div></section>
      <section class="panel"><h2>服务类型订单占比</h2><div ref="shareChart" class="chart"></div></section>
      <section class="panel wide"><h2>用户与订单趋势</h2><div ref="trendChart" class="chart"></div></section>
    </div>
  </section>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getDashboard } from '../api/http'
import { fallbackDashboard } from '../api/fallback'

const range = ref([])
const dashboard = ref(fallbackDashboard)
const tagChart = ref()
const shareChart = ref()
const trendChart = ref()
const quickLinks = [
  { label: '全部用户', path: '/users', icon: 'User' },
  { label: '报告管理', path: '/operations/articles', icon: 'Document' },
  { label: '会话', path: '/service/work-orders', icon: 'ChatLineRound' },
  { label: '全部订单', path: '/trade/orders', icon: 'Tickets' },
  { label: '工单管理', path: '/service/work-orders', icon: 'Finished' },
  { label: '审核管理', path: '/service/audits', icon: 'Stamp' },
  { label: '售后管理', path: '/trade/after-sales', icon: 'RefreshLeft' },
  { label: '动态管理', path: '/operations/posts', icon: 'Collection' }
]

function draw() {
  const data = dashboard.value
  echarts.init(tagChart.value).setOption({
    grid: { left: 80, right: 24, top: 20, bottom: 24 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: data.tagDistribution.map((item) => item.name) },
    series: [{ type: 'bar', data: data.tagDistribution.map((item) => item.value), itemStyle: { color: '#00D39C', borderRadius: 4 } }]
  })
  echarts.init(shareChart.value).setOption({
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: ['46%', '72%'], data: data.serviceShare, color: ['#00D39C', '#1890FF', '#FAAD14'] }]
  })
  echarts.init(trendChart.value).setOption({
    color: ['#00D39C', '#1890FF'],
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '新增订单'] },
    grid: { left: 42, right: 24, top: 42, bottom: 30 },
    xAxis: { type: 'category', data: data.trend.map((item) => item.day) },
    yAxis: { type: 'value' },
    series: [
      { name: '新增用户', type: 'line', areaStyle: {}, smooth: true, data: data.trend.map((item) => item.users) },
      { name: '新增订单', type: 'line', areaStyle: {}, smooth: true, data: data.trend.map((item) => item.orders) }
    ]
  })
}

onMounted(async () => {
  try {
    dashboard.value = await getDashboard()
  } catch (error) {
    dashboard.value = fallbackDashboard
  }
  await nextTick()
  draw()
})
</script>
