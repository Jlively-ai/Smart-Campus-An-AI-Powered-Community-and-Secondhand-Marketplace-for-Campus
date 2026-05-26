package com.mrxu.stucomplarear2.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.entity.*;
import com.mrxu.stucomplarear2.mapper.RecycleBinMapper;
import com.mrxu.stucomplarear2.service.*;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RecycleBinServiceImpl extends ServiceImpl<RecycleBinMapper, RecycleBin> implements RecycleBinService {

    @Autowired
    private PostService postService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private WallService wallService;

    @Override
    @Transactional
    public Result moveToRecycleBin(String userId, String itemType, String itemId) {
        String itemData = null;
        if ("post".equals(itemType)) {
            Post item = postService.getById(itemId);
            if (item == null) return Result.fail("内容不存在");
            if (!item.getUserId().equals(userId)) return Result.fail("无权操作");
            itemData = JSON.toJSONString(item);
            postService.removeById(itemId);
        } else if ("goods".equals(itemType)) {
            Goods item = goodsService.getById(itemId);
            if (item == null) return Result.fail("内容不存在");
            if (!item.getUserId().equals(userId)) return Result.fail("无权操作");
            itemData = JSON.toJSONString(item);
            goodsService.removeById(itemId);
        } else if ("wall".equals(itemType)) {
            Wall item = wallService.getById(itemId);
            if (item == null) return Result.fail("内容不存在");
            if (!item.getUserId().equals(userId)) return Result.fail("无权操作");
            itemData = JSON.toJSONString(item);
            wallService.removeById(itemId);
        } else {
            return Result.fail("不支持的类型");
        }

        RecycleBin rb = new RecycleBin();
        rb.setUserId(userId);
        rb.setItemType(itemType);
        rb.setItemId(itemId);
        rb.setItemData(itemData);
        rb.setDeleteTime(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        rb.setExpireTime(cal.getTime());
        this.save(rb);
        return Result.succ("已移入回收站");
    }

    @Override
    public Result listMyRecycleBin(String userId, Integer pageNum, Integer pageSize, String itemType, String keyword, String sortBy, String sortOrder) {
        // Clean expired items first
        cleanExpired();

        QueryWrapper<RecycleBin> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (itemType != null && !itemType.isEmpty()) {
            wrapper.eq("item_type", itemType);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("item_data", keyword);
        }
        // Sorting
        String sortColumn = "delete_time";
        if ("expireTime".equals(sortBy)) sortColumn = "expire_time";
        if ("asc".equalsIgnoreCase(sortOrder)) {
            wrapper.orderByAsc(sortColumn);
        } else {
            wrapper.orderByDesc(sortColumn);
        }
        Page<RecycleBin> page = new Page<>(pageNum, pageSize);
        Page<RecycleBin> result = this.page(page, wrapper);

        // Parse itemData for display
        List<Map<String, Object>> records = new ArrayList<>();
        for (RecycleBin rb : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", rb.getId());
            map.put("itemType", rb.getItemType());
            map.put("itemId", rb.getItemId());
            map.put("deleteTime", rb.getDeleteTime());
            map.put("expireTime", rb.getExpireTime());
            try {
                Map<String, Object> data = JSON.parseObject(rb.getItemData(), Map.class);
                map.put("itemData", data);
                // Extract preview info
                if ("post".equals(rb.getItemType())) {
                    map.put("preview", data.getOrDefault("title", "无标题"));
                } else if ("goods".equals(rb.getItemType())) {
                    map.put("preview", data.getOrDefault("goodsName", "无名称"));
                    map.put("price", data.getOrDefault("goodsPrice", 0));
                } else if ("wall".equals(rb.getItemType())) {
                    String content = String.valueOf(data.getOrDefault("wallContent", ""));
                    map.put("preview", content.length() > 30 ? content.substring(0, 30) + "..." : content);
                }
            } catch (Exception e) {
                map.put("preview", "数据解析失败");
            }
            records.add(map);
        }

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("records", records);
        resultData.put("total", result.getTotal());
        return Result.succ(resultData);
    }

    @Override
    @Transactional
    public Result restoreItem(String userId, String recycleId) {
        RecycleBin rb = this.getById(recycleId);
        if (rb == null) return Result.fail("回收站记录不存在");
        if (!rb.getUserId().equals(userId)) return Result.fail("无权操作");

        try {
            if ("post".equals(rb.getItemType())) {
                Post item = JSON.parseObject(rb.getItemData(), Post.class);
                postService.save(item);
            } else if ("goods".equals(rb.getItemType())) {
                Goods item = JSON.parseObject(rb.getItemData(), Goods.class);
                goodsService.save(item);
            } else if ("wall".equals(rb.getItemType())) {
                Wall item = JSON.parseObject(rb.getItemData(), Wall.class);
                wallService.save(item);
            } else {
                return Result.fail("不支持的类型");
            }
            this.removeById(recycleId);
            return Result.succ("已恢复");
        } catch (Exception e) {
            return Result.fail("恢复失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result permanentlyDelete(String userId, String recycleId) {
        RecycleBin rb = this.getById(recycleId);
        if (rb == null) return Result.fail("回收站记录不存在");
        if (!rb.getUserId().equals(userId)) return Result.fail("无权操作");
        this.removeById(recycleId);
        return Result.succ("已彻底删除");
    }

    @Override
    @Transactional
    public Result cleanExpired() {
        QueryWrapper<RecycleBin> wrapper = new QueryWrapper<>();
        wrapper.lt("expire_time", new Date());
        this.remove(wrapper);
        return Result.succ("ok");
    }
}
