/**
 * 违规管理API接口封装模块
 * 
 * 文件名：violation.js
 * 功能描述：封装违规管理相关的HTTP请求接口
 * 主要职责：提供违规记录的查询、新增、处理等接口调用
 */
import request from '@/utils/request'

/**
 * 获取违规记录列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword, status, type }
 * @returns {Promise} 违规记录列表数据
 */
export function getViolationList(params) {
  return request({ url: '/violation/list', method: 'get', params })
}

/**
 * 获取违规记录详情
 * @param {number} id - 违规记录ID
 * @returns {Promise} 违规记录详细信息
 */
export function getViolationDetail(id) {
  return request({ url: '/violation/detail/' + id, method: 'get' })
}

/**
 * 新增违规记录
 * @param {Object} data - 违规信息 { userId, type, content, evidence }
 * @returns {Promise}
 */
export function addViolation(data) {
  return request({ url: '/violation/add', method: 'post', data })
}

/**
 * 处理违规记录（确认/驳回）
 * @param {Object} data - { id, status, handleResult }
 * @returns {Promise}
 */
export function handleViolation(data) {
  return request({ url: '/violation/handle', method: 'post', data })
}

/**
 * 删除违规记录
 * @param {number} id - 违规记录ID
 * @returns {Promise}
 */
export function deleteViolation(id) {
  return request({ url: '/violation/delete/' + id, method: 'post' })
}

/**
 * 获取违规类型列表
 * @returns {Promise} 违规类型枚举数据
 */
export function getViolationTypes() {
  return request({ url: '/violation/types', method: 'get' })
}

/**
 * 处理违规申诉
 * @param {Object} data - { id, appealState, appealResult }
 * @returns {Promise}
 */
export function handleViolationAppeal(data) {
  return request({ url: '/violation-delete/handleAppeal', method: 'post', data })
}
