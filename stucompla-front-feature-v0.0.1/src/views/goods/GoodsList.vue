<!--
  组件名：GoodsList
  功能描述：商品列表页
  主要职责：
    1. 商品列表展示（图文/列表模式切换）
    2. 搜索/分类/排序筛选
    3. 发布闲置商品弹窗
    4. AI润色功能
-->
<template>
  <div>
    <el-card>
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
        <span>二手交易</span>
        <div style="display:flex;align-items:center;gap:10px;">
          <el-button-group>
            <el-button :type="viewMode === 'list' ? 'primary' : 'default'" size="mini" icon="el-icon-s-grid" @click="viewMode = 'list'" title="列表模式"></el-button>
            <el-button :type="viewMode === 'card' ? 'primary' : 'default'" size="mini" icon="el-icon-picture" @click="viewMode = 'card'" title="图文模式"></el-button>
          </el-button-group>
          <el-button type="success" size="small" icon="el-icon-plus" @click="openPublishDialog">发布闲置</el-button>

        </div>
      </div>
      <div style="margin-bottom:15px;display:flex;gap:10px;align-items:center;flex-wrap:wrap;">
        <search-panel v-model="keyName" module="goods" placeholder="搜索商品名称" input-style="width:220px;" @search="loadGoods" @clear="keyName = ''; loadGoods()"></search-panel>
        <el-input v-model="searchNickname" placeholder="搜索卖家昵称" prefix-icon="el-icon-user" clearable size="small" style="width:160px;" @clear="loadGoods" @keyup.enter.native="loadGoods"></el-input>
        <el-select v-model="goodsCategoryId" placeholder="选择分类" clearable @change="loadGoods" style="width:140px;">
          <el-option v-for="c in goodsCategories" :key="c.goodsCategoryId" :label="c.goodsCategoryName" :value="c.goodsCategoryId"></el-option>
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="loadGoods">搜索</el-button>
        <div style="margin-left:auto;display:flex;align-items:center;gap:4px;">
          <span style="color:#999;font-size:13px;">排序：</span>
          <el-radio-group v-model="sortBy" size="mini" @change="loadGoods">
            <el-radio-button label="time">最新</el-radio-button>
            <el-radio-button label="priceAsc">价格↑</el-radio-button>
            <el-radio-button label="priceDesc">价格↓</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 图文模式 -->
      <div v-if="viewMode === 'card'">
        <el-row :gutter="0" class="goods-card-row">
          <el-col :span="6" v-for="item in goodsList" :key="item.goodsId">
            <div class="goods-card" @click="$router.push('/goodsDetail/' + item.goodsId)">
              <div class="goods-card-img" v-if="item.goodsImages">
                <img :src="item.goodsImages.split(',')[0]" />
              </div>
              <div class="goods-card-img goods-card-no-img" v-else>
                <span class="goods-card-no-img-text">{{ item.goodsName || '暂无名称' }}</span>
              </div>
              <div class="goods-card-body">
                <div class="goods-card-name">{{ item.goodsName }}</div>
                <div class="goods-card-price">￥{{ item.goodsPrice }}</div>
                <div class="goods-card-meta">
                  <span style="cursor:pointer;color:#409EFF;" @click.stop="$router.push('/userProfile/' + item.userId)">{{ item.nickname || '匿名' }}</span>
                  <span style="margin-left:6px;"><i class="el-icon-box"></i> 库存:{{ item.goodsCount || 0 }}</span>
                  <span style="margin-left:6px;"><i class="el-icon-view"></i> {{ item.viewNum || 0 }}</span>
                  <span style="margin-left:6px;font-size:12px;color:#999;"><i class="el-icon-share"></i> {{ item.shareNum || 0 }}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-empty v-if="goodsList.length === 0" description="暂无商品"></el-empty>
      </div>

      <!-- 列表模式 -->
      <div v-if="viewMode === 'list'">
        <el-table :data="goodsList" style="width:100%" v-loading="loading">
          <el-table-column label="商品" min-width="300">
            <template slot-scope="scope">
              <div style="display:flex;align-items:center;cursor:pointer;" @click="$router.push('/goodsDetail/' + scope.row.goodsId)">
                <div v-if="scope.row.goodsImages" style="width:60px;height:60px;overflow:hidden;border-radius:4px;margin-right:12px;flex-shrink:0;">
                  <img :src="scope.row.goodsImages.split(',')[0]" style="width:100%;height:100%;object-fit:cover;" />
                </div>
                <div v-else style="width:60px;height:60px;background:#f5f7fa;border-radius:4px;margin-right:12px;flex-shrink:0;display:flex;align-items:center;justify-content:center;">
                  <i class="el-icon-goods" style="font-size:20px;color:#C0C4CC;"></i>
                </div>
                <div>
                  <div style="font-weight:500;color:#303133;">{{ scope.row.goodsName }}</div>
                  <div style="color:#f56c6c;font-weight:bold;margin-top:4px;">￥{{ scope.row.goodsPrice }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="nickname" label="卖家" width="120">
            <template slot-scope="scope"><span style="cursor:pointer;color:#409EFF;" @click.stop="$router.push('/userProfile/' + scope.row.userId)">{{ scope.row.nickname || '匿名' }}</span></template>
          </el-table-column>
          <el-table-column prop="goodsCount" label="库存" width="80" align="center"></el-table-column>
          <el-table-column prop="viewNum" label="浏览" width="80" align="center">
            <template slot-scope="scope">{{ scope.row.viewNum || 0 }}</template>
          </el-table-column>
          <el-table-column prop="shareNum" label="分享数" width="80" align="center"></el-table-column>
          <el-table-column prop="goodsStatus" label="状态" width="80" align="center">
            <template slot-scope="scope"><el-tag :type="scope.row.goodsStatus ? 'success' : 'info'" size="mini">{{ scope.row.goodsStatus ? '在售' : '下架' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="createTime" label="发布时间" width="180">
            <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="goodsList.length === 0 && !loading" description="暂无商品"></el-empty>
      </div>

      <el-pagination style="margin-top:20px;text-align:center;" @current-change="handlePageChange" :current-page="pageNum" :page-sizes="viewMode === 'card' ? [8, 16, 24, 32] : [5, 10, 20, 50]" :page-size.sync="pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange"></el-pagination>
    </el-card>

    <el-dialog title="发布闲置商品" :visible.sync="publishDialogVisible" width="620px" @close="resetPublishForm" class="publish-dialog">
      <div class="publish-dialog-body">
        <el-form ref="publishForm" :model="publishForm" :rules="publishRules" label-width="100px">
          <el-form-item label="商品名称" prop="goodsName">
            <div style="display:flex;gap:8px;">
              <el-input v-model="publishForm.goodsName" placeholder="请输入商品名称" style="flex:1;"></el-input>
              <el-button type="warning" size="small" icon="el-icon-magic-stick" :loading="polishingName" @click="aiPolish('name')">润色</el-button>
            </div>
          </el-form-item>
          <el-form-item label="商品价格" prop="goodsPrice">
            <el-input-number v-model="publishForm.goodsPrice" :min="0" :precision="2" style="width:200px;"></el-input-number>
          </el-form-item>
          <el-form-item label="商品分类" prop="goodsCategoryId">
            <el-select v-model="publishForm.goodsCategoryId" placeholder="请选择分类" style="width:100%;">
              <el-option v-for="c in goodsCategories" :key="c.goodsCategoryId" :label="c.goodsCategoryName" :value="c.goodsCategoryId"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="商品数量" prop="goodsCount">
            <el-input-number v-model="publishForm.goodsCount" :min="1" style="width:200px;"></el-input-number>
          </el-form-item>
          <el-form-item label="商品详情" prop="goodsDetail">
            <el-input type="textarea" v-model="publishForm.goodsDetail" :rows="4" placeholder="请描述商品详情"></el-input>
            <el-button type="warning" size="mini" icon="el-icon-magic-stick" :loading="polishing" style="margin-top:6px;" @click="aiPolish('detail')">AI润色详情</el-button>
          </el-form-item>
          <el-form-item label="商品图片">
            <el-upload action="/dev_api/image/upload" :headers="uploadHeaders" name="files" list-type="picture-card" :on-success="handleUploadSuccess" :on-remove="handleRemove" :on-error="() => $message.error('图片上传失败')" :before-upload="beforeUpload" accept="image/*" :limit="9" :on-exceed="() => $message.warning('最多上传9张图片')">
              <i class="el-icon-plus"></i>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer"><el-button @click="publishDialogVisible = false">取消</el-button><el-button type="primary" @click="submitPublish" :loading="publishLoading">发布</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import SearchPanel from '@/components/SearchPanel.vue'
export default {
  name: 'GoodsList',
  components: { SearchPanel },
  data() {
    return {
      goodsList: [], pageNum: 1, pageSize: 8, total: 0, keyName: '', searchNickname: '', goodsCategoryId: null, goodsCategories: [], loading: false, viewMode: 'card', sortBy: 'time',
      publishDialogVisible: false, publishLoading: false,
      polishing: false, polishingName: false,
      publishForm: { goodsName: '', goodsPrice: 0, goodsCategoryId: null, goodsCount: 1, goodsDetail: '', goodsImages: '' },
      publishRules: { goodsName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }], goodsPrice: [{ required: true, message: '请输入价格', trigger: 'blur' }], goodsCategoryId: [{ required: true, message: '请选择分类', trigger: 'change' }] },
      uploadedUrls: [],
      uploadHeaders: { Authorization: sessionStorage.getItem('token') || '' }
    }
  },
  created() {
    if (this.$route.query.keyName) {
      this.keyName = this.$route.query.keyName
    }
    this.loadGoods(); this.loadCategories()
  },
  watch: {
    '$route'(to) {
      if (to.query.keyName) {
        this.keyName = to.query.keyName
      }
      this.loadGoods()
    },
    viewMode(val) {
      if (val === 'card') {
        // 图文模式下确保 pageSize 是8的倍数
        if (this.pageSize % 8 !== 0) {
          this.pageSize = 8
          this.pageNum = 1
          this.loadGoods()
        }
      }
    }
  },
  methods: {
    async loadCategories() {
      try {
        const res = await this.$axios.get('/goods-category/list')
        if (res.code === 200) this.goodsCategories = (res.data || []).concat({ goodsCategoryId: 0, goodsCategoryName: '未分类' })
      } catch (e) {}
    },
    async loadGoods() {
      this.loading = true
      try {
        const params = { pageNum: this.pageNum, pageSize: this.pageSize, goodsStatus: true }
        if (this.keyName) params.keyName = this.keyName
        if (this.searchNickname) params.nickname = this.searchNickname
        if (this.goodsCategoryId) params.goodsCategoryId = this.goodsCategoryId
        if (this.sortBy === 'priceAsc') { params.sortBy = 'goods_price'; params.sortOrder = 'asc' }
        else if (this.sortBy === 'priceDesc') { params.sortBy = 'goods_price'; params.sortOrder = 'desc' }
        const res = await this.$axios.get('/goods/getList', { params })
        if (res.code === 200) { this.goodsList = res.data.records || []; this.total = res.data.total || 0 }
      } catch (e) {}
      this.loading = false
    },
    handlePageChange(val) { this.pageNum = val; this.loadGoods() },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadGoods() },
    openPublishDialog() { this.publishDialogVisible = true },
    resetPublishForm() {
      this.publishForm = { goodsName: '', goodsPrice: 0, goodsCategoryId: null, goodsCount: 1, goodsDetail: '', goodsImages: '' }
      this.uploadedUrls = []
      if (this.$refs.publishForm) this.$refs.publishForm.resetFields()
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
        this.publishForm.goodsImages = this.uploadedUrls.join(',')
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
        this.publishForm.goodsImages = this.uploadedUrls.join(',')
      }
    },
    submitPublish() {
      this.$refs.publishForm.validate(async valid => {
        if (!valid) return
        try {
          const muteRes = await this.$axios.get('/punishment/checkMute')
          if (muteRes.code === 200 && muteRes.data && muteRes.data.muted) {
            this.$alert('您当前已被禁言，原因：' + (muteRes.data.reason || '无'), '禁言提示', { type: 'warning' })
            this.publishLoading = false
            return
          }
        } catch (e) {}
        this.publishLoading = true
        const res = await this.$axios.post('/goods/add', this.publishForm)
        if (res.code === 200) { this.$message.success('发布成功'); this.publishDialogVisible = false; this.loadGoods() }
        else this.$message.error(res.msg || '发布失败')
        this.publishLoading = false
      })
    },
    async aiPolish(field) {
      var content = field === 'name' ? this.publishForm.goodsName : this.publishForm.goodsDetail
      if (!content || !content.trim()) {
        this.$message.warning('请先输入内容再润色')
        return
      }
      if (field === 'name') { this.polishingName = true } else { this.polishing = true }
      try {
        var res = await this.$axios.post('/ai/polish', { content: content, type: 'goods' })
        if (res.code === 200 && res.data && res.data.polished) {
          if (field === 'name') { this.publishForm.goodsName = res.data.polished }
          else { this.publishForm.goodsDetail = res.data.polished }
          if (res.data.hint) { this.$message.warning(res.data.hint) }
          else { this.$message.success('润色完成') }
        } else {
          this.$message.error('润色失败')
        }
      } catch (e) {
        this.$message.error('润色请求失败')
      }
      if (field === 'name') { this.polishingName = false } else { this.polishing = false }
    }
  }
}
</script>

<style scoped>
.goods-card-row .el-col {
  padding: 0 10px;
  margin-bottom: 20px;
}
.goods-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 18px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  height: 100%;
  position: relative;
}
.goods-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.14);
  border-color: rgba(64, 158, 255, 0.15);
}
.goods-card-img {
  width: 100%;
  height: 190px;
  overflow: hidden;
  border-radius: 12px;
  background: linear-gradient(135deg, #f5f7fa, #e6e9f0);
  margin: 10px 10px 0 10px;
  width: calc(100% - 20px);
  position: relative;
}
.goods-card-img::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40%;
  background: linear-gradient(to top, rgba(0,0,0,0.12), transparent);
  pointer-events: none;
  border-radius: 0 0 12px 12px;
}
.goods-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.goods-card:hover .goods-card-img img {
  transform: scale(1.08);
}
.goods-card-no-img {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}
.goods-card-no-img-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
  word-break: break-all;
  text-align: center;
}
.goods-card-body {
  padding: 14px 16px;
}
.goods-card-name {
  font-size: 15px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  transition: color 0.3s ease;
}
.goods-card:hover .goods-card-name {
  color: #409EFF;
}
.goods-card-price {
  margin-top: 8px;
  color: #F56C6C;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(245, 108, 108, 0.1);
}
.goods-card-meta {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* ===== Publish Dialog Beautification ===== */
::v-deep .publish-dialog .el-dialog__body {
  padding: 0;
}
.publish-dialog-body {
  padding: 28px;
  max-height: 60vh;
  overflow-y: auto;
}
::v-deep .publish-dialog .el-upload--picture-card {
  border-radius: 12px;
  transition: all 0.3s ease;
}
::v-deep .publish-dialog .el-upload--picture-card:hover {
  border-color: #409EFF;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}
</style>
