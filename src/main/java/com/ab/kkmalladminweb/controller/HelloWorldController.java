package com.ab.kkmalladminweb.controller;

import com.ab.kkmalladminweb.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Hello World 接口控制器
 */
@RestController
@RequestMapping("/api")
public class HelloWorldController {

    /**
     * 基础 Hello World 接口
     *
     * @return Hello World 消息
     */
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Hello World!你好世界欢迎你来到世界上");
    }

    /**
     * 带参数的 Hello World 接口
     *
     * @param name 名称参数
     * @return 个性化问候消息
     */
    @GetMapping("/hello/greet")
    public Result<Map<String, Object>> greet(@RequestParam(defaultValue = "World") String name) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello, " + name + "!");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("status", "success");

        return Result.success("问候成功", response);
    }

    /**
     * 获取系统信息接口
     *
     * @return 系统信息
     */
    @GetMapping("/hello/info")
    public Result<Map<String, Object>> info() {
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("application", "KKMall Admin Web");
        systemInfo.put("version", "0.0.1-SNAPSHOT");
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osArch", System.getProperty("os.arch"));
        systemInfo.put("serverTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return Result.success(systemInfo);
    }
}
