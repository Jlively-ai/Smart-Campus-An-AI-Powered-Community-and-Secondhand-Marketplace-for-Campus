package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.entity.ViolationDelete;
import com.mrxu.stucomplarear2.utils.response.Result;

public interface ViolationDeleteService extends IService<ViolationDelete> {
    Result moveViolation(String userId, String itemType, String itemId, String reason, String handlerId, String operationType);
    Result listMyViolations(String userId, Integer pageNum, Integer pageSize, String itemType, Integer appealState, String sortBy, String sortOrder);
    Result listAllViolations(Integer pageNum, Integer pageSize, String userId, String itemType, Integer appealState, String sortBy, String sortOrder);
    Result appeal(String userId, String id, String appealReason);
    Result handleAppeal(String id, Integer appealState, String appealResult);
}
