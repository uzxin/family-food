package com.familyfood.controller;

import com.familyfood.common.Result;
import com.familyfood.dto.AddCategoryRequest;
import com.familyfood.entity.Category;
import com.familyfood.entity.Family;
import com.familyfood.service.CategoryService;
import com.familyfood.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "菜品分类")
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final FamilyService familyService;

    @Operation(summary = "获取分类列表")
    @GetMapping("/list")
    public Result<List<Category>> listCategories(
            Authentication auth,
            @RequestHeader(value = "X-Family-Id", required = false) Long familyId) {
        Long userId = (Long) auth.getPrincipal();
        Family family = familyService.getCurrentFamily(userId, familyId);
        if (family == null) {
            return Result.fail("请先创建或加入家庭");
        }
        return Result.ok(categoryService.listByFamilyId(family.getId()));
    }

    @Operation(summary = "新增分类")
    @PostMapping("/add")
    public Result<Category> addCategory(
            Authentication auth,
            @RequestHeader(value = "X-Family-Id", required = false) Long familyId,
            @Valid @RequestBody AddCategoryRequest request) {
        Long userId = (Long) auth.getPrincipal();
        Family family = familyService.getCurrentFamily(userId, familyId);
        if (family == null) {
            return Result.fail("请先创建或加入家庭");
        }
        return Result.ok(categoryService.addCategory(family.getId(), request));
    }
}
