<template>
  <main class="portal-page">
    <header class="portal-header">
      <div>
        <p>服务人员端</p>
        <h1>{{ profile.realName || auth.user?.name || '我的工单' }}</h1>
      </div>
      <el-button @click="logout">
        <el-icon><SwitchButton /></el-icon>
        退出
      </el-button>
    </header>

    <el-alert v-if="error" :title="error" type="error" show-icon />

    <section class="summary-grid">
      <article class="summary-card teal">
        <span>我的工单</span>
        <strong>{{ workOrders.length }}</strong>
        <small>只显示分配给本人</small>
      </article>
      <article class="summary-card amber">
        <span>待服务</span>
        <strong>{{ countStatus('pending') }}</strong>
        <small>可开始服务</small>
      </article>
      <article class="summary-card blue">
        <span>服务中</span>
        <strong>{{ countStatus('service_in') }}</strong>
        <small>进行中的工单</small>
      </article>
    </section>

    <section class="panel">
      <div class="toolbar">
        <el-segmented v-model="statusFilter" :options="statusOptions" />
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      <el-table :data="filteredOrders" stripe @row-click="selectOrder">
        <el-table-column prop="orderNo" label="工单号" min-width="150" />
        <el-table-column prop="serviceItem" label="服务项目" min-width="170" />
        <el-table-column prop="customerName" label="客户" min-width="110" />
        <el-table-column prop="customerPhone" label="客户电话" min-width="130" />
        <el-table-column prop="customerAddress" label="地址" min-width="220" />
        <el-table-column prop="serviceTime" label="服务时间" min-width="160" />
        <el-table-column prop="status" label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="statusTone(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="detailVisible" title="工单详情" width="760px">
      <el-descriptions :column="detailColumns" border>
        <el-descriptions-item label="工单号">{{ selectedOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(selectedOrder.status) }}</el-descriptions-item>
        <el-descriptions-item label="服务项目">{{ selectedOrder.serviceItem }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ selectedOrder.amount }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ selectedOrder.customerName }}</el-descriptions-item>
        <el-descriptions-item label="客户电话">{{ selectedOrder.customerPhone }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ selectedOrder.customerAddress }}</el-descriptions-item>
        <el-descriptions-item label="服务时间">{{ selectedOrder.serviceTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button :disabled="selectedOrder.status !== 'pending'" type="primary" @click="changeStatus('service_in')">开始服务</el-button>
        <el-button :disabled="selectedOrder.status === 'completed' || selectedOrder.status === 'cancelled'" type="success" @click="changeStatus('completed')">完成服务</el-button>
        <el-button :disabled="selectedOrder.status === 'completed' || selectedOrder.status === 'cancelled'" type="danger" plain @click="changeStatus('cancelled')">取消工单</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { getServiceProfile, getServiceWorkOrder, getServiceWorkOrders, updateServiceWorkOrderStatus } from '../api/http'
import { useResponsiveColumns } from '../utils/viewport'

const router = useRouter()
const auth = useAuthStore()
const detailColumns = useResponsiveColumns(2)
const error = ref('')
const profile = ref({})
const workOrders = ref([])
const selectedOrder = ref({})
const detailVisible = ref(false)
const statusFilter = ref('all')

const statusOptions = [
  { label: '全部', value: 'all' },
  { label: '待服务', value: 'pending' },
  { label: '服务中', value: 'service_in' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' }
]

const filteredOrders = computed(() => statusFilter.value === 'all' ? workOrders.value : workOrders.value.filter((item) => item.status === statusFilter.value))

async function loadData() {
  try {
    const [profileData, orders] = await Promise.all([getServiceProfile(), getServiceWorkOrders()])
    profile.value = profileData
    workOrders.value = orders
  } catch (err) {
    error.value = err.message || '加载失败'
  }
}

async function selectOrder(row) {
  selectedOrder.value = await getServiceWorkOrder(row.id)
  detailVisible.value = true
}

async function changeStatus(status) {
  selectedOrder.value = await updateServiceWorkOrderStatus(selectedOrder.value.id, status)
  workOrders.value = workOrders.value.map((item) => item.id === selectedOrder.value.id ? { ...item, ...selectedOrder.value } : item)
  ElMessage.success('状态已更新')
}

function countStatus(status) {
  return workOrders.value.filter((item) => item.status === status).length
}

function statusLabel(status) {
  return { pending: '待服务', service_in: '服务中', completed: '已完成', cancelled: '已取消' }[status] || status
}

function statusTone(status) {
  return { pending: 'warning', service_in: 'primary', completed: 'success', cancelled: 'info' }[status] || ''
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

.teal { background: #166d73; }
.amber { background: #d88a1d; }
.blue { background: #356fc8; }

.panel {
  padding: 18px;
  border: 1px solid #e2eaee;
  border-radius: 8px;
  background: #fff;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

@media (max-width: 720px) {
  .portal-page {
    padding: 14px;
  }

  .portal-header {
    align-items: flex-start;
    gap: 14px;
  }

  .portal-header h1 {
    font-size: 24px;
  }

  .summary-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .summary-card {
    min-height: 104px;
    padding: 16px;
  }

  .panel {
    padding: 12px;
  }

  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar :deep(.el-segmented) {
    width: 100%;
    overflow-x: auto;
  }

  .toolbar > .el-button {
    width: 100%;
    margin: 0;
  }
}
</style>
