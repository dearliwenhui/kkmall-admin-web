package com.ab.kkmalladminweb.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Permission response.
 */
@Data
public class PermissionResponse {

    private Long id;
    private String permissionName;
    private String permissionCode;
    private Integer resourceType;
    private String path;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}