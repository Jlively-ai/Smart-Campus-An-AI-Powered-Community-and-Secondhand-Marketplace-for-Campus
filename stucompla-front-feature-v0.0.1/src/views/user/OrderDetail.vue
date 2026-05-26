<!--
  组件名：OrderDetail
  功能描述：订单详情页
  主要职责：
    1. 订单信息展示
    2. 物流跟踪
    3. 评价/发货操作
-->
<template>
  <div v-loading="loading">
    <div v-if="orderData" style="max-width:900px;margin:0 auto;">
      <!-- 顶部返回+状态 -->
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;">
        <el-page-header @back="$router.back()" content="订单详情"></el-page-header>
        <el-tag :type="statusType(orderData.orderStatus)" size="medium" effect="dark">{{ statusText(orderData.orderStatus) }}</el-tag>
      </div>

      <!-- 订单进度条 -->
      <el-card shadow="hover" style="margin-bottom:20px;">
        <div style="font-weight:bold;margin-bottom:16px;">订单进度</div>
        <el-steps v-if="!isSeller" :active="getStepActive(orderData.orderStatus)" align-center finish-status="success">
          <el-step title="下单" :description="formatTime(orderData.createTime)"></el-step>
          <el-step title="已支付" :description="orderData.orderStatus >= 1 && orderData.payTime ? formatTime(orderData.payTime) : ''"></el-step>
          <el-step title="已发货" :description="orderData.orderStatus >= 2 && orderData.sendTime ? formatTime(orderData.sendTime) : ''"></el-step>
          <el-step title="已完成" :description="orderData.orderStatus >= 3 && orderData.receiptTime ? formatTime(orderData.receiptTime) : ''"></el-step>
        </el-steps>
        <el-steps v-else :active="getStepActive(orderData.orderStatus)" align-center finish-status="success">
          <el-step title="收到订单" :description="formatTime(orderData.createTime)"></el-step>
          <el-step title="已收款" :description="orderData.orderStatus >= 1 && orderData.payTime ? formatTime(orderData.payTime) : ''"></el-step>
          <el-step title="已发货" :description="orderData.orderStatus >= 2 && orderData.sendTime ? formatTime(orderData.sendTime) : ''"></el-step>
          <el-step title="已完成" :description="orderData.orderStatus >= 3 && orderData.receiptTime ? formatTime(orderData.receiptTime) : ''"></el-step>
        </el-steps>
      </el-card>

      <!-- 基础信息 -->
      <el-card shadow="hover" style="margin-bottom:20px;">
        <div style="font-weight:bold;margin-bottom:16px;"><i class="el-icon-document" style="margin-right:6px;"></i>基础信息</div>
        <table class="detail-table">
          <tr>
            <td class="label">订单编号</td><td>{{ orderData.orderId }}</td>
            <td class="label">订单状态</td><td><el-tag :type="statusType(orderData.orderStatus)" size="small">{{ statusText(orderData.orderStatus) }}</el-tag></td>
          </tr>
          <tr>
            <td class="label">下单时间</td><td>{{ formatTime(orderData.createTime) }}</td>
            <td class="label">支付时间</td><td>{{ orderData.payTime ? formatTime(orderData.payTime) : '-' }}</td>
          </tr>
          <tr>
            <td class="label">发货时间</td><td>{{ orderData.sendTime ? formatTime(orderData.sendTime) : '-' }}</td>
            <td class="label">收货时间</td><td>{{ orderData.receiptTime ? formatTime(orderData.receiptTime) : '-' }}</td>
          </tr>
          <tr>
            <td class="label">购买数量</td><td>{{ orderData.buyCount }}</td>
            <td class="label">支付方式</td><td>在线支付</td>
          </tr>
          <tr>
            <td class="label">商品单价</td><td><span style="color:#F56C6C;">￥{{ orderData.goodsPrice || '-' }}</span></td>
            <td class="label">实付金额</td><td><span style="color:#F56C6C;font-weight:bold;font-size:16px;">￥{{ orderData.totalPrice }}</span></td>
          </tr>
        </table>
      </el-card>

      <!-- 购买人 & 出售人信息 -->
      <el-row :gutter="20" style="margin-bottom:20px;">
        <el-col :span="12">
          <el-card shadow="hover">
            <div style="font-weight:bold;margin-bottom:16px;"><i class="el-icon-user" style="margin-right:6px;"></i>购买人信息</div>
            <div style="display:flex;align-items:center;margin-bottom:12px;cursor:pointer;" @click="orderData.buyerId && $router.push('/userProfile/' + orderData.buyerId).catch(function(){})">
              <el-avatar v-if="orderData.buyerAvatar" :src="orderData.buyerAvatar" :size="48" style="margin-right:12px;"></el-avatar>
              <el-avatar v-else :size="48" style="margin-right:12px;background:#409EFF;">{{ (orderData.buyerNickname || '买')[0] }}</el-avatar>
              <div>
                <div style="font-weight:bold;color:#409EFF;">{{ orderData.buyerNickname || '未知' }}<i class="el-icon-arrow-right" style="font-size:12px;margin-left:4px;"></i></div>
                <div style="color:#999;font-size:12px;">点击查看用户主页</div>
              </div>
            </div>
            <table class="detail-table">
              <tr><td class="label">用户编号</td><td>{{ orderData.buyerId }}</td></tr>
              <tr><td class="label">用户昵称</td><td>{{ orderData.buyerNickname || '-' }}</td></tr>
              <tr><td class="label">联系方式</td><td>{{ orderData.buyerPhone || '-' }}</td></tr>
            </table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <div style="font-weight:bold;margin-bottom:16px;"><i class="el-icon-s-custom" style="margin-right:6px;"></i>出售人信息</div>
            <div style="display:flex;align-items:center;margin-bottom:12px;cursor:pointer;" @click="orderData.sellerId && $router.push('/userProfile/' + orderData.sellerId).catch(function(){})">
              <el-avatar v-if="orderData.sellerAvatar" :src="orderData.sellerAvatar" :size="48" style="margin-right:12px;"></el-avatar>
              <el-avatar v-else :size="48" style="margin-right:12px;background:#67C23A;">{{ (orderData.sellerNickname || '卖')[0] }}</el-avatar>
              <div>
                <div style="font-weight:bold;color:#409EFF;">{{ orderData.sellerNickname || '未知' }}<i class="el-icon-arrow-right" style="font-size:12px;margin-left:4px;"></i></div>
                <div style="color:#999;font-size:12px;">点击查看用户主页</div>
              </div>
            </div>
            <table class="detail-table">
              <tr><td class="label">用户编号</td><td>{{ orderData.sellerId }}</td></tr>
              <tr><td class="label">用户昵称</td><td>{{ orderData.sellerNickname || '-' }}</td></tr>
              <tr><td class="label">联系方式</td><td>{{ orderData.sellerPhone || '-' }}</td></tr>
            </table>
          </el-card>
        </el-col>
      </el-row>

      <!-- 收货信息 -->
      <el-card shadow="hover" style="margin-bottom:20px;" v-if="orderData.receiverName || orderData.receiverPhone || orderData.receiverAddress">
        <div style="font-weight:bold;margin-bottom:16px;"><i class="el-icon-location" style="margin-right:6px;"></i>收货信息</div>
        <table class="detail-table">
          <tr>
            <td class="label">收货人</td><td>{{ orderData.receiverName || '-' }}</td>
            <td class="label">联系电话</td><td>{{ orderData.receiverPhone || '-' }}</td>
          </tr>
          <tr>
            <td class="label">收货地址</td><td colspan="3">{{ orderData.receiverAddress || '-' }}</td>
          </tr>
        </table>
      </el-card>

      <!-- 商品信息 -->
      <el-card shadow="hover" style="margin-bottom:20px;">
        <div style="font-weight:bold;margin-bottom:16px;"><i class="el-icon-goods" style="margin-right:6px;"></i>商品信息</div>
        <div style="display:flex;gap:20px;">
          <div v-if="goodsImages.length > 0" style="flex-shrink:0;">
            <el-image :src="goodsImages[0]" style="width:160px;height:160px;border-radius:8px;" fit="cover" :preview-src-list="goodsImages"></el-image>
            <div v-if="goodsImages.length > 1" style="margin-top:6px;display:flex;gap:4px;flex-wrap:wrap;">
              <el-image v-for="(img, idx) in goodsImages.slice(1, 5)" :key="idx" :src="img" style="width:36px;height:36px;border-radius:4px;" fit="cover" :preview-src-list="goodsImages"></el-image>
            </div>
          </div>
          <div style="flex:1;">
            <table class="detail-table">
              <tr><td class="label">商品编号</td><td><el-link type="primary" @click="$router.push('/goodsDetail/' + orderData.goodsId).catch(function(){})">{{ orderData.goodsId }}</el-link></td></tr>
              <tr><td class="label">商品标题</td><td>{{ orderData.goodsName || '-' }}</td></tr>
              <tr><td class="label">商品描述</td><td>{{ orderData.goodsDetail || orderData.goodsDesc || '-' }}</td></tr>
              <tr><td class="label">所属类型</td><td>{{ orderData.categoryName || '-' }}</td></tr>
              <tr><td class="label">库存数量</td><td>{{ orderData.goodsCount || '-' }}</td></tr>
              <tr><td class="label">商品价格</td><td><span style="color:#F56C6C;">￥{{ orderData.goodsPrice || '-' }}</span></td></tr>
              <tr><td class="label">商品状态</td><td><el-tag :type="orderData.goodsStatus ? 'success' : 'info'" size="mini">{{ orderData.goodsStatus ? '上架' : '下架' }}</el-tag></td></tr>
              <tr><td class="label">发布时间</td><td>{{ formatTime(orderData.goodsCreateTime) }}</td></tr>
            </table>
          </div>
        </div>
      </el-card>

      <!-- 物流信息 -->
      <el-card shadow="hover" style="margin-bottom:20px;">
        <div style="font-weight:bold;margin-bottom:16px;"><i class="el-icon-truck" style="margin-right:6px;"></i>物流信息</div>
        <div v-if="orderData.logistics">
          <table class="detail-table">
            <tr>
              <td class="label">物流公司</td><td>{{ orderData.logistics.company || '-' }}</td>
              <td class="label">运单号</td><td>{{ orderData.logistics.trackingNo || '-' }}</td>
            </tr>
            <tr>
              <td class="label">当前状态</td><td><el-tag size="small" :type="orderData.logistics.trackingNo ? 'success' : 'info'">{{ orderData.logistics.trackingNo ? (orderData.logistics.currentStatus || '运输中') : '未发货' }}</el-tag></td>
              <td class="label">更新时间</td><td>{{ formatTime(orderData.logistics.createTime) }}</td>
            </tr>
            <tr>
              <td class="label">物流详情</td>
              <td colspan="3">
                <el-button type="text" @click="logisticsDialogVisible = true"><i class="el-icon-location-outline"></i> 查看物流轨迹</el-button>
              </td>
            </tr>
          </table>
        </div>
        <div v-else-if="orderData.orderStatus >= 2" style="text-align:center;color:#999;padding:20px 0;">
          <i class="el-icon-truck" style="font-size:32px;color:#dcdfe6;"></i>
          <div style="margin-top:8px;">暂无物流信息</div>
        </div>
        <div v-else style="text-align:center;color:#999;padding:20px 0;">
          <i class="el-icon-box" style="font-size:32px;color:#dcdfe6;"></i>
          <div style="margin-top:8px;">等待卖家发货</div>
        </div>
      </el-card>

      <!-- 售后信息 -->
      <el-card v-if="orderData.orderStatus === 4 || orderData.orderStatus === 5 || orderData.orderStatus === 6 || orderData.orderStatus === 7 || afterSaleInfo" shadow="hover" style="margin-bottom:20px;">
        <div style="font-weight:bold;margin-bottom:16px;"><i class="el-icon-warning-outline" style="margin-right:6px;color:#E6A23C;"></i>售后信息</div>
        <table class="detail-table">
          <tr>
            <td class="label">售后类型</td><td><el-tag size="small" type="warning">{{ currentAfterSaleType }}</el-tag></td>
            <td class="label">售后原因</td><td>{{ currentAfterSaleReason }}</td>
          </tr>
          <tr v-if="currentAfterSaleType === '仅退款' || currentAfterSaleType === '退货退款'">
            <td class="label">申请金额</td><td><span style="color:#F56C6C;font-weight:bold;">￥{{ currentAfterSaleAmount }}</span></td>
            <td class="label"></td><td></td>
          </tr>
          <tr v-if="currentAfterSaleType === '换货' || currentAfterSaleType === '补寄'">
            <td class="label">物品要求</td><td colspan="3">{{ currentAfterSaleRequirement }}</td>
          </tr>
          <tr v-if="afterSaleInfo && afterSaleInfo.handleResult">
            <td class="label">处理结果</td>
            <td colspan="3">
              <el-tag v-if="afterSaleInfo.handleResult === '同意'" size="small" type="success">同意</el-tag>
              <el-tag v-else size="small" type="danger">拒绝</el-tag>
            </td>
          </tr>
          <tr v-if="afterSaleInfo && afterSaleInfo.handleResult === '同意' && currentAfterSaleType === '退货退款' && afterSaleInfo.handleInfo && afterSaleInfo.handleInfo.returnAddress">
            <td class="label">退货地址</td><td colspan="3">{{ afterSaleInfo.handleInfo.returnAddress }}</td>
          </tr>
          <tr v-if="afterSaleInfo && afterSaleInfo.handleResult === '同意' && (currentAfterSaleType === '换货' || currentAfterSaleType === '补寄') && afterSaleInfo.handleInfo && afterSaleInfo.handleInfo.shipCompany">
            <td class="label">快递公司</td><td>{{ afterSaleInfo.handleInfo.shipCompany }}</td>
            <td class="label">运单号</td><td>{{ afterSaleInfo.handleInfo.shipTrackingNo || '-' }}</td>
          </tr>
          <tr v-if="afterSaleInfo && afterSaleInfo.handleResult === '拒绝' && afterSaleInfo.handleInfo && afterSaleInfo.handleInfo.rejectReason">
            <td class="label">拒绝原因</td><td colspan="3">{{ afterSaleInfo.handleInfo.rejectReason }}</td>
          </tr>
          <tr v-if="afterSaleInfo && afterSaleInfo.returnShippingSubmitted">
            <td class="label">退货快递</td><td>{{ afterSaleInfo.returnCompany || '-' }}</td>
            <td class="label">退货运单号</td><td>{{ afterSaleInfo.returnTrackingNo || '-' }}</td>
          </tr>
        </table>
        <!-- 买家填写退货快递信息 -->
        <div v-if="!isSeller && orderData.orderStatus === 5 && afterSaleInfo && afterSaleInfo.handleResult === '同意' && (currentAfterSaleType === '退货退款' || currentAfterSaleType === '换货') && !afterSaleInfo.returnShippingSubmitted" style="margin-top:16px;padding:12px;background:#fdf6ec;border-radius:8px;border:1px solid #faecd8;">
          <div style="font-weight:bold;margin-bottom:10px;color:#E6A23C;">填写退货快递信息</div>
          <el-form :model="returnShippingForm" label-width="100px" size="small">
            <el-form-item label="快递公司">
              <el-select v-model="returnShippingForm.company" placeholder="请选择快递公司" style="width:100%;" filterable allow-create>
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
            <el-form-item label="运单号">
              <el-input v-model="returnShippingForm.trackingNo" placeholder="请输入运单号"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitReturnShipping" :loading="returnShippingLoading">提交退货快递</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div style="text-align:center;padding:10px 0 30px;">
        <!-- 买家操作按钮 -->
        <template v-if="!isSeller">
          <el-button v-if="orderData.orderStatus === 0" type="primary" size="medium" @click="payOrder"><i class="el-icon-bank-card"></i> 支付订单</el-button>
          <el-button v-if="orderData.orderStatus === 2" type="success" size="medium" @click="receipt"><i class="el-icon-check"></i> 确认签收</el-button>
          <el-button v-if="orderData.orderStatus === 3" type="warning" size="medium" @click="openAfterSaleDialog"><i class="el-icon-refresh-left"></i> 申请售后</el-button>
          <el-button v-if="orderData.orderStatus === 3" type="info" size="medium" @click="openReviewDialog"><i class="el-icon-star-on"></i> 评价订单</el-button>
          <el-button v-if="orderReview" type="success" size="medium" @click="viewReviewDialogVisible = true"><i class="el-icon-view"></i> 查看评价</el-button>
          <el-button v-if="orderData.orderStatus === 9 && orderReview" type="primary" size="medium" @click="openFollowUpDialog"><i class="el-icon-chat-dot-round"></i> 追加评价</el-button>
          <el-button v-if="orderData.orderStatus === 9" type="warning" size="medium" @click="openAfterSaleDialog"><i class="el-icon-refresh-left"></i> 申请售后</el-button>
        </template>
        <!-- 卖家操作按钮 -->
        <template v-else>
          <el-button v-if="orderData.orderStatus === 1" type="primary" size="medium" @click="openShipDialog"><i class="el-icon-s-promotion"></i> 去发货</el-button>
          <el-button v-if="(orderData.orderStatus === 4 || orderData.orderStatus === 5) && !afterSaleInfo?.handleResult" type="warning" size="medium" @click="openHandleAfterSaleDialog"><i class="el-icon-edit"></i> 处理售后</el-button>
          <el-button v-if="orderData.orderStatus === 5 && afterSaleInfo && afterSaleInfo.returnShippingSubmitted && (currentAfterSaleType === '退货退款' || currentAfterSaleType === '换货')" type="success" size="medium" @click="confirmReturnReceipt"><i class="el-icon-check"></i> 确认退货并退款</el-button>
          <el-button v-if="orderReview" type="success" size="medium" @click="viewReviewDialogVisible = true"><i class="el-icon-view"></i> 查看评价</el-button>
        </template>
      </div>
    </div>
    <el-empty v-else-if="!loading" description="订单不存在"></el-empty>

    <!-- 物流轨迹弹窗 -->
    <el-dialog title="物流轨迹" :visible.sync="logisticsDialogVisible" width="550px">
      <div v-if="orderData && orderData.logisticsHistory && orderData.logisticsHistory.length > 0">
        <el-timeline>
          <el-timeline-item
            v-for="(item, idx) in orderData.logisticsHistory"
            :key="idx"
            :timestamp="formatTime(item.createTime)"
            placement="top"
            :type="idx === 0 ? 'primary' : 'info'"
            :size="idx === 0 ? 'normal' : 'small'"
          >
            <el-card shadow="never" style="padding:4px 8px;">
              <div style="font-size:14px;"><strong>{{ item.currentStatus || '状态更新' }}</strong></div>
              <div style="font-size:13px;color:#606266;margin-top:4px;white-space:pre-wrap;">{{ item.detail || '暂无详细信息' }}</div>
              <div style="font-size:12px;color:#999;margin-top:4px;" v-if="item.company || item.trackingNo">
                {{ item.company || '' }} {{ item.trackingNo ? '运单号: ' + item.trackingNo : '' }}
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
      <div v-else style="text-align:center;color:#999;padding:30px 0;">暂无物流轨迹信息</div>
      <span slot="footer">
        <el-button @click="logisticsDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 发货弹窗 -->
    <el-dialog title="发货" :visible.sync="shipDialogVisible" width="500px" v-if="orderData">
      <div style="margin-bottom:12px;padding:12px;background:#f0f9eb;border-radius:6px;border:1px solid #e1f3d8;">
        <div style="font-weight:bold;margin-bottom:8px;color:#67C23A;"><i class="el-icon-location" style="margin-right:4px;"></i>收货信息</div>
        <div>收货人：{{ orderData.receiverName || '-' }}</div>
        <div>联系电话：{{ orderData.receiverPhone || '-' }}</div>
        <div>收货地址：{{ orderData.receiverAddress || '-' }}</div>
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

    <!-- 申请售后弹窗（买家） -->
    <el-dialog title="申请售后" :visible.sync="afterSaleDialogVisible" width="520px" v-if="orderData">
      <el-form :model="afterSaleForm" label-width="100px">
        <el-form-item label="售后类型">
          <el-radio-group v-model="afterSaleForm.type">
            <el-radio label="仅退款">仅退款</el-radio>
            <el-radio label="退货退款">退货退款</el-radio>
            <el-radio label="换货">换货</el-radio>
            <el-radio label="补寄">补寄</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="售后原因">
          <el-input v-model="afterSaleForm.reason" type="textarea" :rows="3" placeholder="请详细描述售后原因"></el-input>
        </el-form-item>
        <el-form-item v-if="afterSaleForm.type === '仅退款' || afterSaleForm.type === '退货退款'" label="申请金额">
          <el-input-number v-model="afterSaleForm.amount" :min="0" :max="orderData.totalPrice" :precision="2" :step="1" style="width:200px;"></el-input-number>
          <span style="color:#999;font-size:12px;margin-left:8px;">最大￥{{ orderData.totalPrice }}</span>
        </el-form-item>
        <el-form-item v-if="afterSaleForm.type === '换货' || afterSaleForm.type === '补寄'" label="物品要求">
          <el-input v-model="afterSaleForm.requirement" type="textarea" :rows="3" placeholder="请描述换货/补寄的物品要求"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="afterSaleDialogVisible = false">取消</el-button><el-button type="primary" @click="submitAfterSale" :loading="afterSaleLoading">确认提交</el-button></span>
    </el-dialog>

    <!-- 处理售后弹窗（卖家） -->
    <el-dialog title="处理售后" :visible.sync="handleAfterSaleDialogVisible" width="560px" v-if="orderData">
      <!-- 显示买家售后信息 -->
      <div style="margin-bottom:16px;padding:12px;background:#fdf6ec;border-radius:6px;border:1px solid #faecd8;">
        <div style="font-weight:bold;margin-bottom:8px;color:#E6A23C;"><i class="el-icon-warning-outline" style="margin-right:4px;"></i>买家售后申请</div>
        <div style="font-size:13px;color:#606266;">
          <div style="margin-bottom:6px;"><strong>售后类型：</strong>{{ currentAfterSaleType || '-' }}</div>
          <div style="margin-bottom:6px;"><strong>售后原因：</strong>{{ currentAfterSaleReason || '-' }}</div>
          <div v-if="currentAfterSaleType === '仅退款' || currentAfterSaleType === '退货退款'" style="margin-bottom:6px;"><strong>申请金额：</strong><span style="color:#F56C6C;">￥{{ currentAfterSaleAmount || '-' }}</span></div>
          <div v-if="currentAfterSaleType === '换货' || currentAfterSaleType === '补寄'" style="margin-bottom:6px;"><strong>物品要求：</strong>{{ currentAfterSaleRequirement || '-' }}</div>
        </div>
      </div>
      <el-form :model="handleAfterSaleForm" label-width="100px">
        <el-form-item label="处理意见">
          <el-radio-group v-model="handleAfterSaleForm.agree">
            <el-radio :label="true">同意</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="handleAfterSaleForm.agree && currentAfterSaleType === '退货退款'" label="退货地址">
          <el-input v-model="handleAfterSaleForm.returnAddress" placeholder="请填写退货地址"></el-input>
        </el-form-item>
        <el-form-item v-if="handleAfterSaleForm.agree && (currentAfterSaleType === '换货' || currentAfterSaleType === '补寄')" label="快递公司">
          <el-select v-model="handleAfterSaleForm.shipCompany" placeholder="请选择快递公司" style="width:100%;" filterable allow-create>
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
        <el-form-item v-if="handleAfterSaleForm.agree && (currentAfterSaleType === '换货' || currentAfterSaleType === '补寄')" label="运单号">
          <el-input v-model="handleAfterSaleForm.shipTrackingNo" placeholder="请输入运单号"></el-input>
        </el-form-item>
        <el-form-item v-if="!handleAfterSaleForm.agree" label="拒绝原因">
          <el-input v-model="handleAfterSaleForm.rejectReason" type="textarea" :rows="3" placeholder="请填写拒绝原因"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="handleAfterSaleDialogVisible = false">取消</el-button><el-button type="primary" @click="submitHandleAfterSale" :loading="handleAfterSaleLoading">确认提交</el-button></span>
    </el-dialog>

    <!-- 评价弹窗（买家） -->
    <el-dialog title="评价订单" :visible.sync="reviewDialogVisible" width="500px" v-if="orderData">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="评分"><el-rate v-model="reviewForm.rating"></el-rate></el-form-item>
        <el-form-item label="评价内容"><el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="请输入评价内容"></el-input></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="reviewDialogVisible = false">取消</el-button><el-button type="primary" @click="submitReview" :loading="reviewLoading">提交评价</el-button></span>
    </el-dialog>

    <!-- 查看评价弹窗 -->
    <el-dialog title="订单评价" :visible.sync="viewReviewDialogVisible" width="550px" v-if="orderReview">
      <div style="padding:12px;background:#f5f7fa;border-radius:8px;margin-bottom:16px;">
        <div style="display:flex;align-items:center;margin-bottom:10px;">
          <span style="font-weight:bold;margin-right:12px;">买家评价</span>
          <el-rate v-model="orderReview.rating" disabled></el-rate>
        </div>
        <div style="font-size:14px;color:#303133;white-space:pre-wrap;line-height:1.6;">{{ orderReview.content }}</div>
        <div style="font-size:12px;color:#999;margin-top:8px;">{{ formatTime(orderReview.createTime) }}</div>
      </div>
      <div v-if="orderReview.reply" style="padding:12px;background:#ecf5ff;border-radius:8px;border-left:3px solid #409EFF;">
        <div style="font-weight:bold;margin-bottom:6px;color:#409EFF;">卖家回复</div>
        <div style="font-size:14px;color:#303133;white-space:pre-wrap;line-height:1.6;">{{ orderReview.reply }}</div>
        <div style="font-size:12px;color:#999;margin-top:8px;">{{ formatTime(orderReview.replyTime) }}</div>
      </div>
      <!-- 卖家回复表单 -->
      <div v-if="isSeller && !orderReview.reply" style="margin-top:16px;">
        <el-input v-model="replyForm.reply" type="textarea" :rows="3" placeholder="回复买家评价"></el-input>
        <div style="text-align:right;margin-top:8px;">
          <el-button type="primary" size="small" @click="submitReply" :loading="replyLoading">回复</el-button>
        </div>
      </div>
      <!-- 买家追评表单（直接在查看评价弹窗中） -->
      <div v-if="!isSeller && orderReview && !hasFollowUp" style="margin-top:16px;padding-top:12px;border-top:1px dashed #dcdfe6;">
        <div style="font-weight:bold;margin-bottom:8px;color:#E6A23C;">追加评价</div>
        <el-input v-model="followUpForm.content" type="textarea" :rows="3" placeholder="请输入追加评价内容"></el-input>
        <div style="text-align:right;margin-top:8px;">
          <el-button type="primary" size="small" @click="submitFollowUp" :loading="followUpLoading">提交追加评价</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 追加评价弹窗（买家） -->
    <el-dialog title="追加评价" :visible.sync="followUpDialogVisible" width="500px">
      <el-form :model="followUpForm" label-width="80px">
        <el-form-item label="追加评价"><el-input v-model="followUpForm.content" type="textarea" :rows="4" placeholder="请输入追加评价内容"></el-input></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="followUpDialogVisible = false">取消</el-button><el-button type="primary" @click="submitFollowUp" :loading="followUpLoading">提交追加评价</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'OrderDetail',
  data() {
    return { orderData: null, loading: false, isSeller: false, logisticsDialogVisible: false, shipDialogVisible: false, shipLoading: false, shipForm: { company: '', trackingNo: '' }, shipRules: { company: [{ required: true, message: '请选择快递公司', trigger: 'change' }], trackingNo: [{ required: true, message: '请输入运单号', trigger: 'blur' }] }, afterSaleDialogVisible: false, afterSaleLoading: false, afterSaleForm: { type: '仅退款', reason: '', amount: 0, requirement: '' }, handleAfterSaleDialogVisible: false, handleAfterSaleLoading: false, handleAfterSaleForm: { agree: true, rejectReason: '', returnAddress: '', shipCompany: '', shipTrackingNo: '' }, orderReview: null, reviewDialogVisible: false, reviewForm: { rating: 5, content: '' }, reviewLoading: false, viewReviewDialogVisible: false, followUpDialogVisible: false, followUpForm: { content: '' }, followUpLoading: false, replyForm: { reply: '' }, replyLoading: false, returnShippingForm: { company: '', trackingNo: '' }, returnShippingLoading: false }
  },
  computed: {
    goodsImages() {
      if (!this.orderData || !this.orderData.goodsImages) return []
      if (typeof this.orderData.goodsImages === 'string') {
        try { const arr = JSON.parse(this.orderData.goodsImages); return Array.isArray(arr) ? arr : this.orderData.goodsImages.split(',').filter(function(i) { return i.trim() }) } catch (e) { return this.orderData.goodsImages.split(',').filter(function(i) { return i.trim() }) }
      }
      return Array.isArray(this.orderData.goodsImages) ? this.orderData.goodsImages : []
    },
    afterSaleInfo() {
      if (!this.orderData) return null
      // 优先从remark解析售后信息
      if (this.orderData.remark) {
        try {
          const info = JSON.parse(this.orderData.remark)
          if (info && (info.afterSaleType || info.afterSaleReason || info.type || info.handleResult || info.returnShippingSubmitted)) {
            // 规范化布尔值字段
            if (info.returnShippingSubmitted) info.returnShippingSubmitted = true
            if (info.handleResult === '同意' || info.handleResult === true) info.handleResult = '同意'
            if (info.handleResult === '拒绝' || info.handleResult === false) info.handleResult = '拒绝'
            return info
          }
        } catch (e) {}
      }
      // 如果订单状态为退款中/退货中但没有remark详情，返回基本信息
      if (this.orderData.orderStatus === 4 || this.orderData.orderStatus === 5) {
        return { type: this.orderData.orderStatus === 4 ? '仅退款' : '退货退款', reason: '买家申请售后', fromStatus: true }
      }
      return null
    },
    currentAfterSaleType() {
      if (this.orderData && this.orderData.afterSaleType) return this.orderData.afterSaleType
      if (this.afterSaleInfo) return this.afterSaleInfo.afterSaleType || this.afterSaleInfo.type || '-'
      return '-'
    },
    currentAfterSaleReason() {
      if (this.orderData && this.orderData.afterSaleReason) return this.orderData.afterSaleReason
      if (this.afterSaleInfo) return this.afterSaleInfo.afterSaleReason || this.afterSaleInfo.reason || '-'
      return '-'
    },
    currentAfterSaleAmount() {
      if (this.orderData && this.orderData.afterSaleAmount) return this.orderData.afterSaleAmount
      if (this.afterSaleInfo) return this.afterSaleInfo.afterSaleAmount || this.afterSaleInfo.amount || '-'
      return '-'
    },
    currentAfterSaleRequirement() {
      if (this.orderData && this.orderData.afterSaleRequirement) return this.orderData.afterSaleRequirement
      if (this.afterSaleInfo) return this.afterSaleInfo.afterSaleRequirement || this.afterSaleInfo.requirement || '-'
      return '-'
    },
    hasFollowUp() {
      return this.orderReview && this.orderReview.content && this.orderReview.content.indexOf('【追加评价】') >= 0
    }
  },
  created() { this.loadOrder() },
  methods: {
    statusText(s) { return ['未支付', '已支付', '已发货', '已完成', '退款中', '退货中', '已退款', '已退货', '已完成', '已评价'][s] || '未知' },
    statusType(s) { return ['warning', 'primary', 'success', 'success', 'danger', 'warning', 'info', 'info', 'success', 'success'][s] || '' },
    getStepActive(s) { return [0, 1, 2, 3].indexOf(s) >= 0 ? s : (s >= 3 ? 3 : 0) },
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const pad = n => String(n).padStart(2, '0')
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    },
    async loadOrder() {
      this.loading = true
      const orderId = this.$route.params.id
      try {
        const res = await this.$axios.get('/market-order/detail/' + orderId)
        if (res.code === 200) {
          this.orderData = res.data
          // 判断 isSeller：先检查 URL query 参数 from=seller
          const from = this.$route.query.from
          if (from === 'seller') {
            this.isSeller = true
          } else if (from === 'buyer') {
            this.isSeller = false
          } else {
            // 没有 from 参数时，自动判断：比较 sellerId 与当前用户ID
            const userInfo = this.$store.getters.getUserInfo
            const currentUserId = userInfo ? String(userInfo.userId || userInfo.adminId || '') : ''
            const sellerId = this.orderData.sellerId ? String(this.orderData.sellerId) : ''
            this.isSeller = !!(sellerId && currentUserId && sellerId === currentUserId)
          }
          // 如果是卖家且订单状态为退款中/退货中，且尚未处理过，自动弹出处理售后弹窗
          if (this.isSeller && (this.orderData.orderStatus === 4 || this.orderData.orderStatus === 5) && !this.afterSaleInfo?.handleResult) {
            this.$nextTick(() => { this.openHandleAfterSaleDialog() })
          }
          // 加载评价信息
          this.loadOrderReview()
        }
      } catch (e) {}
      this.loading = false
    },
    async payOrder() {
      const res = await this.$axios.post('/market-order/payOrder/' + this.orderData.orderId)
      if (res.code === 200) { this.$message.success('支付成功'); this.loadOrder() }
    },
    async receipt() {
      await this.$confirm('确认签收？', '提示')
      const res = await this.$axios.post('/market-order/receipt/' + this.orderData.orderId)
      if (res.code === 200) { this.$message.success('签收成功'); this.loadOrder() }
    },
    openAfterSaleDialog() {
      this.afterSaleForm = { type: '仅退款', reason: '', amount: 0, requirement: '' }
      this.afterSaleDialogVisible = true
    },
    async submitAfterSale() {
      if (!this.afterSaleForm.reason.trim()) {
        this.$message.warning('请填写售后原因')
        return
      }
      this.afterSaleLoading = true
      try {
        const orderId = this.orderData.orderId
        const body = {
          type: this.afterSaleForm.type,
          reason: this.afterSaleForm.reason,
          amount: (this.afterSaleForm.type === '仅退款' || this.afterSaleForm.type === '退货退款') ? this.afterSaleForm.amount : null,
          requirement: (this.afterSaleForm.type === '换货' || this.afterSaleForm.type === '补寄') ? this.afterSaleForm.requirement : null
        }
        let res = null
        try {
          res = await this.$axios.post('/market-order/applyAfterSale/' + orderId, body)
        } catch (e) {
          res = null
        }
        // 如果专用接口不存在，回退到原有接口，将售后信息存入remark
        if (!res || res.code !== 200) {
          const remark = JSON.stringify({
            afterSaleType: this.afterSaleForm.type,
            afterSaleReason: this.afterSaleForm.reason,
            afterSaleAmount: (this.afterSaleForm.type === '仅退款' || this.afterSaleForm.type === '退货退款') ? this.afterSaleForm.amount : null,
            afterSaleRequirement: (this.afterSaleForm.type === '换货' || this.afterSaleForm.type === '补寄') ? this.afterSaleForm.requirement : null
          })
          res = await this.$axios.post('/market-order/applyReturn/' + orderId, { remark })
        }
        if (res.code === 200) {
          this.$message.success('售后申请已提交')
          this.afterSaleDialogVisible = false
          this.loadOrder()
        } else {
          this.$message.error(res.msg || '提交失败')
        }
      } catch (e) {
        this.$message.error('提交失败')
      }
      this.afterSaleLoading = false
    },
    openHandleAfterSaleDialog() {
      this.handleAfterSaleForm = { agree: true, rejectReason: '', returnAddress: '', shipCompany: '', shipTrackingNo: '' }
      this.handleAfterSaleDialogVisible = true
    },
    async submitHandleAfterSale() {
      if (this.handleAfterSaleForm.agree) {
        if (this.orderData.afterSaleType === '退货退款' && !this.handleAfterSaleForm.returnAddress.trim()) {
          this.$message.warning('请填写退货地址')
          return
        }
        if ((this.orderData.afterSaleType === '换货' || this.orderData.afterSaleType === '补寄')) {
          if (!this.handleAfterSaleForm.shipCompany.trim()) {
            this.$message.warning('请选择快递公司')
            return
          }
          if (!this.handleAfterSaleForm.shipTrackingNo.trim()) {
            this.$message.warning('请填写运单号')
            return
          }
        }
      } else {
        if (!this.handleAfterSaleForm.rejectReason.trim()) {
          this.$message.warning('请填写拒绝原因')
          return
        }
      }
      this.handleAfterSaleLoading = true
      try {
        // 尝试调用专用售后处理接口
        let res = null
        try {
          res = await this.$axios.post('/market-order/handleAfterSale', {
            orderId: this.orderData.orderId,
            agree: this.handleAfterSaleForm.agree,
            rejectReason: this.handleAfterSaleForm.rejectReason,
            returnAddress: this.handleAfterSaleForm.returnAddress,
            shipCompany: this.handleAfterSaleForm.shipCompany,
            shipTrackingNo: this.handleAfterSaleForm.shipTrackingNo
          })
        } catch (e) {
          res = null
        }
        // 如果专用接口不存在，回退到原有审核接口
        if (!res || res.code !== 200) {
          res = await this.$axios.post('/market-order/auditReturn', {
            orderId: this.orderData.orderId,
            auditState: this.handleAfterSaleForm.agree ? 1 : 0
          })
        }
        if (res.code === 200) {
          this.$message.success(this.handleAfterSaleForm.agree ? '已同意售后申请' : '已拒绝售后申请')
          this.handleAfterSaleDialogVisible = false
          this.loadOrder()
        } else {
          this.$message.error(res.msg || '处理失败')
        }
      } catch (e) {
        this.$message.error('处理失败')
      }
      this.handleAfterSaleLoading = false
    },
    openShipDialog() {
      this.shipForm = { company: '', trackingNo: '' }
      this.shipDialogVisible = true
    },
    confirmShip() {
      this.$refs.shipForm.validate(async valid => {
        if (!valid) return
        this.shipLoading = true
        try {
          const res = await this.$axios.post('/market-order/sendGoods/' + this.orderData.orderId, {
            company: this.shipForm.company,
            trackingNo: this.shipForm.trackingNo
          })
          if (res.code === 200) {
            this.$message.success('发货成功')
            this.shipDialogVisible = false
            this.loadOrder()
          } else {
            this.$message.error(res.msg || '发货失败')
          }
        } catch (e) {
          this.$message.error('发货失败')
        }
        this.shipLoading = false
      })
    },
    async loadOrderReview() {
      if (!this.orderData) return
      try {
        const res = await this.$axios.get('/order-review/getByOrder/' + this.orderData.orderId)
        if (res.code === 200 && res.data) {
          this.orderReview = res.data
        } else {
          this.orderReview = null
        }
      } catch (e) {
        this.orderReview = null
      }
    },
    openReviewDialog() {
      this.reviewForm = { rating: 5, content: '' }
      this.reviewDialogVisible = true
    },
    async submitReview() {
      if (!this.reviewForm.content.trim()) {
        this.$message.warning('请输入评价内容')
        return
      }
      this.reviewLoading = true
      try {
        const res = await this.$axios.post('/order-review/add', {
          orderId: this.orderData.orderId,
          rating: this.reviewForm.rating,
          content: this.reviewForm.content
        })
        if (res.code === 200) {
          this.$message.success('评价成功')
          this.reviewDialogVisible = false
          this.loadOrder()
        } else {
          this.$message.error(res.msg || '评价失败')
        }
      } catch (e) {
        this.$message.error('评价失败')
      }
      this.reviewLoading = false
    },
    openFollowUpDialog() {
      this.followUpForm = { content: '' }
      this.followUpDialogVisible = true
    },
    async submitFollowUp() {
      if (!this.followUpForm.content.trim()) {
        this.$message.warning('请输入追加评价内容')
        return
      }
      this.followUpLoading = true
      try {
        // 追加评价：后端会自动拼接【追加评价】前缀
        const res = await this.$axios.post('/order-review/add', {
          orderId: this.orderData.orderId,
          rating: this.orderReview.rating || 5,
          content: this.followUpForm.content
        })
        if (res.code === 200) {
          this.$message.success('追加评价成功')
          this.followUpDialogVisible = false
          this.loadOrderReview()
        } else {
          this.$message.error(res.msg || '追加评价失败')
        }
      } catch (e) {
        this.$message.error('追加评价失败')
      }
      this.followUpLoading = false
    },
    async submitReturnShipping() {
      if (!this.returnShippingForm.company.trim()) {
        this.$message.warning('请选择快递公司')
        return
      }
      if (!this.returnShippingForm.trackingNo.trim()) {
        this.$message.warning('请输入运单号')
        return
      }
      this.returnShippingLoading = true
      try {
        const res = await this.$axios.post('/market-order/submitReturnShipping/' + this.orderData.orderId, {
          returnCompany: this.returnShippingForm.company,
          returnTrackingNo: this.returnShippingForm.trackingNo
        })
        if (res.code === 200) {
          this.$message.success('退货快递信息已提交')
          this.loadOrder()
        } else {
          this.$message.error(res.msg || '提交失败')
        }
      } catch (e) {
        this.$message.error('提交失败')
      }
      this.returnShippingLoading = false
    },
    async confirmReturnReceipt() {
      try {
        await this.$confirm('确认已收到退货商品并同意退款？', '确认退货')
        const res = await this.$axios.post('/market-order/confirmReturnReceipt/' + this.orderData.orderId)
        if (res.code === 200) {
          this.$message.success('已确认退货并处理退款')
          this.loadOrder()
        } else {
          this.$message.error(res.msg || '操作失败')
        }
      } catch (e) {}
    },
    async submitReply() {
      if (!this.replyForm.reply.trim()) {
        this.$message.warning('请输入回复内容')
        return
      }
      this.replyLoading = true
      try {
        const res = await this.$axios.post('/order-review/reply', {
          orderId: this.orderData.orderId,
          reply: this.replyForm.reply
        })
        if (res.code === 200) {
          this.$message.success('回复成功')
          this.replyForm.reply = ''
          this.loadOrderReview()
        } else {
          this.$message.error(res.msg || '回复失败')
        }
      } catch (e) {
        this.$message.error('回复失败')
      }
      this.replyLoading = false
    }
  }
}
</script>

<style scoped>
.detail-table { width: 100%; border-collapse: separate; border-spacing: 0; border-radius: 8px; overflow: hidden; border: 1px solid #ebeef5; }
.detail-table td { padding: 10px 12px; border-bottom: 1px solid #ebeef5; font-size: 13px; color: #303133; }
.detail-table tr:last-child td { border-bottom: none; }
.detail-table .label { background: #fafafa; font-weight: 600; width: 100px; text-align: right; color: #606266; white-space: nowrap; }
</style>
