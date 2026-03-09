package com.ab.kkmalladminweb.controller;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.common.Result;
import com.ab.kkmalladminweb.dto.BatchDeleteRequest;
import com.ab.kkmalladminweb.dto.ProductQueryRequest;
import com.ab.kkmalladminweb.dto.ProductResponse;
import com.ab.kkmalladminweb.dto.ProductSaveRequest;
import com.ab.kkmalladminweb.dto.UpdateStockRequest;
import com.ab.kkmalladminweb.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Product management controller.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    /**
     * Query product list with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('product:manage')")
    public Result<PageResult<ProductResponse>> list(@Valid ProductQueryRequest queryRequest) {
        return Result.success(productService.list(queryRequest));
    }

    /**
     * Get product detail by id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product:manage')")
    public Result<ProductResponse> detail(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    /**
     * Create product.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('product:add')")
    public Result<ProductResponse> create(@Valid @RequestBody ProductSaveRequest request) {
        return Result.success("Created", productService.create(request));
    }

    /**
     * Update product.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('product:edit')")
    public Result<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductSaveRequest request) {
        return Result.success("Updated", productService.update(id, request));
    }

    /**
     * Delete product by id.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('product:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }

    /**
     * Batch delete products.
     */
    @PostMapping("/batch-delete")
    @PreAuthorize("hasAuthority('product:delete')")
    public Result<Void> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {
        productService.batchDelete(request.getIds());
        return Result.success();
    }

    /**
     * Set product status to on sale.
     */
    @PutMapping("/{id}/on-sale")
    @PreAuthorize("hasAuthority('product:edit')")
    public Result<Void> onSale(@PathVariable Long id) {
        productService.updateStatus(id, 1);
        return Result.success();
    }

    /**
     * Set product status to off sale.
     */
    @PutMapping("/{id}/off-sale")
    @PreAuthorize("hasAuthority('product:edit')")
    public Result<Void> offSale(@PathVariable Long id) {
        productService.updateStatus(id, 0);
        return Result.success();
    }

    /**
     * Update stock.
     */
    @PutMapping("/{id}/stock")
    @PreAuthorize("hasAuthority('product:edit')")
    public Result<Void> updateStock(@PathVariable Long id, @Valid @RequestBody UpdateStockRequest request) {
        productService.updateStock(id, request.getStock());
        return Result.success();
    }
}
