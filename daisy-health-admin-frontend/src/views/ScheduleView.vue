<template>
  <section>
    <div class="page-heading">
      <div><h1>预约看板</h1><p>9:00-18:00 当日服务安排</p></div>
      <div class="filters compact">
        <el-date-picker v-model="filters.date" type="date" placeholder="选择日期" />
        <el-select v-model="filters.serviceType" placeholder="服务类型" clearable>
          <el-option label="家政护理" value="家政护理" />
          <el-option label="康复理疗" value="康复理疗" />
          <el-option label="上门体检" value="上门体检" />
        </el-select>
        <el-button type="primary" :icon="Plus" @click="openCreate">新建预约</el-button>
      </div>
    </div>
    <section class="timeline-panel">
      <div v-for="hour in hours" :key="hour" class="time-row">
        <time>{{ hour }}:00</time>
        <div class="appointment-lane">
          <article v-for="item in appointmentsByHour(hour)" :key="item.id" class="appointment-card">
            <strong>{{ item.serviceName }}</strong>
            <span>{{ item.timeRange }} · {{ item.userName }}</span>
            <el-tag :type="tagType(item.status)">{{ item.status }}</el-tag>
          </article>
        </div>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" title="新建预约" width="620px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="服务名称" required><el-input v-model="form.serviceName" placeholder="如：助浴护理" /></el-form-item>
        <el-form-item label="客户ID"><el-input-number v-model="form.customerId" :min="1" controls-position="right" /></el-form-item>
        <el-form-item label="服务人员ID"><el-input-number v-model="form.personnelId" :min="1" controls-position="right" /></el-form-item>
        <el-form-item label="预约日期"><el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="开始时间"><el-time-picker v-model="form.startTime" value-format="HH:mm:ss" placeholder="开始时间" /></el-form-item>
        <el-form-item label="结束时间"><el-time-picker v-model="form.endTime" value-format="HH:mm:ss" placeholder="结束时间" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="form.amount" :min="0" controls-position="right" /></el-form-item>
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
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createAppointment, getAppointments } from '../api/http'

const filters = reactive({ date: new Date(), serviceType: '' })
const hours = Array.from({ length: 10 }, (_, index) => index + 9)
const dialogVisible = ref(false)
const saving = ref(false)
const today = new Date().toISOString().slice(0, 10)
const form = reactive({ serviceName: '', customerId: 10001, personnelId: 1, date: today, startTime: '09:00:00', endTime: '10:00:00', amount: 99 })
const appointments = ref([
  { id: 1, hour: 9, serviceName: '助浴护理', timeRange: '09:00-10:30', userName: '王秀兰', status: '待服务' },
  { id: 2, hour: 10, serviceName: '肩颈康复', timeRange: '10:00-11:00', userName: '陈建国', status: '服务中' },
  { id: 3, hour: 14, serviceName: '上门基础体检', timeRange: '14:00-15:30', userName: '赵桂英', status: '已完成' },
  { id: 4, hour: 16, serviceName: '日常清洁', timeRange: '16:00-18:00', userName: '刘爱华', status: '已取消' }
])
const filtered = computed(() => filters.serviceType ? appointments.value.filter((item) => item.serviceName?.includes(filters.serviceType)) : appointments.value)

function appointmentsByHour(hour) {
  return filtered.value.filter((item) => item.hour === hour)
}
function tagType(status) {
  return { 已完成: 'success', 服务中: 'primary', 待服务: 'warning', 已取消: 'info' }[status] || 'info'
}
function openCreate() {
  Object.assign(form, { serviceName: '', customerId: 10001, personnelId: 1, date: today, startTime: '09:00:00', endTime: '10:00:00', amount: 99 })
  dialogVisible.value = true
}
async function load() {
  try {
    appointments.value = await getAppointments()
  } catch (error) {}
}
async function submitCreate() {
  if (!form.serviceName.trim()) {
    ElMessage.warning('请填写服务名称')
    return
  }
  saving.value = true
  try {
    await createAppointment({
      serviceName: form.serviceName,
      customerId: form.customerId,
      personnelId: form.personnelId,
      amount: form.amount,
      serviceTime: `${form.date} ${form.startTime}`,
      completeTime: `${form.date} ${form.endTime}`
    })
    ElMessage.success('预约已创建')
    dialogVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error('创建失败，请确认后端和数据库已启动')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
