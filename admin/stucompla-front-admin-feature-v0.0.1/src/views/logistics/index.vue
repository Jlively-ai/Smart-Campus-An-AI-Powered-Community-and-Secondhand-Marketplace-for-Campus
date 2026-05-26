<!--
  组件名：LogisticsIndex
  功能描述：物流管理页面
  主要职责：展示物流信息列表，支持搜索、更新物流信息、导出等操作
-->
<template>
  <div>
    <div style="margin-bottom:15px;display:flex;flex-wrap:wrap;gap:10px;align-items:center;">
      <el-input v-model="searchForm.keyword" placeholder="搜索物流公司/运单号" prefix-icon="el-icon-search" style="width:220px;" size="small" clearable @clear="fetchList" @keyup.enter.native="fetchList"></el-input>
      <el-select v-model="sortField" placeholder="排序" size="small" style="width:120px;" clearable @change="handleSortChange">
        <el-option label="创建时间 ↓" value="createTime_desc" />
        <el-option label="创建时间 ↑" value="createTime_asc" />
      </el-select>
      <el-button type="primary" size="small" icon="el-icon-search" @click="fetchList">搜索</el-button>
      <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
      <el-button type="success" size="small" icon="el-icon-plus" @click="openAddDialog">添加物流</el-button>
      <el-button type="success" size="small" icon="el-icon-download" @click="exportData">导出</el-button>
    </div>
    <div style="margin-bottom:12px;display:flex;gap:8px;">
      <el-button type="primary" size="small" icon="el-icon-refresh" :disabled="selectedRows.length === 0" @click="batchUpdate">批量更新</el-button>
    </div>
    <el-table :data="tableData" border stripe style="width:100%;" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="trackingNo" label="运单号" width="200" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.trackingNo || '-' }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="orderId" label="订单ID" width="200" show-overflow-tooltip>
        <template slot-scope="scope"><el-link type="primary" @click="showOrderDetail(scope.row.orderId)">{{ scope.row.orderId | formatId('order') }}</el-link></template>
      </el-table-column>
      <el-table-column prop="company" label="物流公司" width="140"></el-table-column>
      <el-table-column prop="currentStatus" label="当前状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.currentStatus)">{{ scope.row.currentStatus || '待发货' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180">
        <template slot-scope="scope">{{ scope.row.updateTime | formatTime }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template slot-scope="scope">
          <el-button type="primary" size="small" @click="openUpdateDialog(scope.row)">更新</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      style="margin-top:15px;text-align:right;"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :page-size.sync="pageSize"
      :page-sizes="[10, 20, 50]"
      @size-change="handleSizeChange"
      :current-page.sync="currentPage"
      @current-change="fetchList"
    ></el-pagination>

    
    <el-dialog :title="updateForm.logisticsId ? '更新物流' : '添加物流'" :visible.sync="addDialogVisible" width="500px">
      <el-form :model="updateForm" label-width="80px">
        <el-form-item label="订单ID"><el-input v-model="updateForm.orderId" :disabled="!!updateForm.logisticsId" @change="onOrderIdChange" @blur="onOrderIdChange"></el-input></el-form-item>
        <el-form-item label="物流公司">
          <el-select v-model="updateForm.company" filterable allow-create placeholder="请选择物流公司" style="width:100%;">
            <el-option v-for="c in logisticsCompanyOptions" :key="c" :label="c" :value="c"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="运单号"><el-input v-model="updateForm.trackingNo" placeholder="快递单号"></el-input></el-form-item>
        <el-form-item label="当前状态">
          <el-select v-model="updateForm.currentStatus" style="width:100%;">
            <el-option label="待发货" value="待发货"></el-option>
            <el-option label="已发货" value="已发货"></el-option>
            <el-option label="运输中" value="运输中"></el-option>
            <el-option label="派送中" value="派送中"></el-option>
            <el-option label="已签收" value="已签收"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="物流详情"><el-input type="textarea" v-model="updateForm.detail" :rows="3" placeholder="物流轨迹信息"></el-input></el-form-item>
      </el-form>
      
      <div v-if="orderReceiverInfo.receiverName" style="margin:0 0 15px 0;padding:12px 16px;background:#f0f9eb;border-radius:6px;border:1px solid #e1f3d8;">
        <div style="font-weight:bold;color:#67C23A;margin-bottom:8px;"><i class="el-icon-user" style="margin-right:4px;"></i>收货人信息</div>
        <div style="font-size:13px;color:#606266;line-height:1.8;">
          <div>收货人：{{ orderReceiverInfo.receiverName }}</div>
          <div>联系电话：{{ orderReceiverInfo.receiverPhone }}</div>
          <div>收货地址：{{ orderReceiverInfo.receiverAddress }}</div>
        </div>
      </div>
      <span slot="footer"><el-button @click="addDialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></span>
    </el-dialog>

    
    <el-dialog title="物流详情" :visible.sync="detailVisible" width="700px" top="5vh">
      <div v-if="detailRow">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-truck" style="margin-right:6px;"></i>基础信息</div>
          <table class="dt-table">
            <tr>
              <td class="dt-label">运单号</td><td>{{ detailRow.trackingNo || '-' }}</td>
              <td class="dt-label">订单ID</td><td><el-link type="primary" @click="showOrderDetail(detailRow.orderId)">{{ detailRow.orderId | formatId('order') }}</el-link></td>
            </tr>
            <tr>
              <td class="dt-label">物流公司</td><td>{{ detailRow.company || '-' }}</td>
              <td class="dt-label">当前状态</td><td><el-tag :type="statusTagType(detailRow.currentStatus)">{{ detailRow.currentStatus || '待发货' }}</el-tag></td>
            </tr>
            <tr>
              <td class="dt-label">创建时间</td><td>{{ detailRow.createTime | formatTime }}</td>
              <td class="dt-label">更新时间</td><td>{{ detailRow.updateTime | formatTime }}</td>
            </tr>
          </table>
        </el-card>
        <el-card shadow="hover">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-location-outline" style="margin-right:6px;"></i>物流轨迹</div>
          <div v-if="detailRow.detail" style="margin-bottom:12px;white-space:pre-wrap;line-height:1.8;font-size:14px;">{{ detailRow.detail }}</div>
          <el-button type="text" @click="showLogisticsHistory"><i class="el-icon-time"></i> 查看历史轨迹</el-button>
        </el-card>
      </div>
    </el-dialog>

    
    <el-dialog title="历史物流轨迹" :visible.sync="historyVisible" width="550px" append-to-body>
      <div v-loading="historyLoading">
        <div v-if="historyList.length > 0">
          <el-timeline>
            <el-timeline-item v-for="(item, idx) in historyList" :key="idx" :timestamp="item.createTime | formatTime" placement="top" :type="idx === 0 ? 'primary' : 'info'" :size="idx === 0 ? 'normal' : 'small'">
              <el-card shadow="never" style="padding:4px 8px;">
                <div style="font-size:14px;"><strong>{{ item.currentStatus || '状态更新' }}</strong></div>
                <div style="font-size:13px;color:#606266;margin-top:4px;white-space:pre-wrap;">{{ item.detail || '暂无详细信息' }}</div>
                <div style="font-size:12px;color:#999;margin-top:4px;" v-if="item.company || item.trackingNo">{{ item.company || '' }} {{ item.trackingNo ? '运单号: ' + item.trackingNo : '' }}</div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
        <div v-else style="text-align:center;color:#999;padding:30px 0;">暂无历史轨迹信息</div>
      </div>
      <span slot="footer"><el-button @click="historyVisible = false">关闭</el-button></span>
    </el-dialog>

    
    <el-dialog title="订单详情" :visible.sync="orderDetailVisible" width="900px" top="5vh" append-to-body>
      <div v-loading="orderDetailLoading">
        <div v-if="orderDetailData">
          
          <el-card shadow="hover" style="margin-bottom:20px;">
            <div style="font-weight:bold;margin-bottom:16px;">订单进度</div>
            <el-steps :active="getStepActive(orderDetailData.orderStatus)" align-center finish-status="success">
              <el-step title="下单" :description="formatTime(orderDetailData.createTime)"></el-step>
              <el-step title="已支付" :description="orderDetailData.orderStatus >= 1 && orderDetailData.payTime ? formatTime(orderDetailData.payTime) : ''"></el-step>
              <el-step title="已发货" :description="orderDetailData.orderStatus >= 2 && orderDetailData.sendTime ? formatTime(orderDetailData.sendTime) : ''"></el-step>
              <el-step title="已签收" :description="orderDetailData.orderStatus >= 3 && orderDetailData.receiptTime ? formatTime(orderDetailData.receiptTime) : ''"></el-step>
              <el-step title="已完成" :description="orderDetailData.orderStatus >= 5 && orderDetailData.receiptTime ? formatTime(orderDetailData.receiptTime) : ''"></el-step>
            </el-steps>
          </el-card>

          
          <el-card shadow="hover" style="margin-bottom:20px;">
            <div slot="header" style="font-weight:bold;"><i class="el-icon-document" style="margin-right:6px;"></i>基础信息</div>
            <table class="dt-table">
              <tr>
                <td class="dt-label">订单编号</td><td>{{ orderDetailData.orderId | formatId('order') }}</td>
                <td class="dt-label">订单状态</td><td><el-tag :type="['warning','primary','','success','danger','info'][orderDetailData.orderStatus]" size="small">{{ ['未支付','已支付','已发货','已完成','退货中','已完成'][orderDetailData.orderStatus] }}</el-tag></td>
              </tr>
              <tr>
                <td class="dt-label">下单时间</td><td>{{ formatTime(orderDetailData.createTime) }}</td>
                <td class="dt-label">支付时间</td><td>{{ orderDetailData.payTime ? formatTime(orderDetailData.payTime) : '-' }}</td>
              </tr>
              <tr>
                <td class="dt-label">发货时间</td><td>{{ orderDetailData.sendTime ? formatTime(orderDetailData.sendTime) : '-' }}</td>
                <td class="dt-label">收货时间</td><td>{{ orderDetailData.receiptTime ? formatTime(orderDetailData.receiptTime) : '-' }}</td>
              </tr>
              <tr>
                <td class="dt-label">购买数量</td><td>{{ orderDetailData.buyCount }}</td>
                <td class="dt-label">支付方式</td><td>在线支付</td>
              </tr>
              <tr>
                <td class="dt-label">商品单价</td><td><span style="color:#F56C6C;">￥{{ orderDetailGoods.goodsPrice || '-' }}</span></td>
                <td class="dt-label">实付金额</td><td><span style="color:#F56C6C;font-weight:bold;font-size:16px;">￥{{ orderDetailData.totalPrice }}</span></td>
              </tr>
            </table>
          </el-card>

          
          <el-row :gutter="20" style="margin-bottom:20px;">
            <el-col :span="12">
              <el-card shadow="hover">
                <div slot="header" style="font-weight:bold;"><i class="el-icon-user" style="margin-right:6px;"></i>购买人信息</div>
                <div style="display:flex;align-items:center;margin-bottom:12px;cursor:pointer;" @click="showUserDetail(orderDetailData.buyerId)">
                  <el-avatar v-if="orderDetailBuyer.avatar" :src="orderDetailBuyer.avatar" :size="48" style="margin-right:12px;"></el-avatar>
                  <el-avatar v-else :size="48" style="margin-right:12px;background:#409EFF;">{{ (orderDetailBuyer.nickname || '买')[0] }}</el-avatar>
                  <div>
                    <div style="font-weight:bold;color:#409EFF;">{{ orderDetailBuyer.nickname || '未知' }}<i class="el-icon-arrow-right" style="font-size:12px;margin-left:4px;"></i></div>
                    <div style="color:#999;font-size:12px;">点击查看用户详情</div>
                  </div>
                </div>
                <table class="dt-table">
                  <tr><td class="dt-label">用户编号</td><td>{{ orderDetailData.buyerId | formatId('user') }}</td></tr>
                  <tr><td class="dt-label">用户昵称</td><td>{{ orderDetailBuyer.nickname || '-' }}</td></tr>
                  <tr><td class="dt-label">联系方式</td><td>{{ orderDetailBuyer.phone || '-' }}</td></tr>
                </table>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover">
                <div slot="header" style="font-weight:bold;"><i class="el-icon-s-custom" style="margin-right:6px;"></i>出售人信息</div>
                <div style="display:flex;align-items:center;margin-bottom:12px;cursor:pointer;" @click="showUserDetail(orderDetailData.sellerId)">
                  <el-avatar v-if="orderDetailSeller.avatar" :src="orderDetailSeller.avatar" :size="48" style="margin-right:12px;"></el-avatar>
                  <el-avatar v-else :size="48" style="margin-right:12px;background:#67C23A;">{{ (orderDetailSeller.nickname || '卖')[0] }}</el-avatar>
                  <div>
                    <div style="font-weight:bold;color:#409EFF;">{{ orderDetailSeller.nickname || '未知' }}<i class="el-icon-arrow-right" style="font-size:12px;margin-left:4px;"></i></div>
                    <div style="color:#999;font-size:12px;">点击查看用户详情</div>
                  </div>
                </div>
                <table class="dt-table">
                  <tr><td class="dt-label">用户编号</td><td>{{ orderDetailData.sellerId | formatId('user') }}</td></tr>
                  <tr><td class="dt-label">用户昵称</td><td>{{ orderDetailSeller.nickname || '-' }}</td></tr>
                  <tr><td class="dt-label">联系方式</td><td>{{ orderDetailSeller.phone || '-' }}</td></tr>
                </table>
              </el-card>
            </el-col>
          </el-row>

          
          <el-card shadow="hover" style="margin-bottom:20px;">
            <div slot="header" style="font-weight:bold;"><i class="el-icon-goods" style="margin-right:6px;"></i>商品信息</div>
            <div style="display:flex;gap:20px;">
              <div v-if="getOrderDetailGoodsImgList().length > 0" style="flex-shrink:0;">
                <el-image :src="getOrderDetailGoodsImgList()[0]" style="width:160px;height:160px;border-radius:8px;" fit="cover" :preview-src-list="getOrderDetailGoodsImgList()"></el-image>
                <div v-if="getOrderDetailGoodsImgList().length > 1" style="margin-top:6px;display:flex;gap:4px;flex-wrap:wrap;">
                  <el-image v-for="(img, idx) in getOrderDetailGoodsImgList().slice(1, 5)" :key="idx" :src="img" style="width:36px;height:36px;border-radius:4px;" fit="cover" :preview-src-list="getOrderDetailGoodsImgList()"></el-image>
                </div>
              </div>
              <div style="flex:1;">
                <table class="dt-table">
                  <tr><td class="dt-label">商品编号</td><td><el-link type="primary" @click="showGoodsDetail(orderDetailData.goodsId)">{{ orderDetailData.goodsId | formatId('goods') }}</el-link></td></tr>
                  <tr><td class="dt-label">商品标题</td><td>{{ orderDetailGoods.goodsName || '-' }}</td></tr>
                  <tr><td class="dt-label">商品描述</td><td>{{ orderDetailGoods.goodsDetail || orderDetailGoods.goodsDesc || '-' }}</td></tr>
                  <tr><td class="dt-label">库存数量</td><td>{{ orderDetailGoods.goodsCount || '-' }}</td></tr>
                  <tr><td class="dt-label">商品价格</td><td><span style="color:#F56C6C;">￥{{ orderDetailGoods.goodsPrice || '-' }}</span></td></tr>
                  <tr><td class="dt-label">商品状态</td><td><el-tag :type="orderDetailGoods.goodsStatus ? 'success' : 'info'" size="mini">{{ orderDetailGoods.goodsStatus ? '上架' : '下架' }}</el-tag></td></tr>
                </table>
              </div>
            </div>
          </el-card>

          
          <el-card shadow="hover" style="margin-bottom:20px;">
            <div slot="header" style="font-weight:bold;"><i class="el-icon-truck" style="margin-right:6px;"></i>物流信息</div>
            <div v-if="orderDetailLogistics">
              <table class="dt-table">
                <tr>
                  <td class="dt-label">物流公司</td><td>{{ orderDetailLogistics.company || '-' }}</td>
                  <td class="dt-label">运单号</td><td>{{ orderDetailLogistics.trackingNo || '-' }}</td>
                </tr>
                <tr>
                  <td class="dt-label">当前状态</td><td><el-tag size="small">{{ orderDetailLogistics.currentStatus || '运输中' }}</el-tag></td>
                  <td class="dt-label">更新时间</td><td>{{ formatTime(orderDetailLogistics.createTime) }}</td>
                </tr>
                <tr>
                  <td class="dt-label">物流详情</td>
                  <td colspan="3"><el-button type="text" @click="showLogisticsHistoryFromOrderDetail"><i class="el-icon-location-outline"></i> 查看物流轨迹</el-button></td>
                </tr>
              </table>
            </div>
            <div v-else-if="orderDetailData.orderStatus >= 2" style="text-align:center;color:#999;padding:20px 0;">
              <i class="el-icon-truck" style="font-size:32px;color:#dcdfe6;"></i>
              <div style="margin-top:8px;">暂无物流信息</div>
            </div>
            <div v-else style="text-align:center;color:#999;padding:20px 0;">
              <i class="el-icon-box" style="font-size:32px;color:#dcdfe6;"></i>
              <div style="margin-top:8px;">等待卖家发货</div>
            </div>
          </el-card>
        </div>
        <div v-else style="text-align:center;color:#909399;padding:20px;">未找到该订单信息</div>
      </div>
    </el-dialog>

    
    <user-detail-dialog :visible.sync="userDetailVisible" :user-id="userDetailUserId" @show-post="showPostDetail" @show-goods="showGoodsDetailById" @show-wall="showWallDetail"></user-detail-dialog>
    <goods-detail-dialog :visible.sync="goodsDetailVisible" :goods-id="goodsDetailGoodsId" @show-user="showUserDetail"></goods-detail-dialog>
    <post-detail-dialog :visible.sync="postDetailVisible" :post-id="postDetailPostId" @show-user="showUserDetail"></post-detail-dialog>
    <wall-detail-dialog :visible.sync="wallDetailVisible" :wall-id="wallDetailWallId" @show-user="showUserDetail"></wall-detail-dialog>
  </div>
</template>
<script>
import { createLogistics, updateLogistics, getLogisticsByOrderId, getLogisticsList, getOrderList, getUserList, getGoodsList } from '@/api/manage'
import UserDetailDialog from '@/components/UserDetailDialog'
import GoodsDetailDialog from '@/components/GoodsDetailDialog'
import PostDetailDialog from '@/components/PostDetailDialog'
import WallDetailDialog from '@/components/WallDetailDialog'
export default {
  /** 注册子组件 */
  components: { UserDetailDialog, GoodsDetailDialog, PostDetailDialog, WallDetailDialog },
  /** 组件数据定义 */
  data() {
    return {
      searchForm: { keyword: '' },
      sortField: '',
      tableData: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      selectedRows: [],
      addDialogVisible: false,
      updateForm: { orderId: '', company: '', trackingNo: '', currentStatus: '待发货', detail: '' },
      logisticsCompanyOptions: ['顺丰速运', '中通快递', '圆通速递', '韵达快递', '申通快递', '百世快递', '极兔速递', '邮政EMS', '京东物流'],
      orderReceiverInfo: { receiverName: '', receiverPhone: '', receiverAddress: '' },
      detailVisible: false,
      detailRow: null,
      orderDetailVisible: false,
      orderDetailLoading: false,
      orderDetailData: null,
      orderDetailBuyer: {},
      orderDetailSeller: {},
      orderDetailGoods: {},
      orderDetailLogistics: null,
      userDetailVisible: false,
      userDetailUserId: '',
      goodsDetailVisible: false,
      goodsDetailGoodsId: '',
      postDetailVisible: false,
      postDetailPostId: '',
      wallDetailVisible: false,
      wallDetailWallId: '',
      historyVisible: false,
      historyLoading: false,
      historyList: []
    }
  },
  /** 生命周期：组件创建时加载数据 */
  created() { this.fetchList() },
  /** 组件方法定义 */
  methods: {
    statusTagType(status) {
      const map = { '待发货': 'info', '已发货': '', '运输中': 'warning', '派送中': 'success', '已签收': 'success' }
      return map[status] || 'info'
    },
    async fetchList() {
      const res = await getLogisticsList({ pageNum: this.currentPage, pageSize: this.pageSize, ...this.searchForm })
      if (res.code === 200) {
        this.tableData = res.data.records || res.data.list || res.data || []
        this.total = res.data.total || this.tableData.length
      }
    },
    /** 表格选中行变化回调 */
    handleSelectionChange(val) { this.selectedRows = val },
    async batchUpdate() {
      if (this.selectedRows.length === 0) return
      try { await this.$confirm('确定批量更新选中的物流状态？', '提示', { type: 'warning' }) } catch { return }
      for (var i = 0; i < this.selectedRows.length; i++) {
        var row = this.selectedRows[i]
        var nextStatus = { '待发货': '已发货', '已发货': '运输中', '运输中': '派送中', '派送中': '已签收' }
        var newStatus = nextStatus[row.currentStatus] || '已签收'
        await updateLogistics({ logisticsId: row.logisticsId, orderId: row.orderId, company: row.company, trackingNo: row.trackingNo, currentStatus: newStatus, detail: row.detail || '' })
      }
      this.$message.success('批量更新成功'); this.selectedRows = []; this.fetchList()
    },
    openAddDialog() {
      this.updateForm = { orderId: '', company: '', trackingNo: '', currentStatus: '待发货', detail: '' }
      this.orderReceiverInfo = { receiverName: '', receiverPhone: '', receiverAddress: '' }
      this.addDialogVisible = true
    },
    openUpdateDialog(row) {
      this.updateForm = {
        logisticsId: row.logisticsId,
        orderId: row.orderId,
        company: row.company || '',
        trackingNo: row.trackingNo || '',
        currentStatus: row.currentStatus || '待发货',
        detail: row.detail || ''
      }
      this.addDialogVisible = true
    },
    async handleSubmit() {
      if (this.updateForm.logisticsId) {
        await updateLogistics(this.updateForm)
        this.$message.success('更新成功')
      } else {
        await createLogistics(this.updateForm)
        this.$message.success('添加成功')
      }
      this.addDialogVisible = false
      this.updateForm = { orderId: '', company: '', trackingNo: '', currentStatus: '待发货', detail: '' }
      this.fetchList()
    },
    /** 显示详情弹窗 */
    showDetail(row) { this.detailRow = row; this.detailVisible = true },
    async showLogisticsHistory() {
      if (!this.detailRow || !this.detailRow.orderId) { this.$message.warning('无订单信息'); return }
      this.historyList = []
      this.historyVisible = true
      this.historyLoading = true
      try {
        var res = await getLogisticsByOrderId(this.detailRow.orderId)
        if (res.code === 200 && res.data) {
          if (Array.isArray(res.data)) {
            this.historyList = res.data
          } else {
            this.historyList = [res.data]
          }
        }
      } catch (e) { this.historyList = [] }
      this.historyLoading = false
    },
    async onOrderIdChange() {
      var orderId = this.updateForm.orderId
      if (!orderId || !orderId.trim()) {
        this.orderReceiverInfo = { receiverName: '', receiverPhone: '', receiverAddress: '' }
        return
      }
      try {
        const res = await getOrderList({ pageNum: 1, pageSize: 9999 })
        if (res.code === 200) {
          const records = res.data.records || res.data || []
          const order = records.find(o => o.orderId === orderId)
          if (order) {
            
            try {
              const userRes = await getUserList({ pageNum: 1, pageSize: 9999 })
              if (userRes.code === 200) {
                const users = userRes.data.records || userRes.data || []
                const buyer = users.find(u => u.userId === order.buyerId) || {}
                this.orderReceiverInfo = {
                  receiverName: buyer.nickname || buyer.username || '',
                  receiverPhone: buyer.phone || '',
                  receiverAddress: buyer.address || ''
                }
              }
            } catch (e) {
              this.orderReceiverInfo = { receiverName: '', receiverPhone: '', receiverAddress: '' }
            }
          } else {
            this.orderReceiverInfo = { receiverName: '', receiverPhone: '', receiverAddress: '' }
          }
        }
      } catch (e) {
        this.orderReceiverInfo = { receiverName: '', receiverPhone: '', receiverAddress: '' }
      }
    },
    async showOrderDetail(orderId) {
      this.orderDetailData = null
      this.orderDetailBuyer = {}
      this.orderDetailSeller = {}
      this.orderDetailGoods = {}
      this.orderDetailLogistics = null
      this.orderDetailVisible = true
      this.orderDetailLoading = true
      try {
        const res = await getOrderList({ pageNum: 1, pageSize: 9999 })
        if (res.code === 200) {
          const records = res.data.records || res.data || []
          var order = records.find(o => o.orderId === orderId) || null
          this.orderDetailData = order
          if (order) {
            await this.loadOrderFullDetail(order)
          }
        }
      } catch (e) { this.orderDetailData = null }
      this.orderDetailLoading = false
    },
    async loadOrderFullDetail(order) {
      try {
        var userRes = await getUserList({ pageNum: 1, pageSize: 9999 })
        var goodsRes = await getGoodsList({ pageNum: 1, pageSize: 9999 })
        if (userRes.code === 200) {
          var users = userRes.data.records || userRes.data || []
          this.orderDetailBuyer = users.find(u => u.userId === order.buyerId) || {}
          this.orderDetailSeller = users.find(u => u.userId === order.sellerId) || {}
        }
        if (goodsRes.code === 200) {
          var goods = goodsRes.data.records || goodsRes.data || []
          this.orderDetailGoods = goods.find(g => g.goodsId === order.goodsId) || {}
        }
        try {
          var logisticsRes = await getLogisticsByOrderId(order.orderId)
          if (logisticsRes.code === 200 && logisticsRes.data) {
            if (Array.isArray(logisticsRes.data)) {
              this.orderDetailLogistics = logisticsRes.data[0] || null
            } else {
              this.orderDetailLogistics = logisticsRes.data
            }
          }
        } catch (e) {}
      } catch (e) {}
    },
    getStepActive(s) { return [0, 1, 2, 3, 5].indexOf(s) },
    formatTime(time) {
      if (!time) return ''
      var d = new Date(time)
      var pad = function(n) { return String(n).padStart(2, '0') }
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    },
    getOrderDetailGoodsImgList() {
      var img = this.orderDetailGoods.goodsImages || this.orderDetailGoods.goodsImg || this.orderDetailGoods.images || ''
      if (!img) return []
      if (Array.isArray(img)) return img.filter(function(s) { return s && s.trim() })
      var str = String(img).trim()
      if (str === '' || str === '[]' || str === 'null') return []
      try { var parsed = JSON.parse(str); if (Array.isArray(parsed)) return parsed.filter(function(s) { return s && s.trim() }); return [parsed] } catch (e) { return str.split(',').filter(function(s) { return s && s.trim() }) }
    },
    showLogisticsHistoryFromOrderDetail() {
      if (!this.orderDetailData || !this.orderDetailData.orderId) { this.$message.warning('无订单信息'); return }
      this.historyList = []
      this.historyVisible = true
      this.historyLoading = true
      var self = this
      getLogisticsByOrderId(this.orderDetailData.orderId).then(function(res) {
        if (res.code === 200 && res.data) {
          if (Array.isArray(res.data)) {
            self.historyList = res.data
          } else {
            self.historyList = [res.data]
          }
        }
      }).catch(function() { self.historyList = [] }).finally(function() { self.historyLoading = false })
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
    resetSearch() { this.searchForm = { keyword: '' }; this.sortField = ''; this.fetchList() },
    handleSortChange(val) {
      if (!val) return
      var parts = val.split('_')
      var field = parts[0]
      var order = parts[1]
      this.tableData.sort(function(a, b) {
        var va = a[field], vb = b[field]
        if (!va && !vb) return 0
        if (!va) return 1
        if (!vb) return -1
        var ta = new Date(va).getTime(), tb = new Date(vb).getTime()
        return order === 'asc' ? ta - tb : tb - ta
      })
    },
    /** 每页条数变化回调 */
    handleSizeChange(val) { this.pageSize = val; this.currentPage = 1; this.fetchList() },
    /** 导出数据为CSV文件 */
    exportData() {
      if (this.tableData.length === 0) { this.$message.warning('没有可导出的数据'); return }
      const columns = [
        { prop: 'trackingNo', label: '运单号' },
        { prop: 'orderId', label: '订单ID' },
        { prop: 'company', label: '物流公司' },
        { prop: 'currentStatus', label: '当前状态' },
        { prop: 'createTime', label: '创建时间' },
        { prop: 'updateTime', label: '更新时间' }
      ]
      const headers = columns.map(c => c.label).join(',')
      const rows = this.tableData.map(row => columns.map(c => {
        const val = row[c.prop]
        return val != null ? String(val) : ''
      }).join(',')).join('\n')
      const csv = '\uFEFF' + headers + '\n' + rows
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '物流数据.csv'
      link.click()
      URL.revokeObjectURL(link.href)
      this.$message.success('导出成功')
    }
  }
}
</script>
<style scoped>
/* 组件局部样式 */

.page-container { padding: 20px; background: #f5f7fa; min-height: 100%; }

.dt-table { width: 100%; border-collapse: collapse; border-color: #ebeef5; }
.dt-table td { padding: 10px 12px; border: 1px solid #ebeef5; font-size: 13px; color: #303133; }
.dt-label { background: #fafafa; font-weight: bold; width: 100px; text-align: right; color: #606266; white-space: nowrap; }

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
