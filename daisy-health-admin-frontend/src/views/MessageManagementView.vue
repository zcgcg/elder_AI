<template>
  <section>
    <div class="page-heading">
      <div><h1>留言处理</h1><p>按用户查看老人端留言并跟踪处理状态</p></div>
      <el-button :loading="loading" @click="load">刷新</el-button>
    </div>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false">
      <template #default><el-button link type="primary" @click="load">重新加载</el-button></template>
    </el-alert>

    <div class="message-summary">
      <article><span>留言用户</span><strong>{{ groups.length }}</strong></article>
      <article><span>待处理</span><strong>{{ statusCounts['待处理'] }}</strong></article>
      <article><span>处理中</span><strong>{{ statusCounts['处理中'] }}</strong></article>
      <article><span>已解决</span><strong>{{ statusCounts['已解决'] }}</strong></article>
    </div>

    <el-table :data="groups" stripe row-key="userId">
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="user-message-list">
            <article v-for="message in row.messages" :key="message.id" class="user-message-item">
              <div class="message-time">{{ message.createdAt }}</div>
              <p>{{ message.content }}</p>
              <el-select
                v-model="message.status"
                :loading="savingIds.has(message.id)"
                class="message-status"
                @change="updateStatus(message)"
              >
                <el-option v-for="status in statusOptions" :key="status" :label="status" :value="status" />
              </el-select>
            </article>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="用户" min-width="140" />
      <el-table-column prop="phone" label="手机号" min-width="150" />
      <el-table-column prop="messageCount" label="留言数" min-width="100" />
      <el-table-column prop="pendingCount" label="待处理" min-width="100">
        <template #default="{ row }"><el-tag :type="row.pendingCount ? 'warning' : 'success'">{{ row.pendingCount }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="lastMessageTime" label="最近留言时间" min-width="180" />
    </el-table>
    <el-empty v-if="!loading && !groups.length && !error" description="暂无用户留言" />
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMessages, updateMessageStatus } from '../api/http'

const groups = ref([])
const error = ref('')
const loading = ref(false)
const savingIds = ref(new Set())
const statusOptions = ['待处理', '处理中', '已解决']
const statusCounts = computed(() => {
  const counts = { '待处理': 0, '处理中': 0, '已解决': 0 }
  groups.value.forEach((group) => group.messages.forEach((message) => {
    if (Object.prototype.hasOwnProperty.call(counts, message.status)) counts[message.status] += 1
  }))
  return counts
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await getMessages()
    groups.value = data.list || data || []
  } catch (exception) {
    groups.value = []
    error.value = exception.message || '留言数据加载失败'
  } finally {
    loading.value = false
  }
}

async function updateStatus(message) {
  savingIds.value = new Set([...savingIds.value, message.id])
  try {
    await updateMessageStatus(message.id, message.status)
    ElMessage.success('留言状态已更新')
    await load()
  } catch (exception) {
    ElMessage.error(exception.message || '留言状态更新失败')
    await load()
  } finally {
    const next = new Set(savingIds.value)
    next.delete(message.id)
    savingIds.value = next
  }
}

onMounted(load)
</script>

<style scoped>
.message-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.message-summary article {
  padding: 18px;
  border: 1px solid #e2eaee;
  border-radius: 8px;
  background: #fff;
}

.message-summary span,
.message-summary strong {
  display: block;
}

.message-summary span {
  color: #6f7e87;
}

.message-summary strong {
  margin-top: 8px;
  font-size: 28px;
}

.user-message-list {
  padding: 6px 24px 16px 54px;
}

.user-message-item {
  display: grid;
  grid-template-columns: 160px minmax(0, 1fr) 130px;
  align-items: start;
  gap: 18px;
  padding: 16px 0;
  border-bottom: 1px solid #edf1f2;
}

.user-message-item:last-child {
  border-bottom: 0;
}

.user-message-item p {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
}

.message-time {
  color: #788991;
}

.message-status {
  width: 130px;
}

@media (max-width: 760px) {
  .message-summary { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .user-message-list { padding-left: 12px; }
  .user-message-item { grid-template-columns: 1fr; gap: 8px; }
}
</style>
