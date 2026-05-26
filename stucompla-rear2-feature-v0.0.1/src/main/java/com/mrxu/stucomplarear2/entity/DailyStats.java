package com.mrxu.stucomplarear2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("daily_stats")
public class DailyStats {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String itemType;  // post, wall, goods
    private String itemId;    // post_id, wall_id, goods_id
    private String userId;    // owner of the item
    private Integer viewNum;  // daily increment
    private Integer likeNum;
    private Integer collectNum;
    private Integer shareNum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date statDate;    // the date this record is for
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
