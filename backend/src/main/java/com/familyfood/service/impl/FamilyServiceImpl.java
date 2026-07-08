package com.familyfood.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.common.BusinessException;
import com.familyfood.dto.CreateFamilyRequest;
import com.familyfood.dto.FamilyDetailResponse;
import com.familyfood.dto.JoinFamilyRequest;
import com.familyfood.entity.Family;
import com.familyfood.entity.FamilyMember;
import com.familyfood.entity.User;
import com.familyfood.mapper.FamilyMapper;
import com.familyfood.mapper.FamilyMemberMapper;
import com.familyfood.mapper.UserMapper;
import com.familyfood.service.CategoryService;
import com.familyfood.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final UserMapper userMapper;
    private final CategoryService categoryService;

    @Override
    @Transactional
    public Family createFamily(Long userId, CreateFamilyRequest request) {
        Family family = new Family();
        family.setOwnerUserId(userId);
        family.setName(request.getName());
        family.setInviteCode(generateInviteCode());
        familyMapper.insert(family);

        // 添加创建者为管理员
        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getId());
        member.setUserId(userId);
        member.setRole(1); // 管理员
        member.setJoinTime(LocalDateTime.now());

        User user = userMapper.selectById(userId);
        member.setNickname(user != null ? user.getNickname() : "");
        familyMemberMapper.insert(member);

        // 初始化内置分类
        categoryService.initBuiltinCategories(family.getId());

        return family;
    }

    @Override
    @Transactional
    public Family joinFamily(Long userId, JoinFamilyRequest request) {
        String code = request.getInviteCode().trim().toUpperCase();
        Family family = familyMapper.selectOne(
                new LambdaQueryWrapper<Family>().eq(Family::getInviteCode, code));
        if (family == null) {
            throw new BusinessException("邀请码不存在");
        }

        // 检查是否已加入
        Long count = familyMemberMapper.selectCount(
                new LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getFamilyId, family.getId())
                        .eq(FamilyMember::getUserId, userId));
        if (count > 0) {
            return family;
        }

        // 加入家庭
        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getId());
        member.setUserId(userId);
        member.setRole(0); // 成员
        member.setJoinTime(LocalDateTime.now());

        User user = userMapper.selectById(userId);
        member.setNickname(user != null ? user.getNickname() : "");
        familyMemberMapper.insert(member);

        return family;
    }

    @Override
    public List<Family> getUserFamilies(Long userId) {
        // 查询用户加入的所有家庭ID
        List<FamilyMember> memberships = familyMemberMapper.selectList(
                new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getUserId, userId));
        if (memberships.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> familyIds = memberships.stream()
                .map(FamilyMember::getFamilyId)
                .collect(Collectors.toList());

        return familyMapper.selectBatchIds(familyIds);
    }

    @Override
    public FamilyDetailResponse getFamilyDetail(Long familyId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家庭不存在");
        }

        FamilyDetailResponse response = new FamilyDetailResponse();
        response.setId(family.getId());
        response.setName(family.getName());
        response.setInviteCode(family.getInviteCode());
        response.setOwnerUserId(family.getOwnerUserId());
        response.setCreateTime(family.getCreateTime());

        // 查询成员列表
        List<FamilyMember> members = familyMemberMapper.selectList(
                new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getFamilyId, familyId));
        List<FamilyDetailResponse.MemberInfo> memberInfos = members.stream().map(m -> {
            FamilyDetailResponse.MemberInfo info = new FamilyDetailResponse.MemberInfo();
            info.setUserId(m.getUserId());
            info.setNickname(m.getNickname());
            info.setRole(m.getRole());
            info.setJoinTime(m.getJoinTime());
            return info;
        }).collect(Collectors.toList());
        response.setMembers(memberInfos);

        return response;
    }

    @Override
    public Family getCurrentFamily(Long userId) {
        List<Family> families = getUserFamilies(userId);
        if (families.isEmpty()) {
            return null;
        }
        return families.get(0);
    }

    @Override
    public Family getCurrentFamily(Long userId, Long familyId) {
        if (familyId == null) {
            return getCurrentFamily(userId);
        }
        // 校验用户是否为该家庭成员
        Long count = familyMemberMapper.selectCount(
                new LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getFamilyId, familyId)
                        .eq(FamilyMember::getUserId, userId));
        if (count == 0) {
            // 不是成员，回退到第一个
            return getCurrentFamily(userId);
        }
        return familyMapper.selectById(familyId);
    }

    @Override
    public FamilyDetailResponse switchFamily(Long userId, Long familyId) {
        // 校验用户是否为该家庭成员
        Long count = familyMemberMapper.selectCount(
                new LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getFamilyId, familyId)
                        .eq(FamilyMember::getUserId, userId));
        if (count == 0) {
            throw new BusinessException("您不是该家庭的成员");
        }
        return getFamilyDetail(familyId);
    }

    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
