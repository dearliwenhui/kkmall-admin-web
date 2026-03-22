package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Coupon query params.
 */
@Data
public class CouponQueryRequest {

    @Min(value = 1, message = "pageNum must be >= 1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "pageSize must be >= 1")
    @Max(value = 200, message = "pageSize must be <= 200")
    private Long pageSize = 10L;

    private String name;

    @Min(value = 1, message = "type must be 1 or 2")
    @Max(value = 2, message = "type must be 1 or 2")
    private Integer type;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
