package com.ab.kkmalladminweb.security;

import com.ab.kkmalladminweb.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 访问拒绝处理器
 * 处理权限不足的情况，返回 403 禁止访问响应
 *
 * @author KKMall
 * @since 2026-02-01
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 设置响应状态码
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // 设置响应内容类型
        response.setContentType("application/json;charset=UTF-8");

        // 构建错误响应
        Result<String> result = Result.error(403, "权限不足，无法访问");
        result.setData(accessDeniedException.getMessage());

        // 将结果转换为 JSON 并写入响应
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
