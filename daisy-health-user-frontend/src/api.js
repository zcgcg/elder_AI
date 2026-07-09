import axios from 'axios'

export const tokenKey = 'daisy_user_token'

const http = axios.create({
  baseURL: '/api/v1',
  timeout: 8000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(tokenKey)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.code === 0) return body.data
    throw new Error(body?.message || '请求失败')
  },
  (error) => Promise.reject(error)
)

export const login = (payload) => http.post('/auth/login', payload)
export const getAuthProfile = () => http.get('/auth/profile')
export const getProfile = () => http.get('/elderly/profile')
export const getHealthData = () => http.get('/elderly/health-data')
export const getMedications = () => http.get('/elderly/medications')
export const getDevices = () => http.get('/elderly/devices')
export const getReports = () => http.get('/elderly/reports')
export const getOrders = () => http.get('/elderly/orders')
export const getCoupons = () => http.get('/elderly/coupons')
export const getPoints = () => http.get('/elderly/points')
