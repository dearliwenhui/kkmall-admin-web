package com.ab.kkmalladminweb.service;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.*;

import java.util.List;

/**
 * Permission service.
 */
public interface PermissionService {

    /**
     * Query permissions with pagination.
     */
    PageResult<PermissionResponse> list(PermissionQueryRequest queryRequest);

    /**
     * Get permission by id.
     */
    PermissionResponse getById(Long id);

    /**
     * Create permission.
     */
    PermissionResponse create(PermissionCreateRequest request);

    /**
     * Update permission.
     */
    PermissionResponse update(Long id, PermissionUpdateRequest request);

    /**
     * Delete permission.
     */
    void delete(Long id);

    /**
     * Batch delete permissions.
     */
    void batchDelete(List<Long> ids);
}
