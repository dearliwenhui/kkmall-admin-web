package com.ab.kkmalladminweb.controller;

import com.ab.kkmalladminweb.common.Result;
import com.ab.kkmalladminweb.service.MinioStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common endpoints, including file upload.
 */
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final MinioStorageService minioStorageService;

    /**
     * Single file upload.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, String>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "type", required = false) String type
    ) {
        String url = minioStorageService.upload(file, type);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success(data);
    }

    /**
     * Batch file upload.
     */
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, List<String>>> uploadBatch(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(value = "type", required = false) String type
    ) {
        List<String> urls = minioStorageService.uploadBatch(files, type);
        Map<String, List<String>> data = new HashMap<>();
        data.put("urls", urls);
        return Result.success(data);
    }
}
