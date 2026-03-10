package com.ab.kkmalladminweb.service;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.*;

import java.util.List;

/**
 * Role service.
 */
public interface RoleService {

    /**
     * Query roles with pagination.
     */
    PageResult<RoleResponse> list(RoleQueryRequest queryRequest);

    /**
     * Get role by id.
     */
    RoleResponse getById(Long id);

    /**
     * Create role.
     */
    RoleResponse create(RoleCreateRequest request);

    /**
     * Update role.
     */
    RoleResponse update(Long id, RoleUpdateRequest request);

    /**
     * Delete role.
     */
    void delete(Long id);

    /**
     * Batch delete roles.
     */
    void batchDelete(List<Long> ids);

    /**
     * Assign permissions to role.
     */
    void assignPermissions(Long id, AssignPermissionsRequest request);
}