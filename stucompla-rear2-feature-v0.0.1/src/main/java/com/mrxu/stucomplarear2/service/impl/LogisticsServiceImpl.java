package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.entity.Logistics;
import com.mrxu.stucomplarear2.mapper.LogisticsMapper;
import com.mrxu.stucomplarear2.service.LogisticsService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class LogisticsServiceImpl extends ServiceImpl<LogisticsMapper, Logistics> implements LogisticsService {

    @Override
    public Result createLogistics(Logistics logistics) {
        logistics.setLogisticsId(IdGenerator.generateId(IdGenerator.LOGISTICS));
        logistics.setCreateTime(new Date());
        logistics.setUpdateTime(new Date());
        this.save(logistics);
        return Result.succ("success");
    }

    @Override
    public Result updateLogistics(Logistics logistics) {
        Logistics existing = this.getById(logistics.getLogisticsId());
        if (existing == null) return Result.fail("物流信息不存在");
        if (logistics.getCompany() != null) existing.setCompany(logistics.getCompany());
        if (logistics.getTrackingNo() != null) existing.setTrackingNo(logistics.getTrackingNo());
        if (logistics.getCurrentStatus() != null) existing.setCurrentStatus(logistics.getCurrentStatus());
        if (logistics.getDetail() != null) existing.setDetail(logistics.getDetail());
        existing.setUpdateTime(new Date());
        this.updateById(existing);
        return Result.succ("success");
    }

    @Override
    public Result getByOrderId(String orderId) {
        QueryWrapper<Logistics> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        Logistics logistics = this.getOne(wrapper);
        return Result.succ(logistics);
    }

    @Override
    public Result getLogisticsList(Integer pageNum, Integer pageSize) {
        int pn = pageNum != null ? pageNum : 1;
        int ps = pageSize != null ? pageSize : 10;
        Page<Logistics> page = new Page<>(pn, ps);
        QueryWrapper<Logistics> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        Page<Logistics> result = this.page(page, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("records", result.getRecords());
        return Result.succ(map);
    }
}
