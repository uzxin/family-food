package com.familyfood.service;

import com.familyfood.dto.LoginRequest;
import com.familyfood.dto.LoginResponse;
import com.familyfood.dto.RegisterRequest;
import com.familyfood.dto.UpdateProfileRequest;
import com.familyfood.dto.WxLoginRequest;
import com.familyfood.entity.User;

public interface UserService {

    /**
     * 用户名密码注册
     */
    void register(RegisterRequest request);

    /**
     * 用户名密码登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 微信登录
     */
    LoginResponse wxLogin(WxLoginRequest request);

    /**
     * 根据ID获取用户
     */
    User getById(Long id);

    /**
     * 修改用户昵称
     */
    void updateProfile(Long userId, UpdateProfileRequest request);
}
