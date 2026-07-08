package com.familyfood.controller;

import com.familyfood.common.Result;
import com.familyfood.dto.*;
import com.familyfood.entity.Family;
import com.familyfood.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "家庭空间")
@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @Operation(summary = "创建家庭")
    @PostMapping("/create")
    public Result<Family> createFamily(Authentication auth, @Valid @RequestBody CreateFamilyRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(familyService.createFamily(userId, request));
    }

    @Operation(summary = "通过邀请码加入家庭")
    @PostMapping("/join")
    public Result<Family> joinFamily(Authentication auth, @Valid @RequestBody JoinFamilyRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(familyService.joinFamily(userId, request));
    }

    @Operation(summary = "我的家庭列表（含成员）")
    @GetMapping("/list")
    public Result<List<FamilyDetailResponse>> listFamilies(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        List<Family> families = familyService.getUserFamilies(userId);
        List<FamilyDetailResponse> list = families.stream()
                .map(f -> familyService.getFamilyDetail(f.getId()))
                .collect(Collectors.toList());
        return Result.ok(list);
    }

    @Operation(summary = "当前家庭详情")
    @GetMapping("/current")
    public Result<FamilyDetailResponse> currentFamily(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Family family = familyService.getCurrentFamily(userId);
        if (family == null) {
            return Result.ok(null);
        }
        return Result.ok(familyService.getFamilyDetail(family.getId()));
    }

    @Operation(summary = "家庭详情")
    @GetMapping("/{familyId}")
    public Result<FamilyDetailResponse> familyDetail(@PathVariable Long familyId) {
        return Result.ok(familyService.getFamilyDetail(familyId));
    }

    @Operation(summary = "切换当前家庭")
    @PostMapping("/switch")
    public Result<FamilyDetailResponse> switchFamily(Authentication auth, @RequestBody SwitchFamilyRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(familyService.switchFamily(userId, request.getFamilyId()));
    }
}
