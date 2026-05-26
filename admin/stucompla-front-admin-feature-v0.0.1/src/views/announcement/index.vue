<!--
  组件名：AnnouncementIndex
  功能描述：公告管理页面
  主要职责：展示公告列表，支持新增、编辑、删除公告等操作
-->
<template>
  <div>
    <!-- 搜索筛选区域 -->
   
    <el-card shadow="never" class="search-card">
      <div class="search-bar">
        <el-input v-model="searchForm.title" placeholder="搜索公告标题" prefix-icon="el-icon-search" class="search-input" size="small" clearable @clear="loadData" @keyup.enter.native="loadData"></el-input>
        <el-select v-model="searchForm.status" placeholder="筛选状态" class="search-select" size="small" clearable @change="loadData">
          <el-option label="已发布" :value="1"></el-option>
          <el-option label="已下架" :value="0"></el-option>
        </el-select>
        <el-select v-model="sortField" placeholder="排序" size="small" style="width:120px;" clearable @change="handleSortChange">
          <el-option label="浏览量 ↓" value="viewNum_desc" />
          <el-option label="浏览量 ↑" value="viewNum_asc" />
          <el-option label="发布时间 ↓" value="createTime_desc" />
          <el-option label="发布时间 ↑" value="createTime_asc" />
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
        <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        <el-button type="success" size="small" icon="el-icon-plus" @click="openAddDialog">发布公告</el-button>
        <el-button type="success" size="small" icon="el-icon-download" @click="exportData">导出</el-button>
      </div>
    </el-card>
    <!-- 操作工具栏 -->
   

    <div class="toolbar">
      <el-button type="danger" size="small" icon="el-icon-delete" :disabled="selectedRows.length === 0" @click="batchDelete">批量删除</el-button>
    </div>
    <!-- 数据表格 -->
   

    <el-table :data="list" v-loading="loading" border @selection-change="handleSelectionChange" class="data-table">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="announcementId" label="ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.announcementId | formatId('announcement') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip></el-table-column>
      <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip></el-table-column>
      <el-table-column prop="announcementType" label="类型" width="80" align="center">
        <template slot-scope="scope"><el-tag :type="scope.row.announcementType === 1 ? 'danger' : ''">{{ scope.row.announcementType === 1 ? '重要' : '普通' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template slot-scope="scope"><el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '已发布' : '已下架' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="viewNum" label="浏览量" width="80" align="center">
        <template slot-scope="scope">{{ scope.row.viewNum || 0 }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="170">
        <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
      </el-table-column>
      <el-table-column label="操作" width="250" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" :type="scope.row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(scope.row)">{{ scope.row.status === 1 ? '下架' : '上架' }}</el-button>
          <el-button size="mini" type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.row.announcementId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
   
    <el-pagination class="pagination-bar" @current-change="handlePageChange" :current-page="pageNum" :page-size.sync="pageSize" :page-sizes="[10, 20, 50]" @size-change="handleSizeChange" :total="total" layout="total, sizes, prev, pager, next"></el-pagination>

    
    <el-dialog :title="isEdit ? '编辑公告' : '发布公告'" :visible.sync="addDialogVisible" width="550px" class="announce-dialog">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="addForm.title" placeholder="请输入公告标题"></el-input></el-form-item>
        <el-form-item label="内容"><el-input type="textarea" v-model="addForm.content" :rows="4" placeholder="请输入公告内容"></el-input></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="addForm.announcementType" style="width:100%;">
            <el-option label="普通" :value="0"></el-option>
            <el-option label="重要" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload action="/dev_api/image/upload" :headers="uploadHeaders" name="files" list-type="picture-card" :file-list="fileList" :on-success="handleUploadSuccess" :on-remove="handleUploadRemove" :on-error="function() { $message.error('图片上传失败') }" :before-upload="beforeUpload" accept="image/*" :limit="9" :on-exceed="function() { $message.warning('最多上传9张图片') }">
            <i class="el-icon-plus"></i>
          </el-upload>
        </el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="addDialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">{{ isEdit ? '保存' : '发布' }}</el-button></span>
    </el-dialog>

    
    <el-dialog title="公告详情" :visible.sync="detailVisible" width="700px" top="5vh" class="detail-dialog">
      <div v-if="detailData">
        <el-card shadow="hover" class="info-card">
          <div slot="header" class="info-header"><i class="el-icon-bell"></i>基础信息</div>
          <table class="dt-table">
            <tr>
              <td class="dt-label">公告ID</td><td>{{ detailData.announcementId | formatId('announcement') }}</td>
              <td class="dt-label">类型</td><td><el-tag :type="detailData.announcementType === 1 ? 'danger' : ''" size="small">{{ detailData.announcementType === 1 ? '重要' : '普通' }}</el-tag></td>
            </tr>
            <tr>
              <td class="dt-label">状态</td><td><el-tag :type="detailData.status === 1 ? 'success' : 'info'" size="small">{{ detailData.status === 1 ? '已发布' : '已下架' }}</el-tag></td>
              <td class="dt-label">发布时间</td><td>{{ detailData.createTime | formatTime }}</td>
            </tr>
            <tr>
              <td class="dt-label">浏览量</td><td>{{ detailData.viewNum || 0 }}</td>
              <td class="dt-label"></td><td></td>
            </tr>
          </table>
        </el-card>
        <el-card shadow="hover" class="info-card">
          <div slot="header" class="info-header"><i class="el-icon-tickets"></i>内容详情</div>
          <div class="detail-content-row"><span class="detail-label">标题：</span>{{ detailData.title }}</div>
          <div class="detail-content-row"><span class="detail-label">内容：</span><span class="detail-text">{{ detailData.content }}</span></div>
          <div class="detail-content-row">
            <span class="detail-label">附件：</span>
            <span v-if="parseImages(detailData.images).length === 0">无</span>
          </div>
          <div v-if="parseImages(detailData.images).length > 0" class="detail-images">
            <el-image v-for="(img, i) in parseImages(detailData.images)" :key="i" :src="img" :preview-src-list="parseImages(detailData.images)" class="detail-img" fit="cover"></el-image>
          </div>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getAnnouncementList, publishAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/manage'
export default {
  /** 组件数据定义 */
  data() {
    return {
      list: [], pageNum: 1, pageSize: 10, total: 0, loading: false, selectedRows: [],
      searchForm: { title: '', status: null },
      sortField: '',
      addDialogVisible: false, isEdit: false, editId: '',
      addForm: { title: '', content: '', announcementType: 0, images: '' },
      fileList: [], uploadedUrls: [],
      uploadHeaders: { Authorization: sessionStorage.getItem('admin_token') || '' },
      detailVisible: false, detailData: {}
    }
  },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadData() },
  /** 组件方法定义 */
  methods: {
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      const res = await getAnnouncementList({ pageNum: this.pageNum, pageSize: this.pageSize, ...this.searchForm })
      if (res.code === 200) { this.list = res.data.records || []; this.total = res.data.total || 0 }
      this.loading = false
    },
    openAddDialog() {
      this.isEdit = false; this.editId = ''
      this.addForm = { title: '', content: '', announcementType: 0, images: '' }
      this.fileList = []; this.uploadedUrls = []
      this.addDialogVisible = true
    },
    openEditDialog(row) {
      this.isEdit = true; this.editId = row.announcementId
      this.addForm = { title: row.title, content: row.content, announcementType: row.announcementType, images: row.images || '' }
      this.uploadedUrls = row.images ? row.images.split(',').filter(function(s) { return s.trim() }) : []
      this.fileList = this.uploadedUrls.map(function(url) { return { name: url.split('/').pop(), url: url } })
      this.addDialogVisible = true
    },
    async handleSubmit() {
      if (!this.addForm.title) return this.$message.warning('请输入标题')
      if (!this.addForm.content) return this.$message.warning('请输入内容')
      this.addForm.images = this.uploadedUrls.join(',')
      if (this.isEdit) {
        await updateAnnouncement({ announcementId: this.editId, title: this.addForm.title, content: this.addForm.content, announcementType: this.addForm.announcementType, images: this.addForm.images })
        this.$message.success('保存成功')
      } else {
        await publishAnnouncement(this.addForm)
        this.$message.success('发布成功')
      }
      this.addDialogVisible = false; this.loadData()
    },
    beforeUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isImage) { this.$message.error('只能上传图片'); return false }
      if (!isLt5M) { this.$message.error('图片不能超过5MB'); return false }
      return true
    },
    handleUploadSuccess(response, file, fileList) {
      var res = typeof response === 'string' ? JSON.parse(response) : response
      if (res.code === 200 && res.data) {
        var urls = res.data.split(',').filter(function(u) { return u.trim() })
        this.uploadedUrls = this.uploadedUrls.concat(urls)
        
        this.fileList = fileList.map(function(f) {
          if (f.response) {
            var r = typeof f.response === 'string' ? JSON.parse(f.response) : f.response
            if (r.code === 200 && r.data) {
              var firstUrl = r.data.split(',')[0]
              return { name: f.name, url: firstUrl }
            }
          }
          return f
        })
      } else {
        
        this.fileList = fileList.filter(function(f) { return f.uid !== file.uid })
        this.$message.error('图片上传失败')
      }
    },
    handleUploadRemove(file) {
      const url = file.response ? (typeof file.response === 'string' ? JSON.parse(file.response).data : file.response.data) : file.url
      if (url) {
        const urls = url.split(',').filter(function(u) { return u.trim() })
        var self = this
        urls.forEach(function(u) {
          const idx = self.uploadedUrls.indexOf(u.trim())
          if (idx > -1) self.uploadedUrls.splice(idx, 1)
        })
      }
    },
    parseImages(images) {
      if (!images) return []
      try { const arr = JSON.parse(images); return Array.isArray(arr) ? arr : [images] } catch (e) { return images.split(',').filter(function(s) { return s.trim() }) }
    },
    async toggleStatus(row) {
      await updateAnnouncement({ announcementId: row.announcementId, status: row.status === 1 ? 0 : 1 })
      this.$message.success('操作成功'); this.loadData()
    },
    /** 删除记录 */
    async handleDelete(announcementId) {
      try { await this.$confirm('确定删除？', '提示', { type: 'warning' }) } catch { return }
      await deleteAnnouncement(announcementId); this.$message.success('删除成功'); this.loadData()
    },
    /** 表格选中行变化回调 */
    handleSelectionChange(val) { this.selectedRows = val },
    handleSortChange(val) {
      if (!val) return
      var parts = val.split('_')
      var field = parts[0]
      var order = parts[1]
      this.list.sort(function(a, b) {
        var va = a[field], vb = b[field]
        if (!va && !vb) return 0
        if (!va) return 1
        if (!vb) return -1
        if (field === 'viewNum' || typeof va === 'number') {
          return order === 'asc' ? va - vb : vb - va
        }
        var ta = new Date(va).getTime(), tb = new Date(vb).getTime()
        return order === 'asc' ? ta - tb : tb - ta
      })
    },
    /** 批量删除选中记录 */
    async batchDelete() {
      if (this.selectedRows.length === 0) return
      try { await this.$confirm('确定批量删除选中的公告？', '提示', { type: 'warning' }) } catch { return }
      for (var i = 0; i < this.selectedRows.length; i++) { await deleteAnnouncement(this.selectedRows[i].announcementId) }
      this.$message.success('批量删除成功'); this.selectedRows = []; this.loadData()
    },
    /** 分页页码变化回调 */
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    /** 每页条数变化回调 */
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    /** 显示详情弹窗 */
    showDetail(row) { this.detailData = row; this.detailVisible = true },
    /** 重置搜索条件并重新加载数据 */
    resetSearch() { this.searchForm = { title: '', status: null }; this.loadData() },
    /** 导出数据为CSV文件 */
    exportData() {
      if (this.list.length === 0) { this.$message.warning('没有可导出的数据'); return }
      const columns = [
        { prop: 'announcementId', label: '公告ID' }, { prop: 'title', label: '标题' },
        { prop: 'content', label: '内容' }, { prop: 'announcementType', label: '类型' },
        { prop: 'status', label: '状态' }, { prop: 'createTime', label: '发布时间' }
      ]
      const headers = columns.map(c => c.label).join(',')
      const rows = this.list.map(row => columns.map(c => {
        const val = row[c.prop]
        if (c.prop === 'announcementType') return val === 1 ? '重要' : '普通'
        if (c.prop === 'status') return val === 1 ? '已发布' : '已下架'
        return val != null ? String(val) : ''
      }).join(',')).join('\n')
      const csv = '\uFEFF' + headers + '\n' + rows
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob); link.download = '公告数据.csv'; link.click()
      URL.revokeObjectURL(link.href); this.$message.success('导出成功')
    }
  }
}
</script>
<style scoped>
/* 组件局部样式 */
.search-card {
  margin-bottom: 16px;
  border-radius: 16px;
}
.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}
.search-input {
  width: 220px;
}
.search-select {
  width: 160px;
}
.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
.data-table {
  border-radius: 16px;
  overflow: hidden;
}
.pagination-bar {
  margin-top: 16px;
  padding: 8px 0;
}
.info-card {
  margin-bottom: 20px;
  border-radius: 16px;
}
.info-header {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #374151;
}
.dt-table { width: 100%; border-collapse: collapse; border-color: #ebeef5; }
.dt-table td { padding: 10px 12px; border: 1px solid #ebeef5; font-size: 13px; color: #303133; }
.dt-label { background: #f8fafc; font-weight: 600; width: 100px; text-align: right; color: #4b5563; white-space: nowrap; }
.detail-content-row {
  margin-bottom: 12px;
  line-height: 1.8;
}
.detail-label {
  font-weight: 600;
  color: #606266;
  margin-right: 8px;
}
.detail-text {
  white-space: pre-wrap;
  color: #4b5563;
}
.detail-images {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.detail-img {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  transition: transform 0.3s ease;
}
.detail-img:hover {
  transform: scale(1.02);
}
</style>
