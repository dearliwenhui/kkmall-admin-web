package com.ab.kkmalladminweb.service;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.CategoryQueryRequest;
import com.ab.kkmalladminweb.dto.CategoryResponse;
import com.ab.kkmalladminweb.dto.CategorySaveRequest;
import com.ab.kkmalladminweb.dto.CategoryTreeNode;

import java.util.List;

/**
 * Category service.
 */
public interface CategoryService {

    PageResult<CategoryResponse> list(CategoryQueryRequest queryRequest);

    List<CategoryResponse> getTree();

    List<CategoryTreeNode> getTreeNodes();

    CategoryResponse getById(Long id);

    CategoryResponse create(CategorySaveRequest request);

    CategoryResponse update(Long id, CategorySaveRequest request);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void updateSort(Long id, Integer sort);
}
