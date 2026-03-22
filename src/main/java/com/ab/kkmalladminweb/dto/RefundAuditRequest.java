package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Refund audit request.
 */
@Data
public class RefundAuditRequest {

    private Boolean approved;

    @Size(max = 200, message = "rejectReason length must be <= 200")
    private String rejectReason;
}
