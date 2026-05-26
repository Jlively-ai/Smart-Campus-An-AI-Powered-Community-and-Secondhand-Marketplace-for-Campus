import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import { Message } from 'element-ui'
import axios from 'axios'

Vue.use(ElementUI)
Vue.prototype.$axios = axios
axios.defaults.baseURL = '/dev_api'
axios.interceptors.request.use(config => {
  const token = window.sessionStorage.getItem('token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})
axios.interceptors.response.use(res => {
  const data = res.data
  if (data && data.code === 401) {
    window.sessionStorage.removeItem('token')
    router.push('/login').catch(() => {})
    Message.error(data.msg || '未登录或登录已过期')
    return Promise.reject(new Error(data.msg || '未登录'))
  }
  // 账号被锁定时强制退出
  if (data && data.msg && (data.msg.includes('已被锁定') || data.msg.includes('账号已被锁定'))) {
    window.sessionStorage.removeItem('token')
    window.sessionStorage.removeItem('userInfo')
    window.sessionStorage.removeItem('loginType')
    store.commit('CLEAR_USER')
    router.push('/login').catch(() => {})
    Message.error('账号已被锁定，请联系管理员')
    return Promise.reject(new Error('账号已被锁定'))
  }
  return data
}, error => {
  if (error.response) {
    if (error.response.status === 401) {
      window.sessionStorage.removeItem('token')
      router.push('/login').catch(() => {})
      Message.error('未登录或登录已过期')
    } else if (error.response.status === 403) {
      Message.error('没有权限')
    } else if (error.response.status === 500) {
      const data = error.response.data
      Message.error((data && data.msg) || '服务器错误')
    } else {
      Message.error('网络错误')
    }
  } else if (error.code === 'ECONNABORTED') {
    Message.error('请求超时')
  } else {
    Message.error('网络连接失败')
  }
  return Promise.reject(error)
})

Vue.config.productionTip = false

// 全局@提及点击导航函数
window.__goUserProfile = function(userId) {
  var router = require('./router').default
  if (router) {
    router.push('/userProfile/' + userId).catch(function() {})
  }
}

// 全局时间格式化过滤器：年月日时分秒
Vue.filter('formatTime', function(time) {
  if (!time) return ''
  const d = new Date(time)
  if (isNaN(d.getTime())) return time
  const pad = n => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
})

// 全局ID格式化过滤器：添加类型前缀
Vue.filter('formatId', function(id, type) {
  if (!id) return ''
  var prefixMap = {
    user: 'USR', admin: 'Admin', root: 'ROOT', post: 'POST', goods: 'SP',
    order: 'DD', wall: 'WALL', logistics: '', comment: 'PL',
    announcement: 'GG', report: 'JB', punishment: 'CF', review: 'PJ'
  }
  var prefix = prefixMap[type] || type
  if (!prefix) return id
  return prefix + '-' + id
})

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
