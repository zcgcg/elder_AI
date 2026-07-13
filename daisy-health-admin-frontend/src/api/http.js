import axios from 'axios'

const service = axios.create({
  baseURL: '/api/v1',
  timeout: 8000
})

service.interceptors.request.use((config) => {
  const token = localStorage.getItem('daisy_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

service.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.code === 0) return body.data
    if (body && body.message) throw apiError(body.message, body.code, response.status)
    return body
  },
  (error) => Promise.reject(normalizeHttpError(error))
)

function normalizeHttpError(error) {
  const message = error?.response?.data?.message || error?.message || ''
  if (error?.code === 'ECONNABORTED') return apiError('请求超时，请确认后端服务 8080 已启动', error.code)
  if (message === 'Network Error' || !error?.response) return apiError('无法连接后端服务，请先启动 8080 后端', error?.code)
  return apiError(message || '请求失败', error?.response?.data?.code, error?.response?.status)
}

function apiError(message, code, status) {
  const error = new Error(message)
  error.code = code
  error.status = status
  return error
}

export const login = (payload) => service.post('/auth/login', payload)
export const profile = () => service.get('/auth/profile')
export const updateProfile = (payload) => service.put('/auth/profile', payload)
export const getDashboard = () => service.get('/dashboard')
export const getAppointments = (params) => service.get('/appointments', { params })
export const createAppointment = (payload) => service.post('/appointments', payload)
export const updateAppointment = (id, payload) => service.put(`/appointments/${id}`, payload)
export const deleteAppointment = (id) => service.delete(`/appointments/${id}`)
export const getUsers = (params) => service.get('/users', { params })
export const createUser = (payload) => service.post('/users', payload)
export const updateUser = (id, payload) => service.put(`/users/${id}`, payload)
export const deleteUser = (id) => service.delete(`/users/${id}`)
export const getUser = (id) => service.get(`/users/${id}`)
export const getTags = () => service.get('/tags')
export const createTag = (payload) => service.post('/tags', payload)
export const updateTag = (id, payload) => service.put(`/tags/${id}`, payload)
export const deleteTag = (id) => service.delete(`/tags/${id}`)
export const updateUserTags = (id, payload) => service.put(`/users/${id}/tags`, payload)
export const uploadFile = (file, category) => {
  const data = new FormData()
  data.append('file', file)
  data.append('category', category)
  return service.post('/uploads', data)
}
export const assetUrl = (url) => {
  if (!url || !String(url).startsWith('/uploads/')) return url
  return url
}
const resourcePaths = {
  workOrders: 'work-orders',
  afterSales: 'after-sales',
  healthData: 'health-data',
  healthSettings: 'health-settings',
  userPoints: 'user-points',
  pointsRecords: 'points-records',
  memberLevels: 'member-levels',
  pointsRules: 'points-rules',
  productCategories: 'product-categories',
  serviceItems: 'service-items',
  activityEnrolls: 'activity-enrolls',
  assessmentResults: 'assessment-results'
}
export const resourcePath = (resource) => `/${resourcePaths[resource] || resource}`
export const getResource = (resource, params) => service.get(resourcePath(resource), { params })
export const createResource = (resource, payload) => service.post(resourcePath(resource), payload)
export const updateResource = (resource, id, payload) => service.put(`${resourcePath(resource)}/${id}`, payload)
export const deleteResource = (resource, id) => service.delete(`${resourcePath(resource)}/${id}`)
export const getAnalytics = () => service.get('/analytics/overview')
export const getElderlyProfile = () => service.get('/elderly/profile')
export const updateElderlyProfile = (payload) => service.put('/elderly/profile', payload)
export const updateElderlyAvatar = (payload) => service.put('/elderly/profile/avatar', payload)
export const getElderlyHealthData = () => service.get('/elderly/health-data')
export const getElderlyMedications = () => service.get('/elderly/medications')
export const getElderlyDevices = () => service.get('/elderly/devices')
export const updateElderlyDevice = (id, payload) => service.put(`/elderly/devices/${id}`, payload)
export const getElderlyReports = () => service.get('/elderly/reports')
export const getElderlyOrders = () => service.get('/elderly/orders')
export const getElderlyCoupons = () => service.get('/elderly/coupons')
export const getElderlyPoints = () => service.get('/elderly/points')
export const getElderlyCatalogItems = () => service.get('/elderly/catalog-items')
export const getElderlyPersonnel = () => service.get('/elderly/personnel')
export const getElderlyWorkOrders = () => service.get('/elderly/work-orders')
export const createElderlyWorkOrder = (payload) => service.post('/elderly/work-orders', payload)
export const getElderlyActivities = () => service.get('/elderly/activities')
export const enrollElderlyActivity = (id) => service.post(`/elderly/activities/${id}/enroll`)
export const getElderlyHealthArticles = () => service.get('/elderly/health-articles')
export const getElderlyHealthVideos = () => service.get('/elderly/health-videos')
export const getServiceProfile = () => service.get('/service-app/profile')
export const getServiceWorkOrders = () => service.get('/service-app/work-orders')
export const getServiceWorkOrder = (id) => service.get(`/service-app/work-orders/${id}`)
export const updateServiceWorkOrderStatus = (id, status) => service.put(`/service-app/work-orders/${id}/status`, { status })
