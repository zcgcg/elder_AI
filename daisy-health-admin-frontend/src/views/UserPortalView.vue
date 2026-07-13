<template>
  <main class="portal-page">
    <header class="portal-header">
      <div class="portal-identity">
        <div class="portal-avatar-control">
          <el-avatar :size="54" :src="assetUrl(profile.avatarUrl)">{{ profile.realName?.slice(0, 1) || '用' }}</el-avatar>
          <el-button link type="primary" @click="openAvatarEditor">修改头像</el-button>
        </div>
        <div>
        <p>用户端</p>
        <h1>{{ profile.realName || auth.user?.name || '我的健康' }}</h1>
        </div>
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
      <article class="summary-card purple">
        <span>我的工单</span>
        <strong>{{ workOrders.length }}</strong>
        <small>只显示本人工单</small>
      </article>
    </section>

    <el-tabs v-model="activeTab" class="portal-tabs">
      <el-tab-pane label="个人资料" name="profile">
        <section class="panel">
          <div class="panel-heading">
            <h2>完整个人信息</h2>
            <el-button type="primary" @click="openProfileEditor">编辑资料</el-button>
          </div>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="昵称">{{ profile.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="真实姓名">{{ profile.realName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ profile.phone }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ profile.gender }}</el-descriptions-item>
            <el-descriptions-item label="生日">{{ profile.birthday }}</el-descriptions-item>
            <el-descriptions-item label="身份证号">{{ profile.idCard || '-' }}</el-descriptions-item>
            <el-descriptions-item label="地址">{{ profile.address }}</el-descriptions-item>
            <el-descriptions-item label="民族">{{ profile.ethnicity || '-' }}</el-descriptions-item>
            <el-descriptions-item label="文化程度">{{ profile.education || '-' }}</el-descriptions-item>
            <el-descriptions-item label="身高">{{ profile.height ? `${profile.height} cm` : '-' }}</el-descriptions-item>
            <el-descriptions-item label="体重">{{ profile.weight ? `${profile.weight} kg` : '-' }}</el-descriptions-item>
            <el-descriptions-item label="血型">{{ profile.bloodType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="RH阴性">{{ profile.rhNegative ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="慢病">{{ profile.chronicDisease }}</el-descriptions-item>
            <el-descriptions-item label="睡眠质量">{{ profile.sleepQuality || '-' }}</el-descriptions-item>
            <el-descriptions-item label="吸烟频率">{{ profile.smokingFreq || '-' }}</el-descriptions-item>
            <el-descriptions-item label="饮酒频率">{{ profile.drinkingFreq || '-' }}</el-descriptions-item>
            <el-descriptions-item label="运动频率">{{ profile.exerciseFreq || '-' }}</el-descriptions-item>
            <el-descriptions-item label="饮食偏好">{{ profile.dietPreference || '-' }}</el-descriptions-item>
            <el-descriptions-item label="紧急联系人">{{ profile.emergencyContact || '-' }}</el-descriptions-item>
            <el-descriptions-item label="紧急电话">{{ profile.emergencyPhone }}</el-descriptions-item>
            <el-descriptions-item label="个人简介" :span="3">{{ profile.bio || '-' }}</el-descriptions-item>
          </el-descriptions>
        </section>
      </el-tab-pane>
      <el-tab-pane label="社区活动" name="activities">
        <section class="content-grid">
          <article v-for="item in activities" :key="item.id" class="portal-content-card">
            <img v-if="item.coverUrl" :src="assetUrl(item.coverUrl)" :alt="item.title" />
            <div class="content-card-body">
              <div class="content-meta"><el-tag>{{ item.status }}</el-tag><span>{{ item.startTime }}</span></div>
              <h2>{{ item.title }}</h2>
              <p>{{ item.content || '欢迎参加社区活动' }}</p>
              <p><strong>地点：</strong>{{ item.location || '待通知' }}</p>
              <footer>
                <span>已报名 {{ item.enrolled }} / {{ item.quota }} 人</span>
                <el-button type="primary" size="large" :loading="enrollingActivityId === item.id" :disabled="item.joined || !item.canJoin" @click="joinActivity(item)">
                  {{ item.joined ? '已报名' : (item.canJoin ? '加入活动' : '不可报名') }}
                </el-button>
              </footer>
            </div>
          </article>
          <el-empty v-if="!activities.length" description="暂无已发布活动" />
        </section>
      </el-tab-pane>
      <el-tab-pane label="健康资讯" name="articles">
        <section class="content-grid">
          <article v-for="item in healthArticles" :key="item.id" class="portal-content-card">
            <img v-if="item.coverUrl" :src="assetUrl(item.coverUrl)" :alt="item.title" />
            <div class="content-card-body">
              <div class="content-meta"><el-tag type="success">{{ item.category || '健康' }}</el-tag><span>{{ item.createdAt }}</span></div>
              <h2>{{ item.title }}</h2>
              <p>{{ item.summary || item.content || '暂无摘要' }}</p>
              <footer><small>{{ item.author || '健康中心' }}</small><el-button type="primary" plain size="large" @click="openArticle(item)">阅读详情</el-button></footer>
            </div>
          </article>
          <el-empty v-if="!healthArticles.length" description="暂无健康资讯" />
        </section>
      </el-tab-pane>
      <el-tab-pane label="健康讲堂" name="videos">
        <section class="content-grid">
          <article v-for="item in healthVideos" :key="item.id" class="portal-content-card">
            <img v-if="item.coverUrl" :src="assetUrl(item.coverUrl)" :alt="item.title" />
            <div class="content-card-body">
              <div class="content-meta"><el-tag type="warning">{{ item.category || '健康讲堂' }}</el-tag><span>{{ formatDuration(item.duration) }}</span></div>
              <h2>{{ item.title }}</h2>
              <p>{{ item.description || '暂无讲堂简介' }}</p>
              <footer><span>主讲：{{ item.lecturer || '健康专家' }}</span><el-button v-if="item.videoUrl" type="primary" size="large" @click="openVideo(item.videoUrl)">观看讲堂</el-button></footer>
            </div>
          </article>
          <el-empty v-if="!healthVideos.length" description="暂无健康讲堂" />
        </section>
      </el-tab-pane>
      <el-tab-pane label="商品服务" name="catalog">
        <section class="catalog-grid">
          <article v-for="item in catalogItems" :key="item.id" class="catalog-card">
            <div>
              <el-tag size="small">{{ item.itemType || '服务' }}</el-tag>
              <span>{{ item.category }}</span>
            </div>
            <h2>{{ item.name }}</h2>
            <p>{{ item.description || '暂无说明' }}</p>
            <footer>
              <strong>¥{{ item.price }}</strong>
              <small v-if="item.duration">{{ item.duration }} 分钟</small>
              <el-button type="primary" @click="openWorkOrder(item)">创建工单</el-button>
            </footer>
          </article>
        </section>
      </el-tab-pane>
      <el-tab-pane label="我的工单" name="workOrders">
        <el-table :data="workOrders" stripe>
          <el-table-column prop="orderNo" label="工单编号" min-width="160" />
          <el-table-column prop="serviceItem" label="服务项目" min-width="160" />
          <el-table-column prop="customerName" label="客户" min-width="110" />
          <el-table-column prop="amount" label="金额" min-width="80" />
          <el-table-column prop="dispatchTime" label="派单时间" min-width="160" />
          <el-table-column prop="serviceTime" label="服务时间" min-width="160" />
          <el-table-column prop="status" label="状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="statusTone(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="健康数据" name="health">
        <section class="panel">
          <div ref="userHealthChart" class="health-chart"></div>
        </section>
        <data-table :rows="healthData" :columns="healthColumns" />
      </el-tab-pane>
      <el-tab-pane label="用药记录" name="medications">
        <data-table :rows="medications" :columns="medicationColumns" />
      </el-tab-pane>
      <el-tab-pane label="设备报告" name="devices">
        <section class="panel">
          <h2>我的设备</h2>
          <el-table :data="devices" stripe>
            <el-table-column v-for="column in deviceColumns" :key="column.prop" :prop="column.prop" :label="column.label" :min-width="column.width || 120" />
            <el-table-column label="操作" width="90">
              <template #default="{ row }"><el-button link type="primary" @click="openDeviceEditor(row)">编辑</el-button></template>
            </el-table-column>
          </el-table>
        </section>
        <data-table :rows="reports" :columns="reportColumns" title="健康报告" />
      </el-tab-pane>
      <el-tab-pane label="订单资产" name="assets">
        <data-table :rows="orders" :columns="orderColumns" />
        <data-table :rows="coupons" :columns="couponColumns" title="优惠券" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="avatarDialogVisible" title="修改头像" width="680px">
      <avatar-picker ref="avatarPickerRef" :model-value="profile.avatarUrl" :fallback="profile.realName?.slice(0, 1) || '用'" />
      <template #footer>
        <el-button @click="avatarDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="avatarSaving" @click="saveAvatar">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="profileDialogVisible" title="编辑完整个人信息" width="720px">
      <el-form :model="profileForm" label-width="104px">
        <el-form-item label="昵称"><el-input v-model="profileForm.nickname" /></el-form-item>
        <el-form-item label="真实姓名" required><el-input v-model="profileForm.realName" /></el-form-item>
        <el-form-item label="手机号" required><el-input v-model="profileForm.phone" /></el-form-item>
        <el-form-item label="性别"><el-radio-group v-model="profileForm.gender"><el-radio label="女" /><el-radio label="男" /><el-radio label="未知" /></el-radio-group></el-form-item>
        <el-form-item label="出生日期"><el-date-picker v-model="profileForm.birthday" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="profileForm.idCard" /></el-form-item>
        <el-form-item label="家庭住址"><el-input v-model="profileForm.address" /></el-form-item>
        <el-form-item label="个人简介"><el-input v-model="profileForm.bio" type="textarea" /></el-form-item>
        <el-form-item label="民族"><el-input v-model="profileForm.ethnicity" /></el-form-item>
        <el-form-item label="文化程度"><el-input v-model="profileForm.education" /></el-form-item>
        <el-form-item label="身高(cm)"><el-input-number v-model="profileForm.height" :min="0" /></el-form-item>
        <el-form-item label="体重(kg)"><el-input-number v-model="profileForm.weight" :min="0" /></el-form-item>
        <el-form-item label="血型"><el-select v-model="profileForm.bloodType" clearable><el-option v-for="type in ['A', 'B', 'O', 'AB']" :key="type" :label="type" :value="type" /></el-select></el-form-item>
        <el-form-item label="RH阴性"><el-switch v-model="profileForm.rhNegative" /></el-form-item>
        <el-form-item label="慢性病"><el-input v-model="profileForm.chronicDisease" /></el-form-item>
        <el-form-item label="睡眠质量"><el-select v-model="profileForm.sleepQuality" clearable><el-option v-for="item in ['良好', '一般', '较差']" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        <el-form-item label="吸烟频率"><el-input v-model="profileForm.smokingFreq" /></el-form-item>
        <el-form-item label="饮酒频率"><el-input v-model="profileForm.drinkingFreq" /></el-form-item>
        <el-form-item label="运动频率"><el-input v-model="profileForm.exerciseFreq" /></el-form-item>
        <el-form-item label="饮食偏好"><el-input v-model="profileForm.dietPreference" /></el-form-item>
        <el-form-item label="紧急联系人"><el-input v-model="profileForm.emergencyContact" /></el-form-item>
        <el-form-item label="紧急电话"><el-input v-model="profileForm.emergencyPhone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存并同步</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="deviceDialogVisible" title="编辑设备信息" width="560px">
      <el-form :model="deviceForm" label-width="96px">
        <el-form-item label="设备名称" required><el-input v-model="deviceForm.deviceName" /></el-form-item>
        <el-form-item label="设备类型"><el-input v-model="deviceForm.deviceType" /></el-form-item>
        <el-form-item label="设备编号"><el-input v-model="deviceForm.deviceCode" /></el-form-item>
        <el-form-item label="绑定状态"><el-select v-model="deviceForm.status"><el-option label="绑定" value="绑定" /><el-option label="解绑" value="解绑" /></el-select></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deviceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="deviceSaving" @click="saveDevice">保存并同步</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="articleDialogVisible" :title="selectedArticle.title || '健康资讯'" width="760px">
      <div class="article-detail-meta">{{ selectedArticle.author || '健康中心' }} · {{ selectedArticle.createdAt || '' }}</div>
      <p v-if="selectedArticle.summary" class="article-summary">{{ selectedArticle.summary }}</p>
      <div class="article-content">{{ selectedArticle.content || selectedArticle.summary || '暂无正文' }}</div>
      <template #footer><el-button type="primary" size="large" @click="articleDialogVisible = false">我知道了</el-button></template>
    </el-dialog>

    <el-dialog v-model="workOrderVisible" title="创建工单" width="560px">
      <el-form :model="workOrderForm" label-width="96px">
        <el-form-item label="商品服务" required>
          <el-select v-model="workOrderForm.productId" filterable @change="syncWorkOrderAmount">
            <el-option v-for="item in catalogItems" :key="item.id" :label="`${item.name} · ¥${item.price}`" :value="String(item.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额"><el-input-number v-model="workOrderForm.amount" :min="0" disabled /></el-form-item>
        <el-form-item label="服务日期"><el-date-picker v-model="workOrderForm.date" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="服务时间"><el-time-picker v-model="workOrderForm.time" value-format="HH:mm:ss" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="workOrderVisible = false">取消</el-button>
        <el-button type="primary" :loading="workOrderSaving" @click="submitWorkOrder">提交工单</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import AvatarPicker from '../components/AvatarPicker.vue'
import { createHealthChartOption } from '../utils/healthChart'
import { useAuthStore } from '../stores/auth'
import {
  assetUrl,
  createElderlyWorkOrder,
  enrollElderlyActivity,
  getElderlyActivities,
  getElderlyCatalogItems,
  getElderlyCoupons,
  getElderlyDevices,
  getElderlyHealthData,
  getElderlyHealthArticles,
  getElderlyHealthVideos,
  getElderlyMedications,
  getElderlyOrders,
  getElderlyPoints,
  getElderlyProfile,
  getElderlyReports,
  getElderlyWorkOrders,
  updateElderlyAvatar,
  updateElderlyDevice,
  updateElderlyProfile
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
const userHealthChart = ref(null)
const medications = ref([])
const devices = ref([])
const reports = ref([])
const orders = ref([])
const coupons = ref([])
const points = ref({})
const catalogItems = ref([])
const workOrders = ref([])
const activities = ref([])
const healthArticles = ref([])
const healthVideos = ref([])
const enrollingActivityId = ref(null)
const articleDialogVisible = ref(false)
const selectedArticle = ref({})
const workOrderVisible = ref(false)
const workOrderSaving = ref(false)
const avatarDialogVisible = ref(false)
const avatarSaving = ref(false)
const avatarPickerRef = ref(null)
const profileDialogVisible = ref(false)
const profileSaving = ref(false)
const profileForm = reactive({
  nickname: '', realName: '', phone: '', gender: '未知', birthday: '', idCard: '', address: '', bio: '',
  height: null, weight: null, ethnicity: '', education: '', bloodType: '', rhNegative: false,
  chronicDisease: '', sleepQuality: '', smokingFreq: '', drinkingFreq: '', exerciseFreq: '', dietPreference: '',
  emergencyContact: '', emergencyPhone: ''
})
const deviceDialogVisible = ref(false)
const deviceSaving = ref(false)
const deviceForm = reactive({ id: null, deviceName: '', deviceType: '', deviceCode: '', status: '绑定' })
const today = new Date().toISOString().slice(0, 10)
const workOrderForm = reactive({ productId: '', amount: 0, date: today, time: '09:00:00' })

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
function statusTone(status) {
  return { '待服务': 'warning', '服务中': 'primary', '已完成': 'success', '已取消': 'info' }[status] || ''
}

async function loadData() {
  try {
    const [profileData, health, medicationData, deviceData, reportData, orderData, couponData, pointData, catalogData, workOrderData, activityData, articleData, videoData] = await Promise.all([
      getElderlyProfile(),
      getElderlyHealthData(),
      getElderlyMedications(),
      getElderlyDevices(),
      getElderlyReports(),
      getElderlyOrders(),
      getElderlyCoupons(),
      getElderlyPoints(),
      getElderlyCatalogItems(),
      getElderlyWorkOrders(),
      getElderlyActivities(),
      getElderlyHealthArticles(),
      getElderlyHealthVideos()
    ])
    profile.value = profileData
    healthData.value = health
    medications.value = medicationData
    devices.value = deviceData
    reports.value = reportData
    orders.value = orderData
    coupons.value = couponData
    points.value = pointData
    catalogItems.value = catalogData
    workOrders.value = workOrderData
    activities.value = activityData
    healthArticles.value = articleData
    healthVideos.value = videoData
    if (activeTab.value === 'health') {
      await nextTick()
      drawUserHealthChart()
    }
  } catch (err) {
    error.value = err.message || '加载失败'
  }
}

function openWorkOrder(item) {
  Object.assign(workOrderForm, { productId: String(item.id), amount: Number(item.price || 0), date: today, time: '09:00:00' })
  workOrderVisible.value = true
}

function syncWorkOrderAmount(productId) {
  const selected = catalogItems.value.find((item) => String(item.id) === String(productId))
  workOrderForm.amount = Number(selected?.price || 0)
}

async function submitWorkOrder() {
  if (!workOrderForm.productId) {
    ElMessage.warning('请选择商品服务')
    return
  }
  workOrderSaving.value = true
  try {
    await createElderlyWorkOrder({
      productId: workOrderForm.productId,
      serviceTime: `${workOrderForm.date} ${workOrderForm.time}`
    })
    workOrders.value = await getElderlyWorkOrders()
    activeTab.value = 'workOrders'
    workOrderVisible.value = false
    ElMessage.success('工单已创建，派单时间已自动填写')
  } catch (err) {
    ElMessage.error(err.message || '工单创建失败')
  } finally {
    workOrderSaving.value = false
  }
}

function openAvatarEditor() {
  avatarDialogVisible.value = true
  nextTick(() => avatarPickerRef.value?.reset(profile.value.avatarUrl))
}

async function saveAvatar() {
  avatarSaving.value = true
  try {
    const avatarUrl = await avatarPickerRef.value?.commitSelection()
    if (!avatarUrl) {
      ElMessage.warning('请选择一个头像')
      return
    }
    profile.value = await updateElderlyAvatar({ avatarUrl })
    auth.updateCachedAvatar(profile.value.avatarUrl)
    avatarDialogVisible.value = false
    ElMessage.success('头像已更新并同步到管理端')
  } catch (err) {
    ElMessage.error(err.message || '头像保存失败')
  } finally {
    avatarSaving.value = false
  }
}

function drawUserHealthChart() {
  if (!userHealthChart.value) return
  echarts.getInstanceByDom(userHealthChart.value)?.dispose()
  echarts.init(userHealthChart.value).setOption(createHealthChartOption(healthData.value))
}

function openProfileEditor() {
  Object.keys(profileForm).forEach((key) => {
    profileForm[key] = profile.value[key] ?? (key === 'rhNegative' ? false : '')
  })
  profileDialogVisible.value = true
}

async function saveProfile() {
  if (!String(profileForm.realName || '').trim() || !String(profileForm.phone || '').trim()) {
    ElMessage.warning('请填写真实姓名和手机号')
    return
  }
  profileSaving.value = true
  try {
    profile.value = await updateElderlyProfile({ ...profileForm })
    auth.updateCachedAvatar(profile.value.avatarUrl)
    profileDialogVisible.value = false
    ElMessage.success('个人信息已保存并同步到管理端')
  } catch (err) {
    ElMessage.error(err.message || '个人信息保存失败')
  } finally {
    profileSaving.value = false
  }
}

function openDeviceEditor(row) {
  Object.assign(deviceForm, {
    id: row.id,
    deviceName: row.deviceName || '',
    deviceType: row.deviceType || '',
    deviceCode: row.deviceCode || '',
    status: row.status || '绑定'
  })
  deviceDialogVisible.value = true
}

async function saveDevice() {
  if (!String(deviceForm.deviceName || '').trim()) {
    ElMessage.warning('请填写设备名称')
    return
  }
  deviceSaving.value = true
  try {
    await updateElderlyDevice(deviceForm.id, { ...deviceForm })
    devices.value = await getElderlyDevices()
    deviceDialogVisible.value = false
    ElMessage.success('设备信息已保存并同步到管理端')
  } catch (err) {
    ElMessage.error(err.message || '设备信息保存失败')
  } finally {
    deviceSaving.value = false
  }
}

async function joinActivity(item) {
  enrollingActivityId.value = item.id
  try {
    await enrollElderlyActivity(item.id)
    activities.value = await getElderlyActivities()
    ElMessage.success(`已报名“${item.title}”`)
  } catch (err) {
    ElMessage.error(err.message || '活动报名失败')
  } finally {
    enrollingActivityId.value = null
  }
}

function formatDuration(seconds) {
  const minutes = Math.ceil(Number(seconds || 0) / 60)
  return minutes ? `${minutes} 分钟` : '时长待定'
}

function openVideo(url) {
  window.open(url, '_blank', 'noopener,noreferrer')
}

function openArticle(item) {
  selectedArticle.value = item
  articleDialogVisible.value = true
}

function logout() {
  auth.signOut()
  router.push('/login')
}

watch(activeTab, async (tab) => {
  if (tab !== 'health') return
  await nextTick()
  drawUserHealthChart()
})

onBeforeUnmount(() => {
  if (userHealthChart.value) echarts.getInstanceByDom(userHealthChart.value)?.dispose()
})

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

.portal-identity {
  display: flex;
  align-items: center;
  gap: 14px;
}

.portal-avatar-control {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 2px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
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
.purple { background: #7657b6; }

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

.health-chart {
  width: 100%;
  height: 360px;
}

.panel-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.panel-heading h2 {
  margin: 0;
}

.catalog-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.portal-content-card {
  overflow: hidden;
  border: 1px solid #dce8e5;
  border-radius: 12px;
  background: #fff;
}

.portal-content-card > img {
  width: 100%;
  height: 190px;
  object-fit: cover;
}

.content-card-body {
  padding: 20px;
}

.content-card-body h2 {
  margin: 14px 0 10px;
  font-size: 22px;
}

.content-card-body p {
  color: #52646e;
  font-size: 16px;
  line-height: 1.7;
}

.content-card-body footer,
.content-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.content-meta,
.content-card-body small {
  color: #788991;
}

.article-detail-meta {
  margin-bottom: 14px;
  color: #788991;
}

.article-summary {
  padding: 14px 16px;
  border-radius: 8px;
  background: #f2f8f6;
  color: #3e5b55;
  font-size: 17px;
}

.article-content {
  white-space: pre-wrap;
  color: #354850;
  font-size: 17px;
  line-height: 1.9;
}

.catalog-card {
  display: flex;
  min-height: 210px;
  flex-direction: column;
  padding: 18px;
  border: 1px solid #e2eaee;
  border-radius: 10px;
  background: #fbfdfc;
}

.catalog-card > div {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6f7e87;
}

.catalog-card h2 {
  margin: 16px 0 8px;
  font-size: 19px;
}

.catalog-card p {
  flex: 1;
  margin: 0 0 16px;
  color: #6f7e87;
  line-height: 1.6;
}

.catalog-card footer {
  display: flex;
  align-items: center;
  gap: 10px;
}

.catalog-card footer strong {
  color: #159a84;
  font-size: 22px;
}

.catalog-card footer small {
  margin-right: auto;
  color: #6f7e87;
}

@media (max-width: 1000px) {
  .summary-grid,
  .catalog-grid,
  .content-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .portal-page { padding: 14px; }
  .summary-grid,
  .catalog-grid,
  .content-grid { grid-template-columns: 1fr; }
  .portal-header h1 { font-size: 24px; }
  .portal-tabs { padding: 10px; }
}
</style>
