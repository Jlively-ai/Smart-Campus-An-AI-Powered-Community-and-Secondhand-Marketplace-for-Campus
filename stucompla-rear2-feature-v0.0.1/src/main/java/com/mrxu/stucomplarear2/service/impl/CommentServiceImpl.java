package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.dto.CommentDto;
import com.mrxu.stucomplarear2.dto.CommentFindDto;
import com.mrxu.stucomplarear2.entity.Comment;
import com.mrxu.stucomplarear2.entity.Letter;
import com.mrxu.stucomplarear2.entity.Post;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.entity.Admin;
import com.mrxu.stucomplarear2.entity.Wall;
import com.mrxu.stucomplarear2.mapper.CommentMapper;
import com.mrxu.stucomplarear2.mapper.LetterMapper;
import com.mrxu.stucomplarear2.mapper.PostMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.mapper.AdminMapper;
import com.mrxu.stucomplarear2.mapper.WallMapper;
import com.mrxu.stucomplarear2.service.CommentService;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.service.PunishmentService;
import com.mrxu.stucomplarear2.service.ViolationDeleteService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private PostMapper postMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private WallMapper wallMapper;
    @Autowired
    private LetterMapper letterMapper;
    @Autowired
    private LetterService letterService;
    @Autowired
    private PunishmentService punishmentService;
    @Autowired
    private ViolationDeleteService violationDeleteService;

    @Override
    public Result createComment(HttpServletRequest request, CommentDto commentDto) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        // 禁言检查
        if (punishmentService.isUserMuted(userId)) {
            return Result.fail("您已被禁言，无法发表评论。" + punishmentService.getMuteReason(userId));
        }

        // 判断目标类型：post 或 wall
        String targetType = commentDto.getTargetType() != null ? commentDto.getTargetType() : "post";
        boolean isWall = "wall".equals(targetType);

        if (!isWall) {
            Post post = postMapper.selectById(commentDto.getPostId());
            if (post == null) {
                return Result.fail("post not found");
            }
        } else {
            Wall wall = wallMapper.selectById(commentDto.getPostId());
            if (wall == null) {
                return Result.fail("wall not found");
            }
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setImages(commentDto.getImages());
        comment.setPostId(commentDto.getPostId());
        comment.setParentId(commentDto.getParentId() != null ? commentDto.getParentId() : "");
        comment.setUserId(userId);
        String role = JWTUtil.getRole(token);
        comment.setUserType(role != null && (role.equals("admin") || role.equals("super")) ? "admin" : "user");
        comment.setCommentId(IdGenerator.generateId(IdGenerator.COMMENT));
        comment.setCreateTime(new Date());
        comment.setTargetType(targetType);
        comment.setAuditState(0); // 待审核
        // 保存mentionUsers
        comment.setMentionUsers(commentDto.getMentionUsers() != null ? commentDto.getMentionUsers() : "[]");
        this.save(comment);

        // 获取评论者昵称
        String nickname;
        if ("admin".equals(comment.getUserType())) {
            Admin admin = adminMapper.selectById(comment.getUserId());
            nickname = admin != null ? admin.getUsername() : "管理员";
        } else {
            User user = userMapper.selectById(comment.getUserId());
            nickname = user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "用户";
        }

        if (isWall) {
            // 表白墙评论：通知表白墙作者
            Wall wall = wallMapper.selectById(commentDto.getPostId());
            if (wall != null && wall.getUserId() != null && !comment.getUserId().equals(wall.getUserId())) {
                letterService.sendSystemNotification(wall.getUserId(), nickname + " 评论了你的表白墙", "comment", "wall", wall.getWallId());
            }
        } else {
            // 帖子评论：更新帖子评论数并通知帖子作者
            Post post = postMapper.selectById(commentDto.getPostId());
            if (post != null) {
                post.setCommentNum(post.getCommentNum() + 1);
                postMapper.updateById(post);
                if (!comment.getUserId().equals(post.getUserId())) {
                    letterService.sendSystemNotification(post.getUserId(), nickname + " 评论了你的帖子「" + post.getTitle() + "」", "comment", "post", post.getPostId());
                }
            }
        }

        // 如果是回复评论，通知被回复的评论作者
        if (comment.getParentId() != null && !comment.getParentId().isEmpty()) {
            Comment parentComment = this.getById(comment.getParentId());
            if (parentComment != null && !comment.getUserId().equals(parentComment.getUserId())) {
                String replyTargetType = isWall ? "wall" : "post";
                String replyTargetId = isWall ? commentDto.getPostId() : commentDto.getPostId();
                letterService.sendSystemNotification(parentComment.getUserId(), nickname + " 回复了你的评论", "comment", replyTargetType, replyTargetId);
            }
        }

        // 发送@提及通知
        String mentionTargetType = isWall ? "wall" : "post";
        sendMentionNotifications(userId, commentDto.getMentionUsers(), "用户%s 在评论中提到了你", mentionTargetType, commentDto.getPostId());

        return Result.succ("success");
    }

    @Override
    public Result deleteCommentByUser(String commentId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Comment comment = this.getById(commentId);
        if (comment == null) {
            return Result.fail("comment not found");
        }
        if (!comment.getUserId().equals(userId)) {
            return Result.fail("no permission");
        }
        // 仅帖子评论更新帖子评论数
        if (!"wall".equals(comment.getTargetType())) {
            Post post = postMapper.selectById(comment.getPostId());
            if (post != null && post.getCommentNum() > 0) {
                post.setCommentNum(post.getCommentNum() - 1);
                postMapper.updateById(post);
            }
        }
        this.removeById(commentId);
        return Result.succ("success");
    }

    @Override
    public Result deleteCommentByAdmin(String commentId, String cause) {
        Comment comment = this.getById(commentId);
        if (comment == null) {
            return Result.fail("comment not found");
        }
        String userId = comment.getUserId();
        // 仅帖子评论更新帖子评论数
        if (!"wall".equals(comment.getTargetType())) {
            Post post = postMapper.selectById(comment.getPostId());
            if (post != null && post.getCommentNum() > 0) {
                post.setCommentNum(post.getCommentNum() - 1);
                postMapper.updateById(post);
            }
        }
        this.removeById(commentId);
        // 发送通知给评论用户
        if (userId != null && !"0".equals(userId)) {
            Letter letter = new Letter();
            letter.setLetterId(IdGenerator.generateId(IdGenerator.LETTER));
            letter.setSenderId("0");
            letter.setReceiverId(userId);
            letter.setMessageType("system");
            String reason = (cause != null && !cause.isEmpty()) ? cause : "违反社区规范";
            letter.setLetterDetail("您的评论已被管理员删除，原因：" + reason);
            letter.setLetterStatus(0);
            letter.setSessionId("admin_delete_comment_" + userId);
            letter.setCreateTime(new Date());
            letterMapper.insert(letter);
        }
        return Result.succ("success");
    }

    @Override
    public Result listCommentFromPost(String postId, int page, int size) {
        return listCommentFromPost(postId, page, size, null);
    }

    @Override
    public Result listCommentFromPost(String postId, int page, int size, String targetType) {
        Page<Comment> pageObj = new Page<>(page, size);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId);
        if (targetType != null && !targetType.isEmpty()) {
            queryWrapper.eq("target_type", targetType);
        }
        queryWrapper.orderByAsc("create_time");
        Page<Comment> commentPage = this.page(pageObj, queryWrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (int idx = 0; idx < commentPage.getRecords().size(); idx++) {
            Comment c = commentPage.getRecords().get(idx);
            // 过滤锁定的评论
            if (c.getLocked() != null && c.getLocked() == 1) {
                continue;
            }
            // 过滤未审核的评论（仅显示审核通过的）
            if (c.getAuditState() != null && c.getAuditState() != 1) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("commentId", c.getCommentId());
            item.put("text", c.getText());
            item.put("images", c.getImages());
            item.put("postId", c.getPostId());
            item.put("parentId", c.getParentId());
            item.put("userId", c.getUserId());
            item.put("createTime", c.getCreateTime());
            item.put("likeNum", c.getLikeNum());
            item.put("targetType", c.getTargetType());
            item.put("mentionUsers", c.getMentionUsers());
            String userType = c.getUserType();
            if ("admin".equals(userType)) {
                Admin admin = adminMapper.selectById(c.getUserId());
                if (admin != null) {
                    item.put("nickname", admin.getUsername());
                    item.put("avatar", null);
                    item.put("roleName", admin.getRoleId() != null && admin.getRoleId() == 1 ? "super" : "admin");
                }
            } else {
                User user = userMapper.selectById(c.getUserId());
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
                    Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", user.getUsername()).last("LIMIT 1"));
                    if (admin != null) {
                        item.put("roleName", admin.getRoleId() != null && admin.getRoleId() == 1 ? "super" : "admin");
                    }
                }
            }
            item.put("floor", idx + 1);
            if (c.getParentId() != null && !c.getParentId().isEmpty()) {
                Comment parent = this.getById(c.getParentId());
                if (parent != null) {
                    if ("admin".equals(parent.getUserType())) {
                        Admin parentAdmin = adminMapper.selectById(parent.getUserId());
                        if (parentAdmin != null) {
                            item.put("replyToNickname", parentAdmin.getUsername());
                            item.put("replyToSex", null);
                        }
                    } else {
                        User parentUser = userMapper.selectById(parent.getUserId());
                        if (parentUser != null) {
                            item.put("replyToNickname", parentUser.getNickname());
                            item.put("replyToSex", parentUser.getSex());
                        }
                    }
                }
            }
            records.add(item);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("total", commentPage.getTotal());
        map.put("records", records);
        return Result.succ(map);
    }

    @Override
    public Result listComment(CommentFindDto commentFindDto) {
        int pageNum = commentFindDto.getPageNum() != null ? commentFindDto.getPageNum() : 1;
        int pageSize = commentFindDto.getPageSize() != null ? commentFindDto.getPageSize() : 10;
        Page<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        if (commentFindDto.getText() != null && !commentFindDto.getText().isEmpty()) {
            queryWrapper.like("text", commentFindDto.getText());
        }
        queryWrapper.orderByDesc("create_time");
        Page<Comment> commentPage = this.page(page, queryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("total", commentPage.getTotal());
        map.put("records", commentPage.getRecords());
        return Result.succ(map);
    }

    @Override
    public Result getMyList(Integer page, Integer size, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        int pn = page != null ? page : 1;
        int ps = size != null ? size : 10;
        Page<Comment> pageObj = new Page<>(pn, ps);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        Page<Comment> commentPage = this.page(pageObj, queryWrapper);
        return Result.succ(commentPage);
    }

    @Override
    public Result getCommentTotal() {
        long total = this.count();
        return Result.succ(total);
    }

    @Override
    public Result getReceivedComments(Integer page, Integer size, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);

        // 查找当前用户的所有帖子ID
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.eq("user_id", userId).select("post_id");
        List<Post> myPosts = postMapper.selectList(postQueryWrapper);
        List<String> postIds = new ArrayList<>();
        for (Post p : myPosts) {
            postIds.add(p.getPostId());
        }

        Map<String, Object> map = new HashMap<>();
        if (postIds.isEmpty()) {
            map.put("total", 0);
            map.put("records", new ArrayList<>());
            return Result.succ(map);
        }

        // 查询这些帖子下的评论，排除自己的评论
        int pn = page != null ? page : 1;
        int ps = size != null ? size : 10;
        Page<Comment> pageObj = new Page<>(pn, ps);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("post_id", postIds);
        queryWrapper.ne("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        Page<Comment> commentPage = this.page(pageObj, queryWrapper);

        // 构建返回结果，附带帖子标题和评论者昵称
        List<Map<String, Object>> records = new ArrayList<>();
        for (Comment c : commentPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("commentId", c.getCommentId());
            item.put("text", c.getText());
            item.put("images", c.getImages());
            item.put("postId", c.getPostId());
            item.put("parentId", c.getParentId());
            item.put("userId", c.getUserId());
            item.put("createTime", c.getCreateTime());

            // 帖子标题
            Post post = postMapper.selectById(c.getPostId());
            if (post != null) {
                item.put("postTitle", post.getTitle());
            }

            // 评论者昵称
            String userType = c.getUserType();
            if ("admin".equals(userType)) {
                Admin admin = adminMapper.selectById(c.getUserId());
                item.put("nickname", admin != null ? admin.getUsername() : "管理员");
                item.put("sex", null);
            } else {
                User user = userMapper.selectById(c.getUserId());
                item.put("nickname", user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "用户");
                item.put("sex", user != null ? user.getSex() : null);
            }

            records.add(item);
        }

        map.put("total", commentPage.getTotal());
        map.put("records", records);
        return Result.succ(map);
    }

    /**
     * 发送@提及通知给被提及的用户
     * @param senderId 发送者（当前用户）ID
     * @param mentionUsersJson 提及用户ID的JSON数组字符串
     * @param contentTemplate 通知内容模板，%s 会被替换为发送者昵称
     * @param targetType 目标类型（如 "post"）
     * @param targetId 目标ID（如 postId）
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

    @Override
    public Result lockComment(String commentId, String cause, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String handlerId = JWTUtil.getUserId(token);
        Comment comment = this.getById(commentId);
        if (comment == null) return Result.fail("评论不存在");
        if (comment.getLocked() != null && comment.getLocked() == 1) return Result.fail("评论已锁定");
        comment.setLocked(1);
        comment.setLockReason(cause);
        this.updateById(comment);
        violationDeleteService.moveViolation(comment.getUserId(), "comment_lock", commentId, cause, handlerId, "lock");
        return Result.succ("锁定成功");
    }

    @Override
    public Result unlockComment(String commentId) {
        Comment comment = this.getById(commentId);
        if (comment == null) return Result.fail("评论不存在");
        comment.setLocked(0);
        comment.setLockReason(null);
        this.updateById(comment);
        QueryWrapper<com.mrxu.stucomplarear2.entity.ViolationDelete> qw = new QueryWrapper<>();
        qw.eq("item_id", commentId).eq("operation_type", "lock");
        violationDeleteService.remove(qw);
        return Result.succ("解锁成功");
    }

    @Override
    public Result auditComment(String commentId, Integer auditState, String auditFailedCause) {
        Comment comment = this.getById(commentId);
        if (comment == null) return Result.fail("评论不存在");
        comment.setAuditState(auditState);
        this.updateById(comment);
        // 审核通过时发送通知
        if (auditState != null && auditState == 1) {
            letterService.sendSystemNotification(comment.getUserId(), "您的评论审核已通过", "system", "comment", commentId);
        }
        // 审核不通过时发送通知
        if (auditState != null && auditState == 2) {
            String reason = (auditFailedCause != null && !auditFailedCause.isEmpty()) ? auditFailedCause : "内容不符合规范";
            letterService.sendSystemNotification(comment.getUserId(), "您的评论审核未通过，原因：" + reason, "system", "comment", commentId);
        }
        return Result.succ("审核成功");
    }
}
