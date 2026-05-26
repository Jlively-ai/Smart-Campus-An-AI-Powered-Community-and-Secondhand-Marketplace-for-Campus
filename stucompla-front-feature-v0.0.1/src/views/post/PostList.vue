<!--
  组件名：PostList
  功能描述：帖子列表页
  主要职责：
    1. 帖子列表展示（列表/图文模式切换）
    2. 搜索/分类/排序筛选
    3. 发帖弹窗（含可见范围设置）
    4. AI润色功能
    5. @提及输入
-->
<template>
  <div>
    <el-card>
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
        <span>帖子列表</span>
        <div style="display:flex;align-items:center;gap:10px;">
          <el-button-group>
            <el-button :type="viewMode === 'list' ? 'primary' : 'default'" size="mini" icon="el-icon-s-grid" @click="viewMode = 'list'" title="列表模式"></el-button>
            <el-button :type="viewMode === 'card' ? 'primary' : 'default'" size="mini" icon="el-icon-picture" @click="viewMode = 'card'" title="图文模式"></el-button>
          </el-button-group>
          <el-button type="primary" size="small" icon="el-icon-edit" @click="openPublishDialog">发帖</el-button>

        </div>
      </div>
      <div style="margin-bottom:15px;display:flex;gap:10px;align-items:center;flex-wrap:wrap;">
        <search-panel v-model="searchTitle" module="post" placeholder="搜索帖子标题" input-style="width:220px;" @search="loadPosts" @clear="loadPosts"></search-panel>
        <el-input v-model="searchNickname" placeholder="搜索作者昵称" prefix-icon="el-icon-user" clearable size="small" style="width:160px;" @clear="loadPosts" @keyup.enter.native="loadPosts"></el-input>
        <el-select v-model="searchCategoryId" placeholder="选择分类" clearable @change="loadPosts" style="width:140px;">
          <el-option v-for="c in categories" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId"></el-option>
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="loadPosts">搜索</el-button>
        <div style="margin-left:auto;display:flex;align-items:center;gap:4px;">
          <span style="color:#999;font-size:13px;">排序：</span>
          <el-radio-group v-model="sortBy" size="mini" @change="loadPosts">
            <el-radio-button label="time">最新</el-radio-button>
            <el-radio-button label="likeNum">点赞</el-radio-button>
            <el-radio-button label="viewNum">浏览</el-radio-button>
            <el-radio-button label="commentNum">评论</el-radio-button>
            <el-radio-button label="shareNum">分享</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 列表模式 -->
      <el-table v-if="viewMode === 'list'" :data="postList" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200">
          <template slot-scope="scope">
            <el-link type="primary" @click="$router.push('/postDetail/' + scope.row.postId)">{{ scope.row.title }}</el-link>
            <el-tag v-if="scope.row.visibility === 'following'" size="mini" type="warning" style="margin-left:4px;">关注可见</el-tag>
            <el-tag v-else-if="scope.row.visibility === 'mutual'" size="mini" type="info" style="margin-left:4px;">互关可见</el-tag>
            <el-tag v-else-if="scope.row.visibility === 'self'" size="mini" type="danger" style="margin-left:4px;">仅自己可见</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100"></el-table-column>
        <el-table-column prop="nickname" label="作者" width="120">
          <template slot-scope="scope">
            <span style="cursor:pointer;color:#409EFF;" @click.stop="$router.push('/userProfile/' + scope.row.userId)">{{ scope.row.nickname || '匿名' }}</span>
            <el-tag v-if="scope.row.roleName" :type="scope.row.roleName === 'super' ? 'danger' : 'warning'" size="mini">{{ scope.row.roleName === 'super' ? '超管' : '管理员' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="commentNum" label="评论" width="70" align="center"></el-table-column>
        <el-table-column prop="viewNum" label="浏览" width="70" align="center"></el-table-column>
        <el-table-column prop="collectNum" label="收藏" width="70" align="center"></el-table-column>
        <el-table-column prop="likeNum" label="点赞" width="70" align="center">
          <template slot-scope="scope">{{ scope.row.likeNum || 0 }}</template>
        </el-table-column>
        <el-table-column prop="shareNum" label="分享" width="70" align="center">
          <template slot-scope="scope">{{ scope.row.shareNum || 0 }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
        </el-table-column>
      </el-table>

      <!-- 图文模式 -->
      <div v-if="viewMode === 'card'" v-loading="loading">
        <el-row :gutter="0" class="post-card-row">
          <el-col :span="6" v-for="post in postList" :key="post.postId">
            <div class="post-card" @click="$router.push('/postDetail/' + post.postId)">
              <div class="post-card-img" v-if="post.images">
                <img :src="post.images.split(',')[0]" />
              </div>
              <div class="post-card-img post-card-no-img" v-else>
                <span class="post-card-no-img-text">{{ post.title || post.detail || '暂无内容' }}</span>
              </div>
              <div class="post-card-body">
              <div class="post-card-title">
                {{ post.title }}
                <el-tag v-if="post.visibility === 'following'" size="mini" type="warning" style="margin-left:6px;vertical-align:middle;">关注可见</el-tag>
                <el-tag v-else-if="post.visibility === 'mutual'" size="mini" type="info" style="margin-left:6px;vertical-align:middle;">互关可见</el-tag>
                <el-tag v-else-if="post.visibility === 'self'" size="mini" type="danger" style="margin-left:6px;vertical-align:middle;">仅自己可见</el-tag>
              </div>
              <div class="post-card-meta">
                  <span style="cursor:pointer;color:#409EFF;" @click.stop="$router.push('/userProfile/' + post.userId)">{{ post.nickname || '匿名' }}</span>
                  <span><i class="el-icon-view"></i> {{ post.viewNum || 0 }}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-empty v-if="postList.length === 0 && !loading" description="暂无帖子"></el-empty>
      </div>

      <el-pagination style="margin-top:20px;text-align:center;" @current-change="handlePageChange" :current-page="pageNum" :page-sizes="viewMode === 'card' ? [8, 16, 24, 32] : [5, 10, 20, 50]" :page-size.sync="pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange"></el-pagination>
    </el-card>

    <el-dialog title="发帖" :visible.sync="publishDialogVisible" width="620px" @close="resetPublishForm" class="publish-dialog">
      <div class="publish-dialog-body">
        <el-form ref="publishForm" :model="publishForm" :rules="publishRules" label-width="80px">
          <el-form-item label="标题" prop="title">
            <div style="display:flex;gap:8px;">
              <el-input v-model="publishForm.title" placeholder="请输入帖子标题" style="flex:1;" maxlength="30" show-word-limit></el-input>
              <el-button type="warning" size="small" icon="el-icon-magic-stick" :loading="polishingTitle" @click="aiPolish('title')">润色</el-button>
            </div>
          </el-form-item>
          <el-form-item label="分类" prop="categoryId">
            <el-select v-model="publishForm.categoryId" placeholder="请选择分类" style="width:100%;">
              <el-option v-for="c in categories" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="内容" prop="detail">
            <MentionInput v-model="publishForm.detail" :rows="6" placeholder="请输入帖子内容" @mention-change="onMentionChange" />
            <el-button type="warning" size="mini" icon="el-icon-magic-stick" :loading="polishing" style="margin-top:6px;" @click="aiPolish('detail')">AI润色内容</el-button>
          </el-form-item>
          <el-form-item label="图片">
            <el-upload action="/dev_api/image/upload" :headers="uploadHeaders" name="files" list-type="picture-card" :on-success="handleUploadSuccess" :on-remove="handleRemove" :on-error="() => $message.error('图片上传失败')" :before-upload="beforeUpload" accept="image/*" :limit="9" :on-exceed="() => $message.warning('最多上传9张图片')">
              <i class="el-icon-plus"></i>
            </el-upload>
          </el-form-item>
          <el-form-item label="可见范围">
            <el-select v-model="publishForm.visibility" placeholder="请选择可见范围">
              <el-option label="所有人" value="all"></el-option>
              <el-option label="关注的人" value="following"></el-option>
              <el-option label="互相关注" value="mutual"></el-option>
              <el-option label="仅自己" value="self"></el-option>
              <el-option label="不给谁看" value="custom"></el-option>
            </el-select>
            <div v-if="publishForm.visibility === 'custom'" style="margin-top:8px;">
              <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
                <el-tag v-for="uid in publishForm.blockedUsers" :key="uid" closable size="small" @close="removeBlockedUser(uid)">
                  {{ getUserName(uid) }}
                </el-tag>
                <el-button type="primary" size="mini" icon="el-icon-plus" @click="blockDialogVisible = true">添加用户</el-button>
              </div>
            </div>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer"><el-button @click="publishDialogVisible = false">取消</el-button><el-button type="primary" @click="submitPublish" :loading="publishLoading">发布</el-button></span>
    </el-dialog>

    <el-dialog title="选择不给谁看" :visible.sync="blockDialogVisible" width="500px" append-to-body class="block-dialog">
      <div class="block-dialog-body">
        <el-input v-model="blockSearchKeyword" placeholder="搜索用户昵称" prefix-icon="el-icon-search" size="small" style="margin-bottom:12px;" @input="searchBlockUsers"></el-input>
        <div class="block-user-list">
          <div v-for="user in blockSearchResults" :key="user.userId" class="block-user-item">
            <el-avatar :size="36" :src="user.avatar">{{ (user.nickname || user.username || '用')[0] }}</el-avatar>
            <div class="block-user-info">
              <div class="block-user-name">{{ user.nickname || user.username || '用户' }}<span class="block-user-id">USR-{{ user.userId }}</span></div>
            </div>
            <el-button v-if="!isAlreadyBlocked(user.userId)" type="primary" size="mini" plain @click="addBlockedUser(user.userId)">屏蔽</el-button>
            <el-tag v-else size="mini" type="info">已屏蔽</el-tag>
          </div>
          <div v-if="blockSearchResults.length === 0" class="dialog-empty">输入昵称搜索用户</div>
        </div>
      </div>
      <span slot="footer"><el-button @click="blockDialogVisible = false">关闭</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import SearchPanel from '@/components/SearchPanel.vue'
import MentionInput from '@/components/MentionInput.vue'
export default {
  name: 'PostList',
  components: { SearchPanel, MentionInput },
  data() {
    return {
      postList: [], pageNum: 1, pageSize: 10, total: 0, searchTitle: '', searchNickname: '', searchCategoryId: null, categories: [], loading: false, viewMode: 'list', sortBy: 'time',
      publishDialogVisible: false, publishLoading: false,
      polishing: false, polishingTitle: false,
      publishForm: { title: '', detail: '', categoryId: null, images: '', visibility: 'all', blockedUsers: [], mentionUsers: '' },
      publishRules: { title: [{ required: true, message: '请输入标题', trigger: 'blur' }, { max: 30, message: '标题长度不能超过30个字符', trigger: 'blur' }], categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }], detail: [{ required: true, message: '请输入内容', trigger: 'blur' }] },
      uploadedUrls: [],
      uploadHeaders: { Authorization: sessionStorage.getItem('token') || '' },
      blockDialogVisible: false,
      blockSearchKeyword: '',
      blockSearchResults: [],
      userNamesMap: {}
    }
  },
  created() {
    this.initFromRoute()
    this.loadPosts()
    this.loadCategories()
  },
  watch: {
    '$route'(to) {
      this.initFromRoute()
      this.loadPosts()
    },
    viewMode(val) {
      if (val === 'card') {
        // 图文模式下确保 pageSize 是8的倍数
        if (this.pageSize % 8 !== 0) {
          this.pageSize = 8
          this.pageNum = 1
          this.loadPosts()
        }
      }
    }
  },
  methods: {
    initFromRoute() {
      if (this.$route.query.title) {
        this.searchTitle = this.$route.query.title
      }
      if (this.$route.query.categoryId) {
        this.searchCategoryId = this.$route.query.categoryId || null
      }
    },
    async loadCategories() {
      try {
        const res = await this.$axios.get('/category/list')
        if (res.code === 200) this.categories = (res.data || []).concat({ categoryId: 0, categoryName: '其他' })
      } catch (e) {}
    },
    async loadPosts() {
      this.loading = true
      try {
        const params = { pageNum: this.pageNum, pageSize: this.pageSize }
        if (this.searchTitle) params.title = this.searchTitle
        if (this.searchNickname) params.nickname = this.searchNickname
        if (this.searchCategoryId) params.categoryId = this.searchCategoryId
        if (this.sortBy && this.sortBy !== 'time') {
          params.sortBy = this.sortBy
          params.sortOrder = 'desc'
        }
        const res = await this.$axios.get('/post/list', { params })
        if (res.code === 200) {
          this.postList = res.data.records || []
          this.total = res.data.total || 0
        }
      } catch (e) {}
      this.loading = false
    },
    handlePageChange(val) { this.pageNum = val; this.loadPosts() },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadPosts() },
    openPublishDialog() { this.publishDialogVisible = true },
    resetPublishForm() {
      this.publishForm = { title: '', detail: '', categoryId: null, images: '', visibility: 'all', blockedUsers: [], mentionUsers: '' }
      this.uploadedUrls = []
      if (this.$refs.publishForm) this.$refs.publishForm.resetFields()
    },
    beforeUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isImage) { this.$message.error('只能上传图片文件'); return false }
      if (!isLt5M) { this.$message.error('图片大小不能超过5MB'); return false }
      return true
    },
    handleUploadSuccess(response) {
      const res = typeof response === 'string' ? JSON.parse(response) : response
      if (res.code === 200 && res.data) {
        const urls = res.data.split(',').filter(u => u.trim())
        this.uploadedUrls = this.uploadedUrls.concat(urls)
        this.publishForm.images = this.uploadedUrls.join(',')
      }
    },
    handleRemove(file) {
      const url = file.response ? (typeof file.response === 'string' ? JSON.parse(file.response).data : file.response.data) : file.url
      if (url) {
        const urls = url.split(',').filter(u => u.trim())
        urls.forEach(u => {
          const idx = this.uploadedUrls.indexOf(u.trim())
          if (idx > -1) this.uploadedUrls.splice(idx, 1)
        })
        this.publishForm.images = this.uploadedUrls.join(',')
      }
    },
    submitPublish() {
      this.$refs.publishForm.validate(async valid => {
        if (!valid) return
        try {
          const muteRes = await this.$axios.get('/punishment/checkMute')
          if (muteRes.code === 200 && muteRes.data && muteRes.data.muted) {
            this.$alert('您当前已被禁言，原因：' + (muteRes.data.reason || '无'), '禁言提示', { type: 'warning' })
            this.publishLoading = false
            return
          }
        } catch (e) {}
        this.publishLoading = true
        const res = await this.$axios.post('/post/publish', this.publishForm)
        if (res.code === 200) { this.$message.success('发布成功'); this.publishDialogVisible = false; this.loadPosts() }
        else this.$message.error(res.msg || '发布失败')
        this.publishLoading = false
      })
    },
    async loadAllUsers() {
      // 不再预加载所有用户，改用 /user/search 按需搜索
    },
    searchBlockUsers() {
      var keyword = this.blockSearchKeyword.trim()
      if (!keyword) { this.blockSearchResults = []; return }
      var self = this
      this.$axios.get('/user/search', { params: { keyword: keyword } }).then(function(res) {
        if (res.code === 200) {
          var users = res.data || []
          self.blockSearchResults = users.filter(function(u) {
            return String(u.userId) !== String(self.$store.getters.getUserInfo.userId)
          })
          // 缓存用户名
          users.forEach(function(u) { self.$set(self.userNamesMap, String(u.userId), u.nickname || u.username || u.userId) })
        }
      }).catch(function() {})
    },
    getUserName(uid) {
      return this.userNamesMap[String(uid)] || uid
    },
    isAlreadyBlocked(uid) {
      return this.publishForm.blockedUsers.some(function(id) { return String(id) === String(uid) })
    },
    addBlockedUser(uid) {
      if (!this.isAlreadyBlocked(uid)) {
        this.publishForm.blockedUsers.push(uid)
      }
    },
    removeBlockedUser(uid) {
      this.publishForm.blockedUsers = this.publishForm.blockedUsers.filter(function(id) { return String(id) !== String(uid) })
    },
    onMentionChange(userIds) {
      this.publishForm.mentionUsers = JSON.stringify(userIds)
    },
    async aiPolish(field) {
      var content = field === 'title' ? this.publishForm.title : this.publishForm.detail
      if (!content || !content.trim()) {
        this.$message.warning('请先输入内容再润色')
        return
      }
      if (field === 'title') { this.polishingTitle = true } else { this.polishing = true }
      try {
        var res = await this.$axios.post('/ai/polish', { content: content, type: 'post' })
        if (res.code === 200 && res.data && res.data.polished) {
          var polished = res.data.polished
          if (field === 'title') {
            if (polished.length > 30) {
              polished = polished.substring(0, 30)
              this.$message.warning('AI润色标题超过30字符，已自动截断')
            }
            this.publishForm.title = polished
          }
          else { this.publishForm.detail = polished }
          if (res.data.hint) { this.$message.warning(res.data.hint) }
          else { this.$message.success('润色完成') }
        } else {
          this.$message.error('润色失败')
        }
      } catch (e) {
        this.$message.error('润色请求失败')
      }
      if (field === 'title') { this.polishingTitle = false } else { this.polishing = false }
    }
  }
}
</script>

<style scoped>
.post-card-row {
  display: flex;
  flex-wrap: wrap;
}
.post-card-row .el-col {
  padding: 0 10px;
  margin-bottom: 20px;
  display: flex;
}
.post-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  display: flex;
  flex-direction: column;
  flex: 1;
}
.post-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
  border-color: rgba(64, 158, 255, 0.15);
}
.post-card-img {
  width: 100%;
  height: 170px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa, #e6e9f0);
  border-radius: 12px;
  margin: 10px 10px 0 10px;
  width: calc(100% - 20px);
  position: relative;
}
.post-card-img::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40%;
  background: linear-gradient(to top, rgba(0,0,0,0.15), transparent);
  pointer-events: none;
  border-radius: 0 0 12px 12px;
}
.post-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.post-card:hover .post-card-img img {
  transform: scale(1.08);
}
.post-card-no-img {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  min-height: 170px;
}
.post-card-no-img-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
  word-break: break-all;
  text-align: center;
}
.post-card-body {
  padding: 14px 16px;
}
.post-card-title {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  transition: color 0.3s ease;
}
.post-card:hover .post-card-title {
  color: #409EFF;
}
.post-card-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
}
.post-card-meta span {
  transition: color 0.3s ease;
}
.post-card-meta span:hover {
  color: #409EFF;
}

/* ===== Dialog Beautification ===== */
::v-deep .publish-dialog .el-dialog__body {
  padding: 0;
}
.publish-dialog-body {
  padding: 28px;
  max-height: 60vh;
  overflow-y: auto;
}
::v-deep .publish-dialog .el-upload--picture-card {
  border-radius: 12px;
  transition: all 0.3s ease;
}
::v-deep .publish-dialog .el-upload--picture-card:hover {
  border-color: #409EFF;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

::v-deep .block-dialog .el-dialog__body {
  padding: 0;
}
.block-dialog-body {
  padding: 20px 24px;
}
.block-user-list {
  max-height: 320px;
  overflow-y: auto;
}
.block-user-item {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  border-radius: 12px;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  margin-bottom: 8px;
  background: linear-gradient(135deg, #fafafa, #f5f7fa);
  border: 1px solid transparent;
}
.block-user-item:hover {
  background: linear-gradient(135deg, #f0f5ff, #e6f0ff);
  transform: translateX(6px);
  border-color: rgba(64, 158, 255, 0.1);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.08);
}
.block-user-info {
  flex: 1;
  margin-left: 14px;
  overflow: hidden;
}
.block-user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
.block-user-id {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
.dialog-empty {
  text-align: center;
  color: #a0a3a8;
  padding: 40px 0;
  font-size: 14px;
  font-weight: 500;
}
</style>
