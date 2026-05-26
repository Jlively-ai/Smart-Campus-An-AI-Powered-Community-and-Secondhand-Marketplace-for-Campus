<!--
  组件名：GoodsPublish
  功能描述：发布闲置商品页
  主要职责：
    1. 商品发布表单（名称/价格/分类/数量/详情/图片）
    2. AI润色功能
    3. @提及输入
-->
<template>
  <div>
    <el-card>
      <div slot="header"><span>发布闲置商品</span></div>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商品名称" prop="goodsName">
          <div style="display:flex;gap:8px;">
            <el-input v-model="form.goodsName" placeholder="请输入商品名称" style="flex:1;"></el-input>
            <el-button type="warning" size="small" icon="el-icon-magic-stick" :loading="polishingName" @click="aiPolish('name')">润色</el-button>
          </div>
        </el-form-item>
        <el-form-item label="商品价格" prop="goodsPrice">
          <el-input-number v-model="form.goodsPrice" :min="0" :precision="2" style="width:200px;"></el-input-number>
        </el-form-item>
        <el-form-item label="商品分类" prop="goodsCategoryId">
          <el-select v-model="form.goodsCategoryId" placeholder="请选择分类" style="width:100%;">
            <el-option v-for="c in goodsCategories" :key="c.goodsCategoryId" :label="c.goodsCategoryName" :value="c.goodsCategoryId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="商品数量" prop="goodsCount">
          <el-input-number v-model="form.goodsCount" :min="1" style="width:200px;"></el-input-number>
        </el-form-item>
        <el-form-item label="商品详情" prop="goodsDetail">
          <MentionInput v-model="form.goodsDetail" :rows="6" placeholder="请描述商品详情" @mention-change="onMentionChange" />
          <el-button type="warning" size="mini" icon="el-icon-magic-stick" :loading="polishing" style="margin-top:6px;" @click="aiPolish('detail')">AI润色详情</el-button>
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload action="/dev_api/image/upload" :headers="uploadHeaders" name="files" list-type="picture-card" :file-list="fileList" :on-success="handleUploadSuccess" :on-remove="handleRemove" :on-error="() => $message.error('图片上传失败')" :before-upload="beforeUpload" accept="image/*" :limit="9" :on-exceed="() => $message.warning('最多上传9张图片')">
            <i class="el-icon-plus"></i>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit" :loading="loading">发布</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import MentionInput from '@/components/MentionInput.vue'
export default {
  name: 'GoodsPublish',
  components: { MentionInput },
  data() {
    return {
      form: { goodsName: '', goodsPrice: 0, goodsCategoryId: null, goodsCount: 1, goodsDetail: '', goodsImages: '', mentionUsers: '' },
      rules: { goodsName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }], goodsPrice: [{ required: true, message: '请输入价格', trigger: 'blur' }], goodsCategoryId: [{ required: true, message: '请选择分类', trigger: 'change' }] },
      goodsCategories: [], loading: false,
      polishing: false, polishingName: false,
      fileList: [], uploadedUrls: [],
      uploadHeaders: { Authorization: sessionStorage.getItem('token') || '' }
    }
  },
  created() { this.loadCategories() },
  methods: {
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
        this.form.goodsImages = this.uploadedUrls.join(',')
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
        this.form.goodsImages = this.uploadedUrls.join(',')
      }
    },
    async loadCategories() {
      const res = await this.$axios.get('/goods-category/list')
      if (res.code === 200) this.goodsCategories = (res.data || []).concat({ goodsCategoryId: 0, goodsCategoryName: '未分类' })
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        this.loading = true
        const res = await this.$axios.post('/goods/add', this.form)
        if (res.code === 200) { this.$message.success('发布成功，等待审核'); this.$router.push('/goodsList') }
        else this.$message.error(res.msg || '发布失败')
        this.loading = false
      })
    },
    onMentionChange(userIds) {
      this.form.mentionUsers = JSON.stringify(userIds)
    },
    async aiPolish(field) {
      var content = field === 'name' ? this.form.goodsName : this.form.goodsDetail
      if (!content || !content.trim()) {
        this.$message.warning('请先输入内容再润色')
        return
      }
      if (field === 'name') { this.polishingName = true } else { this.polishing = true }
      try {
        var res = await this.$axios.post('/ai/polish', { content: content, type: 'goods' })
        if (res.code === 200 && res.data && res.data.polished) {
          if (field === 'name') { this.form.goodsName = res.data.polished }
          else { this.form.goodsDetail = res.data.polished }
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
.el-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.el-form-item {
  margin-bottom: 20px;
}
</style>
