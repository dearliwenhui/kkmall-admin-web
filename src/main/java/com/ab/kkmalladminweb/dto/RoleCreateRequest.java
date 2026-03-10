package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Role create request.
 */
@Data
public class RoleCreateRequest {

    @NotBlank(message = "roleName is required")
    @Size(max = 50, message = "roleName length must be <= 50")
    private String roleName;

    @NotBlank(message = "roleCode is required")
    @Size(max = 50, message = "roleCode length must be <= 50")
    @Pattern(regexp = "^[A-Z_]+$", message = "roleCode must be uppercase letters and underscores only")
    private String roleCode;

    @Size(max = 200, message = "description length must be <= 200")
    private String description;

    private List<Long> permissionIds;
}