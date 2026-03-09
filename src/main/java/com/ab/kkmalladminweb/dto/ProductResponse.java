package com.ab.kkmalladminweb.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Product response payload.
 */
@Data
public class ProductResponse {

    private Long id;

    private String productName;

    private String productCode;

    private Long categoryId;

    private BigDecimal price;

    private Integer stock;

    private String description;

    private Integer status;

    private List<String> images;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
