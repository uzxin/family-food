package com.familyfood.controller;

import com.familyfood.common.Result;
import com.familyfood.dto.SaveMenuRequest;
import com.familyfood.dto.MenuResponse;
import com.familyfood.entity.Family;
import com.familyfood.service.FamilyService;
import com.familyfood.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "每日菜单")
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final FamilyService familyService;

    @Operation(summary = "获取今日菜单")
    @GetMapping("/today")
    public Result<MenuResponse> getTodayMenu(
            Authentication auth,
            @RequestHeader(value = "X-Family-Id", required = false) Long familyId) {
        Long userId = (Long) auth.getPrincipal();
        Family family = familyService.getCurrentFamily(userId, familyId);
        if (family == null) {
            return Result.fail("请先创建或加入家庭");
        }
        return Result.ok(menuService.getMenuByDate(family.getId(), LocalDate.now()));
    }

    @Operation(summary = "获取指定日期菜单")
    @GetMapping("/date/{date}")
    public Result<MenuResponse> getMenuByDate(
            Authentication auth,
            @RequestHeader(value = "X-Family-Id", required = false) Long familyId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        Long userId = (Long) auth.getPrincipal();
        Family family = familyService.getCurrentFamily(userId, familyId);
        if (family == null) {
            return Result.fail("请先创建或加入家庭");
        }
        return Result.ok(menuService.getMenuByDate(family.getId(), date));
    }

    @Operation(summary = "保存/更新每日菜单")
    @PostMapping("/save")
    public Result<Void> saveMenu(
            Authentication auth,
            @RequestHeader(value = "X-Family-Id", required = false) Long familyId,
            @RequestBody SaveMenuRequest request) {
        Long userId = (Long) auth.getPrincipal();
        Family family = familyService.getCurrentFamily(userId, familyId);
        if (family == null) {
            return Result.fail("请先创建或加入家庭");
        }
        menuService.saveMenu(family.getId(), userId, request);
        return Result.ok();
    }

    @Operation(summary = "从菜单中移除菜品")
    @DeleteMapping("/dish")
    public Result<Void> removeDishFromMenu(
            Authentication auth,
            @RequestHeader(value = "X-Family-Id", required = false) Long familyId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam Long dishId) {
        Long userId = (Long) auth.getPrincipal();
        Family family = familyService.getCurrentFamily(userId, familyId);
        if (family == null) {
            return Result.fail("请先创建或加入家庭");
        }
        menuService.removeDishFromMenu(family.getId(), date, dishId);
        return Result.ok();
    }
}
