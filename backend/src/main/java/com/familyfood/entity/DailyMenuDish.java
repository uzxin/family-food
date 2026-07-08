package com.familyfood.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 菜单-菜品关联表
 */
@Data
@TableName("daily_menu_dish")
public class DailyMenuDish {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 每日菜单ID */
    private Long menuId;

    /** 菜品ID */
    private Long dishId;

    /** 排序 */
    private Integer sortOrder;
}
