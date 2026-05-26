<template>
  <div>
    <el-tabs v-model="activeTab" @tab-click="handleTabChange">
      <!-- 处罚申诉 Tab -->
      <el-tab-pane label="处罚申诉" name="punishment">
        <div style="margin-bottom:15px;display:flex;flex-wrap:wrap;gap:10px;align-items:center;">
          <el-input v-model="punSearch.userId" placeholder="搜索用户ID" prefix-icon="el-icon-search" style="width:200px;" size="small" clearable @clear="loadPunData" @keyup.enter.native="loadPunData"></el-input>
          <el-select v-model="punSearch.appealState" placeholder="申诉状态" style="width:150px;" size="small" clearable @change="loadPunData">
            <el-option label="待审核" :value="0"></el-option>
            <el-option label="已通过" :value="1"></el-option>
            <el-option label="已驳回" :value="2"></el-option>
          </el-select>
          <el-select v-model="punSearch.type" placeholder="处罚类型" style="width:150px;" size="small" clearable @change="loadPunData">
            <el-option label="禁言" value="mute"></el-option>
            <el-option label="封号" value="ban"></el-option>
            <el-option label="警告" value="warning"></el-option>
          </el-select>
          <el-select v-model="punSortField" placeholder="排序" size="small" style="width:120px;" clearable @change="handlePunSortChange">
            <el-option label="申诉时间 ↓" value="appealTime_desc" />
            <el-option label="申诉时间 ↑" value="appealTime_asc" />
          </el-select>
          <el-button type="primary" size="small" icon="el-icon-search" @click="loadPunData">搜索</el-button>
          <el-button size="small" icon="el-icon-refresh" @click="resetPunSearch">重置</el-button>
        </div>
        <div style="margin-bottom:12px;display:flex;gap:8px;">
          <el-button type="success" size="small" icon="el-icon-check" :disabled="selectedPunRows.length === 0" @click="batchApprovePun">批量通过</el-button>
          <el-button type="danger" size="small" icon="el-icon-close" :disabled="selectedPunRows.length === 0" @click="batchRejectPun">批量拒绝</el-button>
        </div>
        <el-table :data="punList" v-loading="punLoading" border @selection-change="handlePunSelectionChange">
          <el-table-column type="selection" width="55" align="center"></el-table-column>
          <el-table-column prop="punishmentId" label="处罚ID" width="100" align="center" show-overflow-tooltip>
            <template slot-scope="scope">
              <el-link type="primary" @click="showPunishmentDetail(scope.row)">{{ scope.row.punishmentId | formatId('punishment') }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="userId" label="用户ID" width="130" align="center" show-overflow-tooltip>
            <template slot-scope="scope"><el-link type="primary" @click="showUserDetail(scope.row.userId)">{{ scope.row.userId | formatId('user') }}</el-link></template>
          </el-table-column>
          <el-table-column prop="type" label="处罚类型" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="punTypeTagType(scope.row.type)">{{ punTypeMap[scope.row.type] || scope.row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="处罚原因" min-width="150" show-overflow-tooltip></el-table-column>
          <el-table-column prop="appealReason" label="申诉原因" min-width="150" show-overflow-tooltip>
            <template slot-scope="scope">{{ scope.row.appealReason || '-' }}</template>
          </el-table-column>
          <el-table-column prop="appealTime" label="申诉时间" width="170">
            <template slot-scope="scope">{{ scope.row.appealTime | formatTime }}</template>
          </el-table-column>
          <el-table-column prop="appealState" label="申诉状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.appealState === 0" type="warning">待审核</el-tag>
              <el-tag v-else-if="scope.row.appealState === 1" type="success">已通过</el-tag>
              <el-tag v-else-if="scope.row.appealState === 2" type="danger">已驳回</el-tag>
              <el-tag v-else type="info">-</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="appealResult" label="申诉结果" min-width="150" show-overflow-tooltip>
            <template slot-scope="scope">{{ scope.row.appealResult || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template slot-scope="scope">
              <el-button v-if="scope.row.appealState === 0" size="mini" type="warning" @click="openPunAppealDialog(scope.row)">处理申诉</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination style="margin-top:15px;" @current-change="punPageChange" :current-page="punPageNum" :page-size.sync="punPageSize" :page-sizes="[10, 20, 50]" @size-change="punSizeChange" :total="punTotal" layout="total, sizes, prev, pager, next"></el-pagination>
      </el-tab-pane>

      <!-- 违规删除申诉 Tab -->
      <el-tab-pane label="违规删除申诉" name="violation">
        <div style="margin-bottom:15px;display:flex;flex-wrap:wrap;gap:10px;align-items:center;">
          <el-input v-model="vioSearch.userId" placeholder="搜索用户ID" prefix-icon="el-icon-search" style="width:200px;" size="small" clearable @clear="loadVioData" @keyup.enter.native="loadVioData"></el-input>
          <el-select v-model="vioSearch.itemType" placeholder="内容类型" style="width:150px;" size="small" clearable @change="loadVioData">
            <el-option label="帖子" value="post"></el-option>
            <el-option label="商品" value="goods"></el-option>
            <el-option label="表白墙" value="wall"></el-option>
          </el-select>
          <el-select v-model="vioSearch.appealState" placeholder="申诉状态" style="width:150px;" size="small" clearable @change="loadVioData">
            <el-option label="申诉中" :value="1"></el-option>
            <el-option label="已通过" :value="2"></el-option>
            <el-option label="已驳回" :value="3"></el-option>
          </el-select>
          <el-select v-model="vioSortField" placeholder="排序" size="small" style="width:120px;" clearable @change="handleVioSortChange">
            <el-option label="申诉时间 ↓" value="appealTime_desc" />
            <el-option label="申诉时间 ↑" value="appealTime_asc" />
          </el-select>
          <el-button type="primary" size="small" icon="el-icon-search" @click="loadVioData">搜索</el-button>
          <el-button size="small" icon="el-icon-refresh" @click="resetVioSearch">重置</el-button>
        </div>
        <div style="margin-bottom:12px;display:flex;gap:8px;">
          <el-button type="success" size="small" icon="el-icon-check" :disabled="selectedVioRows.length === 0" @click="batchApproveVio">批量通过</el-button>
          <el-button type="danger" size="small" icon="el-icon-close" :disabled="selectedVioRows.length === 0" @click="batchRejectVio">批量拒绝</el-button>
        </div>
        <el-table :data="vioList" v-loading="vioLoading" border @selection-change="handleVioSelectionChange">
          <el-table-column type="selection" width="55" align="center"></el-table-column>
          <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
          <el-table-column prop="userId" label="用户ID" width="150" show-overflow-tooltip>
            <template slot-scope="scope">
              <el-link type="primary" @click="showUserDetail(scope.row.userId)">{{ scope.row.userId }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="itemType" label="类型" width="80" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row.itemType === 'post' ? 'primary' : scope.row.itemType === 'goods' ? 'warning' : 'danger'">
                {{ scope.row.itemType === 'post' ? '帖子' : scope.row.itemType === 'goods' ? '商品' : '表白墙' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="preview" label="内容预览" min-width="200" show-overflow-tooltip />
          <el-table-column prop="reason" label="删除原因" width="150" show-overflow-tooltip />
          <el-table-column prop="appealReason" label="申诉理由" min-width="150" show-overflow-tooltip>
            <template slot-scope="scope">{{ scope.row.appealReason || '-' }}</template>
          </el-table-column>
          <el-table-column prop="appealTime" label="申诉时间" width="170">
            <template slot-scope="scope">{{ formatTime(scope.row.appealTime) }}</template>
          </el-table-column>
          <el-table-column prop="appealState" label="申诉状态" width="90" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row.appealState === 1 ? 'warning' : scope.row.appealState === 2 ? 'success' : 'danger'">
                {{ ['未申诉', '申诉中', '已通过', '已驳回'][scope.row.appealState] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="appealResult" label="申诉结果" min-width="150" show-overflow-tooltip>
            <template slot-scope="scope">{{ scope.row.appealResult || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template slot-scope="scope">
              <el-button v-if="scope.row.appealState === 1" size="mini" type="warning" @click="openVioAppealDialog(scope.row)">处理申诉</el-button>
              <span v-else style="color:#C0C4CC;font-size:12px;">-</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination style="margin-top:15px;" @current-change="vioPageChange" :current-page="vioPageNum" :page-size.sync="vioPageSize" :page-sizes="[10, 20, 50]" @size-change="vioSizeChange" :total="vioTotal" layout="total, sizes, prev, pager, next"></el-pagination>
      </el-tab-pane>
    </el-tabs>

    <!-- 处罚详情对话框 -->
    <el-dialog title="处罚详情" :visible.sync="punishmentDetailVisible" width="700px" top="5vh">
      <div v-if="punishmentDetailData">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-warning" style="margin-right:6px;"></i>处罚信息</div>
          <table class="dt-table">
            <tr>
              <td class="dt-label">处罚ID</td><td>{{ punishmentDetailData.punishmentId | formatId('punishment') }}</td>
              <td class="dt-label">处罚类型</td><td><el-tag :type="punTypeTagType(punishmentDetailData.type)">{{ punTypeMap[punishmentDetailData.type] || punishmentDetailData.type }}</el-tag></td>
            </tr>
            <tr>
              <td class="dt-label">用户ID</td><td><el-link type="primary" @click="showUserDetail(punishmentDetailData.userId)">{{ punishmentDetailData.userId | formatId('user') }}</el-link></td>
              <td class="dt-label">处罚状态</td>
              <td>
                <el-tag v-if="punishmentDetailData.status === 0" type="success">生效中</el-tag>
                <el-tag v-else-if="punishmentDetailData.status === 1" type="info">已解除</el-tag>
                <el-tag v-else-if="punishmentDetailData.status === 2" type="info">已过期</el-tag>
                <el-tag v-else type="info">未知</el-tag>
              </td>
            </tr>
            <tr>
              <td class="dt-label">处罚原因</td><td colspan="3" style="white-space:pre-wrap;">{{ punishmentDetailData.reason || '-' }}</td>
            </tr>
            <tr>
              <td class="dt-label">生效时间</td><td>{{ punishmentDetailData.startTime | formatTime }}</td>
              <td class="dt-label">解除时间</td><td>{{ punishmentDetailData.endTime | formatTime }}</td>
            </tr>
            <tr>
              <td class="dt-label">处理人ID</td><td>{{ punishmentDetailData.handlerId || '-' }}</td>
              <td class="dt-label">创建时间</td><td>{{ punishmentDetailData.createTime | formatTime }}</td>
            </tr>
          </table>
        </el-card>
        <el-card v-if="punishmentDetailData.appealState !== null && punishmentDetailData.appealState !== undefined" shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-chat-line-square" style="margin-right:6px;"></i>申诉信息</div>
          <table class="dt-table">
            <tr>
              <td class="dt-label">申诉原因</td><td colspan="3" style="white-space:pre-wrap;">{{ punishmentDetailData.appealReason || '-' }}</td>
            </tr>
            <tr>
              <td class="dt-label">申诉状态</td>
              <td>
                <el-tag v-if="punishmentDetailData.appealState === 0" type="warning">待审核</el-tag>
                <el-tag v-else-if="punishmentDetailData.appealState === 1" type="success">已通过</el-tag>
                <el-tag v-else-if="punishmentDetailData.appealState === 2" type="danger">已驳回</el-tag>
              </td>
              <td class="dt-label">申诉时间</td><td>{{ punishmentDetailData.appealTime | formatTime }}</td>
            </tr>
            <tr>
              <td class="dt-label">申诉结果</td><td colspan="3" style="white-space:pre-wrap;">{{ punishmentDetailData.appealResult || '-' }}</td>
            </tr>
          </table>
        </el-card>
      </div>
    </el-dialog>

    <!-- 处理处罚申诉对话框 -->
    <el-dialog title="处理处罚申诉" :visible.sync="punAppealDialogVisible" width="600px">
      <div v-if="punAppealRow">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-warning" style="margin-right:6px;"></i>处罚信息</div>
          <table class="dt-table">
            <tr>
              <td class="dt-label">处罚ID</td><td>{{ punAppealRow.punishmentId | formatId('punishment') }}</td>
              <td class="dt-label">用户ID</td><td><el-link type="primary" @click="showUserDetail(punAppealRow.userId)">{{ punAppealRow.userId | formatId('user') }}</el-link></td>
            </tr>
            <tr>
              <td class="dt-label">处罚类型</td><td><el-tag :type="punTypeTagType(punAppealRow.type)">{{ punTypeMap[punAppealRow.type] || punAppealRow.type }}</el-tag></td>
              <td class="dt-label">处罚原因</td><td>{{ punAppealRow.reason || '-' }}</td>
            </tr>
          </table>
        </el-card>
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-chat-line-square" style="margin-right:6px;"></i>申诉原因</div>
          <div style="white-space:pre-wrap;font-size:13px;color:#303133;">{{ punAppealRow.appealReason || '-' }}</div>
        </el-card>
        <el-form label-width="100px">
          <el-form-item label="处理结果">
            <el-radio-group v-model="punAppealForm.appealState">
              <el-radio :label="1">通过</el-radio>
              <el-radio :label="2">驳回</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="处理原因">
            <el-input v-model="punAppealForm.appealResult" type="textarea" :rows="4" placeholder="请输入处理原因"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer">
        <el-button @click="punAppealDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPunAppeal">提交</el-button>
      </div>
    </el-dialog>

    <!-- 处理违规删除申诉对话框 -->
    <el-dialog title="处理违规删除申诉" :visible.sync="vioAppealDialogVisible" width="600px">
      <div v-if="vioAppealRow">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-warning" style="margin-right:6px;"></i>违规删除信息</div>
          <table class="dt-table">
            <tr>
              <td class="dt-label">内容类型</td><td>{{ vioAppealRow.itemType === 'post' ? '帖子' : vioAppealRow.itemType === 'goods' ? '商品' : '表白墙' }}</td>
              <td class="dt-label">用户ID</td><td><el-link type="primary" @click="showUserDetail(vioAppealRow.userId)">{{ vioAppealRow.userId }}</el-link></td>
            </tr>
            <tr>
              <td class="dt-label">内容预览</td><td colspan="3">{{ vioAppealRow.preview || '-' }}</td>
            </tr>
            <tr>
              <td class="dt-label">删除原因</td><td colspan="3">{{ vioAppealRow.reason || '-' }}</td>
            </tr>
          </table>
        </el-card>
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div slot="header" style="font-weight:bold;"><i class="el-icon-chat-line-square" style="margin-right:6px;"></i>申诉理由</div>
          <div style="white-space:pre-wrap;font-size:13px;color:#303133;">{{ vioAppealRow.appealReason || '-' }}</div>
        </el-card>
        <el-form label-width="100px">
          <el-form-item label="处理结果">
            <el-radio-group v-model="vioAppealForm.appealState">
              <el-radio :label="2">通过（恢复内容）</el-radio>
              <el-radio :label="3">驳回</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="处理说明">
            <el-input v-model="vioAppealForm.appealResult" type="textarea" :rows="4" placeholder="请填写处理说明"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer">
        <el-button @click="vioAppealDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitVioAppeal">提交</el-button>
      </div>
    </el-dialog>

    <user-detail-dialog :visible.sync="userDetailVisible" :user-id="userDetailUserId" @show-post="showPostDetail" @show-goods="showGoodsDetail" @show-wall="showWallDetail"></user-detail-dialog>
    <post-detail-dialog :visible.sync="postDetailVisible" :post-id="postDetailPostId" @show-user="showUserDetail"></post-detail-dialog>
    <goods-detail-dialog :visible.sync="goodsDetailVisible" :goods-id="goodsDetailGoodsId" @show-user="showUserDetail"></goods-detail-dialog>
    <wall-detail-dialog :visible.sync="wallDetailVisible" :wall-id="wallDetailWallId" @show-user="showUserDetail"></wall-detail-dialog>
  </div>
</template>
<script>
import { getPunishmentList, handleAppeal } from '@/api/manage'
import { getViolationList, handleViolationAppeal } from '@/api/violation'
import UserDetailDialog from '@/components/UserDetailDialog'
import PostDetailDialog from '@/components/PostDetailDialog'
import GoodsDetailDialog from '@/components/GoodsDetailDialog'
import WallDetailDialog from '@/components/WallDetailDialog'
export default {
  components: { UserDetailDialog, PostDetailDialog, GoodsDetailDialog, WallDetailDialog },
  data() {
    return {
      activeTab: 'punishment',
      // 处罚申诉
      punList: [], punPageNum: 1, punPageSize: 10, punTotal: 0, punLoading: false,
      punSearch: { userId: '', appealState: null, type: '' }, selectedPunRows: [],
      punSortField: '',
      punTypeMap: { mute: '禁言', ban: '封号', warning: '警告' },
      punAppealDialogVisible: false, punAppealRow: null,
      punAppealForm: { punishmentId: null, appealState: 1, appealResult: '' },
      punishmentDetailVisible: false, punishmentDetailData: null,
      // 违规删除申诉
      vioList: [], vioPageNum: 1, vioPageSize: 10, vioTotal: 0, vioLoading: false,
      vioSearch: { userId: '', itemType: '', appealState: null }, selectedVioRows: [],
      vioSortField: '',
      vioAppealDialogVisible: false, vioAppealRow: null,
      vioAppealForm: { id: null, appealState: 2, appealResult: '' },
      // 公共
      userDetailVisible: false, userDetailUserId: '',
      postDetailVisible: false, postDetailPostId: '',
      goodsDetailVisible: false, goodsDetailGoodsId: '',
      wallDetailVisible: false, wallDetailWallId: ''
    }
  },
  created() { this.loadPunData() },
  methods: {
    formatTime(time) {
      if (!time) return ''
      var d = new Date(time)
      var pad = function(n) { return String(n).padStart(2, '0') }
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
    },
    handleTabChange(tab) {
      if (tab.name === 'punishment') { this.loadPunData() }
      else if (tab.name === 'violation') { this.loadVioData() }
    },
    // 处罚申诉相关
    punTypeTagType(type) {
      if (type === 'mute') return 'warning'
      if (type === 'ban') return 'danger'
      if (type === 'warning') return 'info'
      return ''
    },
    async loadPunData() {
      this.punLoading = true
      var params = { pageNum: this.punPageNum, pageSize: this.punPageSize }
      if (this.punSearch.userId) { params.userId = this.punSearch.userId }
      if (this.punSearch.type) { params.type = this.punSearch.type }
      if (this.punSearch.appealState !== null && this.punSearch.appealState !== undefined && this.punSearch.appealState !== '') { params.appealState = this.punSearch.appealState }
      var res = await getPunishmentList(params)
      if (res.code === 200) {
        var records = res.data.records || []
        this.punList = records.filter(function(item) { return item.appealState !== null && item.appealState !== undefined })
        this.punTotal = this.punList.length
      }
      this.punLoading = false
    },
    punPageChange(val) { this.punPageNum = val; this.loadPunData() },
    punSizeChange(val) { this.punPageSize = val; this.punPageNum = 1; this.loadPunData() },
    resetPunSearch() { this.punSearch = { userId: '', appealState: null, type: '' }; this.punSortField = ''; this.loadPunData() },
    handlePunSortChange(val) {
      if (!val) return
      var parts = val.split('_')
      var field = parts[0]
      var order = parts[1]
      this.punList.sort(function(a, b) {
        var va = a[field], vb = b[field]
        if (!va && !vb) return 0
        if (!va) return 1
        if (!vb) return -1
        var ta = new Date(va).getTime(), tb = new Date(vb).getTime()
        return order === 'asc' ? ta - tb : tb - ta
      })
    },
    openPunAppealDialog(row) {
      this.punAppealRow = row
      this.punAppealForm = { punishmentId: row.punishmentId, appealState: 1, appealResult: '' }
      this.punAppealDialogVisible = true
    },
    async submitPunAppeal() {
      if (!this.punAppealForm.appealResult) { this.$message.warning('请输入处理原因'); return }
      try {
        var res = await handleAppeal(this.punAppealForm)
        if (res.code === 200) {
          this.$message.success('处理成功')
          this.punAppealDialogVisible = false
          this.loadPunData()
        } else {
          this.$message.error(res.msg || '处理失败')
        }
      } catch (e) { }
    },
    showPunishmentDetail(row) { this.punishmentDetailData = row; this.punishmentDetailVisible = true },
    handlePunSelectionChange(val) { this.selectedPunRows = val },
    async batchApprovePun() {
      if (this.selectedPunRows.length === 0) return
      var pending = this.selectedPunRows.filter(function(row) { return row.appealState === 0 })
      if (pending.length === 0) { this.$message.warning('选中的申诉中没有待审核的记录'); return }
      try { await this.$confirm('确定批量通过选中的处罚申诉？', '提示', { type: 'warning' }) } catch { return }
      for (var i = 0; i < pending.length; i++) { await handleAppeal({ punishmentId: pending[i].punishmentId, appealState: 1, appealResult: '批量通过' }) }
      this.$message.success('批量通过成功'); this.selectedPunRows = []; this.loadPunData()
    },
    async batchRejectPun() {
      if (this.selectedPunRows.length === 0) return
      var pending = this.selectedPunRows.filter(function(row) { return row.appealState === 0 })
      if (pending.length === 0) { this.$message.warning('选中的申诉中没有待审核的记录'); return }
      try { await this.$confirm('确定批量拒绝选中的处罚申诉？', '提示', { type: 'warning' }) } catch { return }
      for (var i = 0; i < pending.length; i++) { await handleAppeal({ punishmentId: pending[i].punishmentId, appealState: 2, appealResult: '批量驳回' }) }
      this.$message.success('批量拒绝成功'); this.selectedPunRows = []; this.loadPunData()
    },
    // 违规删除申诉相关
    async loadVioData() {
      this.vioLoading = true
      try {
        var params = { pageNum: this.vioPageNum, pageSize: this.vioPageSize }
        if (this.vioSearch.userId) params.userId = this.vioSearch.userId
        if (this.vioSearch.itemType) params.itemType = this.vioSearch.itemType
        if (this.vioSearch.appealState !== null && this.vioSearch.appealState !== '') params.appealState = this.vioSearch.appealState
        var res = await getViolationList(params)
        if (res.code === 200) {
          // 只显示有申诉的记录（appealState 不为 0，即已申诉的）
          this.vioList = (res.data.records || []).filter(function(item) { return item.appealState !== 0 })
          this.vioTotal = res.data.total || 0
        }
      } catch (e) { }
      this.vioLoading = false
    },
    vioPageChange(val) { this.vioPageNum = val; this.loadVioData() },
    vioSizeChange(val) { this.vioPageSize = val; this.vioPageNum = 1; this.loadVioData() },
    resetVioSearch() { this.vioSearch = { userId: '', itemType: '', appealState: null }; this.vioSortField = ''; this.vioPageNum = 1; this.loadVioData() },
    openVioAppealDialog(row) {
      this.vioAppealRow = row
      this.vioAppealForm = { id: row.id, appealState: 2, appealResult: '' }
      this.vioAppealDialogVisible = true
    },
    async submitVioAppeal() {
      if (!this.vioAppealForm.appealResult.trim()) { this.$message.warning('请填写处理说明'); return }
      try {
        var res = await handleViolationAppeal(this.vioAppealForm)
        if (res.code === 200) {
          this.$message.success(this.vioAppealForm.appealState === 2 ? '申诉通过，内容已恢复' : '申诉已驳回')
          this.vioAppealDialogVisible = false
          this.loadVioData()
        } else {
          this.$message.error(res.msg || '处理失败')
        }
      } catch (e) { }
    },
    handleVioSelectionChange(val) { this.selectedVioRows = val },
    handleVioSortChange(val) {
      if (!val) return
      var parts = val.split('_')
      var field = parts[0]
      var order = parts[1]
      this.vioList.sort(function(a, b) {
        var va = a[field], vb = b[field]
        if (!va && !vb) return 0
        if (!va) return 1
        if (!vb) return -1
        var ta = new Date(va).getTime(), tb = new Date(vb).getTime()
        return order === 'asc' ? ta - tb : tb - ta
      })
    },
    async batchApproveVio() {
      if (this.selectedVioRows.length === 0) return
      try { await this.$confirm('确定批量通过选中的违规删除申诉？', '提示', { type: 'warning' }) } catch (e) { return }
      for (var i = 0; i < this.selectedVioRows.length; i++) {
        try { await handleViolationAppeal({ id: this.selectedVioRows[i].id, appealState: 2, appealResult: '批量通过' }) } catch (e) { /* ignore */ }
      }
      this.$message.success('批量通过成功'); this.selectedVioRows = []; this.loadVioData()
    },
    async batchRejectVio() {
      if (this.selectedVioRows.length === 0) return
      try { await this.$confirm('确定批量拒绝选中的违规删除申诉？', '提示', { type: 'warning' }) } catch (e) { return }
      for (var i = 0; i < this.selectedVioRows.length; i++) {
        try { await handleViolationAppeal({ id: this.selectedVioRows[i].id, appealState: 3, appealResult: '批量驳回' }) } catch (e) { /* ignore */ }
      }
      this.$message.success('批量拒绝成功'); this.selectedVioRows = []; this.loadVioData()
    },
    // 公共
    showUserDetail(userId) { this.userDetailUserId = userId; this.userDetailVisible = true },
    showPostDetail(postId) { this.postDetailPostId = postId; this.postDetailVisible = true },
    showGoodsDetail(goodsId) { this.goodsDetailGoodsId = goodsId; this.goodsDetailVisible = true },
    showWallDetail(wallId) { this.wallDetailWallId = wallId; this.wallDetailVisible = true }
  }
}
</script>
<style scoped>
/* Page container */
.page-container { padding: 20px; background: #f5f7fa; min-height: 100%; }

/* Custom classes */
.dt-table { width: 100%; border-collapse: collapse; border-color: #ebeef5; }
.dt-table td { padding: 10px 12px; border: 1px solid #ebeef5; font-size: 13px; color: #303133; }
.dt-label { background: #fafafa; font-weight: bold; width: 100px; text-align: right; color: #606266; white-space: nowrap; }

/* Table */
::v-deep .el-table { border-radius: 12px; overflow: hidden; }
::v-deep .el-table th .cell { color: #303133; font-weight: 600; }
::v-deep .el-table .el-button--mini { margin: 0 2px; }

/* Tags */
::v-deep .el-tag { border-radius: 6px; }

/* Cards */
::v-deep .el-card { border-radius: 12px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); transition: box-shadow 0.3s ease; }
::v-deep .el-card:hover { box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1); }

/* Interactive elements */
::v-deep .el-button { transition: all 0.3s ease; }
::v-deep .el-link { transition: color 0.3s ease; }

/* Pagination */
::v-deep .el-pagination { padding: 16px 0; }

/* Tabs */
::v-deep .el-tabs__item { font-weight: 600; color: #303133; }

/* Section titles */
::v-deep .el-dialog__title { font-weight: 600; color: #303133; }
</style>
