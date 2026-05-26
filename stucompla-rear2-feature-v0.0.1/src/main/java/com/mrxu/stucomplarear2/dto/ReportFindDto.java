package com.mrxu.stucomplarear2.dto;

import lombok.Data;

@Data
public class ReportFindDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String targetType;
    private Integer status;
    private String keyword; // 搜索关键词
}
