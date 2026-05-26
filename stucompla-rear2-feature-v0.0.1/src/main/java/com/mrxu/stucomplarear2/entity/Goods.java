package com.mrxu.stucomplarear2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

// ALTER TABLE goods ADD COLUMN audit_state INT DEFAULT 0 COMMENT '审核状态 0待审核 1通过 2拒绝';
@Data
@TableName("goods")
public class Goods {

    @TableId(type = IdType.ASSIGN_ID)
    private String goodsId;

    private String goodsName;

    private String goodsDetail;

    private String goodsImages;

    private Double goodsPrice;

    private Integer goodsCategoryId;

    private Integer goodsCount;

    private Boolean goodsStatus;

    @TableField("audit_state")
    private Integer auditState; // 审核状态 0待审核 1通过 2拒绝

    @TableField("locked")
    private Integer locked; // 0正常 1锁定

    @TableField("lock_reason")
    private String lockReason; // 锁定原因

    private String userId;

    private Integer viewNum;

    @TableField("share_num")
    private Integer shareNum = 0;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
