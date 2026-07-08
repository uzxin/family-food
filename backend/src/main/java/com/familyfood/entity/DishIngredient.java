package com.familyfood.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 菜品食材关联表
 */
@Data
@TableName("dish_ingredient")
public class DishIngredient {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 菜品ID */
    private Long dishId;

    /** 食材名称 */
    private String ingredientName;

    /** 排序 */
    private Integer sortOrder;
}
