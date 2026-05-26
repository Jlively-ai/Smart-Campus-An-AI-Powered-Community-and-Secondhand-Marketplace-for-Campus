<!--
  组件名：MyWall
  功能描述：我的表白墙页
  主要职责：
    1. 搜索/审核状态筛选/排序
    2. 编辑/删除表白墙
    3. 可见范围设置
    4. AI润色功能
-->
<template>
  <div>
    <el-card>
      <div slot="header">
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <span>我的表白墙</span>
          <div style="display:flex;gap:8px;">
            <search-panel v-model="searchKeyword" module="myWall" placeholder="搜索我的表白墙" size="small" input-style="width:200px;" @search="onSearch" @clear="onSearchClear"></search-panel>
            <el-button type="primary" size="small" icon="el-icon-search" @click="onSearch">搜索</el-button>
          </div>
        </div>
        <div style="display:flex;gap:8px;margin-top:10px;align-items:center;">
          <el-select v-model="filterAuditState" placeholder="审核状态" size="small" clearable style="width:130px;" @change="applyFilterSort">
            <el-option label="待审核" :value="0"></el-option>
            <el-option label="已通过" :value="1"></el-option>
            <el-option label="未通过" :value="2"></el-option>
          </el-select>
          <el-select v-model="sortBy" placeholder="排序方式" size="small" style="width:150px;" @change="applyFilterSort">
            <el-option label="申请时间最新" value="createTimeDesc"></el-option>
            <el-option label="申请时间最早" value="createTimeAsc"></el-option>
            <el-option label="浏览量从高到低" value="viewDesc"></el-option>
            <el-option label="点赞数从高到低" value="likeDesc"></el-option>
            <el-option label="收藏数从高到低" value="collectDesc"></el-option>
            <el-option label="分享数从高到低" value="shareDesc"></el-option>
          </el-select>
        </div>
      </div>
      <el-table :data="wallList" v-loading="loading">
        <el-table-column prop="wallContent" label="内容" min-width="250">
          <template slot-scope="scope"><el-link :underline="false" @click="$router.push('/wallDetail/' + scope.row.wallId)" style="color:#303133;">{{ scope.row.wallContent }}</el-link></template>
        </el-table-column>
        <el-table-column prop="auditState" label="审核状态" width="160" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.auditState === 1 ? 'success' : scope.row.auditState === 2 ? 'danger' : 'warning'" size="mini">
              {{ scope.row.auditState === 1 ? '已通过' : scope.row.auditState === 2 ? '未通过' : '待审核' }}
            </el-tag>
            <el-tag v-if="scope.row.locked === 1" type="danger" size="mini" style="margin-left:4px;">已锁定</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewNum" label="浏览量" width="70" align="center"></el-table-column>
        <el-table-column prop="likeNum" label="点赞数" width="70" align="center"></el-table-column>
        <el-table-column prop="collectNum" label="收藏数" width="70" align="center"></el-table-column>
        <el-table-column prop="shareNum" label="分享数" width="70" align="center"></el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="240">
          <template slot-scope="scope">
            {{ scope.row.createTime | formatTime }}<br v-if="scope.row.updateTime && scope.row.updateTime !== scope.row.createTime" />
            <span v-if="scope.row.updateTime && scope.row.updateTime !== scope.row.createTime" style="color:#E6A23C;font-size:12px;">（已重新编辑于 {{ scope.row.updateTime | formatTime }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150" align="center">
          <template slot-scope="scope">
            <div style="white-space:nowrap;">
              <el-button size="mini" type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
              <el-button size="mini" type="danger" @click="deleteWall(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:15px;text-align:center;" @current-change="handlePageChange" :current-page="pageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange"></el-pagination>
    </el-card>

    <el-dialog title="编辑表白墙" :visible.sync="editDialogVisible" width="600px" @close="resetEditForm">
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="内容" prop="wallContent">
          <el-input type="textarea" v-model="editForm.wallContent" :rows="6" placeholder="写下你想说的话..."></el-input>
          <el-button type="warning" size="mini" icon="el-icon-magic-stick" :loading="polishing" style="margin-top:6px;" @click="aiPolish">AI润色内容</el-button>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload action="/dev_api/image/upload" :headers="uploadHeaders" name="files" list-type="picture-card" :file-list="editFileList" :on-success="handleUploadSuccess" :on-remove="handleRemove" :on-error="() => $message.error('图片上传失败')" :before-upload="beforeUpload" accept="image/*" :limit="9" :on-exceed="() => $message.warning('最多上传9张图片')">
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
import SearchPanel from '@/components/SearchPanel.vue'
export default {
  name: 'MyWall',
  components: { SearchPanel },
  data() {
    return {
      wallList: [], pageNum: 1, pageSize: 10, total: 0, loading: false, searchKeyword: '',
      filterAuditState: '', sortBy: 'createTimeDesc',
      rawWallList: [],
      editDialogVisible: false, editLoading: false,
      editForm: { wallId: '', wallContent: '', wallImages: '', visibility: 'all', blockedUsers: [] },
      editRules: {
        wallContent: [{ required: true, message: '请输入内容', trigger: 'blur' }]
      },
      editFileList: [],
      uploadedUrls: [],
      uploadHeaders: { Authorization: sessionStorage.getItem('token') || '' },
      blockDialogVisible: false,
      blockSearchKeyword: '',
      blockSearchResults: [],
      userNamesMap: {},
      polishing: false
    }
  },
  created() { this.loadWall() },
  methods: {
    async loadWall() {
      this.loading = true
      const params = { pageNum: this.pageNum, pageSize: this.pageSize, wallContent: this.searchKeyword }
      // Pass audit state to backend if filtering
      if (this.filterAuditState !== '' && this.filterAuditState !== null && this.filterAuditState !== undefined) {
        params.auditState = this.filterAuditState
      }
      const res = await this.$axios.get('/wall/myWallList', { params })
      if (res.code === 200) { this.rawWallList = res.data.records || []; this.total = res.data.total || 0; this.applyFilterSort() }
      this.loading = false
    },
    applyFilterSort() {
      let list = [...this.rawWallList]
      if (this.filterAuditState !== '' && this.filterAuditState !== null && this.filterAuditState !== undefined) {
        list = list.filter(item => item.auditState === this.filterAuditState)
      }
      switch (this.sortBy) {
        case 'createTimeAsc':
          list.sort((a, b) => new Date(a.createTime) - new Date(b.createTime)); break
        case 'viewDesc':
          list.sort((a, b) => (b.viewNum || 0) - (a.viewNum || 0)); break
        case 'likeDesc':
          list.sort((a, b) => (b.likeNum || 0) - (a.likeNum || 0)); break
        case 'collectDesc':
          list.sort((a, b) => (b.collectNum || 0) - (a.collectNum || 0)); break
        case 'shareDesc':
          list.sort((a, b) => (b.shareNum || 0) - (a.shareNum || 0)); break
        default:
          list.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      }
      this.wallList = list
    },
    handlePageChange(val) { this.pageNum = val; this.loadWall() },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadWall() },
    onSearch() { this.pageNum = 1; this.loadWall() },
    onSearchClear() { this.searchKeyword = ''; this.pageNum = 1; this.loadWall() },
    async deleteWall(row) {
      try { await this.$confirm('确定删除该表白墙？删除后将移入回收站，30天内可恢复。', '提示', { type: 'warning' }) } catch(e) { return }
      var res = await this.$axios.delete('/wall/deleteMyWall/' + row.wallId)
      if (res.code === 200) { this.$message.success('已移入回收站'); this.loadWall() }
      else this.$message.error(res.msg || '删除失败')
    },
    openEditDialog(row) {
      this.editForm = { wallId: row.wallId, wallContent: row.wallContent, wallImages: row.wallImages || '', visibility: row.visibility || 'all', blockedUsers: row.blockedUsers ? JSON.parse(row.blockedUsers) : [] }
      this.uploadedUrls = row.wallImages ? row.wallImages.split(',').filter(u => u.trim()) : []
      this.editFileList = this.uploadedUrls.map((url, index) => ({ name: 'image' + index, url: url }))
      this.editDialogVisible = true
    },
    resetEditForm() {
      this.editForm = { wallId: '', wallContent: '', wallImages: '', visibility: 'all', blockedUsers: [] }
      this.editFileList = []
      this.uploadedUrls = []
      if (this.$refs.editForm) this.$refs.editForm.resetFields()
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
        this.editForm.wallImages = this.uploadedUrls.join(',')
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
        this.editForm.wallImages = this.uploadedUrls.join(',')
      }
    },
    submitEdit() {
      this.$refs.editForm.validate(async valid => {
        if (!valid) return
        this.editLoading = true
        const res = await this.$axios.post('/wall/edit', this.editForm)
        if (res.code === 200) { this.$message.success('编辑成功，等待审核'); this.editDialogVisible = false; this.loadWall() }
        else this.$message.error(res.msg || '编辑失败')
        this.editLoading = false
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
          var myInfo = self.$store.getters.getUserInfo
          var myId = myInfo && (myInfo.userId || myInfo.adminId)
          self.blockSearchResults = users.filter(function(u) {
            return String(u.userId) !== String(myId)
          })
          users.forEach(function(u) { self.$set(self.userNamesMap, String(u.userId), u.nickname || u.username || u.userId) })
        }
      }).catch(function() {})
    },
    getUserName(uid) {
      return this.userNamesMap[String(uid)] || uid
    },
    isAlreadyBlocked(uid) {
      return this.editForm.blockedUsers.some(function(id) { return String(id) === String(uid) })
    },
    addBlockedUser(uid) {
      if (!this.isAlreadyBlocked(uid)) {
        this.editForm.blockedUsers.push(uid)
      }
    },
    removeBlockedUser(uid) {
      this.editForm.blockedUsers = this.editForm.blockedUsers.filter(function(id) { return String(id) !== String(uid) })
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
    }
  }
}
</script>

<style scoped>
</style>
