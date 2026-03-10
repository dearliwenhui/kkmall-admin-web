package com.ab.kkmalladminweb.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User response.
 */
@Data
public class UserResponse {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<RoleInfo> roles;

    @Data
    public static class RoleInfo {
        private Long id;
        private String roleName;
        private String roleCode;
    }
}
