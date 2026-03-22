package com.ab.kkmalladminweb.controller;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.common.Result;
import com.ab.kkmalladminweb.dto.BatchDeleteRequest;
import com.ab.kkmalladminweb.dto.CouponQueryRequest;
import com.ab.kkmalladminweb.dto.CouponResponse;
import com.ab.kkmalladminweb.dto.CouponSaveRequest;
import com.ab.kkmalladminweb.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Coupon management controller.
 */
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Validated
public class CouponController {

    private final CouponService couponService;

    /**
     * Query coupon list with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('coupon:manage')")
    public Result<PageResult<CouponResponse>> list(@Valid CouponQueryRequest queryRequest) {
        return Result.success(couponService.list(queryRequest));
    }

    /**
     * Get coupon detail by id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('coupon:manage')")
    public Result<CouponResponse> detail(@PathVariable Long id) {
        return Result.success(couponService.getById(id));
    }

    /**
     * Create coupon.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('coupon:add')")
    public Result<CouponResponse> create(@Valid @RequestBody CouponSaveRequest request) {
        return Result.success("Created", couponService.create(request));
    }

    /**
     * Update coupon.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('coupon:edit')")
    public Result<CouponResponse> update(@PathVariable Long id, @Valid @RequestBody CouponSaveRequest request) {
        return Result.success("Updated", couponService.update(id, request));
    }

    /**
     * Delete coupon.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('coupon:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return Result.success();
    }

    /**
     * Batch delete coupons.
     */
    @PostMapping("/batch-delete")
    @PreAuthorize("hasAuthority('coupon:delete')")
    public Result<Void> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {
        couponService.batchDelete(request.getIds());
        return Result.success();
    }

    /**
     * Enable coupon.
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('coupon:edit')")
    public Result<Void> enable(@PathVariable Long id) {
        couponService.updateStatus(id, 1);
        return Result.success();
    }

    /**
     * Disable coupon.
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('coupon:edit')")
    public Result<Void> disable(@PathVariable Long id) {
        couponService.updateStatus(id, 0);
        return Result.success();
    }
}
