<!--
  组件名：PostDetailDialog
  功能描述：帖子详情弹窗组件
  主要职责：展示帖子详细内容，包括标题、正文、图片、评论等
-->
<template>
  <el-dialog title="帖子详情" :visible.sync="dialogVisible" width="900px" top="5vh" append-to-body @close="onClose">
    <div v-loading="loading">
      <div v-if="postData">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-document" style="margin-right:6px;"></i>基础信息</div>
          <table class="dt-table">
            <tr>
              <td class="dt-label">帖子ID</td><td>{{ postData.postId | formatId('post') }}</td>
              <td class="dt-label">状态</td><td><el-tag :type="postData.postStatus === 0 ? 'success' : 'danger'" size="small">{{ postData.postStatus === 0 ? '正常' : '锁定' }}</el-tag></td>
            </tr>
            <tr>
              <td class="dt-label">作者</td><td><el-link type="primary" @click="$emit('show-user', postData.userId)">{{ postData.nickname || '匿名' }}</el-link></td>
              <td class="dt-label">作者ID</td><td>{{ postData.userId | formatId('user') }}</td>
            </tr>
            <tr>
              <td class="dt-label">评论数</td><td>{{ postData.commentNum || 0 }}</td>
              <td class="dt-label">浏览量</td><td>{{ postData.viewNum || 0 }}</td>
            </tr>
            <tr>
              <td class="dt-label">点赞数</td><td>{{ postData.likeNum || 0 }}</td>
              <td class="dt-label">收藏数</td><td>{{ postData.collectNum || 0 }}</td>
            </tr>
            <tr>
              <td class="dt-label">分享数</td><td>{{ postData.shareNum || 0 }}</td>
              <td class="dt-label">分类</td><td>{{ postData.categoryName || '未分类' }}</td>
            </tr>
            <tr>
              <td class="dt-label">发布时间</td><td>{{ postData.createTime | formatTime }}</td>
              <td class="dt-label">更新时间</td><td>{{ postData.updateTime | formatTime }}</td>
            </tr>
          </table>
        </el-card>
        <el-card shadow="hover">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-tickets" style="margin-right:6px;"></i>内容详情</div>
          <div style="margin-bottom:12px;"><span style="font-weight:bold;color:#606266;margin-right:8px;">标题：</span>{{ postData.title || '-' }}</div>
          <div style="margin-bottom:12px;"><span style="font-weight:bold;color:#606266;margin-right:8px;">内容：</span><span style="white-space:pre-wrap;line-height:1.8;">{{ postData.detail || '-' }}</span></div>
          <div>
            <span style="font-weight:bold;color:#606266;margin-right:8px;">附件：</span>
            <span v-if="parseImages(postData.images).length === 0">无</span>
          </div>
          <div v-if="parseImages(postData.images).length > 0" style="margin-top:8px;">
            <el-image v-for="(img, i) in parseImages(postData.images)" :key="i" :src="img" :preview-src-list="parseImages(postData.images)" style="width:120px;height:120px;margin-right:8px;margin-bottom:8px;border-radius:8px;" fit="cover"></el-image>
          </div>
        </el-card>
      </div>
      <div v-else style="text-align:center;color:#909399;padding:20px;">未找到该帖子信息</div>
    </div>
  </el-dialog>
</template>
<script>
import { getPostList } from '@/api/manage'
export default {
  name: 'PostDetailDialog',
  props: { visible: Boolean, postId: String },
  /** 组件数据定义 */
  data() { return { loading: false, postData: null } },
  /** 计算属性定义 */
  computed: { dialogVisible: { get() { return this.visible }, set(val) { this.$emit('update:visible', val) } } },
  watch: { visible(val) { if (val && this.postId) this.loadPost() }, postId(val) { if (val && this.visible) this.loadPost() } },
  /** 组件方法定义 */
  methods: {
    async loadPost() {
      this.loading = true; this.postData = null
      try {
        var res = await getPostList({ pageNum: 1, pageSize: 9999 })
        if (res.code === 200) { var records = res.data.records || res.data || []; this.postData = records.find(function(p) { return p.postId === this.postId }.bind(this)) || null }
      } catch (e) { this.postData = null }
      this.loading = false
    },
    parseImages(images) {
      if (!images) return []
      if (Array.isArray(images)) return images.filter(function(s) { return s && s.trim() })
      var str = String(images).trim()
      if (str === '' || str === '[]' || str === 'null') return []
      try { var parsed = JSON.parse(str); if (Array.isArray(parsed)) return parsed.filter(function(s) { return s && s.trim() }); return [parsed] } catch (e) { return str.split(',').filter(function(s) { return s && s.trim() }) }
    },
    onClose() { this.postData = null }
  }
}
</script>
<style scoped>
/* 组件局部样式 */
.dt-table {
  width: 100%;
  border-collapse: collapse;
  border-color: #ebeef5;
}
.dt-table td {
  padding: 12px 16px;
  border: 1px solid #ebeef5;
  font-size: 13px;
  color: #303133;
}
.dt-label {
  background: #fafafa;
  font-weight: 600;
  width: 100px;
  text-align: right;
  color: #303133;
  white-space: nowrap;
}
.dt-table tr {
  transition: background-color 0.3s ease;
}
.dt-table tr:hover td {
  background-color: #f5f7fa;
}
::v-deep .el-card {
  border-radius: 8px;
  transition: box-shadow 0.3s ease;
}
::v-deep .el-card__header {
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
}
::v-deep .el-tag {
  border-radius: 6px;
  transition: transform 0.3s ease;
}
::v-deep .el-tag:hover {
  transform: translateY(-1px);
}
::v-deep .el-image {
  border-radius: 8px;
  transition: transform 0.3s ease;
}
::v-deep .el-image:hover {
  transform: scale(1.03);
}
::v-deep .el-link {
  transition: color 0.3s ease;
}
</style>
