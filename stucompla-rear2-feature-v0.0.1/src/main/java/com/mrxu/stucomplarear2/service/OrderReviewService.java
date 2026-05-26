package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.entity.OrderReview;
import com.mrxu.stucomplarear2.utils.response.Result;

import javax.servlet.http.HttpServletRequest;

public interface OrderReviewService extends IService<OrderReview> {
    Result addReview(OrderReview review, HttpServletRequest request);
    Result listByAdmin(Integer pageNum, Integer pageSize);
    Result getReviewTotal();
    Result getReviewByOrderId(String orderId);
    Result replyReview(String orderId, String reply, HttpServletRequest request);
}
