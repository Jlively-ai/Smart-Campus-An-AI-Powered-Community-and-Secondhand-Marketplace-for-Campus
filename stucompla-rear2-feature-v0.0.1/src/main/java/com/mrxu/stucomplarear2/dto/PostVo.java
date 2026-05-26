package com.mrxu.stucomplarear2.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mrxu.stucomplarear2.entity.Category;
import com.mrxu.stucomplarear2.entity.User;
import lombok.Data;

import java.util.Date;

@Data
public class PostVo {

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
     * 图片
     */
    private String images;

    /**
     * 评论数
     */
    private Integer commentNum;

    /**
     * 点击数
     */
    private Integer viewNum;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 发布人ID
     */
    private String userId;

    /**
     * 发布人信息
     */
    private User user;

    /**
     * 种类ID
     */
    private Category category;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 是否精帖
     */
    private Boolean bestPost;

    /**
     * 收藏数
     */
    private Integer collectNum;

    private Integer likeNum;

    private Integer shareNum;

    private Integer postStatus;

    private String visibility;

    private String blockedUsers;

    private String mentionUsers;

    /**
     * 当前用户是否已点赞
     */
    private Boolean hasLiked;

    /**
     * 当前用户是否已分享
     */
    private Boolean hasShared;

}
