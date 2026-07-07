<template>
  <section>
    <div class="page-heading">
      <div><h1>用户详情</h1><p>个人信息、健康数据、资产和服务记录</p></div>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    <div class="detail-layout">
      <aside class="profile-panel">
        <el-avatar :size="88">{{ user.realName?.slice(0, 1) }}</el-avatar>
        <h2>{{ user.nickname }}</h2>
        <p>ID {{ user.id }} · {{ user.realName }}</p>
        <div class="tag-row"><el-tag v-for="tag in user.tags" :key="tag">{{ tag }}</el-tag></div>
        <dl>
          <dt>手机号</dt><dd>{{ user.phone }}</dd>
          <dt>最近购买</dt><dd>{{ user.lastBuyTime }}</dd>
          <dt>最近登录</dt><dd>{{ user.lastLoginTime }}</dd>
        </dl>
      </aside>
      <main class="panel detail-main">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="个人信息" name="profile"><el-descriptions :column="2" border><el-descriptions-item v-for="item in profileFields" :key="item.label" :label="item.label">{{ item.value }}</el-descriptions-item></el-descriptions></el-tab-pane>
          <el-tab-pane label="健康信息" name="health">
            <div class="tab-actions"><el-button type="primary" @click="openHealthEdit">编辑健康信息</el-button></div>
            <el-descriptions :column="2" border><el-descriptions-item label="身高">{{ user.height }} cm</el-descriptions-item><el-descriptions-item label="体重">{{ user.weight }} kg</el-descriptions-item><el-descriptions-item label="血型">{{ user.bloodType }}</el-descriptions-item><el-descriptions-item label="慢性病">{{ user.chronicDisease }}</el-descriptions-item><el-descriptions-item label="睡眠质量">{{ user.sleepQuality }}</el-descriptions-item><el-descriptions-item label="运动频率">{{ user.exerciseFreq }}</el-descriptions-item></el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="用药信息" name="medication"><el-table :data="user.medications" stripe><el-table-column prop="period" label="时段" /><el-table-column prop="drugName" label="药品名称" /><el-table-column prop="frequency" label="频率" /><el-table-column prop="takeTime" label="时间" /><el-table-column prop="dosage" label="计量" /><el-table-column label="提醒"><template #default="{ row }"><el-switch v-model="row.reminderEnabled" /></template></el-table-column></el-table></el-tab-pane>
          <el-tab-pane label="健康数据" name="data"><div ref="healthChart" class="chart tall"></div></el-tab-pane>
          <el-tab-pane label="设备信息" name="devices"><el-table :data="user.devices" stripe><el-table-column prop="deviceName" label="设备名称" /><el-table-column prop="deviceType" label="类型" /><el-table-column prop="deviceCode" label="编号" /><el-table-column prop="status" label="状态" /></el-table></el-tab-pane>
          <el-tab-pane label="报告信息" name="reports"><el-table :data="user.reports" stripe><el-table-column prop="title" label="报告标题" /><el-table-column prop="reportType" label="类型" /><el-table-column prop="reportDate" label="日期" /><el-table-column prop="doctorName" label="医生" /></el-table></el-tab-pane>
          <el-tab-pane label="订单信息" name="orders"><el-table :data="user.orders" stripe><el-table-column prop="orderNo" label="订单编号" /><el-table-column prop="productName" label="商品" /><el-table-column prop="amount" label="金额" /><el-table-column prop="status" label="状态" /></el-table></el-tab-pane>
          <el-tab-pane label="资产信息" name="assets">
            <el-descriptions v-if="user.points?.length" :column="3" border class="asset-summary"><el-descriptions-item label="积分">{{ user.points[0].points }}</el-descriptions-item><el-descriptions-item label="等级">{{ user.points[0].level }}</el-descriptions-item><el-descriptions-item label="成长值">{{ user.points[0].growthValue }}</el-descriptions-item></el-descriptions>
            <el-table :data="user.coupons" stripe><el-table-column prop="couponNo" label="券编号" /><el-table-column prop="name" label="名称" /><el-table-column prop="discount" label="优惠" /><el-table-column prop="status" label="状态" /><el-table-column prop="expireDate" label="过期日" /></el-table>
          </el-tab-pane>
          <el-tab-pane label="内容信息" name="content"><el-table :data="user.contents" stripe><el-table-column prop="title" label="标题" /><el-table-column prop="type" label="类型" /><el-table-column prop="status" label="状态" /></el-table></el-tab-pane>
          <el-tab-pane label="服务记录" name="services"><el-table :data="user.serviceRecords" stripe><el-table-column prop="orderNo" label="工单编号" /><el-table-column prop="serviceItem" label="服务项目" /><el-table-column prop="amount" label="金额" /><el-table-column prop="status" label="状态" /></el-table></el-tab-pane>
        </el-tabs>
      </main>
    </div>

    <el-dialog v-model="healthDialogVisible" title="编辑健康信息" width="560px">
      <el-form :model="healthForm" label-width="96px">
        <el-form-item label="身高(cm)"><el-input-number v-model="healthForm.height" :min="0" controls-position="right" /></el-form-item>
        <el-form-item label="体重(kg)"><el-input-number v-model="healthForm.weight" :min="0" controls-position="right" /></el-form-item>
        <el-form-item label="血型"><el-select v-model="healthForm.bloodType"><el-option label="A" value="A" /><el-option label="B" value="B" /><el-option label="O" value="O" /><el-option label="AB" value="AB" /></el-select></el-form-item>
        <el-form-item label="慢性病"><el-input v-model="healthForm.chronicDisease" /></el-form-item>
        <el-form-item label="睡眠质量"><el-select v-model="healthForm.sleepQuality"><el-option label="良好" value="良好" /><el-option label="一般" value="一般" /><el-option label="较差" value="较差" /></el-select></el-form-item>
        <el-form-item label="运动频率"><el-input v-model="healthForm.exerciseFreq" /></el-form-item>
        <el-form-item label="饮食偏好"><el-input v-model="healthForm.dietPreference" /></el-form-item>
        <el-form-item label="紧急联系人"><el-input v-model="healthForm.emergencyContact" /></el-form-item>
        <el-form-item label="紧急电话"><el-input v-model="healthForm.emergencyPhone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="healthDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingHealth" @click="saveHealth">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getUser, updateUser } from '../api/http'

const route = useRoute()
const activeTab = ref('profile')
const healthChart = ref()
const healthDialogVisible = ref(false)
const savingHealth = ref(false)
const healthForm = reactive({ height: 0, weight: 0, bloodType: 'A', chronicDisease: '', sleepQuality: '良好', exerciseFreq: '', dietPreference: '', emergencyContact: '', emergencyPhone: '' })
const user = ref({
  id: route.params.id,
  nickname: '兰姨',
  realName: '王秀兰',
  gender: '女',
  birthday: '1952-03-12',
  phone: '138****0001',
  address: '上海市浦东新区',
  height: 158,
  weight: 58.4,
  bloodType: 'A',
  chronicDisease: '高血压',
  sleepQuality: '良好',
  exerciseFreq: '每周3次',
  lastBuyTime: '2026-07-02',
  lastLoginTime: '2026-07-06 08:30',
  tags: ['高血压', '重点关怀'],
  medications: [
    { period: '早餐', drugName: '硝苯地平控释片', frequency: '每天', takeTime: '08:00', dosage: '1片', reminderEnabled: true },
    { period: '晚餐', drugName: '阿司匹林', frequency: '每天', takeTime: '19:00', dosage: '1片', reminderEnabled: true }
  ],
  healthData: [
    { day: '07-01', weight: 58.8, heartRate: 76 },
    { day: '07-02', weight: 58.6, heartRate: 74 },
    { day: '07-03', weight: 58.5, heartRate: 78 },
    { day: '07-04', weight: 58.4, heartRate: 75 },
    { day: '07-05', weight: 58.3, heartRate: 73 }
  ],
  devices: [],
  reports: [],
  orders: [],
  coupons: [],
  points: [],
  contents: [],
  serviceRecords: []
})
const profileFields = computed(() => [
  { label: '昵称', value: user.value.nickname },
  { label: '真实姓名', value: user.value.realName },
  { label: '性别', value: user.value.gender },
  { label: '出生日期', value: user.value.birthday },
  { label: '手机号', value: user.value.phone },
  { label: '家庭住址', value: user.value.address },
  { label: '民族', value: '汉族' },
  { label: '文化程度', value: '高中' }
])
function drawHealthChart() {
  echarts.init(healthChart.value).setOption({
    color: ['#00D39C', '#FAAD14'],
    tooltip: { trigger: 'axis' },
    legend: { data: ['体重', '心率'] },
    grid: { left: 42, right: 24, top: 42, bottom: 30 },
    xAxis: { type: 'category', data: user.value.healthData.map((item) => item.day) },
    yAxis: [{ type: 'value' }, { type: 'value' }],
    series: [
      { name: '体重', type: 'line', smooth: true, data: user.value.healthData.map((item) => item.weight) },
      { name: '心率', type: 'line', smooth: true, yAxisIndex: 1, data: user.value.healthData.map((item) => item.heartRate) }
    ]
  })
}
function openHealthEdit() {
  Object.assign(healthForm, {
    height: Number(user.value.height || 0),
    weight: Number(user.value.weight || 0),
    bloodType: user.value.bloodType || 'A',
    chronicDisease: user.value.chronicDisease || '',
    sleepQuality: user.value.sleepQuality || '良好',
    exerciseFreq: user.value.exerciseFreq || '',
    dietPreference: user.value.dietPreference || '',
    emergencyContact: user.value.emergencyContact || '',
    emergencyPhone: user.value.emergencyPhone || ''
  })
  healthDialogVisible.value = true
}
async function loadUser() {
  user.value = await getUser(route.params.id)
}
async function saveHealth() {
  savingHealth.value = true
  try {
    await updateUser(route.params.id, healthForm)
    await loadUser()
    healthDialogVisible.value = false
    ElMessage.success('健康信息已保存')
  } catch (error) {
    ElMessage.error('保存失败，请确认后端和数据库已启动')
  } finally {
    savingHealth.value = false
  }
}

onMounted(async () => {
  try {
    await loadUser()
  } catch (error) {}
  await nextTick()
  drawHealthChart()
})
</script>
