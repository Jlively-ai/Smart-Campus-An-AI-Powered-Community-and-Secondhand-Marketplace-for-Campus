package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.dto.PostEditDto;
import com.mrxu.stucomplarear2.dto.PostFindDto;
import com.mrxu.stucomplarear2.dto.PostPublishDto;
import com.mrxu.stucomplarear2.dto.PostAuditDto;
import com.mrxu.stucomplarear2.entity.Post;
import com.mrxu.stucomplarear2.utils.response.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface PostService extends IService<Post> {

    Result publishPost(HttpServletRequest request, PostPublishDto postDto);

    Result editPost(HttpServletRequest request, PostEditDto postEditDto);

    Result deleteMyPost(String postId, HttpServletRequest request);

    Result lockedPost(String postId, String cause);

    Result unLockPost(String postId);

    Result deleteByAdmin(String postId, String cause);

    Map<String, Object> findPostList(PostFindDto postFindDto, HttpServletRequest request);

    Result getPostTotal();

    Result getPostData();

    Post updateViewNum(Post post);

    Result getMyLikes(HttpServletRequest request);

    Result auditPost(PostAuditDto auditDto, HttpServletRequest request);
}
