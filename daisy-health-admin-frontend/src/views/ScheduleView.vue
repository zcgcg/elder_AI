<template>
  <section>
    <div class="page-heading">
      <div><h1>预约看板</h1><p>{{ filters.date }} · 06:00–22:00 服务安排</p></div>
      <div class="filters compact">
        <el-date-picker v-model="filters.date" type="date" value-format="YYYY-MM-DD" :clearable="false" placeholder="选择日期" @change="load" />
        <el-button type="primary" :icon="Plus" @click="openCreate">新建预约</el-button>
      </div>
    </div>
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false">
      <template #default><el-button link type="primary" @click="load">重新加载</el-button></template>
    </el-alert>
    <paged-list :items="laidOut" v-slot="{ items }">
    <section class="timeline-panel">
      <div class="timeline-wrapper">
        <div class="time-labels">
          <div v-for="hour in hours" :key="hour" class="time-label">
            <time>{{ formatHour(hour) }}:00</time>
          </div>
        </div>
        <div class="appointment-area">
          <div v-for="hour in hours" :key="hour" class="hour-line"></div>
          <article
            v-for="item in items"
            :key="item.id"
            class="appointment-card"
            :style="cardStyle(item)"
          >
            <div class="appointment-card-title">
              <strong>{{ item.serviceName }}</strong>
              <el-button link type="primary" size="small" @click="openDetail(item)">详情</el-button>
            </div>
            <span>{{ item.timeRange }} · {{ item.userName }}</span>
            <div class="appointment-card-footer">
              <el-dropdown trigger="click" @command="(status) => changeStatus(item, status)">
                <el-tag :type="tagType(statusLabel(item.status))" size="small" effect="dark" class="status-dropdown-tag">
                  {{ statusLabel(item.status) }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-tag>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="status in appointmentStatusOptions" :key="status.value" :command="status.value">{{ status.label }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button link type="danger" size="small" @click="removeAppointment(item)">删除</el-button>
            </div>
          </article>
        </div>
      </div>
    </section>
    </paged-list>

    <el-dialog v-model="dialogVisible" title="新建预约" width="620px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="商品服务" required>
          <el-select v-model="form.productId" filterable placeholder="请选择已有商品服务" @change="syncAmount">
            <el-option
              v-for="item in catalogOptions"
              :key="item.id"
              :label="`${item.name} · ¥${item.price} · ${serviceDurationMinutes(item)}分钟`"
              :value="String(item.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="客户">
          <el-select v-model="form.userRef" filterable clearable placeholder="输入姓名/手机号搜索">
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.realName || user.nickname} · ${user.phone || user.id}`"
              :value="String(user.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="服务人员" required>
          <el-select v-model="form.personnelId" filterable clearable placeholder="请输入姓名选择服务人员">
            <el-option
              v-for="person in personnelOptions"
              :key="person.id"
              :label="`${person.name} · ${person.serviceType} · ${person.phone}`"
              :value="String(person.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期"><el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" :clearable="false" /></el-form-item>
        <el-form-item label="开始时间"><el-time-picker v-model="form.startTime" value-format="HH:mm:ss" placeholder="开始时间" /></el-form-item>
        <el-form-item label="服务时长"><span>{{ selectedDuration }} 分钟（结束时间自动计算）</span></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="form.amount" :min="0" controls-position="right" disabled /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="预约/订单详情" width="680px">
      <el-descriptions v-if="selectedAppointment" :column="2" border class="appointment-detail-summary">
        <el-descriptions-item label="工单编号">{{ selectedAppointment.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ selectedAppointment.serviceOrderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="派单时间">{{ selectedAppointment.dispatchTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ selectedAppointment.completeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户电话">{{ selectedAppointment.customerPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="服务人员电话">{{ selectedAppointment.personnelPhone || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="detailForm" label-width="96px">
        <el-form-item label="商品服务" required>
          <el-select v-model="detailForm.productId" filterable placeholder="请选择商品服务" @change="syncDetailAmount">
            <el-option
              v-for="item in catalogOptions"
              :key="item.id"
              :label="`${item.name} · ¥${item.price} · ${serviceDurationMinutes(item)}分钟`"
              :value="String(item.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="客户" required>
          <el-select v-model="detailForm.userRef" filterable placeholder="请选择客户">
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.realName || user.nickname} · ${user.phone || user.id}`"
              :value="String(user.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="服务人员" required>
          <el-select v-model="detailForm.personnelId" filterable placeholder="请选择服务人员">
            <el-option
              v-for="person in personnelOptions"
              :key="person.id"
              :label="`${person.name} · ${person.serviceType} · ${person.phone}`"
              :value="String(person.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="服务时间" required>
          <el-date-picker v-model="detailForm.serviceTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择服务开始时间" />
        </el-form-item>
        <el-form-item label="服务时长"><span>{{ detailDuration }} 分钟（随商品服务同步）</span></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="detailForm.amount" :min="0" controls-position="right" disabled /></el-form-item>
        <el-form-item label="完成情况">
          <el-select v-model="detailForm.status">
            <el-option v-for="status in appointmentStatusOptions" :key="status.value" :label="status.label" :value="status.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">取消</el-button>
        <el-button type="primary" :loading="detailSaving" @click="submitDetail">保存并同步</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createAppointment, deleteAppointment, getAppointments, getResource, getUsers, updateAppointment } from '../api/http'
import { sevenDayWindow } from '../utils/query'
import { eligiblePersonnel } from '../utils/personnel'
import { clipAppointmentRange, serviceDurationMinutes } from '../utils/serviceDuration'
import PagedList from '../components/PagedList.vue'

const initialWindow = sevenDayWindow()
const filters = reactive({ date: initialWindow.startDate })
const START_HOUR = 6
const END_HOUR = 22
const hours = Array.from({ length: END_HOUR - START_HOUR }, (_, index) => START_HOUR + index)
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({ productId: '', userRef: '', personnelId: '', date: initialWindow.startDate, startTime: '09:00:00', amount: 0 })
const detailVisible = ref(false)
const detailSaving = ref(false)
const selectedAppointment = ref(null)
const detailForm = reactive({ productId: '', userRef: '', personnelId: '', serviceTime: '', amount: 0, status: 'pending' })
const appointmentStatusOptions = [
  { value: 'pending', label: '待服务', tagType: 'warning' },
  { value: 'service_in', label: '服务中', tagType: 'primary' },
  { value: 'completed', label: '已完成', tagType: 'success' },
  { value: 'cancelled', label: '已取消', tagType: 'info' }
]
const userOptions = ref([])
const catalogOptions = ref([])
const personnelOptions = ref([])
const appointments = ref([])
const error = ref('')
const activeWindow = computed(() => sevenDayWindow(new Date(`${filters.date}T00:00:00`)))
const filtered = computed(() => appointments.value.filter((item) => item.serviceDate === filters.date))
const selectedDuration = computed(() => serviceDurationMinutes(findCatalogItem(form.productId)))
const detailDuration = computed(() => serviceDurationMinutes(findCatalogItem(detailForm.productId)))

const ROW_HEIGHT = 84

const laidOut = computed(() => {
  const baseMinutes = START_HOUR * 60
  const items = filtered.value
    .map((item) => {
      const parsed = clipAppointmentRange(item, START_HOUR, END_HOUR)
      if (!parsed) return null
      const start = parsed.startMinutes
      const end = parsed.endMinutes
      return {
        ...item,
        _start: start,
        _end: end,
        _top: ((start - baseMinutes) / 60) * ROW_HEIGHT,
        _height: ((end - start) / 60) * ROW_HEIGHT
      }
    })
    .filter(Boolean)
    .sort((a, b) => a._start - b._start || a._end - b._end)
  // 为重叠的预约分配不同列
  const columnEnds = []
  for (const item of items) {
    let col = columnEnds.findIndex((end) => end <= item._start)
    if (col === -1) {
      col = columnEnds.length
      columnEnds.push(item._end)
    } else {
      columnEnds[col] = item._end
    }
    item._col = col
  }
  const colCount = Math.max(columnEnds.length, 1)
  for (const item of items) {
    item._colCount = colCount
  }
  return items
})

function cardStyle(item) {
  const colWidth = 100 / item._colCount
  return {
    top: `${item._top + 2}px`,
    height: `${Math.max(item._height, 36)}px`,
    left: `calc(${item._col * colWidth}% + 4px)`,
    width: `calc(${colWidth}% - 8px)`
  }
}

function tagType(status) {
  return findStatus(status)?.tagType || 'info'
}
function statusLabel(status) {
  return findStatus(status)?.label || status
}
function statusValue(status) {
  return findStatus(status)?.value || status
}
function findStatus(status) {
  return appointmentStatusOptions.find((option) => option.value === status || option.label === status)
}
function formatHour(hour) {
  return String(hour).padStart(2, '0')
}
function openCreate() {
  const first = catalogOptions.value[0]
  Object.assign(form, { productId: first?.id ? String(first.id) : '', userRef: userOptions.value[0]?.id ? String(userOptions.value[0].id) : '', personnelId: '', date: filters.date, startTime: '09:00:00', amount: Number(first?.price || 0) })
  dialogVisible.value = true
}
function syncAmount(productId) {
  syncProductAmount(form, productId)
}
function findCatalogItem(productId) {
  return catalogOptions.value.find((item) => String(item.id) === String(productId))
}
function syncProductAmount(target, productId) {
  target.amount = Number(findCatalogItem(productId)?.price || 0)
}
async function load() {
  error.value = ''
  try {
    appointments.value = await getAppointments(activeWindow.value)
  } catch (exception) {
    appointments.value = []
    error.value = exception.message || '预约数据加载失败，请检查后端和数据库连接'
  }
}
async function loadUsers() {
  try {
    const data = await getUsers()
    userOptions.value = data.list || []
  } catch (error) {
    userOptions.value = []
  }
}
async function loadCatalog() {
  try {
    const data = await getResource('products')
    catalogOptions.value = (data.list || data || []).filter((item) => item.status !== '下架')
  } catch (error) {
    catalogOptions.value = []
  }
}
function openDetail(item) {
  selectedAppointment.value = item
  Object.assign(detailForm, {
    productId: String(item.productId || ''),
    userRef: String(item.customerId || ''),
    personnelId: String(item.personnelId || ''),
    serviceTime: item.serviceTime || '',
    amount: Number(item.amount || 0),
    status: statusValue(item.status)
  })
  detailVisible.value = true
}
function syncDetailAmount(productId) {
  syncProductAmount(detailForm, productId)
}
async function loadPersonnel() {
  try {
    const data = await getResource('personnel')
    personnelOptions.value = eligiblePersonnel(data.list || data || [])
  } catch (error) {
    personnelOptions.value = []
  }
}
async function submitCreate() {
  if (!form.productId) {
    ElMessage.warning('请选择商品服务')
    return
  }
  if (!form.personnelId) {
    ElMessage.warning('请选择服务人员')
    return
  }
  saving.value = true
  try {
    await createAppointment({
      productId: form.productId,
      userRef: form.userRef,
      personnelId: form.personnelId,
      serviceTime: `${form.date} ${form.startTime}`
    })
    ElMessage.success('预约及对应工单已创建')
    dialogVisible.value = false
    filters.date = form.date
    await load()
  } catch (error) {
    ElMessage.error('创建失败，请确认后端和数据库已启动')
  } finally {
    saving.value = false
  }
}
async function submitDetail() {
  if (!detailForm.productId || !detailForm.userRef || !detailForm.personnelId || !detailForm.serviceTime) {
    ElMessage.warning('请完整填写商品服务、客户、服务人员和服务时间')
    return
  }
  detailSaving.value = true
  try {
    await updateAppointment(selectedAppointment.value.id, {
      productId: detailForm.productId,
      userRef: detailForm.userRef,
      personnelId: detailForm.personnelId,
      serviceTime: detailForm.serviceTime,
      status: statusLabel(detailForm.status)
    })
    ElMessage.success('预约、工单和订单已同步')
    detailVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error(error.message || '预约同步失败')
  } finally {
    detailSaving.value = false
  }
}
async function removeAppointment(item) {
  try {
    await ElMessageBox.confirm(`确认删除预约「${item.serviceName}」？`, '删除确认', { type: 'warning' })
    await deleteAppointment(item.id)
    ElMessage.success('预约已删除')
    await load()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}
async function changeStatus(item, status) {
  try {
    await updateAppointment(item.id, { status: statusLabel(status) })
    ElMessage.success('状态已更新')
    await load()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

onMounted(async () => {
  await Promise.all([load(), loadUsers(), loadCatalog(), loadPersonnel()])
})
</script>

<style scoped>
.status-dropdown-tag {
  cursor: pointer;
}
.status-dropdown-tag .el-icon--right {
  margin-left: 4px;
  font-size: 12px;
}
.appointment-card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-height: 22px;
}
.appointment-card {
  padding: 5px 10px;
}
.appointment-card-title strong {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.appointment-card-title .el-button {
  flex: none;
}
.appointment-detail-summary {
  margin-bottom: 18px;
}
</style>
