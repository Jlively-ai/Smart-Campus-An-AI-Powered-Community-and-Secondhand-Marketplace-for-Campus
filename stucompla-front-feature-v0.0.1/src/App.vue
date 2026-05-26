<!--
  组件名：App
  功能描述：根组件，应用入口
  主要职责：
    1. 渲染路由视图
    2. 全局事件委托处理 @mention 链接点击（跳转到用户主页）
    3. 全局 Element UI 组件样式覆盖（现代风格美化）
-->
<template>
  <!-- 应用根容器 -->
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App',
  /** 组件挂载后：绑定全局点击事件委托，处理@mention链接跳转 */
  mounted: function() {
    var self = this
    document.addEventListener('click', function(e) {
      var target = e.target
      // 向上查找.mention-link元素
      while (target && !target.classList.contains('mention-link')) {
        target = target.parentElement
        if (!target) break
      }
      // 找到@mention链接后跳转到对应用户主页
      if (target && target.classList.contains('mention-link')) {
        var userId = target.getAttribute('data-userid')
        if (userId) {
          self.$router.push('/userProfile/' + userId).catch(function() {})
        }
      }
    })
  }
}
</script>

<style>
/* ===== 全局重置样式 ===== */
* {
  margin: 0;
  padding: 0;
}
/* 根元素及全局字体设置 */
html, body, #app {
  height: 100%;
  font-family: 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
/* @提及链接样式 */
.mention-link {
  color: #409eff;
  text-decoration: none;
  cursor: pointer;
  font-weight: 500;
}
.mention-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* ===== 全局美化：Element UI 组件增强（现代风格） ===== */

/* 卡片圆角阴影 */
.el-card {
  border-radius: 16px !important;
  border: none !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06) !important;
  transition: box-shadow 0.4s cubic-bezier(0.25, 0.8, 0.25, 1), transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1) !important;
}
.el-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12) !important;
  transform: translateY(-4px);
}
.el-card__header {
  border-bottom: 1px solid rgba(0,0,0,0.04) !important;
  padding: 18px 24px !important;
  font-weight: 600 !important;
}

/* 按钮圆角 */
.el-button {
  border-radius: 10px !important;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1) !important;
  font-weight: 500 !important;
}
.el-button--primary {
  background: linear-gradient(135deg, #409EFF, #667eea) !important;
  border-color: transparent !important;
  color: #fff !important;
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.35) !important;
}
.el-button--primary:hover {
  background: linear-gradient(135deg, #66b1ff, #8c9eff) !important;
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.45) !important;
}
.el-button--primary:active {
  transform: translateY(0) scale(0.98);
}
.el-button--primary.is-plain {
  background: #ecf5ff !important;
  border-color: #b3d8ff !important;
  color: #409EFF !important;
  box-shadow: none !important;
}
.el-button--primary.is-plain:hover {
  background: #409EFF !important;
  border-color: #409EFF !important;
  color: #fff !important;
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.35) !important;
}
.el-button--success {
  background: linear-gradient(135deg, #67C23A, #85ce61) !important;
  border-color: transparent !important;
  color: #fff !important;
  box-shadow: 0 4px 14px rgba(103, 194, 58, 0.3) !important;
}
.el-button--success:hover {
  background: linear-gradient(135deg, #85ce61, #a5d98a) !important;
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 24px rgba(103, 194, 58, 0.4) !important;
}
.el-button--success.is-plain {
  background: #f0f9eb !important;
  border-color: #c2e7b0 !important;
  color: #67C23A !important;
  box-shadow: none !important;
}
.el-button--success.is-plain:hover {
  background: #67C23A !important;
  border-color: #67C23A !important;
  color: #fff !important;
  box-shadow: 0 4px 14px rgba(103, 194, 58, 0.3) !important;
}
.el-button--warning {
  background: linear-gradient(135deg, #E6A23C, #f0c78a) !important;
  border-color: transparent !important;
  color: #fff !important;
  box-shadow: 0 4px 14px rgba(230, 162, 60, 0.3) !important;
}
.el-button--warning:hover {
  background: linear-gradient(135deg, #f0c78a, #f5dab1) !important;
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 24px rgba(230, 162, 60, 0.4) !important;
}
.el-button--warning.is-plain {
  background: #fdf6ec !important;
  border-color: #f5dab1 !important;
  color: #E6A23C !important;
  box-shadow: none !important;
}
.el-button--warning.is-plain:hover {
  background: #E6A23C !important;
  border-color: #E6A23C !important;
  color: #fff !important;
  box-shadow: 0 4px 14px rgba(230, 162, 60, 0.3) !important;
}
.el-button--danger {
  background: linear-gradient(135deg, #F56C6C, #f89898) !important;
  border-color: transparent !important;
  color: #fff !important;
  box-shadow: 0 4px 14px rgba(245, 108, 108, 0.3) !important;
}
.el-button--danger:hover {
  background: linear-gradient(135deg, #f89898, #fab6b6) !important;
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 24px rgba(245, 108, 108, 0.4) !important;
}
.el-button--danger.is-plain {
  background: #fef0f0 !important;
  border-color: #fbc4c4 !important;
  color: #F56C6C !important;
  box-shadow: none !important;
}
.el-button--danger.is-plain:hover {
  background: #F56C6C !important;
  border-color: #F56C6C !important;
  color: #fff !important;
  box-shadow: 0 4px 14px rgba(245, 108, 108, 0.3) !important;
}
.el-button--info {
  background: linear-gradient(135deg, #909399, #b4b7bd) !important;
  border-color: transparent !important;
  color: #fff !important;
  box-shadow: 0 4px 14px rgba(144, 147, 153, 0.25) !important;
}
.el-button--info:hover {
  background: linear-gradient(135deg, #b4b7bd, #cdd0d6) !important;
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 24px rgba(144, 147, 153, 0.35) !important;
}

/* 输入框圆角 */
.el-input__inner {
  border-radius: 10px !important;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1) !important;
  border-color: #dcdfe6 !important;
}
.el-input__inner:hover {
  border-color: #c0c4cc !important;
}
.el-input__inner:focus {
  border-color: #409EFF !important;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15) !important;
}
.el-textarea__inner {
  border-radius: 10px !important;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1) !important;
}
.el-textarea__inner:focus {
  border-color: #409EFF !important;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15) !important;
}

/* 对话框圆角 */
.el-dialog {
  border-radius: 20px !important;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.16) !important;
}
.el-dialog__header {
  background: linear-gradient(135deg, #409EFF, #667eea);
  padding: 20px 24px !important;
}
.el-dialog__title {
  color: #fff !important;
  font-weight: 600 !important;
  font-size: 16px !important;
  letter-spacing: 0.5px;
}
.el-dialog__headerbtn .el-dialog__close {
  color: rgba(255,255,255,0.85) !important;
  transition: all 0.3s ease !important;
}
.el-dialog__headerbtn .el-dialog__close:hover {
  color: #fff !important;
  transform: rotate(90deg);
}
.el-dialog__body {
  padding: 24px !important;
}
.el-dialog__footer {
  padding: 16px 24px 20px !important;
  border-top: 1px solid rgba(0,0,0,0.04);
}

/* 表格美化 */
.el-table {
  border-radius: 14px !important;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04) !important;
}
.el-table th {
  background-color: #f8f9fb !important;
  color: #606266 !important;
  font-weight: 600 !important;
  padding: 14px 12px !important;
}
.el-table td {
  padding: 14px 12px !important;
}
.el-table__row {
  transition: background-color 0.25s ease !important;
}
.el-table--enable-row-hover .el-table__body tr:hover > td {
  background-color: #f5f7ff !important;
}
.el-table::before {
  display: none;
}

/* 标签圆角 */
.el-tag {
  border-radius: 8px !important;
  font-weight: 500 !important;
  transition: all 0.3s ease !important;
}
.el-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0,0,0,0.08);
}

/* 分页圆角 */
.el-pagination .btn-prev,
.el-pagination .btn-next,
.el-pagination .el-pager li {
  border-radius: 8px !important;
  transition: all 0.3s ease !important;
}
.el-pagination .el-pager li.active {
  background: linear-gradient(135deg, #409EFF, #667eea) !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.35);
}

/* 下拉菜单圆角 */
.el-select-dropdown {
  border-radius: 12px !important;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12) !important;
  border: none !important;
}
.el-select-dropdown__item {
  border-radius: 8px !important;
  margin: 2px 6px !important;
  padding: 8px 16px !important;
  transition: all 0.2s ease !important;
}
.el-select-dropdown__item.hover,
.el-select-dropdown__item:hover {
  background: #f0f5ff !important;
}

/* 消息提示圆角 */
.el-message {
  border-radius: 12px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12) !important;
  border: none !important;
  padding: 14px 20px !important;
}

/* 通知圆角 */
.el-notification {
  border-radius: 14px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12) !important;
  border: none !important;
  padding: 18px 22px !important;
}

/* 确认框圆角 */
.el-message-box {
  border-radius: 18px !important;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.16) !important;
  border: none !important;
}
.el-message-box__header {
  background: linear-gradient(135deg, #409EFF, #667eea);
  padding: 18px 24px !important;
}
.el-message-box__title {
  color: #fff !important;
  font-weight: 600 !important;
}
.el-message-box__headerbtn .el-message-box__close {
  color: rgba(255,255,255,0.85) !important;
}
.el-message-box__headerbtn .el-message-box__close:hover {
  color: #fff !important;
}

/* 面包屑 */
.el-breadcrumb {
  margin-bottom: 18px;
}
.el-breadcrumb__item {
  font-weight: 500;
}

/* Tabs 圆角 */
.el-tabs__item {
  transition: all 0.3s ease !important;
  font-weight: 500 !important;
}
.el-tabs__item.is-active {
  font-weight: 600 !important;
}
.el-tabs__active-bar {
  border-radius: 2px 2px 0 0;
}

/* 抽屉圆角 */
.el-drawer {
  border-radius: 20px 0 0 20px !important;
  box-shadow: -8px 0 40px rgba(0,0,0,0.1) !important;
}

/* 进度条美化 */
.el-progress-bar__outer {
  border-radius: 10px !important;
  background-color: #f0f2f5 !important;
}
.el-progress-bar__inner {
  border-radius: 10px !important;
}

/* 空状态 */
.el-empty__image svg {
  opacity: 0.5;
}
.el-empty__description {
  color: #a0a3a8 !important;
  font-weight: 500;
}

/* 全局滚动条美化 - 更细更现代 */
::-webkit-scrollbar {
  width: 5px;
  height: 5px;
}
::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: #909399;
}
::-webkit-scrollbar-track {
  background: transparent;
}

/* 全局选中色 */
::selection {
  background: rgba(64, 158, 255, 0.2);
  color: inherit;
}

/* 全局过渡 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}

/* ===== 全局背景增强 ===== */
.app-main {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 50%, #e2e8f0 100%) !important;
  position: relative;
}
.app-main::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23409eff' fill-opacity='0.015' fill-rule='evenodd'/%3E%3C/svg%3E");
  pointer-events: none;
  z-index: 0;
}
.app-main.dark-bg {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%) !important;
}

/* ===== 浮动装饰光球 ===== */
.app-main::after {
  content: '';
  position: fixed;
  top: -100px;
  right: -100px;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(64,158,255,0.06) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
}

/* ===== 导航栏增强 ===== */
.navbar {
  background: rgba(255,255,255,0.75) !important;
  backdrop-filter: blur(24px) saturate(200%) !important;
  -webkit-backdrop-filter: blur(24px) saturate(200%) !important;
  border-bottom: 1px solid rgba(255,255,255,0.6) !important;
  box-shadow: 0 1px 20px rgba(0,0,0,0.04) !important;
}
.navbar.dark-mode {
  background: rgba(15,23,42,0.75) !important;
  border-bottom: 1px solid rgba(255,255,255,0.05) !important;
}

/* ===== 卡片悬停增强效果 ===== */
.el-card {
  position: relative;
  overflow: hidden;
}
.el-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #409EFF, #667eea, #409EFF);
  background-size: 200% 100%;
  opacity: 0;
  transition: opacity 0.4s ease;
}
.el-card:hover::after {
  opacity: 1;
  animation: shimmer 2s linear infinite;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ===== 表格增强 ===== */
.el-table {
  position: relative;
}
.el-table::before {
  display: none !important;
}
.el-table th {
  background: linear-gradient(180deg, #f8fafc, #f1f5f9) !important;
  border-bottom: 2px solid #e2e8f0 !important;
  font-weight: 600 !important;
  letter-spacing: 0.3px;
}

/* ===== 输入框聚焦发光增强 ===== */
.el-input__inner:focus {
  box-shadow: 0 0 0 4px rgba(64,158,255,0.12), 0 0 20px rgba(64,158,255,0.08) !important;
}

/* ===== 徽章增强 ===== */
.el-badge__content {
  border: 2px solid #fff !important;
  box-shadow: 0 2px 8px rgba(245,108,108,0.4) !important;
  font-weight: 600 !important;
}

/* ===== 弹出框增强 ===== */
.el-popover {
  border-radius: 16px !important;
  box-shadow: 0 12px 40px rgba(0,0,0,0.12) !important;
  border: 1px solid rgba(255,255,255,0.8) !important;
  backdrop-filter: blur(10px);
}

/* ===== 通知增强 ===== */
.el-notification {
  border-radius: 16px !important;
  box-shadow: 0 12px 40px rgba(0,0,0,0.15) !important;
  border: 1px solid rgba(255,255,255,0.8) !important;
  backdrop-filter: blur(10px);
}

/* ===== 确认框增强 ===== */
.el-message-box {
  border-radius: 20px !important;
  box-shadow: 0 24px 60px rgba(0,0,0,0.18) !important;
  border: 1px solid rgba(255,255,255,0.8) !important;
  overflow: hidden;
}

/* ===== 下拉选择增强 ===== */
.el-select-dropdown {
  border-radius: 14px !important;
  box-shadow: 0 12px 40px rgba(0,0,0,0.12) !important;
  border: 1px solid rgba(255,255,255,0.8) !important;
  backdrop-filter: blur(10px);
  padding: 6px !important;
}

/* ===== 标签页增强 ===== */
.el-tabs__nav-wrap::after {
  height: 1px !important;
  background: linear-gradient(90deg, transparent, #e2e8f0, transparent) !important;
}
.el-tabs__active-bar {
  background: linear-gradient(90deg, #409EFF, #667eea) !important;
  border-radius: 2px 2px 0 0;
  box-shadow: 0 -2px 8px rgba(64,158,255,0.3);
}

/* ===== 加载遮罩增强 ===== */
.el-loading-mask {
  background: rgba(255,255,255,0.85) !important;
  backdrop-filter: blur(8px) !important;
}

/* ===== 开关增强 ===== */
.el-switch.is-checked .el-switch__core {
  background: linear-gradient(135deg, #409EFF, #667eea) !important;
  border-color: transparent !important;
  box-shadow: 0 2px 8px rgba(64,158,255,0.3);
}

/* ===== 滑块增强 ===== */
.el-slider__runway {
  background: #e2e8f0 !important;
  border-radius: 4px;
}
.el-slider__bar {
  background: linear-gradient(90deg, #409EFF, #667eea) !important;
  border-radius: 4px;
}
.el-slider__button {
  border-color: #409EFF !important;
  box-shadow: 0 2px 8px rgba(64,158,255,0.3) !important;
}

/* ===== 评分增强 ===== */
.el-rate__icon {
  transition: all 0.3s ease;
}
.el-rate__icon:hover {
  transform: scale(1.2);
}

/* ===== 日历增强 ===== */
.el-calendar-table td.is-today {
  background: linear-gradient(135deg, #ecf5ff, #f0f7ff) !important;
  border-radius: 8px;
}
.el-calendar-table td.is-selected {
  background: linear-gradient(135deg, #409EFF, #667eea) !important;
  color: #fff !important;
  border-radius: 8px;
}

/* ===== 折叠面板增强 ===== */
.el-collapse-item__header {
  font-weight: 600 !important;
  border-radius: 12px;
  padding: 0 16px !important;
  background: linear-gradient(135deg, #fafafa, #f5f7fa) !important;
  margin-bottom: 4px;
}

/* ===== 描述列表增强 ===== */
.el-descriptions__title {
  font-weight: 700 !important;
  font-size: 16px !important;
  color: #1f2937 !important;
}
.el-descriptions__label {
  font-weight: 600 !important;
  color: #4b5563 !important;
  background: #f8fafc !important;
  border-radius: 8px 0 0 8px;
}

/* ===== 结果页增强 ===== */
.el-result__icon svg {
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.08));
}

/* ===== 统计数值增强 ===== */
.el-statistic__content {
  font-weight: 700 !important;
  background: linear-gradient(135deg, #1f2937, #4b5563);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* ===== 骨架屏增强 ===== */
.el-skeleton__item {
  border-radius: 12px !important;
}

/* ===== 图片增强 ===== */
.el-image {
  border-radius: 12px;
  overflow: hidden;
}
.el-image__inner {
  transition: transform 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.el-image:hover .el-image__inner {
  transform: scale(1.05);
}

/* ===== 颜色选择器增强 ===== */
.el-color-picker__trigger {
  border-radius: 10px !important;
  overflow: hidden;
}

/* ===== 穿梭框增强 ===== */
.el-transfer-panel {
  border-radius: 14px !important;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06) !important;
  border: none !important;
}

/* ===== 走马灯增强 ===== */
.el-carousel__indicators--outside button {
  border-radius: 4px !important;
  height: 4px !important;
}
.el-carousel__indicator.is-active button {
  background: linear-gradient(90deg, #409EFF, #667eea) !important;
  width: 24px !important;
}

/* ===== 树形控件增强 ===== */
.el-tree-node__content {
  border-radius: 8px;
  margin: 2px 0;
  transition: all 0.3s ease;
}
.el-tree-node__content:hover {
  background: #f0f5ff !important;
}

/* ===== 级联选择增强 ===== */
.el-cascader-menu {
  border-radius: 12px !important;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1) !important;
  border: none !important;
}

/* ===== 日期选择器增强 ===== */
.el-picker-panel {
  border-radius: 16px !important;
  box-shadow: 0 12px 40px rgba(0,0,0,0.12) !important;
  border: none !important;
}
.el-picker-panel__shortcut:hover {
  background: #f0f5ff !important;
  color: #409EFF !important;
}
.el-date-table td.current:not(.disabled) span {
  background: linear-gradient(135deg, #409EFF, #667eea) !important;
  box-shadow: 0 2px 8px rgba(64,158,255,0.3);
}

/* ===== 时间选择器增强 ===== */
.el-time-panel {
  border-radius: 16px !important;
  box-shadow: 0 12px 40px rgba(0,0,0,0.12) !important;
  border: none !important;
}

/* ===== 自动补全增强 ===== */
.el-autocomplete-suggestion {
  border-radius: 12px !important;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1) !important;
  border: none !important;
}

/* ===== 下拉菜单增强 ===== */
.el-dropdown-menu__item--divided {
  border-top-color: #e2e8f0 !important;
}
.el-dropdown-menu__item--divided:before {
  background: transparent !important;
}

/* ===== 面包屑增强 ===== */
.el-breadcrumb__inner.is-link:hover {
  color: #409EFF !important;
  text-shadow: 0 0 8px rgba(64,158,255,0.2);
}

/* ===== 页头增强 ===== */
.el-page-header__left .el-icon-back {
  color: #409EFF !important;
  font-size: 18px !important;
}

/* ===== 回到顶部增强 ===== */
.el-backtop {
  border-radius: 50% !important;
  box-shadow: 0 4px 20px rgba(64,158,255,0.3) !important;
  background: linear-gradient(135deg, #409EFF, #667eea) !important;
  color: #fff !important;
  transition: all 0.3s ease !important;
}
.el-backtop:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(64,158,255,0.4) !important;
}

/* ===== 无限滚动加载增强 ===== */
.el-infinite-scroll__loading {
  color: #409EFF !important;
}

/* ===== 分割线增强 ===== */
.el-divider__text {
  background: transparent !important;
  font-weight: 600;
  color: #6b7280;
}

/* ===== 确认弹出框增强 ===== */
.el-popconfirm__main {
  font-weight: 500;
}

/* ===== 抽屉增强 ===== */
.el-drawer {
  border-radius: 20px 0 0 20px !important;
  box-shadow: -8px 0 40px rgba(0,0,0,0.1) !important;
}
.el-drawer__header {
  font-weight: 700 !important;
  font-size: 18px !important;
  color: #1f2937 !important;
  border-bottom: 1px solid #f1f5f9 !important;
  padding: 20px 24px !important;
}

/* ===== 时间线增强 ===== */
.el-timeline-item__node--normal {
  box-shadow: 0 0 0 4px rgba(64,158,255,0.15);
}
.el-timeline-item__wrapper {
  padding-left: 24px !important;
}

/* ===== 步骤条增强 ===== */
.el-step__head.is-finish {
  color: #409EFF !important;
  border-color: #409EFF !important;
}
.el-step__title.is-finish {
  color: #409EFF !important;
  font-weight: 600;
}
.el-step__description.is-finish {
  color: #667eea !important;
}

/* ===== 上传列表增强 ===== */
.el-upload-list__item {
  border-radius: 10px !important;
  transition: all 0.3s ease !important;
}
.el-upload-list__item:hover {
  background: #f0f5ff !important;
}

/* ===== 标签关闭按钮增强 ===== */
.el-tag .el-icon-close {
  transition: all 0.3s ease;
}
.el-tag .el-icon-close:hover {
  background: rgba(0,0,0,0.1) !important;
  transform: rotate(90deg);
}

/* ===== 警告提示增强 ===== */
.el-alert {
  border-radius: 12px !important;
  border: none !important;
}
.el-alert--success {
  background: linear-gradient(135deg, #f0f9eb, #e6f7e6) !important;
}
.el-alert--warning {
  background: linear-gradient(135deg, #fdf6ec, #fef3e6) !important;
}
.el-alert--error {
  background: linear-gradient(135deg, #fef0f0, #fee6e6) !important;
}
.el-alert--info {
  background: linear-gradient(135deg, #f4f4f5, #ececf0) !important;
}

/* ===== 通知类型增强 ===== */
.el-notification.success {
  border-left: 4px solid #67C23A !important;
}
.el-notification.warning {
  border-left: 4px solid #E6A23C !important;
}
.el-notification.error {
  border-left: 4px solid #F56C6C !important;
}
.el-notification.info {
  border-left: 4px solid #909399 !important;
}

/* ===== 侧边栏菜单增强 ===== */
.el-menu {
  border-right: none !important;
}
.el-menu-item, .el-submenu__title {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1) !important;
  border-radius: 0 12px 12px 0;
  margin: 4px 12px 4px 0;
  height: 48px;
  line-height: 48px;
  font-size: 14px;
  position: relative;
  overflow: hidden;
}
.el-menu-item::before, .el-submenu__title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  width: 3px;
  height: 20px;
  background: linear-gradient(180deg, #409EFF, #667eea);
  border-radius: 0 3px 3px 0;
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.el-menu-item:hover, .el-submenu__title:hover {
  background: rgba(255, 255, 255, 0.06) !important;
}
.el-menu-item:hover::before, .el-submenu__title:hover::before {
  transform: translateY(-50%) scaleY(0.6);
}
.el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(64,158,255,0.18), rgba(102,126,234,0.08)) !important;
  font-weight: 600;
}
.el-menu-item.is-active::before {
  transform: translateY(-50%) scaleY(1);
}
.el-menu-item i, .el-submenu__title i {
  color: inherit;
  font-size: 16px;
  margin-right: 10px;
  transition: all 0.3s ease;
}
.el-menu-item:hover i, .el-submenu__title:hover i {
  transform: scale(1.15);
}
.el-submenu.is-active .el-submenu__title {
  font-weight: 600;
}
.el-submenu .el-menu-item {
  height: 40px;
  line-height: 40px;
  font-size: 13px;
}
.el-submenu .el-menu-item::before {
  display: none;
}
.el-submenu .el-menu-item.is-active {
  background: rgba(64,158,255,0.12) !important;
}
/* 子菜单箭头动画 */
.el-submenu__icon-arrow {
  transition: transform 0.3s ease;
}

/* 表单增强 */
.el-form-item__label {
  font-weight: 500 !important;
  color: #606266 !important;
}
.el-radio__input.is-checked .el-radio__inner {
  border-color: #409EFF !important;
  background: #409EFF !important;
}
.el-checkbox__input.is-checked .el-checkbox__inner {
  border-color: #409EFF !important;
  background: #409EFF !important;
}

/* 开关增强 */
.el-switch.is-checked .el-switch__core {
  border-color: #409EFF !important;
  background-color: #409EFF !important;
}

/* 头像增强 */
.el-avatar {
  transition: all 0.3s ease !important;
}
.el-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0,0,0,0.12);
}

/* 链接增强 */
.el-link {
  transition: all 0.3s ease !important;
}
.el-link.el-link--primary:hover {
  text-shadow: 0 0 8px rgba(64, 158, 255, 0.25);
}

/* 上传组件增强 */
.el-upload--picture-card {
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
}
.el-upload--picture-card:hover {
  border-color: #409EFF !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

/* 时间线增强 */
.el-timeline-item__node {
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.15);
}

/* 步骤条增强 */
.el-step__icon {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

/* 徽章增强 */
.el-badge__content {
  border-radius: 10px !important;
  box-shadow: 0 2px 6px rgba(245, 108, 108, 0.35);
  border: 2px solid #fff !important;
}

/* 下拉菜单增强 */
.el-dropdown-menu {
  border-radius: 12px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12) !important;
  border: none !important;
  padding: 6px !important;
}
.el-dropdown-menu__item {
  border-radius: 8px !important;
  transition: all 0.2s ease !important;
  padding: 8px 16px !important;
}
.el-dropdown-menu__item:hover {
  background: #f0f5ff !important;
  color: #409EFF !important;
}

/* 弹出框增强 */
.el-popover {
  border-radius: 14px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1) !important;
  border: none !important;
}

/* 折叠面板增强 */
.el-collapse-item__header {
  font-weight: 500 !important;
  transition: all 0.3s ease !important;
}

/* 描述列表增强 */
.el-descriptions__title {
  font-weight: 600 !important;
}

/* 结果页增强 */
.el-result__icon svg {
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.08));
}
</style>
