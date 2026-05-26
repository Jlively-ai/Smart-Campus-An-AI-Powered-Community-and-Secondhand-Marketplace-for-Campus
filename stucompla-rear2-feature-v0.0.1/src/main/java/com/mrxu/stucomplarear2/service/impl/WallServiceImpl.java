package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrxu.stucomplarear2.dto.WallApplyDto;
import com.mrxu.stucomplarear2.dto.WallAuditDto;
import com.mrxu.stucomplarear2.dto.WallEditDto;
import com.mrxu.stucomplarear2.dto.WallFindDto;
import com.mrxu.stucomplarear2.entity.Wall;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.entity.Follow;
import com.mrxu.stucomplarear2.mapper.AdminMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.mapper.WallMapper;
import com.mrxu.stucomplarear2.mapper.FollowMapper;
import com.mrxu.stucomplarear2.service.WallService;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.service.PunishmentService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WallServiceImpl extends ServiceImpl<WallMapper, Wall> implements WallService {

    @Resource
    private UserMapper userMapper;
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private PunishmentService punishmentService;
    @Autowired
    private LetterService letterService;

    @Override
    public Result apply(WallApplyDto wallApplyDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        // 禁言检查
        if (punishmentService.isUserMuted(userId)) {
            return Result.fail("您已被禁言，无法发布表白墙内容。" + punishmentService.getMuteReason(userId));
        }
        Wall wall = new Wall();
        wall.setWallId(IdGenerator.generateId(IdGenerator.WALL));
        wall.setWallContent(wallApplyDto.getWallContent());
        wall.setWallImages(wallApplyDto.getWallImages());
        wall.setUserId(userId);
        wall.setIsAnonymous(wallApplyDto.getIsAnonymous() != null && wallApplyDto.getIsAnonymous());
        wall.setVisibility(wallApplyDto.getVisibility() != null ? wallApplyDto.getVisibility() : "all");
        // 保存blockedUsers
        if ("custom".equals(wallApplyDto.getVisibility()) && wallApplyDto.getBlockedUsers() != null && !wallApplyDto.getBlockedUsers().isEmpty()) {
            try {
                wall.setBlockedUsers(new ObjectMapper().writeValueAsString(wallApplyDto.getBlockedUsers()));
            } catch (Exception e) {
                wall.setBlockedUsers("[]");
            }
        } else {
            wall.setBlockedUsers("[]");
        }
        // 保存mentionUsers
        wall.setMentionUsers(wallApplyDto.getMentionUsers() != null ? wallApplyDto.getMentionUsers() : "[]");
        wall.setAuditState(0);
        wall.setCreateTime(new Date());
        wall.setUpdateTime(new Date());
        this.save(wall);
        // 审核通过后再发送@提及通知，此处不发送
        return Result.succ("success");
    }

    @Override
    public String audit(WallAuditDto wallAuditDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String adminId = JWTUtil.getUserId(token);
        Wall wall = this.getById(wallAuditDto.getWallId());
        if (wall == null) {
            return "not found";
        }
        wall.setAuditState(wallAuditDto.getAuditState());
        wall.setAdminId(adminId);
        wall.setCause(wallAuditDto.getAuditFailedCause());
        wall.setUpdateTime(new Date());
        this.updateById(wall);
        // 审核通过时发送@提及通知
        if (wallAuditDto.getAuditState() != null && wallAuditDto.getAuditState() == 1) {
            letterService.sendSystemNotification(wall.getUserId(), "您的表白墙内容审核已通过", "system", "wall", wall.getWallId());
            sendMentionNotifications(wall.getUserId(), wall.getMentionUsers(), "用户%s 在表白墙中提到了你", "wall", wall.getWallId());
        }
        // 审核不通过时发送通知给用户
        if (wallAuditDto.getAuditState() != null && wallAuditDto.getAuditState() == 2) {
            String reason = (wallAuditDto.getAuditFailedCause() != null && !wallAuditDto.getAuditFailedCause().isEmpty()) ? wallAuditDto.getAuditFailedCause() : "内容不符合规范";
            letterService.sendSystemNotification(wall.getUserId(), "您的表白墙内容审核未通过，原因：" + reason, "system", "wall", wall.getWallId());
        }
        return "审核成功";
    }

    @Override
    public Map<String, Object> findWall(WallFindDto wallFindDto, HttpServletRequest request) {
        return findWall(wallFindDto, false, request);
    }

    public Map<String, Object> findWall(WallFindDto wallFindDto, boolean hideAnonymousUserInfo, HttpServletRequest request) {
        // 获取当前用户ID和角色（可能未登录）
        String viewerIdTemp = null;
        boolean viewerIsAdminTemp = false;
        try {
            String token = request.getHeader("Authorization");
            if (token != null && !token.isEmpty()) {
                viewerIdTemp = JWTUtil.getUserId(token);
                String role = JWTUtil.getRole(token);
                viewerIsAdminTemp = "admin".equals(role) || "super".equals(role);
            }
        } catch (Exception ignored) {
        }
        final String viewerId = viewerIdTemp;
        final boolean viewerIsAdmin = viewerIsAdminTemp;

        int pageNum = wallFindDto.getPageNum() != null ? wallFindDto.getPageNum() : 1;
        int pageSize = wallFindDto.getPageSize() != null ? wallFindDto.getPageSize() : 10;
        Page<Wall> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Wall> queryWrapper = new QueryWrapper<>();
        // 非管理员只能看到审核通过的表白墙
        // 但用户查看自己的表白墙时可以看到所有审核状态
        if (wallFindDto.getAuditState() != null) {
            queryWrapper.eq("audit_state", wallFindDto.getAuditState());
        } else if (!viewerIsAdmin) {
            // 如果是查看自己的表白墙，显示所有审核状态
            if (wallFindDto.getUserId() != null && wallFindDto.getUserId().equals(viewerId)) {
                // 用户查看自己的表白墙 - 显示所有审核状态
            } else {
                queryWrapper.eq("audit_state", 1);
            }
        }
        if (wallFindDto.getUserId() != null) {
            queryWrapper.eq("user_id", wallFindDto.getUserId());
        }
        // 昵称模糊搜索：先查匹配昵称的用户，再按user_id过滤
        if (wallFindDto.getNickname() != null && !wallFindDto.getNickname().isEmpty()) {
            QueryWrapper<User> userQw = new QueryWrapper<>();
            userQw.like("nickname", wallFindDto.getNickname());
            List<User> matchedUsers = userMapper.selectList(userQw);
            if (matchedUsers.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                map.put("total", 0);
                map.put("records", new ArrayList<>());
                return map;
            }
            List<String> userIds = matchedUsers.stream().map(User::getUserId).collect(Collectors.toList());
            queryWrapper.in("user_id", userIds);
        }
        if (wallFindDto.getWallContent() != null && !wallFindDto.getWallContent().isEmpty()) {
            queryWrapper.like("wall_content", wallFindDto.getWallContent());
        }
        queryWrapper.orderByDesc("create_time");
        Page<Wall> wallPage = this.page(page, queryWrapper);

        // 根据visibility过滤表白墙（匿名表白墙的userId在过滤时仍使用原始值）
        // 管理员绕过可见性检查，可以看到所有内容
        List<Wall> filteredWalls = new ArrayList<>();
        for (Wall wall : wallPage.getRecords()) {
            // 锁定过滤：非作者不能看到锁定的表白墙（管理员除外）
            if (!viewerIsAdmin && wall.getLocked() != null && wall.getLocked() == 1 && !wall.getUserId().equals(viewerId)) {
                continue;
            }
            // 管理员始终可见
            if (viewerIsAdmin) {
                filteredWalls.add(wall);
                continue;
            }
            // 匿名表白墙：非作者只能看到visibility=all的，作者自己可以看到自己的
            if (wall.getIsAnonymous() != null && wall.getIsAnonymous()) {
                if (wall.getUserId() != null && wall.getUserId().equals(viewerId)) {
                    // 作者自己始终可见
                    filteredWalls.add(wall);
                } else if (wall.getVisibility() == null || "all".equals(wall.getVisibility())) {
                    // 匿名+all → 其他人可见（但userId会被隐藏）
                    filteredWalls.add(wall);
                }
                // 匿名+following/mutual/self → 其他人不可见
                continue;
            }
            if (isWallVisible(wall, viewerId)) {
                filteredWalls.add(wall);
            }
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (Wall wall : filteredWalls) {
            Map<String, Object> item = new HashMap<>();
            item.put("wallId", wall.getWallId());
            item.put("wallContent", wall.getWallContent());
            item.put("wallImages", wall.getWallImages());
            item.put("auditState", wall.getAuditState());
            item.put("cause", wall.getCause());
            item.put("isAnonymous", wall.getIsAnonymous());
            item.put("visibility", wall.getVisibility());
            item.put("blockedUsers", wall.getBlockedUsers());
            item.put("mentionUsers", wall.getMentionUsers());
            item.put("createTime", wall.getCreateTime());
            item.put("updateTime", wall.getUpdateTime());
            item.put("viewNum", wall.getViewNum());
            item.put("likeNum", wall.getLikeNum());
            item.put("collectNum", wall.getCollectNum());
            item.put("shareNum", wall.getShareNum());
            if (hideAnonymousUserInfo && wall.getIsAnonymous() != null && wall.getIsAnonymous()) {
                item.put("userId", null);
                item.put("nickname", "匿名用户");
                item.put("avatar", null);
                item.put("sex", null);
            } else {
                item.put("userId", wall.getUserId());
                User user = userMapper.selectById(wall.getUserId());
                if (user != null) {
                    // 已注销用户显示用户已注销
                    if (user.getStatus() != null && user.getStatus() == 2) {
                        item.put("nickname", "用户已注销");
                        item.put("avatar", null);
                        item.put("sex", null);
                    } else {
                        item.put("nickname", user.getNickname());
                        item.put("avatar", user.getAvatar());
                        item.put("sex", user.getSex());
                    }
                }
            }
            records.add(item);
        }
        Map<String, Object> map = new HashMap<>();
        // Adjust total to account for in-memory filtered records
        long adjustedTotal = wallPage.getTotal() - (wallPage.getRecords().size() - filteredWalls.size());
        map.put("total", Math.max(adjustedTotal, filteredWalls.size()));
        map.put("records", records);
        return map;
    }

    /**
     * 判断表白墙对当前用户是否可见
     * - all 或 null: 所有人可见
     * - following: 仅关注了作者的人可见
     * - mutual: 仅互相关注的人可见
     * - self: 仅作者自己可见
     * - custom: 不给屏蔽列表中的用户看
     */
    private boolean isWallVisible(Wall wall, String viewerId) {
        String visibility = wall.getVisibility();
        // all 或 null → 所有人可见
        if (visibility == null || "all".equals(visibility)) {
            return true;
        }
        // self → 仅作者自己可见
        if ("self".equals(visibility)) {
            return wall.getUserId() != null && wall.getUserId().equals(viewerId);
        }
        // custom → 不给屏蔽列表中的用户看
        if ("custom".equals(visibility)) {
            // 未登录用户可以看到custom的内容
            if (viewerId == null) {
                return true;
            }
            // 作者自己始终可见
            if (wall.getUserId() != null && wall.getUserId().equals(viewerId)) {
                return true;
            }
            // 检查当前用户是否在屏蔽列表中
            String blockedUsersJson = wall.getBlockedUsers();
            if (blockedUsersJson != null && !blockedUsersJson.isEmpty() && !"[]".equals(blockedUsersJson)) {
                try {
                    List<String> blockedUsers = new ObjectMapper().readValue(blockedUsersJson, List.class);
                    return !blockedUsers.contains(viewerId);
                } catch (Exception e) {
                    return true;
                }
            }
            return true;
        }
        // 未登录用户只能看到 all 的表白墙
        if (viewerId == null) {
            return false;
        }
        // 作者自己始终可见
        if (wall.getUserId() != null && wall.getUserId().equals(viewerId)) {
            return true;
        }
        // following → 仅关注了作者的人可见
        if ("following".equals(visibility)) {
            return followMapper.selectCount(new QueryWrapper<Follow>()
                    .eq("follower_id", viewerId)
                    .eq("following_id", wall.getUserId())) > 0;
        }
        // mutual → 仅互相关注的人可见
        if ("mutual".equals(visibility)) {
            boolean viewerFollowsAuthor = followMapper.selectCount(new QueryWrapper<Follow>()
                    .eq("follower_id", viewerId)
                    .eq("following_id", wall.getUserId())) > 0;
            boolean authorFollowsViewer = followMapper.selectCount(new QueryWrapper<Follow>()
                    .eq("follower_id", wall.getUserId())
                    .eq("following_id", viewerId)) > 0;
            return viewerFollowsAuthor && authorFollowsViewer;
        }
        return true;
    }

    @Override
    public Result editWall(WallEditDto wallEditDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Wall wall = this.getById(wallEditDto.getWallId());
        if (wall == null) {
            return Result.fail("wall not found");
        }
        if (!wall.getUserId().equals(userId)) {
            return Result.fail("no permission");
        }
        wall.setWallContent(wallEditDto.getWallContent());
        wall.setWallImages(wallEditDto.getWallImages());
        if (wallEditDto.getVisibility() != null) {
            wall.setVisibility(wallEditDto.getVisibility());
        }
        // 保存blockedUsers
        if ("custom".equals(wallEditDto.getVisibility()) && wallEditDto.getBlockedUsers() != null && !wallEditDto.getBlockedUsers().isEmpty()) {
            try {
                wall.setBlockedUsers(new ObjectMapper().writeValueAsString(wallEditDto.getBlockedUsers()));
            } catch (Exception e) {
                wall.setBlockedUsers("[]");
            }
        } else {
            wall.setBlockedUsers("[]");
        }
        // 保存mentionUsers
        wall.setMentionUsers(wallEditDto.getMentionUsers() != null ? wallEditDto.getMentionUsers() : "[]");
        wall.setAuditState(0); // 编辑后重新进入待审核
        wall.setUpdateTime(new Date());
        this.updateById(wall);
        // 编辑后需要重新审核，审核通过后再发送@提及通知
        return Result.succ("success");
    }

    @Override
    public Result findMyWall(WallFindDto wallFindDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        wallFindDto.setUserId(userId);
        Map<String, Object> map = findWall(wallFindDto, request);
        return Result.succ(map);
    }

    @Override
    public Result getWallTotal() {
        long total = this.count();
        return Result.succ(total);
    }

    @Override
    public Result getWallData() {
        return Result.succ(null);
    }

    /**
     * 发送@提及通知给被提及的用户
     * @param senderId 发送者（当前用户）ID
     * @param mentionUsersJson 提及用户ID的JSON数组字符串
     * @param contentTemplate 通知内容模板，%s 会被替换为发送者昵称
     * @param targetType 目标类型（如 "wall"）
     * @param targetId 目标ID（如 wallId）
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
