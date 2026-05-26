package com.mrxu.stucomplarear2.dto;

import lombok.Data;

@Data
public class PunishmentDto {
    private String userId;
    private String type; // mute, ban, warning
    private String reason;
    private String endTime; // 解除时间
    private String reportId; // 关联举报ID（可选）
}
