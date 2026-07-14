import test from 'node:test'
import assert from 'node:assert/strict'
import { loginFailureMessage } from './loginFeedback.js'

test('invalid login credentials become an inline account-or-password error', () => {
  const error = Object.assign(new Error('Account or password is incorrect'), { status: 200, code: 1005 })

  assert.equal(loginFailureMessage(error), '账号或密码错误')
})

test('a real connection failure keeps the backend availability message', () => {
  const error = new Error('无法连接后端服务，请先启动 8080 后端')

  assert.equal(loginFailureMessage(error), '无法连接后端服务，请先启动 8080 后端')
})
