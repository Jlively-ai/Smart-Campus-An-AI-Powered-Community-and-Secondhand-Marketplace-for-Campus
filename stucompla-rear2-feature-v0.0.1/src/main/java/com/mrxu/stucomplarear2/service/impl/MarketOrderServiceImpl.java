package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.dto.MarketOrderFindDto;
import com.mrxu.stucomplarear2.dto.OrderAddDto;
import com.mrxu.stucomplarear2.entity.Goods;
import com.mrxu.stucomplarear2.entity.GoodsCategory;
import com.mrxu.stucomplarear2.entity.Logistics;
import com.mrxu.stucomplarear2.entity.MarketOrder;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.mapper.GoodsCategoryMapper;
import com.mrxu.stucomplarear2.mapper.GoodsMapper;
import com.mrxu.stucomplarear2.mapper.LogisticsMapper;
import com.mrxu.stucomplarear2.mapper.MarketOrderMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.MarketOrderService;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MarketOrderServiceImpl extends ServiceImpl<MarketOrderMapper, MarketOrder> implements MarketOrderService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LogisticsMapper logisticsMapper;

    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    @Autowired
    private LetterService letterService;

    @Override
    public Result addOrder(OrderAddDto orderAddDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Goods goods = goodsMapper.selectById(orderAddDto.getGoodsId());
        if (goods == null) {
            return Result.fail("goods not found");
        }
        if (!goods.getGoodsStatus()) {
            return Result.fail("goods off shelf");
        }
        if (goods.getGoodsCount() < orderAddDto.getBuyCount()) {
            return Result.fail("out of stock");
        }
        MarketOrder order = new MarketOrder();
        order.setSellerId(goods.getUserId());
        order.setBuyerId(userId);
        order.setGoodsId(orderAddDto.getGoodsId());
        order.setBuyCount(orderAddDto.getBuyCount());
        order.setTotalPrice(goods.getGoodsPrice() * orderAddDto.getBuyCount());
        order.setOrderStatus(0);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setOrderId(com.mrxu.stucomplarear2.utils.IdGenerator.generateId(IdGenerator.ORDER));
        order.setReceiverName(orderAddDto.getReceiverName());
        order.setReceiverPhone(orderAddDto.getReceiverPhone());
        order.setReceiverAddress(orderAddDto.getReceiverAddress());
        this.save(order);
        // 扣减库存
        goods.setGoodsCount(goods.getGoodsCount() - orderAddDto.getBuyCount());
        // 库存为0时自动下架
        if (goods.getGoodsCount() <= 0) {
            goods.setGoodsStatus(false);
        }
        goods.setUpdateTime(new Date());
        goodsMapper.updateById(goods);
        return Result.succ(order);
    }

    @Override
    public Result payOrder(String orderId) {
        MarketOrder order = this.getById(orderId);
        if (order == null) {
            return Result.fail("order not found");
        }
        order.setOrderStatus(1);
        order.setPayTime(new Date());
        order.setUpdateTime(new Date());
        this.updateById(order);
        // 通知卖家有新订单
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            letterService.sendSystemNotification(order.getSellerId(), "你的商品「" + goods.getGoodsName() + "」有新的购买订单，请及时发货", "order", "order", orderId);
            // 通知买家支付成功
            letterService.sendSystemNotification(order.getBuyerId(), "你购买的商品「" + goods.getGoodsName() + "」已支付成功，等待卖家发货", "order", "order", orderId);
        }
        return Result.succ("success");
    }

    @Override
    public Result sendGoods(String orderId, String company, String trackingNo, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        MarketOrder order = this.getById(orderId);
        if (order == null) {
            return Result.fail("order not found");
        }
        if (!order.getSellerId().equals(userId)) {
            return Result.fail("no permission");
        }
        order.setOrderStatus(2);
        order.setSendTime(new Date());
        order.setUpdateTime(new Date());
        this.updateById(order);
        // 创建物流记录
        if (company != null && !company.isEmpty() && trackingNo != null && !trackingNo.isEmpty()) {
            Logistics logistics = new Logistics();
            logistics.setLogisticsId(com.mrxu.stucomplarear2.utils.IdGenerator.generateId(IdGenerator.LOGISTICS));
            logistics.setOrderId(orderId);
            logistics.setCompany(company);
            logistics.setTrackingNo(trackingNo);
            logistics.setCurrentStatus("已发货");
            logistics.setDetail("卖家已发货，快递公司：" + company + "，运单号：" + trackingNo);
            logistics.setCreateTime(new Date());
            logistics.setUpdateTime(new Date());
            logisticsMapper.insert(logistics);
        }
        // 通知买家已发货
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            letterService.sendSystemNotification(order.getBuyerId(), "你购买的商品「" + goods.getGoodsName() + "」已发货" + (trackingNo != null ? "，运单号：" + trackingNo : ""), "logistics", "order", orderId);
        }
        return Result.succ("success");
    }

    @Override
    public Result receipt(String orderId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        MarketOrder order = this.getById(orderId);
        if (order == null) {
            return Result.fail("order not found");
        }
        if (!order.getBuyerId().equals(userId)) {
            return Result.fail("no permission");
        }
        // 签收后自动变为已完成（无售后时）
        order.setOrderStatus(8);
        order.setReceiptTime(new Date());
        order.setUpdateTime(new Date());
        this.updateById(order);
        // 通知卖家已签收
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            letterService.sendSystemNotification(order.getSellerId(), "商品「" + goods.getGoodsName() + "」已被买家签收", "logistics", "order", orderId);
            // 通知买家签收成功
            letterService.sendSystemNotification(order.getBuyerId(), "你购买的商品「" + goods.getGoodsName() + "」已签收成功", "logistics", "order", orderId);
        }
        return Result.succ("success");
    }

    @Override
    public Result applyReturn(String orderId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        MarketOrder order = this.getById(orderId);
        if (order == null) {
            return Result.fail("order not found");
        }
        if (!order.getBuyerId().equals(userId)) {
            return Result.fail("no permission");
        }
        order.setOrderStatus(4);
        order.setUpdateTime(new Date());
        this.updateById(order);
        // 通知卖家有退货申请
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            letterService.sendSystemNotification(order.getSellerId(), "买家申请退货商品「" + goods.getGoodsName() + "」", "order", "order", orderId);
        }
        return Result.succ("success");
    }

    @Override
    public Result auditReturn(String orderId, Integer auditState, String cause, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        MarketOrder order = this.getById(orderId);
        if (order == null) {
            return Result.fail("order not found");
        }
        if (auditState == 1) {
            order.setOrderStatus(5);
        } else {
            order.setOrderStatus(3);
        }
        order.setUpdateTime(new Date());
        this.updateById(order);
        // 通知买家退货审核结果
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        String goodsName = goods != null ? goods.getGoodsName() : "商品";
        if (auditState == 1) {
            letterService.sendSystemNotification(order.getBuyerId(), "你的退货申请已通过，商品「" + goodsName + "」", "order", "order", orderId);
        } else {
            letterService.sendSystemNotification(order.getBuyerId(), "你的退货申请已被拒绝，商品「" + goodsName + "」", "order", "order", orderId);
        }
        return Result.succ("success");
    }

    @Override
    public Result getMyOrder(MarketOrderFindDto marketOrderFindDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        marketOrderFindDto.setBuyerId(userId);
        int pageNum = marketOrderFindDto.getPageNum() != null ? marketOrderFindDto.getPageNum() : 1;
        int pageSize = marketOrderFindDto.getPageSize() != null ? marketOrderFindDto.getPageSize() : 10;
        Page<MarketOrder> page = new Page<>(pageNum, pageSize);
        QueryWrapper<MarketOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("buyer_id", marketOrderFindDto.getBuyerId());
        if (marketOrderFindDto.getOrderStatus() != null) {
            queryWrapper.eq("order_status", marketOrderFindDto.getOrderStatus());
        }
        queryWrapper.orderByDesc("create_time");
        Page<MarketOrder> orderPage = this.page(page, queryWrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (MarketOrder o : orderPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("orderId", o.getOrderId());
            item.put("sellerId", o.getSellerId());
            item.put("buyerId", o.getBuyerId());
            item.put("goodsId", o.getGoodsId());
            item.put("buyCount", o.getBuyCount());
            item.put("totalPrice", o.getTotalPrice());
            item.put("orderStatus", o.getOrderStatus());
            item.put("createTime", o.getCreateTime());
            item.put("updateTime", o.getUpdateTime());
            item.put("receiverName", o.getReceiverName());
            item.put("receiverPhone", o.getReceiverPhone());
            item.put("receiverAddress", o.getReceiverAddress());
            Goods goods = goodsMapper.selectById(o.getGoodsId());
            item.put("goodsName", goods != null ? goods.getGoodsName() : "商品已删除");
            item.put("goodsImages", goods != null ? goods.getGoodsImages() : null);
            User buyer = userMapper.selectById(o.getBuyerId());
            item.put("buyerNickname", buyer != null ? buyer.getNickname() : "未知");
            User seller = userMapper.selectById(o.getSellerId());
            item.put("sellerNickname", seller != null ? seller.getNickname() : "未知");
            records.add(item);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("total", orderPage.getTotal());
        map.put("records", records);
        return Result.succ(map);
    }

    @Override
    public Result getMySalesOrders(MarketOrderFindDto marketOrderFindDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        marketOrderFindDto.setSellerId(userId);
        int pageNum2 = marketOrderFindDto.getPageNum() != null ? marketOrderFindDto.getPageNum() : 1;
        int pageSize2 = marketOrderFindDto.getPageSize() != null ? marketOrderFindDto.getPageSize() : 10;
        Page<MarketOrder> page = new Page<>(pageNum2, pageSize2);
        QueryWrapper<MarketOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("seller_id", marketOrderFindDto.getSellerId());
        if (marketOrderFindDto.getOrderStatus() != null) {
            queryWrapper.eq("order_status", marketOrderFindDto.getOrderStatus());
        }
        queryWrapper.orderByDesc("create_time");
        Page<MarketOrder> orderPage = this.page(page, queryWrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (MarketOrder o : orderPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("orderId", o.getOrderId());
            item.put("sellerId", o.getSellerId());
            item.put("buyerId", o.getBuyerId());
            item.put("goodsId", o.getGoodsId());
            item.put("buyCount", o.getBuyCount());
            item.put("totalPrice", o.getTotalPrice());
            item.put("orderStatus", o.getOrderStatus());
            item.put("createTime", o.getCreateTime());
            item.put("updateTime", o.getUpdateTime());
            item.put("receiverName", o.getReceiverName());
            item.put("receiverPhone", o.getReceiverPhone());
            item.put("receiverAddress", o.getReceiverAddress());
            Goods goods = goodsMapper.selectById(o.getGoodsId());
            item.put("goodsName", goods != null ? goods.getGoodsName() : "商品已删除");
            item.put("goodsImages", goods != null ? goods.getGoodsImages() : null);
            User buyer = userMapper.selectById(o.getBuyerId());
            item.put("buyerNickname", buyer != null ? buyer.getNickname() : "未知");
            User seller = userMapper.selectById(o.getSellerId());
            item.put("sellerNickname", seller != null ? seller.getNickname() : "未知");
            records.add(item);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("total", orderPage.getTotal());
        map.put("records", records);
        return Result.succ(map);
    }

    @Override
    public Result getOrderList(MarketOrderFindDto marketOrderFindDto) {
        int pageNum = marketOrderFindDto.getPageNum() != null ? marketOrderFindDto.getPageNum() : 1;
        int pageSize = marketOrderFindDto.getPageSize() != null ? marketOrderFindDto.getPageSize() : 10;
        Page<MarketOrder> page = new Page<>(pageNum, pageSize);
        QueryWrapper<MarketOrder> queryWrapper = new QueryWrapper<>();
        if (marketOrderFindDto.getOrderStatus() != null) {
            queryWrapper.eq("order_status", marketOrderFindDto.getOrderStatus());
        }
        queryWrapper.orderByDesc("create_time");
        Page<MarketOrder> orderPage = this.page(page, queryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("total", orderPage.getTotal());
        map.put("records", orderPage.getRecords());
        return Result.succ(map);
    }

    @Override
    public Result getOrderTotal() {
        long total = this.count();
        return Result.succ(total);
    }

    @Override
    public Result getSalesData() {
        Map<String, Object> data = new HashMap<>();
        String[] statusNames = {"未支付", "已支付", "已发货", "已签收", "退款中", "退货中", "已退款", "已退货", "已完成", "已评价"};
        long[] statusCounts = new long[10];
        double totalRevenue = 0;
        for (MarketOrder order : this.list()) {
            int s = order.getOrderStatus();
            if (s >= 0 && s < 10) statusCounts[s]++;
            if (order.getOrderStatus() == 1 || order.getOrderStatus() == 2 || order.getOrderStatus() == 3 || order.getOrderStatus() == 8) {
                if (order.getTotalPrice() != null) totalRevenue += order.getTotalPrice();
            }
        }
        java.util.List<Map<String, Object>> statusData = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", statusNames[i]);
            item.put("value", statusCounts[i]);
            statusData.add(item);
        }
        data.put("statusData", statusData);
        data.put("totalRevenue", totalRevenue);
        data.put("totalOrders", this.count());
        return Result.succ(data);
    }

    @Override
    public Result getOrderDetail(String orderId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        MarketOrder order = this.getById(orderId);
        if (order == null) return Result.fail("订单不存在");
        // Verify the user is either buyer or seller
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            return Result.fail("无权查看此订单");
        }
        Map<String, Object> item = new HashMap<>();
        // Basic order info
        item.put("orderId", order.getOrderId());
        item.put("sellerId", order.getSellerId());
        item.put("buyerId", order.getBuyerId());
        item.put("goodsId", order.getGoodsId());
        item.put("buyCount", order.getBuyCount());
        item.put("totalPrice", order.getTotalPrice());
        item.put("orderStatus", order.getOrderStatus());
        item.put("createTime", order.getCreateTime());
        item.put("updateTime", order.getUpdateTime());
        item.put("payTime", order.getPayTime());
        item.put("sendTime", order.getSendTime());
        item.put("receiptTime", order.getReceiptTime());
        item.put("receiverName", order.getReceiverName());
        item.put("receiverPhone", order.getReceiverPhone());
        item.put("receiverAddress", order.getReceiverAddress());
        item.put("remark", order.getRemark());

        // Buyer info
        User buyer = userMapper.selectById(order.getBuyerId());
        item.put("buyerNickname", buyer != null ? buyer.getNickname() : "未知");
        item.put("buyerPhone", buyer != null ? buyer.getPhone() : null);
        item.put("buyerAvatar", buyer != null ? buyer.getAvatar() : null);

        // Seller info
        User seller = userMapper.selectById(order.getSellerId());
        item.put("sellerNickname", seller != null ? seller.getNickname() : "未知");
        item.put("sellerPhone", seller != null ? seller.getPhone() : null);
        item.put("sellerAvatar", seller != null ? seller.getAvatar() : null);

        // Goods info
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        item.put("goodsName", goods != null ? goods.getGoodsName() : "商品已删除");
        item.put("goodsImages", goods != null ? goods.getGoodsImages() : null);
        item.put("goodsPrice", goods != null ? goods.getGoodsPrice() : null);
        item.put("goodsDetail", goods != null ? goods.getGoodsDetail() : null);
        item.put("goodsCategoryId", goods != null ? goods.getGoodsCategoryId() : null);
        item.put("goodsCount", goods != null ? goods.getGoodsCount() : null);
        item.put("goodsStatus", goods != null ? goods.getGoodsStatus() : null);
        item.put("goodsCreateTime", goods != null ? goods.getCreateTime() : null);
        // Category name
        if (goods != null && goods.getGoodsCategoryId() != null) {
            GoodsCategory category = goodsCategoryMapper.selectById(goods.getGoodsCategoryId());
            item.put("categoryName", category != null ? category.getGoodsCategoryName() : null);
        } else {
            item.put("categoryName", null);
        }

        // Logistics info - all records for history tracking
        QueryWrapper<Logistics> logisticsQuery = new QueryWrapper<>();
        logisticsQuery.eq("order_id", orderId).orderByDesc("create_time");
        List<Logistics> logisticsList = logisticsMapper.selectList(logisticsQuery);
        Map<String, Object> logisticsInfo = new HashMap<>();
        if (!logisticsList.isEmpty()) {
            Logistics latest = logisticsList.get(0);
            logisticsInfo.put("company", latest.getCompany());
            logisticsInfo.put("trackingNo", latest.getTrackingNo());
            logisticsInfo.put("currentStatus", latest.getCurrentStatus());
            logisticsInfo.put("detail", latest.getDetail());
            logisticsInfo.put("createTime", latest.getCreateTime());
        }
        List<Map<String, Object>> logisticsHistory = new ArrayList<>();
        for (Logistics l : logisticsList) {
            Map<String, Object> h = new HashMap<>();
            h.put("logisticsId", l.getLogisticsId());
            h.put("company", l.getCompany());
            h.put("trackingNo", l.getTrackingNo());
            h.put("currentStatus", l.getCurrentStatus());
            h.put("detail", l.getDetail());
            h.put("createTime", l.getCreateTime());
            logisticsHistory.add(h);
        }
        item.put("logistics", logisticsInfo);
        item.put("logisticsHistory", logisticsHistory);

        return Result.succ(item);
    }

    @Override
    public Result applyAfterSale(String orderId, Map<String, Object> afterSaleInfo, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        // 查询订单
        MarketOrder order = this.getById(orderId);
        if (order == null) return Result.fail("订单不存在");
        if (!order.getBuyerId().equals(userId)) return Result.fail("无权操作");
        // 更新订单状态为退款中(4)或退货中(5)，根据售后类型
        String afterSaleType = (String) afterSaleInfo.get("type");
        int newStatus = 4; // 默认退款中
        if ("退货退款".equals(afterSaleType) || "换货".equals(afterSaleType)) {
            newStatus = 5; // 退货中
        }
        order.setOrderStatus(newStatus);
        // 将售后信息存入remark字段（JSON格式）
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String remark = mapper.writeValueAsString(afterSaleInfo);
            order.setRemark(remark);
        } catch (Exception e) {
            order.setRemark(afterSaleInfo.toString());
        }
        this.updateById(order);
        // 通知卖家有售后申请
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        String goodsName = goods != null ? goods.getGoodsName() : "商品";
        String afterSaleTypeName = afterSaleType != null ? afterSaleType : "售后";
        letterService.sendSystemNotification(order.getSellerId(), "买家申请" + afterSaleTypeName + "，商品「" + goodsName + "」", "order", "order", orderId);
        letterService.sendSystemNotification(order.getBuyerId(), "你已提交" + afterSaleTypeName + "申请，商品「" + goodsName + "」", "order", "order", orderId);
        return Result.succ("售后申请已提交");
    }

    @Override
    public Result handleAfterSale(Map<String, Object> handleInfo, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        String orderId = (String) handleInfo.get("orderId");
        Boolean agree = (Boolean) handleInfo.get("agree");

        MarketOrder order = this.getById(orderId);
        if (order == null) return Result.fail("订单不存在");
        if (!order.getSellerId().equals(userId)) return Result.fail("无权操作");

        if (agree != null && agree) {
            // 同意售后
            String afterSaleType = "";
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> afterSaleInfo = mapper.readValue(order.getRemark(), Map.class);
                afterSaleType = (String) afterSaleInfo.get("type");
            } catch (Exception e) {}

            if ("仅退款".equals(afterSaleType)) {
                order.setOrderStatus(6); // 已退款
            } else if ("退货退款".equals(afterSaleType)) {
                order.setOrderStatus(5); // 退货中 - 等待买家填写退货快递信息
            } else if ("换货".equals(afterSaleType) || "补寄".equals(afterSaleType)) {
                order.setOrderStatus(3); // 已发货（换货/补寄后重新发货）
            } else {
                order.setOrderStatus(6); // 默认已退款
            }
            // 将处理信息追加到remark
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> existingInfo = mapper.readValue(order.getRemark(), Map.class);
                existingInfo.put("handleResult", "同意");
                existingInfo.put("handleInfo", handleInfo);
                order.setRemark(mapper.writeValueAsString(existingInfo));
            } catch (Exception e) {}
        } else {
            // 拒绝售后，恢复原状态
            order.setOrderStatus(3); // 恢复为已签收
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> existingInfo = mapper.readValue(order.getRemark(), Map.class);
                existingInfo.put("handleResult", "拒绝");
                existingInfo.put("handleInfo", handleInfo);
                order.setRemark(mapper.writeValueAsString(existingInfo));
            } catch (Exception e) {}
        }
        this.updateById(order);
        // 通知买卖家售后处理结果
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        String goodsName = goods != null ? goods.getGoodsName() : "商品";
        if (agree != null && agree) {
            letterService.sendSystemNotification(order.getBuyerId(), "你的售后申请已通过，商品「" + goodsName + "」", "order", "order", orderId);
            letterService.sendSystemNotification(order.getSellerId(), "你已同意售后申请，商品「" + goodsName + "」", "order", "order", orderId);
        } else {
            letterService.sendSystemNotification(order.getBuyerId(), "你的售后申请已被拒绝，商品「" + goodsName + "」", "order", "order", orderId);
            letterService.sendSystemNotification(order.getSellerId(), "你已拒绝售后申请，商品「" + goodsName + "」", "order", "order", orderId);
        }
        return Result.succ("售后处理完成");
    }

    @Override
    public Result submitReturnShipping(String orderId, String returnCompany, String returnTrackingNo, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        MarketOrder order = this.getById(orderId);
        if (order == null) return Result.fail("订单不存在");
        if (!order.getBuyerId().equals(userId)) return Result.fail("无权操作");
        if (order.getOrderStatus() != 5) return Result.fail("当前状态不允许提交退货快递");

        // 将退货快递信息追加到remark
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> existingInfo = mapper.readValue(order.getRemark(), Map.class);
            existingInfo.put("returnShippingSubmitted", true);
            existingInfo.put("returnCompany", returnCompany);
            existingInfo.put("returnTrackingNo", returnTrackingNo);
            order.setRemark(mapper.writeValueAsString(existingInfo));
        } catch (Exception e) {
            return Result.fail("更新退货信息失败");
        }
        order.setUpdateTime(new Date());
        this.updateById(order);

        // 通知卖家买家已提交退货快递
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        String goodsName = goods != null ? goods.getGoodsName() : "商品";
        letterService.sendSystemNotification(order.getSellerId(), "买家已提交退货快递，商品「" + goodsName + "」，快递公司：" + returnCompany + "，运单号：" + returnTrackingNo, "order", "order", orderId);

        return Result.succ("退货快递信息已提交");
    }

    @Override
    public Result confirmReturnReceipt(String orderId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        MarketOrder order = this.getById(orderId);
        if (order == null) return Result.fail("订单不存在");
        if (!order.getSellerId().equals(userId)) return Result.fail("无权操作");
        if (order.getOrderStatus() != 5) return Result.fail("当前状态不允许确认退货");

        // 从remark获取售后类型
        String afterSaleType = "";
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> existingInfo = mapper.readValue(order.getRemark(), Map.class);
            afterSaleType = (String) existingInfo.get("type");
            existingInfo.put("returnConfirmed", true);
            order.setRemark(mapper.writeValueAsString(existingInfo));
        } catch (Exception e) {}

        // 根据售后类型更新状态
        if ("退货退款".equals(afterSaleType)) {
            order.setOrderStatus(6); // 已退款
        } else if ("换货".equals(afterSaleType)) {
            order.setOrderStatus(3); // 已签收（换货完成）
        } else {
            order.setOrderStatus(6); // 默认已退款
        }
        order.setUpdateTime(new Date());
        this.updateById(order);

        // 通知买家
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        String goodsName = goods != null ? goods.getGoodsName() : "商品";
        if ("退货退款".equals(afterSaleType)) {
            letterService.sendSystemNotification(order.getBuyerId(), "退货已完成，退款已处理，商品「" + goodsName + "」", "order", "order", orderId);
        } else if ("换货".equals(afterSaleType)) {
            letterService.sendSystemNotification(order.getBuyerId(), "换货已完成，商品「" + goodsName + "」", "order", "order", orderId);
        }

        return Result.succ("已确认退货并处理");
    }
}
