package com.familyfood.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.common.BusinessException;
import com.familyfood.dto.AddCategoryRequest;
import com.familyfood.entity.Category;
import com.familyfood.mapper.CategoryMapper;
import com.familyfood.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    // 内置分类定义
    private static final String[][] BUILTIN_CATEGORIES = {
            {"cold", "凉菜", "🥗"},
            {"stir-fry", "小炒", "🍳"},
            {"braised", "烧菜", "🍖"},
            {"steamed", "蒸菜", "🫕"},
            {"soup", "汤/煲", "🍲"},
            {"staple", "主食", "🍚"},
            {"snack", "小吃", "🥟"},
            {"special", "特色菜", "⭐"}
    };

    @Override
    public List<Category> listByFamilyId(Long familyId) {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getFamilyId, familyId)
                        .orderByAsc(Category::getSortOrder));
    }

    @Override
    public Category addCategory(Long familyId, AddCategoryRequest request) {
        // 检查是否已存在
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getFamilyId, familyId)
                        .eq(Category::getName, request.getName()));
        if (count > 0) {
            throw new BusinessException("分类已存在");
        }

        Category category = new Category();
        category.setFamilyId(familyId);
        category.setCode("custom-" + UUID.randomUUID().toString().substring(0, 8));
        category.setName(request.getName());
        category.setIcon(request.getIcon() != null ? request.getIcon() : "🍽️");
        category.setIsBuiltin(0);

        // 获取最大排序号
        Long maxSort = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getFamilyId, familyId));
        category.setSortOrder(maxSort.intValue());

        categoryMapper.insert(category);
        return category;
    }

    @Override
    public void initBuiltinCategories(Long familyId) {
        for (int i = 0; i < BUILTIN_CATEGORIES.length; i++) {
            Category category = new Category();
            category.setFamilyId(familyId);
            category.setCode(BUILTIN_CATEGORIES[i][0]);
            category.setName(BUILTIN_CATEGORIES[i][1]);
            category.setIcon(BUILTIN_CATEGORIES[i][2]);
            category.setSortOrder(i);
            category.setIsBuiltin(1);
            categoryMapper.insert(category);
        }
    }
}
