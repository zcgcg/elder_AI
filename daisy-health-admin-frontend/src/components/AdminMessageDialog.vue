<template>
  <el-dialog v-model="visible" title="给管理员留言" width="min(620px, calc(100vw - 28px))">
    <el-form label-position="top">
      <el-form-item label="留言内容" required>
        <el-input
          v-model="content"
          type="textarea"
          :rows="5"
          maxlength="500"
          show-word-limit
          placeholder="请描述需要管理员协助处理的事项"
        />
      </el-form-item>
    </el-form>
    <section v-if="messages.length" class="message-history">
      <h3>最近留言</h3>
      <article v-for="item in messages.slice(0, 5)" :key="item.id">
        <div>
          <span>{{ item.createdAt }}</span>
          <el-tag size="small" :type="statusTone(item.status)">{{ item.status }}</el-tag>
        </div>
        <p>{{ item.content }}</p>
      </article>
    </section>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" :loading="saving" @click="submit">提交留言</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createElderlyMessage, getElderlyMessages } from '../api/http'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue'])
const content = ref('')
const messages = ref([])
const saving = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

watch(() => props.modelValue, async (opened) => {
  if (!opened) return
  content.value = ''
  try {
    messages.value = await getElderlyMessages()
  } catch (error) {
    messages.value = []
    ElMessage.error(error.message || '留言记录加载失败')
  }
})

async function submit() {
  const message = content.value.trim()
  if (!message) {
    ElMessage.warning('请输入留言内容')
    return
  }
  saving.value = true
  try {
    await createElderlyMessage({ content: message })
    messages.value = await getElderlyMessages()
    content.value = ''
    ElMessage.success('留言已提交，管理员将在后台处理')
  } catch (error) {
    ElMessage.error(error.message || '留言提交失败')
  } finally {
    saving.value = false
  }
}

function statusTone(status) {
  return { '待处理': 'warning', '处理中': 'primary', '已解决': 'success' }[status] || 'info'
}
</script>

<style scoped>
.message-history {
  max-height: 260px;
  overflow-y: auto;
  border-top: 1px solid #edf1f2;
}

.message-history article {
  padding: 12px 0;
  border-bottom: 1px solid #edf1f2;
}

.message-history article > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #788991;
}

.message-history p {
  margin: 8px 0 0;
  white-space: pre-wrap;
}
</style>
