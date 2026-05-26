<!--
  组件名：OrderIndex
  功能描述：订单管理页面
  主要职责：展示订单列表，支持搜索、查看详情、导出等操作
-->
<template>
  <div>
    <!-- 搜索筛选区域 -->
   
    <el-card shadow="never" class="search-card">
      <div class="search-bar">
        <el-input v-model="searchForm.orderId" placeholder="搜索订单号" prefix-icon="el-icon-search" class="search-input" size="small" clearable @clear="loadData" @keyup.enter.native="loadData"></el-input>
        <el-select v-model="searchForm.orderStatus" placeholder="筛选状态" class="search-select" size="small" clearable @change="loadData">
          <el-option label="未支付" :value="0"></el-option>
          <el-option label="已支付" :value="1"></el-option>
          <el-option label="已发货" :value="2"></el-option>
          <el-option label="已签收" :value="3"></el-option>
          <el-option label="退款中" :value="4"></el-option>
          <el-option label="退货中" :value="5"></el-option>
          <el-option label="已退款" :value="6"></el-option>
          <el-option label="已退货" :value="7"></el-option>
          <el-option label="已完成" :value="8"></el-option>
          <el-option label="已评价" :value="9"></el-option>
        </el-select>
        <el-select v-model="sortField" placeholder="排序" size="small" style="width:120px;" clearable @change="handleSortChange">
          <el-option label="下单时间 ↓" value="createTime_desc" />
          <el-option label="下单时间 ↑" value="createTime_asc" />
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
        <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        <el-button type="success" size="small" icon="el-icon-download" class="btn-export" @click="exportData">导出</el-button>
      </div>
    </el-card>
    <!-- 数据表格 -->
   

    <el-table :data="list" v-loading="loading" border @selection-change="handleSelectionChange" class="data-table">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="orderId" label="订单号" width="200" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.orderId | formatId('order') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="goodsId" label="商品ID" width="200" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showGoodsDetail(scope.row.goodsId)">{{ scope.row.goodsId | formatId('goods') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="buyerId" label="买家ID" width="200" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showUserDetail(scope.row.buyerId)">{{ scope.row.buyerId | formatId('user') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="sellerId" label="卖家ID" width="200" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showUserDetail(scope.row.sellerId)">{{ scope.row.sellerId | formatId('user') }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="buyCount" label="数量" width="60" align="center"></el-table-column>
      <el-table-column prop="totalPrice" label="总价" width="80">
        <template slot-scope="scope"><span style="color:#f56c6c;font-weight:600;">￥{{ scope.row.totalPrice }}</span></template>
      </el-table-column>
      <el-table-column prop="orderStatus" label="状态" width="90" align="center">
        <template slot-scope="scope"><el-tag :type="statusTagType(scope.row.orderStatus)">{{ statusText(scope.row.orderStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180">
        <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
   
    <el-pagination class="pagination-bar" @current-change="handlePageChange" :current-page="pageNum" :page-size.sync="pageSize" :page-sizes="[10, 20, 50]" @size-change="handleSizeChange" :total="total" layout="total, sizes, prev, pager, next"></el-pagination>

    
    <el-dialog title="订单详情" :visible.sync="detailVisible" width="900px" top="5vh" class="order-dialog">
      <div v-loading="detailLoading" v-if="detailData">
        
        <el-card shadow="hover" class="step-card">
          <div class="step-title">订单进度</div>
          <el-steps :active="getStepActive(detailData.orderStatus)" align-center finish-status="success">
            <el-step title="下单" :description="formatTime(detailData.createTime)"></el-step>
            <el-step title="已支付" :description="detailData.orderStatus >= 1 && detailData.payTime ? formatTime(detailData.payTime) : ''"></el-step>
            <el-step title="已发货" :description="detailData.orderStatus >= 2 && detailData.sendTime ? formatTime(detailData.sendTime) : ''"></el-step>
            <el-step title="已签收" :description="detailData.orderStatus >= 3 && detailData.receiptTime ? formatTime(detailData.receiptTime) : ''"></el-step>
            <el-step title="已完成" :description="detailData.orderStatus >= 8 && detailData.receiptTime ? formatTime(detailData.receiptTime) : ''"></el-step>
          </el-steps>
        </el-card>

        
        <el-card shadow="hover" class="info-card">
          <div slot="header" class="info-header"><i class="el-icon-document"></i>基础信息</div>
          <table class="od-table">
            <tr>
              <td class="od-label">订单编号</td><td>{{ detailData.orderId | formatId('order') }}</td>
              <td class="od-label">订单状态</td><td><el-tag :type="statusTagType(detailData.orderStatus)" size="small">{{ statusText(detailData.orderStatus) }}</el-tag></td>
            </tr>
            <tr>
              <td class="od-label">下单时间</td><td>{{ formatTime(detailData.createTime) }}</td>
              <td class="od-label">支付时间</td><td>{{ detailData.payTime ? formatTime(detailData.payTime) : '-' }}</td>
            </tr>
            <tr>
              <td class="od-label">发货时间</td><td>{{ detailData.sendTime ? formatTime(detailData.sendTime) : '-' }}</td>
              <td class="od-label">收货时间</td><td>{{ detailData.receiptTime ? formatTime(detailData.receiptTime) : '-' }}</td>
            </tr>
            <tr>
              <td class="od-label">购买数量</td><td>{{ detailData.buyCount }}</td>
              <td class="od-label">支付方式</td><td>在线支付</td>
            </tr>
            <tr>
              <td class="od-label">商品单价</td><td><span style="color:#F56C6C;">￥{{ detailGoods.goodsPrice || '-' }}</span></td>
              <td class="od-label">实付金额</td><td><span style="color:#F56C6C;font-weight:bold;font-size:16px;">￥{{ detailData.totalPrice }}</span></td>
            </tr>
          </table>
        </el-card>

        
        <el-row :gutter="20" class="user-row">
          <el-col :span="12">
            <el-card shadow="hover" class="user-card">
              <div slot="header" class="info-header"><i class="el-icon-user"></i>购买人信息</div>
              <div class="user-profile" @click="showUserDetail(detailData.buyerId)">
                <el-avatar v-if="detailBuyer.avatar" :src="detailBuyer.avatar" :size="48" class="user-avatar"></el-avatar>
                <el-avatar v-else :size="48" class="user-avatar" style="background: linear-gradient(135deg, #409EFF, #79bbff);">{{ (detailBuyer.nickname || '买')[0] }}</el-avatar>
                <div class="user-meta">
                  <div class="user-name">{{ detailBuyer.nickname || '未知' }}<i class="el-icon-arrow-right" style="font-size:12px;margin-left:4px;"></i></div>
                  <div class="user-hint">点击查看用户详情</div>
                </div>
              </div>
              <table class="od-table">
                <tr><td class="od-label">用户编号</td><td>{{ detailData.buyerId | formatId('user') }}</td></tr>
                <tr><td class="od-label">用户昵称</td><td>{{ detailBuyer.nickname || '-' }}</td></tr>
                <tr><td class="od-label">联系方式</td><td>{{ detailBuyer.phone || '-' }}</td></tr>
              </table>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="user-card">
              <div slot="header" class="info-header"><i class="el-icon-s-custom"></i>出售人信息</div>
              <div class="user-profile" @click="showUserDetail(detailData.sellerId)">
                <el-avatar v-if="detailSeller.avatar" :src="detailSeller.avatar" :size="48" class="user-avatar"></el-avatar>
                <el-avatar v-else :size="48" class="user-avatar" style="background: linear-gradient(135deg, #67C23A, #85d95c);">{{ (detailSeller.nickname || '卖')[0] }}</el-avatar>
                <div class="user-meta">
                  <div class="user-name">{{ detailSeller.nickname || '未知' }}<i class="el-icon-arrow-right" style="font-size:12px;margin-left:4px;"></i></div>
                  <div class="user-hint">点击查看用户详情</div>
                </div>
              </div>
              <table class="od-table">
                <tr><td class="od-label">用户编号</td><td>{{ detailData.sellerId | formatId('user') }}</td></tr>
                <tr><td class="od-label">用户昵称</td><td>{{ detailSeller.nickname || '-' }}</td></tr>
                <tr><td class="od-label">联系方式</td><td>{{ detailSeller.phone || '-' }}</td></tr>
              </table>
            </el-card>
          </el-col>
        </el-row>

        
        <el-card shadow="hover" class="info-card">
          <div slot="header" class="info-header"><i class="el-icon-goods"></i>商品信息</div>
          <div class="goods-body">
            <div v-if="getGoodsImgList().length > 0" class="goods-images">
              <el-image :src="getGoodsImgList()[0]" class="goods-main-img" fit="cover" :preview-src-list="getGoodsImgList()"></el-image>
              <div v-if="getGoodsImgList().length > 1" class="goods-thumb-list">
                <el-image v-for="(img, idx) in getGoodsImgList().slice(1, 5)" :key="idx" :src="img" class="goods-thumb" fit="cover" :preview-src-list="getGoodsImgList()"></el-image>
              </div>
            </div>
            <div class="goods-info">
              <table class="od-table">
                <tr><td class="od-label">商品编号</td><td><el-link type="primary" @click="showGoodsDetail(detailData.goodsId)">{{ detailData.goodsId | formatId('goods') }}</el-link></td></tr>
                <tr><td class="od-label">商品标题</td><td>{{ detailGoods.goodsName || '-' }}</td></tr>
                <tr><td class="od-label">商品描述</td><td>{{ detailGoods.goodsDetail || detailGoods.goodsDesc || '-' }}</td></tr>
                <tr><td class="od-label">库存数量</td><td>{{ detailGoods.goodsCount || '-' }}</td></tr>
                <tr><td class="od-label">商品价格</td><td><span style="color:#F56C6C;">￥{{ detailGoods.goodsPrice || '-' }}</span></td></tr>
                <tr><td class="od-label">商品状态</td><td><el-tag :type="detailGoods.goodsStatus ? 'success' : 'info'" size="mini">{{ detailGoods.goodsStatus ? '上架' : '下架' }}</el-tag></td></tr>
              </table>
            </div>
          </div>
        </el-card>

        
        <el-card v-if="isAfterSaleStatus(detailData.orderStatus)" shadow="hover" class="info-card">
          <div slot="header" class="info-header"><i class="el-icon-warning-outline"></i>售后信息</div>
          <div v-if="detailAfterSale">
            <table class="od-table">
              <tr>
                <td class="od-label">售后类型</td><td><el-tag :type="afterSaleTypeTag(detailAfterSale.type)" size="small">{{ detailAfterSale.type || '-' }}</el-tag></td>
                <td class="od-label">售后原因</td><td>{{ detailAfterSale.reason || '-' }}</td>
              </tr>
              <tr>
                <td class="od-label">申请金额</td><td>{{ detailAfterSale.amount ? '￥' + detailAfterSale.amount : '-' }}</td>
                <td class="od-label">处理结果</td><td>
                  <el-tag v-if="detailAfterSale.handleResult" :type="detailAfterSale.handleResult === '同意' ? 'success' : 'danger'" size="small">{{ detailAfterSale.handleResult }}</el-tag>
                  <span v-else style="color:#E6A23C;">待处理</span>
                </td>
              </tr>
              <tr v-if="detailAfterSale.returnCompany">
                <td class="od-label">退货快递</td><td>{{ detailAfterSale.returnCompany }}</td>
                <td class="od-label">退货单号</td><td>{{ detailAfterSale.returnTrackingNo || '-' }}</td>
              </tr>
              <tr v-if="detailAfterSale.returnConfirmed">
                <td class="od-label">退货确认</td><td colspan="3"><el-tag type="success" size="mini">卖家已确认退货</el-tag></td>
              </tr>
              <tr v-if="detailAfterSale.description">
                <td class="od-label">问题描述</td><td colspan="3" style="white-space:pre-wrap;">{{ detailAfterSale.description }}</td>
              </tr>
              <tr v-if="detailAfterSale.images">
                <td class="od-label">凭证图片</td><td colspan="3">
                  <div class="aftersale-images">
                    <el-image v-for="(img, idx) in parseAfterSaleImages(detailAfterSale.images)" :key="idx" :src="img" class="aftersale-img" fit="cover" :preview-src-list="parseAfterSaleImages(detailAfterSale.images)"></el-image>
                  </div>
                </td>
              </tr>
            </table>
          </div>
          <div v-else class="empty-block">暂无详细售后信息</div>
        </el-card>

        
        <el-card shadow="hover" class="info-card">
          <div slot="header" class="info-header"><i class="el-icon-truck"></i>物流信息</div>
          <div v-if="detailLogistics">
            <table class="od-table">
              <tr>
                <td class="od-label">物流公司</td><td>{{ detailLogistics.company || '-' }}</td>
                <td class="od-label">运单号</td><td>{{ detailLogistics.trackingNo || '-' }}</td>
              </tr>
              <tr>
                <td class="od-label">当前状态</td><td><el-tag size="small">{{ detailLogistics.currentStatus || '运输中' }}</el-tag></td>
                <td class="od-label">更新时间</td><td>{{ formatTime(detailLogistics.createTime) }}</td>
              </tr>
              <tr>
                <td class="od-label">物流详情</td>
                <td colspan="3"><el-button type="text" @click="logisticsDialogVisible = true"><i class="el-icon-location-outline"></i> 查看物流轨迹</el-button></td>
              </tr>
            </table>
          </div>
          <div v-else-if="detailData.orderStatus >= 2" class="empty-block">
            <i class="el-icon-truck empty-icon"></i>
            <div>暂无物流信息</div>
          </div>
          <div v-else class="empty-block">
            <i class="el-icon-box empty-icon"></i>
            <div>等待卖家发货</div>
          </div>
        </el-card>
      </div>
    </el-dialog>

    
    <el-dialog title="物流轨迹" :visible.sync="logisticsDialogVisible" width="550px" append-to-body class="logistics-dialog">
      <div v-if="detailLogisticsHistory && detailLogisticsHistory.length > 0">
        <el-timeline>
          <el-timeline-item v-for="(item, idx) in detailLogisticsHistory" :key="idx" :timestamp="formatTime(item.createTime)" placement="top" :type="idx === 0 ? 'primary' : 'info'" :size="idx === 0 ? 'normal' : 'small'">
            <el-card shadow="never" class="timeline-card">
              <div class="timeline-status">{{ item.currentStatus || '状态更新' }}</div>
              <div class="timeline-detail">{{ item.detail || '暂无详细信息' }}</div>
              <div class="timeline-meta" v-if="item.company || item.trackingNo">{{ item.company || '' }} {{ item.trackingNo ? '运单号: ' + item.trackingNo : '' }}</div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
      <div v-else class="empty-block">暂无物流轨迹信息</div>
      <span slot="footer"><el-button @click="logisticsDialogVisible = false">关闭</el-button></span>
    </el-dialog>

    
    <user-detail-dialog :visible.sync="userDetailVisible" :user-id="userDetailUserId" @show-post="showPostDetail" @show-goods="showGoodsDetailById" @show-wall="showWallDetail" />

    
    <goods-detail-dialog :visible.sync="goodsDetailVisible" :goods-id="goodsDetailGoodsId" @show-user="function(id) { userDetailUserId = id; userDetailVisible = true }" />

    <post-detail-dialog :visible.sync="postDetailVisible" :post-id="postDetailPostId" @show-user="showUserDetail"></post-detail-dialog>

    <wall-detail-dialog :visible.sync="wallDetailVisible" :wall-id="wallDetailWallId" @show-user="showUserDetail"></wall-detail-dialog>
  </div>
</template>
<script>
import { getOrderList, getUserList, getGoodsList, getLogisticsByOrderId } from '@/api/manage'
import UserDetailDialog from '@/components/UserDetailDialog'
import GoodsDetailDialog from '@/components/GoodsDetailDialog'
import PostDetailDialog from '@/components/PostDetailDialog'
import WallDetailDialog from '@/components/WallDetailDialog'
export default {
  /** 注册子组件 */
  components: { UserDetailDialog, GoodsDetailDialog, PostDetailDialog, WallDetailDialog },
  /** 组件数据定义 */
  data() { return {
    list: [], pageNum: 1, pageSize: 10, total: 0, loading: false, selectedRows: [], searchForm: { orderId: '', orderStatus: null },
    sortField: '',
    detailVisible: false, detailLoading: false, detailData: {},
    detailBuyer: {}, detailSeller: {}, detailGoods: {}, detailLogistics: null, detailLogisticsHistory: [], detailAfterSale: null,
    logisticsDialogVisible: false,
    userDetailVisible: false, userDetailUserId: '',
    goodsDetailVisible: false, goodsDetailGoodsId: '',
    postDetailVisible: false, postDetailPostId: '',
    wallDetailVisible: false, wallDetailWallId: ''
  } },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadData() },
  /** 组件方法定义 */
  methods: {
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      const res = await getOrderList({ pageNum: this.pageNum, pageSize: this.pageSize, ...this.searchForm })
      if (res.code === 200) { this.list = res.data.records || []; this.total = res.data.total || 0 }
      this.loading = false
    },
    /** 表格选中行变化回调 */
    handleSelectionChange(val) { this.selectedRows = val },
    handleSortChange(val) {
      if (!val) return
      var parts = val.split('_')
      var field = parts[0]
      var order = parts[1]
      this.list.sort(function(a, b) {
        var va = a[field], vb = b[field]
        if (!va && !vb) return 0
        if (!va) return 1
        if (!vb) return -1
        var ta = new Date(va).getTime(), tb = new Date(vb).getTime()
        return order === 'asc' ? ta - tb : tb - ta
      })
    },
    /** 分页页码变化回调 */
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    /** 每页条数变化回调 */
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    getStepActive(s) {
      if (s === 0) return 0
      if (s === 1) return 1
      if (s === 2) return 2
      if (s === 3) return 3
      if (s >= 4 && s <= 7) return 3 
      if (s === 8 || s === 9) return 4
      return 0
    },
    statusText(s) {
      var map = ['未支付','已支付','已发货','已签收','退款中','退货中','已退款','已退货','已完成','已评价']
      return map[s] || '未知'
    },
    statusTagType(s) {
      var map = ['warning','primary','','success','danger','warning','info','info','success','']
      return map[s] || 'info'
    },
    isAfterSaleStatus(s) {
      return s === 4 || s === 5 || s === 6 || s === 7
    },
    afterSaleTypeTag(type) {
      var map = { '仅退款': 'warning', '退货退款': 'danger', '换货': '', '补寄': 'success' }
      return map[type] || 'warning'
    },
    parseAfterSaleImages(images) {
      if (!images) return []
      if (Array.isArray(images)) return images.filter(function(s) { return s && s.trim() })
      var str = String(images).trim()
      if (str === '' || str === '[]' || str === 'null') return []
      try { var parsed = JSON.parse(str); if (Array.isArray(parsed)) return parsed.filter(function(s) { return s && s.trim() }); return [parsed] } catch (e) { return str.split(',').filter(function(s) { return s && s.trim() }) }
    },
    formatTime(time) {
      if (!time) return ''
      var d = new Date(time)
      var pad = function(n) { return String(n).padStart(2, '0') }
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    },
    getGoodsImgList() {
      var img = this.detailGoods.goodsImages || this.detailGoods.goodsImg || this.detailGoods.images || ''
      if (!img) return []
      if (Array.isArray(img)) return img.filter(function(s) { return s && s.trim() })
      var str = String(img).trim()
      if (str === '' || str === '[]' || str === 'null') return []
      try { var parsed = JSON.parse(str); if (Array.isArray(parsed)) return parsed.filter(function(s) { return s && s.trim() }); return [parsed] } catch (e) { return str.split(',').filter(function(s) { return s && s.trim() }) }
    },
    /** 显示详情弹窗 */
    async showDetail(row) {
      this.detailData = row; this.detailBuyer = {}; this.detailSeller = {}; this.detailGoods = {}; this.detailLogistics = null; this.detailLogisticsHistory = []; this.detailAfterSale = null
      this.detailVisible = true; this.detailLoading = true
      try {
        var userRes = await getUserList({ pageNum: 1, pageSize: 9999 })
        var goodsRes = await getGoodsList({ pageNum: 1, pageSize: 9999 })
        if (userRes.code === 200) {
          var users = userRes.data.records || userRes.data || []
          this.detailBuyer = users.find(function(u) { return u.userId === row.buyerId }) || {}
          this.detailSeller = users.find(function(u) { return u.userId === row.sellerId }) || {}
        }
        if (goodsRes.code === 200) {
          var goods = goodsRes.data.records || goodsRes.data || []
          this.detailGoods = goods.find(function(g) { return g.goodsId === row.goodsId }) || {}
        }
        try {
          var logisticsRes = await getLogisticsByOrderId(row.orderId)
          if (logisticsRes.code === 200 && logisticsRes.data) {
            if (Array.isArray(logisticsRes.data)) {
              this.detailLogisticsHistory = logisticsRes.data
              this.detailLogistics = logisticsRes.data[0] || null
            } else {
              this.detailLogistics = logisticsRes.data
              this.detailLogisticsHistory = [logisticsRes.data]
            }
          }
        } catch (e) {}
        
        if (row.remark) {
          try {
            this.detailAfterSale = JSON.parse(row.remark)
          } catch (e) {
            this.detailAfterSale = null
          }
        }
      } catch (e) {}
      this.detailLoading = false
    },
    /** 显示用户详情弹窗 */
    showUserDetail(userId) { this.userDetailUserId = userId; this.userDetailVisible = true },
    /** 显示商品详情弹窗 */
    showGoodsDetail(goodsId) { this.goodsDetailGoodsId = goodsId; this.goodsDetailVisible = true },
    showGoodsDetailById(goodsId) { this.goodsDetailGoodsId = goodsId; this.goodsDetailVisible = true },
    /** 显示帖子详情弹窗 */
    showPostDetail(postId) { this.postDetailPostId = postId; this.postDetailVisible = true },
    /** 显示表白墙详情弹窗 */
    showWallDetail(wallId) { this.wallDetailWallId = wallId; this.wallDetailVisible = true },
    /** 重置搜索条件并重新加载数据 */
    resetSearch() { this.searchForm = { orderId: '', orderStatus: null }; this.loadData() },
    /** 导出数据为CSV文件 */
    exportData() {
      if (this.list.length === 0) { this.$message.warning('没有可导出的数据'); return }
      var statusMap = ['未支付','已支付','已发货','已完成','退款中','退货中','已退款','已退货','已完成','已评价']
      var columns = [{ prop: 'orderId', label: '订单号' }, { prop: 'goodsId', label: '商品ID' }, { prop: 'buyerId', label: '买家ID' }, { prop: 'sellerId', label: '卖家ID' }, { prop: 'buyCount', label: '数量' }, { prop: 'totalPrice', label: '总价' }, { prop: 'orderStatus', label: '状态' }, { prop: 'createTime', label: '下单时间' }]
      var headers = columns.map(function(c) { return c.label }).join(',')
      var rows = this.list.map(function(row) { return columns.map(function(c) { var val = row[c.prop]; if (c.prop === 'orderStatus') return statusMap[val] || '未知'; return val != null ? String(val) : '' }).join(',') }).join('\n')
      var csv = '\uFEFF' + headers + '\n' + rows
      var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      var link = document.createElement('a'); link.href = URL.createObjectURL(blob); link.download = '订单数据.csv'; link.click(); URL.revokeObjectURL(link.href)
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
.data-table {
  border-radius: 16px;
  overflow: hidden;
}
.pagination-bar {
  margin-top: 16px;
  padding: 8px 0;
}
.order-dialog .step-card {
  margin-bottom: 20px;
  border-radius: 16px;
}
.step-title {
  font-weight: 600;
  margin-bottom: 16px;
  color: #374151;
  font-size: 15px;
}
.info-card {
  margin-bottom: 20px;
  border-radius: 16px;
}
.info-header {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #374151;
}
.od-table { width: 100%; border-collapse: collapse; border-color: #ebeef5; }
.od-table td { padding: 10px 12px; border: 1px solid #ebeef5; font-size: 13px; color: #303133; }
.od-label { background: #f8fafc; font-weight: 600; width: 100px; text-align: right; color: #4b5563; white-space: nowrap; }
.user-row {
  margin-bottom: 20px;
}
.user-card {
  border-radius: 16px;
}
.user-profile {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  cursor: pointer;
  padding: 10px;
  border-radius: 12px;
  transition: background 0.2s ease;
}
.user-profile:hover {
  background: #f8fafc;
}
.user-avatar {
  margin-right: 12px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}
.user-name {
  font-weight: 600;
  color: #409EFF;
}
.user-hint {
  color: #9ca3af;
  font-size: 12px;
  margin-top: 4px;
}
.goods-body {
  display: flex;
  gap: 20px;
}
.goods-images {
  flex-shrink: 0;
}
.goods-main-img {
  width: 160px;
  height: 160px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.goods-thumb-list {
  margin-top: 8px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.goods-thumb {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.08);
}
.goods-info {
  flex: 1;
}
.aftersale-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.aftersale-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.empty-block {
  text-align: center;
  color: #9ca3af;
  padding: 30px 0;
}
.empty-icon {
  font-size: 40px;
  color: #e5e7eb;
  margin-bottom: 8px;
}
.logistics-dialog .timeline-card {
  padding: 4px 8px;
  border-radius: 12px;
}
.timeline-status {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.timeline-detail {
  font-size: 13px;
  color: #606266;
  margin-top: 4px;
  white-space: pre-wrap;
}
.timeline-meta {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}
</style>
