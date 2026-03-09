package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product create/update request.
 */
@Data
public class ProductSaveRequest {

    @NotBlank(message = "productName is required")
    @Size(max = 100, message = "productName length must be <= 100")
    private String productName;

    @NotBlank(message = "productCode is required")
    @Size(max = 64, message = "productCode length must be <= 64")
    private String productCode;

    @NotNull(message = "categoryId is required")
    @Min(value = 1, message = "categoryId must be >= 1")
    private Long categoryId;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0", inclusive = false, message = "price must be > 0")
    private BigDecimal price;

    @NotNull(message = "stock is required")
    @Min(value = 0, message = "stock must be >= 0")
    private Integer stock;

    @Size(max = 50000, message = "description length must be <= 50000")
    private String description;

    @NotNull(message = "status is required")
    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;

    private List<String> images;
}
