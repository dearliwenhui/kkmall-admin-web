package com.ab.kkmalladminweb.service.impl;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.*;
import com.ab.kkmalladminweb.entity.SysRole;
import com.ab.kkmalladminweb.entity.SysUser;
import com.ab.kkmalladminweb.entity.SysUserRole;
import com.ab.kkmalladminweb.exception.BusinessException;
import com.ab.kkmalladminweb.mapper.SysRoleMapper;
import com.ab.kkmalladminweb.mapper.SysUserMapper;
import com.ab.kkmalladminweb.mapper.SysUserRoleMapper;
import com.ab.kkmalladminweb.security.LoginUser;
import com.ab.kkmalladminweb.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * User service implementation.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserResponse> list(UserQueryRequest queryRequest) {
        long pageNum = queryRequest.getPageNum() == null ? 1L : queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize() == null ? 10L : queryRequest.getPageSize();
        String username = StringUtils.hasText(queryRequest.getUsername())
                ? queryRequest.getUsername().trim()
                : null;
        String nickname = StringUtils.hasText(queryRequest.getNickname())
                ? queryRequest.getNickname().trim()
                : null;

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StringUtils.hasText(username), SysUser::getUsername, username)
                .like(StringUtils.hasText(nickname), SysUser::getNickname, nickname)
                .eq(queryRequest.getStatus() != null, SysUser::getStatus, queryRequest.getStatus())
                .orderByDesc(SysUser::getUpdateTime)
                .orderByDesc(SysUser::getId);

        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        List<UserResponse> records = page.getRecords().stream().map(this::toResponse).toList();

        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public UserResponse getById(Long id) {
        SysUser user = requireUser(id);
        return toResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse create(UserCreateRequest request) {
        // Check username uniqueness
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername().trim());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // Create user
        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname().trim());
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        user.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        user.setStatus(request.getStatus());
        sysUserMapper.insert(user);

        // Assign roles
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            assignRolesInternal(user.getId(), request.getRoleIds());
        }

        return getById(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse update(Long id, UserUpdateRequest request) {
        SysUser user = requireUser(id);

        // Update basic info
        user.setNickname(request.getNickname().trim());
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        user.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        user.setStatus(request.getStatus());
        sysUserMapper.updateById(user);

        // Update roles
        if (request.getRoleIds() != null) {
            assignRolesInternal(id, request.getRoleIds());
        }

        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysUser user = requireUser(id);

        // Protect current user
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new BusinessException("不能删除当前登录用户");
        }

        // Protect admin user
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除 admin 用户");
        }

        // Delete user (logical delete)
        sysUserMapper.deleteById(id);

        // Delete role associations
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, id);
        sysUserRoleMapper.delete(wrapper);
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
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("status must be 0 or 1");
        }

        SysUser user = requireUser(id);

        // Protect current user
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(id) && status == 0) {
            throw new BusinessException("不能禁用当前登录用户");
        }

        // Protect admin user
        if ("admin".equals(user.getUsername()) && status == 0) {
            throw new BusinessException("不能禁用 admin 用户");
        }

        user.setStatus(status);
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, ResetPasswordRequest request) {
        SysUser user = requireUser(id);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long id, AssignRolesRequest request) {
        requireUser(id);
        assignRolesInternal(id, request.getRoleIds());
    }

    private void assignRolesInternal(Long userId, List<Long> roleIds) {
        // Delete old roles
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        sysUserRoleMapper.delete(wrapper);

        // Insert new roles
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                sysUserRoleMapper.insert(userRole);
            }
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            return loginUser.getSysUser().getId();
        }
        return null;
    }

    private SysUser requireUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在: " + id);
        }
        return user;
    }

    private List<UserResponse.RoleInfo> getUserRoles(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(wrapper);

        List<UserResponse.RoleInfo> roleInfos = new ArrayList<>();
        for (SysUserRole userRole : userRoles) {
            SysRole role = sysRoleMapper.selectById(userRole.getRoleId());
            if (role != null) {
                UserResponse.RoleInfo roleInfo = new UserResponse.RoleInfo();
                roleInfo.setId(role.getId());
                roleInfo.setRoleName(role.getRoleName());
                roleInfo.setRoleCode(role.getRoleCode());
                roleInfos.add(roleInfo);
            }
        }
        return roleInfos;
    }

    private UserResponse toResponse(SysUser user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());
        response.setUpdateTime(user.getUpdateTime());
        response.setRoles(getUserRoles(user.getId()));
        return response;
    }
}
