package com.ab.kkmalladminweb.controller;

import com.ab.kkmalladminweb.common.Result;
import com.ab.kkmalladminweb.entity.SysPermission;
import com.ab.kkmalladminweb.mapper.SysPermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Async route controller for frontend menu navigation.
 */
@RestController
@RequiredArgsConstructor
public class RouteController {

    private final SysPermissionMapper sysPermissionMapper;

    /**
     * Get async routes for frontend navigation.
     * This endpoint provides the menu structure for the frontend based on user permissions.
     */
    @GetMapping("/api/get-async-routes")
    public Result<Object> getAsyncRoutes() {
        // Get all permissions from the database
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPermission::getResourceType, 1); // 只获取菜单类型权限
        List<SysPermission> permissions = sysPermissionMapper.selectList(queryWrapper);

        // Create a simple menu structure for the frontend
        List<Map<String, Object>> menu = new ArrayList<>();

        // Add a default dashboard menu item
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("path", "/kkmall/dashboard");
        dashboard.put("name", "Dashboard");
        Map<String, Object> meta = new HashMap<>();
        meta.put("title", "仪表板");
        meta.put("icon", "el-icon-house");
        dashboard.put("meta", meta);
        menu.add(dashboard);

        // Add other menu items based on permissions
        for (SysPermission permission : permissions) {
            if (permission.getPath() != null && !permission.getPath().isEmpty()) {
                Map<String, Object> item = new HashMap<>();
                item.put("path", permission.getPath());
                item.put("name", permission.getPermissionCode().replace(":", "-"));
                Map<String, Object> itemMeta = new HashMap<>();
                itemMeta.put("title", permission.getDescription() != null ? permission.getDescription() : permission.getPermissionName());
                itemMeta.put("icon", "el-icon-menu");
                item.put("meta", itemMeta);
                menu.add(item);
            }
        }

        return Result.success(menu);
    }
}