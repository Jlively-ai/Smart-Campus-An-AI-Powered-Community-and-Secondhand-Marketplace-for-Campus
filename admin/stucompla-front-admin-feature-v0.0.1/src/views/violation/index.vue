<!--
  组件名：ViolationIndex
  功能描述：违规管理页面
  主要职责：展示违规记录列表，支持搜索、处理（确认/驳回）、新增违规、导出等操作
-->
<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabChange">
      <el-tab-pane label="违规删除" name="delete">
        <div class="filter-container">
          <el-input v-model="searchUserId" placeholder="用户ID" size="small" style="width:150px;" clearable @keyup.enter.native="loadData" />
          <el-select v-model="filterType" placeholder="类型" size="small" style="width:100px;" clearable @change="loadData">
            <el-option label="帖子" value="post"></el-option>
            <el-option label="帖子评论" value="comment"></el-option>
            <el-option label="商品" value="goods"></el-option>
            <el-option label="商品评价" value="goods_comment"></el-option>
            <el-option label="表白墙" value="wall"></el-option>
          </el-select>
          <el-select v-model="filterAppeal" placeholder="申诉状态" size="small" style="width:110px;" clearable @change="loadData">
            <el-option label="未申诉" :value="0"></el-option>
            <el-option label="申诉中" :value="1"></el-option>
            <el-option label="已通过" :value="2"></el-option>
            <el-option label="已驳回" :value="3"></el-option>
          </el-select>
          <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
          <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        </div>

        <el-table :data="list" v-loading="loading" border size="small" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center"></el-table-column>
          <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
          <el-table-column prop="userId" label="用户ID" width="150" show-overflow-tooltip>
            <template slot-scope="scope">
              <el-link type="primary" @click="$emit('show-user', scope.row.userId)">{{ scope.row.userId }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="itemType" label="类型" width="90" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row.itemType === 'post' ? 'primary' : scope.row.itemType === 'goods' ? 'warning' : scope.row.itemType === 'wall' ? 'danger' : 'info'">
                {{ { post: '帖子', comment: '帖子评论', goods: '商品', goods_comment: '商品评价', wall: '表白墙' }[scope.row.itemType] || scope.row.itemType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="preview" label="内容预览" min-width="200">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="showContentPreview(scope.row)">{{ scope.row.preview || '查看内容' }}</el-button>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="原因" width="150" show-overflow-tooltip />
          <el-table-column prop="appealState" label="申诉状态" width="90" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row.appealState === 0 ? 'info' : scope.row.appealState === 1 ? 'warning' : scope.row.appealState === 2 ? 'success' : 'danger'">
                {{ ['未申诉', '申诉中', '已通过', '已驳回'][scope.row.appealState] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="170" align="center">
            <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template slot-scope="scope">
              <el-button v-if="scope.row.appealState === 1" type="primary" size="mini" @click="openHandleDialog(scope.row)">处理申诉</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination style="margin-top:15px;text-align:center;" @current-change="handlePageChange" :current-page="pageNum" :page-size.sync="pageSize" :page-sizes="[10, 20, 50]" @size-change="handleSizeChange" :total="total" layout="total, sizes, prev, pager, next" />
      </el-tab-pane>

      <el-tab-pane label="锁定记录" name="lock">
        <div class="filter-container">
          <el-input v-model="searchUserId" placeholder="用户ID" size="small" style="width:150px;" clearable @keyup.enter.native="loadData" />
          <el-select v-model="filterType" placeholder="类型" size="small" style="width:100px;" clearable @change="loadData">
            <el-option label="帖子" value="post"></el-option>
            <el-option label="帖子评论" value="comment"></el-option>
            <el-option label="商品" value="goods"></el-option>
            <el-option label="商品评价" value="goods_comment"></el-option>
            <el-option label="表白墙" value="wall"></el-option>
          </el-select>
          <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
          <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        </div>
        <div style="margin-bottom:12px;display:flex;gap:8px;">
          <el-button type="success" size="small" icon="el-icon-unlock" :disabled="selectedRows.length === 0" @click="batchUnlock">批量解锁</el-button>
        </div>

        <el-table :data="list" v-loading="loading" border size="small" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center"></el-table-column>
          <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
          <el-table-column prop="userId" label="用户ID" width="150" show-overflow-tooltip>
            <template slot-scope="scope">
              <el-link type="primary" @click="$emit('show-user', scope.row.userId)">{{ scope.row.userId }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="itemType" label="锁定类型" width="100" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row.itemType === 'post' ? 'primary' : scope.row.itemType === 'goods' ? 'warning' : scope.row.itemType === 'wall' ? 'danger' : 'info'">
                {{ { post: '帖子', comment: '评论', goods: '商品', goods_comment: '商品评价', wall: '表白墙' }[scope.row.itemType] || scope.row.itemType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="preview" label="锁定内容" min-width="200">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="showContentPreview(scope.row)">{{ scope.row.preview || '查看内容' }}</el-button>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="锁定原因" width="150" show-overflow-tooltip />
          <el-table-column prop="lockStatus" label="锁定状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row.appealState === 2 ? 'success' : 'warning'">
                {{ scope.row.appealState === 2 ? '已解锁' : '锁定中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="锁定时间" width="170" align="center">
            <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center">
            <template slot-scope="scope">
              <el-button type="success" size="mini" @click="unlockItem(scope.row)">解锁</el-button>
              <el-button v-if="scope.row.appealState === 1" type="primary" size="mini" @click="openHandleDialog(scope.row)">处理申诉</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination style="margin-top:15px;text-align:center;" @current-change="handlePageChange" :current-page="pageNum" :page-size.sync="pageSize" :page-sizes="[10, 20, 50]" @size-change="handleSizeChange" :total="total" layout="total, sizes, prev, pager, next" />
      </el-tab-pane>

      <el-tab-pane label="申诉处理" name="appeal">
        <div class="filter-container">
          <el-input v-model="searchUserId" placeholder="用户ID" size="small" style="width:150px;" clearable @keyup.enter.native="loadData" />
          <el-select v-model="filterType" placeholder="类型" size="small" style="width:100px;" clearable @change="loadData">
            <el-option label="帖子" value="post"></el-option>
            <el-option label="帖子评论" value="comment"></el-option>
            <el-option label="商品" value="goods"></el-option>
            <el-option label="商品评价" value="goods_comment"></el-option>
            <el-option label="表白墙" value="wall"></el-option>
          </el-select>
          <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
          <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        </div>
        <div style="margin-bottom:12px;display:flex;gap:8px;">
          <el-button type="primary" size="small" icon="el-icon-check" :disabled="selectedRows.length === 0" @click="batchHandleAppeal">批量处理申诉</el-button>
        </div>

        <el-table :data="list" v-loading="loading" border size="small" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center"></el-table-column>
          <el-table-column prop="userId" label="用户ID" width="150" show-overflow-tooltip>
            <template slot-scope="scope">
              <el-link type="primary" @click="$emit('show-user', scope.row.userId)">{{ scope.row.userId }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="itemType" label="类型" width="90" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row.itemType === 'post' ? 'primary' : scope.row.itemType === 'goods' ? 'warning' : scope.row.itemType === 'wall' ? 'danger' : 'info'">
                {{ { post: '帖子', comment: '帖子评论', goods: '商品', goods_comment: '商品评价', wall: '表白墙' }[scope.row.itemType] || scope.row.itemType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operationType" label="操作类型" width="90" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row.operationType === 'delete' ? 'danger' : 'warning'">
                {{ scope.row.operationType === 'delete' ? '删除' : '锁定' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="preview" label="内容预览" min-width="200">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="showContentPreview(scope.row)">{{ scope.row.preview || '查看内容' }}</el-button>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="原因" width="150" show-overflow-tooltip />
          <el-table-column prop="appealReason" label="申诉理由" width="150" show-overflow-tooltip />
          <el-table-column prop="createTime" label="时间" width="170" align="center">
            <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template slot-scope="scope">
              <el-button type="primary" size="mini" @click="openHandleDialog(scope.row)">处理申诉</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination style="margin-top:15px;text-align:center;" @current-change="handlePageChange" :current-page="pageNum" :page-size.sync="pageSize" :page-sizes="[10, 20, 50]" @size-change="handleSizeChange" :total="total" layout="total, sizes, prev, pager, next" />
      </el-tab-pane>
    </el-tabs>

    
    <el-dialog title="处理申诉" :visible.sync="handleDialogVisible" width="500px">
      <el-form label-width="100px">
        <el-form-item label="内容类型">{{ { post: '帖子', comment: '帖子评论', goods: '商品', goods_comment: '商品评价', wall: '表白墙' }[handleTarget.itemType] || handleTarget.itemType }}</el-form-item>
        <el-form-item label="操作类型">{{ handleTarget.operationType === 'delete' ? '违规删除' : '锁定' }}</el-form-item>
        <el-form-item label="内容预览">
          <el-button size="mini" type="text" @click="showContentPreview(handleTarget)">{{ handleTarget.preview || '查看完整内容' }}</el-button>
        </el-form-item>
        <el-form-item label="原因">{{ handleTarget.reason || '-' }}</el-form-item>
        <el-form-item label="申诉理由">{{ handleTarget.appealReason || '-' }}</el-form-item>
        <el-form-item label="处理结果">
          <el-radio-group v-model="handleResult">
            <el-radio :label="2">通过（恢复内容）</el-radio>
            <el-radio :label="3">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="handleNote" type="textarea" :rows="3" placeholder="请填写处理说明"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确定</el-button>
      </span>
    </el-dialog>

    
    <el-dialog title="内容预览" :visible.sync="contentPreviewVisible" width="700px" top="5vh" append-to-body>
      <div v-loading="contentPreviewLoading">
        <div v-if="contentPreviewData">
          <el-card shadow="hover">
            <div slot="header" style="font-weight:bold;">
              <i :class="contentPreviewType === 'post' ? 'el-icon-document' : contentPreviewType === 'goods' ? 'el-icon-goods' : contentPreviewType === 'wall' ? 'el-icon-star-off' : 'el-icon-chat-dot-round'" style="margin-right:6px;"></i>
              {{ { post: '帖子详情', comment: '评论详情', goods: '商品详情', goods_comment: '商品评价详情', wall: '表白墙详情' }[contentPreviewType] || '内容详情' }}
            </div>
            <div v-if="contentPreviewType === 'post'">
              <p style="font-weight:bold;font-size:16px;margin-bottom:10px;">{{ contentPreviewData.title || '-' }}</p>
              <div style="color:#606266;line-height:1.8;white-space:pre-wrap;">{{ contentPreviewData.content || contentPreviewData.text || '-' }}</div>
              <div style="margin-top:10px;color:#999;font-size:12px;">作者ID：{{ contentPreviewData.userId }} | 创建时间：{{ formatTime(contentPreviewData.createTime) }}</div>
            </div>
            <div v-else-if="contentPreviewType === 'goods'">
              <p style="font-weight:bold;font-size:16px;margin-bottom:10px;">{{ contentPreviewData.goodsName || '-' }}</p>
              <p style="color:#F56C6C;font-size:18px;font-weight:bold;">￥{{ contentPreviewData.goodsPrice || 0 }}</p>
              <div style="color:#606266;line-height:1.8;white-space:pre-wrap;">{{ contentPreviewData.goodsDesc || contentPreviewData.description || '-' }}</div>
              <div style="margin-top:10px;color:#999;font-size:12px;">库存：{{ contentPreviewData.goodsCount || 0 }} | 作者ID：{{ contentPreviewData.userId }} | 创建时间：{{ formatTime(contentPreviewData.createTime) }}</div>
            </div>
            <div v-else-if="contentPreviewType === 'wall'">
              <div style="color:#606266;line-height:1.8;white-space:pre-wrap;font-size:15px;">{{ contentPreviewData.content || contentPreviewData.wallContent || '-' }}</div>
              <div style="margin-top:10px;color:#999;font-size:12px;">作者ID：{{ contentPreviewData.userId }} | 创建时间：{{ formatTime(contentPreviewData.createTime) }}</div>
            </div>
            <div v-else-if="contentPreviewType === 'comment' || contentPreviewType === 'goods_comment'">
              <div style="color:#606266;line-height:1.8;white-space:pre-wrap;">{{ contentPreviewData.text || contentPreviewData.content || '-' }}</div>
              <div style="margin-top:10px;color:#999;font-size:12px;">作者ID：{{ contentPreviewData.userId }} | 创建时间：{{ formatTime(contentPreviewData.createTime) }}</div>
            </div>
          </el-card>
        </div>
        <div v-else style="text-align:center;color:#909399;padding:20px;">未找到该内容</div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getViolationList, handleViolationAppeal } from '@/api/violation'
import { getPostList, getGoodsList, getWallList, getCommentList, getGoodsCommentList, unLockPost, unlockGoods, unlockWall, unlockComment, unlockGoodsComment } from '@/api/manage'

export default {
  name: 'ViolationManagement',
  /** 组件数据定义 */
  data() {
    return {
      activeTab: 'delete',
      list: [], total: 0, pageNum: 1, pageSize: 10, loading: false,
      searchUserId: '', filterType: '', filterAppeal: null, selectedRows: [],
      handleDialogVisible: false, handleTarget: {}, handleResult: 3, handleNote: '',
      contentPreviewVisible: false, contentPreviewLoading: false, contentPreviewData: null, contentPreviewType: ''
    }
  },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadData() },
  /** 组件方法定义 */
  methods: {
    formatTime(time) {
      if (!time) return ''
      var d = new Date(time)
      var pad = function(n) { return String(n).padStart(2, '0') }
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    },
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      try {
        var params = { pageNum: this.pageNum, pageSize: this.pageSize }
        if (this.searchUserId) params.userId = this.searchUserId
        if (this.filterType) params.itemType = this.filterType
        if (this.activeTab === 'delete') {
          params.operationType = 'delete'
          if (this.filterAppeal !== null && this.filterAppeal !== '') {
            params.appealState = this.filterAppeal
          }
        } else if (this.activeTab === 'lock') {
          params.operationType = 'lock'
          if (this.filterAppeal !== null && this.filterAppeal !== '') {
            params.appealState = this.filterAppeal
          }
        } else if (this.activeTab === 'appeal') {
          params.appealState = 1
        }
        var res = await getViolationList(params)
        if (res.code === 200) {
          this.list = res.data.records || []
          this.total = res.data.total || 0
        }
      } catch(e) {}
      this.loading = false
    },
    handleTabChange() { this.pageNum = 1; this.selectedRows = []; this.loadData() },
    /** 分页页码变化回调 */
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    /** 每页条数变化回调 */
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    /** 重置搜索条件并重新加载数据 */
    resetSearch() { this.searchUserId = ''; this.filterType = ''; this.filterAppeal = null; this.pageNum = 1; this.loadData() },
    /** 表格选中行变化回调 */
    handleSelectionChange(val) { this.selectedRows = val },
    /** 批量解锁 */
    async batchUnlock() {
      if (this.selectedRows.length === 0) return
      try { await this.$confirm('确定批量解锁选中的内容？', '提示', { type: 'warning' }) } catch { return }
      for (var i = 0; i < this.selectedRows.length; i++) {
        var row = this.selectedRows[i]
        var targetId = row.itemId || row.targetId
        if (!targetId) continue
        try {
          if (row.itemType === 'post') { await unLockPost(targetId) } else if (row.itemType === 'goods') { await unlockGoods(targetId) } else if (row.itemType === 'wall') { await unlockWall(targetId) } else if (row.itemType === 'comment') { await unlockComment(targetId) } else if (row.itemType === 'goods_comment') { await unlockGoodsComment(targetId) }
        } catch (e) {  }
      }
      this.$message.success('批量解锁成功'); this.selectedRows = []; this.loadData()
    },
    async batchHandleAppeal() {
      if (this.selectedRows.length === 0) return
      try { await this.$confirm('确定批量处理选中的申诉（驳回）？', '提示', { type: 'warning' }) } catch (e) { return }
      for (var i = 0; i < this.selectedRows.length; i++) {
        try { await handleViolationAppeal({ id: this.selectedRows[i].id, appealState: 3, appealResult: '批量驳回' }) } catch (e) {  }
      }
      this.$message.success('批量处理成功'); this.selectedRows = []; this.loadData()
    },
    openHandleDialog(row) { this.handleTarget = row; this.handleResult = 3; this.handleNote = ''; this.handleDialogVisible = true },
    async submitHandle() {
      if (!this.handleNote.trim()) return this.$message.warning('请填写处理说明')
      var res = await handleViolationAppeal({ id: this.handleTarget.id, appealState: this.handleResult, appealResult: this.handleNote })
      if (res.code === 200) { this.$message.success('处理成功'); this.handleDialogVisible = false; this.loadData() }
      else this.$message.error(res.msg || '处理失败')
    },
    async unlockItem(row) {
      var targetId = row.itemId || row.targetId
      if (!targetId) return this.$message.warning('无法获取目标ID')
      this.$confirm('确定要解锁该内容吗？', '解锁确认', { type: 'warning' }).then(async () => {
        try {
          if (row.itemType === 'post') {
            await unLockPost(targetId)
          } else if (row.itemType === 'goods') {
            await unlockGoods(targetId)
          } else if (row.itemType === 'wall') {
            await unlockWall(targetId)
          } else if (row.itemType === 'comment') {
            await unlockComment(targetId)
          } else if (row.itemType === 'goods_comment') {
            await unlockGoodsComment(targetId)
          }
          this.$message.success('解锁成功')
          this.loadData()
        } catch (e) {
          this.$message.error('解锁失败')
        }
      }).catch(() => {})
    },
    async showContentPreview(row) {
      this.contentPreviewType = row.itemType
      this.contentPreviewData = null
      this.contentPreviewVisible = true
      this.contentPreviewLoading = true
      try {
        var targetId = row.itemId || row.targetId
        if (row.itemType === 'post') {
          var res = await getPostList({ pageNum: 1, pageSize: 9999 })
          if (res.code === 200) {
            var records = res.data.records || res.data || []
            this.contentPreviewData = records.find(function(p) { return p.postId === targetId }) || null
          }
        } else if (row.itemType === 'goods') {
          var res = await getGoodsList({ pageNum: 1, pageSize: 9999 })
          if (res.code === 200) {
            var records = res.data.records || res.data || []
            this.contentPreviewData = records.find(function(g) { return g.goodsId === targetId }) || null
          }
        } else if (row.itemType === 'wall') {
          var res = await getWallList({ pageNum: 1, pageSize: 9999 })
          if (res.code === 200) {
            var records = res.data.records || res.data || []
            this.contentPreviewData = records.find(function(w) { return w.wallId === targetId }) || null
          }
        } else if (row.itemType === 'comment') {
          var res = await getCommentList({ pageNum: 1, pageSize: 9999 })
          if (res.code === 200) {
            var records = res.data.records || res.data || []
            this.contentPreviewData = records.find(function(c) { return c.commentId === targetId }) || null
          }
        } else if (row.itemType === 'goods_comment') {
          var res = await getGoodsCommentList({ pageNum: 1, pageSize: 9999 })
          if (res.code === 200) {
            var records = res.data.records || res.data || []
            this.contentPreviewData = records.find(function(c) { return c.commentId === targetId }) || null
          }
        }
      } catch (e) { this.contentPreviewData = null }
      this.contentPreviewLoading = false
    }
  }
}
</script>
<style scoped>
/* 组件局部样式 */

.app-container { padding: 20px; background: #f5f7fa; min-height: 100%; }

.filter-container { background: #fff; padding: 16px 20px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); margin-bottom: 16px; display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }

::v-deep .el-table { border-radius: 12px; overflow: hidden; }
::v-deep .el-table th .cell { color: #303133; font-weight: 600; }
::v-deep .el-table .el-button--mini { margin: 0 2px; }

::v-deep .el-tag { border-radius: 6px; }

::v-deep .el-card { border-radius: 12px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); transition: box-shadow 0.3s ease; }
::v-deep .el-card:hover { box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1); }

::v-deep .el-button { transition: all 0.3s ease; }
::v-deep .el-link { transition: color 0.3s ease; }

::v-deep .el-pagination { padding: 16px 0; }

::v-deep .el-tabs__item { font-weight: 600; color: #303133; }

::v-deep .el-dialog__title { font-weight: 600; color: #303133; }
</style>
