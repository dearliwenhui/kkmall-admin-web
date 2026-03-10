package com.ab.kkmalladminweb.service.impl;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.*;
import com.ab.kkmalladminweb.entity.SysPermission;
import com.ab.kkmalladminweb.entity.SysRolePermission;
import com.ab.kkmalladminweb.exception.BusinessException;
import com.ab.kkmalladminweb.mapper.SysPermissionMapper;
import com.ab.kkmalladminweb.mapper.SysRolePermissionMapper;
import com.ab.kkmalladminweb.service.PermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Permission service implementation.
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public PageResult<PermissionResponse> list(PermissionQueryRequest queryRequest) {
        long pageNum = queryRequest.getPageNum() == null ? 1L : queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize() == null ? 10L : queryRequest.getPageSize();
        String permissionName = StringUtils.hasText(queryRequest.getPermissionName())
                ? queryRequest.getPermissionName().trim()
                : null;
        String permissionCode = StringUtils.hasText(queryRequest.getPermissionCode())
                ? queryRequest.getPermissionCode().trim()
                : null;

        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StringUtils.hasText(permissionName), SysPermission::getPermissionName, permissionName)
                .like(StringUtils.hasText(permissionCode), SysPermission::getPermissionCode, permissionCode)
                .eq(queryRequest.getResourceType() != null, SysPermission::getResourceType, queryRequest.getResourceType())
                .orderByDesc(SysPermission::getUpdateTime)
                .orderByDesc(SysPermission::getId);

        Page<SysPermission> page = sysPermissionMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        List<PermissionResponse> records = page.getRecords().stream().map(this::toResponse).toList();

        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public PermissionResponse getById(Long id) {
        SysPermission permission = requirePermission(id);
        return toResponse(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionResponse create(PermissionCreateRequest request) {
        // Check permissionCode uniqueness
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getPermissionCode, request.getPermissionCode().trim());
        if (sysPermissionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("权限代码已存在");
        }

        // Create permission
        SysPermission permission = new SysPermission();
        permission.setPermissionName(request.getPermissionName().trim());
        permission.setPermissionCode(request.getPermissionCode().trim());
        permission.setResourceType(request.getResourceType());
        permission.setPath(StringUtils.hasText(request.getPath())
                ? request.getPath().trim()
                : null);
        permission.setDescription(StringUtils.hasText(request.getDescription())
                ? request.getDescription().trim()
                : null);
        sysPermissionMapper.insert(permission);

        return getById(permission.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionResponse update(Long id, PermissionUpdateRequest request) {
        SysPermission permission = requirePermission(id);

        // Update basic info
        permission.setPermissionName(request.getPermissionName().trim());
        permission.setResourceType(request.getResourceType());
        permission.setPath(StringUtils.hasText(request.getPath())
                ? request.getPath().trim()
                : null);
        permission.setDescription(StringUtils.hasText(request.getDescription())
                ? request.getDescription().trim()
                : null);
        sysPermissionMapper.updateById(permission);

        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requirePermission(id);

        // Delete permission
        sysPermissionMapper.deleteById(id);

        // Delete role associations
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getPermissionId, id);
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

    private SysPermission requirePermission(Long id) {
        SysPermission permission = sysPermissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException("权限不存在: " + id);
        }
        return permission;
    }

    private PermissionResponse toResponse(SysPermission permission) {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setPermissionName(permission.getPermissionName());
        response.setPermissionCode(permission.getPermissionCode());
        response.setResourceType(permission.getResourceType());
        response.setPath(permission.getPath());
        response.setDescription(permission.getDescription());
        response.setCreateTime(permission.getCreateTime());
        response.setUpdateTime(permission.getUpdateTime());
        return response;
    }
}
