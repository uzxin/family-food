package com.familyfood.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.common.BusinessException;
import com.familyfood.dto.AddDishRequest;
import com.familyfood.dto.DishDetailResponse;
import com.familyfood.dto.UpdateDishRequest;
import com.familyfood.entity.Category;
import com.familyfood.entity.Dish;
import com.familyfood.entity.DishIngredient;
import com.familyfood.mapper.CategoryMapper;
import com.familyfood.mapper.DishIngredientMapper;
import com.familyfood.mapper.DishMapper;
import com.familyfood.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final DishIngredientMapper dishIngredientMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public List<DishDetailResponse> listDishes(Long familyId, String categoryCode, String keyword) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getFamilyId, familyId);

        if (StringUtils.hasText(categoryCode)) {
            wrapper.eq(Dish::getCategoryCode, categoryCode);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Dish::getName, keyword)
                    .or()
                    .apply("id IN (SELECT dish_id FROM dish_ingredient WHERE ingredient_name LIKE {0})", "%" + keyword + "%"));
        }

        wrapper.orderByDesc(Dish::getCreateTime);
        List<Dish> dishes = dishMapper.selectList(wrapper);

        return dishes.stream().map(dish -> {
            String categoryName = getCategoryName(dish.getCategoryCode(), familyId);
            List<String> ingredients = getIngredients(dish.getId());
            return DishDetailResponse.fromDish(dish, categoryName, ingredients);
        }).collect(Collectors.toList());
    }

    @Override
    public DishDetailResponse getDishDetail(Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }

        String categoryName = getCategoryName(dish.getCategoryCode(), dish.getFamilyId());
        List<String> ingredients = getIngredients(dishId);
        return DishDetailResponse.fromDish(dish, categoryName, ingredients);
    }

    @Override
    @Transactional
    public Dish addDish(Long familyId, Long userId, AddDishRequest request) {
        Dish dish = new Dish();
        dish.setFamilyId(familyId);
        dish.setCreatorUserId(userId);
        dish.setName(request.getName());
        dish.setCategoryCode(request.getCategoryCode());
        dish.setDifficulty(request.getDifficulty());
        dish.setImageUrl(request.getImageUrl());
        dish.setRemark(request.getRemark());
        dishMapper.insert(dish);

        // 保存食材
        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            for (int i = 0; i < request.getIngredients().size(); i++) {
                DishIngredient ingredient = new DishIngredient();
                ingredient.setDishId(dish.getId());
                ingredient.setIngredientName(request.getIngredients().get(i));
                ingredient.setSortOrder(i);
                dishIngredientMapper.insert(ingredient);
            }
        }

        return dish;
    }

    @Override
    @Transactional
    public void updateDish(Long dishId, UpdateDishRequest request) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }

        if (StringUtils.hasText(request.getName())) {
            dish.setName(request.getName());
        }
        if (StringUtils.hasText(request.getCategoryCode())) {
            dish.setCategoryCode(request.getCategoryCode());
        }
        if (StringUtils.hasText(request.getDifficulty())) {
            dish.setDifficulty(request.getDifficulty());
        }
        if (request.getImageUrl() != null) {
            dish.setImageUrl(request.getImageUrl());
        }
        if (request.getRemark() != null) {
            dish.setRemark(request.getRemark());
        }
        dishMapper.updateById(dish);

        // 更新食材（先删后增）
        if (request.getIngredients() != null) {
            dishIngredientMapper.delete(
                    new LambdaQueryWrapper<DishIngredient>().eq(DishIngredient::getDishId, dishId));
            for (int i = 0; i < request.getIngredients().size(); i++) {
                DishIngredient ingredient = new DishIngredient();
                ingredient.setDishId(dishId);
                ingredient.setIngredientName(request.getIngredients().get(i));
                ingredient.setSortOrder(i);
                dishIngredientMapper.insert(ingredient);
            }
        }
    }

    @Override
    @Transactional
    public void deleteDish(Long dishId) {
        dishMapper.deleteById(dishId);
        // 级联删除由数据库外键约束处理
    }

    private List<String> getIngredients(Long dishId) {
        List<DishIngredient> ingredients = dishIngredientMapper.selectList(
                new LambdaQueryWrapper<DishIngredient>()
                        .eq(DishIngredient::getDishId, dishId)
                        .orderByAsc(DishIngredient::getSortOrder));
        return ingredients.stream()
                .map(DishIngredient::getIngredientName)
                .collect(Collectors.toList());
    }

    private String getCategoryName(String categoryCode, Long familyId) {
        Category category = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getFamilyId, familyId)
                        .eq(Category::getCode, categoryCode));
        return category != null ? category.getName() : "未分类";
    }
}
