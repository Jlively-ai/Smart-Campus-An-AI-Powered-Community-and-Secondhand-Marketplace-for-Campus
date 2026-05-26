package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.dto.MarketOrderFindDto;
import com.mrxu.stucomplarear2.dto.OrderAddDto;
import com.mrxu.stucomplarear2.entity.MarketOrder;
import com.mrxu.stucomplarear2.utils.response.Result;

import javax.servlet.http.HttpServletRequest;

public interface MarketOrderService extends IService<MarketOrder> {

    Result addOrder(OrderAddDto orderAddDto, HttpServletRequest request);

    Result payOrder(String orderId);

    Result sendGoods(String orderId, String company, String trackingNo, HttpServletRequest request);

    Result receipt(String orderId, HttpServletRequest request);

    Result applyReturn(String orderId, HttpServletRequest request);

    Result auditReturn(String orderId, Integer auditState, String cause, HttpServletRequest request);

    Result getMyOrder(MarketOrderFindDto marketOrderFindDto, HttpServletRequest request);

    Result getMySalesOrders(MarketOrderFindDto marketOrderFindDto, HttpServletRequest request);

    Result getOrderList(MarketOrderFindDto marketOrderFindDto);

    Result getOrderTotal();

    Result getSalesData();

    Result getOrderDetail(String orderId, HttpServletRequest request);

    Result applyAfterSale(String orderId, java.util.Map<String, Object> afterSaleInfo, HttpServletRequest request);

    Result handleAfterSale(java.util.Map<String, Object> handleInfo, HttpServletRequest request);

    Result submitReturnShipping(String orderId, String returnCompany, String returnTrackingNo, HttpServletRequest request);

    Result confirmReturnReceipt(String orderId, HttpServletRequest request);
}
