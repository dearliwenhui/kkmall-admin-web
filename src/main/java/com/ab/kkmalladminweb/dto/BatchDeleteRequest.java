package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Batch delete request payload.
 */
@Data
public class BatchDeleteRequest {

    @NotEmpty(message = "ids must not be empty")
    private List<Long> ids;
}
