<!--
  组件名：MyPunishment
  功能描述：处罚管理页
  主要职责：
    1. 三标签页（违规记录/处罚记录/举报记录）
    2. 筛选/排序
    3. 申诉功能
-->
<template>
  <div>
    <div style="margin-bottom:15px;">
      <el-page-header @back="$router.back()" content="处罚管理"></el-page-header>
    </div>
    <el-card shadow="hover">
      <el-tabs v-model="activeTab">
        <!-- 违规记录（合并违规删除+锁定） -->
        <el-tab-pane :label="'违规记录(' + combinedTotal + ')'" name="violation">
          <div style="margin-bottom:12px;display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
            <el-select v-model="vFilterCategory" placeholder="类型筛选" size="small" style="width:110px;" clearable @change="loadCombinedList">
              <el-option label="帖子" value="post"></el-option>
              <el-option label="帖子评论" value="comment"></el-option>
              <el-option label="商品" value="goods"></el-option>
              <el-option label="商品评价" value="goods_comment"></el-option>
              <el-option label="表白墙" value="wall"></el-option>
            </el-select>
            <el-select v-model="vFilterRecordType" placeholder="记录类型" size="small" style="width:110px;" clearable @change="loadCombinedList">
              <el-option label="违规删除" value="violation"></el-option>
              <el-option label="锁定" value="lock"></el-option>
            </el-select>
            <el-select v-model="vFilterAppeal" placeholder="申诉状态" size="small" style="width:110px;" clearable @change="loadCombinedList">
              <el-option label="未申诉" :value="0"></el-option>
              <el-option label="申诉中" :value="1"></el-option>
              <el-option label="已通过" :value="2"></el-option>
              <el-option label="已驳回" :value="3"></el-option>
            </el-select>
            <el-select v-model="vSortBy" placeholder="排序" size="small" style="width:110px;" @change="loadCombinedList">
              <el-option label="时间" value="createTime"></el-option>
              <el-option label="更新时间" value="updateTime"></el-option>
            </el-select>
            <el-select v-model="vSortOrder" placeholder="方向" size="small" style="width:90px;" @change="loadCombinedList">
              <el-option label="最新" value="desc"></el-option>
              <el-option label="最早" value="asc"></el-option>
            </el-select>
          </div>
          <el-table :data="combinedList" v-loading="combinedLoading" size="small">
            <el-table-column prop="_recordType" label="记录类型" width="90" align="center">
              <template slot-scope="scope">
                <el-tag size="mini" :type="scope.row._recordType === 'violation' ? 'danger' : 'warning'">
                  {{ scope.row._recordType === 'violation' ? '违规删除' : '锁定' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="itemType" label="类型" width="100" align="center">
              <template slot-scope="scope">
                <el-tag size="mini" :type="scope.row._baseItemType === 'post' ? 'primary' : scope.row._baseItemType === 'comment' ? '' : scope.row._baseItemType === 'goods' ? 'warning' : scope.row._baseItemType === 'goods_comment' ? 'success' : scope.row._baseItemType === 'wall' ? 'danger' : 'info'">
                  {{ scope.row._baseItemType === 'post' ? '帖子' : scope.row._baseItemType === 'comment' ? '帖子评论' : scope.row._baseItemType === 'goods' ? '商品' : scope.row._baseItemType === 'goods_comment' ? '商品评价' : scope.row._baseItemType === 'wall' ? '表白墙' : '其他' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="preview" label="内容预览" min-width="200">
              <template slot-scope="scope">
                <span style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap;display:inline-block;max-width:100%;">{{ scope.row.preview || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" width="150">
              <template slot-scope="scope">{{ scope.row.reason || '-' }}</template>
            </el-table-column>
            <el-table-column prop="appealState" label="申诉状态" width="90" align="center">
              <template slot-scope="scope">
                <el-tag size="mini" :type="scope.row.appealState === 0 ? 'info' : scope.row.appealState === 1 ? 'warning' : scope.row.appealState === 2 ? 'success' : 'danger'">
                  {{ ['未申诉', '申诉中', '已通过', '已驳回'][scope.row.appealState] || '未申诉' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" min-width="150" align="center">
              <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template slot-scope="scope">
                <el-button v-if="scope.row.appealState === 0 || scope.row.appealState === null" type="text" size="mini" @click="openCombinedAppealDialog(scope.row)">申诉</el-button>
                <el-tag v-else-if="scope.row.appealState === 1" size="mini" type="warning">处理中</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="combinedTotal > combinedPageSize" style="margin-top:10px;text-align:center;" @current-change="function(p) { combinedPageNum = p; loadCombinedList() }" :current-page="combinedPageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="combinedPageSize" :total="combinedTotal" layout="total, sizes, prev, pager, next" small @size-change="handleCombinedSizeChange"></el-pagination>
        </el-tab-pane>

        <!-- 处罚记录 -->
        <el-tab-pane :label="'处罚记录(' + pFilteredTotal + ')'" name="punishment">
          <div style="margin-bottom:12px;display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
            <el-select v-model="pFilterType" placeholder="处罚类型" size="small" style="width:110px;" clearable @change="pPageNum=1">
              <el-option label="禁言" value="mute"></el-option>
              <el-option label="封号" value="ban"></el-option>
              <el-option label="警告" value="warn"></el-option>
            </el-select>
            <el-select v-model="pFilterStatus" placeholder="状态筛选" size="small" style="width:110px;" clearable @change="pPageNum=1">
              <el-option label="生效中" :value="0"></el-option>
              <el-option label="已解除" :value="1"></el-option>
              <el-option label="已过期" :value="2"></el-option>
            </el-select>
            <el-select v-model="pSortBy" placeholder="排序" size="small" style="width:140px;" @change="pPageNum=1">
              <el-option label="开始时间最新" value="startTimeDesc"></el-option>
              <el-option label="开始时间最早" value="startTimeAsc"></el-option>
              <el-option label="结束时间最近" value="endTimeAsc"></el-option>
              <el-option label="结束时间最远" value="endTimeDesc"></el-option>
            </el-select>
          </div>
          <el-table :data="filteredPunishments" v-loading="pLoading" size="small">
            <el-table-column prop="type" label="类型" width="80" align="center">
              <template slot-scope="scope">
                <el-tag size="mini" :type="scope.row.type === 'mute' ? 'warning' : scope.row.type === 'ban' ? 'danger' : 'info'">
                  {{ scope.row.type === 'mute' ? '禁言' : scope.row.type === 'ban' ? '封号' : '警告' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" min-width="200"></el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template slot-scope="scope">
                <el-tag size="mini" :type="scope.row.status === 0 ? 'danger' : scope.row.status === 1 ? 'success' : 'info'">
                  {{ scope.row.status === 0 ? '生效中' : scope.row.status === 1 ? '已解除' : '已过期' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="150" align="center">
              <template slot-scope="scope">{{ formatTime(scope.row.startTime) }}</template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="150" align="center">
              <template slot-scope="scope">{{ formatTime(scope.row.endTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template slot-scope="scope">
                <el-button v-if="scope.row.status === 0 && scope.row.appealState == null" type="text" size="mini" @click="openPunishmentAppeal(scope.row)">申诉</el-button>
                <el-tag v-else-if="scope.row.appealState === 0" size="mini" type="warning">申诉中</el-tag>
                <el-tag v-else-if="scope.row.appealState === 1" size="mini" type="success">已通过</el-tag>
                <el-tag v-else-if="scope.row.appealState === 2" size="mini" type="danger">已驳回</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="pFilteredTotal > pPageSize" style="margin-top:10px;text-align:center;" @current-change="function(p) { pPageNum = p }" :current-page="pPageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="pPageSize" :total="pFilteredTotal" layout="total, sizes, prev, pager, next" small @size-change="handlePSizeChange"></el-pagination>
        </el-tab-pane>

        <!-- 举报记录 -->
        <el-tab-pane :label="'举报记录(' + rFilteredTotal + ')'" name="report">
          <div style="margin-bottom:12px;display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
            <el-select v-model="rFilterType" placeholder="举报类型" size="small" style="width:110px;" clearable @change="rPageNum=1">
              <el-option label="帖子" value="post"></el-option>
              <el-option label="帖子评论" value="comment"></el-option>
              <el-option label="商品" value="goods"></el-option>
              <el-option label="商品评价" value="goods_comment"></el-option>
              <el-option label="表白墙" value="wall"></el-option>
            </el-select>
            <el-select v-model="rFilterStatus" placeholder="状态筛选" size="small" style="width:110px;" clearable @change="rPageNum=1">
              <el-option label="待处理" :value="0"></el-option>
              <el-option label="已处理" :value="1"></el-option>
              <el-option label="已驳回" :value="2"></el-option>
            </el-select>
            <el-select v-model="rSortBy" placeholder="排序" size="small" style="width:140px;" @change="rPageNum=1">
              <el-option label="举报时间最新" value="createTimeDesc"></el-option>
              <el-option label="举报时间最早" value="createTimeAsc"></el-option>
            </el-select>
          </div>
          <el-table :data="filteredReports" v-loading="rLoading" size="small">
            <el-table-column prop="targetType" label="举报类型" width="90" align="center">
              <template slot-scope="scope">
                <el-tag size="mini">{{ scope.row.targetType === 'post' ? '帖子' : scope.row.targetType === 'comment' ? '帖子评论' : scope.row.targetType === 'goods' ? '商品' : scope.row.targetType === 'goods_comment' ? '商品评价' : scope.row.targetType === 'wall' ? '表白墙' : '其他' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="举报原因" min-width="200"></el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template slot-scope="scope">
                <el-tag size="mini" :type="scope.row.status === 0 ? 'warning' : scope.row.status === 1 ? 'success' : 'info'">
                  {{ scope.row.status === 0 ? '待处理' : scope.row.status === 1 ? '已处理' : '已驳回' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="handleResult" label="处理结果" width="150">
              <template slot-scope="scope">{{ scope.row.handleResult || '-' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="举报时间" width="150" align="center">
              <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
            </el-table-column>
          </el-table>
          <el-pagination v-if="rFilteredTotal > rPageSize" style="margin-top:10px;text-align:center;" @current-change="function(p) { rPageNum = p }" :current-page="rPageNum" :page-sizes="[5, 10, 20, 50]" :page-size.sync="rPageSize" :total="rFilteredTotal" layout="total, sizes, prev, pager, next" small @size-change="handleRSizeChange"></el-pagination>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 违规记录申诉弹窗（合并） -->
    <el-dialog :title="appealTarget._recordType === 'lock' ? '锁定记录申诉' : '违规删除申诉'" :visible.sync="appealDialogVisible" width="450px">
      <el-form label-width="80px">
        <el-form-item label="记录类型">{{ appealTarget._recordType === 'lock' ? '锁定' : '违规删除' }}</el-form-item>
        <el-form-item label="内容类型">{{ appealTarget._baseItemType === 'post' ? '帖子' : appealTarget._baseItemType === 'comment' ? '帖子评论' : appealTarget._baseItemType === 'goods' ? '商品' : appealTarget._baseItemType === 'goods_comment' ? '商品评价' : appealTarget._baseItemType === 'wall' ? '表白墙' : '其他' }}</el-form-item>
        <el-form-item label="内容预览">{{ appealTarget.preview || '-' }}</el-form-item>
        <el-form-item :label="appealTarget._recordType === 'lock' ? '锁定原因' : '删除原因'">{{ appealTarget.reason || '-' }}</el-form-item>
        <el-form-item label="申诉理由">
          <el-input v-model="appealReason" type="textarea" :rows="4" placeholder="请说明申诉理由"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="appealDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCombinedAppeal">提交申诉</el-button>
      </span>
    </el-dialog>

    <!-- 处罚申诉弹窗 -->
    <el-dialog title="处罚申诉" :visible.sync="punishmentAppealVisible" width="450px">
      <el-form label-width="80px">
        <el-form-item label="处罚类型">{{ punishmentAppealTarget.type === 'mute' ? '禁言' : punishmentAppealTarget.type === 'ban' ? '封号' : '警告' }}</el-form-item>
        <el-form-item label="处罚原因">{{ punishmentAppealTarget.reason || '-' }}</el-form-item>
        <el-form-item label="申诉理由">
          <el-input v-model="punishmentAppealReason" type="textarea" :rows="4" placeholder="请说明申诉理由"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="punishmentAppealVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPunishmentAppeal">提交申诉</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'MyPunishment',
  data() {
    return {
      activeTab: 'violation',
      // Combined violation + lock records
      combinedList: [], combinedTotal: 0, combinedPageNum: 1, combinedPageSize: 10, combinedLoading: false,
      vFilterCategory: '', vFilterRecordType: '', vFilterAppeal: null, vSortBy: 'createTime', vSortOrder: 'desc',
      appealDialogVisible: false, appealTarget: {}, appealReason: '',
      // Punishment
      pAllList: [], pLoading: false,
      pFilterType: '', pFilterStatus: null, pSortBy: 'startTimeDesc',
      pPageNum: 1, pPageSize: 10,
      punishmentAppealVisible: false, punishmentAppealTarget: {}, punishmentAppealReason: '',
      // Report
      rAllList: [], rLoading: false,
      rFilterType: '', rFilterStatus: null, rSortBy: 'createTimeDesc',
      rPageNum: 1, rPageSize: 10
    }
  },
  watch: {
    activeTab: function(val) {
      if (val === 'violation') { this.loadCombinedList(); this.markTabNotificationsRead('violation'); this.markTabNotificationsRead('lock') }
      else if (val === 'punishment') { this.loadPunishments(); this.markTabNotificationsRead('punishment') }
      else if (val === 'report') { this.loadReports(); this.markTabNotificationsRead('report') }
    }
  },
  computed: {
    pFilteredAll: function() {
      var list = this.pAllList.slice()
      if (this.pFilterType) {
        list = list.filter(function(item) { return item.type === this.pFilterType }.bind(this))
      }
      if (this.pFilterStatus !== null && this.pFilterStatus !== '') {
        list = list.filter(function(item) { return item.status === this.pFilterStatus }.bind(this))
      }
      var sortBy = this.pSortBy
      list.sort(function(a, b) {
        var field, order
        if (sortBy === 'startTimeDesc') { field = 'startTime'; order = -1 }
        else if (sortBy === 'startTimeAsc') { field = 'startTime'; order = 1 }
        else if (sortBy === 'endTimeAsc') { field = 'endTime'; order = 1 }
        else if (sortBy === 'endTimeDesc') { field = 'endTime'; order = -1 }
        else { field = 'startTime'; order = -1 }
        var ta = a[field] ? new Date(a[field]).getTime() : 0
        var tb = b[field] ? new Date(b[field]).getTime() : 0
        return (ta - tb) * order
      })
      return list
    },
    pFilteredTotal: function() { return this.pFilteredAll.length },
    filteredPunishments: function() {
      var start = (this.pPageNum - 1) * this.pPageSize
      return this.pFilteredAll.slice(start, start + this.pPageSize)
    },
    rFilteredAll: function() {
      var list = this.rAllList.slice()
      if (this.rFilterType) {
        list = list.filter(function(item) { return item.targetType === this.rFilterType }.bind(this))
      }
      if (this.rFilterStatus !== null && this.rFilterStatus !== '') {
        list = list.filter(function(item) { return item.status === this.rFilterStatus }.bind(this))
      }
      var sortBy = this.rSortBy
      list.sort(function(a, b) {
        var order = sortBy === 'createTimeAsc' ? 1 : -1
        var ta = a.createTime ? new Date(a.createTime).getTime() : 0
        var tb = b.createTime ? new Date(b.createTime).getTime() : 0
        return (ta - tb) * order
      })
      return list
    },
    rFilteredTotal: function() { return this.rFilteredAll.length },
    filteredReports: function() {
      var start = (this.rPageNum - 1) * this.rPageSize
      return this.rFilteredAll.slice(start, start + this.rPageSize)
    }
  },
  created() { this.loadCombinedList(); this.markPunishmentNotificationsRead() },
  methods: {
    async markPunishmentNotificationsRead() {
      // 标记所有处罚/违规相关的未读通知为已读
      await this.markTabNotificationsRead('violation')
      await this.markTabNotificationsRead('lock')
      await this.markTabNotificationsRead('punishment')
      await this.markTabNotificationsRead('report')
    },
    async markTabNotificationsRead(tabType) {
      // 标记指定tab类型的未读通知为已读
      // sessionId格式: system_{userId}_violation_{id} / system_{userId}_punishment_{id} / system_{userId}_report_{id}
      try {
        var allMarked = false
        var page = 1
        while (!allMarked) {
          var res = await this.$axios.get('/letter/myMessageList', { params: { type: 'system', pageNum: page, pageSize: 100 } })
          if (res.code === 200 && res.data && res.data.records) {
            var self = this
            // 根据tabType匹配sessionId中的targetType
            var targetLetters = (res.data.records || []).filter(function(item) {
              if (item.letterStatus !== 0) return false
              var sid = item.sessionId || ''
              // 匹配 sessionId 中包含对应的 targetType
              if (sid.indexOf('_' + tabType + '_') !== -1) return true
              // 对于 punishment tab，也匹配没有 targetType 的处罚相关通知（如解除处罚通知）
              if (tabType === 'punishment') {
                var detail = item.letterDetail || ''
                if (detail.indexOf('禁言') !== -1 || detail.indexOf('封号') !== -1 || detail.indexOf('处罚') !== -1 || detail.indexOf('警告') !== -1) return true
              }
              // 对于 violation tab，也匹配违规相关的无targetType通知
              if (tabType === 'violation') {
                var detail = item.letterDetail || ''
                if (detail.indexOf('删除') !== -1 && (detail.indexOf('已被删除') !== -1 || detail.indexOf('被管理员删除') !== -1)) return true
                if (detail.indexOf('申诉') !== -1) return true
              }
              // 对于 lock tab，匹配锁定相关的通知
              if (tabType === 'lock') {
                var detail = item.letterDetail || ''
                if (detail.indexOf('锁定') !== -1 && (detail.indexOf('已被锁定') !== -1 || detail.indexOf('被管理员锁定') !== -1)) return true
                if (detail.indexOf('解锁') !== -1) return true
              }
              // 对于 report tab，也匹配举报相关的无targetType通知
              if (tabType === 'report') {
                var detail = item.letterDetail || ''
                if (detail.indexOf('举报') !== -1) return true
              }
              return false
            })
            for (var i = 0; i < targetLetters.length; i++) {
              await self.$axios.post('/letter/markRead/' + targetLetters[i].letterId).catch(function() {})
            }
            if (res.data.records.length < 100) {
              allMarked = true
            } else {
              page++
            }
          } else {
            allMarked = true
          }
        }
        this.$root.$emit('messages-read')
      } catch (e) {}
    },
    formatTime(time) {
      if (!time) return ''
      var d = new Date(time)
      var pad = function(n) { return String(n).padStart(2, '0') }
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    },
    async loadCombinedList() {
      this.combinedLoading = true
      try {
        var allRecords = []
        // Load violation delete records
        var vParams = { pageNum: 1, pageSize: 1000, sortBy: this.vSortBy, sortOrder: this.vSortOrder }
        if (this.vFilterCategory) vParams.itemType = this.vFilterCategory
        if (this.vFilterAppeal !== null && this.vFilterAppeal !== '') vParams.appealState = this.vFilterAppeal
        var vRes = await this.$axios.get('/violation-delete/my', { params: vParams })
        if (vRes.code === 200) {
          var vRecords = (vRes.data.records || []).map(function(item) {
            item._recordType = 'violation'
            item._baseItemType = item.itemType
            return item
          })
          allRecords = allRecords.concat(vRecords)
        }
        // Load lock records
        var lkParams = { pageNum: 1, pageSize: 1000, operationType: 'lock', sortBy: 'createTime', sortOrder: this.vSortOrder }
        if (this.vFilterCategory) lkParams.itemType = this.vFilterCategory + '_lock'
        if (this.vFilterAppeal !== null && this.vFilterAppeal !== '') lkParams.appealState = this.vFilterAppeal
        var lkRes = await this.$axios.get('/violation-delete/my', { params: lkParams })
        if (lkRes.code === 200) {
          var lkRecords = (lkRes.data.records || []).map(function(item) {
            item._recordType = 'lock'
            item._baseItemType = item.itemType ? item.itemType.replace('_lock', '') : ''
            return item
          })
          allRecords = allRecords.concat(lkRecords)
        }
        // Filter by record type
        if (this.vFilterRecordType) {
          allRecords = allRecords.filter(function(item) { return item._recordType === this.vFilterRecordType }.bind(this))
        }
        // Filter by category (base item type)
        if (this.vFilterCategory) {
          allRecords = allRecords.filter(function(item) { return item._baseItemType === this.vFilterCategory }.bind(this))
        }
        // Sort
        var sortBy = this.vSortBy
        var sortOrder = this.vSortOrder === 'asc' ? 1 : -1
        allRecords.sort(function(a, b) {
          var field = sortBy === 'updateTime' ? 'updateTime' : 'createTime'
          var ta = a[field] ? new Date(a[field]).getTime() : 0
          var tb = b[field] ? new Date(b[field]).getTime() : 0
          return (ta - tb) * sortOrder
        })
        this.combinedTotal = allRecords.length
        var start = (this.combinedPageNum - 1) * this.combinedPageSize
        this.combinedList = allRecords.slice(start, start + this.combinedPageSize)
      } catch(e) {}
      this.combinedLoading = false
    },
    openCombinedAppealDialog(row) { this.appealTarget = row; this.appealReason = ''; this.appealDialogVisible = true },
    async submitCombinedAppeal() {
      if (!this.appealReason.trim()) return this.$message.warning('请填写申诉理由')
      var res = await this.$axios.post('/violation-delete/appeal', { id: this.appealTarget.id, appealReason: this.appealReason })
      if (res.code === 200) { this.$message.success('申诉已提交'); this.appealDialogVisible = false; this.loadCombinedList() }
      else this.$message.error(res.msg || '申诉失败')
    },
    handleCombinedSizeChange(val) { this.combinedPageSize = val; this.combinedPageNum = 1; this.loadCombinedList() },
    async loadPunishments() {
      this.pLoading = true
      try {
        var res = await this.$axios.get('/punishment/my')
        if (res.code === 200) { this.pAllList = res.data || []; this.pPageNum = 1 }
      } catch(e) {}
      this.pLoading = false
    },
    async loadReports() {
      this.rLoading = true
      try {
        var res = await this.$axios.get('/report/my')
        if (res.code === 200) { this.rAllList = res.data || []; this.rPageNum = 1 }
      } catch(e) {}
      this.rLoading = false
    },
    openPunishmentAppeal(row) { this.punishmentAppealTarget = row; this.punishmentAppealReason = ''; this.punishmentAppealVisible = true },
    handlePSizeChange(val) { this.pPageSize = val; this.pPageNum = 1 },
    handleRSizeChange(val) { this.rPageSize = val; this.rPageNum = 1 },
    async submitPunishmentAppeal() {
      if (!this.punishmentAppealReason.trim()) return this.$message.warning('请填写申诉理由')
      var res = await this.$axios.post('/punishment/appeal', { punishmentId: this.punishmentAppealTarget.punishmentId, appealReason: this.punishmentAppealReason })
      if (res.code === 200) { this.$message.success('申诉已提交'); this.punishmentAppealVisible = false; this.loadPunishments() }
      else this.$message.error(res.msg || '申诉失败')
    }
  }
}
</script>

<style scoped>
</style>
