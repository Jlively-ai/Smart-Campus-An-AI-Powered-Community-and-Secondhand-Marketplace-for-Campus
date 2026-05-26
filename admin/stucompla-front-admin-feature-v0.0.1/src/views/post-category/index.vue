<!--
  组件名：PostCategoryIndex
  功能描述：帖子分类管理页面
  主要职责：展示帖子分类列表，支持新增、编辑、删除分类等操作
-->
<template>
  <div>
    <div style="margin-bottom:15px;display:flex;gap:10px;">
      <el-input v-model="newCategoryName" placeholder="新分类名称" style="width:200px;" clearable></el-input>
      <el-button type="success" icon="el-icon-plus" @click="handleAdd">添加分类</el-button>
    </div>
    <div style="margin-bottom:12px;display:flex;gap:8px;">
      <el-button type="danger" size="small" icon="el-icon-delete" :disabled="selectedRows.length === 0" @click="batchDelete">批量删除</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="categoryId" label="ID" width="60" align="center"></el-table-column>
      <el-table-column prop="categoryName" label="分类名称" min-width="200"></el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template slot-scope="scope"><el-button size="mini" type="danger" @click="handleDelete(scope.row.categoryId)">删除</el-button></template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script>
import request from '@/utils/request'
export default {
  /** 组件数据定义 */
  data() { return { list: [], loading: false, selectedRows: [], newCategoryName: '' } },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadData() },
  /** 组件方法定义 */
  methods: {
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      const res = await request({ url: '/category/list', method: 'get' })
      if (res.code === 200) this.list = res.data || []
      this.loading = false
    },
    /** 新增记录 */
    async handleAdd() {
      if (!this.newCategoryName) return this.$message.warning('请输入分类名称')
      await request({ url: '/category/add', method: 'post', params: { categoryName: this.newCategoryName } })
      this.$message.success('添加成功'); this.newCategoryName = ''; this.loadData()
    },
    /** 删除记录 */
    async handleDelete(categoryId) {
      try { await this.$confirm('确定删除该分类？', '提示', { type: 'warning' }) } catch { return }
      await request({ url: '/category/delete', method: 'delete', params: { categoryId } })
      this.$message.success('删除成功'); this.loadData()
    },
    /** 表格选中行变化回调 */
    handleSelectionChange(val) { this.selectedRows = val },
    /** 批量删除选中记录 */
    async batchDelete() {
      if (this.selectedRows.length === 0) return
      try { await this.$confirm('确定批量删除选中的分类？', '提示', { type: 'warning' }) } catch { return }
      for (var i = 0; i < this.selectedRows.length; i++) { await request({ url: '/category/delete', method: 'delete', params: { categoryId: this.selectedRows[i].categoryId } }) }
      this.$message.success('批量删除成功'); this.selectedRows = []; this.loadData()
    }
  }
}
</script>
<style scoped>
/* 组件局部样式 */

.page-container { padding: 20px; background: #f5f7fa; min-height: 100%; }

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
