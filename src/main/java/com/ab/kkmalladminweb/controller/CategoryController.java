package com.ab.kkmalladminweb.controller;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.common.Result;
import com.ab.kkmalladminweb.dto.BatchDeleteRequest;
import com.ab.kkmalladminweb.dto.CategoryQueryRequest;
import com.ab.kkmalladminweb.dto.CategoryResponse;
import com.ab.kkmalladminweb.dto.CategorySaveRequest;
import com.ab.kkmalladminweb.dto.CategoryTreeNode;
import com.ab.kkmalladminweb.dto.UpdateSortRequest;
import com.ab.kkmalladminweb.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Category management controller.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Query category list with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('category:manage')")
    public Result<PageResult<CategoryResponse>> list(@Valid CategoryQueryRequest queryRequest) {
        return Result.success(categoryService.list(queryRequest));
    }

    /**
     * Get category tree.
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('category:manage')")
    public Result<List<CategoryResponse>> getTree() {
        return Result.success(categoryService.getTree());
    }

    /**
     * Get category tree nodes for cascader (public access).
     */
    @GetMapping("/tree-nodes")
    public Result<List<CategoryTreeNode>> getTreeNodes() {
        return Result.success(categoryService.getTreeNodes());
    }

    /**
     * Get category detail by id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('category:manage')")
    public Result<CategoryResponse> detail(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    /**
     * Create category.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('category:add')")
    public Result<CategoryResponse> create(@Valid @RequestBody CategorySaveRequest request) {
        return Result.success("Created", categoryService.create(request));
    }

    /**
     * Update category.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('category:edit')")
    public Result<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategorySaveRequest request) {
        return Result.success("Updated", categoryService.update(id, request));
    }

    /**
     * Delete category by id.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    /**
     * Batch delete categories.
     */
    @PostMapping("/batch-delete")
    @PreAuthorize("hasAuthority('category:delete')")
    public Result<Void> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {
        categoryService.batchDelete(request.getIds());
        return Result.success();
    }

    /**
     * Update category sort.
     */
    @PutMapping("/{id}/sort")
    @PreAuthorize("hasAuthority('category:edit')")
    public Result<Void> updateSort(@PathVariable Long id, @Valid @RequestBody UpdateSortRequest request) {
        categoryService.updateSort(id, request.getSort());
        return Result.success("Sort updated");
    }
}
