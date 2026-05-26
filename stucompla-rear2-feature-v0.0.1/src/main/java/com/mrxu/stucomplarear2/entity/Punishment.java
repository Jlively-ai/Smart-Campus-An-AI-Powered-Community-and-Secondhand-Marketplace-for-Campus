package com.mrxu.stucomplarear2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("punishment")
public class Punishment {

    @TableId(type = IdType.ASSIGN_ID)
    private String punishmentId;

    private String userId;

    private String type; // mute, ban, warning

    private String reason;

    private Date startTime;

    private Date endTime;

    private Integer status; // 0=生效中, 1=已解除, 2=已过期

    private String handlerId;

    private String reportId;

    // 申诉相关字段
    // SQL: ALTER TABLE punishment ADD COLUMN appeal_reason VARCHAR(500) DEFAULT NULL COMMENT '申诉原因';
    // SQL: ALTER TABLE punishment ADD COLUMN appeal_state INT DEFAULT NULL COMMENT '申诉状态: 0=待审核, 1=已通过, 2=已驳回';
    // SQL: ALTER TABLE punishment ADD COLUMN appeal_time DATETIME DEFAULT NULL COMMENT '申诉时间';
    // SQL: ALTER TABLE punishment ADD COLUMN appeal_result VARCHAR(500) DEFAULT NULL COMMENT '申诉处理结果';
    private String appealReason; // 申诉原因

    private Integer appealState; // 申诉状态: 0=待审核, 1=已通过, 2=已驳回

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date appealTime; // 申诉时间

    private String appealResult; // 申诉处理结果

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
