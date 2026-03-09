package com.ab.kkmalladminweb.service;

import com.ab.kkmalladminweb.config.MinioProperties;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

/**
 * MinIO object storage service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioProperties properties;
    private volatile boolean bucketChecked = false;

    private MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    public String upload(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        validateFile(file);
        ensureBucket();

        String objectName = buildObjectName(file, type);
        String contentType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : "application/octet-stream";

        try (InputStream inputStream = file.getInputStream()) {
            minioClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
            return buildAccessUrl(objectName);
        } catch (Exception e) {
            log.error("Failed to upload object to MinIO, object={}", objectName, e);
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }

    public List<String> uploadBatch(List<MultipartFile> files, String type) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> urls = new ArrayList<>(files.size());
        for (MultipartFile file : files) {
            urls.add(upload(file, type));
        }
        return urls;
    }

    private void validateFile(MultipartFile file) {
        long maxBytes = (long) properties.getMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new RuntimeException("File is too large, max " + properties.getMaxSizeMb() + "MB");
        }

        List<String> allowTypes = properties.getAllowTypes();
        if (allowTypes == null || allowTypes.isEmpty()) {
            return;
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(extension)) {
            throw new RuntimeException("File extension is required");
        }

        Set<String> allowed = allowTypes.stream()
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        if (!allowed.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new RuntimeException("File type not allowed: " + extension);
        }
    }

    private synchronized void ensureBucket() {
        if (bucketChecked) {
            return;
        }
        try {
            MinioClient client = minioClient();
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.getBucket()).build()
            );
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
            }
            if ("public".equalsIgnoreCase(properties.getPolicy())) {
                String policy = """
                        {
                          "Version": "2012-10-17",
                          "Statement": [
                            {
                              "Effect": "Allow",
                              "Principal": {"AWS": ["*"]},
                              "Action": ["s3:GetObject"],
                              "Resource": ["arn:aws:s3:::%s/*"]
                            }
                          ]
                        }
                        """.formatted(properties.getBucket());
                client.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(properties.getBucket())
                                .config(policy)
                                .build()
                );
            }
            bucketChecked = true;
        } catch (Exception e) {
            log.error("Failed to initialize bucket {}", properties.getBucket(), e);
            throw new RuntimeException("MinIO bucket init failed: " + e.getMessage(), e);
        }
    }

    private String buildObjectName(MultipartFile file, String type) {
        String extension = getExtension(file.getOriginalFilename());
        String safeType = normalizeType(type);
        LocalDate now = LocalDate.now();
        String datePath = "%d/%02d/%02d".formatted(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String prefix = normalizePrefix(properties.getObjectPrefix());
        String random = UUID.randomUUID().toString().replace("-", "");
        String fileName = StringUtils.hasText(extension) ? random + "." + extension : random;

        if (StringUtils.hasText(safeType)) {
            return prefix + safeType + "/" + datePath + "/" + fileName;
        }
        return prefix + datePath + "/" + fileName;
    }

    private String buildAccessUrl(String objectName) throws Exception {
        if ("public".equalsIgnoreCase(properties.getPolicy())) {
            String baseUrl = StringUtils.hasText(properties.getPublicBaseUrl())
                    ? properties.getPublicBaseUrl().trim()
                    : properties.getEndpoint().trim();
            baseUrl = trimTrailingSlash(baseUrl);
            return baseUrl + "/" + properties.getBucket() + "/" + objectName;
        }
        return minioClient().getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(properties.getBucket())
                        .object(objectName)
                        .expiry(24 * 60 * 60)
                        .build()
        );
    }

    private String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        String result = prefix.trim().replace("\\", "/");
        if (result.startsWith("/")) {
            result = result.substring(1);
        }
        if (!result.endsWith("/")) {
            result += "/";
        }
        return result;
    }

    private String normalizeType(String type) {
        if (!StringUtils.hasText(type)) {
            return "";
        }
        return type.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/_-]", "");
    }

    private String getExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1);
    }

    private String trimTrailingSlash(String input) {
        String value = input;
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
