<!--
  组件名：MentionInput
  功能描述：@提及输入组件，在textarea中输入@符号时弹出用户搜索下拉列表
  主要职责：
    1. 监听输入内容，检测@符号触发用户搜索
    2. 支持键盘上下选择、回车/Tab确认、Esc关闭
    3. 处理中文输入法（IME）兼容
    4. 选中用户后插入@昵称文本并记录被提及用户ID
-->
<template>
  <div class="mention-input-wrapper">
    <el-input
      ref="elInput"
      type="textarea"
      :rows="rows"
      :placeholder="placeholder"
      :value="innerValue"
      @input="onInput"
      resize="none"
    ></el-input>
    <!-- @提及候选用户下拉列表（fixed定位避免被overflow:hidden裁剪） -->
    <div v-if="showDropdown" class="mention-dropdown" :style="dropdownStyle">
      <div class="mention-list" v-if="searchResults.length > 0">
        <div
          v-for="(user, index) in searchResults"
          :key="user.userId"
          class="mention-item"
          :class="{ active: activeIndex === index }"
          @mousedown.prevent="selectUser(user)"
          @mouseenter="activeIndex = index"
        >
          <img v-if="user.avatar" :src="getAvatarUrl(user.avatar)" class="mention-avatar" />
          <div v-else class="mention-avatar-placeholder">{{ (user.nickname || user.username || '').charAt(0) }}</div>
          <span class="mention-name">{{ user.nickname || user.username }}<span class="mention-id">USR-{{ user.userId }}</span></span>
        </div>
      </div>
      <div v-else class="mention-empty">无匹配用户</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MentionInput',
  /** 组件属性 */
  props: {
    /** 输入值（支持v-model双向绑定） */
    value: { type: String, default: '' },
    /** textarea行数 */
    rows: { type: Number, default: 4 },
    /** 输入框占位文本 */
    placeholder: { type: String, default: '' },
    /** 已提及用户ID列表的JSON字符串 */
    mentionUsers: { type: String, default: '' }
  },
  data: function() {
    return {
      /** 内部输入值 */
      innerValue: this.value || '',
      /** 是否显示@提及下拉列表 */
      showDropdown: false,
      /** 当前@搜索关键词 */
      searchKeyword: '',
      /** 搜索结果用户列表 */
      searchResults: [],
      /** @符号在文本中的起始位置索引 */
      mentionStartIndex: -1,
      /** 已提及的用户ID数组 */
      mentionedUserIds: [],
      /** 搜索防抖定时器 */
      searchTimer: null,
      /** 用户信息缓存（避免重复请求） */
      cachedUsers: {},
      /** 下拉列表当前高亮索引 */
      activeIndex: 0,
      /** 是否正在中文输入法组合输入中 */
      isComposing: false,
      /** 最近一次原生输入事件的光标位置 */
      lastCursorPos: null,
      /** 最近一次原生输入事件的文本值 */
      lastNativeValue: null,
      /** 下拉列表的fixed定位坐标 */
      dropdownPos: { left: 0, top: 0, width: 280 }
    }
  },
  computed: {
    /** 下拉列表的fixed定位样式对象 */
    dropdownStyle: function() {
      return {
        position: 'fixed',
        left: this.dropdownPos.left + 'px',
        top: this.dropdownPos.top + 'px',
        width: this.dropdownPos.width + 'px'
      }
    }
  },
  watch: {
    /** 监听外部value变化，同步到内部值 */
    value: function(val) {
      if (val !== this.innerValue) {
        this.innerValue = val || ''
      }
    },
    /** 监听mentionUsers属性变化，解析已提及用户ID列表 */
    mentionUsers: function(val) {
      if (val) {
        try { this.mentionedUserIds = JSON.parse(val) } catch (e) { this.mentionedUserIds = [] }
      } else {
        this.mentionedUserIds = []
      }
    }
  },
  /** 组件挂载后：初始化提及用户ID、绑定原生事件监听器 */
  mounted: function() {
    if (this.mentionUsers) {
      try { this.mentionedUserIds = JSON.parse(this.mentionUsers) } catch (e) { this.mentionedUserIds = [] }
    }
    document.addEventListener('mousedown', this.handleDocumentClick)

    var self = this
    this.$nextTick(function() {
      var ta = self.getTextareaEl()
      if (ta) {
        // 使用capture阶段捕获光标位置，在Element UI处理之前执行
        ta.addEventListener('input', self.handleNativeInput, true)
        ta.addEventListener('compositionstart', self.handleCompositionStart, true)
        ta.addEventListener('compositionend', self.handleCompositionEnd, true)
        ta.addEventListener('keyup', self.handleNativeKeyup)
        ta.addEventListener('keydown', self.handleNativeKeydown)
      }
    })
  },
  /** 组件销毁前：移除事件监听器，清理定时器 */
  beforeDestroy: function() {
    document.removeEventListener('mousedown', this.handleDocumentClick)
    if (this.searchTimer) { clearTimeout(this.searchTimer) }
    var ta = this.getTextareaEl()
    if (ta) {
      ta.removeEventListener('input', this.handleNativeInput, true)
      ta.removeEventListener('compositionstart', this.handleCompositionStart, true)
      ta.removeEventListener('compositionend', this.handleCompositionEnd, true)
      ta.removeEventListener('keyup', this.handleNativeKeyup)
      ta.removeEventListener('keydown', this.handleNativeKeydown)
    }
  },
  methods: {
    /** 获取textarea原生DOM元素 */
    getTextareaEl: function() {
      if (this.$refs.elInput) {
        var inner = this.$refs.elInput.$el
        if (inner) {
          var ta = inner.querySelector('textarea')
          if (ta) return ta
        }
      }
      return null
    },
    /** 处理头像URL，相对路径转为/images/前缀 */
    getAvatarUrl: function(avatar) {
      if (!avatar) return ''
      if (avatar.startsWith('http')) return avatar
      var parts = avatar.split('/')
      return '/images/' + parts[parts.length - 1]
    },
    /** 原生input事件处理（capture阶段）：记录光标位置和文本值 */
    handleNativeInput: function(e) {
      this.lastCursorPos = e.target.selectionStart
      this.lastNativeValue = e.target.value
    },
    /** 中文输入法开始组合输入 */
    handleCompositionStart: function() {
      this.isComposing = true
    },
    /** 中文输入法结束组合输入，更新光标位置和文本值 */
    handleCompositionEnd: function(e) {
      this.isComposing = false
      this.lastCursorPos = e.target.selectionStart
      this.lastNativeValue = e.target.value
    },
    /** 原生keyup事件：更新光标位置，方向键时重新检测@提及 */
    handleNativeKeyup: function(e) {
      this.lastCursorPos = e.target.selectionStart
      // 光标移动键时重新检测@提及
      if (e.key === 'ArrowLeft' || e.key === 'ArrowRight' || e.key === 'Home' || e.key === 'End') {
        this.detectMention(this.innerValue, this.lastCursorPos)
      }
    },
    /** 原生keydown事件：处理下拉列表的键盘导航（上下选择、回车确认、Esc关闭） */
    handleNativeKeydown: function(e) {
      if (!this.showDropdown) return
      if (e.key === 'ArrowDown') {
        e.preventDefault()
        this.activeIndex = (this.activeIndex + 1) % this.searchResults.length
      } else if (e.key === 'ArrowUp') {
        e.preventDefault()
        this.activeIndex = (this.activeIndex - 1 + this.searchResults.length) % this.searchResults.length
      } else if (e.key === 'Enter' || e.key === 'Tab') {
        if (this.searchResults.length > 0 && this.activeIndex >= 0 && this.activeIndex < this.searchResults.length) {
          e.preventDefault()
          this.selectUser(this.searchResults[this.activeIndex])
        }
      } else if (e.key === 'Escape') {
        this.showDropdown = false
      }
    },
    /** Element UI input事件处理：同步值、触发input事件、检测@提及 */
    onInput: function(val) {
      this.innerValue = val
      this.$emit('input', val)
      // 中文输入法组合输入期间跳过@提及检测
      if (this.isComposing) {
        this.showDropdown = false
        return
      }
      // 使用原生事件缓存的光标位置（在Element UI处理之前捕获的）
      var cursorPos = this.lastCursorPos != null ? this.lastCursorPos : (val ? val.length : 0)
      var detectVal = this.lastNativeValue != null ? this.lastNativeValue : val
      this.detectMention(detectVal, cursorPos)
    },
    /** 检测@提及：分析光标前文本，找到@符号并触发搜索 */
    detectMention: function(val, cursorPos) {
      if (!val) {
        this.showDropdown = false
        return
      }

      var textBeforeCursor = val.substring(0, cursorPos)
      var lastAtIndex = textBeforeCursor.lastIndexOf('@')

      if (lastAtIndex === -1) {
        this.showDropdown = false
        return
      }

      var textAfterAt = textBeforeCursor.substring(lastAtIndex + 1)

      // @后如果有空格或换行，关闭下拉列表
      if (textAfterAt.indexOf(' ') !== -1 || textAfterAt.indexOf('\n') !== -1) {
        this.showDropdown = false
        return
      }

      // @必须在文本开头或前面是空格/换行
      if (lastAtIndex > 0) {
        var charBefore = val.charAt(lastAtIndex - 1)
        if (charBefore !== ' ' && charBefore !== '\n' && charBefore !== '\r') {
          this.showDropdown = false
          return
        }
      }

      this.mentionStartIndex = lastAtIndex
      this.searchKeyword = textAfterAt
      this.activeIndex = 0
      this.showDropdown = true
      this.updateDropdownPos()
      this.doSearch(textAfterAt)
    },
    /** 更新下拉列表的fixed定位坐标（基于textarea位置） */
    updateDropdownPos: function() {
      var ta = this.getTextareaEl()
      if (!ta) return
      var rect = ta.getBoundingClientRect()
      this.dropdownPos = {
        left: rect.left,
        top: rect.bottom + 4,
        width: Math.min(280, rect.width)
      }
    },
    /** 搜索用户（150ms防抖），调用/user/search接口 */
    doSearch: function(keyword) {
      var self = this
      if (this.searchTimer) { clearTimeout(this.searchTimer) }
      var kw = (keyword || '').trim()
      this.searchTimer = setTimeout(function() {
        self.$axios.get('/user/search', { params: { keyword: kw } }).then(function(res) {
          if (res.code === 200) {
            var users = res.data || []
            self.searchResults = users
            self.activeIndex = 0
            users.forEach(function(u) {
              self.$set(self.cachedUsers, String(u.userId), u)
            })
            // 布局变化时更新下拉位置
            if (self.showDropdown) {
              self.updateDropdownPos()
            }
          }
        }).catch(function() {
          self.searchResults = []
        })
      }, 150)
    },
    /** 选中用户：替换@关键词为@昵称，记录用户ID，设置光标位置 */
    selectUser: function(user) {
      var val = this.innerValue || ''
      var before = val.substring(0, this.mentionStartIndex)
      var after = val.substring(this.mentionStartIndex + 1 + this.searchKeyword.length)
      var nickname = user.nickname || user.username
      var insertText = '@' + nickname + ' '
      this.innerValue = before + insertText + after
      this.$emit('input', this.innerValue)
      this.showDropdown = false
      this.searchKeyword = ''
      this.searchResults = []

      this.$set(this.cachedUsers, String(user.userId), user)
      if (this.mentionedUserIds.indexOf(user.userId) === -1) {
        this.mentionedUserIds.push(user.userId)
      }
      this.$emit('mention-change', this.mentionedUserIds.slice())

      // 设置插入文本后的光标位置
      var self = this
      this.$nextTick(function() {
        var ta = self.getTextareaEl()
        if (ta) {
          var pos = before.length + insertText.length
          ta.focus()
          ta.setSelectionRange(pos, pos)
          self.lastCursorPos = pos
        }
      })
    },
    /** 点击组件外部时关闭下拉列表 */
    handleDocumentClick: function(e) {
      if (this.$el && !this.$el.contains(e.target)) {
        this.showDropdown = false
      }
    }
  }
}
</script>

<style scoped>
/* 组件外层容器 */
.mention-input-wrapper {
  position: relative;
}
/* @提及下拉列表 */
.mention-dropdown {
  z-index: 9999;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  max-height: 240px;
  overflow-y: auto;
  transition: all 0.3s ease;
}
/* 候选用户列表 */
.mention-list {
  padding: 4px 0;
}
/* 候选用户列表项 */
.mention-item {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  margin: 2px 6px;
}
/* 候选项悬停/高亮状态 */
.mention-item:hover,
.mention-item.active {
  background: #f5f7fa;
  transform: translateX(2px);
}
/* 用户头像 */
.mention-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
/* 头像悬停放大效果 */
.mention-item:hover .mention-avatar {
  transform: scale(1.1);
}
/* 头像占位符（无头像时显示首字母） */
.mention-avatar-placeholder {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #667eea);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: transform 0.3s ease;
}
/* 占位符悬停放大效果 */
.mention-item:hover .mention-avatar-placeholder {
  transform: scale(1.1);
}
/* 用户昵称文本 */
.mention-name {
  margin-left: 10px;
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.mention-id {
  margin-left: 6px;
  font-size: 12px;
  color: #909399;
}
/* 无搜索结果提示 */
.mention-empty {
  padding: 16px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}
</style>
