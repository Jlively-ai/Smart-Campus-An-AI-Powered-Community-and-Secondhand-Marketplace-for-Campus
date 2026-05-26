package com.mrxu.stucomplarear2.controller;


import com.mrxu.stucomplarear2.dto.CommentDto;
import com.mrxu.stucomplarear2.dto.CommentFindDto;
import com.mrxu.stucomplarear2.entity.Comment;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.entity.ViolationDelete;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.CommentService;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.service.ViolationDeleteService;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Mr.Xu
 * @since 2021-12-27
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private LetterService letterService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ViolationDeleteService violationDeleteService;

    @ApiOperation("评论")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/create")
    public Result createComment(HttpServletRequest request, @RequestBody CommentDto commentDto) {
        return commentService.createComment(request, commentDto);
    }

    @ApiOperation("用户删除评论")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/{commentId}")
    public Result deleteCommentByUser(@PathVariable("commentId") String commentId, HttpServletRequest request) {
        return commentService.deleteCommentByUser(commentId, request);
    }

    @ApiOperation("管理员删除评论")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @DeleteMapping("deleteByAdmin")
    public Result deleteCommentByAdmin(String commentId, String cause, HttpServletRequest request) {
        Comment comment = commentService.getById(commentId);
        if (comment == null) return Result.fail("评论不存在");
        String token = request.getHeader("Authorization");
        String handlerId = JWTUtil.getUserId(token);
        return violationDeleteService.moveViolation(comment.getUserId(), "comment", commentId, cause, handlerId, "delete");
    }

    @ApiOperation("帖子的评论列表")
    @GetMapping("/list/{postId}/{page}/{size}")
    public Result listComment(@PathVariable("postId") String postId, @PathVariable("page") int page, @PathVariable("size") int size) {

        return commentService.listCommentFromPost(postId, page, size);
    }

    @ApiOperation("表白墙的评论列表")
    @GetMapping("/wallList/{wallId}/{page}/{size}")
    public Result listWallComment(@PathVariable("wallId") String wallId, @PathVariable("page") int page, @PathVariable("size") int size) {
        return commentService.listCommentFromPost(wallId, page, size, "wall");
    }

    @ApiOperation("评论列表(管理员页面)")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/listByAdmin")
    public Result listCommentByAdmin(CommentFindDto commentFindDto) {
        return commentService.listComment(commentFindDto);
    }

    @ApiOperation("我的评论列表")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/myList/{page}/{size}")
    public Result getMyList(@PathVariable("page") Integer page, @PathVariable("size") Integer size, HttpServletRequest request) {

        return commentService.getMyList(page, size, request);
    }

    @ApiOperation("获取评论总数")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/getCommentTotal")
    public Result getCommentTotal() {
        Result result = commentService.getCommentTotal();
        return result;
    }

    @ApiOperation("我收到的评论")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/received/{page}/{size}")
    public Result getReceivedComments(@PathVariable("page") Integer page, @PathVariable("size") Integer size, HttpServletRequest request) {
        return commentService.getReceivedComments(page, size, request);
    }

    @ApiOperation("点赞评论")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/like/{commentId}")
    public Result likeComment(@PathVariable("commentId") String commentId, HttpServletRequest request) {
        Comment comment = commentService.getById(commentId);
        if (comment == null) return Result.fail("评论不存在");
        comment.setLikeNum(comment.getLikeNum() == null ? 1 : comment.getLikeNum() + 1);
        commentService.updateById(comment);
        // 发送点赞通知
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        if (!userId.equals(comment.getUserId())) {
            User liker = userMapper.selectById(userId);
            String nickname = liker != null ? (liker.getNickname() != null ? liker.getNickname() : liker.getUsername()) : "用户";
            letterService.sendSystemNotification(comment.getUserId(), nickname + " 赞了你的评论", "like", "comment", commentId);
        }
        return Result.succ(comment.getLikeNum());
    }

    @ApiOperation("取消点赞")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/unlike/{commentId}")
    public Result unlikeComment(@PathVariable("commentId") String commentId) {
        Comment comment = commentService.getById(commentId);
        if (comment == null) return Result.fail("评论不存在");
        comment.setLikeNum(comment.getLikeNum() == null || comment.getLikeNum() <= 0 ? 0 : comment.getLikeNum() - 1);
        commentService.updateById(comment);
        return Result.succ(comment.getLikeNum());
    }

    @ApiOperation("管理员锁定评论")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/lockComment")
    public Result lockComment(String commentId, String cause, HttpServletRequest request) {
        return commentService.lockComment(commentId, cause, request);
    }

    @ApiOperation("管理员解锁评论")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/unlockComment")
    public Result unlockComment(String commentId) {
        return commentService.unlockComment(commentId);
    }

    @ApiOperation("审核评论")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/auditComment")
    public Result auditComment(String commentId, Integer auditState, String auditFailedCause) {
        return commentService.auditComment(commentId, auditState, auditFailedCause);
    }

}
