package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Assign permissions request.
 */
@Data
public class AssignPermissionsRequest {

    @NotNull(message = "permissionIds is required")
    private List<Long> permissionIds;
}