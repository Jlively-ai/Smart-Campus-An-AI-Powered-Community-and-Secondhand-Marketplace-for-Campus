package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.entity.GoodsComment;
import com.mrxu.stucomplarear2.utils.response.Result;

import javax.servlet.http.HttpServletRequest;

public interface GoodsCommentService extends IService<GoodsComment> {
    Result addComment(GoodsComment comment, HttpServletRequest request);
    Result deleteComment(String commentId, HttpServletRequest request);
    Result deleteByAdmin(String commentId, String cause);
    Result lockComment(String commentId, String cause, String handlerId);
    Result unlockComment(String commentId);
    Result listByGoodsId(String goodsId, Integer pageNum, Integer pageSize);
    Result listByAdmin(Integer pageNum, Integer pageSize);
    Result getCommentTotal();
}
