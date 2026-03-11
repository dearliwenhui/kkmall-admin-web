package com.ab.kkmalladminweb.mapper;

import com.ab.kkmalladminweb.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Category mapper.
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * Count products by category ID.
     *
     * @param categoryId category ID
     * @return product count
     */
    @Select("SELECT COUNT(*) FROM mall_product WHERE category_id = #{categoryId} AND deleted = 0")
    long countProductsByCategoryId(Long categoryId);
}
