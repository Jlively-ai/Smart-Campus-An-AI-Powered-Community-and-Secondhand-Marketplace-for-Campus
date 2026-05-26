package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.dto.CommentDto;
import com.mrxu.stucomplarear2.dto.CommentFindDto;
import com.mrxu.stucomplarear2.entity.Comment;
import com.mrxu.stucomplarear2.utils.response.Result;

import javax.servlet.http.HttpServletRequest;

public interface CommentService extends IService<Comment> {

    Result createComment(HttpServletRequest request, CommentDto commentDto);

    Result deleteCommentByUser(String commentId, HttpServletRequest request);

    Result deleteCommentByAdmin(String commentId, String cause);

    Result listCommentFromPost(String postId, int page, int size);

    Result listCommentFromPost(String postId, int page, int size, String targetType);

    Result listComment(CommentFindDto commentFindDto);

    Result getMyList(Integer page, Integer size, HttpServletRequest request);

    Result getCommentTotal();

    Result getReceivedComments(Integer page, Integer size, HttpServletRequest request);

    Result lockComment(String commentId, String cause, HttpServletRequest request);

    Result unlockComment(String commentId);

    Result auditComment(String commentId, Integer auditState, String auditFailedCause);
}
