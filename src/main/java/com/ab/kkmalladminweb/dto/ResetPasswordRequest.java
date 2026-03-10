package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Reset password request.
 */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "newPassword is required")
    @Size(min = 6, max = 20, message = "newPassword length must be 6-20")
    private String newPassword;
}
