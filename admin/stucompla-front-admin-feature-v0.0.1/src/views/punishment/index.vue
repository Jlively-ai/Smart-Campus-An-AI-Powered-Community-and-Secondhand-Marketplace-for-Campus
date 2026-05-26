<!--
  组件名：PunishmentIndex
  功能描述：处罚管理页面
  主要职责：展示处罚记录列表，支持搜索、解除处罚、导出等操作
-->
<template>
  <div>
    <!-- 搜索筛选区域 -->
   
    <el-card shadow="never" class="search-card">
      <div class="search-bar">
        <el-input v-model="searchForm.userId" placeholder="搜索用户ID" prefix-icon="el-icon-search" class="search-input" size="small" clearable @clear="loadData" @keyup.enter.native="loadData"></el-input>
        <el-select v-model="searchForm.type" placeholder="处罚类型" class="search-select" size="small" clearable @change="loadData">
          <el-option label="禁言" value="mute"></el-option>
          <el-option label="封号" value="ban"></el-option>
          <el-option label="警告" value="warning"></el-option>
        </el-select>
        <el-select v-model="searchForm.targetType" placeholder="目标类型" class="search-select" size="small" clearable @change="loadData">
          <el-option label="帖子" value="post"></el-option>
          <el-option label="帖子评论" value="comment"></el-option>
          <el-option label="商品" value="goods"></el-option>
          <el-option label="商品评价" value="goods_comment"></el-option>
          <el-option label="表白墙" value="wall"></el-option>
        </el-select>
        <el-select v-model="searchForm.status" placeholder="处罚状态" class="search-select" size="small" clearable @change="loadData">
          <el-option label="生效中" :value="0"></el-option>
          <el-option label="已解除" :value="1"></el-option>
          <el-option label="已过期" :value="2"></el-option>
        </el-select>
        <el-select v-model="sortField" placeholder="排序" size="small" style="width:120px;" clearable @change="handleSortChange">
          <el-option label="生效时间 ↓" value="startTime_desc" />
          <el-option label="生效时间 ↑" value="startTime_asc" />
          <el-option label="解除时间 ↓" value="endTime_desc" />
          <el-option label="解除时间 ↑" value="endTime_asc" />
          <el-option label="创建时间 ↓" value="createTime_desc" />
          <el-option label="创建时间 ↑" value="createTime_asc" />
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
        <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        <el-button type="success" size="small" icon="el-icon-download" class="btn-export" @click="exportData">导出</el-button>
      </div>
    </el-card>
    <!-- 操作工具栏 -->
   

    <div class="toolbar">
      <el-button type="warning" size="small" icon="el-icon-unlock" :disabled="selectedRows.length === 0" @click="batchLift">批量解除</el-button>
    </div>
    <!-- 数据表格 -->
   

    <el-table :data="list" v-loading="loading" border @selection-change="handleSelectionChange" class="data-table">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="punishmentId" label="处罚ID" width="80" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.punishmentId | formatId('punishment') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="用户ID" width="120" align="center" show-overflow-tooltip>
        <template slot-scope="scope"><el-link type="primary" @click="showUserDetail(scope.row.userId)">{{ scope.row.userId | formatId('user') }}</el-link></template>
      </el-table-column>
      <el-table-column prop="type" label="处罚类型" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="typeTagType(scope.row.type)">{{ typeMap[scope.row.type] || scope.row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="处罚原因" min-width="150" show-overflow-tooltip></el-table-column>
      <el-table-column prop="targetType" label="目标类型" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" type="info">{{ targetTypeMap[scope.row.targetType] || scope.row.targetType || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="生效时间" width="170">
        <template slot-scope="scope">{{ scope.row.startTime | formatTime }}</template>
      </el-table-column>
      <el-table-column prop="endTime" label="解除时间" width="170">
        <template slot-scope="scope">{{ scope.row.endTime | formatTime }}</template>
      </el-table-column>
      <el-table-column prop="status" label="处罚状态" width="90" align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusMap[scope.row.status] || '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="appealState" label="申诉状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.appealState === null || scope.row.appealState === undefined" type="info">-</el-tag>
          <el-tag v-else-if="scope.row.appealState === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.appealState === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.appealState === 2" type="danger">已驳回</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handlerId" label="处理人" width="120" align="center" show-overflow-tooltip>
        <template slot-scope="scope">{{ scope.row.handlerId || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="danger" :disabled="scope.row.status !== 0" @click="liftPunishment(scope.row.punishmentId)">解除处罚</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
   
    <el-pagination class="pagination-bar" @current-change="handlePageChange" :current-page="pageNum" :page-size.sync="pageSize" :page-sizes="[10, 20, 50]" @size-change="handleSizeChange" :total="total" layout="total, sizes, prev, pager, next"></el-pagination>

    
    <el-dialog title="处罚详情" :visible.sync="detailVisible" width="700px" top="5vh" class="punish-dialog">
      <div v-if="detailData">
        <el-card shadow="hover" class="info-card">
          <div slot="header" class="info-header"><i class="el-icon-warning"></i>处罚信息</div>
          <table class="dt-table">
            <tr>
              <td class="dt-label">处罚ID</td><td>{{ detailData.punishmentId | formatId('punishment') }}</td>
              <td class="dt-label">处罚类型</td><td><el-tag :type="typeTagType(detailData.type)">{{ typeMap[detailData.type] || detailData.type }}</el-tag></td>
            </tr>
            <tr>
              <td class="dt-label">用户ID</td><td><el-link type="primary" @click="showUserDetail(detailData.userId)">{{ detailData.userId | formatId('user') }}</el-link></td>
              <td class="dt-label">处罚状态</td><td><el-tag :type="statusTagType(detailData.status)">{{ statusMap[detailData.status] || '未知' }}</el-tag></td>
            </tr>
            <tr>
              <td class="dt-label">处罚原因</td><td colspan="3" style="white-space:pre-wrap;">{{ detailData.reason || '-' }}</td>
            </tr>
            <tr>
              <td class="dt-label">生效时间</td><td>{{ detailData.startTime | formatTime }}</td>
              <td class="dt-label">解除时间</td><td>{{ detailData.endTime | formatTime }}</td>
            </tr>
            <tr>
              <td class="dt-label">处理人ID</td><td>{{ detailData.handlerId || '-' }}</td>
              <td class="dt-label">创建时间</td><td>{{ detailData.createTime | formatTime }}</td>
            </tr>
          </table>
        </el-card>
        <el-card v-if="detailData.appealState !== null && detailData.appealState !== undefined" shadow="hover" class="info-card">
          <div slot="header" class="info-header"><i class="el-icon-chat-line-square"></i>申诉信息</div>
          <table class="dt-table">
            <tr>
              <td class="dt-label">申诉原因</td><td colspan="3" style="white-space:pre-wrap;">{{ detailData.appealReason || '-' }}</td>
            </tr>
            <tr>
              <td class="dt-label">申诉状态</td>
              <td>
                <el-tag v-if="detailData.appealState === 0" type="warning">待审核</el-tag>
                <el-tag v-else-if="detailData.appealState === 1" type="success">已通过</el-tag>
                <el-tag v-else-if="detailData.appealState === 2" type="danger">已驳回</el-tag>
              </td>
              <td class="dt-label">申诉时间</td><td>{{ detailData.appealTime | formatTime }}</td>
            </tr>
            <tr>
              <td class="dt-label">申诉结果</td><td colspan="3" style="white-space:pre-wrap;">{{ detailData.appealResult || '-' }}</td>
            </tr>
          </table>
        </el-card>
      </div>
    </el-dialog>

    <user-detail-dialog :visible.sync="userDetailVisible" :user-id="userDetailUserId" @show-post="showPostDetail" @show-goods="showGoodsDetail" @show-wall="showWallDetail"></user-detail-dialog>
    <post-detail-dialog :visible.sync="postDetailVisible" :post-id="postDetailPostId" @show-user="showUserDetail"></post-detail-dialog>
    <goods-detail-dialog :visible.sync="goodsDetailVisible" :goods-id="goodsDetailGoodsId" @show-user="showUserDetail"></goods-detail-dialog>
    <wall-detail-dialog :visible.sync="wallDetailVisible" :wall-id="wallDetailWallId" @show-user="showUserDetail"></wall-detail-dialog>
  </div>
</template>
<script>
import { getPunishmentList, liftPunishment } from '@/api/manage'
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
      list: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      searchForm: { userId: '', type: '', targetType: '', status: null },
      sortField: '',
      loading: false,
      selectedRows: [],
      detailVisible: false, detailData: null,
      userDetailVisible: false, userDetailUserId: '',
      postDetailVisible: false, postDetailPostId: '',
      goodsDetailVisible: false, goodsDetailGoodsId: '',
      wallDetailVisible: false, wallDetailWallId: '',
      typeMap: { mute: '禁言', ban: '封号', warning: '警告' },
      statusMap: { 0: '生效中', 1: '已解除', 2: '已过期' },
      targetTypeMap: { post: '帖子', comment: '帖子评论', goods: '商品', goods_comment: '商品评价', wall: '表白墙' }
    }
  },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadData() },
  /** 组件方法定义 */
  methods: {
    typeTagType(type) {
      if (type === 'mute') return 'warning'
      if (type === 'ban') return 'danger'
      if (type === 'warning') return 'info'
      return ''
    },
    statusTagType(status) {
      if (status === 0) return 'success'
      if (status === 1) return 'info'
      if (status === 2) return 'info'
      return ''
    },
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      const params = { pageNum: this.pageNum, pageSize: this.pageSize, ...this.searchForm }
      const res = await getPunishmentList(params)
      if (res.code === 200) { this.list = res.data.records || []; this.total = res.data.total || 0 }
      this.loading = false
    },
    async liftPunishment(punishmentId) {
      try { await this.$confirm('确定解除该处罚？', '提示', { type: 'warning' }) } catch { return }
      try {
        await liftPunishment(punishmentId)
        this.$message.success('解除成功')
        this.loadData()
      } catch (e) { }
    },
    /** 表格选中行变化回调 */
    handleSelectionChange(val) { this.selectedRows = val },
    async batchLift() {
      if (this.selectedRows.length === 0) return
      var canLift = this.selectedRows.filter(function(row) { return row.status === 0 })
      if (canLift.length === 0) { this.$message.warning('选中的处罚中没有生效中的记录'); return }
      try { await this.$confirm('确定批量解除选中的处罚？', '提示', { type: 'warning' }) } catch { return }
      for (var i = 0; i < canLift.length; i++) { await liftPunishment(canLift[i].punishmentId) }
      this.$message.success('批量解除成功'); this.selectedRows = []; this.loadData()
    },
    /** 分页页码变化回调 */
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    /** 每页条数变化回调 */
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    /** 显示详情弹窗 */
    showDetail(row) { this.detailData = row; this.detailVisible = true },
    /** 显示用户详情弹窗 */
    showUserDetail(userId) { this.userDetailUserId = userId; this.userDetailVisible = true },
    /** 显示帖子详情弹窗 */
    showPostDetail(postId) { this.postDetailPostId = postId; this.postDetailVisible = true },
    /** 显示商品详情弹窗 */
    showGoodsDetail(goodsId) { this.goodsDetailGoodsId = goodsId; this.goodsDetailVisible = true },
    /** 显示表白墙详情弹窗 */
    showWallDetail(wallId) { this.wallDetailWallId = wallId; this.wallDetailVisible = true },
    /** 重置搜索条件并重新加载数据 */
    resetSearch() {
      this.searchForm = { userId: '', type: '', targetType: '', status: null }
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
        { prop: 'punishmentId', label: '处罚ID' },
        { prop: 'userId', label: '用户ID' },
        { prop: 'type', label: '处罚类型' },
        { prop: 'reason', label: '原因' },
        { prop: 'startTime', label: '生效时间' },
        { prop: 'endTime', label: '解除时间' },
        { prop: 'status', label: '状态' },
        { prop: 'handlerId', label: '处理人' },
        { prop: 'createTime', label: '创建时间' }
      ]
      const headers = columns.map(c => c.label).join(',')
      const rows = this.list.map(row => columns.map(c => {
        const val = row[c.prop]
        if (c.prop === 'type') return this.typeMap[val] || val
        if (c.prop === 'status') return this.statusMap[val] || '未知'
        return val != null ? String(val) : ''
      }).join(',')).join('\n')
      const csv = '\uFEFF' + headers + '\n' + rows
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '处罚数据.csv'
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
.punish-dialog .info-card {
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
</style>
