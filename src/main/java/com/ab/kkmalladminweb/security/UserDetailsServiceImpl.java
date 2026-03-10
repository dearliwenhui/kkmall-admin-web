package com.ab.kkmalladminweb.security;

import com.ab.kkmalladminweb.entity.SysPermission;
import com.ab.kkmalladminweb.entity.SysRole;
import com.ab.kkmalladminweb.entity.SysUser;
import com.ab.kkmalladminweb.mapper.SysPermissionMapper;
import com.ab.kkmalladminweb.mapper.SysRoleMapper;
import com.ab.kkmalladminweb.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserDetailsService 实现类
 *
 * @author KKMall
 * @since 2026-02-01
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户信息
        SysUser sysUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );

        if (sysUser == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        // 查询用户的角色
        List<SysRole> userRoles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .inSql(SysRole::getId,
                               "SELECT role_id FROM sys_user_role WHERE user_id = " + sysUser.getId())
        );
        List<String> roles = userRoles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());

        // 查询用户的权限（通过角色）
        List<SysPermission> userPermissions = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .inSql(SysPermission::getId,
                               "SELECT permission_id FROM sys_role_permission WHERE role_id IN " +
                               "(SELECT role_id FROM sys_user_role WHERE user_id = " + sysUser.getId() + ")")
        );
        List<String> permissions = userPermissions.stream()
                .map(SysPermission::getPermissionCode)
                .distinct()
                .collect(Collectors.toList());

        // 封装成 UserDetails 对象
        return new LoginUser(sysUser, permissions, roles);
    }
}
