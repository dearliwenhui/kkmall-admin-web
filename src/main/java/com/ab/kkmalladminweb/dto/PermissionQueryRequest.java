package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Permission query request.
 */
@Data
public class PermissionQueryRequest {

    @Min(value = 1, message = "pageNum must be >= 1")
    private Long pageNum;

    @Min(value = 1, message = "pageSize must be >= 1")
    @Max(value = 200, message = "pageSize must be <= 200")
    private Long pageSize;

    private String permissionName;

    private String permissionCode;

    private Integer resourceType;
}