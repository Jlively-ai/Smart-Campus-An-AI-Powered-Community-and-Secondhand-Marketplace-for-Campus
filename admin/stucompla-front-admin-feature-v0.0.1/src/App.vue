<!--
  组件名：App
  功能描述：后台管理系统根组件
  主要职责：作为Vue应用的顶层组件，承载路由视图和全局Element UI样式美化
-->
<template>
  <!-- 根容器，渲染路由匹配的组件 -->
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  /** 组件名称 */
  name: 'App'
}
</script>

<style>
/* ===== 全局Element UI美化样式 ===== */

/* 卡片组件样式：圆角、阴影、悬浮动效 */
.el-card {
  border-radius: 16px !important;
  border: none !important;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06) !important;
  transition: transform 0.3s ease, box-shadow 0.3s ease !important;
}
.el-card:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 30px rgba(0,0,0,0.1) !important;
}
.el-card__header {
  padding: 18px 24px !important;
  border-bottom: 1px solid #f0f0f0 !important;
  font-weight: 600 !important;
  color: #1f2937 !important;
}

/* 按钮组件样式：圆角、渐变背景、悬浮阴影 */
.el-button {
  border-radius: 10px !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  font-weight: 500 !important;
}
.el-button:active {
  transform: scale(0.97) !important;
}
/* 主要按钮：蓝色渐变 */
.el-button--primary {
  background: linear-gradient(135deg, #409EFF, #667eea) !important;
  border-color: transparent !important;
  box-shadow: 0 4px 14px rgba(64,158,255,0.35) !important;
}
.el-button--primary:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 6px 20px rgba(64,158,255,0.45) !important;
  opacity: 0.95;
}
/* 主要朴素按钮 */
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
  box-shadow: 0 4px 14px rgba(64,158,255,0.35) !important;
}
/* 危险按钮：红色渐变 */
.el-button--danger {
  background: linear-gradient(135deg, #F56C6C, #ff8e8e) !important;
  border-color: transparent !important;
  box-shadow: 0 4px 14px rgba(245,108,108,0.3) !important;
  color: #fff !important;
}
.el-button--danger:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 6px 20px rgba(245,108,108,0.4) !important;
}
/* 危险朴素按钮 */
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
  box-shadow: 0 4px 14px rgba(245,108,108,0.3) !important;
}
/* 警告按钮：橙色渐变 */
.el-button--warning {
  background: linear-gradient(135deg, #E6A23C, #f5c842) !important;
  border-color: transparent !important;
  box-shadow: 0 4px 14px rgba(230,162,60,0.3) !important;
  color: #fff !important;
}
.el-button--warning:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 6px 20px rgba(230,162,60,0.4) !important;
}
/* 警告朴素按钮 */
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
  box-shadow: 0 4px 14px rgba(230,162,60,0.3) !important;
}
/* 成功按钮：绿色渐变 */
.el-button--success {
  background: linear-gradient(135deg, #67C23A, #85d95c) !important;
  border-color: transparent !important;
  box-shadow: 0 4px 14px rgba(103,194,58,0.3) !important;
  color: #fff !important;
}
.el-button--success:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 6px 20px rgba(103,194,58,0.4) !important;
}
/* 成功朴素按钮 */
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
  box-shadow: 0 4px 14px rgba(103,194,58,0.3) !important;
}
/* 信息按钮：灰色渐变 */
.el-button--info {
  background: linear-gradient(135deg, #909399, #b0b3b8) !important;
  border-color: transparent !important;
  box-shadow: 0 4px 14px rgba(144,147,153,0.25) !important;
  color: #fff !important;
}
.el-button--info:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 6px 20px rgba(144,147,153,0.35) !important;
}

/* 输入框样式：圆角、聚焦光环 */
.el-input__inner {
  border-radius: 10px !important;
  transition: all 0.3s ease !important;
  border-color: #dcdfe6 !important;
}
.el-input__inner:focus {
  box-shadow: 0 0 0 4px rgba(64,158,255,0.12) !important;
  border-color: #409EFF !important;
}

/* 对话框样式：大圆角、渐变头部 */
.el-dialog {
  border-radius: 20px !important;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15) !important;
}
.el-dialog__header {
  background: linear-gradient(135deg, #409EFF, #667eea) !important;
  padding: 20px 24px !important;
}
.el-dialog__title {
  color: #fff !important;
  font-weight: 600 !important;
  font-size: 16px !important;
}
.el-dialog__headerbtn .el-dialog__close {
  color: #fff !important;
  transition: transform 0.3s ease !important;
}
.el-dialog__headerbtn:hover .el-dialog__close {
  transform: rotate(90deg) !important;
}
.el-dialog__body {
  padding: 24px !important;
}
.el-dialog__footer {
  padding: 16px 24px 20px !important;
  border-top: 1px solid #f0f0f0;
}

/* 表格样式：圆角、渐变表头、斑马纹 */
.el-table {
  border-radius: 16px !important;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04) !important;
}
.el-table th {
  background: #f8fafc !important;
  font-weight: 600 !important;
  color: #374151 !important;
  padding: 14px 0 !important;
}
.el-table td {
  padding: 12px 0 !important;
  transition: background 0.2s ease;
}
.el-table--striped .el-table__body tr.el-table__row--striped td {
  background: #fafbfc !important;
}
.el-table--enable-row-hover .el-table__body tr:hover > td {
  background: #f0f7ff !important;
}

/* 标签样式：圆角 */
.el-tag {
  border-radius: 8px !important;
  font-weight: 500 !important;
  padding: 0 10px !important;
}

/* 分页样式：圆角按钮、渐变激活页码 */
.el-pagination .btn-prev,
.el-pagination .btn-next,
.el-pagination .el-pager li {
  border-radius: 10px !important;
  transition: all 0.3s ease !important;
  font-weight: 500 !important;
}
.el-pagination .el-pager li.active {
  background: linear-gradient(135deg, #409EFF, #667eea) !important;
  color: #fff !important;
  box-shadow: 0 4px 10px rgba(64,158,255,0.3) !important;
}

/* 下拉选择面板样式 */
.el-select-dropdown {
  border-radius: 12px !important;
  box-shadow: 0 8px 30px rgba(0,0,0,0.1) !important;
}

/* 消息提示样式 */
.el-message {
  border-radius: 12px !important;
  box-shadow: 0 8px 30px rgba(0,0,0,0.12) !important;
  border: none !important;
}

/* 消息弹框样式 */
.el-message-box {
  border-radius: 18px !important;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15) !important;
  border: none !important;
}
.el-message-box__header {
  background: linear-gradient(135deg, #409EFF, #667eea) !important;
}
.el-message-box__title {
  color: #fff !important;
  font-weight: 600 !important;
}
.el-message-box__headerbtn .el-message-box__close {
  color: #fff !important;
}

/* 表单标签样式 */
.el-form-item__label {
  font-weight: 500 !important;
  color: #4b5563 !important;
}

/* 步骤条样式：渐变图标 */
.el-step__icon {
  border-radius: 50% !important;
  background: linear-gradient(135deg, #409EFF, #667eea) !important;
  color: #fff !important;
  border: none !important;
  box-shadow: 0 4px 10px rgba(64,158,255,0.3) !important;
}
.el-step__title.is-success {
  color: #409EFF !important;
  font-weight: 600 !important;
}

/* 时间线节点样式 */
.el-timeline-item__node {
  box-shadow: 0 4px 10px rgba(64,158,255,0.3) !important;
}

/* 上传组件样式 */
.el-upload--picture-card {
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
}
.el-upload--picture-card:hover {
  border-color: #409EFF !important;
  box-shadow: 0 4px 14px rgba(64,158,255,0.2) !important;
}

/* 单选/复选框选中样式 */
.el-radio__input.is-checked .el-radio__inner {
  background: #409EFF !important;
  border-color: #409EFF !important;
}
.el-checkbox__input.is-checked .el-checkbox__inner {
  background: #409EFF !important;
  border-color: #409EFF !important;
}

/* 进度条样式 */
.el-progress-bar__outer {
  border-radius: 10px !important;
  background: #f3f4f6 !important;
}
.el-progress-bar__inner {
  border-radius: 10px !important;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1) !important;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}
::-webkit-scrollbar-track {
  background: transparent;
}

/* 文字选中样式 */
::selection {
  background: rgba(64,158,255,0.2);
}

/* 链接样式 */
.el-link {
  transition: all 0.3s ease !important;
  font-weight: 500 !important;
}
.el-link.el-link--primary:hover {
  opacity: 0.8;
}

/* 头像样式 */
.el-avatar {
  box-shadow: 0 4px 10px rgba(0,0,0,0.1) !important;
}

/* 下拉菜单样式 */
.el-dropdown-menu {
  border-radius: 12px !important;
  box-shadow: 0 8px 30px rgba(0,0,0,0.1) !important;
  border: none !important;
  padding: 8px !important;
}
.el-dropdown-menu__item {
  border-radius: 8px !important;
  transition: all 0.2s ease !important;
  padding: 8px 16px !important;
}
.el-dropdown-menu__item:hover {
  background: #f0f7ff !important;
  color: #409EFF !important;
}

/* 提示气泡样式 */
.el-tooltip__popper {
  border-radius: 10px !important;
  padding: 8px 12px !important;
}

/* 加载遮罩样式 */
.el-loading-mask {
  border-radius: 16px !important;
  background: rgba(255,255,255,0.8) !important;
  backdrop-filter: blur(4px);
}

/* ===== 后台主内容区背景增强 ===== */
.app-main {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 50%, #e8edf5 100%) !important;
  position: relative;
}
/* 背景装饰纹理 */
.app-main::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23409eff' fill-opacity='0.012' fill-rule='evenodd'/%3E%3C/svg%3E");
  pointer-events: none;
  z-index: 0;
}

/* ===== 浮动装饰光球 ===== */
.app-main::after {
  content: '';
  position: fixed;
  top: -100px;
  right: -100px;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(64,158,255,0.05) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
}

/* ===== 侧边栏增强样式 ===== */
.sidebar-container {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 50%, #1e1b4b 100%) !important;
  position: relative;
  box-shadow: 4px 0 24px rgba(0,0,0,0.15) !important;
}
/* 侧边栏纹理背景 */
.sidebar-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23409eff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.6;
  pointer-events: none;
}
/* 侧边栏Logo区域 */
.sidebar-logo {
  display: flex !important;
  align-items: center !important;
  padding: 0 20px !important;
  height: 80px !important;
  background: rgba(255,255,255,0.04) !important;
  border-bottom: 1px solid rgba(255,255,255,0.06) !important;
}
.sidebar-logo .logo-icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: linear-gradient(135deg, #409EFF, #667eea);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  box-shadow: 0 4px 15px rgba(64,158,255,0.35);
}
.sidebar-logo .logo-icon i {
  color: #fff;
  font-size: 22px;
}
.sidebar-logo .logo-text {
  display: flex;
  flex-direction: column;
}
.sidebar-logo .logo-title {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
  line-height: 1.3;
}
.sidebar-logo .logo-subtitle {
  color: rgba(255,255,255,0.45);
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 2px;
  text-transform: uppercase;
  margin-top: 2px;
}

/* ===== 侧边栏菜单增强样式 ===== */
.el-menu {
  border-right: none !important;
  background: transparent !important;
}
/* 菜单项通用样式 */
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
/* 菜单项左侧激活指示条 */
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
/* 菜单项悬浮效果 */
.el-menu-item:hover, .el-submenu__title:hover {
  background: rgba(255, 255, 255, 0.06) !important;
}
.el-menu-item:hover::before, .el-submenu__title:hover::before {
  transform: translateY(-50%) scaleY(0.6);
}
/* 菜单项激活状态 */
.el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(64,158,255,0.18), rgba(102,126,234,0.08)) !important;
  font-weight: 600;
}
.el-menu-item.is-active::before {
  transform: translateY(-50%) scaleY(1);
}
/* 菜单项图标动效 */
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
/* 子菜单项样式 */
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

/* ===== 顶部导航栏增强样式 ===== */
.navbar {
  background: rgba(255,255,255,0.75) !important;
  backdrop-filter: blur(24px) saturate(200%) !important;
  -webkit-backdrop-filter: blur(24px) saturate(200%) !important;
  border-bottom: 1px solid rgba(255,255,255,0.6) !important;
  box-shadow: 0 1px 20px rgba(0,0,0,0.04) !important;
}

/* ===== 卡片微光动画 ===== */
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

/* ===== 表格增强样式 ===== */
.el-table::before {
  display: none !important;
}
.el-table th {
  background: linear-gradient(180deg, #f8fafc, #f1f5f9) !important;
  border-bottom: 2px solid #e2e8f0 !important;
  font-weight: 600 !important;
  letter-spacing: 0.3px;
}

/* ===== 输入框聚焦增强 ===== */
.el-input__inner:focus {
  box-shadow: 0 0 0 4px rgba(64,158,255,0.12), 0 0 20px rgba(64,158,255,0.08) !important;
}

/* ===== 徽标增强 ===== */
.el-badge__content {
  border: 2px solid #fff !important;
  box-shadow: 0 2px 8px rgba(245,108,108,0.4) !important;
  font-weight: 600 !important;
}

/* ===== 弹出层/下拉面板增强 ===== */
.el-popover, .el-select-dropdown, .el-dropdown-menu, .el-autocomplete-suggestion {
  border-radius: 14px !important;
  box-shadow: 0 12px 40px rgba(0,0,0,0.12) !important;
  border: 1px solid rgba(255,255,255,0.8) !important;
  backdrop-filter: blur(10px);
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

/* ===== 步骤条完成状态增强 ===== */
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

/* ===== 树形控件增强 ===== */
.el-tree-node__content {
  border-radius: 8px;
  margin: 2px 0;
  transition: all 0.3s ease;
}
.el-tree-node__content:hover {
  background: #f0f5ff !important;
}

/* ===== 图片组件增强 ===== */
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

/* ===== 走马灯增强 ===== */
.el-carousel__indicators--outside button {
  border-radius: 4px !important;
  height: 4px !important;
}
.el-carousel__indicator.is-active button {
  background: linear-gradient(90deg, #409EFF, #667eea) !important;
  width: 24px !important;
}

/* ===== 穿梭框增强 ===== */
.el-transfer-panel {
  border-radius: 14px !important;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06) !important;
  border: none !important;
}

/* ===== 级联选择器增强 ===== */
.el-cascader-menu {
  border-radius: 12px !important;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1) !important;
  border: none !important;
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

/* ===== 分割线增强 ===== */
.el-divider__text {
  background: transparent !important;
  font-weight: 600;
  color: #6b7280;
}

/* ===== 确认弹框增强 ===== */
.el-popconfirm__main {
  font-weight: 500;
}

/* ===== 评分增强 ===== */
.el-rate__icon {
  transition: all 0.3s ease;
}
.el-rate__icon:hover {
  transform: scale(1.2);
}

/* ===== 骨架屏增强 ===== */
.el-skeleton__item {
  border-radius: 12px !important;
}

/* ===== 颜色选择器增强 ===== */
.el-color-picker__trigger {
  border-radius: 10px !important;
  overflow: hidden;
}

/* ===== 无限滚动加载增强 ===== */
.el-infinite-scroll__loading {
  color: #409EFF !important;
}
</style>
