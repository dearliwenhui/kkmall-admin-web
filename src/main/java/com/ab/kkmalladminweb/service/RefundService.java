package com.ab.kkmalladminweb.service;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.RefundAuditRequest;
import com.ab.kkmalladminweb.dto.RefundQueryRequest;
import com.ab.kkmalladminweb.dto.RefundResponse;

/**
 * Refund management service.
 */
public interface RefundService {

    PageResult<RefundResponse> list(RefundQueryRequest queryRequest);

    RefundResponse getById(Long id);

    void audit(Long id, RefundAuditRequest request);
}
