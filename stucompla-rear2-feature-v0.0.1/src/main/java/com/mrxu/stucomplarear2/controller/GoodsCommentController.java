package com.mrxu.stucomplarear2.controller;

import com.mrxu.stucomplarear2.entity.GoodsComment;
import com.mrxu.stucomplarear2.service.GoodsCommentService;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/goods-comment")
public class GoodsCommentController {

    @Autowired
    private GoodsCommentService goodsCommentService;

    @ApiOperation("添加商品评论")
    @RequiresRoles("user")
    @PostMapping("/add")
    public Result add(@RequestBody GoodsComment comment, HttpServletRequest request) {
        return goodsCommentService.addComment(comment, request);
    }

    @ApiOperation("删除自己的商品评论")
    @RequiresRoles("user")
    @DeleteMapping("/{commentId}")
    public Result delete(@PathVariable String commentId, HttpServletRequest request) {
        return goodsCommentService.deleteComment(commentId, request);
    }

    @ApiOperation("管理员删除商品评论")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/deleteByAdmin")
    public Result deleteByAdmin(String commentId, String cause) {
        return goodsCommentService.deleteByAdmin(commentId, cause);
    }

    @ApiOperation("管理员锁定商品评论")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/lockComment")
    public Result lockComment(String commentId, String cause, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String handlerId = JWTUtil.getUserId(token);
        return goodsCommentService.lockComment(commentId, cause, handlerId);
    }

    @ApiOperation("管理员解锁商品评论")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/unlockComment")
    public Result unlockComment(String commentId) {
        return goodsCommentService.unlockComment(commentId);
    }

    @ApiOperation("商品评论列表")
    @GetMapping("/list/{goodsId}")
    public Result list(@PathVariable String goodsId, Integer pageNum, Integer pageSize) {
        return goodsCommentService.listByGoodsId(goodsId, pageNum, pageSize);
    }

    @ApiOperation("管理端商品评论列表")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/listByAdmin")
    public Result listByAdmin(Integer pageNum, Integer pageSize) {
        return goodsCommentService.listByAdmin(pageNum, pageSize);
    }

    @ApiOperation("商品评论总数")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/getCommentTotal")
    public Result getCommentTotal() {
        return goodsCommentService.getCommentTotal();
    }
}
