<!--
  组件名：SearchResults
  功能描述：搜索结果页
  主要职责：
    1. 全局搜索结果展示
    2. 按类型分类（帖子/商品/表白墙）
    3. 分页
-->
<template>
  <div>
    <el-card>
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
        <el-page-header @back="$router.back()" :content="'搜索结果：' + keyword"></el-page-header>
        <div style="display:flex;gap:8px;align-items:center;">
          <el-input v-model="keyword" placeholder="搜索..." size="small" style="width:250px;" @keyup.enter.native="doSearch"></el-input>
          <el-select v-model="scope" size="small" style="width:120px;" @change="doSearch">
            <el-option label="全部" value="all"></el-option>
            <el-option label="帖子" value="post"></el-option>
            <el-option label="商品" value="goods"></el-option>
            <el-option label="表白墙" value="wall"></el-option>
            <el-option label="用户" value="user"></el-option>
          </el-select>
          <el-button type="primary" size="small" icon="el-icon-search" @click="doSearch">搜索</el-button>
        </div>
      </div>
      <el-tabs v-model="activeTab" @tab-click="onTabChange">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane :label="'帖子(' + postTotal + ')'" name="post"></el-tab-pane>
        <el-tab-pane :label="'商品(' + goodsTotal + ')'" name="goods"></el-tab-pane>
        <el-tab-pane :label="'表白墙(' + wallTotal + ')'" name="wall"></el-tab-pane>
        <el-tab-pane :label="'用户(' + userList.length + ')'" name="user"></el-tab-pane>
      </el-tabs>

      <!-- Posts results -->
      <div v-if="activeTab === 'all' || activeTab === 'post'">
        <div style="font-weight:bold;margin-bottom:10px;" v-if="activeTab === 'all'">帖子</div>
        <div v-for="post in postList" :key="post.postId" style="padding:12px 0;border-bottom:1px solid #f0f0f0;cursor:pointer;" @click="$router.push('/postDetail/' + post.postId)">
          <div style="font-weight:500;">{{ post.title }}</div>
          <div style="color:#999;font-size:12px;margin-top:4px;"><i class="el-icon-view"></i> {{ post.viewNum || 0 }} <i class="el-icon-chat-dot-round"></i> {{ post.commentNum || 0 }} · {{ post.createTime | formatTime }}</div>
        </div>
        <div v-if="postList.length === 0 && (activeTab === 'all' || activeTab === 'post')" style="text-align:center;color:#999;padding:20px 0;">暂无帖子结果</div>
        <el-pagination v-if="postTotal > pageSize && (activeTab === 'post')" style="margin-top:10px;text-align:center;" @current-change="p => { postPage = p; loadPosts() }" :current-page="postPage" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="postTotal" layout="total, sizes, prev, pager, next" small @size-change="handlePostSizeChange"></el-pagination>
      </div>

      <!-- Goods results -->
      <div v-if="activeTab === 'all' || activeTab === 'goods'" style="margin-top:15px;">
        <div style="font-weight:bold;margin-bottom:10px;" v-if="activeTab === 'all'">商品</div>
        <div v-for="goods in goodsList" :key="goods.goodsId" style="padding:12px 0;border-bottom:1px solid #f0f0f0;cursor:pointer;" @click="$router.push('/goodsDetail/' + goods.goodsId)">
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <span style="font-weight:500;">{{ goods.goodsName }}</span>
            <span style="color:#F56C6C;font-weight:bold;">￥{{ goods.goodsPrice }}</span>
          </div>
          <div style="color:#999;font-size:12px;margin-top:4px;">{{ goods.createTime | formatTime }}</div>
        </div>
        <div v-if="goodsList.length === 0 && (activeTab === 'all' || activeTab === 'goods')" style="text-align:center;color:#999;padding:20px 0;">暂无商品结果</div>
        <el-pagination v-if="goodsTotal > pageSize && (activeTab === 'goods')" style="margin-top:10px;text-align:center;" @current-change="p => { goodsPage = p; loadGoods() }" :current-page="goodsPage" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="goodsTotal" layout="total, sizes, prev, pager, next" small @size-change="handleGoodsSizeChange"></el-pagination>
      </div>

      <!-- Wall results -->
      <div v-if="activeTab === 'all' || activeTab === 'wall'" style="margin-top:15px;">
        <div style="font-weight:bold;margin-bottom:10px;" v-if="activeTab === 'all'">表白墙</div>
        <div v-for="wall in wallList" :key="wall.wallId" style="padding:12px 0;border-bottom:1px solid #f0f0f0;cursor:pointer;" @click="$router.push('/wallDetail/' + wall.wallId)">
          <div>{{ wall.wallContent }}</div>
          <div style="color:#999;font-size:12px;margin-top:4px;">{{ wall.createTime | formatTime }}</div>
        </div>
        <div v-if="wallList.length === 0 && (activeTab === 'all' || activeTab === 'wall')" style="text-align:center;color:#999;padding:20px 0;">暂无表白墙结果</div>
      </div>

      <!-- User results -->
      <div v-if="activeTab === 'all' || activeTab === 'user'" style="margin-top:15px;">
        <div style="font-weight:bold;margin-bottom:10px;" v-if="activeTab === 'all'">用户</div>
        <div v-for="user in userList" :key="user.userId" style="padding:12px 0;border-bottom:1px solid #f0f0f0;cursor:pointer;display:flex;align-items:center;" @click="$router.push('/userProfile/' + user.userId)">
          <el-avatar v-if="user.avatar" :src="user.avatar" :size="40" style="margin-right:12px;"></el-avatar>
          <el-avatar v-else :size="40" style="margin-right:12px;background:#409EFF;">{{ (user.nickname || user.username || '用')[0] }}</el-avatar>
          <div>
            <div style="font-weight:500;">{{ user.nickname || user.username }}<span style="margin-left:6px;font-size:12px;color:#909399;font-weight:normal;">USR-{{ user.userId }}</span></div>
            <div style="color:#999;font-size:12px;">ID: {{ user.userId }}</div>
          </div>
        </div>
        <div v-if="userList.length === 0 && (activeTab === 'all' || activeTab === 'user')" style="text-align:center;color:#999;padding:20px 0;">暂无用户结果</div>
      </div>
    </el-card>
  </div>
</template>

<script>
import SearchPanel from '@/components/SearchPanel.vue'
export default {
  name: 'SearchResults',
  components: { SearchPanel },
  data() {
    return {
      keyword: '',
      scope: 'all',
      activeTab: 'all',
      pageSize: 10,
      postList: [],
      postPage: 1,
      postTotal: 0,
      goodsList: [],
      goodsPage: 1,
      goodsTotal: 0,
      wallList: [],
      wallTotal: 0,
      userList: []
    }
  },
  created() {
    this.keyword = this.$route.query.keyword || ''
    this.scope = this.$route.query.scope || 'all'
    this.activeTab = this.scope === 'all' ? 'all' : this.scope
    this.doSearch()
  },
  watch: {
    '$route'(to) {
      this.keyword = to.query.keyword || ''
      this.scope = to.query.scope || 'all'
      this.activeTab = this.scope === 'all' ? 'all' : this.scope
      this.doSearch()
    }
  },
  filters: {
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const pad = n => String(n).padStart(2, '0')
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    }
  },
  methods: {
    doSearch() {
      if (!this.keyword.trim()) return
      this.postPage = 1
      this.goodsPage = 1
      this.loadPosts()
      this.loadGoods()
      this.loadWall()
      this.loadUsers()
    },
    onTabChange() {
      // Already loaded, just display
    },
    async loadPosts() {
      try {
        const res = await this.$axios.get('/post/list', { params: { title: this.keyword, pageNum: this.postPage, pageSize: this.activeTab === 'all' ? 5 : this.pageSize } })
        if (res.code === 200) { this.postList = res.data.records || []; this.postTotal = res.data.total || 0 }
      } catch (e) {}
    },
    async loadGoods() {
      try {
        const res = await this.$axios.get('/goods/getList', { params: { keyName: this.keyword, pageNum: this.goodsPage, pageSize: this.activeTab === 'all' ? 5 : this.pageSize } })
        if (res.code === 200) { this.goodsList = res.data.records || []; this.goodsTotal = res.data.total || 0 }
      } catch (e) {}
    },
    async loadWall() {
      try {
        const res = await this.$axios.get('/wall/wallList', { params: { wallContent: this.keyword, pageNum: 1, pageSize: this.activeTab === 'all' ? 5 : 10 } })
        if (res.code === 200) { this.wallList = res.data.records || []; this.wallTotal = res.data.total || 0 }
      } catch (e) {}
    },
    async loadUsers() {
      try {
        const res = await this.$axios.get('/user/search', { params: { keyword: this.keyword } })
        if (res.code === 200) { this.userList = res.data || [] }
      } catch (e) { this.userList = [] }
    },
    handlePostSizeChange(val) { this.pageSize = val; this.postPage = 1; this.loadPosts() },
    handleGoodsSizeChange(val) { this.pageSize = val; this.goodsPage = 1; this.loadGoods() }
  }
}
</script>

<style scoped>
/* Card beautification */
::v-deep .el-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  border: none;
}

/* Section titles */
.search-section-title {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
  margin-bottom: 10px;
}

/* Search result items */
.search-result-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
}
.search-result-item:hover {
  background: #f5f7fa;
  transform: translateX(4px);
}

/* User result items */
.search-user-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  display: flex;
  align-items: center;
  border-radius: 8px;
  transition: all 0.3s ease;
}
.search-user-item:hover {
  background: #f5f7fa;
  transform: translateX(4px);
}
</style>
