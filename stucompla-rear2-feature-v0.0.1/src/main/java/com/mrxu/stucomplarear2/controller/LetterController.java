package com.mrxu.stucomplarear2.controller;


import com.mrxu.stucomplarear2.dto.LetterAddDto;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Mr.Xu
 * @since 2022-04-28
 */
@RestController
@RequestMapping("/letter")
public class LetterController {

    @Resource
    private LetterService letterService;

    @ApiOperation("发送私信")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/send")
    public Result add(@RequestBody LetterAddDto letterAddDto, HttpServletRequest request) {
        Result result = letterService.add(letterAddDto, request);
        return result;
    }

    @ApiOperation("系统发送通知")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/sendNotice")
    public Result addNotice(@RequestBody LetterAddDto letterAddDto) {
        letterService.sendSystemNotification(letterAddDto.getReceiverId(), letterAddDto.getLetterDetail(), "system");
        return Result.succ("success");
    }

    @ApiOperation("获取我的私信列表")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/mySessionList")
    public Result getMySessionList(HttpServletRequest request) {
        Result result = letterService.getMySessionList(request);
        return result;
    }

    @ApiOperation("获取当前会话的私信列表")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/letterList/{sessionId}")
    public Result getLetterListBySessionId(@PathVariable("sessionId") String sessionId, HttpServletRequest request) {
        Result result = letterService.getLetterListBySessionId(sessionId, request);
        return result;
    }

    @ApiOperation("获取我的通知列表")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/myNoticeList")
    public Result getMyNoticeList(HttpServletRequest request) {
        Result result = letterService.getMyNoticeList(request);
        return result;
    }

    @ApiOperation("获取我的未读总数")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/myUnReadTotal")
    public Result getMyUnReadTotal(HttpServletRequest request) {
        Result result = letterService.getMyUnReadTotal(request);
        return result;
    }

    @ApiOperation("获取我的私信未读数")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/myUnReadLetterTotal")
    public Result getMyUnReadLetterTotal(HttpServletRequest request) {
        Result result = letterService.getMyUnReadLetterTotal(request);
        return result;
    }

    @ApiOperation("获取我的通知未读数")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/myUnReadNoticeTotal")
    public Result getMyUnReadNoticeTotal(HttpServletRequest request) {
        Result result = letterService.getMyUnReadNoticeTotal(request);
        return result;
    }

    @ApiOperation("获取消息列表(按类型分组，分页)")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/myMessageList")
    public Result myMessageList(@RequestParam(required = false) String type,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                HttpServletRequest request) {
        return letterService.myMessageList(type, pageNum, pageSize, request);
    }

    @ApiOperation("标记单条消息已读")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/markRead/{letterId}")
    public Result markRead(@PathVariable("letterId") String letterId, HttpServletRequest request) {
        return letterService.markRead(letterId, request);
    }

    @ApiOperation("标记全部消息已读")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/markAllRead")
    public Result markAllRead(@RequestParam(required = false) String type, HttpServletRequest request) {
        return letterService.markAllRead(type, request);
    }

    @ApiOperation("按类型获取未读数")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/unreadCountByType")
    public Result unreadCountByType(HttpServletRequest request) {
        return letterService.unreadCountByType(request);
    }

}
