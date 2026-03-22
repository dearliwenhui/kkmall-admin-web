package com.ab.kkmalladminweb.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order response.
 */
@Data
public class OrderResponse {

    private Long id;

    private String orderNo;

    private Long userId;

    private String username;

    private String nickname;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private Integer status;

    private String statusText;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String logisticsCompany;

    private String trackingNumber;

    private String remark;

    private Integer itemCount;

    private List<OrderItemResponse> items;

    private LocalDateTime payTime;

    private LocalDateTime shipTime;

    private LocalDateTime confirmTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
