package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Cancel order request.
 */
@Data
public class OrderCancelRequest {

    @Size(max = 200, message = "reason length must be <= 200")
    private String reason;
}
