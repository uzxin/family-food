package com.familyfood.dto;

import com.familyfood.entity.Dish;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DishDetailResponse {
    private Long id;
    private Long familyId;
    private String name;
    private String categoryCode;
    private String categoryName;
    private String difficulty;
    private String imageUrl;
    private String remark;
    private List<String> ingredients;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static DishDetailResponse fromDish(Dish dish, String categoryName, List<String> ingredients) {
        DishDetailResponse response = new DishDetailResponse();
        response.setId(dish.getId());
        response.setFamilyId(dish.getFamilyId());
        response.setName(dish.getName());
        response.setCategoryCode(dish.getCategoryCode());
        response.setCategoryName(categoryName);
        response.setDifficulty(dish.getDifficulty());
        response.setImageUrl(dish.getImageUrl());
        response.setRemark(dish.getRemark());
        response.setIngredients(ingredients);
        response.setCreateTime(dish.getCreateTime());
        response.setUpdateTime(dish.getUpdateTime());
        return response;
    }
}
