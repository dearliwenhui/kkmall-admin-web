package com.ab.kkmalladminweb.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * MinIO config bound from kkmall.admin.minio.
 */
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "kkmall.admin.minio")
public class MinioProperties {

    @NotBlank
    private String endpoint;

    @NotBlank
    private String accessKey;

    @NotBlank
    private String secretKey;

    @NotBlank
    private String bucket;

    /**
     * public | private
     */
    private String policy = "public";

    private String publicBaseUrl;

    @Min(1)
    private Integer maxSizeMb = 5;

    /**
     * Allowed file extensions, for example: jpg,png,webp.
     */
    private List<String> allowTypes;

    private String objectPrefix = "product/";
}
