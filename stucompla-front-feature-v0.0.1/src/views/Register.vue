<!--
  组件名：Register
  功能描述：用户注册页面
  主要职责：
    1. 提供用户名/密码/确认密码/性别注册表单
    2. 自定义校验两次密码输入一致性
    3. 注册成功后跳转到登录页
-->
<template>
  <!-- 注册页容器 -->
  <div class="login-container">
    <div class="login-box">
      <h2>智联校园 - 注册</h2>
      <!-- 注册表单 -->
      <el-form ref="regForm" :model="regForm" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="regForm.username" prefix-icon="el-icon-user" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="regForm.password" prefix-icon="el-icon-lock" placeholder="请输入密码" type="password"></el-input>
        </el-form-item>
        <el-form-item prop="secondPassword">
          <el-input v-model="regForm.secondPassword" prefix-icon="el-icon-lock" placeholder="请确认密码" type="password"></el-input>
        </el-form-item>
        <!-- 性别选择 -->
        <el-form-item>
          <el-radio-group v-model="regForm.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width:100%" @click="register" :loading="loading">注 册</el-button>
        </el-form-item>
        <!-- 登录链接 -->
        <el-form-item>
          <router-link to="/login">已有账号？去登录</router-link>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    /** 自定义校验：确认密码必须与密码一致 */
    const validatePass = (rule, value, callback) => {
      if (value !== this.regForm.password) { callback(new Error('两次输入密码不一致')) } else { callback() }
    }
    return {
      /** 注册表单数据 */
      regForm: { username: '', password: '', secondPassword: '', sex: '男' },
      /** 表单校验规则 */
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        secondPassword: [{ required: true, validator: validatePass, trigger: 'blur' }]
      },
      /** 是否正在注册中 */
      loading: false
    }
  },
  methods: {
    /** 注册方法：校验表单后调用注册接口 */
    register() {
      this.$refs.regForm.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          const res = await this.$axios.post('/user/register', this.regForm)
          if (res === 'success') {
            this.$message.success('注册成功')
            this.$router.push('/login')
          } else {
            this.$message.error(res || '注册失败')
          }
        } catch (e) {
          this.$message.error('注册失败')
        }
        this.loading = false
      })
    }
  }
}
</script>

<style scoped>
/* 注册页容器 - 渐变动画背景 */
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background: linear-gradient(-45deg, #667eea, #764ba2, #f093fb, #f5576c); background-size: 400% 400%; position: relative; overflow: hidden; animation: gradientFlow 15s ease infinite; }
/* 背景渐变流动动画 */
@keyframes gradientFlow { 0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; } }
/* 背景光晕脉冲效果 */
.login-container::before { content: ''; position: absolute; top: -50%; left: -50%; width: 200%; height: 200%; background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%); animation: loginBgPulse 8s ease-in-out infinite; }
@keyframes loginBgPulse { 0%, 100% { transform: scale(1); opacity: 0.5; } 50% { transform: scale(1.1); opacity: 1; } }
/* 注册卡片样式 */
.login-box { width: 440px; padding: 52px 44px; background: rgba(255,255,255,0.92); border-radius: 28px; box-shadow: 0 32px 100px rgba(0,0,0,0.22), 0 0 0 1px rgba(255,255,255,0.4) inset; backdrop-filter: blur(24px) saturate(180%); -webkit-backdrop-filter: blur(24px) saturate(180%); position: relative; z-index: 1; border: 1px solid rgba(255,255,255,0.4); }
/* 标题样式及底部装饰线 */
.login-box h2 { text-align: center; margin-bottom: 36px; color: #303133; font-size: 28px; font-weight: 700; letter-spacing: 1.5px; }
.login-box h2::after { content: ''; display: block; width: 60px; height: 4px; background: linear-gradient(135deg, #667eea, #764ba2); margin: 16px auto 0; border-radius: 2px; }
/* Element UI组件样式覆盖 */
.login-box >>> .el-input__inner { border-radius: 12px !important; height: 48px; font-size: 14px; transition: all 0.3s ease; }
.login-box >>> .el-input__inner:hover { border-color: #c0c4cc; }
.login-box >>> .el-input__inner:focus { border-color: #409EFF; box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15); }
.login-box >>> .el-form-item { margin-bottom: 22px; }
.login-box >>> .el-radio-group { display: flex; justify-content: center; gap: 20px; }
.login-box >>> .el-radio__label { font-size: 14px; }
.login-box >>> .el-button { border-radius: 12px !important; height: 48px; font-size: 16px; font-weight: 600; letter-spacing: 2px; transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1); }
.login-box >>> .el-button:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(64, 158, 255, 0.4); }
.login-box >>> .el-button:active { transform: translateY(0); }
/* 登录链接样式 */
.login-box a { color: #409EFF; text-decoration: none; font-size: 14px; transition: color 0.3s ease; }
.login-box a:hover { color: #66b1ff; text-decoration: underline; }
</style>
