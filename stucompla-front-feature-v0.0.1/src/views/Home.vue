<!--
  组件名：Home
  功能描述：首页框架组件，包含侧边栏、顶部导航栏、主内容区
  主要职责：
    1. 侧边栏导航菜单（首页/公告/论坛/交易/表白墙/个人中心）
    2. 顶部导航栏（搜索/通知/暗黑模式/字体大小/用户菜单）
    3. 主内容区（router-view）
    4. AI聊天组件
    5. 公告弹窗
    6. 未读消息/待处理计数
    7. 定时刷新数据（未读消息30s/公告60s/锁定检查30s/待处理60s）
-->
<template>
  <div class="app-wrapper" :class="{ 'dark-mode': isDark }">
    <div class="sidebar-container" :class="{ 'dark-mode': isDark }">
      <div class="sidebar-logo" @click="$router.push('/dashboard').catch(() => {})">
        <div class="logo-icon">
          <i class="el-icon-school"></i>
        </div>
        <div class="logo-text">
          <div class="logo-title">智联校园</div>
          <div class="logo-subtitle">Smart Campus</div>
        </div>
      </div>
      <div class="sidebar-divider"></div>
      <el-menu :default-active="$route.path" background-color="transparent" text-color="rgba(255,255,255,0.75)" active-text-color="#fff" router :unique-opened="true" :collapse="false">
        <el-menu-item index="/dashboard"><i class="el-icon-s-home"></i><span>首页</span></el-menu-item>
        <el-menu-item index="/announcementList">
          <i class="el-icon-bell"></i><span>公告</span>
          <sup v-if="hasNewAnnouncement" class="sidebar-badge"></sup>
        </el-menu-item>
        <el-menu-item v-if="loginType !== 'admin'" index="/statsPage"><i class="el-icon-data-analysis"></i><span>数据统计</span></el-menu-item>
        <el-submenu index="forum">
          <template slot="title"><i class="el-icon-s-comment"></i><span>校园论坛</span></template>
          <el-menu-item index="/postList"><i class="el-icon-document"></i><span>帖子列表</span></el-menu-item>
          <el-menu-item v-if="loginType !== 'admin'" index="/myPost"><i class="el-icon-notebook-2"></i><span>我的帖子</span></el-menu-item>
        </el-submenu>
        <el-submenu index="trade">
          <template slot="title"><i class="el-icon-s-shop"></i><span>二手交易</span></template>
          <el-menu-item index="/goodsList"><i class="el-icon-goods"></i><span>商品列表</span></el-menu-item>
          <el-menu-item v-if="loginType !== 'admin'" index="/myGoods"><i class="el-icon-s-goods"></i><span>我的闲置</span></el-menu-item>
          <el-menu-item v-if="loginType !== 'admin'" index="/cart"><i class="el-icon-shopping-cart-2"></i><span>购物车</span></el-menu-item>
        </el-submenu>
        <el-submenu index="wall">
          <template slot="title"><i class="el-icon-star-off"></i><span>表白墙</span></template>
          <el-menu-item index="/wallList"><i class="el-icon-chat-dot-round"></i><span>表白墙列表</span></el-menu-item>
          <el-menu-item v-if="loginType !== 'admin'" index="/myWall"><i class="el-icon-s-comment"></i><span>我的表白墙</span></el-menu-item>
        </el-submenu>
        <el-submenu index="personal">
          <template slot="title"><i class="el-icon-user"></i><span>个人中心</span><sup v-if="unreadCount > 0 || pendingCounts.unreadPunishment > 0" class="sidebar-badge"></sup></template>
          <el-menu-item index="/myInfo"><i class="el-icon-user-solid"></i><span>个人信息</span></el-menu-item>
          <el-menu-item index="/myLetter">
            <i class="el-icon-message"></i><span>消息中心</span>
            <sup v-if="unreadCount > 0" class="menu-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</sup>
          </el-menu-item>
          <el-menu-item v-if="loginType !== 'admin'" index="/myOrder"><i class="el-icon-s-order"></i><span>我的订单</span></el-menu-item>
          <el-menu-item v-if="loginType !== 'admin'" index="/recycleBin"><i class="el-icon-delete"></i><span>回收站</span></el-menu-item>
          <el-menu-item v-if="loginType !== 'admin'" index="/myPunishment">
            <i class="el-icon-warning-outline"></i><span>处罚管理</span>
            <sup v-if="pendingCounts.unreadPunishment > 0" class="menu-badge">{{ pendingCounts.unreadPunishment > 99 ? '99+' : pendingCounts.unreadPunishment }}</sup>
          </el-menu-item>
        </el-submenu>
      </el-menu>
    </div>
    <div class="main-container">
      <div class="navbar" :class="{ 'dark-mode': isDark }">
        <span style="font-size:16px;font-weight:bold;">{{ $route.meta.title || '智联校园' }}</span>
        <div style="display:flex;align-items:center;">
          <div v-if="marqueeItems.length > 0" class="marquee-wrap">
  <i class="el-icon-bell" style="color:#E6A23C;margin-right:6px;font-size:14px;"></i>
  <div class="marquee-scroll-area">
    <div class="marquee-scroll-content" :class="{ animate: marqueeItems.length > 1 }">
      <div v-for="(item, i) in marqueeItems" :key="i" class="marquee-scroll-item" @click.stop="goToAnnouncement(i)">
        {{ item.title || item.content }}
      </div>
      <div v-if="marqueeItems.length > 1" class="marquee-scroll-item" @click.stop="goToAnnouncement(0)">
        {{ marqueeItems[0].title || marqueeItems[0].content }}
      </div>
    </div>
  </div>
  <el-button type="text" size="mini" class="marquee-more-btn" @click.stop="$router.push('/announcementList').catch(() => {})">更多 &raquo;</el-button>
</div>
          <el-popover placement="bottom" width="350" trigger="click" @show="loadNoticeList">
            <div style="max-height:450px;overflow-y:auto;">
              <div v-if="noticeList.length === 0" style="text-align:center;color:#999;padding:20px 0;">暂无通知</div>
              <div v-for="(item, index) in noticeList" :key="index" class="notice-item" :class="{ unread: item.letterStatus === 0 }" @click="handleNoticeClick(item)">
                <div class="notice-type-icon" :style="{ background: getTypeColor(item.messageType) }">
                  <i :class="getTypeIcon(item.messageType)"></i>
                </div>
                <div class="notice-content">
                  <div class="notice-text">{{ item.letterDetail || item.title || item.content }}</div>
                  <div class="notice-time">{{ formatTime(item.createTime) }}</div>
                </div>
              </div>
            </div>
            <div style="display:flex;justify-content:space-between;align-items:center;padding-top:10px;border-top:1px solid #eee;margin-top:8px;">
              <el-link type="primary" @click="$router.push('/myLetter').catch(() => {})">查看全部消息</el-link>
              <el-link type="info" @click="markAllNoticeRead">全部已读</el-link>
            </div>
            <div slot="reference" style="cursor:pointer;padding:0 15px;position:relative;">
              <i class="el-icon-bell" style="font-size:20px;color:#606266;"></i>
              <sup v-if="unreadCount > 0" class="custom-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</sup>
            </div>
          </el-popover>
          <el-tooltip :content="isDark ? '切换日间模式' : '切换夜间模式'" placement="bottom">
            <i class="el-icon-moon" :class="{ 'is-dark': isDark }" style="font-size:18px;cursor:pointer;margin-left:8px;" @click="toggleDarkMode"></i>
          </el-tooltip>
          <el-tooltip content="字体大小" placement="bottom">
            <el-dropdown trigger="click" style="margin-left:8px;" @command="setFontSize">
              <span class="font-btn" style="font-size:16px;cursor:pointer;padding:2px 6px;border:1px solid #dcdfe6;border-radius:4px;">Aa</span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="small" :class="{ 'active-font': fontSize === 'small' }"><i class="el-icon-minus"></i> 小</el-dropdown-item>
                <el-dropdown-item command="medium" :class="{ 'active-font': fontSize === 'medium' }">中（默认）</el-dropdown-item>
                <el-dropdown-item command="large" :class="{ 'active-font': fontSize === 'large' }"><i class="el-icon-plus"></i> 大</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-tooltip>
          <div style="display:flex;align-items:center;cursor:pointer;margin-left:10px;" @click="$router.push('/myInfo').catch(() => {})">
            <el-avatar v-if="userInfo.avatar" :src="userInfo.avatar" :size="30" style="margin-right:8px;"></el-avatar>
            <el-avatar v-else :size="30" style="margin-right:8px;background:#409EFF;">{{ (userInfo.nickname || userInfo.username || '用')[0] }}</el-avatar>
            <span style="color:#333;">{{ userInfo.nickname || userInfo.username || '用户' }}</span>
            <el-tag v-if="loginType === 'admin'" :type="userInfo.roleName === 'super' ? 'danger' : 'warning'" size="mini" style="margin-left:5px;">{{ userInfo.roleName === 'super' ? '超管' : '管理员' }}</el-tag>
          </div>
          <el-button type="text" style="margin-left:15px;color:#F56C6C;" @click="logout"><i class="el-icon-switch-button"></i> 退出</el-button>
        </div>
      </div>
      <div class="app-main" :style="{ fontSize: fontSizeMap[fontSize] }" :class="{ 'dark-mode': isDark, 'dark-bg': isDark }">
        <router-view />
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
    <ai-chat ref="aiChat" />
    <el-dialog :title="announcement.title" :visible.sync="announcementVisible" width="500px">
      <el-tag v-if="announcement.announcementType == 1" type="danger" size="small" style="margin-bottom:10px;">重要公告</el-tag>
      <div style="line-height:1.8;white-space:pre-wrap;">{{ announcement.content }}</div>
      <div v-if="parseImages(announcement.images).length > 0" style="margin-top:12px;">
        <el-image v-for="(img, i) in parseImages(announcement.images)" :key="i" :src="img" :preview-src-list="parseImages(announcement.images)" style="width:120px;height:120px;margin-right:8px;margin-bottom:8px;border-radius:8px;" fit="cover"></el-image>
      </div>
      <div style="margin-top:10px;color:#999;font-size:12px;">{{ formatTime(announcement.createTime) }}</div>
      <span slot="footer">
        <el-button type="primary" @click="announcementVisible = false">知道了</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import AiChat from '@/components/AiChat.vue'
export default {
  name: 'Home',
  components: { AiChat },
  data() {
    return {
      /** 未读消息数量 */
      unreadCount: 0,
      /** 通知列表 */
      noticeList: [],
      /** 当前公告内容 */
      announcement: {},
      /** 公告弹窗是否可见 */
      announcementVisible: false,
      /** 是否有新公告 */
      hasNewAnnouncement: false,
      /** 未读消息定时器 */
      unreadTimer: null,
      /** 锁定检查定时器 */
      lockCheckTimer: null,
      /** 公告检查定时器 */
      announcementTimer: null,
      /** 是否启用暗黑模式 */
      isDark: localStorage.getItem('darkMode') === 'true',
      /** 字体大小设置 */
      fontSize: localStorage.getItem('fontSize') || 'medium',
      /** 字体大小映射表 */
      fontSizeMap: { small: '12px', medium: '14px', large: '18px' },
      /** 滚动公告列表 */
      marqueeItems: [],
      /** 待处理计数（审核/未读等） */
      pendingCounts: { pendingPostAudit: 0, pendingGoodsAudit: 0, pendingWallAudit: 0, unreadForum: 0, unreadTrade: 0, unreadWall: 0 },
      /** 待处理计数定时器 */
      pendingTimer: null,
    }
  },
  computed: {
    /** 当前登录用户信息 */
    userInfo() { return this.$store.getters.getUserInfo || {} },
    /** 登录类型 */
    loginType() { return this.$store.getters.getLoginType }
  },
  /** 组件挂载后：初始化暗黑模式、字体大小、定时器、事件监听 */
  mounted() {
    this.applyDarkMode()
    this.setFontSize(this.fontSize)
    this.fetchUnreadCount()
    this.checkNewAnnouncement()
    this.loadMarqueeAnnouncements()
    this.checkLockStatus()
    this.fetchPendingCount()
    this.unreadTimer = setInterval(() => { this.fetchUnreadCount() }, 30000)
    this.announcementTimer = setInterval(() => { this.checkNewAnnouncement() }, 60000)
    this.lockCheckTimer = setInterval(() => { this.checkLockStatus() }, 30000)
    this.pendingTimer = setInterval(() => { this.fetchPendingCount() }, 60000)
    // 全局AI助手调用方法，子页面可通过 this.$root.openAiChat('问题') 调用
    this.$root.$on('open-ai-chat', (question) => {
      if (this.$refs.aiChat) {
        this.$refs.aiChat.openWithQuestion(question)
      }
    })
    // 监听消息已读事件，即时刷新未读数
    this.$root.$on('messages-read', () => {
      this.fetchUnreadCount()
    })
  },
  /** 组件销毁前：清除所有定时器 */
  beforeDestroy() {
    if (this.unreadTimer) clearInterval(this.unreadTimer)
    if (this.announcementTimer) clearInterval(this.announcementTimer)
    if (this.lockCheckTimer) clearInterval(this.lockCheckTimer)
    if (this.pendingTimer) clearInterval(this.pendingTimer)
  },
  methods: {
    /** 格式化时间为 YYYY-MM-DD HH:mm:ss */
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const pad = n => String(n).padStart(2, '0')
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    },
    /** 退出登录，清除用户信息并跳转到登录页 */
    logout() {
      this.$store.commit('CLEAR_USER')
      this.$router.push('/login').catch(() => {})
    },
    /** 获取未读消息数量（排除AI伴侣未读） */
    async fetchUnreadCount() {
      try {
        const res = await this.$axios.get('/letter/unreadCountByType')
        if (res.code === 200) {
          const d = res.data || {}
          // 排除AI伴侣的letter未读数
          const letterCount = d.letter || 0
          let aiUnread = 0
          try {
            const sessionRes = await this.$axios.post('/letter/mySessionList')
            if (sessionRes.code === 200 && sessionRes.data) {
              const sessions = sessionRes.data || []
              sessions.forEach(s => {
                const nickname = (s.otherNickname || '').toLowerCase()
                if (nickname.indexOf('ai伴侣') !== -1 || nickname.indexOf('ai助手') !== -1 || nickname === 'ai' || nickname.indexOf('ai companion') !== -1 || nickname.indexOf('ai chat') !== -1 || /^ai[\s\-_]/.test(nickname)) {
                  aiUnread += (s.unread || 0)
                }
              })
            }
          } catch (e) {}
          const adjustedLetter = Math.max(0, letterCount - aiUnread)
          const newTotal = adjustedLetter + (d.comment || 0) + (d.system || 0) + (d.order || 0) + (d.logistics || 0) + (d.like || 0) + (d.collect || 0) + (d.follow || 0) + (d.mention || 0)
          // 新消息提示
          if (newTotal > this.unreadCount && this.unreadCount >= 0) {
            const diff = newTotal - this.unreadCount
            if (diff > 0) {
              this.$notify({
                title: '新消息',
                message: '您有 ' + diff + ' 条未读消息',
                type: 'warning',
                duration: 3000,
                onClick: () => { this.$router.push('/myLetter').catch(() => {}) }
              })
            }
          }
          this.unreadCount = newTotal
        }
      } catch (e) {}
    },
    /** 获取待处理事项数量（审核/未读等） */
    async fetchPendingCount() {
      try {
        const res = await this.$axios.get('/stats/pendingCount')
        if (res.code === 200) {
          this.pendingCounts = res.data || {}
        }
      } catch (e) {}
    },
    /** 加载通知列表 */
    async loadNoticeList() {
      try {
        const res = await this.$axios.get('/letter/myMessageList', { params: { pageNum: 1, pageSize: 15 } })
        if (res.code === 200) this.noticeList = res.data.records || res.data || []
      } catch (e) {}
    },
    /** 点击通知项：标记已读并跳转 */
    async handleNoticeClick(item) {
      if (item.letterStatus === 0) {
        await this.$axios.post('/letter/markRead/' + item.letterId).catch(() => {})
        item.letterStatus = 1
        this.fetchUnreadCount()
      }
      var type = item.messageType || ''
      // 订单/物流通知 -> 直接跳转到订单详情页
      if (type === 'order' || type === 'logistics') {
        if (item.sessionId) {
          var parts = item.sessionId.split('_')
          if (parts.length >= 4 && parts[2] === 'order') {
            var targetId = parts[3]
            var detail = item.letterDetail || ''
            if (type === 'logistics') {
              this.$router.push('/orderDetail/' + targetId + '?from=buyer').catch(() => {})
            } else if (detail.indexOf('有新的购买订单') >= 0 || detail.indexOf('申请退货') >= 0 || detail.indexOf('申请售后') >= 0 || detail.indexOf('退货申请') >= 0) {
              this.$router.push('/orderDetail/' + targetId + '?from=seller').catch(() => {})
            } else {
              this.$router.push('/orderDetail/' + targetId + '?from=buyer').catch(() => {})
            }
            return
          }
        }
      }
      // 其他通知 -> 跳转到消息中心对应tab
      var tab = 'notification'
      if (type === 'comment' || type === 'like' || type === 'follow') {
        tab = 'interaction'
      } else if (type === 'letter') {
        tab = 'letter'
      }
      this.$router.push({ path: '/myLetter', query: { tab: tab } }).catch(() => {})
    },
    /** 标记所有通知为已读 */
    async markAllNoticeRead() {
      try {
        await this.$axios.post('/letter/markAllRead')
        this.$message.success('已全部标记为已读')
        this.unreadCount = 0
        this.loadNoticeList()
      } catch (e) {}
    },
    getTypeColor(type) {
      if (type === 'comment') return '#409EFF'
      if (type === 'system') return '#E6A23C'
      if (type === 'like') return '#F56C6C'
      if (type === 'follow') return '#409EFF'
      return '#67C23A'
    },
    getTypeIcon(type) {
      if (type === 'comment') return 'el-icon-chat-dot-round'
      if (type === 'system') return 'el-icon-bell'
      if (type === 'like') return 'el-icon-thumb'
      if (type === 'follow') return 'el-icon-user'
      return 'el-icon-message'
    },
    /** 检查是否有新公告 */
    async checkNewAnnouncement() {
      try {
        const res = await this.$axios.get('/announcement/publicList', { params: { pageNum: 1, pageSize: 1 } })
        if (res.code === 200) {
          const records = res.data.records || res.data || []
          if (records.length > 0) {
            const latest = records[0]
            const lastSeenId = sessionStorage.getItem('lastSeenAnnouncementId')
            if (!lastSeenId || String(latest.announcementId) !== String(lastSeenId)) {
              this.hasNewAnnouncement = true
              // Only show popup for important announcements (type=2)
              if (latest.announcementType == 1) {
                this.announcement = latest
                this.announcementVisible = true
              }
              sessionStorage.setItem('lastSeenAnnouncementId', String(latest.announcementId))
            }
          }
        }
      } catch (e) {}
    },
    /** 加载滚动公告列表 */
    async loadMarqueeAnnouncements() {
      try {
        const res = await this.$axios.get('/announcement/publicList', { params: { pageNum: 1, pageSize: 5 } })
        if (res.code === 200) {
          const records = res.data.records || res.data || []
          this.marqueeItems = records.map(a => ({ announcementId: a.announcementId, title: a.title, content: a.content })).filter(item => item.title || item.content)
        }
      } catch (e) {}
    },
    goToAnnouncement(index) {
      if (this.marqueeItems[index]) {
        this.announcement = this.marqueeItems[index]
        this.announcementVisible = true
        sessionStorage.setItem('lastSeenAnnouncementId', String(this.marqueeItems[index].announcementId))
      }
    },
    /** 切换暗黑模式 */
    toggleDarkMode() {
      this.isDark = !this.isDark
      localStorage.setItem('darkMode', String(this.isDark))
      this.applyDarkMode()
    },
    /** 应用暗黑模式到DOM */
    applyDarkMode() {
      if (this.isDark) {
        document.documentElement.classList.add('dark-mode-root')
      } else {
        document.documentElement.classList.remove('dark-mode-root')
      }
    },
    /** 设置全局字体大小 */
    setFontSize(size) {
      this.fontSize = size
      localStorage.setItem('fontSize', size)
      document.documentElement.style.fontSize = this.fontSizeMap[size]
    },
    /** 检查用户锁定状态，若被锁定则强制退出 */
    async checkLockStatus() {
      try {
        const loginType = this.$store.getters.getLoginType
        if (loginType === 'admin') return
        const res = await this.$axios.get('/user/info')
        if (res.code === 200 && res.data && res.data.locked) {
          window.sessionStorage.removeItem('token')
          window.sessionStorage.removeItem('userInfo')
          window.sessionStorage.removeItem('loginType')
          this.$store.commit('CLEAR_USER')
          this.$message.error('账号已被锁定，请联系管理员')
          this.$router.push('/login').catch(() => {})
        }
      } catch (e) {}
    },
    /** 解析图片URL列表（逗号分隔） */
    parseImages(images) {
      if (!images) return []
      try { const arr = JSON.parse(images); return Array.isArray(arr) ? arr : [images] }
      catch (e) { return images.split(',').filter(s => s.trim()) }
    }
  }
}
</script>

<style scoped>
.app-wrapper { display: flex; height: 100vh; }
.sidebar-container { width: 240px; background: linear-gradient(180deg, #1e293b 0%, #0f172a 50%, #1e1b4b 100%); overflow-y: auto; flex-shrink: 0; transition: all 0.4s ease; box-shadow: 4px 0 24px rgba(0,0,0,0.15); position: relative; }
.sidebar-container::before { content: ''; position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23409eff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E"); opacity: 0.6; pointer-events: none; }
.sidebar-container.dark-mode { background: linear-gradient(180deg, #0f172a 0%, #020617 50%, #1e1b4b 100%); }
.sidebar-logo { height: 80px; display: flex; align-items: center; padding: 0 20px; background: rgba(255,255,255,0.04); border-bottom: 1px solid rgba(255,255,255,0.06); position: relative; z-index: 1; cursor: pointer; transition: background 0.3s ease; }
.sidebar-logo:hover { background: rgba(255,255,255,0.08); }
.logo-icon { width: 42px; height: 42px; border-radius: 14px; background: linear-gradient(135deg, #409EFF, #667eea); display: flex; align-items: center; justify-content: center; margin-right: 12px; box-shadow: 0 4px 15px rgba(64,158,255,0.35); transition: all 0.3s ease; }
.logo-icon i { color: #fff; font-size: 22px; }
.sidebar-logo:hover .logo-icon { transform: rotate(-5deg) scale(1.05); box-shadow: 0 6px 20px rgba(64,158,255,0.5); }
.logo-text { display: flex; flex-direction: column; }
.logo-title { color: #fff; font-size: 18px; font-weight: 700; letter-spacing: 1px; line-height: 1.3; }
.logo-subtitle { color: rgba(255,255,255,0.45); font-size: 10px; font-weight: 500; letter-spacing: 2px; text-transform: uppercase; margin-top: 2px; }
.sidebar-divider { height: 1px; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent); margin: 8px 16px; position: relative; z-index: 1; }
.main-container { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.navbar { height: 64px; line-height: 64px; padding: 0 28px; background: rgba(255,255,255,0.85); backdrop-filter: blur(20px) saturate(180%); -webkit-backdrop-filter: blur(20px) saturate(180%); box-shadow: 0 1px 12px rgba(0,0,0,0.06); display: flex; align-items: center; justify-content: space-between; transition: background-color 0.3s, color 0.3s; z-index: 2; border-bottom: 1px solid rgba(0,0,0,0.04); }
.navbar.dark-mode { background: rgba(38,39,39,0.85); color: #bfcbd9; border-bottom: 1px solid rgba(255,255,255,0.04); }
.app-main { flex: 1; padding: 28px; background: #f0f2f5; overflow-y: auto; transition: background-color 0.3s, color 0.3s; }
.app-main.dark-mode { color: #bfcbd9; }
.app-main.dark-bg { background: #141414; }

.marquee-wrap { max-width: 320px; overflow: hidden; white-space: nowrap; margin-right: 15px; border-radius: 14px; padding: 4px 12px; background: linear-gradient(135deg, #667eea22, #764ba222); display: inline-flex; align-items: center; height: 30px; }
.marquee-scroll-area { flex: 1; height: 22px; overflow: hidden; position: relative; }
.marquee-scroll-content { position: relative; }
.marquee-scroll-item { height: 22px; line-height: 22px; font-size: 13px; color: #666; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; cursor: pointer; }
.marquee-scroll-item:hover { color: #409EFF; }
.marquee-scroll-content.animate { animation: marqueeScrollUp 12s linear infinite; }
@keyframes marqueeScrollUp {
  0%, 15% { transform: translateY(0); }
  20%, 35% { transform: translateY(-22px); }
  40%, 55% { transform: translateY(-44px); }
  60%, 75% { transform: translateY(-66px); }
  80%, 95% { transform: translateY(-88px); }
  100% { transform: translateY(-110px); }
}
.marquee-more-btn { color: #409EFF; font-size: 12px; flex-shrink: 0; padding: 0 4px; }
.font-btn { display: inline-block; line-height: 18px; }

.notice-item { display: flex; align-items: center; padding: 12px 10px; border-bottom: 1px solid #f0f0f0; cursor: pointer; transition: all 0.3s ease; border-radius: 10px; margin-bottom: 4px; }
.notice-item:hover { background: #f5f7fa; transform: translateX(4px); }
.notice-item.unread { background: linear-gradient(135deg, #ecf5ff, #f0f8ff); border-left: 3px solid #409EFF; }
.notice-type-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 14px; margin-right: 12px; flex-shrink: 0; box-shadow: 0 2px 8px rgba(0,0,0,0.12); transition: transform 0.3s ease; }
.notice-item:hover .notice-type-icon { transform: scale(1.1) rotate(-5deg); }
.notice-content { flex: 1; min-width: 0; }
.notice-text { font-size: 13px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-weight: 500; }
.notice-time { font-size: 11px; color: #999; margin-top: 4px; }

.el-icon-moon { position: relative; color: #909399; transition: all 0.3s ease; padding: 6px; border-radius: 8px; }
.el-icon-moon:hover { background: rgba(0,0,0,0.04); }
.el-icon-moon.is-dark::before { content: "\E6CF"; color: #F56C6C; }
.el-icon-moon.is-dark { color: #F56C6C; }
.active-font { color: #409EFF; font-weight: bold; }
.custom-badge { position: absolute; top: -2px; right: 4px; background: linear-gradient(135deg, #F56C6C, #f89898); color: #fff; font-size: 10px; height: 18px; line-height: 18px; padding: 0 5px; border-radius: 9px; white-space: nowrap; transform: translateX(50%); box-shadow: 0 2px 6px rgba(245, 108, 108, 0.4); font-weight: 600; }
.sidebar-badge { position: absolute; top: 12px; right: 20px; width: 8px; height: 8px; background: linear-gradient(135deg, #F56C6C, #f89898); border-radius: 50%; box-shadow: 0 0 0 2px rgba(245,108,108,0.2); animation: pulseBadge 2s infinite; }
@keyframes pulseBadge {
  0%, 100% { box-shadow: 0 0 0 2px rgba(245,108,108,0.2); }
  50% { box-shadow: 0 0 0 5px rgba(245,108,108,0); }
}
.menu-badge { position: relative; top: -8px; left: 2px; background: linear-gradient(135deg, #F56C6C, #f89898); color: #fff; font-size: 10px; padding: 1px 6px; border-radius: 10px; line-height: 1.2; font-style: normal; font-weight: 600; box-shadow: 0 2px 6px rgba(245, 108, 108, 0.3); }
/* ===== Site Footer ===== */
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
</style>

<style>
.dark-mode-root .el-card,
.dark-mode-root .el-dialog,
.dark-mode-root .el-drawer,
.dark-mode-root .el-table,
.dark-mode-root .el-form-item__label,
.dark-mode-root .el-input__inner,
.dark-mode-root .el-textarea__inner,
.dark-mode-root .el-select-dropdown,
.dark-mode-root .el-popper,
.dark-mode-root .el-pagination,
.dark-mode-root .el-empty__description,
.dark-mode-root .el-descriptions__label,
.dark-mode-root .el-descriptions__content,
.dark-mode-root .el-tabs__item,
.dark-mode-root .el-breadcrumb__item,
.dark-mode-root .el-step__title,
.dark-mode-root .el-step__description,
.dark-mode-root .el-result__title,
.dark-mode-root .el-result__subtitle,
.dark-mode-root .el-statistic__head,
.dark-mode-root .el-statistic__content,
.dark-mode-root .el-timeline-item__wrapper,
.dark-mode-root .el-collapse-item__header,
.dark-mode-root .el-collapse-item__wrap,
.dark-mode-root .el-menu--popup,
.dark-mode-root .el-dropdown-menu,
.dark-mode-root .el-autocomplete-suggestion,
.dark-mode-root .el-picker-panel,
.dark-mode-root .el-calendar-table th,
.dark-mode-root .el-calendar-table td,
.dark-mode-root .el-tag,
.dark-mode-root .el-badge__content {
  background-color: #262727;
  color: #bfcbd9;
  border-color: #4c4d4f;
}
.dark-mode-root .el-card__header {
  border-bottom-color: #4c4d4f;
}
.dark-mode-root .el-table th,
.dark-mode-root .el-table tr {
  background-color: #262727;
}
.dark-mode-root .el-table--striped .el-table__body tr.el-table__row--striped td {
  background-color: #1d1e1f;
}
.dark-mode-root .el-table--enable-row-hover .el-table__body tr:hover > td {
  background-color: #333;
}
.dark-mode-root .el-input__inner,
.dark-mode-root .el-textarea__inner {
  background-color: #1d1e1f;
  color: #bfcbd9;
}
.dark-mode-root .el-select-dropdown {
  background-color: #262727;
}
.dark-mode-root body {
  background-color: #141414;
  color: #bfcbd9;
}
</style>
