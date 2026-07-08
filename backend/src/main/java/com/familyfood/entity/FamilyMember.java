package com.familyfood.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 家庭成员表
 */
@Data
@TableName("family_member")
public class FamilyMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 家庭ID */
    private Long familyId;

    /** 用户ID */
    private Long userId;

    /** 在家庭中的昵称 */
    private String nickname;

    /** 角色：0=成员, 1=管理员 */
    private Integer role;

    /** 加入时间 */
    private LocalDateTime joinTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
