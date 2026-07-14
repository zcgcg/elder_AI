const INVALID_CREDENTIAL_MESSAGES = new Set([
  'Account or password is incorrect',
  '账号或密码错误',
  '账号或密码不正确'
])

export function loginFailureMessage(error) {
  const message = String(error?.message || '').trim()
  if (INVALID_CREDENTIAL_MESSAGES.has(message)) return '账号或密码错误'
  return message || '登录失败，请稍后重试'
}
