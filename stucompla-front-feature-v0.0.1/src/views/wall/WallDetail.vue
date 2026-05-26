<!--
  组件名：WallDetail
  功能描述：表白墙详情页
  主要职责：
    1. 内容展示
    2. 评论系统
    3. 点赞/收藏/分享/举报/编辑/删除
    4. AI润色功能
-->
<template>
  <div v-loading="loading">
    <el-card v-if="wallData">
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
        <el-page-header @back="$router.back()" content="表白墙详情"></el-page-header>
        <div>
          <el-button v-if="isOwner" type="primary" size="small" @click="openEditDialog">编辑</el-button>
          <el-button v-if="isOwner" type="danger" size="small" @click="deleteWall">删除</el-button>
          <el-button v-if="!isOwner" type="warning" size="small" plain @click="openReportDialog('wall', $route.params.id)">举报</el-button>
          <el-button :type="liked ? 'danger' : 'default'" size="small" @click="toggleLike">
            <i class="el-icon-thumb" :style="liked ? 'color:#F56C6C;' : ''"></i> {{ wallData.likeNum || 0 }}
          </el-button>
          <el-button :type="collected ? 'warning' : 'default'" size="small" @click="toggleCollect">
            <i class="el-icon-star-off"></i> {{ wallData.collectNum || 0 }}
          </el-button>
          <el-button size="small" @click="shareWall">
            <i class="el-icon-share"></i> {{ wallData.shareNum || 0 }}
          </el-button>
        </div>
      </div>
      <div style="margin-bottom:15px;color:#999;font-size:13px;">
        <span v-if="!wallData.isAnonymous" style="cursor:pointer;color:#409EFF;" @click="$router.push('/userProfile/' + wallData.userId).catch(function(){})">作者：{{ wallData.nickname || '匿名' }}</span>
        <span v-else>作者：匿名用户</span>
        <span v-if="wallData.sex === '男'" style="color:#409EFF;font-size:12px;">♂</span>
        <span v-else-if="wallData.sex === '女'" style="color:#F56C6C;font-size:12px;">♀</span>
        <el-divider direction="vertical"></el-divider>
        <span>分类：表白墙</span>
        <el-divider direction="vertical"></el-divider>
        <span>浏览：{{ wallData.viewNum || 0 }}</span>
        <el-divider direction="vertical"></el-divider>
        <span>发布时间：{{ formatTime(wallData.createTime) }}</span>
        <template v-if="wallData.updateTime && wallData.updateTime !== wallData.createTime">
          <br/>
          <span style="color:#E6A23C;font-size:12px;">已重新编辑于 {{ formatTime(wallData.updateTime) }}</span>
        </template>
      </div>
      <div style="font-size:16px;line-height:1.8;white-space:pre-wrap;margin-bottom:20px;" v-html="renderMentionText(wallData.wallContent, wallData.mentionUsers)"></div>
      <div v-if="wallData.locked === 1" style="margin-bottom:10px;">
        <el-tag type="danger" size="small">已锁定</el-tag>
        <el-tag type="warning" size="small" style="margin-left:4px;">仅自己可见</el-tag>
        <div v-if="wallData.lockReason" style="font-size:12px;color:#999;margin-top:4px;">
          锁定原因：{{ wallData.lockReason }}
        </div>
      </div>
      <div v-if="wallImages.length > 0" style="margin-top:20px;">
        <el-image v-for="(img, i) in wallImages" :key="i" :src="img" :preview-src-list="wallImages" fit="cover" style="width:300px;height:200px;margin-right:10px;margin-bottom:10px;border-radius:8px;cursor:pointer;"></el-image>
      </div>
    </el-card>
    <el-empty v-else-if="!loading" description="内容不存在"></el-empty>

    <el-card style="margin-top:20px;">
      <div slot="header"><span>评论 ({{ totalComments }})</span></div>
      <div style="margin-bottom:20px;">
        <div v-if="replyTarget" style="margin-bottom:8px;display:flex;align-items:center;">
          <span style="color:#409EFF;font-size:13px;">回复 {{ replyTarget.nickname || '用户' + replyTarget.userId }} ({{ replyTarget.floor + '楼' }})</span>
          <el-button type="text" size="mini" style="margin-left:8px;" @click="cancelReply">取消</el-button>
        </div>
        <MentionInput v-model="commentForm.text" :rows="3" :placeholder="replyTarget ? '回复 ' + (replyTarget.nickname || '用户' + replyTarget.userId) + '...' : '写下你的评论...'" @mention-change="onCommentMentionChange" />
        <div style="margin-top:10px;text-align:right;">
          <el-button type="primary" size="small" @click="submitComment">{{ replyTarget ? '回复' : '发表评论' }}</el-button>
        </div>
      </div>

      <template v-for="c in topLevelComments">
        <div class="comment-item">
          <div class="comment-header">
            <div class="comment-user">
              <el-avatar v-if="c.avatar" :src="c.avatar" :size="32" style="margin-right:8px;cursor:pointer;" @click="$router.push('/userProfile/' + c.userId).catch(function(){})"></el-avatar>
              <el-avatar v-else :size="32" style="margin-right:8px;background:#409EFF;cursor:pointer;" @click="$router.push('/userProfile/' + c.userId).catch(function(){})">{{ (c.nickname || '用')[0] }}</el-avatar>
              <el-tag size="mini" type="info" style="margin-right:6px;">{{ c.floor + '楼' }}</el-tag>
              <span class="comment-name" style="cursor:pointer;" @click="$router.push('/userProfile/' + c.userId).catch(function(){})">{{ c.nickname || '用户' + c.userId }}</span>
              <span v-if="c.sex === '男'" style="color:#409EFF;font-size:12px;">♂</span>
              <span v-else-if="c.sex === '女'" style="color:#F56C6C;font-size:12px;">♀</span>
              <el-tag v-if="c.roleName" :type="c.roleName === 'super' ? 'danger' : 'warning'" size="mini" style="margin-left:4px;">{{ c.roleName === 'super' ? '超管' : '管理员' }}</el-tag>
            </div>
            <div class="comment-actions">
              <span :class="{ 'liked': isCommentLiked(c.commentId) }" class="action-btn" @click="toggleCommentLike(c)">
                <i class="el-icon-thumb" :style="isCommentLiked(c.commentId) ? 'color:#F56C6C;' : ''"></i> {{ c.likeNum || 0 }}
              </span>
              <span class="action-btn" @click="replyTo(c)"><i class="el-icon-chat-dot-round"></i> 回复</span>
              <span v-if="isCommentOwner(c)" class="action-btn" style="color:#F56C6C;" @click="deleteComment(c.commentId)"><i class="el-icon-delete"></i> 删除</span>
              <span v-else class="action-btn" style="color:#E6A23C;" @click="openReportDialog('comment', c.commentId)"><i class="el-icon-warning"></i> 举报</span>
              <span class="comment-time">{{ formatTime(c.createTime) }}</span>
            </div>
          </div>
          <div class="comment-body" v-html="renderMentionText(c.text, c.mentionUsers)"></div>
          <div v-if="c.images && getImages(c.images).length > 0" class="comment-images">
            <el-image v-for="(img, i) in getImages(c.images)" :key="i" :src="img" :preview-src-list="getImages(c.images)" fit="cover" style="width:80px;height:60px;margin-right:5px;border-radius:4px;cursor:pointer;"></el-image>
          </div>
        </div>
        <div v-for="r in getReplies(c.commentId)" :key="'r-'+r.commentId" class="comment-item reply-item">
          <div class="comment-header">
            <div class="comment-user">
              <el-avatar v-if="r.avatar" :src="r.avatar" :size="28" style="margin-right:6px;cursor:pointer;" @click="$router.push('/userProfile/' + r.userId).catch(function(){})"></el-avatar>
              <el-avatar v-else :size="28" style="margin-right:6px;background:#67C23A;cursor:pointer;" @click="$router.push('/userProfile/' + r.userId).catch(function(){})">{{ (r.nickname || '用')[0] }}</el-avatar>
              <el-tag size="mini" type="info" style="margin-right:6px;">{{ r.floor + '楼' }}</el-tag>
              <span class="comment-name" style="cursor:pointer;" @click="$router.push('/userProfile/' + r.userId).catch(function(){})">{{ r.nickname || '用户' + r.userId }}</span>
              <span v-if="r.sex === '男'" style="color:#409EFF;font-size:12px;">♂</span>
              <span v-else-if="r.sex === '女'" style="color:#F56C6C;font-size:12px;">♀</span>
              <el-tag v-if="r.roleName" :type="r.roleName === 'super' ? 'danger' : 'warning'" size="mini" style="margin-left:4px;">{{ r.roleName === 'super' ? '超管' : '管理员' }}</el-tag>
              <span v-if="r.replyToNickname" class="reply-hint"> 回复 <b>{{ r.replyToNickname }}</b><span v-if="r.replyToSex === '男'" style="color:#409EFF;font-size:11px;">♂</span><span v-else-if="r.replyToSex === '女'" style="color:#F56C6C;font-size:11px;">♀</span></span>
            </div>
            <div class="comment-actions">
              <span :class="{ 'liked': isCommentLiked(r.commentId) }" class="action-btn" @click="toggleCommentLike(r)">
                <i class="el-icon-thumb" :style="isCommentLiked(r.commentId) ? 'color:#F56C6C;' : ''"></i> {{ r.likeNum || 0 }}
              </span>
              <span class="action-btn" @click="replyTo(r)"><i class="el-icon-chat-dot-round"></i> 回复</span>
              <span v-if="isCommentOwner(r)" class="action-btn" style="color:#F56C6C;" @click="deleteComment(r.commentId)"><i class="el-icon-delete"></i> 删除</span>
              <span v-else class="action-btn" style="color:#E6A23C;" @click="openReportDialog('comment', r.commentId)"><i class="el-icon-warning"></i> 举报</span>
              <span class="comment-time">{{ formatTime(r.createTime) }}</span>
            </div>
          </div>
          <div class="comment-body" v-html="renderMentionText(r.text, r.mentionUsers)"></div>
          <div v-if="r.images && getImages(r.images).length > 0" class="comment-images">
            <el-image v-for="(img, i) in getImages(r.images)" :key="i" :src="img" :preview-src-list="getImages(r.images)" fit="cover" style="width:60px;height:45px;margin-right:4px;border-radius:3px;cursor:pointer;"></el-image>
          </div>
        </div>
      </template>

      <el-empty v-if="totalComments === 0" description="暂无评论"></el-empty>
    </el-card>

    <el-dialog title="举报" :visible.sync="reportDialogVisible" width="400px">
      <el-form :model="reportForm" label-width="80px">
        <el-form-item label="举报原因">
          <el-select v-model="reportForm.reason" placeholder="请选择举报原因" style="width:100%;">
            <el-option label="垃圾广告" value="垃圾广告"></el-option>
            <el-option label="色情低俗" value="色情低俗"></el-option>
            <el-option label="违法违规" value="违法违规"></el-option>
            <el-option label="诈骗信息" value="诈骗信息"></el-option>
            <el-option label="人身攻击" value="人身攻击"></el-option>
            <el-option label="虚假信息" value="虚假信息"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="reportForm.reason === '其他'" label="详细原因">
          <el-input v-model="reportForm.customReason" type="textarea" :rows="3" placeholder="请描述举报原因"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="reportDialogVisible = false">取消</el-button><el-button type="primary" @click="submitReport">确定</el-button></span>
    </el-dialog>

    <el-dialog title="编辑表白墙" :visible.sync="editDialogVisible" width="600px" @close="resetEditForm">
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="内容" prop="wallContent">
          <el-input type="textarea" v-model="editForm.wallContent" :rows="6" placeholder="写下你想说的话..."></el-input>
          <el-button type="warning" size="mini" icon="el-icon-magic-stick" :loading="polishing" style="margin-top:6px;" @click="aiPolish">AI润色内容</el-button>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload action="/dev_api/image/upload" :headers="uploadHeaders" name="files" list-type="picture-card" :file-list="editFileList" :on-success="handleUploadSuccess" :on-remove="handleRemove" :on-error="function() { $message.error('图片上传失败') }" :before-upload="beforeUpload" accept="image/*" :limit="9" :on-exceed="function() { $message.warning('最多上传9张图片') }">
            <i class="el-icon-plus"></i>
          </el-upload>
        </el-form-item>
        <el-form-item label="可见范围">
          <el-select v-model="editForm.visibility" placeholder="请选择可见范围" style="width:100%;">
            <el-option label="所有人" value="all"></el-option>
            <el-option label="关注的人" value="following"></el-option>
            <el-option label="互相关注" value="mutual"></el-option>
            <el-option label="仅自己" value="self"></el-option>
            <el-option label="不给谁看" value="custom"></el-option>
          </el-select>
          <div v-if="editForm.visibility === 'custom'" style="margin-top:8px;">
            <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
              <el-tag v-for="uid in editForm.blockedUsers" :key="uid" closable size="small" @close="removeBlockedUser(uid)">
                {{ getUserName(uid) }}
              </el-tag>
              <el-button type="primary" size="mini" icon="el-icon-plus" @click="blockDialogVisible = true">添加用户</el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="选择不给谁看" :visible.sync="blockDialogVisible" width="500px" append-to-body>
      <el-input v-model="blockSearchKeyword" placeholder="搜索用户昵称" prefix-icon="el-icon-search" size="small" style="margin-bottom:12px;" @input="searchBlockUsers"></el-input>
      <div style="max-height:300px;overflow-y:auto;">
        <div v-for="user in blockSearchResults" :key="user.userId" style="display:flex;align-items:center;padding:8px 0;border-bottom:1px solid #f0f0f0;">
          <el-avatar :size="32" :src="user.avatar" style="background:#409EFF;">{{ (user.nickname || user.username || '用')[0] }}</el-avatar>
          <span style="margin-left:10px;flex:1;">{{ user.nickname || user.username || '用户' }}<span style="margin-left:6px;font-size:12px;color:#909399;">USR-{{ user.userId }}</span></span>
          <el-button v-if="!isAlreadyBlocked(user.userId)" type="primary" size="mini" @click="addBlockedUser(user.userId)">屏蔽</el-button>
          <el-tag v-else size="mini" type="info">已屏蔽</el-tag>
        </div>
        <div v-if="blockSearchResults.length === 0" style="text-align:center;color:#999;padding:20px 0;">输入昵称搜索用户</div>
      </div>
      <span slot="footer"><el-button @click="blockDialogVisible = false">关闭</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import MentionInput from '@/components/MentionInput.vue'
export default {
  name: 'WallDetail',
  components: { MentionInput },
  data() {
    return {
      wallData: null,
      loading: false,
      rawComments: [],
      replyTarget: null,
      commentForm: { text: '', mentionUsers: '' },
      likedComments: {},
      liked: false,
      collected: false,
      isOwner: false,
      reportDialogVisible: false,
      reportForm: { targetType: '', targetId: '', reason: '', customReason: '' },
      editDialogVisible: false,
      editLoading: false,
      polishing: false,
      editForm: { wallId: '', wallContent: '', wallImages: '', visibility: 'all', blockedUsers: [] },
      editRules: { wallContent: [{ required: true, message: '请输入内容', trigger: 'blur' }] },
      editFileList: [],
      uploadedUrls: [],
      uploadHeaders: { Authorization: sessionStorage.getItem('token') || '' },
      blockDialogVisible: false,
      blockSearchKeyword: '',
      blockSearchResults: [],
      userNamesMap: {},
      mentionUserMap: {}
    }
  },
  computed: {
    wallImages() { return this.wallData && this.wallData.wallImages ? this.wallData.wallImages.split(',').filter(function(img) { return img.trim() }) : [] },
    topLevelComments() { return this.rawComments.filter(function(c) { return !c.parentId || c.parentId === 0 || c.parentId === '0' }) },
    totalComments() { return this.rawComments.length }
  },
  created() { this.loadWall() },
  methods: {
    formatTime(time) {
      if (!time) return ''
      var d = new Date(time)
      var pad = function(n) { return String(n).padStart(2, '0') }
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    },
    getImages(images) { return images ? images.split(',').filter(function(img) { return img.trim() }) : [] },
    getReplies(parentId) { return this.rawComments.filter(function(c) { return c.parentId === parentId }) },
    isCommentLiked(commentId) { return !!this.likedComments[commentId] },
    isCommentOwner(comment) {
      var userInfo = this.$store.getters.getUserInfo
      if (!userInfo) return false
      return String(comment.userId) === String(userInfo.userId || userInfo.adminId)
    },
    replyTo(comment) { this.replyTarget = comment },
    cancelReply() { this.replyTarget = null },
    async loadWall() {
      this.loading = true
      var self = this
      var wallId = this.$route.params.id
      // Load wall data via detail API
      try {
        var res = await this.$axios.get('/wall/detail/' + wallId)
        if (res.code === 200) {
          this.wallData = res.data
          this.liked = res.data.hasLiked || false
          sessionStorage.setItem('liked_wall_' + wallId, this.liked ? 'true' : 'false')
          if (res.data.hasShared) sessionStorage.setItem('shared_wall_' + wallId, 'true')
          if (self.wallData && self.wallData.mentionUsers) self.loadMentionUsers(self.wallData.mentionUsers)
        }
      } catch (e) {}
      // Fallback: try list API if detail fails
      if (!this.wallData) {
        try {
          var res2 = await this.$axios.get('/wall/wallList', { params: { pageNum: 1, pageSize: 1000 } })
          if (res2.code === 200) {
            var list = res2.data.records || []
            this.wallData = list.find(function(w) { return w.wallId === wallId }) || null
            if (self.wallData && self.wallData.mentionUsers) self.loadMentionUsers(self.wallData.mentionUsers)
          }
        } catch (e) {}
      }
      if (!this.wallData) {
        try {
          var res3 = await this.$axios.get('/wall/myWallList', { params: { pageNum: 1, pageSize: 1000 } })
          if (res3.code === 200) {
            var list2 = res3.data.records || []
            this.wallData = list2.find(function(w) { return w.wallId === wallId }) || null
            if (self.wallData && self.wallData.mentionUsers) self.loadMentionUsers(self.wallData.mentionUsers)
          }
        } catch (e) {}
      }
      // Check ownership
      var userInfo = this.$store.getters.getUserInfo
      if (userInfo && this.wallData) {
        self.isOwner = String(userInfo.userId || userInfo.adminId) === String(self.wallData.userId)
      }
      // Load liked state from sessionStorage if not from API
      if (this.wallData && this.wallData.hasLiked === undefined) {
        this.liked = sessionStorage.getItem('liked_wall_' + wallId) === 'true'
      }
      this.collected = sessionStorage.getItem('collected_wall_' + wallId) === 'true'
      // Increment view count
      try { await this.$axios.get('/wall/view/' + wallId) } catch(e) {}
      // Increment local viewNum
      if (this.wallData) {
        self.$set(self.wallData, 'viewNum', (self.wallData.viewNum || 0) + 1)
      }
      // Load comments
      try {
        var commentRes = await this.$axios.get('/comment/wallList/' + wallId + '/1/50')
        if (commentRes.code === 200) {
          self.rawComments = commentRes.data.records || commentRes.data || []
          self.rawComments.forEach(function(c) {
            self.likedComments[c.commentId] = sessionStorage.getItem('liked_comment_' + c.commentId) === 'true'
            if (c.mentionUsers) self.loadMentionUsers(c.mentionUsers)
          })
        }
      } catch (e) {}
      this.loading = false
    },
    async loadComments() {
      var self = this
      var wallId = this.$route.params.id
      try {
        var commentRes = await this.$axios.get('/comment/wallList/' + wallId + '/1/50')
        if (commentRes.code === 200) {
          self.rawComments = commentRes.data.records || commentRes.data || []
          self.rawComments.forEach(function(c) {
            self.likedComments[c.commentId] = sessionStorage.getItem('liked_comment_' + c.commentId) === 'true'
            if (c.mentionUsers) self.loadMentionUsers(c.mentionUsers)
          })
        }
      } catch (e) {}
    },
    async toggleLike() {
      var wallId = this.$route.params.id
      try {
        if (this.liked) {
          var res = await this.$axios.post('/wall/unlike/' + wallId)
          if (res.code === 200) { this.liked = false; this.wallData.likeNum = res.data; sessionStorage.setItem('liked_wall_' + wallId, 'false') }
          else { this.$message.warning(res.msg || '取消点赞失败') }
        } else {
          var res2 = await this.$axios.post('/wall/like/' + wallId)
          if (res2.code === 200) { this.liked = true; this.wallData.likeNum = res2.data; sessionStorage.setItem('liked_wall_' + wallId, 'true') }
          else { this.$message.warning(res2.msg || '点赞失败'); if (res2.msg && res2.msg.includes('已点赞')) { this.liked = true; sessionStorage.setItem('liked_wall_' + wallId, 'true') } }
        }
      } catch(e) {
        this.$message.error('操作失败，请重试')
      }
    },
    async toggleCollect() {
      var wallId = this.$route.params.id
      if (this.collected) {
        var res = await this.$axios.delete('/wall/uncollect/' + wallId)
        if (res.code === 200) { this.collected = false; this.wallData.collectNum = res.data; this.$message.success('已取消收藏'); sessionStorage.setItem('collected_wall_' + wallId, 'false') }
      } else {
        var res2 = await this.$axios.post('/wall/collect/' + wallId)
        if (res2.code === 200) { this.collected = true; this.wallData.collectNum = res2.data; this.$message.success('收藏成功'); sessionStorage.setItem('collected_wall_' + wallId, 'true') }
      }
    },
    async shareWall() {
      var wallId = this.$route.params.id
      try {
        var res = await this.$axios.post('/wall/share/' + wallId)
        if (res.code === 200) {
          this.wallData.shareNum = res.data
          sessionStorage.setItem('shared_wall_' + wallId, 'true')
        } else if (res.msg && res.msg.includes('已分享')) {
          sessionStorage.setItem('shared_wall_' + wallId, 'true')
        } else {
          this.$message.warning(res.msg || '分享失败')
        }
      } catch(e) {}
      if (navigator.clipboard) { navigator.clipboard.writeText(window.location.href); this.$message.success('链接已复制到剪贴板') }
      else this.$message.success('分享成功')
    },
    async deleteWall() {
      try { await this.$confirm('确定删除该表白墙？删除后将移入回收站，30天内可恢复。', '提示', { type: 'warning' }) } catch(e) { return }
      var res = await this.$axios.delete('/wall/deleteMyWall/' + this.$route.params.id)
      if (res.code === 200) { this.$message.success('删除成功'); this.$router.push('/wallList') }
      else this.$message.error(res.msg || '删除失败')
    },
    openEditDialog() {
      this.editForm = { wallId: this.wallData.wallId, wallContent: this.wallData.wallContent, wallImages: this.wallData.wallImages || '', visibility: this.wallData.visibility || 'all', blockedUsers: this.wallData.blockedUsers ? JSON.parse(this.wallData.blockedUsers) : [] }
      this.uploadedUrls = this.wallData.wallImages ? this.wallData.wallImages.split(',').filter(function(u) { return u.trim() }) : []
      this.editFileList = this.uploadedUrls.map(function(url, index) { return { name: 'image' + index, url: url } })
      this.editDialogVisible = true
    },
    resetEditForm() {
      this.editForm = { wallId: '', wallContent: '', wallImages: '', visibility: 'all', blockedUsers: [] }
      this.editFileList = []
      this.uploadedUrls = []
      if (this.$refs.editForm) this.$refs.editForm.resetFields()
    },
    beforeUpload(file) {
      var isImage = file.type.startsWith('image/')
      var isLt5M = file.size / 1024 / 1024 < 5
      if (!isImage) { this.$message.error('只能上传图片文件'); return false }
      if (!isLt5M) { this.$message.error('图片大小不能超过5MB'); return false }
      return true
    },
    handleUploadSuccess(response) {
      var res = typeof response === 'string' ? JSON.parse(response) : response
      if (res.code === 200 && res.data) {
        var urls = res.data.split(',').filter(function(u) { return u.trim() })
        this.uploadedUrls = this.uploadedUrls.concat(urls)
        this.editForm.wallImages = this.uploadedUrls.join(',')
      }
    },
    handleRemove(file) {
      var url = file.response ? (typeof file.response === 'string' ? JSON.parse(file.response).data : file.response.data) : file.url
      if (url) {
        var urls = url.split(',').filter(function(u) { return u.trim() })
        var self = this
        urls.forEach(function(u) {
          var idx = self.uploadedUrls.indexOf(u.trim())
          if (idx > -1) self.uploadedUrls.splice(idx, 1)
        })
        this.editForm.wallImages = this.uploadedUrls.join(',')
      }
    },
    submitEdit() {
      this.$refs.editForm.validate(async function(valid) {
        if (!valid) return
        this.editLoading = true
        var res = await this.$axios.post('/wall/edit', this.editForm)
        if (res.code === 200) { this.$message.success('编辑成功，等待审核'); this.editDialogVisible = false; this.loadWall() }
        else this.$message.error(res.msg || '编辑失败')
        this.editLoading = false
      }.bind(this))
    },
    async aiPolish() {
      if (!this.editForm.wallContent || !this.editForm.wallContent.trim()) {
        this.$message.warning('请先输入内容再润色')
        return
      }
      this.polishing = true
      try {
        var res = await this.$axios.post('/ai/polish', { content: this.editForm.wallContent, type: 'wall' })
        if (res.code === 200 && res.data && res.data.polished) {
          this.editForm.wallContent = res.data.polished
          if (res.data.hint) { this.$message.warning(res.data.hint) }
          else { this.$message.success('润色完成') }
        } else {
          this.$message.error('润色失败')
        }
      } catch (e) {
        this.$message.error('润色请求失败')
      }
      this.polishing = false
    },
    async submitComment() {
      if (!this.commentForm.text) return this.$message.warning('请输入评论内容')
      try {
        var muteRes = await this.$axios.get('/punishment/checkMute')
        if (muteRes.code === 200 && muteRes.data && muteRes.data.muted) {
          this.$alert('您当前已被禁言，原因：' + (muteRes.data.reason || '无'), '禁言提示', { type: 'warning' })
          return
        }
      } catch (e) {}
      var parentId = this.replyTarget ? this.replyTarget.commentId : ''
      var wallId = this.$route.params.id
      var res = await this.$axios.post('/comment/create', {
        postId: wallId,
        targetType: 'wall',
        parentId: parentId,
        text: this.commentForm.text,
        mentionUsers: this.commentForm.mentionUsers
      })
      if (res.code === 200) {
        this.$message.success(this.replyTarget ? '回复成功' : '评论成功')
        this.commentForm.text = ''
        this.commentForm.mentionUsers = ''
        this.replyTarget = null
        this.loadComments()
      }
    },
    async deleteComment(commentId) {
      try { await this.$confirm('确定删除该评论？', '提示', { type: 'warning' }) } catch(e) { return }
      var res = await this.$axios.delete('/comment/' + commentId)
      if (res.code === 200) { this.$message.success('删除成功'); this.loadComments() }
      else this.$message.error(res.msg || '删除失败')
    },
    async toggleCommentLike(comment) {
      var cid = comment.commentId
      if (this.likedComments[cid]) {
        var res = await this.$axios.post('/comment/unlike/' + cid)
        if (res.code === 200) { this.$set(this.likedComments, cid, false); this.$set(comment, 'likeNum', res.data); sessionStorage.removeItem('liked_comment_' + cid) }
      } else {
        var res2 = await this.$axios.post('/comment/like/' + cid)
        if (res2.code === 200) { this.$set(this.likedComments, cid, true); this.$set(comment, 'likeNum', res2.data); sessionStorage.setItem('liked_comment_' + cid, 'true') }
      }
    },
    openReportDialog(targetType, targetId) {
      if (!targetType) {
        this.reportForm = { targetType: 'wall', targetId: String(this.$route.params.id), reason: '', customReason: '' }
      } else {
        this.reportForm = { targetType: targetType, targetId: String(targetId), reason: '', customReason: '' }
      }
      this.reportDialogVisible = true
    },
    async submitReport() {
      if (!this.reportForm.reason) return this.$message.warning('请选择举报原因')
      if (this.reportForm.reason === '其他' && !this.reportForm.customReason) return this.$message.warning('请填写详细原因')
      var reason = this.reportForm.reason === '其他' ? '其他：' + this.reportForm.customReason : this.reportForm.reason
      var res = await this.$axios.post('/report/submit', { targetType: this.reportForm.targetType, targetId: this.reportForm.targetId, reason: reason })
      if (res.code === 200) { this.$message.success('举报成功'); this.reportDialogVisible = false }
      else this.$message.error(res.msg || '举报失败')
    },
    onCommentMentionChange(userIds) {
      this.commentForm.mentionUsers = JSON.stringify(userIds)
    },
    searchBlockUsers() {
      var keyword = this.blockSearchKeyword.trim()
      if (!keyword) { this.blockSearchResults = []; return }
      var self = this
      this.$axios.get('/user/search', { params: { keyword: keyword } }).then(function(res) {
        if (res.code === 200) {
          var users = res.data || []
          var myInfo = self.$store.getters.getUserInfo
          var myId = myInfo && (myInfo.userId || myInfo.adminId)
          self.blockSearchResults = users.filter(function(u) { return String(u.userId) !== String(myId) })
          users.forEach(function(u) { self.$set(self.userNamesMap, String(u.userId), u.nickname || u.username || u.userId) })
        }
      }).catch(function() {})
    },
    getUserName(uid) { return this.userNamesMap[String(uid)] || uid },
    isAlreadyBlocked(uid) { return this.editForm.blockedUsers.some(function(id) { return String(id) === String(uid) }) },
    addBlockedUser(uid) { if (!this.isAlreadyBlocked(uid)) this.editForm.blockedUsers.push(uid) },
    removeBlockedUser(uid) { this.editForm.blockedUsers = this.editForm.blockedUsers.filter(function(id) { return String(id) !== String(uid) }) },
    async loadMentionUsers(mentionUsers) {
      if (!mentionUsers) return
      var ids = []
      try { ids = JSON.parse(mentionUsers) } catch (e) { return }
      if (!ids || ids.length === 0) return
      var self = this
      try {
        var res = await this.$axios.get('/user/batchInfo', { params: { ids: ids.join(',') } })
        if (res.code === 200 && res.data) {
          res.data.forEach(function(u) { if (u.nickname) self.$set(self.mentionUserMap, u.nickname, u.userId) })
        }
      } catch (e) {}
    },
    renderMentionText(text, mentionUsers) {
      if (!text) return ''
      var self = this
      return text.replace(/@(\S+)/g, function(match, name) {
        var userId = self.mentionUserMap[name]
        if (userId) return '<a href="javascript:void(0)" class="mention-link" data-userid="' + userId + '">' + match + '</a>'
        return match
      })
    }
  }
}
</script>

<style scoped>
.comment-item {
  padding: 14px 16px;
  border-bottom: 1px solid #f2f3f5;
  transition: background 0.3s ease;
}
.comment-item:hover {
  background: #fafbfc;
}
.comment-item:last-child {
  border-bottom: none;
}
.reply-item {
  margin-left: 48px;
  padding: 12px 14px;
  background: #fafafa;
  border-radius: 8px;
  border-left: 3px solid #409EFF;
  margin-top: 8px;
  margin-bottom: 8px;
  transition: background 0.3s ease;
}
.reply-item:hover {
  background: #f5f7fa;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.comment-user {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}
.comment-name {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}
.reply-hint {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}
.comment-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.action-btn {
  cursor: pointer;
  font-size: 13px;
  color: #909399;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 6px;
  border-radius: 4px;
}
.action-btn:hover {
  color: #409EFF;
  background: #ecf5ff;
}
.action-btn.liked {
  color: #F56C6C;
}
.action-btn.liked:hover {
  color: #F78989;
  background: #fef0f0;
}
.comment-time {
  font-size: 12px;
  color: #c0c4cc;
}
.comment-body {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  margin-left: 40px;
  word-break: break-word;
}
.comment-images {
  margin-top: 8px;
  margin-left: 40px;
  display: flex;
  flex-wrap: wrap;
}
.comment-images >>> .el-image {
  border-radius: 10px;
  overflow: hidden;
  transition: transform 0.3s ease;
}
.comment-images >>> .el-image:hover {
  transform: scale(1.05);
}
</style>
