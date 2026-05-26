<!--
  组件名：MyGoods
  功能描述：我的闲置商品页
  主要职责：
    1. 搜索/状态筛选/排序
    2. 编辑/删除商品
    3. AI润色功能
-->
<template>
  <div>
    <el-card>
      <div slot="header">
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <span>我的闲置商品</span>
          <div style="display:flex;gap:8px;">
            <search-panel v-model="searchKeyword" module="myGoods" placeholder="搜索我的商品" size="small" input-style="width:200px;" @search="onSearch" @clear="onSearchClear"></search-panel>
            <el-button type="primary" size="small" icon="el-icon-search" @click="onSearch">搜索</el-button>
            <el-button type="success" size="small" @click="$router.push('/goodsPublish')">发布闲置</el-button>
          </div>
        </div>
        <div style="display:flex;gap:8px;margin-top:10px;align-items:center;">
          <el-select v-model="filterStatus" placeholder="商品状态" size="small" clearable style="width:140px;" @change="applyFilterSort">
            <el-option label="待审核" value="pending"></el-option>
            <el-option label="审核未通过" value="rejected"></el-option>
            <el-option label="已锁定" value="locked"></el-option>
            <el-option label="上架" value="onShelf"></el-option>
            <el-option label="下架" value="offShelf"></el-option>
          </el-select>
          <el-select v-model="sortBy" placeholder="排序方式" size="small" style="width:160px;" @change="applyFilterSort">
            <el-option label="发布时间最新" value="createTimeDesc"></el-option>
            <el-option label="发布时间最早" value="createTimeAsc"></el-option>
            <el-option label="价格从高到低" value="priceDesc"></el-option>
            <el-option label="价格从低到高" value="priceAsc"></el-option>
            <el-option label="浏览量从高到低" value="viewDesc"></el-option>
            <el-option label="分享数从高到低" value="shareDesc"></el-option>
          </el-select>
        </div>
      </div>
      <el-table :data="goodsList" v-loading="loading">
        <el-table-column prop="goodsName" label="商品名称" min-width="150">
          <template slot-scope="scope"><el-link type="primary" @click="$router.push('/goodsDetail/' + scope.row.goodsId)">{{ scope.row.goodsName }}</el-link></template>
        </el-table-column>
        <el-table-column prop="goodsPrice" label="价格" width="100">
          <template slot-scope="scope"><span style="color:#f56c6c;">￥{{ scope.row.goodsPrice }}</span></template>
        </el-table-column>
        <el-table-column prop="goodsCount" label="库存" width="70" align="center"></el-table-column>
        <el-table-column prop="goodsStatus" label="状态" width="180" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.auditState === 0" type="warning" size="mini">待审核</el-tag>
            <el-tag v-else-if="scope.row.auditState === 2" type="danger" size="mini">审核未通过</el-tag>
            <el-tag v-if="scope.row.locked === 1" type="danger" size="mini">已锁定</el-tag>
            <el-tag v-if="scope.row.auditState === 1 && scope.row.locked !== 1 && scope.row.goodsStatus" type="success" size="mini">上架</el-tag>
            <el-tag v-if="scope.row.auditState === 1 && scope.row.locked !== 1 && !scope.row.goodsStatus" type="info" size="mini">下架</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewNum" label="浏览量" width="70" align="center"></el-table-column>
        <el-table-column prop="shareNum" label="分享数" width="70" align="center"></el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="240">
          <template slot-scope="scope">
            {{ scope.row.createTime | formatTime }}<br v-if="scope.row.updateTime && scope.row.updateTime !== scope.row.createTime" />
            <span v-if="scope.row.updateTime && scope.row.updateTime !== scope.row.createTime" style="color:#E6A23C;font-size:12px;">（已重新编辑于 {{ scope.row.updateTime | formatTime }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button size="mini" :type="scope.row.goodsStatus ? 'warning' : 'success'" @click="toggleShelf(scope.row)">{{ scope.row.goodsStatus ? '下架' : '上架' }}</el-button>
            <el-button size="mini" type="danger" @click="deleteGoods(scope.row.goodsId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:15px;text-align:center;" @current-change="handlePageChange" :current-page="pageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange"></el-pagination>
    </el-card>

    <el-dialog title="编辑商品" :visible.sync="editDialogVisible" width="700px" @close="resetEditForm">
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
          <el-upload action="/dev_api/image/upload" :headers="uploadHeaders" name="files" list-type="picture-card" :file-list="editFileList" :on-success="handleUploadSuccess" :on-remove="handleRemove" :on-error="() => $message.error('图片上传失败')" :before-upload="beforeUpload" accept="image/*" :limit="9" :on-exceed="() => $message.warning('最多上传9张图片')">
            <i class="el-icon-plus"></i>
          </el-upload>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import SearchPanel from '@/components/SearchPanel.vue'
export default {
  name: 'MyGoods',
  components: { SearchPanel },
  data() {
    return {
      goodsList: [], pageNum: 1, pageSize: 10, total: 0, loading: false, searchKeyword: '',
      filterStatus: '', sortBy: 'createTimeDesc',
      rawGoodsList: [],
      editDialogVisible: false, editLoading: false,
      editForm: { goodsId: '', goodsName: '', goodsPrice: 0, goodsCategoryId: null, goodsCount: 1, goodsDetail: '', goodsImages: '' },
      editRules: {
        goodsName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        goodsPrice: [{ required: true, message: '请输入价格', trigger: 'blur' }],
        goodsCategoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
      },
      goodsCategories: [],
      editFileList: [],
      uploadedUrls: [],
      uploadHeaders: { Authorization: sessionStorage.getItem('token') || '' },
      polishing: false
    }
  },
  created() { this.loadGoods(); this.loadCategories() },
  methods: {
    async loadGoods() {
      this.loading = true
      const userInfo = this.$store.getters.getUserInfo
      const uid = userInfo && (userInfo.userId || userInfo.adminId)
      const params = { pageNum: this.pageNum, pageSize: this.pageSize, userId: uid, keyName: this.searchKeyword }
      // Pass audit state to backend if filtering
      if (this.filterStatus === 'pending') params.auditState = 0
      else if (this.filterStatus === 'rejected') params.auditState = 2
      else if (this.filterStatus === 'onShelf' || this.filterStatus === 'offShelf') params.auditState = 1
      const res = await this.$axios.get('/goods/getList', { params })
      if (res.code === 200) { this.rawGoodsList = res.data.records || []; this.total = res.data.total || 0; this.applyFilterSort() }
      this.loading = false
    },
    applyFilterSort() {
      let list = [...this.rawGoodsList]
      if (this.filterStatus) {
        list = list.filter(item => {
          switch (this.filterStatus) {
            case 'pending': return item.auditState === 0
            case 'rejected': return item.auditState === 2
            case 'locked': return item.locked === 1
            case 'onShelf': return item.auditState === 1 && item.locked !== 1 && item.goodsStatus
            case 'offShelf': return item.auditState === 1 && item.locked !== 1 && !item.goodsStatus
            default: return true
          }
        })
      }
      // Update total to reflect filtered count when client-side filtering is applied
      if (this.filterStatus === 'locked') {
        this.total = list.length
      }
      switch (this.sortBy) {
        case 'createTimeAsc':
          list.sort((a, b) => new Date(a.createTime) - new Date(b.createTime)); break
        case 'priceDesc':
          list.sort((a, b) => b.goodsPrice - a.goodsPrice); break
        case 'priceAsc':
          list.sort((a, b) => a.goodsPrice - b.goodsPrice); break
        case 'viewDesc':
          list.sort((a, b) => (b.viewNum || 0) - (a.viewNum || 0)); break
        case 'shareDesc':
          list.sort((a, b) => (b.shareNum || 0) - (a.shareNum || 0)); break
        default:
          list.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      }
      this.goodsList = list
    },
    async loadCategories() {
      const res = await this.$axios.get('/goods-category/list')
      if (res.code === 200) this.goodsCategories = (res.data || []).concat({ goodsCategoryId: 0, goodsCategoryName: '未分类' })
    },
    async toggleShelf(row) {
      const url = row.goodsStatus ? '/goods/unShelveMyGoods/' : '/goods/putMyGoods/'
      const res = await this.$axios.post(url + row.goodsId)
      if (res.code === 200) { this.$message.success('操作成功'); this.loadGoods() }
    },
    async deleteGoods(goodsId) {
      await this.$confirm('确定删除该商品？删除后将移入回收站，30天内可恢复。', '提示', { type: 'warning' })
      const res = await this.$axios.delete('/goods/' + goodsId)
      if (res.code === 200) { this.$message.success('删除成功'); this.loadGoods() }
    },
    handlePageChange(val) { this.pageNum = val; this.loadGoods() },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadGoods() },
    onSearch() { this.pageNum = 1; this.loadGoods() },
    onSearchClear() { this.searchKeyword = ''; this.pageNum = 1; this.loadGoods() },
    openEditDialog(row) {
      this.editForm = { goodsId: row.goodsId, goodsName: row.goodsName, goodsPrice: row.goodsPrice, goodsCategoryId: row.goodsCategoryId, goodsCount: row.goodsCount, goodsDetail: row.goodsDetail, goodsImages: row.goodsImages || '' }
      this.uploadedUrls = row.goodsImages ? row.goodsImages.split(',').filter(u => u.trim()) : []
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
        this.$message.warning('请先输入内容再润色')
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
</style>
