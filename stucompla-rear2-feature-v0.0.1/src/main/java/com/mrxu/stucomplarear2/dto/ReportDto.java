package com.mrxu.stucomplarear2.dto;

import lombok.Data;

@Data
public class ReportDto {
    private String targetType; // post, goods, wall, comment, goods_comment
    private String targetId;
    private String reason;
}
