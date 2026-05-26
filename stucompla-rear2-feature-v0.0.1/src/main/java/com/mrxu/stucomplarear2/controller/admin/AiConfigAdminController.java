package com.mrxu.stucomplarear2.controller.admin;

import com.mrxu.stucomplarear2.entity.AiConfig;
import com.mrxu.stucomplarear2.service.AiConfigService;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI配置管理控制器（管理员）
 * 需要 ai_manage 权限才能访问
 */
@RestController
@RequestMapping("/ai-config")
public class AiConfigAdminController {

    @Autowired
    private AiConfigService aiConfigService;

    @ApiOperation("获取AI配置列表")
    @RequiresPermissions("ai_manage")
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Integer configType) {
        if (configType != null) {
            return aiConfigService.getConfigListByType(configType);
        }
        return aiConfigService.getConfigList();
    }

    @ApiOperation("获取当前激活的AI配置")
    @RequiresPermissions("ai_manage")
    @GetMapping("/active")
    public Result getActive(@RequestParam(required = false) Integer configType) {
        if (configType != null) {
            return aiConfigService.getActiveConfigByType(configType);
        }
        return aiConfigService.getActiveConfig();
    }

    @ApiOperation("添加AI配置")
    @RequiresPermissions("ai_manage")
    @PostMapping("/add")
    public Result add(@RequestBody AiConfig aiConfig) {
        return aiConfigService.addConfig(aiConfig);
    }

    @ApiOperation("更新AI配置")
    @RequiresPermissions("ai_manage")
    @PostMapping("/update")
    public Result update(@RequestBody AiConfig aiConfig) {
        return aiConfigService.updateConfig(aiConfig);
    }

    @ApiOperation("删除AI配置")
    @RequiresPermissions("ai_manage")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        return aiConfigService.deleteConfig(id);
    }

    @ApiOperation("激活指定AI配置")
    @RequiresPermissions("ai_manage")
    @PostMapping("/activate/{id}")
    public Result activate(@PathVariable("id") Integer id) {
        return aiConfigService.activateConfig(id);
    }

    @ApiOperation("切换AI配置启用/禁用")
    @RequiresPermissions("ai_manage")
    @PostMapping("/toggle/{id}")
    public Result toggle(@PathVariable("id") Integer id) {
        return aiConfigService.toggleEnabled(id);
    }
}
