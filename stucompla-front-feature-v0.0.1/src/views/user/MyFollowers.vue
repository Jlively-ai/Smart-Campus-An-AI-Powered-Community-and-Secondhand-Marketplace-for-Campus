<!--
  组件名：MyFollowers
  功能描述：我的粉丝页
  主要职责：
    1. 粉丝列表展示
    2. 关注/取消关注操作
-->
<template>
  <el-card>
    <div slot="header"><span>我的粉丝</span></div>
    <div v-for="item in list" :key="item.followId" style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #eee;">
      <el-avatar v-if="item.avatar" :src="item.avatar" :size="40" style="margin-right:12px;cursor:pointer;" @click="$router.push('/userProfile/' + item.userId).catch(() => {})"></el-avatar>
      <el-avatar v-else :size="40" style="margin-right:12px;background:#409EFF;cursor:pointer;" @click="$router.push('/userProfile/' + item.userId).catch(() => {})">{{ (item.nickname || item.username || '用')[0] }}</el-avatar>
      <div style="flex:1;">
        <div style="font-weight:bold;cursor:pointer;" @click="$router.push('/userProfile/' + item.userId).catch(() => {})">{{ item.nickname || item.username || '用户' }}</div>
        <div style="color:#999;font-size:12px;">关注时间：{{ formatTime(item.createTime) }}</div>
      </div>
      <div style="display:flex;gap:8px;align-items:center;">
        <el-button size="small" :type="isFollowing(item.userId) ? 'default' : 'primary'" plain @click="followBack(item.userId)">{{ isFollowing(item.userId) ? '已关注' : '回关' }}</el-button>
        <el-button size="small" type="success" plain @click="openDM(item)">私信</el-button>
      </div>
    </div>
    <el-empty v-if="list.length === 0" description="暂无粉丝"></el-empty>
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
  name: 'MyFollowers',
  data() {
    return {
      list: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      followingIds: [],
      dmDialogVisible: false,
      dmTarget: {},
      dmLetterList: [],
      dmContent: '',
      dmSessionId: ''
    }
  },
  computed: { userInfo() { return this.$store.getters.getUserInfo || {} } },
  created() { this.loadData(); this.loadFollowingIds() },
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
      const res = await this.$axios.get('/follow/followers/' + userId, { params: { pageNum: this.pageNum, pageSize: this.pageSize } })
      if (res.code === 200) { this.list = res.data.records || []; this.total = res.data.total || 0 }
    },
    async loadFollowingIds() {
      const userId = this.userInfo.userId || this.userInfo.adminId
      if (!userId) return
      const res = await this.$axios.get('/follow/following/' + userId, { params: { pageNum: 1, pageSize: 9999 } })
      if (res.code === 200) this.followingIds = (res.data.records || []).map(r => r.userId)
    },
    isFollowing(userId) { return this.followingIds.includes(userId) },
    async followBack(userId) {
      if (this.isFollowing(userId)) {
        await this.$axios.delete('/follow/cancel/' + userId)
        this.$message.success('已取消关注')
      } else {
        await this.$axios.post('/follow/add/' + userId)
        this.$message.success('关注成功')
      }
      this.loadFollowingIds()
    },
    async openDM(item) {
      this.dmTarget = item
      const myId = String(this.userInfo.userId || this.userInfo.adminId)
      const otherId = String(item.userId)
      const minId = myId < otherId ? myId : otherId
      const maxId = myId >= otherId ? myId : otherId
      this.dmSessionId = minId + '_' + maxId
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
        const myId = String(this.userInfo.userId || this.userInfo.adminId)
        const msgRes = await this.$axios.get('/letter/letterList/' + this.dmSessionId)
        if (msgRes.code === 200) {
          this.dmLetterList = (msgRes.data || []).map(l => ({ ...l, isMine: String(l.senderId) === myId }))
        }
        this.$nextTick(() => {
          const box = this.$refs.dmChatBox
          if (box) box.scrollTop = box.scrollHeight
        })
      }
    },
    handlePageChange(val) { this.pageNum = val; this.loadData() },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadData() }
  }
}
</script>

<style scoped>
.follower-user-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  border-radius: 8px;
  transition: all 0.3s ease;
}
.follower-user-item:hover {
  background: #f5f7fa;
  transform: translateY(-2px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.follower-user-item:last-child { border-bottom: none; }
</style>
