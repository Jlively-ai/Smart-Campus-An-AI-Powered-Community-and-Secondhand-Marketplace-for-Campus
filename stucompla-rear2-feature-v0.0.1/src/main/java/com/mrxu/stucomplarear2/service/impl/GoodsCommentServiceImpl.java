package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.entity.Goods;
import com.mrxu.stucomplarear2.entity.GoodsComment;
import com.mrxu.stucomplarear2.entity.Letter;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.entity.ViolationDelete;
import com.mrxu.stucomplarear2.mapper.GoodsCommentMapper;
import com.mrxu.stucomplarear2.mapper.GoodsMapper;
import com.mrxu.stucomplarear2.mapper.LetterMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.GoodsCommentService;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.service.PunishmentService;
import com.mrxu.stucomplarear2.service.ViolationDeleteService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoodsCommentServiceImpl extends ServiceImpl<GoodsCommentMapper, GoodsComment> implements GoodsCommentService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LetterMapper letterMapper;
    @Autowired
    private PunishmentService punishmentService;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private LetterService letterService;
    @Lazy
    @Autowired
    private ViolationDeleteService violationDeleteService;

    @Override
    public Result addComment(GoodsComment comment, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        // 禁言检查
        if (punishmentService.isUserMuted(userId)) {
            return Result.fail("您已被禁言，无法发表商品评价。" + punishmentService.getMuteReason(userId));
        }
        comment.setUserId(userId);
        comment.setCreateTime(new Date());
        // 保存mentionUsers
        if (comment.getMentionUsers() == null || comment.getMentionUsers().isEmpty()) {
            comment.setMentionUsers("[]");
        }
        this.save(comment);

        // 获取评论者昵称
        User commenter = userMapper.selectById(userId);
        String nickname = commenter != null ? (commenter.getNickname() != null ? commenter.getNickname() : commenter.getUsername()) : "用户";

        // 获取商品信息
        Goods goods = goodsMapper.selectById(comment.getGoodsId());
        String goodsName = goods != null ? goods.getGoodsName() : "未知商品";

        // 通知商品卖家有人评价了其商品（避免自通知）
        if (goods != null && goods.getUserId() != null && !userId.equals(goods.getUserId())) {
            letterService.sendSystemNotification(goods.getUserId(), "用户" + nickname + " 评价了你的商品「" + goodsName + "」", "comment", "goods", comment.getGoodsId());
        }

        // 发送@提及通知
        sendMentionNotifications(userId, comment.getMentionUsers(), "用户%s 在商品「" + goodsName + "」的评价中提到了你", "goods", comment.getGoodsId());

        return Result.succ("success");
    }

    @Override
    public Result deleteComment(String commentId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        GoodsComment comment = this.getById(commentId);
        if (comment == null) return Result.fail("评论不存在");
        if (!comment.getUserId().equals(userId)) return Result.fail("无权删除");
        this.removeById(commentId);
        return Result.succ("success");
    }

    @Override
    public Result deleteByAdmin(String commentId, String cause) {
        GoodsComment comment = this.getById(commentId);
        if (comment == null) {
            return Result.fail("comment not found");
        }
        String userId = comment.getUserId();
        this.removeById(commentId);
        // 发送通知给评价用户
        if (userId != null && !"0".equals(userId)) {
            Letter letter = new Letter();
            letter.setLetterId(IdGenerator.generateId(IdGenerator.LETTER));
            letter.setSenderId("0");
            letter.setReceiverId(userId);
            letter.setMessageType("system");
            String reason = (cause != null && !cause.isEmpty()) ? cause : "违反平台规定";
            letter.setLetterDetail("您的商品评价已被管理员删除，原因：" + reason);
            letter.setLetterStatus(0);
            letter.setSessionId("admin_delete_goods_comment_" + userId);
            letter.setCreateTime(new Date());
            letterMapper.insert(letter);
        }
        return Result.succ("success");
    }

    @Override
    public Result lockComment(String commentId, String cause, String handlerId) {
        GoodsComment comment = this.getById(commentId);
        if (comment == null) return Result.fail("评论不存在");
        if (comment.getLocked() != null && comment.getLocked() == 1) return Result.fail("评论已锁定");
        comment.setLocked(1);
        String safeReason = (cause != null && !cause.isEmpty()) ? cause : "违反社区规范";
        comment.setLockReason(safeReason);
        this.updateById(comment);
        // 创建违规删除记录用于申诉
        violationDeleteService.moveViolation(comment.getUserId(), "goods_comment_lock", commentId, safeReason, handlerId, "lock");
        return Result.succ("锁定成功");
    }

    @Override
    public Result unlockComment(String commentId) {
        GoodsComment comment = this.getById(commentId);
        if (comment == null) return Result.fail("评论不存在");
        comment.setLocked(0);
        comment.setLockReason(null);
        this.updateById(comment);
        // 删除对应的锁定违规记录
        QueryWrapper<ViolationDelete> qw = new QueryWrapper<>();
        qw.eq("item_id", commentId).eq("operation_type", "lock");
        violationDeleteService.remove(qw);
        return Result.succ("解锁成功");
    }

    @Override
    public Result listByGoodsId(String goodsId, Integer pageNum, Integer pageSize) {
        int pn = pageNum != null ? pageNum : 1;
        int ps = pageSize != null ? pageSize : 10;
        Page<GoodsComment> page = new Page<>(pn, ps);
        QueryWrapper<GoodsComment> wrapper = new QueryWrapper<>();
        wrapper.eq("goods_id", goodsId);
        wrapper.orderByDesc("create_time");
        Page<GoodsComment> result = this.page(page, wrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (GoodsComment c : result.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("commentId", c.getCommentId());
            item.put("goodsId", c.getGoodsId());
            item.put("userId", c.getUserId());
            // 锁定的评论隐藏内容
            if (c.getLocked() != null && c.getLocked() == 1) {
                item.put("content", "该评论已被锁定");
                item.put("locked", 1);
                item.put("lockReason", c.getLockReason());
            } else {
                item.put("content", c.getContent());
                item.put("locked", 0);
            }
            item.put("rating", c.getRating());
            item.put("createTime", c.getCreateTime());
            User user = userMapper.selectById(c.getUserId());
            if (user != null) {
                // 已注销用户显示用户已注销
                if (user.getStatus() != null && user.getStatus() == 2) {
                    item.put("nickname", "用户已注销");
                    item.put("avatar", null);
                } else {
                    item.put("nickname", user.getNickname());
                    item.put("avatar", user.getAvatar());
                }
            }
            records.add(item);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("records", records);
        return Result.succ(map);
    }

    @Override
    public Result listByAdmin(Integer pageNum, Integer pageSize) {
        int pn = pageNum != null ? pageNum : 1;
        int ps = pageSize != null ? pageSize : 10;
        Page<GoodsComment> page = new Page<>(pn, ps);
        QueryWrapper<GoodsComment> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        Page<GoodsComment> result = this.page(page, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("records", result.getRecords());
        return Result.succ(map);
    }

    @Override
    public Result getCommentTotal() {
        return Result.succ(this.count());
    }

    /**
     * 发送@提及通知给被提及的用户
     * @param senderId 发送者（当前用户）ID
     * @param mentionUsersJson 提及用户ID的JSON数组字符串
     * @param contentTemplate 通知内容模板，%s 会被替换为发送者昵称
     * @param targetType 目标类型（如 "goods"）
     * @param targetId 目标ID（如 goodsId）
     */
    private void sendMentionNotifications(String senderId, String mentionUsersJson, String contentTemplate, String targetType, String targetId) {
        if (mentionUsersJson == null || mentionUsersJson.isEmpty() || "[]".equals(mentionUsersJson)) {
            return;
        }
        try {
            JSONArray mentionArray = JSONArray.parseArray(mentionUsersJson);
            User sender = userMapper.selectById(senderId);
            String senderNickname = sender != null ? (sender.getNickname() != null ? sender.getNickname() : sender.getUsername()) : "用户";
            for (int i = 0; i < mentionArray.size(); i++) {
                String mentionedUserId = mentionArray.getString(i);
                // 不给自己发通知
                if (mentionedUserId.equals(senderId)) {
                    continue;
                }
                String content = String.format(contentTemplate, senderNickname);
                letterService.sendSystemNotification(mentionedUserId, content, "mention", targetType, targetId);
            }
        } catch (Exception e) {
            // 解析失败则忽略
        }
    }
}
