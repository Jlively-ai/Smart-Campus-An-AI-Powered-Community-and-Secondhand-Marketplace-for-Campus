<!--
  组件名：CommentIndex
  功能描述：评论管理页面
  主要职责：展示评论列表，支持搜索、删除、导出等操作
-->
<template>
  <div>
    <div style="margin-bottom:15px;display:flex;flex-wrap:wrap;gap:10px;align-items:center;">
      <el-input v-model="searchForm.text" placeholder="搜索评论内容" prefix-icon="el-icon-search" style="width:200px;" size="small" clearable @clear="loadData" @keyup.enter.native="loadData"></el-input>
      <el-select v-model="sortField" placeholder="排序" size="small" style="width:120px;" clearable @change="handleSortChange">
        <el-option label="评论时间 ↓" value="createTime_desc" />
        <el-option label="评论时间 ↑" value="createTime_asc" />
      </el-select>
      <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
      <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
      <el-button type="success" size="small" icon="el-icon-download" style="margin-left:auto;" @click="exportData">导出</el-button>
    </div>
    <div style="margin-bottom:10px;" v-if="selectedRows.length > 0">
      <el-button type="danger" size="small" icon="el-icon-delete" @click="batchDelete">批量删除 ({{ selectedRows.length }})</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50"></el-table-column>
      <el-table-column prop="commentId" label="ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showCommentDetail(scope.row)">{{ scope.row.commentId | formatId('comment') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="text" label="评论内容" min-width="250" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showCommentDetail(scope.row)">{{ scope.row.text }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="postId" label="帖子ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showPostInfo(scope.row.postId)">{{ scope.row.postId | formatId('post') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="用户ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showUserInfo(scope.row.userId)">{{ scope.row.userId | formatId('user') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="locked" label="状态" width="90" align="center">
        <template slot-scope="scope"><el-tag :type="scope.row.locked === 1 ? 'danger' : 'success'">{{ scope.row.locked === 1 ? '锁定' : '正常' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="auditState" label="审核" width="90" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.auditState === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.auditState === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.auditState === 2" type="danger">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="评论时间" width="170">
        <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" :disabled="scope.row.auditState !== 0" @click="handleAuditComment(scope.row.commentId)">审核</el-button>
          <el-button size="mini" :type="scope.row.locked === 1 ? 'success' : 'warning'" @click="scope.row.locked === 1 ? unlockComment(scope.row.commentId) : lockComment(scope.row.commentId)">{{ scope.row.locked === 1 ? '解锁' : '锁定' }}</el-button>
          <el-button size="mini" type="danger" @click="deleteComment(scope.row.commentId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:15px;" @current-change="handlePageChange" :current-page="pageNum" :page-size.sync="pageSize" :page-sizes="[10, 20, 50]" @size-change="handleSizeChange" :total="total" layout="total, sizes, prev, pager, next"></el-pagination>

    
    <el-dialog title="评论详情" :visible.sync="commentDialogVisible" width="700px" top="5vh">
      <div v-if="currentComment">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-chat-dot-round" style="margin-right:6px;"></i>评论信息</div>
          <table class="dt-table">
            <tr><td class="dt-label">评论ID</td><td>{{ currentComment.commentId | formatId('comment') }}</td></tr>
            <tr><td class="dt-label">评论内容</td><td style="white-space:pre-wrap;">{{ currentComment.text }}</td></tr>
            <tr v-if="currentComment.images">
              <td class="dt-label">评论图片</td>
              <td>
                <el-image v-for="(img, i) in parseCommentImages(currentComment.images)" :key="i" :src="img" :preview-src-list="parseCommentImages(currentComment.images)" style="width:80px;height:80px;margin-right:6px;border-radius:4px;" fit="cover"></el-image>
              </td>
            </tr>
            <tr><td class="dt-label">所属帖子</td><td><el-link type="primary" @click="showPostInfo(currentComment.postId)">{{ currentComment.postId | formatId('post') }}</el-link></td></tr>
            <tr><td class="dt-label">评论者</td><td><el-link type="primary" @click="showUserInfo(currentComment.userId)">{{ currentComment.userId | formatId('user') }}</el-link></td></tr>
            <tr><td class="dt-label">用户类型</td><td>{{ currentComment.userType === 'admin' ? '管理员' : '普通用户' }}</td></tr>
            <tr><td class="dt-label">点赞数</td><td>{{ currentComment.likeNum || 0 }}</td></tr>
            <tr v-if="currentComment.parentId"><td class="dt-label">回复评论</td><td>{{ currentComment.parentId }}</td></tr>
            <tr><td class="dt-label">评论时间</td><td>{{ currentComment.createTime | formatTime }}</td></tr>
          </table>
        </el-card>
      </div>
    </el-dialog>

    
    <post-detail-dialog :visible.sync="postDetailVisible" :post-id="postDetailPostId" @show-user="showUserInfo"></post-detail-dialog>

    
    <user-detail-dialog :visible.sync="userDetailVisible" :user-id="userDetailUserId" @show-post="showPostInfo" @show-goods="showGoodsDetail" @show-wall="showWallDetail"></user-detail-dialog>

    <goods-detail-dialog :visible.sync="goodsDetailVisible" :goods-id="goodsDetailGoodsId" @show-user="showUserInfo"></goods-detail-dialog>

    <wall-detail-dialog :visible.sync="wallDetailVisible" :wall-id="wallDetailWallId" @show-user="showUserInfo"></wall-detail-dialog>

    
    <el-dialog title="审核评论" :visible.sync="auditDialogVisible" width="450px" :close-on-click-modal="false">
      <p style="margin-bottom:15px;color:#666;">请选择审核结果：</p>
      <el-radio-group v-model="auditForm.auditState" style="display:flex;flex-direction:column;gap:10px;">
        <el-radio :label="1">通过</el-radio>
        <el-radio :label="2">拒绝</el-radio>
      </el-radio-group>
      <el-input v-if="auditForm.auditState === 2" v-model="auditForm.auditFailedCause" placeholder="请输入拒绝原因" style="margin-top:10px;" maxlength="100" show-word-limit></el-input>
      <div slot="footer">
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAuditComment" :loading="auditLoading">确定</el-button>
      </div>
    </el-dialog>

    
    <el-dialog title="锁定评论" :visible.sync="lockDialogVisible" width="450px" :close-on-click-modal="false">
      <p style="margin-bottom:15px;color:#666;">确定要锁定该评论吗？请选择锁定原因：</p>
      <el-radio-group v-model="lockCause" style="display:flex;flex-direction:column;gap:10px;">
        <el-radio label="含有违规内容">含有违规内容</el-radio>
        <el-radio label="含有广告信息">含有广告信息</el-radio>
        <el-radio label="恶意攻击或辱骂">恶意攻击或辱骂</el-radio>
        <el-radio label="散布不实信息">散布不实信息</el-radio>
        <el-radio label="侵犯他人隐私">侵犯他人隐私</el-radio>
        <el-radio label="其他">其他</el-radio>
      </el-radio-group>
      <el-input v-if="lockCause === '其他'" v-model="lockCauseCustom" placeholder="请输入锁定原因" style="margin-top:10px;" maxlength="50" show-word-limit></el-input>
      <div slot="footer">
        <el-button @click="lockDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmLockComment" :loading="lockLoading">确定锁定</el-button>
      </div>
    </el-dialog>

    
    <el-dialog title="删除评论" :visible.sync="deleteDialogVisible" width="450px" :close-on-click-modal="false">
      <p style="margin-bottom:15px;color:#666;">确定要删除该评论吗？请选择删除原因：</p>
      <el-radio-group v-model="deleteCause" style="display:flex;flex-direction:column;gap:10px;">
        <el-radio label="含有违规内容">含有违规内容</el-radio>
        <el-radio label="含有广告信息">含有广告信息</el-radio>
        <el-radio label="恶意攻击或辱骂">恶意攻击或辱骂</el-radio>
        <el-radio label="散布不实信息">散布不实信息</el-radio>
        <el-radio label="侵犯他人隐私">侵犯他人隐私</el-radio>
        <el-radio label="其他">其他</el-radio>
      </el-radio-group>
      <el-input v-if="deleteCause === '其他'" v-model="deleteCauseCustom" placeholder="请输入删除原因" style="margin-top:10px;" maxlength="50" show-word-limit></el-input>
      <div slot="footer">
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete" :loading="deleteLoading">确定删除</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getCommentList, deleteComment, lockComment, unlockComment, auditComment } from '@/api/manage'
import UserDetailDialog from '@/components/UserDetailDialog'
import PostDetailDialog from '@/components/PostDetailDialog'
import GoodsDetailDialog from '@/components/GoodsDetailDialog'
import WallDetailDialog from '@/components/WallDetailDialog'
export default {
  /** 注册子组件 */
  components: { UserDetailDialog, PostDetailDialog, GoodsDetailDialog, WallDetailDialog },
  /** 组件数据定义 */
  data() {
    return {
      list: [], pageNum: 1, pageSize: 10, total: 0, loading: false, selectedRows: [], searchForm: { text: '' },
      sortField: '',
      commentDialogVisible: false, currentComment: {},
      postDetailVisible: false, postDetailPostId: '',
      userDetailVisible: false, userDetailUserId: '',
      goodsDetailVisible: false, goodsDetailGoodsId: '',
      wallDetailVisible: false, wallDetailWallId: '',
      auditDialogVisible: false, auditCommentId: null, auditForm: { auditState: 1, auditFailedCause: '' }, auditLoading: false,
      lockDialogVisible: false, lockCommentId: null, lockCause: '含有违规内容', lockCauseCustom: '', lockLoading: false,
      deleteDialogVisible: false, deleteCommentId: null, deleteCause: '含有违规内容', deleteCauseCustom: '', deleteLoading: false
    }
  },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadData() },
  /** 组件方法定义 */
  methods: {
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      const res = await getCommentList({ pageNum: this.pageNum, pageSize: this.pageSize, ...this.searchForm })
      if (res.code === 200) { this.list = res.data.records || []; this.total = res.data.total || 0 }
      this.loading = false
    },
    handleAuditComment(commentId) { this.auditCommentId = commentId; this.auditForm = { auditState: 1, auditFailedCause: '' }; this.auditDialogVisible = true },
    async confirmAuditComment() {
      if (this.auditForm.auditState === 2 && !this.auditForm.auditFailedCause.trim()) { this.$message.warning('请输入拒绝原因'); return }
      this.auditLoading = true
      try { await auditComment(this.auditCommentId, this.auditForm.auditState, this.auditForm.auditFailedCause); this.$message.success('审核成功'); this.auditDialogVisible = false; this.loadData() } catch (e) { }
      this.auditLoading = false
    },
    lockComment(commentId) { this.lockCommentId = commentId; this.lockCause = '含有违规内容'; this.lockCauseCustom = ''; this.lockDialogVisible = true },
    async confirmLockComment() {
      const cause = this.lockCause === '其他' ? this.lockCauseCustom : this.lockCause
      if (this.lockCause === '其他' && !this.lockCauseCustom.trim()) { this.$message.warning('请输入锁定原因'); return }
      this.lockLoading = true
      try { await lockComment(this.lockCommentId, cause); this.$message.success('锁定成功'); this.lockDialogVisible = false; this.loadData() } catch (e) { }
      this.lockLoading = false
    },
    async unlockComment(commentId) { await unlockComment(commentId); this.$message.success('解锁成功'); this.loadData() },
    deleteComment(commentId) {
      this.deleteCommentId = commentId
      this.deleteCause = '含有违规内容'
      this.deleteCauseCustom = ''
      this.deleteDialogVisible = true
    },
    async confirmDelete() {
      const cause = this.deleteCause === '其他' ? this.deleteCauseCustom : this.deleteCause
      if (this.deleteCause === '其他' && !this.deleteCauseCustom.trim()) {
        this.$message.warning('请输入删除原因')
        return
      }
      this.deleteLoading = true
      try {
        await deleteComment(this.deleteCommentId, cause)
        this.$message.success('删除成功')
        this.deleteDialogVisible = false
        this.loadData()
      } catch (e) {  }
      this.deleteLoading = false
    },
    /** 表格选中行变化回调 */
    handleSelectionChange(val) { this.selectedRows = val },
    /** 批量删除选中记录 */
    batchDelete() {
      if (this.selectedRows.length === 0) return this.$message.warning('请先选择要删除的评论')
      this.$confirm('确定要批量删除选中的 ' + this.selectedRows.length + ' 条评论吗？', '批量删除', { type: 'warning' }).then(async () => {
        var count = 0
        for (var i = 0; i < this.selectedRows.length; i++) {
          try { await deleteComment(this.selectedRows[i].commentId, '批量删除'); count++ } catch (e) {}
        }
        this.$message.success('成功删除 ' + count + ' 条评论')
        this.loadData()
      }).catch(() => {})
    },
    /** 分页页码变化回调 */
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    /** 每页条数变化回调 */
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    showCommentDetail(row) { this.currentComment = row; this.commentDialogVisible = true },
    parseCommentImages(images) {
      if (!images) return []
      if (Array.isArray(images)) return images.filter(function(s) { return s && s.trim() })
      var str = String(images).trim()
      if (str === '' || str === '[]' || str === 'null') return []
      try { var parsed = JSON.parse(str); if (Array.isArray(parsed)) return parsed.filter(function(s) { return s && s.trim() }); return [parsed] } catch (e) { return str.split(',').filter(function(s) { return s && s.trim() }) }
    },
    showPostInfo(postId) { this.postDetailPostId = postId; this.postDetailVisible = true },
    showUserInfo(userId) { this.userDetailUserId = userId; this.userDetailVisible = true },
    /** 显示商品详情弹窗 */
    showGoodsDetail(goodsId) { this.goodsDetailGoodsId = goodsId; this.goodsDetailVisible = true },
    /** 显示表白墙详情弹窗 */
    showWallDetail(wallId) { this.wallDetailWallId = wallId; this.wallDetailVisible = true },
    /** 重置搜索条件并重新加载数据 */
    resetSearch() {
      this.searchForm = { text: '' }
      this.sortField = ''
      this.loadData()
    },
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
        var ta = new Date(va).getTime(), tb = new Date(vb).getTime()
        return order === 'asc' ? ta - tb : tb - ta
      })
    },
    /** 导出数据为CSV文件 */
    exportData() {
      if (this.list.length === 0) { this.$message.warning('没有可导出的数据'); return }
      const columns = [
        { prop: 'commentId', label: '评论ID' },
        { prop: 'text', label: '评论内容' },
        { prop: 'postId', label: '帖子ID' },
        { prop: 'userId', label: '用户ID' },
        { prop: 'createTime', label: '评论时间' }
      ]
      const headers = columns.map(c => c.label).join(',')
      const rows = this.list.map(row => columns.map(c => {
        const val = row[c.prop]
        return val != null ? String(val) : ''
      }).join(',')).join('\n')
      const csv = '\uFEFF' + headers + '\n' + rows
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '评论数据.csv'
      link.click()
      URL.revokeObjectURL(link.href)
      this.$message.success('导出成功')
    }
  }
}
</script>
<style scoped>
/* 组件局部样式 */

.page-container { padding: 20px; background: #f5f7fa; min-height: 100%; }

.id-cell { display: inline-block; max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; vertical-align: middle; }
.dt-table { width: 100%; border-collapse: collapse; border-color: #ebeef5; }
.dt-table td { padding: 10px 12px; border: 1px solid #ebeef5; font-size: 13px; color: #303133; }
.dt-label { background: #fafafa; font-weight: bold; width: 100px; text-align: right; color: #606266; white-space: nowrap; }

::v-deep .el-table { border-radius: 12px; overflow: hidden; }
::v-deep .el-table th .cell { color: #303133; font-weight: 600; }
::v-deep .el-table .el-button--mini { margin: 0 2px; }

::v-deep .el-tag { border-radius: 6px; }

::v-deep .el-card { border-radius: 12px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); transition: box-shadow 0.3s ease; }
::v-deep .el-card:hover { box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1); }

::v-deep .el-button { transition: all 0.3s ease; }
::v-deep .el-link { transition: color 0.3s ease; }

::v-deep .el-pagination { padding: 16px 0; }

::v-deep .el-dialog__title { font-weight: 600; color: #303133; }
</style>
