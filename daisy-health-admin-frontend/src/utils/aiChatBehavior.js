export function removeFailedMessageForRetry(messages, id) {
  const failed = messages.find((item) => item.id === id && item.failed)
  return {
    content: failed?.content || '',
    messages: failed ? messages.filter((item) => item.id !== id) : messages
  }
}

export function scrollConversationToEnd(element) {
  if (element) element.scrollTop = element.scrollHeight
}

export function messagePresentation(role) {
  const assistant = role === 'assistant'
  return {
    alignment: assistant ? 'left' : 'right',
    showAiAvatar: assistant,
    showUserAvatar: !assistant
  }
}

export function conversationPresentation(loading, messages, sending) {
  return {
    showEmpty: !loading && messages.length === 0,
    showThinking: Boolean(sending)
  }
}
