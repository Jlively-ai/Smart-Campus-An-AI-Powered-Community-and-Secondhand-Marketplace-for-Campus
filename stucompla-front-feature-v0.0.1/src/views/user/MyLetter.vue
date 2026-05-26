<!--
  组件名：MyLetter
  功能描述：消息中心页
  主要职责：
    1. 三标签页（私信/通知/互动消息）
    2. AI伴侣聊天弹窗
    3. 历史会话管理
    4. 私信对话（含图片发送）
    5. 禁言检查
-->
<template>
  <div>
    <el-card>
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
        <span style="font-weight:bold;font-size:16px;">消息中心</span>
        <div style="display:flex;gap:8px;align-items:center;">
          <search-panel v-model="searchKeyword" module="letter" placeholder="搜索消息内容" size="small" input-style="width:180px;" @search="onSearch" @clear="onSearchClear"></search-panel>
          <el-button type="primary" size="small" icon="el-icon-search" @click="onSearch">搜索</el-button>
          <el-button v-if="activeTab === 'letter' || activeTab === 'notification' || interactionFilter !== 'sent_comments' && interactionFilter !== 'received_comments'" type="text" @click="markAllRead">全部已读</el-button>
        </div>
      </div>
      <el-tabs v-model="activeTab" @tab-click="onTabChange">
        <el-tab-pane name="letter">
          <span slot="label"><i class="el-icon-message"></i> 私信 <el-badge v-if="unreadCounts.letter > 0" :value="unreadCounts.letter" :max="99" style="margin-left:4px;" /></span>
          <!-- 私信子标签：消息 / 联系人 -->
          <el-tabs v-model="letterSubTab" type="card" @tab-click="onLetterSubTabChange">
            <el-tab-pane name="messages">
              <span slot="label">消息</span>
              <!-- AI伴侣入口 -->
              <div class="msg-item" style="background:linear-gradient(135deg,#f0f5ff,#e8f0fe);border:1px solid #d0e2ff;border-radius:8px;margin-bottom:10px;" @click="openAiCompanion">
                <div class="msg-icon" style="background:linear-gradient(135deg,#409EFF,#66b1ff);"><i class="el-icon-s-custom"></i></div>
                <div class="msg-content">
                  <div class="msg-text"><span style="color:#409EFF;font-weight:600;">AI伴侣</span><el-tag size="mini" type="primary" style="margin-left:4px;">在线</el-tag> 随时陪我聊天~</div>
                  <div class="msg-time" style="color:#409EFF;">点击开始对话</div>
                </div>
                <i class="el-icon-chat-dot-round" style="color:#409EFF;font-size:18px;"></i>
              </div>
              <!-- 联系人消息 -->
              <div v-if="normalSessions.length > 0" style="margin-bottom:8px;">
                <div style="font-size:13px;color:#909399;padding:6px 10px 2px;">联系人消息</div>
                <div v-for="session in normalSessions" :key="session.sessionId" class="msg-item" @click="openSession(session)">
                  <div class="msg-avatar-wrap">
                    <el-avatar v-if="session.otherAvatar" :src="session.otherAvatar" :size="40" class="msg-avatar"></el-avatar>
                    <el-avatar v-else :size="40" class="msg-avatar" style="background:#67C23A;">{{ (session.otherNickname || '对')[0] }}</el-avatar>
                    <el-badge v-if="session.unread > 0" :value="session.unread" :max="99" class="msg-avatar-badge" />
                  </div>
                  <div class="msg-content">
                    <div class="msg-text"><span style="cursor:pointer;color:#409EFF;" @click.stop="goProfile(session.otherId)">{{ session.otherNickname || '对方' }}</span><el-tag v-if="mutualFollowerIds.indexOf(String(session.otherId)) !== -1" size="mini" type="success" style="margin-left:4px;">互相关注</el-tag>{{ session.otherNickname ? '：' : '' }}{{ session.lastContent || '暂无消息' }}</div>
                    <div class="msg-time">{{ session.lastTime | formatTime }}</div>
                  </div>
                </div>
              </div>
              <!-- 陌生人消息 -->
              <div v-if="strangerSessions.length > 0">
                <div style="font-size:13px;color:#E6A23C;padding:6px 10px 2px;display:flex;align-items:center;"><i class="el-icon-warning-outline" style="margin-right:4px;"></i>陌生人消息</div>
                <div v-for="session in strangerSessions" :key="session.sessionId" class="msg-item" @click="openSession(session)">
                  <div class="msg-avatar-wrap">
                    <el-avatar v-if="session.otherAvatar" :src="session.otherAvatar" :size="40" class="msg-avatar"></el-avatar>
                    <el-avatar v-else :size="40" class="msg-avatar" style="background:#E6A23C;">{{ (session.otherNickname || '对')[0] }}</el-avatar>
                    <el-badge v-if="session.unread > 0" :value="session.unread" :max="99" class="msg-avatar-badge" />
                  </div>
                  <div class="msg-content">
                    <div class="msg-text"><span style="cursor:pointer;color:#409EFF;" @click.stop="goProfile(session.otherId)">{{ session.otherNickname || '对方' }}</span>{{ session.otherNickname ? '：' : '' }}{{ session.lastContent || '暂无消息' }}</div>
                    <div class="msg-time">{{ session.lastTime | formatTime }}</div>
                  </div>
                </div>
              </div>
              <!-- 无消息 -->
              <div v-if="normalSessions.length === 0 && strangerSessions.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无私信</div>
            </el-tab-pane>
            <el-tab-pane name="contacts">
              <span slot="label">联系人</span>
              <div v-if="contactsList.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无互关联系人</div>
              <div v-for="contact in contactsList" :key="contact.userId" class="msg-item" @click="startConversation(contact)">
                <el-avatar v-if="contact.avatar" :src="contact.avatar" :size="36" style="margin-right:12px;flex-shrink:0;cursor:pointer;" @click.native.stop="goProfile(contact.userId)"></el-avatar>
                <el-avatar v-else :size="36" style="margin-right:12px;flex-shrink:0;background:#409EFF;cursor:pointer;" @click.native.stop="goProfile(contact.userId)">{{ (contact.nickname || contact.username || '用')[0] }}</el-avatar>
                <div class="msg-content">
                  <div class="msg-text">
                    <span style="cursor:pointer;color:#409EFF;" @click.stop="goProfile(contact.userId)">{{ contact.nickname || contact.username || '用户' }}</span>
                    <el-tag size="mini" type="success" style="margin-left:6px;">互相关注</el-tag>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
        <el-tab-pane name="notification">
          <span slot="label"><i class="el-icon-bell"></i> 通知 <el-badge v-if="notificationUnreadTotal > 0" :value="notificationUnreadTotal" :max="99" style="margin-left:4px;" /></span>
          <div style="margin-bottom:12px;">
            <el-radio-group v-model="notificationFilter" size="small" @change="onNotificationFilterChange">
              <el-radio-button label="system">系统通知<el-badge v-if="unreadCounts.system > 0" :value="unreadCounts.system" :max="99" style="margin-left:4px;" /></el-radio-button>
              <el-radio-button label="order">订单通知<el-badge v-if="unreadCounts.order > 0" :value="unreadCounts.order" :max="99" style="margin-left:4px;" /></el-radio-button>
              <el-radio-button label="logistics">物流信息<el-badge v-if="unreadCounts.logistics > 0" :value="unreadCounts.logistics" :max="99" style="margin-left:4px;" /></el-radio-button>
            </el-radio-group>
          </div>
          <div v-if="notificationList.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无{{ notificationFilter === 'system' ? '系统通知' : notificationFilter === 'order' ? '订单通知' : '物流信息' }}</div>
          <div v-for="item in notificationList" :key="item.letterId" class="msg-item" :class="{ 'unread-bg': item.letterStatus === 0 }" @click="markNotificationRead(item)">
            <div class="msg-dot" v-if="item.letterStatus === 0"></div>
            <div style="width:36px;height:36px;border-radius:50%;display:flex;align-items:center;justify-content:center;color:#fff;margin-right:12px;flex-shrink:0;" :style="{ background: notificationFilter === 'system' ? '#E6A23C' : notificationFilter === 'order' ? '#409EFF' : '#67C23A' }">
              <i :class="notificationFilter === 'system' ? 'el-icon-bell' : notificationFilter === 'order' ? 'el-icon-shopping-bag-1' : 'el-icon-truck'"></i>
            </div>
            <div style="flex:1;min-width:0;">
              <div style="font-size:14px;color:#303133;">{{ item.letterDetail }}</div>
              <div style="font-size:12px;color:#999;margin-top:4px;">{{ item.createTime | formatTime }}</div>
            </div>
          </div>
          <el-pagination v-if="notificationTotal > notificationPageSize" style="margin-top:15px;text-align:center;" @current-change="p => { notificationPageNum = p; loadNotifications() }" :current-page="notificationPageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="notificationPageSize" :total="notificationTotal" layout="total, sizes, prev, pager, next" small @size-change="handleNotificationSizeChange"></el-pagination>
        </el-tab-pane>
        <el-tab-pane name="interaction">
          <span slot="label"><i class="el-icon-chat-dot-round"></i> 互动消息 <el-badge v-if="unreadCounts.interaction > 0" :value="unreadCounts.interaction" :max="99" style="margin-left:4px;" /></span>
          <div style="margin-bottom:12px;">
            <el-radio-group v-model="interactionFilter" size="small" @change="onFilterChange">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="follow">新关注<el-badge v-if="unreadCounts.follow > 0" :value="unreadCounts.follow" :max="99" style="margin-left:4px;" /></el-radio-button>
              <el-radio-button label="like">点赞与收藏<el-badge v-if="unreadCounts.like > 0" :value="unreadCounts.like" :max="99" style="margin-left:4px;" /></el-radio-button>
              <el-radio-button label="mention">提及<el-badge v-if="unreadCounts.mention > 0" :value="unreadCounts.mention" :max="99" style="margin-left:4px;" /></el-radio-button>
              <el-radio-button label="comment_received">收到的评论<el-badge v-if="unreadCounts.comment > 0" :value="unreadCounts.comment" :max="99" style="margin-left:4px;" /></el-radio-button>
              <el-radio-button label="sent_comments">发出的评论</el-radio-button>
            </el-radio-group>
          </div>

          <!-- 评论列表模式（发出的/收到的） -->
          <div v-if="interactionFilter === 'sent_comments' || interactionFilter === 'comment_received'">
            <div v-if="commentList.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无{{ interactionFilter === 'sent_comments' ? '发出的' : '收到的' }}评论</div>
            <div v-for="item in commentList" :key="item.commentId" class="msg-item" @click="item.postId && $router.push('/postDetail/' + item.postId).catch(function(){})">
              <div class="msg-icon" :style="{ background: interactionFilter === 'sent_comments' ? '#409EFF' : '#E6A23C' }"><i class="el-icon-chat-dot-round"></i></div>
              <div class="msg-content">
                <div class="msg-text">
                  <template v-if="interactionFilter === 'comment_received'">
                    <strong style="cursor:pointer;color:#409EFF;" @click.stop="item.userId && $router.push('/userProfile/' + item.userId).catch(function(){})">{{ item.nickname || '匿名用户' }}</strong> 评论了你的帖子「<span style="cursor:pointer;color:#409EFF;" @click.stop="item.postId && $router.push('/postDetail/' + item.postId).catch(function(){})">{{ item.postTitle || '帖子' }}</span>」
                  </template>
                  <template v-else>
                    你评论了「<span style="cursor:pointer;color:#409EFF;" @click.stop="item.postId && $router.push('/postDetail/' + item.postId).catch(function(){})">{{ item.postTitle || '帖子' }}</span>」
                  </template>
                </div>
                <div style="margin-top:4px;color:#666;font-size:13px;">{{ item.text }}</div>
                <div class="msg-time">{{ item.createTime | formatTime }}</div>
              </div>
              <el-button size="mini" type="danger" plain v-if="interactionFilter === 'sent_comments'" @click.stop="deleteComment(item.commentId)">删除</el-button>
            </div>
            <el-pagination v-if="commentTotal > pageSize" style="margin-top:15px;text-align:center;" @current-change="p => { commentPage = p; loadComments() }" :current-page="commentPage" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="commentTotal" layout="total, sizes, prev, pager, next" small @size-change="handleCommentSizeChange"></el-pagination>
          </div>

          <!-- 消息列表模式（点赞/关注/收到的评论通知/全部） -->
          <div v-else>
            <div v-if="interactionList.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无互动消息</div>
            <div v-for="msg in interactionList" :key="msg.letterId" class="msg-item" :class="{ unread: msg.letterStatus === 0 }" @click="handleInteractionClick(msg)">
              <div class="msg-dot" v-if="msg.letterStatus === 0"></div>
              <div class="msg-icon" :style="{ background: getInteractionIconBg(msg) }"><i :class="getInteractionIcon(msg)"></i></div>
              <div class="msg-content">
                <div class="msg-text" v-html="renderInteractionMessage(msg)"></div>
                <div v-if="msg._commentData && msg._commentData.text" style="margin-top:4px;color:#666;font-size:13px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{{ msg._commentData.text }}</div>
                <div class="msg-time">{{ msg.createTime | formatTime }}</div>
              </div>
            </div>
            <el-pagination v-if="interactionTotal > pageSize" style="margin-top:15px;text-align:center;" @current-change="p => { interactionPage = p; loadInteractions() }" :current-page="interactionPage" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="interactionTotal" layout="total, sizes, prev, pager, next" small @size-change="handleInteractionSizeChange"></el-pagination>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog :title="'与 ' + (currentSessionNickname || '对方') + ' 的对话'" :visible.sync="letterDialogVisible" width="500px" @close="onSessionClose">
      <div style="max-height:400px;overflow-y:auto;" ref="chatBox">
        <div v-for="l in letterList" :key="l.letterId" :style="{textAlign: l.isMine ? 'right' : 'left', margin: '10px 0'}">
          <div v-if="!l.isMine" style="font-size:12px;color:#999;margin-bottom:2px;">{{ currentSessionNickname || '对方' }}</div>
          <div v-if="!isImageUrl(l.letterDetail)" :class="['chat-bubble', l.isMine ? 'chat-bubble-right' : 'chat-bubble-left']">{{ l.letterDetail }}</div>
          <el-image v-else :src="l.letterDetail" style="max-width:200px;max-height:200px;border-radius:8px;cursor:pointer;" fit="cover" :preview-src-list="[l.letterDetail]"></el-image>
          <div style="font-size:11px;color:#999;margin-top:2px;">{{ l.createTime | formatTime }}</div>
        </div>
      </div>
      <div style="margin-top:10px;display:flex;gap:10px;align-items:flex-end;">
        <el-input v-model="replyContent" placeholder="输入消息..." @keyup.enter.native="sendLetter" style="flex:1;"></el-input>
        <el-upload action="/dev_api/image/upload" :headers="uploadHeaders" name="files" :show-file-list="false" :on-success="handleChatImageSuccess" :before-upload="beforeChatImageUpload" accept="image/*">
          <el-button size="small" icon="el-icon-picture-outline" title="发送图片"></el-button>
        </el-upload>
        <el-button type="primary" @click="sendLetter"><i v-if="isMuted" class="el-icon-warning" style="color:red;margin-right:4px;"></i>发送</el-button>
      </div>
      <el-alert v-if="isMuted" type="error" :closable="false" style="margin-top:10px;">
        <template slot="title">您当前已被禁言，无法发送消息{{ muteReason ? '，原因：' + muteReason : '' }}</template>
      </el-alert>
    </el-dialog>

    <!-- AI伴侣聊天弹窗 -->
    <el-dialog title="AI伴侣" :visible.sync="aiCompanionVisible" width="560px" @close="onAiCompanionClose" top="8vh">
      <div style="height:460px;display:flex;">
        <!-- 左侧历史会话列表 -->
        <div class="companion-history-panel" v-if="companionShowHistory">
          <div class="companion-history-header">
            <span>历史会话</span>
            <div class="companion-history-actions">
              <template v-if="!companionSelectMode">
                <el-tooltip content="选择删除" placement="bottom">
                  <i class="el-icon-finished companion-history-action-btn" @click="enterCompanionSelectMode"></i>
                </el-tooltip>
                <el-tooltip content="清空全部" placement="bottom">
                  <i class="el-icon-delete companion-history-action-btn" @click="clearAllCompanionHistory"></i>
                </el-tooltip>
              </template>
              <template v-else>
                <el-button type="text" size="mini" style="color:#409EFF;padding:0;" @click="toggleCompanionSelectAll">{{ companionIsAllSelected ? '取消全选' : '全选' }}</el-button>
                <el-button type="text" size="mini" style="color:#909399;padding:0;" @click="exitCompanionSelectMode">取消</el-button>
              </template>
            </div>
          </div>
          <div class="companion-history-list">
            <div v-if="companionSessionHistory.length === 0" style="text-align:center;color:#999;padding:40px 0;font-size:13px;">暂无历史会话</div>
            <div v-for="(session, idx) in companionSessionHistory" :key="session.id" class="companion-history-item" :class="{ current: session.id === companionCurrentSessionId, selected: companionSelectMode && companionSelectedIds.includes(session.id) }" @click="onCompanionHistoryItemClick(session)">
              <div v-if="companionSelectMode" class="companion-history-check" @click.stop>
                <el-checkbox :value="companionSelectedIds.includes(session.id)" @change="toggleCompanionSelectConv(session.id)"></el-checkbox>
              </div>
              <div class="companion-history-info">
                <div class="companion-history-title">
                  <span v-if="session.id === companionCurrentSessionId" class="companion-current-badge">当前</span>
                  {{ session.preview || '会话' }}
                </div>
                <div class="companion-history-meta">{{ session.time }} · {{ session.messages ? session.messages.length : 0 }}条消息</div>
              </div>
              <i v-if="!companionSelectMode" class="el-icon-delete companion-history-del" @click.stop="deleteCompanionSession(idx)"></i>
            </div>
          </div>
          <!-- 选择模式底部操作栏 -->
          <div v-if="companionSelectMode" class="companion-select-bar">
            <span class="companion-select-count">已选 {{ companionSelectedIds.length }} 项</span>
            <el-button type="danger" size="mini" :disabled="companionSelectedIds.length === 0" @click="deleteCompanionSelected">删除所选</el-button>
          </div>
        </div>
        <!-- 右侧聊天区域 -->
        <div style="flex:1;display:flex;flex-direction:column;min-width:0;">
          <div style="margin-bottom:8px;display:flex;align-items:center;gap:8px;">
            <span style="font-size:13px;color:#606266;">模型：</span>
            <el-select v-model="companionConfigId" size="mini" style="flex:1;" placeholder="选择模型">
              <el-option v-for="cfg in companionConfigs" :key="cfg.id" :label="cfg.configName + ' - ' + cfg.modelName" :value="cfg.id">
                <div style="display:flex;justify-content:space-between;align-items:center;width:100%;">
                  <span style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:160px;">{{ cfg.configName }}</span>
                  <span style="color:#8492a6;font-size:11px;flex-shrink:0;margin-left:6px;">{{ cfg.modelName }}{{ cfg.isActive ? ' ★' : '' }}</span>
                </div>
              </el-option>
            </el-select>
            <el-button size="mini" type="primary" icon="el-icon-plus" @click="newCompanionSession">新建</el-button>
            <el-button size="mini" type="warning" icon="el-icon-delete" @click="clearCompanionSession">清除</el-button>
            <el-tooltip content="历史会话" placement="bottom" v-if="!companionShowHistory">
              <el-button size="mini" type="text" style="color:#409EFF;" @click="companionShowHistory = true"><i class="el-icon-time"></i> 历史</el-button>
            </el-tooltip>
            <el-tooltip content="返回对话" placement="bottom" v-else>
              <el-button size="mini" type="text" style="color:#409EFF;" @click="companionShowHistory = false"><i class="el-icon-back"></i> 返回</el-button>
            </el-tooltip>
          </div>
          <div ref="aiChatBox" style="flex:1;overflow-y:auto;padding:8px 0;">
            <div v-for="(msg, idx) in aiMessages" :key="idx" :style="{textAlign: msg.role === 'user' ? 'right' : 'left', margin: '10px 0'}">
              <div v-if="msg.role === 'assistant'" style="font-size:12px;color:#409EFF;margin-bottom:2px;">AI伴侣</div>
              <div :class="['chat-bubble', msg.role === 'user' ? 'chat-bubble-right' : 'chat-bubble-left']">{{ msg.content }}</div>
            </div>
            <div v-if="aiCompanionLoading" style="text-align:left;margin:10px 0;">
              <el-tag type="info" effect="light">正在思考...</el-tag>
            </div>
          </div>
          <div style="margin-top:10px;display:flex;gap:8px;">
            <el-input v-model="aiCompanionInput" placeholder="和AI伴侣说点什么..." @keyup.enter.native="sendAiCompanionMessage" style="flex:1;" :disabled="aiCompanionLoading"></el-input>
            <el-button type="primary" @click="sendAiCompanionMessage" :loading="aiCompanionLoading">发送</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import SearchPanel from '@/components/SearchPanel.vue'
export default {
  name: 'MyLetter',
  components: { SearchPanel },
  data() {
    return {
      activeTab: 'letter',
      letterSubTab: 'messages',
      interactionFilter: 'all',
      notificationFilter: 'system',
      aiCompanionVisible: false,
      aiCompanionInput: '',
      aiCompanionLoading: false,
      aiMessages: [],
      companionConfigs: [],
      companionConfigId: null,
      companionSessionHistory: [],
      companionCurrentSessionId: null,
      companionShowHistory: false,
      companionSelectMode: false,
      companionSelectedIds: [],
      sessionList: [],
      interactionList: [],
      interactionPage: 1,
      interactionTotal: 0,
      commentList: [],
      commentPage: 1,
      commentTotal: 0,
      unreadCounts: { letter: 0, interaction: 0, comment: 0, system: 0, like: 0, follow: 0, mention: 0 },
      pageSize: 10,
      letterDialogVisible: false,
      currentSession: '',
      currentSessionNickname: '',
      letterList: [],
      replyContent: '',
      receiverId: null,
      notificationList: [],
      notificationTotal: 0,
      notificationPageNum: 1,
      notificationPageSize: 10,
      isMuted: false,
      muteReason: '',
      uploadHeaders: { Authorization: sessionStorage.getItem('token') || '' },
      searchKeyword: '',
      originalSessionList: [],
      originalNotificationList: [],
      originalInteractionList: [],
      originalCommentList: [],
      contactsList: [],
      mutualFollowerIds: [],
      senderInfoCache: {}
    }
  },
  computed: {
    normalSessions: function() {
      var self = this
      return this.sessionList.filter(function(s) {
        return self.mutualFollowerIds.indexOf(String(s.otherId)) !== -1 && !self.isAiCompanionSession(s)
      })
    },
    strangerSessions: function() {
      var self = this
      return this.sessionList.filter(function(s) {
        return self.mutualFollowerIds.indexOf(String(s.otherId)) === -1 && !self.isAiCompanionSession(s)
      })
    },
    notificationUnreadTotal: function() {
      return (this.unreadCounts.system || 0) + (this.unreadCounts.order || 0) + (this.unreadCounts.logistics || 0)
    },
    // 是否全选
    companionIsAllSelected: function() {
      return this.companionSessionHistory.length > 0 && this.companionSelectedIds.length === this.companionSessionHistory.length
    }
  },
  watch: {
    '$route.query.tab': function(newTab) {
      if (newTab === 'letter' || newTab === 'notification' || newTab === 'interaction') {
        this.activeTab = newTab
        if (newTab === 'notification') {
          this.loadNotifications()
        } else if (newTab === 'interaction') {
          this.loadCurrentFilter()
        } else if (newTab === 'letter') {
          this.loadSessions()
          this.loadContacts()
        }
      }
    },
    companionConfigId: function(val) {
      if (val) localStorage.setItem('ai_companion_configId', val)
    }
  },
  created() {
    // 设置全局路由引用，供 v-html 中的 onclick 调用
    if (typeof window !== 'undefined') {
      window.__letterVueRouter = this.$router
    }
    // 读取路由参数，自动切换到对应标签页
    var tab = this.$route.query.tab
    if (tab === 'letter' || tab === 'notification' || tab === 'interaction') {
      this.activeTab = tab
    }
    var self = this
    this.loadSessions().then(function() {
      self.loadUnreadCounts()
    })
    this.loadContacts()
    this.checkMuteStatus()
    // 加载当前标签页的数据
    if (this.activeTab === 'notification') {
      this.loadNotifications()
    } else if (this.activeTab === 'interaction') {
      this.loadCurrentFilter()
    }
  },
  methods: {
    formatTime: function(time) {
      if (!time) return ''
      var d = new Date(time)
      if (isNaN(d.getTime())) return time
      var pad = function(n) { return String(n).padStart(2, '0') }
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    },
    goProfile: function(userId) {
      this.$router.push('/userProfile/' + userId).catch(function() {})
    },
    goDetail: function(type, id) {
      if (type === 'post' && id) {
        this.$router.push('/postDetail/' + id).catch(function() {})
      } else if (type === 'wall' && id) {
        this.$router.push('/wallDetail/' + id).catch(function() {})
      } else if (type === 'goods' && id) {
        this.$router.push('/goodsDetail/' + id).catch(function() {})
      }
    },
    onTabChange: function(tab) {
      // 切换tab时更新URL，确保返回时能恢复正确的tab
      this.$router.replace({ path: '/myLetter', query: { tab: tab.name } }).catch(function() {})
      if (tab.name === 'letter') {
        this.loadSessions()
        this.loadContacts()
      } else if (tab.name === 'notification') {
        this.loadNotifications()
      } else if (tab.name === 'interaction') {
        this.loadCurrentFilter()
      }
    },
    onLetterSubTabChange: function() {
      if (this.letterSubTab === 'contacts') {
        this.loadContacts()
      }
    },
    onFilterChange: function() {
      this.interactionPage = 1
      this.commentPage = 1
      this.loadCurrentFilter()
    },
    handleNotificationSizeChange: function(val) { this.notificationPageSize = val; this.notificationPageNum = 1; this.loadNotifications() },
    handleCommentSizeChange: function(val) { this.pageSize = val; this.commentPage = 1; this.loadComments() },
    handleInteractionSizeChange: function(val) { this.pageSize = val; this.interactionPage = 1; this.loadInteractions() },
    loadCurrentFilter: function() {
      if (this.interactionFilter === 'sent_comments' || this.interactionFilter === 'comment_received') {
        this.loadComments()
      } else {
        this.loadInteractions()
      }
    },
    getInteractionIcon: function(msg) {
      var type = msg.messageType || ''
      if (type === 'sent_comment') return 'el-icon-s-promotion'
      if (type === 'comment_received') return 'el-icon-chat-dot-round'
      if (type === 'like') return 'el-icon-thumb'
      if (type === 'collect') return 'el-icon-star-on'
      if (type === 'follow') return 'el-icon-user'
      if (type === 'comment') return 'el-icon-chat-dot-round'
      if (type === 'mention') return 'el-icon-user'
      if (type === 'system') return 'el-icon-bell'
      var detail = msg.letterDetail || ''
      if (detail.indexOf('赞了') !== -1) return 'el-icon-thumb'
      if (detail.indexOf('收藏') !== -1) return 'el-icon-star-on'
      if (detail.indexOf('关注') !== -1) return 'el-icon-user'
      if (detail.indexOf('评论') !== -1 || detail.indexOf('回复') !== -1) return 'el-icon-chat-dot-round'
      if (detail.indexOf('提到') !== -1) return 'el-icon-user'
      return 'el-icon-bell'
    },
    getInteractionIconBg: function(msg) {
      var type = msg.messageType || ''
      if (type === 'sent_comment') return '#409EFF'
      if (type === 'comment_received') return '#E6A23C'
      if (type === 'like') return '#F56C6C'
      if (type === 'collect') return '#E6A23C'
      if (type === 'follow') return '#409EFF'
      if (type === 'comment') return '#E6A23C'
      if (type === 'mention') return '#9C27B0'
      if (type === 'system') return '#909399'
      var detail = msg.letterDetail || ''
      if (detail.indexOf('赞了') !== -1) return '#F56C6C'
      if (detail.indexOf('收藏') !== -1) return '#E6A23C'
      if (detail.indexOf('关注') !== -1) return '#409EFF'
      if (detail.indexOf('评论') !== -1 || detail.indexOf('回复') !== -1) return '#E6A23C'
      if (detail.indexOf('提到') !== -1) return '#9C27B0'
      return '#909399'
    },
    renderInteractionMessage: function(msg) {
      var type = msg.messageType || ''
      var detail = msg.letterDetail || ''
      var senderId = msg.senderId || ''

      // 处理来自评论表的数据
      if (msg._source === 'comment') {
        var commentData = msg._commentData || {}
        if (type === 'comment_received') {
          var senderLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoProfile(\'' + (commentData.userId || '') + '\')">' + this.escapeHtml(commentData.nickname || '匿名用户') + '</a>'
          var postLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoDetail(\'post\',\'' + (commentData.postId || '') + '\')">「' + this.escapeHtml(commentData.postTitle || '帖子') + '」</a>'
          return senderLink + ' 评论了你的帖子' + postLink
        } else if (type === 'sent_comment') {
          var postLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoDetail(\'post\',\'' + (commentData.postId || '') + '\')">「' + this.escapeHtml(commentData.postTitle || '帖子') + '」</a>'
          return '你评论了' + postLink
        }
      }

      // 从 letterDetail 中提取发送者昵称
      // 后端格式: "XXX赞了你的帖子「YYY」" / "XXX评论了你的帖子「YYY」" / "XXX关注了你" / "XXX赞了你的评论" / "XXX回复了你的评论"
      var senderNickname = ''
      var targetTitle = ''
      if (type === 'like') {
        // 格式: "XXX赞了你的帖子「YYY」" 或 "XXX赞了你的评论"
        var likePostMatch = detail.match(/^(.+?)赞了你的帖子「(.+?)」$/)
        var likeCommentMatch = detail.match(/^(.+?)赞了你的评论$/)
        if (likePostMatch) {
          senderNickname = likePostMatch[1]
          targetTitle = likePostMatch[2]
        } else if (likeCommentMatch) {
          senderNickname = likeCommentMatch[1]
        } else {
          senderNickname = detail.replace(/赞了.*$/, '')
        }
        var senderLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoProfile(\'' + senderId + '\')">' + this.escapeHtml(senderNickname) + '</a>'
        if (targetTitle) {
          // 尝试从 sessionId 中提取目标信息
          var targetInfo = this.extractPostIdFromSessionId(msg.sessionId)
          if (targetInfo) {
            var postLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoDetail(\'' + targetInfo.type + '\',\'' + targetInfo.id + '\')">「' + this.escapeHtml(targetTitle) + '」</a>'
            return senderLink + ' 赞了你的帖子' + postLink
          }
          return senderLink + ' 赞了你的帖子「' + this.escapeHtml(targetTitle) + '」'
        }
        return senderLink + ' 赞了你的评论'
      } else if (type === 'collect') {
        // 格式: "XXX收藏了你的帖子「YYY」"
        var collectPostMatch = detail.match(/^(.+?)收藏了你的帖子「(.+?)」$/)
        var collectGoodsMatch = detail.match(/^(.+?)收藏了你的商品「(.+?)」$/)
        if (collectPostMatch) {
          senderNickname = collectPostMatch[1]
          targetTitle = collectPostMatch[2]
        } else if (collectGoodsMatch) {
          senderNickname = collectGoodsMatch[1]
          targetTitle = collectGoodsMatch[2]
        } else {
          senderNickname = detail.replace(/收藏了.*$/, '')
        }
        var senderLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoProfile(\'' + senderId + '\')">' + this.escapeHtml(senderNickname) + '</a>'
        if (targetTitle) {
          var targetInfo = this.extractPostIdFromSessionId(msg.sessionId)
          if (targetInfo) {
            var postLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoDetail(\'' + targetInfo.type + '\',\'' + targetInfo.id + '\')">「' + this.escapeHtml(targetTitle) + '」</a>'
            if (collectGoodsMatch) {
              return senderLink + ' 收藏了你的商品' + postLink
            }
            return senderLink + ' 收藏了你的帖子' + postLink
          }
          if (collectGoodsMatch) {
            return senderLink + ' 收藏了你的商品「' + this.escapeHtml(targetTitle) + '」'
          }
          return senderLink + ' 收藏了你的帖子「' + this.escapeHtml(targetTitle) + '」'
        }
        return senderLink + ' 收藏了你的内容'
      } else if (type === 'comment') {
        // 格式: "XXX评论了你的帖子「YYY」" 或 "XXX回复了你的评论"
        var commentPostMatch = detail.match(/^(.+?)评论了你的帖子「(.+?)」$/)
        var replyMatch = detail.match(/^(.+?)回复了你的评论$/)
        if (commentPostMatch) {
          senderNickname = commentPostMatch[1]
          targetTitle = commentPostMatch[2]
        } else if (replyMatch) {
          senderNickname = replyMatch[1]
        } else {
          senderNickname = detail.replace(/评论了.*$/, '').replace(/回复了.*$/, '')
        }
        var senderLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoProfile(\'' + senderId + '\')">' + this.escapeHtml(senderNickname) + '</a>'
        if (targetTitle) {
          var targetInfo = this.extractPostIdFromSessionId(msg.sessionId)
          if (targetInfo) {
            var postLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoDetail(\'' + targetInfo.type + '\',\'' + targetInfo.id + '\')">「' + this.escapeHtml(targetTitle) + '」</a>'
            return senderLink + ' 评论了你的帖子' + postLink
          }
          return senderLink + ' 评论了你的帖子「' + this.escapeHtml(targetTitle) + '」'
        }
        return senderLink + ' 回复了你的评论'
      } else if (type === 'follow') {
        // 格式: "XXX关注了你"
        var followMatch = detail.match(/^(.+?)关注了你$/)
        if (followMatch) {
          senderNickname = followMatch[1]
        } else {
          senderNickname = detail.replace(/关注了.*$/, '')
        }
        var senderLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoProfile(\'' + senderId + '\')">' + this.escapeHtml(senderNickname) + '</a>'
        return senderLink + ' 关注了你'
      } else if (type === 'mention') {
        // 格式: "用户XXX 在帖子「标题」中提到了你" / "用户XXX 在表白墙中提到了你" / "用户XXX 在评论中提到了你"
        var mentionPostMatch = detail.match(/^(.+?)在帖子「(.+?)」中提到了你$/)
        var mentionWallMatch = detail.match(/^(.+?)在表白墙中提到了你$/)
        var mentionCommentMatch = detail.match(/^(.+?)在评论中提到了你$/)
        var mentionTarget = ''
        if (mentionPostMatch) {
          senderNickname = mentionPostMatch[1]
          mentionTarget = mentionPostMatch[2]
        } else if (mentionWallMatch) {
          senderNickname = mentionWallMatch[1]
        } else if (mentionCommentMatch) {
          senderNickname = mentionCommentMatch[1]
        } else {
          senderNickname = detail.replace(/在.*$/, '')
        }
        var senderLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoProfile(\'' + senderId + '\')">' + this.escapeHtml(senderNickname) + '</a>'
        if (mentionTarget) {
          var targetInfo = this.extractPostIdFromSessionId(msg.sessionId)
          if (targetInfo) {
            var postLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoDetail(\'' + targetInfo.type + '\',\'' + targetInfo.id + '\')">「' + this.escapeHtml(mentionTarget) + '」</a>'
            return senderLink + ' 在帖子' + postLink + '中提到了你'
          }
          return senderLink + ' 在帖子「' + this.escapeHtml(mentionTarget) + '」中提到了你'
        } else if (mentionWallMatch) {
          var targetInfo = this.extractPostIdFromSessionId(msg.sessionId)
          if (targetInfo) {
            var wallLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoDetail(\'wall\',\'' + targetInfo.id + '\')">表白墙</a>'
            return senderLink + ' 在' + wallLink + '中提到了你'
          }
          return senderLink + ' 在表白墙中提到了你'
        } else if (mentionCommentMatch) {
          var targetInfo = this.extractPostIdFromSessionId(msg.sessionId)
          if (targetInfo) {
            var commentLink = '<a style="cursor:pointer;color:#409EFF;" onclick="window.__letterGoDetail(\'' + targetInfo.type + '\',\'' + targetInfo.id + '\')">评论</a>'
            return senderLink + ' 在' + commentLink + '中提到了你'
          }
          return senderLink + ' 在评论中提到了你'
        }
        return senderLink + ' 提到了你'
      }
      // 其他类型直接返回原文
      return this.escapeHtml(detail)
    },
    escapeHtml: function(str) {
      if (!str) return ''
      return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
    },
    extractPostIdFromSessionId: function(sessionId) {
      // 新格式: "system_receiverId_targetType_targetId"
      // 旧格式: "system_receiverId"（无目标信息）
      if (!sessionId) return null
      var parts = sessionId.split('_')
      // parts[0] = "system", parts[1] = receiverId, parts[2] = targetType, parts[3] = targetId
      if (parts.length >= 4 && parts[0] === 'system') {
        return { type: parts[2], id: parts.slice(3).join('_') }
      }
      return null
    },
    async loadUnreadCounts() {
      try {
        var res = await this.$axios.get('/letter/unreadCountByType')
        if (res.code === 200) {
          var d = res.data || {}
          // 计算AI伴侣会话的未读数，从letter未读数中排除
          var aiUnread = 0
          var self = this
          // 确保sessionList已加载
          if (this.sessionList.length === 0) {
            try { await this.loadSessions() } catch(e) {}
          }
          this.sessionList.forEach(function(s) {
            if (self.isAiCompanionSession(s) && s.unread > 0) {
              aiUnread += s.unread
            }
          })
          var letterCount = (d.letter || 0) - aiUnread
          if (letterCount < 0) letterCount = 0
          this.unreadCounts = {
            letter: letterCount,
            comment: d.comment || 0,
            system: d.system || 0,
            order: d.order || 0,
            logistics: d.logistics || 0,
            like: (d.like || 0) + (d.collect || 0),
            follow: d.follow || 0,
            mention: d.mention || 0,
            interaction: (d.comment || 0) + (d.like || 0) + (d.follow || 0) + (d.mention || 0) + (d.collect || 0)
          }
        }
      } catch (e) {}
    },
    isAiCompanionSession(session) {
      // 通过昵称包含"AI"或特定标识判断AI伴侣会话
      var nickname = (session.otherNickname || '').toLowerCase()
      // 匹配各种可能的AI伴侣昵称
      if (nickname.indexOf('ai伴侣') !== -1 || nickname.indexOf('ai助手') !== -1 || nickname === 'ai' || nickname.indexOf('ai companion') !== -1 || nickname.indexOf('ai chat') !== -1) return true
      // 匹配昵称以"AI"开头的情况（如"AI小助手"、"AI聊天"等）
      if (/^ai[\s\-_]/.test(nickname)) return true
      return false
    },
    async loadInteractions() {
      try {
        var type = this.interactionFilter === 'all' ? 'interaction' : this.interactionFilter
        var res = await this.$axios.get('/letter/myMessageList', { params: { type: type, pageNum: this.interactionPage, pageSize: this.pageSize } })
        if (res.code === 200) {
          var items = res.data.records || res.data || []
          this.interactionTotal = res.data.total || 0

          // 「全部」模块：合并点赞/关注通知 + 收到的评论 + 发出的评论
          if (this.interactionFilter === 'all') {
            // 过滤掉 letter 表中的 comment 类型（用实际评论记录替代）
            var letterItems = items.filter(function(m) { return m.messageType !== 'comment' })
            var self = this
            var userInfo = this.$store.getters.getUserInfo

            // 并行加载收到的评论和发出的评论
            var promises = []
            promises.push(this.$axios.get('/comment/received/1/100').catch(function() { return { code: 0 } }))
            promises.push(this.$axios.get('/comment/myList/1/100').catch(function() { return { code: 0 } }))

            var results = await Promise.all(promises)

            // 处理收到的评论
            var receivedRes = results[0]
            if (receivedRes.code === 200) {
              var receivedComments = (receivedRes.data.records || []).map(function(c) {
                return {
                  letterId: 'received_comment_' + c.commentId,
                  messageType: 'comment_received',
                  senderId: c.userId || '',
                  letterDetail: (c.nickname || '匿名用户') + ' 评论了你的帖子「' + (c.postTitle || '帖子') + '」',
                  createTime: c.createTime,
                  letterStatus: 1,
                  sessionId: '',
                  _source: 'comment',
                  _commentData: c
                }
              })
              letterItems = letterItems.concat(receivedComments)
            }

            // 处理发出的评论
            var sentRes = results[1]
            if (sentRes.code === 200) {
              var sentComments = (sentRes.data.records || []).map(function(c) {
                return {
                  letterId: 'sent_comment_' + c.commentId,
                  messageType: 'sent_comment',
                  senderId: (userInfo || {}).userId || '',
                  letterDetail: '你评论了「' + (c.postTitle || '帖子') + '」',
                  createTime: c.createTime,
                  letterStatus: 1,
                  sessionId: '',
                  _source: 'comment',
                  _commentData: c
                }
              })
              letterItems = letterItems.concat(sentComments)
            }

            // 按时间降序排列
            letterItems.sort(function(a, b) { return b.createTime > a.createTime ? 1 : -1 })
            items = letterItems
            this.interactionTotal = items.length
          }

          this.interactionList = items
          this.originalInteractionList = [].concat(this.interactionList)
          this.preloadSenderInfo(this.interactionList)
        }
      } catch (e) {}
    },
    async preloadSenderInfo(list) {
      var self = this
      var ids = []
      list.forEach(function(msg) {
        var sid = String(msg.senderId || '')
        if (sid && sid !== '0' && !self.senderInfoCache[sid]) {
          if (ids.indexOf(sid) === -1) ids.push(sid)
        }
      })
      // 并行请求所有用户信息
      var promises = ids.map(function(sid) {
        return self.$axios.get('/user/publicInfo/' + sid).then(function(userRes) {
          if (userRes.code === 200 && userRes.data) {
            self.senderInfoCache[sid] = userRes.data.nickname || userRes.data.username || '用户'
          }
        }).catch(function() {})
      })
      await Promise.all(promises)
    },
    async loadComments() {
      try {
        if (this.interactionFilter === 'sent_comments') {
          var res = await this.$axios.get('/comment/myList/' + this.commentPage + '/' + this.pageSize)
          if (res.code === 200) {
            this.commentList = res.data.records || []
            this.commentTotal = res.data.total || 0
            this.originalCommentList = [].concat(this.commentList)
          }
        } else if (this.interactionFilter === 'comment_received') {
          var res = await this.$axios.get('/comment/received/' + this.commentPage + '/' + this.pageSize)
          if (res.code === 200) {
            this.commentList = res.data.records || []
            this.commentTotal = res.data.total || 0
            this.originalCommentList = [].concat(this.commentList)
          }
        }
      } catch (e) {}
    },
    async deleteComment(commentId) {
      await this.$confirm('确定删除该评论？', '提示', { type: 'warning' })
      var res = await this.$axios.delete('/comment/' + commentId)
      if (res.code === 200) { this.$message.success('删除成功'); this.loadComments() }
    },
    async loadContacts() {
      try {
        var userInfo = this.$store.getters.getUserInfo
        if (!userInfo) return
        var userId = String(userInfo.userId || userInfo.adminId)
        var followingRes = await this.$axios.get('/follow/following/' + userId, { params: { pageNum: 1, pageSize: 1000 } })
        var followingIds = []
        var followingMap = {}
        if (followingRes.code === 200) {
          var records = (followingRes.data && followingRes.data.records) || []
          records.forEach(function(r) {
            var uid = String(r.userId)
            followingIds.push(uid)
            followingMap[uid] = r
          })
        }
        // 获取关注我的人
        var followerRes = await this.$axios.get('/follow/followers/' + userId, { params: { pageNum: 1, pageSize: 1000 } })
        var followerIds = []
        if (followerRes.code === 200) {
          var records = (followerRes.data && followerRes.data.records) || []
          records.forEach(function(r) {
            followerIds.push(String(r.userId))
          })
        }
        // 计算互关：我关注的且关注我的
        var mutualIds = followingIds.filter(function(id) {
          return followerIds.indexOf(id) !== -1
        })
        this.mutualFollowerIds = mutualIds
        // 构建联系人列表
        var contacts = []
        mutualIds.forEach(function(id) {
          var info = followingMap[id]
          if (info) {
            contacts.push({
              userId: id,
              nickname: info.nickname || info.username || '用户',
              avatar: info.avatar || ''
            })
          }
        })
        this.contactsList = contacts
      } catch (e) {}
    },
    startConversation: function(contact) {
      var userInfo = this.$store.getters.getUserInfo
      if (!userInfo) return this.$message.warning('请先登录')
      var myId = String(userInfo.userId || userInfo.adminId)
      var otherId = String(contact.userId)
      var minId = myId < otherId ? myId : otherId
      var maxId = myId >= otherId ? myId : otherId
      var sessionId = minId + '_' + maxId
      var session = {
        sessionId: sessionId,
        otherId: otherId,
        otherNickname: contact.nickname || '用户',
        lastContent: '',
        lastTime: '',
        unread: 0
      }
      this.openSession(session)
    },
    async loadSessions() {
      try {
        var res = await this.$axios.post('/letter/mySessionList')
        if (res.code === 200) {
          // 后端已返回完整的会话元数据，无需再逐个查询
          var sessionData = (res.data || []).map(function(s) {
            return {
              sessionId: s.sessionId,
              otherId: String(s.otherId),
              otherNickname: s.otherNickname || '对方',
              otherAvatar: s.otherAvatar || '',
              lastContent: s.lastContent || '',
              lastTime: s.lastTime || '',
              unread: s.unread || 0
            }
          })
          this.sessionList = sessionData.sort(function(a, b) { return b.lastTime > a.lastTime ? 1 : -1 })
          this.originalSessionList = [].concat(this.sessionList)
        }
      } catch (e) {}
    },
    async openSession(session) {
      this.currentSession = session.sessionId
      this.currentSessionNickname = session.otherNickname || ''
      this.receiverId = session.otherId
      var res = await this.$axios.get('/letter/letterList/' + session.sessionId)
      if (res.code === 200) {
        var userInfo = this.$store.getters.getUserInfo
        this.letterList = (res.data || []).map(function(l) { return Object.assign({}, l, { isMine: String(l.senderId) === String(userInfo.userId || userInfo.adminId) }) })
      }
      this.letterDialogVisible = true
      this.loadUnreadCounts()
      var self = this
      this.$nextTick(function() { var box = self.$refs.chatBox; if (box) box.scrollTop = box.scrollHeight })
    },
    onSessionClose: function() { var self = this; this.loadSessions().then(function() { self.loadUnreadCounts() }) },
    async sendLetter() {
      if (!this.replyContent.trim()) return
      try {
        var muteRes = await this.$axios.get('/punishment/checkMute')
        if (muteRes.code === 200 && muteRes.data && muteRes.data.muted) {
          this.isMuted = true
          this.muteReason = muteRes.data.reason || ''
          this.$alert('您当前已被禁言，原因：' + (muteRes.data.reason || '无'), '禁言提示', { type: 'warning' })
          return
        }
      } catch (e) {}
      var res = await this.$axios.post('/letter/send', { receiverId: this.receiverId, letterDetail: this.replyContent })
      if (res.code === 200) {
        this.replyContent = ''
        var msgRes = await this.$axios.get('/letter/letterList/' + this.currentSession)
        if (msgRes.code === 200) {
          var userInfo = this.$store.getters.getUserInfo
          this.letterList = (msgRes.data || []).map(function(l) { return Object.assign({}, l, { isMine: String(l.senderId) === String(userInfo.userId || userInfo.adminId) }) })
        }
        var self = this
        this.$nextTick(function() { var box = self.$refs.chatBox; if (box) box.scrollTop = box.scrollHeight })
      }
    },
    isImageUrl(str) {
      if (!str) return false
      return str.indexOf('/images/') === 0 || str.indexOf('http') === 0 || /\.(jpg|jpeg|png|gif|bmp|webp)(\?|$)/i.test(str)
    },
    beforeChatImageUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isImage) { this.$message.error('只能上传图片'); return false }
      if (!isLt5M) { this.$message.error('图片不能超过5MB'); return false }
      return true
    },
    async handleChatImageSuccess(response) {
      var res = typeof response === 'string' ? JSON.parse(response) : response
      if (res.code === 200 && res.data) {
        var urls = res.data.split(',').filter(function(u) { return u.trim() })
        for (var i = 0; i < urls.length; i++) {
          await this.$axios.post('/letter/send', { receiverId: this.receiverId, letterDetail: urls[i].trim() })
        }
        var msgRes = await this.$axios.get('/letter/letterList/' + this.currentSession)
        if (msgRes.code === 200) {
          var userInfo = this.$store.getters.getUserInfo
          this.letterList = (msgRes.data || []).map(function(l) { return Object.assign({}, l, { isMine: String(l.senderId) === String(userInfo.userId || userInfo.adminId) }) })
        }
        var self = this
        this.$nextTick(function() { var box = self.$refs.chatBox; if (box) box.scrollTop = box.scrollHeight })
      }
    },
    async handleInteractionClick(msg) {
      // 标记已读（仅 letter 表的消息）
      if (msg.letterStatus === 0 && !msg._source) {
        await this.$axios.post('/letter/markRead/' + msg.letterId).catch(function() {})
        msg.letterStatus = 1
        this.unreadCounts.interaction = Math.max(0, this.unreadCounts.interaction - 1)
        this.loadUnreadCounts()
      }
      // 点击互动消息时导航
      var type = msg.messageType || ''
      // 来自评论表的消息：跳转到帖子详情
      if (msg._source === 'comment') {
        var postId = msg._commentData && msg._commentData.postId
        if (postId) {
          this.goDetail('post', postId)
        }
        return
      }
      // 来自 letter 表的消息
      var senderId = msg.senderId || ''
      if (type === 'follow') {
        this.goProfile(senderId)
      } else if (type === 'like' || type === 'comment' || type === 'mention' || type === 'collect') {
        // 尝试从 sessionId 提取目标信息，如果没有则跳转到发送者主页
        var targetInfo = this.extractPostIdFromSessionId(msg.sessionId)
        if (targetInfo) {
          this.goDetail(targetInfo.type, targetInfo.id)
        } else {
          this.goProfile(senderId)
        }
      }
    },
    async loadNotifications() {
      try {
        var res = await this.$axios.get('/letter/myMessageList', { params: { type: this.notificationFilter, pageNum: this.notificationPageNum, pageSize: this.notificationPageSize } })
        if (res.code === 200) {
          this.notificationList = res.data.records || res.data || []
          this.notificationTotal = res.data.total || 0
          this.originalNotificationList = [].concat(this.notificationList)
        }
      } catch (e) {}
    },
    onNotificationFilterChange: function() {
      this.notificationPageNum = 1
      this.loadNotifications()
    },
    async markNotificationRead(item) {
      if (item.letterStatus === 0) {
        await this.$axios.post('/letter/markRead/' + item.letterId).catch(function() {})
        item.letterStatus = 1
        // 根据消息类型减少对应未读数
        var type = item.messageType || 'system'
        if (type === 'order') {
          this.unreadCounts.order = Math.max(0, (this.unreadCounts.order || 0) - 1)
        } else if (type === 'logistics') {
          this.unreadCounts.logistics = Math.max(0, (this.unreadCounts.logistics || 0) - 1)
        } else {
          this.unreadCounts.system = Math.max(0, (this.unreadCounts.system || 0) - 1)
        }
        this.loadUnreadCounts()
        this.$root.$emit('messages-read')
      }
      // 根据sessionId中的targetType跳转到对应页面
      this.handleNotificationClick(item)
    },
    handleNotificationClick(item) {
      if (!item.sessionId) return
      var parts = item.sessionId.split('_')
      // sessionId格式: system_{userId} 或 system_{userId}_{targetType}_{targetId}
      if (parts.length < 4) return
      var targetType = parts[2]
      var targetId = parts[3]
      if (targetType === 'violation') {
        // 违规删除通知 -> 跳转到处罚管理页
        this.$router.push('/myPunishment').catch(function() {})
      } else if (targetType === 'post') {
        this.$router.push('/postDetail/' + targetId).catch(function() {})
      } else if (targetType === 'goods') {
        this.$router.push('/goodsDetail/' + targetId).catch(function() {})
      } else if (targetType === 'wall') {
        this.$router.push('/wallDetail/' + targetId).catch(function() {})
      } else if (targetType === 'punishment') {
        // 处罚通知 -> 跳转到处罚管理页
        this.$router.push('/myPunishment').catch(function() {})
      } else if (targetType === 'order') {
        // 订单/物流通知 -> 跳转到订单详情页
        if (item.messageType === 'logistics') {
          // 物流通知 -> 跳转到订单详情页（买家视角，可查看物流信息）
          this.$router.push('/orderDetail/' + targetId + '?from=buyer').catch(function() {})
        } else {
          // 订单通知 -> 跳转到订单详情页，根据消息内容判断买家/卖家视角
          var detail = item.letterDetail || ''
          if (detail.indexOf('有新的购买订单') >= 0 || detail.indexOf('申请退货') >= 0 || detail.indexOf('申请售后') >= 0 || detail.indexOf('退货申请') >= 0) {
            // 卖家收到的通知 -> 卖家视角
            this.$router.push('/orderDetail/' + targetId + '?from=seller').catch(function() {})
          } else {
            // 买家收到的通知 -> 买家视角
            this.$router.push('/orderDetail/' + targetId + '?from=buyer').catch(function() {})
          }
        }
      }
    },
    async markAllNotificationRead() {
      try {
        await this.$axios.post('/letter/markAllRead', null, { params: { type: this.notificationFilter } })
        this.$message.success('已全部标记为已读')
        this.loadNotifications()
        this.loadUnreadCounts()
      } catch (e) {}
    },
    async markAllRead() {
      try {
        if (this.activeTab === 'letter') {
          // 调用后端 markAllRead 接口一次性标记所有私信为已读
          await this.$axios.post('/letter/markAllRead', null, { params: { type: 'letter' } })
          this.$message.success('已全部标记为已读')
          this.unreadCounts.letter = 0
          this.loadSessions()
        } else if (this.activeTab === 'notification') {
          await this.markAllNotificationRead()
        } else {
          // 互动消息：使用 interaction 类型一次性标记 comment/like/follow 为已读
          await this.$axios.post('/letter/markAllRead', null, { params: { type: 'interaction' } })
          this.$message.success('已全部标记为已读')
          this.unreadCounts.interaction = 0
          this.loadInteractions()
        }
        this.loadUnreadCounts()
        // 通知Home.vue刷新未读数
        this.$root.$emit('messages-read')
      } catch (e) {}
    },
    async checkMuteStatus() {
      try {
        var res = await this.$axios.get('/punishment/checkMute')
        if (res.code === 200 && res.data && res.data.muted) {
          this.isMuted = true
          this.muteReason = res.data.reason || ''
        }
      } catch (e) {}
    },
    onSearch: function() {
      var kw = this.searchKeyword.trim().toLowerCase()
      if (!kw) { this.onSearchClear(); return }
      if (this.activeTab === 'letter') {
        this.sessionList = this.originalSessionList.filter(function(s) { return (s.lastContent || '').toLowerCase().indexOf(kw) !== -1 || (s.otherNickname || '').toLowerCase().indexOf(kw) !== -1 })
      } else if (this.activeTab === 'notification') {
        this.notificationList = this.originalNotificationList.filter(function(n) { return (n.letterDetail || '').toLowerCase().indexOf(kw) !== -1 })
      } else if (this.activeTab === 'interaction') {
        if (this.interactionFilter === 'sent_comments' || this.interactionFilter === 'comment_received') {
          this.commentList = this.originalCommentList.filter(function(c) { return (c.text || '').toLowerCase().indexOf(kw) !== -1 || (c.postTitle || '').toLowerCase().indexOf(kw) !== -1 })
        } else {
          this.interactionList = this.originalInteractionList.filter(function(m) { return (m.letterDetail || '').toLowerCase().indexOf(kw) !== -1 })
        }
      }
    },
    onSearchClear: function() {
      this.searchKeyword = ''
      if (this.activeTab === 'letter') {
        this.sessionList = [].concat(this.originalSessionList)
      } else if (this.activeTab === 'notification') {
        this.notificationList = [].concat(this.originalNotificationList)
      } else if (this.activeTab === 'interaction') {
        if (this.interactionFilter === 'sent_comments' || this.interactionFilter === 'comment_received') {
          this.commentList = [].concat(this.originalCommentList)
        } else {
          this.interactionList = [].concat(this.originalInteractionList)
        }
      }
    },
    openAiCompanion() {
      this.aiCompanionVisible = true
      if (this.aiMessages.length === 0) {
        try {
          var saved = localStorage.getItem('ai_companion_messages')
          if (saved) {
            this.aiMessages = JSON.parse(saved)
          }
        } catch (e) {}
      }
      if (this.aiMessages.length === 0) {
        this.aiMessages.push({ role: 'assistant', content: '你好呀！我是你的AI伴侣，随时都可以找我聊天哦~ 有什么想说的尽管告诉我吧！' })
      }
      this.loadCompanionConfigs()
      this.$nextTick(function() {
        var box = this.$refs.aiChatBox
        if (box) box.scrollTop = box.scrollHeight
      })
      try {
        this.companionSessionHistory = JSON.parse(localStorage.getItem('ai_companion_session_history') || '[]')
      } catch(e) { this.companionSessionHistory = [] }
      try {
        this.companionCurrentSessionId = localStorage.getItem('ai_companion_current_session_id') || null
      } catch(e) { this.companionCurrentSessionId = null }
    },
    onAiCompanionClose() {
      this.aiCompanionInput = ''
      this.companionShowHistory = false
      this.companionSelectMode = false
      this.companionSelectedIds = []
      this.syncCompanionCurrentToList()
      this.saveCompanionMessages()
    },
    newCompanionSession() {
      this.syncCompanionCurrentToList()
      this.aiMessages = [{ role: 'assistant', content: '你好呀！我是你的AI伴侣，随时都可以找我聊天哦~ 有什么想说的尽管告诉我吧！' }]
      this.companionCurrentSessionId = null
      this.saveCompanionMessages()
      localStorage.setItem('ai_companion_current_session_id', '')
    },
    clearCompanionSession() {
      if (this.aiMessages.length <= 1) return
      this.$confirm('确定清除当前会话？会话将保存到历史记录中。', '提示', { type: 'warning' }).then(() => {
        this.syncCompanionCurrentToList()
        this.aiMessages = [{ role: 'assistant', content: '你好呀！我是你的AI伴侣，随时都可以找我聊天哦~ 有什么想说的尽管告诉我吧！' }]
        this.companionCurrentSessionId = null
        this.saveCompanionMessages()
        localStorage.setItem('ai_companion_current_session_id', '')
      }).catch(() => {})
    },
    syncCompanionCurrentToList() {
      var userMsgs = this.aiMessages.filter(function(m) { return m.role === 'user' })
      if (userMsgs.length === 0) return
      var preview = ''
      for (var i = 0; i < this.aiMessages.length; i++) {
        if (this.aiMessages[i].role === 'user' && this.aiMessages[i].content) {
          preview = this.aiMessages[i].content.substring(0, 20)
          break
        }
      }
      var now = new Date()
      var pad = function(n) { return String(n).padStart(2, '0') }
      var timeStr = pad(now.getMonth() + 1) + '/' + pad(now.getDate()) + ' ' + pad(now.getHours()) + ':' + pad(now.getMinutes())
      var sessionId = this.companionCurrentSessionId || Date.now().toString()
      var session = { id: sessionId, messages: this.aiMessages.slice(), preview: preview, time: timeStr }
      var idx = this.companionSessionHistory.findIndex(function(s) { return s.id === sessionId })
      if (idx >= 0) {
        this.companionSessionHistory.splice(idx, 1, session)
      } else {
        this.companionSessionHistory.unshift(session)
      }
      if (this.companionSessionHistory.length > 10) {
        this.companionSessionHistory = this.companionSessionHistory.slice(0, 10)
      }
      localStorage.setItem('ai_companion_session_history', JSON.stringify(this.companionSessionHistory))
    },
    saveCompanionSessionToHistory() {
      this.syncCompanionCurrentToList()
    },
    switchCompanionSession(session) {
      if (!session || !session.messages) {
        this.companionShowHistory = false
        return
      }
      this.syncCompanionCurrentToList()
      this.aiMessages = session.messages.slice()
      this.companionCurrentSessionId = session.id
      this.saveCompanionMessages()
      localStorage.setItem('ai_companion_current_session_id', session.id)
      this.companionShowHistory = false
      this.$nextTick(() => {
        var box = this.$refs.aiChatBox
        if (box) box.scrollTop = box.scrollHeight
      })
    },
    deleteCompanionSession(idx) {
      var deleted = this.companionSessionHistory[idx]
      this.companionSessionHistory.splice(idx, 1)
      localStorage.setItem('ai_companion_session_history', JSON.stringify(this.companionSessionHistory))
      if (deleted && deleted.id === this.companionCurrentSessionId) {
        this.aiMessages = [{ role: 'assistant', content: '你好呀！我是你的AI伴侣，随时都可以找我聊天哦~ 有什么想说的尽管告诉我吧！' }]
        this.companionCurrentSessionId = null
        this.saveCompanionMessages()
        localStorage.setItem('ai_companion_current_session_id', '')
      }
    },
    clearAllCompanionHistory() {
      this.$confirm('确定清空全部历史会话？', '提示', { type: 'warning' }).then(() => {
        this.companionSessionHistory = []
        localStorage.removeItem('ai_companion_session_history')
        this.aiMessages = [{ role: 'assistant', content: '你好呀！我是你的AI伴侣，随时都可以找我聊天哦~ 有什么想说的尽管告诉我吧！' }]
        this.companionCurrentSessionId = null
        this.saveCompanionMessages()
        localStorage.setItem('ai_companion_current_session_id', '')
        this.$message.success('已清空全部历史会话')
      }).catch(() => {})
    },
    enterCompanionSelectMode() {
      this.companionSelectMode = true
      this.companionSelectedIds = []
    },
    exitCompanionSelectMode() {
      this.companionSelectMode = false
      this.companionSelectedIds = []
    },
    toggleCompanionSelectConv(id) {
      var idx = this.companionSelectedIds.indexOf(id)
      if (idx >= 0) {
        this.companionSelectedIds.splice(idx, 1)
      } else {
        this.companionSelectedIds.push(id)
      }
    },
    toggleCompanionSelectAll() {
      if (this.companionIsAllSelected) {
        this.companionSelectedIds = []
      } else {
        this.companionSelectedIds = this.companionSessionHistory.map(function(s) { return s.id })
      }
    },
    onCompanionHistoryItemClick(session) {
      if (this.companionSelectMode) {
        this.toggleCompanionSelectConv(session.id)
      } else if (session.id === this.companionCurrentSessionId) {
        this.companionShowHistory = false
      } else {
        this.switchCompanionSession(session)
      }
    },
    deleteCompanionSelected() {
      if (this.companionSelectedIds.length === 0) return
      this.$confirm('确定删除选中的 ' + this.companionSelectedIds.length + ' 个会话？', '提示', { type: 'warning' }).then(() => {
        if (this.companionSelectedIds.includes(this.companionCurrentSessionId)) {
          this.aiMessages = [{ role: 'assistant', content: '你好呀！我是你的AI伴侣，随时都可以找我聊天哦~ 有什么想说的尽管告诉我吧！' }]
          this.companionCurrentSessionId = null
          this.saveCompanionMessages()
          localStorage.setItem('ai_companion_current_session_id', '')
        }
        this.companionSessionHistory = this.companionSessionHistory.filter(function(s) {
          return !this.companionSelectedIds.includes(s.id)
        }.bind(this))
        localStorage.setItem('ai_companion_session_history', JSON.stringify(this.companionSessionHistory))
        this.$message.success('已删除 ' + this.companionSelectedIds.length + ' 个会话')
        this.exitCompanionSelectMode()
      }).catch(() => {})
    },
    saveCompanionMessages() {
      localStorage.setItem('ai_companion_messages', JSON.stringify(this.aiMessages))
    },
    async loadCompanionConfigs() {
      try {
        // 优先加载AI伴侣配置(configType=2)，如果没有则回退到AI助手配置(configType=1)
        var res = await this.$axios.get('/ai/configs?configType=2')
        if (res.code === 200 && res.data && res.data.length > 0) {
          this.companionConfigs = res.data
        } else {
          var res2 = await this.$axios.get('/ai/configs?configType=1')
          if (res2.code === 200 && res2.data) {
            this.companionConfigs = res2.data || []
          }
        }
        // 自动选择激活的配置
        if (!this.companionConfigId) {
          var savedConfigId = localStorage.getItem('ai_companion_configId')
          if (savedConfigId) {
            this.companionConfigId = parseInt(savedConfigId)
          } else {
            var activeConfig = this.companionConfigs.find(function(c) { return c.isActive })
            if (activeConfig) this.companionConfigId = activeConfig.id
          }
        }
      } catch (e) {}
    },
    async sendAiCompanionMessage() {
      var text = this.aiCompanionInput.trim()
      if (!text || this.aiCompanionLoading) return
      this.aiMessages.push({ role: 'user', content: text })
      this.aiCompanionInput = ''
      this.aiCompanionLoading = true
      this.$nextTick(function() {
        var box = this.$refs.aiChatBox
        if (box) box.scrollTop = box.scrollHeight
      })
      try {
        var reqParams = {
          message: text,
          mode: 'companion',
          history: this.aiMessages.slice(-10).map(function(m) { return { role: m.role, content: m.content } })
        }
        if (this.companionConfigId) {
          reqParams.configId = this.companionConfigId
        }
        var res = await this.$axios.post('/ai/chat', reqParams)
        if (res.code === 200 && res.data && res.data.text) {
          this.aiMessages.push({ role: 'assistant', content: res.data.text })
        } else {
          this.aiMessages.push({ role: 'assistant', content: '抱歉，我暂时无法回复，请稍后再试~' })
        }
      } catch (e) {
        this.aiMessages.push({ role: 'assistant', content: '网络出了点问题，请稍后再试~' })
      }
      this.aiCompanionLoading = false
      // 保存聊天记录
      localStorage.setItem('ai_companion_messages', JSON.stringify(this.aiMessages))
      this.$nextTick(function() {
        var box = this.$refs.aiChatBox
        if (box) box.scrollTop = box.scrollHeight
      })
    }
  }
}

// 全局方法，供 v-html 中的 onclick 调用
if (typeof window !== 'undefined') {
  window.__letterGoProfile = function(userId) {
    var router = window.__letterVueRouter
    if (router) {
      router.push('/userProfile/' + userId).catch(function() {})
    }
  }
  window.__letterGoDetail = function(type, id) {
    var router = window.__letterVueRouter
    if (router) {
      if (type === 'post' && id) {
        router.push('/postDetail/' + id).catch(function() {})
      } else if (type === 'wall' && id) {
        router.push('/wallDetail/' + id).catch(function() {})
      } else if (type === 'goods' && id) {
        router.push('/goodsDetail/' + id).catch(function() {})
      }
    }
  }
}
</script>

<style scoped>
.msg-item { display: flex; align-items: center; padding: 12px; border-bottom: 1px solid #f0f0f0; cursor: pointer; transition: background 0.3s ease; position: relative; border-radius: 8px; }
.msg-item:hover { background: #f5f7fa; }
.msg-item.unread { background: #ecf5ff; }
.msg-dot { width: 8px; height: 8px; border-radius: 50%; background: #F56C6C; position: absolute; top: 12px; left: 4px; }
.msg-icon { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 16px; margin-right: 12px; flex-shrink: 0; }
.msg-avatar-wrap { position: relative; margin-right: 12px; flex-shrink: 0; }
.msg-avatar { display: block; border-radius: 50%; }
.msg-avatar-badge { position: absolute; top: -4px; right: -8px; }
.msg-content { flex: 1; min-width: 0; }
.msg-text { font-size: 14px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.msg-time { font-size: 12px; color: #999; margin-top: 4px; }
.unread-bg { background: #ecf5ff; }
.chat-bubble { display: inline-block; max-width: 80%; padding: 8px 14px; border-radius: 12px; font-size: 13px; line-height: 1.8; word-break: break-word; overflow-wrap: break-word; white-space: pre-wrap; text-align: left; box-sizing: border-box; }
.chat-bubble-left { background: #f0f9eb; color: #67C23A; border-top-left-radius: 4px; }
.chat-bubble-right { background: #ecf5ff; color: #409EFF; border-top-right-radius: 4px; }

/* AI伴侣历史会话面板 */
.companion-history-panel {
  width: 200px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  margin: -20px 0;
  padding: 0;
}
.companion-history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 12px 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
}
.companion-history-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.companion-history-action-btn {
  cursor: pointer;
  font-size: 15px;
  color: #909399;
  transition: color 0.3s ease;
}
.companion-history-action-btn:hover {
  color: #409EFF;
}
.companion-history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.companion-history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin-bottom: 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
  background: #fff;
}
.companion-history-item:hover {
  border-color: #409EFF;
  background: #ecf5ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}
.companion-history-item.current {
  border-color: #409EFF;
  background: #ecf5ff;
}
.companion-history-item.selected {
  border-color: #409EFF;
  background: #ecf5ff;
}
.companion-history-check {
  flex-shrink: 0;
  margin-right: 6px;
  display: flex;
  align-items: center;
}
.companion-history-info {
  flex: 1;
  min-width: 0;
}
.companion-history-title {
  font-size: 12px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.companion-history-meta {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}
.companion-current-badge {
  display: inline-block;
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 6px;
  background: linear-gradient(135deg, #67C23A, #409EFF);
  color: #fff;
  vertical-align: middle;
  margin-right: 3px;
  line-height: 14px;
}
.companion-history-del {
  color: #C0C4CC;
  font-size: 13px;
  padding: 3px;
  border-radius: 4px;
  transition: all 0.3s ease;
  flex-shrink: 0;
  margin-left: 4px;
}
.companion-history-del:hover {
  color: #F56C6C;
  background: #fef0f0;
}
.companion-select-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-top: 1px solid #ebeef5;
  background: #fff;
}
.companion-select-count {
  font-size: 12px;
  color: #606266;
}
</style>
