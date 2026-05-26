<template>
  <div v-loading="loading">
    <el-card>
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
        <span style="font-weight:bold;font-size:16px;">🛒 我的购物车</span>
        <div>
          <el-button size="small" type="warning" plain @click="clearInvalid" :disabled="invalidCount === 0">清理失效商品 ({{ invalidCount }})</el-button>
          <el-button size="small" type="danger" plain @click="clearAll">清空购物车</el-button>
        </div>
      </div>
      <div v-if="cartList.length === 0" style="text-align:center;padding:60px 0;">
        <i class="el-icon-shopping-cart-2" style="font-size:64px;color:#dcdfe6;"></i>
        <div style="color:#999;margin-top:16px;">购物车是空的</div>
        <el-button type="primary" style="margin-top:16px;" @click="$router.push('/goodsList').catch(() => {})">去逛逛</el-button>
      </div>
      <div v-else>
        <div style="margin-bottom:12px;padding:8px 12px;background:#f5f7fa;border-radius:4px;display:flex;align-items:center;">
          <el-checkbox v-model="selectAll" @change="handleSelectAll" :indeterminate="isIndeterminate">全选</el-checkbox>
          <span style="margin-left:20px;color:#999;">共 {{ cartList.length }} 件商品</span>
        </div>
        <div v-for="item in cartList" :key="item.cartId" class="cart-item" :class="{ 'cart-item-invalid': isInvalid(item) }">
          <el-checkbox v-model="item.selected" @change="handleItemChange" :disabled="isInvalid(item)" style="margin-right:12px;"></el-checkbox>
          <div class="cart-item-img" @click="!isInvalid(item) && $router.push('/goodsDetail/' + item.goodsId).catch(() => {})">
            <el-image v-if="item.goodsImages && !item.goodsDeleted" :src="getFirstImage(item.goodsImages)" fit="cover" style="width:80px;height:80px;border-radius:6px;cursor:pointer;"></el-image>
            <div v-else style="width:80px;height:80px;background:#f5f7fa;border-radius:6px;display:flex;align-items:center;justify-content:center;"><i class="el-icon-picture-outline" style="font-size:24px;color:#dcdfe6;"></i></div>
          </div>
          <div class="cart-item-info">
            <div class="cart-item-name" @click="!isInvalid(item) && $router.push('/goodsDetail/' + item.goodsId).catch(() => {})">
              {{ item.goodsName || '商品已删除' }}
              <el-tag v-if="item.goodsDeleted" type="danger" size="mini" style="margin-left:6px;">已删除</el-tag>
              <el-tag v-else-if="item.locked === 1" type="warning" size="mini" style="margin-left:6px;">已锁定</el-tag>
              <el-tag v-else-if="!item.goodsStatus" type="info" size="mini" style="margin-left:6px;">已下架</el-tag>
              <el-tag v-else-if="item.goodsCount !== undefined && item.goodsCount <= 0" type="danger" size="mini" style="margin-left:6px;">已售罄</el-tag>
            </div>
            <div style="color:#999;font-size:12px;margin-top:4px;" v-if="item.sellerNickname">卖家：{{ item.sellerNickname }}</div>
          </div>
          <div class="cart-item-price" style="color:#F56C6C;font-weight:bold;font-size:16px;min-width:80px;text-align:center;">
            ￥{{ item.goodsPrice || 0 }}
          </div>
          <div class="cart-item-quantity">
            <el-input-number v-model="item.quantity" :min="1" :max="item.goodsCount || 99" size="small" :disabled="isInvalid(item)" @change="updateQuantity(item)"></el-input-number>
          </div>
          <div class="cart-item-action">
            <el-button type="text" size="small" @click="removeItem(item.cartId)">删除</el-button>
          </div>
        </div>
        <div style="margin-top:20px;padding:16px;background:#fafafa;border-radius:6px;display:flex;justify-content:space-between;align-items:center;">
          <div>
            <el-button size="small" type="danger" plain @click="batchRemove" :disabled="selectedCount === 0">删除选中 ({{ selectedCount }})</el-button>
          </div>
          <div style="display:flex;align-items:center;gap:16px;">
            <span>已选 <b style="color:#F56C6C;">{{ selectedCount }}</b> 件</span>
            <span>合计：<b style="color:#F56C6C;font-size:20px;">￥{{ totalPrice.toFixed(2) }}</b></span>
            <el-button type="danger" size="medium" @click="checkout" :disabled="selectedCount === 0">结算</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 购买弹窗 -->
    <el-dialog :title="'确认购买（' + selectedBuyItems.length + '件商品）'" :visible.sync="buyDialogVisible" width="500px" @close="resetBuyForm">
      <div style="margin-bottom:15px;padding:12px;background:#f5f7fa;border-radius:6px;">
        <div v-for="(item, index) in selectedBuyItems" :key="item.cartId" :style="index > 0 ? 'margin-top:10px;padding-top:10px;border-top:1px dashed #dcdfe6;' : ''">
          <div style="font-weight:bold;font-size:14px;">{{ item.goodsName }}</div>
          <div style="color:#f56c6c;font-size:16px;margin-top:4px;">￥{{ item.goodsPrice }} × {{ item.quantity }} = <b>￥{{ ((item.goodsPrice || 0) * (item.quantity || 1)).toFixed(2) }}</b></div>
        </div>
        <div v-if="selectedBuyItems.length > 1" style="margin-top:12px;padding-top:10px;border-top:2px solid #e4e7ed;text-align:right;font-size:16px;">
          合计：<b style="color:#F56C6C;font-size:20px;">￥{{ selectedBuyItems.reduce((sum, i) => sum + (i.goodsPrice || 0) * (i.quantity || 1), 0).toFixed(2) }}</b>
        </div>
      </div>
      <el-form ref="buyForm" :model="buyForm" :rules="buyRules" label-width="100px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="buyForm.receiverName" placeholder="请输入收货人姓名"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="receiverPhone">
          <el-input v-model="buyForm.receiverPhone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="收货地址" prop="receiverAddress">
          <el-input v-model="buyForm.receiverAddress" type="textarea" :rows="2" placeholder="请输入详细收货地址"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="buyDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmBuy" :loading="buyLoading">确认下单</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Cart',
  data() {
    return {
      cartList: [],
      loading: false,
      selectAll: false,
      buyDialogVisible: false,
      buyLoading: false,
      currentBuyItem: {},
      selectedBuyItems: [],
      buyForm: { receiverName: '', receiverPhone: '', receiverAddress: '' },
      buyRules: {
        receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
        receiverPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
        receiverAddress: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
      }
    }
  },
  computed: {
    isIndeterminate() {
      const validItems = this.cartList.filter(i => !this.isInvalid(i))
      const selectedValid = validItems.filter(i => i.selected)
      return selectedValid.length > 0 && selectedValid.length < validItems.length
    },
    selectedCount() {
      return this.cartList.filter(i => i.selected && !this.isInvalid(i)).length
    },
    totalPrice() {
      return this.cartList.filter(i => i.selected && !this.isInvalid(i)).reduce((sum, i) => sum + (i.goodsPrice || 0) * (i.quantity || 1), 0)
    },
    invalidCount() {
      return this.cartList.filter(i => this.isInvalid(i)).length
    }
  },
  created() { this.loadCart() },
  methods: {
    isInvalid(item) {
      return item.goodsDeleted || item.locked === 1 || !item.goodsStatus || (item.goodsCount !== undefined && item.goodsCount <= 0)
    },
    getFirstImage(images) {
      if (!images) return ''
      const arr = images.split(',').filter(i => i.trim())
      return arr.length > 0 ? arr[0] : ''
    },
    async loadCart() {
      this.loading = true
      try {
        const res = await this.$axios.get('/cart/list')
        if (res.code === 200) {
          this.cartList = (res.data || []).map(i => ({ ...i, selected: false }))
        }
      } catch (e) {}
      this.loading = false
    },
    handleSelectAll(val) {
      this.cartList.forEach(i => { if (!this.isInvalid(i)) i.selected = val })
    },
    handleItemChange() {
      const validItems = this.cartList.filter(i => !this.isInvalid(i))
      this.selectAll = validItems.length > 0 && validItems.every(i => i.selected)
    },
    async updateQuantity(item) {
      try {
        await this.$axios.post('/cart/update', { cartId: item.cartId, quantity: item.quantity })
      } catch (e) {}
    },
    async removeItem(cartId) {
      await this.$confirm('确定删除该商品？', '提示', { type: 'warning' })
      const res = await this.$axios.delete('/cart/remove/' + cartId)
      if (res.code === 200) { this.$message.success('已删除'); this.loadCart() }
    },
    async batchRemove() {
      const ids = this.cartList.filter(i => i.selected && !this.isInvalid(i)).map(i => i.cartId).join(',')
      if (!ids) return
      const res = await this.$axios.delete('/cart/remove/' + ids)
      if (res.code === 200) { this.$message.success('已删除'); this.loadCart() }
    },
    async clearInvalid() {
      const ids = this.cartList.filter(i => this.isInvalid(i)).map(i => i.cartId).join(',')
      if (!ids) return
      const res = await this.$axios.delete('/cart/remove/' + ids)
      if (res.code === 200) { this.$message.success('已清理'); this.loadCart() }
    },
    async clearAll() {
      await this.$confirm('确定清空购物车？', '提示', { type: 'warning' })
      const res = await this.$axios.delete('/cart/clear')
      if (res.code === 200) { this.$message.success('已清空'); this.loadCart() }
    },
    checkout() {
      const selected = this.cartList.filter(i => i.selected && !this.isInvalid(i))
      if (selected.length === 0) return this.$message.warning('请选择商品')
      this.selectedBuyItems = selected
      this.currentBuyItem = selected[0]
      this.buyDialogVisible = true
    },
    resetBuyForm() {
      this.buyForm = { receiverName: '', receiverPhone: '', receiverAddress: '' }
      if (this.$refs.buyForm) this.$refs.buyForm.resetFields()
    },
    confirmBuy() {
      this.$refs.buyForm.validate(async valid => {
        if (!valid) return
        this.buyLoading = true
        const failedItems = []
        const successCartIds = []
        for (const item of this.selectedBuyItems) {
          try {
            const res = await this.$axios.post('/market-order/addOrder', {
              goodsId: item.goodsId,
              buyCount: item.quantity,
              receiverName: this.buyForm.receiverName,
              receiverPhone: this.buyForm.receiverPhone,
              receiverAddress: this.buyForm.receiverAddress
            })
            if (res.code === 200) {
              successCartIds.push(item.cartId)
            } else {
              failedItems.push(item.goodsName)
            }
          } catch (e) {
            failedItems.push(item.goodsName)
          }
        }
        this.buyLoading = false
        if (successCartIds.length > 0) {
          await this.$axios.delete('/cart/remove/' + successCartIds.join(','))
        }
        if (failedItems.length === 0) {
          this.$message.success('下单成功，请前往订单支付')
          this.buyDialogVisible = false
          this.$router.push('/myOrder')
        } else if (successCartIds.length > 0) {
          this.$message.warning(`部分商品下单失败：${failedItems.join('、')}`)
          this.buyDialogVisible = false
          this.$router.push('/myOrder')
        } else {
          this.$message.error('下单失败')
        }
      })
    }
  }
}
</script>

<style scoped>
.cart-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 12px;
  transition: all 0.3s ease;
}
.cart-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}
.cart-item-invalid {
  background: #fafafa;
  opacity: 0.6;
}
.cart-item-invalid:hover {
  transform: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.cart-item-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
}
.cart-item-name {
  font-size: 14px;
  color: #303133;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s ease;
}
.cart-item-name:hover {
  color: #409EFF;
}
.cart-item-quantity {
  margin: 0 16px;
}
</style>
