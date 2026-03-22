package com.ab.kkmalladminweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Deliver order request.
 */
@Data
public class OrderDeliverRequest {

    @NotBlank(message = "expressCompany is required")
    @Size(max = 50, message = "expressCompany length must be <= 50")
    private String expressCompany;

    @NotBlank(message = "expressNo is required")
    @Size(max = 100, message = "expressNo length must be <= 100")
    private String expressNo;
}
