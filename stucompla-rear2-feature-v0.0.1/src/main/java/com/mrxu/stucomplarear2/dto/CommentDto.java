package com.mrxu.stucomplarear2.dto;

import lombok.Data;

@Data
public class CommentDto {
    private String postId;
    private String parentId;
    private String text;
    private String images;

    private String targetType;

    /**
     * @提及的用户ID列表，JSON数组格式如 ["userId1","userId2"]
     */
    private String mentionUsers;
}
