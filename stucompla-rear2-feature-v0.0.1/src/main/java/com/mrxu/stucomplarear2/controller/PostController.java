package com.mrxu.stucomplarear2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrxu.stucomplarear2.dto.PostEditDto;
import com.mrxu.stucomplarear2.dto.PostFindDto;
import com.mrxu.stucomplarear2.dto.PostPublishDto;
import com.mrxu.stucomplarear2.dto.PostAuditDto;
import com.mrxu.stucomplarear2.dto.PostVo;
import com.mrxu.stucomplarear2.entity.Category;
import com.mrxu.stucomplarear2.entity.Post;
import com.mrxu.stucomplarear2.entity.PostLike;
import com.mrxu.stucomplarear2.entity.ContentShare;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.entity.ViolationDelete;
import com.mrxu.stucomplarear2.mapper.CategoryMapper;
import com.mrxu.stucomplarear2.mapper.PostLikeMapper;
import com.mrxu.stucomplarear2.mapper.ContentShareMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.PostService;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.service.RecycleBinService;
import com.mrxu.stucomplarear2.service.impl.DailyStatsService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.service.ViolationDeleteService;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Mr.Xu
 * @since 2021-12-27
 */
@CrossOrigin
@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PostService postService;
    @Autowired
    private LetterService letterService;
    @Autowired
    private PostLikeMapper postLikeMapper;
    @Autowired
    private RecycleBinService recycleBinService;
    @Autowired
    private ViolationDeleteService violationDeleteService;
    @Autowired
    private DailyStatsService dailyStatsService;
    @Autowired
    private ContentShareMapper contentShareMapper;

    @ApiOperation("发帖")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/publish")
    public Result publish(HttpServletRequest request, @RequestBody PostPublishDto postDto) {
        //System.out.println(postDto);
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", postDto.getCategoryId());
        if (categoryMapper.selectOne(queryWrapper) == null || postDto.getTitle().isEmpty() || postDto.getDetail().isEmpty()) {
            return Result.fail("参数错误");
        }
        if (postDto.getTitle().length() < 1 || postDto.getTitle().length() > 30) {
            return Result.fail("标题长度只能在1-30位");
        }
        Result publishResult = postService.publishPost(request, postDto);
        if (publishResult.getCode() != 200) {
            return publishResult;
        }
        return Result.succ(postDto);
    }

    @ApiOperation("修改帖子")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/edit")
    public Result edit(HttpServletRequest request, @RequestBody PostEditDto postEditDto) {
        //System.out.println(postDto);
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", postEditDto.getCategoryId());
        if (categoryMapper.selectOne(queryWrapper) == null || postEditDto.getTitle().isEmpty() || postEditDto.getDetail().isEmpty()) {
            return Result.fail("种类参数错误");
        }
        Result result = postService.editPost(request, postEditDto);
        return result;
    }

    @ApiOperation("用户删除帖子")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/{postId}")
    public Result deleteMyPost(@PathVariable("postId") String postId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        return recycleBinService.moveToRecycleBin(userId, "post", postId);
    }

    @ApiOperation("管理员锁定帖子")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/lockedPost")
    public Result lockedPost(String postId, String cause, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String handlerId = JWTUtil.getUserId(token);
        Post post = postService.getById(postId);
        if (post == null) return Result.fail("帖子不存在");
        if (post.getPostStatus() != null && post.getPostStatus() == 1) return Result.fail("帖子已锁定");
        post.setPostStatus(1);
        post.setLockReason(cause);
        post.setUpdateTime(new Date());
        postService.updateById(post);
        violationDeleteService.moveViolation(post.getUserId(), "post_lock", postId, cause, handlerId, "lock");
        return Result.succ("锁定成功");
    }

    @ApiOperation("解锁帖子")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/unLockPost")
    public Result unLockPost(String postId) {
        Post post = postService.getById(postId);
        if (post == null) return Result.fail("帖子不存在");
        post.setPostStatus(0);
        post.setLockReason(null);
        post.setUpdateTime(new Date());
        postService.updateById(post);
        // 删除对应的锁定违规记录
        QueryWrapper<ViolationDelete> qw = new QueryWrapper<>();
        qw.eq("item_id", postId).eq("operation_type", "lock");
        violationDeleteService.remove(qw);
        return Result.succ("解锁成功");
    }

    @ApiOperation("管理员删除帖子")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/deleteByAdmin")
    public Result deleteByAdmin(String postId, String cause, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String handlerId = JWTUtil.getUserId(token);
        Post post = postService.getById(postId);
        if (post == null) return Result.fail("帖子不存在");
        return violationDeleteService.moveViolation(post.getUserId(), "post", postId, cause, handlerId, "delete");
    }

    @ApiOperation("审核帖子")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/audit")
    public Result auditPost(@RequestBody PostAuditDto auditDto, HttpServletRequest request) {
        return postService.auditPost(auditDto, request);
    }

    @ApiOperation("帖子详情")
    @GetMapping("/{postId}")
    public Result getPost(@PathVariable("postId") String postId, HttpServletRequest request) {
        try {
            // 获取当前用户ID和角色
            String viewerId = null;
            boolean viewerIsAdmin = false;
            try {
                String token = request.getHeader("Authorization");
                if (token != null && !token.isEmpty()) {
                    viewerId = JWTUtil.getUserId(token);
                    String role = JWTUtil.getRole(token);
                    viewerIsAdmin = "admin".equals(role) || "super".equals(role);
                }
            } catch (Exception ignored) {}

            QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("post_id", postId);
            Post post = postService.getOne(queryWrapper);
            if (post == null) {
                return Result.fail("帖子不存在");
            }
            // 审核状态检查：非管理员只能查看审核通过的帖子
            if (!viewerIsAdmin
                    && post.getAuditState() != null && post.getAuditState() != 1) {
                return Result.fail("帖子不存在或未通过审核");
            }
            post = postService.updateViewNum(post);
            dailyStatsService.incrementStat("post", post.getPostId(), post.getUserId(), "viewNum");
            PostVo postVo = new PostVo();
            BeanUtils.copyProperties(post, postVo);
            //查对应的发布人信息
            User user = userMapper.selectById(post.getUserId());
            postVo.setUser(user);
            //查对应的帖子类型信息
            Category category = categoryMapper.selectById(post.getCategoryId());
            postVo.setCategory(category);

            // Check if current user has liked this post
            if (viewerId != null) {
                try {
                    QueryWrapper<PostLike> likeQw = new QueryWrapper<>();
                    likeQw.eq("user_id", viewerId).eq("post_id", postId);
                    postVo.setHasLiked(postLikeMapper.selectCount(likeQw) > 0);
                } catch (Exception e) {
                    postVo.setHasLiked(false);
                }
                try {
                    QueryWrapper<ContentShare> shareQw = new QueryWrapper<>();
                    shareQw.eq("user_id", viewerId).eq("item_type", "post").eq("item_id", postId);
                    postVo.setHasShared(contentShareMapper.selectCount(shareQw) > 0);
                } catch (Exception e) {
                    postVo.setHasShared(false);
                }
            } else {
                postVo.setHasLiked(false);
                postVo.setHasShared(false);
            }

            return Result.succ(postVo);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        }
    }

    @ApiOperation("获取帖子列表")
//    @RequiresRoles(value = {"admin", "super","user"}, logical = Logical.OR)
    @GetMapping("/list")
    public Result listPost(PostFindDto postFindDto, HttpServletRequest request) {
        Map<String, Object> map = postService.findPostList(postFindDto, request);
        return Result.succ(map);
    }
//
//    @ApiOperation("获取帖子收藏数量")
//    @GetMapping("/getCollectNum")
//    public Result getCollectNum(Integer postId) {
////        int collectNum = postService.getCollectNum(postId);
//        return null;
//    }

    @ApiOperation("获取帖子总数")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/getPostTotal")
    public Result getPostTotal() {
        Result result = postService.getPostTotal();
        return result;
    }

    @ApiOperation("帖子分类统计")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/getPostData")
    public Result getPostData() {
        Result result = postService.getPostData();
        return result;
    }

    @ApiOperation("点赞帖子")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/like/{postId}")
    public Result likePost(@PathVariable("postId") String postId, HttpServletRequest request) {
        Post post = postService.getById(postId);
        if (post == null) return Result.fail("帖子不存在");
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        // 检查是否已点赞，防止重复
        QueryWrapper<PostLike> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("post_id", postId);
        if (postLikeMapper.selectCount(qw) > 0) {
            return Result.fail("已点赞过该帖子");
        }
        // 记录点赞
        PostLike postLike = new PostLike();
        postLike.setLikeId(IdGenerator.generateId(IdGenerator.POST_LIKE));
        postLike.setUserId(userId);
        postLike.setPostId(postId);
        postLike.setCreateTime(new Date());
        postLikeMapper.insert(postLike);
        post.setLikeNum(post.getLikeNum() == null ? 1 : post.getLikeNum() + 1);
        postService.updateById(post);
        dailyStatsService.incrementStat("post", post.getPostId(), post.getUserId(), "likeNum");
        // 发送点赞通知
        if (!userId.equals(post.getUserId())) {
            User liker = userMapper.selectById(userId);
            String nickname = liker != null ? (liker.getNickname() != null ? liker.getNickname() : liker.getUsername()) : "用户";
            letterService.sendSystemNotification(post.getUserId(), nickname + " 赞了你的帖子「" + post.getTitle() + "」", "like", "post", postId);
        }
        return Result.succ(post.getLikeNum());
    }

    @ApiOperation("取消点赞")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/unlike/{postId}")
    public Result unlikePost(@PathVariable("postId") String postId, HttpServletRequest request) {
        Post post = postService.getById(postId);
        if (post == null) return Result.fail("帖子不存在");
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        // 删除点赞记录
        QueryWrapper<PostLike> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("post_id", postId);
        postLikeMapper.delete(qw);
        post.setLikeNum(post.getLikeNum() == null || post.getLikeNum() <= 0 ? 0 : post.getLikeNum() - 1);
        postService.updateById(post);
        return Result.succ(post.getLikeNum());
    }

    @ApiOperation("分享帖子")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/share/{postId}")
    public Result sharePost(@PathVariable("postId") String postId, HttpServletRequest request) {
        Post post = postService.getById(postId);
        if (post == null) return Result.fail("帖子不存在");
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        // 检查是否已分享，防止重复
        QueryWrapper<ContentShare> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("item_type", "post").eq("item_id", postId);
        if (contentShareMapper.selectCount(qw) > 0) {
            return Result.fail("已分享过该帖子");
        }
        // 记录分享
        ContentShare share = new ContentShare();
        share.setShareId(IdGenerator.generateId(IdGenerator.CONTENT_SHARE));
        share.setUserId(userId);
        share.setItemType("post");
        share.setItemId(postId);
        share.setCreateTime(new Date());
        contentShareMapper.insert(share);
        post.setShareNum(post.getShareNum() == null ? 1 : post.getShareNum() + 1);
        postService.updateById(post);
        dailyStatsService.incrementStat("post", post.getPostId(), post.getUserId(), "shareNum");
        return Result.succ(post.getShareNum());
    }

    @ApiOperation("获取我的点赞列表")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/myLikes")
    public Result getMyLikes(HttpServletRequest request) {
        return postService.getMyLikes(request);
    }


}
