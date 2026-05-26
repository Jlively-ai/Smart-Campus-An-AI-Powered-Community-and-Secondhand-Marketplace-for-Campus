<!--
  组件名：RecycleBin
  功能描述：回收站页
  主要职责：
    1. 已删除内容列表
    2. 恢复/彻底删除操作
-->
<template>
  <div v-loading="loading">
    <div style="margin-bottom:15px;">
      <el-page-header @back="$router.back()" content="回收站"></el-page-header>
    </div>
    <el-card shadow="hover">
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
        <span style="font-weight:bold;">🗑️ 回收站（保留30天）</span>
        <span style="color:#999;font-size:13px;">共 {{ total }} 项</span>
      </div>
      <!-- 搜索、筛选、排序 -->
      <div style="margin-bottom:15px;display:flex;flex-wrap:wrap;gap:10px;align-items:center;">
        <el-input v-model="searchKeyword" placeholder="搜索内容关键词" prefix-icon="el-icon-search" size="small" style="width:200px;" clearable @clear="loadData" @keyup.enter.native="loadData"></el-input>
        <el-select v-model="filterType" placeholder="筛选类型" size="small" style="width:120px;" clearable @change="loadData">
          <el-option label="帖子" value="post"></el-option>
          <el-option label="商品" value="goods"></el-option>
          <el-option label="表白墙" value="wall"></el-option>
        </el-select>
        <el-select v-model="sortBy" placeholder="排序字段" size="small" style="width:120px;" @change="loadData">
          <el-option label="删除时间" value="deleteTime"></el-option>
          <el-option label="过期时间" value="expireTime"></el-option>
        </el-select>
        <el-select v-model="sortOrder" placeholder="排序方向" size="small" style="width:100px;" @change="loadData">
          <el-option label="最新优先" value="desc"></el-option>
          <el-option label="最早优先" value="asc"></el-option>
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
        <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
      </div>
      <div v-if="list.length === 0 && !loading" style="text-align:center;color:#999;padding:40px 0;">
        <i class="el-icon-delete" style="font-size:48px;color:#dcdfe6;"></i>
        <div style="margin-top:12px;">回收站是空的</div>
      </div>
      <div v-else>
        <div v-for="item in list" :key="item.id" style="display:flex;align-items:center;padding:14px 0;border-bottom:1px solid #f0f0f0;">
          <div style="flex:1;min-width:0;">
            <div style="display:flex;align-items:center;gap:8px;">
              <el-tag size="mini" :type="item.itemType === 'post' ? 'primary' : item.itemType === 'goods' ? 'warning' : 'danger'">
                {{ item.itemType === 'post' ? '帖子' : item.itemType === 'goods' ? '商品' : '表白墙' }}
              </el-tag>
              <span style="font-size:14px;color:#303133;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{{ item.preview || '未知内容' }}</span>
              <span v-if="item.price" style="color:#F56C6C;font-weight:bold;font-size:13px;">￥{{ item.price }}</span>
            </div>
            <div style="margin-top:6px;font-size:12px;color:#999;">
              <span>删除时间：{{ formatTime(item.deleteTime) }}</span>
              <span style="margin-left:15px;">过期时间：{{ formatTime(item.expireTime) }}</span>
            </div>
          </div>
          <div style="display:flex;gap:8px;flex-shrink:0;margin-left:12px;">
            <el-button type="primary" size="mini" @click="restoreItem(item)">恢复</el-button>
            <el-button type="danger" size="mini" @click="permanentlyDelete(item)">彻底删除</el-button>
          </div>
        </div>
      </div>
      <el-pagination v-if="total > pageSize" style="margin-top:15px;text-align:center;" @current-change="handlePageChange" :current-page="pageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="total" layout="total, sizes, prev, pager, next" small @size-change="handleSizeChange"></el-pagination>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'RecycleBin',
  data() {
    return {
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      loading: false,
      searchKeyword: '',
      filterType: '',
      sortBy: 'deleteTime',
      sortOrder: 'desc'
    }
  },
  created() { this.loadData() },
  methods: {
    formatTime(time) {
      if (!time) return ''
      var d = new Date(time)
      var pad = function(n) { return String(n).padStart(2, '0') }
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    },
    async loadData() {
      this.loading = true
      try {
        var params = { pageNum: this.pageNum, pageSize: this.pageSize, sortBy: this.sortBy, sortOrder: this.sortOrder }
        if (this.searchKeyword) params.keyword = this.searchKeyword
        if (this.filterType) params.itemType = this.filterType
        var res = await this.$axios.get('/recycle-bin/list', { params: params })
        if (res.code === 200) {
          this.list = res.data.records || []
          this.total = res.data.total || 0
        }
      } catch (e) {}
      this.loading = false
    },
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    resetSearch() {
      this.searchKeyword = ''
      this.filterType = ''
      this.sortBy = 'deleteTime'
      this.sortOrder = 'desc'
      this.pageNum = 1
      this.loadData()
    },
    async restoreItem(item) {
      try { await this.$confirm('确定恢复该内容？恢复后可在原列表中查看。', '提示', { type: 'info' }) } catch(e) { return }
      try {
        var res = await this.$axios.post('/recycle-bin/restore/' + item.id)
        if (res.code === 200) { this.$message.success('已恢复'); this.loadData() }
        else this.$message.error(res.msg || '恢复失败')
      } catch(e) { this.$message.error('恢复失败') }
    },
    async permanentlyDelete(item) {
      try { await this.$confirm('彻底删除后将无法恢复，确定继续？', '警告', { type: 'warning' }) } catch(e) { return }
      try {
        var res = await this.$axios.delete('/recycle-bin/' + item.id)
        if (res.code === 200) { this.$message.success('已彻底删除'); this.loadData() }
        else this.$message.error(res.msg || '删除失败')
      } catch(e) { this.$message.error('删除失败') }
    }
  }
}
</script>

<style scoped>
.recycle-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  border-radius: 8px;
  transition: background 0.3s ease;
}
.recycle-item:hover {
  background: #f5f7fa;
}
.recycle-item:last-child { border-bottom: none; }
</style>
