package com.ab.kkmalladminweb.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Refund response.
 */
@Data
public class RefundResponse {

    private Long id;

    private String refundNo;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private String username;

    private String nickname;

    private BigDecimal refundAmount;

    private Integer refundType;

    private String refundTypeText;

    private String reason;

    private String description;

    private List<String> images;

    private Integer status;

    private String statusText;

    private String rejectReason;

    private List<RefundAuditLogResponse> auditLogs;

    private LocalDateTime refundTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
