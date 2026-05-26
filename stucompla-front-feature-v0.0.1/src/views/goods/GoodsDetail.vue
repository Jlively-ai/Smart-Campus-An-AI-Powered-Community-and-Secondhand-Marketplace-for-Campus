<!--
  组件名：GoodsDetail
  功能描述：商品详情页
  主要职责：
    1. 商品信息展示
    2. 购买/加购物车操作
    3. 评价系统
    4. 举报/编辑/删除
    5. AI润色功能
-->
<template>
  <div v-loading="loading">
    <el-card v-if="goods">
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <el-page-header @back="$router.back()" content="商品详情"></el-page-header>
        <el-button v-if="isOwner" type="primary" size="small" icon="el-icon-edit" @click="openEditDialog">编辑</el-button>
      </div>
      <div style="margin-top:20px;position:relative;">
        <div class="goods-top-actions">
          <el-button size="mini" icon="el-icon-share" @click="shareGoods">分享 {{ goods.shareNum || 0 }}</el-button>
          <el-button v-if="!isOwner" size="mini" type="warning" plain icon="el-icon-warning" @click="openReportDialog">举报</el-button>
          <el-button v-if="goods.userId && loginType === 'user' && !isOwner" :type="isFollowing ? 'danger' : 'primary'" size="mini" plain @click="toggleFollow">{{ isFollowing ? '已关注卖家' : '关注卖家' }}</el-button>
        </div>
        <h2>{{ goods.goodsName }}
          <el-tag v-if="goods.locked === 1" type="danger" size="small" style="margin-left:8px;">已锁定</el-tag>
          <el-tag v-if="goods.locked === 1" type="warning" size="small" style="margin-left:4px;">仅自己可见</el-tag>
        </h2>
        <div v-if="goods.locked === 1 && goods.lockReason" style="font-size:12px;color:#999;margin-top:4px;">
          锁定原因：{{ goods.lockReason }}
        </div>
        <div style="font-size:28px;color:#f56c6c;font-weight:bold;margin:15px 0;">￥{{ goods.goodsPrice }}</div>
        <div style="color:#999;margin-bottom:10px;">
          <span>卖家：<span style="cursor:pointer;color:#409EFF;" @click="$router.push('/userProfile/' + goods.userId).catch(() => {})">{{ goods.nickname || goods.sellerName || '卖家' }}</span></span>
          <el-divider direction="vertical"></el-divider>
          <span>分类：{{ goods.goodsCategoryName || '未分类' }}</span>
          <el-divider direction="vertical"></el-divider>
          <span>库存：{{ goods.goodsCount }}</span>
          <el-divider direction="vertical"></el-divider>
          <span>浏览：{{ goods.viewNum }}</span>
          <el-divider direction="vertical"></el-divider>
          <span>发布时间：{{ goods.createTime | formatTime }}</span>
          <template v-if="goods.updateTime && goods.updateTime !== goods.createTime">
            <br/>
            <span style="color:#E6A23C;font-size:12px;">已重新编辑于 {{ goods.updateTime | formatTime }}</span>
          </template>
        </div>
        <div style="margin:20px 0;line-height:1.8;white-space:pre-wrap;">{{ goods.goodsDetail }}</div>
        <div v-if="goodsImages.length > 0" style="margin-top:10px;">
          <el-image v-for="(img, i) in goodsImages" :key="i" :src="img" :preview-src-list="goodsImages" fit="contain" class="goods-detail-img"></el-image>
        </div>
        <el-empty v-else description="暂无图片" :image-size="100"></el-empty>
        <div v-if="goods.goodsStatus && goods.goodsCount > 0 && !isOwner" style="margin-top:20px;">
          <el-input-number v-model="buyCount" :min="1" :max="goods.goodsCount" style="margin-right:15px;"></el-input-number>
          <el-button type="danger" size="medium" @click="buyGoods">立即购买</el-button>
          <el-button type="warning" size="medium" plain @click="addToCart">加入购物车</el-button>
        </div>
        <el-tag v-else-if="isOwner" type="success" style="margin-top:20px;">这是我发布的商品</el-tag>
        <el-tag v-else type="info" style="margin-top:20px;">已下架或无库存</el-tag>
      </div>
    </el-card>

    <el-card style="margin-top:20px;">
      <div slot="header"><span>商品评价 ({{ comments.length }})</span></div>
      <div style="margin-bottom:20px;">
        <MentionInput v-model="commentForm.content" :rows="2" placeholder="写下你对商品的评价..." @mention-change="onCommentMentionChange" />
        <div style="margin-top:10px;display:flex;align-items:center;justify-content:space-between;">
          <div><span style="margin-right:10px;">评分：</span><el-rate v-model="commentForm.rating"></el-rate></div>
          <el-button type="primary" size="small" @click="submitComment">发表评价</el-button>
        </div>
      </div>
      <div v-for="c in comments" :key="c.commentId" style="padding:15px 0;border-bottom:1px solid #eee;">
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <div style="display:flex;align-items:center;gap:8px;">
            <el-avatar :size="32" style="cursor:pointer;background:#409EFF;" @click.native="$router.push('/userProfile/' + c.userId).catch(() => {})">{{ (c.nickname || '用')[0] }}</el-avatar>
            <span style="font-weight:bold;cursor:pointer;color:#409EFF;" @click="$router.push('/userProfile/' + c.userId).catch(() => {})">{{ c.nickname || '用户' + c.userId }}</span>
            <el-rate v-model="c.rating" disabled style="display:inline-block;"></el-rate>
          </div>
          <div style="display:flex;align-items:center;gap:10px;">
            <el-button v-if="!isCommentOwner(c)" size="mini" type="text" style="color:#E6A23C;" @click="openCommentReportDialog(c)">举报</el-button>
            <span style="color:#999;font-size:12px;">{{ c.createTime | formatTime }}</span>
          </div>
        </div>
        <div style="margin-top:8px;padding-left:40px;">{{ c.content }}</div>
      </div>
      <el-empty v-if="comments.length === 0" description="暂无评价"></el-empty>
    </el-card>
    <el-dialog title="举报" :visible.sync="reportDialogVisible" width="420px" class="report-dialog">
      <div class="report-dialog-body">
        <el-form :model="reportForm" label-width="80px">
          <el-form-item label="举报原因">
            <el-select v-model="reportForm.reason" placeholder="请选择举报原因" style="width:100%;">
              <el-option label="垃圾广告" value="垃圾广告"></el-option>
              <el-option label="色情低俗" value="色情低俗"></el-option>
              <el-option label="违法违规" value="违法违规"></el-option>
              <el-option label="诈骗信息" value="诈骗信息"></el-option>
              <el-option label="人身攻击" value="人身攻击"></el-option>
              <el-option label="虚假信息" value="虚假信息"></el-option>
              <el-option label="其他" value="其他"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item v-if="reportForm.reason === '其他'" label="详细原因">
            <el-input v-model="reportForm.customReason" type="textarea" :rows="3" placeholder="请描述举报原因"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer"><el-button @click="reportDialogVisible = false">取消</el-button><el-button type="primary" @click="submitReport">确定</el-button></span>
    </el-dialog>
    <el-dialog title="确认购买" :visible.sync="buyDialogVisible" width="520px" @close="resetBuyForm" class="buy-dialog">
      <div class="buy-dialog-body">
        <div v-if="goods" class="buy-goods-summary">
          <div class="buy-goods-name">{{ goods.goodsName }}</div>
          <div class="buy-goods-price">￥{{ goods.goodsPrice }} × {{ buyCount }} = <b>￥{{ (goods.goodsPrice * buyCount).toFixed(2) }}</b></div>
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
      </div>
      <span slot="footer">
        <el-button @click="buyDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmBuy" :loading="buyLoading">确认下单</el-button>
      </span>
    </el-dialog>
    <el-dialog title="编辑商品" :visible.sync="editDialogVisible" width="720px" @close="resetEditForm" class="edit-dialog">
      <div class="edit-dialog-body">
        <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px">
          <el-form-item label="商品名称" prop="goodsName">
            <el-input v-model="editForm.goodsName" placeholder="请输入商品名称"></el-input>
          </el-form-item>
          <el-form-item label="商品价格" prop="goodsPrice">
            <el-input-number v-model="editForm.goodsPrice" :min="0" :precision="2" style="width:200px;"></el-input-number>
          </el-form-item>
          <el-form-item label="商品分类" prop="goodsCategoryId">
            <el-select v-model="editForm.goodsCategoryId" placeholder="请选择分类" style="width:100%;">
              <el-option v-for="c in goodsCategories" :key="c.goodsCategoryId" :label="c.goodsCategoryName" :value="c.goodsCategoryId"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="商品数量" prop="goodsCount">
            <el-input-number v-model="editForm.goodsCount" :min="1" style="width:200px;"></el-input-number>
          </el-form-item>
          <el-form-item label="商品详情" prop="goodsDetail">
            <el-input type="textarea" v-model="editForm.goodsDetail" :rows="6" placeholder="请描述商品详情"></el-input>
            <el-button type="warning" size="mini" icon="el-icon-magic-stick" :loading="polishing" style="margin-top:6px;" @click="aiPolish">AI润色内容</el-button>
          </el-form-item>
          <el-form-item label="商品图片">
            <el-upload action="/dev_api/image/upload" :headers="uploadHeaders" name="files" list-type="picture-card" :file-list="editFileList" :on-success="handleUploadSuccess" :on-remove="handleRemove" :on-error="function() { $message.error('图片上传失败') }" :before-upload="beforeUpload" accept="image/*" :limit="9" :on-exceed="function() { $message.warning('最多上传9张图片') }">
              <i class="el-icon-plus"></i>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import MentionInput from '@/components/MentionInput.vue'
export default {
  name: 'GoodsDetail',
  components: { MentionInput },
  data() { return { goods: null, buyCount: 1, loading: true, isFollowing: false, isOwner: false, comments: [], commentForm: { content: '', rating: 5, mentionUsers: '' }, reportDialogVisible: false, reportForm: { targetType: '', targetId: '', reason: '', customReason: '' }, buyDialogVisible: false, buyLoading: false, buyForm: { receiverName: '', receiverPhone: '', receiverAddress: '' }, buyRules: { receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }], receiverPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }], receiverAddress: [{ required: true, message: '请输入收货地址', trigger: 'blur' }] }, editDialogVisible: false, editLoading: false, polishing: false, editForm: { goodsId: '', goodsName: '', goodsPrice: 0, goodsCategoryId: null, goodsCount: 1, goodsDetail: '', goodsImages: '' }, editRules: { goodsName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }], goodsPrice: [{ required: true, message: '请输入价格', trigger: 'blur' }], goodsCategoryId: [{ required: true, message: '请选择分类', trigger: 'change' }] }, goodsCategories: [], editFileList: [], uploadedUrls: [], uploadHeaders: { Authorization: sessionStorage.getItem('token') || '' } } },
  computed: {
    goodsImages() { return this.goods && this.goods.goodsImages ? this.goods.goodsImages.split(',').filter(img => img.trim()) : [] },
    loginType() { return this.$store.getters.getLoginType }
  },
  created() { this.loadGoods(); this.loadComments(); this.loadCategories() },
  methods: {
    async loadGoods() {
      try {
        const res = await this.$axios.get('/goods/' + this.$route.params.id)
        if (res.code === 200) {
          this.goods = res.data
          if (res.data.hasShared) sessionStorage.setItem('shared_goods_' + res.data.goodsId, 'true')
        }
      } catch(e) {}
      const userInfo = this.$store.getters.getUserInfo
      if (userInfo && this.goods) {
        this.isOwner = String(userInfo.userId || userInfo.adminId) === String(this.goods.userId)
      }
      if (this.goods && this.goods.userId) {
        const userInfo = this.$store.getters.getUserInfo
        if ((userInfo.userId || userInfo.adminId) !== this.goods.userId) {
          try {
            const followRes = await this.$axios.get('/follow/check/' + this.goods.userId)
            if (followRes.code === 200) this.isFollowing = followRes.data
          } catch(e) {}
        }
      }
      this.loading = false
    },
    async loadComments() {
      const res = await this.$axios.get('/goods-comment/list/' + this.$route.params.id, { params: { pageNum: 1, pageSize: 50 } })
      if (res.code === 200) this.comments = res.data.records || []
    },
    async toggleFollow() {
      if (this.isFollowing) {
        await this.$axios.delete('/follow/cancel/' + this.goods.userId)
        this.isFollowing = false
        this.$message.success('已取消关注')
      } else {
        await this.$axios.post('/follow/add/' + this.goods.userId)
        this.isFollowing = true
        this.$message.success('关注成功')
      }
    },
    async shareGoods() {
      const id = this.goods.goodsId
      try {
        const res = await this.$axios.post('/goods/share/' + id)
        if (res.code === 200) {
          this.goods.shareNum = res.data
          sessionStorage.setItem('shared_goods_' + id, 'true')
        } else if (res.msg && res.msg.includes('已分享')) {
          sessionStorage.setItem('shared_goods_' + id, 'true')
        } else {
          this.$message.warning(res.msg || '分享失败')
        }
      } catch(e) {}
      if (navigator.clipboard) {
        navigator.clipboard.writeText(window.location.href)
        this.$message.success('链接已复制到剪贴板')
      } else {
        const input = document.createElement('input')
        input.value = window.location.href
        document.body.appendChild(input)
        input.select()
        document.execCommand('copy')
        document.body.removeChild(input)
        this.$message.success('链接已复制到剪贴板')
      }
    },
    buyGoods() {
      this.buyDialogVisible = true
    },
    async addToCart() {
      // 先检查购物车是否已有该商品
      try {
        const cartRes = await this.$axios.get('/cart/list')
        if (cartRes.code === 200 && cartRes.data) {
          const exists = (cartRes.data || []).find(item => item.goodsId === this.goods.goodsId)
          if (exists) {
            try {
              await this.$confirm('该商品已在购物车中，是否继续添加？', '提示', {
                confirmButtonText: '继续添加',
                cancelButtonText: '取消',
                type: 'warning'
              })
            } catch (e) { return }
          }
        }
      } catch (e) {}
      const res = await this.$axios.post('/cart/add', { goodsId: this.goods.goodsId, quantity: this.buyCount })
      if (res.code === 200) { this.$message.success(res.msg || '已加入购物车') }
      else this.$message.error(res.msg || '加入购物车失败')
    },
    resetBuyForm() {
      this.buyForm = { receiverName: '', receiverPhone: '', receiverAddress: '' }
      if (this.$refs.buyForm) this.$refs.buyForm.resetFields()
    },
    confirmBuy() {
      this.$refs.buyForm.validate(async valid => {
        if (!valid) return
        this.buyLoading = true
        try {
          const res = await this.$axios.post('/market-order/addOrder', {
            goodsId: this.goods.goodsId,
            buyCount: this.buyCount,
            receiverName: this.buyForm.receiverName,
            receiverPhone: this.buyForm.receiverPhone,
            receiverAddress: this.buyForm.receiverAddress
          })
          if (res.code === 200) {
            this.$message.success('下单成功，请前往订单支付')
            this.buyDialogVisible = false
            this.$router.push('/myOrder')
          } else {
            this.$message.error(res.msg || '下单失败')
          }
        } catch (e) {
          this.$message.error('下单失败')
        }
        this.buyLoading = false
      })
    },
    async submitComment() {
      if (!this.commentForm.content) return this.$message.warning('请输入评价内容')
      const res = await this.$axios.post('/goods-comment/add', { ...this.commentForm, goodsId: this.$route.params.id })
      if (res.code === 200) { this.$message.success('评价成功'); this.commentForm = { content: '', rating: 5, mentionUsers: '' }; this.loadComments() }
    },
    openReportDialog() {
      this.reportForm = { targetType: 'goods', targetId: String(this.$route.params.id), reason: '', customReason: '' }
      this.reportDialogVisible = true
    },
    openCommentReportDialog(comment) {
      this.reportForm = { targetType: 'goods_comment', targetId: String(comment.commentId), reason: '', customReason: '' }
      this.reportDialogVisible = true
    },
    isCommentOwner(comment) {
      const userInfo = this.$store.getters.getUserInfo
      return userInfo && String(userInfo.userId || userInfo.adminId) === String(comment.userId)
    },
    async submitReport() {
      if (!this.reportForm.reason) return this.$message.warning('请选择举报原因')
      if (this.reportForm.reason === '其他' && !this.reportForm.customReason) return this.$message.warning('请填写详细原因')
      const reason = this.reportForm.reason === '其他' ? '其他：' + this.reportForm.customReason : this.reportForm.reason
      const res = await this.$axios.post('/report/submit', { targetType: this.reportForm.targetType, targetId: this.reportForm.targetId, reason: reason })
      if (res.code === 200) { this.$message.success('举报成功'); this.reportDialogVisible = false }
      else this.$message.error(res.msg || '举报失败')
    },
    onCommentMentionChange(userIds) {
      this.commentForm.mentionUsers = JSON.stringify(userIds)
    },
    async loadCategories() {
      const res = await this.$axios.get('/goods-category/list')
      if (res.code === 200) this.goodsCategories = res.data || []
    },
    openEditDialog() {
      this.editForm = { goodsId: this.goods.goodsId, goodsName: this.goods.goodsName, goodsPrice: this.goods.goodsPrice, goodsCategoryId: this.goods.goodsCategoryId, goodsCount: this.goods.goodsCount, goodsDetail: this.goods.goodsDetail, goodsImages: this.goods.goodsImages || '' }
      this.uploadedUrls = this.goods.goodsImages ? this.goods.goodsImages.split(',').filter(u => u.trim()) : []
      this.editFileList = this.uploadedUrls.map((url, index) => ({ name: 'image' + index, url: url }))
      this.editDialogVisible = true
    },
    resetEditForm() {
      this.editForm = { goodsId: '', goodsName: '', goodsPrice: 0, goodsCategoryId: null, goodsCount: 1, goodsDetail: '', goodsImages: '' }
      this.editFileList = []
      this.uploadedUrls = []
      if (this.$refs.editForm) this.$refs.editForm.resetFields()
    },
    beforeUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isImage) { this.$message.error('只能上传图片文件'); return false }
      if (!isLt5M) { this.$message.error('图片大小不能超过5MB'); return false }
      return true
    },
    handleUploadSuccess(response) {
      const res = typeof response === 'string' ? JSON.parse(response) : response
      if (res.code === 200 && res.data) {
        const urls = res.data.split(',').filter(u => u.trim())
        this.uploadedUrls = this.uploadedUrls.concat(urls)
        this.editForm.goodsImages = this.uploadedUrls.join(',')
      }
    },
    handleRemove(file) {
      const url = file.response ? (typeof file.response === 'string' ? JSON.parse(file.response).data : file.response.data) : file.url
      if (url) {
        const urls = url.split(',').filter(u => u.trim())
        urls.forEach(u => {
          const idx = this.uploadedUrls.indexOf(u.trim())
          if (idx > -1) this.uploadedUrls.splice(idx, 1)
        })
        this.editForm.goodsImages = this.uploadedUrls.join(',')
      }
    },
    submitEdit() {
      this.$refs.editForm.validate(async valid => {
        if (!valid) return
        this.editLoading = true
        const res = await this.$axios.post('/goods/edit', this.editForm)
        if (res.code === 200) { this.$message.success('编辑成功，等待审核'); this.editDialogVisible = false; this.loadGoods() }
        else this.$message.error(res.msg || '编辑失败')
        this.editLoading = false
      })
    },
    async aiPolish() {
      if (!this.editForm.goodsDetail || !this.editForm.goodsDetail.trim()) {
        this.$message.warning('请先输入商品详情再润色')
        return
      }
      this.polishing = true
      try {
        var res = await this.$axios.post('/ai/polish', { content: this.editForm.goodsDetail, type: 'goods' })
        if (res.code === 200 && res.data && res.data.polished) {
          this.editForm.goodsDetail = res.data.polished
          if (res.data.hint) { this.$message.warning(res.data.hint) }
          else { this.$message.success('润色完成') }
        } else {
          this.$message.error('润色失败')
        }
      } catch (e) {
        this.$message.error('润色请求失败')
      }
      this.polishing = false
    }
  }
}
</script>

<style scoped>
.goods-top-actions {
  position: absolute;
  top: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.goods-detail-img {
  width: 100%;
  max-height: 200px;
  border-radius: 10px;
  margin-bottom: 10px;
  cursor: pointer;
  background: #f5f7fa;
  display: block;
  overflow: hidden;
  transition: all 0.3s ease;
}
.goods-detail-img:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}
.goods-detail-img >>> .el-image__inner {
  max-height: 200px !important;
  object-fit: contain !important;
}

/* ===== Dialog Beautification ===== */
::v-deep .report-dialog .el-dialog__body,
::v-deep .buy-dialog .el-dialog__body,
::v-deep .edit-dialog .el-dialog__body {
  padding: 0;
}
.report-dialog-body,
.buy-dialog-body,
.edit-dialog-body {
  padding: 24px;
}
.buy-goods-summary {
  margin-bottom: 20px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff5f5, #fff0f0);
  border-radius: 12px;
  border: 1px solid #ffe0e0;
}
.buy-goods-name {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}
.buy-goods-price {
  color: #f56c6c;
  font-size: 18px;
  margin-top: 8px;
}
.edit-dialog-body {
  max-height: 60vh;
  overflow-y: auto;
}
::v-deep .edit-dialog .el-upload--picture-card {
  border-radius: 10px;
  transition: all 0.3s ease;
}
::v-deep .edit-dialog .el-upload--picture-card:hover {
  border-color: #409EFF;
  transform: translateY(-2px);
}
</style>
