<!--
  组件名：MyFollowing
  功能描述：我的关注页
  主要职责：
    1. 关注列表展示
    2. 取消关注操作
-->
<template>
  <el-card>
    <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
      <span>我的关注</span>
      <div style="display:flex;gap:8px;align-items:center;">
        <el-input v-model="searchId" placeholder="输入用户ID搜索关注" size="small" style="width:220px;" @keyup.enter.native="searchAndFollow"></el-input>
        <el-button type="primary" size="small" @click="searchAndFollow">搜索关注</el-button>
      </div>
    </div>
    <div v-for="item in list" :key="item.followId" style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #eee;">
      <el-avatar v-if="item.avatar" :src="item.avatar" :size="40" style="margin-right:12px;cursor:pointer;" @click="$router.push('/userProfile/' + item.userId).catch(() => {})"></el-avatar>
      <el-avatar v-else :size="40" style="margin-right:12px;background:#409EFF;cursor:pointer;" @click="$router.push('/userProfile/' + item.userId).catch(() => {})">{{ (item.nickname || item.username || '用')[0] }}</el-avatar>
      <div style="flex:1;">
        <div style="display:flex;align-items:center;gap:6px;">
          <span style="font-weight:bold;cursor:pointer;" @click="$router.push('/userProfile/' + item.userId).catch(() => {})">{{ item.nickname || item.username || '用户' }}</span>
          <el-tag v-if="item.isMutual" size="mini" type="success">互相关注</el-tag>
        </div>
        <div style="color:#999;font-size:12px;">关注时间：{{ formatTime(item.createTime) }}</div>
      </div>
      <div style="display:flex;gap:8px;align-items:center;">
        <el-button v-if="!item.isMutual && item.followsMe" size="small" type="primary" plain @click="followBack(item)">回关</el-button>
        <el-button size="small" type="success" plain @click="openDM(item)">私信</el-button>
        <el-button size="small" type="danger" plain @click="unfollow(item.userId)">取消关注</el-button>
      </div>
    </div>
    <el-empty v-if="list.length === 0" description="暂无关注"></el-empty>
    <el-pagination style="margin-top:20px;text-align:center;" @current-change="handlePageChange" :current-page="pageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange"></el-pagination>

    <el-dialog :title="'与 ' + (dmTarget.nickname || dmTarget.username || '用户') + ' 的对话'" :visible.sync="dmDialogVisible" width="500px" @close="dmDialogVisible = false">
      <div style="max-height:400px;overflow-y:auto;" ref="dmChatBox">
        <div v-for="l in dmLetterList" :key="l.letterId" :style="{textAlign: l.isMine ? 'right' : 'left', margin: '10px 0'}">
          <el-tag :type="l.isMine ? 'primary' : 'success'" effect="light">{{ l.letterDetail }}</el-tag>
          <div style="font-size:11px;color:#999;margin-top:2px;">{{ l.createTime | formatTime }}</div>
        </div>
      </div>
      <div style="margin-top:10px;display:flex;gap:10px;">
        <el-input v-model="dmContent" placeholder="输入消息..." @keyup.enter.native="sendDM"></el-input>
        <el-button type="primary" @click="sendDM">发送</el-button>
      </div>
    </el-dialog>
  </el-card>
</template>

<script>
export default {
  name: 'MyFollowing',
  data() {
    return {
      list: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      dmDialogVisible: false,
      dmTarget: {},
      dmLetterList: [],
      dmContent: '',
      dmSessionId: '',
      searchId: ''
    }
  },
  computed: { userInfo() { return this.$store.getters.getUserInfo || {} } },
  created() { this.loadData() },
  methods: {
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const pad = n => String(n).padStart(2, '0')
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    },
    async loadData() {
      const userId = this.userInfo.userId || this.userInfo.adminId
      if (!userId) return
      const res = await this.$axios.get('/follow/following/' + userId, { params: { pageNum: this.pageNum, pageSize: this.pageSize } })
      if (res.code === 200) {
        const records = res.data.records || []
        // 检查对方是否关注了我
        for (const item of records) {
          try {
            const checkRes = await this.$axios.get('/follow/checkFollowsMe/' + item.userId)
            item.followsMe = checkRes.code === 200 && checkRes.data === true
            item.isMutual = item.followsMe
          } catch (e) {
            item.followsMe = false
            item.isMutual = false
          }
        }
        this.list = records
        this.total = res.data.total || 0
      }
    },
    async followBack(item) {
      try {
        await this.$axios.post('/follow/add/' + item.userId)
        this.$message.success('关注成功')
        item.isMutual = true
      } catch (e) {}
    },
    async unfollow(userId) {
      await this.$axios.delete('/follow/cancel/' + userId)
      this.$message.success('已取消关注')
      this.loadData()
    },
    async openDM(item) {
      this.dmTarget = item
      const myId = String(this.userInfo.userId || this.userInfo.adminId)
      const otherId = String(item.userId)
      const minId = myId < otherId ? myId : otherId
      const maxId = myId >= otherId ? myId : otherId
      this.dmSessionId = minId + '_' + maxId
      // 加载历史消息
      try {
        const res = await this.$axios.get('/letter/letterList/' + this.dmSessionId)
        if (res.code === 200) {
          this.dmLetterList = (res.data || []).map(l => ({ ...l, isMine: String(l.senderId) === myId }))
        }
      } catch (e) {
        this.dmLetterList = []
      }
      this.dmDialogVisible = true
      this.$nextTick(() => {
        const box = this.$refs.dmChatBox
        if (box) box.scrollTop = box.scrollHeight
      })
    },
    async sendDM() {
      if (!this.dmContent.trim()) return
      const res = await this.$axios.post('/letter/send', { receiverId: this.dmTarget.userId, letterDetail: this.dmContent })
      if (res.code === 200) {
        this.dmContent = ''
        const msgRes = await this.$axios.get('/letter/letterList/' + this.dmSessionId)
        if (msgRes.code === 200) {
          const myId = String(this.userInfo.userId || this.userInfo.adminId)
          this.dmLetterList = (msgRes.data || []).map(l => ({ ...l, isMine: String(l.senderId) === myId }))
        }
        this.$nextTick(() => {
          const box = this.$refs.dmChatBox
          if (box) box.scrollTop = box.scrollHeight
        })
      }
    },
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() },
    async searchAndFollow() {
      if (!this.searchId.trim()) return this.$message.warning('请输入用户ID')
      try {
        const res = await this.$axios.get('/user/publicInfo/' + this.searchId.trim())
        if (res.code === 200 && res.data) {
          const user = res.data
          if (user.deactivated) return this.$message.warning('该用户已注销')
          const myId = String(this.userInfo.userId || this.userInfo.adminId)
          if (String(user.userId) === myId) return this.$message.warning('不能关注自己')
          try {
            await this.$confirm('找到用户：' + (user.nickname || user.username || '未知') + '，是否关注？', '搜索结果', { confirmButtonText: '关注', cancelButtonText: '取消', type: 'info' })
            await this.$axios.post('/follow/add/' + user.userId)
            this.$message.success('关注成功')
            this.loadData()
          } catch {}
        } else {
          this.$message.warning('未找到该用户')
        }
      } catch {
        this.$message.warning('未找到该用户')
      }
    }
  }
}
</script>

<style scoped>
.follow-user-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  border-radius: 8px;
  transition: all 0.3s ease;
}
.follow-user-item:hover {
  background: #f5f7fa;
  transform: translateY(-2px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.follow-user-item:last-child { border-bottom: none; }
</style>
