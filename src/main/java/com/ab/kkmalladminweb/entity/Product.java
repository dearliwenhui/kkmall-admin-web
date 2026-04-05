package com.ab.kkmalladminweb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity.
 */
@Data
@TableName("mall_product")
public class Product {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String productName;

    private String productCode;

    private Long categoryId;

    private BigDecimal price;

    private Integer stock;

    private String description;

    /**
     * 1: on sale, 0: off sale.
     */
    private Integer status;

    /**
     * Comma-separated image URLs.
     */
    private String images;

    @Version
    private Long version;

    /**
     * Logical delete flag: NULL for active records, timestamp (milliseconds) for deleted records.
     * This allows reusing product_code after deletion.
     */
    @TableLogic(value = "NULL", delval = "UNIX_TIMESTAMP(NOW()) * 1000")
    private Long deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
