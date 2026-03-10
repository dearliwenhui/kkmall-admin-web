package com.ab.kkmalladminweb.controller;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.common.Result;
import com.ab.kkmalladminweb.dto.*;
import com.ab.kkmalladminweb.entity.SysRole;
import com.ab.kkmalladminweb.mapper.SysRoleMapper;
import com.ab.kkmalladminweb.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role management controller.
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Validated
public class RoleController {

    private final SysRoleMapper sysRoleMapper;
    private final RoleService roleService;

    /**
     * Get all roles (for dropdown selection).
     */
    @GetMapping("/all")
    public Result<List<SysRole>> getAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysRole::getId);
        List<SysRole> roles = sysRoleMapper.selectList(wrapper);
        return Result.success(roles);
    }

    /**
     * Query roles with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<PageResult<RoleResponse>> list(@Valid RoleQueryRequest request) {
        return Result.success(roleService.list(request));
    }

    /**
     * Get role by id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<RoleResponse> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    /**
     * Create role.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('role:add')")
    public Result<RoleResponse> create(@Valid @RequestBody RoleCreateRequest request) {
        return Result.success("创建成功", roleService.create(request));
    }

    /**
     * Update role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:edit')")
    public Result<RoleResponse> update(@PathVariable Long id,
                                       @Valid @RequestBody RoleUpdateRequest request) {
        return Result.success("更新成功", roleService.update(id, request));
    }

    /**
     * Delete role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * Batch delete roles.
     */
    @PostMapping("/batch-delete")
    @PreAuthorize("hasAuthority('role:delete')")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        roleService.batchDelete(ids);
        return Result.success("批量删除成功");
    }

    /**
     * Assign permissions to role.
     */
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:edit')")
    public Result<Void> assignPermissions(@PathVariable Long id,
                                          @Valid @RequestBody AssignPermissionsRequest request) {
        roleService.assignPermissions(id, request);
        return Result.success("分配权限成功");
    }
}
