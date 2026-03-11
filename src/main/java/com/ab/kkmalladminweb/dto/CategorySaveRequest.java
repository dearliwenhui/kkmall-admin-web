package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Category create/update request.
 */
@Data
public class CategorySaveRequest {

    @NotBlank(message = "name is required")
    @Size(max = 50, message = "name length must be <= 50")
    private String name;

    @NotNull(message = "parentId is required")
    @Min(value = 0, message = "parentId must be >= 0")
    private Long parentId;

    @NotNull(message = "level is required")
    @Min(value = 1, message = "level must be 1, 2, or 3")
    @Max(value = 3, message = "level must be 1, 2, or 3")
    private Integer level;

    @Min(value = 0, message = "sort must be >= 0")
    private Integer sort = 0;

    @Size(max = 255, message = "icon length must be <= 255")
    private String icon;
}
