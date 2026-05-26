package com.mrxu.stucomplarear2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * AI配置实体
 * 存储大模型API相关配置信息
 * 兼容OpenAI/DeepSeek/通义千问等OpenAI兼容格式的API
 */
@Data
@TableName("ai_config")
public class AiConfig {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 配置名称（如：DeepSeek-默认、通义千问等）
     */
    private String configName;

    /**
     * 大模型提供商（openai/deepseek/qwen/wenxin/zhipu/custom）
     */
    private String provider;

    /**
     * API基础地址（如：https://api.deepseek.com）
     */
    private String apiUrl;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 模型名称（如：deepseek-v4-flash, deepseek-v4-pro, gpt-3.5-turbo, qwen-turbo等）
     */
    private String modelName;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 温度参数（0-2，越高越随机，思考模式下不生效）
     */
    private Double temperature;

    /**
     * 最大输出token数
     */
    private Integer maxTokens;

    /**
     * 是否启用思考模式（DeepSeek等模型支持）
     */
    private Boolean thinkingEnabled;

    /**
     * 思考强度（low/high/max），思考模式下生效
     */
    private String reasoningEffort;

    /**
     * 是否启用流式输出
     */
    private Boolean streamEnabled;

    /**
     * 上下文轮数（多轮对话时保留的历史轮数）
     */
    private Integer contextRounds;

    /**
     * 配置类型（1=AI助手, 2=AI伴侣）
     */
    private Integer configType;

    /**
     * 是否启用（1启用 0禁用）
     */
    private Boolean enabled;

    /**
     * 是否为当前使用的配置（1是 0否）
     */
    private Boolean isActive;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;
}
