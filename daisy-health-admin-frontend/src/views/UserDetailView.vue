<template>
  <section>
    <div class="page-heading">
      <div><h1>用户详情</h1><p>个人信息、健康数据、资产和服务记录</p></div>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    <div class="detail-layout">
      <aside class="profile-panel">
        <el-avatar :size="88" :src="assetUrl(user.avatarUrl)">{{ user.realName?.slice(0, 1) }}</el-avatar>
        <h2>{{ user.nickname }}</h2>
        <p>ID {{ user.id }} · {{ user.realName }}</p>
        <div class="tag-row"><el-tag v-for="tag in user.tags" :key="tag">{{ tag }}</el-tag></div>
        <dl>
          <dt>手机号</dt><dd>{{ user.phone }}</dd>
          <dt>最近购买</dt><dd>{{ user.lastBuyTime || '-' }}</dd>
          <dt>最近登录</dt><dd>{{ user.lastLoginTime || '-' }}</dd>
        </dl>
      </aside>

      <main class="panel detail-main">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="个人信息" name="profile">
            <div class="tab-actions"><el-button type="primary" @click="openProfileEdit">编辑个人信息</el-button></div>
            <el-descriptions :column="detailColumns" border>
              <el-descriptions-item v-for="item in profileFields" :key="item.label" :label="item.label">{{ item.value || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane label="用药信息" name="medication">
            <editable-table section="medications" :rows="user.medications" :columns="sectionMap.medications.columns" @create="openSectionCreate" @edit="openSectionEdit" @remove="removeSectionRow" />
          </el-tab-pane>

          <el-tab-pane label="健康数据" name="data">
            <div ref="healthChart" class="chart tall"></div>
            <editable-table section="healthData" :rows="healthDataRows" :columns="sectionMap.healthData.columns" @create="openSectionCreate" @edit="openSectionEdit" @remove="removeSectionRow" />
          </el-tab-pane>

          <el-tab-pane label="设备信息" name="devices">
            <editable-table section="devices" :rows="user.devices" :columns="sectionMap.devices.columns" @create="openSectionCreate" @edit="openSectionEdit" @remove="removeSectionRow" />
          </el-tab-pane>

          <el-tab-pane label="报告信息" name="reports">
            <editable-table section="reports" :rows="user.reports" :columns="sectionMap.reports.columns" @create="openSectionCreate" @edit="openSectionEdit" @remove="removeSectionRow" />
          </el-tab-pane>

          <el-tab-pane label="订单信息" name="orders">
            <editable-table section="orders" :rows="user.orders" :columns="sectionMap.orders.columns" @create="openSectionCreate" @edit="openSectionEdit" @remove="removeSectionRow" />
          </el-tab-pane>

          <el-tab-pane label="资产信息" name="assets">
            <h3 class="section-subtitle">积分</h3>
            <editable-table section="userPoints" :rows="user.points" :columns="sectionMap.userPoints.columns" @create="openSectionCreate" @edit="openSectionEdit" @remove="removeSectionRow" />
            <h3 class="section-subtitle">优惠券</h3>
            <editable-table section="coupons" :rows="user.coupons" :columns="sectionMap.coupons.columns" @create="openSectionCreate" @edit="openSectionEdit" @remove="removeSectionRow" />
          </el-tab-pane>

          <el-tab-pane label="内容信息" name="content">
            <editable-table section="posts" :rows="user.contents" :columns="sectionMap.posts.columns" @create="openSectionCreate" @edit="openSectionEdit" @remove="removeSectionRow" />
          </el-tab-pane>

          <el-tab-pane label="服务记录" name="services">
            <editable-table section="workOrders" :rows="user.serviceRecords" :columns="sectionMap.workOrders.columns" @create="openSectionCreate" @edit="openSectionEdit" @remove="removeSectionRow" />
          </el-tab-pane>
        </el-tabs>
      </main>
    </div>

    <el-dialog v-model="profileDialogVisible" title="编辑个人信息" width="680px">
      <el-form :model="profileForm" label-width="104px">
        <el-form-item label="昵称"><el-input v-model="profileForm.nickname" /></el-form-item>
        <el-form-item label="真实姓名" required><el-input v-model="profileForm.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="profileForm.phone" /></el-form-item>
        <el-form-item label="头像">
          <avatar-picker ref="avatarPickerRef" :model-value="profileForm.avatarUrl" :fallback="profileForm.realName?.slice(0, 1) || '用'" />
        </el-form-item>
        <el-form-item label="性别"><el-radio-group v-model="profileForm.gender"><el-radio label="女" /><el-radio label="男" /><el-radio label="未知" /></el-radio-group></el-form-item>
        <el-form-item label="出生日期"><el-date-picker v-model="profileForm.birthday" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="profileForm.idCard" /></el-form-item>
        <el-form-item label="家庭住址"><el-input v-model="profileForm.address" /></el-form-item>
        <el-form-item label="个人简介"><el-input v-model="profileForm.bio" type="textarea" /></el-form-item>
        <el-form-item label="民族"><el-input v-model="profileForm.ethnicity" /></el-form-item>
        <el-form-item label="文化程度"><el-input v-model="profileForm.education" /></el-form-item>
        <el-form-item label="身高（厘米）"><el-input-number v-model="profileForm.height" :min="0" controls-position="right" /></el-form-item>
        <el-form-item label="体重（千克）"><el-input-number v-model="profileForm.weight" :min="0" controls-position="right" /></el-form-item>
        <el-form-item label="血型"><el-select v-model="profileForm.bloodType"><el-option label="A" value="A" /><el-option label="B" value="B" /><el-option label="O" value="O" /><el-option label="AB" value="AB" /></el-select></el-form-item>
        <el-form-item label="RH阴性"><el-switch v-model="profileForm.rhNegative" /></el-form-item>
        <el-form-item label="慢性病"><el-input v-model="profileForm.chronicDisease" /></el-form-item>
        <el-form-item label="睡眠质量"><el-select v-model="profileForm.sleepQuality"><el-option label="良好" value="良好" /><el-option label="一般" value="一般" /><el-option label="较差" value="较差" /></el-select></el-form-item>
        <el-form-item label="吸烟频率"><el-input v-model="profileForm.smokingFreq" /></el-form-item>
        <el-form-item label="饮酒频率"><el-input v-model="profileForm.drinkingFreq" /></el-form-item>
        <el-form-item label="运动频率"><el-input v-model="profileForm.exerciseFreq" /></el-form-item>
        <el-form-item label="饮食偏好"><el-input v-model="profileForm.dietPreference" /></el-form-item>
        <el-form-item label="紧急联系人"><el-input v-model="profileForm.emergencyContact" /></el-form-item>
        <el-form-item label="紧急电话"><el-input v-model="profileForm.emergencyPhone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="sectionDialogVisible" :title="sectionDialogTitle" width="640px">
      <el-form :model="sectionForm" label-width="104px">
        <el-form-item v-for="field in currentSection.fields" :key="field.prop" :label="field.label" :required="field.required">
          <el-input-number v-if="field.type === 'number'" v-model="sectionForm[field.prop]" :min="0" controls-position="right" :disabled="field.readonly" />
          <el-select v-else-if="field.type === 'product'" v-model="sectionForm[field.prop]" filterable placeholder="请选择商品服务" @change="syncSectionProductAmount">
            <el-option v-for="item in productOptions" :key="item.id" :label="`${item.name} · ¥${item.price} · ${serviceDurationMinutes(item)}分钟`" :value="String(item.id)" />
          </el-select>
          <el-select v-else-if="field.type === 'personnel'" v-model="sectionForm[field.prop]" filterable placeholder="请选择服务人员">
            <el-option v-for="item in sectionPersonnelOptions" :key="item.id" :label="`${item.name} · ${item.serviceType} · ${item.phone}`" :value="String(item.id)" />
          </el-select>
          <el-select v-else-if="field.type === 'select'" v-model="sectionForm[field.prop]" placeholder="请选择">
            <el-option v-for="option in field.options" :key="selectOptionValue(option)" :label="selectOptionLabel(option)" :value="selectOptionValue(option)" />
          </el-select>
          <el-date-picker v-else-if="field.type === 'date'" v-model="sectionForm[field.prop]" type="date" value-format="YYYY-MM-DD" />
          <el-time-picker v-else-if="field.type === 'time'" v-model="sectionForm[field.prop]" value-format="HH:mm:ss" />
          <el-date-picker v-else-if="field.type === 'datetime'" v-model="sectionForm[field.prop]" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" :placeholder="field.placeholder || '请选择时间'" />
          <div v-else-if="field.type === 'upload'" class="upload-field">
            <div v-if="sectionForm[field.prop]" class="upload-file-row">
              <span>{{ fileNameFromUrl(sectionForm[field.prop]) }}</span>
              <el-button link type="primary" @click="openFile(sectionForm[field.prop])">查看</el-button>
            </div>
            <el-upload
              :show-file-list="false"
              :http-request="(options) => handleSectionUpload(field, options)"
              :accept="field.accept"
            >
              <el-button>上传文件</el-button>
            </el-upload>
          </div>
          <el-input v-else-if="field.type === 'textarea'" v-model="sectionForm[field.prop]" type="textarea" :rows="4" :placeholder="field.placeholder" />
          <el-input v-else v-model="sectionForm[field.prop]" :placeholder="field.placeholder" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sectionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingSection" @click="saveSection">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, defineComponent, h, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { ElButton, ElMessage, ElMessageBox, ElPagination, ElTable, ElTableColumn } from 'element-plus'
import AvatarPicker from '../components/AvatarPicker.vue'
import { assetUrl, createResource, deleteResource, getResource, getUser, updateResource, updateUser, uploadFile } from '../api/http'
import { openExternalUrl } from '../native/app.js'
import { createHealthChartOption } from '../utils/healthChart'
import { useResponsiveColumns } from '../utils/viewport'
import { PAGE_SIZE, normalizePage, paginate } from '../utils/pagination'
import { localizeValue } from '../utils/localizeValue'
import { serviceDurationMinutes } from '../utils/serviceDuration'
import { eligiblePersonnel } from '../utils/personnel'
import { personnelByProduct } from '../utils/serviceCategory'

const EditableTable = defineComponent({
  props: { section: String, rows: Array, columns: Array },
  emits: ['create', 'edit', 'remove'],
  setup(props, { emit }) {
    const currentPage = ref(1)
    const pagedRows = computed(() => paginate(props.rows || [], currentPage.value))
    watch(() => props.rows, () => { currentPage.value = 1 })
    watch(() => (props.rows || []).length, (total) => {
      currentPage.value = normalizePage(currentPage.value, total)
    })
    return () => h('div', [
      h('div', { class: 'tab-actions' }, [h(ElButton, { type: 'primary', onClick: () => emit('create', props.section) }, () => '新增')]),
      h(ElTable, { data: pagedRows.value, stripe: true }, () => [
        ...(props.columns || []).map((column) => {
          if (column.type === 'file') {
            return h(ElTableColumn, { prop: column.prop, label: column.label, minWidth: column.width || 120 }, {
              default: ({ row }) => row[column.prop]
                ? h(ElButton, { link: true, type: 'primary', onClick: () => openExternalUrl(assetUrl(row[column.prop])) }, () => '查看')
                : h('span', '-')
            })
          }
          return h(ElTableColumn, { prop: column.prop, label: column.label, minWidth: column.width || 120 }, {
            default: ({ row }) => localizeValue(row[column.prop])
          })
        }),
        h(ElTableColumn, { label: '操作', width: 150, align: 'right' }, {
          default: ({ row }) => [
            h(ElButton, { link: true, type: 'primary', onClick: () => emit('edit', props.section, row) }, () => '编辑'),
            h(ElButton, { link: true, type: 'danger', onClick: () => emit('remove', props.section, row) }, () => '删除')
          ]
        })
      ]),
      (props.rows || []).length > PAGE_SIZE
        ? h(ElPagination, {
            class: 'pagination', background: true, layout: 'prev, pager, next, total',
            pageSize: PAGE_SIZE, total: (props.rows || []).length, currentPage: currentPage.value,
            'onUpdate:currentPage': (page) => { currentPage.value = page }
          })
        : null
    ])
  }
})

const route = useRoute()
const detailColumns = useResponsiveColumns(2)
const activeTab = ref('profile')
const healthChart = ref()
const profileDialogVisible = ref(false)
const sectionDialogVisible = ref(false)
const savingProfile = ref(false)
const savingSection = ref(false)
const avatarPickerRef = ref(null)
const editingSection = ref('')
const editingId = ref(null)
const sectionForm = reactive({})
const productOptions = ref([])
const personnelOptions = ref([])
const profileForm = reactive({
  nickname: '', realName: '', phone: '', avatarUrl: '', gender: '未知', birthday: '', idCard: '', address: '', bio: '', ethnicity: '', education: '',
  height: 0, weight: 0, bloodType: 'A', rhNegative: false, chronicDisease: '', sleepQuality: '良好', smokingFreq: '', drinkingFreq: '', exerciseFreq: '', dietPreference: '', emergencyContact: '', emergencyPhone: ''
})
const user = ref({ id: route.params.id, nickname: '', realName: '', tags: [], medications: [], healthData: [], devices: [], reports: [], orders: [], coupons: [], points: [], contents: [], serviceRecords: [] })

const sectionMap = {
  medications: {
    title: '用药信息',
    resource: 'medications',
    defaults: () => ({ userRef: String(user.value.id), period: '早餐', drugName: '', frequency: '每天', takeTime: '08:00:00', dosage: '', status: '启用' }),
    columns: [{ prop: 'period', label: '时段' }, { prop: 'drugName', label: '药品名称' }, { prop: 'frequency', label: '频率' }, { prop: 'takeTime', label: '时间' }, { prop: 'dosage', label: '剂量' }],
    fields: [{ prop: 'period', label: '时段', required: true }, { prop: 'drugName', label: '药品名称', required: true }, { prop: 'frequency', label: '频率' }, { prop: 'takeTime', label: '服用时间', type: 'time' }, { prop: 'dosage', label: '剂量' }, { prop: 'status', label: '提醒', type: 'select', options: ['启用', '禁用'] }]
  },
  healthData: {
    title: '健康数据',
    resource: 'healthData',
    defaults: () => ({ userRef: String(user.value.id), dataType: 'weight', value: '', unit: 'kg', recordDate: new Date().toISOString().slice(0, 10), recordTime: '08:00:00', source: '后台录入' }),
    columns: [{ prop: 'recordDate', label: '日期' }, { prop: 'dataType', label: '数据类型' }, { prop: 'value', label: '数值' }, { prop: 'unit', label: '单位' }, { prop: 'source', label: '来源' }],
    fields: [{ prop: 'dataType', label: '数据类型', type: 'select', options: [{ label: '体重', value: 'weight' }, { label: '心率', value: 'heart_rate' }, { label: '血压', value: 'blood_pressure' }, { label: '血糖', value: 'blood_sugar' }] }, { prop: 'value', label: '数值', required: true }, { prop: 'unit', label: '单位' }, { prop: 'recordDate', label: '日期', type: 'date' }, { prop: 'recordTime', label: '时间', type: 'time' }, { prop: 'source', label: '来源' }]
  },
  devices: {
    title: '设备信息',
    resource: 'devices',
    defaults: () => ({ userRef: String(user.value.id), deviceName: '', deviceType: 'band', deviceCode: '', status: '启用' }),
    columns: [{ prop: 'deviceName', label: '设备名称' }, { prop: 'deviceType', label: '类型' }, { prop: 'deviceCode', label: '编号' }, { prop: 'status', label: '状态' }],
    fields: [{ prop: 'deviceName', label: '设备名称', required: true }, { prop: 'deviceType', label: '设备类型', type: 'select', options: [{ label: '智能手环', value: 'band' }, { label: '智能手表', value: 'watch' }, { label: '智能体重秤', value: 'scale' }] }, { prop: 'deviceCode', label: '设备编号' }, { prop: 'status', label: '状态', type: 'select', options: ['启用', '禁用', '绑定', '解绑'] }]
  },
  reports: {
    title: '报告信息',
    resource: 'reports',
    defaults: () => ({ userRef: String(user.value.id), title: '', reportType: '健康评估', reportDate: new Date().toISOString().slice(0, 10), doctorName: '', fileUrl: '', summary: '' }),
    columns: [{ prop: 'title', label: '报告标题', width: 180 }, { prop: 'reportType', label: '类型' }, { prop: 'reportDate', label: '日期' }, { prop: 'doctorName', label: '医生' }, { prop: 'fileUrl', label: '文件', type: 'file' }],
    fields: [{ prop: 'title', label: '报告标题', required: true }, { prop: 'reportType', label: '报告类型' }, { prop: 'reportDate', label: '报告日期', type: 'date' }, { prop: 'doctorName', label: '医生' }, { prop: 'fileUrl', label: '报告文件', type: 'upload', category: 'report', accept: '.jpg,.jpeg,.png,.webp,.pdf' }, { prop: 'summary', label: '摘要', type: 'textarea' }]
  },
  orders: {
    title: '订单信息',
    resource: 'orders',
    defaults: () => ({ userRef: String(user.value.id), productId: productOptions.value[0]?.id ? String(productOptions.value[0].id) : '', amount: Number(productOptions.value[0]?.price || 0), status: '待接单' }),
    columns: [{ prop: 'orderNo', label: '订单编号', width: 160 }, { prop: 'productName', label: '商品' }, { prop: 'amount', label: '金额' }, { prop: 'status', label: '状态' }],
    fields: [{ prop: 'productId', label: '商品服务', type: 'product', required: true }, { prop: 'amount', label: '金额', type: 'number', readonly: true }, { prop: 'status', label: '状态', type: 'select', options: ['待接单', '待服务', '已完成', '已关闭', '售后中'] }]
  },
  coupons: {
    title: '优惠券',
    resource: 'coupons',
    defaults: () => ({ userRef: String(user.value.id), name: '', type: '满减', discount: 10, minAmount: 0, status: '未使用', expireDate: new Date().toISOString().slice(0, 10) }),
    columns: [{ prop: 'couponNo', label: '券编号', width: 160 }, { prop: 'name', label: '名称' }, { prop: 'discount', label: '优惠' }, { prop: 'status', label: '状态' }, { prop: 'expireDate', label: '过期日' }],
    fields: [{ prop: 'couponNo', label: '券编号' }, { prop: 'name', label: '名称', required: true }, { prop: 'type', label: '类型', type: 'select', options: ['满减', '折扣', '现金'] }, { prop: 'discount', label: '优惠', type: 'number' }, { prop: 'minAmount', label: '门槛', type: 'number' }, { prop: 'status', label: '状态', type: 'select', options: ['未使用', '已使用', '已过期'] }, { prop: 'expireDate', label: '过期日', type: 'date' }]
  },
  userPoints: {
    title: '积分',
    resource: 'userPoints',
    defaults: () => ({ userRef: String(user.value.id), points: 0, totalEarned: 0, totalSpent: 0, level: '普通', growthValue: 0 }),
    columns: [{ prop: 'points', label: '积分' }, { prop: 'level', label: '等级' }, { prop: 'growthValue', label: '成长值' }],
    fields: [{ prop: 'points', label: '当前积分', type: 'number' }, { prop: 'totalEarned', label: '累计获得', type: 'number' }, { prop: 'totalSpent', label: '累计消耗', type: 'number' }, { prop: 'level', label: '等级' }, { prop: 'growthValue', label: '成长值', type: 'number' }]
  },
  posts: {
    title: '内容信息',
    resource: 'posts',
    defaults: () => ({ title: '', publisher: user.value.realName, author: user.value.realName, status: '已发布' }),
    columns: [{ prop: 'title', label: '标题' }, { prop: 'type', label: '类型' }, { prop: 'status', label: '状态' }],
    fields: [{ prop: 'title', label: '标题', required: true }, { prop: 'publisher', label: '发布人' }, { prop: 'author', label: '作者' }, { prop: 'status', label: '状态', type: 'select', options: ['已发布', '草稿'] }]
  },
  workOrders: {
    title: '服务记录',
    resource: 'workOrders',
    defaults: () => ({ userRef: String(user.value.id), productId: productOptions.value[0]?.id ? String(productOptions.value[0].id) : '', personnelId: '', amount: Number(productOptions.value[0]?.price || 0), status: '待服务', serviceTime: '' }),
    columns: [{ prop: 'orderNo', label: '工单编号', width: 160 }, { prop: 'serviceItem', label: '服务项目' }, { prop: 'personnelName', label: '服务人员' }, { prop: 'amount', label: '金额' }, { prop: 'status', label: '状态' }],
    fields: [{ prop: 'productId', label: '商品服务', type: 'product', required: true }, { prop: 'personnelId', label: '服务人员', type: 'personnel', required: true }, { prop: 'amount', label: '金额', type: 'number', readonly: true }, { prop: 'status', label: '状态', type: 'select', options: ['待服务', '服务中', '已完成', '已取消'] }, { prop: 'serviceTime', label: '服务时间', type: 'datetime', required: true, placeholder: '请选择服务开始时间' }]
  }
}

const currentSection = computed(() => sectionMap[editingSection.value] || { title: '', fields: [], resource: '' })
const sectionPersonnelOptions = computed(() => personnelByProduct(personnelOptions.value, productOptions.value, sectionForm.productId))
const selectOptionValue = (option) => typeof option === 'object' ? option.value : option
const selectOptionLabel = (option) => typeof option === 'object' ? option.label : option
const sectionDialogTitle = computed(() => `${editingId.value ? '编辑' : '新增'}${currentSection.value.title}`)
const healthDataRows = computed(() => (user.value.healthData || []).map((item) => ({
  ...item,
  dataType: item.dataType || (item.heartRate != null && item.weight == null ? 'heart_rate' : 'weight'),
  value: item.value ?? item.weight ?? item.heartRate ?? '',
  unit: item.unit || (item.heartRate != null && item.weight == null ? 'bpm' : 'kg')
})))
const profileFields = computed(() => [
  { label: '昵称', value: user.value.nickname }, { label: '真实姓名', value: user.value.realName }, { label: '性别', value: user.value.gender }, { label: '出生日期', value: user.value.birthday },
  { label: '手机号', value: user.value.phone }, { label: '身份证号', value: user.value.idCard }, { label: '家庭住址', value: user.value.address }, { label: '个人简介', value: user.value.bio },
  { label: '民族', value: user.value.ethnicity }, { label: '文化程度', value: user.value.education }, { label: '身高', value: user.value.height ? `${user.value.height} 厘米` : '' }, { label: '体重', value: user.value.weight ? `${user.value.weight} 千克` : '' },
  { label: '血型', value: user.value.bloodType }, { label: 'RH阴性', value: user.value.rhNegative ? '是' : '否' }, { label: '慢性病', value: user.value.chronicDisease }, { label: '睡眠质量', value: user.value.sleepQuality },
  { label: '吸烟频率', value: user.value.smokingFreq }, { label: '饮酒频率', value: user.value.drinkingFreq }, { label: '运动频率', value: user.value.exerciseFreq }, { label: '饮食偏好', value: user.value.dietPreference },
  { label: '紧急联系人', value: user.value.emergencyContact }, { label: '紧急电话', value: user.value.emergencyPhone }
])

function drawHealthChart() {
  if (!healthChart.value) return
  echarts.getInstanceByDom(healthChart.value)?.dispose()
  echarts.init(healthChart.value).setOption(createHealthChartOption(user.value.healthData || []))
}
function openProfileEdit() {
  Object.keys(profileForm).forEach((key) => { profileForm[key] = user.value[key] ?? profileForm[key] })
  profileForm.phone = String(user.value.phone || '').includes('****') ? '' : user.value.phone || ''
  profileDialogVisible.value = true
  nextTick(() => avatarPickerRef.value?.reset(profileForm.avatarUrl))
}
function openSectionCreate(section) {
  editingSection.value = section
  editingId.value = null
  Object.keys(sectionForm).forEach((key) => delete sectionForm[key])
  Object.assign(sectionForm, sectionMap[section].defaults())
  sectionDialogVisible.value = true
}
function openSectionEdit(section, row) {
  editingSection.value = section
  editingId.value = row.id
  Object.keys(sectionForm).forEach((key) => delete sectionForm[key])
  Object.assign(sectionForm, sectionMap[section].defaults(), row)
  if (section === 'orders' || section === 'workOrders') sectionForm.productId = String(row.productId || '')
  if (section === 'workOrders') sectionForm.personnelId = String(row.personnelId || '')
  sectionForm.userRef = String(user.value.id)
  sectionDialogVisible.value = true
}
function syncSectionProductAmount(productId) {
  const selected = productOptions.value.find((item) => String(item.id) === String(productId))
  sectionForm.amount = Number(selected?.price || 0)
  if (!sectionPersonnelOptions.value.some((person) => String(person.id) === String(sectionForm.personnelId))) {
    sectionForm.personnelId = ''
  }
}
async function loadUser() {
  user.value = await getUser(route.params.id)
  await nextTick()
  drawHealthChart()
}
async function loadProductOptions() {
  try {
    const data = await getResource('products')
    productOptions.value = (data.list || data || []).filter((item) => item.status !== '下架')
  } catch (error) {
    productOptions.value = []
  }
}
async function loadPersonnelOptions() {
  try {
    const data = await getResource('personnel')
    personnelOptions.value = eligiblePersonnel(data.list || data || [])
  } catch (error) {
    personnelOptions.value = []
  }
}
async function saveProfile() {
  if (!profileForm.realName.trim()) {
    ElMessage.warning('请填写真实姓名')
    return
  }
  savingProfile.value = true
  try {
    const avatarUrl = await avatarPickerRef.value?.commitSelection()
    await updateUser(route.params.id, { ...profileForm, avatarUrl: avatarUrl || '' })
    await loadUser()
    profileDialogVisible.value = false
    ElMessage.success('个人信息已保存')
  } catch (error) {
    ElMessage.error(error.message || '保存失败，请确认后端和数据库已启动')
  } finally {
    savingProfile.value = false
  }
}
async function handleSectionUpload(field, options) {
  try {
    const data = await uploadFile(options.file, field.category || 'report')
    sectionForm[field.prop] = data.url
    options.onSuccess?.(data)
    ElMessage.success('上传成功')
  } catch (error) {
    options.onError?.(error)
    ElMessage.error(error.message || '上传失败')
  }
}
function openFile(url) {
  if (!url) return
  openExternalUrl(assetUrl(url))
}
function fileNameFromUrl(url) {
  return String(url || '').split('/').filter(Boolean).pop() || '已上传文件'
}
async function saveSection() {
  const required = currentSection.value.fields.find((field) => field.required && !String(sectionForm[field.prop] || '').trim())
  if (required) {
    ElMessage.warning(`请填写${required.label}`)
    return
  }
  savingSection.value = true
  try {
    if (editingId.value) await updateResource(currentSection.value.resource, editingId.value, sectionForm)
    else await createResource(currentSection.value.resource, sectionForm)
    await loadUser()
    sectionDialogVisible.value = false
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败，请确认后端和数据库已启动')
  } finally {
    savingSection.value = false
  }
}
async function removeSectionRow(section, row) {
  try {
    await ElMessageBox.confirm(`确认删除「${row.title || row.name || row.drugName || row.deviceName || row.orderNo || row.id}」？`, '删除确认', { type: 'warning' })
    await deleteResource(sectionMap[section].resource, row.id)
    await loadUser()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  try {
    await Promise.all([loadUser(), loadProductOptions(), loadPersonnelOptions()])
  } catch (error) {
    drawHealthChart()
  }
})

watch(activeTab, async (tab) => {
  if (tab !== 'data') return
  await nextTick()
  drawHealthChart()
})

onBeforeUnmount(() => {
  if (healthChart.value) echarts.getInstanceByDom(healthChart.value)?.dispose()
})
</script>
