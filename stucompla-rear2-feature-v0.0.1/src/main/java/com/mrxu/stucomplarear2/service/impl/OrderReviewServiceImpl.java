package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.entity.MarketOrder;
import com.mrxu.stucomplarear2.entity.OrderReview;
import com.mrxu.stucomplarear2.entity.Goods;
import com.mrxu.stucomplarear2.mapper.MarketOrderMapper;
import com.mrxu.stucomplarear2.mapper.OrderReviewMapper;
import com.mrxu.stucomplarear2.mapper.GoodsMapper;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.service.OrderReviewService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderReviewServiceImpl extends ServiceImpl<OrderReviewMapper, OrderReview> implements OrderReviewService {

    @Autowired
    private MarketOrderMapper marketOrderMapper;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private LetterService letterService;

    @Override
    public Result addReview(OrderReview review, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        review.setUserId(userId);
        review.setReviewId(IdGenerator.generateId(IdGenerator.ORDER_REVIEW));
        review.setCreateTime(new Date());

        // 检查是否已有评价（追评场景），如果有则更新而非新增
        QueryWrapper<OrderReview> existWrapper = new QueryWrapper<>();
        existWrapper.eq("order_id", review.getOrderId());
        OrderReview existingReview = this.getOne(existWrapper, false);

        if (existingReview != null) {
            // 追加评价：更新原评价内容
            existingReview.setContent(existingReview.getContent() + "\n【追加评价】" + review.getContent());
            existingReview.setUpdateTime(new Date());
            this.updateById(existingReview);
        } else {
            this.save(review);
        }

        // 更新订单状态为已评价(9)
        MarketOrder order = marketOrderMapper.selectById(review.getOrderId());
        if (order != null) {
            order.setOrderStatus(9);
            order.setUpdateTime(new Date());
            marketOrderMapper.updateById(order);

            // 获取商品名称，发送通知给卖家
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            String goodsName = goods != null ? goods.getGoodsName() : "未知商品";
            String content = "你出售的商品「" + goodsName + "」已收到买家评价";
            letterService.sendSystemNotification(order.getSellerId(), content, "order", "order", order.getOrderId());
        }

        return Result.succ("success");
    }

    @Override
    public Result listByAdmin(Integer pageNum, Integer pageSize) {
        int pn = pageNum != null ? pageNum : 1;
        int ps = pageSize != null ? pageSize : 10;
        Page<OrderReview> page = new Page<>(pn, ps);
        QueryWrapper<OrderReview> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        Page<OrderReview> result = this.page(page, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("records", result.getRecords());
        return Result.succ(map);
    }

    @Override
    public Result getReviewTotal() {
        return Result.succ(this.count());
    }

    @Override
    public Result getReviewByOrderId(String orderId) {
        QueryWrapper<OrderReview> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId).orderByDesc("create_time").last("LIMIT 1");
        OrderReview review = this.getOne(wrapper, false);
        if (review == null) {
            return Result.fail("该订单暂无评价");
        }
        return Result.succ(review);
    }

    @Override
    public Result replyReview(String orderId, String reply, HttpServletRequest request) {
        QueryWrapper<OrderReview> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId).orderByDesc("create_time").last("LIMIT 1");
        OrderReview review = this.getOne(wrapper, false);
        if (review == null) {
            return Result.fail("评价不存在");
        }
        // 验证当前用户是该订单的卖家
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        MarketOrder order = marketOrderMapper.selectById(orderId);
        if (order == null || !order.getSellerId().equals(userId)) {
            return Result.fail("无权回复该评价");
        }
        review.setReply(reply);
        review.setReplyTime(new Date());
        this.updateById(review);
        return Result.succ("success");
    }
}
