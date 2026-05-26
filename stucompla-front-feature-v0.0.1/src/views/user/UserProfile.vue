<!--
  组件名：UserProfile
  功能描述：用户主页，展示其他用户的个人信息和内容
  主要职责：
    1. 展示用户资料和统计数据
    2. 内容标签页（帖子/商品/表白墙）
    3. 关注/取消关注操作
-->
<template>
  <div v-loading="loading">
    <div style="margin-bottom:15px;">
      <el-page-header @back="$router.back()" content="用户主页"></el-page-header>
    </div>
    <!-- Top Section: User Profile Card -->
    <el-card shadow="hover" style="border-radius:8px;">
      <div style="display:flex;align-items:flex-start;">
        <el-avatar v-if="userInfo.avatar" :src="userInfo.avatar" :size="100"></el-avatar>
        <el-avatar v-else :size="100" style="background:#409EFF;font-size:36px;">{{ (userInfo.nickname || userInfo.username || '用')[0] }}</el-avatar>
        <div style="margin-left:20px;flex:1;">
          <div style="display:flex;align-items:center;flex-wrap:wrap;gap:8px;">
            <span style="font-size:22px;font-weight:bold;">{{ userInfo.nickname || userInfo.username || '用户' }}</span>
            <span v-if="userInfo.sex === '男'" style="color:#409EFF;font-size:14px;">♂</span>
            <span v-else-if="userInfo.sex === '女'" style="color:#F56C6C;font-size:14px;">♀</span>
            <el-tag v-if="isMutual" size="small" type="success">互相关注</el-tag>
          </div>
          <div style="color:#999;font-size:13px;margin-top:6px;">ID: {{ userId | formatId('user') }}</div>
          <div v-if="activePunishments.length > 0" style="margin-top:4px;">
            <el-tag v-for="p in activePunishments" :key="p.punishmentId" size="mini" :type="p.type === 'mute' ? 'warning' : p.type === 'ban' ? 'danger' : 'info'" style="margin-right:4px;">
              {{ p.type === 'mute' ? '禁言中' : p.type === 'ban' ? '封号中' : '警告' }}
            </el-tag>
          </div>
          <div style="margin-top:12px;display:flex;gap:10px;">
            <el-button v-if="!isSelf" :type="isFollowing ? 'default' : 'primary'" size="small" @click="toggleFollow">
              {{ isFollowing ? '已关注' : '+ 关注' }}
            </el-button>
            <el-button v-if="!isSelf" type="success" size="small" plain @click="openDM">私信</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Stats Row -->
    <el-card shadow="hover" style="margin-top:16px;border-radius:8px;">
      <div style="display:flex;justify-content:space-around;text-align:center;padding:8px 0;">
        <div style="cursor:pointer;" @click="showLikeDialog">
          <div style="font-size:22px;font-weight:bold;color:#303133;">{{ likeCount }}</div>
          <div style="font-size:13px;color:#909399;margin-top:4px;">获赞数</div>
        </div>
        <div style="cursor:pointer;" @click="showMutualDialog">
          <div style="font-size:22px;font-weight:bold;color:#303133;">{{ mutualCount }}</div>
          <div style="font-size:13px;color:#909399;margin-top:4px;">互关数</div>
        </div>
        <div style="cursor:pointer;" @click="showFollowingDialog">
          <div style="font-size:22px;font-weight:bold;color:#303133;">{{ followingCount }}</div>
          <div style="font-size:13px;color:#909399;margin-top:4px;">关注数</div>
        </div>
        <div style="cursor:pointer;" @click="showFollowersDialog">
          <div style="font-size:22px;font-weight:bold;color:#303133;">{{ followerCount }}</div>
          <div style="font-size:13px;color:#909399;margin-top:4px;">粉丝数</div>
        </div>
      </div>
    </el-card>

    <!-- Signature -->
    <el-card v-if="userInfo.signature" shadow="hover" style="margin-top:16px;border-radius:8px;">
      <div style="color:#606266;font-size:14px;line-height:1.6;">
        <i class="el-icon-chat-line-square" style="margin-right:6px;color:#909399;"></i>{{ userInfo.signature }}
      </div>
    </el-card>

    <!-- Content Tabs -->
    <el-card shadow="hover" style="margin-top:16px;border-radius:8px;">
      <el-tabs v-model="activeTab" @tab-click="onTabChange">
        <el-tab-pane :label="'帖子(' + postList.length + ')'" name="posts">
          <div v-if="postList.length === 0" style="text-align:center;color:#999;padding:30px 0;">暂无帖子</div>
          <div v-for="post in postList" :key="post.postId" class="profile-content-item" @click="$router.push('/postDetail/' + post.postId).catch(() => {})">
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <span style="font-weight:500;font-size:15px;">{{ post.title }}</span>
              <span style="color:#999;font-size:12px;white-space:nowrap;margin-left:12px;">{{ post.createTime | formatTime }}</span>
            </div>
            <div style="color:#999;font-size:12px;margin-top:6px;">
              <i class="el-icon-view"></i> {{ post.viewNum || 0 }}
              <i class="el-icon-chat-dot-round" style="margin-left:10px;"></i> {{ post.commentNum || 0 }}
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="'商品(' + goodsList.length + ')'" name="goods">
          <div v-if="goodsList.length === 0" style="text-align:center;color:#999;padding:30px 0;">暂无商品</div>
          <div v-for="goods in goodsList" :key="goods.goodsId" class="profile-content-item" @click="$router.push('/goodsDetail/' + goods.goodsId).catch(() => {})">
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <span style="font-weight:500;font-size:15px;">{{ goods.goodsName }}</span>
              <span style="color:#F56C6C;font-weight:bold;font-size:15px;">￥{{ goods.goodsPrice }}</span>
            </div>
            <div style="color:#999;font-size:12px;margin-top:6px;">{{ goods.createTime | formatTime }}</div>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="'表白墙(' + wallList.length + ')'" name="walls">
          <div v-if="wallList.length === 0" style="text-align:center;color:#999;padding:30px 0;">暂无表白墙</div>
          <div v-for="wall in wallList" :key="wall.wallId" class="profile-content-item" @click="$router.push('/wallDetail/' + wall.wallId).catch(() => {})">
            <div style="font-weight:500;font-size:15px;" v-html="renderMentionText(wall.content || wall.wallContent || '表白墙 #' + wall.wallId, wall.mentionUsers)"></div>
            <div style="color:#999;font-size:12px;margin-top:6px;">{{ wall.createTime | formatTime }}</div>
          </div>
        </el-tab-pane>
        <el-tab-pane v-if="isSelf" :label="'收藏(' + collectList.length + ')'" name="collect">
          <div v-if="collectList.length === 0" style="text-align:center;color:#999;padding:30px 0;">暂无收藏</div>
          <div v-for="item in collectList" :key="item.collectId" class="profile-content-item" @click="$router.push('/postDetail/' + item.postId).catch(() => {})">
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <span style="font-weight:500;font-size:15px;">{{ item.title || '帖子 #' + item.postId }}</span>
              <el-tag size="mini" type="info">帖子</el-tag>
            </div>
            <div style="color:#999;font-size:12px;margin-top:6px;">{{ item.createTime | formatTime }}</div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Following Dialog -->
    <el-dialog title="关注列表" :visible.sync="followingDialogVisible" width="520px" append-to-body class="user-list-dialog">
      <div v-loading="dialogLoading">
        <div v-if="followingList.length === 0" class="dialog-empty">暂无关注</div>
        <div v-for="item in followingList" :key="item.followId" class="profile-user-item">
          <el-avatar v-if="item.avatar" :src="item.avatar" :size="42" @click="goUserProfile(item.userId)"></el-avatar>
          <el-avatar v-else :size="42" style="background:#409EFF;" @click="goUserProfile(item.userId)">{{ (item.nickname || item.username || '用')[0] }}</el-avatar>
          <div class="profile-user-info">
            <div style="display:flex;align-items:center;gap:6px;">
              <span class="profile-user-name" @click="goUserProfile(item.userId)">{{ item.nickname || item.username || '用户' }}</span>
              <el-tag v-if="item.isMutual" size="mini" type="success">互相关注</el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- Followers Dialog -->
    <el-dialog title="粉丝列表" :visible.sync="followersDialogVisible" width="520px" append-to-body class="user-list-dialog">
      <div v-loading="dialogLoading">
        <div v-if="followersList.length === 0" class="dialog-empty">暂无粉丝</div>
        <div v-for="item in followersList" :key="item.followId" class="profile-user-item">
          <el-avatar v-if="item.avatar" :src="item.avatar" :size="42" @click="goUserProfile(item.userId)"></el-avatar>
          <el-avatar v-else :size="42" style="background:#409EFF;" @click="goUserProfile(item.userId)">{{ (item.nickname || item.username || '用')[0] }}</el-avatar>
          <div class="profile-user-info">
            <span class="profile-user-name" @click="goUserProfile(item.userId)">{{ item.nickname || item.username || '用户' }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- Mutual Dialog -->
    <el-dialog title="互相关注" :visible.sync="mutualDialogVisible" width="520px" append-to-body class="user-list-dialog">
      <div v-loading="dialogLoading">
        <div v-if="mutualList.length === 0" class="dialog-empty">暂无互关</div>
        <div v-for="item in mutualList" :key="item.userId" class="profile-user-item">
          <el-avatar v-if="item.avatar" :src="item.avatar" :size="42" @click="goUserProfile(item.userId)"></el-avatar>
          <el-avatar v-else :size="42" style="background:#409EFF;" @click="goUserProfile(item.userId)">{{ (item.nickname || item.username || '用')[0] }}</el-avatar>
          <div class="profile-user-info">
            <span class="profile-user-name">{{ item.nickname || item.username || '用户' }}</span>
          </div>
          <el-tag size="small" type="success">互相关注</el-tag>
        </div>
      </div>
    </el-dialog>

    <!-- Like Dialog -->
    <el-dialog title="获赞详情" :visible.sync="likeDialogVisible" width="420px" append-to-body class="like-dialog">
      <div class="like-dialog-body">
        <div class="like-count">{{ likeCount }}</div>
        <div class="like-label">累计获赞数</div>
      </div>
    </el-dialog>

    <!-- DM Dialog -->
    <el-dialog :title="'与 ' + (userInfo.nickname || userInfo.username || '用户') + ' 的对话'" :visible.sync="dmDialogVisible" width="520px" @close="dmDialogVisible = false" class="dm-dialog">
      <div class="dm-chat-box" ref="dmChatBox">
        <div v-for="l in dmLetterList" :key="l.letterId" :class="['dm-message', l.isMine ? 'dm-mine' : 'dm-other']">
          <div class="dm-bubble">{{ l.letterDetail }}</div>
          <div class="dm-time">{{ l.createTime | formatTime }}</div>
        </div>
      </div>
      <div class="dm-input-area">
        <el-input v-model="dmContent" placeholder="输入消息..." @keyup.enter.native="sendDM"></el-input>
        <el-button type="primary" @click="sendDM">发送</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: 'UserProfile',
  data() {
    return {
      userId: '',
      userInfo: {},
      likeCount: 0,
      mutualCount: 0,
      followerCount: 0,
      followingCount: 0,
      isFollowing: false,
      isMutual: false,
      isSelf: false,
      loading: false,
      activeTab: 'posts',
      activePunishments: [],
      postList: [],
      goodsList: [],
      wallList: [],
      collectList: [],
      followingList: [],
      followersList: [],
      mutualList: [],
      dialogLoading: false,
      followingDialogVisible: false,
      followersDialogVisible: false,
      mutualDialogVisible: false,
      likeDialogVisible: false,
      dmDialogVisible: false,
      dmLetterList: [],
      dmContent: '',
      dmSessionId: '',
      mentionUserMap: {}
    }
  },
  created() {
    this.userId = this.$route.params.id
    this.loadUserInfo()
    this.loadPosts()
    this.loadGoods()
    this.loadWalls()
    this.checkFollow()
    this.loadPunishments()
  },
  watch: {
    '$route'(to) {
      this.userId = to.params.id
      this.loadUserInfo()
      this.loadPosts()
      this.loadGoods()
      this.loadWalls()
      this.checkFollow()
      this.loadPunishments()
    }
  },
  methods: {
    async loadUserInfo() {
      this.loading = true
      try {
        var res = await this.$axios.get('/user/publicInfo/' + this.userId)
        if (res.code === 200) this.userInfo = res.data || {}
      } catch (e) {
        try {
          var res2 = await this.$axios.get('/user/info')
          if (res2.code === 200 && res2.data && String(res2.data.userId) === String(this.userId)) {
            this.userInfo = res2.data
            this.isSelf = true
          }
        } catch (e2) {}
      }
      try {
        var fc = await this.$axios.get('/follow/followerCount/' + this.userId)
        if (fc.code === 200) this.followerCount = fc.data || 0
        var fgc = await this.$axios.get('/follow/followingCount/' + this.userId)
        if (fgc.code === 200) this.followingCount = fgc.data || 0
      } catch (e) {}
      this.mutualCount = Math.min(this.followerCount, this.followingCount)
      try {
        var lc = await this.$axios.get('/stats/likeCount?userId=' + this.userId)
        if (lc.code === 200) this.likeCount = lc.data || 0
      } catch (e) { this.likeCount = 0 }
      var myInfo = this.$store.getters.getUserInfo
      var myId = myInfo && (myInfo.userId || myInfo.adminId)
      if (myInfo && String(myId) === String(this.userId)) this.isSelf = true
      this.loading = false
    },
    async checkFollow() {
      var myInfo = this.$store.getters.getUserInfo
      if (!myInfo || this.isSelf) return
      try {
        var res = await this.$axios.get('/follow/check/' + this.userId)
        if (res.code === 200) this.isFollowing = res.data || false
        if (this.isFollowing) {
          var mutualRes = await this.$axios.get('/follow/checkMutual/' + this.userId)
          if (mutualRes.code === 200) this.isMutual = mutualRes.data || false
        }
      } catch (e) {}
    },
    async toggleFollow() {
      try {
        if (this.isFollowing) {
          await this.$axios.delete('/follow/cancel/' + this.userId)
          this.isFollowing = false
          this.isMutual = false
          this.followerCount = Math.max(0, this.followerCount - 1)
          this.$message.success('已取消关注')
        } else {
          await this.$axios.post('/follow/add/' + this.userId)
          this.isFollowing = true
          this.followerCount += 1
          this.$message.success('关注成功')
          var mutualRes = await this.$axios.get('/follow/checkMutual/' + this.userId)
          if (mutualRes.code === 200) this.isMutual = mutualRes.data || false
        }
      } catch (e) {}
    },
    onTabChange(tab) {
      if (tab.name === 'posts') this.loadPosts()
      else if (tab.name === 'goods') this.loadGoods()
      else if (tab.name === 'walls') this.loadWalls()
      else if (tab.name === 'collect') this.loadCollect()
    },
    async loadPunishments() {
      try {
        var res = await this.$axios.get('/punishment/publicList/' + this.userId)
        if (res.code === 200) {
          var all = res.data || []
          this.activePunishments = all.filter(function(p) { return p.status === 0 })
        }
      } catch (e) {}
    },
    async loadPosts() {
      try {
        var res = await this.$axios.get('/post/list', { params: { userId: this.userId, pageNum: 1, pageSize: 9999 } })
        if (res.code === 200) {
          this.postList = res.data.records || []
        }
      } catch (e) {}
    },
    async loadGoods() {
      try {
        var res = await this.$axios.get('/goods/getList', { params: { userId: this.userId, pageNum: 1, pageSize: 9999 } })
        if (res.code === 200) {
          this.goodsList = res.data.records || []
        }
      } catch (e) {}
    },
    async loadWalls() {
      try {
        var res = await this.$axios.get('/wall/wallList', { params: { userId: this.userId, pageNum: 1, pageSize: 9999 } })
        if (res.code === 200) {
          this.wallList = res.data.records || []
          var self = this
          this.wallList.forEach(function(w) {
            if (w.mentionUsers) self.loadMentionUsers(w.mentionUsers)
          })
        }
      } catch (e) {}
    },
    async loadCollect() {
      try {
        var userInfo = this.$store.getters.getUserInfo
        var uid = userInfo && (userInfo.userId || userInfo.adminId)
        var res = await this.$axios.get('/collect/list', { params: { pageNum: 1, pageSize: 9999, userId: uid } })
        if (res.code === 200) this.collectList = res.data.records || []
      } catch (e) {}
    },
    // Dialog methods
    async showLikeDialog() {
      if (!this.isSelf) {
        try {
          var res = await this.$axios.get('/privacy/check', { params: { targetUserId: this.userId, field: 'likes' } })
          if (res.code === 200 && !res.data) {
            this.$message.warning('该用户设置了隐私保护')
            return
          }
        } catch (e) {}
      }
      this.likeDialogVisible = true
    },
    async showFollowingDialog() {
      if (!this.isSelf) {
        try {
          var res = await this.$axios.get('/privacy/check', { params: { targetUserId: this.userId, field: 'following' } })
          if (res.code === 200 && !res.data) {
            this.$message.warning('该用户设置了隐私保护')
            return
          }
        } catch (e) {}
      }
      this.followingDialogVisible = true
      this.dialogLoading = true
      try {
        var res = await this.$axios.get('/follow/following/' + this.userId, { params: { pageNum: 1, pageSize: 9999 } })
        if (res.code === 200) this.followingList = res.data.records || []
      } catch (e) {}
      this.dialogLoading = false
    },
    async showFollowersDialog() {
      if (!this.isSelf) {
        try {
          var res = await this.$axios.get('/privacy/check', { params: { targetUserId: this.userId, field: 'followers' } })
          if (res.code === 200 && !res.data) {
            this.$message.warning('该用户设置了隐私保护')
            return
          }
        } catch (e) {}
      }
      this.followersDialogVisible = true
      this.dialogLoading = true
      try {
        var res = await this.$axios.get('/follow/followers/' + this.userId, { params: { pageNum: 1, pageSize: 9999 } })
        if (res.code === 200) this.followersList = res.data.records || []
      } catch (e) {}
      this.dialogLoading = false
    },
    async showMutualDialog() {
      if (!this.isSelf) {
        try {
          var res = await this.$axios.get('/privacy/check', { params: { targetUserId: this.userId, field: 'following' } })
          if (res.code === 200 && !res.data) {
            this.$message.warning('该用户设置了隐私保护')
            return
          }
        } catch (e) {}
      }
      this.mutualDialogVisible = true
      this.dialogLoading = true
      try {
        var res = await this.$axios.get('/follow/following/' + this.userId, { params: { pageNum: 1, pageSize: 9999 } })
        if (res.code === 200) {
          var following = res.data.records || []
          var mutualIds = []
          for (var i = 0; i < following.length; i++) {
            try {
              var checkRes = await this.$axios.get('/follow/check/' + following[i].userId)
              if (checkRes.code === 200 && checkRes.data) mutualIds.push(following[i].userId)
            } catch (e) {}
          }
          this.mutualList = following.filter(function(f) { return mutualIds.indexOf(f.userId) !== -1 })
        }
      } catch (e) {}
      this.dialogLoading = false
    },
    goUserProfile(uid) {
      this.$router.push('/userProfile/' + uid).catch(function() {})
    },
    async loadMentionUsers(mentionUsers) {
      if (!mentionUsers) return
      var ids = []
      try { ids = JSON.parse(mentionUsers) } catch (e) { return }
      if (!ids || ids.length === 0) return
      var self = this
      try {
        var res = await this.$axios.get('/user/batchInfo', { params: { ids: ids.join(',') } })
        if (res.code === 200 && res.data) {
          res.data.forEach(function(u) {
            if (u.nickname) {
              self.$set(self.mentionUserMap, u.nickname, u.userId)
            }
          })
        }
      } catch (e) {}
    },
    renderMentionText(text, mentionUsers) {
      if (!text) return ''
      var self = this
      return text.replace(/@(\S+)/g, function(match, name) {
        var userId = self.mentionUserMap[name]
        if (userId) {
          return '<a href="javascript:void(0)" class="mention-link" data-userid="' + userId + '">' + match + '</a>'
        }
        return match
      })
    },
    // DM
    async openDM() {
      var myInfo = this.$store.getters.getUserInfo
      if (!myInfo) return this.$message.warning('请先登录')
      var myId = String(myInfo.userId || myInfo.adminId)
      var otherId = String(this.userId)
      var minId = myId < otherId ? myId : otherId
      var maxId = myId >= otherId ? myId : otherId
      this.dmSessionId = minId + '_' + maxId
      try {
        var res = await this.$axios.get('/letter/letterList/' + this.dmSessionId)
        if (res.code === 200) {
          this.dmLetterList = (res.data || []).map(function(l) { return Object.assign({}, l, { isMine: String(l.senderId) === myId }) })
        }
      } catch (e) { this.dmLetterList = [] }
      this.dmDialogVisible = true
      var self = this
      this.$nextTick(function() {
        var box = self.$refs.dmChatBox
        if (box) box.scrollTop = box.scrollHeight
      })
    },
    async sendDM() {
      if (!this.dmContent.trim()) return
      var res = await this.$axios.post('/letter/send', { receiverId: this.userId, letterDetail: this.dmContent })
      if (res.code === 200) {
        this.dmContent = ''
        var myInfo = this.$store.getters.getUserInfo
        var myId = String(myInfo.userId || myInfo.adminId)
        var msgRes = await this.$axios.get('/letter/letterList/' + this.dmSessionId)
        if (msgRes.code === 200) {
          this.dmLetterList = (msgRes.data || []).map(function(l) { return Object.assign({}, l, { isMine: String(l.senderId) === myId }) })
        }
        var self = this
        this.$nextTick(function() {
          var box = self.$refs.dmChatBox
          if (box) box.scrollTop = box.scrollHeight
        })
      }
    }
  }
}
</script>
<style scoped>
.profile-content-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s ease;
  border-radius: 8px;
}
.profile-content-item:hover { background: #f5f7fa; }
.profile-content-item:last-child { border-bottom: none; }
.profile-user-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  border-radius: 8px;
  transition: background 0.3s ease;
}
.profile-user-item:hover { background: #f5f7fa; }
.profile-user-item:last-child { border-bottom: none; }

/* ===== Dialog Beautification ===== */
::v-deep .user-list-dialog .el-dialog__body {
  padding: 8px 16px 16px;
}
.profile-user-info {
  flex: 1;
  margin-left: 12px;
  overflow: hidden;
}
.profile-user-name {
  font-weight: 600;
  color: #303133;
  cursor: pointer;
  transition: color 0.25s ease;
}
.profile-user-name:hover {
  color: #409EFF;
}
.dialog-empty {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
}

::v-deep .like-dialog .el-dialog__body {
  padding: 0;
}
.like-dialog-body {
  text-align: center;
  padding: 40px 0;
}
.like-count {
  font-size: 56px;
  font-weight: bold;
  background: linear-gradient(135deg, #409EFF, #667eea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.like-label {
  color: #909399;
  margin-top: 12px;
  font-size: 14px;
}

::v-deep .dm-dialog .el-dialog__body {
  padding: 0;
}
.dm-chat-box {
  max-height: 400px;
  overflow-y: auto;
  padding: 16px 20px;
  background: #f8f9fa;
}
.dm-message {
  margin: 10px 0;
  display: flex;
  flex-direction: column;
}
.dm-mine {
  align-items: flex-end;
}
.dm-other {
  align-items: flex-start;
}
.dm-bubble {
  display: inline-block;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 14px;
  max-width: 80%;
  word-break: break-word;
}
.dm-mine .dm-bubble {
  background: linear-gradient(135deg, #409EFF, #667eea);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.dm-other .dm-bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #ebeef5;
  border-bottom-left-radius: 4px;
}
.dm-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}
.dm-input-area {
  display: flex;
  gap: 10px;
  padding: 12px 20px;
  border-top: 1px solid #eee;
  background: #fff;
}
</style>
