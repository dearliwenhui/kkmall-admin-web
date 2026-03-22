package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Coupon create/update request.
 */
@Data
public class CouponSaveRequest {

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name length must be <= 100")
    private String name;

    @NotNull(message = "type is required")
    @Min(value = 1, message = "type must be 1 or 2")
    @Max(value = 2, message = "type must be 1 or 2")
    private Integer type;

    private BigDecimal discountAmount;

    private BigDecimal discountRate;

    private BigDecimal minAmount;

    @NotNull(message = "totalCount is required")
    @Min(value = 1, message = "totalCount must be >= 1")
    private Integer totalCount;

    @NotNull(message = "validDays is required")
    @Min(value = 1, message = "validDays must be >= 1")
    private Integer validDays;

    @NotBlank(message = "startTime is required")
    private String startTime;

    @NotBlank(message = "endTime is required")
    private String endTime;

    @NotNull(message = "status is required")
    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
