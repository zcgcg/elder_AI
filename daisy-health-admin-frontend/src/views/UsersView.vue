<template>
  <section>
    <div class="page-heading">
      <div><h1>全部用户</h1><p>后台分配用户档案、健康数据与资产信息管理</p></div>
      <el-segmented v-model="viewMode" :options="['卡片', '列表']" />
    </div>
    <div class="filters">
      <el-select v-model="filters.tag" placeholder="用户标签" clearable>
        <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.name" />
      </el-select>
      <el-date-picker v-model="filters.dateRange" type="daterange" start-placeholder="创建开始" end-placeholder="创建结束" />
      <el-input v-model="filters.keyword" placeholder="搜索姓名/手机号" clearable />
      <el-button type="primary" @click="load">搜索</el-button>
      <el-button @click="reset">重置</el-button>
    </div>
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openCreate">新增用户</el-button>
      <el-button :icon="PriceTag" @click="openTagManager">标签管理</el-button>
    </div>

    <div v-if="viewMode === '卡片'" class="user-card-grid">
      <article v-for="user in filteredRows" :key="user.id" class="user-profile-card">
        <el-button class="user-delete-btn" :icon="Delete" text @click="removeUser(user)" />
        <div class="user-card-head">
          <el-avatar :size="54" :src="user.avatarUrl">{{ user.realName?.slice(0, 1) }}</el-avatar>
          <div>
            <strong>{{ user.nickname || user.realName }}</strong>
            <span>ID:{{ user.id }}</span>
          </div>
        </div>
        <div class="user-tag-strip">
          <span
            v-for="tag in user.tags"
            :key="tag"
            class="soft-tag"
            :class="`soft-tag-${tagColor(tag)}`"
            @click="openAddTags(user)"
          >
            {{ tag }}
          </span>
        </div>
        <dl class="user-card-meta">
          <dt>真实姓名：</dt><dd>{{ user.realName }}</dd>
          <dt>手机号码：</dt><dd>{{ user.phone }}</dd>
          <dt>注册时间：</dt><dd>{{ user.createdAt }}</dd>
        </dl>
        <div class="user-card-actions">
          <el-button type="primary" @click="$router.push(`/users/${user.id}`)">用户详情</el-button>
          <el-button @click="openAddTags(user)">添加标签</el-button>
        </div>
      </article>
    </div>

    <el-table v-else :data="filteredRows" stripe>
      <el-table-column prop="nickname" label="用户名" min-width="130" />
      <el-table-column prop="realName" label="真实姓名" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column label="标签" min-width="220">
        <template #default="{ row }">
          <span v-for="tag in row.tags" :key="tag" class="soft-tag mini" :class="`soft-tag-${tagColor(tag)}`">{{ tag }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="250" align="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/users/${row.id}`)">详情</el-button>
          <el-button link @click="openAddTags(row)">标签</el-button>
          <el-button link @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeUser(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="620px">
      <el-form :model="newUser" label-width="96px">
        <el-form-item label="昵称"><el-input v-model="newUser.nickname" placeholder="如：兰姨" /></el-form-item>
        <el-form-item label="真实姓名" required><el-input v-model="newUser.realName" placeholder="请输入真实姓名" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="newUser.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="头像">
          <div class="avatar-picker">
            <el-avatar :size="64" :src="newUser.avatarUrl">{{ newUser.realName?.slice(0, 1) || '用' }}</el-avatar>
            <div class="avatar-choice-grid">
              <button
                v-for="avatar in defaultAvatars"
                :key="avatar"
                type="button"
                class="avatar-choice"
                :class="{ active: newUser.avatarUrl === avatar }"
                @click="newUser.avatarUrl = avatar"
              >
                <img :src="avatar" alt="默认头像" />
              </button>
            </div>
            <el-upload :show-file-list="false" :http-request="handleAvatarUpload" accept=".jpg,.jpeg,.png,.webp">
              <el-button>上传头像</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="newUser.gender"><el-radio label="女" /><el-radio label="男" /><el-radio label="未知" /></el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期"><el-date-picker v-model="newUser.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" /></el-form-item>
        <el-form-item label="家庭住址"><el-input v-model="newUser.address" placeholder="请输入地址" /></el-form-item>
        <el-form-item label="慢性病"><el-input v-model="newUser.chronicDisease" placeholder="如：高血压、糖尿病" /></el-form-item>
        <el-form-item label="标签">
          <el-select v-model="newUser.tagNames" multiple clearable placeholder="请选择标签">
            <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.name" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tagDialogVisible" title="添加标签" width="660px" class="tag-picker-dialog">
      <div class="tag-picker">
        <span>选择标签</span>
        <el-checkbox-group v-model="selectedTagIds" class="tag-check-grid">
          <el-checkbox v-for="tag in tags" :key="tag.id" :label="tag.id">{{ tag.name }}</el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="tagSaving" @click="saveUserTags">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tagManagerVisible" title="标签管理" width="760px">
      <div class="tag-manager-toolbar">
        <el-input v-model="tagForm.name" placeholder="标签名称" />
        <el-select v-model="tagForm.color" placeholder="颜色">
          <el-option label="绿色" value="green" />
          <el-option label="红色" value="red" />
          <el-option label="紫色" value="purple" />
          <el-option label="蓝色" value="blue" />
          <el-option label="橙色" value="orange" />
          <el-option label="灰色" value="gray" />
        </el-select>
        <el-button type="primary" @click="saveTag">{{ tagEditingId ? '保存标签' : '新增标签' }}</el-button>
        <el-button v-if="tagEditingId" @click="resetTagForm">取消编辑</el-button>
      </div>
      <el-table :data="tags" stripe>
        <el-table-column prop="name" label="标签名称" />
        <el-table-column label="颜色">
          <template #default="{ row }"><span class="soft-tag mini" :class="`soft-tag-${row.color || 'green'}`">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="userCount" label="用户数" width="100" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="150" align="right">
          <template #default="{ row }">
            <el-button link @click="editTag(row)">编辑</el-button>
            <el-button link type="danger" @click="removeTag(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Plus, PriceTag } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createTag, createUser, deleteTag, deleteUser, getTags, getUsers, updateTag, updateUser, updateUserTags, uploadFile } from '../api/http'

const viewMode = ref('卡片')
const filters = reactive({ tag: '', dateRange: [], keyword: '' })
const rows = ref([])
const tags = ref([])
const dialogVisible = ref(false)
const tagDialogVisible = ref(false)
const tagManagerVisible = ref(false)
const saving = ref(false)
const tagSaving = ref(false)
const editingId = ref(null)
const selectedUser = ref(null)
const selectedTagIds = ref([])
const tagEditingId = ref(null)
const newUser = reactive({
  nickname: '',
  realName: '',
  phone: '',
  avatarUrl: '',
  gender: '女',
  birthday: '',
  address: '',
  chronicDisease: '',
  tagNames: []
})
const defaultAvatars = ['/default-avatars/avatar-01.svg', '/default-avatars/avatar-02.svg', '/default-avatars/avatar-03.svg', '/default-avatars/avatar-04.svg', '/default-avatars/avatar-05.svg', '/default-avatars/avatar-06.svg']
const tagForm = reactive({ name: '', color: 'green' })
const fallbackUsers = [
  { id: 10001, nickname: '笑看人生', realName: '王强', phone: '19233664486', tags: ['高血压', '糖尿病', '多次购买'], tagIds: [1, 5], createdAt: '2024-10-09 10:09:09' },
  { id: 10002, nickname: '兰姨', realName: '王秀兰', phone: '13800010001', tags: ['高血压', '重点关怀'], tagIds: [1, 2], createdAt: '2026-06-18 09:00:00' }
]

const filteredRows = computed(() => {
  if (!filters.tag) return rows.value
  return rows.value.filter((row) => Array.isArray(row.tags) && row.tags.includes(filters.tag))
})

async function loadTags() {
  try {
    const data = await getTags()
    tags.value = data.list || data
  } catch (error) {
    tags.value = [
      { id: 1, name: '潜在客户', color: 'gray' },
      { id: 2, name: '重点客户', color: 'green' },
      { id: 3, name: '普通客户', color: 'blue' },
      { id: 4, name: '多次消费客户', color: 'purple' },
      { id: 5, name: '高血压', color: 'green' },
      { id: 6, name: '高血糖', color: 'orange' },
      { id: 7, name: '高血脂', color: 'purple' },
      { id: 8, name: '慢性病', color: 'red' }
    ]
  }
}
async function load() {
  try {
    const data = await getUsers(filters)
    rows.value = normalizeRows(data.list || data)
  } catch (error) {
    rows.value = normalizeRows(fallbackUsers)
  }
}
function normalizeRows(list) {
  return list.map((row) => ({
    ...row,
    tags: Array.isArray(row.tags) ? row.tags : String(row.tags || '').split(',').filter(Boolean),
    tagIds: Array.isArray(row.tagIds) ? row.tagIds.map(Number) : String(row.tagIds || '').split(',').filter(Boolean).map(Number)
  }))
}
function tagColor(name) {
  const item = tags.value.find((tag) => tag.name === name)
  if (item?.color) return item.color
  if (name.includes('糖尿病') || name.includes('慢性')) return 'red'
  if (name.includes('多次')) return 'purple'
  if (name.includes('血脂')) return 'purple'
  if (name.includes('血糖')) return 'orange'
  return 'green'
}
function reset() {
  filters.tag = ''
  filters.dateRange = []
  filters.keyword = ''
  load()
}
function openCreate() {
  editingId.value = null
  Object.assign(newUser, { nickname: '', realName: '', phone: '', avatarUrl: '', gender: '女', birthday: '', address: '', chronicDisease: '', tagNames: [] })
  dialogVisible.value = true
}
function openEdit(row) {
  editingId.value = row.id
  Object.assign(newUser, {
    nickname: row.nickname || '',
    realName: row.realName || '',
    phone: row.phone || '',
    avatarUrl: row.avatarUrl || '',
    gender: row.gender || '未知',
    birthday: row.birthday || '',
    address: row.address || '',
    chronicDisease: row.chronicDisease || '',
    tagNames: Array.isArray(row.tags) ? [...row.tags] : []
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
    const payload = { ...newUser, tags: newUser.tagNames.join(',') }
    if (editingId.value) await updateUser(editingId.value, payload)
    else await createUser(payload)
    ElMessage.success(editingId.value ? '用户已更新' : '用户已新增')
    dialogVisible.value = false
    await loadTags()
    await load()
  } catch (error) {
    ElMessage.error('保存失败，请确认后端和数据库已启动')
  } finally {
    saving.value = false
  }
}
async function handleAvatarUpload(options) {
  try {
    const data = await uploadFile(options.file, 'avatar')
    newUser.avatarUrl = data.url
    options.onSuccess?.(data)
    ElMessage.success('头像上传成功')
  } catch (error) {
    options.onError?.(error)
    ElMessage.error(error?.response?.data?.message || '头像上传失败')
  }
}
async function removeUser(row) {
  try {
    await ElMessageBox.confirm(`确认删除用户「${row.realName || row.nickname}」？`, '删除确认', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('用户已删除')
    await loadTags()
    await load()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}
function openAddTags(user) {
  selectedUser.value = user
  selectedTagIds.value = Array.isArray(user.tagIds) ? [...user.tagIds] : tags.value.filter((tag) => user.tags?.includes(tag.name)).map((tag) => tag.id)
  tagDialogVisible.value = true
}
async function saveUserTags() {
  if (!selectedUser.value) return
  tagSaving.value = true
  try {
    await updateUserTags(selectedUser.value.id, { tagIds: selectedTagIds.value })
    ElMessage.success('标签已保存')
    tagDialogVisible.value = false
    await loadTags()
    await load()
  } catch (error) {
    ElMessage.error('标签保存失败')
  } finally {
    tagSaving.value = false
  }
}
function openTagManager() {
  resetTagForm()
  tagManagerVisible.value = true
}
function editTag(row) {
  tagEditingId.value = row.id
  Object.assign(tagForm, { name: row.name, color: row.color || 'green' })
}
function resetTagForm() {
  tagEditingId.value = null
  Object.assign(tagForm, { name: '', color: 'green' })
}
async function saveTag() {
  if (!tagForm.name.trim()) {
    ElMessage.warning('请填写标签名称')
    return
  }
  try {
    if (tagEditingId.value) await updateTag(tagEditingId.value, tagForm)
    else await createTag(tagForm)
    ElMessage.success(tagEditingId.value ? '标签已更新' : '标签已新增')
    resetTagForm()
    await loadTags()
  } catch (error) {
    ElMessage.error('标签保存失败')
  }
}
async function removeTag(row) {
  try {
    await ElMessageBox.confirm(`确认删除标签「${row.name}」？删除后会从用户身上移除。`, '删除确认', { type: 'warning' })
    await deleteTag(row.id)
    ElMessage.success('标签已删除')
    await loadTags()
    await load()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('标签删除失败')
  }
}
onMounted(async () => {
  await loadTags()
  await load()
})
</script>
