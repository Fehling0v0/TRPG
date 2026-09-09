import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器：自动注入 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理业务错误
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端统一响应结构 {code, message, data}
    if (res.code !== undefined) {
      if (res.code === 200) {
        return res
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.message || error.message

    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    } else if (status === 403) {
      ElMessage.error('没有权限执行此操作')
    } else {
      ElMessage.error(msg || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
