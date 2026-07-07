<template>
  <section>
    <div class="page-heading">
      <div><h1>{{ title }}</h1><p>{{ descriptor }}</p></div>
      <el-button v-if="canCreate" type="primary" :icon="Plus" @click="openCreate">新增</el-button>
    </div>
    <div class="filters">
      <el-select v-model="filters.status" placeholder="状态" clearable>
        <el-option label="待处理" value="待处理" />
        <el-option label="处理中" value="处理中" />
        <el-option label="已完成" value="已完成" />
      </el-select>
      <el-date-picker v-model="filters.dateRange" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" />
      <el-input v-model="filters.keyword" placeholder="关键词搜索" clearable />
      <el-button type="primary" @click="load">搜索</el-button>
      <el-button @click="reset">重置</el-button>
    </div>
    <div class="toolbar">
      <el-button>批量操作</el-button>
      <el-button>导出数据</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column v-for="column in columns" :key="column.prop" :prop="column.prop" :label="column.label" :min-width="column.width || 120">
        <template #default="{ row }">
          <el-tag v-if="isStatusColumn(column.prop)" :type="tagType(row[column.prop])">{{ row[column.prop] }}</el-tag>
          <span v-else>{{ row[column.prop] }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190" align="right">
        <template #default="{ row }">
          <el-button link type="primary">详情</el-button>
          <el-button link @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination"><el-pagination layout="prev, pager, next, total" :total="total" /></div>

    <el-dialog v-model="dialogVisible" :title="editingId ? `编辑${title}` : `新增${title}`" width="620px">
      <el-form :model="form" label-width="96px">
        <el-form-item v-for="field in createFields" :key="field.prop" :label="field.label" :required="field.required">
          <el-input-number v-if="field.type === 'number'" v-model="form[field.prop]" :min="0" controls-position="right" />
          <el-select v-else-if="field.type === 'select'" v-model="form[field.prop]" placeholder="请选择">
            <el-option v-for="option in field.options" :key="option" :label="option" :value="option" />
          </el-select>
          <el-input v-else-if="field.type === 'textarea'" v-model="form[field.prop]" type="textarea" :rows="4" :placeholder="field.placeholder" />
          <el-input v-else v-model="form[field.prop]" :placeholder="field.placeholder" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createResource, deleteResource, getResource, updateResource } from '../api/http'
import { fallbackRows } from '../api/fallback'

const route = useRoute()
const filters = reactive({ status: '', dateRange: [], keyword: '' })
const rows = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = reactive({})
const resource = computed(() => route.meta.resourceFromParam ? route.params.resource : route.meta.resource)
const title = computed(() => route.meta.title)
const descriptor = computed(() => descriptors[resource.value] || '列表筛选、批量操作、状态流转与数据维护')
const columns = computed(() => columnMap[resource.value] || defaultColumns)
const createFields = computed(() => createFieldMap[resource.value] || createFieldMap.posts)
const canCreate = computed(() => !['audits', 'logs'].includes(resource.value))

const descriptors = {
  personnel: '服务人员生命周期、负责区域与启用状态管理',
  audits: '服务人员资质审核，通过或驳回申请',
  workOrders: '待服务、服务中、已完成、已取消工单流转',
  products: '家政护理、康复理疗、上门体检服务商品管理',
  orders: '订单从待接单到服务完成的全生命周期管理',
  afterSales: '售后申请审核、处理和关闭',
  reviews: '服务评价回复、隐藏与统计',
  posts: '生活圈动态内容运营',
  activities: '活动发布、报名与导出',
  articles: '健康资讯发布与互动数据维护',
  staffs: '后台员工账号和角色分配',
  roles: 'RBAC 角色与模块权限配置',
  logs: '系统操作审计日志'
}
const defaultColumns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'title', label: '名称', width: 180 },
  { prop: 'status', label: '状态' }
]
const columnMap = {
  personnel: [{ prop: 'name', label: '服务人员' }, { prop: 'serviceType', label: '服务类型' }, { prop: 'area', label: '负责区域' }, { prop: 'status', label: '状态' }, { prop: 'updatedAt', label: '更新时间', width: 170 }],
  audits: [{ prop: 'name', label: '申请人' }, { prop: 'serviceType', label: '服务类型' }, { prop: 'auditStatus', label: '审核状态' }, { prop: 'phone', label: '手机号' }, { prop: 'updatedAt', label: '申请时间', width: 170 }],
  workOrders: [{ prop: 'orderNo', label: '工单编号', width: 170 }, { prop: 'serviceItem', label: '服务项目' }, { prop: 'customer', label: '服务客户' }, { prop: 'status', label: '状态' }, { prop: 'updatedAt', label: '派单时间', width: 170 }],
  products: [{ prop: 'name', label: '商品信息', width: 190 }, { prop: 'category', label: '分类' }, { prop: 'price', label: '价格' }, { prop: 'status', label: '状态' }, { prop: 'updatedAt', label: '更新时间', width: 170 }],
  orders: [{ prop: 'orderNo', label: '订单编号', width: 170 }, { prop: 'productName', label: '商品信息' }, { prop: 'buyer', label: '买家' }, { prop: 'amount', label: '金额' }, { prop: 'status', label: '订单状态' }],
  afterSales: [{ prop: 'orderNo', label: '订单编号', width: 170 }, { prop: 'applicant', label: '申请人' }, { prop: 'reason', label: '售后原因' }, { prop: 'status', label: '状态' }],
  reviews: [{ prop: 'productName', label: '商品' }, { prop: 'user', label: '用户' }, { prop: 'rating', label: '评分' }, { prop: 'status', label: '状态' }],
  staffs: [{ prop: 'staffNo', label: '员工编号' }, { prop: 'name', label: '姓名' }, { prop: 'role', label: '角色' }, { prop: 'status', label: '状态' }],
  roles: [{ prop: 'name', label: '角色名称' }, { prop: 'description', label: '描述', width: 220 }, { prop: 'status', label: '状态' }],
  logs: [{ prop: 'operator', label: '操作人' }, { prop: 'actionType', label: '操作类型' }, { prop: 'target', label: '操作对象' }, { prop: 'createdAt', label: '操作时间', width: 170 }]
}
const createFieldMap = {
  personnel: [
    { prop: 'name', label: '姓名', required: true, placeholder: '服务人员姓名' },
    { prop: 'phone', label: '手机号', placeholder: '不填则自动生成' },
    { prop: 'serviceType', label: '服务类型', type: 'select', options: ['家政护理', '康复理疗', '上门体检'] },
    { prop: 'area', label: '负责区域', placeholder: '如：浦东新区' },
    { prop: 'auditStatus', label: '审核状态', type: 'select', options: ['待审核', '已通过', '已驳回'] }
  ],
  workOrders: [
    { prop: 'serviceItem', label: '服务项目', required: true, placeholder: '如：助浴护理' },
    { prop: 'amount', label: '金额', type: 'number' },
    { prop: 'customerId', label: '客户ID', type: 'number' },
    { prop: 'status', label: '状态', type: 'select', options: ['pending', 'service_in', 'completed', 'cancelled'] }
  ],
  products: [
    { prop: 'name', label: '商品名称', required: true, placeholder: '如：助餐陪诊服务' },
    { prop: 'category', label: '分类', type: 'select', options: ['家政护理', '康复理疗', '上门体检'] },
    { prop: 'price', label: '价格', type: 'number' },
    { prop: 'status', label: '状态', type: 'select', options: ['上架', '下架'] }
  ],
  orders: [
    { prop: 'productName', label: '商品名称', placeholder: '不填则使用默认商品' },
    { prop: 'buyerId', label: '买家ID', type: 'number' },
    { prop: 'amount', label: '金额', type: 'number' },
    { prop: 'serviceType', label: '服务类型', type: 'select', options: ['家政护理', '康复理疗', '上门体检'] },
    { prop: 'status', label: '状态', type: 'select', options: ['pending_accept', 'pending_service', 'completed', 'closed', 'after_sale'] }
  ],
  afterSales: [
    { prop: 'orderId', label: '订单ID', type: 'number' },
    { prop: 'applicantId', label: '申请人ID', type: 'number' },
    { prop: 'reason', label: '售后原因', required: true, placeholder: '请输入售后原因' },
    { prop: 'status', label: '状态', type: 'select', options: ['处理中', '已完成', '已关闭'] }
  ],
  reviews: [
    { prop: 'userId', label: '用户ID', type: 'number' },
    { prop: 'productId', label: '商品ID', type: 'number' },
    { prop: 'rating', label: '评分', type: 'number' },
    { prop: 'content', label: '评价内容', type: 'textarea', placeholder: '请输入评价内容' }
  ],
  staffs: [
    { prop: 'staffNo', label: '员工编号', placeholder: '不填则自动生成' },
    { prop: 'name', label: '姓名', required: true, placeholder: '员工姓名' },
    { prop: 'phone', label: '手机号', placeholder: '不填则自动生成' },
    { prop: 'roleId', label: '角色ID', type: 'number' },
    { prop: 'remark', label: '备注', placeholder: '备注' }
  ],
  roles: [
    { prop: 'name', label: '角色名称', required: true, placeholder: '如：运营主管' },
    { prop: 'description', label: '描述', placeholder: '角色描述' }
  ],
  agreements: [
    { prop: 'title', label: '协议标题', required: true, placeholder: '协议标题' },
    { prop: 'type', label: '类型', placeholder: 'privacy/service/custom' },
    { prop: 'content', label: '内容', type: 'textarea', placeholder: '协议内容' }
  ],
  posts: [
    { prop: 'title', label: '标题', required: true, placeholder: '内容标题' },
    { prop: 'publisher', label: '发布人', placeholder: '发布人' },
    { prop: 'author', label: '作者', placeholder: '作者' },
    { prop: 'location', label: '地点', placeholder: '活动/机构地点' },
    { prop: 'quota', label: '名额', type: 'number' }
  ],
  activities: [
    { prop: 'title', label: '活动名称', required: true, placeholder: '活动名称' },
    { prop: 'location', label: '地点', placeholder: '活动地点' },
    { prop: 'quota', label: '名额', type: 'number' }
  ],
  articles: [
    { prop: 'title', label: '资讯标题', required: true, placeholder: '资讯标题' },
    { prop: 'author', label: '作者', placeholder: '作者' }
  ]
}
createFieldMap.recipes = createFieldMap.articles
createFieldMap.diseases = createFieldMap.articles
createFieldMap.institutions = createFieldMap.activities
createFieldMap.videos = createFieldMap.articles
createFieldMap.comments = createFieldMap.posts
createFieldMap.foods = createFieldMap.articles
createFieldMap.assessments = createFieldMap.articles

function isStatusColumn(prop) {
  return ['status', 'auditStatus'].includes(prop)
}
function tagType(value) {
  if (['启用', '上架', '已发布', '已完成', '已显示'].includes(value)) return 'success'
  if (['待审核', '待服务', '待接单', '报名中', '处理中'].includes(value)) return 'warning'
  if (['服务中'].includes(value)) return 'primary'
  return 'info'
}
async function load() {
  try {
    const data = await getResource(resource.value, filters)
    rows.value = data.list || data
    total.value = data.total || rows.value.length
  } catch (error) {
    rows.value = fallbackRows[resource.value] || fallbackRows.posts
    total.value = rows.value.length
  }
}
function reset() {
  filters.status = ''
  filters.dateRange = []
  filters.keyword = ''
  load()
}
function openCreate() {
  editingId.value = null
  Object.keys(form).forEach((key) => delete form[key])
  createFields.value.forEach((field) => {
    if (field.type === 'number') form[field.prop] = 0
    else if (field.type === 'select') form[field.prop] = field.options[0]
    else form[field.prop] = ''
  })
  dialogVisible.value = true
}
function openEdit(row) {
  editingId.value = row.id
  Object.keys(form).forEach((key) => delete form[key])
  createFields.value.forEach((field) => {
    const value = row[field.prop]
    if (field.type === 'number') form[field.prop] = Number(value || 0)
    else if (field.type === 'select') form[field.prop] = value || field.options[0]
    else form[field.prop] = value || ''
  })
  dialogVisible.value = true
}
async function submitCreate() {
  const required = createFields.value.find((field) => field.required && !String(form[field.prop] || '').trim())
  if (required) {
    ElMessage.warning(`请填写${required.label}`)
    return
  }
  saving.value = true
  try {
    if (editingId.value) await updateResource(resource.value, editingId.value, form)
    else await createResource(resource.value, form)
    ElMessage.success(editingId.value ? '编辑成功' : '新增成功')
    dialogVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error('新增失败，请确认后端和数据库已启动')
  } finally {
    saving.value = false
  }
}
async function removeRow(row) {
  try {
    const name = row.name || row.title || row.orderNo || row.productName || row.id
    await ElMessageBox.confirm(`确认删除「${name}」？`, '删除确认', { type: 'warning' })
    await deleteResource(resource.value, row.id)
    ElMessage.success('删除成功')
    await load()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}
watch(() => route.fullPath, load)
onMounted(load)
</script>
