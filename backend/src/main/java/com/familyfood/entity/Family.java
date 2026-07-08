package com.familyfood.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 家庭空间表
 */
@Data
@TableName("family")
public class Family {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建者用户ID */
    private Long ownerUserId;

    /** 家庭名称 */
    private String name;

    /** 邀请码 */
    private String inviteCode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
