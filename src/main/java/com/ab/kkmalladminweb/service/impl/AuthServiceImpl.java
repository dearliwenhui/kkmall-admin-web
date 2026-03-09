package com.ab.kkmalladminweb.service.impl;

import com.ab.kkmalladminweb.dto.LoginRequest;
import com.ab.kkmalladminweb.dto.LoginResponse;
import com.ab.kkmalladminweb.dto.UserInfo;
import com.ab.kkmalladminweb.entity.SysUser;
import com.ab.kkmalladminweb.security.LoginUser;
import com.ab.kkmalladminweb.service.AuthService;
import com.ab.kkmalladminweb.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现类
 *
 * @author KKMall
 * @since 2026-02-01
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    // 最大失败次数
    private static final int MAX_FAIL_COUNT = 5;
    // 锁定时间 15 分钟
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private String lockKey(String username) {
        return "login:lock:" + username;
    }

    private String failKey(String username) {
        return "login:fail:" + username;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();

        // 检查账号是否被锁定
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey(username)))) {
            throw new LockedException("账号已被锁定，请 15 分钟后再试");
        }

        // 1. 使用 Spring Security 进行认证
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            // 累加失败次数
            Long failCount = redisTemplate.opsForValue().increment(failKey(username));
            if (failCount == 1) {
                // 第一次失败，设置过期时间
                redisTemplate.expire(failKey(username), LOCK_DURATION);
            }
            if (failCount != null && failCount >= MAX_FAIL_COUNT) {
                // 达到上限，锁定账号并清除计数
                redisTemplate.opsForValue().set(lockKey(username), "1", LOCK_DURATION);
                redisTemplate.delete(failKey(username));
                throw new LockedException("密码错误次数过多，账号已被锁定 15 分钟");
            }
            throw new BadCredentialsException("用户名或密码错误，还剩 " + (MAX_FAIL_COUNT - failCount) + " 次机会");
        } catch (AuthenticationException e) {
            throw e;
        }

        // 2. 设置认证信息到 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. 获取用户信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        SysUser sysUser = loginUser.getSysUser();

        // 登录成功，清除失败计数
        redisTemplate.delete(failKey(username));

        // 4. 生成 JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", sysUser.getId());
        claims.put("username", sysUser.getUsername());
        claims.put("roles", loginUser.getRoles());
        claims.put("permissions", loginUser.getPermissions());

        String token = jwtUtil.generateToken(sysUser.getUsername(), claims);

        // 5. 将 Token 存入 Redis（用于登出和验证）
        String redisKey = "token:" + sysUser.getUsername();
        redisTemplate.opsForValue().set(
                redisKey,
                token,
                Duration.ofMillis(jwtExpiration)
        );

        // 6. 构建用户信息
        UserInfo userInfo = buildUserInfo(loginUser);

        // 7. 返回登录响应
        return new LoginResponse(token, userInfo);
    }

    @Override
    public void logout(String token) {
        try {
            // 1. 从 Token 中获取用户名
            String username = jwtUtil.getUsernameFromToken(token);

            // 2. 删除 Redis 中的 Token
            String redisKey = "token:" + username;
            redisTemplate.delete(redisKey);

            // 3. 清除 SecurityContext
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            throw new RuntimeException("登出失败", e);
        }
    }

    @Override
    public UserInfo getCurrentUserInfo(String username) {
        // 1. 从 SecurityContext 中获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            throw new RuntimeException("未登录或登录已过期");
        }

        // 2. 获取用户信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        // 3. 构建并返回用户信息
        return buildUserInfo(loginUser);
    }

    @Override
    public String refreshToken(String oldToken) {
        // 1. 刷新 Token
        String newToken = jwtUtil.refreshToken(oldToken);

        // 2. 更新 Redis 中的 Token
        String username = jwtUtil.getUsernameFromToken(newToken);
        String redisKey = "token:" + username;
        redisTemplate.opsForValue().set(
                redisKey,
                newToken,
                Duration.ofMillis(jwtExpiration)
        );

        return newToken;
    }

    /**
     * 构建用户信息 DTO
     */
    private UserInfo buildUserInfo(LoginUser loginUser) {
        SysUser sysUser = loginUser.getSysUser();

        UserInfo userInfo = new UserInfo();
        userInfo.setId(sysUser.getId());
        userInfo.setUsername(sysUser.getUsername());
        userInfo.setNickname(sysUser.getNickname());
        userInfo.setAvatar(sysUser.getAvatar());
        userInfo.setEmail(sysUser.getEmail());
        userInfo.setPhone(sysUser.getPhone());
        userInfo.setRoles(loginUser.getRoles());
        userInfo.setPermissions(loginUser.getPermissions());

        return userInfo;
    }
}
