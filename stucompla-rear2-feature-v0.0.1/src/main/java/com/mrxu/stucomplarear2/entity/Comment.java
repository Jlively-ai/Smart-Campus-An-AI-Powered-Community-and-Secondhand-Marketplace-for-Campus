package com.mrxu.stucomplarear2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.ASSIGN_ID)
    private String commentId;

    private String text;

    private String images;

    private String postId;

    private String parentId;

    private String userId;

    private String userType;

    private String targetType;

    private Integer likeNum;

    @com.baomidou.mybatisplus.annotation.TableField("mention_users")
    private String mentionUsers; // JSON数组，存储@提及的用户ID列表

    @TableField("locked")
    private Integer locked; // 0正常 1锁定

    @TableField("lock_reason")
    private String lockReason; // 锁定原因

    @TableField("audit_state")
    private Integer auditState; // 0待审核 1通过 2拒绝

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
