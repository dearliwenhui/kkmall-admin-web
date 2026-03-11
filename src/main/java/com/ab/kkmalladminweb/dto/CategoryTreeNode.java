package com.ab.kkmalladminweb.dto;

import lombok.Data;

import java.util.List;

/**
 * Category tree node for cascader.
 */
@Data
public class CategoryTreeNode {

    private Long value;

    private String label;

    private Integer level;

    private List<CategoryTreeNode> children;
}
