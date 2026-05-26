<!--
  组件名：LayoutIndex
  功能描述：管理后台主布局组件
  主要职责：
    1. 渲染侧边栏导航菜单（系统管理、社区管理、交易管理、安全管理、AI管理）
    2. 渲染顶部导航栏（页面标题、全局搜索、用户信息下拉）
    3. 渲染主内容区域和底部版权信息
    4. 管理待审核数量徽标和全局搜索功能
-->
<template>
  <div class="app-wrapper">
    <!-- ===== 侧边栏区域 ===== -->
    <div class="sidebar-container">
      <!-- Logo区域，点击跳转首页 -->
      <div class="sidebar-logo" @click="$router.push('/dashboard')">
        <div class="logo-icon"><i class="el-icon-s-platform"></i></div>
        <div class="logo-text">
          <div class="logo-title">智联校园</div>
          <div class="logo-subtitle">Admin Panel</div>
        </div>
      </div>
      <!-- 侧边栏分隔线 -->
      <div class="sidebar-divider"></div>
      <!-- 导航菜单，使用router模式自动跳转 -->
      <el-menu :default-active="$route.path" background-color="transparent" text-color="rgba(255,255,255,0.75)" active-text-color="#fff" router :unique-opened="true">
        <!-- 首页菜单项 -->
        <el-menu-item index="/dashboard"><i class="el-icon-s-home"></i><span>首页</span></el-menu-item>

        <!-- 系统管理子菜单 -->
        <el-submenu index="system">
          <template slot="title"><i class="el-icon-s-tools"></i><span>系统管理</span></template>
          <el-menu-item v-if="hasPerm('user_manage')" index="/user/list"><i class="el-icon-user"></i><span>用户管理</span></el-menu-item>
          <el-menu-item v-if="hasPerm('user_manage')" index="/admin/list"><i class="el-icon-s-custom"></i><span>管理员管理</span></el-menu-item>
          <el-menu-item v-if="hasPerm('announcement_manage')" index="/announcement/list"><i class="el-icon-bell"></i><span>公告管理</span></el-menu-item>
        </el-submenu>

        <!-- 社区管理子菜单，带待审核徽标 -->
        <el-submenu index="community">
          <template slot="title"><i class="el-icon-s-comment"></i><span>社区管理</span><sup v-if="pendingCounts.pendingPostAudit > 0 || pendingCounts.pendingWallAudit > 0" class="sidebar-badge"></sup></template>
          <!-- 帖子管理，显示待审核数量 -->
          <el-menu-item v-if="hasPerm('post_manage')" index="/post/list">
            <i class="el-icon-document"></i><span>帖子管理</span>
            <sup v-if="pendingCounts.pendingPostAudit > 0" class="menu-badge">{{ pendingCounts.pendingPostAudit > 99 ? '99+' : pendingCounts.pendingPostAudit }}</sup>
          </el-menu-item>
          <el-menu-item v-if="hasPerm('post_manage')" index="/post-category/list"><i class="el-icon-collection-tag"></i><span>帖子分类</span></el-menu-item>
          <el-menu-item v-if="hasPerm('comment_manage')" index="/comment/list"><i class="el-icon-chat-dot-round"></i><span>评论管理</span></el-menu-item>
          <!-- 表白墙管理，显示待审核数量 -->
          <el-menu-item v-if="hasPerm('wall_manage')" index="/wall/list">
            <i class="el-icon-star-off"></i><span>表白墙管理</span>
            <sup v-if="pendingCounts.pendingWallAudit > 0" class="menu-badge">{{ pendingCounts.pendingWallAudit > 99 ? '99+' : pendingCounts.pendingWallAudit }}</sup>
          </el-menu-item>
        </el-submenu>

        <!-- 交易管理子菜单，带待审核徽标 -->
        <el-submenu index="trade">
          <template slot="title"><i class="el-icon-s-shop"></i><span>交易管理</span><sup v-if="pendingCounts.pendingGoodsAudit > 0" class="sidebar-badge"></sup></template>
          <!-- 商品管理，显示待审核数量 -->
          <el-menu-item v-if="hasPerm('goods_manage')" index="/goods/list">
            <i class="el-icon-goods"></i><span>商品管理</span>
            <sup v-if="pendingCounts.pendingGoodsAudit > 0" class="menu-badge">{{ pendingCounts.pendingGoodsAudit > 99 ? '99+' : pendingCounts.pendingGoodsAudit }}</sup>
          </el-menu-item>
          <el-menu-item v-if="hasPerm('goods_manage')" index="/goods-category/list"><i class="el-icon-menu"></i><span>商品类型</span></el-menu-item>
          <el-menu-item v-if="hasPerm('order_manage')" index="/order/list"><i class="el-icon-s-order"></i><span>订单管理</span></el-menu-item>
          <el-menu-item v-if="hasPerm('order_manage')" index="/after-sale/list"><i class="el-icon-service"></i><span>售后管理</span></el-menu-item>
          <el-menu-item v-if="hasPerm('order_manage')" index="/review/list"><i class="el-icon-star-on"></i><span>评价管理</span></el-menu-item>
          <el-menu-item v-if="hasPerm('order_manage')" index="/logistics/index"><i class="el-icon-truck"></i><span>物流管理</span></el-menu-item>
        </el-submenu>

        <!-- 安全管理子菜单，带待处理举报徽标 -->
        <el-submenu index="safety">
          <template slot="title"><i class="el-icon-s-check"></i><span>安全管理</span><sup v-if="pendingCounts.pendingReport > 0" class="sidebar-badge"></sup></template>
          <!-- 举报管理，显示待处理数量 -->
          <el-menu-item v-if="hasPerm('report_manage')" index="/report/list">
            <i class="el-icon-warning-outline"></i><span>举报管理</span>
            <sup v-if="pendingCounts.pendingReport > 0" class="menu-badge">{{ pendingCounts.pendingReport > 99 ? '99+' : pendingCounts.pendingReport }}</sup>
          </el-menu-item>
          <el-menu-item v-if="hasPerm('punishment_manage')" index="/punishment/list"><i class="el-icon-s-flag"></i><span>处罚管理</span></el-menu-item>
          <el-menu-item v-if="hasPerm('appeal_manage') || hasPerm('violation_manage')" index="/violation/list"><i class="el-icon-delete"></i><span>违规管理</span></el-menu-item>
        </el-submenu>

        <!-- AI管理子菜单 -->
        <el-submenu index="ai">
          <template slot="title"><i class="el-icon-cpu"></i><span>AI管理</span></template>
          <el-menu-item v-if="hasPerm('ai_manage')" index="/ai-config/list"><i class="el-icon-setting"></i><span>模型配置</span></el-menu-item>
        </el-submenu>
      </el-menu>
    </div>

    <!-- ===== 主内容区域 ===== -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <!-- 当前页面标题 -->
        <span style="font-size:16px;font-weight:bold;">{{ $route.meta.title || '后台管理' }}</span>
        <!-- 全局搜索区域 -->
        <div class="navbar-center">
          <el-popover v-model="searchVisible" placement="bottom" width="520" trigger="manual" popper-class="global-search-popover">
            <!-- 搜索结果列表 -->
            <div class="global-search-results" v-loading="searchLoading">
              <!-- 无结果提示 -->
              <div v-if="searchKeyword && searchResults.length === 0 && !searchLoading" class="search-empty">
                <i class="el-icon-search" style="font-size:32px;color:#dcdfe6;"></i>
                <p>未找到相关结果</p>
              </div>
              <!-- 初始提示 -->
              <div v-else-if="!searchKeyword" class="search-empty">
                <i class="el-icon-search" style="font-size:32px;color:#dcdfe6;"></i>
                <p>输入关键词搜索用户、帖子、商品、表白墙等内容</p>
              </div>
              <!-- 搜索结果分组展示 -->
              <div v-else>
                <div v-for="group in searchResults" :key="group.type" class="search-group">
                  <div class="search-group-title"><i :class="group.icon"></i>{{ group.label }}</div>
                  <div v-for="item in group.items" :key="item.id" class="search-result-item" @click="handleSearchItemClick(group.type, item)">
                    <div class="search-item-main">
                      <span class="search-item-name">{{ item.name || item.title || item.username || item.content }}</span>
                      <el-tag size="mini" :type="group.tagType" style="margin-left:8px;">{{ group.label }}</el-tag>
                    </div>
                    <div class="search-item-sub">{{ item.sub }}</div>
                  </div>
                </div>
              </div>
            </div>
            <!-- 搜索输入框 -->
            <div slot="reference" class="global-search-wrapper">
              <el-input v-model="searchKeyword" placeholder="全局搜索..." prefix-icon="el-icon-search" size="medium" clearable @input="handleSearchInput" @focus="searchVisible = true" @clear="searchResults = []; searchVisible = false" class="global-search-input" />
            </div>
          </el-popover>
        </div>
        <!-- 右侧用户信息下拉 -->
        <div class="navbar-right">
          <el-dropdown @command="handleCommand">
            <div style="display:flex;align-items:center;cursor:pointer;">
              <el-avatar :size="30" style="margin-right:8px;background:#409EFF;">{{ (name || '管')[0] }}</el-avatar>
              <span style="color:#333;">{{ name }}</span>
              <i class="el-icon-arrow-down" style="margin-left:4px;"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
      <!-- 主内容区域，渲染子路由 -->
      <div class="app-main">
        <router-view />
        <!-- 底部版权信息 -->
        <div class="site-footer">
          <div class="footer-links">
            <a href="javascript:;">关于智联校园</a><span class="sep">|</span>
            <a href="javascript:;">帮助中心</a><span class="sep">|</span>
            <a href="javascript:;">网站地图</a><span class="sep">|</span>
            <a href="javascript:;">商务合作</a><span class="sep">|</span>
            <a href="javascript:;">联系我们</a><span class="sep">|</span>
            <a href="javascript:;">家长监护&举报</a><span class="sep">|</span>
            <a href="javascript:;">隐私政策</a>
          </div>
          <div class="footer-info">
            <span>豫ICP备XXXXXXXX号-1</span><span class="sep">|</span>
            <span>豫公网安备XXXXXXXXXXXXXX号</span><span class="sep">|</span>
            <span>增值电信业务经营许可证：豫B2-XXXXXXXX</span><span class="sep">|</span>
            <span>违法和不良信息举报：support@smartcampus.edu.cn</span>
          </div>
          <div class="footer-copyright">
            Copyright &copy;2025 新乡市创智数联信息技术有限责任公司 All Rights Reserved
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getPendingCount, getUserList, getPostList, getGoodsList, getWallList, getOrderList, getAnnouncementList, getReportList, getAdminList } from '@/api/manage'

export default {
  data() {
    return {
      /** 待审核数量统计对象 */
      pendingCounts: { pendingPostAudit: 0, pendingGoodsAudit: 0, pendingWallAudit: 0, pendingReport: 0 },
      /** 待审核数量轮询定时器 */
      pendingTimer: null,
      /** 全局搜索关键词 */
      searchKeyword: '',
      /** 全局搜索弹窗是否可见 */
      searchVisible: false,
      /** 全局搜索加载状态 */
      searchLoading: false,
      /** 全局搜索结果列表 */
      searchResults: [],
      /** 搜索防抖定时器 */
      searchTimer: null
    }
  },
  computed: {
    /** 从Vuex获取管理员名称、角色和权限 */
    ...mapGetters(['name', 'roles', 'permissions']),
    /** 判断是否为超级管理员 */
    isSuper() { return this.roles.includes('super') }
  },
  /** 生命周期：组件挂载后启动待审核数量轮询和全局点击监听 */
  mounted() {
    this.fetchPendingCount()
    this.pendingTimer = setInterval(() => { this.fetchPendingCount() }, 60000)
    document.addEventListener('click', this.handleDocClick)
  },
  /** 生命周期：组件销毁前清除定时器和事件监听 */
  beforeDestroy() {
    if (this.pendingTimer) clearInterval(this.pendingTimer)
    document.removeEventListener('click', this.handleDocClick)
  },
  methods: {
    /**
     * 权限判断方法
     * @param {string} perm - 权限标识
     * @returns {boolean} 是否拥有该权限
     */
    hasPerm(perm) {
      if (this.isSuper) return true
      if (!this.permissions || this.permissions === '') return true
      return this.permissions.split(',').includes(perm)
    },
    /**
     * 处理下拉菜单命令
     * @param {string} cmd - 命令标识（如'logout'）
     */
    handleCommand(cmd) {
      if (cmd === 'logout') { this.$store.dispatch('user/logout'); this.$router.push('/login') }
    },
    /**
     * 获取待审核数量统计
     * 定时轮询调用，更新侧边栏徽标数字
     */
    async fetchPendingCount() {
      try {
        var res = await getPendingCount()
        if (res.code === 200) {
          this.pendingCounts = res.data || {}
        }
      } catch (e) {}
    },
    /**
     * 全局点击事件处理
     * 点击搜索框和弹窗外部时关闭搜索弹窗
     * @param {Event} e - 点击事件对象
     */
    handleDocClick(e) {
      if (!e.target.closest('.global-search-wrapper') && !e.target.closest('.global-search-popover')) {
        this.searchVisible = false
      }
    },
    /**
     * 搜索输入处理
     * 防抖500ms后执行搜索，空关键词时清空结果
     */
    handleSearchInput() {
      if (this.searchTimer) clearTimeout(this.searchTimer)
      if (!this.searchKeyword || this.searchKeyword.trim().length < 1) {
        this.searchResults = []
        this.searchVisible = false
        return
      }
      this.searchVisible = true
      this.searchTimer = setTimeout(() => { this.doSearch() }, 500)
    },
    /**
     * 执行全局搜索
     * 并行搜索用户、帖子、商品、表白墙、订单、公告、举报、管理员
     */
    async doSearch() {
      var kw = this.searchKeyword.trim().toLowerCase()
      if (!kw) return
      this.searchLoading = true
      var results = []
      try {
        var promises = [
          this.searchUsers(kw),
          this.searchPosts(kw),
          this.searchGoods(kw),
          this.searchWalls(kw),
          this.searchOrders(kw),
          this.searchAnnouncements(kw),
          this.searchReports(kw),
          this.searchAdmins(kw)
        ]
        var res = await Promise.allSettled(promises)
        res.forEach(function(r) { if (r.status === 'fulfilled' && r.value && r.value.items && r.value.items.length > 0) results.push(r.value) })
      } catch (e) {}
      this.searchResults = results
      this.searchLoading = false
    },
    /** 搜索用户，最多返回5条匹配结果 */
    async searchUsers(kw) {
      try {
        var res = await getUserList({ pageNum: 1, pageSize: 20, username: kw })
        var records = (res.data && res.data.records) || []
        var items = records.filter(function(u) { return (u.username || '').toLowerCase().includes(kw) || (u.userId || '').toString().includes(kw) }).slice(0, 5).map(function(u) {
          return { id: u.userId, username: u.username, name: u.username || u.userId, sub: 'ID: ' + u.userId + (u.email ? ' | ' + u.email : '') }
        })
        return { type: 'user', label: '用户', icon: 'el-icon-user', tagType: 'info', items: items }
      } catch (e) { return { type: 'user', label: '用户', icon: 'el-icon-user', tagType: 'info', items: [] } }
    },
    /** 搜索帖子，最多返回5条匹配结果 */
    async searchPosts(kw) {
      try {
        var res = await getPostList({ pageNum: 1, pageSize: 20, title: kw })
        var records = (res.data && res.data.records) || []
        var items = records.filter(function(p) { return (p.title || '').toLowerCase().includes(kw) || (p.postId || '').toString().includes(kw) || (p.username || '').toLowerCase().includes(kw) }).slice(0, 5).map(function(p) {
          return { id: p.postId, title: p.title, name: p.title, sub: '作者: ' + (p.username || '-') + ' | ID: ' + p.postId }
        })
        return { type: 'post', label: '帖子', icon: 'el-icon-document', tagType: 'primary', items: items }
      } catch (e) { return { type: 'post', label: '帖子', icon: 'el-icon-document', tagType: 'primary', items: [] } }
    },
    /** 搜索商品，最多返回5条匹配结果 */
    async searchGoods(kw) {
      try {
        var res = await getGoodsList({ pageNum: 1, pageSize: 20, goodsName: kw })
        var records = (res.data && res.data.records) || []
        var items = records.filter(function(g) { return (g.goodsName || '').toLowerCase().includes(kw) || (g.goodsId || '').toString().includes(kw) }).slice(0, 5).map(function(g) {
          return { id: g.goodsId, title: g.goodsName, name: g.goodsName, sub: '价格: ¥' + (g.price || 0) + ' | ID: ' + g.goodsId }
        })
        return { type: 'goods', label: '商品', icon: 'el-icon-goods', tagType: 'warning', items: items }
      } catch (e) { return { type: 'goods', label: '商品', icon: 'el-icon-goods', tagType: 'warning', items: [] } }
    },
    /** 搜索表白墙，最多返回5条匹配结果 */
    async searchWalls(kw) {
      try {
        var res = await getWallList({ pageNum: 1, pageSize: 20 })
        var records = (res.data && res.data.records) || []
        var items = records.filter(function(w) { return (w.content || '').toLowerCase().includes(kw) || (w.wallId || '').toString().includes(kw) || (w.username || '').toLowerCase().includes(kw) }).slice(0, 5).map(function(w) {
          var content = (w.content || '').substring(0, 40)
          return { id: w.wallId, content: content, name: content, sub: '作者: ' + (w.username || '-') + ' | ID: ' + w.wallId }
        })
        return { type: 'wall', label: '表白墙', icon: 'el-icon-star-off', tagType: 'danger', items: items }
      } catch (e) { return { type: 'wall', label: '表白墙', icon: 'el-icon-star-off', tagType: 'danger', items: [] } }
    },
    /** 搜索订单，最多返回5条匹配结果 */
    async searchOrders(kw) {
      try {
        var res = await getOrderList({ pageNum: 1, pageSize: 20, orderId: kw })
        var records = (res.data && res.data.records) || []
        var items = records.filter(function(o) { return (o.orderId || '').toString().includes(kw) }).slice(0, 5).map(function(o) {
          return { id: o.orderId, name: '订单 ' + o.orderId, sub: '金额: ¥' + (o.totalAmount || 0) + ' | 状态: ' + (o.orderStatus || '-') }
        })
        return { type: 'order', label: '订单', icon: 'el-icon-s-order', tagType: 'success', items: items }
      } catch (e) { return { type: 'order', label: '订单', icon: 'el-icon-s-order', tagType: 'success', items: [] } }
    },
    /** 搜索公告，最多返回5条匹配结果 */
    async searchAnnouncements(kw) {
      try {
        var res = await getAnnouncementList({ pageNum: 1, pageSize: 20, title: kw })
        var records = (res.data && res.data.records) || []
        var items = records.filter(function(a) { return (a.title || '').toLowerCase().includes(kw) || (a.announcementId || '').toString().includes(kw) }).slice(0, 5).map(function(a) {
          return { id: a.announcementId, title: a.title, name: a.title, sub: 'ID: ' + a.announcementId }
        })
        return { type: 'announcement', label: '公告', icon: 'el-icon-bell', tagType: 'info', items: items }
      } catch (e) { return { type: 'announcement', label: '公告', icon: 'el-icon-bell', tagType: 'info', items: [] } }
    },
    /** 搜索举报，最多返回5条匹配结果 */
    async searchReports(kw) {
      try {
        var res = await getReportList({ pageNum: 1, pageSize: 20 })
        var records = (res.data && res.data.records) || []
        var items = records.filter(function(r) { return (r.reportId || '').toString().includes(kw) || (r.targetId || '').toString().includes(kw) }).slice(0, 5).map(function(r) {
          return { id: r.reportId, name: '举报 #' + r.reportId, sub: '目标: ' + (r.targetType || '-') + ' #' + (r.targetId || '-') }
        })
        return { type: 'report', label: '举报', icon: 'el-icon-warning-outline', tagType: 'danger', items: items }
      } catch (e) { return { type: 'report', label: '举报', icon: 'el-icon-warning-outline', tagType: 'danger', items: [] } }
    },
    /** 搜索管理员，最多返回5条匹配结果 */
    async searchAdmins(kw) {
      try {
        var res = await getAdminList({ pageNum: 1, pageSize: 20, username: kw })
        var records = (res.data && res.data.records) || []
        var items = records.filter(function(a) { return (a.username || '').toLowerCase().includes(kw) || (a.adminId || '').toString().includes(kw) }).slice(0, 5).map(function(a) {
          return { id: a.adminId, username: a.username, name: a.username, sub: 'ID: ' + a.adminId + ' | 角色: ' + (a.roleName || '-') }
        })
        return { type: 'admin', label: '管理员', icon: 'el-icon-s-custom', tagType: '', items: items }
      } catch (e) { return { type: 'admin', label: '管理员', icon: 'el-icon-s-custom', tagType: '', items: [] } }
    },
    /**
     * 搜索结果点击处理
     * 根据搜索类型跳转到对应管理页面
     * @param {string} type - 搜索结果类型
     * @param {Object} item - 搜索结果项
     */
    handleSearchItemClick(type, item) {
      this.searchVisible = false
      var routeMap = {
        user: '/user/list',
        post: '/post/list',
        goods: '/goods/list',
        wall: '/wall/list',
        order: '/order/list',
        announcement: '/announcement/list',
        report: '/report/list',
        admin: '/admin/list'
      }
      var route = routeMap[type]
      if (route) {
        this.$router.push(route).catch(function() {})
      }
    }
  }
}
</script>

<!-- ===== 组件局部样式 ===== -->
<style scoped>
/* 整体布局：左右分栏 */
.app-wrapper { display: flex; height: 100vh; }
/* 侧边栏容器：固定宽度、深色渐变背景 */
.sidebar-container {
  width: 240px;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 50%, #1e1b4b 100%);
  overflow-y: auto;
  box-shadow: 4px 0 24px rgba(0,0,0,0.15);
  position: relative;
  flex-shrink: 0;
}
/* 侧边栏纹理背景 */
.sidebar-container::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23409eff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.6;
  pointer-events: none;
}
/* Logo区域样式 */
.sidebar-logo {
  height: 80px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: rgba(255,255,255,0.04);
  border-bottom: 1px solid rgba(255,255,255,0.06);
  position: relative;
  z-index: 1;
  cursor: pointer;
}
/* Logo图标样式 */
.logo-icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: linear-gradient(135deg, #409EFF, #667eea);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  box-shadow: 0 4px 15px rgba(64,158,255,0.35);
  transition: all 0.3s ease;
}
.logo-icon i { color: #fff; font-size: 22px; }
/* Logo悬浮动效 */
.sidebar-logo:hover .logo-icon { transform: rotate(-5deg) scale(1.05); box-shadow: 0 6px 20px rgba(64,158,255,0.5); }
/* Logo文字样式 */
.logo-text { display: flex; flex-direction: column; }
.logo-title { color: #fff; font-size: 18px; font-weight: 700; letter-spacing: 1px; line-height: 1.3; }
.logo-subtitle { color: rgba(255,255,255,0.45); font-size: 10px; font-weight: 500; letter-spacing: 2px; text-transform: uppercase; margin-top: 2px; }
/* 侧边栏分隔线 */
.sidebar-divider { height: 1px; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent); margin: 8px 16px; position: relative; z-index: 1; }
/* 主内容容器 */
.main-container { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
/* 顶部导航栏：毛玻璃效果 */
.navbar {
  height: 64px;
  line-height: 64px;
  padding: 0 28px;
  background: rgba(255,255,255,0.75);
  backdrop-filter: blur(24px) saturate(200%);
  -webkit-backdrop-filter: blur(24px) saturate(200%);
  border-bottom: 1px solid rgba(255,255,255,0.6);
  box-shadow: 0 1px 20px rgba(0,0,0,0.04);
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 1;
}
/* 主内容区域 */
.app-main {
  flex: 1;
  padding: 28px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 50%, #e8edf5 100%);
  overflow-y: auto;
  position: relative;
}
/* 侧边栏待审核徽标：脉冲动画 */
.sidebar-badge {
  position: absolute;
  top: 50%;
  right: 40px;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #F56C6C, #f89898);
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(245,108,108,0.2);
  animation: pulseBadge 2s infinite;
}
@keyframes pulseBadge {
  0%, 100% { box-shadow: 0 0 0 2px rgba(245,108,108,0.2); }
  50% { box-shadow: 0 0 0 5px rgba(245,108,108,0); }
}
/* 菜单项数字徽标 */
.menu-badge {
  position: relative;
  top: -8px;
  left: 2px;
  background: linear-gradient(135deg, #F56C6C, #f89898);
  color: #fff;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 10px;
  line-height: 1.2;
  font-style: normal;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(245,108,108,0.3);
}
/* ===== 底部版权信息 ===== */
.site-footer {
  background: linear-gradient(180deg, #2b2b2b 0%, #1f1f1f 100%);
  color: rgba(255,255,255,0.55);
  font-size: 12px;
  padding: 24px 20px;
  text-align: center;
  line-height: 1.8;
  margin-top: 20px;
}
.footer-links {
  margin-bottom: 8px;
}
.footer-links a {
  color: rgba(255,255,255,0.65);
  text-decoration: none;
  transition: color 0.25s ease;
  margin: 0 4px;
}
.footer-links a:hover { color: #409EFF; }
.footer-links .sep {
  color: rgba(255,255,255,0.15);
  margin: 0 4px;
}
.footer-info span {
  margin: 0 6px;
}
.footer-info .sep {
  color: rgba(255,255,255,0.15);
}
.footer-copyright {
  color: rgba(255,255,255,0.35);
  letter-spacing: 0.5px;
  margin-top: 8px;
}
/* ===== 全局搜索样式 ===== */
.navbar-center {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 0 24px;
}
.global-search-wrapper {
  width: 100%;
  max-width: 420px;
}
/* 搜索输入框样式 */
.global-search-input ::v-deep .el-input__inner {
  border-radius: 20px;
  background: rgba(255,255,255,0.6);
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  transition: all 0.3s ease;
}
.global-search-input ::v-deep .el-input__inner:focus {
  background: #fff;
  border-color: #409EFF;
  box-shadow: 0 0 0 4px rgba(64,158,255,0.1), 0 4px 16px rgba(0,0,0,0.06);
}
/* 右侧用户区域 */
.navbar-right {
  display: flex;
  align-items: center;
}
</style>

<!-- ===== 全局搜索弹窗样式（非scoped） ===== -->
<style>
/* 搜索弹窗容器 */
.global-search-popover {
  border-radius: 16px !important;
  box-shadow: 0 12px 40px rgba(0,0,0,0.12) !important;
  border: 1px solid #f1f5f9 !important;
  padding: 0 !important;
  margin-top: 8px !important;
}
/* 搜索结果列表 */
.global-search-results {
  max-height: 400px;
  overflow-y: auto;
  padding: 8px;
}
/* 搜索空状态 */
.search-empty {
  text-align: center;
  padding: 32px 16px;
  color: #909399;
}
.search-empty p {
  margin-top: 12px;
  font-size: 13px;
}
/* 搜索结果分组 */
.search-group {
  margin-bottom: 8px;
}
.search-group-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  padding: 8px 12px 4px;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.search-group-title i {
  margin-right: 6px;
}
/* 搜索结果项 */
.search-result-item {
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.search-result-item:hover {
  background: #f0f5ff;
}
.search-item-main {
  display: flex;
  align-items: center;
}
.search-item-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 320px;
}
.search-item-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
