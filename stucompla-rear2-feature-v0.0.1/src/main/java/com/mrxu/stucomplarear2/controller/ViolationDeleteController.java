package com.mrxu.stucomplarear2.controller;

import com.mrxu.stucomplarear2.service.ViolationDeleteService;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/violation-delete")
public class ViolationDeleteController {

    @Autowired
    private ViolationDeleteService violationDeleteService;

    @ApiOperation("我的违规删除列表")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/my")
    public Result listMyViolations(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) String itemType,
                                   @RequestParam(required = false) Integer appealState,
                                   @RequestParam(defaultValue = "createTime") String sortBy,
                                   @RequestParam(defaultValue = "desc") String sortOrder,
                                   HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        return violationDeleteService.listMyViolations(userId, pageNum, pageSize, itemType, appealState, sortBy, sortOrder);
    }

    @ApiOperation("管理员-违规删除列表")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/list")
    public Result listAllViolations(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) String userId,
                                    @RequestParam(required = false) String itemType,
                                    @RequestParam(required = false) Integer appealState,
                                    @RequestParam(defaultValue = "createTime") String sortBy,
                                    @RequestParam(defaultValue = "desc") String sortOrder) {
        return violationDeleteService.listAllViolations(pageNum, pageSize, userId, itemType, appealState, sortBy, sortOrder);
    }

    @ApiOperation("用户申诉")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/appeal")
    public Result appeal(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        String id = params.get("id");
        String appealReason = params.get("appealReason");
        return violationDeleteService.appeal(userId, id, appealReason);
    }

    @ApiOperation("管理员处理申诉")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/handleAppeal")
    public Result handleAppeal(@RequestBody Map<String, Object> params) {
        String id = (String) params.get("id");
        Integer appealState = (Integer) params.get("appealState");
        String appealResult = (String) params.get("appealResult");
        return violationDeleteService.handleAppeal(id, appealState, appealResult);
    }
}
