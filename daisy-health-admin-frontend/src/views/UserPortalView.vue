<template>
  <main class="portal-page">
    <header class="portal-header">
      <div>
        <p>用户端</p>
        <h1>{{ profile.realName || auth.user?.name || '我的健康' }}</h1>
      </div>
      <el-button @click="logout">
        <el-icon><SwitchButton /></el-icon>
        退出
      </el-button>
    </header>

    <el-alert v-if="error" :title="error" type="error" show-icon />

    <section class="summary-grid">
      <article class="summary-card green">
        <span>当前积分</span>
        <strong>{{ points.points ?? 0 }}</strong>
        <small>{{ points.level || '普通' }}</small>
      </article>
      <article class="summary-card coral">
        <span>我的订单</span>
        <strong>{{ orders.length }}</strong>
        <small>只显示本人订单</small>
      </article>
      <article class="summary-card blue">
        <span>绑定设备</span>
        <strong>{{ devices.length }}</strong>
        <small>只显示本人设备</small>
      </article>
    </section>

    <el-tabs v-model="activeTab" class="portal-tabs">
      <el-tab-pane label="个人资料" name="profile">
        <section class="panel">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="手机号">{{ profile.phone }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ profile.gender }}</el-descriptions-item>
            <el-descriptions-item label="生日">{{ profile.birthday }}</el-descriptions-item>
            <el-descriptions-item label="地址">{{ profile.address }}</el-descriptions-item>
            <el-descriptions-item label="慢病">{{ profile.chronicDisease }}</el-descriptions-item>
            <el-descriptions-item label="紧急电话">{{ profile.emergencyPhone }}</el-descriptions-item>
          </el-descriptions>
        </section>
      </el-tab-pane>
      <el-tab-pane label="健康数据" name="health">
        <data-table :rows="healthData" :columns="healthColumns" />
      </el-tab-pane>
      <el-tab-pane label="用药记录" name="medications">
        <data-table :rows="medications" :columns="medicationColumns" />
      </el-tab-pane>
      <el-tab-pane label="设备报告" name="devices">
        <data-table :rows="devices" :columns="deviceColumns" />
        <data-table :rows="reports" :columns="reportColumns" title="健康报告" />
      </el-tab-pane>
      <el-tab-pane label="订单资产" name="assets">
        <data-table :rows="orders" :columns="orderColumns" />
        <data-table :rows="coupons" :columns="couponColumns" title="优惠券" />
      </el-tab-pane>
    </el-tabs>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import {
  getElderlyCoupons,
  getElderlyDevices,
  getElderlyHealthData,
  getElderlyMedications,
  getElderlyOrders,
  getElderlyPoints,
  getElderlyProfile,
  getElderlyReports
} from '../api/http'

const DataTable = {
  props: { rows: { type: Array, default: () => [] }, columns: { type: Array, default: () => [] }, title: String },
  template: `
    <section class="panel">
      <h2 v-if="title">{{ title }}</h2>
      <el-table :data="rows" stripe>
        <el-table-column v-for="column in columns" :key="column.prop" :prop="column.prop" :label="column.label" :min-width="column.width || 120" />
      </el-table>
    </section>
  `
}

const router = useRouter()
const auth = useAuthStore()
const activeTab = ref('profile')
const error = ref('')
const profile = ref({})
const healthData = ref([])
const medications = ref([])
const devices = ref([])
const reports = ref([])
const orders = ref([])
const coupons = ref([])
const points = ref({})

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
  { prop: 'status', label: '状态' },
  { prop: 'serviceTime', label: '服务时间' }
]
const couponColumns = [
  { prop: 'couponNo', label: '券编号' },
  { prop: 'name', label: '名称' },
  { prop: 'discount', label: '优惠' },
  { prop: 'minAmount', label: '门槛' },
  { prop: 'status', label: '状态' },
  { prop: 'expireDate', label: '有效期' }
]

async function loadData() {
  try {
    const [profileData, health, medicationData, deviceData, reportData, orderData, couponData, pointData] = await Promise.all([
      getElderlyProfile(),
      getElderlyHealthData(),
      getElderlyMedications(),
      getElderlyDevices(),
      getElderlyReports(),
      getElderlyOrders(),
      getElderlyCoupons(),
      getElderlyPoints()
    ])
    profile.value = profileData
    healthData.value = health
    medications.value = medicationData
    devices.value = deviceData
    reports.value = reportData
    orders.value = orderData
    coupons.value = couponData
    points.value = pointData
  } catch (err) {
    error.value = err.message || '加载失败'
  }
}

function logout() {
  auth.signOut()
  router.push('/login')
}

onMounted(loadData)
</script>

<style scoped>
.portal-page {
  min-height: 100vh;
  padding: 28px;
  background: #f3f7f6;
}

.portal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.portal-header p {
  margin: 0 0 6px;
  color: #6f7e87;
}

.portal-header h1 {
  margin: 0;
  font-size: 30px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.summary-card {
  min-height: 124px;
  padding: 20px;
  border-radius: 8px;
  color: #fff;
}

.summary-card span,
.summary-card small {
  display: block;
}

.summary-card strong {
  display: block;
  margin: 12px 0 4px;
  font-size: 34px;
}

.green { background: #159a84; }
.coral { background: #e56a54; }
.blue { background: #3578c8; }

.portal-tabs {
  padding: 18px;
  border: 1px solid #e2eaee;
  border-radius: 8px;
  background: #fff;
}

.panel {
  margin-bottom: 16px;
}

.panel h2 {
  margin: 0 0 12px;
  font-size: 18px;
}
</style>
