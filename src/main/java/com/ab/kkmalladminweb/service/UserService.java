package com.ab.kkmalladminweb.service;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.*;

import java.util.List;

/**
 * User service.
 */
public interface UserService {

    PageResult<UserResponse> list(UserQueryRequest queryRequest);

    UserResponse getById(Long id);

    UserResponse create(UserCreateRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void updateStatus(Long id, Integer status);

    void resetPassword(Long id, ResetPasswordRequest request);

    void assignRoles(Long id, AssignRolesRequest request);
}
