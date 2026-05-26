package com.mrxu.stucomplarear2.dto;

import lombok.Data;

@Data
public class PostAuditDto {
    /**
     * 帖子ID
     */
    private String postId;

    /**
     * 审核状态：0-待审 1-审核通过 2-未通过
     */
    private Integer auditState;

    /**
     * 审核不过原因
     */
    private String auditFailedCause;
}
