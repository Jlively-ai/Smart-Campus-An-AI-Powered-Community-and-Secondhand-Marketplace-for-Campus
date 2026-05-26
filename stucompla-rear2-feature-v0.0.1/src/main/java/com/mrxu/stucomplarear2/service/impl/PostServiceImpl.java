package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.dto.PostEditDto;
import com.mrxu.stucomplarear2.dto.PostFindDto;
import com.mrxu.stucomplarear2.dto.PostPublishDto;
import com.mrxu.stucomplarear2.dto.PostAuditDto;
import com.mrxu.stucomplarear2.entity.Post;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrxu.stucomplarear2.entity.Category;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.entity.Admin;
import com.mrxu.stucomplarear2.entity.Follow;
import com.mrxu.stucomplarear2.entity.Letter;
import com.mrxu.stucomplarear2.entity.PostLike;
import com.mrxu.stucomplarear2.mapper.CategoryMapper;
import com.mrxu.stucomplarear2.mapper.PostMapper;
import com.mrxu.stucomplarear2.mapper.PostLikeMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.mapper.AdminMapper;
import com.mrxu.stucomplarear2.mapper.FollowMapper;
import com.mrxu.stucomplarear2.mapper.LetterMapper;
import com.mrxu.stucomplarear2.service.PostService;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.service.PrivacySettingService;
import com.mrxu.stucomplarear2.service.PunishmentService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private LetterMapper letterMapper;
    @Autowired
    private PostLikeMapper postLikeMapper;
    @Autowired
    private PrivacySettingService privacySettingService;
    @Autowired
    private PunishmentService punishmentService;
    @Autowired
    private LetterService letterService;

    @Override
    public Result publishPost(HttpServletRequest request, PostPublishDto postDto) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        // 禁言检查
        if (punishmentService.isUserMuted(userId)) {
            return Result.fail("您已被禁言，无法发布帖子。" + punishmentService.getMuteReason(userId));
        }
        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setDetail(postDto.getDetail());
        post.setImages(postDto.getImages());
        post.setCategoryId(postDto.getCategoryId());
        post.setUserId(userId);
        post.setCommentNum(0);
        post.setViewNum(0);
        post.setBestPost(false);
        post.setCollectNum(0);
        post.setPostStatus(0);
        post.setAuditState(0); // 待审核
        post.setVisibility(postDto.getVisibility() != null ? postDto.getVisibility() : "all");
        // 保存blockedUsers
        if ("custom".equals(postDto.getVisibility()) && postDto.getBlockedUsers() != null && !postDto.getBlockedUsers().isEmpty()) {
            try {
                post.setBlockedUsers(new ObjectMapper().writeValueAsString(postDto.getBlockedUsers()));
            } catch (Exception e) {
                post.setBlockedUsers("[]");
            }
        } else {
            post.setBlockedUsers("[]");
        }
        // 保存mentionUsers
        post.setMentionUsers(postDto.getMentionUsers() != null ? postDto.getMentionUsers() : "[]");
        post.setPostId(IdGenerator.generateId(IdGenerator.POST));
        post.setCreateTime(new Date());
        post.setUpdateTime(new Date());
        this.save(post);
        // 审核通过后再发送@提及通知和关注者通知，此处不发送
        return Result.succ("success");
    }

    @Override
    public Result editPost(HttpServletRequest request, PostEditDto postEditDto) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Post post = this.getById(postEditDto.getPostId());
        if (post == null) {
            return Result.fail("post not found");
        }
        if (!post.getUserId().equals(userId)) {
            return Result.fail("no permission");
        }
        post.setTitle(postEditDto.getTitle());
        post.setDetail(postEditDto.getDetail());
        post.setImages(postEditDto.getImages());
        post.setCategoryId(postEditDto.getCategoryId());
        if (postEditDto.getVisibility() != null) {
            post.setVisibility(postEditDto.getVisibility());
        }
        // 保存blockedUsers
        if ("custom".equals(postEditDto.getVisibility()) && postEditDto.getBlockedUsers() != null && !postEditDto.getBlockedUsers().isEmpty()) {
            try {
                post.setBlockedUsers(new ObjectMapper().writeValueAsString(postEditDto.getBlockedUsers()));
            } catch (Exception e) {
                post.setBlockedUsers("[]");
            }
        } else {
            post.setBlockedUsers("[]");
        }
        // 保存mentionUsers
        post.setMentionUsers(postEditDto.getMentionUsers() != null ? postEditDto.getMentionUsers() : "[]");
        post.setAuditState(0); // 编辑后重新进入待审核
        post.setUpdateTime(new Date());
        this.updateById(post);
        // 编辑后需要重新审核，审核通过后再发送@提及通知
        return Result.succ("success");
    }

    @Override
    public Result deleteMyPost(String postId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Post post = this.getById(postId);
        if (post == null) {
            return Result.fail("post not found");
        }
        if (!post.getUserId().equals(userId)) {
            return Result.fail("no permission");
        }
        this.removeById(postId);
        return Result.succ("success");
    }

    @Override
    public Result lockedPost(String postId, String cause) {
        Post post = this.getById(postId);
        if (post == null) {
            return Result.fail("post not found");
        }
        post.setPostStatus(1);
        this.updateById(post);
        return Result.succ("success");
    }

    @Override
    public Result unLockPost(String postId) {
        Post post = this.getById(postId);
        if (post == null) {
            return Result.fail("post not found");
        }
        post.setPostStatus(0);
        this.updateById(post);
        return Result.succ("success");
    }

    @Override
    public Result deleteByAdmin(String postId, String cause) {
        Post post = this.getById(postId);
        if (post == null) {
            return Result.fail("post not found");
        }
        String userId = post.getUserId();
        this.removeById(postId);
        // 发送通知给帖子作者
        if (userId != null && !"0".equals(userId)) {
            Letter letter = new Letter();
            letter.setLetterId(IdGenerator.generateId(IdGenerator.LETTER));
            letter.setSenderId("0");
            letter.setReceiverId(userId);
            letter.setMessageType("system");
            String reason = (cause != null && !cause.isEmpty()) ? cause : "违反社区规范";
            letter.setLetterDetail("您的帖子「" + post.getTitle() + "」已被管理员删除，原因：" + reason);
            letter.setLetterStatus(0);
            letter.setSessionId("admin_delete_post_" + userId);
            letter.setCreateTime(new Date());
            letterMapper.insert(letter);
        }
        return Result.succ("success");
    }

    @Override
    public Map<String, Object> findPostList(PostFindDto postFindDto, HttpServletRequest request) {
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

        int pageNum = postFindDto.getPageNum() != null ? postFindDto.getPageNum() : 1;
        int pageSize = postFindDto.getPageSize() != null ? postFindDto.getPageSize() : 10;
        Page<Post> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postFindDto.getTitle() != null && !postFindDto.getTitle().isEmpty()) {
            queryWrapper.like("title", postFindDto.getTitle());
        }
        if (postFindDto.getCategoryId() != null) {
            queryWrapper.eq("category_id", postFindDto.getCategoryId());
        }
        if (postFindDto.getUserId() != null && !postFindDto.getUserId().isEmpty()) {
            queryWrapper.eq("user_id", postFindDto.getUserId());
        }
        // 昵称模糊搜索：先查匹配昵称的用户，再按user_id过滤
        if (postFindDto.getNickname() != null && !postFindDto.getNickname().isEmpty()) {
            QueryWrapper<User> userQw = new QueryWrapper<>();
            userQw.like("nickname", postFindDto.getNickname());
            List<User> matchedUsers = userMapper.selectList(userQw);
            if (matchedUsers.isEmpty()) {
                // 没有匹配的用户，返回空结果
                Map<String, Object> map = new HashMap<>();
                map.put("total", 0);
                map.put("records", new ArrayList<>());
                return map;
            }
            List<String> userIds = matchedUsers.stream().map(User::getUserId).collect(Collectors.toList());
            queryWrapper.in("user_id", userIds);
        }
        // 非管理员只能看到审核通过的帖子；管理员可以看到所有帖子
        // 但用户查看自己的帖子时可以看到所有审核状态
        if (!viewerIsAdmin) {
            if (postFindDto.getAuditState() != null) {
                // 前端明确传了审核状态，按该状态过滤
                queryWrapper.eq("audit_state", postFindDto.getAuditState());
            } else if (postFindDto.getUserId() != null && postFindDto.getUserId().equals(viewerId)) {
                // 用户查看自己的帖子 - 显示所有审核状态
            } else {
                queryWrapper.eq("audit_state", 1);
            }
        } else {
            if (postFindDto.getAuditState() != null) {
                queryWrapper.eq("audit_state", postFindDto.getAuditState());
            }
        }
        // 非管理员：排除其他用户的锁定帖子（自己的帖子不受影响）
        if (!viewerIsAdmin) {
            queryWrapper.and(w -> w.ne("post_status", 1).or().eq("user_id", viewerId));
        }
        // 排序
        String sortBy = postFindDto.getSortBy();
        String sortOrder = postFindDto.getSortOrder();
        if (sortBy != null && !sortBy.isEmpty()) {
            String column = sortBy;
            if ("likeNum".equals(sortBy)) column = "like_num";
            else if ("viewNum".equals(sortBy)) column = "view_num";
            else if ("commentNum".equals(sortBy)) column = "comment_num";
            else if ("shareNum".equals(sortBy)) column = "share_num";
            else if ("collectNum".equals(sortBy)) column = "collect_num";
            if ("asc".equalsIgnoreCase(sortOrder)) {
                queryWrapper.orderByAsc(column);
            } else {
                queryWrapper.orderByDesc(column);
            }
        } else {
            queryWrapper.orderByDesc("create_time");
        }
        Page<Post> postPage = this.page(page, queryWrapper);

        // 根据visibility过滤帖子（管理员绕过可见性检查）
        List<Post> filteredPosts = new ArrayList<>();
        for (Post post : postPage.getRecords()) {
            // 锁定过滤：非作者不能看到锁定的帖子（管理员除外）
            if (!viewerIsAdmin && post.getPostStatus() != null && post.getPostStatus() == 1 && !post.getUserId().equals(viewerId)) {
                continue;
            }
            if (viewerIsAdmin || isPostVisible(post, viewerId)) {
                filteredPosts.add(post);
            }
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (Post post : filteredPosts) {
            Map<String, Object> item = new HashMap<>();
            item.put("postId", post.getPostId());
            item.put("title", post.getTitle());
            item.put("detail", post.getDetail());
            item.put("images", post.getImages());
            item.put("userId", post.getUserId());
            item.put("categoryId", post.getCategoryId());
            item.put("commentNum", post.getCommentNum());
            item.put("viewNum", post.getViewNum());
            item.put("bestPost", post.getBestPost());
            item.put("collectNum", post.getCollectNum());
            item.put("likeNum", post.getLikeNum());
            item.put("shareNum", post.getShareNum());
            item.put("postStatus", post.getPostStatus());
            item.put("auditState", post.getAuditState());
            item.put("visibility", post.getVisibility());
            item.put("blockedUsers", post.getBlockedUsers());
            item.put("mentionUsers", post.getMentionUsers());
            item.put("createTime", post.getCreateTime());
            item.put("updateTime", post.getUpdateTime());
            User user = userMapper.selectById(post.getUserId());
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
                    Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", user.getUsername()).last("LIMIT 1"));
                    if (admin != null && admin.getRoleId() != null) {
                        item.put("roleName", admin.getRoleId() == 1 ? "super" : "admin");
                    }
                }
            }
            Category category = categoryMapper.selectById(post.getCategoryId());
            if (category != null) {
                item.put("categoryName", category.getCategoryName());
            }
            records.add(item);
        }
        Map<String, Object> map = new HashMap<>();
        // Adjust total to account for in-memory filtered records
        long adjustedTotal = postPage.getTotal() - (postPage.getRecords().size() - filteredPosts.size());
        map.put("total", Math.max(adjustedTotal, filteredPosts.size()));
        map.put("records", records);
        return map;
    }

    /**
     * 判断帖子对当前用户是否可见
     * - all 或 null: 所有人可见
     * - following: 仅关注了作者的人可见
     * - mutual: 仅互相关注的人可见
     * - self: 仅作者自己可见
     * - custom: 不给屏蔽列表中的用户看
     */
    private boolean isPostVisible(Post post, String viewerId) {
        String visibility = post.getVisibility();
        // all 或 null → 所有人可见
        if (visibility == null || "all".equals(visibility)) {
            return true;
        }
        // self → 仅作者自己可见
        if ("self".equals(visibility)) {
            return post.getUserId() != null && post.getUserId().equals(viewerId);
        }
        // custom → 不给屏蔽列表中的用户看
        if ("custom".equals(visibility)) {
            // 未登录用户可以看到custom的内容
            if (viewerId == null) {
                return true;
            }
            // 作者自己始终可见
            if (post.getUserId() != null && post.getUserId().equals(viewerId)) {
                return true;
            }
            // 检查当前用户是否在屏蔽列表中
            String blockedUsersJson = post.getBlockedUsers();
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
        // 未登录用户只能看到 all 的帖子
        if (viewerId == null) {
            return false;
        }
        // 作者自己始终可见
        if (post.getUserId() != null && post.getUserId().equals(viewerId)) {
            return true;
        }
        // following → 仅关注了作者的人可见
        if ("following".equals(visibility)) {
            return followMapper.selectCount(new QueryWrapper<Follow>()
                    .eq("follower_id", viewerId)
                    .eq("following_id", post.getUserId())) > 0;
        }
        // mutual → 仅互相关注的人可见
        if ("mutual".equals(visibility)) {
            boolean viewerFollowsAuthor = followMapper.selectCount(new QueryWrapper<Follow>()
                    .eq("follower_id", viewerId)
                    .eq("following_id", post.getUserId())) > 0;
            boolean authorFollowsViewer = followMapper.selectCount(new QueryWrapper<Follow>()
                    .eq("follower_id", post.getUserId())
                    .eq("following_id", viewerId)) > 0;
            return viewerFollowsAuthor && authorFollowsViewer;
        }
        return true;
    }

    @Override
    public Result getPostTotal() {
        long total = this.count();
        return Result.succ(total);
    }

    @Override
    public Result getPostData() {
        List<Category> categories = categoryMapper.selectList(null);
        List<Post> posts = this.list();
        Map<Integer, Long> countMap = posts.stream()
                .filter(p -> p.getCategoryId() != null)
                .collect(Collectors.groupingBy(Post::getCategoryId, Collectors.counting()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Category category : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", category.getCategoryName());
            item.put("value", countMap.getOrDefault(category.getCategoryId(), 0L));
            result.add(item);
        }
        return Result.succ(result);
    }

    @Override
    public Post updateViewNum(Post post) {
        post.setViewNum(post.getViewNum() + 1);
        this.updateById(post);
        return post;
    }

    @Override
    public Result getMyLikes(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        // 查询当前用户的所有点赞记录
        List<PostLike> postLikes = postLikeMapper.selectList(
                new QueryWrapper<PostLike>().eq("user_id", userId).orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (PostLike pl : postLikes) {
            Post post = this.getById(pl.getPostId());
            if (post == null) continue;
            // 过滤掉审核未通过的帖子
            if (post.getAuditState() != null && post.getAuditState() != 1) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("postId", post.getPostId());
            item.put("title", post.getTitle());
            item.put("detail", post.getDetail());
            item.put("images", post.getImages());
            item.put("userId", post.getUserId());
            item.put("categoryId", post.getCategoryId());
            item.put("commentNum", post.getCommentNum());
            item.put("viewNum", post.getViewNum());
            item.put("bestPost", post.getBestPost());
            item.put("collectNum", post.getCollectNum());
            item.put("likeNum", post.getLikeNum());
            item.put("shareNum", post.getShareNum());
            item.put("postStatus", post.getPostStatus());
            item.put("auditState", post.getAuditState());
            item.put("visibility", post.getVisibility());
            item.put("createTime", post.getCreateTime());
            item.put("updateTime", post.getUpdateTime());
            item.put("likeTime", pl.getCreateTime());
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                if (user.getStatus() != null && user.getStatus() == 2) {
                    item.put("nickname", "用户已注销");
                    item.put("avatar", null);
                } else {
                    item.put("nickname", user.getNickname());
                    item.put("avatar", user.getAvatar());
                }
            }
            Category category = categoryMapper.selectById(post.getCategoryId());
            if (category != null) {
                item.put("categoryName", category.getCategoryName());
            }
            result.add(item);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("total", result.size());
        map.put("records", result);
        return Result.succ(map);
    }

    @Override
    public Result auditPost(PostAuditDto auditDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String adminId = JWTUtil.getUserId(token);
        Post post = this.getById(auditDto.getPostId());
        if (post == null) {
            return Result.fail("帖子不存在");
        }
        post.setAuditState(auditDto.getAuditState());
        post.setUpdateTime(new Date());
        this.updateById(post);
        if (auditDto.getAuditState() != null && auditDto.getAuditState() == 1) {
            letterService.sendSystemNotification(post.getUserId(), "您的帖子「" + post.getTitle() + "」审核已通过", "system", "post", post.getPostId());
            sendMentionNotifications(post.getUserId(), post.getMentionUsers(), "用户%s 在帖子「" + post.getTitle() + "」中提到了你", "post", post.getPostId());
            List<Follow> followList = followMapper.selectList(new QueryWrapper<Follow>().eq("following_id", post.getUserId()));
            for (Follow f : followList) {
                if (!isPostVisible(post, f.getFollowerId())) {
                    continue;
                }
                if (!privacySettingService.checkVisibility(post.getUserId(), f.getFollowerId(), "posts")) {
                    continue;
                }
                Letter letter = new Letter();
                letter.setLetterId(IdGenerator.generateId(IdGenerator.LETTER));
                letter.setReceiverId(f.getFollowerId());
                letter.setSenderId(post.getUserId());
                letter.setLetterDetail("你关注的用户发布了新帖子: " + post.getTitle());
                letter.setLetterStatus(0);
                letter.setSessionId("post_notify_" + f.getFollowerId() + "_" + post.getUserId());
                letterMapper.insert(letter);
            }
        }
        // 审核不通过时发送通知给帖子作者
        if (auditDto.getAuditState() != null && auditDto.getAuditState() == 2) {
            String reason = (auditDto.getAuditFailedCause() != null && !auditDto.getAuditFailedCause().isEmpty()) ? auditDto.getAuditFailedCause() : "内容不符合规范";
            letterService.sendSystemNotification(post.getUserId(), "您的帖子「" + post.getTitle() + "」审核未通过，原因：" + reason, "system", "post", post.getPostId());
        }
        return Result.succ("审核成功");
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
}
