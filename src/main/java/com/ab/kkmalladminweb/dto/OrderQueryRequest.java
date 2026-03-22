package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Order list query request.
 */
@Data
public class OrderQueryRequest {

    @Min(value = 1, message = "pageNum must be >= 1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "pageSize must be >= 1")
    @Max(value = 200, message = "pageSize must be <= 200")
    private Integer pageSize = 10;

    private String orderNo;

    private Integer status;

    private String startTime;

    private String endTime;

    private String keyword;
}
