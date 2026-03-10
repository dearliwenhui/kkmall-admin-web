package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Assign roles request.
 */
@Data
public class AssignRolesRequest {

    @NotEmpty(message = "roleIds must not be empty")
    private List<Long> roleIds;
}
