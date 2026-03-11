package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Category query params.
 */
@Data
public class CategoryQueryRequest {

    @Min(value = 1, message = "pageNum must be >= 1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "pageSize must be >= 1")
    @Max(value = 200, message = "pageSize must be <= 200")
    private Long pageSize = 10L;

    private String name;

    private Long parentId;

    @Min(value = 1, message = "level must be 1, 2, or 3")
    @Max(value = 3, message = "level must be 1, 2, or 3")
    private Integer level;
}
