package com.familyfood.service;

import com.familyfood.dto.CreateFamilyRequest;
import com.familyfood.dto.FamilyDetailResponse;
import com.familyfood.dto.JoinFamilyRequest;
import com.familyfood.entity.Family;

import java.util.List;

public interface FamilyService {

    /**
     * 创建家庭
     */
    Family createFamily(Long userId, CreateFamilyRequest request);

    /**
     * 通过邀请码加入家庭
     */
    Family joinFamily(Long userId, JoinFamilyRequest request);

    /**
     * 获取用户加入的家庭列表
     */
    List<Family> getUserFamilies(Long userId);

    /**
     * 获取家庭详情（含成员列表）
     */
    FamilyDetailResponse getFamilyDetail(Long familyId);

    /**
     * 获取用户当前家庭（默认返回第一个）
     */
    Family getCurrentFamily(Long userId);

    /**
     * 获取用户当前家庭（优先使用指定 familyId，校验成员身份）
     */
    Family getCurrentFamily(Long userId, Long familyId);

    /**
     * 切换当前家庭（校验用户是否为该家庭成员）
     */
    FamilyDetailResponse switchFamily(Long userId, Long familyId);
}
