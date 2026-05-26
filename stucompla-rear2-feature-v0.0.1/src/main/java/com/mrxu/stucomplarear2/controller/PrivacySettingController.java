package com.mrxu.stucomplarear2.controller;

import com.mrxu.stucomplarear2.entity.PrivacySetting;
import com.mrxu.stucomplarear2.service.PrivacySettingService;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/privacy")
public class PrivacySettingController {

    @Autowired
    private PrivacySettingService privacySettingService;

    @ApiOperation("获取我的隐私设置")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/my")
    public Result getMyPrivacy(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        PrivacySetting setting = privacySettingService.getMyPrivacy(userId);
        return Result.succ(setting);
    }

    @ApiOperation("更新隐私设置")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/update")
    public Result updatePrivacy(@RequestBody PrivacySetting setting, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        setting.setUserId(userId);
        return privacySettingService.updatePrivacy(setting);
    }

    @ApiOperation("检查可见性")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/check")
    public Result checkVisibility(@RequestParam String targetUserId, @RequestParam String field, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String viewerUserId = JWTUtil.getUserId(token);
        String viewerRole = JWTUtil.getRole(token);
        boolean visible = privacySettingService.checkVisibility(targetUserId, viewerUserId, field, viewerRole);
        return Result.succ(visible);
    }
}
