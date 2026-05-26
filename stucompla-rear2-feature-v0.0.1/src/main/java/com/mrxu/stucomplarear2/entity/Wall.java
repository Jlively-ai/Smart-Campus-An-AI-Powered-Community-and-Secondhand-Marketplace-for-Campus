package com.mrxu.stucomplarear2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

// ALTER TABLE wall ADD COLUMN visibility VARCHAR(20) DEFAULT 'all' COMMENT '可见范围: all/following/mutual/self';
// ALTER TABLE wall ADD COLUMN locked INT DEFAULT 0 COMMENT '0正常 1锁定';
// ALTER TABLE wall ADD COLUMN lock_reason VARCHAR(500) DEFAULT NULL COMMENT '锁定原因';
@Data
@TableName("wall")
public class Wall {

    @TableId(type = IdType.ASSIGN_ID)
    private String wallId;

    private String wallContent;

    private String wallImages;

    private String userId;

    private String adminId;

    private Integer auditState;

    private String cause;

    @TableField("is_anonymous")
    private Boolean isAnonymous;

    @TableField("visibility")
    private String visibility; // all=所有人, following=关注的人, mutual=互相关注, self=仅自己, custom=不给谁看

    @TableField("blocked_users")
    private String blockedUsers; // JSON数组，存储不给看的用户ID列表

    @TableField("mention_users")
    private String mentionUsers; // JSON数组，存储@提及的用户ID列表

    @TableField("view_num")
    private Integer viewNum;

    @TableField("like_num")
    private Integer likeNum;

    @TableField("collect_num")
    private Integer collectNum;

    @TableField("share_num")
    private Integer shareNum;

    @TableField("locked")
    private Integer locked; // 0正常 1锁定

    @TableField("lock_reason")
    private String lockReason; // 锁定原因

    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private String nickname;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
