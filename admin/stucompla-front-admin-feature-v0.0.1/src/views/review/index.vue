<!--
  组件名：ReviewIndex
  功能描述：评价管理页面
  主要职责：展示订单评价列表，支持搜索、删除、导出等操作
-->
<template>
  <div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="商品评论" name="goods">
        <div style="margin-bottom:15px;display:flex;flex-wrap:wrap;gap:10px;align-items:center;">
          <el-input v-model="goodsSearchForm.content" placeholder="搜索评论内容" prefix-icon="el-icon-search" style="width:200px;" size="small" clearable @clear="loadGoodsComments" @keyup.enter.native="loadGoodsComments"></el-input>
          <el-select v-model="goodsSortField" placeholder="排序" size="small" style="width:120px;" clearable @change="handleGoodsSortChange">
            <el-option label="评分 ↓" value="rating_desc" />
            <el-option label="评分 ↑" value="rating_asc" />
            <el-option label="评论时间 ↓" value="createTime_desc" />
            <el-option label="评论时间 ↑" value="createTime_asc" />
          </el-select>
          <el-button type="primary" size="small" icon="el-icon-search" @click="loadGoodsComments">搜索</el-button>
          <el-button size="small" icon="el-icon-refresh" @click="resetGoodsSearch">重置</el-button>
          <el-button type="success" size="small" icon="el-icon-download" style="margin-left:auto;" @click="exportGoodsData">导出</el-button>
        </div>
        <div style="margin-bottom:12px;display:flex;gap:8px;">
          <el-button type="danger" size="small" icon="el-icon-delete" :disabled="selectedGoodsComments.length === 0" @click="batchDeleteGoodsComment">批量删除</el-button>
        </div>
        <el-table :data="goodsComments" v-loading="loading1" border @selection-change="handleGoodsCommentSelectionChange">
          <el-table-column type="selection" width="55" align="center"></el-table-column>
          <el-table-column prop="commentId" label="ID" width="200" align="center" show-overflow-tooltip>
            <template slot-scope="scope">
              <el-link type="primary" @click="showGoodsDetail(scope.row)">{{ scope.row.commentId | formatId('comment') }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="goodsId" label="商品ID" width="200" align="center" show-overflow-tooltip>
            <template slot-scope="scope"><el-link type="primary" @click="showGoodsIdDetail(scope.row.goodsId)">{{ scope.row.goodsId | formatId('goods') }}</el-link></template>
          </el-table-column>
          <el-table-column prop="userId" label="用户ID" width="200" align="center" show-overflow-tooltip>
            <template slot-scope="scope"><el-link type="primary" @click="showUserIdDetail(scope.row.userId)">{{ scope.row.userId | formatId('user') }}</el-link></template>
          </el-table-column>
          <el-table-column prop="content" label="评论内容" min-width="250">
            <template slot-scope="scope"><el-link type="primary" @click="showGoodsDetail(scope.row)">{{ scope.row.content }}</el-link></template>
          </el-table-column>
          <el-table-column prop="rating" label="评分" width="160" align="center">
            <template slot-scope="scope"><el-rate v-model="scope.row.rating" disabled></el-rate></template>
          </el-table-column>
          <el-table-column prop="locked" label="锁定" width="80" align="center">
            <template slot-scope="scope"><el-tag :type="scope.row.locked === 1 ? 'danger' : 'success'">{{ scope.row.locked === 1 ? '锁定' : '正常' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="createTime" label="评论时间" width="180">
            <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template slot-scope="scope">
              <el-button size="mini" :type="scope.row.locked === 1 ? 'success' : 'warning'" @click="scope.row.locked === 1 ? unlockGoodsComment(scope.row.commentId) : lockGoodsComment(scope.row.commentId)">{{ scope.row.locked === 1 ? '解锁' : '锁定' }}</el-button>
              <el-button size="mini" type="danger" @click="delGoodsComment(scope.row.commentId)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination style="margin-top:15px;" @current-change="p1=>{gPage=p1;loadGoodsComments()}" :current-page="gPage" :page-size.sync="gPageSize" :page-sizes="[10, 20, 50]" @size-change="gSizeChange" :total="gTotal" layout="total, sizes, prev, pager, next"></el-pagination>
      </el-tab-pane>
      <el-tab-pane label="订单评价" name="order">
        <div style="margin-bottom:15px;display:flex;flex-wrap:wrap;gap:10px;align-items:center;">
          <el-input v-model="orderSearchForm.content" placeholder="搜索评价内容" prefix-icon="el-icon-search" style="width:200px;" size="small" clearable @clear="loadOrderReviews" @keyup.enter.native="loadOrderReviews"></el-input>
          <el-select v-model="orderSortField" placeholder="排序" size="small" style="width:120px;" clearable @change="handleOrderSortChange">
            <el-option label="评分 ↓" value="rating_desc" />
            <el-option label="评分 ↑" value="rating_asc" />
            <el-option label="评论时间 ↓" value="createTime_desc" />
            <el-option label="评论时间 ↑" value="createTime_asc" />
          </el-select>
          <el-button type="primary" size="small" icon="el-icon-search" @click="loadOrderReviews">搜索</el-button>
          <el-button size="small" icon="el-icon-refresh" @click="resetOrderSearch">重置</el-button>
          <el-button type="success" size="small" icon="el-icon-download" style="margin-left:auto;" @click="exportOrderData">导出</el-button>
        </div>
        <el-table :data="orderReviews" v-loading="loading2" border>
          <el-table-column prop="reviewId" label="ID" width="200" align="center" show-overflow-tooltip>
            <template slot-scope="scope">
              <el-link type="primary" @click="showOrderDetail(scope.row)">{{ scope.row.reviewId | formatId('review') }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="orderId" label="订单ID" width="200" align="center" show-overflow-tooltip>
            <template slot-scope="scope"><el-link type="primary" @click="showOrderIdDetail(scope.row.orderId)">{{ scope.row.orderId | formatId('order') }}</el-link></template>
          </el-table-column>
          <el-table-column prop="userId" label="用户ID" width="200" align="center" show-overflow-tooltip>
            <template slot-scope="scope"><el-link type="primary" @click="showUserIdDetail(scope.row.userId)">{{ scope.row.userId | formatId('user') }}</el-link></template>
          </el-table-column>
          <el-table-column prop="rating" label="评分" width="160" align="center">
            <template slot-scope="scope"><el-rate v-model="scope.row.rating" disabled></el-rate></template>
          </el-table-column>
          <el-table-column prop="content" label="评价内容" min-width="250">
            <template slot-scope="scope"><el-link type="primary" @click="showOrderDetail(scope.row)">{{ scope.row.content }}</el-link></template>
          </el-table-column>
          <el-table-column prop="createTime" label="评价时间" width="180">
            <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
          </el-table-column>
        </el-table>
        <el-pagination style="margin-top:15px;" @current-change="p2=>{oPage=p2;loadOrderReviews()}" :current-page="oPage" :page-size.sync="oPageSize" :page-sizes="[10, 20, 50]" @size-change="oSizeChange" :total="oTotal" layout="total, sizes, prev, pager, next"></el-pagination>
      </el-tab-pane>
    </el-tabs>
    <el-dialog title="商品评论详情" :visible.sync="goodsCommentDetailVisible" width="600px">
      <table border="1" cellspacing="0" cellpadding="8" style="width:100%;border-color:#ebeef5;">
        <tr>
          <td style="background:#fafafa;width:100px;font-weight:bold;">评论ID</td>
          <td>{{ goodsCommentDetailRow.commentId | formatId('comment') }}</td>
          <td style="background:#fafafa;width:100px;font-weight:bold;">商品ID</td>
          <td><el-link type="primary" @click="showGoodsIdDetail(goodsCommentDetailRow.goodsId)">{{ goodsCommentDetailRow.goodsId | formatId('goods') }}</el-link></td>
        </tr>
        <tr>
          <td style="background:#fafafa;font-weight:bold;">用户ID</td>
          <td><el-link type="primary" @click="showUserIdDetail(goodsCommentDetailRow.userId)">{{ goodsCommentDetailRow.userId | formatId('user') }}</el-link></td>
          <td style="background:#fafafa;font-weight:bold;">评分</td>
          <td><el-rate v-model="goodsCommentDetailRow.rating" disabled></el-rate></td>
        </tr>
        <tr>
          <td style="background:#fafafa;font-weight:bold;">评论内容</td>
          <td colspan="3">{{ goodsCommentDetailRow.content }}</td>
        </tr>
        <tr>
          <td style="background:#fafafa;font-weight:bold;">评论时间</td>
          <td colspan="3">{{ goodsCommentDetailRow.createTime | formatTime }}</td>
        </tr>
      </table>
    </el-dialog>
    <el-dialog title="订单评价详情" :visible.sync="orderReviewDetailVisible" width="600px">
      <table border="1" cellspacing="0" cellpadding="8" style="width:100%;border-color:#ebeef5;">
        <tr>
          <td style="background:#fafafa;width:100px;font-weight:bold;">评价ID</td>
          <td>{{ orderReviewDetailRow.reviewId | formatId('review') }}</td>
          <td style="background:#fafafa;width:100px;font-weight:bold;">订单ID</td>
          <td><el-link type="primary" @click="showOrderIdDetail(orderReviewDetailRow.orderId)">{{ orderReviewDetailRow.orderId | formatId('order') }}</el-link></td>
        </tr>
        <tr>
          <td style="background:#fafafa;font-weight:bold;">用户ID</td>
          <td><el-link type="primary" @click="showUserIdDetail(orderReviewDetailRow.userId)">{{ orderReviewDetailRow.userId | formatId('user') }}</el-link></td>
          <td style="background:#fafafa;font-weight:bold;">评分</td>
          <td><el-rate v-model="orderReviewDetailRow.rating" disabled></el-rate></td>
        </tr>
        <tr>
          <td style="background:#fafafa;font-weight:bold;">评价内容</td>
          <td colspan="3">{{ orderReviewDetailRow.content }}</td>
        </tr>
        <tr>
          <td style="background:#fafafa;font-weight:bold;">评价时间</td>
          <td colspan="3">{{ orderReviewDetailRow.createTime | formatTime }}</td>
        </tr>
      </table>
    </el-dialog>
    <user-detail-dialog :visible.sync="userDetailVisible" :user-id="userDetailUserId" @show-post="showPostDetail" @show-goods="showGoodsIdDetail" @show-wall="showWallDetail"></user-detail-dialog>
    <goods-detail-dialog :visible.sync="goodsDetailVisible" :goods-id="goodsDetailGoodsId" @show-user="showUserIdDetail"></goods-detail-dialog>
    <post-detail-dialog :visible.sync="postDetailVisible" :post-id="postDetailPostId" @show-user="showUserIdDetail"></post-detail-dialog>
    <wall-detail-dialog :visible.sync="wallDetailVisible" :wall-id="wallDetailWallId" @show-user="showUserIdDetail"></wall-detail-dialog>
    <el-dialog title="订单详情" :visible.sync="orderIdDetailVisible" width="700px" top="5vh" append-to-body>
      <div v-loading="orderIdDetailLoading">
        <div v-if="orderIdDetailData">
          <el-card shadow="hover" style="margin-bottom:20px;">
            <div slot="header" style="font-weight:bold;"><i class="el-icon-document" style="margin-right:6px;"></i>订单信息</div>
            <table class="dt-table">
              <tr>
                <td class="dt-label">订单ID</td><td>{{ orderIdDetailData.orderId | formatId('order') }}</td>
                <td class="dt-label">订单状态</td><td><el-tag :type="['warning','primary','','success','danger','info'][orderIdDetailData.orderStatus]" size="small">{{ ['未支付','已支付','已发货','已完成','退货中','已完成'][orderIdDetailData.orderStatus] }}</el-tag></td>
              </tr>
              <tr>
                <td class="dt-label">商品ID</td><td><el-link type="primary" @click="showGoodsIdDetail(orderIdDetailData.goodsId)">{{ orderIdDetailData.goodsId | formatId('goods') }}</el-link></td>
                <td class="dt-label">购买数量</td><td>{{ orderIdDetailData.buyCount }}</td>
              </tr>
              <tr>
                <td class="dt-label">买家ID</td><td><el-link type="primary" @click="showUserIdDetail(orderIdDetailData.buyerId)">{{ orderIdDetailData.buyerId | formatId('user') }}</el-link></td>
                <td class="dt-label">卖家ID</td><td><el-link type="primary" @click="showUserIdDetail(orderIdDetailData.sellerId)">{{ orderIdDetailData.sellerId | formatId('user') }}</el-link></td>
              </tr>
              <tr>
                <td class="dt-label">总价</td><td><span style="color:#F56C6C;font-weight:bold;font-size:16px;">￥{{ orderIdDetailData.totalPrice }}</span></td>
                <td class="dt-label">创建时间</td><td>{{ orderIdDetailData.createTime | formatTime }}</td>
              </tr>
            </table>
          </el-card>
        </div>
        <div v-else style="text-align:center;color:#909399;padding:20px;">未找到该订单信息</div>
      </div>
    </el-dialog>

    
    <el-dialog title="锁定评论" :visible.sync="lockDialogVisible" width="450px" :close-on-click-modal="false">
      <p style="margin-bottom:15px;color:#666;">确定要锁定该评论吗？请选择锁定原因：</p>
      <el-radio-group v-model="lockCause" style="display:flex;flex-direction:column;gap:10px;">
        <el-radio label="含有违规内容">含有违规内容</el-radio>
        <el-radio label="含有广告信息">含有广告信息</el-radio>
        <el-radio label="恶意攻击或辱骂">恶意攻击或辱骂</el-radio>
        <el-radio label="虚假评价">虚假评价</el-radio>
        <el-radio label="侵犯他人隐私">侵犯他人隐私</el-radio>
        <el-radio label="其他">其他</el-radio>
      </el-radio-group>
      <el-input v-if="lockCause === '其他'" v-model="lockCauseCustom" placeholder="请输入锁定原因" style="margin-top:10px;" maxlength="50" show-word-limit></el-input>
      <div slot="footer">
        <el-button @click="lockDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmLock" :loading="lockLoading">确定锁定</el-button>
      </div>
    </el-dialog>

    
    <el-dialog title="删除评论" :visible.sync="deleteDialogVisible" width="450px" :close-on-click-modal="false">
      <p style="margin-bottom:15px;color:#666;">确定要删除该评论吗？请选择删除原因：</p>
      <el-radio-group v-model="deleteCause" style="display:flex;flex-direction:column;gap:10px;">
        <el-radio label="含有违规内容">含有违规内容</el-radio>
        <el-radio label="含有广告信息">含有广告信息</el-radio>
        <el-radio label="恶意攻击或辱骂">恶意攻击或辱骂</el-radio>
        <el-radio label="虚假评价">虚假评价</el-radio>
        <el-radio label="侵犯他人隐私">侵犯他人隐私</el-radio>
        <el-radio label="其他">其他</el-radio>
      </el-radio-group>
      <el-input v-if="deleteCause === '其他'" v-model="deleteCauseCustom" placeholder="请输入删除原因" style="margin-top:10px;" maxlength="50" show-word-limit></el-input>
      <div slot="footer">
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete" :loading="deleteLoading">确定删除</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getGoodsCommentList, deleteGoodsComment, lockGoodsComment, unlockGoodsComment, getOrderReviewList, getOrderList } from '@/api/manage'
import UserDetailDialog from '@/components/UserDetailDialog'
import GoodsDetailDialog from '@/components/GoodsDetailDialog'
import PostDetailDialog from '@/components/PostDetailDialog'
import WallDetailDialog from '@/components/WallDetailDialog'
export default {
  /** 注册子组件 */
  components: { UserDetailDialog, GoodsDetailDialog, PostDetailDialog, WallDetailDialog },
  /** 组件数据定义 */
  data() { return { activeTab: 'goods', goodsComments: [], gPage: 1, gPageSize: 10, gTotal: 0, loading1: false, selectedGoodsComments: [], goodsSearchForm: { content: '' }, goodsSortField: '', orderReviews: [], oPage: 1, oPageSize: 10, oTotal: 0, loading2: false, orderSearchForm: { content: '' }, orderSortField: '', goodsCommentDetailVisible: false, goodsCommentDetailRow: {}, orderReviewDetailVisible: false, orderReviewDetailRow: {}, userDetailVisible: false, userDetailUserId: '', goodsDetailVisible: false, goodsDetailGoodsId: '', postDetailVisible: false, postDetailPostId: '', wallDetailVisible: false, wallDetailWallId: '', orderIdDetailVisible: false, orderIdDetailLoading: false, orderIdDetailData: null, deleteDialogVisible: false, deleteCommentId: null, deleteCause: '含有违规内容', deleteCauseCustom: '', deleteLoading: false, lockDialogVisible: false, lockCommentId: null, lockCause: '含有违规内容', lockCauseCustom: '', lockLoading: false } },
  /** 生命周期：组件创建时加载数据 */
  created() { this.loadGoodsComments(); this.loadOrderReviews() },
  /** 组件方法定义 */
  methods: {
    async loadGoodsComments() {
      this.loading1 = true
      const res = await getGoodsCommentList({ pageNum: this.gPage, pageSize: 10, ...this.goodsSearchForm })
      if (res.code === 200) { this.goodsComments = res.data.records || []; this.gTotal = res.data.total || 0 }
      this.loading1 = false
    },
    async delGoodsComment(commentId) { this.deleteCommentId = commentId; this.deleteCause = '含有违规内容'; this.deleteCauseCustom = ''; this.deleteDialogVisible = true },
    async confirmDelete() {
      const cause = this.deleteCause === '其他' ? this.deleteCauseCustom : this.deleteCause
      if (this.deleteCause === '其他' && !this.deleteCauseCustom.trim()) { this.$message.warning('请输入删除原因'); return }
      this.deleteLoading = true
      try { await deleteGoodsComment(this.deleteCommentId, cause); this.$message.success('删除成功'); this.deleteDialogVisible = false; this.loadGoodsComments() } catch (e) {  }
      this.deleteLoading = false
    },
    lockGoodsComment(commentId) { this.lockCommentId = commentId; this.lockCause = '含有违规内容'; this.lockCauseCustom = ''; this.lockDialogVisible = true },
    async confirmLock() {
      const cause = this.lockCause === '其他' ? this.lockCauseCustom : this.lockCause
      if (this.lockCause === '其他' && !this.lockCauseCustom.trim()) { this.$message.warning('请输入锁定原因'); return }
      this.lockLoading = true
      try { await lockGoodsComment(this.lockCommentId, cause); this.$message.success('锁定成功'); this.lockDialogVisible = false; this.loadGoodsComments() } catch (e) {  }
      this.lockLoading = false
    },
    async unlockGoodsComment(commentId) {
      try { await this.$confirm('确定要解锁该评论吗？', '提示', { type: 'warning' }) } catch (e) { return }
      try { await unlockGoodsComment(commentId); this.$message.success('解锁成功'); this.loadGoodsComments() } catch (e) {  }
    },
    handleGoodsCommentSelectionChange(val) { this.selectedGoodsComments = val },
    async batchDeleteGoodsComment() {
      if (this.selectedGoodsComments.length === 0) return
      try { await this.$confirm('确定批量删除选中的商品评论？', '提示', { type: 'warning' }) } catch (e) { return }
      for (var i = 0; i < this.selectedGoodsComments.length; i++) { await deleteGoodsComment(this.selectedGoodsComments[i].commentId, '批量删除') }
      this.$message.success('批量删除成功'); this.selectedGoodsComments = []; this.loadGoodsComments()
    },
    async loadOrderReviews() {
      this.loading2 = true
      const res = await getOrderReviewList({ pageNum: this.oPage, pageSize: this.oPageSize, ...this.orderSearchForm })
      if (res.code === 200) { this.orderReviews = res.data.records || []; this.oTotal = res.data.total || 0 }
      this.loading2 = false
    },
    /** 显示商品详情弹窗 */
    showGoodsDetail(row) { this.goodsCommentDetailRow = row; this.goodsCommentDetailVisible = true },
    showOrderDetail(row) { this.orderReviewDetailRow = row; this.orderReviewDetailVisible = true },
    showUserIdDetail(userId) { this.userDetailUserId = userId; this.userDetailVisible = true },
    showGoodsIdDetail(goodsId) { this.goodsDetailGoodsId = goodsId; this.goodsDetailVisible = true },
    /** 显示帖子详情弹窗 */
    showPostDetail(postId) { this.postDetailPostId = postId; this.postDetailVisible = true },
    /** 显示表白墙详情弹窗 */
    showWallDetail(wallId) { this.wallDetailWallId = wallId; this.wallDetailVisible = true },
    async showOrderIdDetail(orderId) {
      this.orderIdDetailData = null
      this.orderIdDetailVisible = true
      this.orderIdDetailLoading = true
      try {
        const res = await getOrderList({ pageNum: 1, pageSize: 9999 })
        if (res.code === 200) {
          const records = res.data.records || res.data || []
          this.orderIdDetailData = records.find(o => o.orderId === orderId) || null
        }
      } catch (e) { this.orderIdDetailData = null }
      this.orderIdDetailLoading = false
    },
    resetGoodsSearch() {
      this.goodsSearchForm = { content: '' }
      this.goodsSortField = ''
      this.loadGoodsComments()
    },
    resetOrderSearch() {
      this.orderSearchForm = { content: '' }
      this.orderSortField = ''
      this.loadOrderReviews()
    },
    handleGoodsSortChange(val) {
      if (!val) return
      var parts = val.split('_')
      var field = parts[0]
      var order = parts[1]
      this.goodsComments.sort(function(a, b) {
        var va = a[field], vb = b[field]
        if (!va && !vb) return 0
        if (!va) return 1
        if (!vb) return -1
        if (field === 'rating' || typeof va === 'number') {
          return order === 'asc' ? va - vb : vb - va
        }
        var ta = new Date(va).getTime(), tb = new Date(vb).getTime()
        return order === 'asc' ? ta - tb : tb - ta
      })
    },
    handleOrderSortChange(val) {
      if (!val) return
      var parts = val.split('_')
      var field = parts[0]
      var order = parts[1]
      this.orderReviews.sort(function(a, b) {
        var va = a[field], vb = b[field]
        if (!va && !vb) return 0
        if (!va) return 1
        if (!vb) return -1
        if (field === 'rating' || typeof va === 'number') {
          return order === 'asc' ? va - vb : vb - va
        }
        var ta = new Date(va).getTime(), tb = new Date(vb).getTime()
        return order === 'asc' ? ta - tb : tb - ta
      })
    },
    gSizeChange(val) { this.gPageSize = val; this.gPage = 1; this.loadGoodsComments() },
    oSizeChange(val) { this.oPageSize = val; this.oPage = 1; this.loadOrderReviews() },
    exportGoodsData() {
      if (this.goodsComments.length === 0) { this.$message.warning('没有可导出的数据'); return }
      const columns = [
        { prop: 'commentId', label: '评论ID' },
        { prop: 'goodsId', label: '商品ID' },
        { prop: 'userId', label: '用户ID' },
        { prop: 'content', label: '评论内容' },
        { prop: 'rating', label: '评分' },
        { prop: 'createTime', label: '评论时间' }
      ]
      const headers = columns.map(c => c.label).join(',')
      const rows = this.goodsComments.map(row => columns.map(c => {
        const val = row[c.prop]
        return val != null ? String(val) : ''
      }).join(',')).join('\n')
      const csv = '\uFEFF' + headers + '\n' + rows
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '商品评论数据.csv'
      link.click()
      URL.revokeObjectURL(link.href)
      this.$message.success('导出成功')
    },
    exportOrderData() {
      if (this.orderReviews.length === 0) { this.$message.warning('没有可导出的数据'); return }
      const columns = [
        { prop: 'reviewId', label: '评价ID' },
        { prop: 'orderId', label: '订单ID' },
        { prop: 'userId', label: '用户ID' },
        { prop: 'rating', label: '评分' },
        { prop: 'content', label: '评价内容' },
        { prop: 'createTime', label: '评价时间' }
      ]
      const headers = columns.map(c => c.label).join(',')
      const rows = this.orderReviews.map(row => columns.map(c => {
        const val = row[c.prop]
        return val != null ? String(val) : ''
      }).join(',')).join('\n')
      const csv = '\uFEFF' + headers + '\n' + rows
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '订单评价数据.csv'
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

.id-cell { display: inline-block; max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; vertical-align: middle; }
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

::v-deep .el-tabs__item { font-weight: 600; color: #303133; }

::v-deep .el-dialog__title { font-weight: 600; color: #303133; }
</style>
