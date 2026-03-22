package com.ab.kkmalladminweb.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Order statistics response.
 */
@Data
public class OrderStatisticsResponse {

    private long totalOrders;

    private long pendingPaymentOrders;

    private long pendingShipmentOrders;

    private long pendingReceiptOrders;

    private long completedOrders;

    private long cancelledOrders;

    private long todayOrders;

    private BigDecimal totalSales = BigDecimal.ZERO;

    private BigDecimal todaySales = BigDecimal.ZERO;
}
