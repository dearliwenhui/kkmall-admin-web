package com.ab.kkmalladminweb.security;

import com.ab.kkmalladminweb.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 *
 * @author KKMall
 * @since 2026-02-01
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.header:Authorization}")
    private String tokenHeader;

    @Value("${jwt.prefix:Bearer}")
    private String tokenPrefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求头中获取 Token
        String authHeader = request.getHeader(tokenHeader);

        // 验证 Token 格式
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(tokenPrefix + " ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 提取 Token
        String token = authHeader.substring(tokenPrefix.length() + 1);

        try {
            // 验证 Token 有效性
            if (!jwtUtil.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 从 Token 中获取用户名
            String username = jwtUtil.getUsernameFromToken(token);

            // 检查 Redis 中是否存在该 Token（用于登出功能）
            String redisKey = "token:" + username;
            Object cachedToken = redisTemplate.opsForValue().get(redisKey);
            if (cachedToken == null || !cachedToken.equals(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 如果 SecurityContext 中已有认证信息，直接跳过
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 从数据库加载用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 创建认证对象
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            // 设置认证详情
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // 将认证对象存入 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            logger.error("JWT 认证失败", e);
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}
