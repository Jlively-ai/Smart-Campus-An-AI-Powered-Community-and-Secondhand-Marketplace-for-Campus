package com.mrxu.stucomplarear2.controller;


import com.mrxu.stucomplarear2.dto.MarketOrderFindDto;
import com.mrxu.stucomplarear2.dto.OrderAddDto;
import com.mrxu.stucomplarear2.dto.OrderAuditDto;
import com.mrxu.stucomplarear2.service.MarketOrderService;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Mr.Xu
 * @since 2022-04-20
 */
@RestController
@RequestMapping("/market-order")
public class MarketOrderController {
    @Resource
    private MarketOrderService marketOrderService;

    @ApiOperation("添加订单")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/addOrder")
    public Result addOrder(@RequestBody OrderAddDto orderAddDto, HttpServletRequest request) {
        Result result = marketOrderService.addOrder(orderAddDto, request);
        return result;
    }

//    @ApiOperation("删除订单")
//    @RequiresRoles("user")
//    @DeleteMapping("/{orderId}")
//    public Result deleteOrder(@PathVariable("orderId") String orderId, HttpServletRequest request) {
//        Result result = marketOrderService.deleteOrder(orderId, request);
//        return result;
//    }

    @ApiOperation("订单支付")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/payOrder/{orderId}")
    public Result payOrder(@PathVariable("orderId") String orderId) {
        Result result = marketOrderService.payOrder(orderId);
        return result;
    }

    @ApiOperation("订单发货")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/sendGoods/{orderId}")
    public Result sendGoods(@PathVariable("orderId") String orderId, @RequestBody java.util.Map<String, String> body, HttpServletRequest request) {
        String company = body != null ? body.get("company") : null;
        String trackingNo = body != null ? body.get("trackingNo") : null;
        Result result = marketOrderService.sendGoods(orderId, company, trackingNo, request);
        return result;
    }

    @ApiOperation("订单签收")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/receipt/{orderId}")
    public Result receipt(@PathVariable("orderId") String orderId, HttpServletRequest request) {
        Result result = marketOrderService.receipt(orderId, request);
        return result;
    }

    @ApiOperation("申请退货")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/applyReturn/{orderId}")
    public Result applyReturn(@PathVariable("orderId") String orderId, HttpServletRequest request) {
        Result result = marketOrderService.applyReturn(orderId, request);
        return result;
    }

    @ApiOperation("审核退货")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/auditReturn")
    public Result auditReturn(@RequestBody OrderAuditDto orderAuditDto, HttpServletRequest request) {
        Result result = marketOrderService.auditReturn(orderAuditDto.getOrderId(), orderAuditDto.getAuditState(), orderAuditDto.getCause(), request);
        return result;
    }

    @ApiOperation("申请售后")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/applyAfterSale/{orderId}")
    public Result applyAfterSale(@PathVariable("orderId") String orderId, @RequestBody java.util.Map<String, Object> afterSaleInfo, HttpServletRequest request) {
        Result result = marketOrderService.applyAfterSale(orderId, afterSaleInfo, request);
        return result;
    }

    @ApiOperation("处理售后")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/handleAfterSale")
    public Result handleAfterSale(@RequestBody java.util.Map<String, Object> handleInfo, HttpServletRequest request) {
        Result result = marketOrderService.handleAfterSale(handleInfo, request);
        return result;
    }

    @ApiOperation("买家提交退货快递")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/submitReturnShipping/{orderId}")
    public Result submitReturnShipping(@PathVariable String orderId, @RequestBody Map<String, String> shippingInfo, HttpServletRequest request) {
        return marketOrderService.submitReturnShipping(orderId, shippingInfo.get("returnCompany"), shippingInfo.get("returnTrackingNo"), request);
    }

    @ApiOperation("卖家确认退货收货")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/confirmReturnReceipt/{orderId}")
    public Result confirmReturnReceipt(@PathVariable String orderId, HttpServletRequest request) {
        return marketOrderService.confirmReturnReceipt(orderId, request);
    }

    @ApiOperation("获取我的订单")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/myOrder")
    public Result getMyOrder(MarketOrderFindDto marketOrderFindDto, HttpServletRequest request) {
        Result result = marketOrderService.getMyOrder(marketOrderFindDto, request);
        return result;
    }

    @ApiOperation("获取我的销售订单")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/mySalesOrders")
    public Result getMySalesOrders(MarketOrderFindDto marketOrderFindDto, HttpServletRequest request) {
        Result result = marketOrderService.getMySalesOrders(marketOrderFindDto, request);
        return result;
    }

    @ApiOperation("获取订单详情")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/detail/{orderId}")
    public Result getOrderDetail(@PathVariable("orderId") String orderId, HttpServletRequest request) {
        return marketOrderService.getOrderDetail(orderId, request);
    }


    @ApiOperation("管理员获取订单列表")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/orderList")
    public Result getOrderList(MarketOrderFindDto marketOrderFindDto) {
        Result result = marketOrderService.getOrderList(marketOrderFindDto);
        return result;
    }


    @ApiOperation("获取订单总数")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/getOrderTotal")
    public Result getOrderTotal() {
        Result result = marketOrderService.getOrderTotal();
        return result;
    }

    @ApiOperation("销售情况统计")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/getSalesData")
    public Result getSalesData() {
        return marketOrderService.getSalesData();
    }

}
