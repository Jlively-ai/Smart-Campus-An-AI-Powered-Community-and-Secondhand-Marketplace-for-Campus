<!--
  组件名：AiConfigIndex
  功能描述：AI配置管理页面
  主要职责：展示和编辑AI模型配置参数，支持保存配置
-->
<template>
  <div>
    
    <el-tabs v-model="activeTab" @tab-click="onTabChange">
      <el-tab-pane label="AI 助手" name="1">
        <template slot="label">
          <span><i class="el-icon-search"></i> AI 助手</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="AI 伴侣" name="2">
        <template slot="label">
          <span><i class="el-icon-chat-dot-round"></i> AI 伴侣</span>
        </template>
      </el-tab-pane>
    </el-tabs>

    
    <div style="margin-bottom:15px;display:flex;flex-wrap:wrap;gap:10px;align-items:center;">
      <span style="font-size:16px;font-weight:bold;">{{ activeTab === '1' ? 'AI 助手' : 'AI 伴侣' }} 模型配置</span>
      <el-button type="primary" size="small" icon="el-icon-plus" @click="openAddDialog">添加配置</el-button>
      <el-button size="small" icon="el-icon-refresh" @click="loadData">刷新</el-button>
      <div style="margin-left:auto;">
        <el-alert v-if="activeConfigs.length > 0" type="success" :closable="false" show-icon style="padding:4px 12px;">
          <template slot="title">
            已启用 {{ activeConfigs.length }} 个模型：<b>{{ activeConfigs.map(c => c.configName).join('、') }}</b>
          </template>
        </el-alert>
        <el-alert v-else type="warning" :closable="false" show-icon style="padding:4px 12px;">
          <template slot="title">{{ activeTab === '1' ? '未启用任何模型，AI助手使用本地搜索模式' : '未启用任何模型，AI伴侣不可用' }}</template>
        </el-alert>
      </div>
    </div>

    
    <el-table :data="configList" v-loading="loading" border style="width:100%" size="small">
      <el-table-column label="状态" width="70" align="center" fixed>
        <template slot-scope="scope">
          <el-switch :value="scope.row.isActive" @change="toggleActive(scope.row)" active-color="#13ce66" inactive-color="#dcdfe6"></el-switch>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="ID" width="50" align="center"></el-table-column>
      <el-table-column prop="configName" label="配置名称" min-width="120" show-overflow-tooltip>
        <template slot-scope="scope">
          <span :style="{fontWeight: scope.row.isActive ? '600' : 'normal', color: scope.row.isActive ? '#303133' : '#909399'}">{{ scope.row.configName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="provider" label="提供商" width="95">
        <template slot-scope="scope">
          <el-tag :type="getProviderTagType(scope.row.provider)" size="mini">{{ getProviderLabel(scope.row.provider) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="modelName" label="模型" min-width="120" show-overflow-tooltip></el-table-column>
      <el-table-column prop="apiUrl" label="API地址" min-width="160" show-overflow-tooltip></el-table-column>
      <el-table-column label="思考" width="55" align="center">
        <template slot-scope="scope">
          <el-switch :value="scope.row.thinkingEnabled" @change="toggleField(scope.row, 'thinkingEnabled')" active-color="#E6A23C" inactive-color="#dcdfe6" :disabled="!scope.row.isActive"></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="流式" width="55" align="center">
        <template slot-scope="scope">
          <el-switch :value="scope.row.streamEnabled" @change="toggleField(scope.row, 'streamEnabled')" active-color="#13ce66" inactive-color="#dcdfe6" :disabled="!scope.row.isActive"></el-switch>
        </template>
      </el-table-column>
      <el-table-column prop="temperature" label="温度" width="50" align="center"></el-table-column>
      <el-table-column prop="maxTokens" label="Token" width="60" align="center"></el-table-column>
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template slot-scope="scope">
          <div style="display:flex;justify-content:center;gap:4px;">
            <el-button type="primary" size="mini" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button type="danger" size="mini" @click="deleteConfig(scope.row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="720px" :close-on-click-modal="false" top="6vh">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px" size="small">
        
        <div class="config-section">
          <div class="config-section-title"><i class="el-icon-setting"></i> 基础配置</div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="配置名称" prop="configName">
                <el-input v-model="form.configName" placeholder="如：DeepSeek-默认配置"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="提供商" prop="provider">
                <el-select v-model="form.provider" placeholder="选择提供商" style="width:100%;" @change="onProviderChange">
                  <el-option-group label="国内模型">
                    <el-option label="DeepSeek 深度求索" value="deepseek"></el-option>
                    <el-option label="通义千问 (Qwen)" value="qwen"></el-option>
                    <el-option label="智谱AI (GLM)" value="zhipu"></el-option>
                    <el-option label="Kimi 月之暗面" value="moonshot"></el-option>
                    <el-option label="百度文心一言" value="wenxin"></el-option>
                    <el-option label="讯飞星火" value="spark"></el-option>
                    <el-option label="MiniMax" value="minimax"></el-option>
                    <el-option label="百川智能" value="baichuan"></el-option>
                  </el-option-group>
                  <el-option-group label="国外模型">
                    <el-option label="OpenAI (GPT)" value="openai"></el-option>
                    <el-option label="OpenRouter 聚合" value="openrouter"></el-option>
                  </el-option-group>
                  <el-option-group label="其他">
                    <el-option label="自定义接口" value="custom"></el-option>
                  </el-option-group>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="API地址" prop="apiUrl">
            <el-input v-model="form.apiUrl" placeholder="如：https://api.deepseek.com">
              <el-button slot="append" @click="fillDefaultApiUrl">填充默认</el-button>
            </el-input>
            <div class="form-hint">只需填写 Base URL，系统会自动拼接 /chat/completions</div>
          </el-form-item>

          <el-form-item label="API密钥" prop="apiKey">
            <el-input v-model="form.apiKey" :placeholder="isEdit ? '不修改请留空' : '输入API Key'" show-password></el-input>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="模型名称" prop="modelName">
                <el-select v-model="form.modelName" filterable allow-create default-first-option placeholder="选择或输入模型名" style="width:100%;">
                  <el-option v-for="m in currentModels" :key="m.value" :label="m.label" :value="m.value"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="最大Token" prop="maxTokens">
                <el-input-number v-model="form.maxTokens" :min="256" :max="65536" :step="256" style="width:100%;"></el-input-number>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="系统提示词" prop="systemPrompt">
            <el-input v-model="form.systemPrompt" type="textarea" :rows="3" :placeholder="systemPromptPlaceholder"></el-input>
          </el-form-item>
        </div>

        
        <div class="config-section">
          <div class="config-section-title"><i class="el-icon-s-tools"></i> 高级设置</div>

          <div class="adv-grid">
            <div class="adv-item">
              <div class="adv-label">温度参数</div>
              <el-slider v-model="form.temperature" :min="0" :max="2" :step="0.1" show-input input-size="mini"></el-slider>
              <div class="adv-desc">输出随机性，思考模式下不生效</div>
            </div>
            <div class="adv-item">
              <div class="adv-label">上下文轮数</div>
              <el-input-number v-model="form.contextRounds" :min="0" :max="20" :step="1" size="small" style="width:100%;"></el-input-number>
              <div class="adv-desc">多轮对话保留的历史轮数</div>
            </div>
          </div>

          <div class="adv-grid" style="margin-top:12px;">
            <div class="adv-item">
              <div class="adv-label">思考模式</div>
              <el-switch v-model="form.thinkingEnabled" active-color="#E6A23C" inactive-color="#C0C4CC"></el-switch>
              <div class="adv-desc">DeepSeek/GLM/Kimi/星火X1.5等支持</div>
            </div>
            <div class="adv-item" v-if="form.thinkingEnabled">
              <div class="adv-label">思考强度</div>
              <el-radio-group v-model="form.reasoningEffort" size="mini">
                <el-radio-button label="low">Low</el-radio-button>
                <el-radio-button label="high">High</el-radio-button>
                <el-radio-button label="max">Max</el-radio-button>
              </el-radio-group>
              <div class="adv-desc">推荐 High，Max 更慢但更深入</div>
            </div>
            <div class="adv-item">
              <div class="adv-label">流式输出</div>
              <el-switch v-model="form.streamEnabled" active-color="#13ce66" inactive-color="#C0C4CC"></el-switch>
              <div class="adv-desc">逐字输出，暂不支持前端流式</div>
            </div>
          </div>
        </div>

        
        <div class="config-section">
          <div class="config-section-title"><i class="el-icon-s-flag"></i> 备注</div>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息"></el-input>
          </el-form-item>
        </div>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false" size="small">取消</el-button>
        <el-button type="info" @click="testConnection" size="small" :loading="testing">测试连接</el-button>
        <el-button type="primary" @click="submitForm" size="small" :loading="submitting">确定</el-button>
      </span>
    </el-dialog>

    
    <el-dialog title="连接测试结果" :visible.sync="testResultVisible" width="500px">
      <div v-if="testResult.success">
        <div style="text-align:center;padding:20px 0;">
          <i class="el-icon-circle-check" style="font-size:48px;color:#67C23A;"></i>
          <div style="font-size:16px;font-weight:bold;margin-top:10px;">连接成功</div>
          <div style="color:#909399;font-size:13px;margin-top:4px;">模型：{{ testResult.model || '' }}</div>
        </div>
        <div style="text-align:left;background:#f5f7fa;padding:12px;border-radius:8px;margin-top:10px;">
          <div style="margin-bottom:8px;"><b>AI回复：</b></div>
          <div style="color:#303133;">{{ testResult.reply }}</div>
          <div v-if="testResult.usage" style="margin-top:8px;color:#909399;font-size:12px;">
            Token用量 - 输入：{{ testResult.usage.prompt_tokens }} / 输出：{{ testResult.usage.completion_tokens }} / 总计：{{ testResult.usage.total_tokens }}
          </div>
        </div>
      </div>
      <div v-else>
        <div style="text-align:center;padding:20px 0;">
          <i class="el-icon-circle-close" style="font-size:48px;color:#F56C6C;"></i>
          <div style="font-size:16px;font-weight:bold;margin-top:10px;">连接失败</div>
          <div style="color:#909399;font-size:13px;margin-top:4px;">{{ testResult.error }}</div>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="testResultVisible = false" size="small">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAiConfigList, addAiConfig, updateAiConfig, deleteAiConfig, toggleAiConfigEnabled, testAiConnection } from '@/api/manage'

export default {
  name: 'AiConfigList',
  /** 组件数据定义 */
  data() {
    return {
      activeTab: '1', 
      configList: [],
      activeConfigs: [],
      loading: false,
      dialogVisible: false,
      isEdit: false,
      submitting: false,
      testing: false,
      testResultVisible: false,
      testResult: { success: false, reply: '', model: '', usage: null, error: '' },
      form: {
        id: null,
        configName: '',
        configType: 1,
        provider: 'deepseek',
        apiUrl: '',
        apiKey: '',
        modelName: '',
        systemPrompt: '',
        temperature: 0.7,
        maxTokens: 2048,
        thinkingEnabled: false,
        reasoningEffort: 'high',
        streamEnabled: false,
        contextRounds: 5,
        remark: ''
      },
      rules: {
        configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
        provider: [{ required: true, message: '请选择提供商', trigger: 'change' }],
        apiUrl: [{ required: true, message: '请输入API地址', trigger: 'blur' }],
        modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }]
      },
      providerDefaults: {
        deepseek: { apiUrl: 'https://api.deepseek.com', modelName: 'deepseek-v4-flash' },
        openai: { apiUrl: 'https://api.openai.com/v1', modelName: 'gpt-4o-mini' },
        qwen: { apiUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1', modelName: 'qwen-plus' },
        wenxin: { apiUrl: 'https://qianfan.baidubce.com/v2', modelName: 'ernie-4.0-8k' },
        zhipu: { apiUrl: 'https://open.bigmodel.cn/api/paas/v4', modelName: 'glm-4-flash' },
        moonshot: { apiUrl: 'https://api.moonshot.cn/v1', modelName: 'moonshot-v1-8k' },
        spark: { apiUrl: 'https://spark-api-open.xf-yun.com/v1', modelName: 'generalv3' },
        minimax: { apiUrl: 'https://api.minimaxi.com/v1', modelName: 'MiniMax-M2.5' },
        baichuan: { apiUrl: 'https://api.baichuan-ai.com/v1', modelName: 'Baichuan4-Turbo' },
        openrouter: { apiUrl: 'https://openrouter.ai/api/v1', modelName: 'openai/gpt-4o-mini' },
        custom: { apiUrl: '', modelName: '' }
      },
      providerModels: {
        deepseek: [
          { value: 'deepseek-v4-flash', label: 'deepseek-v4-flash（快速）' },
          { value: 'deepseek-v4-pro', label: 'deepseek-v4-pro（深度思考）' },
          { value: 'deepseek-chat', label: 'deepseek-chat（兼容旧名）' },
          { value: 'deepseek-reasoner', label: 'deepseek-reasoner（兼容旧名）' }
        ],
        openai: [
          { value: 'gpt-4o', label: 'GPT-4o' },
          { value: 'gpt-4o-mini', label: 'GPT-4o-mini' },
          { value: 'gpt-4.1', label: 'GPT-4.1' },
          { value: 'gpt-4.1-mini', label: 'GPT-4.1-mini' },
          { value: 'gpt-4.1-nano', label: 'GPT-4.1-nano' },
          { value: 'o3', label: 'o3（推理）' },
          { value: 'o4-mini', label: 'o4-mini（推理）' }
        ],
        qwen: [
          { value: 'qwen-turbo', label: '通义千问-Turbo' },
          { value: 'qwen-plus', label: '通义千问-Plus' },
          { value: 'qwen-max', label: '通义千问-Max' },
          { value: 'qwen-flash', label: '通义千问-Flash' },
          { value: 'qwen-long', label: '通义千问-Long' },
          { value: 'qwq-32b-preview', label: 'QwQ-32B（推理）' },
          { value: 'qwen-coder', label: '通义千问-Coder' }
        ],
        wenxin: [
          { value: 'ernie-4.0-8k', label: 'ERNIE-4.0-8K' },
          { value: 'ernie-4.0-128k', label: 'ERNIE-4.0-128K' },
          { value: 'ernie-3.5-8k', label: 'ERNIE-3.5-8K' },
          { value: 'ernie-speed-8k', label: 'ERNIE-Speed-8K' },
          { value: 'ernie-speed-128k', label: 'ERNIE-Speed-128K' },
          { value: 'ernie-lite-8k', label: 'ERNIE-Lite-8K' }
        ],
        zhipu: [
          { value: 'glm-4-flash', label: 'GLM-4-Flash' },
          { value: 'glm-4', label: 'GLM-4' },
          { value: 'glm-4.7-flash', label: 'GLM-4.7-Flash' },
          { value: 'glm-4.7', label: 'GLM-4.7' },
          { value: 'glm-5-turbo', label: 'GLM-5-Turbo' },
          { value: 'glm-5', label: 'GLM-5' }
        ],
        moonshot: [
          { value: 'moonshot-v1-8k', label: 'Moonshot-v1-8K' },
          { value: 'moonshot-v1-32k', label: 'Moonshot-v1-32K' },
          { value: 'moonshot-v1-128k', label: 'Moonshot-v1-128K' },
          { value: 'kimi-k2', label: 'Kimi-K2' },
          { value: 'kimi-k2-thinking', label: 'Kimi-K2-Thinking' }
        ],
        spark: [
          { value: 'lite', label: 'Spark Lite（免费）' },
          { value: 'generalv3', label: 'Spark Pro' },
          { value: 'pro-128k', label: 'Spark Pro 128K' },
          { value: 'generalv3.5', label: 'Spark Max' },
          { value: 'max-32k', label: 'Spark Max 32K' },
          { value: '4.0Ultra', label: 'Spark Ultra' },
          { value: 'spark-x', label: 'Spark X1.5（深度推理）' }
        ],
        minimax: [
          { value: 'MiniMax-M2.7', label: 'MiniMax-M2.7' },
          { value: 'MiniMax-M2.7-highspeed', label: 'MiniMax-M2.7-HighSpeed' },
          { value: 'MiniMax-M2.5', label: 'MiniMax-M2.5' },
          { value: 'MiniMax-M2.5-highspeed', label: 'MiniMax-M2.5-HighSpeed' },
          { value: 'MiniMax-M2.1', label: 'MiniMax-M2.1' }
        ],
        baichuan: [
          { value: 'Baichuan4-Turbo', label: 'Baichuan4-Turbo' },
          { value: 'Baichuan4-Air', label: 'Baichuan4-Air' },
          { value: 'Baichuan4', label: 'Baichuan4' },
          { value: 'Baichuan3-Turbo', label: 'Baichuan3-Turbo' },
          { value: 'Baichuan3-Turbo-128k', label: 'Baichuan3-Turbo-128K' }
        ],
        openrouter: [
          { value: 'openai/gpt-4o-mini', label: 'GPT-4o-mini (OpenAI)' },
          { value: 'openai/gpt-4o', label: 'GPT-4o (OpenAI)' },
          { value: 'anthropic/claude-sonnet-latest', label: 'Claude Sonnet (Anthropic)' },
          { value: 'deepseek/deepseek-chat', label: 'DeepSeek Chat' },
          { value: 'google/gemini-2.0-flash-001', label: 'Gemini 2.0 Flash (Google)' },
          { value: 'meta-llama/llama-3.3-70b-instruct', label: 'Llama 3.3 70B (Meta)' }
        ],
        custom: []
      }
    }
  },
  /** 计算属性定义 */
  computed: {
    currentModels() {
      return this.providerModels[this.form.provider] || []
    },
    dialogTitle() {
      const typeName = this.activeTab === '1' ? 'AI助手' : 'AI伴侣'
      return (this.isEdit ? '编辑' : '添加') + typeName + '配置'
    },
    systemPromptPlaceholder() {
      if (this.activeTab === '1') {
        return '设置AI助手的角色和行为，如：你是智联校园的智能助手，帮助用户搜索商品和帖子...'
      }
      return '设置AI伴侣的角色和行为，如：你是一个温暖有趣的AI伴侣，陪伴用户聊天...'
    }
  },
  /** 生命周期：组件创建时加载数据 */
  created() {
    this.loadData()
  },
  /** 组件方法定义 */
  methods: {
    onTabChange() {
      this.loadData()
    },
    /** 加载数据列表 */
    async loadData() {
      this.loading = true
      try {
        const configType = parseInt(this.activeTab)
        const res = await getAiConfigList(configType)
        if (res.code === 200) {
          this.configList = res.data || []
          this.activeConfigs = this.configList.filter(c => c.isActive)
        }
      } catch (e) {}
      this.loading = false
    },
    getProviderLabel(provider) {
      const map = {
        deepseek: 'DeepSeek', openai: 'OpenAI', qwen: '通义千问',
        wenxin: '文心一言', zhipu: '智谱AI', moonshot: 'Kimi',
        spark: '讯飞星火', minimax: 'MiniMax', baichuan: '百川',
        openrouter: 'OpenRouter', custom: '自定义'
      }
      return map[provider] || provider
    },
    getProviderTagType(provider) {
      const map = {
        deepseek: '', openai: 'success', qwen: 'warning',
        wenxin: 'danger', zhipu: 'info', moonshot: '',
        spark: 'success', minimax: 'warning', baichuan: 'danger',
        openrouter: 'info', custom: 'info'
      }
      return map[provider] || 'info'
    },
    onProviderChange(provider) {
      const defaults = this.providerDefaults[provider]
      if (defaults) {
        this.form.apiUrl = defaults.apiUrl
        if (!this.isEdit) {
          this.form.modelName = defaults.modelName
        }
      }
      const thinkingProviders = ['deepseek', 'zhipu', 'moonshot', 'spark']
      if (thinkingProviders.includes(provider)) {
        this.form.thinkingEnabled = true
        this.form.reasoningEffort = 'high'
      } else {
        this.form.thinkingEnabled = false
      }
    },
    fillDefaultApiUrl() {
      const defaults = this.providerDefaults[this.form.provider]
      if (defaults) {
        this.form.apiUrl = defaults.apiUrl
        this.form.modelName = defaults.modelName
      }
    },
    getDefaultSystemPrompt() {
      if (this.activeTab === '1') {
        return '你是智联校园的全能AI助手，具备以下能力：\n1. 搜索推荐：帮用户搜索商品、帖子、表白墙内容并推荐\n2. 数据分析：查看平台销售数据、趋势、用户统计，给出经营建议\n3. 功能引导：指导用户发布商品/帖子、查看订单、管理个人中心等操作\n4. 问题解答：回答校园生活、选课、考试、求职等常见问题\n5. 内容创作：帮用户写帖子、商品描述、表白文案等\n请用简洁友好的语气回答，优先基于平台数据推荐。'
      }
      return '你是一个温暖、有趣的AI伴侣，陪伴用户聊天。你的特点是：\n1. 语气亲切自然，像朋友一样交流\n2. 善于倾听，会关心用户的感受\n3. 可以聊生活、学习、情感等各种话题\n4. 回复简洁有趣，不要过长\n5. 适当使用语气词让对话更自然\n注意：你只负责聊天陪伴，不搜索平台数据。如果用户问平台相关内容（如商品、帖子等），建议他们使用AI助手搜索。'
    },
    openAddDialog() {
      this.isEdit = false
      const configType = parseInt(this.activeTab)
      this.form = {
        id: null,
        configName: '',
        configType: configType,
        provider: 'deepseek',
        apiUrl: 'https://api.deepseek.com',
        apiKey: '',
        modelName: 'deepseek-v4-flash',
        systemPrompt: this.getDefaultSystemPrompt(),
        temperature: 0.7,
        maxTokens: configType === 2 ? 512 : 2048,
        thinkingEnabled: configType === 1,
        reasoningEffort: 'high',
        streamEnabled: false,
        contextRounds: configType === 2 ? 10 : 2,
        remark: ''
      }
      this.dialogVisible = true
      this.$nextTick(() => { if (this.$refs.form) this.$refs.form.clearValidate() })
    },
    openEditDialog(row) {
      this.isEdit = true
      this.form = {
        id: row.id,
        configName: row.configName,
        configType: row.configType || parseInt(this.activeTab),
        provider: row.provider,
        apiUrl: row.apiUrl,
        apiKey: '',
        modelName: row.modelName,
        systemPrompt: row.systemPrompt || '',
        temperature: row.temperature || 0.7,
        maxTokens: row.maxTokens || 2048,
        thinkingEnabled: row.thinkingEnabled || false,
        reasoningEffort: row.reasoningEffort || 'high',
        streamEnabled: row.streamEnabled || false,
        contextRounds: row.contextRounds || 5,
        remark: row.remark || ''
      }
      this.dialogVisible = true
      this.$nextTick(() => { if (this.$refs.form) this.$refs.form.clearValidate() })
    },
    /** 提交表单 */
    submitForm() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        this.submitting = true
        try {
          
          this.form.configType = parseInt(this.activeTab)
          let res
          if (this.isEdit) {
            res = await updateAiConfig(this.form)
          } else {
            if (!this.form.apiKey) {
              this.$message.warning('请输入API密钥')
              this.submitting = false
              return
            }
            res = await addAiConfig(this.form)
          }
          if (res.code === 200) {
            this.$message.success(this.isEdit ? '更新成功' : '添加成功')
            this.dialogVisible = false
            this.loadData()
          } else {
            this.$message.error(res.msg || '操作失败')
          }
        } catch (e) {
          this.$message.error('操作失败')
        }
        this.submitting = false
      })
    },
    async testConnection() {
      if (!this.form.apiUrl || !this.form.modelName) {
        this.$message.warning('请先填写API地址和模型名称')
        return
      }
      if (!this.isEdit && !this.form.apiKey) {
        this.$message.warning('请先填写API密钥')
        return
      }
      this.testing = true
      try {
        const res = await testAiConnection({
          id: this.form.id || undefined,
          apiUrl: this.form.apiUrl,
          apiKey: this.form.apiKey || undefined,
          modelName: this.form.modelName,
          thinkingEnabled: this.form.thinkingEnabled,
          reasoningEffort: this.form.reasoningEffort
        })
        const resultData = res.data || res
        if (resultData && resultData.success) {
          this.testResult = { success: true, reply: resultData.reply || '', model: resultData.model || '', usage: resultData.usage || null, error: '' }
        } else {
          this.testResult = { success: false, reply: '', model: '', usage: null, error: resultData.reply || res.msg || '连接失败' }
        }
      } catch (e) {
        const errMsg = (e && e.message) || '请求失败'
        this.testResult = { success: false, reply: '', model: '', usage: null, error: errMsg }
      }
      this.testing = false
      this.testResultVisible = true
    },
    async toggleActive(row) {
      try {
        const res = await toggleAiConfigEnabled(row.id)
        if (res.code === 200) {
          this.$message.success(res.msg || '操作成功')
          this.loadData()
        } else {
          this.$message.error(res.msg || '操作失败')
        }
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    async toggleField(row, field) {
      try {
        const data = { id: row.id, configType: row.configType }
        data[field] = !row[field]
        const res = await updateAiConfig(data)
        if (res.code === 200) {
          this.$message.success('更新成功')
          this.loadData()
        } else {
          this.$message.error(res.msg || '操作失败')
        }
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    async deleteConfig(row) {
      try {
        await this.$confirm('确定删除配置「' + row.configName + '」？', '提示', { type: 'warning' })
        const res = await deleteAiConfig(row.id)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.loadData()
        } else {
          this.$message.error(res.msg || '删除失败')
        }
      } catch (e) {
        if (e !== 'cancel') this.$message.error('删除失败')
      }
    }
  }
}
</script>

<style scoped>
/* 组件局部样式 */
.config-section {
  margin-bottom: 20px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #ebeef5;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}
.config-section:hover {
  border-color: #d0d3d9;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.config-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
.config-section-title i {
  margin-right: 4px;
  color: #409EFF;
}
.form-hint {
  color: #b0b4bb;
  font-size: 11px;
  line-height: 1.4;
  margin-top: 2px;
}
.adv-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px 20px;
}
.adv-item {
  background: #fafbfc;
  border: 1px solid #f0f2f5;
  border-radius: 12px;
  padding: 12px 14px;
  transition: border-color 0.3s ease, box-shadow 0.3s ease, transform 0.3s ease;
}
.adv-item:hover {
  border-color: #c0c4cc;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}
.adv-label {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}
.adv-desc {
  color: #b0b4bb;
  font-size: 11px;
  line-height: 1.4;
  margin-top: 6px;
}
::v-deep .el-tag {
  border-radius: 6px;
  transition: transform 0.3s ease;
}
::v-deep .el-tag:hover {
  transform: translateY(-1px);
}
::v-deep .el-switch {
  transition: opacity 0.3s ease;
}
::v-deep .el-button {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
::v-deep .el-button:hover {
  transform: translateY(-1px);
}
</style>
