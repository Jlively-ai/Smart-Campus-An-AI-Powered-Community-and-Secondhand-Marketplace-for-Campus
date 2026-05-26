<!--
  组件名：MyOrder
  功能描述：我的订单页
  主要职责：
    1. 双标签页（我买的/我卖的）
    2. 搜索/筛选/排序
    3. 物流信息弹窗
    4. 评价弹窗
    5. 发货弹窗
-->
<template>
  <div>
    <el-card>
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
        <span>我的订单</span>

      </div>
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="我买的" name="buy">
          <div style="margin-bottom:12px;display:flex;align-items:center;gap:10px;flex-wrap:wrap;">
            <el-input v-model="buySearchKeyword" placeholder="搜索商品名称" prefix-icon="el-icon-search" clearable size="small" style="width:200px;" @clear="filterBuyOrders" @keyup.enter.native="filterBuyOrders"></el-input>
            <el-select v-model="buyFilterStatus" placeholder="订单状态" size="small" clearable style="width:140px;" @change="filterBuyOrders">
              <el-option label="全部" value=""></el-option>
              <el-option label="未支付" :value="0"></el-option>
              <el-option label="已支付" :value="1"></el-option>
              <el-option label="已发货" :value="2"></el-option>
              <el-option label="已完成" :value="3"></el-option>
              <el-option label="退款中" :value="4"></el-option>
              <el-option label="退货中" :value="5"></el-option>
              <el-option label="已退款" :value="6"></el-option>
              <el-option label="已退货" :value="7"></el-option>
              <el-option label="已完成" :value="8"></el-option>
              <el-option label="已评价" :value="9"></el-option>
            </el-select>
            <el-select v-model="buySortBy" placeholder="排序方式" size="small" clearable style="width:170px;" @change="filterBuyOrders">
              <el-option label="下单时间最新" value="createTimeDesc"></el-option>
              <el-option label="下单时间最早" value="createTimeAsc"></el-option>
              <el-option label="总价从高到低" value="priceDesc"></el-option>
              <el-option label="总价从低到高" value="priceAsc"></el-option>
            </el-select>
            <el-button size="small" icon="el-icon-refresh" @click="resetBuyFilter">重置</el-button>
          </div>
          <el-table :data="buyOrderList" v-loading="buyLoading" @row-click="buyRowClick" style="cursor:pointer;">
            <el-table-column prop="orderId" label="订单号" width="160">
              <template slot-scope="scope"><el-link type="primary">{{ scope.row.orderId }}</el-link></template>
            </el-table-column>
            <el-table-column prop="goodsName" label="商品" min-width="150">
              <template slot-scope="scope"><el-link type="primary" v-if="scope.row.goodsId" @click="$router.push('/goodsDetail/' + scope.row.goodsId)">{{ scope.row.goodsName || '查看商品' }}</el-link><span v-else>{{ scope.row.goodsName }}</span></template>
            </el-table-column>
            <el-table-column prop="buyCount" label="数量" width="70" align="center"></el-table-column>
            <el-table-column prop="totalPrice" label="总价" width="100">
              <template slot-scope="scope"><span style="color:#f56c6c;">￥{{ scope.row.totalPrice }}</span></template>
            </el-table-column>
            <el-table-column prop="orderStatus" label="状态" width="100" align="center">
              <template slot-scope="scope"><el-tag :type="statusType(scope.row.orderStatus)">{{ statusText(scope.row.orderStatus) }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="createTime" label="下单时间" min-width="180">
              <template slot-scope="scope"><span style="white-space:nowrap;">{{ scope.row.createTime | formatTime }}</span></template>
            </el-table-column>
            <el-table-column label="操作" min-width="220" align="center">
              <template slot-scope="scope">
                <div style="white-space:nowrap;">
                <el-button size="mini" type="primary" @click.stop="$router.push('/orderDetail/' + scope.row.orderId).catch(() => {})">查看详情</el-button>
                <el-button v-if="scope.row.orderStatus === 2" size="mini" type="success" @click.stop="receipt(scope.row.orderId)">签收</el-button>
                <el-button v-if="scope.row.orderStatus === 3" size="mini" type="warning" @click.stop="$router.push('/orderDetail/' + scope.row.orderId).catch(() => {})">售后/评价</el-button>
                <el-button v-if="scope.row.orderStatus === 9 && !scope.row._hasFollowUp" size="mini" type="primary" @click.stop="$router.push('/orderDetail/' + scope.row.orderId).catch(() => {})">查看评价/追评</el-button>
                <el-button v-if="scope.row.orderStatus === 9 && scope.row._hasFollowUp" size="mini" type="info" @click.stop="$router.push('/orderDetail/' + scope.row.orderId).catch(() => {})">查看评价</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination style="margin-top:15px;text-align:center;" @current-change="buyPageChange" :current-page="buyPageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="buyTotal" layout="total, sizes, prev, pager, next" @size-change="handleBuySizeChange"></el-pagination>
        </el-tab-pane>
        <el-tab-pane label="我卖的" name="sale">
          <div style="margin-bottom:10px;">
            <el-button type="success" size="small" icon="el-icon-plus" @click="$router.push('/goodsPublish')">发布商品</el-button>
          </div>
          <div style="margin-bottom:12px;display:flex;align-items:center;gap:10px;flex-wrap:wrap;">
            <el-input v-model="saleSearchKeyword" placeholder="搜索商品名称" prefix-icon="el-icon-search" clearable size="small" style="width:200px;" @clear="filterSaleOrders" @keyup.enter.native="filterSaleOrders"></el-input>
            <el-select v-model="saleFilterStatus" placeholder="订单状态" size="small" clearable style="width:140px;" @change="filterSaleOrders">
              <el-option label="全部" value=""></el-option>
              <el-option label="未支付" :value="0"></el-option>
              <el-option label="已支付" :value="1"></el-option>
              <el-option label="已发货" :value="2"></el-option>
              <el-option label="已完成" :value="3"></el-option>
              <el-option label="退款中" :value="4"></el-option>
              <el-option label="退货中" :value="5"></el-option>
              <el-option label="已退款" :value="6"></el-option>
              <el-option label="已退货" :value="7"></el-option>
              <el-option label="已完成" :value="8"></el-option>
              <el-option label="已评价" :value="9"></el-option>
            </el-select>
            <el-select v-model="saleSortBy" placeholder="排序方式" size="small" clearable style="width:170px;" @change="filterSaleOrders">
              <el-option label="下单时间最新" value="createTimeDesc"></el-option>
              <el-option label="下单时间最早" value="createTimeAsc"></el-option>
              <el-option label="总价从高到低" value="priceDesc"></el-option>
              <el-option label="总价从低到高" value="priceAsc"></el-option>
            </el-select>
            <el-button size="small" icon="el-icon-refresh" @click="resetSaleFilter">重置</el-button>
          </div>
          <el-table :data="saleOrderList" v-loading="saleLoading" @row-click="saleRowClick" style="cursor:pointer;">
            <el-table-column prop="orderId" label="订单号" width="160">
              <template slot-scope="scope"><el-link type="primary">{{ scope.row.orderId }}</el-link></template>
            </el-table-column>
            <el-table-column prop="goodsName" label="商品" min-width="150">
              <template slot-scope="scope"><el-link type="primary" v-if="scope.row.goodsId" @click="$router.push('/goodsDetail/' + scope.row.goodsId)">{{ scope.row.goodsName || '查看商品' }}</el-link><span v-else>{{ scope.row.goodsName }}</span></template>
            </el-table-column>
            <el-table-column prop="buyCount" label="数量" width="70" align="center"></el-table-column>
            <el-table-column prop="totalPrice" label="总价" width="100">
              <template slot-scope="scope"><span style="color:#f56c6c;">￥{{ scope.row.totalPrice }}</span></template>
            </el-table-column>
            <el-table-column prop="orderStatus" label="状态" width="100" align="center">
              <template slot-scope="scope"><el-tag :type="statusType(scope.row.orderStatus)">{{ statusText(scope.row.orderStatus) }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="createTime" label="下单时间" min-width="180">
              <template slot-scope="scope"><span style="white-space:nowrap;">{{ scope.row.createTime | formatTime }}</span></template>
            </el-table-column>
            <el-table-column label="操作" min-width="260" align="center">
              <template slot-scope="scope">
                <div style="white-space:nowrap;">
                <el-button size="mini" type="primary" @click.stop="$router.push('/orderDetail/' + scope.row.orderId + '?from=seller').catch(() => {})">查看详情</el-button>
                <el-button v-if="scope.row.orderStatus === 1" size="mini" type="primary" @click.stop="openShipDialog(scope.row)">发货</el-button>
                <el-button v-if="scope.row.orderStatus === 4 || scope.row.orderStatus === 5" size="mini" type="warning" @click.stop="$router.push('/orderDetail/' + scope.row.orderId + '?from=seller').catch(() => {})">处理售后</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination style="margin-top:15px;text-align:center;" @current-change="salePageChange" :current-page="salePageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="saleTotal" layout="total, sizes, prev, pager, next" @size-change="handleSaleSizeChange"></el-pagination>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog title="物流信息" :visible.sync="logisticsVisible" width="500px">
      <div v-if="logisticsData">
        <table class="detail-table">
          <tr><td class="label">物流公司</td><td>{{ logisticsData.company || '暂无' }}</td></tr>
          <tr><td class="label">运单号</td><td>{{ logisticsData.trackingNo || '暂无' }}</td></tr>
          <tr><td class="label">当前状态</td><td><el-tag :type="logisticsData.trackingNo ? 'success' : 'info'">{{ logisticsData.trackingNo ? (logisticsData.currentStatus || '运输中') : '未发货' }}</el-tag></td></tr>
          <tr><td class="label">物流详情</td><td style="white-space:pre-wrap;">{{ logisticsData.detail || '暂无详细信息' }}</td></tr>
        </table>
      </div>
      <el-empty v-else description="暂无物流信息"></el-empty>
    </el-dialog>

    <el-dialog title="订单评价" :visible.sync="reviewVisible" width="500px">
      <el-form label-width="80px">
        <el-form-item label="评分"><el-rate v-model="reviewForm.rating"></el-rate></el-form-item>
        <el-form-item label="评价内容"><el-input type="textarea" v-model="reviewForm.content" :rows="4" placeholder="请输入评价内容"></el-input></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="reviewVisible = false">取消</el-button><el-button type="primary" @click="submitReview">提交评价</el-button></span>
    </el-dialog>

    <el-dialog title="发货" :visible.sync="shipDialogVisible" width="500px">
      <div style="margin-bottom:12px;padding:12px;background:#f0f9eb;border-radius:6px;border:1px solid #e1f3d8;">
        <div style="font-weight:bold;margin-bottom:8px;color:#67C23A;"><i class="el-icon-location" style="margin-right:4px;"></i>收货信息</div>
        <div>收货人：{{ shipForm.receiverName || '-' }}</div>
        <div>联系电话：{{ shipForm.receiverPhone || '-' }}</div>
        <div>收货地址：{{ shipForm.receiverAddress || '-' }}</div>
      </div>
      <el-form ref="shipForm" :model="shipForm" :rules="shipRules" label-width="100px">
        <el-form-item label="快递公司" prop="company">
          <el-select v-model="shipForm.company" placeholder="请选择快递公司" style="width:100%;" filterable allow-create>
            <el-option label="顺丰速运" value="顺丰速运"></el-option>
            <el-option label="中通快递" value="中通快递"></el-option>
            <el-option label="圆通速递" value="圆通速递"></el-option>
            <el-option label="韵达快递" value="韵达快递"></el-option>
            <el-option label="申通快递" value="申通快递"></el-option>
            <el-option label="百世快递" value="百世快递"></el-option>
            <el-option label="极兔速递" value="极兔速递"></el-option>
            <el-option label="邮政EMS" value="邮政EMS"></el-option>
            <el-option label="京东物流" value="京东物流"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="运单号" prop="trackingNo">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入运单号"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="shipDialogVisible = false">取消</el-button><el-button type="primary" @click="confirmShip" :loading="shipLoading">确认发货</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: 'MyOrder',
  data() {
    return {
      activeTab: this.$route.query.tab === 'sale' ? 'sale' : 'buy',
      buySearchKeyword: '', buyFilterStatus: '', buySortBy: '',
      saleSearchKeyword: '', saleFilterStatus: '', saleSortBy: '',
      buyOrderList: [], buyPageNum: 1, buyTotal: 0, buyLoading: false,
      saleOrderList: [], salePageNum: 1, saleTotal: 0, saleLoading: false,
      pageSize: 10,
      buyRawList: [], saleRawList: [],
      logisticsVisible: false, logisticsData: null,
      reviewVisible: false, reviewForm: { orderId: null, rating: 5, content: '' },
      shipDialogVisible: false, shipLoading: false,
      shipForm: { orderId: '', goodsName: '', company: '', trackingNo: '' },
      shipRules: { company: [{ required: true, message: '请选择快递公司', trigger: 'change' }], trackingNo: [{ required: true, message: '请输入运单号', trigger: 'blur' }] }
    }
  },
  created() { this.activeTab = this.$route.query.tab === 'sale' ? 'sale' : 'buy'; this.loadBuyOrders(); this.loadSaleOrders() },
  watch: {
    '$route.query.tab'(val) {
      if (val === 'sale' || val === 'buy') {
        this.activeTab = val
      }
    }
  },
  methods: {
    statusText(s) { return ['未支付', '已支付', '已发货', '已完成', '退款中', '退货中', '已退款', '已退货', '已完成', '已评价'][s] || '未知' },
    statusType(s) { return ['warning', 'primary', 'success', 'success', 'danger', 'warning', 'info', 'info', 'success', 'success'][s] || '' },
    handleTabClick() {
      // 切换tab时更新URL，确保返回时能恢复正确的tab
      this.$router.replace({ path: '/myOrder', query: { tab: this.activeTab } }).catch(() => {})
      if (this.activeTab === 'buy' && this.buyOrderList.length === 0) this.loadBuyOrders()
      if (this.activeTab === 'sale' && this.saleOrderList.length === 0) this.loadSaleOrders()
    },
    async loadBuyOrders() {
      this.buyLoading = true
      const res = await this.$axios.get('/market-order/myOrder', { params: { pageNum: this.buyPageNum, pageSize: this.pageSize } })
      if (res.code === 200) { this.buyRawList = res.data.records || []; this.buyTotal = res.data.total || 0; this.applyBuyFilter() }
      this.buyLoading = false
    },
    async loadSaleOrders() {
      this.saleLoading = true
      const res = await this.$axios.get('/market-order/mySalesOrders', { params: { pageNum: this.salePageNum, pageSize: this.pageSize } })
      if (res.code === 200) { this.saleRawList = res.data.records || []; this.saleTotal = res.data.total || 0; this.applySaleFilter() }
      this.saleLoading = false
    },
    applyBuyFilter() {
      let list = [...this.buyRawList]
      if (this.buySearchKeyword) {
        const kw = this.buySearchKeyword.toLowerCase()
        list = list.filter(item => (item.goodsName || '').toLowerCase().includes(kw))
      }
      if (this.buyFilterStatus !== '' && this.buyFilterStatus !== null && this.buyFilterStatus !== undefined) {
        list = list.filter(item => item.orderStatus === this.buyFilterStatus)
      }
      list = this.sortList(list, this.buySortBy)
      this.buyOrderList = list
    },
    applySaleFilter() {
      let list = [...this.saleRawList]
      if (this.saleSearchKeyword) {
        const kw = this.saleSearchKeyword.toLowerCase()
        list = list.filter(item => (item.goodsName || '').toLowerCase().includes(kw))
      }
      if (this.saleFilterStatus !== '' && this.saleFilterStatus !== null && this.saleFilterStatus !== undefined) {
        list = list.filter(item => item.orderStatus === this.saleFilterStatus)
      }
      list = this.sortList(list, this.saleSortBy)
      this.saleOrderList = list
    },
    sortList(list, sortBy) {
      if (!sortBy) return list
      return list.sort((a, b) => {
        if (sortBy === 'createTimeDesc') return new Date(b.createTime) - new Date(a.createTime)
        if (sortBy === 'createTimeAsc') return new Date(a.createTime) - new Date(b.createTime)
        if (sortBy === 'priceDesc') return (b.totalPrice || 0) - (a.totalPrice || 0)
        if (sortBy === 'priceAsc') return (a.totalPrice || 0) - (b.totalPrice || 0)
        return 0
      })
    },
    filterBuyOrders() { this.applyBuyFilter() },
    filterSaleOrders() { this.applySaleFilter() },
    resetBuyFilter() {
      this.buySearchKeyword = ''; this.buyFilterStatus = ''; this.buySortBy = ''
      this.applyBuyFilter()
    },
    resetSaleFilter() {
      this.saleSearchKeyword = ''; this.saleFilterStatus = ''; this.saleSortBy = ''
      this.applySaleFilter()
    },
    async payOrder(orderId) { const res = await this.$axios.post('/market-order/payOrder/' + orderId); if (res.code === 200) { this.$message.success('支付成功'); this.loadBuyOrders() } },
    async receipt(orderId) { try { await this.$confirm('确认签收？', '提示'); const res = await this.$axios.post('/market-order/receipt/' + orderId); if (res.code === 200) { this.$message.success('签收成功'); this.loadBuyOrders() } } catch (e) { if (e !== 'cancel' && e.message !== 'cancel') this.$message.error('操作失败') } },
    async applyReturn(orderId) { try { await this.$confirm('确定申请退货？', '提示'); const res = await this.$axios.post('/market-order/applyReturn/' + orderId); if (res.code === 200) { this.$message.success('已申请退货'); this.loadBuyOrders() } } catch (e) { if (e !== 'cancel' && e.message !== 'cancel') this.$message.error('操作失败') } },
    async viewLogistics(orderId) {
      this.logisticsVisible = true; this.logisticsData = null
      const res = await this.$axios.get('/logistics/getByOrderId/' + orderId)
      if (res.code === 200) this.logisticsData = res.data
    },
    openReview(orderId) { this.reviewForm = { orderId, rating: 5, content: '' }; this.reviewVisible = true },
    async submitReview() {
      if (!this.reviewForm.content) return this.$message.warning('请输入评价内容')
      const res = await this.$axios.post('/order-review/add', this.reviewForm)
      if (res.code === 200) { this.$message.success('评价成功'); this.reviewVisible = false; this.loadBuyOrders() }
    },
    openShipDialog(row) {
      this.shipForm = { orderId: row.orderId, goodsName: row.goodsName || '商品', company: '', trackingNo: '', receiverName: row.receiverName || '', receiverPhone: row.receiverPhone || '', receiverAddress: row.receiverAddress || '' }
      this.shipDialogVisible = true
    },
    confirmShip() {
      this.$refs.shipForm.validate(async valid => {
        if (!valid) return
        this.shipLoading = true
        try {
          const res = await this.$axios.post('/market-order/sendGoods/' + this.shipForm.orderId, {
            company: this.shipForm.company,
            trackingNo: this.shipForm.trackingNo
          })
          if (res.code === 200) {
            this.$message.success('发货成功')
            this.shipDialogVisible = false
            this.loadSaleOrders()
          } else {
            this.$message.error(res.msg || '发货失败')
          }
        } catch (e) {
          this.$message.error('发货失败')
        }
        this.shipLoading = false
      })
    },
    async auditReturn(orderId) { try { await this.$confirm('同意退货？', '审核退货', { type: 'warning' }); const res = await this.$axios.post('/market-order/auditReturn', { orderId, auditState: 1 }); if (res.code === 200) { this.$message.success('已同意退货'); this.loadSaleOrders() } } catch (e) { if (e !== 'cancel' && e.message !== 'cancel') this.$message.error('操作失败') } },
    buyPageChange(val) { this.buyPageNum = val; this.loadBuyOrders() },
    salePageChange(val) { this.salePageNum = val; this.loadSaleOrders() },
    handleBuySizeChange(val) { this.pageSize = val; this.buyPageNum = 1; this.loadBuyOrders() },
    handleSaleSizeChange(val) { this.pageSize = val; this.salePageNum = 1; this.loadSaleOrders() },
    buyRowClick(row) { this.$router.push('/orderDetail/' + row.orderId + '?from=buyer').catch(() => {}) },
    saleRowClick(row) { this.$router.push('/orderDetail/' + row.orderId + '?from=seller').catch(() => {}) }
  }
}
</script>

<style scoped>
.detail-table { width: 100%; border-collapse: separate; border-spacing: 0; border-radius: 14px; overflow: hidden; border: 1px solid #ebeef5; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
.detail-table td { padding: 14px 16px; border-bottom: 1px solid #ebeef5; font-size: 13px; transition: background 0.3s ease; }
.detail-table tr:hover td { background: #f8f9fb; }
.detail-table tr:last-child td { border-bottom: none; }
.detail-table .label { background: linear-gradient(135deg, #fafafa, #f5f7fa); font-weight: 600; width: 100px; text-align: right; color: #606266; white-space: nowrap; }

/* 物流信息弹窗增强 */
::v-deep .el-dialog__body .el-empty__description {
  color: #a0a3a8;
  font-weight: 500;
}

/* 发货弹窗收货信息卡片增强 */
.el-dialog .el-form-item__content > div[style*="background:#f0f9eb"] {
  border-radius: 12px !important;
  padding: 16px !important;
  border: 1px solid rgba(103, 194, 58, 0.15) !important;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.08) !important;
}
</style>
