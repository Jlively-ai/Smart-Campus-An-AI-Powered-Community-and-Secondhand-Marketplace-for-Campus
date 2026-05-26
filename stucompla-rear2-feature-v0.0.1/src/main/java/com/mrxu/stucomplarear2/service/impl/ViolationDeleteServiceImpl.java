package com.mrxu.stucomplarear2.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.entity.*;
import com.mrxu.stucomplarear2.mapper.ViolationDeleteMapper;
import com.mrxu.stucomplarear2.service.*;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ViolationDeleteServiceImpl extends ServiceImpl<ViolationDeleteMapper, ViolationDelete> implements ViolationDeleteService {

    @Autowired
    private PostService postService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private WallService wallService;
    @Autowired
    private LetterService letterService;
    @Lazy
    @Autowired
    private CommentService commentService;

    @Override
    @Transactional
    public Result moveViolation(String userId, String itemType, String itemId, String reason, String handlerId, String operationType) {
        String itemData = null;
        boolean isLock = "lock".equals(operationType);

        if ("post".equals(itemType) || "post_lock".equals(itemType)) {
            Post item = postService.getById(itemId);
            if (item == null) return Result.fail("内容不存在");
            itemData = JSON.toJSONString(item);
            if (!isLock) {
                postService.removeById(itemId);
            }
        } else if ("goods".equals(itemType) || "goods_lock".equals(itemType)) {
            Goods item = goodsService.getById(itemId);
            if (item == null) return Result.fail("内容不存在");
            itemData = JSON.toJSONString(item);
            if (!isLock) {
                goodsService.removeById(itemId);
            }
        } else if ("wall".equals(itemType) || "wall_lock".equals(itemType)) {
            Wall item = wallService.getById(itemId);
            if (item == null) return Result.fail("内容不存在");
            itemData = JSON.toJSONString(item);
            if (!isLock) {
                wallService.removeById(itemId);
            }
        } else if ("comment".equals(itemType) || "comment_lock".equals(itemType)) {
            Comment item = commentService.getById(itemId);
            if (item == null) return Result.fail("内容不存在");
            itemData = JSON.toJSONString(item);
            if (!isLock) {
                commentService.removeById(itemId);
            }
        } else {
            return Result.fail("不支持的类型");
        }

        ViolationDelete vd = new ViolationDelete();
        vd.setUserId(userId);
        vd.setItemType(itemType);
        vd.setItemId(itemId);
        vd.setItemData(itemData);
        String safeReason = (reason != null && !reason.isEmpty()) ? reason : "违反社区规范";
        vd.setReason(safeReason);
        vd.setHandlerId(handlerId);
        vd.setAppealState(0);
        vd.setOperationType(operationType != null ? operationType : "delete");
        vd.setCreateTime(new Date());
        this.save(vd);

        // Send notification to user
        String typeLabel;
        if ("post".equals(itemType) || "post_lock".equals(itemType)) {
            typeLabel = "帖子";
        } else if ("goods".equals(itemType) || "goods_lock".equals(itemType)) {
            typeLabel = "商品";
        } else if ("wall".equals(itemType) || "wall_lock".equals(itemType)) {
            typeLabel = "表白墙";
        } else if ("comment".equals(itemType) || "comment_lock".equals(itemType)) {
            typeLabel = "评论";
        } else {
            typeLabel = "内容";
        }
        try {
            if (isLock) {
                letterService.sendSystemNotification(userId, "您的" + typeLabel + "因「" + safeReason + "」被管理员锁定，仅您自己可见，如不服可前往处罚管理申诉", "system", "violation", vd.getId());
            } else {
                letterService.sendSystemNotification(userId, "您的" + typeLabel + "因「" + safeReason + "」被管理员删除，如不服可前往处罚管理申诉", "system", "violation", vd.getId());
            }
        } catch (Exception e) {}

        return Result.succ(isLock ? "已违规锁定" : "已违规删除");
    }

    @Override
    public Result listMyViolations(String userId, Integer pageNum, Integer pageSize, String itemType, Integer appealState, String sortBy, String sortOrder) {
        QueryWrapper<ViolationDelete> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (itemType != null && !itemType.isEmpty()) wrapper.eq("item_type", itemType);
        if (appealState != null) wrapper.eq("appeal_state", appealState);
        applySorting(wrapper, sortBy, sortOrder);
        Page<ViolationDelete> page = new Page<>(pageNum, pageSize);
        Page<ViolationDelete> result = this.page(page, wrapper);
        return Result.succ(buildPageResult(result));
    }

    @Override
    public Result listAllViolations(Integer pageNum, Integer pageSize, String userId, String itemType, Integer appealState, String sortBy, String sortOrder) {
        QueryWrapper<ViolationDelete> wrapper = new QueryWrapper<>();
        if (userId != null && !userId.isEmpty()) wrapper.eq("user_id", userId);
        if (itemType != null && !itemType.isEmpty()) wrapper.eq("item_type", itemType);
        if (appealState != null) wrapper.eq("appeal_state", appealState);
        applySorting(wrapper, sortBy, sortOrder);
        Page<ViolationDelete> page = new Page<>(pageNum, pageSize);
        Page<ViolationDelete> result = this.page(page, wrapper);
        return Result.succ(buildPageResult(result));
    }

    private void applySorting(QueryWrapper<ViolationDelete> wrapper, String sortBy, String sortOrder) {
        String sortColumn = "create_time";
        if ("updateTime".equals(sortBy)) sortColumn = "update_time";
        if ("asc".equalsIgnoreCase(sortOrder)) wrapper.orderByAsc(sortColumn);
        else wrapper.orderByDesc(sortColumn);
    }

    private Map<String, Object> buildPageResult(Page<ViolationDelete> result) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (ViolationDelete vd : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", vd.getId());
            map.put("userId", vd.getUserId());
            map.put("itemType", vd.getItemType());
            map.put("itemId", vd.getItemId());
            map.put("reason", vd.getReason());
            map.put("handlerId", vd.getHandlerId());
            map.put("appealState", vd.getAppealState());
            map.put("appealReason", vd.getAppealReason());
            map.put("appealTime", vd.getAppealTime());
            map.put("appealResult", vd.getAppealResult());
            map.put("operationType", vd.getOperationType());
            map.put("createTime", vd.getCreateTime());
            map.put("updateTime", vd.getUpdateTime());
            // Extract preview
            try {
                Map<String, Object> data = JSON.parseObject(vd.getItemData(), Map.class);
                if ("post".equals(vd.getItemType())) {
                    map.put("preview", data.getOrDefault("title", "无标题"));
                } else if ("goods".equals(vd.getItemType())) {
                    map.put("preview", data.getOrDefault("goodsName", "无名称"));
                } else if ("wall".equals(vd.getItemType())) {
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
        return resultData;
    }

    @Override
    @Transactional
    public Result appeal(String userId, String id, String appealReason) {
        ViolationDelete vd = this.getById(id);
        if (vd == null) return Result.fail("记录不存在");
        if (!vd.getUserId().equals(userId)) return Result.fail("无权操作");
        if (vd.getAppealState() != 0) return Result.fail("已申诉过");
        vd.setAppealState(1);
        vd.setAppealReason(appealReason);
        vd.setAppealTime(new Date());
        vd.setUpdateTime(new Date());
        this.updateById(vd);
        return Result.succ("申诉已提交");
    }

    @Override
    @Transactional
    public Result handleAppeal(String id, Integer appealState, String appealResult) {
        ViolationDelete vd = this.getById(id);
        if (vd == null) return Result.fail("记录不存在");
        if (vd.getAppealState() != 1) return Result.fail("该记录未在申诉中");
        vd.setAppealState(appealState); // 2=approved, 3=rejected
        vd.setAppealResult(appealResult);
        vd.setUpdateTime(new Date());

        String operationType = vd.getOperationType();
        boolean isLock = "lock".equals(operationType);

        // If approved, restore or unlock the content
        if (appealState == 2) {
            try {
                if (isLock) {
                    // 锁定类型：解锁内容
                    String itemType = vd.getItemType();
                    if ("post_lock".equals(itemType)) {
                        Post post = postService.getById(vd.getItemId());
                        if (post != null) {
                            post.setPostStatus(0);
                            post.setLockReason(null);
                            postService.updateById(post);
                        }
                        this.removeById(id);
                    } else if ("goods_lock".equals(itemType)) {
                        Goods goods = goodsService.getById(vd.getItemId());
                        if (goods != null) {
                            goods.setLocked(0);
                            goods.setLockReason(null);
                            goodsService.updateById(goods);
                        }
                        this.removeById(id);
                    } else if ("wall_lock".equals(itemType)) {
                        Wall wall = wallService.getById(vd.getItemId());
                        if (wall != null) {
                            wall.setLocked(0);
                            wall.setLockReason(null);
                            wallService.updateById(wall);
                        }
                        this.removeById(id);
                    } else if ("comment_lock".equals(itemType)) {
                        Comment comment = commentService.getById(vd.getItemId());
                        if (comment != null) {
                            comment.setLocked(0);
                            comment.setLockReason(null);
                            commentService.updateById(comment);
                        }
                        this.removeById(id);
                    }
                    // Notify user
                    try {
                        String typeLabel = getItemTypeLabel(vd.getItemType());
                        letterService.sendSystemNotification(vd.getUserId(), "您的" + typeLabel + "申诉已通过，内容已解锁", "system");
                    } catch (Exception e) {}
                    return Result.succ("申诉通过，内容已解锁");
                } else {
                    // 删除类型：恢复内容
                    if ("post".equals(vd.getItemType())) {
                        Post item = JSON.parseObject(vd.getItemData(), Post.class);
                        postService.save(item);
                    } else if ("goods".equals(vd.getItemType())) {
                        Goods item = JSON.parseObject(vd.getItemData(), Goods.class);
                        goodsService.save(item);
                    } else if ("wall".equals(vd.getItemType())) {
                        Wall item = JSON.parseObject(vd.getItemData(), Wall.class);
                        wallService.save(item);
                    }
                    // Remove from violation delete after restore
                    this.removeById(id);
                    // Notify user
                    try {
                        String typeLabel = getItemTypeLabel(vd.getItemType());
                        letterService.sendSystemNotification(vd.getUserId(), "您的" + typeLabel + "申诉已通过，内容已恢复", "system");
                    } catch (Exception e) {}
                    return Result.succ("申诉通过，内容已恢复");
                }
            } catch (Exception e) {
                return Result.fail("恢复失败：" + e.getMessage());
            }
        } else {
            this.updateById(vd);
            // Notify user
            try {
                String typeLabel = getItemTypeLabel(vd.getItemType());
                letterService.sendSystemNotification(vd.getUserId(), "您的" + typeLabel + "申诉已被驳回，原因：" + appealResult, "system");
            } catch (Exception e) {}
            return Result.succ("申诉已驳回");
        }
    }

    private String getItemTypeLabel(String itemType) {
        if ("post".equals(itemType) || "post_lock".equals(itemType)) {
            return "帖子";
        } else if ("goods".equals(itemType) || "goods_lock".equals(itemType)) {
            return "商品";
        } else if ("wall".equals(itemType) || "wall_lock".equals(itemType)) {
            return "表白墙";
        } else if ("comment".equals(itemType) || "comment_lock".equals(itemType)) {
            return "评论";
        }
        return "内容";
    }
}
