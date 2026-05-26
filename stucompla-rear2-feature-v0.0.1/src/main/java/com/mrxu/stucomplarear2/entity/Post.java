package com.mrxu.stucomplarear2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

// ALTER TABLE post ADD COLUMN visibility VARCHAR(20) DEFAULT 'all' COMMENT '可见范围: all/following/mutual/self';
// ALTER TABLE post ADD COLUMN audit_state INT DEFAULT 0 COMMENT '审核状态 0待审核 1通过 2拒绝';
// ALTER TABLE post ADD COLUMN lock_reason VARCHAR(500) DEFAULT NULL COMMENT '锁定原因';
@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.ASSIGN_ID)
    private String postId;

    private String title;

    private String detail;

    private String images;

    private String userId;

    private Integer categoryId;

    private Integer commentNum;

    private Integer viewNum;

    private Boolean bestPost;

    private Integer collectNum;

    private Integer likeNum;

    private Integer shareNum;

    private Integer postStatus;

    @TableField("audit_state")
    private Integer auditState; // 审核状态 0待审核 1通过 2拒绝

    @TableField("visibility")
    private String visibility; // all=所有人, following=关注的人, mutual=互相关注, self=仅自己, custom=不给谁看

    @TableField("blocked_users")
    private String blockedUsers; // JSON数组，存储不给看的用户ID列表

    @TableField("mention_users")
    private String mentionUsers; // JSON数组，存储@提及的用户ID列表

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField("lock_reason")
    private String lockReason; // 锁定原因

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
