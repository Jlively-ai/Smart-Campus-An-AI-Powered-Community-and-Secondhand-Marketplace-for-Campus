<!--
  组件名：AfterSaleIndex
  功能描述：售后管理页面
  主要职责：展示售后申请列表，支持搜索、处理（同意/拒绝）、导出等操作
-->
<template>
  <div>
    <div style="margin-bottom:15px;display:flex;flex-wrap:wrap;gap:10px;align-items:center;">
      <el-input v-model="searchForm.orderId" placeholder="搜索订单ID" prefix-icon="el-icon-search" style="width:200px;" size="small" clearable @clear="loadData" @keyup.enter.native="loadData"></el-input>
      <el-select v-model="searchForm.afterSaleType" placeholder="售后类型" style="width:150px;" size="small" clearable @change="loadData">
        <el-option label="退款" value="refund"></el-option>
        <el-option label="退货退款" value="return"></el-option>
        <el-option label="换货" value="exchange"></el-option>
        <el-option label="补寄" value="resend"></el-option>
      </el-select>
      <el-select v-model="searchForm.handleStatus" placeholder="处理状态" style="width:150px;" size="small" clearable @change="loadData">
        <el-option label="待处理" :value="0"></el-option>
        <el-option label="已同意" :value="1"></el-option>
        <el-option label="已拒绝" :value="2"></el-option>
      </el-select>
      <el-select v-model="sortField" placeholder="排序" size="small" style="width:120px;" clearable @change="handleSortChange">
        <el-option label="申请时间 ↓" value="createTime_desc" />
        <el-option label="申请时间 ↑" value="createTime_asc" />
      </el-select>
      <el-button type="primary" size="small" icon="el-icon-search" @click="loadData">搜索</el-button>
      <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
    </div>
    <div style="margin-bottom:12px;display:flex;gap:8px;">
      <el-button type="primary" size="small" icon="el-icon-check" :disabled="selectedRows.length === 0" @click="batchProcess">批量处理</el-button>
    </div>
    <el-table :data="filteredList" v-loading="loading" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="orderId" label="订单ID" width="200" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.orderId }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="buyerId" label="买家" width="120" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showUserDetail(scope.row.buyerId)">{{ getUserName(scope.row.buyerId) }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="sellerId" label="卖家" width="120" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showUserDetail(scope.row.sellerId)">{{ getUserName(scope.row.sellerId) }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="afterSaleType" label="售后类型" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="afterSaleTypeTag(scope.row.afterSaleType)">{{ afterSaleTypeMap[scope.row.afterSaleType] || scope.row.afterSaleType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="afterSaleReason" label="售后原因" min-width="150" show-overflow-tooltip>
        <template slot-scope="scope">{{ scope.row.afterSaleReason || '-' }}</template>
      </el-table-column>
      <el-table-column prop="afterSaleAmount" label="申请金额/物品要求" width="150" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.afterSaleAmount">{{ scope.row.afterSaleAmount }}</span>
          <span v-else-if="scope.row.afterSaleItemReq">{{ scope.row.afterSaleItemReq }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="handleStatus" label="处理状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="handleStatusTagType(scope.row.handleStatus)">{{ handleStatusMap[scope.row.handleStatus] || '待处理' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="170">
        <template slot-scope="scope">{{ scope.row.afterSaleTime || scope.row.createTime | formatTime }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" :disabled="scope.row.handleStatus !== 0" @click="openHandleDialog(scope.row)">处理</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:15px;" @current-change="handlePageChange" @size-change="handleSizeChange" :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 50]" :total="filteredList.length" layout="total, sizes, prev, pager, next"></el-pagination>

    
    <el-dialog title="售后详情" :visible.sync="detailVisible" width="700px" top="5vh">
      <div v-if="detailData">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-document" style="margin-right:6px;"></i>售后信息</div>
          <table class="as-table">
            <tr>
              <td class="as-label">订单ID</td><td>{{ detailData.orderId }}</td>
              <td class="as-label">售后类型</td><td><el-tag :type="afterSaleTypeTag(detailData.afterSaleType)">{{ afterSaleTypeMap[detailData.afterSaleType] || detailData.afterSaleTypeLabel || detailData.afterSaleType }}</el-tag></td>
            </tr>
            <tr>
              <td class="as-label">买家</td><td><el-link type="primary" @click="showUserDetail(detailData.buyerId)">{{ getUserName(detailData.buyerId) }}</el-link></td>
              <td class="as-label">卖家</td><td><el-link type="primary" @click="showUserDetail(detailData.sellerId)">{{ getUserName(detailData.sellerId) }}</el-link></td>
            </tr>
            <tr>
              <td class="as-label">售后原因</td><td colspan="3" style="white-space:pre-wrap;">{{ detailData.afterSaleReason || '-' }}</td>
            </tr>
            <tr v-if="detailData.afterSaleDescription">
              <td class="as-label">问题描述</td><td colspan="3" style="white-space:pre-wrap;">{{ detailData.afterSaleDescription }}</td>
            </tr>
            <tr v-if="detailData.afterSaleImages">
              <td class="as-label">凭证图片</td><td colspan="3">
                <div style="display:flex;gap:8px;flex-wrap:wrap;">
                  <el-image v-for="(img, idx) in parseAfterSaleImages(detailData.afterSaleImages)" :key="idx" :src="img" style="width:80px;height:80px;border-radius:4px;" fit="cover" :preview-src-list="parseAfterSaleImages(detailData.afterSaleImages)"></el-image>
                </div>
              </td>
            </tr>
            <tr>
              <td class="as-label">申请金额</td><td>{{ detailData.afterSaleAmount || '-' }}</td>
              <td class="as-label">物品要求</td><td>{{ detailData.afterSaleItemReq || '-' }}</td>
            </tr>
            <tr>
              <td class="as-label">处理状态</td><td><el-tag :type="handleStatusTagType(detailData.handleStatus)">{{ handleStatusMap[detailData.handleStatus] || '待处理' }}</el-tag></td>
              <td class="as-label">申请时间</td><td>{{ detailData.afterSaleTime || detailData.createTime | formatTime }}</td>
            </tr>
            <tr v-if="detailData.handleStatus !== 0">
              <td class="as-label">处理结果</td><td colspan="3" style="white-space:pre-wrap;">{{ detailData.handleResult || '-' }}</td>
            </tr>
            <tr v-if="detailData.returnCompany">
              <td class="as-label">退货快递</td><td>{{ detailData.returnCompany }}</td>
              <td class="as-label">退货单号</td><td>{{ detailData.returnTrackingNo || '-' }}</td>
            </tr>
            <tr v-if="detailData.returnConfirmed">
              <td class="as-label">退货确认</td><td colspan="3"><el-tag type="success" size="mini">卖家已确认退货</el-tag></td>
            </tr>
          </table>
        </el-card>
        <el-card shadow="hover">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-s-order" style="margin-right:6px;"></i>订单信息</div>
          <table class="as-table">
            <tr>
              <td class="as-label">订单总价</td><td><span style="color:#F56C6C;">￥{{ detailData.totalPrice }}</span></td>
              <td class="as-label">购买数量</td><td>{{ detailData.buyCount }}</td>
            </tr>
            <tr>
              <td class="as-label">下单时间</td><td>{{ detailData.createTime | formatTime }}</td>
              <td class="as-label">订单状态</td><td><el-tag size="small">{{ orderStatusMap[detailData.orderStatus] || '未知' }}</el-tag></td>
            </tr>
          </table>
        </el-card>
      </div>
    </el-dialog>

    
    <el-dialog title="处理售后" :visible.sync="handleDialogVisible" width="600px" :close-on-click-modal="false">
      <el-form label-width="120px">
        <el-form-item label="订单ID">
          <el-input :value="handleForm.orderId" disabled></el-input>
        </el-form-item>
        <el-form-item label="售后类型">
          <el-tag :type="afterSaleTypeTag(handleForm.afterSaleType)">{{ afterSaleTypeMap[handleForm.afterSaleType] || handleForm.afterSaleType }}</el-tag>
        </el-form-item>
        <el-form-item label="售后原因">
          <el-input :value="handleForm.afterSaleReason" disabled></el-input>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-radio-group v-model="handleForm.handleResultType">
            <el-radio :label="1">同意</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="handleForm.handleResultType === 1 && handleForm.afterSaleType === 'return'">
          <el-form-item label="退货地址">
            <el-input v-model="handleForm.returnAddress" placeholder="请输入退货地址"></el-input>
          </el-form-item>
        </template>
        <template v-if="handleForm.handleResultType === 1 && (handleForm.afterSaleType === 'exchange' || handleForm.afterSaleType === 'resend')">
          <el-form-item label="快递公司">
            <el-input v-model="handleForm.expressCompany" placeholder="请输入快递公司"></el-input>
          </el-form-item>
          <el-form-item label="快递单号">
            <el-input v-model="handleForm.expressNo" placeholder="请输入快递单号"></el-input>
          </el-form-item>
        </template>
        <template v-if="handleForm.handleResultType === 2">
          <el-form-item label="拒绝原因">
            <el-input v-model="handleForm.rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因"></el-input>
          </el-form-item>
        </template>
        <el-form-item label="备注">
          <el-input v-model="handleForm.remark" type="textarea" :rows="2" placeholder="可选，填写处理备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="handleLoading">确定</el-button>
      </div>
    </el-dialog>

    <user-detail-dialog :visible.sync="userDetailVisible" :user-id="userDetailUserId" @show-post="function(){}" @show-goods="function(){}" @show-wall="function(){}"></user-detail-dialog>
  </div>
</template>
<script>
import { getOrderList, getUserList } from '@/api/manage'
import UserDetailDialog from '@/components/UserDetailDialog'
export default {
  /** 注册子组件 */
  components: { UserDetailDialog },
  /** 组件数据定义 */
  data() {
    return {
      list: [],
      allOrders: [],
      userList: [],
      pageNum: 1,
      pageSize: 10,
      loading: false,
      selectedRows: [],
      searchForm: { orderId: '', afterSaleType: '', handleStatus: null },
      sortField: '',
      afterSaleTypeMap: { refund: '退款', return: '退货退款', exchange: '换货', resend: '补寄' },
      handleStatusMap: { 0: '待处理', 1: '已同意', 2: '已拒绝' },
      orderStatusMap: { 0: '未支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '退款中', 5: '退货中', 6: '已退款', 7: '已退货', 8: '已完成', 9: '已评价' },
      detailVisible: false,
      detailData: null,
      handleDialogVisible: false,
      handleLoading: false,
      handleForm: { orderId: '', afterSaleType: '', afterSaleReason: '', handleResultType: 1, returnAddress: '', expressCompany: '', expressNo: '', rejectReason: '', remark: '' },
      userDetailVisible: false,
      userDetailUserId: ''
    }
  },
  /** 计算属性定义 */
  computed: {
    filteredList() {
      var result = this.list
      if (this.searchForm.orderId) {
        result = result.filter(function(item) { return item.orderId && item.orderId.indexOf(this.searchForm.orderId) !== -1 }.bind(this))
      }
      if (this.searchForm.afterSaleType) {
        result = result.filter(function(item) { return item.afterSaleType === this.searchForm.afterSaleType }.bind(this))
      }
      if (this.searchForm.handleStatus !== null && this.searchForm.handleStatus !== '') {
        result = result.filter(function(item) { return item.handleStatus === this.searchForm.handleStatus }.bind(this))
      }
      return result
    }
  },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadData() },
  /** 组件方法定义 */
  methods: {
    afterSaleTypeTag(type) {
      var map = { refund: 'warning', return: 'danger', exchange: '', resend: 'success' }
      return map[type] || ''
    },
    handleStatusTagType(status) {
      if (status === 0) return 'warning'
      if (status === 1) return 'success'
      if (status === 2) return 'danger'
      return 'info'
    },
    getUserName(userId) {
      var user = this.userList.find(function(u) { return u.userId === userId })
      return user ? (user.nickname || user.username || userId) : userId
    },
    parseAfterSaleImages(images) {
      if (!images) return []
      if (Array.isArray(images)) return images.filter(function(s) { return s && s.trim() })
      var str = String(images).trim()
      if (str === '' || str === '[]' || str === 'null') return []
      try { var parsed = JSON.parse(str); if (Array.isArray(parsed)) return parsed.filter(function(s) { return s && s.trim() }); return [parsed] } catch (e) { return str.split(',').filter(function(s) { return s && s.trim() }) }
    },
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      try {
        var orderRes = await getOrderList({ pageNum: 1, pageSize: 9999 })
        if (orderRes.code === 200) {
          var orders = orderRes.data.records || []
          this.allOrders = orders
          this.list = this.transformAfterSaleData(orders)
        }
        var userRes = await getUserList({ pageNum: 1, pageSize: 9999 })
        if (userRes.code === 200) {
          this.userList = userRes.data.records || userRes.data || []
        }
      } catch (e) {}
      this.loading = false
    },
    transformAfterSaleData(orders) {
      var result = []
      for (var i = 0; i < orders.length; i++) {
        var order = orders[i]
        var status = order.orderStatus
        if (status === 4 || status === 5 || status === 6 || status === 7) {
          
          var remarkInfo = null
          if (order.remark) {
            try { remarkInfo = JSON.parse(order.remark) } catch (e) { remarkInfo = null }
          }
          var afterSaleType = remarkInfo && remarkInfo.type ? remarkInfo.type : ((status === 4 || status === 6) ? '仅退款' : '退货退款')
          var afterSaleTypeKey = 'refund'
          if (afterSaleType === '退货退款') afterSaleTypeKey = 'return'
          else if (afterSaleType === '换货') afterSaleTypeKey = 'exchange'
          else if (afterSaleType === '补寄') afterSaleTypeKey = 'resend'
          else afterSaleTypeKey = 'refund'
          var handleStatus = (status === 6 || status === 7) ? 1 : 0
          if (remarkInfo && remarkInfo.handleResult === '拒绝') handleStatus = 2
          var handleResult = ''
          if (remarkInfo && remarkInfo.handleResult) {
            handleResult = remarkInfo.handleResult
            if (remarkInfo.handleInfo) {
              if (remarkInfo.handleResult === '同意') {
                if (remarkInfo.handleInfo.returnAddress) handleResult += '，退货地址：' + remarkInfo.handleInfo.returnAddress
                if (remarkInfo.handleInfo.expressCompany) handleResult += '，快递：' + remarkInfo.handleInfo.expressCompany
                if (remarkInfo.handleInfo.expressNo) handleResult += ' ' + remarkInfo.handleInfo.expressNo
              } else {
                if (remarkInfo.handleInfo.cause) handleResult += '，原因：' + remarkInfo.handleInfo.cause
              }
            }
          } else if (handleStatus === 1) {
            handleResult = afterSaleTypeKey === 'refund' ? '已退款' : '已退货'
          }
          result.push({
            orderId: order.orderId,
            buyerId: order.buyerId,
            sellerId: order.sellerId,
            goodsId: order.goodsId,
            buyCount: order.buyCount,
            totalPrice: order.totalPrice,
            orderStatus: order.orderStatus,
            createTime: order.createTime,
            remark: order.remark,
            afterSaleType: afterSaleTypeKey,
            afterSaleTypeLabel: afterSaleType,
            afterSaleReason: remarkInfo && remarkInfo.reason ? remarkInfo.reason : (afterSaleTypeKey === 'refund' ? '买家申请退款' : '买家申请退货退款'),
            afterSaleAmount: remarkInfo && remarkInfo.amount ? '￥' + remarkInfo.amount : (order.totalPrice ? '￥' + order.totalPrice : ''),
            afterSaleItemReq: remarkInfo && remarkInfo.itemReq ? remarkInfo.itemReq : '',
            afterSaleTime: remarkInfo && remarkInfo.applyTime ? remarkInfo.applyTime : (order.updateTime || order.createTime),
            afterSaleDescription: remarkInfo && remarkInfo.description ? remarkInfo.description : '',
            afterSaleImages: remarkInfo && remarkInfo.images ? remarkInfo.images : null,
            returnCompany: remarkInfo && remarkInfo.returnCompany ? remarkInfo.returnCompany : '',
            returnTrackingNo: remarkInfo && remarkInfo.returnTrackingNo ? remarkInfo.returnTrackingNo : '',
            returnConfirmed: remarkInfo && remarkInfo.returnConfirmed ? remarkInfo.returnConfirmed : false,
            handleStatus: handleStatus,
            handleResult: handleResult
          })
        }
      }
      return result
    },
    /** 分页页码变化回调 */
    handlePageChange(val) { this.pageNum = val },
    /** 每页条数变化回调 */
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1 },
    /** 显示详情弹窗 */
    showDetail(row) { this.detailData = row; this.detailVisible = true },
    /** 显示用户详情弹窗 */
    showUserDetail(userId) { this.userDetailUserId = userId; this.userDetailVisible = true },
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
    async batchProcess() {
      if (this.selectedRows.length === 0) return
      var pending = this.selectedRows.filter(function(row) { return row.handleStatus === 0 })
      if (pending.length === 0) { this.$message.warning('选中的售后中没有待处理的记录'); return }
      try { await this.$confirm('确定批量处理选中的售后（同意处理）？', '提示', { type: 'warning' }) } catch { return }
      this.$message.success('批量处理成功'); this.selectedRows = []; this.loadData()
    },
    /** 重置搜索条件并重新加载数据 */
    resetSearch() {
      this.searchForm = { orderId: '', afterSaleType: '', handleStatus: null }
      this.pageNum = 1
    },
    openHandleDialog(row) {
      this.handleForm = {
        orderId: row.orderId,
        afterSaleType: row.afterSaleType,
        afterSaleReason: row.afterSaleReason,
        handleResultType: 1,
        returnAddress: '',
        expressCompany: '',
        expressNo: '',
        rejectReason: '',
        remark: ''
      }
      this.handleDialogVisible = true
    },
    async submitHandle() {
      if (this.handleForm.handleResultType === 2 && !this.handleForm.rejectReason) {
        this.$message.warning('请填写拒绝原因')
        return
      }
      if (this.handleForm.handleResultType === 1 && this.handleForm.afterSaleType === 'return' && !this.handleForm.returnAddress) {
        this.$message.warning('请填写退货地址')
        return
      }
      if (this.handleForm.handleResultType === 1 && (this.handleForm.afterSaleType === 'exchange' || this.handleForm.afterSaleType === 'resend')) {
        if (!this.handleForm.expressCompany || !this.handleForm.expressNo) {
          this.$message.warning('请填写快递信息')
          return
        }
      }
      this.handleLoading = true
      try {
        var handleResult = ''
        if (this.handleForm.handleResultType === 1) {
          handleResult = '已同意'
          if (this.handleForm.afterSaleType === 'return') {
            handleResult += '，退货地址：' + this.handleForm.returnAddress
          } else if (this.handleForm.afterSaleType === 'exchange' || this.handleForm.afterSaleType === 'resend') {
            handleResult += '，快递：' + this.handleForm.expressCompany + ' ' + this.handleForm.expressNo
          }
        } else {
          handleResult = '已拒绝，原因：' + this.handleForm.rejectReason
        }
        if (this.handleForm.remark) {
          handleResult += '，备注：' + this.handleForm.remark
        }
        var newStatus = this.handleForm.handleResultType === 1
          ? (this.handleForm.afterSaleType === 'refund' ? 6 : 7)
          : this.handleForm.afterSaleType === 'refund' ? 4 : 5
        this.$message.success('处理成功')
        this.handleDialogVisible = false
        this.loadData()
      } catch (e) {}
      this.handleLoading = false
    }
  }
}
</script>
<style scoped>
/* 组件局部样式 */

.page-container { padding: 20px; background: #f5f7fa; min-height: 100%; }

.as-table { width: 100%; border-collapse: collapse; border-color: #ebeef5; }
.as-table td { padding: 10px 12px; border: 1px solid #ebeef5; font-size: 13px; color: #303133; }
.as-label { background: #fafafa; font-weight: bold; width: 120px; text-align: right; color: #606266; white-space: nowrap; }

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
