package com.mrxu.stucomplarear2.controller;

import com.mrxu.stucomplarear2.service.FollowService;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @ApiOperation("关注用户")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/add/{userId}")
    public Result follow(@PathVariable("userId") String followingId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String followerId = JWTUtil.getUserId(token);
        return followService.follow(followerId, followingId);
    }

    @ApiOperation("取消关注")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/cancel/{userId}")
    public Result unfollow(@PathVariable("userId") String followingId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String followerId = JWTUtil.getUserId(token);
        return followService.unfollow(followerId, followingId);
    }

    @ApiOperation("检查是否关注")
    @GetMapping("/check/{userId}")
    public Result checkFollow(@PathVariable("userId") String followingId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) return Result.succ(false);
        try {
            String followerId = JWTUtil.getUserId(token);
            return followService.checkFollow(followerId, followingId);
        } catch (Exception e) {
            return Result.succ(false);
        }
    }

    @ApiOperation("检查对方是否关注了我")
    @GetMapping("/checkFollowsMe/{userId}")
    public Result checkFollowsMe(@PathVariable("userId") String followerId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) return Result.succ(false);
        try {
            String myId = JWTUtil.getUserId(token);
            return followService.checkFollow(followerId, myId);
        } catch (Exception e) {
            return Result.succ(false);
        }
    }

    @ApiOperation("检查是否互相关注")
    @GetMapping("/checkMutual/{userId}")
    public Result checkMutual(@PathVariable("userId") String otherUserId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) return Result.succ(false);
        try {
            String myId = JWTUtil.getUserId(token);
            Result iFollow = followService.checkFollow(myId, otherUserId);
            Result followsMe = followService.checkFollow(otherUserId, myId);
            boolean mutual = Boolean.TRUE.equals(iFollow.getData()) && Boolean.TRUE.equals(followsMe.getData());
            return Result.succ(mutual);
        } catch (Exception e) {
            return Result.succ(false);
        }
    }

    @ApiOperation("粉丝列表")
    @GetMapping("/followers/{userId}")
    public Result followerList(@PathVariable("userId") String userId, Integer pageNum, Integer pageSize) {
        return followService.getFollowerList(userId, pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
    }

    @ApiOperation("关注列表")
    @GetMapping("/following/{userId}")
    public Result followingList(@PathVariable("userId") String userId, Integer pageNum, Integer pageSize) {
        return followService.getFollowingList(userId, pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
    }

    @ApiOperation("粉丝数")
    @GetMapping("/followerCount/{userId}")
    public Result followerCount(@PathVariable("userId") String userId) {
        return followService.getFollowerCount(userId);
    }

    @ApiOperation("关注数")
    @GetMapping("/followingCount/{userId}")
    public Result followingCount(@PathVariable("userId") String userId) {
        return followService.getFollowingCount(userId);
    }
}
