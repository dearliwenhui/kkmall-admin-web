package com.ab.kkmalladminweb.controller;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.common.Result;
import com.ab.kkmalladminweb.dto.*;
import com.ab.kkmalladminweb.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * User management controller.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * Query user list with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('user:manage')")
    public Result<PageResult<UserResponse>> list(@Valid UserQueryRequest queryRequest) {
        return Result.success(userService.list(queryRequest));
    }

    /**
     * Get user detail by id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:manage')")
    public Result<UserResponse> detail(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * Create user.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('user:add')")
    public Result<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success("Created", userService.create(request));
    }

    /**
     * Update user.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return Result.success("Updated", userService.update(id, request));
    }

    /**
     * Delete user by id.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    /**
     * Batch delete users.
     */
    @PostMapping("/batch-delete")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<Void> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {
        userService.batchDelete(request.getIds());
        return Result.success();
    }

    /**
     * Enable user.
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<Void> enable(@PathVariable Long id) {
        userService.updateStatus(id, 1);
        return Result.success();
    }

    /**
     * Disable user.
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<Void> disable(@PathVariable Long id) {
        userService.updateStatus(id, 0);
        return Result.success();
    }

    /**
     * Reset user password.
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return Result.success();
    }

    /**
     * Assign roles to user.
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody AssignRolesRequest request) {
        userService.assignRoles(id, request);
        return Result.success();
    }
}
