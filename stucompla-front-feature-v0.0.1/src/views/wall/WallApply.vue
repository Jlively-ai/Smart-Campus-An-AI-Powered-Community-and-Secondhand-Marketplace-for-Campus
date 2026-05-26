<!--
  组件名：WallApply
  功能描述：申请上墙页
  主要职责：
    1. 上墙申请表单（内容/图片/可见范围）
    2. AI润色功能
    3. @提及输入
-->
<template>
  <div>
    <el-card>
      <div slot="header"><span>申请上墙</span></div>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="表白内容" prop="wallContent">
          <MentionInput v-model="form.wallContent" :rows="6" placeholder="写下你想说的话..." @mention-change="onMentionChange" />
          <el-button type="warning" size="mini" icon="el-icon-magic-stick" :loading="polishing" style="margin-top:6px;" @click="aiPolish">AI润色内容</el-button>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload action="/dev_api/image/upload" :headers="uploadHeaders" name="files" list-type="picture-card" :file-list="fileList" :on-success="handleUploadSuccess" :on-remove="handleRemove" :on-error="() => $message.error('图片上传失败')" :before-upload="beforeUpload" accept="image/*" :limit="9" :on-exceed="() => $message.warning('最多上传9张图片')">
            <i class="el-icon-plus"></i>
          </el-upload>
        </el-form-item>
        <el-form-item label="可见范围">
          <el-select v-model="form.visibility" placeholder="请选择可见范围">
            <el-option label="所有人" value="all"></el-option>
            <el-option label="关注的人" value="following"></el-option>
            <el-option label="互相关注" value="mutual"></el-option>
            <el-option label="仅自己" value="self"></el-option>
            <el-option label="不给谁看" value="custom"></el-option>
          </el-select>
          <div v-if="form.visibility === 'custom'" style="margin-top:8px;">
            <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
              <el-tag v-for="uid in form.blockedUsers" :key="uid" closable size="small" @close="removeBlockedUser(uid)">
                {{ getUserName(uid) }}
              </el-tag>
              <el-button type="primary" size="mini" icon="el-icon-plus" @click="blockDialogVisible = true">添加用户</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit" :loading="loading">提交申请</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

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
  name: 'WallApply',
  components: { MentionInput },
  data() {
    return {
      form: { wallContent: '', wallImages: '', visibility: 'all', blockedUsers: [], mentionUsers: '' },
      rules: { wallContent: [{ required: true, message: '请输入表白内容', trigger: 'blur' }] },
      loading: false, polishing: false,
      fileList: [], uploadedUrls: [],
      uploadHeaders: { Authorization: sessionStorage.getItem('token') || '' },
      blockDialogVisible: false,
      blockSearchKeyword: '',
      blockSearchResults: [],
      userNamesMap: {}
    }
  },
  created() {},
  methods: {
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
        this.form.wallImages = this.uploadedUrls.join(',')
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
        this.form.wallImages = this.uploadedUrls.join(',')
      }
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          const res = await this.$axios.post('/wall/apply', this.form)
          if (res.code === 200) { this.$message.success('申请已提交，等待审核'); this.$router.push('/wallList') }
          else this.$message.error(res.msg || '申请失败')
        } catch (e) { this.$message.error('申请失败') }
        this.loading = false
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
      return this.form.blockedUsers.some(function(id) { return String(id) === String(uid) })
    },
    addBlockedUser(uid) {
      if (!this.isAlreadyBlocked(uid)) {
        this.form.blockedUsers.push(uid)
      }
    },
    removeBlockedUser(uid) {
      this.form.blockedUsers = this.form.blockedUsers.filter(function(id) { return String(id) !== String(uid) })
    },
    onMentionChange(userIds) {
      this.form.mentionUsers = JSON.stringify(userIds)
    },
    async aiPolish() {
      if (!this.form.wallContent || !this.form.wallContent.trim()) {
        this.$message.warning('请先输入内容再润色')
        return
      }
      this.polishing = true
      try {
        var res = await this.$axios.post('/ai/polish', { content: this.form.wallContent, type: 'wall' })
        if (res.code === 200 && res.data && res.data.polished) {
          this.form.wallContent = res.data.polished
          if (res.data.hint) { this.$message.warning(res.data.hint) }
          else { this.$message.success('润色完成') }
        } else {
          this.$message.error('润色失败')
        }
      } catch (e) {
        this.$message.error('润色请求失败')
      }
      this.polishing = false
    }
  }
}
</script>

<style scoped>
::v-deep .el-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s ease;
}
::v-deep .el-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}
::v-deep .el-card .el-card__header {
  font-weight: 600;
  color: #303133;
}
::v-deep .el-tag {
  border-radius: 6px;
  transition: all 0.3s ease;
}
::v-deep .el-avatar {
  border-radius: 50%;
  transition: transform 0.3s ease;
}
::v-deep .el-avatar:hover {
  transform: scale(1.05);
}
::v-deep .el-button {
  transition: all 0.3s ease;
}
::v-deep .el-upload--picture-card {
  border-radius: 10px;
  overflow: hidden;
  transition: all 0.3s ease;
}
::v-deep .el-upload--picture-card:hover {
  border-color: #409EFF;
}
</style>
