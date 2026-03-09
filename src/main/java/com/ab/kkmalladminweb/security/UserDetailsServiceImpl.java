package com.ab.kkmalladminweb.security;

import com.ab.kkmalladminweb.entity.SysUser;
import com.ab.kkmalladminweb.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

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

        // 查询用户的角色和权限（暂时返回空列表，后续从数据库查询）
        // TODO: 从 sys_user_role 和 sys_role_permission 表中查询
        List<String> roles = List.of("ROLE_ADMIN");  // 示例：默认为管理员
        List<String> permissions = List.of(
                "product:manage",
                "order:manage",
                "user:manage",
                "product:add",
                "product:delete",
                "order:deliver"
        );

        // 封装成 UserDetails 对象
        return new LoginUser(sysUser, permissions, roles);
    }
}
