<!--
  组件名：LoginIndex
  功能描述：管理员登录页面
  主要职责：
    1. 提供管理员账号密码登录表单
    2. 支持记住密码和自动登录功能
    3. 登录成功后跳转首页
-->
<template>
  <div class="login-container">
    <!-- 登录表单 -->
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <!-- 系统标题 -->
      <h3 class="title">智联校园后台管理</h3>
      <!-- 账号输入框 -->
      <el-form-item prop="username">
        <el-input v-model="loginForm.username" prefix-icon="el-icon-user" placeholder="管理员账号"></el-input>
      </el-form-item>
      <!-- 密码输入框，回车触发登录 -->
      <el-form-item prop="password">
        <el-input v-model="loginForm.password" prefix-icon="el-icon-lock" placeholder="密码" type="password" @keyup.enter.native="handleLogin"></el-input>
      </el-form-item>
      <!-- 记住密码和自动登录选项 -->
      <el-form-item>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <el-checkbox v-model="rememberPassword">记住密码</el-checkbox>
          <el-checkbox v-model="autoLogin">自动登录</el-checkbox>
        </div>
      </el-form-item>
      <!-- 登录按钮 -->
      <el-button type="primary" style="width:100%;" :loading="loading" @click="handleLogin">登 录</el-button>
    </el-form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      /** 登录表单数据 */
      loginForm: { username: '', password: '' },
      /** 表单验证规则 */
      loginRules: {
        username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      /** 登录按钮加载状态 */
      loading: false,
      /** 是否记住密码 */
      rememberPassword: false,
      /** 是否自动登录 */
      autoLogin: false
    }
  },
  /**
   * 生命周期：组件创建时
   * 检查本地存储的登录信息，若存在则自动填充表单
   * 若开启自动登录且有有效Token，则自动跳转首页
   */
  created() {
    const saved = localStorage.getItem('admin_saved_login')
    if (saved) {
      try {
        const data = JSON.parse(saved)
        this.loginForm.username = data.username || ''
        this.loginForm.password = data.password || ''
        this.rememberPassword = true
        this.autoLogin = data.autoLogin || false
        // 自动登录逻辑
        if (this.autoLogin && data.token) {
          window.sessionStorage.setItem('admin_token', data.token)
          this.$store.commit('user/SET_TOKEN', data.token)
          this.$store.dispatch('user/getInfo').then(() => {
            this.$router.push('/')
          }).catch(() => {
            localStorage.removeItem('admin_saved_login')
            window.sessionStorage.removeItem('admin_token')
          })
        }
      } catch (e) {
        localStorage.removeItem('admin_saved_login')
      }
    }
  },
  methods: {
    /**
     * 处理登录操作
     * 1. 表单验证
     * 2. 调用Vuex登录action
     * 3. 记住密码时保存到localStorage
     * 4. 登录成功跳转首页
     */
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (!valid) return
        this.loading = true
        this.$store.dispatch('user/login', this.loginForm).then(() => {
          this.loading = false
          // 记住密码逻辑
          if (this.rememberPassword) {
            const token = this.$store.state.user.token
            const saveData = {
              username: this.loginForm.username,
              password: this.loginForm.password,
              autoLogin: this.autoLogin,
              token: this.autoLogin ? token : ''
            }
            localStorage.setItem('admin_saved_login', JSON.stringify(saveData))
          } else {
            localStorage.removeItem('admin_saved_login')
          }
          this.$router.push('/')
        }).catch(err => {
          this.$message.error(err.message || '登录失败')
          this.loading = false
        })
      })
    }
  }
}
</script>

<style scoped>
/* 登录容器：全屏居中、渐变背景动画 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #1a2a6c, #2d3a4b 40%, #667eea);
  background-size: 200% 200%;
  animation: gradientShift 8s ease infinite;
}
@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
/* 登录表单卡片：毛玻璃效果 */
.login-form {
  width: 380px;
  padding: 48px 40px;
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255,255,255,0.3);
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
}
/* 标题样式：底部渐变装饰线 */
.title {
  text-align: center;
  margin-bottom: 36px;
  color: #303133;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 2px;
  position: relative;
  padding-bottom: 14px;
}
.title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 50px;
  height: 3px;
  background: linear-gradient(135deg, #409EFF, #667eea);
  border-radius: 2px;
}
/* 输入框样式覆盖 */
.login-form >>> .el-input__inner {
  border-radius: 8px;
  height: 44px;
  background: rgba(255,255,255,0.7);
}
/* 登录按钮样式覆盖 */
.login-form >>> .el-button--primary {
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  border-radius: 8px;
}
</style>
