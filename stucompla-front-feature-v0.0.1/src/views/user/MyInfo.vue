<!--
  组件名：MyInfo
  功能描述：个人信息页
  主要职责：
    1. 头像上传与资料编辑
    2. 统计数据展示（获赞/互关/关注/粉丝）
    3. 内容标签页（帖子/商品/表白墙/喜欢/收藏）
    4. 关注/粉丝/互关列表弹窗
-->
<template>
  <div v-loading="loading">
    <!-- Top Section: User Profile Card -->
    <el-card shadow="hover" style="border-radius:8px;">
      <div style="display:flex;align-items:flex-start;">
        <div style="position:relative;">
          <el-avatar v-if="form.avatar" :src="form.avatar" :size="100"></el-avatar>
          <el-avatar v-else :size="100" :style="isAdmin ? 'background:#E6A23C;font-size:36px;' : 'background:#409EFF;font-size:36px;'">{{ (form.nickname || form.username || '用')[0] }}</el-avatar>
          <el-button v-if="!isAdmin" type="primary" size="mini" circle icon="el-icon-camera" style="position:absolute;bottom:0;right:0;" @click="$refs.avatarInput.click()"></el-button>
          <input ref="avatarInput" type="file" accept="image/*" style="display:none;" @change="handleAvatarChange" />
        </div>
        <div style="margin-left:20px;flex:1;">
          <div style="display:flex;align-items:center;flex-wrap:wrap;gap:8px;">
            <span style="font-size:22px;font-weight:bold;">{{ form.nickname || form.username || '用户' }}</span>
            <span v-if="!isAdmin && form.sex === '男'" style="color:#409EFF;font-size:14px;">♂</span>
            <span v-else-if="!isAdmin && form.sex === '女'" style="color:#F56C6C;font-size:14px;">♀</span>
            <el-tag v-if="isAdmin" :type="form.roleName === 'super' ? 'danger' : 'warning'" size="small">{{ form.roleName === 'super' ? '超级管理员' : '管理员' }}</el-tag>
          </div>
          <div style="color:#999;font-size:13px;margin-top:6px;">
            <template v-if="isAdmin">ID: {{ form.adminId | formatId(form.roleName === 'super' ? 'root' : 'admin') }}</template>
            <template v-else>ID: {{ form.userId | formatId('user') }}</template>
          </div>
          <div v-if="isAdmin && form.permissions" style="margin-top:6px;">
            <span style="color:#909399;font-size:12px;">权限: </span>
            <el-tag v-for="perm in permissionLabels" :key="perm" size="mini" type="info" style="margin-right:4px;margin-bottom:2px;">{{ perm }}</el-tag>
          </div>
          <div style="margin-top:12px;display:flex;gap:10px;">
            <el-button v-if="!isAdmin" type="primary" size="small" icon="el-icon-edit" @click="editDialogVisible = true">编辑资料</el-button>
            <el-button v-if="isAdmin" type="primary" size="small" icon="el-icon-edit" @click="editDialogVisible = true">修改用户名</el-button>
            <el-button size="small" icon="el-icon-lock" @click="$router.push('/securityCenter').catch(() => {})">安全中心</el-button>
            <el-button size="small" icon="el-icon-warning-outline" @click="$router.push('/myPunishment').catch(() => {})">处罚管理</el-button>

          </div>
        </div>
      </div>
    </el-card>

    <!-- Stats Row - only for regular users -->
    <el-card v-if="!isAdmin" shadow="hover" style="margin-top:16px;border-radius:8px;">
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
    <el-card v-if="form.signature" shadow="hover" style="margin-top:16px;border-radius:8px;">
      <div style="color:#606266;font-size:14px;line-height:1.6;">
        <i class="el-icon-chat-line-square" style="margin-right:6px;color:#909399;"></i>{{ form.signature }}
      </div>
    </el-card>

    <!-- Content Tabs -->
    <el-card shadow="hover" style="margin-top:16px;border-radius:8px;">
      <el-tabs v-model="activeTab" @tab-click="onTabChange">
        <el-tab-pane :label="'帖子(' + postList.length + ')'" name="posts">
          <div v-if="postList.length === 0" style="text-align:center;color:#999;padding:30px 0;">暂无帖子</div>
          <div v-for="post in postList" :key="post.postId" class="profile-content-item" @click="$router.push('/postDetail/' + post.postId).catch(() => {})">
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <span style="font-weight:500;font-size:15px;">
                {{ post.title }}
                <el-tag v-if="post.auditState === 0" size="mini" type="warning" style="margin-left:6px;">待审核</el-tag>
                <el-tag v-else-if="post.auditState === 2" size="mini" type="danger" style="margin-left:6px;">未通过</el-tag>
              </span>
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
              <span style="font-weight:500;font-size:15px;">
                {{ goods.goodsName }}
                <el-tag v-if="goods.auditState === 0" size="mini" type="warning" style="margin-left:6px;">待审核</el-tag>
                <el-tag v-else-if="goods.auditState === 2" size="mini" type="danger" style="margin-left:6px;">未通过</el-tag>
              </span>
              <span style="color:#F56C6C;font-weight:bold;font-size:15px;">￥{{ goods.goodsPrice }}</span>
            </div>
            <div style="color:#999;font-size:12px;margin-top:6px;">{{ goods.createTime | formatTime }}</div>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="'表白墙(' + wallList.length + ')'" name="walls">
          <div v-if="wallList.length === 0" style="text-align:center;color:#999;padding:30px 0;">暂无表白墙</div>
          <div v-for="wall in wallList" :key="wall.wallId" class="profile-content-item" @click="$router.push('/wallDetail/' + wall.wallId).catch(() => {})">
            <div style="font-weight:500;font-size:15px;">
              <span v-html="renderMentionText(wall.content || wall.wallContent || '表白墙 #' + wall.wallId, wall.mentionUsers)"></span>
              <el-tag v-if="wall.auditState === 0" size="mini" type="warning" style="margin-left:6px;">待审核</el-tag>
              <el-tag v-else-if="wall.auditState === 2" size="mini" type="danger" style="margin-left:6px;">未通过</el-tag>
            </div>
            <div style="color:#999;font-size:12px;margin-top:6px;">{{ wall.createTime | formatTime }}</div>
          </div>
        </el-tab-pane>
        <el-tab-pane v-if="!isAdmin" :label="'喜欢(' + likeList.length + ')'" name="likes">
          <div v-if="likeList.length === 0" style="text-align:center;color:#999;padding:30px 0;">暂无喜欢</div>
          <div v-for="post in likeList" :key="post.postId" class="profile-content-item" @click="$router.push('/postDetail/' + post.postId).catch(() => {})">
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
        <el-tab-pane v-if="!isAdmin" :label="'收藏(' + collectList.length + ')'" name="collect">
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

    <!-- Edit Profile Dialog - User -->
    <el-dialog v-if="!isAdmin" title="编辑资料" :visible.sync="editDialogVisible" width="520px" class="edit-profile-dialog">
      <div class="edit-profile-body">
        <el-form ref="editForm" :model="editForm" label-width="80px">
          <el-form-item label="用户名"><el-input v-model="editForm.username" disabled></el-input></el-form-item>
          <el-form-item label="昵称"><el-input v-model="editForm.nickname" placeholder="请输入昵称"></el-input></el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="editForm.sex">
              <el-radio label="男">男</el-radio>
              <el-radio label="女">女</el-radio>
              <el-radio label="不愿透露">不愿透露</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="个性签名"><el-input type="textarea" v-model="editForm.signature" :rows="3" placeholder="写点什么..."></el-input></el-form-item>
        </el-form>
      </div>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="saveInfo">保存</el-button>
      </span>
    </el-dialog>

    <!-- Edit Profile Dialog - Admin -->
    <el-dialog v-if="isAdmin" title="修改用户名" :visible.sync="editDialogVisible" width="520px" class="edit-profile-dialog">
      <div class="edit-profile-body">
        <el-form ref="editForm" :model="editForm" label-width="80px">
          <el-form-item label="用户名"><el-input v-model="editForm.username" placeholder="请输入新用户名"></el-input></el-form-item>
        </el-form>
      </div>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="saveInfo">保存</el-button>
      </span>
    </el-dialog>

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
          <el-button size="small" type="danger" plain @click="unfollowUser(item.userId)">取消关注</el-button>
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
          <el-button size="small" :type="isFollowingUser(item.userId) ? 'default' : 'primary'" plain @click="toggleFollowUser(item.userId)">
            {{ isFollowingUser(item.userId) ? '已关注' : '回关' }}
          </el-button>
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
  </div>
</template>
<script>
var PERMISSION_MAP = {
  post_manage: '帖子管理', goods_manage: '商品管理', wall_manage: '表白墙管理',
  comment_manage: '评论管理', order_manage: '订单管理', announcement_manage: '公告管理',
  user_manage: '用户管理', report_manage: '举报管理', punishment_manage: '处罚管理',
  stats_view: '数据统计'
}

export default {
  name: 'MyInfo',
  data() {
    return {
      form: { userId: '', adminId: '', username: '', nickname: '', sex: '男', signature: '', avatar: '', roleId: null, roleName: '', permissions: '' },
      editForm: { username: '', nickname: '', sex: '男', signature: '' },
      editDialogVisible: false,
      saveLoading: false,
      loading: false,
      likeCount: 0,
      mutualCount: 0,
      followerCount: 0,
      followingCount: 0,
      activeTab: this.$route.query.tab || 'posts',
      postList: [],
      goodsList: [],
      wallList: [],
      likeList: [],
      collectList: [],
      followingList: [],
      followersList: [],
      mutualList: [],
      myFollowingIds: [],
      dialogLoading: false,
      followingDialogVisible: false,
      followersDialogVisible: false,
      mutualDialogVisible: false,
      likeDialogVisible: false,
      mentionUserMap: {}
    }
  },
  computed: {
    isAdmin() { return this.$store.getters.getLoginType === 'admin' },
    permissionLabels() {
      if (!this.form.permissions) return []
      var perms = this.form.permissions.split(',').map(function(s) { return s.trim() }).filter(Boolean)
      return perms.map(function(p) { return PERMISSION_MAP[p] || p })
    },
    currentUserId() {
      return this.isAdmin ? this.form.adminId : this.form.userId
    }
  },
  created() {
    this.loadUserInfo()
  },
  watch: {
    '$route.query.tab'(val) {
      if (val && ['posts', 'goods', 'walls', 'likes', 'collect'].includes(val)) {
        this.activeTab = val
        this.onTabChange({ name: val })
      }
    },
    editDialogVisible(val) {
      if (val) {
        if (this.isAdmin) {
          this.editForm = { username: this.form.username }
        } else {
          this.editForm = { username: this.form.username, nickname: this.form.nickname, sex: this.form.sex, signature: this.form.signature }
        }
      }
    }
  },
  methods: {
    async loadUserInfo() {
      this.loading = true
      try {
        if (this.isAdmin) {
          var res = await this.$axios.get('/admin/info')
          if (res.code === 200) {
            this.form = { ...this.form, ...res.data }
            this.$store.commit('SET_USER_INFO', res.data)
          }
        } else {
          var res = await this.$axios.get('/user/info')
          if (res.code === 200) {
            this.form = { ...this.form, ...res.data }
            this.$store.commit('SET_USER_INFO', res.data)
          }
        }
      } catch (e) {}
      this.loading = false
      // Load stats and posts after user info is available
      if (!this.isAdmin) {
        this.loadStats()
      }
      // 根据初始tab加载对应数据
      var initTab = this.$route.query.tab || 'posts'
      if (initTab === 'posts' || !initTab) this.loadPosts()
      if (initTab === 'goods') this.loadGoods()
      if (initTab === 'walls') this.loadWalls()
      if (initTab === 'likes') this.loadLikes()
      if (initTab === 'collect') this.loadCollect()
      // 预加载其他tab数据
      if (initTab !== 'posts') this.loadPosts()
      if (initTab !== 'goods') this.loadGoods()
      if (initTab !== 'walls') this.loadWalls()
      if (!this.isAdmin && initTab !== 'likes') this.loadLikes()
      if (!this.isAdmin && initTab !== 'collect') this.loadCollect()
    },
    async loadStats() {
      var userId = this.form.userId
      if (!userId) return
      try {
        const fc = await this.$axios.get('/follow/followerCount/' + userId)
        if (fc.code === 200) this.followerCount = fc.data || 0
        const fgc = await this.$axios.get('/follow/followingCount/' + userId)
        if (fgc.code === 200) this.followingCount = fgc.data || 0
      } catch (e) {}
      this.mutualCount = Math.min(this.followerCount, this.followingCount)
      try {
        const lc = await this.$axios.get('/stats/likeCount?userId=' + userId)
        if (lc.code === 200) this.likeCount = lc.data || 0
      } catch (e) { this.likeCount = 0 }
    },
    handleAvatarChange(e) {
      var file = e.target.files[0]
      if (!file) return
      this.uploadAvatar(file)
      e.target.value = ''
    },
    async uploadAvatar(file) {
      if (this.isAdmin) return
      try {
        var formData = new FormData()
        formData.append('file', file)
        var res = await this.$axios.post('/user/uploadAvatar', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
        if (res.code === 200) {
          this.form.avatar = res.data
          this.$message.success('头像上传成功')
          var userRes = await this.$axios.get('/user/info')
          if (userRes.code === 200) this.$store.commit('SET_USER_INFO', userRes.data)
        } else {
          this.$message.error(res.msg || '头像上传失败')
        }
      } catch (e) { this.$message.error('头像上传失败') }
    },
    async saveInfo() {
      this.saveLoading = true
      try {
        if (this.isAdmin) {
          var params = new URLSearchParams()
          params.append('username', this.editForm.username)
          var res = await this.$axios.post('/admin/info/changeMyUsername', params)
          if (res.code === 200) {
            this.$message.success('修改成功')
            this.editDialogVisible = false
            await this.loadUserInfo()
          } else {
            this.$message.error(res.msg || '修改失败')
          }
        } else {
          var res = await this.$axios.post('/user/editUserInfo', this.editForm)
          if (res.code === 200) {
            this.$message.success('修改成功')
            this.editDialogVisible = false
            await this.loadUserInfo()
          }
        }
      } catch (e) {}
      this.saveLoading = false
    },
    onTabChange(tab) {
      if (tab.name === 'posts') this.loadPosts()
      else if (tab.name === 'goods') this.loadGoods()
      else if (tab.name === 'walls') this.loadWalls()
      else if (tab.name === 'likes') this.loadLikes()
      else if (tab.name === 'collect') this.loadCollect()
    },
    async loadPosts() {
      try {
        var uid = this.currentUserId
        var res = await this.$axios.get('/post/list', { params: { pageNum: 1, pageSize: 9999, userId: uid } })
        if (res.code === 200) {
          this.postList = res.data.records || []
        }
      } catch (e) {}
    },
    async loadGoods() {
      try {
        var uid = this.currentUserId
        var res = await this.$axios.get('/goods/getList', { params: { pageNum: 1, pageSize: 9999, userId: uid } })
        if (res.code === 200) {
          this.goodsList = res.data.records || []
        }
      } catch (e) {}
    },
    async loadWalls() {
      try {
        var res = await this.$axios.get('/wall/myWallList', { params: { pageNum: 1, pageSize: 9999 } })
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
        var res = await this.$axios.get('/collect/list', { params: { pageNum: 1, pageSize: 9999, userId: this.form.userId } })
        if (res.code === 200) this.collectList = res.data.records || []
      } catch (e) {}
    },
    async loadLikes() {
      try {
        var res = await this.$axios.get('/post/myLikes', { params: { pageNum: 1, pageSize: 9999 } })
        if (res.code === 200) this.likeList = res.data.records || []
      } catch (e) {}
    },
    // Dialog methods
    showLikeDialog() { this.likeDialogVisible = true },
    async showFollowingDialog() {
      this.followingDialogVisible = true
      this.dialogLoading = true
      try {
        var res = await this.$axios.get('/follow/following/' + this.form.userId, { params: { pageNum: 1, pageSize: 9999 } })
        if (res.code === 200) this.followingList = res.data.records || []
      } catch (e) {}
      this.dialogLoading = false
    },
    async showFollowersDialog() {
      this.followersDialogVisible = true
      this.dialogLoading = true
      try {
        var res = await this.$axios.get('/follow/followers/' + this.form.userId, { params: { pageNum: 1, pageSize: 9999 } })
        if (res.code === 200) this.followersList = res.data.records || []
        var fgRes = await this.$axios.get('/follow/following/' + this.form.userId, { params: { pageNum: 1, pageSize: 9999 } })
        if (fgRes.code === 200) this.myFollowingIds = (fgRes.data.records || []).map(function(r) { return r.userId })
      } catch (e) {}
      this.dialogLoading = false
    },
    async showMutualDialog() {
      this.mutualDialogVisible = true
      this.dialogLoading = true
      try {
        var res = await this.$axios.get('/follow/following/' + this.form.userId, { params: { pageNum: 1, pageSize: 9999 } })
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
    isFollowingUser(uid) { return this.myFollowingIds.indexOf(uid) !== -1 },
    async toggleFollowUser(uid) {
      try {
        if (this.isFollowingUser(uid)) {
          await this.$axios.delete('/follow/cancel/' + uid)
          this.$message.success('已取消关注')
          var idx = this.myFollowingIds.indexOf(uid)
          if (idx !== -1) this.myFollowingIds.splice(idx, 1)
        } else {
          await this.$axios.post('/follow/add/' + uid)
          this.$message.success('关注成功')
          this.myFollowingIds.push(uid)
        }
        this.loadStats()
      } catch (e) {}
    },
    async unfollowUser(uid) {
      try {
        await this.$axios.delete('/follow/cancel/' + uid)
        this.$message.success('已取消关注')
        this.showFollowingDialog()
        this.loadStats()
      } catch (e) {}
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
    }
  }
}
</script>
<style scoped>
.profile-content-item {
  padding: 14px 18px;
  border-bottom: 1px solid rgba(0,0,0,0.04);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  border-radius: 12px;
  margin-bottom: 6px;
  background: linear-gradient(135deg, #fafafa, #f5f7fa);
  border: 1px solid transparent;
}
.profile-content-item:hover {
  background: linear-gradient(135deg, #f0f5ff, #e6f0ff);
  transform: translateX(6px);
  border-color: rgba(64, 158, 255, 0.1);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.08);
}
.profile-content-item:last-child { border-bottom: none; margin-bottom: 0; }
.profile-user-item {
  display: flex;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid rgba(0,0,0,0.04);
  border-radius: 12px;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  margin-bottom: 8px;
  background: linear-gradient(135deg, #fafafa, #f5f7fa);
  border: 1px solid transparent;
}
.profile-user-item:hover {
  background: linear-gradient(135deg, #f0f5ff, #e6f0ff);
  transform: translateX(6px);
  border-color: rgba(64, 158, 255, 0.1);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.08);
}
.profile-user-item:last-child { border-bottom: none; margin-bottom: 0; }

/* ===== Dialog Beautification ===== */
::v-deep .edit-profile-dialog .el-dialog__body {
  padding: 0;
}
.edit-profile-body {
  padding: 28px;
}

::v-deep .user-list-dialog .el-dialog__body {
  padding: 12px 20px 20px;
}
.profile-user-info {
  flex: 1;
  margin-left: 14px;
  overflow: hidden;
}
.profile-user-name {
  font-weight: 600;
  color: #303133;
  cursor: pointer;
  transition: color 0.3s ease;
}
.profile-user-name:hover {
  color: #409EFF;
  text-shadow: 0 0 8px rgba(64, 158, 255, 0.15);
}
.dialog-empty {
  text-align: center;
  color: #a0a3a8;
  padding: 48px 0;
  font-size: 14px;
  font-weight: 500;
}

::v-deep .like-dialog .el-dialog__body {
  padding: 0;
}
.like-dialog-body {
  text-align: center;
  padding: 48px 0;
}
.like-count {
  font-size: 64px;
  font-weight: bold;
  background: linear-gradient(135deg, #409EFF, #667eea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}
.like-label {
  color: #909399;
  margin-top: 16px;
  font-size: 14px;
  font-weight: 500;
}
</style>
