package com.familyfood.service;

import com.familyfood.dto.AddCategoryRequest;
import com.familyfood.entity.Category;

import java.util.List;

public interface CategoryService {

    /**
     * 获取家庭的分类列表
     */
    List<Category> listByFamilyId(Long familyId);

    /**
     * 新增自定义分类
     */
    Category addCategory(Long familyId, AddCategoryRequest request);

    /**
     * 初始化内置分类
     */
    void initBuiltinCategories(Long familyId);
}
