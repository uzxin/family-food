package com.familyfood.controller;

import com.familyfood.common.Result;
import com.familyfood.dto.UpdateProfileRequest;
import com.familyfood.entity.User;
import com.familyfood.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户信息")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userService.getById(userId);
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("avatarUrl", user.getAvatarUrl());
        return Result.ok(info);
    }

    @Operation(summary = "修改用户昵称")
    @PutMapping("/profile")
    public Result<Void> updateProfile(Authentication auth, @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = (Long) auth.getPrincipal();
        userService.updateProfile(userId, request);
        return Result.ok();
    }
}
