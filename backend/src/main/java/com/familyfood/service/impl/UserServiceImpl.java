package com.familyfood.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.common.BusinessException;
import com.familyfood.common.JwtUtil;
import com.familyfood.dto.LoginRequest;
import com.familyfood.dto.LoginResponse;
import com.familyfood.dto.RegisterRequest;
import com.familyfood.dto.UpdateProfileRequest;
import com.familyfood.dto.WxLoginRequest;
import com.familyfood.entity.User;
import com.familyfood.mapper.UserMapper;
import com.familyfood.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${wechat.app-id}")
    private String appId;

    @Value("${wechat.app-secret}")
    private String appSecret;

    @Override
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getNickname());
    }

    @Override
    public LoginResponse wxLogin(WxLoginRequest request) {
        // 调用微信接口获取openid
        String openid = getOpenidByCode(request.getCode());
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException("微信登录失败，请重试");
        }

        // 查找或创建用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));

        if (user == null) {
            // 自动注册
            user = new User();
            user.setOpenid(openid);
            user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : "微信用户");
            user.setAvatarUrl(request.getAvatarUrl());
            userMapper.insert(user);
        }

        String username = user.getUsername() != null ? user.getUsername() : "wx_" + openid;
        String token = jwtUtil.generateToken(user.getId(), username);
        return new LoginResponse(token, user.getId(), user.getNickname());
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setNickname(request.getNickname());
        userMapper.updateById(user);
    }

    /**
     * 通过微信code换取openid
     */
    private String getOpenidByCode(String code) {
        try {
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    appId, appSecret, code);
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            if (result != null && result.containsKey("openid")) {
                return (String) result.get("openid");
            }
            log.error("微信登录失败: {}", result);
            return null;
        } catch (Exception e) {
            log.error("调用微信接口异常: ", e);
            return null;
        }
    }
}
