package com.mrxu.stucomplarear2.dto;

import lombok.Data;

@Data
public class PostFindDto {

    /**
     * 帖子ID
     */
    private String postId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String detail;

    /**
     * 评论数
     */
    private Integer commentNum;

    /**
     * 点击数
     */
    private Integer viewNum;

    /**
     * 发布人ID
     */
    private String userId;

    /**
     * 种类ID
     */
    private Integer categoryId;

    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 页面大小
     */
    private Integer pageSize;

    private String sort;

    private String sortBy;

    private String sortOrder;

    /**
     * 是否精帖
     */
    private Boolean bestPost;

    /**
     * 帖子状态
     */
    private Integer postStatus;

    /**
     * 审核状态：0-待审 1-审核通过 2-未通过
     */
    private Integer auditState;

    /**
     * 作者昵称（模糊搜索）
     */
    private String nickname;
}
