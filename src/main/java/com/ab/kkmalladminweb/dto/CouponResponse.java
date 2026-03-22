package com.ab.kkmalladminweb.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Coupon response.
 */
@Data
public class CouponResponse {

    private Long id;

    private String name;

    private Integer type;

    private String typeName;

    private BigDecimal discountAmount;

    private BigDecimal discountRate;

    private BigDecimal minAmount;

    private Integer totalCount;

    private Integer receivedCount;

    private Integer remainingCount;

    private Integer validDays;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String statusName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
