<!--
  组件名：MyComment
  功能描述：我的评论页
  主要职责：
    1. 展示用户发表的所有评论
    2. 删除评论
    3. 跳转到原内容详情
-->
<template>
  <div>
    <el-card>
      <div slot="header"><span>我的评论</span></div>
      <el-tabs v-model="activeTab" @tab-click="onTabChange">
        <el-tab-pane label="我发出的" name="sent">
          <el-table :data="commentList" v-loading="loading">
            <el-table-column prop="text" label="评论内容" min-width="250">
              <template slot-scope="scope"><el-link :underline="false" @click="$router.push('/postDetail/' + scope.row.postId)" style="color:#303133;">{{ scope.row.text }}</el-link></template>
            </el-table-column>
            <el-table-column prop="postId" label="帖子" width="120" align="center">
              <template slot-scope="scope"><el-link type="primary" @click="$router.push('/postDetail/' + scope.row.postId)">查看帖子</el-link></template>
            </el-table-column>
            <el-table-column prop="createTime" label="评论时间" width="180">
              <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template slot-scope="scope"><el-button size="mini" type="danger" @click="deleteComment(scope.row.commentId)">删除</el-button></template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="sentTotal > pageSize" style="margin-top:15px;text-align:center;" @current-change="(p) => { sentPage = p; loadSentComments() }" :current-page="sentPage" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="sentTotal" layout="total, sizes, prev, pager, next" @size-change="handleSentSizeChange"></el-pagination>
        </el-tab-pane>

        <el-tab-pane label="我收到的" name="received">
          <div v-if="receivedList.length === 0 && !loading" style="text-align:center;color:#999;padding:40px 0;">暂无收到的评论</div>
          <div v-for="item in receivedList" :key="item.commentId" class="received-item" @click="$router.push('/postDetail/' + item.postId)">
            <div class="received-avatar">
              <el-avatar v-if="item.avatar" :src="item.avatar" :size="36"></el-avatar>
              <el-avatar v-else :size="36" style="background:#409EFF;">{{ (item.nickname || '用')[0] }}</el-avatar>
            </div>
            <div class="received-body">
              <div style="display:flex;justify-content:space-between;align-items:flex-start;">
                <div><strong>{{ item.nickname || '匿名用户' }}</strong> 评论了你的帖子「<el-link type="primary">{{ item.postTitle || '帖子' + item.postId }}</el-link>」</div>
                <el-tag size="mini" effect="plain">{{ item.createTime | formatTime }}</el-tag>
              </div>
              <div style="margin-top:6px;color:#666;font-size:13px;">{{ item.text }}</div>
            </div>
          </div>
          <el-pagination v-if="receivedTotal > pageSize" style="margin-top:15px;text-align:center;" @current-change="(p) => { receivedPage = p; loadReceivedComments() }" :current-page="receivedPage" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="receivedTotal" layout="total, sizes, prev, pager, next" @size-change="handleReceivedSizeChange"></el-pagination>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'MyComment',
  data() {
    return {
      activeTab: 'sent',
      commentList: [],
      sentPage: 1,
      sentTotal: 0,
      receivedList: [],
      receivedPage: 1,
      receivedTotal: 0,
      pageSize: 10,
      loading: false
    }
  },
  created() {
    this.loadSentComments()
  },
  methods: {
    onTabChange(tab) {
      if (tab.name === 'sent') this.loadSentComments()
      else if (tab.name === 'received') this.loadReceivedComments()
    },
    async loadSentComments() {
      this.loading = true
      const res = await this.$axios.get('/comment/myList/' + this.sentPage + '/' + this.pageSize)
      if (res.code === 200) { this.commentList = res.data.records || []; this.sentTotal = res.data.total || 0 }
      this.loading = false
    },
    async loadReceivedComments() {
      this.loading = true
      try {
        const res = await this.$axios.get('/comment/received/' + this.receivedPage + '/' + this.pageSize)
        if (res.code === 200) { this.receivedList = res.data.records || []; this.receivedTotal = res.data.total || 0 }
      } catch (e) {}
      this.loading = false
    },
    async deleteComment(commentId) {
      await this.$confirm('确定删除该评论？', '提示', { type: 'warning' })
      const res = await this.$axios.delete('/comment/' + commentId)
      if (res.code === 200) { this.$message.success('删除成功'); this.loadSentComments() }
    },
    handleSentSizeChange(val) { this.pageSize = val; this.sentPage = 1; this.loadSentComments() },
    handleReceivedSizeChange(val) { this.pageSize = val; this.receivedPage = 1; this.loadReceivedComments() }
  }
}
</script>

<style scoped>
.received-item { display: flex; padding: 12px; border-bottom: 1px solid #f0f0f0; cursor: pointer; transition: background 0.3s ease; border-radius: 8px; }
.received-item:hover { background: #f5f7fa; }
.received-item:last-child { border-bottom: none; }
.received-avatar { flex-shrink: 0; margin-right: 12px; }
.received-body { flex: 1; min-width: 0; }
</style>
