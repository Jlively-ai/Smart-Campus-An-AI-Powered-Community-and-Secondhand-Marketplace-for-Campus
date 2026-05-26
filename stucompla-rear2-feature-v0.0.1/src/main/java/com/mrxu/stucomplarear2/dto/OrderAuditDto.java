package com.mrxu.stucomplarear2.dto;

import lombok.Data;

@Data
public class OrderAuditDto {
    private String orderId;
    private Integer auditState;
    private String cause;
}
