package com.ab.kkmalladminweb.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Category response.
 */
@Data
public class CategoryResponse {

    private Long id;

    private String name;

    private Long parentId;

    private Integer level;

    private Integer sort;

    private String icon;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<CategoryResponse> children;
}
