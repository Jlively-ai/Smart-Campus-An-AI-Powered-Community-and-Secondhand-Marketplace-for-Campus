<!--
  组件名：WallDetailDialog
  功能描述：表白墙详情弹窗组件
  主要职责：展示表白墙详细内容，包括发布者信息、内容、图片等
-->
<template>
  <el-dialog title="表白墙详情" :visible.sync="dialogVisible" width="900px" top="5vh" append-to-body @close="onClose">
    <div v-loading="loading">
      <div v-if="wallData">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-document" style="margin-right:6px;"></i>基础信息</div>
          <table class="dt-table">
            <tr>
              <td class="dt-label">ID</td><td>{{ wallData.wallId | formatId('wall') }}</td>
              <td class="dt-label">审核状态</td><td><el-tag :type="wallData.auditState === 1 ? 'success' : wallData.auditState === 2 ? 'danger' : 'warning'" size="small">{{ wallData.auditState === 1 ? '已通过' : wallData.auditState === 2 ? '未通过' : '待审核' }}</el-tag></td>
            </tr>
            <tr>
              <td class="dt-label">发布者</td><td><template v-if="wallData.isAnonymous">匿名用户</template><el-link v-else type="primary" @click="$emit('show-user', wallData.userId)">{{ wallData.nickname || '未知' }}</el-link></td>
              <td class="dt-label">是否匿名</td><td>{{ wallData.isAnonymous ? '是' : '否' }}</td>
            </tr>
            <tr>
              <td class="dt-label">发布时间</td><td>{{ wallData.createTime | formatTime }}</td>
              <td class="dt-label">更新时间</td><td>{{ wallData.updateTime | formatTime }}</td>
            </tr>
            <tr>
              <td class="dt-label">浏览量</td><td>{{ wallData.viewNum || 0 }}</td>
              <td class="dt-label">点赞数</td><td>{{ wallData.likeNum || 0 }}</td>
            </tr>
            <tr>
              <td class="dt-label">收藏数</td><td>{{ wallData.collectNum || 0 }}</td>
              <td class="dt-label">分享数</td><td>{{ wallData.shareNum || 0 }}</td>
            </tr>
          </table>
        </el-card>
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-tickets" style="margin-right:6px;"></i>内容详情</div>
          <div style="margin-bottom:12px;"><span style="font-weight:bold;color:#606266;margin-right:8px;">内容：</span><span style="white-space:pre-wrap;line-height:1.8;">{{ wallData.wallContent || '-' }}</span></div>
          <div>
            <span style="font-weight:bold;color:#606266;margin-right:8px;">附件：</span>
            <span v-if="parseImages(wallData.wallImages).length === 0">无</span>
          </div>
          <div v-if="parseImages(wallData.wallImages).length > 0" style="margin-top:8px;">
            <el-image v-for="(img, i) in parseImages(wallData.wallImages)" :key="i" :src="img" :preview-src-list="parseImages(wallData.wallImages)" style="width:120px;height:120px;margin-right:8px;margin-bottom:8px;border-radius:8px;" fit="cover"></el-image>
          </div>
        </el-card>
        <el-card v-if="wallData.auditFailedCause" shadow="hover">
          <div slot="header" style="font-weight:bold;color:#F56C6C;"><i class="el-icon-warning" style="margin-right:6px;"></i>拒绝原因</div>
          <div style="color:#F56C6C;">{{ wallData.auditFailedCause }}</div>
        </el-card>
      </div>
      <div v-else style="text-align:center;color:#909399;padding:20px;">未找到该表白墙信息</div>
    </div>
  </el-dialog>
</template>
<script>
import { getWallList } from '@/api/manage'
export default {
  name: 'WallDetailDialog',
  props: { visible: Boolean, wallId: String },
  /** 组件数据定义 */
  data() { return { loading: false, wallData: null } },
  /** 计算属性定义 */
  computed: { dialogVisible: { get() { return this.visible }, set(val) { this.$emit('update:visible', val) } } },
  watch: { visible(val) { if (val && this.wallId) this.loadWall() }, wallId(val) { if (val && this.visible) this.loadWall() } },
  /** 组件方法定义 */
  methods: {
    async loadWall() {
      this.loading = true; this.wallData = null
      try {
        var res = await getWallList({ pageNum: 1, pageSize: 9999 })
        if (res.code === 200) { var records = res.data.records || res.data || []; this.wallData = records.find(function(w) { return w.wallId === this.wallId }.bind(this)) || null }
      } catch (e) { this.wallData = null }
      this.loading = false
    },
    parseImages(images) {
      if (!images) return []
      if (Array.isArray(images)) return images.filter(function(s) { return s && s.trim() })
      var str = String(images).trim()
      if (str === '' || str === '[]' || str === 'null') return []
      try { var parsed = JSON.parse(str); if (Array.isArray(parsed)) return parsed.filter(function(s) { return s && s.trim() }); return [parsed] } catch (e) { return str.split(',').filter(function(s) { return s && s.trim() }) }
    },
    onClose() { this.wallData = null }
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
>>> .el-card {
  border-radius: 8px;
  transition: box-shadow 0.3s ease;
}
>>> .el-card__header {
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
}
>>> .el-tag {
  border-radius: 6px;
  transition: transform 0.3s ease;
}
>>> .el-tag:hover {
  transform: translateY(-1px);
}
>>> .el-image {
  border-radius: 8px;
  transition: transform 0.3s ease;
}
>>> .el-image:hover {
  transform: scale(1.03);
}
>>> .el-link {
  transition: color 0.3s ease;
}
</style>
