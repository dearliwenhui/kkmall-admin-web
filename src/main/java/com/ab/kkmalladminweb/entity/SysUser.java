package com.ab.kkmalladminweb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 *
 * @author KKMall
 * @since 2026-02-01
 */
@Data
@TableName("sys_user")
public class SysUser {

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * Logical delete flag: NULL for active records, timestamp (milliseconds) for deleted records.
     * This allows reusing username after deletion.
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
