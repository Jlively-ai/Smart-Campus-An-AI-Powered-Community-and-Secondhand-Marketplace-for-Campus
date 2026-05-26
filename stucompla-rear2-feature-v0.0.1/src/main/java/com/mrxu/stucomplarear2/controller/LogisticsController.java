package com.mrxu.stucomplarear2.controller;

import com.mrxu.stucomplarear2.entity.Logistics;
import com.mrxu.stucomplarear2.service.LogisticsService;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logistics")
public class LogisticsController {

    @Autowired
    private LogisticsService logisticsService;

    @ApiOperation("创建物流信息")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/create")
    public Result create(@RequestBody Logistics logistics) {
        return logisticsService.createLogistics(logistics);
    }

    @ApiOperation("更新物流信息")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/update")
    public Result update(@RequestBody Logistics logistics) {
        return logisticsService.updateLogistics(logistics);
    }

    @ApiOperation("根据订单查询物流")
    @GetMapping("/getByOrderId/{orderId}")
    public Result getByOrderId(@PathVariable String orderId) {
        return logisticsService.getByOrderId(orderId);
    }

    @ApiOperation("物流列表")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/list")
    public Result list(Integer pageNum, Integer pageSize) {
        return logisticsService.getLogisticsList(pageNum, pageSize);
    }
}
