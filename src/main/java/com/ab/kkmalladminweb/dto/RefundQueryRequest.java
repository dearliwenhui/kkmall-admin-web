package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Refund list query request.
 */
@Data
public class RefundQueryRequest {

    @Min(value = 1, message = "pageNum must be >= 1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "pageSize must be >= 1")
    @Max(value = 200, message = "pageSize must be <= 200")
    private Integer pageSize = 10;

    private String refundNo;

    private String orderNo;

    private Integer status;

    private Integer refundType;

    private String keyword;
}
