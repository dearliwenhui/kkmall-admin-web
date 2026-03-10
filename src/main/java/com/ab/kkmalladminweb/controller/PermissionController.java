package com.ab.kkmalladminweb.controller;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.common.Result;
import com.ab.kkmalladminweb.dto.*;
import com.ab.kkmalladminweb.entity.SysPermission;
import com.ab.kkmalladminweb.mapper.SysPermissionMapper;
import com.ab.kkmalladminweb.service.PermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Permission controller.
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Validated
public class PermissionController {

    private final PermissionService permissionService;
    private final SysPermissionMapper sysPermissionMapper;

    /**
     * Get all permissions (for dropdown selection).
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<List<SysPermission>> getAllPermissions() {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysPermission::getId);
        List<SysPermission> permissions = sysPermissionMapper.selectList(wrapper);
        return Result.success(permissions);
    }

    /**
     * Query permissions with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('permission:manage')")
    public Result<PageResult<PermissionResponse>> list(@Valid PermissionQueryRequest request) {
        return Result.success(permissionService.list(request));
    }

    /**
     * Get permission by id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:manage')")
    public Result<PermissionResponse> getById(@PathVariable Long id) {
        return Result.success(permissionService.getById(id));
    }

    /**
     * Create permission.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('permission:add')")
    public Result<PermissionResponse> create(@Valid @RequestBody PermissionCreateRequest request) {
        return Result.success("创建成功", permissionService.create(request));
    }

    /**
     * Update permission.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:edit')")
    public Result<PermissionResponse> update(@PathVariable Long id,
                                             @Valid @RequestBody PermissionUpdateRequest request) {
        return Result.success("更新成功", permissionService.update(id, request));
    }

    /**
     * Delete permission.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * Batch delete permissions.
     */
    @PostMapping("/batch-delete")
    @PreAuthorize("hasAuthority('permission:delete')")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        permissionService.batchDelete(ids);
        return Result.success("批量删除成功");
    }
}
