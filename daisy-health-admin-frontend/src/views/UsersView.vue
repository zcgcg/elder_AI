<template>
  <section>
    <div class="page-heading">
      <div><h1>全部用户</h1><p>后台分配用户档案、健康数据与资产信息管理</p></div>
      <el-segmented v-model="viewMode" :options="['卡片', '列表']" />
    </div>
    <div class="filters">
      <el-select v-model="filters.tag" placeholder="用户标签" clearable><el-option label="高血压" value="高血压" /><el-option label="独居老人" value="独居老人" /></el-select>
      <el-date-picker v-model="filters.dateRange" type="daterange" start-placeholder="创建开始" end-placeholder="创建结束" />
      <el-input v-model="filters.keyword" placeholder="搜索姓名/手机号" clearable />
      <el-button type="primary" @click="load">搜索</el-button>
      <el-button @click="reset">重置</el-button>
    </div>
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openCreate">新增用户</el-button>
      <el-button>批量操作</el-button>
    </div>
    <div v-if="viewMode === '卡片'" class="user-grid">
      <article v-for="user in rows" :key="user.id" class="user-card" @click="$router.push(`/users/${user.id}`)">
        <el-avatar :size="56">{{ user.realName?.slice(0, 1) }}</el-avatar>
        <div>
          <strong>{{ user.nickname }}</strong>
          <span>ID {{ user.id }} · {{ user.realName }}</span>
          <span>{{ user.phone }} · {{ user.createdAt }}</span>
          <div><el-tag v-for="tag in user.tags" :key="tag" size="small">{{ tag }}</el-tag></div>
          <div class="card-actions">
            <el-button link type="primary" @click.stop="openEdit(user)">编辑</el-button>
            <el-button link type="danger" @click.stop="removeUser(user)">删除</el-button>
          </div>
        </div>
      </article>
    </div>
    <el-table v-else :data="rows" stripe>
      <el-table-column prop="nickname" label="用户名" min-width="130" />
      <el-table-column prop="realName" label="真实姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column label="标签" min-width="180"><template #default="{ row }"><el-tag v-for="tag in row.tags" :key="tag" size="small">{{ tag }}</el-tag></template></el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="160" />
      <el-table-column label="操作" width="210" align="right"><template #default="{ row }"><el-button link type="primary" @click="$router.push(`/users/${row.id}`)">详情</el-button><el-button link @click="openEdit(row)">编辑</el-button><el-button link type="danger" @click="removeUser(row)">删除</el-button></template></el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="620px">
      <el-form :model="newUser" label-width="96px">
        <el-form-item label="昵称"><el-input v-model="newUser.nickname" placeholder="如：兰姨" /></el-form-item>
        <el-form-item label="真实姓名" required><el-input v-model="newUser.realName" placeholder="请输入真实姓名" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="newUser.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="newUser.gender"><el-radio label="女" /><el-radio label="男" /><el-radio label="未知" /></el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期"><el-date-picker v-model="newUser.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" /></el-form-item>
        <el-form-item label="家庭住址"><el-input v-model="newUser.address" placeholder="请输入地址" /></el-form-item>
        <el-form-item label="慢性病"><el-input v-model="newUser.chronicDisease" placeholder="如：高血压、糖尿病" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="newUser.tags" placeholder="多个标签用逗号分隔" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUser, deleteUser, getUsers, updateUser } from '../api/http'

const viewMode = ref('卡片')
const filters = reactive({ tag: '', dateRange: [], keyword: '' })
const rows = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const newUser = reactive({
  nickname: '',
  realName: '',
  phone: '',
  gender: '女',
  birthday: '',
  address: '',
  chronicDisease: '',
  tags: ''
})
const fallbackUsers = [
  { id: 10001, nickname: '兰姨', realName: '王秀兰', phone: '138****0001', tags: ['高血压', '重点关怀'], createdAt: '2026-06-18' },
  { id: 10002, nickname: '建国叔', realName: '陈建国', phone: '138****0002', tags: ['康复护理'], createdAt: '2026-06-21' },
  { id: 10003, nickname: '桂英', realName: '赵桂英', phone: '138****0003', tags: ['独居老人'], createdAt: '2026-06-28' }
]

async function load() {
  try {
    const data = await getUsers(filters)
    rows.value = data.list || data
  } catch (error) {
    rows.value = fallbackUsers
  }
}
function reset() {
  filters.tag = ''
  filters.dateRange = []
  filters.keyword = ''
  load()
}
function openCreate() {
  editingId.value = null
  Object.assign(newUser, { nickname: '', realName: '', phone: '', gender: '女', birthday: '', address: '', chronicDisease: '', tags: '' })
  dialogVisible.value = true
}
function openEdit(row) {
  editingId.value = row.id
  Object.assign(newUser, {
    nickname: row.nickname || '',
    realName: row.realName || '',
    phone: String(row.phone || '').includes('****') ? '' : row.phone || '',
    gender: row.gender || '未知',
    birthday: row.birthday || '',
    address: row.address || '',
    chronicDisease: row.chronicDisease || '',
    tags: Array.isArray(row.tags) ? row.tags.join(',') : row.tags || ''
  })
  dialogVisible.value = true
}
async function submitCreate() {
  if (!newUser.realName.trim()) {
    ElMessage.warning('请填写真实姓名')
    return
  }
  saving.value = true
  try {
    if (editingId.value) await updateUser(editingId.value, newUser)
    else await createUser(newUser)
    ElMessage.success(editingId.value ? '用户已更新' : '用户已新增')
    dialogVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error('新增失败，请确认后端和数据库已启动')
  } finally {
    saving.value = false
  }
}
async function removeUser(row) {
  try {
    await ElMessageBox.confirm(`确认删除用户「${row.realName || row.nickname}」？`, '删除确认', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('用户已删除')
    await load()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}
onMounted(load)
</script>
