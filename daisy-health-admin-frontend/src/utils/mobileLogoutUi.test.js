import assert from 'node:assert/strict'
import fs from 'node:fs'
import test from 'node:test'

const adminLayout = fs.readFileSync(new URL('../layout/AdminLayout.vue', import.meta.url), 'utf8')
const userPortal = fs.readFileSync(new URL('../views/UserPortalView.vue', import.meta.url), 'utf8')
const servicePortal = fs.readFileSync(new URL('../views/ServicePortalView.vue', import.meta.url), 'utf8')

test('every mobile role exposes a clearly labelled logout button', () => {
  const visibleLogoutButton = /<el-button[^>]*class="mobile-logout-button"[^>]*@click="logout"[^>]*>[\s\S]*?退出登录[\s\S]*?<\/el-button>/

  assert.match(adminLayout, visibleLogoutButton)
  assert.match(userPortal, visibleLogoutButton)
  assert.match(servicePortal, visibleLogoutButton)
})
