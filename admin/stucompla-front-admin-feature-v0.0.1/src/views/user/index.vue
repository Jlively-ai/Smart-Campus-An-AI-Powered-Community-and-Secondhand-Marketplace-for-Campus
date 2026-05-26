<!--
  组件名：UserIndex
  功能描述：用户管理页面
  主要职责：展示用户列表，支持搜索、锁定/解锁、重置密码、处罚、导出等操作
-->
<template>
  <div>
    <!-- 搜索筛选区域 -->
   
    <el-card shadow="never" class="search-card">
      <div class="search-bar">
        <el-input v-model="searchForm.username" placeholder="搜索用户名/昵称" prefix-icon="el-icon-search" class="search-input" size="small" clearable @clear="loadData" @keyup.enter.native="loadData"></el-input>
        <el-select v-model="searchForm.status" placeholder="筛选状态" class="search-select" size="small" clearable @change="loadData">
          <el-option label="正常" :value="0"></el-option>
          <el-option label="禁言" :value="3"></el-option>
          <el-option label="锁定" :value="1"></el-option>
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
        <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        <el-button type="success" size="small" icon="el-icon-download" class="btn-export" @click="exportData">导出</el-button>
      </div>
    </el-card>
    <!-- 操作工具栏 -->
   

    <div class="toolbar">
      <el-button type="danger" size="small" icon="el-icon-lock" :disabled="selectedRows.length === 0" @click="batchLock">批量锁定</el-button>
      <el-button type="success" size="small" icon="el-icon-unlock" :disabled="selectedRows.length === 0" @click="batchUnlock">批量解锁</el-button>
    </div>
    <!-- 数据表格 -->
   

    <el-table :data="list" v-loading="loading" border @selection-change="handleSelectionChange" class="data-table">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="userId" label="ID" width="180" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.userId | formatId('user') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" width="120" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.username }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="昵称" width="120" show-overflow-tooltip></el-table-column>
      <el-table-column prop="sex" label="性别" width="60" align="center"></el-table-column>
      <el-table-column prop="phone" label="手机" width="120" show-overflow-tooltip></el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template slot-scope="scope"><el-tag :type="scope.row.status === 0 ? 'success' : scope.row.status === 3 ? 'warning' : 'danger'">{{ scope.row.status === 0 ? '正常' : scope.row.status === 3 ? '禁言' : '锁定' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" sortable>
        <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="240" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button v-if="scope.row.status === 0" size="mini" type="danger" @click="lockUser(scope.row.userId)">锁定</el-button>
          <el-button v-if="scope.row.status !== 0" size="mini" type="success" @click="unlockUser(scope.row.userId)">{{ scope.row.status === 3 ? '解除禁言' : '解锁' }}</el-button>
          <el-button size="mini" type="warning" @click="resetPwd(scope.row.userId)">重置密码</el-button>
          <el-button size="mini" type="info" @click="openPunishDialog(scope.row)">处罚</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
   
    <el-pagination class="pagination-bar" @current-change="handlePageChange" :current-page="pageNum" :page-size.sync="pageSize" :page-sizes="[10, 20, 50]" @size-change="handleSizeChange" :total="total" layout="total, sizes, prev, pager, next"></el-pagination>

    <user-detail-dialog :visible.sync="sharedUserDetailVisible" :user-id="sharedUserDetailUserId" @show-post="showPostDetail" @show-goods="showGoodsDetail" @show-wall="showWallDetail" />
    <post-detail-dialog :visible.sync="postDetailVisible" :post-id="postDetailPostId" @show-user="showDetailById"></post-detail-dialog>
    <goods-detail-dialog :visible.sync="goodsDetailVisible" :goods-id="goodsDetailGoodsId" @show-user="showDetailById"></goods-detail-dialog>
    <wall-detail-dialog :visible.sync="wallDetailVisible" :wall-id="wallDetailWallId" @show-user="showDetailById"></wall-detail-dialog>

    <el-dialog title="处罚用户" :visible.sync="punishDialogVisible" width="500px" class="punish-dialog">
      <div class="punish-user-info">
        <el-avatar :size="48" style="background: linear-gradient(135deg, #F56C6C, #ff8e8e); color: #fff; font-size: 20px; margin-right: 12px;">{{ (punishUser.nickname || punishUser.username || '用')[0] }}</el-avatar>
        <div>
          <div class="punish-user-name">{{ punishUser.nickname || punishUser.username }}</div>
          <div class="punish-user-id">{{ punishUser.userId | formatId('user') }}</div>
        </div>
      </div>
      <el-form :model="punishForm" label-width="80px" class="punish-form">
        <el-form-item label="处罚类型">
          <el-select v-model="punishForm.type" style="width:100%;">
            <el-option label="禁言" value="mute"></el-option>
            <el-option label="封号" value="ban"></el-option>
            <el-option label="警告" value="warning"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="原因"><el-input v-model="punishForm.reason" type="textarea" :rows="3" placeholder="请输入处罚原因"></el-input></el-form-item>
        <el-form-item label="解除时间"><el-date-picker v-model="punishForm.endTime" type="datetime" placeholder="选择解除时间" style="width:100%;" value-format="yyyy-MM-dd HH:mm:ss"></el-date-picker></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="punishDialogVisible = false">取消</el-button><el-button type="primary" @click="submitPunish">确定处罚</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getUserList, lockedUser, unLockUser, resetUserPwd, createPunishment } from '@/api/manage'
import UserDetailDialog from '@/components/UserDetailDialog'
import PostDetailDialog from '@/components/PostDetailDialog'
import GoodsDetailDialog from '@/components/GoodsDetailDialog'
import WallDetailDialog from '@/components/WallDetailDialog'
export default {
  /** 注册子组件 */
  components: { UserDetailDialog, PostDetailDialog, GoodsDetailDialog, WallDetailDialog },
  /** 组件数据定义 */
  data() { return { list: [], pageNum: 1, pageSize: 10, total: 0, searchForm: { username: '', status: null }, loading: false, selectedRows: [], sharedUserDetailVisible: false, sharedUserDetailUserId: '', punishDialogVisible: false, punishUser: {}, punishForm: { type: 'mute', reason: '', endTime: '' }, postDetailVisible: false, postDetailPostId: '', goodsDetailVisible: false, goodsDetailGoodsId: '', wallDetailVisible: false, wallDetailWallId: '' } },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadData() },
  /** 组件方法定义 */
  methods: {
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      const params = { pageNum: this.pageNum, pageSize: this.pageSize, ...this.searchForm }
      const res = await getUserList(params)
      if (res.code === 200) { this.list = res.data.records || []; this.total = res.data.total || 0 }
      this.loading = false
    },
    /** 锁定用户 */
    async lockUser(userId) { try { await this.$confirm('确定锁定该用户？', '提示', { type: 'warning' }) } catch { return } await lockedUser(userId, '管理员锁定'); this.$message.success('锁定成功'); this.loadData() },
    /** 解锁用户 */
    async unlockUser(userId) { await unLockUser(userId); this.$message.success('解锁成功'); this.loadData() },
    /** 表格选中行变化回调 */
    handleSelectionChange(val) { this.selectedRows = val },
    /** 批量锁定 */
    async batchLock() {
      if (this.selectedRows.length === 0) return
      try { await this.$confirm('确定批量锁定选中的用户？', '提示', { type: 'warning' }) } catch { return }
      for (var i = 0; i < this.selectedRows.length; i++) { await lockedUser(this.selectedRows[i].userId, '管理员批量锁定') }
      this.$message.success('批量锁定成功'); this.selectedRows = []; this.loadData()
    },
    /** 批量解锁 */
    async batchUnlock() {
      if (this.selectedRows.length === 0) return
      try { await this.$confirm('确定批量解锁选中的用户？', '提示', { type: 'warning' }) } catch { return }
      for (var i = 0; i < this.selectedRows.length; i++) { await unLockUser(this.selectedRows[i].userId) }
      this.$message.success('批量解锁成功'); this.selectedRows = []; this.loadData()
    },
    /** 重置密码 */
    async resetPwd(userId) { try { await this.$confirm('确定重置该用户密码为123456？', '提示') } catch { return } await resetUserPwd({ newPassword: '123456', secondPassword: '123456', userId }); this.$message.success('重置成功') },
    /** 打开处罚弹窗 */
    openPunishDialog(row) { this.punishUser = row; this.punishForm = { type: 'mute', reason: '', endTime: '' }; this.punishDialogVisible = true },
    /** 提交处罚 */
    async submitPunish() {
      if (!this.punishForm.reason) return this.$message.warning('请填写处罚原因')
      const res = await createPunishment({ userId: this.punishUser.userId, type: this.punishForm.type, reason: this.punishForm.reason, endTime: this.punishForm.endTime })
      if (res.code === 200) { this.$message.success('处罚成功'); this.punishDialogVisible = false; this.loadData() }
      else this.$message.error(res.msg || '处罚失败')
    },
    /** 分页页码变化回调 */
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    /** 每页条数变化回调 */
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    /** 显示详情弹窗 */
    showDetail(row) { this.sharedUserDetailUserId = row.userId; this.sharedUserDetailVisible = true },
    /** 根据ID显示详情弹窗 */
    showDetailById(userId) { this.sharedUserDetailUserId = userId; this.sharedUserDetailVisible = true },
    /** 显示帖子详情弹窗 */
    showPostDetail(postId) { this.postDetailPostId = postId; this.postDetailVisible = true },
    /** 显示商品详情弹窗 */
    showGoodsDetail(goodsId) { this.goodsDetailGoodsId = goodsId; this.goodsDetailVisible = true },
    /** 显示表白墙详情弹窗 */
    showWallDetail(wallId) { this.wallDetailWallId = wallId; this.wallDetailVisible = true },
    /** 重置搜索条件并重新加载数据 */
    resetSearch() {
      this.searchForm = { username: '', status: null }
      this.loadData()
    },
    /** 导出数据为CSV文件 */
    exportData() {
      if (this.list.length === 0) { this.$message.warning('没有可导出的数据'); return }
      const columns = [
        { prop: 'userId', label: '用户ID' },
        { prop: 'username', label: '用户名' },
        { prop: 'nickname', label: '昵称' },
        { prop: 'sex', label: '性别' },
        { prop: 'phone', label: '手机' },
        { prop: 'status', label: '状态' },
        { prop: 'createTime', label: '注册时间' }
      ]
      const headers = columns.map(c => c.label).join(',')
      const rows = this.list.map(row => columns.map(c => {
        const val = row[c.prop]
        if (c.prop === 'status') return val === 0 ? '正常' : '锁定'
        return val != null ? String(val) : ''
      }).join(',')).join('\n')
      const csv = '\uFEFF' + headers + '\n' + rows
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '用户数据.csv'
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
.punish-dialog .punish-user-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #fef0f0, #fff);
  border-radius: 12px;
}
.punish-user-name {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}
.punish-user-id {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.punish-form >>> .el-form-item__label {
  font-weight: 500;
}
</style>
