package com.familyfood.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddCategoryRequest {
    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String icon;
}
