package com.ab.kkmalladminweb.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Refund statistics response.
 */
@Data
public class RefundStatisticsResponse {

    private long totalRefunds;

    private long pendingRefunds;

    private long rejectedRefunds;

    private long refundedCount;

    private BigDecimal totalRefundAmount = BigDecimal.ZERO;

    private BigDecimal refundedAmount = BigDecimal.ZERO;

    private BigDecimal pendingAmount = BigDecimal.ZERO;
}
