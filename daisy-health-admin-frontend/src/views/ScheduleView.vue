<template>
  <section>
    <div class="page-heading">
      <div><h1>预约看板</h1><p>{{ filters.date }} · 全天服务安排（加载 {{ dateWindowLabel }}）</p></div>
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
            <time>{{ hour }}:00</time>
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
            <strong>{{ item.serviceName }}</strong>
            <span>{{ item.timeRange }} · {{ item.userName }}</span>
            <div class="appointment-card-footer">
              <el-dropdown trigger="click" @command="(status) => changeStatus(item, status)">
                <el-tag :type="tagType(statusLabel(item.status))" size="small" effect="dark" class="status-dropdown-tag">
                  {{ statusLabel(item.status) }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-tag>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="pending">待服务</el-dropdown-item>
                    <el-dropdown-item command="service_in">服务中</el-dropdown-item>
                    <el-dropdown-item command="completed">已完成</el-dropdown-item>
                    <el-dropdown-item command="cancelled">已取消</el-dropdown-item>
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
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createAppointment, deleteAppointment, getAppointments, getResource, getUsers, updateAppointment } from '../api/http'
import { sevenDayWindow } from '../utils/query'
import { eligiblePersonnel } from '../utils/personnel'
import { appointmentRangeMinutes, serviceDurationMinutes } from '../utils/serviceDuration'
import PagedList from '../components/PagedList.vue'

const initialWindow = sevenDayWindow()
const filters = reactive({ date: initialWindow.startDate })
const hours = Array.from({ length: 24 }, (_, index) => index)
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({ productId: '', userRef: '', personnelId: '', date: initialWindow.startDate, startTime: '09:00:00', amount: 0 })
const userOptions = ref([])
const catalogOptions = ref([])
const personnelOptions = ref([])
const appointments = ref([])
const error = ref('')
const activeWindow = computed(() => sevenDayWindow(new Date(`${filters.date}T00:00:00`)))
const dateWindowLabel = computed(() => `${activeWindow.value.startDate} 至 ${activeWindow.value.endDate}`)
const filtered = computed(() => appointments.value.filter((item) => item.serviceDate === filters.date))
const selectedDuration = computed(() => serviceDurationMinutes(catalogOptions.value.find((item) => String(item.id) === String(form.productId))))

const ROW_HEIGHT = 84
const START_HOUR = 0
const END_HOUR = 24

const laidOut = computed(() => {
  const baseMinutes = START_HOUR * 60
  const maxMinutes = END_HOUR * 60
  const items = filtered.value
    .map((item) => {
      const parsed = appointmentRangeMinutes(item)
      if (!parsed) return null
      const start = Math.max(parsed.startMinutes, baseMinutes)
      const end = Math.min(parsed.endMinutes, maxMinutes)
      if (end <= start) return null
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
  return { 已完成: 'success', 服务中: 'primary', 待服务: 'warning', 已取消: 'info' }[status] || 'info'
}
function statusLabel(status) {
  return { pending: '待服务', service_in: '服务中', completed: '已完成', cancelled: '已取消' }[status] || status
}
function openCreate() {
  const first = catalogOptions.value[0]
  Object.assign(form, { productId: first?.id ? String(first.id) : '', userRef: userOptions.value[0]?.id ? String(userOptions.value[0].id) : '', personnelId: '', date: filters.date, startTime: '09:00:00', amount: Number(first?.price || 0) })
  dialogVisible.value = true
}
function syncAmount(productId) {
  const selected = catalogOptions.value.find((item) => String(item.id) === String(productId))
  form.amount = Number(selected?.price || 0)
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
</style>
