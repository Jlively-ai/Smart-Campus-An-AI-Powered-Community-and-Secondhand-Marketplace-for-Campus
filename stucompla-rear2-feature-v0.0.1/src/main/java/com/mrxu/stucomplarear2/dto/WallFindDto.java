package com.mrxu.stucomplarear2.dto;

import lombok.Data;

@Data
public class WallFindDto {
    /**
     * 墙ID
     */
    private String wallId;

    /**
     * 审核人ID
     */
    private String adminId;

    /**
     * 审核状态：0-待审 1-审核通过 2-未通过
     */
    private Integer auditState;

    /**
     * 申请人ID
     */
    private String userId;

    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 页面大小
     */
    private Integer pageSize;

    /**
     * 墙内容搜索
     */
    private String wallContent;

    /**
     * 作者昵称（模糊搜索）
     */
    private String nickname;

    private String sort;
}
