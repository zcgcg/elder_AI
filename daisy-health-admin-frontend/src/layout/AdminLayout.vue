<template>
  <el-container class="app-shell">
    <el-aside width="232px" class="sidebar">
      <div class="brand">
        <span class="brand-mark">D</span>
        <div>
          <strong>黛西健康</strong>
          <small>智慧养老后台</small>
        </div>
      </div>
      <el-menu router :default-active="$route.path" class="side-menu">
        <el-menu-item index="/dashboard"><el-icon><DataBoard /></el-icon><span>首页工作台</span></el-menu-item>
        <el-menu-item index="/schedule"><el-icon><Calendar /></el-icon><span>预约看板</span></el-menu-item>
        <el-sub-menu index="users">
          <template #title><el-icon><User /></el-icon><span>用户管理</span></template>
          <el-menu-item index="/users">全部用户</el-menu-item>
          <el-menu-item index="/user-health/devices">设备信息</el-menu-item>
          <el-menu-item index="/user-health/reports">报告信息</el-menu-item>
          <el-menu-item index="/user-health/healthSettings">健康设置</el-menu-item>
          <el-menu-item index="/user-assets/coupons">优惠券管理</el-menu-item>
          <el-menu-item index="/user-assets/userPoints">用户积分</el-menu-item>
          <el-menu-item index="/user-assets/memberLevels">等级管理</el-menu-item>
          <el-menu-item index="/user-assets/pointsRules">积分规则</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="service">
          <template #title><el-icon><Service /></el-icon><span>服务管理</span></template>
          <el-menu-item index="/service/personnel">服务人员</el-menu-item>
          <el-menu-item index="/service/audits">审核管理</el-menu-item>
          <el-menu-item index="/service/work-orders">工单管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="products">
          <template #title><el-icon><Goods /></el-icon><span>商品管理</span></template>
          <el-menu-item index="/products">商品管理</el-menu-item>
          <el-menu-item index="/product-ext/productCategories">分类管理</el-menu-item>
          <el-menu-item index="/product-ext/serviceItems">服务项目</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="operations">
          <template #title><el-icon><Collection /></el-icon><span>运营管理</span></template>
          <el-menu-item index="/operations/posts">动态管理</el-menu-item>
          <el-menu-item index="/operations/topics">话题管理</el-menu-item>
          <el-menu-item index="/operations/banners">轮播图管理</el-menu-item>
          <el-menu-item index="/operations/activities">活动管理</el-menu-item>
          <el-menu-item index="/operations/activityEnrolls">活动报名</el-menu-item>
          <el-menu-item index="/operations/recipes">食谱管理</el-menu-item>
          <el-menu-item index="/operations/articles">健康资讯</el-menu-item>
          <el-menu-item index="/operations/diseases">疾病宝典</el-menu-item>
          <el-menu-item index="/operations/institutions">养老机构</el-menu-item>
          <el-menu-item index="/operations/videos">健康讲堂</el-menu-item>
          <el-menu-item index="/operations/foods">食物管理</el-menu-item>
          <el-menu-item index="/operations/assessments">测评管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="trade">
          <template #title><el-icon><Tickets /></el-icon><span>交易管理</span></template>
          <el-menu-item index="/trade/orders">订单管理</el-menu-item>
          <el-menu-item index="/trade/after-sales">售后管理</el-menu-item>
          <el-menu-item index="/trade/reviews">评价管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/analytics"><el-icon><TrendCharts /></el-icon><span>数据分析</span></el-menu-item>
        <el-sub-menu index="system">
          <template #title><el-icon><Setting /></el-icon><span>系统设置</span></template>
          <el-menu-item index="/system/staffs">员工管理</el-menu-item>
          <el-menu-item index="/system/roles">角色管理</el-menu-item>
          <el-menu-item index="/system/logs">操作日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <el-input class="global-search" placeholder="搜索用户、订单、工单" clearable>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <div class="top-actions">
          <el-button :icon="Bell" circle />
          <el-button :icon="ChatDotRound" circle />
          <el-dropdown>
            <div class="profile">
              <el-avatar :size="34" :src="auth.user?.avatarUrl">{{ auth.user?.name?.slice(0, 1) || '黛' }}</el-avatar>
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

  <el-dialog v-model="profileVisible" title="个人资料" width="520px">
    <div class="admin-profile">
      <el-avatar :size="76" :src="profileForm.avatarUrl">{{ profileForm.name?.slice(0, 1) || '管' }}</el-avatar>
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
        <el-input v-model="profileForm.avatarUrl" placeholder="头像 URL" />
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
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { Bell, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const profileVisible = ref(false)
const profileSaving = ref(false)
const profileForm = reactive({ id: '', name: '', staffNo: '', phone: '', avatarUrl: '', role: '', remark: '' })

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
}

async function saveProfile() {
  profileSaving.value = true
  try {
    await auth.saveProfile({
      id: profileForm.id,
      name: profileForm.name,
      staffNo: profileForm.staffNo,
      phone: profileForm.phone,
      avatarUrl: profileForm.avatarUrl,
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
  syncProfile()
  auth.loadProfile().then(syncProfile).catch(() => {})
})
</script>
