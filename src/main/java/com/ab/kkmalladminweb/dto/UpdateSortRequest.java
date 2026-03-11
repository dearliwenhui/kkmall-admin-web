package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Update category sort request.
 */
@Data
public class UpdateSortRequest {

    @NotNull(message = "sort is required")
    @Min(value = 0, message = "sort must be >= 0")
    private Integer sort;
}
