/**
 * Token管理工具模块
 * 
 * 文件名：auth.js
 * 功能描述：管理管理员登录令牌（Token）的存取和删除
 * 主要职责：提供Token的获取、设置和移除功能，基于sessionStorage实现会话级存储
 */

/** Token在sessionStorage中存储的键名 */
const TokenKey = 'admin_token'

/**
 * 获取管理员Token
 * @returns {string|null} 返回存储的Token值，若不存在则返回null
 */
export function getToken() {
  return window.sessionStorage.getItem(TokenKey)
}

/**
 * 设置管理员Token
 * @param {string} token - 需要存储的Token值
 * @returns {void}
 */
export function setToken(token) {
  return window.sessionStorage.setItem(TokenKey, token)
}

/**
 * 移除管理员Token
 * 用于退出登录时清除会话信息
 * @returns {void}
 */
export function removeToken() {
  return window.sessionStorage.removeItem(TokenKey)
}
