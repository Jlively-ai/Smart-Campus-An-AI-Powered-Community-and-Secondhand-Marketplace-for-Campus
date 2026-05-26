import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
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
  return res.data
}, error => {
  if (error.response && error.response.status === 401) {
    window.sessionStorage.removeItem('token')
    router.push('/login')
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
