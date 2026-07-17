package com.familyfood.service;

import com.familyfood.dto.AddDishRequest;
import com.familyfood.dto.DishDetailResponse;
import com.familyfood.dto.UpdateDishRequest;
import com.familyfood.entity.Dish;

import java.util.List;

public interface DishService {

    /**
     * 菜品列表（支持分类筛选和搜索）
     */
    List<DishDetailResponse> listDishes(Long familyId, String categoryCode, String keyword);

    /**
     * 菜品详情
     */
    DishDetailResponse getDishDetail(Long dishId);

    /**
     * 新增菜品
     */
    Dish addDish(Long familyId, Long userId, AddDishRequest request);

    /**
     * 更新菜品
     */
    void updateDish(Long dishId, UpdateDishRequest request);

    /**
     * 删除菜品
     */
    void deleteDish(Long dishId);

    /**
     * 菜品评分
     */
    void rateDish(Long dishId, int rating);
}
