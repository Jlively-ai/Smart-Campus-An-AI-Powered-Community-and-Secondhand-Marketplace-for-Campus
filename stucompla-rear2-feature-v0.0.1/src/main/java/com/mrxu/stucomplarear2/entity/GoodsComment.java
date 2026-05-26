package com.mrxu.stucomplarear2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("goods_comment")
public class GoodsComment {

    @TableId(type = IdType.ASSIGN_ID)
    private String commentId;

    private String goodsId;

    private String userId;

    private String content;

    private Integer rating;

    @TableField("mention_users")
    private String mentionUsers;

    @TableField("locked")
    private Integer locked; // 0正常 1锁定

    @TableField("lock_reason")
    private String lockReason; // 锁定原因

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
