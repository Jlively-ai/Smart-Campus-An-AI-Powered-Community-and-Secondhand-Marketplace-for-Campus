<!--
  组件名：SecurityCenter
  功能描述：安全中心页
  主要职责：
    1. 修改密码
    2. 账号安全设置
-->
<template>
  <div>
    <el-card>
      <div slot="header"><span>安全中心</span></div>
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form ref="pwdForm" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width:500px;">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码"></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="inPassword">
              <el-input v-model="pwdForm.inPassword" type="password" placeholder="请输入新密码"></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="secondPassword">
              <el-input v-model="pwdForm.secondPassword" type="password" placeholder="请确认新密码"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="changePwd" :loading="pwdLoading">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 处罚查询 -->
        <el-tab-pane label="处罚查询" name="punishment">
          <el-table :data="punishmentList" border stripe style="width:100%;" v-loading="punishmentLoading">
            <el-table-column label="处罚ID" width="120">
              <template slot-scope="scope">
                {{ scope.row.punishmentId | formatId('punishment') }}
              </template>
            </el-table-column>
            <el-table-column label="处罚类型" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.type === 'mute' ? 'warning' : scope.row.type === 'ban' ? 'danger' : 'info'">{{ { mute: '禁言', ban: '封号', warning: '警告' }[scope.row.type] || scope.row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="处罚原因"></el-table-column>
            <el-table-column label="处罚时间" width="160">
              <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
            </el-table-column>
            <el-table-column label="处罚状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">{{ { 0: '生效中', 1: '已解除', 2: '已过期' }[scope.row.status] || '未知' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="申诉状态" width="120">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.appealState === 0" type="warning">待审核</el-tag>
                <el-tag v-else-if="scope.row.appealState === 1" type="success">已通过</el-tag>
                <el-tag v-else-if="scope.row.appealState === 2" type="danger">已驳回</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 举报查询 -->
        <el-tab-pane label="举报查询" name="report">
          <el-table :data="reportList" border stripe style="width:100%;" v-loading="reportLoading">
            <el-table-column label="举报ID" width="120">
              <template slot-scope="scope">
                {{ scope.row.reportId | formatId('report') }}
              </template>
            </el-table-column>
            <el-table-column prop="targetType" label="目标类型"></el-table-column>
            <el-table-column prop="reportReason" label="举报原因"></el-table-column>
            <el-table-column prop="reportTime" label="举报时间"></el-table-column>
            <el-table-column prop="handleState" label="处理状态"></el-table-column>
          </el-table>
          <div v-if="reportError" style="text-align:center;color:#909399;padding:20px;">暂无数据</div>
        </el-tab-pane>

        <!-- 处罚申诉 -->
        <el-tab-pane label="处罚申诉" name="appeal">
          <el-table :data="appealableList" border stripe style="width:100%;" v-loading="appealLoading">
            <el-table-column label="处罚ID" width="120">
              <template slot-scope="scope">
                {{ scope.row.punishmentId | formatId('punishment') }}
              </template>
            </el-table-column>
            <el-table-column label="处罚类型" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.type === 'mute' ? 'warning' : scope.row.type === 'ban' ? 'danger' : 'info'">{{ { mute: '禁言', ban: '封号', warning: '警告' }[scope.row.type] || scope.row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="处罚原因"></el-table-column>
            <el-table-column label="处罚时间" width="160">
              <template slot-scope="scope">{{ scope.row.createTime | formatTime }}</template>
            </el-table-column>
            <el-table-column label="申诉状态" width="120">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.appealState === 0" type="warning">待审核</el-tag>
                <el-tag v-else-if="scope.row.appealState === 1" type="success">已通过</el-tag>
                <el-tag v-else-if="scope.row.appealState === 2" type="danger">已驳回</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button type="primary" size="small" @click="openAppealDialog(scope.row)">申诉</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 账号注销 -->
        <el-tab-pane v-if="!isAdmin" label="账号注销" name="deactivate">
          <div style="max-width:500px;">
            <el-alert type="error" :closable="false" show-icon style="margin-bottom:15px;">
              <template slot="title">注销账号后，您的所有数据将被清除且无法恢复，昵称将显示为'用户已注销'。此操作不可逆！</template>
            </el-alert>
            <el-button type="danger" @click="deactivateAccount">注销账号</el-button>
          </div>
        </el-tab-pane>

        <!-- 隐私设置 -->
        <el-tab-pane label="隐私设置" name="privacy">
          <div style="max-width:700px;">
            <div v-for="item in privacyItems" :key="item.key" style="padding:16px 0;border-bottom:1px solid #ebeef5;">
              <div style="display:flex;justify-content:space-between;align-items:center;">
                <div>
                  <div style="font-weight:bold;font-size:15px;">{{ item.label }}</div>
                  <div style="color:#909399;font-size:13px;margin-top:4px;">{{ item.desc }}</div>
                </div>
                <el-select v-model="privacy[item.key]" size="small" style="width:140px;" @change="onVisibilityChange(item.key)">
                  <el-option label="所有人" value="all"></el-option>
                  <el-option label="仅关注的人" value="following"></el-option>
                  <el-option label="互相关注" value="mutual"></el-option>
                  <el-option label="仅自己" value="self"></el-option>
                  <el-option label="不给谁看" value="custom"></el-option>
                </el-select>
              </div>
              <div v-if="privacy[item.key] === 'custom'" style="margin-top:12px;">
                <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
                  <el-tag v-for="uid in getBlockedUsers(item.key)" :key="uid" closable size="small" @close="removeBlockedUser(item.key, uid)">
                    {{ getBlockedUserName(uid) }}
                  </el-tag>
                  <el-button type="primary" size="mini" icon="el-icon-plus" @click="openBlockDialog(item.key)">添加用户</el-button>
                </div>
                <div style="color:#909399;font-size:12px;margin-top:6px;">已屏蔽 {{ getBlockedUsers(item.key).length }} 人，这些用户将无法查看你的{{ item.label }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 申诉对话框 -->
    <el-dialog title="处罚申诉" :visible.sync="appealDialogVisible" width="500px">
      <el-form label-width="100px">
        <el-form-item label="处罚ID">
          <span>{{ currentAppeal.punishmentId | formatId('punishment') }}</span>
        </el-form-item>
        <el-form-item label="申诉理由">
          <el-input type="textarea" v-model="appealReason" :rows="4" placeholder="请输入申诉理由"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="appealDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAppeal" :loading="appealSubmitting">提交申诉</el-button>
      </span>
    </el-dialog>

    <!-- 屏蔽用户选择对话框 -->
    <el-dialog title="选择不给谁看" :visible.sync="blockDialogVisible" width="500px" append-to-body>
      <div v-loading="blockDialogLoading">
        <el-input v-model="blockSearchKeyword" placeholder="搜索用户昵称" prefix-icon="el-icon-search" size="small" style="margin-bottom:12px;" @input="searchBlockUsers"></el-input>
        <div style="max-height:300px;overflow-y:auto;">
          <div v-for="user in blockSearchResults" :key="user.userId" style="display:flex;align-items:center;padding:8px 0;border-bottom:1px solid #f0f0f0;">
            <el-avatar :size="32" :src="user.avatar" style="background:#409EFF;">{{ (user.nickname || user.username || '用')[0] }}</el-avatar>
            <span style="margin-left:10px;flex:1;">{{ user.nickname || user.username || '用户' }}<span style="margin-left:6px;font-size:12px;color:#909399;">USR-{{ user.userId }}</span></span>
            <el-button v-if="!isAlreadyBlocked(currentBlockField, user.userId)" type="primary" size="mini" @click="addBlockedUser(currentBlockField, user.userId)">屏蔽</el-button>
            <el-tag v-else size="mini" type="info">已屏蔽</el-tag>
          </div>
          <div v-if="blockSearchResults.length === 0" style="text-align:center;color:#999;padding:20px 0;">输入昵称搜索用户</div>
        </div>
      </div>
      <span slot="footer"><el-button @click="blockDialogVisible = false">关闭</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: 'SecurityCenter',
  data() {
    const validatePass = (rule, value, callback) => {
      if (value !== this.pwdForm.inPassword) callback(new Error('两次密码不一致'))
      else callback()
    }
    return {
      activeTab: 'password',
      // 修改密码
      pwdForm: { oldPassword: '', inPassword: '', secondPassword: '' },
      pwdRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        inPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
        secondPassword: [{ required: true, validator: validatePass, trigger: 'blur' }]
      },
      pwdLoading: false,
      // 处罚查询
      punishmentList: [],
      punishmentLoading: false,
      // 举报查询
      reportList: [],
      reportLoading: false,
      reportError: false,
      // 处罚申诉
      appealableList: [],
      appealLoading: false,
      appealDialogVisible: false,
      currentAppeal: {},
      appealReason: '',
      appealSubmitting: false,
      // 隐私设置
      privacy: { following: 'all', followers: 'all', likes: 'all', collect: 'all', posts: 'all', goods: 'all' },
      blockedUsers: { following: [], followers: [], likes: [], collect: [], posts: [], goods: [] },
      userNamesMap: {},
      privacyItems: [
        { key: 'following', label: '关注列表', desc: '控制谁可以查看你的关注列表' },
        { key: 'followers', label: '粉丝列表', desc: '控制谁可以查看你的粉丝列表' },
        { key: 'likes', label: '喜欢列表', desc: '控制谁可以查看你喜欢的帖子' },
        { key: 'collect', label: '收藏列表', desc: '控制谁可以查看你的收藏' },
        { key: 'posts', label: '帖子列表', desc: '控制谁可以查看你发布的帖子' },
        { key: 'goods', label: '商品列表', desc: '控制谁可以查看你发布的商品' }
      ],
      blockDialogVisible: false,
      blockDialogLoading: false,
      blockSearchKeyword: '',
      blockSearchResults: [],
      currentBlockField: ''
    }
  },
  watch: {
    activeTab(val) {
      if (val === 'punishment') this.loadPunishments()
      else if (val === 'report') this.loadReports()
      else if (val === 'appeal') this.loadAppealable()
      else if (val === 'privacy') this.loadPrivacy()
    }
  },
  computed: {
    isAdmin() { return this.$store.getters.getLoginType === 'admin' }
  },
  methods: {
    // 修改密码
    changePwd() {
      this.$refs.pwdForm.validate(async valid => {
        if (!valid) return
        this.pwdLoading = true
        var params = new URLSearchParams()
        params.append('oldPassword', this.pwdForm.oldPassword)
        params.append('inPassword', this.pwdForm.inPassword)
        params.append('secondPassword', this.pwdForm.secondPassword)
        var isAdmin = this.$store.getters.getLoginType === 'admin'
        var url = isAdmin ? '/admin/info/changePassword' : '/user/changePassword'
        var res = await this.$axios.post(url, params)
        if (res.code === 200) {
          this.$message.success('密码修改成功，请重新登录')
          this.$store.commit('CLEAR_USER')
          this.$router.push('/login').catch(function() {})
        } else {
          this.$message.error(res.msg || '修改失败')
        }
        this.pwdLoading = false
      })
    },
    // 处罚查询
    async loadPunishments() {
      var userInfo = this.$store.getters.getUserInfo
      var uid = userInfo && (userInfo.userId || userInfo.adminId)
      if (!uid) return
      this.punishmentLoading = true
      try {
        var res = await this.$axios.get('/punishment/my', { params: { userId: uid } })
        if (res.code === 200) {
          this.punishmentList = res.data || []
        } else {
          this.punishmentList = []
        }
      } catch (e) {
        this.punishmentList = []
      }
      this.punishmentLoading = false
    },
    // 举报查询
    async loadReports() {
      var userInfo = this.$store.getters.getUserInfo
      var uid = userInfo && (userInfo.userId || userInfo.adminId)
      if (!uid) return
      this.reportLoading = true
      this.reportError = false
      try {
        var res = await this.$axios.get('/report/my', { params: { userId: uid } })
        if (res.code === 200) {
          this.reportList = res.data || []
        } else {
          this.reportList = []
          this.reportError = true
        }
      } catch (e) {
        this.reportList = []
        this.reportError = true
      }
      this.reportLoading = false
    },
    // 处罚申诉 - 加载可申诉的处罚
    async loadAppealable() {
      var userInfo = this.$store.getters.getUserInfo
      var uid = userInfo && (userInfo.userId || userInfo.adminId)
      if (!uid) return
      this.appealLoading = true
      try {
        var res = await this.$axios.get('/punishment/my', { params: { userId: uid } })
        if (res.code === 200) {
          var list = res.data || []
          this.appealableList = list.filter(function(item) {
            return item.status === 0 && (item.appealState === null || item.appealState === undefined || item.appealState === 2)
          })
        } else {
          this.appealableList = []
        }
      } catch (e) {
        this.appealableList = []
      }
      this.appealLoading = false
    },
    // 打开申诉对话框
    openAppealDialog(row) {
      this.currentAppeal = row
      this.appealReason = ''
      this.appealDialogVisible = true
    },
    // 提交申诉
    async submitAppeal() {
      if (!this.appealReason.trim()) {
        this.$message.warning('请输入申诉理由')
        return
      }
      this.appealSubmitting = true
      try {
        var res = await this.$axios.post('/punishment/appeal', {
          punishmentId: this.currentAppeal.punishmentId,
          appealReason: this.appealReason
        })
        if (res.code === 200) {
          this.$message.success('申诉提交成功')
          this.appealDialogVisible = false
          this.loadAppealable()
        } else {
          this.$message.error(res.msg || '申诉提交失败')
        }
      } catch (e) {
        this.$message.error('申诉提交失败')
      }
      this.appealSubmitting = false
    },
    // 账号注销
    deactivateAccount() {
      this.$confirm('确定要注销账号吗？此操作不可逆，所有数据将被清除！', '警告', {
        type: 'error',
        confirmButtonText: '确定注销',
        cancelButtonText: '取消'
      }).then(async function() {
        var res = await this.$axios.post('/user/deactivate')
        if (res.code === 200) {
          this.$message.success('账号已注销')
          this.$store.commit('CLEAR_USER')
          this.$router.push('/login').catch(function() {})
        } else {
          this.$message.error(res.msg || '注销失败')
        }
      }.bind(this)).catch(function() {})
    },
    // 隐私设置
    async loadPrivacy() {
      try {
        var res = await this.$axios.get('/privacy/my')
        if (res.code === 200 && res.data) {
          this.privacy = Object.assign({}, this.privacy, res.data)
          if (res.data.blockedUsers) {
            try {
              var parsed = typeof res.data.blockedUsers === 'string' ? JSON.parse(res.data.blockedUsers) : res.data.blockedUsers
              this.blockedUsers = Object.assign({}, this.blockedUsers, parsed)
            } catch (e) {}
          }
        }
        // 不再预加载所有用户，改用 /user/search 按需搜索
      } catch (e) {}
    },
    onVisibilityChange(field) {
      if (this.privacy[field] !== 'custom') {
        this.blockedUsers[field] = []
      }
      this.savePrivacy()
    },
    getBlockedUsers(field) {
      return this.blockedUsers[field] || []
    },
    getBlockedUserName(uid) {
      return this.userNamesMap[String(uid)] || uid
    },
    isAlreadyBlocked(field, uid) {
      var list = this.blockedUsers[field] || []
      return list.some(function(id) { return String(id) === String(uid) })
    },
    addBlockedUser(field, uid) {
      if (!this.blockedUsers[field]) this.blockedUsers[field] = []
      if (!this.isAlreadyBlocked(field, uid)) {
        this.blockedUsers[field].push(uid)
        this.savePrivacy()
      }
    },
    removeBlockedUser(field, uid) {
      var list = this.blockedUsers[field] || []
      this.blockedUsers[field] = list.filter(function(id) { return String(id) !== String(uid) })
      this.savePrivacy()
    },
    openBlockDialog(field) {
      this.currentBlockField = field
      this.blockSearchKeyword = ''
      this.blockSearchResults = []
      this.blockDialogVisible = true
    },
    searchBlockUsers() {
      var keyword = this.blockSearchKeyword.trim()
      if (!keyword) { this.blockSearchResults = []; return }
      var self = this
      this.$axios.get('/user/search', { params: { keyword: keyword } }).then(function(res) {
        if (res.code === 200) {
          var users = res.data || []
          var myInfo = self.$store.getters.getUserInfo
          var myId = myInfo && (myInfo.userId || myInfo.adminId)
          self.blockSearchResults = users.filter(function(u) {
            return String(u.userId) !== String(myId)
          })
          users.forEach(function(u) { self.$set(self.userNamesMap, String(u.userId), u.nickname || u.username || u.userId) })
        }
      }).catch(function() {})
    },
    async savePrivacy() {
      try {
        var data = Object.assign({}, this.privacy, { blockedUsers: JSON.stringify(this.blockedUsers) })
        var res = await this.$axios.post('/privacy/update', data)
        if (res.code === 200) {
          this.$message.success('隐私设置已保存')
        }
      } catch (e) {
        this.$message.error('保存失败')
      }
    }
  }
}
</script>

<style scoped>
.privacy-item {
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
  transition: background 0.3s ease;
}
.privacy-item:last-child { border-bottom: none; }
</style>
