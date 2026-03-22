package com.ab.kkmalladminweb.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Refund audit log response.
 */
@Data
public class RefundAuditLogResponse {

    private Long id;

    private String actionCode;

    private String actionText;

    private String operatorType;

    private String operatorTypeText;

    private Long operatorId;

    private String operatorName;

    private String operatorDisplayName;

    private String remark;

    private LocalDateTime createTime;
}
