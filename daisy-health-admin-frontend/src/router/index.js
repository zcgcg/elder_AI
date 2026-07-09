import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AdminLayout from '../layout/AdminLayout.vue'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import ScheduleView from '../views/ScheduleView.vue'
import UsersView from '../views/UsersView.vue'
import UserDetailView from '../views/UserDetailView.vue'
import GenericListView from '../views/GenericListView.vue'
import AnalyticsView from '../views/AnalyticsView.vue'

const routes = [
  { path: '/login', component: LoginView },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: DashboardView, meta: { title: '工作台' } },
      { path: 'schedule', component: ScheduleView, meta: { title: '预约看板' } },
      { path: 'users', component: UsersView, meta: { title: '全部用户' } },
      { path: 'users/:id', component: UserDetailView, meta: { title: '用户详情' } },
      { path: 'user-assets/:resource', component: GenericListView, meta: { title: '用户资产', resourceFromParam: true } },
      { path: 'user-health/:resource', component: GenericListView, meta: { title: '用户健康扩展', resourceFromParam: true } },
      { path: 'service/personnel', component: GenericListView, meta: { title: '服务人员', resource: 'personnel' } },
      { path: 'service/audits', component: GenericListView, meta: { title: '审核管理', resource: 'audits' } },
      { path: 'service/work-orders', component: GenericListView, meta: { title: '工单管理', resource: 'workOrders' } },
      { path: 'products/:category?', component: GenericListView, meta: { title: '商品管理', resource: 'products' } },
      { path: 'product-ext/:resource', component: GenericListView, meta: { title: '商品扩展', resourceFromParam: true } },
      { path: 'operations/:resource', component: GenericListView, meta: { title: '运营管理', resourceFromParam: true } },
      { path: 'trade/orders', component: GenericListView, meta: { title: '订单管理', resource: 'orders' } },
      { path: 'trade/after-sales', component: GenericListView, meta: { title: '售后管理', resource: 'afterSales' } },
      { path: 'trade/reviews', component: GenericListView, meta: { title: '评价管理', resource: 'reviews' } },
      { path: 'analytics', component: AnalyticsView, meta: { title: '数据分析' } },
      { path: 'system/staffs', component: GenericListView, meta: { title: '员工管理', resource: 'staffs' } },
      { path: 'system/roles', component: GenericListView, meta: { title: '角色管理', resource: 'roles' } },
      { path: 'system/logs', component: GenericListView, meta: { title: '操作日志', resource: 'logs' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.isAuthenticated) return '/login'
  if (to.path !== '/login' && auth.user?.roleType && auth.user.roleType !== 'staff') {
    auth.signOut()
    return '/login'
  }
  if (to.path === '/login' && auth.isAuthenticated) return '/dashboard'
  const module = moduleForPath(to.path)
  if (module && auth.permissions && !auth.canAccess(module, 'view')) {
    const fallback = firstAccessiblePath(auth)
    if (!fallback) {
      auth.signOut()
      return '/login'
    }
    if (to.path !== fallback) {
      return fallback
    }
    return false
  }
  return true
})

function moduleForPath(path) {
  if (path.startsWith('/dashboard') || path.startsWith('/schedule')) return 'dashboard'
  if (path.startsWith('/users') || path.startsWith('/user-assets') || path.startsWith('/user-health')) return 'users'
  if (path.startsWith('/service')) return 'service'
  if (path.startsWith('/products') || path.startsWith('/product-ext')) return 'products'
  if (path.startsWith('/operations')) return 'operations'
  if (path.startsWith('/trade')) return 'trade'
  if (path.startsWith('/analytics')) return 'analytics'
  if (path.startsWith('/system')) return 'system'
  return null
}

function firstAccessiblePath(auth) {
  const candidates = [
    { path: '/dashboard', module: 'dashboard' },
    { path: '/users', module: 'users' },
    { path: '/service/personnel', module: 'service' },
    { path: '/products', module: 'products' },
    { path: '/operations/posts', module: 'operations' },
    { path: '/trade/orders', module: 'trade' },
    { path: '/analytics', module: 'analytics' },
    { path: '/system/staffs', module: 'system' }
  ]
  return candidates.find((item) => auth.canAccess(item.module, 'view'))?.path || ''
}

export default router
