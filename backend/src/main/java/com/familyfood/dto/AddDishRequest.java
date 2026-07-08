package com.familyfood.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AddDishRequest {
    @NotBlank(message = "菜品名称不能为空")
    private String name;

    @NotBlank(message = "分类不能为空")
    private String categoryCode;

    private String difficulty = "normal";
    private String imageUrl;
    private String remark;
    private List<String> ingredients;
}
