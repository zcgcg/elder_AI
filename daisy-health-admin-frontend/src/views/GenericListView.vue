<template>
  <section>
    <div class="page-heading">
      <div><h1>{{ title }}</h1><p>{{ descriptor }}</p></div>
      <el-button v-if="canCreate" type="primary" :icon="Plus" @click="openCreate">新增</el-button>
    </div>
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false">
      <template #default><el-button link type="primary" @click="load">重新加载</el-button></template>
    </el-alert>
    <div v-if="searchEnabled" class="filters">
      <el-select v-if="resource === 'workOrders'" v-model="filters.personnelId" filterable clearable placeholder="查询服务人员">
        <el-option v-for="person in personnelOptions" :key="person.id" :label="`${person.name} · ${person.phone}`" :value="String(person.id)" />
      </el-select>
      <el-select v-if="resource === 'workOrders'" v-model="filters.customerId" filterable clearable placeholder="查询用户">
        <el-option v-for="user in userOptions" :key="user.id" :label="`${user.realName || user.nickname} · ${user.phone}`" :value="String(user.id)" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态" clearable>
        <el-option v-for="status in filterStatuses" :key="status" :label="status" :value="status" />
      </el-select>
      <el-date-picker v-model="filters.dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" />
      <el-input v-model="filters.keyword" placeholder="关键词搜索" clearable @keyup.enter="load" />
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
          <img v-else-if="column.type === 'image' && row[column.prop]" class="table-thumb" :src="assetUrl(row[column.prop])" :alt="row.title || row.name || column.label" />
          <el-button v-else-if="column.type === 'file'" link type="primary" :disabled="!row[column.prop]" @click="openFile(row[column.prop])">查看</el-button>
          <span v-else>{{ row[column.prop] }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190" align="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button link @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination"><el-pagination layout="prev, pager, next, total" :total="total" /></div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px">
      <el-form :model="form" label-width="112px">
        <el-form-item v-for="field in createFields" :key="field.prop" :label="field.label" :required="field.required">
          <el-input-number v-if="field.type === 'number'" v-model="form[field.prop]" :min="0" controls-position="right" :disabled="readonly || field.readonly" />
          <el-select
            v-else-if="field.type === 'user'"
            v-model="form[field.prop]"
            filterable
            clearable
            placeholder="输入姓名/手机号搜索"
            :disabled="readonly"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.realName || user.nickname} · ${user.phone || user.id}`"
              :value="String(user.id)"
            />
          </el-select>
          <el-select
            v-else-if="field.type === 'product'"
            v-model="form[field.prop]"
            filterable
            clearable
            placeholder="请选择商品服务"
            :disabled="readonly"
            @change="handleProductChange(field, $event)"
          >
            <el-option
              v-for="product in productOptions"
              :key="product.id"
              :label="`${product.name} · ¥${product.price} · ${product.itemType || '商品'}`"
              :value="String(product.id)"
            />
          </el-select>
          <el-select
            v-else-if="field.type === 'personnel'"
            v-model="form[field.prop]"
            filterable
            clearable
            placeholder="请选择服务人员"
            :disabled="readonly"
          >
            <el-option
              v-for="person in personnelOptions"
              :key="person.id"
              :label="`${person.name} · ${person.serviceType} · ${person.phone}`"
              :value="String(person.id)"
            />
          </el-select>
          <el-select
            v-else-if="field.type === 'activity'"
            v-model="form[field.prop]"
            filterable
            clearable
            placeholder="请选择活动"
            :disabled="readonly"
          >
            <el-option
              v-for="activity in activityOptions"
              :key="activity.id"
              :label="activity.title"
              :value="String(activity.id)"
            />
          </el-select>
          <el-select v-else-if="field.type === 'select'" v-model="form[field.prop]" placeholder="请选择" :disabled="readonly">
            <el-option v-for="option in field.options" :key="option" :label="option" :value="option" :disabled="isSelectOptionDisabled(field, option)" />
          </el-select>
          <el-date-picker
            v-else-if="field.type === 'datetime'"
            v-model="form[field.prop]"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            :placeholder="field.placeholder || '请选择时间'"
            :disabled="readonly"
          />
          <div v-else-if="field.type === 'upload'" class="upload-field">
            <img v-if="field.preview === 'image' && form[field.prop]" class="upload-preview" :src="assetUrl(form[field.prop])" :alt="field.label" />
            <div v-else-if="form[field.prop]" class="upload-file-row">
              <span>{{ fileNameFromUrl(form[field.prop]) }}</span>
              <el-button link type="primary" @click="openFile(form[field.prop])">查看</el-button>
            </div>
            <el-upload
              :show-file-list="false"
              :http-request="(options) => handleFieldUpload(field, options)"
              :accept="field.accept"
              :disabled="readonly"
            >
              <el-button :disabled="readonly">上传文件</el-button>
            </el-upload>
          </div>
          <el-input v-else-if="field.type === 'textarea'" v-model="form[field.prop]" type="textarea" :rows="4" :placeholder="field.placeholder" :disabled="readonly" />
          <el-input v-else v-model="form[field.prop]" :placeholder="field.placeholder" :disabled="readonly" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ readonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!readonly" type="primary" :loading="saving" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { assetUrl, createResource, deleteResource, getResource, getUsers, updateResource, uploadFile } from '../api/http'
import { toQueryParams } from '../utils/query'
import { isListSearchEnabled, listSearchStatuses } from '../utils/listSearch'

const route = useRoute()
const filters = reactive({ status: '', dateRange: [], keyword: '', personnelId: '', customerId: '' })
const rows = ref([])
const total = ref(0)
const error = ref('')
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = reactive({})
const userOptions = ref([])
const productOptions = ref([])
const activityOptions = ref([])
const personnelOptions = ref([])
const readonly = ref(false)
const resource = computed(() => route.meta.resourceFromParam ? route.params.resource : route.meta.resource)
const title = computed(() => titleMap[resource.value] || route.meta.title)
const dialogTitle = computed(() => readonly.value ? `${title.value}详情` : (editingId.value ? `编辑${title.value}` : `新增${title.value}`))
const descriptor = computed(() => descriptors[resource.value] || '列表浏览、批量操作、状态流转与数据维护')
const columns = computed(() => columnMap[resource.value] || defaultColumns)
const createFields = computed(() => createFieldMap[resource.value] || createFieldMap.posts)
const canCreate = computed(() => {
  if (['audits', 'logs'].includes(resource.value)) return false
  if (resource.value === 'memberLevels') return hasAvailableOption('name')
  if (resource.value === 'pointsRules') return hasAvailableOption('actionType')
  return true
})
const searchEnabled = computed(() => isListSearchEnabled(resource.value))
const filterStatuses = computed(() => listSearchStatuses(resource.value))

const descriptors = {
  personnel: '服务人员生命周期、负责区域与启用状态管理',
  audits: '服务人员资质审核，通过或驳回申请',
  workOrders: '待服务、服务中、已完成、已取消工单流转',
  products: '统一维护商品与服务，工单金额以此处价格为准',
  orders: '订单从待接单到服务完成的全生命周期管理',
  afterSales: '售后申请审核、处理和关闭',
  reviews: '服务评价回复、隐藏与统计',
  posts: '生活圈动态内容运营',
  activities: '活动发布、报名与导出',
  articles: '健康资讯发布与互动数据维护',
  staffs: '后台员工账号和角色分配',
  roles: 'RBAC 角色与模块权限配置',
  logs: '系统操作审计日志',
  devices: '用户绑定设备与同步状态管理',
  reports: '用户健康报告、体检报告与评估摘要',
  healthSettings: '健康告警阈值、步数目标与用药提醒设置',
  coupons: '优惠券发放、状态与过期时间管理',
  userPoints: '用户积分、等级与成长值管理',
  memberLevels: '会员等级和成长值区间配置',
  pointsRules: '签到、订单、评价等积分和成长值规则',
  productCategories: '家政护理、康复理疗、上门体检分类管理',
  serviceItems: '商品下属服务项目、时长与价格维护',
  banners: '首页和活动位轮播图配置',
  topics: '生活圈话题与动态数量维护',
  activityEnrolls: '活动报名用户、状态和备注管理',
  recipes: '健康膳食菜谱管理',
  diseases: '疾病宝典条目和健康建议',
  institutions: '养老机构资料维护',
  videos: '健康讲堂视频内容管理',
  foods: '食物营养成分维护',
  assessments: '健康测评题目和规则管理'
}
const titleMap = {
  devices: '设备信息',
  reports: '报告信息',
  healthSettings: '健康设置',
  coupons: '优惠券管理',
  userPoints: '用户积分',
  pointsRecords: '积分记录',
  memberLevels: '等级管理',
  pointsRules: '积分规则',
  productCategories: '分类管理',
  serviceItems: '服务项目管理',
  banners: '轮播图管理',
  topics: '话题管理',
  activityEnrolls: '活动报名',
  recipes: '食谱管理',
  diseases: '疾病宝典',
  institutions: '养老机构',
  videos: '健康讲堂',
  foods: '食物管理',
  assessments: '测评管理',
  assessmentResults: '测评结果'
}
const defaultColumns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'title', label: '名称', width: 180 },
  { prop: 'status', label: '状态' }
]
const columnMap = {
  personnel: [{ prop: 'name', label: '服务人员' }, { prop: 'serviceType', label: '服务类型' }, { prop: 'area', label: '负责区域' }, { prop: 'status', label: '状态' }, { prop: 'updatedAt', label: '更新时间', width: 170 }],
  audits: [{ prop: 'name', label: '申请人' }, { prop: 'serviceType', label: '服务类型' }, { prop: 'auditStatus', label: '审核状态' }, { prop: 'phone', label: '手机号' }, { prop: 'updatedAt', label: '申请时间', width: 170 }],
  workOrders: [{ prop: 'orderNo', label: '工单编号', width: 170 }, { prop: 'serviceItem', label: '服务项目' }, { prop: 'personnelName', label: '服务人员' }, { prop: 'customer', label: '服务客户' }, { prop: 'status', label: '状态' }, { prop: 'updatedAt', label: '派单时间', width: 170 }],
  products: [{ prop: 'name', label: '商品服务', width: 190 }, { prop: 'itemType', label: '类型' }, { prop: 'category', label: '分类' }, { prop: 'duration', label: '时长(分钟)' }, { prop: 'price', label: '价格' }, { prop: 'status', label: '状态' }, { prop: 'updatedAt', label: '更新时间', width: 170 }],
  orders: [{ prop: 'orderNo', label: '订单编号', width: 170 }, { prop: 'productName', label: '商品信息' }, { prop: 'buyer', label: '买家' }, { prop: 'amount', label: '金额' }, { prop: 'serviceType', label: '服务类型' }, { prop: 'status', label: '订单状态' }],
  afterSales: [{ prop: 'orderNo', label: '订单编号', width: 170 }, { prop: 'applicant', label: '申请人' }, { prop: 'reason', label: '售后原因' }, { prop: 'status', label: '状态' }],
  reviews: [{ prop: 'productName', label: '商品' }, { prop: 'user', label: '用户' }, { prop: 'rating', label: '评分' }, { prop: 'status', label: '状态' }],
  staffs: [{ prop: 'staffNo', label: '员工编号' }, { prop: 'name', label: '姓名' }, { prop: 'role', label: '角色' }, { prop: 'status', label: '状态' }],
  roles: [{ prop: 'name', label: '角色名称' }, { prop: 'description', label: '描述', width: 220 }, { prop: 'status', label: '状态' }],
  logs: [{ prop: 'operator', label: '操作人' }, { prop: 'actionType', label: '操作类型' }, { prop: 'target', label: '操作对象' }, { prop: 'createdAt', label: '操作时间', width: 170 }]
}
Object.assign(columnMap, {
  devices: [{ prop: 'userName', label: '用户' }, { prop: 'deviceName', label: '设备名称' }, { prop: 'deviceType', label: '类型' }, { prop: 'deviceCode', label: '设备编号' }, { prop: 'status', label: '状态' }],
  reports: [{ prop: 'userName', label: '用户' }, { prop: 'title', label: '报告标题', width: 220 }, { prop: 'reportType', label: '类型' }, { prop: 'reportDate', label: '报告日期' }, { prop: 'doctorName', label: '医生' }, { prop: 'fileUrl', label: '文件', type: 'file' }],
  healthSettings: [{ prop: 'userName', label: '用户' }, { prop: 'stepGoal', label: '步数目标' }, { prop: 'sleepGoal', label: '睡眠目标' }, { prop: 'status', label: '用药提醒' }],
  coupons: [{ prop: 'couponNo', label: '券编号', width: 170 }, { prop: 'name', label: '名称' }, { prop: 'type', label: '类型' }, { prop: 'discount', label: '优惠' }, { prop: 'status', label: '状态' }, { prop: 'expireDate', label: '过期日' }],
  userPoints: [{ prop: 'userName', label: '用户' }, { prop: 'points', label: '积分' }, { prop: 'level', label: '等级' }, { prop: 'growthValue', label: '成长值' }],
  pointsRecords: [{ prop: 'userName', label: '用户' }, { prop: 'changeValue', label: '变动' }, { prop: 'reason', label: '原因' }, { prop: 'createdAt', label: '时间' }],
  memberLevels: [{ prop: 'name', label: '等级名称' }, { prop: 'minGrowth', label: '成长值下限' }, { prop: 'maxGrowth', label: '成长值上限' }, { prop: 'status', label: '状态' }],
  pointsRules: [{ prop: 'actionType', label: '行为' }, { prop: 'description', label: '说明' }, { prop: 'points', label: '积分' }, { prop: 'growth', label: '成长值' }, { prop: 'status', label: '状态' }],
  productCategories: [{ prop: 'name', label: '分类名称' }, { prop: 'code', label: '编码' }, { prop: 'description', label: '描述' }, { prop: 'sortOrder', label: '排序' }, { prop: 'status', label: '状态' }],
  serviceItems: [{ prop: 'productName', label: '商品' }, { prop: 'name', label: '项目名称' }, { prop: 'duration', label: '时长' }, { prop: 'price', label: '价格' }, { prop: 'status', label: '状态' }],
  banners: [{ prop: 'title', label: '标题' }, { prop: 'imageUrl', label: '图片', type: 'image', width: 150 }, { prop: 'location', label: '位置' }, { prop: 'sortOrder', label: '排序' }, { prop: 'status', label: '状态' }],
  activities: [{ prop: 'coverUrl', label: '封面', type: 'image', width: 130 }, { prop: 'title', label: '活动名称', width: 180 }, { prop: 'location', label: '地点' }, { prop: 'startTime', label: '开始时间', width: 170 }, { prop: 'quota', label: '名额' }, { prop: 'enrolled', label: '已报名' }, { prop: 'status', label: '状态' }],
  topics: [{ prop: 'name', label: '话题名称' }, { prop: 'description', label: '描述' }, { prop: 'postCount', label: '动态数' }, { prop: 'status', label: '状态' }],
  activityEnrolls: [{ prop: 'activityTitle', label: '活动' }, { prop: 'userName', label: '用户' }, { prop: 'status', label: '状态' }, { prop: 'remark', label: '备注' }],
  recipes: [{ prop: 'title', label: '菜谱名称' }, { prop: 'category', label: '分类' }, { prop: 'calories', label: '热量' }, { prop: 'suitableFor', label: '适宜人群' }, { prop: 'status', label: '状态' }],
  diseases: [{ prop: 'title', label: '疾病名称' }, { prop: 'category', label: '分类' }, { prop: 'summary', label: '简介', width: 220 }, { prop: 'status', label: '状态' }],
  institutions: [{ prop: 'title', label: '机构名称' }, { prop: 'type', label: '类型' }, { prop: 'address', label: '地址', width: 220 }, { prop: 'rating', label: '评分' }, { prop: 'capacity', label: '床位' }, { prop: 'status', label: '状态' }],
  videos: [{ prop: 'title', label: '视频标题' }, { prop: 'lecturer', label: '讲师' }, { prop: 'category', label: '分类' }, { prop: 'duration', label: '时长' }, { prop: 'status', label: '状态' }],
  foods: [{ prop: 'title', label: '食物名称' }, { prop: 'category', label: '分类' }, { prop: 'calories', label: '热量' }, { prop: 'protein', label: '蛋白质' }, { prop: 'fat', label: '脂肪' }, { prop: 'carbs', label: '碳水' }, { prop: 'status', label: '状态' }],
  assessments: [{ prop: 'title', label: '测评名称' }, { prop: 'type', label: '类型' }, { prop: 'status', label: '状态' }],
  assessmentResults: [{ prop: 'assessmentTitle', label: '测评' }, { prop: 'userName', label: '用户' }, { prop: 'score', label: '得分' }, { prop: 'result', label: '结果' }]
})
const createFieldMap = {
  personnel: [
    { prop: 'name', label: '姓名', required: true, placeholder: '服务人员姓名' },
    { prop: 'phone', label: '手机号', placeholder: '不填则自动生成' },
    { prop: 'serviceType', label: '服务类型', type: 'select', options: ['家政护理', '康复理疗', '上门体检'] },
    { prop: 'area', label: '负责区域', placeholder: '如：浦东新区' },
    { prop: 'auditStatus', label: '审核状态', type: 'select', options: ['待审核', '已通过', '已驳回'] }
  ],
  workOrders: [
    { prop: 'productId', label: '商品服务', type: 'product', required: true },
    { prop: 'amount', label: '金额', type: 'number', readonly: true },
    { prop: 'userRef', label: '客户', type: 'user', required: true },
    { prop: 'personnelId', label: '服务人员', type: 'personnel', required: true },
    { prop: 'serviceTime', label: '服务时间', placeholder: 'YYYY-MM-DD HH:mm:ss' },
    { prop: 'status', label: '状态', type: 'select', options: ['待服务', '服务中', '已完成', '已取消'] }
  ],
  products: [
    { prop: 'name', label: '商品服务名称', required: true, placeholder: '如：助餐陪诊服务' },
    { prop: 'code', label: '编码', placeholder: '不填则自动生成' },
    { prop: 'itemType', label: '类型', type: 'select', options: ['服务', '商品'] },
    { prop: 'category', label: '分类', type: 'select', options: ['家政护理', '康复理疗', '上门体检'] },
    { prop: 'description', label: '说明', type: 'textarea', placeholder: '商品或服务说明' },
    { prop: 'duration', label: '时长分钟', type: 'number' },
    { prop: 'price', label: '价格', type: 'number' },
    { prop: 'status', label: '状态', type: 'select', options: ['上架', '下架'] }
  ],
  orders: [
    { prop: 'productId', label: '商品服务', type: 'product', required: true },
    { prop: 'userRef', label: '买家', type: 'user' },
    { prop: 'amount', label: '金额', type: 'number', readonly: true },
    { prop: 'status', label: '状态', type: 'select', options: ['待接单', '待服务', '已完成', '已关闭', '售后中'] }
  ],
  afterSales: [
    { prop: 'orderId', label: '订单ID', type: 'number' },
    { prop: 'userRef', label: '申请人', type: 'user' },
    { prop: 'reason', label: '售后原因', required: true, placeholder: '请输入售后原因' },
    { prop: 'status', label: '状态', type: 'select', options: ['处理中', '已完成', '已关闭'] }
  ],
  reviews: [
    { prop: 'userRef', label: '用户', type: 'user' },
    { prop: 'productId', label: '商品', type: 'product' },
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
    { prop: 'coverUrl', label: '活动封面', type: 'upload', category: 'banner', accept: '.jpg,.jpeg,.png,.webp', preview: 'image' },
    { prop: 'location', label: '地点', placeholder: '活动地点' },
    { prop: 'startTime', label: '开始时间', type: 'datetime', required: true },
    { prop: 'endTime', label: '结束时间', type: 'datetime' },
    { prop: 'quota', label: '名额', type: 'number' },
    { prop: 'status', label: '状态', type: 'select', options: ['草稿', '已发布', '已结束'] },
    { prop: 'content', label: '活动介绍', type: 'textarea', placeholder: '活动具体内容、流程和注意事项' }
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
Object.assign(createFieldMap, {
  devices: [
    { prop: 'userRef', label: '用户', type: 'user' },
    { prop: 'deviceName', label: '设备名称', required: true, placeholder: '如：小米手环8' },
    { prop: 'deviceType', label: '设备类型', placeholder: 'band/watch/scale' },
    { prop: 'deviceCode', label: '设备编号', placeholder: '设备编号或 MAC' },
    { prop: 'status', label: '状态', type: 'select', options: ['绑定', '解绑'] }
  ],
  reports: [
    { prop: 'userRef', label: '用户', type: 'user' },
    { prop: 'title', label: '报告标题', required: true, placeholder: '报告标题' },
    { prop: 'reportType', label: '报告类型', placeholder: '体检/健康评估/月度总结' },
    { prop: 'reportDate', label: '报告日期', placeholder: 'YYYY-MM-DD' },
    { prop: 'doctorName', label: '医生', placeholder: '医生/评估人' },
    { prop: 'fileUrl', label: '报告文件', type: 'upload', category: 'report', accept: '.jpg,.jpeg,.png,.webp,.pdf' },
    { prop: 'summary', label: '摘要', type: 'textarea', placeholder: '报告摘要' }
  ],
  healthSettings: [
    { prop: 'userRef', label: '用户', type: 'user' },
    { prop: 'heartRateUpper', label: '心率上限', type: 'number' },
    { prop: 'heartRateLower', label: '心率下限', type: 'number' },
    { prop: 'stepGoal', label: '步数目标', type: 'number' },
    { prop: 'sleepGoal', label: '睡眠目标', type: 'number' },
    { prop: 'status', label: '用药提醒', type: 'select', options: ['启用', '禁用'] }
  ],
  coupons: [
    { prop: 'userRef', label: '用户', type: 'user' },
    { prop: 'couponNo', label: '券编号', placeholder: '不填则自动生成' },
    { prop: 'name', label: '名称', required: true, placeholder: '优惠券名称' },
    { prop: 'type', label: '类型', type: 'select', options: ['满减', '折扣', '现金'] },
    { prop: 'discount', label: '优惠', type: 'number' },
    { prop: 'minAmount', label: '门槛', type: 'number' },
    { prop: 'status', label: '状态', type: 'select', options: ['未使用', '已使用', '已过期', '可发放'] },
    { prop: 'expireDate', label: '过期日', placeholder: 'YYYY-MM-DD' }
  ],
  userPoints: [
    { prop: 'userRef', label: '用户', type: 'user' },
    { prop: 'points', label: '当前积分', type: 'number' },
    { prop: 'totalEarned', label: '累计获得', type: 'number' },
    { prop: 'totalSpent', label: '累计消耗', type: 'number' },
    { prop: 'level', label: '等级', type: 'select', options: ['普通', '银卡', '金卡'] },
    { prop: 'growthValue', label: '成长值', type: 'number' }
  ],
  memberLevels: [
    { prop: 'name', label: '等级名称', required: true, type: 'select', options: ['普通', '银卡', '金卡'] },
    { prop: 'minGrowth', label: '成长值下限', type: 'number' },
    { prop: 'maxGrowth', label: '成长值上限', type: 'number' },
    { prop: 'benefits', label: '权益', type: 'textarea', placeholder: '权益说明' },
    { prop: 'status', label: '状态', type: 'select', options: ['启用', '禁用'] }
  ],
  pointsRules: [
    { prop: 'actionType', label: '行为类型', required: true, type: 'select', options: ['签到', '完成订单', '发布评价'] },
    { prop: 'description', label: '说明', placeholder: '规则说明' },
    { prop: 'points', label: '积分', type: 'number' },
    { prop: 'growth', label: '成长值', type: 'number' },
    { prop: 'status', label: '状态', type: 'select', options: ['启用', '禁用'] }
  ],
  productCategories: [
    { prop: 'name', label: '分类名称', required: true, placeholder: '分类名称' },
    { prop: 'code', label: '编码', placeholder: '分类编码' },
    { prop: 'description', label: '描述', placeholder: '描述' },
    { prop: 'sortOrder', label: '排序', type: 'number' },
    { prop: 'status', label: '状态', type: 'select', options: ['启用', '禁用'] }
  ],
  serviceItems: [
    { prop: 'productId', label: '商品', type: 'product' },
    { prop: 'name', label: '项目名称', required: true, placeholder: '服务项目名称' },
    { prop: 'description', label: '描述', type: 'textarea', placeholder: '服务项目描述' },
    { prop: 'duration', label: '时长分钟', type: 'number' },
    { prop: 'price', label: '价格', type: 'number' },
    { prop: 'status', label: '状态', type: 'select', options: ['启用', '禁用'] }
  ],
  banners: [
    { prop: 'title', label: '标题', required: true, placeholder: '轮播标题' },
    { prop: 'imageUrl', label: '图片', required: true, type: 'upload', category: 'banner', accept: '.jpg,.jpeg,.png,.webp', preview: 'image' },
    { prop: 'linkUrl', label: '跳转URL', placeholder: '跳转链接' },
    { prop: 'location', label: '位置', placeholder: 'home/activity' },
    { prop: 'sortOrder', label: '排序', type: 'number' },
    { prop: 'status', label: '状态', type: 'select', options: ['启用', '禁用'] }
  ],
  topics: [
    { prop: 'name', label: '话题名称', required: true, placeholder: '话题名称' },
    { prop: 'description', label: '描述', placeholder: '话题描述' },
    { prop: 'postCount', label: '动态数', type: 'number' },
    { prop: 'status', label: '状态', type: 'select', options: ['启用', '禁用'] }
  ],
  activityEnrolls: [
    { prop: 'activityId', label: '活动', type: 'activity' },
    { prop: 'userRef', label: '用户', type: 'user' },
    { prop: 'status', label: '状态', type: 'select', options: ['已报名', '已取消', '已到场'] },
    { prop: 'remark', label: '备注', placeholder: '备注' }
  ],
  recipes: [
    { prop: 'title', label: '菜谱名称', required: true, placeholder: '菜谱名称' },
    { prop: 'category', label: '分类', placeholder: '早餐/午餐/晚餐' },
    { prop: 'ingredients', label: '食材', type: 'textarea', placeholder: '食材清单' },
    { prop: 'steps', label: '步骤', type: 'textarea', placeholder: '制作步骤' },
    { prop: 'calories', label: '热量', type: 'number' },
    { prop: 'suitableFor', label: '适宜人群', placeholder: '如：高血压' }
  ],
  diseases: [
    { prop: 'title', label: '疾病名称', required: true, placeholder: '疾病名称' },
    { prop: 'category', label: '分类', placeholder: '心血管/内分泌' },
    { prop: 'summary', label: '简介', type: 'textarea', placeholder: '简介' },
    { prop: 'symptoms', label: '症状', type: 'textarea', placeholder: '症状' },
    { prop: 'prevention', label: '预防', type: 'textarea', placeholder: '预防措施' }
  ],
  institutions: [
    { prop: 'title', label: '机构名称', required: true, placeholder: '机构名称' },
    { prop: 'type', label: '类型', placeholder: '养老院/护理院' },
    { prop: 'address', label: '地址', placeholder: '地址' },
    { prop: 'phone', label: '电话', placeholder: '联系电话' },
    { prop: 'rating', label: '评分', type: 'number' },
    { prop: 'capacity', label: '容量', type: 'number' }
  ],
  videos: [
    { prop: 'title', label: '视频标题', required: true, placeholder: '视频标题' },
    { prop: 'lecturer', label: '讲师', placeholder: '讲师' },
    { prop: 'category', label: '分类', placeholder: '分类' },
    { prop: 'videoUrl', label: '视频URL', placeholder: '视频 URL' },
    { prop: 'duration', label: '时长秒', type: 'number' }
  ],
  foods: [
    { prop: 'title', label: '食物名称', required: true, placeholder: '食物名称' },
    { prop: 'category', label: '分类', placeholder: '主食/蔬菜/肉类' },
    { prop: 'calories', label: '热量', type: 'number' },
    { prop: 'protein', label: '蛋白质', type: 'number' },
    { prop: 'fat', label: '脂肪', type: 'number' },
    { prop: 'carbs', label: '碳水', type: 'number' }
  ],
  assessments: [
    { prop: 'title', label: '测评名称', required: true, placeholder: '测评名称' },
    { prop: 'type', label: '类型', type: 'select', options: ['睡眠测评', '跌倒风险', '综合测评'] },
    { prop: 'description', label: '说明', type: 'textarea', placeholder: '测评说明' },
    { prop: 'status', label: '状态', type: 'select', options: ['启用', '禁用'] }
  ]
})

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
  error.value = ''
  try {
    const params = searchEnabled.value ? toQueryParams(filters) : undefined
    const data = await getResource(resource.value, params)
    rows.value = data.list || data
    total.value = data.total || rows.value.length
  } catch (exception) {
    rows.value = []
    total.value = 0
    error.value = exception.message || '数据加载失败，请检查后端和数据库连接'
  }
}
function reset() {
  filters.status = ''
  filters.dateRange = []
  filters.keyword = ''
  filters.personnelId = ''
  filters.customerId = ''
  load()
}
function openCreate() {
  readonly.value = false
  editingId.value = null
  Object.keys(form).forEach((key) => delete form[key])
  createFields.value.forEach((field) => {
    if (field.type === 'number') form[field.prop] = 0
    else if (field.type === 'user' || field.type === 'product' || field.type === 'activity' || field.type === 'personnel') form[field.prop] = ''
    else if (field.type === 'select') form[field.prop] = firstAvailableOption(field) || field.options[0]
    else form[field.prop] = ''
  })
  dialogVisible.value = true
}
function openEdit(row) {
  readonly.value = false
  editingId.value = row.id
  fillForm(row)
  dialogVisible.value = true
}
function openDetail(row) {
  readonly.value = true
  editingId.value = row.id
  fillForm(row)
  dialogVisible.value = true
}
function fillForm(row) {
  Object.keys(form).forEach((key) => delete form[key])
  createFields.value.forEach((field) => {
    const value = field.type === 'user' ? userRefFromRow(row) : row[field.prop]
    if (field.type === 'number') form[field.prop] = Number(value || 0)
    else if (field.type === 'user' || field.type === 'product' || field.type === 'activity' || field.type === 'personnel') form[field.prop] = String(value || '')
    else if (field.type === 'select') form[field.prop] = value || field.options[0]
    else form[field.prop] = value || ''
  })
}
function handleProductChange(field, value) {
  if (field.prop !== 'productId' || !['workOrders', 'orders'].includes(resource.value)) return
  const selected = productOptions.value.find((item) => String(item.id) === String(value))
  form.amount = selected ? Number(selected.price || 0) : 0
}
function userRefFromRow(row) {
  const name = row.userName || row.customer || row.buyer || row.applicant || row.user
  const matched = userOptions.value.find((item) => item.realName === name || item.nickname === name)
  return matched ? matched.id : name
}
function isSelectOptionDisabled(field, option) {
  if (resource.value === 'memberLevels' && field.prop === 'name') {
    return isOptionTaken('name', option)
  }
  if (resource.value === 'pointsRules' && field.prop === 'actionType') {
    return isOptionTaken('actionType', option)
  }
  return false
}
function isOptionTaken(prop, option) {
  return rows.value.some((row) => row.id !== editingId.value && row[prop] === option)
}
function firstAvailableOption(field) {
  if (!Array.isArray(field.options)) return ''
  return field.options.find((option) => !isSelectOptionDisabled(field, option)) || ''
}
function hasAvailableOption(prop) {
  const field = createFields.value.find((item) => item.prop === prop)
  return Boolean(field && field.options?.some((option) => !rows.value.some((row) => row[prop] === option)))
}
async function handleFieldUpload(field, options) {
  try {
    const data = await uploadFile(options.file, field.category || 'avatar')
    form[field.prop] = data.url
    options.onSuccess?.(data)
    ElMessage.success('上传成功')
  } catch (error) {
    options.onError?.(error)
    ElMessage.error(error.message || '上传失败')
  }
}
function openFile(url) {
  if (!url) return
  window.open(assetUrl(url), '_blank')
}
function fileNameFromUrl(url) {
  return String(url || '').split('/').filter(Boolean).pop() || '已上传文件'
}
async function loadUsers() {
  try {
    const data = await getUsers()
    userOptions.value = data.list || []
  } catch (error) {
    userOptions.value = []
  }
}
async function loadOptions() {
  const [products, activities, personnel] = await Promise.allSettled([getResource('products'), getResource('activities'), getResource('personnel')])
  if (products.status === 'fulfilled') {
    const options = products.value.list || products.value || []
    productOptions.value = options.filter((item) => item.status !== '下架')
  }
  if (activities.status === 'fulfilled') activityOptions.value = activities.value.list || activities.value || []
  if (personnel.status === 'fulfilled') {
    const options = personnel.value.list || personnel.value || []
    personnelOptions.value = options.filter((item) => item.status === '启用' && item.auditStatus === '已通过')
  }
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
onMounted(async () => {
  await Promise.all([load(), loadUsers(), loadOptions()])
})
</script>
