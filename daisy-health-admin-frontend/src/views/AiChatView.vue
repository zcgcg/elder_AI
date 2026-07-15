<template>
  <main class="ai-chat-page">
    <header class="chat-header">
      <div class="chat-heading">
        <el-button circle aria-label="返回用户首页" @click="router.push('/portal/user')">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <div class="ai-title-icon"><el-icon><Service /></el-icon></div>
        <div>
          <h1>AI 智能客服</h1>
          <p>健康记录解读与项目使用帮助</p>
        </div>
      </div>
      <el-button type="primary" plain @click="messageVisible = true">给管理员留言</el-button>
    </header>

    <el-alert
      v-if="loadError"
      :title="loadError"
      type="error"
      show-icon
      :closable="false"
      class="chat-alert"
    >
      <template #default><el-button link type="primary" @click="load">重新加载</el-button></template>
    </el-alert>

    <section ref="conversationRef" class="conversation" aria-live="polite">
      <div v-if="conversationUi.showEmpty" class="empty-chat">
        <div class="ai-title-icon large"><el-icon><Service /></el-icon></div>
        <h2>您好，我是黛西健康智能客服</h2>
        <p>可以问我系统功能、商品服务、活动，以及您本人的健康记录。</p>
        <p class="medical-notice">健康回答仅供科普参考，不能替代医生诊断和用药指导。</p>
      </div>

      <template v-for="(message, index) in messages" :key="message.id">
        <div v-if="showDate(index)" class="date-separator">{{ formatDate(message.createdAt) }}</div>
        <div :class="['message-row', message.role, messagePresentation(message.role).alignment]">
          <div v-if="messagePresentation(message.role).showAiAvatar" class="ai-avatar"><el-icon><Service /></el-icon></div>
          <div class="message-content">
            <div class="message-bubble">{{ message.content }}</div>
            <div class="message-meta">
              <span>{{ formatTime(message.createdAt) }}</span>
              <el-button v-if="message.failed" link type="danger" @click="retryMessage(message)">发送失败，点击重试</el-button>
            </div>
          </div>
          <el-avatar v-if="messagePresentation(message.role).showUserAvatar" :size="44" :src="userAvatar">{{ userInitial }}</el-avatar>
        </div>
      </template>

      <div v-if="conversationUi.showThinking" class="message-row assistant thinking-row">
        <div class="ai-avatar"><el-icon><Service /></el-icon></div>
        <div class="message-bubble thinking"><i></i><i></i><i></i><span>正在思考</span></div>
      </div>
    </section>

    <footer class="chat-composer">
      <el-input
        v-model="question"
        type="textarea"
        :autosize="{ minRows: 2, maxRows: 5 }"
        maxlength="1000"
        show-word-limit
        resize="none"
        placeholder="请输入您的问题"
        @keydown.enter.exact.prevent="send"
      />
      <el-button type="primary" size="large" :loading="sending" :disabled="!question.trim()" @click="send">发送</el-button>
    </footer>

    <admin-message-dialog v-model="messageVisible" />
  </main>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AdminMessageDialog from '../components/AdminMessageDialog.vue'
import { assetUrl, getAiChatMessages, getElderlyProfile, sendAiChatMessage } from '../api/http'
import { useAuthStore } from '../stores/auth'
import {
  conversationPresentation,
  messagePresentation,
  removeFailedMessageForRetry,
  scrollConversationToEnd
} from '../utils/aiChatBehavior.js'

const router = useRouter()
const auth = useAuthStore()
const messages = ref([])
const profile = ref({})
const question = ref('')
const loading = ref(false)
const sending = ref(false)
const loadError = ref('')
const messageVisible = ref(false)
const conversationRef = ref(null)
let localId = 0

const userAvatar = computed(() => assetUrl(profile.value.avatarUrl || auth.user?.avatarUrl || ''))
const userInitial = computed(() => (profile.value.realName || auth.user?.name || '用').slice(0, 1))
const conversationUi = computed(() => conversationPresentation(loading.value, messages.value, sending.value))

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const [profileData, history] = await Promise.all([getElderlyProfile(), getAiChatMessages()])
    profile.value = profileData
    messages.value = history || []
    await scrollToBottom()
  } catch (error) {
    loadError.value = error.message || '聊天记录加载失败'
  } finally {
    loading.value = false
  }
}

async function send() {
  await sendContent(question.value)
}

async function sendContent(rawContent) {
  const content = String(rawContent || '').trim()
  if (!content || sending.value) return
  const pending = {
    id: `local-${++localId}`,
    role: 'user',
    content,
    createdAt: new Date().toISOString(),
    failed: false
  }
  messages.value.push(pending)
  question.value = ''
  sending.value = true
  await scrollToBottom()
  try {
    const exchange = await sendAiChatMessage({ content })
    const index = messages.value.findIndex((item) => item.id === pending.id)
    if (index >= 0) messages.value.splice(index, 1, exchange.userMessage)
    messages.value.push(exchange.assistantMessage)
  } catch (error) {
    pending.failed = true
    ElMessage.error(error.message || 'AI 服务暂时不可用，请稍后重试或给管理员留言')
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

async function retryMessage(message) {
  if (sending.value) return
  const retry = removeFailedMessageForRetry(messages.value, message.id)
  messages.value = retry.messages
  await sendContent(retry.content)
}

function showDate(index) {
  if (index === 0) return true
  return dateKey(messages.value[index - 1].createdAt) !== dateKey(messages.value[index].createdAt)
}

function dateKey(value) {
  const date = parseMessageDate(value)
  return Number.isNaN(date.getTime()) ? '' : `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`
}

function formatDate(value) {
  const date = parseMessageDate(value)
  if (Number.isNaN(date.getTime())) return ''
  const today = new Date()
  if (date.toDateString() === today.toDateString()) return '今天'
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

function formatTime(value) {
  const date = parseMessageDate(value)
  if (Number.isNaN(date.getTime())) return ''
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function parseMessageDate(value) {
  return new Date(String(value || '').replace(' ', 'T'))
}

async function scrollToBottom() {
  await nextTick()
  scrollConversationToEnd(conversationRef.value)
}

onMounted(load)
</script>

<style scoped>
.ai-chat-page {
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr) auto;
  width: min(920px, 100%);
  height: 100vh;
  margin: 0 auto;
  background: #f7faf9;
  box-shadow: 0 0 32px rgb(35 62 57 / 8%);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 24px;
  border-bottom: 1px solid #e2ebe8;
  background: #fff;
}

.chat-heading {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-heading h1,
.chat-heading p {
  margin: 0;
}

.chat-heading h1 { font-size: 22px; }
.chat-heading p { margin-top: 4px; color: #71807d; font-size: 13px; }

.ai-title-icon,
.ai-avatar {
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  color: #fff;
  background: linear-gradient(145deg, #3fba9b, #167f77);
}

.ai-title-icon { width: 44px; height: 44px; border-radius: 14px; font-size: 24px; }
.ai-title-icon.large { width: 70px; height: 70px; margin: 0 auto; border-radius: 22px; font-size: 36px; }
.ai-avatar { width: 44px; height: 44px; border-radius: 50%; font-size: 23px; }

.chat-alert { margin: 12px 20px 0; }

.conversation {
  overflow-y: auto;
  min-height: 0;
  padding: 26px 28px 38px;
  scroll-behavior: smooth;
}

.empty-chat {
  max-width: 560px;
  margin: 9vh auto 0;
  padding: 28px;
  text-align: center;
  color: #53635f;
}

.empty-chat h2 { margin: 18px 0 10px; color: #223530; }
.empty-chat p { line-height: 1.8; }
.medical-notice { color: #8b6d36; }

.date-separator {
  width: max-content;
  margin: 20px auto;
  padding: 5px 13px;
  border-radius: 20px;
  color: #84918e;
  background: #e9eeec;
  font-size: 13px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin: 18px 0;
}

.message-row.assistant { justify-content: flex-start; }
.message-row.user { justify-content: flex-end; }
.message-row.user .message-content { align-items: flex-end; }

.message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: min(70%, 620px);
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 18px;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  line-height: 1.7;
  font-size: 16px;
}

.assistant .message-bubble {
  border-top-left-radius: 5px;
  color: #30413d;
  background: #fff;
  box-shadow: 0 5px 18px rgb(51 77 70 / 8%);
}

.user .message-bubble {
  border-top-right-radius: 5px;
  color: #fff;
  background: linear-gradient(135deg, #42cfa9, #22a985);
}

.message-meta {
  display: flex;
  align-items: center;
  min-height: 24px;
  margin-top: 5px;
  color: #8b9895;
  font-size: 12px;
}

.thinking { display: flex; align-items: center; gap: 5px; color: #72817e; }
.thinking i { width: 7px; height: 7px; border-radius: 50%; background: #39ae92; animation: pulse 1.1s infinite alternate; }
.thinking i:nth-child(2) { animation-delay: .2s; }
.thinking i:nth-child(3) { animation-delay: .4s; }
.thinking span { margin-left: 5px; }

.chat-composer {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: 14px;
  padding: 18px 24px max(18px, env(safe-area-inset-bottom));
  border-top: 1px solid #e2ebe8;
  background: #fff;
}

.chat-composer :deep(textarea) { font-size: 17px; line-height: 1.6; }
.chat-composer .el-button { min-width: 92px; height: 48px; }

@keyframes pulse { from { opacity: .25; transform: translateY(2px); } to { opacity: 1; transform: translateY(-2px); } }

@media (max-width: 640px) {
  .chat-header { padding: 12px 14px; }
  .chat-heading { gap: 8px; }
  .chat-heading p { display: none; }
  .chat-heading h1 { font-size: 18px; }
  .ai-title-icon { width: 38px; height: 38px; }
  .conversation { padding: 14px 12px 24px; }
  .message-content { max-width: calc(100% - 58px); }
  .message-bubble { padding: 12px 15px; font-size: 16px; }
  .chat-composer { padding: 12px max(12px, env(safe-area-inset-right)) max(12px, env(safe-area-inset-bottom)) max(12px, env(safe-area-inset-left)); }
  .chat-composer .el-button { min-width: 70px; }
}
</style>
