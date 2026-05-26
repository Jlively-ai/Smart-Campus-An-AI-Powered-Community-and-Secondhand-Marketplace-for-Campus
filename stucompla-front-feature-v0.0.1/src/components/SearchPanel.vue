<!--
  组件名：SearchPanel
  功能描述：通用搜索面板组件，支持搜索历史记录和热门搜索标签
  主要职责：
    1. 提供搜索输入框，支持回车搜索和清空
    2. 聚焦时展示搜索历史和热门搜索标签
    3. 搜索历史按用户+模块隔离存储到localStorage
-->
<template>
  <div style="position:relative;display:inline-block;">
    <!-- 搜索输入框 -->
    <el-input v-model="keyword" :placeholder="placeholder" :size="size" :style="inputStyle" clearable @focus="onFocus" @blur="onBlur" @keyup.enter.native="onSearch" @clear="onClear"></el-input>
    <!-- 搜索下拉面板 -->
    <div v-if="showPanel" class="search-panel-dropdown">
      <!-- 搜索历史区域 -->
      <div v-if="history.length > 0" style="margin-bottom:10px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
          <span style="font-size:13px;color:#999;">搜索历史</span>
          <el-link type="info" :underline="false" style="font-size:12px;" @click="clearHistory">清空</el-link>
        </div>
        <div style="display:flex;flex-wrap:wrap;gap:6px;">
          <el-tag v-for="(h, i) in history" :key="i" size="small" effect="plain" style="cursor:pointer;" @click.native="selectHistory(h)">{{ h }}</el-tag>
        </div>
      </div>
      <!-- 热门搜索区域 -->
      <div v-if="hotTags.length > 0">
        <div style="font-size:13px;color:#999;margin-bottom:6px;">热门搜索</div>
        <div style="display:flex;flex-wrap:wrap;gap:6px;">
          <el-tag v-for="(tag, i) in hotTags" :key="i" size="small" :type="i < 3 ? 'danger' : 'info'" effect="plain" style="cursor:pointer;" @click.native="selectHistory(tag)">{{ i + 1 }}. {{ tag }}</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SearchPanel',
  /** 组件属性 */
  props: {
    /** 搜索关键词（支持v-model双向绑定） */
    value: { type: String, default: '' },
    /** 模块标识，用于区分不同模块的搜索历史 */
    module: { type: String, required: true },
    /** 输入框占位文本 */
    placeholder: { type: String, default: '搜索...' },
    /** 输入框尺寸 */
    size: { type: String, default: 'small' },
    /** 输入框自定义样式 */
    inputStyle: { type: String, default: '' },
    /** 热门搜索标签列表 */
    hotTags: { type: Array, default: () => [] }
  },
  data() {
    return {
      /** 是否显示搜索下拉面板 */
      showPanel: false,
      /** 搜索历史记录列表 */
      history: []
    }
  },
  computed: {
    /** 搜索关键词的双向绑定计算属性 */
    keyword: {
      get() { return this.value },
      set(val) { this.$emit('input', val) }
    },
    /** 搜索历史的localStorage存储键，按用户ID和模块名隔离 */
    historyKey() {
      const info = this.$store.getters.getUserInfo || {}
      const userId = info.userId || info.adminId || 'guest'
      return 'searchHistory_' + this.module + '_' + userId
    }
  },
  watch: {
    /** 监听historyKey变化，用户切换时重新加载历史 */
    historyKey() { this.loadHistory() }
  },
  /** 组件创建时加载搜索历史 */
  created() { this.loadHistory() },
  methods: {
    /** 从localStorage加载搜索历史 */
    loadHistory() {
      this.history = JSON.parse(localStorage.getItem(this.historyKey) || '[]')
    },
    /** 输入框获得焦点时，加载历史并显示下拉面板 */
    onFocus() {
      this.loadHistory()
      this.showPanel = true
    },
    /** 输入框失去焦点时，延迟隐藏下拉面板（等待点击事件处理） */
    onBlur() { setTimeout(() => { this.showPanel = false }, 200) },
    /** 执行搜索：保存关键词到历史，隐藏面板，触发search事件 */
    onSearch() {
      const kw = this.keyword.trim()
      if (!kw) return
      this.saveHistory(kw)
      this.showPanel = false
      this.$emit('search', kw)
    },
    /** 清空搜索关键词时触发clear事件 */
    onClear() {
      this.$emit('clear')
    },
    /** 点击历史标签或热门标签时，填入关键词并搜索 */
    selectHistory(tag) {
      this.keyword = tag
      this.showPanel = false
      this.$emit('search', tag)
    },
    /** 保存搜索关键词到历史记录（去重，最多保留10条） */
    saveHistory(kw) {
      if (!this.history.includes(kw)) {
        this.history.unshift(kw)
        if (this.history.length > 10) this.history.pop()
      } else {
        const idx = this.history.indexOf(kw)
        this.history.splice(idx, 1)
        this.history.unshift(kw)
      }
      localStorage.setItem(this.historyKey, JSON.stringify(this.history))
    },
    /** 清空搜索历史记录 */
    clearHistory() {
      this.history = []
      localStorage.removeItem(this.historyKey)
    }
  }
}
</script>

<style scoped>
/* 搜索下拉面板样式 */
.search-panel-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  padding: 12px;
  z-index: 9999;
  max-height: 350px;
  overflow-y: auto;
  margin-top: 4px;
  transition: all 0.3s ease;
}
/* 下拉面板顶部三角箭头 */
.search-panel-dropdown::before {
  content: '';
  position: absolute;
  top: -6px;
  left: 20px;
  width: 12px;
  height: 12px;
  background: #fff;
  border-left: 1px solid #e4e7ed;
  border-top: 1px solid #e4e7ed;
  transform: rotate(45deg);
}

/* 标签悬停效果 */
::v-deep .el-tag {
  transition: all 0.3s ease;
  border-radius: 6px;
}
::v-deep .el-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
</style>
