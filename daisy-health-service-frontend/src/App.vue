<template>
  <main v-if="!session.token" class="login-shell">
    <section class="login-visual">
      <div class="brand-mark">Daisy Health</div>
      <div class="login-copy">
        <h1>服务人员端</h1>
        <p>查看个人工单、客户信息和服务进度。</p>
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
          登录服务端
        </el-button>
      </el-form>
    </section>
  </main>

  <main v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-icon">S</span>
        <div>
          <strong>黛西健康</strong>
          <small>服务人员端</small>
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
          <img :src="avatarUrl" alt="服务人员头像" />
          <div>
            <strong>{{ profile.realName || session.user?.name || '服务人员' }}</strong>
            <small>{{ profile.serviceType || session.user?.phone }}</small>
          </div>
          <el-button circle title="退出登录" @click="logout">
            <el-icon><SwitchButton /></el-icon>
          </el-button>
        </div>
      </header>

      <el-alert v-if="pageError" :title="pageError" type="error" show-icon />

      <section v-if="activeTab === 'overview'" class="page-grid">
        <article class="summary-card accent-teal">
          <span>我的工单</span>
          <strong>{{ workOrders.length }}</strong>
          <small>仅统计分配给本人</small>
        </article>
        <article class="summary-card accent-amber">
          <span>待服务</span>
          <strong>{{ countByStatus('pending') }}</strong>
          <small>可开始服务</small>
        </article>
        <article class="summary-card accent-blue">
          <span>服务中</span>
          <strong>{{ countByStatus('service_in') }}</strong>
          <small>进行中的工单</small>
        </article>
        <article class="panel wide">
          <div class="panel-head">
            <h2>今日排班</h2>
            <el-button text @click="activeTab = 'orders'">查看工单</el-button>
          </div>
          <div class="timeline">
            <div v-for="item in todayOrders" :key="item.id" class="timeline-item" @click="openDetail(item.id)">
              <span>{{ item.serviceTime || '未排期' }}</span>
              <strong>{{ item.serviceItem }}</strong>
              <small>{{ item.customerName }} · {{ statusLabel(item.status) }}</small>
            </div>
          </div>
        </article>
        <article class="panel">
          <div class="panel-head">
            <h2>服务资料</h2>
            <el-icon><User /></el-icon>
          </div>
          <ul class="clean-list">
            <li><span>{{ profile.realName }}</span><small>{{ profile.phone }}</small></li>
            <li><span>{{ profile.serviceType }}</span><small>{{ profile.area }}</small></li>
            <li><span>{{ profile.auditStatus }}</span><small>{{ profile.joinTime }}</small></li>
          </ul>
        </article>
      </section>

      <section v-if="activeTab === 'orders'" class="panel">
        <div class="filter-row">
          <el-segmented v-model="statusFilter" :options="statusOptions" />
          <el-button :loading="loading.page" @click="loadAll">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        <el-table :data="filteredOrders" height="calc(100vh - 235px)" stripe @row-click="(row) => openDetail(row.id)">
          <el-table-column prop="orderNo" label="工单号" min-width="150" />
          <el-table-column prop="serviceItem" label="服务项目" min-width="180" />
          <el-table-column prop="customerName" label="客户" min-width="120" />
          <el-table-column prop="customerAddress" label="地址" min-width="220" />
          <el-table-column prop="serviceTime" label="服务时间" min-width="160" />
          <el-table-column prop="status" label="状态" min-width="120">
            <template #default="{ row }">
              <el-tag :type="statusTone(row.status)">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="activeTab === 'detail'" class="detail-layout">
        <article class="panel detail-panel">
          <div class="panel-head">
            <h2>{{ selectedOrder.orderNo }}</h2>
            <el-tag :type="statusTone(selectedOrder.status)">{{ statusLabel(selectedOrder.status) }}</el-tag>
          </div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="服务项目">{{ selectedOrder.serviceItem }}</el-descriptions-item>
            <el-descriptions-item label="金额">{{ selectedOrder.amount }}</el-descriptions-item>
            <el-descriptions-item label="客户">{{ selectedOrder.customerName }}</el-descriptions-item>
            <el-descriptions-item label="客户电话">{{ selectedOrder.customerPhone }}</el-descriptions-item>
            <el-descriptions-item label="客户地址">{{ selectedOrder.customerAddress }}</el-descriptions-item>
            <el-descriptions-item label="服务时间">{{ selectedOrder.serviceTime }}</el-descriptions-item>
            <el-descriptions-item label="派单时间">{{ selectedOrder.dispatchTime }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">{{ selectedOrder.completeTime }}</el-descriptions-item>
          </el-descriptions>
          <div class="action-row">
            <el-button :disabled="selectedOrder.status !== 'pending'" type="primary" @click="changeStatus('service_in')">
              <el-icon><VideoPlay /></el-icon>
              开始服务
            </el-button>
            <el-button :disabled="selectedOrder.status === 'completed' || selectedOrder.status === 'cancelled'" type="success" @click="changeStatus('completed')">
              <el-icon><CircleCheck /></el-icon>
              完成服务
            </el-button>
            <el-button :disabled="selectedOrder.status === 'completed' || selectedOrder.status === 'cancelled'" type="danger" plain @click="changeStatus('cancelled')">
              <el-icon><CircleClose /></el-icon>
              取消
            </el-button>
          </div>
        </article>
      </section>

      <section v-if="activeTab === 'profile'" class="panel profile-panel">
        <div class="profile-hero">
          <img :src="avatarUrl" alt="服务人员头像" />
          <div>
            <h2>{{ profile.realName }}</h2>
            <p>{{ profile.serviceType }} · {{ profile.area }}</p>
          </div>
        </div>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="手机号">{{ profile.phone }}</el-descriptions-item>
          <el-descriptions-item label="服务类型">{{ profile.serviceType }}</el-descriptions-item>
          <el-descriptions-item label="区域">{{ profile.area }}</el-descriptions-item>
          <el-descriptions-item label="入职时间">{{ profile.joinTime }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">{{ profile.auditStatus }}</el-descriptions-item>
          <el-descriptions-item label="评分">{{ profile.rating || '暂无' }}</el-descriptions-item>
        </el-descriptions>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAuthProfile, getProfile, getWorkOrder, getWorkOrders, login, tokenKey, updateWorkOrderStatus } from './api'

const session = reactive({ token: localStorage.getItem(tokenKey), user: null })
const loginForm = reactive({ phone: '13900020001', password: '753951' })
const loading = reactive({ login: false, page: false, status: false })
const loginError = ref('')
const pageError = ref('')
const activeTab = ref('overview')
const statusFilter = ref('all')
const profile = ref({})
const workOrders = ref([])
const selectedOrder = ref({})

const navItems = [
  { key: 'overview', label: '总览', icon: 'DataBoard' },
  { key: 'orders', label: '工单', icon: 'Tickets' },
  { key: 'profile', label: '资料', icon: 'User' }
]

const statusOptions = [
  { label: '全部', value: 'all' },
  { label: '待服务', value: 'pending' },
  { label: '服务中', value: 'service_in' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' }
]

const currentTitle = computed(() => (activeTab.value === 'detail' ? '工单详情' : navItems.find((item) => item.key === activeTab.value)?.label || '总览'))
const todayText = computed(() => new Intl.DateTimeFormat('zh-CN', { dateStyle: 'full' }).format(new Date()))
const avatarUrl = computed(() => profile.value.avatarUrl || `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(profile.value.realName || 'Service')}`)
const filteredOrders = computed(() => statusFilter.value === 'all' ? workOrders.value : workOrders.value.filter((item) => item.status === statusFilter.value))
const todayOrders = computed(() => workOrders.value.slice(0, 6))

async function submitLogin() {
  loginError.value = ''
  loading.login = true
  try {
    const result = await login(loginForm)
    if (result.user?.roleType !== 'service') throw new Error('该账号不是服务人员端账号')
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
    if (auth.roleType !== 'service') throw new Error('当前登录状态不属于服务人员端')
    session.user = auth
    const [profileData, orderData] = await Promise.all([getProfile(), getWorkOrders()])
    profile.value = profileData
    workOrders.value = orderData
  } catch (error) {
    pageError.value = error.message || '加载失败'
    if (/token|登录状态|账号/.test(pageError.value)) logout(false)
  } finally {
    loading.page = false
  }
}

async function openDetail(id) {
  selectedOrder.value = await getWorkOrder(id)
  activeTab.value = 'detail'
}

async function changeStatus(status) {
  loading.status = true
  try {
    selectedOrder.value = await updateWorkOrderStatus(selectedOrder.value.id, status)
    workOrders.value = workOrders.value.map((item) => (item.id === selectedOrder.value.id ? { ...item, ...selectedOrder.value } : item))
    ElMessage.success('状态已更新')
  } finally {
    loading.status = false
  }
}

function countByStatus(status) {
  return workOrders.value.filter((item) => item.status === status).length
}

function statusLabel(status) {
  const labels = { pending: '待服务', service_in: '服务中', completed: '已完成', cancelled: '已取消' }
  return labels[status] || status
}

function statusTone(status) {
  const tones = { pending: 'warning', service_in: 'primary', completed: 'success', cancelled: 'info' }
  return tones[status] || ''
}

function logout(showMessage = true) {
  localStorage.removeItem(tokenKey)
  session.token = ''
  session.user = null
  if (showMessage) ElMessage.success('已退出')
}

onMounted(() => {
  if (session.token) loadAll()
})
</script>
