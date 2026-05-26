import Vue from 'vue'
import App from './App'
import store from './store'
import router from './router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import './styles/index.scss'

Vue.use(ElementUI, { size: 'small' })
Vue.config.productionTip = false

// 全局时间格式化过滤器：年月日时分秒
Vue.filter('formatTime', function(time) {
  if (!time) return ''
  const d = new Date(time)
  if (isNaN(d.getTime())) return time
  const pad = n => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
})

// 全局ID格式化过滤器：直接显示原始ID
Vue.filter('formatId', function(id, type) {
  if (!id) return ''
  return id
})

new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
})
