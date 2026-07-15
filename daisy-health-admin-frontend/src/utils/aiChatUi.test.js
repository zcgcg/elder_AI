import assert from 'node:assert/strict'
import fs from 'node:fs'
import test from 'node:test'

const userPortal = fs.readFileSync(new URL('../views/UserPortalView.vue', import.meta.url), 'utf8')
const aiChat = fs.readFileSync(new URL('../views/AiChatView.vue', import.meta.url), 'utf8')
const router = fs.readFileSync(new URL('../router/index.js', import.meta.url), 'utf8')
const api = fs.readFileSync(new URL('../api/http.js', import.meta.url), 'utf8')

test('elderly home exposes an AI assistant card and router permits its child page', () => {
  assert.match(userPortal, /class="summary-card ai-assistant-card"/)
  assert.match(userPortal, /router\.push\('\/portal\/user\/ai-chat'\)/)
  assert.match(router, /path: '\/portal\/user\/ai-chat'/)
  assert.match(router, /portalRouteDecision/)
})

test('chat keeps AI on the left, user on the right, and reuses administrator messages', () => {
  assert.match(aiChat, /messagePresentation\(message\.role\)\.alignment/)
  assert.match(aiChat, /\.message-row\.assistant[\s\S]*justify-content: flex-start/)
  assert.match(aiChat, /\.message-row\.user[\s\S]*justify-content: flex-end/)
  assert.match(aiChat, /<admin-message-dialog v-model="messageVisible"/)
  assert.match(aiChat, /maxlength="1000"/)
  assert.match(aiChat, /getAiChatMessages/)
  assert.match(aiChat, /sendAiChatMessage/)
  assert.match(aiChat, /retryMessage/)
})

test('AI requests outlive the provider read timeout instead of failing at the global eight seconds', () => {
  assert.match(api, /sendAiChatMessage[\s\S]*timeout:\s*35000/)
})
