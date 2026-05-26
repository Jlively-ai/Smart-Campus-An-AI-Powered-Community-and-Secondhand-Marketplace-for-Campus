package com.mrxu.stucomplarear2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("report")
public class Report {

    @TableId(type = IdType.ASSIGN_ID)
    private String reportId;

    private String reporterId;

    private String targetType; // post, goods, wall, comment, goods_comment

    private String targetId;

    private String reason;

    private Integer status; // 0=待处理, 1=已处理, 2=已驳回

    private String handlerId;

    private String handleResult;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
