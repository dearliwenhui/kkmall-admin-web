package com.ab.kkmalladminweb.service.impl;

import com.ab.kkmalladminweb.common.PageResult;
import com.ab.kkmalladminweb.dto.CategoryQueryRequest;
import com.ab.kkmalladminweb.dto.CategoryResponse;
import com.ab.kkmalladminweb.dto.CategorySaveRequest;
import com.ab.kkmalladminweb.dto.CategoryTreeNode;
import com.ab.kkmalladminweb.entity.Category;
import com.ab.kkmalladminweb.mapper.CategoryMapper;
import com.ab.kkmalladminweb.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Category service implementation.
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<CategoryResponse> list(CategoryQueryRequest queryRequest) {
        long pageNum = queryRequest.getPageNum() == null ? 1L : queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize() == null ? 10L : queryRequest.getPageSize();
        String name = StringUtils.hasText(queryRequest.getName())
                ? queryRequest.getName().trim()
                : null;

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StringUtils.hasText(name), Category::getName, name)
                .eq(queryRequest.getParentId() != null, Category::getParentId, queryRequest.getParentId())
                .eq(queryRequest.getLevel() != null, Category::getLevel, queryRequest.getLevel())
                .orderByAsc(Category::getSort)
                .orderByDesc(Category::getId);

        Page<Category> page = categoryMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        List<CategoryResponse> records = page.getRecords().stream().map(this::toResponse).toList();

        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<CategoryResponse> getTree() {
        List<Category> allCategories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort)
        );
        List<CategoryResponse> responses = allCategories.stream().map(this::toResponse).toList();
        return buildTree(responses, 0L);
    }

    @Override
    public List<CategoryTreeNode> getTreeNodes() {
        List<Category> allCategories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort)
        );
        List<CategoryTreeNode> nodes = allCategories.stream().map(this::toTreeNode).toList();
        return buildTreeNodes(nodes, 0L);
    }

    @Override
    public CategoryResponse getById(Long id) {
        Category category = requireCategory(id);
        return toResponse(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryResponse create(CategorySaveRequest request) {
        validateParentAndLevel(null, request.getParentId(), request.getLevel());

        Category category = new Category();
        applyRequest(category, request);
        categoryMapper.insert(category);
        return getById(category.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryResponse update(Long id, CategorySaveRequest request) {
        Category category = requireCategory(id);

        // Check if trying to set parent to itself or its descendant
        if (!category.getParentId().equals(request.getParentId())) {
            if (id.equals(request.getParentId())) {
                throw new RuntimeException("Cannot set parent to itself");
            }
            if (isDescendant(id, request.getParentId())) {
                throw new RuntimeException("Cannot set parent to its descendant");
            }
        }

        validateParentAndLevel(id, request.getParentId(), request.getLevel());

        applyRequest(category, request);
        categoryMapper.updateById(category);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Category category = requireCategory(id);

        // Check if has children
        long childCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getParentId, id)
        );
        if (childCount > 0) {
            throw new RuntimeException("Cannot delete category with children");
        }

        // Check if has products
        long productCount = categoryMapper.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new RuntimeException("Cannot delete category with products");
        }

        int affected = categoryMapper.deleteById(id);
        if (affected == 0) {
            throw new RuntimeException("Category not found: " + id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            delete(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSort(Long id, Integer sort) {
        if (sort == null || sort < 0) {
            throw new RuntimeException("sort must be >= 0");
        }
        Category category = requireCategory(id);
        category.setSort(sort);
        categoryMapper.updateById(category);
    }

    private Category requireCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("Category not found: " + id);
        }
        return category;
    }

    private void applyRequest(Category category, CategorySaveRequest request) {
        category.setName(request.getName().trim());
        category.setParentId(request.getParentId());
        category.setLevel(request.getLevel());
        category.setSort(request.getSort() != null ? request.getSort() : 0);
        category.setIcon(request.getIcon());
    }

    private CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setParentId(category.getParentId());
        response.setLevel(category.getLevel());
        response.setSort(category.getSort());
        response.setIcon(category.getIcon());
        response.setCreateTime(category.getCreateTime());
        response.setUpdateTime(category.getUpdateTime());
        return response;
    }

    private CategoryTreeNode toTreeNode(Category category) {
        CategoryTreeNode node = new CategoryTreeNode();
        node.setValue(category.getId());
        node.setLabel(category.getName());
        node.setLevel(category.getLevel());
        return node;
    }

    private List<CategoryResponse> buildTree(List<CategoryResponse> categories, Long parentId) {
        Map<Long, List<CategoryResponse>> groupedByParent = categories.stream()
                .collect(Collectors.groupingBy(CategoryResponse::getParentId));

        return buildTreeRecursive(groupedByParent, parentId);
    }

    private List<CategoryResponse> buildTreeRecursive(
            Map<Long, List<CategoryResponse>> groupedByParent,
            Long parentId
    ) {
        List<CategoryResponse> children = groupedByParent.get(parentId);
        if (children == null) {
            return new ArrayList<>();
        }

        for (CategoryResponse child : children) {
            List<CategoryResponse> subChildren = buildTreeRecursive(groupedByParent, child.getId());
            if (!subChildren.isEmpty()) {
                child.setChildren(subChildren);
            }
        }

        return children;
    }

    private List<CategoryTreeNode> buildTreeNodes(List<CategoryTreeNode> nodes, Long parentId) {
        // Group by parent using category data
        List<Category> allCategories = categoryMapper.selectList(null);
        Map<Long, List<CategoryTreeNode>> groupedByParent = new java.util.HashMap<>();

        for (CategoryTreeNode node : nodes) {
            Category category = allCategories.stream()
                    .filter(c -> c.getId().equals(node.getValue()))
                    .findFirst()
                    .orElse(null);

            if (category != null) {
                groupedByParent.computeIfAbsent(category.getParentId(), k -> new ArrayList<>()).add(node);
            }
        }

        return buildTreeNodesRecursive(groupedByParent, parentId);
    }

    private List<CategoryTreeNode> buildTreeNodesRecursive(
            Map<Long, List<CategoryTreeNode>> groupedByParent,
            Long parentId
    ) {
        List<CategoryTreeNode> children = groupedByParent.get(parentId);
        if (children == null) {
            return new ArrayList<>();
        }

        for (CategoryTreeNode child : children) {
            List<CategoryTreeNode> subChildren = buildTreeNodesRecursive(groupedByParent, child.getValue());
            if (!subChildren.isEmpty()) {
                child.setChildren(subChildren);
            }
        }

        return children;
    }

    private void validateParentAndLevel(Long currentId, Long parentId, Integer level) {
        if (parentId == 0) {
            // Top-level category must be level 1
            if (level != 1) {
                throw new RuntimeException("Top-level category must be level 1");
            }
        } else {
            // Child category
            Category parent = requireCategory(parentId);
            if (level != parent.getLevel() + 1) {
                throw new RuntimeException("Level must be parent level + 1");
            }
            if (level > 3) {
                throw new RuntimeException("Maximum level is 3");
            }
        }
    }

    private boolean isDescendant(Long ancestorId, Long descendantId) {
        if (ancestorId.equals(descendantId)) {
            return true;
        }

        Category descendant = categoryMapper.selectById(descendantId);
        if (descendant == null || descendant.getParentId() == 0) {
            return false;
        }

        return isDescendant(ancestorId, descendant.getParentId());
    }
}
