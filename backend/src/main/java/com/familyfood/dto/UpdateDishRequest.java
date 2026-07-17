package com.familyfood.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdateDishRequest {
    private String name;
    private String categoryCode;
    private String difficulty;
    private String imageUrl;
    private Integer rating;
    private String remark;
    private List<String> ingredients;
}
