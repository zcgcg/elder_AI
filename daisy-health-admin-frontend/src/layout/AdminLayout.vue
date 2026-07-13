<template>
  <el-container class="app-shell">
    <el-aside width="300px" class="nav-shell desktop-nav">
      <nav class="main-rail">
        <div class="rail-brand">
          <span class="brand-heart">❤</span>
          <strong>黛西健康</strong>
        </div>
        <button
          v-for="group in visibleMenuGroups"
          :key="group.key"
          type="button"
          :class="['rail-item', { active: activeGroup === group.key }]"
          :title="group.label"
          @click="selectGroup(group.key)"
        >
          <el-icon><component :is="group.icon" /></el-icon>
        </button>
      </nav>
      <section class="sub-nav">
        <header>{{ currentGroup.label }}</header>
        <h2>{{ currentGroup.label }}</h2>
        <button
          v-for="item in currentGroup.children"
          :key="item.path"
          type="button"
          :class="['sub-nav-item', { active: $route.path === item.path }]"
          @click="router.push(item.path)"
        >
          {{ item.label }}
        </button>
      </section>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <el-button class="mobile-menu-button" :icon="Menu" circle aria-label="打开功能菜单" @click="mobileNavOpen = true" />
        <el-input class="global-search" placeholder="搜索用户、订单、工单" clearable>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <div class="top-actions">
          <el-button :icon="Bell" circle />
          <el-button :icon="ChatDotRound" circle />
          <el-dropdown>
            <div class="profile">
              <el-avatar :size="34" :src="assetUrl(auth.user?.avatarUrl)">{{ auth.user?.name?.slice(0, 1) || '黛' }}</el-avatar>
              <span>{{ auth.user?.name || '系统管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="openProfile">个人资料</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <el-drawer v-model="mobileNavOpen" class="mobile-nav-drawer" direction="ltr" size="min(86vw, 340px)" :with-header="false">
    <nav class="mobile-nav" aria-label="移动端功能导航">
      <div class="mobile-nav-brand">
        <span class="brand-heart">❤</span>
        <div><strong>黛西健康</strong><small>管理中心</small></div>
      </div>
      <div class="mobile-nav-tools">
        <el-input placeholder="搜索用户、订单、工单" clearable>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <div>
          <el-button :icon="Bell">通知</el-button>
          <el-button :icon="ChatDotRound">消息</el-button>
        </div>
      </div>
      <section v-for="group in visibleMenuGroups" :key="group.key" class="mobile-nav-group">
        <button
          type="button"
          :class="['mobile-nav-group-title', { active: activeGroup === group.key }]"
          @click="selectGroup(group.key)"
        >
          <span><el-icon><component :is="group.icon" /></el-icon>{{ group.label }}</span>
          <el-icon><ArrowDown /></el-icon>
        </button>
        <div v-show="activeGroup === group.key" class="mobile-nav-children">
          <button
            v-for="item in group.children"
            :key="item.path"
            type="button"
            :class="{ active: $route.path === item.path }"
            @click="goToMobileRoute(item.path)"
          >
            {{ item.label }}
          </button>
        </div>
      </section>
    </nav>
  </el-drawer>

  <el-dialog v-model="profileVisible" title="个人资料" width="520px">
    <div class="admin-profile">
      <el-avatar :size="76" :src="assetUrl(profileForm.avatarUrl)">{{ profileForm.name?.slice(0, 1) || '管' }}</el-avatar>
      <dl>
        <dt>姓名</dt><dd>{{ profileForm.name }}</dd>
        <dt>员工编号</dt><dd>{{ profileForm.staffNo }}</dd>
        <dt>手机号码</dt><dd>{{ profileForm.phone }}</dd>
        <dt>角色</dt><dd>{{ profileForm.role }}</dd>
      </dl>
    </div>
    <el-form label-width="72px">
      <el-form-item label="姓名">
        <el-input v-model="profileForm.name" placeholder="姓名" />
      </el-form-item>
      <el-form-item label="编号">
        <el-input v-model="profileForm.staffNo" placeholder="员工编号" />
      </el-form-item>
      <el-form-item label="手机">
        <el-input v-model="profileForm.phone" placeholder="手机号码" />
      </el-form-item>
      <el-form-item label="头像">
        <avatar-picker ref="avatarPickerRef" :model-value="profileForm.avatarUrl" :fallback="profileForm.name?.slice(0, 1) || '管'" />
      </el-form-item>
      <el-form-item label="角色">
        <el-input v-model="profileForm.role" placeholder="角色名称" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="profileForm.remark" type="textarea" :rows="4" maxlength="200" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="profileVisible = false">取消</el-button>
      <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { Bell, ChatDotRound, Menu } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AvatarPicker from '../components/AvatarPicker.vue'
import { assetUrl } from '../api/http'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const profileVisible = ref(false)
const mobileNavOpen = ref(false)
const profileSaving = ref(false)
const avatarPickerRef = ref(null)
const profileForm = reactive({ id: '', name: '', staffNo: '', phone: '', avatarUrl: '', role: '', remark: '' })
const activeGroup = ref('home')
const menuGroups = [
  { key: 'home', module: 'dashboard', label: '首页', icon: 'House', children: [{ label: '工作台', path: '/dashboard' }, { label: '预约看板', path: '/schedule' }] },
  { key: 'users', label: '用户', icon: 'User', children: [{ label: '全部用户', path: '/users' }, { label: '设备信息', path: '/user-health/devices' }, { label: '报告信息', path: '/user-health/reports' }, { label: '健康设置', path: '/user-health/healthSettings' }, { label: '优惠券管理', path: '/user-assets/coupons' }, { label: '用户积分', path: '/user-assets/userPoints' }, { label: '等级管理', path: '/user-assets/memberLevels' }, { label: '积分规则', path: '/user-assets/pointsRules' }] },
  { key: 'service', label: '服务', icon: 'Service', children: [{ label: '服务人员', path: '/service/personnel' }, { label: '审核管理', path: '/service/audits' }, { label: '工单管理', path: '/service/work-orders' }] },
  { key: 'products', label: '商品服务', icon: 'Goods', children: [{ label: '商品服务管理', path: '/products' }] },
  { key: 'operations', label: '运营', icon: 'Star', children: [{ label: '动态管理', path: '/operations/posts' }, { label: '话题管理', path: '/operations/topics' }, { label: '轮播图管理', path: '/operations/banners' }, { label: '活动管理', path: '/operations/activities' }, { label: '活动报名', path: '/operations/activityEnrolls' }, { label: '食谱管理', path: '/operations/recipes' }, { label: '健康资讯', path: '/operations/articles' }, { label: '疾病宝典', path: '/operations/diseases' }, { label: '养老机构', path: '/operations/institutions' }, { label: '健康讲堂', path: '/operations/videos' }, { label: '食物管理', path: '/operations/foods' }, { label: '测评管理', path: '/operations/assessments' }] },
  { key: 'trade', label: '交易', icon: 'Wallet', children: [{ label: '订单管理', path: '/trade/orders' }, { label: '售后管理', path: '/trade/after-sales' }, { label: '评价管理', path: '/trade/reviews' }] },
  { key: 'analytics', module: 'analytics', label: '数据', icon: 'TrendCharts', children: [{ label: '数据分析', path: '/analytics' }] },
  { key: 'system', label: '系统', icon: 'Setting', children: [{ label: '员工管理', path: '/system/staffs' }, { label: '角色管理', path: '/system/roles' }, { label: '操作日志', path: '/system/logs' }] }
]
const visibleMenuGroups = computed(() => menuGroups
  .map((group) => ({
    ...group,
    children: group.children.filter((item) => auth.canAccess(item.module || group.module || group.key, 'view'))
  }))
  .filter((group) => group.children.length > 0))
const currentGroup = computed(() => visibleMenuGroups.value.find((group) => group.key === activeGroup.value) || visibleMenuGroups.value[0] || menuGroups[0])

function selectGroup(key) {
  activeGroup.value = key
}

function goToMobileRoute(path) {
  router.push(path)
  mobileNavOpen.value = false
}

function syncActiveGroup(path = route.path) {
  const matched = menuGroups.find((group) => group.children.some((item) => path === item.path || path.startsWith(item.path + '/')))
  activeGroup.value = matched?.key || 'home'
}

function syncProfile(user = auth.user || {}) {
  Object.assign(profileForm, {
    id: user.id || '',
    name: user.name || '系统管理员',
    staffNo: user.staffNo || 'S0001',
    phone: user.phone || '',
    avatarUrl: user.avatarUrl || '',
    role: user.role || '超级管理员',
    remark: user.remark || ''
  })
}

async function openProfile() {
  try {
    const user = await auth.loadProfile()
    syncProfile(user)
  } catch (error) {
    syncProfile()
  }
  profileVisible.value = true
  nextTick(() => avatarPickerRef.value?.reset(profileForm.avatarUrl))
}

async function saveProfile() {
  profileSaving.value = true
  try {
    const avatarUrl = await avatarPickerRef.value?.commitSelection()
    await auth.saveProfile({
      id: profileForm.id,
      name: profileForm.name,
      staffNo: profileForm.staffNo,
      phone: profileForm.phone,
      avatarUrl: avatarUrl || '',
      role: profileForm.role,
      remark: profileForm.remark
    })
    syncProfile(auth.user)
    ElMessage.success('个人资料已保存')
    profileVisible.value = false
  } catch (error) {
    ElMessage.error('保存失败，请确认后端和数据库已启动')
  } finally {
    profileSaving.value = false
  }
}

function logout() {
  auth.signOut()
  router.push('/login')
}

onMounted(() => {
  syncActiveGroup()
  syncProfile()
  auth.loadProfile().then(syncProfile).catch(() => {})
})
watch(() => route.path, (path) => {
  syncActiveGroup(path)
  mobileNavOpen.value = false
})
</script>
