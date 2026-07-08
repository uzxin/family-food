package com.familyfood.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class MenuResponse {
    private Long id;
    private LocalDate menuDate;
    private List<Long> dishIds;
    private List<DishSimpleInfo> dishes;

    @Data
    public static class DishSimpleInfo {
        private Long id;
        private String name;
        private String categoryCode;
        private String categoryName;
        private String imageUrl;
    }
}
