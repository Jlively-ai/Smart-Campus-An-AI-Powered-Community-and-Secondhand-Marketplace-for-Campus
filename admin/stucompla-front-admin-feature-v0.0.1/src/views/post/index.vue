<!--
  组件名：PostIndex
  功能描述：帖子管理页面
  主要职责：展示帖子列表，支持搜索、审核、锁定/解锁、删除、批量操作、导出等
-->
<template>
  <div>
    <!-- 搜索筛选区域 -->
   
    <el-card shadow="never" class="search-card">
      <div class="search-bar">
        <el-input v-model="searchForm.title" placeholder="搜索帖子标题" prefix-icon="el-icon-search" class="search-input" size="small" clearable @clear="loadData" @keyup.enter.native="loadData"></el-input>
        <el-select v-model="searchForm.categoryId" placeholder="筛选分类" class="search-select" size="small" clearable @change="loadData">
          <el-option label="分类1" value="1"></el-option>
          <el-option label="分类2" value="2"></el-option>
          <el-option label="分类3" value="3"></el-option>
        </el-select>
        <el-select v-model="searchForm.auditState" placeholder="审核状态" class="search-select-sm" size="small" clearable @change="loadData">
          <el-option label="待审核" :value="0"></el-option>
          <el-option label="已通过" :value="1"></el-option>
          <el-option label="已拒绝" :value="2"></el-option>
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
        <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        <el-button type="success" size="small" icon="el-icon-download" class="btn-export" @click="exportData">导出</el-button>
      </div>
    </el-card>
    <!-- 操作工具栏 -->
   

    <div class="toolbar" v-if="selectedRows.length > 0">
      <el-button type="danger" size="small" icon="el-icon-delete" @click="batchDelete">批量删除 ({{ selectedRows.length }})</el-button>
      <el-button type="success" size="small" icon="el-icon-check" @click="batchApprove">批量通过审核</el-button>
      <el-button type="warning" size="small" icon="el-icon-close" @click="batchReject">批量拒绝审核</el-button>
    </div>
    <!-- 数据表格 -->
   

    <el-table :data="list" v-loading="loading" border @selection-change="handleSelectionChange" class="data-table">
      <el-table-column type="selection" width="50"></el-table-column>
      <el-table-column prop="postId" label="ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.postId | formatId('post') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="作者" width="100" show-overflow-tooltip></el-table-column>
      <el-table-column prop="userId" label="用户ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showUserDetail(scope.row.userId)">{{ scope.row.userId | formatId('user') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="commentNum" label="评论" width="70" align="center"></el-table-column>

      <el-table-column prop="postStatus" label="状态" width="80" align="center">
        <template slot-scope="scope"><el-tag :type="scope.row.postStatus === 0 ? 'success' : 'danger'">{{ scope.row.postStatus === 0 ? '正常' : '锁定' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="auditState" label="审核" width="90" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.auditState === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.auditState === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.auditState === 2" type="danger">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewNum" label="浏览量" width="70" align="center"></el-table-column>
      <el-table-column prop="likeNum" label="点赞数" width="70" align="center"></el-table-column>
      <el-table-column prop="collectNum" label="收藏数" width="70" align="center"></el-table-column>
      <el-table-column prop="shareNum" label="分享数" width="70" align="center"></el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="170"></el-table-column>
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" :disabled="scope.row.auditState !== 0" @click="handleAuditPost(scope.row.postId)">审核</el-button>
          <el-button size="mini" :type="scope.row.postStatus === 1 ? 'success' : 'warning'" @click="scope.row.postStatus === 1 ? unlockPost(scope.row.postId) : lockPost(scope.row.postId)">{{ scope.row.postStatus === 1 ? '解锁' : '锁定' }}</el-button>
          <el-button size="mini" type="danger" @click="handleDeletePost(scope.row.postId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
   
    <el-pagination class="pagination-bar" @current-change="handlePageChange" :current-page="pageNum" :page-size.sync="pageSize" :page-sizes="[10, 20, 50]" @size-change="handleSizeChange" :total="total" layout="total, sizes, prev, pager, next"></el-pagination>

    <post-detail-dialog :visible.sync="postDetailVisible" :post-id="postDetailPostId" @show-user="showUserDetail"></post-detail-dialog>

    <user-detail-dialog :visible.sync="userDetailVisible" :user-id="userDetailUserId" @show-post="showDetailById" @show-goods="showGoodsDetail" @show-wall="showWallDetail" />

    <goods-detail-dialog :visible.sync="goodsDetailVisible" :goods-id="goodsDetailGoodsId" @show-user="showUserDetail"></goods-detail-dialog>

    <wall-detail-dialog :visible.sync="wallDetailVisible" :wall-id="wallDetailWallId" @show-user="showUserDetail"></wall-detail-dialog>

    
    <el-dialog title="删除帖子" :visible.sync="deleteDialogVisible" width="450px" :close-on-click-modal="false" class="confirm-dialog">
      <div class="confirm-body">
        <div class="confirm-icon danger"><i class="el-icon-warning-outline"></i></div>
        <div class="confirm-title">确定要删除该帖子吗？</div>
        <div class="confirm-sub">请选择删除原因：</div>
        <el-radio-group v-model="deleteCause" class="confirm-options">
          <el-radio label="含有违规内容">含有违规内容</el-radio>
          <el-radio label="含有广告信息">含有广告信息</el-radio>
          <el-radio label="恶意攻击或辱骂">恶意攻击或辱骂</el-radio>
          <el-radio label="散布不实信息">散布不实信息</el-radio>
          <el-radio label="侵犯他人隐私">侵犯他人隐私</el-radio>
          <el-radio label="其他">其他</el-radio>
        </el-radio-group>
        <el-input v-if="deleteCause === '其他'" v-model="deleteCauseCustom" placeholder="请输入删除原因" style="margin-top:10px;" maxlength="50" show-word-limit></el-input>
      </div>
      <div slot="footer">
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDeletePost" :loading="deleteLoading">确定删除</el-button>
      </div>
    </el-dialog>

    
    <el-dialog title="审核帖子" :visible.sync="auditDialogVisible" width="450px" :close-on-click-modal="false" class="confirm-dialog">
      <div class="confirm-body">
        <div class="confirm-icon primary"><i class="el-icon-s-check"></i></div>
        <div class="confirm-title">请选择审核结果</div>
        <el-radio-group v-model="auditForm.auditState" class="confirm-options">
          <el-radio :label="1"><span style="color:#67C23A;font-weight:600;">通过</span></el-radio>
          <el-radio :label="2"><span style="color:#F56C6C;font-weight:600;">拒绝</span></el-radio>
        </el-radio-group>
        <el-input v-if="auditForm.auditState === 2" v-model="auditForm.auditFailedCause" placeholder="请输入拒绝原因" style="margin-top:10px;" maxlength="100" show-word-limit></el-input>
      </div>
      <div slot="footer">
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAuditPost" :loading="auditLoading">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getPostList, lockedPost, unLockPost, deletePost, auditPost } from '@/api/manage'
import UserDetailDialog from '@/components/UserDetailDialog'
import PostDetailDialog from '@/components/PostDetailDialog'
import GoodsDetailDialog from '@/components/GoodsDetailDialog'
import WallDetailDialog from '@/components/WallDetailDialog'
export default {
  /** 注册子组件 */
  components: { UserDetailDialog, PostDetailDialog, GoodsDetailDialog, WallDetailDialog },
  /** 组件数据定义 */
  data() { return { list: [], pageNum: 1, pageSize: 10, total: 0, searchForm: { title: '', categoryId: null, auditState: null }, loading: false, selectedRows: [], postDetailVisible: false, postDetailPostId: '', userDetailVisible: false, userDetailUserId: '', goodsDetailVisible: false, goodsDetailGoodsId: '', wallDetailVisible: false, wallDetailWallId: '', deleteDialogVisible: false, deletePostId: null, deleteCause: '含有违规内容', deleteCauseCustom: '', deleteLoading: false, auditDialogVisible: false, auditPostId: null, auditForm: { auditState: 1, auditFailedCause: '' }, auditLoading: false } },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadData() },
  /** 组件方法定义 */
  methods: {
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      const res = await getPostList({ pageNum: this.pageNum, pageSize: this.pageSize, ...this.searchForm })
      if (res.code === 200) { this.list = res.data.records || []; this.total = res.data.total || 0 }
      this.loading = false
    },
    /** 锁定帖子 */
    async lockPost(postId) { await lockedPost(postId, '管理员锁定'); this.$message.success('锁定成功'); this.loadData() },
    /** 解锁帖子 */
    async unlockPost(postId) { await unLockPost(postId); this.$message.success('解锁成功'); this.loadData() },
    /** 打开删除弹窗 */
    async handleDeletePost(postId) { this.deletePostId = postId; this.deleteCause = '含有违规内容'; this.deleteCauseCustom = ''; this.deleteDialogVisible = true },
    /** 确认删除帖子 */
    async confirmDeletePost() {
      const cause = this.deleteCause === '其他' ? this.deleteCauseCustom : this.deleteCause
      if (this.deleteCause === '其他' && !this.deleteCauseCustom.trim()) { this.$message.warning('请输入删除原因'); return }
      this.deleteLoading = true
      try { await deletePost(this.deletePostId, cause); this.$message.success('删除成功'); this.deleteDialogVisible = false; this.loadData() } catch (e) { }
      this.deleteLoading = false
    },
    /** 打开审核弹窗 */
    handleAuditPost(postId) { this.auditPostId = postId; this.auditForm = { auditState: 1, auditFailedCause: '' }; this.auditDialogVisible = true },
    /** 确认审核帖子 */
    async confirmAuditPost() {
      if (this.auditForm.auditState === 2 && !this.auditForm.auditFailedCause.trim()) { this.$message.warning('请输入拒绝原因'); return }
      this.auditLoading = true
      try { await auditPost({ postId: this.auditPostId, auditState: this.auditForm.auditState, auditFailedCause: this.auditForm.auditFailedCause }); this.$message.success('审核成功'); this.auditDialogVisible = false; this.loadData() } catch (e) { }
      this.auditLoading = false
    },
    /** 表格选中行变化回调 */
    handleSelectionChange(val) { this.selectedRows = val },
    /** 批量删除选中记录 */
    batchDelete() {
      if (this.selectedRows.length === 0) return this.$message.warning('请先选择要删除的帖子')
      this.$confirm('确定要批量删除选中的 ' + this.selectedRows.length + ' 个帖子吗？', '批量删除', { type: 'warning' }).then(async () => {
        var count = 0
        for (var i = 0; i < this.selectedRows.length; i++) {
          try { await deletePost(this.selectedRows[i].postId, '批量删除'); count++ } catch (e) {}
        }
        this.$message.success('成功删除 ' + count + ' 个帖子')
        this.loadData()
      }).catch(() => {})
    },
    /** 批量通过审核 */
    batchApprove() {
      var items = this.selectedRows.filter(r => r.auditState === 0)
      if (items.length === 0) return this.$message.warning('选中的帖子中没有待审核的')
      this.$confirm('确定要批量通过选中的 ' + items.length + ' 个帖子吗？', '批量通过审核', { type: 'warning' }).then(async () => {
        var count = 0
        for (var i = 0; i < items.length; i++) {
          try { await auditPost({ postId: items[i].postId, auditState: 1 }); count++ } catch (e) {}
        }
        this.$message.success('成功通过 ' + count + ' 个帖子')
        this.loadData()
      }).catch(() => {})
    },
    /** 批量拒绝审核 */
    batchReject() {
      var items = this.selectedRows.filter(r => r.auditState === 0)
      if (items.length === 0) return this.$message.warning('选中的帖子中没有待审核的')
      this.$confirm('确定要批量拒绝选中的 ' + items.length + ' 个帖子吗？', '批量拒绝审核', { type: 'warning' }).then(async () => {
        var count = 0
        for (var i = 0; i < items.length; i++) {
          try { await auditPost({ postId: items[i].postId, auditState: 2, auditFailedCause: '批量拒绝' }); count++ } catch (e) {}
        }
        this.$message.success('成功拒绝 ' + count + ' 个帖子')
        this.loadData()
      }).catch(() => {})
    },
    /** 分页页码变化回调 */
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    /** 每页条数变化回调 */
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    /** 显示详情弹窗 */
    showDetail(row) { this.postDetailPostId = row.postId; this.postDetailVisible = true },
    /** 根据ID显示详情弹窗 */
    showDetailById(postId) { this.postDetailPostId = postId; this.postDetailVisible = true },
    /** 显示用户详情弹窗 */
    showUserDetail(userId) { this.userDetailUserId = userId; this.userDetailVisible = true },
    /** 显示商品详情弹窗 */
    showGoodsDetail(goodsId) { this.goodsDetailGoodsId = goodsId; this.goodsDetailVisible = true },
    /** 显示表白墙详情弹窗 */
    showWallDetail(wallId) { this.wallDetailWallId = wallId; this.wallDetailVisible = true },
    /** 重置搜索条件并重新加载数据 */
    resetSearch() {
      this.searchForm = { title: '', categoryId: null, auditState: null }
      this.loadData()
    },
    /** 导出数据为CSV文件 */
    exportData() {
      if (this.list.length === 0) { this.$message.warning('没有可导出的数据'); return }
      const columns = [
        { prop: 'postId', label: '帖子ID' },
        { prop: 'title', label: '标题' },
        { prop: 'nickname', label: '作者昵称' },
        { prop: 'commentNum', label: '评论数' },
        { prop: 'viewNum', label: '浏览数' },
        { prop: 'postStatus', label: '状态' },
        { prop: 'createTime', label: '发布时间' }
      ]
      const headers = columns.map(c => c.label).join(',')
      const rows = this.list.map(row => columns.map(c => {
        const val = row[c.prop]
        if (c.prop === 'postStatus') return val === 0 ? '正常' : '锁定'
        return val != null ? String(val) : ''
      }).join(',')).join('\n')
      const csv = '\uFEFF' + headers + '\n' + rows
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '帖子数据.csv'
      link.click()
      URL.revokeObjectURL(link.href)
      this.$message.success('导出成功')
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
.search-select-sm {
  width: 130px;
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
.confirm-body {
  text-align: center;
  padding: 8px 0 16px;
}
.confirm-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-bottom: 12px;
}
.confirm-icon.danger {
  background: linear-gradient(135deg, #fef0f0, #fde2e2);
  color: #F56C6C;
}
.confirm-icon.primary {
  background: linear-gradient(135deg, #ecf5ff, #d9ecff);
  color: #409EFF;
}
.confirm-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.confirm-sub {
  font-size: 13px;
  color: #909399;
  margin-bottom: 16px;
}
.confirm-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: left;
  padding: 0 20px;
}
</style>
