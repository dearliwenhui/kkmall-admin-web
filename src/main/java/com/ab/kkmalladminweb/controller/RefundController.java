package com.ab.kkmalladminweb.controller;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.common.Result;
import com.ab.kkmalladminweb.dto.RefundAuditRequest;
import com.ab.kkmalladminweb.dto.RefundQueryRequest;
import com.ab.kkmalladminweb.dto.RefundResponse;
import com.ab.kkmalladminweb.dto.RefundStatisticsResponse;
import com.ab.kkmalladminweb.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Refund management controller.
 */
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Validated
public class RefundController {

    private final RefundService refundService;

    /**
     * Query refund list with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('order:manage')")
    public Result<PageResult<RefundResponse>> list(@Valid RefundQueryRequest queryRequest) {
        return Result.success(refundService.list(queryRequest));
    }

    /**
     * Get refund statistics.
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('order:manage')")
    public Result<RefundStatisticsResponse> statistics() {
        return Result.success(refundService.statistics());
    }

    /**
     * Get refund detail.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('order:manage')")
    public Result<RefundResponse> detail(@PathVariable Long id) {
        return Result.success(refundService.getById(id));
    }

    /**
     * Audit refund.
     */
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasAuthority('order:manage')")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody RefundAuditRequest request) {
        refundService.audit(id, request);
        return Result.success();
    }
}
