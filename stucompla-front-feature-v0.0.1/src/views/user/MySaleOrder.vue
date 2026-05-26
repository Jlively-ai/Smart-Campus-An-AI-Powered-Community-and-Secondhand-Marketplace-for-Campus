<!--
  组件名：MySaleOrder
  功能描述：销售订单页
  主要职责：
    1. 卖家视角订单列表
    2. 搜索/筛选/排序
    3. 发货操作
    4. 查看物流
-->
<template>
  <div>
    <el-card>
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
        <span>销售订单</span>

      </div>
      <el-table :data="orderList" v-loading="loading" @row-click="rowClick" style="cursor:pointer;">
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
        <el-table-column prop="createTime" label="下单时间" width="180">
          <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="scope">
            <el-button v-if="scope.row.orderStatus === 1" size="mini" type="primary" @click="sendGoods(scope.row.orderId)">发货</el-button>
            <el-button v-if="scope.row.orderStatus === 4" size="mini" type="warning" @click="auditReturn(scope.row.orderId)">审核退货</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:15px;text-align:center;" @current-change="handlePageChange" :current-page="pageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange"></el-pagination>
    </el-card>
  </div>
</template>
<script>
export default {
  name: 'MySaleOrder',
  data() { return { orderList: [], pageNum: 1, pageSize: 10, total: 0, loading: false } },
  created() { this.loadOrders() },
  methods: {
    statusText(s) { return ['未支付', '已支付', '已发货', '已完成', '退款中', '退货中', '已退款', '已退货', '已完成', '已评价'][s] || '未知' },
    statusType(s) { return ['warning', 'primary', 'success', 'success', 'danger', 'warning', 'info', 'info', 'success', ''][s] || '' },
    async loadOrders() {
      this.loading = true
      const res = await this.$axios.get('/market-order/mySalesOrders', { params: { pageNum: this.pageNum, pageSize: this.pageSize } })
      if (res.code === 200) { this.orderList = res.data.records || []; this.total = res.data.total || 0 }
      this.loading = false
    },
    async sendGoods(orderId) { const res = await this.$axios.get('/market-order/sendGoods/' + orderId); if (res.code === 200) { this.$message.success('发货成功'); this.loadOrders() } },
    async auditReturn(orderId) { await this.$confirm('同意退货？', '审核退货', { type: 'warning' }); const res = await this.$axios.post('/market-order/auditReturn', { orderId, auditState: 1 }); if (res.code === 200) { this.$message.success('已同意退货'); this.loadOrders() } },
    handlePageChange(val) { this.pageNum = val; this.loadOrders() },
    rowClick(row) { this.$router.push('/orderDetail/' + row.orderId + '?from=seller').catch(() => {}) }
  }
}
</script>

<style scoped>
</style>
