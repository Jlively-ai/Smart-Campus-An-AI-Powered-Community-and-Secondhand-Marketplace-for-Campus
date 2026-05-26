package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.entity.Logistics;
import com.mrxu.stucomplarear2.utils.response.Result;

public interface LogisticsService extends IService<Logistics> {
    Result createLogistics(Logistics logistics);
    Result updateLogistics(Logistics logistics);
    Result getByOrderId(String orderId);
    Result getLogisticsList(Integer pageNum, Integer pageSize);
}
