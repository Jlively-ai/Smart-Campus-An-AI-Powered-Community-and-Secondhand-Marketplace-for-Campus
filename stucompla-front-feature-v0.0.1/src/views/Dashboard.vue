<!--
  组件名：Dashboard
  功能描述：仪表盘页面，展示平台概览信息
  主要职责：
    1. 公告滚动展示
    2. 搜索面板
    3. 分类标签导航
    4. 个人数据统计
    5. 订单统计
    6. 物流信息
    7. 最新/热门帖子商品表白墙
    8. 粉丝列表弹窗
-->
<template>
  <div>
    <el-card shadow="hover" style="margin-bottom:20px;" v-if="announcements.length > 0">
      <div style="display:flex;align-items:center;">
        <el-tag size="small" type="danger" style="margin-right:10px;flex-shrink:0;">📢 公告</el-tag>
        <div class="announce-scroll-wrap">
          <div class="announce-scroll-inner" :class="{ scrolling: announcements.length > 1 }">
            <div v-for="(a, i) in announcements" :key="i" class="announce-scroll-item" @click="showAnnounce(a)">
              {{ a.title || a.content }}
            </div>
          </div>
        </div>
        <el-link type="primary" style="margin-left:10px;flex-shrink:0;" @click="$router.push('/announcementList').catch(() => {})">更多</el-link>
      </div>
    </el-card>

    <el-card shadow="hover" style="margin-bottom:20px;overflow:visible;">
      <div style="display:flex;align-items:center;flex-wrap:wrap;gap:8px;">
        <el-select v-model="searchScope" size="small" style="width:100px;flex-shrink:0;">
          <el-option label="全部" value="all"></el-option>
          <el-option label="帖子" value="post"></el-option>
          <el-option label="商品" value="goods"></el-option>
          <el-option label="表白墙" value="wall"></el-option>
          <el-option label="用户" value="user"></el-option>
        </el-select>
        <search-panel v-model="searchKeyword" module="dashboard" :placeholder="scrollPlaceholder" input-style="flex:1;min-width:400px;" :hot-tags="hotSearchTags" @search="doSearch"></search-panel>
        <el-button type="primary" style="flex-shrink:0;" @click="doSearch">搜索</el-button>
      </div>
      <div style="margin-top:10px;">
        <div style="margin-bottom:6px;display:flex;align-items:center;gap:8px;">
          <span style="font-size:13px;color:#409EFF;font-weight:bold;">帖子分类</span>
          <div style="display:flex;flex-wrap:wrap;gap:6px;">
            <el-tag v-for="cat in categories.slice(0, 10)" :key="cat.categoryId" class="topic-tag" effect="plain" size="small" type="primary" @click.native="goCategory(cat.categoryId)">{{ cat.categoryName }}</el-tag>
          </div>
        </div>
        <div style="display:flex;align-items:center;gap:8px;">
          <span style="font-size:13px;color:#303133;font-weight:bold;">商品分类</span>
          <div style="display:flex;flex-wrap:wrap;gap:6px;">
            <el-tag v-for="cat in goodsCategories.slice(0, 10)" :key="cat.goodsCategoryId" class="topic-tag" effect="plain" size="small" type="info" style="color:#303133;border-color:#303133;" @click.native="goGoodsCategory(cat.goodsCategoryId)">{{ cat.goodsCategoryName }}</el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 第一行：我的数据、我的订单、物流信息（我买的）、物流信息（我卖的） -->
    <el-row :gutter="20" style="margin-bottom:20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="dashboard-top-card">
          <div slot="header" style="font-weight:bold;font-size:15px;">📊 我的数据</div>
          <div class="stat-row stat-clickable" @click="showFollowersDialog"><span class="stat-label">我的粉丝</span><span class="stat-value" style="color:#909399;">{{ myStats.myFollowerCount || 0 }} <i class="el-icon-arrow-right" style="font-size:12px;"></i></span></div>
          <div class="stat-row"><span class="stat-label">收到浏览</span><span class="stat-value" style="color:#409EFF;">{{ myStats.myReceivedViewCount || 0 }}</span></div>
          <div class="stat-row"><span class="stat-label">收到点赞</span><span class="stat-value" style="color:#F56C6C;">{{ myStats.myReceivedLikeCount || 0 }}</span></div>
          <div class="stat-row"><span class="stat-label">收到评论</span><span class="stat-value" style="color:#67C23A;">{{ myStats.myReceivedCommentCount || 0 }}</span></div>
          <div class="stat-row"><span class="stat-label">收到收藏</span><span class="stat-value" style="color:#E6A23C;">{{ myStats.myReceivedCollectCount || 0 }}</span></div>
          <div class="stat-row"><span class="stat-label">收到@</span><span class="stat-value" style="color:#9b59b6;">{{ myStats.myReceivedMentionCount || 0 }}</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="dashboard-top-card">
          <div slot="header" style="font-weight:bold;font-size:15px;">📦 我的订单</div>
          <div class="stat-row stat-clickable" @click="$router.push('/myOrder').catch(() => {})">
            <span class="stat-label">我买到的</span>
            <span class="stat-value" style="color:#409EFF;">{{ myStats.myOrderCount || 0 }} <i class="el-icon-arrow-right" style="font-size:12px;"></i></span>
          </div>
          <div class="stat-row stat-clickable" @click="$router.push('/myOrder?tab=sale').catch(() => {})">
            <span class="stat-label">我卖出的</span>
            <span class="stat-value" style="color:#E6A23C;">{{ myStats.mySaleCount || 0 }} <i class="el-icon-arrow-right" style="font-size:12px;"></i></span>
          </div>
          <div class="stat-row stat-clickable" @click="$router.push('/myOrder?tab=sale').catch(() => {})">
            <span class="stat-label">本月订单</span>
            <span class="stat-value" style="color:#67C23A;">{{ myStats.myMonthOrderCount || 0 }} <i class="el-icon-arrow-right" style="font-size:12px;"></i></span>
          </div>
          <div class="stat-row stat-clickable" @click="$router.push('/myOrder?tab=sale').catch(() => {})">
            <span class="stat-label">本月收入</span>
            <span class="stat-value" style="color:#67C23A;">￥{{ myStats.myMonthRevenue || 0 }} <i class="el-icon-arrow-right" style="font-size:12px;"></i></span>
          </div>
          <div class="stat-row stat-clickable" @click="$router.push('/myOrder?tab=sale').catch(() => {})">
            <span class="stat-label">累计订单</span>
            <span class="stat-value" style="color:#409EFF;">{{ myStats.totalOrderCount || 0 }} <i class="el-icon-arrow-right" style="font-size:12px;"></i></span>
          </div>
          <div class="stat-row stat-clickable" @click="$router.push('/myOrder?tab=sale').catch(() => {})">
            <span class="stat-label">累计收入</span>
            <span class="stat-value" style="color:#F56C6C;">￥{{ myStats.myRevenue || 0 }} <i class="el-icon-arrow-right" style="font-size:12px;"></i></span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="dashboard-top-card">
          <div slot="header" style="font-weight:bold;font-size:15px;">🚚 物流信息（我买的）</div>
          <div v-if="buyLogisticsList.length === 0" style="text-align:center;color:#999;padding:20px 0;">暂无物流信息</div>
          <div v-else class="logistics-scroll-wrap" ref="buyLogisticsWrap" @mouseenter="pauseLogisticsScroll('buy')" @mouseleave="startLogisticsScroll('buy')">
            <div class="logistics-scroll-inner">
              <div v-for="item in buyLogisticsList" :key="'buy-' + item.orderId" class="logistics-item">
                <div class="logistics-main" @click="$router.push('/orderDetail/' + item.orderId).catch(() => {})">
                  <span class="logistics-goods">{{ item.goodsName || '商品' }}</span>
                  <el-tag size="mini" :type="item.trackingNo ? 'success' : 'info'">{{ item.trackingNo ? (item.logisticsStatus || '运输中') : '未发货' }}</el-tag>
                </div>
                <div class="logistics-detail" v-if="item.company || item.trackingNo">
                  <span v-if="item.company">{{ item.company }}</span>
                  <span v-if="item.trackingNo" style="color:#999;margin-left:8px;">{{ item.trackingNo }}</span>
                </div>
                <div v-if="item.orderStatus >= 3" style="text-align:right;margin-top:4px;">
                  <el-button type="text" size="mini" @click.stop="dismissLogistics('buy', item.orderId)" style="color:#F56C6C;">删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="dashboard-top-card">
          <div slot="header" style="font-weight:bold;font-size:15px;">🚚 物流信息（我卖的）</div>
          <div v-if="saleLogisticsList.length === 0" style="text-align:center;color:#999;padding:20px 0;">暂无物流信息</div>
          <div v-else class="logistics-scroll-wrap" ref="saleLogisticsWrap" @mouseenter="pauseLogisticsScroll('sale')" @mouseleave="startLogisticsScroll('sale')">
            <div class="logistics-scroll-inner">
              <div v-for="item in saleLogisticsList" :key="'sale-' + item.orderId" class="logistics-item">
                <div class="logistics-main" @click="$router.push('/orderDetail/' + item.orderId).catch(() => {})">
                  <span class="logistics-goods">{{ item.goodsName || '商品' }}</span>
                  <el-tag size="mini" :type="item.trackingNo ? 'success' : 'info'">{{ item.trackingNo ? (item.logisticsStatus || '运输中') : '未发货' }}</el-tag>
                </div>
                <div class="logistics-detail" v-if="item.company || item.trackingNo">
                  <span v-if="item.company">{{ item.company }}</span>
                  <span v-if="item.trackingNo" style="color:#999;margin-left:8px;">{{ item.trackingNo }}</span>
                </div>
                <div v-if="item.orderStatus >= 3" style="text-align:right;margin-top:4px;">
                  <el-button type="text" size="mini" @click.stop="dismissLogistics('sale', item.orderId)" style="color:#F56C6C;">删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二栏：数据统计 -->
    <div class="stat-row-flex" style="margin-bottom:20px;">
      <div v-for="(item, idx) in personalStats" :key="'ps-'+idx" class="stat-col-flex">
        <el-card shadow="hover" class="stat-card-clickable" @click.native="item.action">
          <div style="text-align:center;padding:15px 0;">
            <div style="font-size:24px;font-weight:bold;" :style="{color: item.color}">{{ item.value }}</div>
            <div style="color:#999;margin-top:6px;">{{ item.label }}</div>
          </div>
        </el-card>
      </div>
    </div>
    <div class="stat-row-flex" style="margin-bottom:20px;">
      <div v-for="(item, idx) in totalStats" :key="'ts-'+idx" class="stat-col-flex">
        <el-card shadow="hover">
          <div style="text-align:center;padding:15px 0;">
            <div style="font-size:24px;font-weight:bold;" :style="{color: item.color}">{{ item.value }}</div>
            <div style="color:#999;margin-top:6px;">{{ item.label }}</div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 第三行：最新帖子、最新商品、最新表白墙 -->
    <el-row :gutter="20" style="margin-bottom:20px;">
      <el-col :span="8">
        <el-card shadow="hover" class="list-card">
          <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
            <span style="font-weight:bold;font-size:15px;">✨ 最新帖子</span>
            <el-link type="primary" @click="$router.push('/postList').catch(() => {})">更多</el-link>
          </div>
          <div v-if="latestPosts.length === 0" style="text-align:center;color:#999;padding:20px 0;">暂无帖子</div>
          <div v-else>
            <div v-for="post in latestPosts" :key="post.postId" class="latest-card" @click="$router.push('/postDetail/' + post.postId).catch(() => {})">
              <div class="latest-title">{{ post.title }}</div>
              <div class="latest-meta">
                <span>{{ post.nickname || '匿名' }}</span>
                <span><i class="el-icon-view"></i> {{ post.viewNum || 0 }} <i class="el-icon-thumb"></i> {{ post.likeNum || 0 }}</span>
                <span class="latest-time">{{ formatShortTime(post.createTime) }}</span>
              </div>
            </div>
            <div v-for="n in (5 - latestPosts.length)" :key="'ph-post-' + n" class="latest-card latest-placeholder">
              <div class="latest-title" style="color:#ddd;">暂无数据</div>
              <div class="latest-meta"><span>&nbsp;</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="list-card">
          <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
            <span style="font-weight:bold;font-size:15px;">🛍️ 最新商品</span>
            <el-link type="primary" @click="$router.push('/goodsList').catch(() => {})">更多</el-link>
          </div>
          <div v-if="latestGoods.length === 0" style="text-align:center;color:#999;padding:20px 0;">暂无商品</div>
          <div v-else>
            <div v-for="goods in latestGoods" :key="goods.goodsId" class="latest-card" @click="$router.push('/goodsDetail/' + goods.goodsId).catch(() => {})">
              <div class="latest-title">{{ goods.goodsName }}</div>
              <div class="latest-meta">
                <span style="color:#F56C6C;font-weight:bold;">￥{{ goods.goodsPrice }}</span>
                <span><i class="el-icon-view"></i> {{ goods.viewNum || 0 }} <i class="el-icon-box"></i> {{ goods.goodsCount || 0 }}</span>
                <span class="latest-time">{{ formatShortTime(goods.createTime) }}</span>
              </div>
            </div>
            <div v-for="n in (5 - latestGoods.length)" :key="'ph-goods-' + n" class="latest-card latest-placeholder">
              <div class="latest-title" style="color:#ddd;">暂无数据</div>
              <div class="latest-meta"><span>&nbsp;</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="list-card">
          <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
            <span style="font-weight:bold;font-size:15px;">💕 最新表白墙</span>
            <el-link type="primary" @click="$router.push('/wallList').catch(() => {})">更多</el-link>
          </div>
          <div v-if="latestWalls.length === 0" style="text-align:center;color:#999;padding:20px 0;">暂无内容</div>
          <div v-else>
            <div v-for="wall in latestWalls" :key="wall.wallId" class="latest-card" @click="$router.push('/wallDetail/' + wall.wallId).catch(() => {})">
              <div class="latest-title wall-title-ellipsis">{{ wall.wallContent }}</div>
              <div class="latest-meta">
                <span>{{ wall.isAnonymous ? '匿名' : (wall.nickname || '用户') }}</span>
                <span><i class="el-icon-view"></i> {{ wall.viewNum || 0 }} <i class="el-icon-thumb"></i> {{ wall.likeNum || 0 }}</span>
                <span class="latest-time">{{ formatShortTime(wall.createTime) }}</span>
              </div>
            </div>
            <div v-for="n in (5 - latestWalls.length)" :key="'ph-wall-' + n" class="latest-card latest-placeholder">
              <div class="latest-title" style="color:#ddd;">暂无数据</div>
              <div class="latest-meta"><span>&nbsp;</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第三行：热门帖子排行、热门商品排行、热门表白墙排行 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="list-card">
          <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
            <span style="font-weight:bold;font-size:15px;">🔥 热帖排行</span>
            <el-link type="primary" @click="$router.push('/postList').catch(() => {})">更多</el-link>
          </div>
          <div v-if="hotPosts.length === 0" style="text-align:center;color:#999;padding:20px 0;">暂无热帖</div>
          <div v-else>
            <div v-for="(post, idx) in hotPosts" :key="post.postId" class="hot-item" @click="$router.push('/postDetail/' + post.postId).catch(() => {})">
              <span class="hot-rank" :class="'rank-' + (idx < 3 ? idx : 'default')">{{ idx + 1 }}</span>
              <div class="hot-info">
                <div class="hot-title">{{ post.title }}</div>
                <div class="hot-meta">
                  <span>{{ post.nickname || '匿名' }}</span>
                  <span><i class="el-icon-view"></i> {{ post.viewNum || 0 }}</span>
                  <span><i class="el-icon-thumb"></i> {{ post.likeNum || 0 }}</span>
                  <span class="hot-time">{{ formatShortTime(post.createTime) }}</span>
                </div>
              </div>
            </div>
            <div v-for="n in (5 - hotPosts.length)" :key="'ph-hpost-' + n" class="hot-item hot-placeholder">
              <span class="hot-rank rank-default">{{ hotPosts.length + n }}</span>
              <div class="hot-info">
                <div class="hot-title" style="color:#ddd;">暂无数据</div>
                <div class="hot-meta"><span>&nbsp;</span></div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="list-card">
          <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
            <span style="font-weight:bold;font-size:15px;">🛒 热门商品</span>
            <el-link type="primary" @click="$router.push('/goodsList').catch(() => {})">更多</el-link>
          </div>
          <div v-if="hotGoods.length === 0" style="text-align:center;color:#999;padding:20px 0;">暂无商品</div>
          <div v-else>
            <div v-for="(goods, idx) in hotGoods" :key="goods.goodsId" class="hot-item" @click="$router.push('/goodsDetail/' + goods.goodsId).catch(() => {})">
              <span class="hot-rank" :class="'rank-' + (idx < 3 ? idx : 'default')">{{ idx + 1 }}</span>
              <div class="hot-info">
                <div class="hot-title">{{ goods.goodsName }}</div>
                <div class="hot-meta">
                  <span style="color:#F56C6C;font-weight:bold;">￥{{ goods.goodsPrice }}</span>
                  <span><i class="el-icon-view"></i> {{ goods.viewNum || 0 }}</span>
                  <span><i class="el-icon-box"></i> 库存{{ goods.goodsCount || 0 }}</span>
                  <span class="hot-time">{{ formatShortTime(goods.createTime) }}</span>
                </div>
              </div>
            </div>
            <div v-for="n in (5 - hotGoods.length)" :key="'ph-hgoods-' + n" class="hot-item hot-placeholder">
              <span class="hot-rank rank-default">{{ hotGoods.length + n }}</span>
              <div class="hot-info">
                <div class="hot-title" style="color:#ddd;">暂无数据</div>
                <div class="hot-meta"><span>&nbsp;</span></div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="list-card">
          <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
            <span style="font-weight:bold;font-size:15px;">💕 热门表白墙</span>
            <el-link type="primary" @click="$router.push('/wallList').catch(() => {})">更多</el-link>
          </div>
          <div v-if="hotWalls.length === 0" style="text-align:center;color:#999;padding:20px 0;">暂无内容</div>
          <div v-else>
            <div v-for="(wall, idx) in hotWalls" :key="wall.wallId" class="hot-item" @click="$router.push('/wallDetail/' + wall.wallId).catch(() => {})">
              <span class="hot-rank" :class="'rank-' + (idx < 3 ? idx : 'default')">{{ idx + 1 }}</span>
              <div class="hot-info">
                <div class="hot-title wall-title-ellipsis">{{ wall.wallContent }}</div>
                <div class="hot-meta">
                  <span>{{ wall.isAnonymous ? '匿名' : (wall.nickname || '用户') }}</span>
                  <span><i class="el-icon-view"></i> {{ wall.viewNum || 0 }}</span>
                  <span><i class="el-icon-thumb"></i> {{ wall.likeNum || 0 }}</span>
                  <span class="hot-time">{{ formatShortTime(wall.createTime) }}</span>
                </div>
              </div>
            </div>
            <div v-for="n in (5 - hotWalls.length)" :key="'ph-hwall-' + n" class="hot-item hot-placeholder">
              <span class="hot-rank rank-default">{{ hotWalls.length + n }}</span>
              <div class="hot-info">
                <div class="hot-title" style="color:#ddd;">暂无数据</div>
                <div class="hot-meta"><span>&nbsp;</span></div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog :title="announceDetail.title" :visible.sync="announceVisible" width="520px" class="announce-dialog">
      <div class="announce-dialog-body">
        <div style="margin-bottom:12px;">
          <el-tag size="small" :type="announceDetail.announcementType == 1 ? 'danger' : 'info'">{{ announceDetail.announcementType == 1 ? '重要' : '普通' }}</el-tag>
          <span style="margin-left:10px;color:#999;font-size:12px;">{{ formatShortTime(announceDetail.createTime) }}</span>
        </div>
        <div class="announce-content-text">{{ announceDetail.content }}</div>
        <div v-if="parseImages(announceDetail.images).length > 0" class="announce-images">
          <el-image v-for="(img, i) in parseImages(announceDetail.images)" :key="i" :src="img" :preview-src-list="parseImages(announceDetail.images)" fit="cover"></el-image>
        </div>
      </div>
      <span slot="footer"><el-button type="primary" @click="announceVisible = false">关闭</el-button></span>
    </el-dialog>
    <!-- 粉丝列表弹窗 -->
    <el-dialog title="我的粉丝" :visible.sync="followersVisible" width="480px" class="followers-dialog">
      <div v-loading="followersLoading">
        <div v-if="followersList.length === 0" class="dialog-empty">暂无粉丝</div>
        <div v-for="f in followersList" :key="f.userId" class="follower-item">
          <el-avatar :size="40" :src="f.avatar">{{ f.nickname && f.nickname[0] }}</el-avatar>
          <div class="follower-info">
            <div class="follower-name">{{ f.nickname || '用户' }}</div>
          </div>
          <el-button size="mini" type="primary" plain @click="$router.push('/userProfile/' + f.userId).catch(() => {}); followersVisible = false">查看</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import SearchPanel from '@/components/SearchPanel.vue'
export default {
  name: 'Dashboard',
  components: { SearchPanel },
  data() {
    return {
      hotPosts: [],
      latestPosts: [],
      latestGoods: [],
      hotGoods: [],
      latestWalls: [],
      hotWalls: [],
      categories: [],
      goodsCategories: [],
      announcements: [],
      myStats: {},
      hotSearchTags: ['学习资料', '二手教材', '电子产品', '租房', '兼职', '考研', '实习', '运动器材', '生活用品', '校园活动'],
      announceDetail: {},
      announceVisible: false,
      searchKeyword: '',
      searchScope: 'all',
      scrollPlaceholder: '搜索帖子、商品...',
      scrollTimer: null,
      scrollIndex: 0,
      followersVisible: false,
      followersList: [],
      followersLoading: false,
      buyLogisticsList: [],
      saleLogisticsList: [],
      logisticsScrollTimers: { buy: null, sale: null }
    }
  },
  computed: {
    userInfo() { return this.$store.getters.getUserInfo || {} },
    personalStats() {
      var self = this
      return [
        { label: '我的帖子', value: this.myStats.myPostCount || 0, color: '#409EFF', action: function() { self.$router.push('/myPost').catch(function() {}) } },
        { label: '我的商品', value: this.myStats.myGoodsCount || 0, color: '#67C23A', action: function() { self.$router.push('/myGoods').catch(function() {}) } },
        { label: '我的表白墙', value: this.myStats.myWallCount || 0, color: '#F56C6C', action: function() { self.$router.push('/myWall').catch(function() {}) } },
        { label: '我的关注', value: this.myStats.myFollowingCount || 0, color: '#E6A23C', action: function() { self.$router.push('/myFollowing').catch(function() {}) } },
        { label: '我的粉丝', value: this.myStats.myFollowerCount || 0, color: '#909399', action: function() { self.showFollowersDialog() } }
      ]
    },
    totalStats() {
      return [
        { label: '总浏览量', value: this.myStats.totalViewCount || 0, color: '#409EFF' },
        { label: '总点赞量', value: this.myStats.totalLikeCount || 0, color: '#F56C6C' },
        { label: '总评论量', value: this.myStats.totalCommentCount || 0, color: '#67C23A' },
        { label: '总收藏量', value: this.myStats.totalCollectCount || 0, color: '#E6A23C' },
        { label: '总分享量', value: this.myStats.totalShareCount || 0, color: '#909399' }
      ]
    }
  },
  created() { this.loadData() },
  mounted() { this.startSearchScroll(); this.$nextTick(function() { this.startLogisticsScroll('buy'); this.startLogisticsScroll('sale') }) },
  beforeDestroy() { if (this.scrollTimer) clearInterval(this.scrollTimer); this.pauseLogisticsScroll('buy'); this.pauseLogisticsScroll('sale') },
  methods: {
    async showFollowersDialog() {
      this.followersVisible = true
      this.followersLoading = true
      try {
        var userId = this.userInfo.userId
        var res = await this.$axios.get('/follow/followers/' + userId)
        if (res.code === 200) { this.followersList = res.data || [] }
      } catch (e) {}
      this.followersLoading = false
    },
    goMyLikes() {
      this.$router.push('/myInfo?tab=likes').catch(function() {})
    },
    async loadBuyLogistics() {
      try {
        const res = await this.$axios.get('/market-order/myOrder', { params: { pageNum: 1, pageSize: 100 } })
        if (res.code === 200) {
          const orders = (res.data.records || res.data || []).filter(o => o.orderStatus >= 2)
          this.buyLogisticsList = await Promise.all(orders.slice(0, 5).map(async (o) => {
            const item = { orderId: o.orderId, goodsName: o.goodsName, orderStatus: o.orderStatus, logisticsStatus: '', company: '', trackingNo: '' }
            try {
              const logRes = await this.$axios.get('/logistics/getByOrderId/' + o.orderId)
              if (logRes.code === 200 && logRes.data) {
                item.logisticsStatus = logRes.data.currentStatus || ''
                item.company = logRes.data.company || ''
                item.trackingNo = logRes.data.trackingNo || ''
              }
            } catch (e) {}
            return item
          }))
          this.$nextTick(() => { this.startLogisticsScroll('buy') })
        }
      } catch (e) {}
    },
    async loadSaleLogistics() {
      try {
        const res = await this.$axios.get('/market-order/mySalesOrders', { params: { pageNum: 1, pageSize: 100 } })
        if (res.code === 200) {
          const orders = (res.data.records || res.data || []).filter(o => o.orderStatus >= 2 && o.orderStatus <= 5)
          this.saleLogisticsList = await Promise.all(orders.slice(0, 5).map(async (o) => {
            const item = { orderId: o.orderId, goodsName: o.goodsName, orderStatus: o.orderStatus, logisticsStatus: '', company: '', trackingNo: '' }
            try {
              const logRes = await this.$axios.get('/logistics/getByOrderId/' + o.orderId)
              if (logRes.code === 200 && logRes.data) {
                item.logisticsStatus = logRes.data.currentStatus || ''
                item.company = logRes.data.company || ''
                item.trackingNo = logRes.data.trackingNo || ''
              }
            } catch (e) {}
            return item
          }))
          this.$nextTick(() => { this.startLogisticsScroll('sale') })
        }
      } catch (e) {}
    },
    dismissLogistics(type, orderId) {
      if (type === 'buy') {
        this.buyLogisticsList = this.buyLogisticsList.filter(i => i.orderId !== orderId)
      } else {
        this.saleLogisticsList = this.saleLogisticsList.filter(i => i.orderId !== orderId)
      }
    },
    startLogisticsScroll(type) {
      var self = this
      var list = type === 'buy' ? this.buyLogisticsList : this.saleLogisticsList
      if (list.length <= 1) return
      if (this.logisticsScrollTimers[type]) return
      this.logisticsScrollTimers[type] = setInterval(function() {
        var refName = type === 'buy' ? 'buyLogisticsWrap' : 'saleLogisticsWrap'
        var el = self.$refs[refName]
        if (!el) return
        if (el.scrollTop + el.clientHeight >= el.scrollHeight - 1) {
          el.scrollTop = 0
        } else {
          el.scrollTop += 1
        }
      }, 50)
    },
    pauseLogisticsScroll(type) {
      if (this.logisticsScrollTimers[type]) {
        clearInterval(this.logisticsScrollTimers[type])
        this.logisticsScrollTimers[type] = null
      }
    },
    formatShortTime(time) {
      if (!time) return ''
      const d = new Date(time)
      if (isNaN(d.getTime())) return time
      const pad = n => String(n).padStart(2, '0')
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    },
    showAnnounce(a) { this.announceDetail = a; this.announceVisible = true },
    goCategory(categoryId) { this.$router.push({ path: '/postList', query: { categoryId } }).catch(() => {}) },
    goGoodsCategory(goodsCategoryId) { this.$router.push({ path: '/goodsList', query: { goodsCategoryId } }).catch(() => {}) },
    doSearch() {
      const kw = this.searchKeyword.trim()
      if (!kw) return this.$message.warning('请输入搜索内容')
      this.$router.push({ path: '/search', query: { keyword: kw, scope: this.searchScope } }).catch(() => {})
    },
    startSearchScroll() {
      if (this.hotSearchTags.length <= 1) return
      this.scrollTimer = setInterval(() => {
        this.scrollIndex = (this.scrollIndex + 1) % this.hotSearchTags.length
        this.scrollPlaceholder = '搜索 ' + this.hotSearchTags[this.scrollIndex]
      }, 3000)
    },
    async loadData() {
      // 帖子 - 最新按创建时间
      try {
        const res = await this.$axios.get('/post/list', { params: { pageNum: 1, pageSize: 30 } })
        if (res.code === 200) {
          const records = res.data.records || res.data || []
          this.hotPosts = records.slice().sort((a, b) => (b.viewNum || 0) + (b.likeNum || 0) - (a.viewNum || 0) - (a.likeNum || 0)).slice(0, 5)
          this.latestPosts = records.slice().sort((a, b) => new Date(b.createTime) - new Date(a.createTime)).slice(0, 5)
        }
      } catch (e) {}
      // 商品 - 最新按创建时间，热门按浏览量
      try {
        const res = await this.$axios.get('/goods/getList', { params: { pageNum: 1, pageSize: 30, goodsStatus: true } })
        if (res.code === 200) {
          const goodsList = res.data.records || res.data || []
          this.hotGoods = goodsList.slice().sort((a, b) => (b.viewNum || 0) - (a.viewNum || 0)).slice(0, 5)
          this.latestGoods = goodsList.slice().sort((a, b) => new Date(b.createTime) - new Date(a.createTime)).slice(0, 5)
        }
      } catch (e) {}
      // 表白墙 - 最新按创建时间
      try {
        const res = await this.$axios.get('/wall/wallList', { params: { pageNum: 1, pageSize: 30, auditState: 1 } })
        if (res.code === 200) {
          const wallList = res.data.records || res.data || []
          this.hotWalls = wallList.slice().sort((a, b) => (b.viewNum || 0) + (b.likeNum || 0) - (a.viewNum || 0) - (a.likeNum || 0)).slice(0, 5)
          this.latestWalls = wallList.slice().sort((a, b) => new Date(b.createTime) - new Date(a.createTime)).slice(0, 5)
        }
      } catch (e) {}
      // 其他
      try { const res = await this.$axios.get('/category/list'); if (res.code === 200) this.categories = (res.data || []).concat({ categoryId: 0, categoryName: '其他' }) } catch (e) {}
      try { const res = await this.$axios.get('/goods-category/list'); if (res.code === 200) this.goodsCategories = (res.data || []).concat({ goodsCategoryId: 0, goodsCategoryName: '未分类' }) } catch (e) {}
      try { const res = await this.$axios.get('/announcement/publicList', { params: { pageNum: 1, pageSize: 5 } }); if (res.code === 200) this.announcements = res.data.records || res.data || [] } catch (e) {}
      try { const r = await this.$axios.get('/stats/myStats'); if (r.code === 200) { this.myStats = r.data || {} } } catch (e) {}
      // 加载物流信息
      this.loadBuyLogistics()
      this.loadSaleLogistics()
    },
    parseImages(images) {
      if (!images) return []
      try { const arr = JSON.parse(images); return Array.isArray(arr) ? arr : [images] }
      catch (e) { return images.split(',').filter(s => s.trim()) }
    }
  }
}
</script>

<style scoped>
/* ===== Card & Container Base ===== */
::v-deep .el-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1), box-shadow 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: none;
  overflow: hidden;
}
::v-deep .el-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

/* ===== Announcement / Welcome Section ===== */
.announce-scroll-wrap { flex: 1; overflow: hidden; height: 24px; position: relative; }
.announce-scroll-inner { position: relative; }
.announce-scroll-inner.scrolling { animation: announceScroll 12s linear infinite; }
.announce-scroll-item {
  height: 24px;
  line-height: 24px;
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.25s ease;
}
.announce-scroll-item:hover { color: #409EFF; }
@keyframes announceScroll {
  0%, 20% { transform: translateY(0); }
  25%, 45% { transform: translateY(-24px); }
  50%, 70% { transform: translateY(-48px); }
  75%, 95% { transform: translateY(-72px); }
  100% { transform: translateY(-120px); }
}

/* ===== Stat Row Flex (Gradient Stat Cards) ===== */
.stat-row-flex { display: flex; gap: 16px; }
.stat-col-flex { flex: 1; min-width: 0; }

.stat-card-clickable {
  cursor: pointer;
  transition: transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1), box-shadow 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.stat-card-clickable:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
}

/* Personal stat cards – gradient backgrounds with icon decoration */
.stat-row-flex:first-of-type .stat-col-flex:nth-child(1) ::v-deep .el-card {
  background: linear-gradient(135deg, #409EFF, #667eea);
  color: #fff;
  position: relative;
  overflow: hidden;
}
.stat-row-flex:first-of-type .stat-col-flex:nth-child(1) ::v-deep .el-card::before {
  content: '📝'; position: absolute; right: 12px; top: 12px; font-size: 48px; opacity: 0.12; transform: rotate(12deg);
}
.stat-row-flex:first-of-type .stat-col-flex:nth-child(2) ::v-deep .el-card {
  background: linear-gradient(135deg, #67C23A, #85ce61);
  color: #fff;
  position: relative;
  overflow: hidden;
}
.stat-row-flex:first-of-type .stat-col-flex:nth-child(2) ::v-deep .el-card::before {
  content: '🛍️'; position: absolute; right: 12px; top: 12px; font-size: 48px; opacity: 0.12; transform: rotate(12deg);
}
.stat-row-flex:first-of-type .stat-col-flex:nth-child(3) ::v-deep .el-card {
  background: linear-gradient(135deg, #F56C6C, #f89898);
  color: #fff;
  position: relative;
  overflow: hidden;
}
.stat-row-flex:first-of-type .stat-col-flex:nth-child(3) ::v-deep .el-card::before {
  content: '💕'; position: absolute; right: 12px; top: 12px; font-size: 48px; opacity: 0.12; transform: rotate(12deg);
}
.stat-row-flex:first-of-type .stat-col-flex:nth-child(4) ::v-deep .el-card {
  background: linear-gradient(135deg, #E6A23C, #f0c78a);
  color: #fff;
  position: relative;
  overflow: hidden;
}
.stat-row-flex:first-of-type .stat-col-flex:nth-child(4) ::v-deep .el-card::before {
  content: '👥'; position: absolute; right: 12px; top: 12px; font-size: 48px; opacity: 0.12; transform: rotate(12deg);
}
.stat-row-flex:first-of-type .stat-col-flex:nth-child(5) ::v-deep .el-card {
  background: linear-gradient(135deg, #909399, #b4b7bd);
  color: #fff;
  position: relative;
  overflow: hidden;
}
.stat-row-flex:first-of-type .stat-col-flex:nth-child(5) ::v-deep .el-card::before {
  content: '👤'; position: absolute; right: 12px; top: 12px; font-size: 48px; opacity: 0.12; transform: rotate(12deg);
}

/* Total stat cards – lighter gradient backgrounds */
.stat-row-flex:last-of-type .stat-col-flex:nth-child(1) ::v-deep .el-card {
  background: linear-gradient(135deg, #409EFF, #79bbff);
  color: #fff;
}
.stat-row-flex:last-of-type .stat-col-flex:nth-child(2) ::v-deep .el-card {
  background: linear-gradient(135deg, #F56C6C, #fab6b6);
  color: #fff;
}
.stat-row-flex:last-of-type .stat-col-flex:nth-child(3) ::v-deep .el-card {
  background: linear-gradient(135deg, #67C23A, #b3e19d);
  color: #fff;
}
.stat-row-flex:last-of-type .stat-col-flex:nth-child(4) ::v-deep .el-card {
  background: linear-gradient(135deg, #E6A23C, #f5daa3);
  color: #fff;
}
.stat-row-flex:last-of-type .stat-col-flex:nth-child(5) ::v-deep .el-card {
  background: linear-gradient(135deg, #909399, #cdd0d6);
  color: #fff;
}

/* Override text color inside gradient stat cards */
.stat-row-flex ::v-deep .el-card__body {
  border-radius: 12px;
}
.stat-row-flex ::v-deep .el-card__body div[style*="color:#999"] {
  color: rgba(255, 255, 255, 0.9) !important;
}

/* ===== Stat Values – Larger & Bolder ===== */
.stat-value {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.08);
}
.stat-row { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid rgba(0,0,0,0.04); height: 46px; box-sizing: border-box; }
.stat-row:last-child { border-bottom: none; }
.stat-label { font-size: 13px; color: #666; font-weight: 500; }
.stat-clickable {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border-radius: 10px;
  padding: 10px 10px;
  margin: 0 -4px;
}
.stat-clickable:hover { background: linear-gradient(135deg, #f0f5ff, #e6f0ff); transform: translateX(4px); box-shadow: 0 2px 8px rgba(64,158,255,0.1); }

/* ===== Dashboard Top Cards ===== */
.dashboard-top-card { height: 100%; }
.dashboard-top-card ::v-deep .el-card__header {
  border-radius: 12px 12px 0 0;
  padding: 14px 20px;
}
.dashboard-top-card ::v-deep .el-card__body {
  min-height: unset;
  max-height: unset;
  overflow-y: hidden;
  padding: 12px 20px;
}

/* ===== List Cards ===== */
.list-card { height: 100%; }
.list-card ::v-deep .el-card__header {
  border-radius: 12px 12px 0 0;
  padding: 14px 20px;
  height: 52px;
  box-sizing: border-box;
}
.list-card ::v-deep .el-card__body {
  overflow-y: hidden;
  padding: 12px 20px;
  box-sizing: border-box;
}

/* ===== Hot Items ===== */
.hot-item {
  display: flex;
  align-items: flex-start;
  padding: 10px 12px;
  background: linear-gradient(135deg, #fafafa, #f5f7fa);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  margin-bottom: 8px;
  border: 1px solid transparent;
}
.hot-item:hover {
  background: linear-gradient(135deg, #f0f5ff, #e6f0ff);
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.12);
  border-color: rgba(64, 158, 255, 0.1);
}
.hot-item:last-child { margin-bottom: 0; }
.hot-rank {
  width: 30px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-radius: 10px;
  font-size: 13px;
  font-weight: bold;
  color: #fff;
  margin-right: 12px;
  flex-shrink: 0;
  margin-top: 1px;
  transition: transform 0.3s ease;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}
.hot-item:hover .hot-rank { transform: scale(1.15) rotate(-5deg); }
.rank-0 { background: linear-gradient(135deg, #F56C6C, #f89898); }
.rank-1 { background: linear-gradient(135deg, #E6A23C, #f0c78a); }
.rank-2 { background: linear-gradient(135deg, #409EFF, #79bbff); }
.rank-default { background: linear-gradient(135deg, #C0C4CC, #dcdfe6); }
.hot-info { flex: 1; min-width: 0; }
.hot-title {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s ease;
  font-weight: 500;
}
.hot-item:hover .hot-title { color: #409EFF; }
.hot-meta { font-size: 12px; color: #909399; margin-top: 6px; display: flex; gap: 12px; }
.hot-time { font-size: 11px; color: #c0c4cc; margin-left: auto; flex-shrink: 0; }

/* ===== Topic Tags ===== */
.topic-grid { display: flex; flex-wrap: wrap; gap: 8px; }
.topic-tag { cursor: pointer; transition: all 0.25s ease; }
.topic-tag:hover { transform: translateY(-2px); box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }

/* ===== Logistics Items ===== */
.logistics-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 8px;
}
.logistics-item:hover {
  background: #f0f5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.logistics-placeholder { cursor: default; }
.logistics-placeholder:hover { background: #fafafa; transform: none; box-shadow: none; }
.logistics-main { display: flex; justify-content: space-between; align-items: center; }
.logistics-goods { font-size: 14px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; margin-right: 8px; }
.logistics-detail { font-size: 12px; color: #999; margin-top: 4px; }

/* ===== Latest Cards ===== */
.latest-card {
  padding: 10px 12px;
  background: linear-gradient(135deg, #fafafa, #f5f7fa);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  margin-bottom: 8px;
  border: 1px solid transparent;
}
.latest-card:hover {
  background: linear-gradient(135deg, #f0f5ff, #e6f0ff);
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.12);
  border-color: rgba(64, 158, 255, 0.1);
}
.latest-placeholder { cursor: default; }
.latest-placeholder:hover { background: linear-gradient(135deg, #fafafa, #f5f7fa); transform: none; box-shadow: none; border-color: transparent; }
.hot-placeholder { cursor: default; }
.hot-placeholder:hover { background: linear-gradient(135deg, #fafafa, #f5f7fa); transform: none; box-shadow: none; border-color: transparent; }
.latest-title {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
  transition: color 0.3s ease;
  font-weight: 500;
}
.latest-card:hover .latest-title { color: #409EFF; }
.latest-meta { font-size: 12px; color: #909399; display: flex; justify-content: space-between; align-items: center; }
.latest-time { font-size: 11px; color: #c0c4cc; flex-shrink: 0; margin-left: 6px; }
.wall-title-ellipsis { max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* ===== Logistics Scroll ===== */
.logistics-scroll-wrap { max-height: 280px; overflow-y: auto; position: relative; }
.logistics-scroll-inner { position: relative; }

/* ===== Avatar Rounding ===== */
::v-deep .el-avatar {
  border-radius: 50%;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}
::v-deep .el-avatar:hover {
  transform: scale(1.08);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* ===== Dialog Beautification ===== */
::v-deep .announce-dialog .el-dialog__body {
  padding: 0;
}
.announce-dialog-body {
  padding: 24px 28px;
}
.announce-content-text {
  line-height: 1.8;
  white-space: pre-wrap;
  font-size: 14px;
  color: #303133;
  background: linear-gradient(135deg, #f8f9fa, #f0f5ff);
  border-radius: 14px;
  padding: 20px;
  border: 1px solid rgba(64, 158, 255, 0.08);
}
.announce-images {
  margin-top: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.announce-images .el-image {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.announce-images .el-image:hover {
  transform: scale(1.05);
}

.dialog-empty {
  text-align: center;
  color: #a0a3a8;
  padding: 48px 0;
  font-size: 14px;
  font-weight: 500;
}
.follower-item {
  display: flex;
  align-items: center;
  padding: 14px 18px;
  border-radius: 12px;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  margin-bottom: 8px;
  background: linear-gradient(135deg, #fafafa, #f5f7fa);
  border: 1px solid transparent;
}
.follower-item:hover {
  background: linear-gradient(135deg, #f0f5ff, #e6f0ff);
  transform: translateX(6px);
  border-color: rgba(64, 158, 255, 0.1);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.08);
}
.follower-info {
  margin-left: 14px;
  flex: 1;
  overflow: hidden;
}
.follower-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
