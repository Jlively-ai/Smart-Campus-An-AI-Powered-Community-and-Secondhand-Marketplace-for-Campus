<!--
  组件名：WallList
  功能描述：表白墙列表页
  主要职责：
    1. 卡片式展示表白墙内容
    2. 发布弹窗（支持匿名/可见范围/AI润色）
    3. @提及渲染
-->
<template>
  <div>
    <el-card>
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
        <span>表白墙</span>
        <div style="display:flex;align-items:center;gap:10px;">
          <el-button type="danger" size="small" icon="el-icon-edit" @click="openPublishDialog">发布</el-button>

        </div>
      </div>
      <div style="margin-bottom:15px;display:flex;gap:10px;align-items:center;flex-wrap:wrap;">
        <el-input v-model="searchNickname" placeholder="搜索作者昵称" prefix-icon="el-icon-user" clearable size="small" style="width:160px;" @clear="loadWall" @keyup.enter.native="loadWall"></el-input>
      </div>
      <el-row :gutter="20">
        <el-col :span="8" v-for="item in wallList" :key="item.wallId">
          <el-card shadow="hover" style="margin-bottom:20px;cursor:pointer;" @click.native="$router.push('/wallDetail/' + item.wallId)">
            <div style="font-size:15px;line-height:1.6;min-height:80px;" v-html="renderMentionText(item.wallContent, item.mentionUsers)"></div>
            <el-tag v-if="item.visibility === 'following'" size="mini" type="warning" style="margin-top:5px;">关注可见</el-tag>
            <el-tag v-else-if="item.visibility === 'mutual'" size="mini" type="info" style="margin-top:5px;">互关可见</el-tag>
            <el-tag v-else-if="item.visibility === 'self'" size="mini" type="danger" style="margin-top:5px;">仅自己可见</el-tag>
            <div v-if="item.wallImages" style="margin-top:10px;">
              <img v-for="(img, i) in item.wallImages.split(',')" :key="i" :src="img" style="width:100%;max-height:200px;object-fit:cover;border-radius:4px;margin-top:5px;" />
            </div>
            <div style="margin-top:10px;color:#999;font-size:12px;display:flex;justify-content:space-between;align-items:center;">
              <div>
                <span v-if="item.isAnonymous" style="color:#999;">匿名用户</span>
                <span v-else style="cursor:pointer;color:#409EFF;" @click.stop="$router.push('/userProfile/' + item.userId)">{{ item.nickname || '匿名用户' }}</span>
              </div>
              <div style="display:flex;gap:10px;">
                <span><i class="el-icon-view"></i> {{ item.viewNum || 0 }}</span>
                <span><i class="el-icon-thumb"></i> {{ item.likeNum || 0 }}</span>
                <span><i class="el-icon-star-off"></i> {{ item.collectNum || 0 }}</span>
              </div>
            </div>
            <div style="margin-top:4px;color:#c0c4cc;font-size:11px;">{{ item.createTime | formatTime }}</div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="wallList.length === 0" description="暂无表白墙内容"></el-empty>
      <el-pagination style="margin-top:20px;text-align:center;" @current-change="handlePageChange" :current-page="pageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange"></el-pagination>
    </el-card>

    <el-dialog title="发布表白墙" :visible.sync="publishDialogVisible" width="620px" @close="resetPublishForm" class="publish-dialog">
      <div class="publish-dialog-body">
        <el-form ref="publishForm" :model="publishForm" :rules="publishRules" label-width="100px">
          <el-form-item label="表白内容" prop="wallContent">
            <MentionInput v-model="publishForm.wallContent" :rows="6" placeholder="写下你想说的话..." @mention-change="onPublishMentionChange" />
            <el-button type="warning" size="mini" icon="el-icon-magic-stick" :loading="polishing" style="margin-top:6px;" @click="aiPolish">AI润色内容</el-button>
          </el-form-item>
          <el-form-item label="图片">
            <el-upload action="/dev_api/image/upload" :headers="uploadHeaders" name="files" list-type="picture-card" :on-success="handleUploadSuccess" :on-remove="handleRemove" :on-error="() => $message.error('图片上传失败')" :before-upload="beforeUpload" accept="image/*" :limit="9" :on-exceed="() => $message.warning('最多上传9张图片')">
              <i class="el-icon-plus"></i>
            </el-upload>
          </el-form-item>
          <el-form-item label="匿名发布">
            <el-switch v-model="publishForm.isAnonymous" active-text="匿名" inactive-text="实名"></el-switch>
            <span style="font-size:12px;color:#999;margin-left:10px;">匿名发布后，其他用户无法看到你的昵称和头像</span>
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
      <span slot="footer"><el-button @click="publishDialogVisible = false">取消</el-button><el-button type="primary" @click="submitPublish" :loading="publishLoading">提交申请</el-button></span>
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
import MentionInput from '@/components/MentionInput.vue'
export default {
  name: 'WallList',
  components: { MentionInput },
  data() {
    return {
      wallList: [], pageNum: 1, pageSize: 10, total: 0, searchNickname: '',
      publishDialogVisible: false, publishLoading: false,
      polishing: false,
      publishForm: { wallContent: '', wallImages: '', isAnonymous: false, visibility: 'all', blockedUsers: [], mentionUsers: '' },
      publishRules: { wallContent: [{ required: true, message: '请输入表白内容', trigger: 'blur' }] },
      uploadedUrls: [],
      uploadHeaders: { Authorization: sessionStorage.getItem('token') || '' },
      blockDialogVisible: false,
      blockSearchKeyword: '',
      blockSearchResults: [],
      userNamesMap: {},
      mentionUserMap: {}
    }
  },
  created() { this.loadWall() },
  methods: {
    async loadWall() {
      const params = { pageNum: this.pageNum, pageSize: this.pageSize }
      if (this.searchNickname) params.nickname = this.searchNickname
      const res = await this.$axios.get('/wall/wallList', { params })
      if (res.code === 200) { this.wallList = res.data.records || []; this.total = res.data.total || 0; this.loadAllMentionUsers() }
    },
    handlePageChange(val) { this.pageNum = val; this.loadWall() },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadWall() },
    openPublishDialog() { this.publishDialogVisible = true },
    resetPublishForm() {
      this.publishForm = { wallContent: '', wallImages: '', isAnonymous: false, visibility: 'all', blockedUsers: [], mentionUsers: '' }
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
        this.publishForm.wallImages = this.uploadedUrls.join(',')
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
        this.publishForm.wallImages = this.uploadedUrls.join(',')
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
        const res = await this.$axios.post('/wall/apply', this.publishForm)
        if (res.code === 200) { this.$message.success('申请已提交，等待审核'); this.publishDialogVisible = false; this.loadWall() }
        else this.$message.error(res.msg || '申请失败')
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
    onPublishMentionChange(userIds) {
      this.publishForm.mentionUsers = JSON.stringify(userIds)
    },
    async aiPolish() {
      if (!this.publishForm.wallContent || !this.publishForm.wallContent.trim()) {
        this.$message.warning('请先输入内容再润色')
        return
      }
      this.polishing = true
      try {
        var res = await this.$axios.post('/ai/polish', { content: this.publishForm.wallContent, type: 'wall' })
        if (res.code === 200 && res.data && res.data.polished) {
          this.publishForm.wallContent = res.data.polished
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
    loadAllMentionUsers() {
      var self = this
      var allIds = []
      this.wallList.forEach(function(item) {
        if (item.mentionUsers) {
          var ids = []
          try { ids = JSON.parse(item.mentionUsers) } catch (e) {}
          if (ids && ids.length > 0) allIds = allIds.concat(ids)
        }
      })
      if (allIds.length === 0) return
      // Deduplicate
      var uniqueIds = []
      allIds.forEach(function(id) {
        if (uniqueIds.indexOf(id) === -1) uniqueIds.push(id)
      })
      this.$axios.get('/user/batchInfo', { params: { ids: uniqueIds.join(',') } }).then(function(res) {
        if (res.code === 200 && res.data) {
          res.data.forEach(function(u) {
            if (u.nickname) {
              self.$set(self.mentionUserMap, u.nickname, u.userId)
            }
          })
        }
      }).catch(function() {})
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
::v-deep .el-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
}
::v-deep .el-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
}
::v-deep .el-card .el-card__header span {
  font-weight: 600;
  color: #303133;
}
.wall-card {
  border-radius: 18px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
  position: relative;
  background: linear-gradient(135deg, #fff, #fffaf5);
  border: 1px solid rgba(245, 108, 108, 0.08);
}
.wall-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 48px rgba(245, 108, 108, 0.12);
  border-color: rgba(245, 108, 108, 0.15);
}
.wall-card::before {
  content: '💕';
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 32px;
  opacity: 0.08;
  transform: rotate(15deg);
  pointer-events: none;
}
.wall-card ::v-deep .el-card__body {
  line-height: 1.8;
  word-break: break-word;
}
.wall-card img {
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.wall-card:hover img {
  transform: scale(1.03);
}
::v-deep .el-tag {
  border-radius: 8px;
  transition: all 0.3s ease;
}
::v-deep .el-avatar {
  border-radius: 50%;
  transition: all 0.3s ease;
}
::v-deep .el-avatar:hover {
  transform: scale(1.08);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
::v-deep .el-button {
  transition: all 0.3s ease;
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
