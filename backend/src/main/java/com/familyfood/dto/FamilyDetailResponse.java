package com.familyfood.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FamilyDetailResponse {
    private Long id;
    private String name;
    private String inviteCode;
    private Long ownerUserId;
    private LocalDateTime createTime;
    private List<MemberInfo> members;

    @Data
    public static class MemberInfo {
        private Long userId;
        private String nickname;
        private Integer role;
        private LocalDateTime joinTime;
    }
}
