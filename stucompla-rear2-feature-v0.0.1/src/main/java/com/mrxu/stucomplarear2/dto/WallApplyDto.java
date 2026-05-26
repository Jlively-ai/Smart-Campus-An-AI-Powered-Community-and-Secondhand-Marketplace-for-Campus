package com.mrxu.stucomplarear2.dto;

import lombok.Data;

@Data
public class WallApplyDto {
    /**
     * 墙内容
     */
    private String wallContent;

    /**
     * 墙图片
     */
    private String wallImages;

    /**
     * 是否匿名发布
     */
    private Boolean isAnonymous;

    /**
     * 可见范围: all=所有人, following=关注的人, mutual=互相关注, self=仅自己, custom=不给谁看
     */
    private String visibility;

    /**
     * 不给谁看的用户ID列表
     */
    private java.util.List<String> blockedUsers;

    /**
     * @提及的用户ID列表，JSON数组格式如 ["userId1","userId2"]
     */
    private String mentionUsers;

}
