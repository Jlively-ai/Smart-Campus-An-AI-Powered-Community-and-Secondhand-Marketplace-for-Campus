<!--
  组件名：AdminIndex
  功能描述：管理员管理页面
  主要职责：展示管理员列表，支持添加、删除、权限设置、导出等操作
-->
<template>
  <div>
    <!-- 搜索筛选区域 -->
   
    <el-card shadow="never" class="search-card">
      <div class="search-bar">
        <el-input v-model="searchForm.username" placeholder="搜索管理员" prefix-icon="el-icon-search" class="search-input" size="small" clearable @clear="loadData" @keyup.enter.native="loadData"></el-input>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
        <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        <el-button type="success" size="small" icon="el-icon-plus" @click="addDialogVisible = true">添加管理员</el-button>
        <el-button type="success" size="small" icon="el-icon-download" @click="exportData">导出</el-button>
      </div>
    </el-card>
    <!-- 操作工具栏 -->
   

    <div class="toolbar">
      <el-button type="danger" size="small" icon="el-icon-delete" :disabled="selectedRows.length === 0" @click="batchDelete">批量删除</el-button>
    </div>
    <!-- 数据表格 -->
   

    <el-table :data="list" v-loading="loading" border @selection-change="handleSelectionChange" class="data-table">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="adminId" label="ID" width="200" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.adminId | formatId(scope.row.roleId === 1 ? 'root' : 'admin') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" width="150"></el-table-column>
      <el-table-column prop="roleId" label="角色" width="100" align="center">
        <template slot-scope="scope"><el-tag :type="scope.row.roleId === 1 ? 'danger' : ''">{{ scope.row.roleId === 1 ? '超级管理员' : '管理员' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="permissions" label="权限" min-width="200">
        <template slot-scope="scope">
          <template v-if="scope.row.permissions">
            <el-tag v-for="p in scope.row.permissions.split(',')" :key="p" size="mini" style="margin:2px;">{{ permissionLabel(p) }}</el-tag>
          </template>
          <span v-else style="color:#999;">无权限</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" sortable>
        <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="warning" @click="openPermDialog(scope.row)" :disabled="scope.row.roleId === 1">权限</el-button>
          <el-button size="mini" type="danger" @click="deleteAdmin(scope.row.adminId)" :disabled="scope.row.roleId === 1">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
   
    <el-pagination class="pagination-bar" @current-change="handlePageChange" :current-page="pageNum" :page-size.sync="pageSize" :page-sizes="[10, 20, 50]" @size-change="handleSizeChange" :total="total" layout="total, sizes, prev, pager, next"></el-pagination>

    <el-dialog title="添加管理员" :visible.sync="addDialogVisible" width="400px" class="admin-dialog">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="addForm.username" placeholder="请输入用户名"></el-input></el-form-item>
        <el-form-item label="密码"><el-input v-model="addForm.password" type="password" placeholder="请输入密码"></el-input></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="addForm.roleId" style="width:100%;">
            <el-option label="管理员" :value="2"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="addDialogVisible = false">取消</el-button><el-button type="primary" @click="handleAdd">确定</el-button></span>
    </el-dialog>

    <el-dialog title="管理员详情" :visible.sync="detailVisible" width="700px" top="5vh" class="admin-dialog">
      <div v-if="detailData">
        <el-card shadow="hover" class="detail-card">
          <div slot="header" class="detail-header"><i class="el-icon-user"></i>管理员信息</div>
          <table class="dt-table">
            <tr>
              <td class="dt-label">管理员ID</td><td>{{ detailData.adminId | formatId('admin') }}</td>
              <td class="dt-label">用户名</td><td>{{ detailData.username }}</td>
            </tr>
            <tr>
              <td class="dt-label">角色</td><td><el-tag :type="detailData.roleId === 1 ? 'danger' : ''">{{ detailData.roleId === 1 ? '超级管理员' : '管理员' }}</el-tag></td>
              <td class="dt-label">创建时间</td><td>{{ detailData.createTime | formatTime }}</td>
            </tr>
            <tr>
              <td class="dt-label">权限</td>
              <td colspan="3">
                <template v-if="detailData.permissions">
                  <el-tag v-for="p in detailData.permissions.split(',')" :key="p" size="mini" style="margin:2px;">{{ permissionLabel(p) }}</el-tag>
                </template>
                <span v-else style="color:#999;">无权限</span>
              </td>
            </tr>
          </table>
        </el-card>
      </div>
    </el-dialog>

    <el-dialog title="设置权限" :visible.sync="permDialogVisible" width="600px" class="perm-dialog">
      <div class="perm-admin-info">
        <el-avatar :size="40" style="background: linear-gradient(135deg, #409EFF, #79bbff); color: #fff; margin-right: 10px;">{{ (permAdmin.username || '管')[0] }}</el-avatar>
        <div>
          <div class="perm-admin-name">{{ permAdmin.username }}</div>
          <div class="perm-admin-role"><el-tag size="mini">{{ permAdmin.roleId === 1 ? '超级管理员' : '管理员' }}</el-tag></div>
        </div>
      </div>
      <el-checkbox-group v-model="permCheckList">
        <div v-for="group in permissionGroups" :key="group.name" class="perm-group">
          <div class="perm-group-title">
            <i :class="group.icon"></i>{{ group.name }}
          </div>
          <el-checkbox v-for="p in group.items" :key="p.key" :label="p.key" class="perm-checkbox">{{ p.label }}</el-checkbox>
        </div>
      </el-checkbox-group>
      <span slot="footer"><el-button @click="permDialogVisible = false">取消</el-button><el-button type="primary" @click="savePermissions">保存</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getAdminList, addAdmin, deleteAdmin, changePermissions, getPermissionList } from '@/api/manage'
export default {
  /** 组件数据定义 */
  data() { return { list: [], pageNum: 1, pageSize: 10, total: 0, searchForm: { username: '' }, loading: false, selectedRows: [], addDialogVisible: false, addForm: { username: '', password: '', roleId: 2 }, detailVisible: false, detailData: {}, permDialogVisible: false, permAdmin: {}, permCheckList: [], allPermissions: [] } },
  /** 计算属性定义 */
  computed: {
    permissionGroups() {
      const groupMap = {
        '系统管理': { icon: 'el-icon-s-tools', keys: ['user_manage', 'announcement_manage', 'stats_view'] },
        '社区管理': { icon: 'el-icon-s-comment', keys: ['post_manage', 'comment_manage', 'wall_manage'] },
        '交易管理': { icon: 'el-icon-s-shop', keys: ['goods_manage', 'order_manage'] },
        '安全管理': { icon: 'el-icon-s-check', keys: ['report_manage', 'punishment_manage', 'appeal_manage'] },
        'AI管理': { icon: 'el-icon-cpu', keys: ['ai_manage'] }
      }
      const result = []
      for (const [name, config] of Object.entries(groupMap)) {
        const items = this.allPermissions.filter(p => config.keys.includes(p.key))
        if (items.length > 0) {
          result.push({ name, icon: config.icon, items })
        }
      }
      
      const groupedKeys = Object.values(groupMap).flatMap(g => g.keys)
      const ungrouped = this.allPermissions.filter(p => !groupedKeys.includes(p.key))
      if (ungrouped.length > 0) {
        result.push({ name: '其他权限', icon: 'el-icon-setting', items: ungrouped })
      }
      return result
    }
  },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadData(); this.loadPermissions() },
  /** 组件方法定义 */
  methods: {
    /** 获取权限中文名 */
    permissionLabel(key) {
      const found = this.allPermissions.find(p => p.key === key)
      return found ? found.label : key
    },
    /** 加载权限列表 */
    async loadPermissions() {
      const res = await getPermissionList()
      if (res.code === 200) this.allPermissions = res.data || []
    },
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      const res = await getAdminList({ pageNum: this.pageNum, pageSize: this.pageSize, ...this.searchForm })
      if (res.code === 200) { this.list = res.data.records || []; this.total = res.data.total || 0 }
      this.loading = false
    },
    /** 新增记录 */
    async handleAdd() { await addAdmin(this.addForm.username, this.addForm.password, this.addForm.roleId); this.$message.success('添加成功'); this.addDialogVisible = false; this.addForm = { username: '', password: '', roleId: 2 }; this.loadData() },
    /** 删除管理员 */
    async deleteAdmin(adminId) { try { await this.$confirm('确定删除？', '提示', { type: 'warning' }) } catch { return } await deleteAdmin(adminId); this.$message.success('删除成功'); this.loadData() },
    /** 表格选中行变化回调 */
    handleSelectionChange(val) { this.selectedRows = val },
    /** 批量删除选中记录 */
    async batchDelete() {
      if (this.selectedRows.length === 0) return
      var canDelete = this.selectedRows.filter(function(row) { return row.roleId !== 1 })
      if (canDelete.length === 0) { this.$message.warning('选中的均为超级管理员，无法删除'); return }
      try { await this.$confirm('确定批量删除选中的管理员？（超级管理员不会被删除）', '提示', { type: 'warning' }) } catch { return }
      for (var i = 0; i < canDelete.length; i++) { await deleteAdmin(canDelete[i].adminId) }
      this.$message.success('批量删除成功'); this.selectedRows = []; this.loadData()
    },
    /** 显示详情弹窗 */
    showDetail(row) { this.detailData = row; this.detailVisible = true },
    /** 打开权限设置弹窗 */
    openPermDialog(row) {
      this.permAdmin = row
      this.permCheckList = row.permissions ? row.permissions.split(',') : []
      this.permDialogVisible = true
    },
    /** 保存权限设置 */
    async savePermissions() {
      const res = await changePermissions({ adminId: this.permAdmin.adminId, permissions: this.permCheckList.join(',') })
      if (res.code === 200) { this.$message.success('权限设置成功'); this.permDialogVisible = false; this.loadData() }
      else this.$message.error(res.msg || '设置失败')
    },
    /** 分页页码变化回调 */
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    /** 每页条数变化回调 */
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    /** 重置搜索条件并重新加载数据 */
    resetSearch() {
      this.searchForm = { username: '' }
      this.loadData()
    },
    /** 导出数据为CSV文件 */
    exportData() {
      if (this.list.length === 0) { this.$message.warning('没有可导出的数据'); return }
      const columns = [
        { prop: 'adminId', label: '管理员ID' },
        { prop: 'username', label: '用户名' },
        { prop: 'roleId', label: '角色' },
        { prop: 'permissions', label: '权限' },
        { prop: 'createTime', label: '创建时间' }
      ]
      const headers = columns.map(c => c.label).join(',')
      const rows = this.list.map(row => columns.map(c => {
        const val = row[c.prop]
        if (c.prop === 'roleId') return val === 1 ? '超级管理员' : '管理员'
        return val != null ? String(val) : ''
      }).join(',')).join('\n')
      const csv = '\uFEFF' + headers + '\n' + rows
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '管理员数据.csv'
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
.detail-card {
  border-radius: 16px;
}
.detail-header {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}
.dt-table { width: 100%; border-collapse: collapse; border-color: #ebeef5; }
.dt-table td { padding: 10px 12px; border: 1px solid #ebeef5; font-size: 13px; color: #303133; }
.dt-label { background: #f8fafc; font-weight: 600; width: 100px; text-align: right; color: #4b5563; white-space: nowrap; }

.perm-dialog .perm-admin-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #ecf5ff, #fff);
  border-radius: 12px;
}
.perm-admin-name {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}
.perm-admin-role {
  margin-top: 4px;
}
.perm-group {
  margin-bottom: 18px;
}
.perm-group-title {
  font-weight: 600;
  font-size: 14px;
  color: #374151;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  align-items: center;
  gap: 6px;
}
.perm-group-title i {
  color: #409EFF;
  font-size: 16px;
}
.perm-checkbox {
  width: 45%;
  margin-left: 0;
  margin-bottom: 8px;
  padding: 6px 10px;
  border-radius: 8px;
  transition: background 0.2s ease;
}
.perm-checkbox:hover {
  background: #f8fafc;
}
</style>
