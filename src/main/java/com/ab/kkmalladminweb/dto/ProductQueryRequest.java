package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Product query params.
 */
@Data
public class ProductQueryRequest {

    @Min(value = 1, message = "pageNum must be >= 1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "pageSize must be >= 1")
    @Max(value = 200, message = "pageSize must be <= 200")
    private Long pageSize = 10L;

    private String productName;

    private Long categoryId;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
