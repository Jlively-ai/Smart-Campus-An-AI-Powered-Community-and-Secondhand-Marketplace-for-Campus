<!--
  组件名：Login
  功能描述：登录页面，支持普通用户和管理员两种登录模式
  主要职责：
    1. 提供用户名/密码登录表单
    2. 支持记住密码和自动登录功能
    3. 根据登录类型调用不同的后端接口
    4. 登录成功后将token和用户信息存入Vuex和sessionStorage
-->
<template>
  <!-- 登录页容器 -->
  <div class="login-container">
    <div class="login-box">
      <h2>智联校园 - 登录</h2>
      <!-- 登录类型切换：普通用户/管理员 -->
      <el-radio-group v-model="loginType" style="display:flex;justify-content:center;margin-bottom:20px;">
        <el-radio-button label="user">普通用户</el-radio-button>
        <el-radio-button label="admin">管理员</el-radio-button>
      </el-radio-group>
      <!-- 登录表单 -->
      <el-form ref="loginForm" :model="loginForm" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" prefix-icon="el-icon-user" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" prefix-icon="el-icon-lock" placeholder="请输入密码" type="password" @keyup.enter.native="login"></el-input>
        </el-form-item>
        <!-- 记住密码/自动登录选项 -->
        <el-form-item>
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <el-checkbox v-model="rememberPassword">记住密码</el-checkbox>
            <el-checkbox v-model="autoLogin">自动登录</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width:100%" @click="login" :loading="loading">登 录</el-button>
        </el-form-item>
        <!-- 注册链接（仅普通用户显示） -->
        <el-form-item v-if="loginType === 'user'">
          <router-link to="/register">没有账号？去注册</router-link>
        </el-form-item>
        <!-- 后台管理系统入口 -->
        <div class="admin-entry">
          <a href="http://localhost:9528" target="_blank"><i class="el-icon-setting"></i> 后台管理系统</a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      /** 登录类型：'user'普通用户 / 'admin'管理员 */
      loginType: 'user',
      /** 登录表单数据 */
      loginForm: { username: '', password: '' },
      /** 表单校验规则 */
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      /** 是否正在登录中 */
      loading: false,
      /** 是否记住密码 */
      rememberPassword: false,
      /** 是否自动登录 */
      autoLogin: false
    }
  },
  /** 组件创建时加载保存的登录信息 */
  created() {
    this.loadSavedLogin()
  },
  methods: {
    /** 从localStorage加载保存的登录信息，支持自动登录 */
    loadSavedLogin() {
      const saved = localStorage.getItem('savedLogin')
      if (saved) {
        try {
          const data = JSON.parse(saved)
          this.loginForm.username = data.username || ''
          this.loginForm.password = data.password || ''
          this.loginType = data.loginType || 'user'
          this.rememberPassword = true
          this.autoLogin = data.autoLogin || false
          // 自动登录：使用保存的token直接获取用户信息并跳转
          if (this.autoLogin && data.token) {
            this.$store.commit('SET_TOKEN', data.token)
            this.$store.commit('SET_LOGIN_TYPE', data.loginType)
            if (data.loginType === 'admin') {
              this.$axios.get('/admin/info').then(res => {
                if (res.code === 200) {
                  this.$store.commit('SET_USER_INFO', res.data)
                  this.$router.push('/home')
                }
              }).catch(() => {
                localStorage.removeItem('savedLogin')
              })
            } else {
              this.$axios.get('/user/info').then(res => {
                if (res.code === 200) {
                  this.$store.commit('SET_USER_INFO', res.data)
                  this.$router.push('/home')
                }
              }).catch(() => {
                localStorage.removeItem('savedLogin')
              })
            }
          }
        } catch (e) {
          localStorage.removeItem('savedLogin')
        }
      }
    },
    /** 保存登录信息到localStorage（记住密码/自动登录时调用） */
    saveLoginInfo(token) {
      if (this.rememberPassword) {
        const data = {
          username: this.loginForm.username,
          password: this.loginForm.password,
          loginType: this.loginType,
          autoLogin: this.autoLogin,
          token: this.autoLogin ? token : ''
        }
        localStorage.setItem('savedLogin', JSON.stringify(data))
      } else {
        localStorage.removeItem('savedLogin')
      }
    },
    /** 登录方法：校验表单后根据登录类型调用不同接口 */
    login() {
      this.$refs.loginForm.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          if (this.loginType === 'admin') {
            // 管理员登录
            const res = await this.$axios.post('/admin/info/login', this.loginForm)
            if (res.code === 200) {
              const token = res.data
              this.$store.commit('SET_TOKEN', token)
              this.$store.commit('SET_LOGIN_TYPE', 'admin')
              const adminRes = await this.$axios.get('/admin/info')
              if (adminRes.code === 200) {
                this.$store.commit('SET_USER_INFO', adminRes.data)
              }
              this.saveLoginInfo(token)
              this.$message.success('管理员登录成功')
              this.$router.push('/home')
            } else {
              this.$message.error(res.msg || '登录失败')
            }
          } else {
            // 普通用户登录
            const res = await this.$axios.post('/user/login', this.loginForm)
            if (res.code === 200) {
              const token = res.data
              this.$store.commit('SET_TOKEN', token)
              this.$store.commit('SET_LOGIN_TYPE', 'user')
              const userRes = await this.$axios.get('/user/info')
              if (userRes.code === 200) {
                this.$store.commit('SET_USER_INFO', userRes.data)
              }
              this.saveLoginInfo(token)
              this.$message.success('登录成功')
              this.$router.push('/home')
            } else {
              this.$message.error(res.msg || '登录失败')
            }
          }
        } catch (e) {
          this.$message.error('登录失败')
        }
        this.loading = false
      })
    }
  }
}
</script>

<style scoped>
/* 登录页容器 - 渐变动画背景 */
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background: linear-gradient(-45deg, #667eea, #764ba2, #f093fb, #f5576c); background-size: 400% 400%; position: relative; overflow: hidden; animation: gradientFlow 15s ease infinite; }
/* 背景渐变流动动画 */
@keyframes gradientFlow { 0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; } }
/* 背景光晕脉冲效果 */
.login-container::before { content: ''; position: absolute; top: -50%; left: -50%; width: 200%; height: 200%; background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%); animation: loginBgPulse 8s ease-in-out infinite; }
@keyframes loginBgPulse { 0%, 100% { transform: scale(1); opacity: 0.5; } 50% { transform: scale(1.1); opacity: 1; } }
/* 登录卡片样式 */
.login-box { width: 420px; padding: 48px 40px; background: rgba(255,255,255,0.92); border-radius: 24px; box-shadow: 0 25px 80px rgba(0,0,0,0.2); backdrop-filter: blur(20px); position: relative; z-index: 1; border: 1px solid rgba(255,255,255,0.3); }
/* 标题样式及底部装饰线 */
.login-box h2 { text-align: center; margin-bottom: 32px; color: #303133; font-size: 26px; font-weight: 700; letter-spacing: 1px; }
.login-box h2::after { content: ''; display: block; width: 50px; height: 4px; background: linear-gradient(135deg, #667eea, #764ba2); margin: 14px auto 0; border-radius: 2px; }
/* 后台管理系统入口链接 */
.admin-entry { text-align: center; margin-top: 16px; }
.admin-entry a { color: #909399; font-size: 13px; text-decoration: none; transition: color 0.3s; }
.admin-entry a:hover { color: #409EFF; }
.admin-entry i { margin-right: 4px; }
/* Element UI组件样式覆盖 */
.login-box >>> .el-radio-button__inner { border-radius: 8px !important; }
.login-box >>> .el-input__inner { border-radius: 10px !important; height: 44px; }
.login-box >>> .el-button { border-radius: 10px !important; height: 44px; font-size: 15px; font-weight: 600; }
</style>
