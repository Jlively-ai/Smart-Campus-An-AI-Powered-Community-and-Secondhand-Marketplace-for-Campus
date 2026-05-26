/**
 * 路由配置文件
 * 功能描述：定义校园综合平台的前端路由规则，包括登录、注册、主页及其子路由
 * 主要职责：
 *   1. 配置所有页面路由及懒加载
 *   2. 处理路由重复导航错误
 *   3. 实现路由守卫（未登录跳转到登录页）
 */
import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

/** 路由配置表 */
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    redirect: '/dashboard',
    /** Home的子路由，所有业务页面都嵌套在Home布局中 */
    children: [
      { path: '/dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '首页' } },
      { path: '/postList', name: 'PostList', component: () => import('@/views/post/PostList.vue'), meta: { title: '帖子列表' } },
      { path: '/postDetail/:id', name: 'PostDetail', component: () => import('@/views/post/PostDetail.vue'), meta: { title: '帖子详情' } },
      { path: '/postPublish', name: 'PostPublish', component: () => import('@/views/post/PostPublish.vue'), meta: { title: '发帖' } },
      { path: '/wallList', name: 'WallList', component: () => import('@/views/wall/WallList.vue'), meta: { title: '表白墙' } },
      { path: '/wallApply', name: 'WallApply', component: () => import('@/views/wall/WallApply.vue'), meta: { title: '申请表白墙' } },
      { path: '/goodsList', name: 'GoodsList', component: () => import('@/views/goods/GoodsList.vue'), meta: { title: '商品列表' } },
      { path: '/goodsDetail/:id', name: 'GoodsDetail', component: () => import('@/views/goods/GoodsDetail.vue'), meta: { title: '商品详情' } },
      { path: '/goodsPublish', name: 'GoodsPublish', component: () => import('@/views/goods/GoodsPublish.vue'), meta: { title: '发布商品' } },
      { path: '/myPost', name: 'MyPost', component: () => import('@/views/user/MyPost.vue'), meta: { title: '我的帖子' } },
      { path: '/myComment', name: 'MyComment', component: () => import('@/views/user/MyComment.vue'), meta: { title: '我的评论' } },
      { path: '/myGoods', name: 'MyGoods', component: () => import('@/views/user/MyGoods.vue'), meta: { title: '我的闲置' } },
      { path: '/myOrder', name: 'MyOrder', component: () => import('@/views/user/MyOrder.vue'), meta: { title: '我的订单' } },
      { path: '/cart', name: 'Cart', component: () => import('@/views/goods/Cart.vue'), meta: { title: '购物车' } },
      { path: '/mySaleOrder', name: 'MySaleOrder', component: () => import('@/views/user/MySaleOrder.vue'), meta: { title: '销售订单' } },
      { path: '/myWall', name: 'MyWall', component: () => import('@/views/user/MyWall.vue'), meta: { title: '我的表白墙' } },
      { path: '/myInfo', name: 'MyInfo', component: () => import('@/views/user/MyInfo.vue'), meta: { title: '个人信息' } },
      { path: '/myLetter', name: 'MyLetter', component: () => import('@/views/user/MyLetter.vue'), meta: { title: '我的消息' } },
      { path: '/announcementList', name: 'AnnouncementList', component: () => import('@/views/announcement/AnnouncementList.vue'), meta: { title: '公告' } },
      { path: '/statsPage', name: 'StatsPage', component: () => import('@/views/StatsPage.vue'), meta: { title: '数据统计' } },
      { path: '/myFollowing', name: 'MyFollowing', component: () => import('@/views/user/MyFollowing.vue'), meta: { title: '我的关注' } },
      { path: '/myFollowers', name: 'MyFollowers', component: () => import('@/views/user/MyFollowers.vue'), meta: { title: '我的粉丝' } },
      { path: '/userProfile/:id', name: 'UserProfile', component: () => import('@/views/user/UserProfile.vue'), meta: { title: '用户主页' } },
      { path: '/wallDetail/:id', name: 'WallDetail', component: () => import('@/views/wall/WallDetail.vue'), meta: { title: '表白墙详情' } },
      { path: '/orderDetail/:id', name: 'OrderDetail', component: () => import('@/views/user/OrderDetail.vue'), meta: { title: '订单详情' } },
      { path: '/search', name: 'SearchResults', component: () => import('@/views/search/SearchResults.vue'), meta: { title: '搜索结果' } },
      { path: '/securityCenter', name: 'SecurityCenter', component: () => import('@/views/user/SecurityCenter.vue'), meta: { title: '安全中心' } },
      { path: '/recycleBin', name: 'RecycleBin', component: () => import('@/views/user/RecycleBin.vue'), meta: { title: '回收站' } },
      { path: '/myPunishment', name: 'MyPunishment', component: () => import('@/views/user/MyPunishment.vue'), meta: { title: '处罚管理' } }
    ]
  }
]

const router = new VueRouter({
  routes
})

/** 重写Vue Router的push方法，忽略重复导航错误 */
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => {
    if (err.name !== 'NavigationDuplicated' && !err.message.includes('Navigation cancelled')) {
      return Promise.reject(err)
    }
  })
}

/** 重写Vue Router的replace方法，忽略重复导航错误 */
const originalReplace = VueRouter.prototype.replace
VueRouter.prototype.replace = function replace(location) {
  return originalReplace.call(this, location).catch(err => {
    if (err.name !== 'NavigationDuplicated' && !err.message.includes('Navigation cancelled')) {
      return Promise.reject(err)
    }
  })
}

/**
 * 全局路由守卫
 * 逻辑：登录页和注册页直接放行，其他页面需检查token，未登录则跳转到登录页
 */
router.beforeEach((to, from, next) => {
  if (to.path === '/login' || to.path === '/register') {
    next()
  } else {
    const token = window.sessionStorage.getItem('token')
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router
