<!--
  组件名：AnnouncementList
  功能描述：公告列表页
  主要职责：
    1. 公告列表展示
    2. 查看公告详情
    3. 分页
-->
<template>
  <div>
    <el-card>
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
        <span>系统公告</span>
        <div style="display:flex;align-items:center;gap:10px;">
          <el-button v-if="loginType === 'admin'" type="primary" size="small" @click="publishDialogVisible = true"><i class="el-icon-plus"></i> 发布公告</el-button>

        </div>
      </div>
      <div v-for="item in list" :key="item.announcementId" style="padding:15px 0;border-bottom:1px solid #eee;cursor:pointer;" @click="showDetail(item)">
        <div style="display:flex;align-items:center;margin-bottom:8px;">
          <el-tag v-if="item.announcementType == 1" type="danger" size="mini" style="margin-right:8px;">重要</el-tag>
          <el-tag v-else size="mini" style="margin-right:8px;">普通</el-tag>
          <span style="font-size:16px;font-weight:bold;">{{ item.title }}</span>
          <span style="margin-left:auto;color:#999;font-size:12px;">{{ formatTime(item.createTime) }}</span>
        </div>
        <div style="line-height:1.8;color:#333;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{{ item.content }}</div>
      </div>
      <el-empty v-if="list.length === 0" description="暂无公告"></el-empty>
      <el-pagination style="margin-top:20px;text-align:center;" @current-change="handlePageChange" :current-page="pageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange"></el-pagination>
    </el-card>
    <el-dialog :title="detail.title" :visible.sync="detailVisible" width="620px" class="announce-detail-dialog">
      <div class="announce-detail-body">
        <div class="announce-detail-header">
          <el-tag v-if="detail.announcementType == 1" type="danger" size="small">重要</el-tag>
          <el-tag v-else size="small">普通</el-tag>
          <span class="announce-detail-time">{{ formatTime(detail.createTime) }}</span>
          <span class="announce-detail-view">浏览量：{{ detail.viewNum || 0 }}</span>
        </div>
        <div class="announce-detail-content">{{ detail.content }}</div>
        <div v-if="parseImages(detail.images).length > 0" class="announce-detail-images">
          <el-image v-for="(img, i) in parseImages(detail.images)" :key="i" :src="img" :preview-src-list="parseImages(detail.images)" fit="cover"></el-image>
        </div>
      </div>
      <span slot="footer"><el-button type="primary" @click="detailVisible = false">关闭</el-button></span>
    </el-dialog>
    <el-dialog title="发布公告" :visible.sync="publishDialogVisible" width="620px" class="publish-announce-dialog">
      <div class="publish-announce-body">
        <el-form :model="publishForm" label-width="80px">
          <el-form-item label="标题"><el-input v-model="publishForm.title" placeholder="请输入公告标题"></el-input></el-form-item>
          <el-form-item label="类型">
            <el-select v-model="publishForm.announcementType" style="width:100%;">
              <el-option :value="1" label="普通公告"></el-option>
              <el-option :value="2" label="重要公告"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="内容"><el-input type="textarea" v-model="publishForm.content" :rows="6" placeholder="请输入公告内容"></el-input></el-form-item>
        </el-form>
      </div>
      <span slot="footer"><el-button @click="publishDialogVisible = false">取消</el-button><el-button type="primary" @click="submitPublish">发布</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AnnouncementList',
  data() { return { list: [], pageNum: 1, pageSize: 10, total: 0, detail: {}, detailVisible: false, publishDialogVisible: false, publishForm: { title: '', content: '', announcementType: 1 } } },
  computed: {
    loginType() { return this.$store.getters.getLoginType }
  },
  created() { this.loadData() },
  methods: {
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const pad = n => String(n).padStart(2, '0')
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    },
    async loadData() {
      const res = await this.$axios.get('/announcement/publicList', { params: { pageNum: this.pageNum, pageSize: this.pageSize } })
      if (res.code === 200) { this.list = res.data.records || []; this.total = res.data.total || 0 }
    },
    async showDetail(item) {
      this.detail = item
      this.detailVisible = true
      try {
        var res = await this.$axios.get('/announcement/view/' + item.announcementId)
        if (res.code === 200) {
          this.$set(this.detail, 'viewNum', res.data)
        }
      } catch(e) {}
    },
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    async submitPublish() {
      if (!this.publishForm.title || !this.publishForm.content) return this.$message.warning('请填写标题和内容')
      const res = await this.$axios.post('/announcement/publish', this.publishForm)
      if (res.code === 200) { this.$message.success('发布成功'); this.publishDialogVisible = false; this.publishForm = { title: '', content: '', announcementType: 1 }; this.loadData() }
      else this.$message.error(res.msg || '发布失败')
    },
    parseImages(images) {
      if (!images) return []
      try { const arr = JSON.parse(images); return Array.isArray(arr) ? arr : [images] }
      catch (e) { return images.split(',').filter(s => s.trim()) }
    }
  }
}
</script>

<style scoped>
/* Card beautification */
::v-deep .el-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  border: none;
}
::v-deep .el-card__header {
  font-weight: 600;
  color: #303133;
}

/* Announcement items */
.announcement-item {
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
}
.announcement-item:hover {
  background: #f5f7fa;
}
.announcement-item:last-child {
  border-bottom: none;
}

/* Announcement title */
.announcement-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* Announcement content */
.announcement-content {
  line-height: 1.8;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== Dialog Beautification ===== */
::v-deep .announce-detail-dialog .el-dialog__body,
::v-deep .publish-announce-dialog .el-dialog__body {
  padding: 0;
}
.announce-detail-body,
.publish-announce-body {
  padding: 24px;
}
.announce-detail-header {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.announce-detail-time,
.announce-detail-view {
  color: #999;
  font-size: 13px;
}
.announce-detail-content {
  line-height: 1.8;
  white-space: pre-wrap;
  font-size: 15px;
  color: #303133;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
}
.announce-detail-images {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.announce-detail-images .el-image {
  width: 120px;
  height: 120px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: transform 0.3s ease;
}
.announce-detail-images .el-image:hover {
  transform: scale(1.03);
}
</style>
