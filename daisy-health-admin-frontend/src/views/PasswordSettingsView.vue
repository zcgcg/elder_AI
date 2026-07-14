<template>
  <section>
    <div class="page-heading">
      <div><h1>密码设置</h1><p>修改当前管理员密码，或将用户和服务人员密码重置为固定初始密码</p></div>
      <el-button type="primary" @click="changeVisible = true">修改我的密码</el-button>
    </div>

    <el-alert
      title="重置操作不能自定义目标密码；重置后密码统一为 753951。"
      type="warning"
      :closable="false"
      show-icon
    />
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />

    <el-tabs v-model="activeTab" class="password-settings-tabs">
      <el-tab-pane label="用户密码重置" name="users">
        <el-table :data="users" stripe>
          <el-table-column prop="realName" label="用户" min-width="140" />
          <el-table-column prop="nickname" label="昵称" min-width="130" />
          <el-table-column prop="phone" label="手机号" min-width="150" />
          <el-table-column label="操作" width="150" align="right">
            <template #default="{ row }">
              <el-button link type="danger" :loading="resettingKey === `user:${row.id}`" @click="resetUser(row)">重置密码</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="服务人员密码重置" name="personnel">
        <el-table :data="personnel" stripe>
          <el-table-column prop="name" label="服务人员" min-width="140" />
          <el-table-column prop="phone" label="手机号" min-width="150" />
          <el-table-column prop="serviceType" label="服务类型" min-width="140" />
          <el-table-column label="操作" width="150" align="right">
            <template #default="{ row }">
              <el-button link type="danger" :loading="resettingKey === `personnel:${row.id}`" @click="resetServicePersonnel(row)">重置密码</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <password-change-dialog v-model="changeVisible" />
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PasswordChangeDialog from '../components/PasswordChangeDialog.vue'
import { getResource, getUsers, resetPersonnelPassword, resetUserPassword } from '../api/http'

const activeTab = ref('users')
const users = ref([])
const personnel = ref([])
const error = ref('')
const resettingKey = ref('')
const changeVisible = ref(false)

async function load() {
  error.value = ''
  const [userResult, personnelResult] = await Promise.allSettled([getUsers(), getResource('personnel')])
  if (userResult.status === 'fulfilled') users.value = userResult.value.list || userResult.value
  else error.value = userResult.reason?.message || '用户列表加载失败'
  if (personnelResult.status === 'fulfilled') personnel.value = personnelResult.value.list || personnelResult.value
  else error.value = [error.value, personnelResult.reason?.message || '服务人员列表加载失败'].filter(Boolean).join('；')
}

async function resetUser(row) {
  await confirmAndReset({
    key: `user:${row.id}`,
    name: row.realName || row.nickname,
    action: () => resetUserPassword(row.id)
  })
}

async function resetServicePersonnel(row) {
  await confirmAndReset({
    key: `personnel:${row.id}`,
    name: row.name,
    action: () => resetPersonnelPassword(row.id)
  })
}

async function confirmAndReset({ key, name, action }) {
  try {
    await ElMessageBox.confirm(`确认将「${name}」的密码重置为 753951？`, '重置密码', { type: 'warning' })
    resettingKey.value = key
    await action()
    ElMessage.success('密码已重置为 753951')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message || '密码重置失败')
  } finally {
    resettingKey.value = ''
  }
}

onMounted(load)
</script>

<style scoped>
.password-settings-tabs {
  margin-top: 20px;
  padding: 18px;
  border-radius: 10px;
  background: #fff;
}
</style>
