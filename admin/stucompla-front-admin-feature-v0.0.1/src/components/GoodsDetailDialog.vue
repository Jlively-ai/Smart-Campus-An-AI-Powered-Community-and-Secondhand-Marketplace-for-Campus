<!--
  组件名：GoodsDetailDialog
  功能描述：商品详情弹窗组件
  主要职责：展示商品详细信息，包括名称、价格、描述、图片等
-->
<template>
  <el-dialog title="商品详情" :visible.sync="dialogVisible" width="900px" top="5vh" append-to-body @close="onClose">
    <div v-loading="loading">
      <div v-if="goodsData">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-goods" style="margin-right:6px;"></i>基础信息</div>
          <table class="dt-table">
            <tr>
              <td class="dt-label">商品ID</td><td>{{ goodsData.goodsId | formatId('goods') }}</td>
              <td class="dt-label">状态</td><td><el-tag :type="goodsData.goodsStatus ? 'success' : 'info'" size="small">{{ goodsData.goodsStatus ? '在售' : '已下架' }}</el-tag></td>
            </tr>
            <tr>
              <td class="dt-label">商品价格</td><td><span style="color:#F56C6C;font-weight:bold;font-size:16px;">￥{{ goodsData.goodsPrice }}</span></td>
              <td class="dt-label">库存数量</td><td>{{ goodsData.goodsCount || 0 }}</td>
            </tr>
            <tr>
              <td class="dt-label">卖家</td><td><el-link type="primary" @click="$emit('show-user', goodsData.userId)">{{ goodsData.nickname || '未知' }}</el-link></td>
              <td class="dt-label">卖家ID</td><td>{{ goodsData.userId | formatId('user') }}</td>
            </tr>
            <tr>
              <td class="dt-label">分类</td><td>{{ goodsData.goodsCategoryName || '-' }}</td>
              <td class="dt-label">浏览数</td><td>{{ goodsData.viewNum || 0 }}</td>
            </tr>
            <tr>
              <td class="dt-label">发布时间</td><td>{{ goodsData.createTime | formatTime }}</td>
              <td class="dt-label">更新时间</td><td>{{ goodsData.updateTime | formatTime }}</td>
            </tr>
          </table>
        </el-card>
        <el-card shadow="hover">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-tickets" style="margin-right:6px;"></i>商品描述</div>
          <div style="margin-bottom:12px;"><span style="font-weight:bold;color:#606266;margin-right:8px;">标题：</span>{{ goodsData.goodsName || '-' }}</div>
          <div style="margin-bottom:12px;"><span style="font-weight:bold;color:#606266;margin-right:8px;">内容：</span><span style="white-space:pre-wrap;line-height:1.8;">{{ goodsData.goodsDetail || goodsData.goodsDesc || goodsData.description || '-' }}</span></div>
          <div>
            <span style="font-weight:bold;color:#606266;margin-right:8px;">附件：</span>
            <span v-if="getImgList().length === 0">无</span>
          </div>
          <div v-if="getImgList().length > 0" style="margin-top:8px;">
            <el-image v-for="(img, i) in getImgList()" :key="i" :src="img" :preview-src-list="getImgList()" style="width:120px;height:120px;margin-right:8px;margin-bottom:8px;border-radius:8px;" fit="cover"></el-image>
          </div>
        </el-card>
      </div>
      <div v-else style="text-align:center;color:#909399;padding:20px;">未找到该商品信息</div>
    </div>
  </el-dialog>
</template>
<script>
import { getGoodsList } from '@/api/manage'
export default {
  name: 'GoodsDetailDialog',
  props: { visible: Boolean, goodsId: String },
  /** 组件数据定义 */
  data() { return { loading: false, goodsData: null } },
  /** 计算属性定义 */
  computed: { dialogVisible: { get() { return this.visible }, set(val) { this.$emit('update:visible', val) } } },
  watch: { visible(val) { if (val && this.goodsId) this.loadGoods() }, goodsId(val) { if (val && this.visible) this.loadGoods() } },
  /** 组件方法定义 */
  methods: {
    async loadGoods() {
      this.loading = true; this.goodsData = null
      try {
        var res = await getGoodsList({ pageNum: 1, pageSize: 9999 })
        if (res.code === 200) { var records = res.data.records || res.data || []; this.goodsData = records.find(function(g) { return g.goodsId === this.goodsId }.bind(this)) || null }
      } catch (e) { this.goodsData = null }
      this.loading = false
    },
    getImgList() {
      var img = this.goodsData ? (this.goodsData.goodsImages || this.goodsData.goodsImg || this.goodsData.images || '') : ''
      if (!img) return []
      if (Array.isArray(img)) return img.filter(function(s) { return s && s.trim() })
      var str = String(img).trim()
      if (str === '' || str === '[]' || str === 'null') return []
      try { var parsed = JSON.parse(str); if (Array.isArray(parsed)) return parsed.filter(function(s) { return s && s.trim() }); return [parsed] } catch (e) { return str.split(',').filter(function(s) { return s && s.trim() }) }
    },
    onClose() { this.goodsData = null }
  }
}
</script>
<style scoped>
/* 组件局部样式 */
.dt-table {
  width: 100%;
  border-collapse: collapse;
  border-color: #ebeef5;
}
.dt-table td {
  padding: 12px 16px;
  border: 1px solid #ebeef5;
  font-size: 13px;
  color: #303133;
}
.dt-label {
  background: #fafafa;
  font-weight: 600;
  width: 100px;
  text-align: right;
  color: #303133;
  white-space: nowrap;
}
.dt-table tr {
  transition: background-color 0.3s ease;
}
.dt-table tr:hover td {
  background-color: #f5f7fa;
}
::v-deep .el-card {
  border-radius: 8px;
  transition: box-shadow 0.3s ease;
}
::v-deep .el-card__header {
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
}
::v-deep .el-tag {
  border-radius: 6px;
  transition: transform 0.3s ease;
}
::v-deep .el-tag:hover {
  transform: translateY(-1px);
}
::v-deep .el-image {
  border-radius: 8px;
  transition: transform 0.3s ease;
}
::v-deep .el-image:hover {
  transform: scale(1.03);
}
::v-deep .el-link {
  transition: color 0.3s ease;
}
</style>
