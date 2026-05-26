<!--
  组件名：AiChat
  功能描述：AI智能助手聊天组件，浮动在页面右下角
  主要职责：
    1. 提供本地搜索和LLM大模型两种对话模式
    2. 支持商品/帖子/表白墙/公告等搜索结果卡片展示
    3. 管理历史会话（新增/删除/批量删除/切换）
    4. 支持语音输入（Web Speech API）
    5. 根据当前路由动态生成上下文感知的快捷问题
-->
<template>
  <div class="ai-chat-wrapper">
    <!-- 浮动按钮 -->
    <div class="ai-chat-fab" :class="{ active: isOpen }" @click="toggleChat">
      <i :class="isOpen ? 'el-icon-close' : 'el-icon-chat-line-round'"></i>
    </div>

    <!-- 聊天面板 -->
    <transition name="ai-chat-slide">
      <div v-if="isOpen" class="ai-chat-panel" :class="{ 'dark-mode': isDark }">
        <!-- 头部 -->
        <div class="ai-chat-header">
          <div class="ai-chat-header-left">
            <el-tooltip content="历史会话" placement="bottom" v-if="!showHistory">
              <i class="el-icon-time" style="cursor:pointer;" @click="showHistory = true"></i>
            </el-tooltip>
            <el-tooltip content="返回对话" placement="bottom" v-else>
              <i class="el-icon-back" style="cursor:pointer;" @click="showHistory = false"></i>
            </el-tooltip>
            <span>{{ showHistory ? '历史会话' : 'AI 智能助手' }}</span>
          </div>
          <div class="ai-chat-header-right" v-if="!showHistory">
            <el-select v-model="currentMode" size="mini" class="ai-mode-select" popper-class="ai-mode-popper" @change="onModeChange" placement="bottom-end">
              <el-option label="本地搜索" value="local">
                <span style="float:left">本地搜索</span>
                <span style="float:right;color:#8492a6;font-size:11px">快速</span>
              </el-option>
              <el-option v-for="cfg in aiConfigs" :key="cfg.id" :label="cfg.configName + ' - ' + cfg.modelName" :value="'llm_' + cfg.id">
                <div style="display:flex;justify-content:space-between;align-items:center;width:100%;">
                  <span style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:100px;">{{ cfg.configName }}</span>
                  <span style="color:#8492a6;font-size:11px;flex-shrink:0;margin-left:6px;">{{ cfg.modelName }}{{ cfg.isActive ? ' ★' : '' }}</span>
                </div>
              </el-option>
            </el-select>
            <el-tooltip content="清空对话" placement="bottom">
              <i class="el-icon-delete" style="cursor:pointer;font-size:16px;" @click="clearMessages"></i>
            </el-tooltip>
          </div>
          <div class="ai-chat-header-right" v-else>
            <template v-if="!isSelectMode">
              <el-tooltip content="选择删除" placement="bottom">
                <i class="el-icon-finished" style="cursor:pointer;font-size:16px;" @click="enterSelectMode"></i>
              </el-tooltip>
              <el-tooltip content="清空全部历史" placement="bottom">
                <i class="el-icon-delete" style="cursor:pointer;font-size:16px;" @click="clearAllHistory"></i>
              </el-tooltip>
            </template>
            <template v-else>
              <el-button type="text" size="mini" style="color:#fff;padding:0;" @click="toggleSelectAll">{{ isAllSelected ? '取消全选' : '全选' }}</el-button>
              <el-button type="text" size="mini" style="color:#fff;padding:0;" @click="exitSelectMode">取消</el-button>
            </template>
          </div>
        </div>

        <!-- 历史会话列表 -->
        <div v-if="showHistory" class="ai-chat-history">
          <div v-if="conversations.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无历史会话</div>
          <div v-for="(conv, idx) in conversations" :key="conv.id" class="ai-chat-history-item" :class="{ current: conv.id === currentConvId, selected: isSelectMode && selectedConvIds.includes(conv.id) }" @click="onHistoryItemClick(conv)">
            <div v-if="isSelectMode" class="ai-chat-history-check" @click.stop>
              <el-checkbox :value="selectedConvIds.includes(conv.id)" @change="toggleSelectConv(conv.id)"></el-checkbox>
            </div>
            <div class="ai-chat-history-info">
              <div class="ai-chat-history-title">
                <span v-if="conv.id === currentConvId" class="ai-chat-current-badge">当前</span>
                {{ conv.title }}
              </div>
              <div class="ai-chat-history-meta">{{ conv.time }} · {{ conv.count }}条消息</div>
            </div>
            <i v-if="!isSelectMode" class="el-icon-delete ai-chat-history-del" @click.stop="deleteConversation(idx)"></i>
          </div>
        </div>

        <!-- 选择模式底部操作栏 -->
        <div v-if="showHistory && isSelectMode" class="ai-chat-select-bar">
          <span class="ai-chat-select-count">已选 {{ selectedConvIds.length }} 项</span>
          <el-button type="danger" size="mini" :disabled="selectedConvIds.length === 0" @click="deleteSelected">删除所选</el-button>
        </div>

        <!-- 消息区域 -->
        <div v-if="!showHistory" class="ai-chat-messages" ref="messagesContainer">
          <div v-for="(msg, index) in messages" :key="index" class="ai-chat-msg" :class="msg.role">
            <div class="ai-chat-msg-avatar">
              <i v-if="msg.role === 'ai'" class="el-icon-cpu"></i>
              <i v-else class="el-icon-user-solid"></i>
            </div>
            <div class="ai-chat-msg-content">
              <!-- AI 推荐商品卡片 -->
              <template v-if="msg.goods && msg.goods.length > 0">
                <div class="ai-chat-text" v-html="msg.text"></div>
                <div class="ai-chat-goods-list">
                  <div v-for="goods in msg.goods" :key="goods.goodsId" class="ai-chat-goods-card" @click="goGoodsDetail(goods.goodsId)">
                    <div class="ai-chat-goods-img" v-if="goods.goodsImages">
                      <img :src="goods.goodsImages.split(',')[0]" />
                    </div>
                    <div class="ai-chat-goods-img ai-chat-goods-no-img" v-else>
                      <i class="el-icon-goods"></i>
                    </div>
                    <div class="ai-chat-goods-info">
                      <div class="ai-chat-goods-name">{{ goods.goodsName }}</div>
                      <div class="ai-chat-goods-price">￥{{ goods.goodsPrice }}</div>
                    </div>
                  </div>
                </div>
              </template>
              <!-- AI 推荐帖子卡片 -->
              <template v-if="msg.posts && msg.posts.length > 0">
                <div class="ai-chat-text" v-if="!msg.goods || msg.goods.length === 0" v-html="msg.text"></div>
                <div class="ai-chat-posts-list">
                  <div v-for="post in msg.posts" :key="post.postId" class="ai-chat-post-card" @click="goPostDetail(post.postId)">
                    <div class="ai-chat-post-title">{{ post.postTitle || post.title }}</div>
                    <div class="ai-chat-post-detail" v-if="post.postDetail || post.detail">{{ post.postDetail || post.detail }}</div>
                    <div class="ai-chat-post-meta">
                      <span>{{ post.nickname || '匿名' }}</span>
                      <span>{{ post.createTime | formatTime }}</span>
                      <span v-if="post.viewNum">浏览{{ post.viewNum }}</span>
                      <span v-if="post.likeNum">赞{{ post.likeNum }}</span>
                    </div>
                  </div>
                </div>
              </template>
              <!-- 表白墙卡片（独立于帖子，可单独显示） -->
              <template v-if="msg.walls && msg.walls.length > 0">
                <div class="ai-chat-text" v-if="(!msg.goods || msg.goods.length === 0) && (!msg.posts || msg.posts.length === 0)" v-html="msg.text"></div>
                <div class="ai-chat-walls-list">
                  <div class="ai-chat-walls-title">表白墙</div>
                  <div v-for="wall in msg.walls" :key="wall.wallId" class="ai-chat-wall-card" @click="goWallDetail(wall.wallId)">
                    <div class="ai-chat-wall-content">{{ wall.wallContent }}</div>
                    <div class="ai-chat-wall-meta">
                      <span>{{ wall.nickname || '匿名' }}</span>
                      <span><i class="el-icon-view"></i> {{ wall.viewNum || 0 }}</span>
                      <span><i class="el-icon-thumb"></i> {{ wall.likeNum || 0 }}</span>
                      <span>{{ wall.createTime | formatTime }}</span>
                    </div>
                  </div>
                </div>
              </template>
              <!-- 公告卡片 -->
              <template v-if="msg.announcements && msg.announcements.length > 0">
                <div class="ai-chat-text" v-if="(!msg.goods || msg.goods.length === 0) && (!msg.posts || msg.posts.length === 0) && (!msg.walls || msg.walls.length === 0)" v-html="msg.text"></div>
                <div class="ai-chat-announcements-list">
                  <div class="ai-chat-announcements-title">📢 平台公告</div>
                  <div v-for="ann in msg.announcements" :key="ann.announcementId" class="ai-chat-announcement-card">
                    <div class="ai-chat-announcement-title">{{ ann.title }}</div>
                    <div class="ai-chat-announcement-content" v-if="ann.content">{{ ann.content }}</div>
                    <div class="ai-chat-announcement-meta">
                      <span>{{ ann.createTime | formatTime }}</span>
                    </div>
                  </div>
                </div>
              </template>
              <!-- 普通文本（无任何卡片时显示） -->
              <template v-if="(!msg.goods || msg.goods.length === 0) && (!msg.posts || msg.posts.length === 0) && (!msg.walls || msg.walls.length === 0) && (!msg.announcements || msg.announcements.length === 0)">
                <div class="ai-chat-text" v-html="msg.text"></div>
                <!-- 导航跳转按钮 -->
                <div v-if="msg.navigate" class="ai-chat-navigate">
                  <el-button type="primary" size="mini" icon="el-icon-right" @click="navigateTo(msg.navigate)">前往{{ msg.navigateName }}</el-button>
                </div>
              </template>
              <!-- 来源标签 -->
              <div v-if="msg.source" class="ai-chat-source">{{ msg.source }}</div>
            </div>
          </div>
          <!-- 加载中 -->
          <div v-if="isLoading" class="ai-chat-msg ai">
            <div class="ai-chat-msg-avatar"><i class="el-icon-cpu"></i></div>
            <div class="ai-chat-msg-content">
              <div class="ai-chat-typing">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷问题 -->
        <div v-if="messages.length <= 1" class="ai-chat-quick">
          <div class="ai-chat-quick-title">试试问我：</div>
          <div class="ai-chat-quick-list">
            <span v-for="(q, i) in contextQuestions" :key="i" class="ai-chat-quick-item" @click="sendQuickQuestion(q)">{{ q }}</span>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="ai-chat-input-area">
          <el-input
            v-model="inputText"
            placeholder="问我任何问题，如：推荐一本好书..."
            size="small"
            @keyup.enter.native="sendMessage"
            :disabled="isLoading"
          >
            <template slot="suffix">
              <div class="ai-chat-voice-wrap" @click="toggleVoice" :title="isListening ? '点击停止录音' : '语音输入'">
                <i
                  :class="[isListening ? 'el-icon-microphone' : 'el-icon-mic', 'ai-chat-voice-icon', { listening: isListening }]"
                ></i>
                <span v-if="isListening" class="ai-chat-voice-pulse"></span>
              </div>
            </template>
            <el-button slot="append" icon="el-icon-s-promotion" @click="sendMessage" :loading="isLoading"></el-button>
          </el-input>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'AiChat',
  data() {
    return {
      /** 聊天面板是否打开 */
      isOpen: false,
      /** 是否显示历史会话列表 */
      showHistory: false,
      /** 输入框文本 */
      inputText: '',
      /** 当前会话消息列表 */
      messages: [],
      /** 对话历史（用于LLM上下文） */
      chatHistory: [],
      /** 是否正在等待AI回复 */
      isLoading: false,
      /** 是否正在语音输入 */
      isListening: false,
      /** Web Speech API识别实例 */
      recognition: null,
      /** 当前对话模式：'local'本地搜索 / 'llm_{id}'大模型 */
      currentMode: localStorage.getItem('ai_chat_mode') || 'local',
      /** 可用的大模型配置列表 */
      aiConfigs: [],
      /** 历史会话列表（持久化到localStorage） */
      conversations: JSON.parse(localStorage.getItem('ai_conversations') || '[]'),
      /** 当前活跃会话ID */
      currentConvId: null,
      /** 是否处于批量选择模式 */
      isSelectMode: false,
      /** 批量选中的会话ID列表 */
      selectedConvIds: [],
      /** 默认快捷问题列表 */
      quickQuestions: [
        '推荐一些二手教材',
        '有什么便宜的电子产品？',
        '最近有什么热门帖子？',
        '帮我找自行车'
      ]
    }
  },
  computed: {
    /** 是否启用暗黑模式 */
    isDark() {
      return this.$store ? (this.$store.getters.getUserInfo ? localStorage.getItem('darkMode') === 'true' : false) : false
    },
    /** 根据当前路由动态生成上下文感知的快捷问题 */
    contextQuestions() {
      const path = this.$route ? this.$route.path : ''
      if (path.includes('postList') || path.includes('myPost')) {
        return ['最近有什么热门帖子？', '帮我找学习经验帖', '有什么求助帖？', '推荐有趣的讨论']
      } else if (path.includes('goodsList') || path.includes('myGoods')) {
        return ['推荐便宜的二手教材', '有什么电子产品？', '帮我找自行车', '最近有什么新商品？']
      } else if (path.includes('wallList') || path.includes('myWall')) {
        return ['表白墙最近有什么内容？', '帮我找匿名表白', '有什么有趣的墙内容？']
      } else if (path.includes('myOrder') || path.includes('mySaleOrder')) {
        return ['查看我的销售数据', '最近订单趋势如何？', '给我一些经营建议', '如何提高销量？']
      } else if (path.includes('myInfo') || path.includes('userProfile')) {
        return ['查看平台数据概览', '给我一些经营建议', '热门商品推荐', '最近热门帖子']
      } else if (path.includes('statsPage')) {
        return ['分析一下销售趋势', '给我一些经营建议', '如何提高收入？', '平台数据概览']
      } else if (path.includes('announcementList')) {
        return ['最近有什么重要公告？', '平台有什么新动态？']
      } else if (path.includes('search')) {
        return ['帮我搜索二手教材', '有什么便宜的电子产品？', '推荐热门帖子']
      }
      return this.quickQuestions
    },
    /** 是否全选 */
    isAllSelected() {
      return this.conversations.length > 0 && this.selectedConvIds.length === this.conversations.length
    }
  },
  methods: {
    /** 切换聊天面板的打开/关闭状态 */
    toggleChat() {
      this.isOpen = !this.isOpen
      if (this.isOpen && this.messages.length === 0) {
        this.messages.push({
          role: 'ai',
          text: '你好！我是智联校园 AI 助手<br>我可以帮你搜索商品、推荐好物、查找帖子、查看数据等。<br>试试问我：<b>推荐一些二手教材</b> 或 <b>最近有什么热门帖子？</b>'
        })
      }
      if (this.isOpen) {
        this.loadAiConfigs()
        this.$nextTick(() => { this.scrollToBottom() })
      } else {
        // 关闭面板时退出选择模式，不自动保存到历史
        this.isSelectMode = false
        this.selectedConvIds = []
      }
    },
    /** 外部调用：打开AI聊天面板并发送预设问题 */
    openWithQuestion(question) {
      if (!this.isOpen) {
        this.isOpen = true
        if (this.messages.length === 0) {
          this.messages.push({
            role: 'ai',
            text: '你好！我是智联校园 AI 助手，有什么可以帮你的？'
          })
        }
        this.loadAiConfigs()
      }
      this.$nextTick(() => {
        this.inputText = question
        this.sendMessage()
      })
    },
    /** 加载AI大模型配置列表 */
    async loadAiConfigs() {
      try {
        const res = await this.$axios.get('/ai/configs?configType=1')
        if (res.code === 200 && res.data) {
          this.aiConfigs = res.data || []
          // 仅在首次加载且用户未手动选择过时，自动选择激活的配置
          if (this.currentMode === 'local') {
            const activeConfig = this.aiConfigs.find(c => c.isActive)
            if (activeConfig) {
              this.currentMode = 'llm_' + activeConfig.id
            }
          }
        }
      } catch (e) {}
    },
    /** 模式切换时保存到localStorage并清空对话历史 */
    onModeChange() {
      localStorage.setItem('ai_chat_mode', this.currentMode)
      this.chatHistory = []
    },
    /** 清空当前对话消息，保存到历史后新建空会话 */
    clearMessages() {
      // 保存当前会话到列表
      this.syncCurrentConvToList()
      // 新建空会话
      this.messages = []
      this.chatHistory = []
      this.currentConvId = null
      this.messages.push({
        role: 'ai',
        text: '你好！我是智联校园 AI 助手<br>我可以帮你搜索商品、推荐好物、查找帖子、查看数据等。<br>试试问我：<b>推荐一些二手教材</b> 或 <b>最近有什么热门帖子？</b>'
      })
    },
    /** 将当前活跃会话同步到conversations数组并持久化到localStorage */
    syncCurrentConvToList() {
      var userMsgs = this.messages.filter(function(m) { return m.role === 'user' })
      if (userMsgs.length === 0 || !this.currentConvId) return
      var title = userMsgs[0].text
      if (title.length > 20) title = title.substring(0, 20) + '...'
      var now = new Date()
      var timeStr = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0') + '-' + String(now.getDate()).padStart(2, '0') + ' ' + String(now.getHours()).padStart(2, '0') + ':' + String(now.getMinutes()).padStart(2, '0')
      var conv = {
        id: this.currentConvId,
        title: title,
        time: timeStr,
        count: this.messages.length,
        messages: JSON.parse(JSON.stringify(this.messages)),
        chatHistory: JSON.parse(JSON.stringify(this.chatHistory)),
        mode: this.currentMode
      }
      var idx = this.conversations.findIndex(function(c) { return c.id === conv.id })
      if (idx >= 0) {
        this.conversations.splice(idx, 1, conv)
      } else {
        this.conversations.unshift(conv)
      }
      if (this.conversations.length > 20) {
        this.conversations = this.conversations.slice(0, 20)
      }
      localStorage.setItem('ai_conversations', JSON.stringify(this.conversations))
    },
    /** 保留旧方法名兼容（清除时保存到历史） */
    saveCurrentConversation() {
      this.syncCurrentConvToList()
    },
    /** 加载指定历史会话 */
    loadConversation(conv) {
      // 先同步当前会话到列表
      this.syncCurrentConvToList()
      // 加载目标会话
      this.messages = JSON.parse(JSON.stringify(conv.messages))
      this.chatHistory = conv.chatHistory ? JSON.parse(JSON.stringify(conv.chatHistory)) : []
      this.currentConvId = conv.id
      if (conv.mode) this.currentMode = conv.mode
      this.showHistory = false
      this.$nextTick(function() { this.scrollToBottom() })
    },
    /** 删除指定历史会话 */
    deleteConversation(idx) {
      this.conversations.splice(idx, 1)
      localStorage.setItem('ai_conversations', JSON.stringify(this.conversations))
    },
    /** 清空全部历史会话 */
    clearAllHistory() {
      var self = this
      this.$confirm('确定清空全部历史会话？', '提示', { type: 'warning' }).then(function() {
        self.conversations = []
        localStorage.removeItem('ai_conversations')
        self.$message.success('已清空全部历史会话')
      }).catch(function() {})
    },
    /** 进入批量选择模式 */
    enterSelectMode() {
      this.isSelectMode = true
      this.selectedConvIds = []
    },
    /** 退出批量选择模式 */
    exitSelectMode() {
      this.isSelectMode = false
      this.selectedConvIds = []
    },
    /** 切换选中某个会话 */
    toggleSelectConv(id) {
      var idx = this.selectedConvIds.indexOf(id)
      if (idx >= 0) {
        this.selectedConvIds.splice(idx, 1)
      } else {
        this.selectedConvIds.push(id)
      }
    },
    /** 全选/取消全选 */
    toggleSelectAll() {
      if (this.isAllSelected) {
        this.selectedConvIds = []
      } else {
        this.selectedConvIds = this.conversations.map(function(c) { return c.id })
      }
    },
    /** 点击历史会话项：选择模式下切换选中，否则加载会话 */
    onHistoryItemClick(conv) {
      if (this.isSelectMode) {
        this.toggleSelectConv(conv.id)
      } else if (conv.id === this.currentConvId) {
        // 点击当前会话，直接返回聊天
        this.showHistory = false
      } else {
        this.loadConversation(conv)
      }
    },
    /** 批量删除选中的历史会话 */
    deleteSelected() {
      var self = this
      if (this.selectedConvIds.length === 0) return
      this.$confirm('确定删除选中的 ' + this.selectedConvIds.length + ' 个会话？', '提示', { type: 'warning' }).then(function() {
        // 如果删除了当前会话，重置聊天
        if (self.selectedConvIds.includes(self.currentConvId)) {
          self.messages = []
          self.chatHistory = []
          self.currentConvId = null
          self.messages.push({ role: 'ai', text: '对话已清空，有什么可以帮你的？' })
        }
        self.conversations = self.conversations.filter(function(c) {
          return !self.selectedConvIds.includes(c.id)
        })
        localStorage.setItem('ai_conversations', JSON.stringify(self.conversations))
        self.$message.success('已删除 ' + self.selectedConvIds.length + ' 个会话')
        self.exitSelectMode()
      }).catch(function() {})
    },
    /** 点击快捷问题发送消息 */
    sendQuickQuestion(question) {
      this.inputText = question
      this.sendMessage()
    },
    /** 发送消息：构建请求参数，调用后端AI接口，处理响应结果 */
    async sendMessage() {
      const text = this.inputText.trim()
      if (!text || this.isLoading) return

      // 添加用户消息
      this.messages.push({ role: 'user', text: text })
      this.chatHistory.push({ role: 'user', content: text })
      // 首次发消息时分配会话ID
      if (!this.currentConvId) this.currentConvId = Date.now().toString()
      this.inputText = ''
      this.isLoading = true
      this.scrollToBottom()

      try {
        // 构建请求参数
        const params = { message: text }
        if (this.currentMode === 'local') {
          params.mode = 'local'
          // 本地搜索不需要历史
        } else if (this.currentMode.startsWith('llm_')) {
          params.configId = this.currentMode.replace('llm_', '')
          // ★ 不发送历史，避免历史污染当前问题（每次提问独立）
        }

        // 调用后端AI接口
        const res = await this.$axios.post('/ai/chat', params)
        if (res.code === 200 && res.data) {
          const data = res.data
          const aiMsg = { role: 'ai' }

          if (data.mode === 'llm') {
            // 大模型模式：显示AI文本回复 + 搜索结果卡片
            aiMsg.text = data.text || '抱歉，我无法回答这个问题。'
            aiMsg.source = data.source || ''
            // 大模型模式下也展示搜索到的商品/帖子/表白墙卡片
            if (data.goods && data.goods.length > 0) {
              aiMsg.goods = data.goods
            }
            if (data.posts && data.posts.length > 0) {
              aiMsg.posts = data.posts
            }
            if (data.walls && data.walls.length > 0) {
              aiMsg.walls = data.walls
            }
            if (data.announcements && data.announcements.length > 0) {
              aiMsg.announcements = data.announcements
            }
            // 更新对话历史
            this.chatHistory.push({ role: 'assistant', content: data.text || '' })
          } else {
            // 本地搜索模式
            let hintText = data.text || ''
            if (data.llmError) {
              hintText = '<span style="color:#E6A23C;font-size:12px;">' + data.llmError + '</span><br>' + hintText
            }
            aiMsg.text = hintText
            aiMsg.source = data.source || '本地搜索'
            if (data.goods && data.goods.length > 0) {
              aiMsg.goods = data.goods
            }
            if (data.posts && data.posts.length > 0) {
              aiMsg.posts = data.posts
            }
            if (data.walls && data.walls.length > 0) {
              aiMsg.walls = data.walls
            }
            if (data.announcements && data.announcements.length > 0) {
              aiMsg.announcements = data.announcements
            }
            // 导航跳转处理
            if (data.navigate) {
              aiMsg.navigate = data.navigate
              aiMsg.navigateName = data.navigateName
            }
            // 本地模式不需要维护chatHistory给大模型
          }

          this.messages.push(aiMsg)
        } else {
          this.messages.push({
            role: 'ai',
            text: '抱歉，出了点问题，请稍后再试。'
          })
        }
      } catch (e) {
        this.messages.push({
          role: 'ai',
          text: '抱歉，网络出了点问题，请稍后再试。'
        })
      }
      this.isLoading = false
      this.scrollToBottom()
    },

    /** 跳转到商品详情页 */
    goGoodsDetail(goodsId) {
      this.$router.push('/goodsDetail/' + goodsId).catch(() => {})
    },
    /** 跳转到帖子详情页 */
    goPostDetail(postId) {
      this.$router.push('/postDetail/' + postId).catch(() => {})
    },
    /** 跳转到表白墙列表页 */
    goWallDetail(wallId) {
      // 表白墙没有独立详情页，跳转到表白墙列表
      this.$router.push('/wallList').catch(() => {})
    },
    /** 导航跳转到指定路由并关闭聊天面板 */
    navigateTo(route) {
      this.$router.push(route).catch(() => {})
      this.isOpen = false // 跳转后关闭聊天面板
    },
    /** 滚动消息区域到底部 */
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },
    /** 切换语音输入的开启/关闭 */
    toggleVoice() {
      if (this.isListening) {
        this.stopVoice()
      } else {
        this.startVoice()
      }
    },
    /** 启动语音识别（Web Speech API） */
    startVoice() {
      var SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
      if (!SpeechRecognition) {
        this.$message.warning('您的浏览器不支持语音输入，请使用Chrome浏览器')
        return
      }
      // 先清理旧实例
      this.stopVoice()
      var recognition = new SpeechRecognition()
      recognition.lang = 'zh-CN'
      recognition.continuous = true
      recognition.interimResults = true
      this.recognition = recognition
      var self = this
      var finalTranscript = ''
      recognition.onstart = function() {
        self.isListening = true
      }
      recognition.onresult = function(event) {
        var interim = ''
        for (var i = event.resultIndex; i < event.results.length; i++) {
          var transcript = event.results[i][0].transcript
          if (event.results[i].isFinal) {
            finalTranscript += transcript
          } else {
            interim += transcript
          }
        }
        self.inputText = finalTranscript + interim
      }
      recognition.onend = function() {
        self.isListening = false
        if (finalTranscript) {
          self.$nextTick(function() {
            var input = self.$el.querySelector('.ai-chat-input-area .el-input__inner')
            if (input) input.focus()
          })
        }
      }
      recognition.onerror = function(event) {
        self.isListening = false
        if (event.error === 'not-allowed') {
          self.$message.warning('请允许浏览器使用麦克风')
        } else if (event.error !== 'aborted' && event.error !== 'no-speech') {
          self.$message.warning('语音识别失败，请重试')
        }
      }
      try {
        recognition.start()
      } catch (e) {
        this.isListening = false
        this.$message.warning('语音启动失败，请重试')
      }
    },
    /** 停止语音识别 */
    stopVoice() {
      if (this.recognition) {
        try {
          this.recognition.abort()
        } catch (e) { /* ignore */ }
        this.recognition = null
      }
      this.isListening = false
    }
  }
}
</script>

<style scoped>
.ai-chat-wrapper {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
}

/* 浮动按钮 */
.ai-chat-fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #667eea);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
  transition: all 0.3s ease;
  font-size: 24px;
}
.ai-chat-fab:hover {
  transform: scale(1.1) translateY(-2px);
  box-shadow: 0 8px 28px rgba(64, 158, 255, 0.5);
}
.ai-chat-fab.active {
  background: linear-gradient(135deg, #F56C6C, #E6A23C);
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.4);
}
.ai-chat-fab.active:hover {
  transform: scale(1.1) translateY(-2px);
  box-shadow: 0 8px 28px rgba(245, 108, 108, 0.5);
}

/* 聊天面板 */
.ai-chat-panel {
  position: absolute;
  bottom: 68px;
  right: 0;
  width: 380px;
  height: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.ai-chat-panel.dark-mode {
  background: #1d1e1f;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.5);
}

/* 头部 */
.ai-chat-header {
  height: 48px;
  background: linear-gradient(135deg, #409EFF, #667eea);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  flex-shrink: 0;
}
.ai-chat-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
}
.ai-chat-header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.ai-chat-header i {
  font-size: 18px;
  transition: all 0.3s ease;
}
.ai-chat-header i:hover {
  transform: scale(1.15);
}
.ai-mode-select {
  width: 160px;
}
.ai-mode-select >>> .el-input__inner {
  background: rgba(255,255,255,0.2);
  border-color: rgba(255,255,255,0.3);
  color: #fff;
  font-size: 12px;
  height: 28px;
  line-height: 28px;
  border-radius: 14px;
  padding: 0 28px 0 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}
.ai-mode-select >>> .el-input__inner:hover {
  background: rgba(255,255,255,0.3);
  border-color: rgba(255,255,255,0.5);
}
.ai-mode-select >>> .el-input__suffix {
  right: 6px;
}
.ai-mode-select >>> .el-select__caret {
  color: #fff;
  font-size: 12px;
  line-height: 28px;
}
.ai-mode-select >>> .el-input__inner::placeholder {
  color: rgba(255,255,255,0.6);
}

/* 消息区域 */
.ai-chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
}

/* 历史会话列表 */
.ai-chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  background: #f5f7fa;
}
.dark-mode .ai-chat-history {
  background: #141414;
}
.ai-chat-history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin-bottom: 4px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fff;
  border: 1px solid #ebeef5;
}
.dark-mode .ai-chat-history-item {
  background: #262727;
  border-color: #4c4d4f;
}
.ai-chat-history-item:hover {
  border-color: #409EFF;
  background: #ecf5ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}
.dark-mode .ai-chat-history-item:hover {
  background: #1d3a5c;
  border-color: #409EFF;
}
.ai-chat-history-item.current {
  border-color: #409EFF;
  background: #ecf5ff;
}
.dark-mode .ai-chat-history-item.current {
  background: #1d3a5c;
  border-color: #409EFF;
}
.ai-chat-current-badge {
  display: inline-block;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 8px;
  background: linear-gradient(135deg, #409EFF, #667eea);
  color: #fff;
  vertical-align: middle;
  margin-right: 4px;
  line-height: 16px;
}
.ai-chat-history-info {
  flex: 1;
  min-width: 0;
}
.ai-chat-history-title {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dark-mode .ai-chat-history-title {
  color: #bfcbd9;
}
.ai-chat-history-meta {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}
.ai-chat-history-del {
  color: #C0C4CC;
  font-size: 14px;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.3s ease;
  flex-shrink: 0;
  margin-left: 8px;
}
.ai-chat-history-del:hover {
  color: #F56C6C;
  background: #fef0f0;
  transform: scale(1.15);
}
.ai-chat-history-check {
  flex-shrink: 0;
  margin-right: 8px;
  display: flex;
  align-items: center;
}
.ai-chat-history-item.selected {
  border-color: #409EFF;
  background: #ecf5ff;
}
.dark-mode .ai-chat-history-item.selected {
  background: #1d3a5c;
  border-color: #409EFF;
}
.ai-chat-select-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  border-top: 1px solid #ebeef5;
  background: #fff;
  flex-shrink: 0;
}
.dark-mode .ai-chat-select-bar {
  border-color: #4c4d4f;
  background: #1d1e1f;
}
.ai-chat-select-count {
  font-size: 13px;
  color: #606266;
}
.dark-mode .ai-chat-select-count {
  color: #bfcbd9;
}
.dark-mode .ai-chat-messages {
  background: #141414;
}

.ai-chat-msg {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}
.ai-chat-msg.user {
  flex-direction: row-reverse;
}

.ai-chat-msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
  color: #fff;
  transition: transform 0.3s ease;
}
.ai-chat-msg-avatar:hover {
  transform: scale(1.1);
}
.ai-chat-msg.ai .ai-chat-msg-avatar {
  background: linear-gradient(135deg, #409EFF, #667eea);
}
.ai-chat-msg.user .ai-chat-msg-avatar {
  background: linear-gradient(135deg, #67C23A, #409EFF);
}

.ai-chat-msg-content {
  max-width: 280px;
  min-width: 0;
  margin: 0 10px;
}
.ai-chat-msg.user .ai-chat-msg-content {
  text-align: right;
}

.ai-chat-text {
  display: inline-block;
  padding: 8px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.8;
  word-break: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
  max-width: 100%;
  box-sizing: border-box;
  transition: all 0.3s ease;
}
.ai-chat-msg.ai .ai-chat-text {
  background: #fff;
  color: #303133;
  border-top-left-radius: 4px;
  text-align: left;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.dark-mode .ai-chat-msg.ai .ai-chat-text {
  background: #262727;
  color: #bfcbd9;
}
.ai-chat-msg.user .ai-chat-text {
  background: linear-gradient(135deg, #409EFF, #667eea);
  color: #fff;
  border-top-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

/* 商品推荐卡片 */
.ai-chat-goods-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.ai-chat-goods-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 10px;
  padding: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
}
.dark-mode .ai-chat-goods-card {
  background: #262727;
  border-color: #4c4d4f;
}
.ai-chat-goods-card:hover {
  border-color: #409EFF;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}
.ai-chat-goods-img {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  margin-right: 10px;
}
.ai-chat-goods-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.ai-chat-goods-no-img {
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #C0C4CC;
  font-size: 20px;
}
.dark-mode .ai-chat-goods-no-img {
  background: #1d1e1f;
}
.ai-chat-goods-info {
  flex: 1;
  min-width: 0;
}
.ai-chat-goods-name {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dark-mode .ai-chat-goods-name {
  color: #bfcbd9;
}
.ai-chat-goods-price {
  font-size: 14px;
  color: #F56C6C;
  font-weight: bold;
  margin-top: 4px;
}

/* 帖子推荐卡片 */
.ai-chat-posts-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.ai-chat-post-card {
  background: #fff;
  border-radius: 10px;
  padding: 12px 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
}
.dark-mode .ai-chat-post-card {
  background: #262727;
  border-color: #4c4d4f;
}
.ai-chat-post-card:hover {
  border-color: #409EFF;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}
.ai-chat-post-title {
  font-size: 13px;
  color: #303133;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dark-mode .ai-chat-post-title {
  color: #bfcbd9;
}
.ai-chat-post-detail {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ai-chat-post-meta {
  display: flex;
  gap: 12px;
  margin-top: 4px;
  font-size: 11px;
  color: #999;
}

/* 打字动画 */
.ai-chat-walls-list {
  margin-top: 10px;
}
.ai-chat-walls-title {
  font-size: 12px;
  color: #E6A23C;
  font-weight: 600;
  margin-bottom: 6px;
}
.ai-chat-wall-card {
  background: #fff;
  border-radius: 10px;
  padding: 12px 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
  margin-bottom: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
}
.dark-mode .ai-chat-wall-card {
  background: #262727;
  border-color: #4c4d4f;
}
.ai-chat-wall-card:hover {
  border-color: #E6A23C;
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.15);
  transform: translateY(-2px);
}
.ai-chat-wall-content {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.dark-mode .ai-chat-wall-content {
  color: #bfcbd9;
}
.ai-chat-wall-meta {
  display: flex;
  gap: 12px;
  margin-top: 4px;
  font-size: 11px;
  color: #999;
}
.ai-chat-announcements-list {
  margin-top: 6px;
}
.ai-chat-announcements-title {
  font-size: 13px;
  font-weight: 600;
  color: #E6A23C;
  margin-bottom: 6px;
}
.ai-chat-announcement-card {
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 6px;
  cursor: default;
  transition: all 0.3s ease;
}
.ai-chat-announcement-card:hover {
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.12);
}
.ai-chat-announcement-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}
.ai-chat-announcement-content {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
  line-height: 1.4;
}
.ai-chat-announcement-meta {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}
.ai-chat-navigate {
  margin-top: 8px;
}
.ai-chat-source {
  margin-top: 6px;
  font-size: 11px;
  color: #b0b4bb;
  display: flex;
  align-items: center;
  gap: 3px;
}
.ai-chat-source::before {
  content: '●';
  font-size: 6px;
}

/* 打字动画 */
.ai-chat-typing {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 10px 14px;
  background: #fff;
  border-radius: 12px;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.dark-mode .ai-chat-typing {
  background: #262727;
}
.ai-chat-typing span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #409EFF;
  animation: aiTyping 1.2s infinite;
}
.ai-chat-typing span:nth-child(2) {
  animation-delay: 0.2s;
}
.ai-chat-typing span:nth-child(3) {
  animation-delay: 0.4s;
}
@keyframes aiTyping {
  0%, 60%, 100% { opacity: 0.3; transform: scale(0.8); }
  30% { opacity: 1; transform: scale(1.2); }
}

/* 快捷问题 */
.ai-chat-quick {
  padding: 8px 16px;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}
.dark-mode .ai-chat-quick {
  border-color: #4c4d4f;
}
.ai-chat-quick-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}
.ai-chat-quick-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.ai-chat-quick-item {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 14px;
  background: #ecf5ff;
  color: #409EFF;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}
.dark-mode .ai-chat-quick-item {
  background: #1d3a5c;
  color: #66b1ff;
}
.ai-chat-quick-item:hover {
  background: #409EFF;
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

/* 输入区域 */
.ai-chat-input-area {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
  background: #fff;
  border-radius: 0 0 16px 16px;
}
.dark-mode .ai-chat-input-area {
  border-color: #4c4d4f;
  background: #1d1e1f;
}

/* 语音按钮（输入框内suffix位置） */
.ai-chat-input-area >>> .el-input__suffix {
  pointer-events: auto !important;
}
.ai-chat-voice-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  cursor: pointer;
}
.ai-chat-voice-icon {
  font-size: 16px;
  color: #909399;
  transition: color 0.3s ease;
  line-height: 28px;
}
.ai-chat-voice-icon:hover {
  color: #409EFF;
}
.ai-chat-voice-icon.listening {
  color: #F56C6C;
}
.ai-chat-voice-pulse {
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  border-radius: 50%;
  border: 2px solid #F56C6C;
  animation: voicePulse 1.2s infinite;
  pointer-events: none;
}
@keyframes voicePulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

/* 动画 */
.ai-chat-slide-enter-active,
.ai-chat-slide-leave-active {
  transition: all 0.3s ease;
}
.ai-chat-slide-enter,
.ai-chat-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* 滚动条美化 */
.ai-chat-messages::-webkit-scrollbar {
  width: 4px;
}
.ai-chat-messages::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 2px;
}
.ai-chat-messages::-webkit-scrollbar-track {
  background: transparent;
}
</style>

<style>
/* 下拉弹窗需要全局样式确保z-index高于聊天面板 */
.ai-mode-popper {
  z-index: 10001 !important;
  min-width: 220px !important;
}
.ai-mode-popper .el-select-dropdown__item {
  height: 34px;
  line-height: 34px;
  padding: 0 16px;
}
.ai-mode-popper .el-select-dropdown__item span {
  font-size: 13px;
}
</style>
