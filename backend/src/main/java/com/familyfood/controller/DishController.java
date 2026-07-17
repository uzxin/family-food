package com.familyfood.controller;

import com.familyfood.common.Result;
import com.familyfood.dto.AddDishRequest;
import com.familyfood.dto.DishDetailResponse;
import com.familyfood.dto.UpdateDishRequest;
import com.familyfood.entity.Dish;
import com.familyfood.entity.Family;
import com.familyfood.service.DishService;
import com.familyfood.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "菜品库")
@RestController
@RequestMapping("/api/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;
    private final FamilyService familyService;

    @Operation(summary = "菜品列表")
    @GetMapping("/list")
    public Result<List<DishDetailResponse>> listDishes(
            Authentication auth,
            @RequestHeader(value = "X-Family-Id", required = false) Long familyId,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String keyword) {
        Long userId = (Long) auth.getPrincipal();
        Family family = familyService.getCurrentFamily(userId, familyId);
        if (family == null) {
            return Result.fail("请先创建或加入家庭");
        }
        return Result.ok(dishService.listDishes(family.getId(), categoryCode, keyword));
    }

    @Operation(summary = "菜品详情")
    @GetMapping("/{id}")
    public Result<DishDetailResponse> getDishDetail(@PathVariable Long id) {
        return Result.ok(dishService.getDishDetail(id));
    }

    @Operation(summary = "新增菜品")
    @PostMapping("/add")
    public Result<Dish> addDish(
            Authentication auth,
            @RequestHeader(value = "X-Family-Id", required = false) Long familyId,
            @Valid @RequestBody AddDishRequest request) {
        Long userId = (Long) auth.getPrincipal();
        Family family = familyService.getCurrentFamily(userId, familyId);
        if (family == null) {
            return Result.fail("请先创建或加入家庭");
        }
        return Result.ok(dishService.addDish(family.getId(), userId, request));
    }

    @Operation(summary = "编辑菜品")
    @PutMapping("/update/{id}")
    public Result<Void> updateDish(@PathVariable Long id, @RequestBody UpdateDishRequest request) {
        dishService.updateDish(id, request);
        return Result.ok();
    }

    @Operation(summary = "删除菜品")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id);
        return Result.ok();
    }

    @Operation(summary = "菜品评分")
    @PutMapping("/{id}/rate")
    public Result<Void> rateDish(@PathVariable Long id, @RequestParam int rating) {
        dishService.rateDish(id, rating);
        return Result.ok();
    }
}
