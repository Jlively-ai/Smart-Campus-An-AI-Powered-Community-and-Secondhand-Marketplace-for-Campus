package com.mrxu.stucomplarear2.controller;

import com.mrxu.stucomplarear2.entity.OrderReview;
import com.mrxu.stucomplarear2.service.OrderReviewService;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/order-review")
public class OrderReviewController {

    @Autowired
    private OrderReviewService orderReviewService;

    @ApiOperation("添加订单评价")
    @RequiresRoles("user")
    @PostMapping("/add")
    public Result add(@RequestBody OrderReview review, HttpServletRequest request) {
        return orderReviewService.addReview(review, request);
    }

    @ApiOperation("管理端评价列表")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/list")
    public Result list(Integer pageNum, Integer pageSize) {
        return orderReviewService.listByAdmin(pageNum, pageSize);
    }

    @ApiOperation("评价总数")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/getReviewTotal")
    public Result getReviewTotal() {
        return orderReviewService.getReviewTotal();
    }

    @ApiOperation("根据订单ID获取评价")
    @GetMapping("/getByOrder/{orderId}")
    public Result getByOrder(@PathVariable("orderId") String orderId) {
        return orderReviewService.getReviewByOrderId(orderId);
    }

    @ApiOperation("卖家回复评价")
    @RequiresRoles("user")
    @PostMapping("/reply")
    public Result reply(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String orderId = params.get("orderId");
        String reply = params.get("reply");
        if (orderId == null || orderId.isEmpty() || reply == null || reply.isEmpty()) {
            return Result.fail("参数错误");
        }
        return orderReviewService.replyReview(orderId, reply, request);
    }
}
