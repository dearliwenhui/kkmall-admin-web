package com.ab.kkmalladminweb.service.impl;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.ProductQueryRequest;
import com.ab.kkmalladminweb.dto.ProductResponse;
import com.ab.kkmalladminweb.dto.ProductSaveRequest;
import com.ab.kkmalladminweb.entity.Product;
import com.ab.kkmalladminweb.mapper.ProductMapper;
import com.ab.kkmalladminweb.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product service implementation.
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public PageResult<ProductResponse> list(ProductQueryRequest queryRequest) {
        long pageNum = queryRequest.getPageNum() == null ? 1L : queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize() == null ? 10L : queryRequest.getPageSize();
        String productName = StringUtils.hasText(queryRequest.getProductName())
                ? queryRequest.getProductName().trim()
                : null;

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(
                        StringUtils.hasText(productName),
                        Product::getProductName,
                        productName
                )
                .eq(queryRequest.getCategoryId() != null, Product::getCategoryId, queryRequest.getCategoryId())
                .eq(queryRequest.getStatus() != null, Product::getStatus, queryRequest.getStatus())
                .orderByDesc(Product::getUpdateTime)
                .orderByDesc(Product::getId);

        Page<Product> page = productMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        List<ProductResponse> records = page.getRecords().stream().map(this::toResponse).toList();

        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public ProductResponse getById(Long id) {
        Product product = requireProduct(id);
        return toResponse(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductResponse create(ProductSaveRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        productMapper.insert(product);
        return getById(product.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductResponse update(Long id, ProductSaveRequest request) {
        Product product = requireProduct(id);
        applyRequest(product, request);
        productMapper.updateById(product);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        int affected = productMapper.deleteById(id);
        if (affected == 0) {
            throw new RuntimeException("Product not found: " + id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        productMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new RuntimeException("status must be 0 or 1");
        }
        Product product = requireProduct(id);
        product.setStatus(status);
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStock(Long id, Integer stock) {
        if (stock == null || stock < 0) {
            throw new RuntimeException("stock must be >= 0");
        }
        Product product = requireProduct(id);
        product.setStock(stock);
        productMapper.updateById(product);
    }

    private Product requireProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("Product not found: " + id);
        }
        return product;
    }

    private void applyRequest(Product product, ProductSaveRequest request) {
        product.setProductName(request.getProductName().trim());
        product.setProductCode(request.getProductCode().trim());
        product.setCategoryId(request.getCategoryId());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setStatus(request.getStatus());
        product.setImages(joinImages(request.getImages()));
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setProductName(product.getProductName());
        response.setProductCode(product.getProductCode());
        response.setCategoryId(product.getCategoryId());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setDescription(product.getDescription());
        response.setStatus(product.getStatus());
        response.setImages(parseImages(product.getImages()));
        response.setCreateTime(product.getCreateTime());
        response.setUpdateTime(product.getUpdateTime());
        return response;
    }

    private String joinImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        String value = images.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining(","));
        return StringUtils.hasText(value) ? value : null;
    }

    private List<String> parseImages(String images) {
        if (!StringUtils.hasText(images)) {
            return Collections.emptyList();
        }
        return Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}
