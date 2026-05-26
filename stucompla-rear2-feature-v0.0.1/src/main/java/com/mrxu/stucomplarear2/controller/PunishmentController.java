package com.mrxu.stucomplarear2.controller;

import com.mrxu.stucomplarear2.dto.PunishmentDto;
import com.mrxu.stucomplarear2.entity.Punishment;
import com.mrxu.stucomplarear2.service.PunishmentService;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/punishment")
public class PunishmentController {

    @Autowired
    private PunishmentService punishmentService;

    @ApiOperation("创建处罚（管理员）")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/create")
    public Result createPunishment(@RequestBody PunishmentDto punishmentDto, HttpServletRequest request) {
        return punishmentService.createPunishment(punishmentDto, request);
    }

    @ApiOperation("获取处罚列表（管理员）")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/list")
    public Result getPunishmentList(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) String userId,
                                    @RequestParam(required = false) String type,
                                    @RequestParam(required = false) Integer status) {
        Map<String, Object> map = punishmentService.findPunishmentList(pageNum, pageSize, userId, type, status);
        return Result.succ(map);
    }

    @ApiOperation("解除处罚（管理员）")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/lift/{punishmentId}")
    public Result liftPunishment(@PathVariable String punishmentId) {
        return punishmentService.liftPunishment(punishmentId);
    }

    @ApiOperation("检查当前用户是否被禁言")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/checkMute")
    public Result checkMute(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(jwt);
        boolean muted = punishmentService.isUserMuted(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("muted", muted);
        if (muted) {
            result.put("reason", punishmentService.getMuteReason(userId));
        }
        return Result.succ(result);
    }

    @ApiOperation("获取用户公开处罚记录")
    @GetMapping("/publicList/{userId}")
    public Result getPublicPunishments(@PathVariable String userId) {
        List<Punishment> list = punishmentService.getPublicPunishments(userId);
        return Result.succ(list);
    }

    @ApiOperation("申诉处罚（用户）")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/appeal")
    public Result appeal(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String punishmentId = params.get("punishmentId");
        String appealReason = params.get("appealReason");
        if (punishmentId == null || punishmentId.isEmpty()) {
            return Result.fail("处罚ID不能为空");
        }
        if (appealReason == null || appealReason.isEmpty()) {
            return Result.fail("申诉原因不能为空");
        }
        String jwt = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(jwt);
        return punishmentService.appeal(punishmentId, appealReason, userId);
    }

    @ApiOperation("处理申诉（管理员）")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/handleAppeal")
    public Result handleAppeal(@RequestBody Map<String, Object> params) {
        String punishmentId = (String) params.get("punishmentId");
        Integer appealState = (Integer) params.get("appealState"); // 1=通过, 2=驳回
        String appealResult = (String) params.get("appealResult");
        if (punishmentId == null || punishmentId.isEmpty()) {
            return Result.fail("处罚ID不能为空");
        }
        if (appealState == null || (appealState != 1 && appealState != 2)) {
            return Result.fail("申诉状态参数错误，1=通过，2=驳回");
        }
        return punishmentService.handleAppeal(punishmentId, appealState, appealResult);
    }

    @ApiOperation("查询我的处罚记录（用户）")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/my")
    public Result getMyPunishments(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        List<Punishment> list = punishmentService.getMyPunishments(userId);
        return Result.succ(list);
    }
}
