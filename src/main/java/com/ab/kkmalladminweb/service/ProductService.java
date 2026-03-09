package com.ab.kkmalladminweb.service;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.ProductQueryRequest;
import com.ab.kkmalladminweb.dto.ProductResponse;
import com.ab.kkmalladminweb.dto.ProductSaveRequest;

import java.util.List;

/**
 * Product service.
 */
public interface ProductService {

    PageResult<ProductResponse> list(ProductQueryRequest queryRequest);

    ProductResponse getById(Long id);

    ProductResponse create(ProductSaveRequest request);

    ProductResponse update(Long id, ProductSaveRequest request);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void updateStatus(Long id, Integer status);

    void updateStock(Long id, Integer stock);
}
