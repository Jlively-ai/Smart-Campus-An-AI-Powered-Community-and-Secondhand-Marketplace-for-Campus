<!--
  组件名：UserDetailDialog
  功能描述：用户详情弹窗组件
  主要职责：展示用户详细信息，包括基本资料、帖子、商品、表白墙等关联数据
-->
<template>
  <el-dialog title="用户详情" :visible.sync="dialogVisible" width="900px" top="5vh" append-to-body @close="onClose">
    <div v-loading="loading">
      <div v-if="userData">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-user" style="margin-right:6px;"></i>基本信息</div>
          <div style="display:flex;gap:20px;align-items:flex-start;">
            <div style="flex-shrink:0;text-align:center;">
              <el-avatar v-if="userData.avatar" :src="userData.avatar" :size="80"></el-avatar>
              <el-avatar v-else :size="80" style="background:#409EFF;font-size:28px;">{{ (userData.nickname || userData.username || '用')[0] }}</el-avatar>
              <div style="margin-top:8px;"><el-tag :type="userData.status === 0 ? 'success' : userData.status === 3 ? 'warning' : 'danger'">{{ userData.status === 0 ? '正常' : userData.status === 3 ? '禁言' : '锁定' }}</el-tag></div>
            </div>
            <table class="dt-table" style="flex:1;">
              <tr><td class="dt-label">用户ID</td><td>{{ userData.userId | formatId('user') }}</td></tr>
              <tr><td class="dt-label">用户名</td><td>{{ userData.username || '-' }}</td></tr>
              <tr><td class="dt-label">昵称</td><td>{{ userData.nickname || '-' }}</td></tr>
              <tr><td class="dt-label">性别</td><td>{{ userData.sex || '-' }}</td></tr>
              <tr><td class="dt-label">手机号</td><td>{{ userData.phone || '-' }}</td></tr>
              <tr><td class="dt-label">邮箱</td><td>{{ userData.email || '-' }}</td></tr>
              <tr><td class="dt-label">个性签名</td><td>{{ userData.signature || '-' }}</td></tr>
              <tr><td class="dt-label">注册时间</td><td>{{ userData.createTime | formatTime }}</td></tr>
              <tr><td class="dt-label">粉丝数</td><td>{{ userFollowerCount }}</td></tr>
              <tr><td class="dt-label">关注数</td><td>{{ userFollowingCount }}</td></tr>
            </table>
          </div>
        </el-card>
        <el-card shadow="hover">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-folder-opened" style="margin-right:6px;"></i>发布内容</div>
          <el-tabs v-model="activeTab">
            <el-tab-pane :label="'帖子(' + userPosts.length + ')'" name="post">
              <el-table :data="userPosts" v-loading="tabLoading" size="small" border>
                <el-table-column prop="postId" label="帖子ID" width="200" show-overflow-tooltip>
                  <template slot-scope="scope"><el-link type="primary" @click="$emit('show-post', scope.row.postId)">{{ scope.row.postId | formatId('post') }}</el-link></template>
                </el-table-column>
                <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip></el-table-column>
                <el-table-column prop="commentNum" label="评论" width="60" align="center"></el-table-column>
                <el-table-column prop="viewNum" label="浏览" width="60" align="center"></el-table-column>
                <el-table-column prop="postStatus" label="状态" width="70" align="center">
                  <template slot-scope="scope"><el-tag :type="scope.row.postStatus === 0 ? 'success' : 'danger'" size="mini">{{ scope.row.postStatus === 0 ? '正常' : '锁定' }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="createTime" label="发布时间" width="160">
                  <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane :label="'商品(' + userGoods.length + ')'" name="goods">
              <el-table :data="userGoods" v-loading="tabLoading" size="small" border>
                <el-table-column prop="goodsId" label="商品ID" width="200" show-overflow-tooltip>
                  <template slot-scope="scope"><el-link type="primary" @click="$emit('show-goods', scope.row.goodsId)">{{ scope.row.goodsId | formatId('goods') }}</el-link></template>
                </el-table-column>
                <el-table-column prop="goodsName" label="名称" min-width="150" show-overflow-tooltip></el-table-column>
                <el-table-column prop="goodsPrice" label="价格" width="80">
                  <template slot-scope="scope"><span style="color:#f56c6c;">￥{{ scope.row.goodsPrice }}</span></template>
                </el-table-column>
                <el-table-column prop="goodsCount" label="库存" width="60" align="center"></el-table-column>
                <el-table-column prop="goodsStatus" label="状态" width="70" align="center">
                  <template slot-scope="scope"><el-tag :type="scope.row.goodsStatus ? 'success' : 'info'" size="mini">{{ scope.row.goodsStatus ? '在售' : '下架' }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="createTime" label="发布时间" width="160">
                  <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane :label="'表白墙(' + userWalls.length + ')'" name="wall">
              <el-table :data="userWalls" v-loading="tabLoading" size="small" border>
                <el-table-column prop="wallId" label="ID" width="200" show-overflow-tooltip>
                  <template slot-scope="scope"><el-link type="primary" @click="$emit('show-wall', scope.row.wallId)">{{ scope.row.wallId | formatId('wall') }}</el-link></template>
                </el-table-column>
                <el-table-column prop="wallContent" label="内容" min-width="200" show-overflow-tooltip></el-table-column>
                <el-table-column prop="auditState" label="审核" width="70" align="center">
                  <template slot-scope="scope"><el-tag :type="scope.row.auditState === 1 ? 'success' : scope.row.auditState === 2 ? 'danger' : 'warning'" size="mini">{{ scope.row.auditState === 1 ? '通过' : scope.row.auditState === 2 ? '拒绝' : '待审' }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="createTime" label="发布时间" width="160">
                  <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
      <div v-else style="text-align:center;color:#909399;padding:20px;">未找到该用户信息</div>
    </div>
  </el-dialog>
</template>
<script>
import { getUserList, getPostList, getGoodsList, getWallList } from '@/api/manage'
export default {
  name: 'UserDetailDialog',
  props: { visible: Boolean, userId: String },
  /** 组件数据定义 */
  data() { return { loading: false, userData: null, activeTab: 'post', tabLoading: false, userPosts: [], userGoods: [], userWalls: [], userFollowerCount: 0, userFollowingCount: 0 } },
  /** 计算属性定义 */
  computed: { dialogVisible: { get() { return this.visible }, set(val) { this.$emit('update:visible', val) } } },
  watch: {
    visible(val) { if (val && this.userId) this.loadUser() },
    userId(val) { if (val && this.visible) this.loadUser() }
  },
  /** 组件方法定义 */
  methods: {
    async loadUser() {
      this.loading = true; this.userData = null; this.userPosts = []; this.userGoods = []; this.userWalls = []
      try {
        var res = await getUserList({ pageNum: 1, pageSize: 9999 })
        if (res.code === 200) { var records = res.data.records || res.data || []; this.userData = records.find(function(u) { return u.userId === this.userId }.bind(this)) || null }
      } catch (e) { this.userData = null }
      this.loading = false
      if (this.userData) { this.loadAllTabData() }
      try {
        var followRes = await this.$axios.get('/follow/count?userId=' + this.userId)
        if (followRes.code === 200 && followRes.data) {
          this.userFollowerCount = followRes.data.followerCount || 0
          this.userFollowingCount = followRes.data.followingCount || 0
        }
      } catch(e) {}
    },
    async loadAllTabData() {
      this.tabLoading = true
      try {
        var self = this
        var postRes = await getPostList({ pageNum: 1, pageSize: 9999 })
        if (postRes.code === 200) { var records = postRes.data.records || postRes.data || []; self.userPosts = records.filter(function(p) { return p.userId === self.userId }) }
        var goodsRes = await getGoodsList({ pageNum: 1, pageSize: 9999 })
        if (goodsRes.code === 200) { var records2 = goodsRes.data.records || goodsRes.data || []; self.userGoods = records2.filter(function(g) { return g.userId === self.userId }) }
        var wallRes = await getWallList({ pageNum: 1, pageSize: 9999 })
        if (wallRes.code === 200) { var records3 = wallRes.data.records || wallRes.data || []; self.userWalls = records3.filter(function(w) { return w.wallId; return w.userId === self.userId }) }
      } catch (e) {}
      this.tabLoading = false
    },
    onClose() { this.userData = null; this.userPosts = []; this.userGoods = []; this.userWalls = [] }
  }
}
</script>
<style scoped>
/* 组件局部样式 */
.dt-table {
  width: 100%;
  border-collapse: collapse;
  border-color: #ebeef5;
}
.dt-table td {
  padding: 12px 16px;
  border: 1px solid #ebeef5;
  font-size: 13px;
  color: #303133;
}
.dt-label {
  background: #fafafa;
  font-weight: 600;
  width: 100px;
  text-align: right;
  color: #303133;
  white-space: nowrap;
}
.dt-table tr {
  transition: background-color 0.3s ease;
}
.dt-table tr:hover td {
  background-color: #f5f7fa;
}
::v-deep .el-card {
  border-radius: 8px;
  transition: box-shadow 0.3s ease;
}
::v-deep .el-card__header {
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
}
::v-deep .el-avatar {
  border-radius: 50%;
  transition: transform 0.3s ease;
}
::v-deep .el-avatar:hover {
  transform: scale(1.05);
}
::v-deep .el-tag {
  border-radius: 6px;
  transition: transform 0.3s ease;
}
::v-deep .el-tag:hover {
  transform: translateY(-1px);
}
::v-deep .el-link {
  transition: color 0.3s ease;
}
::v-deep .el-tabs__item {
  transition: color 0.3s ease;
}
</style>
