package com.ab.kkmalladminweb.controller;

import com.ab.kkmalladminweb.common.Result;
import com.ab.kkmalladminweb.dto.LoginRequest;
import com.ab.kkmalladminweb.dto.LoginResponse;
import com.ab.kkmalladminweb.dto.UserInfo;
import com.ab.kkmalladminweb.service.AuthService;
import com.ab.kkmalladminweb.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author KKMall
 * @since 2026-02-01
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return Result.success("登录成功", loginResponse);
    }

    /**
     * 用户登出
     *
     * @param request HTTP 请求
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        // 从请求头中获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return Result.success();
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request HTTP 请求
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserInfo> getCurrentUserInfo(HttpServletRequest request) {
        // 从请求头中获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);

        UserInfo userInfo = authService.getCurrentUserInfo(username);
        return Result.success(userInfo);
    }

    /**
     * 刷新 Token
     *
     * @param request HTTP 请求
     * @return 新的 Token
     */
    @PostMapping("/refresh")
    public Result<String> refreshToken(HttpServletRequest request) {
        // 从请求头中获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(401, "Token 无效");
        }

        String oldToken = authHeader.substring(7);
        String newToken = authService.refreshToken(oldToken);

        return Result.success("Token 刷新成功", newToken);
    }
}
