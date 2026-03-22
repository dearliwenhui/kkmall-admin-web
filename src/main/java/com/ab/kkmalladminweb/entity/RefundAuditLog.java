package com.ab.kkmalladminweb.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Refund audit log entity.
 */
@Data
@TableName("mall_refund_audit_log")
public class RefundAuditLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long refundId;

    private String refundNo;

    private String actionCode;

    private String operatorType;

    private Long operatorId;

    private String operatorName;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
