package com.ab.kkmalladminweb.service.impl;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.*;
import com.ab.kkmalladminweb.entity.SysPermission;
import com.ab.kkmalladminweb.entity.SysRole;
import com.ab.kkmalladminweb.entity.SysRolePermission;
import com.ab.kkmalladminweb.exception.BusinessException;
import com.ab.kkmalladminweb.mapper.SysPermissionMapper;
import com.ab.kkmalladminweb.mapper.SysRoleMapper;
import com.ab.kkmalladminweb.mapper.SysRolePermissionMapper;
import com.ab.kkmalladminweb.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Role service implementation.
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public PageResult<RoleResponse> list(RoleQueryRequest queryRequest) {
        long pageNum = queryRequest.getPageNum() == null ? 1L : queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize() == null ? 10L : queryRequest.getPageSize();
        String roleName = StringUtils.hasText(queryRequest.getRoleName())
                ? queryRequest.getRoleName().trim()
                : null;
        String roleCode = StringUtils.hasText(queryRequest.getRoleCode())
                ? queryRequest.getRoleCode().trim()
                : null;

        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName)
                .like(StringUtils.hasText(roleCode), SysRole::getRoleCode, roleCode)
                .orderByDesc(SysRole::getUpdateTime)
                .orderByDesc(SysRole::getId);

        Page<SysRole> page = sysRoleMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        List<RoleResponse> records = page.getRecords().stream().map(this::toResponse).toList();

        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public RoleResponse getById(Long id) {
        SysRole role = requireRole(id);
        return toResponse(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleResponse create(RoleCreateRequest request) {
        // Check roleCode uniqueness
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, request.getRoleCode().trim());
        if (sysRoleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("角色代码已存在");
        }

        // Create role
        SysRole role = new SysRole();
        role.setRoleName(request.getRoleName().trim());
        role.setRoleCode(request.getRoleCode().trim());
        role.setDescription(StringUtils.hasText(request.getDescription())
                ? request.getDescription().trim()
                : null);
        sysRoleMapper.insert(role);

        // Assign permissions
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            assignPermissionsInternal(role.getId(), request.getPermissionIds());
        }

        return getById(role.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleResponse update(Long id, RoleUpdateRequest request) {
        SysRole role = requireRole(id);

        // Protect system roles
        if ("ADMIN".equals(role.getRoleCode())) {
            throw new BusinessException("不能修改系统角色");
        }

        // Update basic info
        role.setRoleName(request.getRoleName().trim());
        role.setDescription(StringUtils.hasText(request.getDescription())
                ? request.getDescription().trim()
                : null);
        sysRoleMapper.updateById(role);

        // Update permissions
        if (request.getPermissionIds() != null) {
            assignPermissionsInternal(id, request.getPermissionIds());
        }

        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysRole role = requireRole(id);

        // Protect system roles
        if ("ADMIN".equals(role.getRoleCode())) {
            throw new BusinessException("不能删除系统角色");
        }

        // Delete role
        sysRoleMapper.deleteById(id);

        // Delete permission associations
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, id);
        sysRolePermissionMapper.delete(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            delete(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long id, AssignPermissionsRequest request) {
        requireRole(id);
        assignPermissionsInternal(id, request.getPermissionIds());
    }

    private void assignPermissionsInternal(Long roleId, List<Long> permissionIds) {
        // Delete old permissions
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        sysRolePermissionMapper.delete(wrapper);

        // Insert new permissions
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                sysRolePermissionMapper.insert(rolePermission);
            }
        }
    }

    private SysRole requireRole(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在: " + id);
        }
        return role;
    }

    private List<RoleResponse.PermissionInfo> getRolePermissions(Long roleId) {
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        List<SysRolePermission> rolePermissions = sysRolePermissionMapper.selectList(wrapper);

        List<RoleResponse.PermissionInfo> permissionInfos = new ArrayList<>();
        for (SysRolePermission rolePermission : rolePermissions) {
            SysPermission permission = sysPermissionMapper.selectById(rolePermission.getPermissionId());
            if (permission != null) {
                RoleResponse.PermissionInfo permissionInfo = new RoleResponse.PermissionInfo();
                permissionInfo.setId(permission.getId());
                permissionInfo.setPermissionName(permission.getPermissionName());
                permissionInfo.setPermissionCode(permission.getPermissionCode());
                permissionInfo.setResourceType(permission.getResourceType());
                permissionInfo.setPath(permission.getPath());
                permissionInfos.add(permissionInfo);
            }
        }
        return permissionInfos;
    }

    private RoleResponse toResponse(SysRole role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setRoleName(role.getRoleName());
        response.setRoleCode(role.getRoleCode());
        response.setDescription(role.getDescription());
        response.setCreateTime(role.getCreateTime());
        response.setUpdateTime(role.getUpdateTime());
        response.setPermissions(getRolePermissions(role.getId()));
        return response;
    }
}
