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
        <el-menu-item index="/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-sub-menu index="service">
          <template #title><el-icon><Service /></el-icon><span>服务管理</span></template>
          <el-menu-item index="/service/personnel">服务人员</el-menu-item>
          <el-menu-item index="/service/audits">审核管理</el-menu-item>
          <el-menu-item index="/service/work-orders">工单管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/products"><el-icon><Goods /></el-icon><span>商品管理</span></el-menu-item>
        <el-sub-menu index="operations">
          <template #title><el-icon><Collection /></el-icon><span>运营管理</span></template>
          <el-menu-item index="/operations/posts">动态管理</el-menu-item>
          <el-menu-item index="/operations/activities">活动管理</el-menu-item>
          <el-menu-item index="/operations/articles">健康资讯</el-menu-item>
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
              <el-avatar :size="34">黛</el-avatar>
              <span>{{ auth.user?.name || '系统管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人资料</el-dropdown-item>
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
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { Bell, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const auth = useAuthStore()

function logout() {
  auth.signOut()
  router.push('/login')
}
</script>
