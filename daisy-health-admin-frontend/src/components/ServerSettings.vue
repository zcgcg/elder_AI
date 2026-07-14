<template>
  <section
    v-if="isNativeApp"
    class="server-settings-summary"
    :class="{ 'is-floating': floating }"
    aria-label="服务器设置"
  >
    <div>
      <span class="server-status-dot" :class="{ ready: configured }" aria-hidden="true"></span>
      <span>{{ configured ? displayOrigin : '尚未配置服务器' }}</span>
    </div>
    <el-button link type="primary" @click="open">服务器设置</el-button>
  </section>

  <el-dialog
    v-if="isNativeApp"
    v-model="visible"
    title="服务器设置"
    width="min(92vw, 480px)"
    :show-close="configured"
    :close-on-click-modal="configured"
    :close-on-press-escape="configured"
  >
    <p class="server-settings-help">
      局域网请输入电脑地址，例如 <code>http://192.168.1.20:8080</code>；未来公网服务请输入 HTTPS 地址。
    </p>
    <el-input v-model="draft" size="large" placeholder="http://电脑WLAN地址:8080" @input="errorMessage = ''" />
    <p v-if="errorMessage" class="server-settings-error" role="alert">{{ errorMessage }}</p>
    <template #footer>
      <el-button v-if="configured" @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="testing" @click="testAndSave">测试并保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { pingServer } from '../api/http.js'
import { saveServerForSession } from '../config/server.js'
import { isNativeApp, serverConfig } from '../config/runtime.js'
import { useAuthStore } from '../stores/auth.js'

const auth = useAuthStore()
const router = useRouter()
defineProps({ floating: Boolean })
const configured = ref(serverConfig.isConfigured())
const visible = ref(isNativeApp && !configured.value)
const draft = ref(serverConfig.currentOrigin())
const testing = ref(false)
const errorMessage = ref('')
const displayOrigin = computed(() => configured.value ? serverConfig.currentOrigin() : '尚未配置服务器')

function open() {
  draft.value = serverConfig.currentOrigin()
  errorMessage.value = ''
  visible.value = true
}

async function testAndSave() {
  testing.value = true
  errorMessage.value = ''
  try {
    const status = await pingServer(draft.value)
    const result = await saveServerForSession(serverConfig, auth, status.origin)
    configured.value = true
    visible.value = false
    ElMessage.success('服务器连接成功，地址已保存')
    if (result.changed) await router.replace('/login')
  } catch (error) {
    errorMessage.value = error?.message || '无法连接服务器，请检查地址和网络'
  } finally {
    testing.value = false
  }
}

defineExpose({ open })
</script>

<style scoped>
.server-settings-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 22px 0 -32px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #f2fbf8;
  color: #64736f;
  font-size: 13px;
}

.server-settings-summary.is-floating {
  position: fixed;
  top: calc(env(safe-area-inset-top, 0px) + 12px);
  right: 12px;
  z-index: 1900;
  max-width: min(72vw, 360px);
  margin: 0;
  box-shadow: 0 4px 18px rgba(30, 76, 65, 0.14);
}

.server-settings-summary > div {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.server-settings-summary span:last-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.server-status-dot {
  width: 8px;
  height: 8px;
  flex: 0 0 auto;
  border-radius: 50%;
  background: #e6a23c;
}

.server-status-dot.ready {
  background: #20b486;
}

.server-settings-help {
  margin: 0 0 16px;
  color: #66736f;
  line-height: 1.7;
}

.server-settings-help code {
  word-break: break-all;
}

.server-settings-error {
  margin: 10px 0 0;
  color: #e45656;
  font-size: 13px;
}
</style>
