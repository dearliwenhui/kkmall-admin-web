package com.ab.kkmalladminweb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统角色实体
 *
 * @author KKMall
 * @since 2026-02-01
 */
@Data
@TableName("sys_role")
public class SysRole {

    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * Logical delete flag: NULL for active records, timestamp (milliseconds) for deleted records.
     * This allows reusing role_code after deletion.
     */
    @TableLogic(value = "NULL", delval = "UNIX_TIMESTAMP(NOW()) * 1000")
    private Long deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
