export const fallbackDashboard = {
  metrics: [
    { label: '新增用户', value: 128, delta: '+12.4%', tone: 'success' },
    { label: '新增工单', value: 76, delta: '+8.7%', tone: 'warning' },
    { label: '新增订单', value: 214, delta: '+16.1%', tone: 'success' },
    { label: '新增动态', value: 43, delta: '-3.2%', tone: 'danger' }
  ],
  tagDistribution: [
    { name: '高血压', value: 168 },
    { name: '独居老人', value: 132 },
    { name: '康复护理', value: 98 },
    { name: '糖尿病', value: 86 },
    { name: '重点关怀', value: 64 }
  ],
  serviceShare: [
    { name: '家政护理', value: 42 },
    { name: '康复理疗', value: 34 },
    { name: '上门体检', value: 24 }
  ],
  trend: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'].map((day, index) => ({
    day,
    users: [18, 24, 36, 42, 48, 56, 64][index],
    orders: [32, 40, 46, 51, 62, 71, 86][index]
  }))
}

export const fallbackRows = {
  personnel: [
    { id: 1, name: '张敏', serviceType: '家政护理', area: '浦东新区', status: '启用', updatedAt: '2026-07-05 10:20' },
    { id: 2, name: '李华', serviceType: '康复理疗', area: '徐汇区', status: '启用', updatedAt: '2026-07-04 15:30' }
  ],
  audits: [
    { id: 1, name: '周丽', serviceType: '上门体检', auditStatus: '待审核', phone: '138****8123', updatedAt: '2026-07-06 09:10' }
  ],
  workOrders: [
    { id: 1, orderNo: 'WO20260706001', serviceItem: '助浴护理', customer: '王秀兰', status: '待服务', updatedAt: '2026-07-06 11:00' },
    { id: 2, orderNo: 'WO20260706002', serviceItem: '肩颈康复', customer: '陈建国', status: '服务中', updatedAt: '2026-07-06 12:00' }
  ],
  products: [
    { id: 1, name: '2小时日常清洁', category: '家政护理', price: 129, status: '上架', updatedAt: '2026-07-03 17:00' },
    { id: 2, name: '脑中风康复训练', category: '康复理疗', price: 299, status: '上架', updatedAt: '2026-07-01 14:30' }
  ],
  orders: [
    { id: 1, orderNo: 'OD20260706001', productName: '上门基础体检', buyer: '赵桂英', amount: 399, status: '待接单' }
  ],
  afterSales: [
    { id: 1, orderNo: 'OD20260702008', applicant: '刘爱华', reason: '服务时间变更', status: '处理中' }
  ],
  reviews: [
    { id: 1, productName: '助餐服务', user: '沈美玲', rating: 5, status: '已显示' }
  ],
  posts: [
    { id: 1, title: '社区晨练打卡', publisher: '王秀兰', likes: 89, status: '已发布' }
  ],
  activities: [
    { id: 1, title: '慢病管理讲座', location: '浦东社区中心', quota: 60, status: '报名中' }
  ],
  articles: [
    { id: 1, title: '夏季老人补水指南', author: '运营中心', status: '已发布' }
  ],
  staffs: [
    { id: 1, staffNo: 'S0001', name: '系统管理员', role: '超级管理员', status: '启用' }
  ],
  roles: [
    { id: 1, name: '超级管理员', description: '全部模块与全部操作', status: '启用' }
  ],
  logs: [
    { id: 1, operator: '系统管理员', actionType: '登录', target: '后台系统', createdAt: '2026-07-06 09:00' }
  ]
}
