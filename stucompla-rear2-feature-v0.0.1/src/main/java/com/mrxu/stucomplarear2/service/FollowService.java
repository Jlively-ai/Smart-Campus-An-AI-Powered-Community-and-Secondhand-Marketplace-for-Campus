package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.entity.Follow;
import com.mrxu.stucomplarear2.utils.response.Result;

public interface FollowService extends IService<Follow> {
    Result follow(String followerId, String followingId);
    Result unfollow(String followerId, String followingId);
    Result checkFollow(String followerId, String followingId);
    Result getFollowerList(String userId, Integer pageNum, Integer pageSize);
    Result getFollowingList(String userId, Integer pageNum, Integer pageSize);
    Result getFollowerCount(String userId);
    Result getFollowingCount(String userId);
}
