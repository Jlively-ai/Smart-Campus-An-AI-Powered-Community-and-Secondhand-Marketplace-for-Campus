<!--
  组件名：ReportIndex
  功能描述：举报管理页面
  主要职责：展示举报列表，支持搜索、处理（通过/驳回）、导出等操作
-->
<template>
  <div>
    <!-- 搜索筛选区域 -->
   
    <el-card shadow="never" class="search-card">
      <div class="search-bar">
        <el-input v-model="searchForm.reportId" placeholder="搜索举报ID" prefix-icon="el-icon-search" class="search-input" size="small" clearable @clear="loadData" @keyup.enter.native="loadData"></el-input>
        <el-select v-model="searchForm.status" placeholder="筛选状态" class="search-select" size="small" clearable @change="loadData">
          <el-option label="待处理" :value="0"></el-option>
          <el-option label="已处理" :value="1"></el-option>
          <el-option label="已驳回" :value="2"></el-option>
        </el-select>
        <el-select v-model="searchForm.targetType" placeholder="筛选类型" class="search-select" size="small" clearable @change="loadData">
          <el-option label="帖子" value="post"></el-option>
          <el-option label="评论" value="comment"></el-option>
          <el-option label="商品" value="goods"></el-option>
          <el-option label="商品评价" value="goods_comment"></el-option>
          <el-option label="表白墙" value="wall"></el-option>
          <el-option label="用户" value="user"></el-option>
        </el-select>
        <el-select v-model="sortField" placeholder="排序" size="small" style="width:120px;" clearable @change="handleSortChange">
          <el-option label="举报时间 ↓" value="createTime_desc" />
          <el-option label="举报时间 ↑" value="createTime_asc" />
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
        <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        <el-button type="success" size="small" icon="el-icon-download" class="btn-export" @click="exportData">导出</el-button>
      </div>
    </el-card>
    <!-- 数据表格 -->
   

    <el-table :data="list" v-loading="loading" border @selection-change="handleSelectionChange" class="data-table">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="reportId" label="ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.reportId | formatId('report') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="targetType" label="举报类型" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="typeTag(scope.row.targetType)" size="small">{{ typeText(scope.row.targetType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetId" label="目标ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="handleTargetClick(scope.row)">{{ scope.row.targetId | formatId(scope.row.targetType) }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="举报原因" min-width="180" show-overflow-tooltip></el-table-column>
      <el-table-column prop="reporterId" label="举报人ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showUserDetail(scope.row.reporterId)">{{ scope.row.reporterId | formatId('user') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 0" type="warning">待处理</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">已处理</el-tag>
          <el-tag v-else type="info">已驳回</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="举报时间" width="170">
        <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" :disabled="scope.row.status !== 0" @click="handleProcess(scope.row)">处理</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
   
    <el-pagination class="pagination-bar" @current-change="handlePageChange" :current-page="pageNum" :page-size.sync="pageSize" :page-sizes="[10, 20, 50]" @size-change="handleSizeChange" :total="total" layout="total, sizes, prev, pager, next"></el-pagination>

    
    <el-dialog title="处理举报" :visible.sync="processDialogVisible" width="550px" class="process-dialog">
      <div v-if="processData" class="process-body">
        <div class="process-summary">
          <div class="process-icon"><i class="el-icon-warning-outline"></i></div>
          <div class="process-meta">
            <div class="process-type">{{ typeText(processData.targetType) }}举报</div>
            <div class="process-reason">{{ processData.reason }}</div>
          </div>
        </div>
        <el-divider></el-divider>
        <el-form :model="processForm" label-width="80px">
          <el-form-item label="处理结果">
            <el-radio-group v-model="processForm.result">
              <el-radio label="通过"><span style="color:#67C23A;font-weight:600;">通过</span></el-radio>
              <el-radio label="驳回"><span style="color:#909399;font-weight:600;">驳回</span></el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="处理说明">
            <el-input v-model="processForm.remark" type="textarea" :rows="3" placeholder="请输入处理说明"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer"><el-button @click="processDialogVisible = false">取消</el-button><el-button type="primary" @click="confirmProcess">确定</el-button></span>
    </el-dialog>

    
    <el-dialog title="举报详情" :visible.sync="detailVisible" width="700px" top="5vh" class="detail-dialog">
      <div v-if="detailData">
        <el-card shadow="hover" class="info-card">
          <div slot="header" class="info-header"><i class="el-icon-document"></i>基础信息</div>
          <table class="dt-table">
            <tr>
              <td class="dt-label">举报ID</td><td>{{ detailData.reportId | formatId('report') }}</td>
              <td class="dt-label">类型</td><td><el-tag :type="typeTag(detailData.targetType)" size="small">{{ typeText(detailData.targetType) }}</el-tag></td>
            </tr>
            <tr>
              <td class="dt-label">状态</td><td>
                <el-tag v-if="detailData.status === 0" type="warning" size="small">待处理</el-tag>
                <el-tag v-else-if="detailData.status === 1" type="success" size="small">已处理</el-tag>
                <el-tag v-else type="info" size="small">已驳回</el-tag>
              </td>
              <td class="dt-label">举报时间</td><td>{{ detailData.createTime | formatTime }}</td>
            </tr>
            <tr>
              <td class="dt-label">目标ID</td><td>
                <el-link type="primary" @click="handleTargetClick(detailData)">{{ detailData.targetId | formatId(detailData.targetType) }}</el-link>
              </td>
              <td class="dt-label">举报人ID</td><td>
                <el-link type="primary" @click="showUserDetail(detailData.reporterId)">{{ detailData.reporterId | formatId('user') }}</el-link>
              </td>
            </tr>
          </table>
        </el-card>
        <el-card shadow="hover" class="info-card">
          <div slot="header" class="info-header"><i class="el-icon-tickets"></i>举报详情</div>
          <div class="detail-content-row"><span class="detail-label">举报原因：</span>{{ detailData.reason }}</div>
          <div class="detail-content-row"><span class="detail-label">详细描述：</span><span class="detail-text">{{ detailData.description || '无' }}</span></div>
          <div v-if="detailData.images" class="detail-content-row">
            <span class="detail-label">凭证图片：</span>
            <div class="detail-images">
              <el-image v-for="(img, i) in parseImages(detailData.images)" :key="i" :src="img" :preview-src-list="parseImages(detailData.images)" class="detail-img" fit="cover"></el-image>
            </div>
          </div>
        </el-card>
        <el-card v-if="detailData.result" shadow="hover" class="info-card">
          <div slot="header" class="info-header"><i class="el-icon-finished"></i>处理结果</div>
          <div class="detail-content-row"><span class="detail-label">处理结果：</span><el-tag :type="detailData.result === '通过' ? 'success' : 'info'" size="small">{{ detailData.result }}</el-tag></div>
          <div class="detail-content-row"><span class="detail-label">处理说明：</span><span class="detail-text">{{ detailData.remark || '无' }}</span></div>
          <div class="detail-content-row"><span class="detail-label">处理时间：</span>{{ detailData.handleTime | formatTime }}</div>
        </el-card>
      </div>
    </el-dialog>

    <user-detail-dialog :visible.sync="userDetailVisible" :user-id="userDetailUserId" @show-post="showPostDetail" @show-goods="showGoodsDetail" @show-wall="showWallDetail" />

    <post-detail-dialog :visible.sync="postDetailVisible" :post-id="postDetailPostId" @show-user="showUserDetail"></post-detail-dialog>

    <goods-detail-dialog :visible.sync="goodsDetailVisible" :goods-id="goodsDetailGoodsId" @show-user="showUserDetail"></goods-detail-dialog>

    <wall-detail-dialog :visible.sync="wallDetailVisible" :wall-id="wallDetailWallId" @show-user="showUserDetail"></wall-detail-dialog>

    
    <el-dialog :title="commentDetailType === 'goods_comment' ? '商品评价详情' : '评论详情'" :visible.sync="commentDetailVisible" width="700px" top="5vh" append-to-body>
      <div v-loading="commentDetailLoading">
        <div v-if="commentDetailData">
          <el-card shadow="hover">
            <div slot="header" style="font-weight:bold;"><i class="el-icon-chat-dot-round" style="margin-right:6px;"></i>{{ commentDetailType === 'goods_comment' ? '评价信息' : '评论信息' }}</div>
            <table class="dt-table">
              <tr>
                <td class="dt-label">ID</td><td>{{ commentDetailData.commentId }}</td>
                <td class="dt-label">作者ID</td><td><el-link type="primary" @click="showUserDetail(commentDetailData.userId)">{{ commentDetailData.userId }}</el-link></td>
              </tr>
              <tr>
                <td class="dt-label">创建时间</td><td>{{ commentDetailData.createTime | formatTime }}</td>
                <td class="dt-label">点赞数</td><td>{{ commentDetailData.likeNum || 0 }}</td>
              </tr>
              <tr>
                <td class="dt-label">内容</td><td colspan="3" style="white-space:pre-wrap;line-height:1.8;">{{ commentDetailData.text || commentDetailData.content || '-' }}</td>
              </tr>
            </table>
          </el-card>
        </div>
        <div v-else style="text-align:center;color:#909399;padding:40px 0;">未找到该评论内容</div>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getReportList, handleReport, getCommentList, getGoodsCommentList } from '@/api/manage'
import UserDetailDialog from '@/components/UserDetailDialog'
import PostDetailDialog from '@/components/PostDetailDialog'
import GoodsDetailDialog from '@/components/GoodsDetailDialog'
import WallDetailDialog from '@/components/WallDetailDialog'
export default {
  /** 注册子组件 */
  components: { UserDetailDialog, PostDetailDialog, GoodsDetailDialog, WallDetailDialog },
  /** 组件数据定义 */
  data() { return { list: [], pageNum: 1, pageSize: 10, total: 0, loading: false, selectedRows: [], searchForm: { reportId: '', status: null, targetType: '' }, sortField: '', processDialogVisible: false, processData: {}, processForm: { result: '通过', remark: '' }, detailVisible: false, detailData: {}, userDetailVisible: false, userDetailUserId: '', postDetailVisible: false, postDetailPostId: '', goodsDetailVisible: false, goodsDetailGoodsId: '', wallDetailVisible: false, wallDetailWallId: '', commentDetailVisible: false, commentDetailLoading: false, commentDetailData: null, commentDetailType: '' } },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadData() },
  /** 组件方法定义 */
  methods: {
    typeText(type) { var map = { 'post': '帖子', 'comment': '评论', 'goods': '商品', 'goods_comment': '商品评价', 'wall': '表白墙', 'user': '用户' }; return map[type] || type },
    typeTag(type) { var map = { 'post': 'primary', 'comment': 'success', 'goods': 'warning', 'goods_comment': 'info', 'wall': 'danger', 'user': 'info' }; return map[type] || '' },
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      const res = await getReportList({ pageNum: this.pageNum, pageSize: this.pageSize, ...this.searchForm })
      if (res.code === 200) { this.list = res.data.records || []; this.total = res.data.total || 0 }
      this.loading = false
    },
    handleProcess(row) { this.processData = row; this.processForm = { result: '通过', remark: '' }; this.processDialogVisible = true },
    async confirmProcess() {
      var status = this.processForm.result === '通过' ? 1 : 2
      var params = {
        reportId: this.processData.reportId,
        status: status,
        handleResult: this.processForm.result,
        targetType: this.processData.targetType,
        targetId: this.processData.targetId
      }
      if (this.processForm.result === '通过') {
        params.punishType = 'warning'
        params.lockReason = this.processForm.remark || '违规处理'
      } else {
        params.punishType = ''
        params.lockReason = this.processForm.remark || ''
      }
      const res = await handleReport(params)
      if (res.code === 200) { this.$message.success('处理成功'); this.processDialogVisible = false; this.loadData() }
      else this.$message.error(res.msg || '处理失败')
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
    /** 分页页码变化回调 */
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    /** 每页条数变化回调 */
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    /** 显示详情弹窗 */
    showDetail(row) { this.detailData = row; this.detailVisible = true },
    handleTargetClick(row) {
      if (row.targetType === 'post') { this.postDetailPostId = row.targetId; this.postDetailVisible = true }
      else if (row.targetType === 'goods') { this.goodsDetailGoodsId = row.targetId; this.goodsDetailVisible = true }
      else if (row.targetType === 'user') { this.userDetailUserId = row.targetId; this.userDetailVisible = true }
      else if (row.targetType === 'wall') { this.wallDetailWallId = row.targetId; this.wallDetailVisible = true }
      else if (row.targetType === 'comment' || row.targetType === 'goods_comment') { this.showCommentDetail(row.targetId, row.targetType) }
    },
    async showCommentDetail(commentId, type) {
      this.commentDetailType = type
      this.commentDetailData = null
      this.commentDetailVisible = true
      this.commentDetailLoading = true
      try {
        var api = type === 'goods_comment' ? getGoodsCommentList : getCommentList
        var res = await api({ pageNum: 1, pageSize: 9999 })
        if (res.code === 200) {
          var records = res.data.records || res.data || []
          this.commentDetailData = records.find(function(c) { return c.commentId === commentId }) || null
        }
      } catch (e) { this.commentDetailData = null }
      this.commentDetailLoading = false
    },
    /** 显示用户详情弹窗 */
    showUserDetail(userId) { this.userDetailUserId = userId; this.userDetailVisible = true },
    /** 显示帖子详情弹窗 */
    showPostDetail(postId) { this.postDetailPostId = postId; this.postDetailVisible = true },
    /** 显示商品详情弹窗 */
    showGoodsDetail(goodsId) { this.goodsDetailGoodsId = goodsId; this.goodsDetailVisible = true },
    /** 显示表白墙详情弹窗 */
    showWallDetail(wallId) { this.wallDetailWallId = wallId; this.wallDetailVisible = true },
    parseImages(images) {
      if (!images) return []
      try { const arr = JSON.parse(images); return Array.isArray(arr) ? arr : [images] } catch (e) { return images.split(',').filter(function(s) { return s.trim() }) }
    },
    /** 重置搜索条件并重新加载数据 */
    resetSearch() { this.searchForm = { reportId: '', status: null, targetType: '' }; this.loadData() },
    /** 导出数据为CSV文件 */
    exportData() {
      if (this.list.length === 0) { this.$message.warning('没有可导出的数据'); return }
      const columns = [
        { prop: 'reportId', label: '举报ID' }, { prop: 'targetType', label: '类型' },
        { prop: 'targetId', label: '目标ID' }, { prop: 'reason', label: '举报原因' },
        { prop: 'reporterId', label: '举报人ID' }, { prop: 'status', label: '状态' },
        { prop: 'createTime', label: '举报时间' }
      ]
      const headers = columns.map(c => c.label).join(',')
      const rows = this.list.map(row => columns.map(c => {
        const val = row[c.prop]
        if (c.prop === 'targetType') return this.typeText(val)
        if (c.prop === 'status') return val === 0 ? '待处理' : val === 1 ? '已处理' : '已驳回'
        return val != null ? String(val) : ''
      }).join(',')).join('\n')
      const csv = '\uFEFF' + headers + '\n' + rows
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob); link.download = '举报数据.csv'; link.click()
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
.btn-export {
  margin-left: auto;
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
.process-dialog .process-body {
  padding: 8px 0;
}
.process-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #fdf6ec, #fff);
  border-radius: 12px;
}
.process-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #E6A23C, #f5c842);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(230,162,60,0.3);
}
.process-meta {
  flex: 1;
}
.process-type {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}
.process-reason {
  color: #606266;
  font-size: 13px;
  margin-top: 4px;
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
.dt-table { width: 100%; border-collapse: collapse; border-color: #ebeef5; }
.dt-table td { padding: 10px 12px; border: 1px solid #ebeef5; font-size: 13px; color: #303133; }
.dt-label { background: #f8fafc; font-weight: 600; width: 100px; text-align: right; color: #4b5563; white-space: nowrap; }
</style>
