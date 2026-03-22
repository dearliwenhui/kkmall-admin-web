package com.ab.kkmalladminweb.service;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.CouponQueryRequest;
import com.ab.kkmalladminweb.dto.CouponResponse;
import com.ab.kkmalladminweb.dto.CouponSaveRequest;

import java.util.List;

/**
 * Coupon service.
 */
public interface CouponService {

    PageResult<CouponResponse> list(CouponQueryRequest queryRequest);

    CouponResponse getById(Long id);

    CouponResponse create(CouponSaveRequest request);

    CouponResponse update(Long id, CouponSaveRequest request);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void updateStatus(Long id, Integer status);
}
