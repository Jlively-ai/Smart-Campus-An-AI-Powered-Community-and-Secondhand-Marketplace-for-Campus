<!--
  组件名：DashboardIndex
  功能描述：仪表盘首页，展示系统各模块统计数据
  主要职责：
    1. 展示系统信息（注册用户数、管理员数、公告数）
    2. 展示社区信息（帖子数、评论数、表白墙数、帖子分类统计）
    3. 展示二手交易模块（商品数、订单数、销售额、商品分类统计、订单状态分布）
-->
<template>
  <div>
    <!-- ===== 系统信息统计卡片 ===== -->
    <el-card shadow="never" class="section-card">
      <div slot="header" class="section-header"><i class="el-icon-monitor"></i> 系统信息</div>
      <el-row :gutter="20">
        <!-- 注册用户数 -->
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card-wrap stat-blue">
            <div class="stat-card">
              <div class="stat-icon"><i class="el-icon-user"></i></div>
              <div class="stat-num">{{ stats.userTotal }}</div>
              <div class="stat-label">注册用户数</div>
            </div>
          </el-card>
        </el-col>
        <!-- 管理员数 -->
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card-wrap stat-orange">
            <div class="stat-card">
              <div class="stat-icon"><i class="el-icon-s-custom"></i></div>
              <div class="stat-num">{{ stats.adminTotal }}</div>
              <div class="stat-label">管理员数</div>
            </div>
          </el-card>
        </el-col>
        <!-- 公告数 -->
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card-wrap stat-green">
            <div class="stat-card">
              <div class="stat-icon"><i class="el-icon-bell"></i></div>
              <div class="stat-num">{{ stats.announcementTotal }}</div>
              <div class="stat-label">公告数</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- ===== 社区信息统计卡片 ===== -->
    <el-card shadow="never" class="section-card">
      <div slot="header" class="section-header"><i class="el-icon-chat-dot-round"></i> 社区信息</div>
      <el-row :gutter="20" style="margin-bottom:20px;">
        <!-- 帖子数 -->
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card-wrap stat-green">
            <div class="stat-card">
              <div class="stat-icon"><i class="el-icon-document"></i></div>
              <div class="stat-num">{{ stats.postTotal }}</div>
              <div class="stat-label">帖子数</div>
            </div>
          </el-card>
        </el-col>
        <!-- 评论数 -->
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card-wrap stat-gray">
            <div class="stat-card">
              <div class="stat-icon"><i class="el-icon-chat-line-round"></i></div>
              <div class="stat-num">{{ stats.commentTotal }}</div>
              <div class="stat-label">评论数</div>
            </div>
          </el-card>
        </el-col>
        <!-- 表白墙数 -->
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card-wrap stat-pink">
            <div class="stat-card">
              <div class="stat-icon"><i class="el-icon-magic-stick"></i></div>
              <div class="stat-num">{{ stats.wallTotal }}</div>
              <div class="stat-label">表白墙数</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <!-- 帖子分类统计进度条 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="chart-header">帖子分类统计</div>
            <div v-if="postData.length === 0" class="empty-data">暂无数据</div>
            <div v-else class="chart-body">
              <div style="flex:1;">
                <div v-for="(item, idx) in postData.slice(0, 10)" :key="'post-l-'+idx" class="progress-row">
                  <span class="progress-label">{{ item.name }}</span>
                  <el-progress :percentage="item.percentage" :color="item.color" :stroke-width="16" :text-inside="true" style="flex:1;"></el-progress>
                  <span class="progress-value">{{ item.value }}</span>
                </div>
              </div>
              <div v-if="postData.length > 10" class="chart-divider"></div>
              <div v-if="postData.length > 10" style="flex:1;">
                <div v-for="(item, idx) in postData.slice(10)" :key="'post-r-'+idx" class="progress-row">
                  <span class="progress-label">{{ item.name }}</span>
                  <el-progress :percentage="item.percentage" :color="item.color" :stroke-width="16" :text-inside="true" style="flex:1;"></el-progress>
                  <span class="progress-value">{{ item.value }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- ===== 二手交易模块统计卡片 ===== -->
    <el-card shadow="never" class="section-card">
      <div slot="header" class="section-header"><i class="el-icon-shopping-bag-1"></i> 二手交易模块</div>
      <el-row :gutter="20" style="margin-bottom:20px;">
        <!-- 商品数 -->
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card-wrap stat-orange">
            <div class="stat-card">
              <div class="stat-icon"><i class="el-icon-goods"></i></div>
              <div class="stat-num">{{ stats.goodsTotal }}</div>
              <div class="stat-label">商品数</div>
            </div>
          </el-card>
        </el-col>
        <!-- 订单数 -->
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card-wrap stat-red">
            <div class="stat-card">
              <div class="stat-icon"><i class="el-icon-s-order"></i></div>
              <div class="stat-num">{{ stats.orderTotal }}</div>
              <div class="stat-label">订单数</div>
            </div>
          </el-card>
        </el-col>
        <!-- 销售额 -->
        <el-col :span="5">
          <el-card shadow="hover" class="stat-card-wrap stat-green">
            <div class="stat-card">
              <div class="stat-icon"><i class="el-icon-money"></i></div>
              <div class="stat-num">￥{{ salesData.totalRevenue || 0 }}</div>
              <div class="stat-label">销售额</div>
            </div>
          </el-card>
        </el-col>
        <!-- 商品评论数 -->
        <el-col :span="5">
          <el-card shadow="hover" class="stat-card-wrap stat-blue">
            <div class="stat-card">
              <div class="stat-icon"><i class="el-icon-chat-dot-square"></i></div>
              <div class="stat-num">{{ stats.goodsCommentTotal }}</div>
              <div class="stat-label">商品评论数</div>
            </div>
          </el-card>
        </el-col>
        <!-- 订单评价数 -->
        <el-col :span="5">
          <el-card shadow="hover" class="stat-card-wrap stat-orange">
            <div class="stat-card">
              <div class="stat-icon"><i class="el-icon-star-off"></i></div>
              <div class="stat-num">{{ stats.reviewTotal }}</div>
              <div class="stat-label">订单评价数</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <!-- 商品分类统计和订单状态分布 -->
      <el-row :gutter="20">
        <!-- 商品分类统计进度条 -->
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="chart-header">商品分类统计</div>
            <div v-if="goodsData.length === 0" class="empty-data">暂无数据</div>
            <div v-else class="chart-body">
              <div style="flex:1;">
                <div v-for="(item, idx) in goodsData.slice(0, 10)" :key="'goods-l-'+idx" class="progress-row">
                  <span class="progress-label">{{ item.name }}</span>
                  <el-progress :percentage="item.percentage" :color="item.color" :stroke-width="16" :text-inside="true" style="flex:1;"></el-progress>
                  <span class="progress-value">{{ item.value }}</span>
                </div>
              </div>
              <div v-if="goodsData.length > 10" class="chart-divider"></div>
              <div v-if="goodsData.length > 10" style="flex:1;">
                <div v-for="(item, idx) in goodsData.slice(10)" :key="'goods-r-'+idx" class="progress-row">
                  <span class="progress-label">{{ item.name }}</span>
                  <el-progress :percentage="item.percentage" :color="item.color" :stroke-width="16" :text-inside="true" style="flex:1;"></el-progress>
                  <span class="progress-value">{{ item.value }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <!-- 订单状态分布进度条 -->
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="chart-header">订单状态分布</div>
            <div v-if="!salesData.statusData || salesData.statusData.length === 0" class="empty-data">暂无数据</div>
            <div v-else>
              <div v-for="(item, idx) in salesData.statusData" :key="'status-'+idx" class="progress-row">
                <span class="progress-label">{{ getOrderStatusName(item.name) }}</span>
                <el-progress :percentage="item.percentage" :color="getOrderStatusColor(item.name)" :stroke-width="16" :text-inside="true" style="flex:1;"></el-progress>
                <span class="progress-value">{{ item.value }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { getUserTotal, getPostTotal, getGoodsTotal, getOrderTotal, getCommentTotal, getWallTotal, getPostData, getGoodsByCategory, getSalesData, getGoodsCommentTotal, getOrderReviewTotal, getAnnouncementList } from '@/api/manage'
import request from '@/utils/request'

/** 图表配色方案 */
const COLORS = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#f47983', '#00d1b2', '#7b68ee']

/**
 * 格式化图表数据
 * 将后端返回的原始数据转换为进度条组件所需的格式
 * @param {Array} rawData - 原始数据数组
 * @returns {Array} 格式化后的数据数组
 */
function formatChartData(rawData) {
  if (!rawData || !Array.isArray(rawData) || rawData.length === 0) return []
  const total = rawData.reduce((sum, item) => sum + (item.value || item.count || 0), 0)
  if (total === 0) return []
  return rawData.map((item, idx) => ({
    name: item.name || item.categoryName || item.goodsCategoryName || item.category || '未知',
    value: item.value || item.count || 0,
    percentage: Math.round(((item.value || item.count || 0) / total) * 100),
    color: COLORS[idx % COLORS.length]
  }))
}

export default {
  data() {
    return {
      /** 各模块统计数据 */
      stats: { userTotal: 0, postTotal: 0, goodsTotal: 0, orderTotal: 0, commentTotal: 0, wallTotal: 0, goodsCommentTotal: 0, reviewTotal: 0, adminTotal: 0, announcementTotal: 0 },
      /** 帖子分类统计数据 */
      postData: [],
      /** 商品分类统计数据 */
      goodsData: [],
      /** 销售数据（含总额和状态分布） */
      salesData: {}
    }
  },
  /** 生命周期：组件创建时加载统计数据 */
  created() { this.loadStats() },
  methods: {
    /**
     * 获取订单状态中文名
     * @param {string|number} name - 订单状态码
     * @returns {string} 状态中文名
     */
    getOrderStatusName(name) {
      var statusMap = { '0': '未支付', '1': '已支付', '2': '已发货', '3': '已签收', '4': '退款中', '5': '退货中', '6': '已退款', '7': '已退货', '8': '已完成', '9': '已评价' }
      return statusMap[String(name)] || name
    },
    /**
     * 获取订单状态对应颜色
     * @param {string|number} name - 订单状态码
     * @returns {string} 颜色值
     */
    getOrderStatusColor(name) {
      var statusColors = {
        '0': '#909399', '1': '#409EFF', '2': '#67C23A', '3': '#2ecc71',
        '4': '#F56C6C', '5': '#9b59b6', '6': '#e74c3c', '7': '#8e44ad',
        '8': '#2ecc71', '9': '#f39c12',
        '未支付': '#909399', '已支付': '#409EFF', '已发货': '#67C23A', '已签收': '#2ecc71',
        '退款中': '#F56C6C', '退货中': '#9b59b6', '已退款': '#e74c3c', '已退货': '#8e44ad',
        '已完成': '#2ecc71', '已评价': '#f39c12'
      }
      return statusColors[String(name)] || '#909399'
    },
    /**
     * 加载所有统计数据
     * 并行请求各模块统计接口，静默处理错误
     */
    async loadStats() {
      try { const r = await getUserTotal(); this.stats.userTotal = r.data || 0 } catch(e) {}
      try { const r = await getPostTotal(); this.stats.postTotal = r.data || 0 } catch(e) {}
      try { const r = await getGoodsTotal(); this.stats.goodsTotal = r.data || 0 } catch(e) {}
      try { const r = await getOrderTotal(); this.stats.orderTotal = r.data || 0 } catch(e) {}
      try { const r = await getCommentTotal(); this.stats.commentTotal = r.data || 0 } catch(e) {}
      try { const r = await getWallTotal(); this.stats.wallTotal = r.data || 0 } catch(e) {}
      try { const r = await getGoodsCommentTotal(); this.stats.goodsCommentTotal = r.data || 0 } catch(e) {}
      try { const r = await getOrderReviewTotal(); this.stats.reviewTotal = r.data || 0 } catch(e) {}
      // 管理员列表需要超级管理员权限，普通管理员静默跳过
      try { const r = await request({ url: '/admin/info/list', method: 'get', params: { pageNum: 1, pageSize: 1 }, _silent: true }); this.stats.adminTotal = r.data.total || 0 } catch(e) { this.stats.adminTotal = '-' }
      try { const r = await getAnnouncementList({ pageNum: 1, pageSize: 1 }); this.stats.announcementTotal = r.data.total || 0 } catch(e) {}
      try { const r = await getPostData(); this.postData = formatChartData(r.data) } catch(e) {}
      try { const r = await getGoodsByCategory(); this.goodsData = formatChartData(r.data) } catch(e) {}
      try { const r = await getSalesData(); this.salesData = r.data || {}; if (this.salesData.statusData) this.salesData.statusData = formatChartData(this.salesData.statusData) } catch(e) {}
    }
  }
}
</script>

<style scoped>
/* 区块卡片 */
.section-card {
  margin-bottom: 24px;
  border-radius: 16px;
}
/* 区块标题 */
.section-header {
  font-weight: 600;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1f2937;
}
.section-header i {
  font-size: 18px;
  color: #409EFF;
}
/* 统计卡片内容 */
.stat-card {
  text-align: center;
  padding: 24px 0 20px;
  border-radius: 16px;
}
/* 统计卡片容器：悬浮动效 */
.stat-card-wrap {
  border-radius: 16px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
}
.stat-card-wrap:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12) !important;
}
/* 统计图标 */
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
/* 各颜色主题的图标背景 */
.stat-blue .stat-icon { background: linear-gradient(135deg, #409EFF, #79bbff); }
.stat-orange .stat-icon { background: linear-gradient(135deg, #E6A23C, #f5c842); }
.stat-green .stat-icon { background: linear-gradient(135deg, #67C23A, #85d95c); }
.stat-gray .stat-icon { background: linear-gradient(135deg, #909399, #b0b3b8); }
.stat-pink .stat-icon { background: linear-gradient(135deg, #f47983, #ff9aa2); }
.stat-red .stat-icon { background: linear-gradient(135deg, #F56C6C, #ff8e8e); }
/* 统计数字 */
.stat-num {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}
/* 统计标签 */
.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 8px;
  font-weight: 500;
}
/* 图表卡片 */
.chart-card {
  border-radius: 16px;
}
.chart-header {
  font-weight: 600;
  color: #374151;
  font-size: 15px;
}
/* 图表主体：双列布局 */
.chart-body {
  display: flex;
  gap: 20px;
}
/* 图表分隔线 */
.chart-divider {
  width: 1px;
  background: #e5e7eb;
}
/* 进度条行 */
.progress-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
/* 进度条标签 */
.progress-label {
  width: 70px;
  text-align: right;
  margin-right: 12px;
  font-size: 13px;
  color: #4b5563;
  font-weight: 500;
}
/* 进度条数值 */
.progress-value {
  width: 44px;
  text-align: right;
  font-size: 13px;
  color: #6b7280;
  font-weight: 600;
}
/* 空数据提示 */
.empty-data {
  text-align: center;
  color: #9ca3af;
  padding: 40px 0;
  font-size: 14px;
}
</style>
