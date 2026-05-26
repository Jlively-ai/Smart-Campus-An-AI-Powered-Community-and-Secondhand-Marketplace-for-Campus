<!--
  组件名：GoodsIndex
  功能描述：商品管理页面
  主要职责：展示商品列表，支持搜索、审核、上架/下架、删除、导出等操作
-->
<template>
  <div>
    <!-- 搜索筛选区域 -->
   
    <el-card shadow="never" class="search-card">
      <div class="search-bar">
        <el-input v-model="searchForm.goodsName" placeholder="搜索商品名称" prefix-icon="el-icon-search" class="search-input" size="small" clearable @clear="loadData" @keyup.enter.native="loadData"></el-input>
        <el-select v-model="searchForm.goodsCategoryId" placeholder="筛选分类" class="search-select" size="small" clearable @change="loadData">
          <el-option label="分类1" value="1"></el-option>
          <el-option label="分类2" value="2"></el-option>
          <el-option label="分类3" value="3"></el-option>
        </el-select>
        <el-select v-model="searchForm.goodsStatus" placeholder="筛选状态" class="search-select" size="small" clearable @change="loadData">
          <el-option label="在售" :value="true"></el-option>
          <el-option label="已下架" :value="false"></el-option>
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
      <el-button type="success" size="small" icon="el-icon-top" @click="batchShelve">批量上架</el-button>
      <el-button type="warning" size="small" icon="el-icon-bottom" @click="batchUnshelve">批量下架</el-button>
    </div>
    <!-- 数据表格 -->
   

    <el-table :data="list" v-loading="loading" border @selection-change="handleSelectionChange" class="data-table">
      <el-table-column type="selection" width="50"></el-table-column>
      <el-table-column prop="goodsId" label="ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.goodsId | formatId('goods') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="goodsName" label="商品名称" min-width="180" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.goodsName }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="goodsPrice" label="价格" width="90">
        <template slot-scope="scope"><span style="color:#f56c6c;font-weight:600;">￥{{ scope.row.goodsPrice }}</span></template>
      </el-table-column>
      <el-table-column prop="goodsCount" label="库存" width="70" align="center"></el-table-column>
      <el-table-column prop="goodsStatus" label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.locked === 1" type="danger">已锁定</el-tag>
          <el-tag v-else-if="scope.row.goodsStatus" type="success">上架</el-tag>
          <el-tag v-else type="info">下架</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditState" label="审核" width="90" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.auditState === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.auditState === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.auditState === 2" type="danger">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewNum" label="浏览量" width="70" align="center"></el-table-column>
      <el-table-column prop="shareNum" label="分享数" width="70" align="center"></el-table-column>
      <el-table-column prop="userId" label="卖家ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showUserDetail(scope.row.userId)">{{ scope.row.userId | formatId('user') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="170">
        <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" :disabled="scope.row.auditState !== 0" @click="handleAuditGoods(scope.row.goodsId)">审核</el-button>
          <el-button size="mini" :type="scope.row.locked === 1 ? 'success' : 'warning'" @click="scope.row.locked === 1 ? unlockGoods(scope.row.goodsId) : lockGoods(scope.row.goodsId)">{{ scope.row.locked === 1 ? '解锁' : '锁定' }}</el-button>
          <el-button size="mini" type="danger" @click="handleDeleteGoods(scope.row.goodsId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
   
    <el-pagination class="pagination-bar" @current-change="handlePageChange" :current-page="pageNum" :page-size.sync="pageSize" :page-sizes="[10, 20, 50]" @size-change="handleSizeChange" :total="total" layout="total, sizes, prev, pager, next"></el-pagination>

    <goods-detail-dialog :visible.sync="goodsDetailVisible" :goods-id="goodsDetailGoodsId" @show-user="showUserDetail"></goods-detail-dialog>

    <user-detail-dialog :visible.sync="userDetailVisible" :user-id="userDetailUserId" @show-post="showPostDetail" @show-goods="showDetailById" @show-wall="showWallDetail" />

    <post-detail-dialog :visible.sync="postDetailVisible" :post-id="postDetailPostId" @show-user="showUserDetail"></post-detail-dialog>

    <wall-detail-dialog :visible.sync="wallDetailVisible" :wall-id="wallDetailWallId" @show-user="showUserDetail"></wall-detail-dialog>

    
    <el-dialog title="删除商品" :visible.sync="deleteDialogVisible" width="450px" :close-on-click-modal="false" class="confirm-dialog">
      <div class="confirm-body">
        <div class="confirm-icon danger"><i class="el-icon-warning-outline"></i></div>
        <div class="confirm-title">确定要删除该商品吗？</div>
        <div class="confirm-sub">请选择删除原因：</div>
        <el-radio-group v-model="deleteCause" class="confirm-options">
          <el-radio label="商品信息不实">商品信息不实</el-radio>
          <el-radio label="含有违规内容">含有违规内容</el-radio>
          <el-radio label="含有广告信息">含有广告信息</el-radio>
          <el-radio label="商品已下架/失效">商品已下架/失效</el-radio>
          <el-radio label="侵犯他人权益">侵犯他人权益</el-radio>
          <el-radio label="其他">其他</el-radio>
        </el-radio-group>
        <el-input v-if="deleteCause === '其他'" v-model="deleteCauseCustom" placeholder="请输入删除原因" style="margin-top:10px;" maxlength="50" show-word-limit></el-input>
      </div>
      <div slot="footer">
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDeleteGoods" :loading="deleteLoading">确定删除</el-button>
      </div>
    </el-dialog>

    
    <el-dialog title="锁定商品" :visible.sync="lockDialogVisible" width="450px" :close-on-click-modal="false" class="confirm-dialog">
      <div class="confirm-body">
        <div class="confirm-icon warning"><i class="el-icon-lock"></i></div>
        <div class="confirm-title">确定要锁定该商品吗？</div>
        <div class="confirm-sub">请选择锁定原因：</div>
        <el-radio-group v-model="lockCause" class="confirm-options">
          <el-radio label="商品信息不实">商品信息不实</el-radio>
          <el-radio label="含有违规内容">含有违规内容</el-radio>
          <el-radio label="含有广告信息">含有广告信息</el-radio>
          <el-radio label="价格异常">价格异常</el-radio>
          <el-radio label="侵犯他人权益">侵犯他人权益</el-radio>
          <el-radio label="其他">其他</el-radio>
        </el-radio-group>
        <el-input v-if="lockCause === '其他'" v-model="lockCauseCustom" placeholder="请输入锁定原因" style="margin-top:10px;" maxlength="50" show-word-limit></el-input>
      </div>
      <div slot="footer">
        <el-button @click="lockDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmLockGoods" :loading="lockLoading">确定锁定</el-button>
      </div>
    </el-dialog>

    
    <el-dialog title="审核商品" :visible.sync="auditDialogVisible" width="450px" :close-on-click-modal="false" class="confirm-dialog">
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
        <el-button type="primary" @click="confirmAuditGoods" :loading="auditLoading">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getGoodsList, lockGoods, unlockGoods, deleteGoods, auditGoods, unShelveGoods } from '@/api/manage'
import UserDetailDialog from '@/components/UserDetailDialog'
import GoodsDetailDialog from '@/components/GoodsDetailDialog'
import PostDetailDialog from '@/components/PostDetailDialog'
import WallDetailDialog from '@/components/WallDetailDialog'
export default {
  /** 注册子组件 */
  components: { UserDetailDialog, GoodsDetailDialog, PostDetailDialog, WallDetailDialog },
  /** 组件数据定义 */
  data() { return { list: [], pageNum: 1, pageSize: 10, total: 0, searchForm: { goodsName: '', goodsCategoryId: null, goodsStatus: null, auditState: null }, loading: false, selectedRows: [], goodsDetailVisible: false, goodsDetailGoodsId: '', userDetailVisible: false, userDetailUserId: '', postDetailVisible: false, postDetailPostId: '', wallDetailVisible: false, wallDetailWallId: '', deleteDialogVisible: false, deleteGoodsId: null, deleteCause: '商品信息不实', deleteCauseCustom: '', deleteLoading: false, lockDialogVisible: false, lockGoodsId: null, lockCause: '商品信息不实', lockCauseCustom: '', lockLoading: false, auditDialogVisible: false, auditGoodsId: null, auditForm: { auditState: 1, auditFailedCause: '' }, auditLoading: false } },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadData() },
  /** 组件方法定义 */
  methods: {
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      const res = await getGoodsList({ pageNum: this.pageNum, pageSize: this.pageSize, ...this.searchForm })
      if (res.code === 200) { this.list = res.data.records || []; this.total = res.data.total || 0 }
      this.loading = false
    },
    lockGoods(goodsId) { this.lockGoodsId = goodsId; this.lockCause = '商品信息不实'; this.lockCauseCustom = ''; this.lockDialogVisible = true },
    async confirmLockGoods() {
      const cause = this.lockCause === '其他' ? this.lockCauseCustom : this.lockCause
      if (this.lockCause === '其他' && !this.lockCauseCustom.trim()) { this.$message.warning('请输入锁定原因'); return }
      this.lockLoading = true
      try { await lockGoods(this.lockGoodsId, cause); this.$message.success('锁定成功'); this.lockDialogVisible = false; this.loadData() } catch (e) { }
      this.lockLoading = false
    },
    async unlockGoods(goodsId) { await unlockGoods(goodsId); this.$message.success('解锁成功'); this.loadData() },
    handleDeleteGoods(goodsId) { this.deleteGoodsId = goodsId; this.deleteCause = '商品信息不实'; this.deleteCauseCustom = ''; this.deleteDialogVisible = true },
    async confirmDeleteGoods() {
      const cause = this.deleteCause === '其他' ? this.deleteCauseCustom : this.deleteCause
      if (this.deleteCause === '其他' && !this.deleteCauseCustom.trim()) { this.$message.warning('请输入删除原因'); return }
      this.deleteLoading = true
      try { await deleteGoods(this.deleteGoodsId, cause); this.$message.success('删除成功'); this.deleteDialogVisible = false; this.loadData() } catch (e) { }
      this.deleteLoading = false
    },
    handleAuditGoods(goodsId) { this.auditGoodsId = goodsId; this.auditForm = { auditState: 1, auditFailedCause: '' }; this.auditDialogVisible = true },
    async confirmAuditGoods() {
      if (this.auditForm.auditState === 2 && !this.auditForm.auditFailedCause.trim()) { this.$message.warning('请输入拒绝原因'); return }
      this.auditLoading = true
      try { await auditGoods({ goodsId: this.auditGoodsId, auditState: this.auditForm.auditState, auditFailedCause: this.auditForm.auditFailedCause }); this.$message.success('审核成功'); this.auditDialogVisible = false; this.loadData() } catch (e) { }
      this.auditLoading = false
    },
    /** 表格选中行变化回调 */
    handleSelectionChange(val) { this.selectedRows = val },
    /** 批量删除选中记录 */
    batchDelete() {
      if (this.selectedRows.length === 0) return this.$message.warning('请先选择要删除的商品')
      this.$confirm('确定要批量删除选中的 ' + this.selectedRows.length + ' 个商品吗？', '批量删除', { type: 'warning' }).then(async () => {
        var count = 0
        for (var i = 0; i < this.selectedRows.length; i++) {
          try { await deleteGoods(this.selectedRows[i].goodsId, '批量删除'); count++ } catch (e) {}
        }
        this.$message.success('成功删除 ' + count + ' 个商品')
        this.loadData()
      }).catch(() => {})
    },
    batchShelve() {
      var items = this.selectedRows.filter(r => !r.goodsStatus && r.locked !== 1)
      if (items.length === 0) return this.$message.warning('选中的商品中没有可上架的')
      this.$confirm('确定要批量上架选中的 ' + items.length + ' 个商品吗？', '批量上架', { type: 'warning' }).then(async () => {
        var count = 0
        for (var i = 0; i < items.length; i++) {
          try { await unlockGoods(items[i].goodsId); count++ } catch (e) {}
        }
        this.$message.success('成功上架 ' + count + ' 个商品')
        this.loadData()
      }).catch(() => {})
    },
    batchUnshelve() {
      var items = this.selectedRows.filter(r => r.goodsStatus)
      if (items.length === 0) return this.$message.warning('选中的商品中没有可下架的')
      this.$confirm('确定要批量下架选中的 ' + items.length + ' 个商品吗？', '批量下架', { type: 'warning' }).then(async () => {
        var count = 0
        for (var i = 0; i < items.length; i++) {
          try { await unShelveGoods(items[i].goodsId); count++ } catch (e) {}
        }
        this.$message.success('成功下架 ' + count + ' 个商品')
        this.loadData()
      }).catch(() => {})
    },
    /** 分页页码变化回调 */
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    /** 每页条数变化回调 */
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    /** 显示详情弹窗 */
    showDetail(row) { this.goodsDetailGoodsId = row.goodsId; this.goodsDetailVisible = true },
    /** 根据ID显示详情弹窗 */
    showDetailById(goodsId) { this.goodsDetailGoodsId = goodsId; this.goodsDetailVisible = true },
    /** 显示用户详情弹窗 */
    showUserDetail(userId) { this.userDetailUserId = userId; this.userDetailVisible = true },
    /** 显示帖子详情弹窗 */
    showPostDetail(postId) { this.postDetailPostId = postId; this.postDetailVisible = true },
    /** 显示表白墙详情弹窗 */
    showWallDetail(wallId) { this.wallDetailWallId = wallId; this.wallDetailVisible = true },
    /** 重置搜索条件并重新加载数据 */
    resetSearch() {
      this.searchForm = { goodsName: '', goodsCategoryId: null, goodsStatus: null, auditState: null }
      this.loadData()
    },
    /** 导出数据为CSV文件 */
    exportData() {
      if (this.list.length === 0) { this.$message.warning('没有可导出的数据'); return }
      const columns = [
        { prop: 'goodsId', label: '商品ID' },
        { prop: 'goodsName', label: '商品名称' },
        { prop: 'goodsPrice', label: '价格' },
        { prop: 'goodsCount', label: '库存' },
        { prop: 'goodsStatus', label: '状态' },
        { prop: 'userId', label: '卖家ID' },
        { prop: 'createTime', label: '发布时间' }
      ]
      const headers = columns.map(c => c.label).join(',')
      const rows = this.list.map(row => columns.map(c => {
        const val = row[c.prop]
        if (c.prop === 'goodsStatus') return val ? '在售' : '已下架'
        return val != null ? String(val) : ''
      }).join(',')).join('\n')
      const csv = '\uFEFF' + headers + '\n' + rows
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '商品数据.csv'
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
.confirm-icon.warning {
  background: linear-gradient(135deg, #fdf6ec, #faecd8);
  color: #E6A23C;
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
