package com.ab.kkmalladminweb.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Order item response.
 */
@Data
public class OrderItemResponse {

    private Long id;

    private Long productId;

    private String productName;

    private String productImage;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal totalAmount;
}
