/**
 * Vuex 状态管理配置文件
 * 功能描述：管理全局共享状态，包括用户信息、登录令牌、登录类型
 * 主要职责：
 *   1. 从sessionStorage初始化状态（页面刷新后保持登录）
 *   2. 提供mutations用于修改状态并同步到sessionStorage
 *   3. 提供getters供组件获取状态
 */
import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  /** 全局状态 */
  state: {
    /** 当前登录用户信息对象 */
    userInfo: JSON.parse(window.sessionStorage.getItem('userInfo') || '{}'),
    /** 登录令牌 */
    token: window.sessionStorage.getItem('token') || '',
    /** 登录类型：'user'普通用户 / 'admin'管理员 */
    loginType: window.sessionStorage.getItem('loginType') || 'user'
  },
  /** 状态变更方法（同步） */
  mutations: {
    /** 设置用户信息并持久化到sessionStorage */
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      window.sessionStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    /** 设置登录令牌并持久化到sessionStorage */
    SET_TOKEN(state, token) {
      state.token = token
      window.sessionStorage.setItem('token', token)
    },
    /** 设置登录类型并持久化到sessionStorage */
    SET_LOGIN_TYPE(state, loginType) {
      state.loginType = loginType
      window.sessionStorage.setItem('loginType', loginType)
    },
    /** 清除所有用户信息（退出登录时调用） */
    CLEAR_USER(state) {
      state.userInfo = {}
      state.token = ''
      state.loginType = 'user'
      window.sessionStorage.removeItem('userInfo')
      window.sessionStorage.removeItem('token')
      window.sessionStorage.removeItem('loginType')
    }
  },
  actions: {},
  /** 状态计算属性 */
  getters: {
    /** 获取当前用户信息 */
    getUserInfo: state => state.userInfo,
    /** 获取登录令牌 */
    getToken: state => state.token,
    /** 获取登录类型 */
    getLoginType: state => state.loginType,
    /** 判断是否为管理员 */
    isAdmin: state => state.loginType === 'admin',
    /** 获取用户角色名称 */
    roleName: state => state.userInfo.roleName || ''
  }
})
