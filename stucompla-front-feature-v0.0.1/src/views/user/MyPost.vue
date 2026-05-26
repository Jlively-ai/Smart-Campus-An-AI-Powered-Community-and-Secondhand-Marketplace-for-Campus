<!--
  组件名：MyPost
  功能描述：我的帖子页
  主要职责：
    1. 搜索/状态筛选/分类筛选/排序
    2. 编辑/删除帖子
    3. AI润色功能
-->
<template>
  <div>
    <el-card>
      <div slot="header">
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <span>我的帖子</span>
          <div style="display:flex;gap:8px;">
            <search-panel v-model="searchKeyword" module="myPost" placeholder="搜索我的帖子" size="small" input-style="width:200px;" @search="onSearch" @clear="onSearchClear"></search-panel>
            <el-button type="primary" size="small" icon="el-icon-search" @click="onSearch">搜索</el-button>
          </div>
        </div>
        <div style="display:flex;gap:8px;margin-top:10px;align-items:center;">
          <el-select v-model="filterStatus" placeholder="帖子状态" size="small" clearable style="width:140px;" @change="applyFilterSort">
            <el-option label="待审核" value="pending"></el-option>
            <el-option label="审核未通过" value="rejected"></el-option>
            <el-option label="已锁定" value="locked"></el-option>
            <el-option label="正常" value="normal"></el-option>
          </el-select>
          <el-select v-model="filterCategoryId" placeholder="帖子分类" size="small" clearable style="width:140px;" @change="applyFilterSort">
            <el-option v-for="c in categories" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId"></el-option>
          </el-select>
          <el-select v-model="sortBy" placeholder="排序方式" size="small" style="width:160px;" @change="applyFilterSort">
            <el-option label="发布时间最新" value="createTimeDesc"></el-option>
            <el-option label="发布时间最早" value="createTimeAsc"></el-option>
            <el-option label="浏览量从高到低" value="viewDesc"></el-option>
            <el-option label="评论量从高到低" value="commentDesc"></el-option>
            <el-option label="点赞数从高到低" value="likeDesc"></el-option>
            <el-option label="收藏数从高到低" value="collectDesc"></el-option>
            <el-option label="分享数从高到低" value="shareDesc"></el-option>
          </el-select>
        </div>
      </div>
      <el-table :data="postList" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200">
          <template slot-scope="scope"><el-link type="primary" @click="$router.push('/postDetail/' + scope.row.postId)">{{ scope.row.title }}</el-link></template>
        </el-table-column>
        <el-table-column prop="commentNum" label="评论" width="70" align="center"></el-table-column>
        <el-table-column prop="postStatus" label="状态" width="180" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.auditState === 0" type="warning" size="mini">待审核</el-tag>
            <el-tag v-else-if="scope.row.auditState === 2" type="danger" size="mini">审核未通过</el-tag>
            <el-tooltip v-if="scope.row.postStatus === 1 && scope.row.lockReason" :content="'锁定原因：' + scope.row.lockReason" placement="top">
              <el-tag type="danger" size="mini">已锁定</el-tag>
            </el-tooltip>
            <el-tag v-if="scope.row.postStatus === 1 && !scope.row.lockReason" type="danger" size="mini">已锁定</el-tag>
            <el-tag v-if="scope.row.auditState === 1 && scope.row.postStatus === 0" type="success" size="mini">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewNum" label="浏览量" width="70" align="center"></el-table-column>
        <el-table-column prop="likeNum" label="点赞数" width="70" align="center"></el-table-column>
        <el-table-column prop="collectNum" label="收藏数" width="70" align="center"></el-table-column>
        <el-table-column prop="shareNum" label="分享数" width="70" align="center"></el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="240">
          <template slot-scope="scope">
            {{ scope.row.createTime | formatTime }}<br v-if="scope.row.updateTime && scope.row.updateTime !== scope.row.createTime" />
            <span v-if="scope.row.updateTime && scope.row.updateTime !== scope.row.createTime" style="color:#E6A23C;font-size:12px;">（已重新编辑于 {{ scope.row.updateTime | formatTime }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="160" align="center">
          <template slot-scope="scope">
            <div style="white-space:nowrap;">
              <el-button size="mini" type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
              <el-button size="mini" type="danger" @click="deletePost(scope.row.postId)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:15px;text-align:center;" @current-change="handlePageChange" :current-page="pageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange"></el-pagination>
    </el-card>

    <el-dialog title="编辑帖子" :visible.sync="editDialogVisible" width="700px" @close="resetEditForm">
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入帖子标题"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="editForm.categoryId" placeholder="请选择分类" style="width:100%;">
            <el-option v-for="c in categories" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="detail">
          <el-input type="textarea" v-model="editForm.detail" :rows="8" placeholder="请输入帖子内容"></el-input>
          <el-button type="warning" size="mini" icon="el-icon-magic-stick" :loading="polishing" style="margin-top:6px;margin-left:80px;" @click="aiPolish">AI润色内容</el-button>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload action="/dev_api/image/upload" :headers="uploadHeaders" name="files" list-type="picture-card" :file-list="editFileList" :on-success="handleUploadSuccess" :on-remove="handleRemove" :on-error="() => $message.error('图片上传失败')" :before-upload="beforeUpload" accept="image/*" :limit="9" :on-exceed="() => $message.warning('最多上传9张图片')">
            <i class="el-icon-plus"></i>
          </el-upload>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import SearchPanel from '@/components/SearchPanel.vue'
export default {
  name: 'MyPost',
  components: { SearchPanel },
  data() {
    return {
      postList: [], pageNum: 1, pageSize: 10, total: 0, loading: false, searchKeyword: '',
      filterStatus: '', filterCategoryId: null, sortBy: 'createTimeDesc',
      rawPostList: [],
      editDialogVisible: false, editLoading: false,
      editForm: { postId: '', title: '', detail: '', categoryId: null, images: '' },
      editRules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
        detail: [{ required: true, message: '请输入内容', trigger: 'blur' }]
      },
      categories: [],
      editFileList: [],
      uploadedUrls: [],
      uploadHeaders: { Authorization: sessionStorage.getItem('token') || '' },
      polishing: false
    }
  },
  created() { this.loadPosts(); this.loadCategories() },
  methods: {
    async loadPosts() {
      this.loading = true
      const userInfo = this.$store.getters.getUserInfo
      const uid = userInfo && (userInfo.userId || userInfo.adminId)
      const params = { pageNum: this.pageNum, pageSize: this.pageSize, userId: uid, title: this.searchKeyword }
      // Pass audit state to backend if filtering
      if (this.filterStatus === 'pending') params.auditState = 0
      else if (this.filterStatus === 'rejected') params.auditState = 2
      else if (this.filterStatus === 'normal') params.auditState = 1
      // Pass category filter to backend
      if (this.filterCategoryId) params.categoryId = this.filterCategoryId
      const res = await this.$axios.get('/post/list', { params })
      if (res.code === 200) { this.rawPostList = res.data.records || []; this.total = res.data.total || 0; this.applyFilterSort() }
      this.loading = false
    },
    applyFilterSort() {
      let list = [...this.rawPostList]
      if (this.filterStatus) {
        list = list.filter(item => {
          switch (this.filterStatus) {
            case 'pending': return item.auditState === 0
            case 'rejected': return item.auditState === 2
            case 'locked': return item.postStatus === 1
            case 'normal': return item.auditState === 1 && item.postStatus === 0
            default: return true
          }
        })
      }
      if (this.filterCategoryId !== null && this.filterCategoryId !== undefined && this.filterCategoryId !== '') {
        list = list.filter(item => item.categoryId === this.filterCategoryId)
      }
      // Update total to reflect filtered count when client-side filtering is applied
      if (this.filterStatus === 'locked') {
        this.total = list.length
      }
      switch (this.sortBy) {
        case 'createTimeAsc':
          list.sort((a, b) => new Date(a.createTime) - new Date(b.createTime)); break
        case 'viewDesc':
          list.sort((a, b) => (b.viewNum || 0) - (a.viewNum || 0)); break
        case 'commentDesc':
          list.sort((a, b) => (b.commentNum || 0) - (a.commentNum || 0)); break
        case 'likeDesc':
          list.sort((a, b) => (b.likeNum || 0) - (a.likeNum || 0)); break
        case 'collectDesc':
          list.sort((a, b) => (b.collectNum || 0) - (a.collectNum || 0)); break
        case 'shareDesc':
          list.sort((a, b) => (b.shareNum || 0) - (a.shareNum || 0)); break
        default:
          list.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      }
      this.postList = list
    },
    async loadCategories() {
      const res = await this.$axios.get('/category/list')
      if (res.code === 200) this.categories = (res.data || []).concat({ categoryId: 0, categoryName: '其他' })
    },
    async deletePost(postId) {
      await this.$confirm('确定删除该帖子？删除后将移入回收站，30天内可恢复。', '提示', { type: 'warning' })
      const res = await this.$axios.delete('/post/' + postId)
      if (res.code === 200) { this.$message.success('删除成功'); this.loadPosts() }
    },
    handlePageChange(val) { this.pageNum = val; this.loadPosts() },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadPosts() },
    onSearch() { this.pageNum = 1; this.loadPosts() },
    onSearchClear() { this.searchKeyword = ''; this.pageNum = 1; this.loadPosts() },
    openEditDialog(row) {
      this.editForm = { postId: row.postId, title: row.title, detail: row.detail, categoryId: row.categoryId, images: row.images || '' }
      this.uploadedUrls = row.images ? row.images.split(',').filter(u => u.trim()) : []
      this.editFileList = this.uploadedUrls.map((url, index) => ({ name: 'image' + index, url: url }))
      this.editDialogVisible = true
    },
    resetEditForm() {
      this.editForm = { postId: '', title: '', detail: '', categoryId: null, images: '' }
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
        this.editForm.images = this.uploadedUrls.join(',')
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
        this.editForm.images = this.uploadedUrls.join(',')
      }
    },
    submitEdit() {
      this.$refs.editForm.validate(async valid => {
        if (!valid) return
        this.editLoading = true
        const res = await this.$axios.post('/post/edit', this.editForm)
        if (res.code === 200) { this.$message.success('编辑成功，等待审核'); this.editDialogVisible = false; this.loadPosts() }
        else this.$message.error(res.msg || '编辑失败')
        this.editLoading = false
      })
    },
    async aiPolish() {
      if (!this.editForm.detail || !this.editForm.detail.trim()) {
        this.$message.warning('请先输入内容再润色')
        return
      }
      this.polishing = true
      try {
        var res = await this.$axios.post('/ai/polish', { content: this.editForm.detail, type: 'post' })
        if (res.code === 200 && res.data && res.data.polished) {
          this.editForm.detail = res.data.polished
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
