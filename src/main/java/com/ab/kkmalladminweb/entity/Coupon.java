package com.ab.kkmalladminweb.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Coupon entity.
 */
@Data
@TableName("mall_coupon")
public class Coupon {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 1: full reduction, 2: discount.
     */
    private Integer type;

    private BigDecimal discountAmount;

    /**
     * Discount value aligned with mall-api usage.
     * Example: 8 means 20% off, 9.5 means 5% off.
     */
    private BigDecimal discountRate;

    private BigDecimal minAmount;

    private Integer totalCount;

    private Integer receivedCount;

    private Integer validDays;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 1: enabled, 0: disabled.
     */
    private Integer status;

    @Version
    private Long version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
