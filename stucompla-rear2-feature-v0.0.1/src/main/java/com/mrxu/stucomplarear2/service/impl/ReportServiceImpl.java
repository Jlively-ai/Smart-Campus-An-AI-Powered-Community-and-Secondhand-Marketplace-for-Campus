package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.dto.ReportDto;
import com.mrxu.stucomplarear2.dto.ReportFindDto;
import com.mrxu.stucomplarear2.entity.*;
import com.mrxu.stucomplarear2.mapper.ReportMapper;
import com.mrxu.stucomplarear2.service.*;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Autowired
    @Lazy
    private LetterService letterService;
    @Autowired
    @Lazy
    private PostService postService;
    @Autowired
    @Lazy
    private GoodsService goodsService;
    @Autowired
    @Lazy
    private WallService wallService;
    @Autowired
    @Lazy
    private CommentService commentService;
    @Autowired
    @Lazy
    private GoodsCommentService goodsCommentService;

    @Override
    public Result submitReport(ReportDto reportDto, HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(jwt);

        Report report = new Report();
        report.setReportId(IdGenerator.generateId(IdGenerator.REPORT));
        report.setReporterId(userId);
        report.setTargetType(reportDto.getTargetType());
        report.setTargetId(reportDto.getTargetId());
        report.setReason(reportDto.getReason());
        report.setStatus(0); // 待处理
        report.setCreateTime(new Date());
        report.setUpdateTime(new Date());
        this.save(report);
        return Result.succ("举报成功");
    }

    @Override
    public Map<String, Object> findReportList(ReportFindDto reportFindDto) {
        int pageNum = reportFindDto.getPageNum() != null ? reportFindDto.getPageNum() : 1;
        int pageSize = reportFindDto.getPageSize() != null ? reportFindDto.getPageSize() : 10;
        Page<Report> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Report> qw = new QueryWrapper<>();
        if (reportFindDto.getTargetType() != null && !reportFindDto.getTargetType().isEmpty()) {
            qw.eq("target_type", reportFindDto.getTargetType());
        }
        if (reportFindDto.getStatus() != null) {
            qw.eq("status", reportFindDto.getStatus());
        }
        if (reportFindDto.getKeyword() != null && !reportFindDto.getKeyword().isEmpty()) {
            qw.and(w -> w.like("reason", reportFindDto.getKeyword()).or().like("target_id", reportFindDto.getKeyword()));
        }
        qw.orderByDesc("create_time");
        this.page(page, qw);

        Map<String, Object> map = new HashMap<>();
        map.put("records", page.getRecords());
        map.put("total", page.getTotal());
        map.put("pages", page.getPages());
        map.put("current", page.getCurrent());
        return map;
    }

    @Override
    public Result handleReport(String reportId, Integer status, String handleResult, HttpServletRequest request) {
        return handleReport(reportId, status, handleResult, null, null, null, null, request);
    }

    public Result handleReport(String reportId, Integer status, String handleResult,
                               String punishType, String lockReason, String targetType, String targetId,
                               HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        String handlerId = JWTUtil.getUserId(jwt);

        Report report = this.getById(reportId);
        if (report == null) return Result.fail("举报不存在");

        report.setStatus(status);
        report.setHandlerId(handlerId);
        report.setHandleResult(handleResult);
        report.setUpdateTime(new Date());
        this.updateById(report);

        // 通知举报人处理结果
        String reporterId = report.getReporterId();
        String targetTypeName = "帖子";
        if ("goods".equals(report.getTargetType())) targetTypeName = "商品";
        else if ("wall".equals(report.getTargetType())) targetTypeName = "表白墙";
        else if ("comment".equals(report.getTargetType())) targetTypeName = "评论";
        else if ("goods_comment".equals(report.getTargetType())) targetTypeName = "商品评价";

        if (status == 1) {
            // 已处理 - 执行处罚
            String actualTargetType = targetType != null ? targetType : report.getTargetType();
            String actualTargetId = targetId != null ? targetId : report.getTargetId();

            if ("delete".equals(punishType)) {
                // 删除内容
                performDelete(actualTargetType, actualTargetId, handlerId, "举报处罚：" + (handleResult != null ? handleResult : "内容违规"));
            } else if ("lock".equals(punishType)) {
                // 锁定内容
                performLock(actualTargetType, actualTargetId, lockReason != null ? lockReason : "内容违规");
            }

            // 通知举报人
            letterService.sendSystemNotification(reporterId,
                "您举报的" + targetTypeName + "已处理，处罚结果：" + ("delete".equals(punishType) ? "内容已删除" : "内容已锁定") + "。" + (handleResult != null ? handleResult : ""),
                "system", "report", reportId);
        } else if (status == 2) {
            // 已驳回 - 通知举报人
            letterService.sendSystemNotification(reporterId,
                "您举报的" + targetTypeName + "已被驳回。" + (handleResult != null ? "原因：" + handleResult : ""),
                "system", "report", reportId);
        }

        return Result.succ("处理成功");
    }

    private void performDelete(String targetType, String targetId, String adminId, String reason) {
        try {
            if ("post".equals(targetType)) {
                Post post = postService.getById(targetId);
                if (post != null) {
                    post.setPostStatus(1); // 标记为删除
                    postService.updateById(post);
                    // 记录违规删除
                    ViolationDelete vd = new ViolationDelete();
                    vd.setId(IdGenerator.generateId(IdGenerator.VIOLATION_DELETE));
                    vd.setUserId(post.getUserId());
                    vd.setItemType("post");
                    vd.setItemId(targetId);
                    vd.setItemData(post.getTitle());
                    vd.setReason(reason);
                    vd.setOperationType("delete");
                    vd.setCreateTime(new Date());
                    vd.setUpdateTime(new Date());
                    // 通知作者
                    letterService.sendSystemNotification(post.getUserId(), "您的帖子「" + post.getTitle() + "」因「" + reason + "」已被删除", "system", "punishment", vd.getId());
                }
            } else if ("goods".equals(targetType)) {
                Goods goods = goodsService.getById(targetId);
                if (goods != null) {
                    goods.setGoodsStatus(false); // 下架
                    goods.setLocked(1); // 锁定
                    goodsService.updateById(goods);
                    ViolationDelete vd = new ViolationDelete();
                    vd.setId(IdGenerator.generateId(IdGenerator.VIOLATION_DELETE));
                    vd.setUserId(goods.getUserId());
                    vd.setItemType("goods");
                    vd.setItemId(targetId);
                    vd.setItemData(goods.getGoodsName());
                    vd.setReason(reason);
                    vd.setOperationType("delete");
                    vd.setCreateTime(new Date());
                    vd.setUpdateTime(new Date());
                    letterService.sendSystemNotification(goods.getUserId(), "您的商品「" + goods.getGoodsName() + "」因「" + reason + "」已被删除", "system", "punishment", vd.getId());
                }
            } else if ("wall".equals(targetType)) {
                Wall wall = wallService.getById(targetId);
                if (wall != null) {
                    wall.setLocked(1);
                    wall.setLockReason(reason);
                    wallService.updateById(wall);
                    ViolationDelete vd = new ViolationDelete();
                    vd.setId(IdGenerator.generateId(IdGenerator.VIOLATION_DELETE));
                    vd.setUserId(wall.getUserId());
                    vd.setItemType("wall");
                    vd.setItemId(targetId);
                    String preview = wall.getWallContent() != null ? wall.getWallContent().substring(0, Math.min(50, wall.getWallContent().length())) : "";
                    vd.setItemData(preview);
                    vd.setReason(reason);
                    vd.setOperationType("delete");
                    vd.setCreateTime(new Date());
                    vd.setUpdateTime(new Date());
                    letterService.sendSystemNotification(wall.getUserId(), "您的表白墙内容因「" + reason + "」已被删除", "system", "punishment", vd.getId());
                }
            } else if ("goods_comment".equals(targetType)) {
                GoodsComment goodsComment = goodsCommentService.getById(targetId);
                if (goodsComment != null) {
                    goodsCommentService.removeById(targetId);
                    letterService.sendSystemNotification(goodsComment.getUserId(), "您的商品评价因「" + reason + "」已被删除", "system", "punishment", targetId);
                }
            }
        } catch (Exception e) {
            // 忽略处罚执行错误
        }
    }

    private void performLock(String targetType, String targetId, String reason) {
        try {
            if ("post".equals(targetType)) {
                Post post = postService.getById(targetId);
                if (post != null) {
                    post.setPostStatus(1); // 锁定
                    post.setLockReason(reason);
                    postService.updateById(post);
                    letterService.sendSystemNotification(post.getUserId(), "您的帖子「" + post.getTitle() + "」因「" + reason + "」已被锁定", "system", "punishment", targetId);
                }
            } else if ("goods".equals(targetType)) {
                Goods goods = goodsService.getById(targetId);
                if (goods != null) {
                    goods.setLocked(1);
                    goods.setLockReason(reason);
                    goodsService.updateById(goods);
                    letterService.sendSystemNotification(goods.getUserId(), "您的商品「" + goods.getGoodsName() + "」因「" + reason + "」已被锁定", "system", "punishment", targetId);
                }
            } else if ("wall".equals(targetType)) {
                Wall wall = wallService.getById(targetId);
                if (wall != null) {
                    wall.setLocked(1);
                    wall.setLockReason(reason);
                    wallService.updateById(wall);
                    letterService.sendSystemNotification(wall.getUserId(), "您的表白墙内容因「" + reason + "」已被锁定", "system", "punishment", targetId);
                }
            } else if ("goods_comment".equals(targetType)) {
                goodsCommentService.lockComment(targetId, reason, "0");
            }
        } catch (Exception e) {
            // 忽略锁定执行错误
        }
    }

    @Override
    public List<Report> getMyReports(String userId) {
        QueryWrapper<Report> qw = new QueryWrapper<>();
        qw.eq("reporter_id", userId);
        qw.orderByDesc("create_time");
        return this.list(qw);
    }
}
