package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.dto.GoodsAddDto;
import com.mrxu.stucomplarear2.dto.GoodsEditDto;
import com.mrxu.stucomplarear2.dto.GoodsFindDto;
import com.mrxu.stucomplarear2.dto.GoodsAuditDto;
import com.mrxu.stucomplarear2.entity.Goods;
import com.mrxu.stucomplarear2.entity.GoodsCategory;
import com.mrxu.stucomplarear2.entity.Follow;
import com.mrxu.stucomplarear2.entity.Letter;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.mapper.GoodsCategoryMapper;
import com.mrxu.stucomplarear2.mapper.GoodsMapper;
import com.mrxu.stucomplarear2.mapper.FollowMapper;
import com.mrxu.stucomplarear2.mapper.LetterMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.GoodsService;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.service.PrivacySettingService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private LetterMapper letterMapper;
    @Autowired
    private PrivacySettingService privacySettingService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LetterService letterService;

    @Override
    public Result add(GoodsAddDto goodsDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Goods goods = new Goods();
        goods.setGoodsName(goodsDto.getGoodsName());
        goods.setGoodsDetail(goodsDto.getGoodsDetail());
        goods.setGoodsImages(goodsDto.getGoodsImages());
        goods.setGoodsPrice(goodsDto.getGoodsPrice());
        goods.setGoodsCategoryId(goodsDto.getGoodsCategoryId());
        goods.setGoodsCount(goodsDto.getGoodsCount());
        goods.setGoodsStatus(true);
        goods.setAuditState(0); // 待审核
        goods.setGoodsId(IdGenerator.generateId(IdGenerator.GOODS));
        goods.setUserId(userId);
        goods.setViewNum(0);
        goods.setCreateTime(new Date());
        goods.setUpdateTime(new Date());
        this.save(goods);
        // 审核通过后再发送关注者通知，此处不发送
        return Result.succ("success");
    }

    @Override
    public Result editGoods(GoodsEditDto goodsEditDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Goods goods = this.getById(goodsEditDto.getGoodsId());
        if (goods == null) {
            return Result.fail("goods not found");
        }
        if (!goods.getUserId().equals(userId)) {
            return Result.fail("no permission");
        }
        goods.setGoodsName(goodsEditDto.getGoodsName());
        goods.setGoodsDetail(goodsEditDto.getGoodsDetail());
        goods.setGoodsImages(goodsEditDto.getGoodsImages());
        goods.setGoodsPrice(goodsEditDto.getGoodsPrice());
        goods.setGoodsCategoryId(goodsEditDto.getGoodsCategoryId());
        goods.setGoodsCount(goodsEditDto.getGoodsCount());
        goods.setAuditState(0); // 编辑后重新进入待审核
        goods.setUpdateTime(new Date());
        this.updateById(goods);
        return Result.succ("success");
    }

    @Override
    public Result deleteMyGoods(String goodsId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Goods goods = this.getById(goodsId);
        if (goods == null) {
            return Result.fail("goods not found");
        }
        if (!goods.getUserId().equals(userId)) {
            return Result.fail("no permission");
        }
        this.removeById(goodsId);
        return Result.succ("success");
    }

    @Override
    public Result putMyGoods(String goodsId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Goods goods = this.getById(goodsId);
        if (goods == null) {
            return Result.fail("goods not found");
        }
        if (!goods.getUserId().equals(userId)) {
            return Result.fail("no permission");
        }
        goods.setGoodsStatus(true);
        goods.setUpdateTime(new Date());
        this.updateById(goods);
        return Result.succ("success");
    }

    @Override
    public Result unShelveMyGoods(String goodsId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Goods goods = this.getById(goodsId);
        if (goods == null) {
            return Result.fail("goods not found");
        }
        if (!goods.getUserId().equals(userId)) {
            return Result.fail("no permission");
        }
        goods.setGoodsStatus(false);
        goods.setUpdateTime(new Date());
        this.updateById(goods);
        return Result.succ("success");
    }

    @Override
    public Result unShelveGoods(String goodsId) {
        Goods goods = this.getById(goodsId);
        if (goods == null) {
            return Result.fail("goods not found");
        }
        goods.setGoodsStatus(false);
        this.updateById(goods);
        return Result.succ("success");
    }

    @Override
    public Result deleteGoods(String goodsId, String cause) {
        Goods goods = this.getById(goodsId);
        if (goods == null) {
            return Result.fail("goods not found");
        }
        String userId = goods.getUserId();
        this.removeById(goodsId);
        // 发送通知给商品发布者
        if (userId != null && !"0".equals(userId)) {
            Letter letter = new Letter();
            letter.setLetterId(IdGenerator.generateId(IdGenerator.LETTER));
            letter.setSenderId("0");
            letter.setReceiverId(userId);
            letter.setMessageType("system");
            String reason = (cause != null && !cause.isEmpty()) ? cause : "违反平台规定";
            letter.setLetterDetail("您的商品「" + goods.getGoodsName() + "」已被管理员删除，原因：" + reason);
            letter.setLetterStatus(0);
            letter.setSessionId("admin_delete_goods_" + userId);
            letter.setCreateTime(new Date());
            letterMapper.insert(letter);
        }
        return Result.succ("success");
    }

    @Override
    public Result findGoods(GoodsFindDto goodsFindDto, HttpServletRequest request) {
        // 获取当前用户ID和角色（可能未登录）
        String viewerIdTemp = null;
        boolean viewerIsAdmin = false;
        try {
            String token = request.getHeader("Authorization");
            if (token != null && !token.isEmpty()) {
                viewerIdTemp = JWTUtil.getUserId(token);
                String role = JWTUtil.getRole(token);
                viewerIsAdmin = "admin".equals(role) || "super".equals(role);
            }
        } catch (Exception ignored) {
        }
        final String viewerId = viewerIdTemp;

        int pageNum = goodsFindDto.getPageNum() != null ? goodsFindDto.getPageNum() : 1;
        int pageSize = goodsFindDto.getPageSize() != null ? goodsFindDto.getPageSize() : 10;
        Page<Goods> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        if (goodsFindDto.getKeyName() != null && !goodsFindDto.getKeyName().isEmpty()) {
            queryWrapper.like("goods_name", goodsFindDto.getKeyName());
        }
        if (goodsFindDto.getGoodsCategoryId() != null) {
            queryWrapper.eq("goods_category_id", goodsFindDto.getGoodsCategoryId());
        }
        if (goodsFindDto.getGoodsStatus() != null) {
            queryWrapper.eq("goods_status", goodsFindDto.getGoodsStatus());
        }
        if (goodsFindDto.getUserId() != null && !goodsFindDto.getUserId().isEmpty()) {
            queryWrapper.eq("user_id", goodsFindDto.getUserId());
        }
        // 昵称模糊搜索：先查匹配昵称的用户，再按user_id过滤
        if (goodsFindDto.getNickname() != null && !goodsFindDto.getNickname().isEmpty()) {
            QueryWrapper<User> userQw = new QueryWrapper<>();
            userQw.like("nickname", goodsFindDto.getNickname());
            List<User> matchedUsers = userMapper.selectList(userQw);
            if (matchedUsers.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                map.put("total", 0);
                map.put("records", new ArrayList<>());
                return Result.succ(map);
            }
            List<String> userIds = matchedUsers.stream().map(User::getUserId).collect(Collectors.toList());
            queryWrapper.in("user_id", userIds);
        }
        // 非管理员只能看到审核通过的商品；管理员可以看到所有商品
        // 但用户查看自己的商品时可以看到所有审核状态
        if (!viewerIsAdmin) {
            if (goodsFindDto.getAuditState() != null) {
                // 前端明确传了审核状态，按该状态过滤
                queryWrapper.eq("audit_state", goodsFindDto.getAuditState());
            } else if (goodsFindDto.getUserId() != null && goodsFindDto.getUserId().equals(viewerId)) {
                // 用户查看自己的商品 - 显示所有审核状态
            } else {
                queryWrapper.eq("audit_state", 1);
            }
        } else {
            if (goodsFindDto.getAuditState() != null) {
                queryWrapper.eq("audit_state", goodsFindDto.getAuditState());
            }
        }
        // 非管理员：排除其他用户的锁定商品（自己的商品不受影响）
        if (!viewerIsAdmin) {
            queryWrapper.and(w -> w.ne("locked", 1).or().eq("user_id", viewerId));
        }
        // 排序
        String sortBy = goodsFindDto.getSortBy();
        String sortOrder = goodsFindDto.getSortOrder();
        if (sortBy != null && !sortBy.isEmpty()) {
            if ("asc".equalsIgnoreCase(sortOrder)) {
                queryWrapper.orderByAsc(sortBy);
            } else {
                queryWrapper.orderByDesc(sortBy);
            }
        } else {
            queryWrapper.orderByDesc("create_time");
        }
        Page<Goods> goodsPage = this.page(page, queryWrapper);
        // 为每条商品关联查询卖家昵称，并过滤锁定的商品
        List<Map<String, Object>> records = new ArrayList<>();
        int filteredOut = 0;
        for (Goods goods : goodsPage.getRecords()) {
            // 锁定过滤：非作者不能看到锁定的商品（管理员除外）
            if (!viewerIsAdmin && goods.getLocked() != null && goods.getLocked() == 1 && !goods.getUserId().equals(viewerId)) {
                filteredOut++;
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("goodsId", goods.getGoodsId());
            item.put("goodsName", goods.getGoodsName());
            item.put("goodsDetail", goods.getGoodsDetail());
            item.put("goodsImages", goods.getGoodsImages());
            item.put("goodsPrice", goods.getGoodsPrice());
            item.put("goodsCategoryId", goods.getGoodsCategoryId());
            item.put("goodsStatus", goods.getGoodsStatus());
            item.put("auditState", goods.getAuditState());
            item.put("goodsCount", goods.getGoodsCount());
            item.put("userId", goods.getUserId());
            item.put("createTime", goods.getCreateTime());
            item.put("updateTime", goods.getUpdateTime());
            item.put("viewNum", goods.getViewNum());
            item.put("shareNum", goods.getShareNum());
            item.put("locked", goods.getLocked());
            item.put("lockReason", goods.getLockReason());
            // 查询卖家昵称
            if (goods.getUserId() != null) {
                User seller = userMapper.selectById(goods.getUserId());
                item.put("nickname", seller != null ? seller.getNickname() : "未知");
            } else {
                item.put("nickname", "未知");
            }
            records.add(item);
        }
        Map<String, Object> map = new HashMap<>();
        // Adjust total to account for in-memory filtered records
        long adjustedTotal = goodsPage.getTotal() - filteredOut;
        map.put("total", Math.max(adjustedTotal, records.size()));
        map.put("records", records);
        return Result.succ(map);
    }

    @Override
    public Result getGoodsTotal() {
        long total = this.count();
        return Result.succ(total);
    }

    @Override
    public Result getGoodsByCategory() {
        List<GoodsCategory> categories = goodsCategoryMapper.selectList(null);
        List<Goods> goodsList = this.list();
        Map<Integer, Long> countMap = goodsList.stream()
                .filter(g -> g.getGoodsCategoryId() != null)
                .collect(Collectors.groupingBy(Goods::getGoodsCategoryId, Collectors.counting()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (GoodsCategory category : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", category.getGoodsCategoryName());
            item.put("value", countMap.getOrDefault(category.getGoodsCategoryId(), 0L));
            result.add(item);
        }
        return Result.succ(result);
    }

    @Override
    public Result auditGoods(GoodsAuditDto auditDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String adminId = JWTUtil.getUserId(token);
        Goods goods = this.getById(auditDto.getGoodsId());
        if (goods == null) {
            return Result.fail("商品不存在");
        }
        goods.setAuditState(auditDto.getAuditState());
        goods.setUpdateTime(new Date());
        this.updateById(goods);
        // 审核通过时发送关注者通知
        if (auditDto.getAuditState() != null && auditDto.getAuditState() == 1) {
            letterService.sendSystemNotification(goods.getUserId(), "您的商品「" + goods.getGoodsName() + "」审核已通过", "system", "goods", goods.getGoodsId());
            List<Follow> followList = followMapper.selectList(new QueryWrapper<Follow>().eq("following_id", goods.getUserId()));
            for (Follow f : followList) {
                if (!privacySettingService.checkVisibility(goods.getUserId(), f.getFollowerId(), "goods")) {
                    continue;
                }
                Letter letter = new Letter();
                letter.setLetterId(IdGenerator.generateId(IdGenerator.LETTER));
                letter.setReceiverId(f.getFollowerId());
                letter.setSenderId(goods.getUserId());
                letter.setLetterDetail("你关注的用户发布了新商品: " + goods.getGoodsName());
                letter.setLetterStatus(0);
                letter.setSessionId("goods_notify_" + f.getFollowerId() + "_" + goods.getUserId());
                letterMapper.insert(letter);
            }
        }
        // 审核不通过时发送通知给商品发布者
        if (auditDto.getAuditState() != null && auditDto.getAuditState() == 2) {
            String reason = (auditDto.getAuditFailedCause() != null && !auditDto.getAuditFailedCause().isEmpty()) ? auditDto.getAuditFailedCause() : "内容不符合规范";
            letterService.sendSystemNotification(goods.getUserId(), "您的商品「" + goods.getGoodsName() + "」审核未通过，原因：" + reason, "system", "goods", goods.getGoodsId());
        }
        return Result.succ("审核成功");
    }

    @Override
    public Goods updateViewNum(Goods goods) {
        goods.setViewNum(goods.getViewNum() + 1);
        this.updateById(goods);
        return goods;
    }
}
