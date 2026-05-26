package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.entity.AiConfig;
import com.mrxu.stucomplarear2.utils.response.Result;

public interface AiConfigService extends IService<AiConfig> {

    /**
     * 获取当前激活的AI配置
     */
    Result getActiveConfig();

    /**
     * 根据配置类型获取当前激活的AI配置
     * @param configType 1=AI助手, 2=AI伴侣
     */
    Result getActiveConfigByType(Integer configType);

    /**
     * 获取所有AI配置列表
     */
    Result getConfigList();

    /**
     * 根据配置类型获取AI配置列表
     * @param configType 1=AI助手, 2=AI伴侣
     */
    Result getConfigListByType(Integer configType);

    /**
     * 添加AI配置
     */
    Result addConfig(AiConfig aiConfig);

    /**
     * 更新AI配置
     */
    Result updateConfig(AiConfig aiConfig);

    /**
     * 删除AI配置
     */
    Result deleteConfig(Integer id);

    /**
     * 激活指定配置（同时取消其他配置的激活状态）
     */
    Result activateConfig(Integer id);

    /**
     * 切换AI功能启用/禁用
     */
    Result toggleEnabled(Integer id);
}
