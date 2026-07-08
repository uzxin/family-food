package com.familyfood.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.dto.MonthStatsResponse;
import com.familyfood.entity.DailyMenu;
import com.familyfood.entity.DailyMenuDish;
import com.familyfood.entity.Dish;
import com.familyfood.mapper.DailyMenuDishMapper;
import com.familyfood.mapper.DailyMenuMapper;
import com.familyfood.mapper.DishMapper;
import com.familyfood.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final DailyMenuMapper dailyMenuMapper;
    private final DailyMenuDishMapper dailyMenuDishMapper;
    private final DishMapper dishMapper;

    @Override
    public MonthStatsResponse getMonthStats(Long familyId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 查询本月所有菜单
        List<DailyMenu> menus = dailyMenuMapper.selectList(
                new LambdaQueryWrapper<DailyMenu>()
                        .eq(DailyMenu::getFamilyId, familyId)
                        .ge(DailyMenu::getMenuDate, startDate)
                        .le(DailyMenu::getMenuDate, endDate)
                        .orderByAsc(DailyMenu::getMenuDate));

        MonthStatsResponse response = new MonthStatsResponse();

        if (menus.isEmpty()) {
            response.setTotalDays(0);
            response.setTotalDishes(0);
            response.setUniqueDishes(0);
            response.setMenuDates(Collections.emptyList());
            response.setTopDishes(Collections.emptyList());
            return response;
        }

        // 有菜单的日期
        List<String> menuDates = menus.stream()
                .map(m -> m.getMenuDate().toString())
                .collect(Collectors.toList());
        response.setMenuDates(menuDates);
        response.setTotalDays(menus.size());

        // 统计菜品频次
        List<Long> menuIds = menus.stream().map(DailyMenu::getId).collect(Collectors.toList());
        List<DailyMenuDish> allMenuDishes = dailyMenuDishMapper.selectList(
                new LambdaQueryWrapper<DailyMenuDish>()
                        .in(DailyMenuDish::getMenuId, menuIds));

        Map<Long, Integer> freqMap = new HashMap<>();
        Set<Long> uniqueDishIds = new HashSet<>();
        int totalDishes = 0;

        for (DailyMenuDish md : allMenuDishes) {
            freqMap.merge(md.getDishId(), 1, Integer::sum);
            uniqueDishIds.add(md.getDishId());
            totalDishes++;
        }

        response.setTotalDishes(totalDishes);
        response.setUniqueDishes(uniqueDishIds.size());

        // Top 5 高频菜品
        List<Map.Entry<Long, Integer>> sorted = freqMap.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(5)
                .collect(Collectors.toList());

        int maxCount = sorted.isEmpty() ? 1 : sorted.get(0).getValue();

        List<MonthStatsResponse.TopDish> topDishes = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : sorted) {
            Dish dish = dishMapper.selectById(entry.getKey());
            if (dish != null) {
                MonthStatsResponse.TopDish topDish = new MonthStatsResponse.TopDish();
                topDish.setDishId(dish.getId());
                topDish.setName(dish.getName());
                topDish.setCount(entry.getValue());
                topDish.setPercent((int) Math.round((double) entry.getValue() / maxCount * 100));
                topDishes.add(topDish);
            }
        }
        response.setTopDishes(topDishes);

        return response;
    }
}
