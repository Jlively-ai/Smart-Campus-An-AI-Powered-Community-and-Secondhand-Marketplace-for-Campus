/**
 * 后台管理API接口封装模块
 * 
 * 文件名：manage.js
 * 功能描述：封装后台管理系统所有业务模块的HTTP请求接口
 * 主要职责：统一管理各业务模块（用户、帖子、商品、订单等）的增删改查接口调用
 */
import request from '@/utils/request'

/** ==================== 管理员模块 ==================== */

/**
 * 管理员登录
 * @param {Object} data - 登录信息 { username, password }
 * @returns {Promise} 登录结果
 */
export function login(data) {
  return request({ url: '/admin/info/login', method: 'post', data })
}

/**
 * 获取当前登录管理员信息
 * @returns {Promise} 管理员信息
 */
export function getInfo() {
  return request({ url: '/admin/info', method: 'get' })
}

/**
 * 管理员退出登录
 * @returns {Promise}
 */
export function logout() {
  return request({ url: '/admin/info/logout', method: 'delete' })
}

/**
 * 获取管理员列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword }
 * @returns {Promise} 管理员列表数据
 */
export function getAdminList(params) {
  return request({ url: '/admin/info/list', method: 'get', params })
}

/**
 * 新增管理员
 * @param {Object} data - 管理员信息
 * @returns {Promise}
 */
export function addAdmin(data) {
  return request({ url: '/admin/info/add', method: 'post', data })
}

/**
 * 修改管理员信息
 * @param {Object} data - 管理员信息（含id）
 * @returns {Promise}
 */
export function updateAdmin(data) {
  return request({ url: '/admin/update', method: 'post', data })
}

/**
 * 删除管理员
 * @param {number} id - 管理员ID
 * @returns {Promise}
 */
export function deleteAdmin(id) {
  return request({ url: '/admin/info/deleteAdmin', method: 'delete', params: { id } })
}

/**
 * 修改管理员权限
 * @param {Object} data - { adminId, permissions }
 * @returns {Promise}
 */
export function changePermissions(data) {
  return request({ url: '/admin/info/changePermissions', method: 'post', data })
}

/**
 * 获取权限列表
 * @returns {Promise} 权限列表数据
 */
export function getPermissionList() {
  return request({ url: '/admin/info/permissionList', method: 'get' })
}

/** ==================== 用户模块 ==================== */

/**
 * 获取用户列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword, status }
 * @returns {Promise} 用户列表数据
 */
export function getUserList(params) {
  return request({ url: '/user/list', method: 'get', params })
}

/**
 * 修改用户状态（启用/禁用）
 * @param {Object} data - { id, status }
 * @returns {Promise}
 */
export function updateUserStatus(data) {
  return request({ url: '/user/updateStatus', method: 'post', data })
}

/**
 * 获取用户详情
 * @param {number} id - 用户ID
 * @returns {Promise} 用户详细信息
 */
export function getUserDetail(id) {
  return request({ url: '/user/detail/' + id, method: 'get' })
}

/**
 * 锁定用户
 * @param {Object} data - { userId }
 * @returns {Promise}
 */
export function lockedUser(data) {
  return request({ url: '/user/lockedUser', method: 'post', data })
}

/**
 * 解锁用户
 * @param {Object} data - { userId }
 * @returns {Promise}
 */
export function unLockUser(data) {
  return request({ url: '/user/unLockUser', method: 'post', data })
}

/**
 * 管理员重置用户密码
 * @param {Object} data - { userId }
 * @returns {Promise}
 */
export function resetUserPwd(data) {
  return request({ url: '/user/changePwdByAdmin', method: 'post', data })
}

/**
 * 获取用户总数
 * @returns {Promise} 用户总数
 */
export function getUserTotal() {
  return request({ url: '/user/getUserTotal', method: 'get' })
}

/** ==================== 帖子模块 ==================== */

/**
 * 获取帖子列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword, categoryId, status }
 * @returns {Promise} 帖子列表数据
 */
export function getPostList(params) {
  return request({ url: '/post/list', method: 'get', params })
}

/**
 * 获取帖子详情
 * @param {number} id - 帖子ID
 * @returns {Promise} 帖子详细信息
 */
export function getPostDetail(id) {
  return request({ url: '/post/detail/' + id, method: 'get' })
}

/**
 * 修改帖子状态（审核/下架等）
 * @param {Object} data - { id, status }
 * @returns {Promise}
 */
export function updatePostStatus(data) {
  return request({ url: '/post/updateStatus', method: 'post', data })
}

/**
 * 删除帖子
 * @param {number} id - 帖子ID
 * @returns {Promise}
 */
export function deletePost(id) {
  return request({ url: '/post/delete/' + id, method: 'post' })
}

/**
 * 锁定帖子
 * @param {Object} data - { postId }
 * @returns {Promise}
 */
export function lockedPost(data) {
  return request({ url: '/post/lockedPost', method: 'post', data })
}

/**
 * 解锁帖子
 * @param {Object} data - { postId }
 * @returns {Promise}
 */
export function unLockPost(data) {
  return request({ url: '/post/unLockPost', method: 'post', data })
}

/**
 * 审核帖子
 * @param {Object} data - { postId, auditState }
 * @returns {Promise}
 */
export function auditPost(data) {
  return request({ url: '/post/audit', method: 'post', data })
}

/**
 * 获取帖子总数
 * @returns {Promise} 帖子总数
 */
export function getPostTotal() {
  return request({ url: '/post/getPostTotal', method: 'get' })
}

/**
 * 获取帖子统计数据
 * @returns {Promise} 帖子统计数据
 */
export function getPostData() {
  return request({ url: '/post/getPostData', method: 'get' })
}

/** ==================== 评论模块 ==================== */

/**
 * 获取评论列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword, postId }
 * @returns {Promise} 评论列表数据
 */
export function getCommentList(params) {
  return request({ url: '/comment/list', method: 'get', params })
}

/**
 * 删除评论
 * @param {number} id - 评论ID
 * @returns {Promise}
 */
export function deleteComment(id) {
  return request({ url: '/comment/delete/' + id, method: 'post' })
}

/**
 * 锁定评论
 * @param {Object} data - { commentId }
 * @returns {Promise}
 */
export function lockComment(data) {
  return request({ url: '/comment/lockComment', method: 'post', data })
}

/**
 * 解锁评论
 * @param {Object} data - { commentId }
 * @returns {Promise}
 */
export function unlockComment(data) {
  return request({ url: '/comment/unlockComment', method: 'post', data })
}

/**
 * 审核评论
 * @param {Object} data - { commentId, auditState }
 * @returns {Promise}
 */
export function auditComment(data) {
  return request({ url: '/comment/auditComment', method: 'post', data })
}

/**
 * 获取评论总数
 * @returns {Promise} 评论总数
 */
export function getCommentTotal() {
  return request({ url: '/comment/getCommentTotal', method: 'get' })
}

/** ==================== 表白墙模块 ==================== */

/**
 * 获取表白墙列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword, status }
 * @returns {Promise} 表白墙列表数据
 */
export function getWallList(params) {
  return request({ url: '/wall/list', method: 'get', params })
}

/**
 * 获取表白墙详情
 * @param {number} id - 表白墙ID
 * @returns {Promise} 表白墙详细信息
 */
export function getWallDetail(id) {
  return request({ url: '/wall/detail/' + id, method: 'get' })
}

/**
 * 审核表白墙（通过/拒绝）
 * @param {Object} data - { id, status }
 * @returns {Promise}
 */
export function auditWall(data) {
  return request({ url: '/wall/audit', method: 'post', data })
}

/**
 * 删除表白墙
 * @param {number} id - 表白墙ID
 * @returns {Promise}
 */
export function deleteWall(id) {
  return request({ url: '/wall/delete/' + id, method: 'post' })
}

/**
 * 锁定表白墙
 * @param {Object} data - { wallId }
 * @returns {Promise}
 */
export function lockWall(data) {
  return request({ url: '/wall/lockWall/' + data.wallId, method: 'post' })
}

/**
 * 解锁表白墙
 * @param {Object} data - { wallId }
 * @returns {Promise}
 */
export function unlockWall(data) {
  return request({ url: '/wall/unlockWall/' + data.wallId, method: 'post' })
}

/**
 * 获取表白墙总数
 * @returns {Promise} 表白墙总数
 */
export function getWallTotal() {
  return request({ url: '/wall/getWallTotal', method: 'get' })
}

/** ==================== 商品模块 ==================== */

/**
 * 获取商品列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword, categoryId, status }
 * @returns {Promise} 商品列表数据
 */
export function getGoodsList(params) {
  return request({ url: '/goods/list', method: 'get', params })
}

/**
 * 获取商品详情
 * @param {number} id - 商品ID
 * @returns {Promise} 商品详细信息
 */
export function getGoodsDetail(id) {
  return request({ url: '/goods/detail/' + id, method: 'get' })
}

/**
 * 修改商品状态（上架/下架）
 * @param {Object} data - { id, status }
 * @returns {Promise}
 */
export function updateGoodsStatus(data) {
  return request({ url: '/goods/updateStatus', method: 'post', data })
}

/**
 * 删除商品
 * @param {number} id - 商品ID
 * @returns {Promise}
 */
export function deleteGoods(id) {
  return request({ url: '/goods/delete/' + id, method: 'post' })
}

/**
 * 锁定商品
 * @param {string} goodsId - 商品ID
 * @returns {Promise}
 */
export function lockGoods(goodsId) {
  return request({ url: '/goods/lockGoods/' + goodsId, method: 'post' })
}

/**
 * 解锁商品
 * @param {string} goodsId - 商品ID
 * @returns {Promise}
 */
export function unlockGoods(goodsId) {
  return request({ url: '/goods/unlockGoods/' + goodsId, method: 'post' })
}

/**
 * 审核商品
 * @param {Object} data - { goodsId, auditState }
 * @returns {Promise}
 */
export function auditGoods(data) {
  return request({ url: '/goods/audit', method: 'post', data })
}

/**
 * 下架商品
 * @param {string} goodsId - 商品ID
 * @returns {Promise}
 */
export function unShelveGoods(goodsId) {
  return request({ url: '/goods/unShelve/' + goodsId, method: 'post' })
}

/**
 * 获取商品总数
 * @returns {Promise} 商品总数
 */
export function getGoodsTotal() {
  return request({ url: '/goods/getGoodsTotal', method: 'get' })
}

/**
 * 按分类获取商品统计
 * @returns {Promise} 商品分类统计数据
 */
export function getGoodsByCategory() {
  return request({ url: '/goods/getGoodsByCategory', method: 'get' })
}

/** ==================== 商品分类模块 ==================== */

/**
 * 获取商品分类列表
 * @param {Object} params - 查询参数 { page, size }
 * @returns {Promise} 商品分类列表数据
 */
export function getGoodsCategoryList(params) {
  return request({ url: '/goods/category/list', method: 'get', params })
}

/**
 * 新增商品分类
 * @param {Object} data - 分类信息 { name, icon, sort }
 * @returns {Promise}
 */
export function addGoodsCategory(data) {
  return request({ url: '/goods/category/add', method: 'post', data })
}

/**
 * 修改商品分类
 * @param {Object} data - 分类信息（含id）
 * @returns {Promise}
 */
export function updateGoodsCategory(data) {
  return request({ url: '/goods/category/update', method: 'post', data })
}

/**
 * 删除商品分类
 * @param {number} id - 分类ID
 * @returns {Promise}
 */
export function deleteGoodsCategory(id) {
  return request({ url: '/goods/category/delete/' + id, method: 'post' })
}

/** ==================== 帖子分类模块 ==================== */

/**
 * 获取帖子分类列表
 * @param {Object} params - 查询参数 { page, size }
 * @returns {Promise} 帖子分类列表数据
 */
export function getPostCategoryList(params) {
  return request({ url: '/post/category/list', method: 'get', params })
}

/**
 * 新增帖子分类
 * @param {Object} data - 分类信息 { name, icon, sort }
 * @returns {Promise}
 */
export function addPostCategory(data) {
  return request({ url: '/post/category/add', method: 'post', data })
}

/**
 * 修改帖子分类
 * @param {Object} data - 分类信息（含id）
 * @returns {Promise}
 */
export function updatePostCategory(data) {
  return request({ url: '/post/category/update', method: 'post', data })
}

/**
 * 删除帖子分类
 * @param {number} id - 分类ID
 * @returns {Promise}
 */
export function deletePostCategory(id) {
  return request({ url: '/post/category/delete/' + id, method: 'post' })
}

/** ==================== 订单模块 ==================== */

/**
 * 获取订单列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword, status }
 * @returns {Promise} 订单列表数据
 */
export function getOrderList(params) {
  return request({ url: '/order/list', method: 'get', params })
}

/**
 * 获取订单详情
 * @param {number} id - 订单ID
 * @returns {Promise} 订单详细信息
 */
export function getOrderDetail(id) {
  return request({ url: '/order/detail/' + id, method: 'get' })
}

/**
 * 获取订单总数
 * @returns {Promise} 订单总数
 */
export function getOrderTotal() {
  return request({ url: '/market-order/getOrderTotal', method: 'get' })
}

/**
 * 获取销售统计数据
 * @returns {Promise} 销售统计数据
 */
export function getSalesData() {
  return request({ url: '/market-order/getSalesData', method: 'get' })
}

/** ==================== 售后模块 ==================== */

/**
 * 获取售后列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword, status }
 * @returns {Promise} 售后列表数据
 */
export function getAfterSaleList(params) {
  return request({ url: '/after-sale/list', method: 'get', params })
}

/**
 * 处理售后申请（同意/拒绝）
 * @param {Object} data - { id, status, reply }
 * @returns {Promise}
 */
export function handleAfterSale(data) {
  return request({ url: '/after-sale/handle', method: 'post', data })
}

/** ==================== 评价模块 ==================== */

/**
 * 获取评价列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword, goodsId }
 * @returns {Promise} 评价列表数据
 */
export function getReviewList(params) {
  return request({ url: '/review/list', method: 'get', params })
}

/**
 * 删除评价
 * @param {number} id - 评价ID
 * @returns {Promise}
 */
export function deleteReview(id) {
  return request({ url: '/review/delete/' + id, method: 'post' })
}

/** ==================== 商品评价模块 ==================== */

/**
 * 获取商品评价列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword }
 * @returns {Promise} 商品评价列表数据
 */
export function getGoodsCommentList(params) {
  return request({ url: '/goods-comment/listByAdmin', method: 'get', params })
}

/**
 * 删除商品评价
 * @param {number} id - 评价ID
 * @returns {Promise}
 */
export function deleteGoodsComment(id) {
  return request({ url: '/goods-comment/deleteByAdmin', method: 'delete', params: { id } })
}

/**
 * 锁定商品评价
 * @param {Object} data - { commentId }
 * @returns {Promise}
 */
export function lockGoodsComment(data) {
  return request({ url: '/goods-comment/lockComment', method: 'post', data })
}

/**
 * 解锁商品评价
 * @param {Object} data - { commentId }
 * @returns {Promise}
 */
export function unlockGoodsComment(data) {
  return request({ url: '/goods-comment/unlockComment', method: 'post', data })
}

/**
 * 获取商品评价总数
 * @returns {Promise} 商品评价总数
 */
export function getGoodsCommentTotal() {
  return request({ url: '/goods-comment/getCommentTotal', method: 'get' })
}

/** ==================== 订单评价模块 ==================== */

/**
 * 获取订单评价列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword }
 * @returns {Promise} 订单评价列表数据
 */
export function getOrderReviewList(params) {
  return request({ url: '/order-review/list', method: 'get', params })
}

/**
 * 获取订单评价总数
 * @returns {Promise} 订单评价总数
 */
export function getOrderReviewTotal() {
  return request({ url: '/order-review/getReviewTotal', method: 'get' })
}

/** ==================== 物流模块 ==================== */

/**
 * 获取物流列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword }
 * @returns {Promise} 物流列表数据
 */
export function getLogisticsList(params) {
  return request({ url: '/logistics/list', method: 'get', params })
}

/**
 * 更新物流信息
 * @param {Object} data - 物流信息（含id）
 * @returns {Promise}
 */
export function updateLogistics(data) {
  return request({ url: '/logistics/update', method: 'post', data })
}

/**
 * 创建物流记录
 * @param {Object} data - 物流信息
 * @returns {Promise}
 */
export function createLogistics(data) {
  return request({ url: '/logistics/create', method: 'post', data })
}

/**
 * 根据订单ID获取物流信息
 * @param {string} orderId - 订单ID
 * @returns {Promise} 物流信息
 */
export function getLogisticsByOrderId(orderId) {
  return request({ url: '/logistics/getByOrderId/' + orderId, method: 'get' })
}

/** ==================== 公告模块 ==================== */

/**
 * 获取公告列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword }
 * @returns {Promise} 公告列表数据
 */
export function getAnnouncementList(params) {
  return request({ url: '/announcement/list', method: 'get', params })
}

/**
 * 新增公告
 * @param {Object} data - 公告信息 { title, content, type }
 * @returns {Promise}
 */
export function addAnnouncement(data) {
  return request({ url: '/announcement/add', method: 'post', data })
}

/**
 * 发布公告
 * @param {Object} data - 公告信息
 * @returns {Promise}
 */
export function publishAnnouncement(data) {
  return request({ url: '/announcement/publish', method: 'post', data })
}

/**
 * 修改公告
 * @param {Object} data - 公告信息（含id）
 * @returns {Promise}
 */
export function updateAnnouncement(data) {
  return request({ url: '/announcement/update', method: 'post', data })
}

/**
 * 删除公告
 * @param {number} id - 公告ID
 * @returns {Promise}
 */
export function deleteAnnouncement(id) {
  return request({ url: '/announcement/delete/' + id, method: 'post' })
}

/** ==================== 举报模块 ==================== */

/**
 * 获取举报列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword, status }
 * @returns {Promise} 举报列表数据
 */
export function getReportList(params) {
  return request({ url: '/report/list', method: 'get', params })
}

/**
 * 处理举报（通过/驳回）
 * @param {Object} data - { id, status, reply }
 * @returns {Promise}
 */
export function handleReport(data) {
  return request({ url: '/report/handle', method: 'post', data })
}

/** ==================== 处罚模块 ==================== */

/**
 * 获取处罚列表（分页）
 * @param {Object} params - 查询参数 { page, size, keyword }
 * @returns {Promise} 处罚列表数据
 */
export function getPunishmentList(params) {
  return request({ url: '/punishment/list', method: 'get', params })
}

/**
 * 新增处罚记录
 * @param {Object} data - 处罚信息 { userId, type, reason, duration }
 * @returns {Promise}
 */
export function addPunishment(data) {
  return request({ url: '/punishment/add', method: 'post', data })
}

/**
 * 创建处罚记录
 * @param {Object} data - 处罚信息
 * @returns {Promise}
 */
export function createPunishment(data) {
  return request({ url: '/punishment/create', method: 'post', data })
}

/**
 * 解除处罚
 * @param {Object} data - { id }
 * @returns {Promise}
 */
export function revokePunishment(data) {
  return request({ url: '/punishment/revoke', method: 'post', data })
}

/**
 * 解除/撤销处罚
 * @param {string} punishmentId - 处罚ID
 * @returns {Promise}
 */
export function liftPunishment(punishmentId) {
  return request({ url: '/punishment/lift/' + punishmentId, method: 'post' })
}

/**
 * 处理申诉
 * @param {Object} data - 申诉处理信息
 * @returns {Promise}
 */
export function handleAppeal(data) {
  return request({ url: '/punishment/handleAppeal', method: 'post', data })
}

/** ==================== 仪表盘模块 ==================== */

/**
 * 获取待审核数量统计（红点提醒）
 * @returns {Promise} 待审核帖子、商品、表白墙、举报数量
 */
export function getPendingCount() {
  return request({ url: '/stats/pendingCount', method: 'get' })
}

/**
 * 获取仪表盘统计数据
 * @returns {Promise} 各模块的统计数据
 */
export function getDashboardData() {
  return request({ url: '/dashboard/data', method: 'get' })
}

/** ==================== AI配置模块 ==================== */

/**
 * 获取AI配置信息
 * @returns {Promise} AI配置数据
 */
export function getAiConfig() {
  return request({ url: '/ai-config/get', method: 'get' })
}

/**
 * 获取AI配置列表
 * @param {Object} params - 查询参数
 * @returns {Promise} AI配置列表数据
 */
export function getAiConfigList(params) {
  return request({ url: '/ai-config/list', method: 'get', params })
}

/**
 * 新增AI配置
 * @param {Object} data - AI配置信息
 * @returns {Promise}
 */
export function addAiConfig(data) {
  return request({ url: '/ai-config/add', method: 'post', data })
}

/**
 * 更新AI配置
 * @param {Object} data - AI配置信息
 * @returns {Promise}
 */
export function updateAiConfig(data) {
  return request({ url: '/ai-config/update', method: 'post', data })
}

/**
 * 删除AI配置
 * @param {number} id - AI配置ID
 * @returns {Promise}
 */
export function deleteAiConfig(id) {
  return request({ url: '/ai-config/' + id, method: 'delete' })
}

/**
 * 切换AI配置启用状态
 * @param {number} id - AI配置ID
 * @returns {Promise}
 */
export function toggleAiConfigEnabled(id) {
  return request({ url: '/ai-config/toggle/' + id, method: 'post' })
}

/**
 * 测试AI连接
 * @param {Object} data - 测试参数
 * @returns {Promise}
 */
export function testAiConnection(data) {
  return request({ url: '/ai/test', method: 'post', data })
}
