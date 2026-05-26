package com.mrxu.stucomplarear2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

// ALTER TABLE violation_delete ADD COLUMN operation_type VARCHAR(20) DEFAULT 'delete' COMMENT '操作类型: delete删除 lock锁定';
@Data
@TableName("violation_delete")
public class ViolationDelete {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String userId;

    private String itemType; // post, goods, wall

    private String itemId;

    private String itemData; // JSON string of the original item

    private String reason; // reason for deletion

    private String handlerId; // admin who deleted

    private Integer appealState; // 0=none, 1=pending, 2=approved, 3=rejected

    private String appealReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date appealTime;

    private String appealResult;

    @TableField("operation_type")
    private String operationType; // 操作类型: delete删除 lock锁定

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
