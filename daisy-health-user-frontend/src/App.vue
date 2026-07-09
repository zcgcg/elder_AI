<template>
  <main v-if="!session.token" class="login-shell">
    <section class="login-visual">
      <div class="brand-mark">Daisy Health</div>
      <div class="login-copy">
        <h1>健康用户端</h1>
        <p>个人健康、服务订单、设备与资产集中查看。</p>
      </div>
    </section>
    <section class="login-panel">
      <el-form :model="loginForm" label-position="top" @submit.prevent="submitLogin">
        <h2>登录</h2>
        <el-form-item label="手机号">
          <el-input v-model="loginForm.phone" size="large" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" size="large" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-alert v-if="loginError" :title="loginError" type="error" :closable="false" />
        <el-button class="wide-button" type="primary" size="large" :loading="loading.login" @click="submitLogin">
          <el-icon><Unlock /></el-icon>
          登录用户端
        </el-button>
      </el-form>
    </section>
  </main>

  <main v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-icon">D</span>
        <div>
          <strong>黛西健康</strong>
          <small>用户端</small>
        </div>
      </div>
      <nav class="nav-list">
        <button v-for="item in navItems" :key="item.key" :class="{ active: activeTab === item.key }" @click="activeTab = item.key">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </nav>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div>
          <h1>{{ currentTitle }}</h1>
          <p>{{ todayText }}</p>
        </div>
        <div class="account">
          <img :src="avatarUrl" alt="用户头像" />
          <div>
            <strong>{{ profile.realName || session.user?.name || '用户' }}</strong>
            <small>{{ profile.phone || session.user?.phone }}</small>
          </div>
          <el-button circle title="退出登录" @click="logout">
            <el-icon><SwitchButton /></el-icon>
          </el-button>
        </div>
      </header>

      <el-alert v-if="pageError" :title="pageError" type="error" show-icon />

      <section v-if="activeTab === 'overview'" class="page-grid">
        <article class="summary-card accent-green">
          <span>当前积分</span>
          <strong>{{ points.points ?? 0 }}</strong>
          <small>{{ points.level || '普通' }}</small>
        </article>
        <article class="summary-card accent-coral">
          <span>待服务订单</span>
          <strong>{{ pendingOrders }}</strong>
          <small>来自本人订单</small>
        </article>
        <article class="summary-card accent-blue">
          <span>绑定设备</span>
          <strong>{{ devices.length }}</strong>
          <small>最近同步 {{ lastSyncText }}</small>
        </article>
        <article class="panel wide">
          <div class="panel-head">
            <h2>最近健康数据</h2>
            <el-button text @click="activeTab = 'health'">查看全部</el-button>
          </div>
          <div class="health-strip">
            <div v-for="item in healthData.slice(0, 8)" :key="item.id" class="metric">
              <span>{{ dataTypeLabel(item.dataType) }}</span>
              <strong>{{ item.value }}{{ item.unit }}</strong>
              <small>{{ item.recordDate }}</small>
            </div>
          </div>
        </article>
        <article class="panel">
          <div class="panel-head">
            <h2>今日用药</h2>
            <el-icon><FirstAidKit /></el-icon>
          </div>
          <ul class="clean-list">
            <li v-for="item in medications.slice(0, 4)" :key="item.id">
              <span>{{ item.drugName }}</span>
              <small>{{ item.period }} {{ item.takeTime }} {{ item.dosage }}</small>
            </li>
          </ul>
        </article>
      </section>

      <section v-if="activeTab === 'profile'" class="panel profile-panel">
        <div class="profile-hero">
          <img :src="avatarUrl" alt="用户头像" />
          <div>
            <h2>{{ profile.realName }}</h2>
            <p>{{ profile.address || '未填写地址' }}</p>
          </div>
        </div>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="手机号">{{ profile.phone }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ profile.gender }}</el-descriptions-item>
          <el-descriptions-item label="生日">{{ profile.birthday }}</el-descriptions-item>
          <el-descriptions-item label="身高">{{ profile.height }} cm</el-descriptions-item>
          <el-descriptions-item label="体重">{{ profile.weight }} kg</el-descriptions-item>
          <el-descriptions-item label="慢病">{{ profile.chronicDisease }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系人">{{ profile.emergencyContact }}</el-descriptions-item>
          <el-descriptions-item label="紧急电话">{{ profile.emergencyPhone }}</el-descriptions-item>
        </el-descriptions>
      </section>

      <data-table v-if="activeTab === 'health'" :rows="healthData" :columns="healthColumns" />
      <data-table v-if="activeTab === 'medications'" :rows="medications" :columns="medicationColumns" />
      <data-table v-if="activeTab === 'devices'" :rows="devices" :columns="deviceColumns" />
      <data-table v-if="activeTab === 'reports'" :rows="reports" :columns="reportColumns" />
      <data-table v-if="activeTab === 'orders'" :rows="orders" :columns="orderColumns" />

      <section v-if="activeTab === 'assets'" class="page-grid">
        <article class="summary-card accent-green">
          <span>可用积分</span>
          <strong>{{ points.points ?? 0 }}</strong>
          <small>累计获得 {{ points.totalEarned ?? 0 }}</small>
        </article>
        <article class="summary-card accent-coral">
          <span>优惠券</span>
          <strong>{{ coupons.length }}</strong>
          <small>仅展示本人资产</small>
        </article>
        <article class="panel wide">
          <div class="panel-head">
            <h2>优惠券</h2>
            <el-icon><Ticket /></el-icon>
          </div>
          <data-table :rows="coupons" :columns="couponColumns" embedded />
        </article>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getAuthProfile,
  getCoupons,
  getDevices,
  getHealthData,
  getMedications,
  getOrders,
  getPoints,
  getProfile,
  getReports,
  login,
  tokenKey
} from './api'

const DataTable = {
  props: { rows: { type: Array, default: () => [] }, columns: { type: Array, default: () => [] }, embedded: Boolean },
  template: `
    <section :class="embedded ? 'table-embed' : 'panel'">
      <el-table :data="rows" height="100%" stripe>
        <el-table-column v-for="column in columns" :key="column.prop" :prop="column.prop" :label="column.label" :min-width="column.width || 120" />
      </el-table>
    </section>
  `
}

const session = reactive({ token: localStorage.getItem(tokenKey), user: null })
const loginForm = reactive({ phone: '13800010001', password: '753951' })
const loading = reactive({ login: false, page: false })
const loginError = ref('')
const pageError = ref('')
const activeTab = ref('overview')
const profile = ref({})
const healthData = ref([])
const medications = ref([])
const devices = ref([])
const reports = ref([])
const orders = ref([])
const coupons = ref([])
const points = ref({})

const navItems = [
  { key: 'overview', label: '总览', icon: 'DataBoard' },
  { key: 'profile', label: '资料', icon: 'User' },
  { key: 'health', label: '健康', icon: 'TrendCharts' },
  { key: 'medications', label: '用药', icon: 'FirstAidKit' },
  { key: 'devices', label: '设备', icon: 'Watch' },
  { key: 'reports', label: '报告', icon: 'Document' },
  { key: 'orders', label: '订单', icon: 'Tickets' },
  { key: 'assets', label: '资产', icon: 'Wallet' }
]

const healthColumns = [
  { prop: 'dataType', label: '类型' },
  { prop: 'value', label: '数值' },
  { prop: 'unit', label: '单位' },
  { prop: 'recordDate', label: '日期' },
  { prop: 'recordTime', label: '时间' },
  { prop: 'source', label: '来源' }
]
const medicationColumns = [
  { prop: 'period', label: '时段' },
  { prop: 'drugName', label: '药品' },
  { prop: 'frequency', label: '频次' },
  { prop: 'takeTime', label: '时间' },
  { prop: 'dosage', label: '剂量' }
]
const deviceColumns = [
  { prop: 'deviceName', label: '设备' },
  { prop: 'deviceType', label: '类型' },
  { prop: 'deviceCode', label: '编号' },
  { prop: 'lastSyncTime', label: '同步时间' },
  { prop: 'status', label: '状态' }
]
const reportColumns = [
  { prop: 'title', label: '报告' },
  { prop: 'reportType', label: '类型' },
  { prop: 'reportDate', label: '日期' },
  { prop: 'doctorName', label: '医生' },
  { prop: 'summary', label: '摘要', width: 220 }
]
const orderColumns = [
  { prop: 'orderNo', label: '订单号' },
  { prop: 'productName', label: '项目' },
  { prop: 'amount', label: '金额' },
  { prop: 'serviceType', label: '服务类型' },
  { prop: 'status', label: '订单状态' },
  { prop: 'serviceTime', label: '服务时间' }
]
const couponColumns = [
  { prop: 'couponNo', label: '券编号' },
  { prop: 'name', label: '名称' },
  { prop: 'type', label: '类型' },
  { prop: 'discount', label: '优惠' },
  { prop: 'minAmount', label: '门槛' },
  { prop: 'status', label: '状态' },
  { prop: 'expireDate', label: '有效期' }
]

const currentTitle = computed(() => navItems.find((item) => item.key === activeTab.value)?.label || '总览')
const todayText = computed(() => new Intl.DateTimeFormat('zh-CN', { dateStyle: 'full' }).format(new Date()))
const avatarUrl = computed(() => profile.value.avatarUrl || `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(profile.value.realName || 'Daisy')}`)
const pendingOrders = computed(() => orders.value.filter((item) => ['pending_accept', 'pending_service'].includes(item.status)).length)
const lastSyncText = computed(() => devices.value.find((item) => item.lastSyncTime)?.lastSyncTime || '暂无')

async function submitLogin() {
  loginError.value = ''
  loading.login = true
  try {
    const result = await login(loginForm)
    if (result.user?.roleType !== 'elderly') throw new Error('该账号不是用户端账号')
    localStorage.setItem(tokenKey, result.token)
    session.token = result.token
    session.user = result.user
    await loadAll()
  } catch (error) {
    loginError.value = error.message || '登录失败'
  } finally {
    loading.login = false
  }
}

async function loadAll() {
  pageError.value = ''
  loading.page = true
  try {
    const auth = await getAuthProfile()
    if (auth.roleType !== 'elderly') throw new Error('当前登录状态不属于用户端')
    session.user = auth
    const [profileData, health, medicationData, deviceData, reportData, orderData, couponData, pointData] = await Promise.all([
      getProfile(),
      getHealthData(),
      getMedications(),
      getDevices(),
      getReports(),
      getOrders(),
      getCoupons(),
      getPoints()
    ])
    profile.value = profileData
    healthData.value = health
    medications.value = medicationData
    devices.value = deviceData
    reports.value = reportData
    orders.value = orderData
    coupons.value = couponData
    points.value = pointData
  } catch (error) {
    pageError.value = error.message || '加载失败'
    if (/token|登录状态|账号/.test(pageError.value)) logout(false)
  } finally {
    loading.page = false
  }
}

function logout(showMessage = true) {
  localStorage.removeItem(tokenKey)
  session.token = ''
  session.user = null
  if (showMessage) ElMessage.success('已退出')
}

function dataTypeLabel(type) {
  const labels = { weight: '体重', heart_rate: '心率', blood_pressure: '血压', blood_sugar: '血糖' }
  return labels[type] || type
}

onMounted(() => {
  if (session.token) loadAll()
})
</script>
