package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * User create request.
 */
@Data
public class UserCreateRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50, message = "username length must be 3-50")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "username can only contain letters, numbers and underscores")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 20, message = "password length must be 6-20")
    private String password;

    @NotBlank(message = "nickname is required")
    @Size(max = 50, message = "nickname length must be <= 50")
    private String nickname;

    @Email(message = "invalid email format")
    @Size(max = 100, message = "email length must be <= 100")
    private String email;

    @Pattern(regexp = "^$|^1[0-9]{10}$", message = "invalid phone format")
    private String phone;

    @NotNull(message = "status is required")
    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;

    private List<Long> roleIds;
}
