import { login, getInfo, logout } from '@/api/manage'
import { getToken, setToken, removeToken } from '@/utils/auth'

const state = { token: getToken(), name: '', avatar: '', roles: [], permissions: '' }
const mutations = { SET_TOKEN: (state, token) => { state.token = token }, SET_NAME: (state, name) => { state.name = name }, SET_ROLES: (state, roles) => { state.roles = roles }, SET_PERMISSIONS: (state, permissions) => { state.permissions = permissions } }
const actions = {
  login({ commit }, userInfo) {
    return new Promise((resolve, reject) => {
      login(userInfo).then(res => {
        const token = res.data
        commit('SET_TOKEN', token)
        setToken(token)
        resolve()
      }).catch(reject)
    })
  },
  getInfo({ commit }) {
    return new Promise((resolve, reject) => {
      getInfo().then(res => {
        const data = res.data
        commit('SET_NAME', data.username || 'admin')
        commit('SET_ROLES', [data.roleId === 1 ? 'super' : 'admin'])
        commit('SET_PERMISSIONS', data.permissions || '')
        resolve(data)
      }).catch(reject)
    })
  },
  logout({ commit }) { commit('SET_TOKEN', ''); commit('SET_PERMISSIONS', ''); removeToken(); return Promise.resolve() },
  resetToken({ commit }) { commit('SET_TOKEN', ''); removeToken(); return Promise.resolve() }
}

export default { namespaced: true, state, mutations, actions }
