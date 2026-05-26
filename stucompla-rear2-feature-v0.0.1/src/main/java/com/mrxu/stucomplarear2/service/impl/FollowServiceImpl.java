package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.entity.Follow;
import com.mrxu.stucomplarear2.entity.Letter;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.mapper.FollowMapper;
import com.mrxu.stucomplarear2.mapper.LetterMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.FollowService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LetterMapper letterMapper;

    @Override
    public Result follow(String followerId, String followingId) {
        if (followerId.equals(followingId)) return Result.fail("不能关注自己");
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId).eq("following_id", followingId);
        if (this.getOne(wrapper) != null) return Result.fail("已关注");
        Follow follow = new Follow();
        follow.setFollowId(IdGenerator.generateId(IdGenerator.FOLLOW));
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        this.save(follow);
        Letter letter = new Letter();
        letter.setLetterId(IdGenerator.generateId(IdGenerator.LETTER));
        letter.setReceiverId(followingId);
        letter.setSenderId(followerId);
        User follower = userMapper.selectById(followerId);
        letter.setLetterDetail((follower != null ? (follower.getNickname() != null ? follower.getNickname() : follower.getUsername()) : "用户") + " 关注了你");
        letter.setLetterStatus(0);
        letter.setMessageType("follow");
        String minId = followerId.compareTo(followingId) < 0 ? followerId : followingId;
        String maxId = followerId.compareTo(followingId) >= 0 ? followerId : followingId;
        letter.setSessionId("follow_" + minId + "_" + maxId);
        letterMapper.insert(letter);
        return Result.succ("关注成功");
    }

    @Override
    public Result unfollow(String followerId, String followingId) {
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId).eq("following_id", followingId);
        Follow follow = this.getOne(wrapper);
        if (follow == null) return Result.fail("未关注");
        this.removeById(follow.getFollowId());
        return Result.succ("取消关注成功");
    }

    @Override
    public Result checkFollow(String followerId, String followingId) {
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId).eq("following_id", followingId);
        return Result.succ(this.getOne(wrapper) != null);
    }

    @Override
    public Result getFollowerList(String userId, Integer pageNum, Integer pageSize) {
        Page<Follow> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("following_id", userId).orderByDesc("create_time");
        Page<Follow> result = this.page(page, wrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Follow f : result.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("followId", f.getFollowId());
            item.put("userId", f.getFollowerId());
            item.put("createTime", f.getCreateTime());
            User user = userMapper.selectById(f.getFollowerId());
            if (user != null) {
                item.put("nickname", user.getNickname());
                item.put("avatar", user.getAvatar());
                item.put("username", user.getUsername());
            }
            records.add(item);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("records", records);
        return Result.succ(map);
    }

    @Override
    public Result getFollowingList(String userId, Integer pageNum, Integer pageSize) {
        Page<Follow> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", userId).orderByDesc("create_time");
        Page<Follow> result = this.page(page, wrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Follow f : result.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("followId", f.getFollowId());
            item.put("userId", f.getFollowingId());
            item.put("createTime", f.getCreateTime());
            User user = userMapper.selectById(f.getFollowingId());
            if (user != null) {
                item.put("nickname", user.getNickname());
                item.put("avatar", user.getAvatar());
                item.put("username", user.getUsername());
            }
            records.add(item);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("records", records);
        return Result.succ(map);
    }

    @Override
    public Result getFollowerCount(String userId) {
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("following_id", userId);
        return Result.succ(this.count(wrapper));
    }

    @Override
    public Result getFollowingCount(String userId) {
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", userId);
        return Result.succ(this.count(wrapper));
    }
}
