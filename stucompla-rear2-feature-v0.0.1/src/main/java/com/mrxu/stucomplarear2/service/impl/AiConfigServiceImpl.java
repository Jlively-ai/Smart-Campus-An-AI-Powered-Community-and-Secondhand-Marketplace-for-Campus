package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.entity.AiConfig;
import com.mrxu.stucomplarear2.mapper.AiConfigMapper;
import com.mrxu.stucomplarear2.service.AiConfigService;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AiConfigServiceImpl extends ServiceImpl<AiConfigMapper, AiConfig> implements AiConfigService {

    @Override
    public Result getActiveConfig() {
        QueryWrapper<AiConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("is_active", true);
        AiConfig config = this.getOne(wrapper);
        if (config != null) {
            // 脱敏：隐藏API Key中间部分
            config.setApiKey(maskApiKey(config.getApiKey()));
        }
        return Result.succ(config);
    }

    @Override
    public Result getActiveConfigByType(Integer configType) {
        QueryWrapper<AiConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("is_active", true).eq("config_type", configType);
        AiConfig config = this.getOne(wrapper);
        if (config != null) {
            config.setApiKey(maskApiKey(config.getApiKey()));
        }
        return Result.succ(config);
    }

    @Override
    public Result getConfigList() {
        QueryWrapper<AiConfig> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("is_active").orderByDesc("create_time");
        List<AiConfig> list = this.list(wrapper);
        // 脱敏处理
        for (AiConfig config : list) {
            config.setApiKey(maskApiKey(config.getApiKey()));
        }
        return Result.succ(list);
    }

    @Override
    public Result getConfigListByType(Integer configType) {
        QueryWrapper<AiConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("config_type", configType).orderByDesc("is_active").orderByDesc("create_time");
        List<AiConfig> list = this.list(wrapper);
        for (AiConfig config : list) {
            config.setApiKey(maskApiKey(config.getApiKey()));
        }
        return Result.succ(list);
    }

    @Override
    @Transactional
    public Result addConfig(AiConfig aiConfig) {
        aiConfig.setCreateTime(new Date());
        aiConfig.setUpdateTime(new Date());
        if (aiConfig.getIsActive() == null) {
            aiConfig.setIsActive(false);
        }
        if (aiConfig.getEnabled() == null) {
            aiConfig.setEnabled(true);
        }
        if (aiConfig.getTemperature() == null) {
            aiConfig.setTemperature(0.7);
        }
        if (aiConfig.getMaxTokens() == null) {
            aiConfig.setMaxTokens(2048);
        }
        if (aiConfig.getThinkingEnabled() == null) {
            aiConfig.setThinkingEnabled(false);
        }
        if (aiConfig.getReasoningEffort() == null) {
            aiConfig.setReasoningEffort("high");
        }
        if (aiConfig.getStreamEnabled() == null) {
            aiConfig.setStreamEnabled(false);
        }
        if (aiConfig.getContextRounds() == null) {
            aiConfig.setContextRounds(5);
        }
        if (aiConfig.getConfigType() == null) {
            aiConfig.setConfigType(1);
        }
        // 同类型允许多个激活，用户可自行选择
        this.save(aiConfig);
        return Result.succ("添加成功");
    }

    @Override
    @Transactional
    public Result updateConfig(AiConfig aiConfig) {
        aiConfig.setUpdateTime(new Date());
        // 如果API Key是脱敏的或为空，不更新该字段（编辑时留空表示不修改）
        if (aiConfig.getApiKey() == null || aiConfig.getApiKey().isEmpty() || aiConfig.getApiKey().contains("****")) {
            aiConfig.setApiKey(null);
        }
        // 同类型允许多个激活，用户可自行选择
        this.updateById(aiConfig);
        return Result.succ("更新成功");
    }

    @Override
    @Transactional
    public Result deleteConfig(Integer id) {
        AiConfig config = this.getById(id);
        if (config == null) {
            return Result.fail("配置不存在");
        }
        this.removeById(id);
        // 删除后ID前移，不留断层
        this.baseMapper.shiftIdsAfterDelete(id);
        this.baseMapper.resetAutoIncrement();
        return Result.succ("删除成功");
    }

    @Override
    @Transactional
    public Result activateConfig(Integer id) {
        AiConfig config = this.getById(id);
        if (config == null) {
            return Result.fail("配置不存在");
        }
        // 同类型允许多个激活，直接激活
        config.setIsActive(true);
        config.setUpdateTime(new Date());
        this.updateById(config);
        return Result.succ("已激活");
    }

    @Override
    @Transactional
    public Result toggleEnabled(Integer id) {
        AiConfig config = this.getById(id);
        if (config == null) {
            return Result.fail("配置不存在");
        }
        // 同类型允许多个激活，直接切换当前配置状态
        config.setIsActive(!Boolean.TRUE.equals(config.getIsActive()));
        config.setUpdateTime(new Date());
        this.updateById(config);
        return Result.succ(config.getIsActive() ? "已启用" : "已关闭");
    }

    /**
     * API Key脱敏：只显示前4位和后4位
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return apiKey != null ? "****" : null;
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
