/**
 * 路由配置模块
 * 
 * 文件名：router/index.js
 * 功能描述：定义后台管理系统的路由规则和导航守卫
 * 主要职责：
 *   1. 配置所有页面路由（登录页、布局页、各业务模块页）
 *   2. 设置路由导航守卫，实现登录状态校验和权限控制
 */
import Vue from 'vue'
import Router from 'vue-router'
import { getToken } from '@/utils/auth'

Vue.use(Router)

/**
 * 常量路由表
 * 不需要登录权限即可访问的路由（如登录页）
 */
const constantRoutes = [
  {
    /** 登录页路由 */
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  }
]

/**
 * 异步路由表
 * 需要登录权限才能访问的路由，登录后动态加载
 */
const asyncRoutes = [
  {
    /** 管理后台主布局路由 */
    path: '/',
    component: () => import('@/layout'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index'),
        meta: { title: '仪表盘', icon: 'dashboard' }
      },
      {
        path: 'user/list',
        name: 'User',
        component: () => import('@/views/user/index'),
        meta: { title: '用户管理', icon: 'user' }
      },
      {
        path: 'admin/list',
        name: 'Admin',
        component: () => import('@/views/admin/index'),
        meta: { title: '管理员管理', icon: 'admin' }
      },
      {
        path: 'post/list',
        name: 'Post',
        component: () => import('@/views/post/index'),
        meta: { title: '帖子管理', icon: 'post' }
      },
      {
        path: 'post-category/list',
        name: 'PostCategory',
        component: () => import('@/views/post-category/index'),
        meta: { title: '帖子分类', icon: 'category' }
      },
      {
        path: 'comment/list',
        name: 'Comment',
        component: () => import('@/views/comment/index'),
        meta: { title: '评论管理', icon: 'comment' }
      },
      {
        path: 'wall/list',
        name: 'Wall',
        component: () => import('@/views/wall/index'),
        meta: { title: '表白墙审核', icon: 'wall' }
      },
      {
        path: 'goods/list',
        name: 'Goods',
        component: () => import('@/views/goods/index'),
        meta: { title: '商品管理', icon: 'goods' }
      },
      {
        path: 'goods-category/list',
        name: 'GoodsCategory',
        component: () => import('@/views/goods-category/index'),
        meta: { title: '商品分类', icon: 'category' }
      },
      {
        path: 'order/list',
        name: 'Order',
        component: () => import('@/views/order/index'),
        meta: { title: '订单管理', icon: 'order' }
      },
      {
        path: 'after-sale/list',
        name: 'AfterSale',
        component: () => import('@/views/after-sale/index'),
        meta: { title: '售后管理', icon: 'after-sale' }
      },
      {
        path: 'review/list',
        name: 'Review',
        component: () => import('@/views/review/index'),
        meta: { title: '评价管理', icon: 'review' }
      },
      {
        path: 'logistics/index',
        name: 'Logistics',
        component: () => import('@/views/logistics/index'),
        meta: { title: '物流管理', icon: 'logistics' }
      },
      {
        path: 'announcement/list',
        name: 'Announcement',
        component: () => import('@/views/announcement/index'),
        meta: { title: '公告管理', icon: 'announcement' }
      },
      {
        path: 'report/list',
        name: 'Report',
        component: () => import('@/views/report/index'),
        meta: { title: '举报管理', icon: 'report' }
      },
      {
        path: 'punishment/list',
        name: 'Punishment',
        component: () => import('@/views/punishment/index'),
        meta: { title: '处罚管理', icon: 'punishment' }
      },
      {
        path: 'violation/list',
        name: 'Violation',
        component: () => import('@/views/violation/index'),
        meta: { title: '违规管理', icon: 'violation' }
      },
      {
        path: 'ai-config/list',
        name: 'AiConfig',
        component: () => import('@/views/ai-config/index'),
        meta: { title: 'AI配置', icon: 'ai' }
      }
    ]
  },
  {
    /** 404页面，匹配所有未定义路由 */
    path: '*',
    redirect: '/dashboard',
    hidden: true
  }
]

/** 创建路由实例 */
const createRouter = () => new Router({
  scrollBehavior: () => ({ y: 0 }),
  routes: [...constantRoutes, ...asyncRoutes]
})

const router = createRouter()

/**
 * 路由导航守卫
 * 在每次路由跳转前检查登录状态：
 *   - 访问登录页时，若已登录则跳转首页
 *   - 访问其他页面时，若未登录则跳转登录页
 */
router.beforeEach((to, from, next) => {
  if (to.path === '/login') {
    // 已登录用户访问登录页，直接跳转首页
    if (getToken()) {
      next({ path: '/' })
    } else {
      next()
    }
  } else {
    // 未登录用户访问其他页面，跳转登录页
    if (!getToken()) {
      next(`/login?redirect=${to.path}`)
    } else {
      next()
    }
  }
})

export default router
