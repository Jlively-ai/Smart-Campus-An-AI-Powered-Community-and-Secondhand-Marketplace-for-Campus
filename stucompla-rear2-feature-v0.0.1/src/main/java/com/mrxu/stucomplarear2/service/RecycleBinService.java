package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.entity.RecycleBin;
import com.mrxu.stucomplarear2.utils.response.Result;

public interface RecycleBinService extends IService<RecycleBin> {
    Result moveToRecycleBin(String userId, String itemType, String itemId);
    Result listMyRecycleBin(String userId, Integer pageNum, Integer pageSize, String itemType, String keyword, String sortBy, String sortOrder);
    Result restoreItem(String userId, String recycleId);
    Result permanentlyDelete(String userId, String recycleId);
    Result cleanExpired();
}
