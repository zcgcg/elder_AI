import assert from 'node:assert/strict'
import test from 'node:test'

import { portalRouteDecision } from './portalRouteDecision.js'
import {
  conversationPresentation,
  messagePresentation,
  removeFailedMessageForRetry,
  scrollConversationToEnd
} from './aiChatBehavior.js'

test('portal routing admits the elderly chat child and redirects every other role', () => {
  assert.equal(portalRouteDecision('elderly', '/portal/user/ai-chat'), true)
  assert.equal(portalRouteDecision('elderly', '/dashboard'), '/portal/user')
  assert.equal(portalRouteDecision('service', '/portal/user/ai-chat'), '/portal/service')
  assert.equal(portalRouteDecision('staff', '/portal/user/ai-chat'), '/dashboard')
  assert.equal(portalRouteDecision('staff', '/dashboard'), null)
})

test('retry removes only the failed local message and preserves its content', () => {
  const result = removeFailedMessageForRetry([
    { id: 1, content: 'older', failed: false },
    { id: 'local-2', content: 'retry me', failed: true },
    { id: 3, content: 'newer', failed: false }
  ], 'local-2')

  assert.equal(result.content, 'retry me')
  assert.deepEqual(result.messages.map((item) => item.id), [1, 3])
})

test('auto-scroll moves the conversation viewport to its latest message', () => {
  const element = { scrollTop: 0, scrollHeight: 640 }
  scrollConversationToEnd(element)
  assert.equal(element.scrollTop, 640)
  assert.doesNotThrow(() => scrollConversationToEnd(null))
})

test('assistant and user messages expose opposite alignment and avatar behavior', () => {
  assert.deepEqual(messagePresentation('assistant'), {
    alignment: 'left',
    showAiAvatar: true,
    showUserAvatar: false
  })
  assert.deepEqual(messagePresentation('user'), {
    alignment: 'right',
    showAiAvatar: false,
    showUserAvatar: true
  })
})

test('empty and thinking states follow loading, messages and sending state', () => {
  assert.deepEqual(conversationPresentation(false, [], false), { showEmpty: true, showThinking: false })
  assert.deepEqual(conversationPresentation(true, [], false), { showEmpty: false, showThinking: false })
  assert.deepEqual(conversationPresentation(false, [{ id: 1 }], true), { showEmpty: false, showThinking: true })
})
