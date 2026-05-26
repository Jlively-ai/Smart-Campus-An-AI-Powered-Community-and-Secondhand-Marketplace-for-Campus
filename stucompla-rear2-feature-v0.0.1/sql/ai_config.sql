-- AI配置表建表语句
-- 在MySQL中执行此SQL创建ai_config表

CREATE TABLE IF NOT EXISTS `ai_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_name` varchar(100) DEFAULT NULL COMMENT '配置名称',
  `provider` varchar(50) DEFAULT NULL COMMENT '大模型提供商(openai/deepseek/qwen/wenxin/zhipu/custom)',
  `api_url` varchar(500) DEFAULT NULL COMMENT 'API基础地址(如:https://api.deepseek.com)',
  `api_key` varchar(500) DEFAULT NULL COMMENT 'API密钥',
  `model_name` varchar(100) DEFAULT NULL COMMENT '模型名称(如:deepseek-v4-flash,deepseek-v4-pro,gpt-3.5-turbo)',
  `system_prompt` text COMMENT '系统提示词',
  `temperature` double DEFAULT 0.7 COMMENT '温度参数(0-2,思考模式下不生效)',
  `max_tokens` int DEFAULT 2048 COMMENT '最大输出token数',
  `thinking_enabled` tinyint(1) DEFAULT 0 COMMENT '是否启用思考模式(DeepSeek等支持)',
  `reasoning_effort` varchar(20) DEFAULT 'high' COMMENT '思考强度(low/high/max)',
  `stream_enabled` tinyint(1) DEFAULT 0 COMMENT '是否启用流式输出',
  `context_rounds` int DEFAULT 5 COMMENT '上下文轮数(多轮对话保留的历史轮数)',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用(1启用 0禁用)',
  `is_active` tinyint(1) DEFAULT 0 COMMENT '是否为当前使用的配置(1是 0否)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI大模型配置表';
