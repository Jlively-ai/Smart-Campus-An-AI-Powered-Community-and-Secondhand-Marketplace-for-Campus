<!--
  组件名：WallIndex
  功能描述：表白墙审核页面
  主要职责：展示表白墙列表，支持搜索、审核（通过/拒绝）、删除、导出等操作
-->
<template>
  <div>
    <div style="margin-bottom:15px;display:flex;flex-wrap:wrap;gap:10px;align-items:center;">
      <el-input v-model="searchForm.wallContent" placeholder="搜索墙内容" prefix-icon="el-icon-search" style="width:200px;" size="small" clearable @clear="loadData" @keyup.enter.native="loadData"></el-input>
      <el-select v-model="searchForm.auditState" placeholder="筛选审核状态" style="width:150px;" size="small" clearable @change="loadData">
        <el-option label="待审核" :value="0"></el-option>
        <el-option label="已通过" :value="1"></el-option>
        <el-option label="已拒绝" :value="2"></el-option>
      </el-select>
      <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
      <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
      <el-button type="success" size="small" icon="el-icon-download" style="margin-left:auto;" @click="exportData">导出</el-button>
    </div>
    <div style="margin-bottom:10px;" v-if="selectedRows.length > 0">
      <el-button type="danger" size="small" icon="el-icon-delete" @click="batchDelete">批量删除 ({{ selectedRows.length }})</el-button>
      <el-button type="success" size="small" icon="el-icon-check" @click="batchApprove">批量通过审核</el-button>
      <el-button type="warning" size="small" icon="el-icon-close" @click="batchReject">批量拒绝审核</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50"></el-table-column>
      <el-table-column prop="wallId" label="ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showWallDetail(scope.row)">{{ scope.row.wallId | formatId('wall') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="wallContent" label="内容" min-width="250" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showWallDetail(scope.row)">{{ scope.row.wallContent }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="申请人ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showUserDetail(scope.row.userId)">{{ scope.row.userId | formatId('user') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="locked" label="状态" width="80" align="center">
        <template slot-scope="scope"><el-tag :type="scope.row.locked === 1 ? 'danger' : 'success'">{{ scope.row.locked === 1 ? '锁定' : '正常' }}</el-tag></template>
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
      <el-table-column prop="createTime" label="申请时间" width="170">
        <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" :disabled="scope.row.auditState !== 0" @click="handleAudit(scope.row.wallId)">审核</el-button>
          <el-button size="mini" :type="scope.row.locked === 1 ? 'success' : 'warning'" @click="scope.row.locked === 1 ? unlockWall(scope.row.wallId) : lockWall(scope.row.wallId)">{{ scope.row.locked === 1 ? '解锁' : '锁定' }}</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.row.wallId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:15px;" @current-change="handlePageChange" :current-page="pageNum" :page-size.sync="pageSize" :page-sizes="[10, 20, 50]" @size-change="handleSizeChange" :total="total" layout="total, sizes, prev, pager, next"></el-pagination>

    <wall-detail-dialog :visible.sync="wallDetailVisible" :wall-id="wallDetailWallId" @show-user="showUserDetail"></wall-detail-dialog>

    <user-detail-dialog :visible.sync="userDetailVisible" :user-id="userDetailUserId" @show-post="showPostDetail" @show-goods="showGoodsDetail" @show-wall="showWallDetailById"></user-detail-dialog>

    <post-detail-dialog :visible.sync="postDetailVisible" :post-id="postDetailPostId" @show-user="showUserDetail"></post-detail-dialog>

    <goods-detail-dialog :visible.sync="goodsDetailVisible" :goods-id="goodsDetailGoodsId" @show-user="showUserDetail"></goods-detail-dialog>

    
    <el-dialog title="审核表白墙" :visible.sync="auditDialogVisible" width="450px" :close-on-click-modal="false">
      <p style="margin-bottom:15px;color:#666;">请选择审核结果：</p>
      <el-radio-group v-model="auditForm.auditState" style="display:flex;flex-direction:column;gap:10px;">
        <el-radio :label="1">通过</el-radio>
        <el-radio :label="2">拒绝</el-radio>
      </el-radio-group>
      <el-input v-if="auditForm.auditState === 2" v-model="auditForm.auditFailedCause" placeholder="请输入拒绝原因" style="margin-top:10px;" maxlength="100" show-word-limit></el-input>
      <div slot="footer">
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAuditWall" :loading="auditLoading">确定</el-button>
      </div>
    </el-dialog>

    
    <el-dialog title="锁定表白墙" :visible.sync="lockDialogVisible" width="450px" :close-on-click-modal="false">
      <p style="margin-bottom:15px;color:#666;">确定要锁定该表白墙内容吗？请选择锁定原因：</p>
      <el-radio-group v-model="lockCause" style="display:flex;flex-direction:column;gap:10px;">
        <el-radio label="含有违规内容">含有违规内容</el-radio>
        <el-radio label="色情低俗内容">色情低俗内容</el-radio>
        <el-radio label="恶意攻击或辱骂">恶意攻击或辱骂</el-radio>
        <el-radio label="含有广告信息">含有广告信息</el-radio>
        <el-radio label="侵犯他人隐私">侵犯他人隐私</el-radio>
        <el-radio label="其他">其他</el-radio>
      </el-radio-group>
      <el-input v-if="lockCause === '其他'" v-model="lockCauseCustom" placeholder="请输入锁定原因" style="margin-top:10px;" maxlength="50" show-word-limit></el-input>
      <div slot="footer">
        <el-button @click="lockDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmLockWall" :loading="lockLoading">确定锁定</el-button>
      </div>
    </el-dialog>

    
    <el-dialog title="删除表白墙" :visible.sync="deleteDialogVisible" width="450px" :close-on-click-modal="false">
      <p style="margin-bottom:15px;color:#666;">确定要删除该表白墙内容吗？请选择删除原因：</p>
      <el-radio-group v-model="deleteCause" style="display:flex;flex-direction:column;gap:10px;">
        <el-radio label="含有违规内容">含有违规内容</el-radio>
        <el-radio label="色情低俗内容">色情低俗内容</el-radio>
        <el-radio label="恶意攻击或辱骂">恶意攻击或辱骂</el-radio>
        <el-radio label="含有广告信息">含有广告信息</el-radio>
        <el-radio label="侵犯他人隐私">侵犯他人隐私</el-radio>
        <el-radio label="其他">其他</el-radio>
      </el-radio-group>
      <el-input v-if="deleteCause === '其他'" v-model="deleteCauseCustom" placeholder="请输入删除原因" style="margin-top:10px;" maxlength="50" show-word-limit></el-input>
      <div slot="footer">
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDeleteWall">确定删除</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getWallList, auditWall, deleteWall, lockWall, unlockWall } from '@/api/manage'
import UserDetailDialog from '@/components/UserDetailDialog'
import WallDetailDialog from '@/components/WallDetailDialog'
import PostDetailDialog from '@/components/PostDetailDialog'
import GoodsDetailDialog from '@/components/GoodsDetailDialog'
export default {
  /** 注册子组件 */
  components: { UserDetailDialog, WallDetailDialog, PostDetailDialog, GoodsDetailDialog },
  /** 组件数据定义 */
  data() {
    return {
      list: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      selectedRows: [],
      searchForm: { wallContent: '', auditState: null },
      wallDetailVisible: false,
      wallDetailWallId: '',
      userDetailVisible: false,
      userDetailUserId: '',
      postDetailVisible: false,
      postDetailPostId: '',
      goodsDetailVisible: false,
      goodsDetailGoodsId: '',
      auditDialogVisible: false,
      auditWallId: null,
      auditForm: { auditState: 1, auditFailedCause: '' },
      auditLoading: false,
      lockDialogVisible: false,
      lockWallId: null,
      lockCause: '含有违规内容',
      lockCauseCustom: '',
      lockLoading: false,
      deleteDialogVisible: false,
      deleteWallId: null,
      deleteCause: '含有违规内容',
      deleteCauseCustom: ''
    }
  },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadData() },
  /** 组件方法定义 */
  methods: {
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      const res = await getWallList({ pageNum: this.pageNum, pageSize: this.pageSize, ...this.searchForm })
      if (res.code === 200) { this.list = res.data.records || []; this.total = res.data.total || 0 }
      this.loading = false
    },
    /** 显示表白墙详情弹窗 */
    showWallDetail(row) {
      this.wallDetailWallId = row.wallId
      this.wallDetailVisible = true
    },
    /** 显示用户详情弹窗 */
    showUserDetail(userId) { this.userDetailUserId = userId; this.userDetailVisible = true },
    /** 显示帖子详情弹窗 */
    showPostDetail(postId) { this.postDetailPostId = postId; this.postDetailVisible = true },
    /** 显示商品详情弹窗 */
    showGoodsDetail(goodsId) { this.goodsDetailGoodsId = goodsId; this.goodsDetailVisible = true },
    showWallDetailById(wallId) { this.wallDetailWallId = wallId; this.wallDetailVisible = true },
    /** 处理审核操作 */
    handleAudit(wallId) { this.auditWallId = wallId; this.auditForm = { auditState: 1, auditFailedCause: '' }; this.auditDialogVisible = true },
    async confirmAuditWall() {
      if (this.auditForm.auditState === 2 && !this.auditForm.auditFailedCause.trim()) { this.$message.warning('请输入拒绝原因'); return }
      this.auditLoading = true
      try {
        await auditWall({ wallId: this.auditWallId, auditState: this.auditForm.auditState, auditFailedCause: this.auditForm.auditFailedCause })
        this.$message.success('审核成功')
        this.auditDialogVisible = false
        this.loadData()
      } catch (e) { }
      this.auditLoading = false
    },
    lockWall(wallId) { this.lockWallId = wallId; this.lockCause = '含有违规内容'; this.lockCauseCustom = ''; this.lockDialogVisible = true },
    async confirmLockWall() {
      const cause = this.lockCause === '其他' ? this.lockCauseCustom : this.lockCause
      if (this.lockCause === '其他' && !this.lockCauseCustom.trim()) { this.$message.warning('请输入锁定原因'); return }
      this.lockLoading = true
      try { await lockWall(this.lockWallId, cause); this.$message.success('锁定成功'); this.lockDialogVisible = false; this.loadData() } catch (e) { }
      this.lockLoading = false
    },
    async unlockWall(wallId) { await unlockWall(wallId); this.$message.success('解锁成功'); this.loadData() },
    /** 表格选中行变化回调 */
    handleSelectionChange(val) { this.selectedRows = val },
    /** 批量删除选中记录 */
    batchDelete() {
      if (this.selectedRows.length === 0) return this.$message.warning('请先选择要删除的表白墙')
      this.$confirm('确定要批量删除选中的 ' + this.selectedRows.length + ' 条表白墙吗？', '批量删除', { type: 'warning' }).then(async () => {
        var count = 0
        for (var i = 0; i < this.selectedRows.length; i++) {
          try { await deleteWall(this.selectedRows[i].wallId, '批量删除'); count++ } catch (e) {}
        }
        this.$message.success('成功删除 ' + count + ' 条表白墙')
        this.loadData()
      }).catch(() => {})
    },
    /** 批量通过审核 */
    batchApprove() {
      var items = this.selectedRows.filter(r => r.auditState === 0)
      if (items.length === 0) return this.$message.warning('选中的表白墙中没有待审核的')
      this.$confirm('确定要批量通过选中的 ' + items.length + ' 条表白墙吗？', '批量通过审核', { type: 'warning' }).then(async () => {
        var count = 0
        for (var i = 0; i < items.length; i++) {
          try { await auditWall({ wallId: items[i].wallId, auditState: 1 }); count++ } catch (e) {}
        }
        this.$message.success('成功通过 ' + count + ' 条表白墙')
        this.loadData()
      }).catch(() => {})
    },
    /** 批量拒绝审核 */
    batchReject() {
      var items = this.selectedRows.filter(r => r.auditState === 0)
      if (items.length === 0) return this.$message.warning('选中的表白墙中没有待审核的')
      this.$confirm('确定要批量拒绝选中的 ' + items.length + ' 条表白墙吗？', '批量拒绝审核', { type: 'warning' }).then(async () => {
        var count = 0
        for (var i = 0; i < items.length; i++) {
          try { await auditWall({ wallId: items[i].wallId, auditState: 2, auditFailedCause: '批量拒绝' }); count++ } catch (e) {}
        }
        this.$message.success('成功拒绝 ' + count + ' 条表白墙')
        this.loadData()
      }).catch(() => {})
    },
    /** 分页页码变化回调 */
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    /** 每页条数变化回调 */
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    /** 删除记录 */
    handleDelete(wallId) {
      this.deleteWallId = wallId
      this.deleteCause = '含有违规内容'
      this.deleteCauseCustom = ''
      this.deleteDialogVisible = true
    },
    async confirmDeleteWall() {
      const cause = this.deleteCause === '其他' ? this.deleteCauseCustom : this.deleteCause
      if (this.deleteCause === '其他' && !this.deleteCauseCustom.trim()) { this.$message.warning('请输入删除原因'); return }
      try {
        const res = await deleteWall(this.deleteWallId, cause)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.deleteDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(res.msg || '删除失败')
        }
      } catch (e) {}
    },
    /** 重置搜索条件并重新加载数据 */
    resetSearch() {
      this.searchForm = { wallContent: '', auditState: null }
      this.loadData()
    },
    /** 导出数据为CSV文件 */
    exportData() {
      if (this.list.length === 0) { this.$message.warning('没有可导出的数据'); return }
      const auditMap = { 0: '待审核', 1: '已通过', 2: '未通过' }
      const columns = [
        { prop: 'wallId', label: '墙ID' },
        { prop: 'wallContent', label: '内容' },
        { prop: 'userId', label: '申请人ID' },
        { prop: 'auditState', label: '审核状态' },
        { prop: 'createTime', label: '申请时间' }
      ]
      const headers = columns.map(c => c.label).join(',')
      const rows = this.list.map(row => columns.map(c => {
        const val = row[c.prop]
        if (c.prop === 'auditState') return auditMap[val] || '未知'
        return val != null ? String(val) : ''
      }).join(',')).join('\n')
      const csv = '\uFEFF' + headers + '\n' + rows
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '墙数据.csv'
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
