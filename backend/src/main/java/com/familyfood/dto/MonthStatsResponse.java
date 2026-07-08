package com.familyfood.dto;

import lombok.Data;
import java.util.List;

@Data
public class MonthStatsResponse {
    /** 记录天数 */
    private int totalDays;
    /** 总菜品次 */
    private int totalDishes;
    /** 不重复菜数 */
    private int uniqueDishes;
    /** 有菜单的日期列表 */
    private List<String> menuDates;
    /** Top高频菜品 */
    private List<TopDish> topDishes;

    @Data
    public static class TopDish {
        private Long dishId;
        private String name;
        private int count;
        /** 占比百分比（相对最高频菜品） */
        private int percent;
    }
}
