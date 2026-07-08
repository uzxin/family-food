package com.familyfood.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.dto.MenuResponse;
import com.familyfood.dto.SaveMenuRequest;
import com.familyfood.entity.Category;
import com.familyfood.entity.DailyMenu;
import com.familyfood.entity.DailyMenuDish;
import com.familyfood.entity.Dish;
import com.familyfood.mapper.CategoryMapper;
import com.familyfood.mapper.DailyMenuDishMapper;
import com.familyfood.mapper.DailyMenuMapper;
import com.familyfood.mapper.DishMapper;
import com.familyfood.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final DailyMenuMapper dailyMenuMapper;
    private final DailyMenuDishMapper dailyMenuDishMapper;
    private final DishMapper dishMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public MenuResponse getMenuByDate(Long familyId, LocalDate date) {
        DailyMenu menu = dailyMenuMapper.selectOne(
                new LambdaQueryWrapper<DailyMenu>()
                        .eq(DailyMenu::getFamilyId, familyId)
                        .eq(DailyMenu::getMenuDate, date));

        MenuResponse response = new MenuResponse();
        response.setMenuDate(date);

        if (menu == null) {
            response.setDishIds(Collections.emptyList());
            response.setDishes(Collections.emptyList());
            return response;
        }

        response.setId(menu.getId());

        // 查询关联的菜品
        List<DailyMenuDish> menuDishes = dailyMenuDishMapper.selectList(
                new LambdaQueryWrapper<DailyMenuDish>()
                        .eq(DailyMenuDish::getMenuId, menu.getId())
                        .orderByAsc(DailyMenuDish::getSortOrder));

        List<Long> dishIds = menuDishes.stream()
                .map(DailyMenuDish::getDishId)
                .collect(Collectors.toList());
        response.setDishIds(dishIds);

        // 查询菜品详情
        if (!dishIds.isEmpty()) {
            List<Dish> dishes = dishMapper.selectBatchIds(dishIds);
            List<MenuResponse.DishSimpleInfo> dishInfos = dishes.stream().map(dish -> {
                MenuResponse.DishSimpleInfo info = new MenuResponse.DishSimpleInfo();
                info.setId(dish.getId());
                info.setName(dish.getName());
                info.setCategoryCode(dish.getCategoryCode());
                info.setCategoryName(getCategoryName(dish.getCategoryCode(), familyId));
                info.setImageUrl(dish.getImageUrl());
                return info;
            }).collect(Collectors.toList());
            response.setDishes(dishInfos);
        } else {
            response.setDishes(Collections.emptyList());
        }

        return response;
    }

    @Override
    @Transactional
    public void saveMenu(Long familyId, Long userId, SaveMenuRequest request) {
        LocalDate date = request.getDate() != null ? request.getDate() : LocalDate.now();

        // 查找或创建菜单
        DailyMenu menu = dailyMenuMapper.selectOne(
                new LambdaQueryWrapper<DailyMenu>()
                        .eq(DailyMenu::getFamilyId, familyId)
                        .eq(DailyMenu::getMenuDate, date));

        if (menu == null) {
            menu = new DailyMenu();
            menu.setFamilyId(familyId);
            menu.setMenuDate(date);
            menu.setCreatorUserId(userId);
            dailyMenuMapper.insert(menu);
        } else {
            // 清除旧的关联
            dailyMenuDishMapper.delete(
                    new LambdaQueryWrapper<DailyMenuDish>()
                            .eq(DailyMenuDish::getMenuId, menu.getId()));
        }

        // 保存新的菜品关联
        if (request.getDishIds() != null && !request.getDishIds().isEmpty()) {
            for (int i = 0; i < request.getDishIds().size(); i++) {
                DailyMenuDish menuDish = new DailyMenuDish();
                menuDish.setMenuId(menu.getId());
                menuDish.setDishId(request.getDishIds().get(i));
                menuDish.setSortOrder(i);
                dailyMenuDishMapper.insert(menuDish);
            }
        }
    }

    @Override
    @Transactional
    public void removeDishFromMenu(Long familyId, LocalDate date, Long dishId) {
        DailyMenu menu = dailyMenuMapper.selectOne(
                new LambdaQueryWrapper<DailyMenu>()
                        .eq(DailyMenu::getFamilyId, familyId)
                        .eq(DailyMenu::getMenuDate, date));

        if (menu == null) {
            return;
        }

        dailyMenuDishMapper.delete(
                new LambdaQueryWrapper<DailyMenuDish>()
                        .eq(DailyMenuDish::getMenuId, menu.getId())
                        .eq(DailyMenuDish::getDishId, dishId));

        // 如果没有菜品了，删除菜单记录
        Long count = dailyMenuDishMapper.selectCount(
                new LambdaQueryWrapper<DailyMenuDish>()
                        .eq(DailyMenuDish::getMenuId, menu.getId()));
        if (count == 0) {
            dailyMenuMapper.deleteById(menu.getId());
        }
    }

    private String getCategoryName(String categoryCode, Long familyId) {
        Category category = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getFamilyId, familyId)
                        .eq(Category::getCode, categoryCode));
        return category != null ? category.getName() : "未分类";
    }
}
