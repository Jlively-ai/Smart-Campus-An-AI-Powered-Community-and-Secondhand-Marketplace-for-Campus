package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.dto.CollectFindDto;
import com.mrxu.stucomplarear2.entity.Collect;
import com.mrxu.stucomplarear2.entity.Post;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.mapper.CollectMapper;
import com.mrxu.stucomplarear2.mapper.PostMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.CollectService;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.service.impl.DailyStatsService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {

    @Autowired
    private PostMapper postMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LetterService letterService;
    @Autowired
    private DailyStatsService dailyStatsService;

    @Override
    public Result checkCollect(String postId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        QueryWrapper<Collect> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("post_id", postId);
        Collect collect = this.getOne(queryWrapper);
        if (collect != null) {
            return Result.succ(true);
        }
        return Result.succ(false);
    }

    @Override
    public Result add(String postId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return Result.fail("post not found");
        }
        QueryWrapper<Collect> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("post_id", postId);
        if (this.getOne(queryWrapper) != null) {
            return Result.fail("already collected");
        }
        Collect collect = new Collect();
        collect.setCollectId(IdGenerator.generateId(IdGenerator.COLLECT));
        collect.setUserId(userId);
        collect.setPostId(postId);
        collect.setCreateTime(new Date());
        this.save(collect);
        post.setCollectNum(post.getCollectNum() + 1);
        postMapper.updateById(post);
        dailyStatsService.incrementStat("post", post.getPostId(), post.getUserId(), "collectNum");
        // 发送收藏通知
        if (!userId.equals(post.getUserId())) {
            User collector = userMapper.selectById(userId);
            String nickname = collector != null ? (collector.getNickname() != null ? collector.getNickname() : collector.getUsername()) : "用户";
            letterService.sendSystemNotification(post.getUserId(), nickname + " 收藏了你的帖子「" + post.getTitle() + "」", "collect", "post", postId);
        }
        return Result.succ("success");
    }

    @Override
    public Result deleteCollect(String postId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        QueryWrapper<Collect> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("post_id", postId);
        Collect collect = this.getOne(queryWrapper);
        if (collect == null) {
            return Result.fail("not collected");
        }
        this.removeById(collect.getCollectId());
        Post post = postMapper.selectById(postId);
        if (post != null && post.getCollectNum() > 0) {
            post.setCollectNum(post.getCollectNum() - 1);
            postMapper.updateById(post);
        }
        return Result.succ("success");
    }

    @Override
    public Result listCollect(CollectFindDto collectFindDto) {
        int pageNum = collectFindDto.getPageNum() != null ? collectFindDto.getPageNum() : 1;
        int pageSize = collectFindDto.getPageSize() != null ? collectFindDto.getPageSize() : 10;
        Page<Collect> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Collect> queryWrapper = new QueryWrapper<>();
        if (collectFindDto.getUserId() != null) {
            queryWrapper.eq("user_id", collectFindDto.getUserId());
        }
        queryWrapper.orderByDesc("create_time");
        Page<Collect> collectPage = this.page(page, queryWrapper);
        return Result.succ(collectPage);
    }
}
