/**
 * Axios请求封装模块
 * 
 * 文件名：request.js
 * 功能描述：基于Axios创建统一的HTTP请求实例，配置请求/响应拦截器
 * 主要职责：
 *   1. 创建带基础配置的Axios实例
 *   2. 请求拦截：自动注入Token到请求头
 *   3. 响应拦截：统一处理错误码（401未登录、非200业务错误、网络错误等）
 */
import axios from 'axios'
import { Message } from 'element-ui'
import store from '@/store'
import { getToken } from '@/utils/auth'

/** 创建Axios实例，配置基础URL和超时时间 */
const service = axios.create({
  /** 接口基础路径，从环境变量读取 */
  baseURL: process.env.VUE_APP_BASE_API,
  /** 请求超时时间：60秒 */
  timeout: 60000
})

/**
 * 请求拦截器
 * 在每个请求发送前，自动从sessionStorage读取Token并注入到请求头Authorization字段
 */
service.interceptors.request.use(config => {
  const token = getToken()
  if (token) config.headers['Authorization'] = token
  return config
}, error => {
  console.error('Request error:', error)
  return Promise.reject(error)
})

/**
 * 响应拦截器
 * 统一处理响应数据：
 *   - 401：未登录或登录过期，自动跳转登录页
 *   - 非200：业务错误，弹出错误提示
 *   - 200：正常返回业务数据
 *   - 网络错误：根据HTTP状态码显示对应提示
 */
service.interceptors.response.use(res => {
  const data = res.data
  // 401未登录处理
  if (data && data.code === 401) {
    if (!res.config._silent) {
      Message({ message: data.msg || '未登录或登录已过期', type: 'error', duration: 3000 })
      store.dispatch('user/logout').then(() => { location.reload() })
    }
    return Promise.reject(new Error(data.msg || '未登录'))
  }
  // 非200业务错误处理
  if (data.code !== 200) {
    if (!res.config._silent) {
      Message({ message: data.msg || '请求失败', type: 'error', duration: 3000 })
    }
    return Promise.reject(new Error(data.msg || '请求失败'))
  }
  return data
}, error => {
  // 网络错误处理
  if (!error.config || !error.config._silent) {
    let message = '网络错误'
    if (error.response) {
      const responseData = error.response.data
      if (responseData && responseData.msg) {
        message = responseData.msg
      } else if (error.response.status === 401) {
        message = '未登录或登录已过期'
      } else if (error.response.status === 403) {
        message = '没有权限'
      } else if (error.response.status === 500) {
        message = '服务器错误'
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    }
    Message({ message: message, type: 'error', duration: 3000 })
  }
  return Promise.reject(error)
})

export default service
