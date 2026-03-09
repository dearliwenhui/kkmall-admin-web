package com.ab.kkmalladminweb.service;

import com.ab.kkmalladminweb.dto.LoginRequest;
import com.ab.kkmalladminweb.dto.LoginResponse;
import com.ab.kkmalladminweb.dto.UserInfo;

/**
 * 认证服务接口
 *
 * @author KKMall
 * @since 2026-02-01
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应（包含 Token 和用户信息）
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户登出
     *
     * @param token JWT Token
     */
    void logout(String token);

    /**
     * 获取当前登录用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserInfo getCurrentUserInfo(String username);

    /**
     * 刷新 Token
     *
     * @param oldToken 旧的 Token
     * @return 新的 Token
     */
    String refreshToken(String oldToken);
}
