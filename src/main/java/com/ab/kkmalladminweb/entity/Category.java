package com.ab.kkmalladminweb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Category entity.
 */
@Data
@TableName("mall_category")
public class Category {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private Long parentId;

    private Integer level;

    private Integer sort;

    private String icon;

    @Version
    private Long version;

    /**
     * Logical delete flag: NULL for active records, timestamp (milliseconds) for deleted records.
     */
    @TableLogic(value = "NULL", delval = "UNIX_TIMESTAMP(NOW()) * 1000")
    private Long deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * Children categories (not a database field).
     */
    @TableField(exist = false)
    private List<Category> children;
}
