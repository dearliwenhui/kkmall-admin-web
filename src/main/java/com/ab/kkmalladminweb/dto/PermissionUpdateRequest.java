package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Permission update request.
 */
@Data
public class PermissionUpdateRequest {

    @NotBlank(message = "permissionName is required")
    @Size(max = 50, message = "permissionName length must be <= 50")
    private String permissionName;

    @NotNull(message = "resourceType is required")
    private Integer resourceType;

    @Size(max = 200, message = "path length must be <= 200")
    private String path;

    @Size(max = 200, message = "description length must be <= 200")
    private String description;
}