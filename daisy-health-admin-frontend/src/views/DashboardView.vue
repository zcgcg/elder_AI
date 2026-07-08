<template>
  <section class="dashboard-page">
    <div class="dashboard-greeting">👏 早上好！Daisy</div>

    <div class="metric-grid dashboard-metrics">
      <article v-for="(item, index) in dashboard.metrics" :key="item.label" class="metric-card rich-metric">
        <div>
          <span>{{ item.label }}数量</span>
          <strong>{{ item.value }}</strong>
          <em :class="{ down: String(item.delta).includes('-') }">较上周 {{ item.delta }}</em>
        </div>
        <div class="metric-visual" :class="`visual-${index}`">
          <i v-for="bar in 5" :key="bar"></i>
        </div>
      </article>
    </div>

    <div class="dashboard-middle">
      <section class="panel quick-panel">
        <header><h2>快捷入口</h2><el-icon><Setting /></el-icon></header>
        <div class="quick-entry-grid">
          <button v-for="item in quickLinks" :key="item.path" class="quick-entry" type="button" @click="$router.push(item.path)">
            <span :class="item.tone"><el-icon><component :is="item.icon" /></el-icon></span>
            <b>{{ item.label }}</b>
          </button>
        </div>
      </section>

      <section class="panel tag-panel">
        <h2>用户标签分布</h2>
        <div class="tag-bars">
          <div v-for="item in topTags" :key="item.name" class="tag-bar-row">
            <span>{{ item.name }}</span>
            <i><b :style="{ width: `${tagPercent(item.value)}%` }"></b></i>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
      </section>

      <section class="panel share-panel">
        <h2>各服务类型商品订单量占比</h2>
        <div ref="shareChart" class="chart compact-chart"></div>
      </section>
    </div>

    <section class="panel trend-panel">
      <h2>用户趋势统计</h2>
      <div ref="trendChart" class="chart trend-chart"></div>
    </section>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { Setting } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDashboard } from '../api/http'
import { fallbackDashboard } from '../api/fallback'

const dashboard = ref(fallbackDashboard)
const shareChart = ref()
const trendChart = ref()
const quickLinks = [
  { label: '全部用户', path: '/users', icon: 'User', tone: 'mint' },
  { label: '报告管理', path: '/user-health/reports', icon: 'DocumentCopy', tone: 'cream' },
  { label: '会话', path: '/service/work-orders', icon: 'Message', tone: 'rose' },
  { label: '全部订单', path: '/trade/orders', icon: 'Coin', tone: 'violet' },
  { label: '工单管理', path: '/service/work-orders', icon: 'Document', tone: 'blue' },
  { label: '审核管理', path: '/service/audits', icon: 'Refresh', tone: 'green' },
  { label: '售后管理', path: '/trade/after-sales', icon: 'Star', tone: 'yellow' },
  { label: '动态管理', path: '/operations/posts', icon: 'Promotion', tone: 'red' }
]
const topTags = computed(() => (dashboard.value.tagDistribution || []).slice(0, 6))
const maxTagValue = computed(() => Math.max(1, ...topTags.value.map((item) => Number(item.value || 0))))

function tagPercent(value) {
  return Math.max(12, Math.round((Number(value || 0) / maxTagValue.value) * 100))
}
function draw() {
  echarts.getInstanceByDom(shareChart.value)?.dispose()
  echarts.getInstanceByDom(trendChart.value)?.dispose()
  echarts.init(shareChart.value).setOption({
    tooltip: { trigger: 'item' },
    legend: { right: 0, top: 'middle', orient: 'vertical', icon: 'circle' },
    series: [{
      type: 'pie',
      radius: ['0%', '72%'],
      center: ['34%', '52%'],
      label: { show: false },
      data: dashboard.value.serviceShare,
      color: ['#54d39d', '#ffda6b', '#f66d67', '#6e6df7']
    }]
  })
  echarts.init(trendChart.value).setOption({
    color: ['#52d6ad'],
    tooltip: { trigger: 'axis' },
    legend: { bottom: 4, data: ['新增用户数量'] },
    grid: { left: 42, right: 32, top: 50, bottom: 56 },
    xAxis: { type: 'category', boundaryGap: false, data: dashboard.value.trend.map((item) => item.day), axisLine: { lineStyle: { color: '#e8eeee' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#eef3f2' } } },
    series: [{
      name: '新增用户数量',
      type: 'line',
      smooth: true,
      symbolSize: 8,
      lineStyle: { width: 3 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(82, 214, 173, .45)' },
          { offset: 1, color: 'rgba(82, 214, 173, .04)' }
        ])
      },
      data: dashboard.value.trend.map((item) => item.users)
    }]
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
