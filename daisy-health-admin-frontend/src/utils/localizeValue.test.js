import test from 'node:test'
import assert from 'node:assert/strict'
import { localizeValue } from './localizeValue.js'

test('界面展示时把内部英文健康和设备代码转换为中文', () => {
  assert.equal(localizeValue('heart_rate'), '心率')
  assert.equal(localizeValue('band'), '智能手环')
  assert.equal(localizeValue('bpm'), '次/分钟')
  assert.equal(localizeValue('普通文本'), '普通文本')
})
