package com.familyfood.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜品表
 */
@Data
@TableName("dish")
public class Dish {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 家庭ID */
    private Long familyId;

    /** 创建者用户ID */
    private Long creatorUserId;

    /** 菜品名称 */
    private String name;

    /** 分类编码 */
    private String categoryCode;

    /** 难度：quick/normal/hard */
    private String difficulty;

    /** 菜品图片URL */
    private String imageUrl;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
