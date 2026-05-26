package com.mrxu.stucomplarear2.controller;

import com.mrxu.stucomplarear2.service.RecycleBinService;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/recycle-bin")
public class RecycleBinController {

    @Autowired
    private RecycleBinService recycleBinService;

    @ApiOperation("移入回收站")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/move")
    public Result moveToRecycleBin(@RequestBody java.util.Map<String, String> params, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        String itemType = params.get("itemType");
        String itemId = params.get("itemId");
        return recycleBinService.moveToRecycleBin(userId, itemType, itemId);
    }

    @ApiOperation("我的回收站列表")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/list")
    public Result listMyRecycleBin(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) String itemType,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(defaultValue = "deleteTime") String sortBy,
                                   @RequestParam(defaultValue = "desc") String sortOrder,
                                   HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        return recycleBinService.listMyRecycleBin(userId, pageNum, pageSize, itemType, keyword, sortBy, sortOrder);
    }

    @ApiOperation("恢复项目")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/restore/{id}")
    public Result restoreItem(@PathVariable("id") String id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        return recycleBinService.restoreItem(userId, id);
    }

    @ApiOperation("彻底删除")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/{id}")
    public Result permanentlyDelete(@PathVariable("id") String id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        return recycleBinService.permanentlyDelete(userId, id);
    }
}
