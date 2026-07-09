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
    if (body && body.message) throw new Error(body.message)
    return body
  },
  (error) => Promise.reject(normalizeHttpError(error))
)

function normalizeHttpError(error) {
  const message = error?.response?.data?.message || error?.message || ''
  if (error?.code === 'ECONNABORTED') return new Error('请求超时，请确认后端服务 8080 已启动')
  if (message === 'Network Error' || !error?.response) return new Error('无法连接后端服务，请先启动 8080 后端')
  return new Error(message || '请求失败')
}

export const login = (payload) => service.post('/auth/login', payload)
export const profile = () => service.get('/auth/profile')
export const updateProfile = (payload) => service.put('/auth/profile', payload)
export const getDashboard = () => service.get('/dashboard')
export const getAppointments = (params) => service.get('/appointments', { params })
export const createAppointment = (payload) => service.post('/appointments', payload)
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
  return service.post('/uploads', data, { headers: { 'Content-Type': 'multipart/form-data' } })
}
export const assetUrl = (url) => {
  if (!url || !String(url).startsWith('/uploads/')) return url
  if (typeof window === 'undefined' || window.location.port === '8080') return url
  return `${window.location.protocol}//${window.location.hostname}:8080${url}`
}
const resourcePaths = {
  workOrders: 'work-orders',
  afterSales: 'after-sales',
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
export const getResource = (resource, params) => service.get(`/${resourcePaths[resource] || resource}`, { params })
export const createResource = (resource, payload) => service.post(`/${resourcePaths[resource] || resource}`, payload)
export const updateResource = (resource, id, payload) => service.put(`/${resourcePaths[resource] || resource}/${id}`, payload)
export const deleteResource = (resource, id) => service.delete(`/${resourcePaths[resource] || resource}/${id}`)
export const getAnalytics = () => service.get('/analytics/overview')
