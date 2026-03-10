package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Role update request.
 */
@Data
public class RoleUpdateRequest {

    @NotBlank(message = "roleName is required")
    @Size(max = 50, message = "roleName length must be <= 50")
    private String roleName;

    @Size(max = 200, message = "description length must be <= 200")
    private String description;

    private List<Long> permissionIds;
}