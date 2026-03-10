package com.ab.kkmalladminweb.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Role response.
 */
@Data
public class RoleResponse {

    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<PermissionInfo> permissions;

    @Data
    public static class PermissionInfo {
        private Long id;
        private String permissionName;
        private String permissionCode;
        private Integer resourceType;
        private String path;
    }
}